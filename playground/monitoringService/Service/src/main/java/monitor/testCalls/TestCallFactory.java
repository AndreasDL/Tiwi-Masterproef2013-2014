/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.util.HashMap;
import java.util.Properties;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 * Used to create the different testtypes
 * @author drew
 */
public class TestCallFactory {
    /**
     * Creates a normaltest
     * @param resup the resultuploader the testneeds to upload his result.
     * @param test The testInstance
     * @param testDefinition The testDefinition
     * @param testbeds The hashmap of testbeds for lookups
     * @param prop the properties
     * @return the testCall associated with the given arguments.
     */
    public static TestCall makeTest(ResultUploader resup,TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop){
        return makeTest(resup, test, testDefinition, testbeds, prop,false);
    }
    /**
     * Creates a normaltest
     * @param resup the resultuploader the testneeds to upload his result.
     * @param test The testInstance
     * @param testDefinition The testDefinition
     * @param testbeds The hashmap of testbeds for lookups
     * @param prop the properties
     * @param isLoadTest when set to true the scheduling is ignored and the next run is not updated.
     * @return the testCall associated with the given arguments.
     */
    public static TestCall makeTest(ResultUploader resup,TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop,boolean isLoadTest){
        TestCall ret = null;
        //switch(test.getTestDefinitionName()) {
        switch(testDefinition.getTesttype()){
            case "ping":
                ret = new PingTestCall(resup,test,testDefinition,testbeds,prop,isLoadTest);
                break;
            case "login":
                ret = new LoginTestCall(resup,test, testDefinition, testbeds,prop,isLoadTest);
                break;
            case "login3":
                ret = new Login3TestCall(resup,test, testDefinition, testbeds,prop,isLoadTest);
                break;
            case "stitch":
                ret = new StitchingTestCall(resup,test, testDefinition, testbeds, prop,isLoadTest);
                break;
            case "automatedTesterTestCall":
                ret = new AutomatedTesterTestCall(resup, test, testDefinition, testbeds, prop,isLoadTest);
                break;
            default:
                ret = new BashTestCall(resup,test,testDefinition,testbeds,prop,isLoadTest);
        }
        return ret;
    }
    /**
     * Creates a deep copy of the testCall.
     * @param c testcall to copy
     * @return deep copy of testcall c
     */
    public static TestCall copyTest(TestCall c){
        //shallow copy, but test,testdefinitions,testbeds are final
        //prop is only set once & never changes
        //resultuploader is the same for all object.
        return makeTest(c.resultUploader,c.test,c.testDefinition,c.testbeds,c.prop,c.isLoadTest);
    }
}
