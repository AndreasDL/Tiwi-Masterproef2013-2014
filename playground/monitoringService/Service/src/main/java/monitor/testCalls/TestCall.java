/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.testCalls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import monitor.ResultUploader;
import monitor.WebServiceAccess;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public abstract class TestCall implements Runnable{
    private final String outputDir;

    protected final TestInstance test;
    protected final TestDefinition testDefinition;
    protected final HashMap<String, Testbed> testbeds;
    private String testOutputDir;
    protected final Properties prop;
    protected ResultUploader resultUploader;
    protected int seqNumber;
    protected boolean seqNumberSet;
    protected long start;


    public TestCall(ResultUploader resultUploader,TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop) {
        this.resultUploader = resultUploader;
        this.test = test;
        this.testDefinition = testDefinition;
        this.testbeds = testbeds;
        this.prop = prop;
        this.outputDir = prop.getProperty("outputDir");
        this.seqNumberSet = false;
        this.start = System.currentTimeMillis();
    }

    
    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
        seqNumberSet = true;
    }
    
    public TestDefinition getTestDefinition() {
        return testDefinition;
    }
    public TestInstance getTest() {
        return test;
    }
    public String getTestDefinitionName() {
        return test.getTestDefinitionName();
    }

    public long getStart(){
        return start; // 1000; //we only want seconds & not milliseconds
    }
    @Override
    public abstract void run();
    protected abstract ArrayList<String> getParameters(String parsedCommand);
    
    protected String makeTestOutputDir() {
        if(testOutputDir == null){
            Calendar now = Calendar.getInstance();
            testOutputDir = outputDir + test.getTestDefinitionName() + "/"+ test.getTestInstanceId() +"/";
            if (seqNumberSet){
                testOutputDir += "seq" + this.seqNumber + "/";
            }
            
            testOutputDir += now.get(Calendar.YEAR) + "/"
                + now.get(Calendar.MONTH) + "/"
                + now.get(Calendar.DAY_OF_MONTH) + "/"
                + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND)
                + "." + now.get(Calendar.MILLISECOND) + "/";
        (new File(testOutputDir)).mkdirs();
        }
        //System.out.println(testOutputDir);
        return testOutputDir;
    }
    protected String prepare(String testOutputDir) {
        String ret = parse(testDefinition.getTestcommand());
        return ret;
    }
    public String parse(String text){
        Pattern p = Pattern.compile("<([^>]*)>");
        //parse command
        StringBuffer stibu = new StringBuffer();
        Matcher m = p.matcher(text);
        while (m.find()) {
            //get values
            m.appendReplacement(stibu, getParamValue(m.group(1)));
            //System.out.println(m.group(1) +" => " + getParamValue(m.group(1)));
        }
        m.appendTail(stibu);

        return stibu.toString();
    }
    protected TestResult handleResults(String consoleOutput,int returnValue){
        PrintWriter writer = null;
        TestResult t = new TestResult(test);
        try {
            //t.addSubResult("testInstanceId", test.getInstanceId());
            //will be taken care of when adding the result(webserviceAccess.addResult), here only the subvalues
            String fileName = testOutputDir + "console.log";
            writer = new PrintWriter(fileName, "UTF-8");
            writer.write(consoleOutput);
            writer.close();
            t.addSubResult("log",fileName);
            t.addSubResult("returnValue",returnValue + "");
            t.addSubResult("startTime",getStart()+ ""); 
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestCall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TestCall.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
        return t;
    }
    protected String getParamValue(String propId) {
        //laatste testbed ipv meerdere
        
        //parse param
        String[] s = propId.split("\\.");
        String ret = null;
        //TestDefinition def = testDefinitions.get(t.getTesttype());
        //System.out.println(s[0]);
        String paramType = testDefinition.getParameters().get(s[0]).get("type");
        if (s.length > 1) {
            //use property of parameter and not parameter itself
            //if (paramType.equals("testbed")) {
                if (s[1].equals("url")) {
                    for (String testbed : test.getParameters().get(s[0])) {
                        ret = testbeds.get(testbed).getUrl();
                    }
                } else if (s[1].equals("urn")) {
                    for (String testbed : test.getParameters().get(s[0])) {
                        ret = testbeds.get(testbed).getUrn();
                    }
                }
            //}
        } else {
            if (paramType.equals("file")) {
                String fileName = testOutputDir + "context-file.txt";
                ret = parse(testDefinition.getParameters().get(s[0]).get("description"));
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(fileName, "UTF-8");
                    writer.print(ret);
                    writer.close();
                   /* try {
                        Thread.sleep(4000);
                    } catch (InterruptedException ex) {
                    }*/
                    ret = fileName;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(WebServiceAccess.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    writer.close();
                }
            }else if(s[0].equals("output-dir")){
                ret = makeTestOutputDir();
                
            }else{
                ret = test.getParameters().get(s[0]).get(0);
            }

        }

        return ret;
    }

    public ResultUploader getResultUploader() {
        return resultUploader;
    }
    
    
}