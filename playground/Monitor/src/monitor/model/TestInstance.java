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
    private ArrayList<HashMap<String,String>> parameters;

    public TestInstance(String testname, String testtype, int frequency, ArrayList<HashMap<String, String>> parameters) {
        this.testname = testname;
        this.testtype = testtype;
        this.frequency = frequency;
        this.parameters = parameters;
    }

    public ArrayList<HashMap<String, String>> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<HashMap<String, String>> parameters) {
        this.parameters = parameters;
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
}
