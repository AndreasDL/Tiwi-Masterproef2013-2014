/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.ExecutableTests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class PingTest extends BashTest {
    public PingTest(TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop) {
        super(test, testDefinition, testbeds, prop);
    }



    @Override
    protected TestResult handleResults(String outputDir, String consoleOutput) throws FileNotFoundException, UnsupportedEncodingException,IOException {
        TestResult r = super.handleResults(outputDir, consoleOutput);
        r.addSubResult("pingValue", consoleOutput);
        return r;
    }
    
    
}
