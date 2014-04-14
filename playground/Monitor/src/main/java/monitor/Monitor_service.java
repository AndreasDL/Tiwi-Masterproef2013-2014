/*package monitor;

import monitor.testCalls.TestCall;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 
public class Monitor {
    
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
        options.addOption( OptionBuilder.withLongOpt("threads")
                .withDescription( "Amount of threads to run" )
                .hasArg()
                .withArgName("number of threads")
                .isRequired()
                .create() );
        options.addOption( OptionBuilder.withLongOpt("wait-time")
                .withDescription( "time in between tests calls in seconds" )
                .hasArg()
                .withArgName("time in between")
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
        this.prop = new Properties();
        prop.load(new FileReader("config.properties"));
        
        //create webAccess
        this.webAccess = new WebServiceAccess(prop);
        
        //create thread pool
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        //threadPool = Executors.newFixedThreadPool(/*1);//Integer.parseInt(line.getOptionValue("threads")));
        
        
        //threading!!
        Set<TestCall> tasks = webAccess.getTests();
        
        for(TestCall test : tasks){
            threadPool.submit(test);
            try {
                Thread.sleep(Integer.parseInt(line.getOptionValue("wait-time")));
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

*/