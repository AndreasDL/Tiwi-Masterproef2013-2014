package monitor;

import monitor.ExecutableTests.ExecutableTest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.TestResult;

/**
 *
 * @author drew
 */
public class Monitor {

    private WebServiceAccess webAccess;
    private Properties prop;

    public static void main(String[] args) throws IOException {
        new Monitor();
    }

    public Monitor() throws IOException {
        this.prop = new Properties();
        prop.load(new FileReader("config.properties"));
        this.webAccess = new WebServiceAccess(prop);

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
