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
    private boolean isLoadTest;

    public TestResult(TestInstance ti){
        this(ti,false);
    }
    public TestResult(TestInstance ti,boolean isLoadTest){
        results = new HashMap<>();
        this.ti = ti;
        this.isLoadTest = isLoadTest;
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
    
    public boolean isLoadTest(){
        //setting this to true will avoid the webservice from trying to update the lastrun time.
        return isLoadTest;
    }
    

}
