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
public class TestResult {

    String testType;
    HashMap<String, String> results;

    public TestResult() {
        results = new HashMap<>();
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public void addSubResult(String subResultName, String subResultValue) {
        results.put(subResultName, subResultValue);
    }

    public String getSubResult(String subResultName) {
        return results.get(subResultName);
    }
    public HashMap<String, String> getResults() {
        return results;
    }

}
