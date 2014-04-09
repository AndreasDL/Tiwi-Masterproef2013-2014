/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExecutableTests;

import be.iminds.ilabt.jfed.ui.cli.AutomatedTesterCli;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class JavaMainTest extends ExecutableTest {

    public JavaMainTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds) {
        super(test, testDefinition, testbeds);
    }
    @Override
    public TestResult run() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        //Parse
        String testOutputDir = makeTestOutputDir();
        String parsedCommand = prepare(testOutputDir);

        ArrayList<String> commands = getParameters(parsedCommand);
        String s[] = new String[commands.size()];
        String consoleOutput="";
        PrintStream ps = new PrintStream("/home/drew/sample.txt");
        //ByteArrayOutputStream os = new ByteArrayOutputStream();
        //PrintStream ps = new PrintStream(os);
        System.out.println("b4 redirect");
        PrintStream original = System.out;
        System.setOut(ps);//redirect std output
        
        try {
            AutomatedTesterCli.main(commands.toArray(s));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally{
            System.setOut(original);
            ps.close();
            
            //consoleOutput = os.toString("UTF-8");
            System.out.println("hoi");
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
