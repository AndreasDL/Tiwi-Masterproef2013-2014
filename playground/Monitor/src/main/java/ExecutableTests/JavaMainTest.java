/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ExecutableTests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;
import be.iminds.ilabt.jfed.ui.cli.AutomatedTesterCli;
import com.sun.org.apache.xalan.internal.lib.ExsltStrings;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        try {
            //Parse
            String testOutputDir = makeTestOutputDir();
            String parsedCommand = prepare(testOutputDir);
            
            ArrayList<String> commands = getParameters(parsedCommand);
            String s[] = new String[commands.size()];
            AutomatedTesterCli.main(commands.toArray(s));
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JavaMainTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        String[] s = parsedCommand.split(" ");
        
        ArrayList<String> p = new ArrayList<>();
        for (String ss : s){
            p.add(ss);
        }
        
        
        return p;
    }
    
    
    
}
