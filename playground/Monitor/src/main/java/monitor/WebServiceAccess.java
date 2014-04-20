/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import monitor.model.TestInstance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.Testbed;
import monitor.model.TestDefinition;
import monitor.testCalls.TestCall;
import monitor.testCalls.TestCallFactory;
import java.util.Properties;
import java.util.Queue;
import monitor.model.TestResult;

public class WebServiceAccess {

    private Gson g;
    private HashMap<String, Testbed> testbeds;
    private HashMap<String, TestDefinition> testDefinitions;
    private Properties prop;
    private Thread uploader;
    private ResultUploader resultUploader;

    public WebServiceAccess(Properties prop) {
        this.prop = prop;
        //this.g = new Gson();
        this.g = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        updateCache();
        //make thread result submission
        this.resultUploader = new ResultUploader(this);
        uploader = new Thread(resultUploader);
        uploader.start();
    }

    public void shutDownOnUploadComplete() {
        resultUploader.stop();
    }

    public Queue<TestCall> getTests() {
        Queue<TestCall> tests = new LinkedList<>();
        HashMap<String, TestInstance> testInstances = getTestInstances();

        for (String id : testInstances.keySet()) {
            TestInstance ti = testInstances.get(id);
            ti.setTestInstanceId(id);
            TestCall t = TestCallFactory.makeTest(resultUploader, ti, testDefinitions.get(ti.getTestDefinitionName()), testbeds, prop);
            tests.add(t);
        }
        return tests;
    }

    public TestCall getTestByName(String name) {
        HashMap<String, TestInstance> testInstances = null;
        try {
            String jsonText = getFromURL(prop.getProperty("urlTestInstances") + "?testname=" + name);

            //parse json string
            TestInstanceResults t = g.fromJson(jsonText, TestInstanceResults.class);
            testInstances = t.getData();
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }

        TestCall t = null;
        //indien iemand meerdere tests opgeeft, enkel laatste teruggeven & niet vastlopen
        for (String id : testInstances.keySet()) {
            TestInstance ti = testInstances.get(id);
            ti.setTestInstanceId(id);
            t = TestCallFactory.makeTest(resultUploader, ti, testDefinitions.get(ti.getTestDefinitionName()), testbeds, prop);
        }
        return t;
    }

    public HashMap<String, TestInstance> getTestInstances() {
        TestInstanceResults t = null;
        try {
            //TODO alle tests
            String jsonText = getFromURL(prop.getProperty("urlTestInstances"));

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
            String jsonText = getFromURL(prop.getProperty("urlTestbeds"));
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
            String jsonText = getFromURL(prop.getProperty("urlTestDefinitions"));
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

    public void addResult(TestResult result) {
        try {
            URL url = new URL(prop.getProperty("urlAddResult"));
            StringBuilder postData = new StringBuilder();
            postData.append("testinstanceid=").append(result.getTestInstance().getTestInstanceId());
            for (String key : result.getResults().keySet()) {
                try {
                    if (postData.length() != 0) {
                        postData.append('&');
                    }
                    postData.append(URLEncoder.encode(key, "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(result.getSubResult(key), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("Send: " + postData.toString());
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            for (int c; (c = in.read()) >= 0; System.out.print((char) c));
            System.out.println("");
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

    public void updateLastRun(TestResult result) {
        try {
            URL url = new URL(prop.getProperty("urlUpdateLastRun"));
            StringBuilder postData = new StringBuilder();
            postData.append("testinstanceid=").append(result.getTestInstance().getTestInstanceId());
            postData.append("&lastrun=").append(result.getSubResult("startTime"));//current time
            System.out.println("UpdateTime: " + postData.toString());
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            for (int c; (c = in.read()) >= 0; System.out.print((char) c));
            System.out.println("");
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

    public Thread getUploadThread(){
        return uploader;
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
