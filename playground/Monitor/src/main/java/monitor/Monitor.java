package monitor;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
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
public class Monitor{
    
    private Properties prop;
    private WebServiceAccess webAccess;
    private ExecutorService threadPool;

    public static void main(String[] args) throws IOException {
        new Monitor(args);
    }
    
    public static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200,"Monitor [options ... ]", "Options:", options,"");
        System.exit(0);
    }
    
    
    public Monitor(String[] args) throws IOException {
        Options options = new Options();
        options.addOption( OptionBuilder.withLongOpt("number-of-tests")
                .withDescription( "Amount of tests to run, each test runs on it's own thread" )
                .hasArg()
                .withArgName("number of tests")
                .isRequired()
                .create() );
        options.addOption( OptionBuilder.withLongOpt("wait-time")
                .withDescription( "time in between tests calls in seconds" )
                .hasArg()
                .withArgName("time in between")
                .isRequired()
                .create() );
        options.addOption( OptionBuilder.withLongOpt("test-name")
                .withDescription( "test to run multiple times")
                .hasArg()
                .withArgName("test for multiple runs")
                .isRequired()
                .create() );
        
        CommandLine line = null;
        CommandLineParser parser = new BasicParser();
        try {
            line = parser.parse( options, args );
        } catch (ParseException ex) {
            help(options);
        }
        
        //load properties
        //no more config file not found error ! :)
        this.prop = new Properties();
        prop.setProperty("urlTestInstances","http://localhost/service/index.php/testInstance");
        prop.setProperty("urlTestbeds","http://localhost/service/index.php/testbed");
        prop.setProperty("urlTestDefinitions","http://localhost/service/index.php/testDefinition");
        prop.setProperty("urlAddResult","http://localhost/service/index.php/addResult");
        prop.setProperty("outputDir","results/");
        prop.setProperty("authFileDir","~/.auth/authorities.xml");
        
        //create webAccess
        this.webAccess = new WebServiceAccess(prop);
        
        //create thread pool
        int aantal = Integer.parseInt(line.getOptionValue("number-of-tests"));
        int waitTime = Integer.parseInt(line.getOptionValue("wait-time"));
        waitTime *= 1000;//to seconds
        
        ExecutorService threadPool = Executors.newFixedThreadPool(aantal);
        final TestCall test = webAccess.getTestByName(line.getOptionValue("test-name"));
        
        for (int i = 0; i < aantal ; i++) {
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
            threadPool.awaitTermination(1,TimeUnit.DAYS);
            webAccess.shutDownOnUploadComplete();
            //System.exit(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
