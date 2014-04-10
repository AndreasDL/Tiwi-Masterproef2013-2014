/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.testCalls;

import be.iminds.ilabt.jfed.ui.cli.AutomatedTesterCli;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class JavaMainTestCall extends TestCall {
    public JavaMainTestCall(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop) {
        super(test, testDefinition, testbeds, prop);
    }


    @Override
    public TestResult call() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        //Parse
        String testOutputDir = makeTestOutputDir();
        String parsedCommand = prepare(testOutputDir);

        String consoleOutput="";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        
        try {
            ArrayList<String> commands = getParameters(parsedCommand);
            String s[] = new String[commands.size()];
            AutomatedTesterCli.main_helper(commands.toArray(s),ps,ps,System.in);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally{
            //System.setOut(original);
            consoleOutput = os.toString("UTF-8");
            System.out.println(consoleOutput);
            os.close();
            ps.close();
        }

        return handleResults(testOutputDir, consoleOutput);
    }
    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(parsedCommand.split(" ")));

        return p;
    }

}
