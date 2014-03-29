/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author drew
 */
public class TestForExecution {

    //omzetten naar interface en concrete tests => probleem met toevoegen
    private String Command;
    private HashMap<String, ArrayList<String>> parameters;

    public String getCommand() {
        return Command;
    }
    public HashMap<String, ArrayList<String>> getParametes() {
        return parameters;
    }
    
    public TestForExecution(String Command, HashMap<String, ArrayList<String>> parametes) {
        this.Command = Command;
        this.parameters = parametes;
    }

    public void run(HashMap<String,Testbed> testbeds,HashMap<String,TestDefinition> testDefinitions) {
        //swap params for real values
        //run shell command
        System.out.print("Exec " + Command + " with => ");
        for (String key : parameters.keySet()) {
            System.out.print(key + " = ");
            for (String value : parameters.get(key)) {
                System.out.print(value + " ");
            }
            System.out.print("  |  ");
        }
        System.out.println("");

    }
}
