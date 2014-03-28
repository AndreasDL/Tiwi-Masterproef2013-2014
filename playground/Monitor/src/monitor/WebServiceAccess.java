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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.Testbed;
import monitor.model.TestDefinition;

public class WebServiceAccess {

    private Gson g;

    public WebServiceAccess() {
        this.g = new Gson();
    }

    //TODO configuratie file
    public HashMap<String, TestInstance> getTestInstances() {
        TestInstanceResults t = null;
        try {
            String jsonText = getFromURL("http://localhost/service/index.php/testInstance");

            //parse json string
            t = g.fromJson(jsonText, TestInstanceResults.class);
            /*
            for (TestInstance ti : t.getData().values()){
                System.out.println(ti.getTestname());
                HashMap<String,ArrayList<String>> h = ti.getParameters();
                for (String key : h.keySet()){
                    System.out.println("\t" + key);
                    for (String val : h.get(key)){
                        System.out.println("\t\t" + val);
                    }
                }
            }*/

        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (t == null) ? null : t.getData();
    }
    public HashMap<String, Testbed> getTestBeds() {
        TestbedResults t = null;
        try {
            String jsonText = getFromURL("http://localhost/service/index.php/testbed");
            //System.out.println(jsonText);

            //parse json string
            t = g.fromJson(jsonText, TestbedResults.class);
            /*System.out.println(t.getData().size());
            for (TestBed te : t.getData().values()) {
                System.out.println(te.getName());
            }*/
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (t == null) ? null : t.getData();
    }
    public HashMap<String, TestDefinition> getTestDefinitions(){
        TestDefinitionResults t = null;
        try {
            String jsonText = getFromURL("http://localhost/service/index.php/testDefinition");
            //System.out.println(jsonText);

            //parse json string
            t = g.fromJson(jsonText, TestDefinitionResults.class);
            /*System.out.println(t.getData().size());
            for (TestDefinition te : t.getData().values()) {
                System.out.println(te.getTestcommand());
                for (String s : te.getParameters().keySet()){
                    System.out.println(s);
                }
            }*/

        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (t == null) ? null : t.getData();
    }
    
    private static String getFromURL(String url) throws MalformedURLException, IOException {
        URL ur = new URL(url);
        InputStream is = ur.openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }




private class TestbedResults {
    private int status;
    private String msg;
    private HashMap<String,Testbed> data;

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public HashMap<String, Testbed> getData() {
        return data;
    }
    
}
private class TestInstanceResults {
    private int Status;
    private String msg;
    private HashMap<String,TestInstance> data;

    public HashMap<String, TestInstance> getData() {
        return data;
    }

    public int getStatus() {
        return Status;
    }

    public String getMsg() {
        return msg;
    }
}
private class TestDefinitionResults{
    private int status;
    private String msg;
    private HashMap<String,TestDefinition> data;

        public int getStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }

        public HashMap<String, TestDefinition> getData() {
            return data;
        }
    
}
}
