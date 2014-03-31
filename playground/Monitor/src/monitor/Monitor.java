package monitor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.TestForExecution;

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
        //chache testbeds & testdefinitions
        //i don't want to call the service all the time and i don't wan't to make complexer view too so, caching suddenly seems like a good idea.

        List<TestForExecution> tests = webAccess.getTests();
        for (int i = 0; i < tests.size(); i++) {
            //System.out.println(tests.get(i).getCommand());
            
            try {
                //System.out.println(i + "  " + tests.get(i).run());
                System.out.println(tests.get(i).run());
            } catch (IOException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
