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
 * This testcall executes a login amv2 testcall.
 * @author Andreas De Lille
 */
public class Login2TestCall extends AutomatedTesterTestCall {

    public Login2TestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, prop, isLoadTest);
    }

    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> commands = super.getParameters(parsedCommand);
        commands.add("--test-class");
        commands.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager2");
        commands.add("--group");
        commands.add("nodelogin");
        commands.add("--output-dir");
        commands.add(makeTestOutputDir());
        //commands.add("-q"); //quiet
        commands.add("--show-credentials");//=> debug only
        commands.add("--context-file");
        commands.add(getParamValue("context-file"));
        

        return commands;
    }
    /*
    @Override
    protected TestResult handleResults(String consoleOutput,int returnValue){

        TestResult r = super.handleResults(consoleOutput,returnValue);
        r.addSubResult("resultHtml", makeTestOutputDir() + "result.html");
        r.addSubResult("result-overview", makeTestOutputDir() + "result-overview.xml");

        //parse result => read overview
        File xmlFile = new File(makeTestOutputDir()+"result-overview.xml");
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            
            NodeList nList = doc.getElementsByTagName("method");
            for (int i = 0 ; i < nList.getLength() ; i++){
                Node n = nList.item(i);
                
                if (n.getNodeType() == Node.ELEMENT_NODE){
                    Element el = (Element) n;
                    
                    r.addSubResult(el.getElementsByTagName("methodName").item(0).getTextContent(),
                            el.getElementsByTagName("state").item(0).getTextContent());
                }
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LoginTestCall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(LoginTestCall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoginTestCall.class.getName()).log(Level.SEVERE, null, ex);
        } 

        return r;
    }*/

}
