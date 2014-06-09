/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.util.Map;
import java.util.Properties;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;
import monitor.model.User;

/**
 * Used to create the different testtypes
 * @author Andreas De Lille
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
    public static TestCall makeTest(ResultUploader resup,TestInstance test, TestDefinition testDefinition, Map<String, Testbed> testbeds,Map<String,User> users, Properties prop){
        return makeTest(resup, test, testDefinition, testbeds,users, prop,false);
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
    public static TestCall makeTest(ResultUploader resup,TestInstance test, TestDefinition testDefinition, Map<String, Testbed> testbeds,Map<String,User> users,Properties prop,boolean isLoadTest){
        TestCall ret = null;
        //switch(test.getTestDefinitionName()) {
        switch(testDefinition.getTesttype()){
            case "ping":
                ret = new PingTestCall(resup,test,testDefinition,testbeds,users,prop,isLoadTest);
                break;
            case "login2":
                ret = new Login2TestCall(resup,test, testDefinition, testbeds,users,prop,isLoadTest);
                break;
            case "login3":
                ret = new Login3TestCall(resup,test, testDefinition, testbeds,users,prop,isLoadTest);
                break;
            case "stitch":
                ret = new StitchingTestCall(resup,test, testDefinition, testbeds,users, prop,isLoadTest);
                break;
            case "automatedTesterTestCall":
                ret = new AutomatedTesterTestCall(resup, test, testDefinition, testbeds,users, prop,isLoadTest);
                break;
            case "getVersion2":
                ret = new GetVersion2TestCall(resup, test, testDefinition, testbeds,users, prop, isLoadTest);
                break;
            case "getVersion3":
                ret = new GetVersion2TestCall(resup, test, testDefinition, testbeds,users, prop, isLoadTest);
                break;
            case "listResources":
                ret = new ListResourcesWrapper(resup, test, testDefinition, testbeds,users, prop, isLoadTest);
                break;
            default:
                ret = new BashTestCall(resup,test,testDefinition,testbeds,users,prop,isLoadTest);
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
        return makeTest(c.getResultUploader(),c.getTest(),c.getTestDefinition(),c.getTestbeds(),c.getUsers(),c.getProp(),c.isLoadTest());
    }
}
