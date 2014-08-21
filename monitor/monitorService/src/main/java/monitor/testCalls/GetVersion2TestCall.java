/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;
import monitor.model.User;

/**
 * this class will call the automated tester for a getversion test using amv2
 * @author Andreas De Lille
 */
public class GetVersion2TestCall extends AutomatedTesterTestCall {

    public GetVersion2TestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, Map<String, Testbed> testbeds, Map<String, User> users, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, users, prop, isLoadTest);
    }




    
    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> commands = super.getParameters(parsedCommand);
        commands.add("--test-class");
        commands.add("be.iminds.ilabt.jfed.lowlevel.api.test.ConnectivityTestAggregateManager2");
        commands.add("--output-dir");
        commands.add(makeTestOutputDir());
        //commands.add("-q"); //quiet
        //commands.add("--show-credentials");//=> debug only
        commands.add("--context-file");
        commands.add(getParamValue("context-file"));

        return commands;
    }
    
}