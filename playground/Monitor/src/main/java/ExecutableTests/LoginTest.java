/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ExecutableTests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class LoginTest extends JavaMainTest {
    public LoginTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds) {
        super(test, testDefinition, testbeds);
    }
    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> commands = super.getParameters(parsedCommand);
        commands.add("--test-class");
        commands.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager3");
        commands.add("--group");
        commands.add("nodelogin");
        commands.add("--authorities-file");
        commands.add("/home/drew/masterproef/playground/Monitor/params/auth/authorities.xml");
        commands.add("--output-dir");
        commands.add(super.makeTestOutputDir());
        
        
        return commands;
    }

    
    
    
    @Override
    protected TestResult handleResults(String testOutputDir, String consoleOutput) throws FileNotFoundException, UnsupportedEncodingException,IOException {
        TestResult r = super.handleResults(testOutputDir, consoleOutput);
        
        BufferedReader buf = new BufferedReader(new StringReader(consoleOutput));
        String previous,line;
        while ((line=buf.readLine()) != null){
            
            
            previous = line;
        }
        
        return r;
    }
    
    
    
    
}
