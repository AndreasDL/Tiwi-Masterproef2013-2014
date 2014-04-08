package monitor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ExecutableTests.ExecutableTest;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import monitor.model.TestResult;

/**
 *
 * @author drew
 */
public class Monitor {

    private WebServiceAccess webAccess;

    public static void main(String[] args) {
        new Monitor();
    }

    public Monitor() {
        this.webAccess = new WebServiceAccess();

        for(ExecutableTest test : webAccess.getTests()){
            try {
                TestResult r = test.run();
                webAccess.addResult(r, test);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
