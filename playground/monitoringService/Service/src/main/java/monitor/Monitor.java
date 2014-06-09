package monitor;



import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import monitor.testCalls.TestCall;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * this class will monitor testbeds
 * @author Andreas De Lille
 */
public class Monitor {

    private Properties prop;
    private WebServiceAccess webAccess;
    private ExecutorService threadPool;

    public static void main(String[] args) throws IOException {
        //while (true){
            new Monitor(args);
        /*    try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
            }
        }*/
    }
/**
 * prints documentation when something is wrong.
 * @param options Options used by the program
 */
    public static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, "monitorService [options ... ]", "Options:", options, "");
        System.exit(0);
    }
/**
 * Creates a monitorobject and runs all the tests on multiple threads
 * @param args
 * @throws IOException 
 */
    public Monitor(String[] args) throws IOException {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("threads")
                .withDescription("Amount of threads to use. Default is the number of availableProcessors.")
                .hasArg()
                .withArgName("number of threads")
                .create("n"));
        options.addOption(OptionBuilder.withLongOpt("test-name")
                .withDescription("Limit tests by name(s). Multiple values should be separated by ',' e.g. testname1,testname2.")
                .hasArg()
                .withArgName("testname(s)")
                .create("tn"));
        options.addOption(OptionBuilder.withLongOpt("test-definition-name")
                .withDescription("Limit tests by definitionname(s). Multiple values should be separated by ',' e.g. testtype1,testtype2.")
                .hasArg()
                .withArgName("testdefinitionname(s)")
                .create("tdn"));
        options.addOption(OptionBuilder.withLongOpt("test-instance-id")
                .withDescription("Limit tests by instance id(s). Multiple values should be separated by ',' e.g. 17,31.")
                .hasArg()
                .withArgName("testinstanceid")
                .create("tid"));
        options.addOption(OptionBuilder.withLongOpt("param")
                .withDescription("Limit tests by parameter value (testbed, user, ...) . Multiple values should be separated by ',' e.g. testbed1,testbed2. or ftester,user2")
                .hasArg()
                .withArgName("param")
                .create("p"));
        options.addOption(OptionBuilder.withLongOpt("help")
                .withDescription("print help message")
                .create("h"));

        CommandLine line = null;
        CommandLineParser parser = new BasicParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException ex) {
            help(options);
        }

        if (line.hasOption("help")) {
            help(options);
        } else {
            //parse args
            //default values
            int threadCount = Integer.parseInt(line.getOptionValue("threads", Runtime.getRuntime().availableProcessors()*2 + 1+""));
            String testnames = line.getOptionValue("test-name","ALL");
            String testdefnames = line.getOptionValue("test-definition-name","ALL");
            String testinstances = line.getOptionValue("test-instance-id","ALL");
            String testbeds = line.getOptionValue("testbed","ALL");
            
            //load properties
            //no more config file not found error ! :)
            this.prop = getProp();

            //create webAccess
            this.webAccess = new WebServiceAccess(prop);

            //get Tests
            Queue<TestCall> tasks = webAccess.getScheduledTests(testnames,testdefnames,testbeds,testinstances);
            System.out.println("Run at: " + (new Date()).getTime() + " Tasks in queue: " + tasks.size() );
            if (tasks != null) {
                //create thread pool
                threadPool = Executors.newFixedThreadPool(threadCount);
                while (!tasks.isEmpty()) {
                    
                    TestCall test = tasks.poll();
                    if (test.getTest().isEnabled() && test.getTest().isScheduled()) { //=> webservice will determine if the test is scheduled
                        System.out.println("Starting " + test.getTest().getTestname() + " at:" + (new Date()).getTime() + " Tasks left in queue: " + tasks.size() );
                        threadPool.submit(test);
                        try {
                            Thread.sleep(1000); //avoid loadtest by running all calls at once
                        } catch (InterruptedException ex) {
                        }
                    }else{
                        System.out.println(test.getTest().getTestname() + " doesn't need to run yet and/or is disabled, skipping.");
                    }
                }
                try {
                    //wait for all tasks to be complete
                    threadPool.shutdown();
                    threadPool.awaitTermination(1 , TimeUnit.HOURS);
                    
                    Thread.sleep(1000);
                    
                    webAccess.shutDownOnUploadComplete();
                } catch (InterruptedException ex) {
                }
                System.out.println("Execution complete");
                System.exit(0);
            } else {
                System.out.println("Something went wrong while contacting the webService. Check your connection and try again.");
                System.exit(-1);
            }
        }
    }
    /**
     * returns the properties
     * @return 
     */
    public static Properties getProp() {
        //String serviceUrl = "http://localhost/longrun/service/index.php/";
        String serviceUrl = "http://localhost/service/index.php/";
        Properties prop = new Properties();
        prop.setProperty("urlTestInstances", serviceUrl + "testInstance");//testdefinitionname=loginGen");
        prop.setProperty("urlTestbeds", serviceUrl + "testbed");
        prop.setProperty("urlTestDefinitions", serviceUrl + "testDefinition");
        prop.setProperty("urlAddResult", serviceUrl + "addResult");
        prop.setProperty("urlUpdateNextRun", serviceUrl + "updateNextRun");
        prop.setProperty("urlUsers", serviceUrl + "user");
        prop.setProperty("outputDir", "results/");
        //prop.setProperty("authCertDir",System.getProperty("user.home") + "/.auth/getsslcert.txt");
        //prop.setProperty("authFilePass","");
        return prop;
    }
}
