package monitor;

import java.io.IOException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.testCalls.TestCall;
import monitor.testCalls.TestCallFactory;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author drew
 */
public class Monitor {

    private Properties prop;
    private WebServiceAccess webAccess;
    private ExecutorService threadPool;

    public static void main(String[] args) throws IOException {
        new Monitor(args);
    }

    public static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, "Monitor [options ... ]", "Options:", options, "");
        System.exit(0);
    }

    public Monitor(String[] args) throws IOException {
        //stressTest(args);
        monitorService(args);
    }

    public void monitorService(String[] args) {

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
        options.addOption(OptionBuilder.withLongOpt("testbed")
                .withDescription("Limit tests by testbed(s). Multiple values should be separated by ',' e.g. testbed1,testbed2.")
                .hasArg()
                .withArgName("testbed")
                .create("tb"));
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
            int threadCount = Integer.parseInt(line.getOptionValue("threads", Runtime.getRuntime().availableProcessors()+""));
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
            Queue<TestCall> tasks = webAccess.getTests(testnames,testdefnames,testbeds,testinstances);
            if (tasks != null) {
                //create thread pool
                threadPool = Executors.newFixedThreadPool(threadCount);
                while (!tasks.isEmpty()) {
                    TestCall test = tasks.poll();
                    //System.out.println(test.getTest().getTestname());
                    if (test.getTest().isEnabled() && test.getTest().isScheduled()) {
                        threadPool.submit(test);
                    }
                }
                try {
                    //wait for all tasks to be complete
                    threadPool.shutdown();
                    threadPool.awaitTermination(20, TimeUnit.SECONDS);

                    webAccess.shutDownOnUploadComplete();
                    //System.exit(0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("Something went wrong while contacting the webService. Check your connection and try again.");
            }
        }
    }

    public void stressTest(String[] args) {
        Options options = new Options();
        //options.addOption(OptionBuilder.withLongOpt("test-types"))

        options.addOption(OptionBuilder.withLongOpt("number-of-tests")
                .withDescription("Amount of tests to run, each test runs on it's own thread")
                .hasArg()
                .withArgName("number of tests")
                .isRequired()
                .create("N"));
        options.addOption(OptionBuilder.withLongOpt("wait-time")
                .withDescription("time in between tests calls in seconds")
                .hasArg()
                .withArgName("time in between")
                .isRequired()
                .create("t"));
        options.addOption(OptionBuilder.withLongOpt("test-name")
                .withDescription("test to run multiple times")
                .hasArg()
                .withArgName("test for multiple runs")
                .isRequired()
                .create("tn"));

        CommandLine line = null;
        CommandLineParser parser = new BasicParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException ex) {
            help(options);
        }

        //load properties
        //no more config file not found error ! :)
        this.prop = getProp();

        int aantal = Integer.parseInt(line.getOptionValue("number-of-tests"));
        int waitTime = Integer.parseInt(line.getOptionValue("wait-time"));
        String testName = line.getOptionValue("test-name");
        waitTime *= 1000;//to seconds

        //create webAccess
        this.webAccess = new WebServiceAccess(prop);

        //getTest
        final TestCall test = webAccess.getTestByName(testName);
        if (test != null) {
            //create thread pool
            threadPool = Executors.newFixedThreadPool(aantal);

            for (int i = 0; i < aantal; i++) {
                TestCall t = TestCallFactory.copyTest(test);
                t.setSeqNumber(i);
                threadPool.submit(t);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                }
            }
            try {
                //wait for all tasks to be complete
                threadPool.shutdown();
                threadPool.awaitTermination(1, TimeUnit.DAYS);
                webAccess.shutDownOnUploadComplete();
                //System.exit(0);
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Test " + testName + " not found!\n Check your connection and check for typo's and try again");
            help(options);
        }
    }

    public Properties getProp() {
        Properties prop = new Properties();
        prop.setProperty("urlTestInstances", "http://localhost/service/index.php/testInstance");//testdefinitionname=loginGen");
        prop.setProperty("urlTestbeds", "http://localhost/service/index.php/testbed");
        prop.setProperty("urlTestDefinitions", "http://localhost/service/index.php/testDefinition");
        prop.setProperty("urlAddResult", "http://localhost/service/index.php/addResult");
        prop.setProperty("urlUpdateLastRun", "http://localhost/service/index.php/updateLastRun");
        prop.setProperty("outputDir", "results/");
        prop.setProperty("authFileDir", "~/.auth/authorities.xml");
        return prop;
    }
}
