/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class webServiceAccess {
    public ArrayList<Test> getTests(){
        try {
            //get json string
            URL url = new URL("http://localhost/service/index.php/testInstance");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            
            
            //parse json string
            
            //run test
            
            //send to database
            
            
            return null;
        } catch (MalformedURLException ex) {
            Logger.getLogger(webServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(webServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
