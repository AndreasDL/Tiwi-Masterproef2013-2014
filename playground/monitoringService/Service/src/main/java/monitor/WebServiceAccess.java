/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import monitor.model.TestInstance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.Testbed;
import monitor.model.TestDefinition;
import monitor.testCalls.TestCall;
import monitor.testCalls.TestCallFactory;
import java.util.Properties;
import java.util.Queue;
import java.util.TimeZone;
import monitor.model.TestResult;
/**
 * this class is used to interact with the webservice.
 * @author Andreas De Lille
 */
public class WebServiceAccess {

    private Gson g;
    private Map<String, Testbed> testbeds;
    private Map<String, TestDefinition> testDefinitions;
    private Properties prop;
    private Thread uploader;
    private ResultUploader resultUploader;
/**
 * Creates a webserviceAccess object
 * @param prop the properties
 */
    public WebServiceAccess(Properties prop) {
        this.prop = prop;
        //this.g = new Gson();
        //this.g = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        this.g = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create();//java 7.0 required
        updateCache();
        //make thread result submission
        this.resultUploader = new ResultUploader(this);
        uploader = new Thread(resultUploader);
        uploader.start();
    }
/**
 * tell the resultuploader to stop when the uploads are complete
 */
    public void shutDownOnUploadComplete() {
        resultUploader.stop();
    }
/**
 * get all testinstances formatted as a testcall.
 * @return 
 */
    public Queue<TestCall> getTests() {
        Queue<TestCall> tests = new LinkedList<>();
        Map<String, TestInstance> testInstances = getTestInstances();

        for (String id : testInstances.keySet()) {
            TestInstance ti = testInstances.get(id);
            ti.setTestInstanceId(id);
            TestCall t = TestCallFactory.makeTest(resultUploader, ti, testDefinitions.get(ti.getTestDefinitionName()), testbeds, prop,false);
            tests.add(t);
        }
        return tests;
    }
    /**
     * get the schedules tests
     * @param name filter tests on testName
     * @param defname filter tests on testDefinitionName
     * @param testbed filter tests on testbeds
     * @param tid filter tests on testinstanceid
     * @return 
     */
    public Queue<TestCall> getScheduledTests(String name,String defname,String testbed,String tid){
        Map<String, TestInstance> t = null;
        Queue<TestCall> tests = new LinkedList<>();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        
        try {
            String url = prop.getProperty("urlTestInstances") 
                    + "?testname=" + name
                    + "&testdefinitionname=" + defname
                    + "&testbed=" + testbed
                    + "&testinstanceid=" + tid
                    + "&nextrun=" + nowAsISO;
            //System.out.println(url);
            String jsonText = getFromURL(url);

            //parse json string
            Type type = new TypeToken<Map<String,TestInstance>>(){}.getType();
            t = g.fromJson(jsonText, type);
            
        } catch (MalformedURLException ex) {
            System.out.println("something wrong with the url");
        } catch (IOException ex) {
            System.out.println("something wrong with io");
        }

        for (String id : t.keySet()) {
            TestInstance ti = t.get(id);
            ti.setTestInstanceId(id);
            TestCall tc = TestCallFactory.makeTest(resultUploader, ti, testDefinitions.get(ti.getTestDefinitionName()), testbeds, prop);
            tests.add(tc);
        }
        return tests;
    }
/**
 * get a single test in loadtestmode by name.
 * @param name the name of the test.
 * @return the testcall associated with the test associated with the name.
 */
    public TestCall getTestByName(String name) {
        //stresstestmode
        Map<String, TestInstance> testInstances = null;
        try {
            String jsonText = getFromURL(prop.getProperty("urlTestInstances") + "?testname=" + name);

            //parse json string
            Type type = new TypeToken<Map<String,TestInstance>>(){}.getType();
            testInstances = g.fromJson(jsonText, type);
            
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
            t = TestCallFactory.makeTest(resultUploader, ti, testDefinitions.get(ti.getTestDefinitionName()), testbeds, prop,true);//true=>loadtest so resultuploader wont update the lastrun
        }
        return t;
    }
/**
 * Get all the testInstances
 * @return hashmap testinstanceid => testinstance
 */
    public Map<String, TestInstance> getTestInstances() {
        Map<String,TestInstance> t = null;
        try {
            //TODO alle tests
            String jsonText = getFromURL(prop.getProperty("urlTestInstances"));

            //parse json string
            Type type = new TypeToken<Map<String,TestInstance>>(){}.getType();
            t = g.fromJson(jsonText, type);

        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }
/**
 * gets the testbeds from the webservice.
 * @return hashmap name=>testbed
 */
    public Map<String, Testbed> getTestBeds() {
        Map<String,Testbed> t = null;
        try {
            String jsonText = getFromURL(prop.getProperty("urlTestbeds"));

            //parse json string
            Type type = new TypeToken<Map<String,Testbed>>(){}.getType();
            t = g.fromJson(jsonText,type);
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return t;
    }
/**
 * Gets the testdefinitions
 * @return hashmap testdefinitionname => testdefinition
 */
    public Map<String, TestDefinition> getTestDefinitions() {
        Map <String,TestDefinition> t = null;
        try {
            String jsonText = getFromURL(prop.getProperty("urlTestDefinitions"));

            //parse json string
            Type type = new TypeToken<Map<String,TestDefinition>>(){}.getType();
            t = g.fromJson(jsonText, type);

        } catch (MalformedURLException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
        }

        return t;
    }
/**
 * post a testresult to the webservice
 * @param result the testresult to post to the webservice.
 */
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
/**
 * Updates the nextrun value of a testinstance. The testinstance is determined by the testresult.
 * @param result the result used to determine the testinstanceid in order to update the nextrun field.
 */
    public void updateNextRun(TestResult result) {
        try {
            URL url = new URL(prop.getProperty("urlUpdateNextRun"));
            StringBuilder postData = new StringBuilder();
            postData.append("testinstanceid=").append(result.getTestInstance().getTestInstanceId());
            long nextrun = Long.parseLong(result.getSubResult("startTime")) + result.getTestInstance().getFrequency()*1000;
            nextrun /= 1000; //naar seconds
            postData.append("&nextrun=").append(nextrun);//current time
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
    /**
     * gets the testbeds & testdefinitions. Stores them in the object. The cache is called automatically on construction. 
     */
    public void updateCache() {
        //cache => testbeds and testdefinitions
        this.testbeds = getTestBeds();
        this.testDefinitions = getTestDefinitions();
    }
/**
 * gets the url page (json) as a string.
 * @param url The url to the page to put in the string
 * @return The text on the webpage
 * @throws MalformedURLException
 * @throws IOException 
 */
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
    
/**
 * returns the resultUploader thread
 * @return 
 */
    public Thread getUploadThread(){
        return uploader;
    }
    
}