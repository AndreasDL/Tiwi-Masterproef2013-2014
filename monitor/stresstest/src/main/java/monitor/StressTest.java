package monitor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
 * this class will pull a test then run it simultaneously on multiple threads to simulate heavy load on a testbed
 * @author Andreas De Lille
 */
public class StressTest {
    private Properties prop;
    private WebServiceAccess webAccess;
    private ExecutorService threadPool;
    
    public static void main(String[] args) {
        new StressTest(args);
    }

    public StressTest(String[] args) {
        stressTest(args);
    }
    /**
 * prints documentation when something is wrong.
 * @param options Options used by the program
 */
    public static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, "stressTest [options ... ]", "Options:", options, "");
        System.exit(0);
    }
    
    
    public void stressTest(String[] args) {
        Options options = new Options();
        //options.addOption(OptionBuilder.withLongOpt("test-types"))

        options.addOption(OptionBuilder.withLongOpt("number-of-tests")
                .withDescription("Amount of tests to run, each test runs on it's own thread")
                .hasArg()
                .withArgName("number of tests")
                .isRequired()
                .create("n"));
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
        options.addOption(OptionBuilder.withLongOpt("configfile")
                .withDescription("location of the proprerty file containing the configuration. Default ~/.jFed/config")
                .hasArg()
                .create("conf"));

        CommandLine line = null;
        CommandLineParser parser = new BasicParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException ex) {
            help(options);
        }

        int aantal = Integer.parseInt(line.getOptionValue("number-of-tests"));
        int waitTime = Integer.parseInt(line.getOptionValue("wait-time"));
        String testName = line.getOptionValue("test-name");
        waitTime *= 1000;//to seconds
        String configfile = line.getOptionValue("configfile","~/.jFed/config");
        
        prop = new Properties();
        try{
            prop.load(new FileInputStream(configfile));
        }catch (Exception e){
            System.out.println("Config file not found");
            System.exit(-2);
        }
        
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
            } catch (InterruptedException ex) {
                
            }
            System.out.println("Execution complete");
            System.exit(0);
        } else {
            System.out.println("Test " + testName + " not found!\n Check your connection and check for typo's and try again");
            help(options);
            System.exit(-1);
        }
    }
}