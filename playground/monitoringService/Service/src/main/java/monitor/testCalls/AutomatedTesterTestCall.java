/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.testCalls;

import be.iminds.ilabt.jfed.ui.cli.AutomatedTesterCli;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.ResultUploader;
import monitor.TeePrintStream;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 * This class runs a test using the automated tester
 * @author drew
 */
public class AutomatedTesterTestCall extends TestCall {

    public AutomatedTesterTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop,boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, prop, isLoadTest);
    }
    
    @Override
    public void run() {
        start = System.currentTimeMillis();
        //Parse
        makeTestOutputDir();
        String parsedCommand = prepare();
        System.out.println("Starting " + getTestDefinitionName() + " test " + getTest().getTestname() + " with id " +getTest().getTestInstanceId());
        
        String consoleOutput="";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        TeePrintStream tee = null;
        try {
            tee = new TeePrintStream(ps,System.out);
        } catch (IOException ex) {
            Logger.getLogger(AutomatedTesterTestCall.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int returnValue = -1;
        try {
            ArrayList<String> commands = getParameters(parsedCommand);
            String s[] = new String[commands.size()];
            //System.out.println("calling main : commands = " + commands);
            returnValue = AutomatedTesterCli.main_helper(commands.toArray(s),/*ps,ps*/tee,tee/**/,System.in);//System.out,System.out,System.in);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally{
            try {
                //System.setOut(original);
                consoleOutput = os.toString("UTF-8");
                //System.out.println(consoleOutput);
                os.close();
                ps.close();
                tee.close();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AutomatedTesterTestCall.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AutomatedTesterTestCall.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Monitor...
        getResultUploader().addResultToQueue(handleResults(consoleOutput,returnValue));
    }
    
    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(parsedCommand.split(" ")));

        return p;
    }
    
    @Override
    protected TestResult handleResults(String consoleOutput, int returnValue) {
        TestResult r =  super.handleResults(consoleOutput,returnValue);
        r.addSubResult("duration",""+(System.currentTimeMillis() -  start));
        
        return r;
    }
    
    

}
