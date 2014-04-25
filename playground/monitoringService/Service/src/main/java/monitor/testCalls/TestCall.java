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
    boolean isLoadTest;

/**
 * This class represents a testcall. The differenct testCalls are bashTestcall => runs a bash command/script.
 * automated tester => calls the automated tester.jar
 * @param resultUploader The resultuploader which will run on a different thread uploading results one by one while tests are executed.
 * @param test The testInstance associated with the call.
 * @param testDefinition The testDefinition associated with the testInstance.
 * @param testbeds Hashmap of all known testbeds for lookups.
 * @param prop The properties set in the mainclass.
 * @param isLoadTest Marks test as loadtest . Loadtests ignore scheduling & nextrun. When this value is set to true, the resultuploaders wont change the nextrun value.
 */
    public TestCall(ResultUploader resultUploader,TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop,boolean isLoadTest) {
        this.resultUploader = resultUploader;
        this.test = test;
        this.testDefinition = testDefinition;
        this.testbeds = testbeds;
        this.prop = prop;
        this.outputDir = prop.getProperty("outputDir");
        this.seqNumberSet = false;
        this.start = System.currentTimeMillis();
        this.isLoadTest = isLoadTest;
    }

    /**
     * Set the sequence number of the test, for loadtests. 
     * This parameter must be set in order to avoid that a loadTest has 2 same outputDirectories.
     * They are made while running and since they run simultaneously, the timestamp may be the same thus overwriting results.
     * @param seqNumber The sequence number of the loadtest.
     */
    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
        seqNumberSet = true;
    }
    
    /**
     * Returns the testDefinitions associated with the testcall.
     * @return 
     */
    public TestDefinition getTestDefinition() {
        return testDefinition;
    }
    
    /**
     * Returns the testInstance associated with the testcall.
     * @return 
     */
    public TestInstance getTest() {
        return test;
    }
    /**
     * Returns the testDefinitionName of the testDefinition associated with the testcall.
     * @return 
     */
    public String getTestDefinitionName() {
        return test.getTestDefinitionName();
    }
/**
 * Gets the start time of the test. The time is formatted as a unix timestamp (epoch time)
 * @return milliseconds since 1 jan 1970 .
 */
    public long getStart(){
        return start; // 1000; //we only want seconds & not milliseconds
    }
    /**
     * Runs the testcall.
     */
    @Override
    public abstract void run();
    
    /**
     * Method that returns the parameters of the test.
     * @param parsedCommand The command from testDefinition parsed by the parse function
     * @return The list of parameters.
     */
    protected abstract ArrayList<String> getParameters(String parsedCommand);
    
    /**
     * Makes an output directory to store the output files generated be the testcall.
     * @return 
     */
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
    /**
     * Prepares the command & creates an outputDirectory
     * @return the parsed command.
     */
    protected String prepare() {
        String ret = parse(testDefinition.getTestcommand());
        return ret;
    }
    /**
     * parses the giving text. Makes output directory if needed. Also used for parsing the contextfile/
     * @param text inputText to be parsed
     * @return the parsed value
     */
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
    /**
     * Handle the results, writes consoleOutput to testOutputDir/console.log
     * @param consoleOutput The output generated by the command.
     * @param returnValue The TestResult.
     * @return 
     */
    protected TestResult handleResults(String consoleOutput,int returnValue){
        PrintWriter writer = null;
        TestResult t = new TestResult(test,isLoadTest);
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
    /**
     * Returns the values associated with the parameter. e.g. testbed.urn will return the urn associated with the testbed.
     * if the parametertype is a file, the file will be created in outputDir & the path will be returned.
     * @param propId the property e.g. testbed.urn or context-file
     * @return the value of the parameter
     */
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

    /**
     * Returns the resultUploader used for uploading the testresult.
     * @return 
     */
    protected ResultUploader getResultUploader() {
        return resultUploader;
    }
    
    
}
