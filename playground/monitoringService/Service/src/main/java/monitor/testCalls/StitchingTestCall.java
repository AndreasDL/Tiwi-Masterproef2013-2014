/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 * Calls the automated tester to run a stitching test.
 * This class represents a testcall. The differenct testCalls are bashTestcall => runs a bash command/script.
 * automated tester => calls the automated tester.jar
 */
public class StitchingTestCall extends AutomatedTesterTestCall{

    public StitchingTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, prop, isLoadTest);
    }


    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> commands = super.getParameters(parsedCommand);
        commands.add("--test-class");
        commands.add("be.iminds.ilabt.jfed.lowlevel.api.test.StitchingTest");
        commands.add("--group");
        commands.add("nodelogin");
        commands.add("--output-dir");
        commands.add(super.makeTestOutputDir());
        commands.add("--context-file");
        commands.add(getParamValue("context-file"));
        
        
        return commands;
    }
    
    
}
