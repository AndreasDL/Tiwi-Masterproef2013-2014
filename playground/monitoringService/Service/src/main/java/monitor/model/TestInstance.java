/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * this class represents a testinstance. A testinstance is a testdefinition with the parameters set. 
 * The definition will tell you what parameters and returnvalues are to be expected; the instance will tell you the value of them.
 * @author drew
 */
public class TestInstance {
    private String testname,testdefinitionname;
    private int frequency;
    private HashMap<String,ArrayList<String>> parameters;
    private String testinstanceid;
    private boolean enabled;
    private Timestamp nextrun;

    /**
     * returns the nextrun value for the instance
     * @return iso 8601 format
     */
    public Date getNextrun() {
        return nextrun;
    }
    /**
     * returns the testinstance id
     * @return 
     */
    public String getTestInstanceId() {
        return testinstanceid;
    }
    /**
     * tell you if the test is enabled.
     * @return 
     */
    public boolean isEnabled(){
        return enabled;
    }
    /**
     * tell you if the test should run. A test should run when the nextrun is in the past
     * @return 
     */
    public boolean isScheduled(){
        if (nextrun != null){
            long next = nextrun.getTime();
            next += frequency * 1000;
            
            return next < System.currentTimeMillis();
        }else{
            //null => instance has never run
            return true;
        }
    }
    /**
     * sets the testinsance id
     * @param instanceId 
     */
    public void setTestInstanceId(String instanceId) {
        this.testinstanceid = instanceId;
    }

/**
 * returns the name of the test
 * @return 
 */
    public String getTestname() {
        return testname;
    }
/**
 * sets the name of the test
 * @param testname 
 */
    public void setTestname(String testname) {
        this.testname = testname;
    }
/**
 * returns the name of the definition associated with this test.
 * @return 
 */
    public String getTestDefinitionName() {
        return testdefinitionname;
    }
/**
 * sets the definitionname of this test.
 * @param testdefinitionname 
 */
    public void setTestDefinitionName(String testdefinitionname) {
        this.testdefinitionname = testdefinitionname;
    }
/**
 * return the frequency
 * @return frequency in seconds
 */
    public int getFrequency() {
        return frequency;
    }
/**
 * sets the frequency
 * @param frequency the frequency in seconds
 */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
/**
 * returns the parameters
 * @return hashmap parametername=>parametervalue
 */
    public HashMap<String, ArrayList<String>> getParameters() {
        return parameters;
    }
/**
 * sets the parameters 
 * @param parameters hashmap parametername => parameter values
 */
    public void setParameters(HashMap<String, ArrayList<String>> parameters) {
        this.parameters = parameters;
    }

}
