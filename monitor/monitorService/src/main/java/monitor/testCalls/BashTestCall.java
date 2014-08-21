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
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;
import monitor.model.User;

/**
 * This class executes a bashscript/command e.g. ping
 * @author Andreas De Lille
 */
public class BashTestCall extends TestCall {

    public BashTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, Map<String, Testbed> testbeds, Map<String, User> users, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, users, prop, isLoadTest);
    }






    @Override
    public void run() {
        System.out.println("Starting " + super.test.getTestname() + " with id" + super.test.getTestInstanceId() + " at " + (new Date()).getTime());
        start=System.currentTimeMillis();
        //Parse
        makeTestOutputDir();
        String parsedCommand = prepare();
        
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
                    
                    //vermijden dat er een \n staat bij de ping test.
                    if (previous != null)
                        out.append('\n');
                    
                    previous = line;
                    //System.out.println(line);
                    out.append(line);//.append('\n');
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(TestCall.class.getName()).log(Level.SEVERE, null, ex);
        }

        getResultUploader().addResultToQueue(handleResults(out.toString(),0));
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
