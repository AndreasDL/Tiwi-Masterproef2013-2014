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
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 * Uses the fping command to ping a testbed.
 * @author Andreas De Lille
 */
public class PingTestCall extends BashTestCall {

    public PingTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, prop, isLoadTest);
    }



    @Override
    protected TestResult handleResults(String consoleOutput, int returnValue) {
        TestResult r = super.handleResults(consoleOutput,returnValue);
        r.addSubResult("pingValue", consoleOutput);
        return r;
    }
}
