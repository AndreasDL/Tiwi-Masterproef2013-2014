package monitor;

import java.util.ArrayList;
import java.util.HashMap;
import monitor.model.TestForExecution;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.Testbed;

/**
 *
 * @author drew
 */
public class Monitor {

    private WebServiceAccess webAccess;

    private HashMap<String, Testbed> testbeds;
    private HashMap<String, TestDefinition> testDefinitions;

    public static void main(String[] args) {
        new Monitor();
    }

    public Monitor() {
        this.webAccess = new WebServiceAccess();
        //chache testbeds & testdefinitions
        //i don't want to call the service all the time and i don't wan't to make complexer view too so, caching suddenly seems like a good idea.

        this.testbeds = webAccess.getTestBeds();
        this.testDefinitions = webAccess.getTestDefinitions();
        getTestInstances();
    }

    public ArrayList<TestForExecution> getTestInstances() {
        //pingstest only
        HashMap<String, TestInstance> instances = webAccess.getTestInstances();

        for (TestInstance ti : instances.values()) {
            HashMap<String, ArrayList<String>> params = new HashMap<>();

            for (String parameterName : ti.getParameters().keySet()) {
                String property = testDefinitions.get(ti.getTesttype()).getParameters().get(parameterName).get("property");
                ArrayList<String> parameterValue = ti.getParameters().get(parameterName);

                //if property is url => we need url of testbed and not the testbed itself.
                if (!property.equals("value")) {
                    if (property.equals("url")) {
                        for (int i = 0; i < parameterValue.size(); i++) {
                            String s = parameterValue.get(i);
                            parameterValue.set(i, testbeds.get(s).getUrl());
                        }
                    }
                }
                
                //save
                params.put(parameterName,parameterValue);
            }
            //test
            TestForExecution t = new TestForExecution(
               testDefinitions.get(ti.getTesttype()).getTestcommand(),
               params
            );
            t.run();
        }
                
                
        return null;
    }
}
