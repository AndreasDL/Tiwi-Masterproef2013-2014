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
 *
 * @author drew
 */
public class TestCallFactory {
    public static TestCall makeTest(ResultUploader resup,TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds,Properties prop){
        TestCall ret = null;
        switch(test.getTesttype()) {
            case "ping":
                ret = new PingTestCall(resup,test,testDefinition,testbeds,prop);
                break;
            case "login":
                ret = new LoginTestCall(resup,test, testDefinition, testbeds,prop);
                break;
            case "stitch":
                ret = new StitchingTestCall(resup,test, testDefinition, testbeds, prop);
                break;
            default:
                ret = new BashTestCall(resup,test,testDefinition,testbeds,prop);
        }
        return ret;
    }
    public static TestCall copyTest(TestCall c){
        //shallow copy, but test,testdefinitions,testbeds are final
        //prop is only set once & never changes
        //resultuploader is the same for all object.
        return makeTest(c.resultUploader,c.test,c.testDefinition,c.testbeds,c.prop);
    }
}
