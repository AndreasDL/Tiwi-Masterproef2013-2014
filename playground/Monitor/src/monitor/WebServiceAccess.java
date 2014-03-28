/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import monitor.model.TestInstance;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.InstanceResults;
import monitor.model.Results;

public class WebServiceAccess {
    //TODO configuratie file

    public ArrayList<TestInstance> getTests() {
        try {
            //get json string
            URL url = new URL("http://localhost/service/index.php/testInstance");
            InputStream is = url.openStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            //System.out.println(jsonText);
            
            //parse json string
            Gson g = new Gson();
            
            InstanceResults t = g.fromJson(jsonText,InstanceResults.class);
            System.out.println(t.getTests().size());
            for (TestInstance te : t.getTests().values()){
                System.out.println(te.getTestname());
                System.out.println(te.getParameters().size());
                for (HashMap<String,String> p : te.getParameters()){
                    for (String s : p.keySet()){
                        System.out.println(s + " => " + p.get(s));
                    }
                }
            }
            
            /*
            Results<TestInstance> t = g.fromJson(jsonText,Results.class);
            System.out.println(t.getData().size());
            for(String s : t.getData().keySet()){
                System.out.println(s);
                System.out.println(t.getData().get(s));
                
            }
            */
            
            //run test
            //send to database
            return null;
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
