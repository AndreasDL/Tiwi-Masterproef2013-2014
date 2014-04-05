/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import monitor.model.TestInstance;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import monitor.model.Testbed;
import monitor.model.TestDefinition;
import monitor.model.TestForExecution;
import monitor.model.TestResult;

public class WebServiceAccess {

    private Gson g;
    private HashMap<String, Testbed> testbeds;
    private HashMap<String, TestDefinition> testDefinitions;
    //TODO configuratie file

    public WebServiceAccess() {
        this.g = new Gson();
        updateCache();
    }

    public ArrayList<TestForExecution> getTests() {
        //pingstest only atm => limited in testinstance
        ArrayList<TestForExecution> tests = new ArrayList<>();

        Pattern p = Pattern.compile("<([^>]*)>");
        for (String instanceId : getTestInstances().keySet()) {
            TestInstance ti = getTestInstances().get(instanceId);
            ti.setInstanceId(instanceId);

            //parse command
            StringBuffer stibu = new StringBuffer();
            Matcher m = p.matcher(testDefinitions.get(ti.getTesttype()).getTestcommand());
            while (m.find()) {
                //get values
                m.appendReplacement(stibu, getParamValue(ti, m.group(1)));
            }
            m.appendTail(stibu);

            TestForExecution t = new TestForExecution(stibu.toString(), ti);
            tests.add(t);
        }
        return tests;
    }

    public HashMap<String, TestInstance> getTestInstances() {
        TestInstanceResults t = null;
        try {
            //TODO alle tests
            String jsonText = getFromURL("http://localhost/service/index.php/testInstance?testtype=ping,login");

            //parse json string
            t = g.fromJson(jsonText, TestInstanceResults.class);
            /*
             for (TestInstance ti : t.getData().values()) {
             System.out.println(ti.getTestname());
             HashMap<String, ArrayList<String>> h = ti.getParameters();
             for (String key : h.keySet()) {
             System.out.println("\t" + key);
             for (String val : h.get(key)) {
             System.out.println("\t\t" + val);
             }
             }
             }
             */
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

    public HashMap<String, TestDefinition> getTestDefinitions() {
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

    public void addResult(TestResult result, TestForExecution t) {
        try {
            URL url = new URL("http://localhost/service/index.php/addResult");
            StringBuilder postData = new StringBuilder();
            postData.append("testinstanceid="+ t.getTest().getInstanceId());
            for (String key : result.getResults().keySet()) {
                try {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(key, "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(result.getSubResult(key),"UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.print("Send: " + postData.toString() + "\t");
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            for (int c; (c = in.read()) >= 0; System.out.print((char)c));
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateCache() {
        //cache => testbeds and testdefinitions
        this.testbeds = getTestBeds();
        this.testDefinitions = getTestDefinitions();
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

    private String getParamValue(TestInstance t, String propId) {
        //parse param
        String[] s = propId.split("\\.");
        String ret = null;
        TestDefinition def = testDefinitions.get(t.getTesttype());
        //System.out.println(s[0]);
        String paramType = def.getParameters().get(s[0]).get("type");

        if (s.length > 1) {
            //use property of parameter and not parameter itself
            if (paramType.equals("testbed")) {
                if (s[1].equals("url")) {
                    for (String testbed : t.getParameters().get("testbed")) {
                        ret = testbeds.get(testbed).getUrl();
                    }
                } else if (s[1].equals("urn")) {
                    for (String testbed : t.getParameters().get("testbed")) {
                        ret = testbeds.get(testbed).getUrn();
                    }
                }
            }
        } else {
            ret = t.getParameters().get(s[0]).get(0);
            String fileName = "/home/drew/masterproef/site/service/output/context-file.txt";
            if (paramType.equals("file")) {
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(fileName, "UTF-8");
                    writer.print(ret);
                    writer.close();
                    ret = fileName;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    writer.close();
                }
                
            }
            
        }

        return ret;
    }

    //needed for json extraction
    private class TestbedResults {

        private int status;
        private String msg;
        private HashMap<String, Testbed> data;

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
        private HashMap<String, TestInstance> data;

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

    private class TestDefinitionResults {

        private int status;
        private String msg;
        private HashMap<String, TestDefinition> data;

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
