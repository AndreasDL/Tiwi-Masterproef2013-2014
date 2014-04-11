/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class BashTestCall extends TestCall {
    public BashTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop) {
        super(resultUploader, test, testDefinition, testbeds, prop);
    }



    @Override
    public void run() {

                //Parse
        String testOutputDir = makeTestOutputDir();
        String parsedCommand = prepare(testOutputDir);
        
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
            Logger.getLogger(TestCall.class.getName()).log(Level.SEVERE, null, ex);
        }

        super.getResultUploader().addResultToQueue(handleResults(out.toString()));
    }
    
    @Override
    protected ArrayList<String> getParameters(String parsedCommand){
        ArrayList<String> commands = new ArrayList<>();
        commands.add("/usr/bin/bash");
        commands.add("-c");
        commands.add(parsedCommand);
        return commands;
    }
    
}
