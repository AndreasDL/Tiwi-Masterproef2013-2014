/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

import java.util.HashMap;

/**
 *
 * @author drew
 */
public class TestDefinition {
    private String testcommand;
    private HashMap<String,HashMap<String,String>> parameters;
    private HashMap<String,HashMap<String,String>> returnValues;    

    public String getTestcommand() {
        return testcommand;
    }

    public HashMap<String, HashMap<String, String>> getParameters() {
        return parameters;
    }

    public HashMap<String, HashMap<String, String>> getReturnValues() {
        return returnValues;
    }
    
    
}
