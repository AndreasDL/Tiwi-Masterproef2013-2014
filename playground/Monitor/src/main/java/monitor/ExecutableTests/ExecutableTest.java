/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.ExecutableTests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import monitor.WebServiceAccess;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public abstract class ExecutableTest {

    //TODO properties file
    private final String outputDir;

    private final TestInstance test;
    private final TestDefinition testDefinition;
    private final HashMap<String, Testbed> testbeds;
    private String testOutputDir;
    private Properties prop;
    
    public TestDefinition getTestDefinition() {
        return testDefinition;
    }
    public TestInstance getTest() {
        return test;
    }
    public String getTestType() {
        return test.getTesttype();
    }

    public ExecutableTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop) {
        this.test = test;
        this.testDefinition = testDefinition;
        this.testbeds = testbeds;
        this.prop = prop;
        this.outputDir = prop.getProperty("outputDir");
    }

    public abstract TestResult run() throws FileNotFoundException, UnsupportedEncodingException,IOException ;
    protected abstract ArrayList<String> getParameters(String parsedCommand);
    
    protected String makeTestOutputDir() {
        if(testOutputDir == null){
            Calendar now = Calendar.getInstance();
            testOutputDir = outputDir + test.getTesttype() + "/" + now.get(Calendar.YEAR) + "/"
                + now.get(Calendar.MONTH) + "/"
                + now.get(Calendar.DAY_OF_MONTH) + "/"
                + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND)
                + "." + now.get(Calendar.MILLISECOND) + "/";
        //System.out.println(testOutputDir);
        (new File(testOutputDir)).mkdirs();
        }
        return testOutputDir;
    }
    protected String prepare(String testOutputDir) {
        Pattern p = Pattern.compile("<([^>]*)>");
        //parse command
        StringBuffer stibu = new StringBuffer();
        Matcher m = p.matcher(testDefinition.getTestcommand());
        while (m.find()) {
            //get values
            m.appendReplacement(stibu, getParamValue(test, m.group(1),testOutputDir));
        }
        m.appendTail(stibu);

        return stibu.toString();
    }
    protected TestResult handleResults(String testOutputDir, String consoleOutput) throws FileNotFoundException, UnsupportedEncodingException,IOException {
        TestResult t = new TestResult();
        //t.addSubResult("testInstanceId", test.getInstanceId()); 
        //will be taken care of when adding the result(webserviceAccess.addResult), here only the subvalues
        t.setTestType(test.getTesttype());
        
        String fileName = testOutputDir + "console.log";
        //System.out.println("writing console output to " + fileName);
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.write(consoleOutput);
        writer.close();
        t.addSubResult("log",fileName);
        
        return t;
    }
    protected String getParamValue(TestInstance t, String propId,String testOutputDir) {
        //you need to set the outputDir field before running this method.

        //parse param
        String[] s = propId.split("\\.");
        String ret = null;
        //TestDefinition def = testDefinitions.get(t.getTesttype());
        //System.out.println(s[0]);
        String paramType = testDefinition.getParameters().get(s[0]).get("type");

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
            String fileName = testOutputDir + "context-file.txt";
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
}
