/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor.model;

import java.util.HashMap;

/**
 * This class represents a testresult.
 * @author drew
 */
public class TestResult {

    HashMap<String, String> results;
    TestInstance ti;
    private boolean isLoadTest;
/**
 * Creates a testresult 
 * @param ti the instance to associate with the result
 */
    public TestResult(TestInstance ti){
        this(ti,false);
    }
     /**
      * Creates a testresult
      * @param ti the instance to associate with the result
      * @param isLoadTest when set to true, the result is handled as loadtest & therefor the resultuploader won't change the nextrun value
      */       
    public TestResult(TestInstance ti,boolean isLoadTest){
        results = new HashMap<>();
        this.ti = ti;
        this.isLoadTest = isLoadTest;
    }
/**
 * returns the definitionname
 * @return 
 */
    public String getTestDefinitionName() {
        return ti.getTestDefinitionName();
    }
/**
 * adds a subresult
 * @param subResultName the name of the subresult
 * @param subResultValue the value of the subresult
 */
    public void addSubResult(String subResultName, String subResultValue) {
        results.put(subResultName, subResultValue);
    }
/**
 * returns tha value of a subresult
 * @param subResultName the name of the subresult
 * @return the value of the subresult
 */
    public String getSubResult(String subResultName) {
        return results.get(subResultName);
    }
    /**
     * the hashmap of the subresults
     * @return hashmap resultname => resultvalue
     */
    public HashMap<String, String> getResults() {
        return results;
    }
    /**
     * the testinstance for this testresult
     * @return 
     */
    public TestInstance getTestInstance() {
        return ti;
    }
    /**
     * returns true if this is a loadtest, avoiding that the resultuploader will change the nextrun
     * @return 
     */
    public boolean isLoadTest(){
        //setting this to true will avoid the webservice from trying to update the lastrun time.
        return isLoadTest;
    }
    

}
