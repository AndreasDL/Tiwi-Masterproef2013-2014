/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ExecutableTests;

import java.util.HashMap;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class TestFactory {
    public static ExecutableTest makeTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds){
        ExecutableTest ret = null;
        switch(test.getTesttype()) {
            case "ping":
                ret = new PingTest(test,testDefinition,testbeds);
                break;
            case "login":
                ret = new LoginTest(test, testDefinition, testbeds);
                break;
            default:
                ret = new BashTest(test,testDefinition,testbeds);
        }
        return ret;
    }
    
}
