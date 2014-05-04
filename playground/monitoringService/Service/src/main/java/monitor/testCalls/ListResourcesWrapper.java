package monitor.testCalls;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.log.PrintlnResultLogger;
import be.iminds.ilabt.jfed.lowlevel.AnyCredential;
import be.iminds.ilabt.jfed.lowlevel.JFedException;
import be.iminds.ilabt.jfed.lowlevel.SimpleGeniUser;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.AggregateManagerWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.UserAndSliceApiWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.impl.AutomaticAggregateManagerWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.impl.AutomaticUserAndSliceApiWrapper;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnectionPool;
import be.iminds.ilabt.jfed.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;


public class ListResourcesWrapper extends TestCall{

    public ListResourcesWrapper(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, Map<String, Testbed> testbeds, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, prop, isLoadTest);
    }


    @Override
    public void run() {
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Logger logger = new Logger();
            logger.addResultListener(new PrintlnResultLogger());
            
            String pemKeyCertFilename = prop.getProperty("authCertDir");
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            File defaultPemKeyCertFile = new File(pemKeyCertFilename);//System.getProperty("user.home")+ File.separator+".ssl"+File.separator+"geni_cert.pem");
            //String pemKeyCertFilename = IOUtils.askCommandLineInput("PEM key and certificate filename (default: \"" + defaultPemKeyCertFile.getPath() + "\")");
            char[] pass = "".toCharArray();//IOUtils.askCommandLinePassword("Key password (if any)");
            //if (pemKeyCertFilename == null || pemKeyCertFilename.equals(""))
            //    pemKeyCertFilename = defaultPemKeyCertFile.getPath();

            ////////////////////////////////////////// Get target test authority ///////////////////////////////////////

            ArrayList<String> s = getParameters("");
            SfaAuthority wall = JFedAuthorityList.getAuthorityListModel().getByUrn(s.get(0));

            //////////////////////////////////////////// Setup user ////////////////////////////////////////////////////

            SimpleGeniUser user = new SimpleGeniUser(null, null, IOUtils.fileToString(pemKeyCertFilename), (pass.length == 0) ? null : pass, defaultPemKeyCertFile, defaultPemKeyCertFile);

            ///////////////////////////////////////////// Call any GetVersion /////////////////////////////////////////

            SfaConnectionPool conPool = new SfaConnectionPool();
            AggregateManagerWrapper amWrapper = new AutomaticAggregateManagerWrapper(logger, user, conPool, wall);
            UserAndSliceApiWrapper credWrapper = new AutomaticUserAndSliceApiWrapper(logger, user, conPool);

            amWrapper.getVersion();

            ///////////////////// Get a user credential. /////////////////////////

            AnyCredential cred = credWrapper.getUserCredentials(user.getUserUrn());
            System.out.println("\n\ncredential:\n"+cred.getCredentialXml().substring(0, cred.getCredentialXml().length() > 300 ? 300 : cred.getCredentialXml().length())+"...\n");

            //////////////////////////////////////////// Call ListResources ///////////////////////////////////////

            System.out.println("hoi!s");
            String rspec = amWrapper.listResources(cred, true/*available*/);

            if (rspec != null)
                System.out.println("\n\nAdvertisement RSpec: "+rspec);
            else
                System.out.println("\n\nListResources failed.");

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            //parse Rspec
            //TestResult r = handleResults("",0);
            //LISTRESOURCES_COUNT=`
            //xmlstarlet pyx ${LISTRESOURCES_OUTPUT_FILE} 
            //|grep 'ZOTAC-vm\|SERVER1P-vm\|SERVER5P-vm\|pcgen03-1p-vm\|pcgen03-2p-vm\|pcgen03-3p-vm\|pcgen03-4p-vm\|pcgen03-5p-vm\|supernode-vm\|plab-pc\|testSSHAccessableResourceHardwareType\|openEPC-WiFi\|laptop\|node+omf.nitos.node\|node+omf.netmode.node\|pc850-vm\|d710-vm\|d820-vm\|pc3000-vm\|pc600-vm\|pc3000w-vm\|pc2400w-vm\|pc2000-vm\|type1-vm\|orca-vm-cloud\|omf:nitos+node'|wc -l`;
            System.out.println(rspec);
            
            
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
        } catch (JFedException e) {
            System.err.println("JFedException: "+e);
            e.printStackTrace();
            if (e.getXmlRpcResult() != null) {
                System.err.println("HTTP request:\n"+e.getXmlRpcResult().getRequestHttpContent());
                System.err.println("\nHTTP response:\n"+e.getXmlRpcResult().getResultHttpContent());
                System.err.println("\nXMLRPC request:\n"+e.getXmlRpcResult().getRequestXmlRpcString());
                System.err.println("\nXMLRPC response:\n"+e.getXmlRpcResult().getResultXmlRpcString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        //getResultUploader().addResultToQueue(r);
    }

    @Override
    protected ArrayList<String> getParameters(String parsedCommand) {
        ArrayList<String> commands = new ArrayList<>();
        
        commands.add(getParamValue("testbed.urn"));

        return commands;
    }
    

    @Override
    protected TestResult handleResults(String consoleOutput, int returnValue) {
        return new TestResult(test, loadTest);
    }
    
    
}
