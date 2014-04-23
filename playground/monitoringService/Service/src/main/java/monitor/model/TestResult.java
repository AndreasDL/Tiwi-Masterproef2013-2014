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

    HashMap<String, String> results;
    TestInstance ti;

    public TestResult(TestInstance ti){
        results = new HashMap<>();
        this.ti = ti;
    }

    public String getTestDefinitionName() {
        return ti.getTestDefinitionName();
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
    public TestInstance getTestInstance() {
        return ti;
    }
    
    

}
