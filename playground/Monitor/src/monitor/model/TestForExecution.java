/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author drew
 */
public class TestForExecution {

    //omzetten naar interface en concrete tests => probleem met toevoegen
    private String command;

    public TestForExecution(String Command) {
        this.command = Command;
    }

    public String getCommand() {
        return command;
    }

    public String run() throws IOException, InterruptedException {
        //swap params for real values
        //run shell command
        System.out.println("Exec " + command);
        ArrayList<String> commands = new ArrayList<>();
        commands.add("/usr/bin/sh");
        commands.add("-c");
        commands.add(command);
        ProcessBuilder pb = new ProcessBuilder(commands);
        //pb.directory(new File("/home/drew/brol"));
        pb.redirectErrorStream(true);
        Process p = pb.start();

        //Read output
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
        String line = null, previous = null;
        while ((line = br.readLine()) != null) {
            if (!line.equals(previous)) {
                previous = line;
                out.append(line);//.append('\n');
                //System.out.println(line);
            }
        }
        return out.toString();
        /*
        //Check result
        if (p.waitFor() == 0) {
            System.exit(0);
            return line;
        }else{
            //Abnormal termination: Log command parameters and output and throw ExecutionException
            System.err.println(command);
            System.err.println(out.toString());
            System.exit(1);
            return null;
        }*/
    }

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

}
