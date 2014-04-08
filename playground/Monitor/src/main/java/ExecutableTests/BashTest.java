/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ExecutableTests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
public class BashTest extends ExecutableTest {
    public BashTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds) {
        super(test, testDefinition, testbeds);
    }
    @Override
    public TestResult run() throws FileNotFoundException, UnsupportedEncodingException,IOException {

                //Parse
        String testOutputDir = makeTestOutputDir();
        String parsedCommand = prepare(testOutputDir);
        
        TestResult r = new TestResult();
        //run shell command
        System.out.println("Exec " + parsedCommand);
        ArrayList<String> commands = getParameters(parsedCommand);
        StringBuilder out = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);

            pb.redirectErrorStream(true);
            Process p = pb.start();

            //Read output
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = null, previous = null;
            while ((line = br.readLine()) != null) {
                if (!line.equals(previous)) {
                    previous = line;
                    //System.out.println(line);
                    out.append(line).append('\n');
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ExecutableTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return handleResults(testOutputDir, out.toString());
    }
    
    protected ArrayList<String> getParameters(String parsedCommand){
        ArrayList<String> commands = new ArrayList<>();
        commands.add("/usr/bin/bash");
        commands.add("-c");
        commands.add(parsedCommand);
        return commands;
    }
    
}
