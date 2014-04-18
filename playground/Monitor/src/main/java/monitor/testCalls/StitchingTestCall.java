/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.testCalls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author drew
 */
public class StitchingTestCall extends JavaMainTestCall{
    public StitchingTestCall(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, HashMap<String, Testbed> testbeds, Properties prop) {
        super(resultUploader, test, testDefinition, testbeds, prop);
    }

    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> commands = super.getParameters(parsedCommand);
        commands.add("--test-class");
        commands.add("be.iminds.ilabt.jfed.lowlevel.api.test.StitchingTest");
        commands.add("--group");
        commands.add("nodelogin");
        //commands.add("--authorities-file");
        //commands.add("/home/drew/masterproef/playground/Monitor/params/auth/authorities.xml");
        commands.add("--output-dir");
        commands.add(super.makeTestOutputDir());
        
        
        return commands;
    }
    
    @Override
    protected TestResult handleResults(String consoleOutput) {

        TestResult r = super.handleResults(consoleOutput);
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
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(LoginTestCall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StitchingTestCall.class.getName()).log(Level.SEVERE, null, ex);
        } 

        return r;
    }
    
    
}
