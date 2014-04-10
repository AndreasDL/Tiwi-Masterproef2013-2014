/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.util.HashMap;
import java.util.Properties;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class TestCallFactory {
    public static TestCall makeTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop){
        TestCall ret = null;
        switch(test.getTesttype()) {
            case "ping":
                ret = new PingTestCall(test,testDefinition,testbeds,prop);
                break;
            case "login":
                ret = new LoginTestCall(test, testDefinition, testbeds,prop);
                break;
            default:
                ret = new BashTestCall(test,testDefinition,testbeds,prop);
        }
        return ret;
    }
    
}
