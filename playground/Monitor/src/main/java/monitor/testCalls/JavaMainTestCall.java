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
import monitor.Monitor;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class JavaMainTestCall extends TestCall {
    public JavaMainTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop) {
        super(resultUploader, test, testDefinition, testbeds, prop);
    }
    
    @Override
    public void run() {
        //Parse
        makeTestOutputDir();
        String parsedCommand = prepare(makeTestOutputDir());
        System.out.println("Starting javamaintest " + getTest().getTestInstanceId());
        
        String consoleOutput="";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        
        try {
            ArrayList<String> commands = getParameters(parsedCommand);
            String s[] = new String[commands.size()];
            //TODO terugzetten van outputstreams zodak consoleOutput terug kan wegschrijven
            AutomatedTesterCli.main_helper(commands.toArray(s),System.out,System.out,System.in);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally{
            try {
                //System.setOut(original);
                consoleOutput = os.toString("UTF-8");
                System.out.println(consoleOutput);
                os.close();
                ps.close();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(JavaMainTestCall.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(JavaMainTestCall.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Monitor...
        super.getResultUploader().addResultToQueue(handleResults(consoleOutput));
    }
    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(parsedCommand.split(" ")));
        p.add("--context-file");
        p.add(getParamValue("context-file"));
        

        return p;
    }

}