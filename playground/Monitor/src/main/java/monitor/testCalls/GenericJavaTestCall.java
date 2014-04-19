/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.util.ArrayList;
import java.util.Arrays;
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
public class GenericJavaTestCall extends JavaMainTestCall{

    public GenericJavaTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop) {
        super(resultUploader, test, testDefinition, testbeds, prop);
    }

    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(parsedCommand.split(" ")));
        /*p.add("--context-file");
        p.add(getParamValue("context-file"));=> niet nodig alles in definitie steken
                */
        

        return p;
    }
    
    
    
}
