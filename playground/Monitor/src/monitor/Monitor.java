
package monitor;

/**
 *
 * @author drew
 */
public class Monitor {
    private WebServiceAccess webAccess;
    
    public static void main(String[] args) {
        new Monitor();
        //kleine hoeveelheid data veel nodig => caches bij opstarten?
        //testdefinities cachen
        //testbeds caches
        
        //vraag ping test op van webservice
        
        //zet params goed 
        
        //voor uit
                
        //parse
        
        //stuur terug
    }

    public Monitor() {
        webAccess = new WebServiceAccess();
        webAccess.getTests();
    }
}
