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
public class TestInstance {
    private String testname,testtype;
    private int frequency;
    private HashMap<String,ArrayList<String>> parameters;
    private String testinstanceid;
    private boolean enabled;
    
    public String getTestInstanceId() {
        return testinstanceid;
    }
    public boolean isEnabled(){
        return enabled;
    }
    public void setTestInstanceId(String instanceId) {
        this.testinstanceid = instanceId;
    }


    public String getTestname() {
        return testname;
    }

    public void setTestname(String testname) {
        this.testname = testname;
    }

    public String getTesttype() {
        return testtype;
    }

    public void setTesttype(String testtype) {
        this.testtype = testtype;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public HashMap<String, ArrayList<String>> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, ArrayList<String>> parameters) {
        this.parameters = parameters;
    }

}
