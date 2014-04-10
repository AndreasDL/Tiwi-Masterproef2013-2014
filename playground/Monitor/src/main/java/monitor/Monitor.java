package monitor;

import monitor.testCalls.TestCall;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.TestResult;

/**
 *
 * @author drew
 */
public class Monitor {
    
    private Properties prop;
    private WebServiceAccess webAccess;
    private ExecutorService threadPool;

    public static void main(String[] args) throws IOException {
        new Monitor();
    }

    public Monitor() throws IOException {
        //load properties
        this.prop = new Properties();
        prop.load(new FileReader("config.properties"));
        
        //create webAccess
        this.webAccess = new WebServiceAccess(prop);
        
        //create thread pool
        threadPool = Executors.newFixedThreadPool(10);
        
        //threading!!
        Set<TestCall> tasks = webAccess.getTests();
        Set<Future<TestResult>> set = new HashSet<>();
        for(TestCall test : tasks){
            set.add(threadPool.submit(test));
        }
        
        for(Future<TestResult> r : set){
            try {
                webAccess.addResult(r.get());
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //threadpool stays running
        System.exit(0);
    }
}
