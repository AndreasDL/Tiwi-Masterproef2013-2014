/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

import java.util.HashMap;

/**
 * This class represents a testdefinition. The testdefinitions is meant to hold the command, the basic configuration.
 * A testInstance is an instance of a testDefinition. You could compare it with oo; a testdefinition represents the class while an instance represents the object.
 * @author Andreas De Lille
 */
public class TestDefinition {
    private String testcommand;//don't use camel case, postgresql doesn't like camels
    private String testtype;
    private String testdefinitionname;
    private HashMap<String,HashMap<String,String>> parameters;
    private HashMap<String,HashMap<String,String>> returnValues;    

    /**
     * returns the testType of the test. The testtype is the type used internally in the monitoring.
     * @return 
     */
    public String getTesttype() {
        return testtype;
    }
/**
 * returns the testdefinitionname, this is the name of the definition. 
 * The difference between the testtype and the testdefinitions is that a testtype is used internally and can be automatedtester while a testdefinitionname is the testdefinition it self.
 * e.g. a testtype could be automated tester while the name could be login. The definition would than internally call the automated tester with the configuration provided.
 * @return 
 * */
    public String getTestdefinitionname() {
        return testdefinitionname;
    }

    /**
     * returns the unparsed command given in the definition. Strings between <> are parsed e.g. <testbed.urn> will lookup the parameter testbed and returns its urn.
     * @return 
     */
    public String getTestcommand() {
        return testcommand;
    }
/**
 * will the parameters set in the definition
 * @return hashmap parametername => parameter(type & description)
 */
    public HashMap<String, HashMap<String, String>> getParameters() {
        return parameters;
    }
/**
 * returns the return values set in the definition
 * @return hashmap returnname => returnvalue (type & description)
 */
    public HashMap<String, HashMap<String, String>> getReturnValues() {
        return returnValues;
    }
    
    
}
