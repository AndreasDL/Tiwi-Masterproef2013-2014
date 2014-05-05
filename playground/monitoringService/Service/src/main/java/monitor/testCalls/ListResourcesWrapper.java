package monitor.testCalls;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.log.PrintlnResultLogger;
import be.iminds.ilabt.jfed.lowlevel.AnyCredential;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.JFedException;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.AggregateManagerWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.UserAndSliceApiWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.impl.AutomaticAggregateManagerWrapper;
import be.iminds.ilabt.jfed.lowlevel.api_wrapper.impl.AutomaticUserAndSliceApiWrapper;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.connection.SfaConnectionPool;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.XmlUtil;
import static be.iminds.ilabt.jfed.util.XmlUtil.parseXmlFromString;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import monitor.ResultUploader;
import monitor.model.TestDefinition;
import monitor.model.TestInstance;
import monitor.model.TestResult;
import monitor.model.Testbed;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class ListResourcesWrapper extends TestCall {

    public ListResourcesWrapper(ResultUploader resultUploader, TestInstance test, TestDefinition testDefinition, Map<String, Testbed> testbeds, Properties prop, boolean isLoadTest) {
        super(resultUploader, test, testDefinition, testbeds, prop, isLoadTest);
    }

    @Override
    public void run() {
        start = System.currentTimeMillis();
        try {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Logger logger = new Logger();
            logger.addResultListener(new PrintlnResultLogger());

            //String pemKeyCertFilename = prop.getProperty("authCertDir");
            ArrayList<String> s = getParameters("");

            File testContextFile = new File(getParameters("").get(1));
            if (!testContextFile.exists()) {
                throw new FileNotFoundException("Cannot find Test Context properties file: " + testContextFile.getAbsolutePath());
            }
            CommandExecutionContext testContext = CommandExecutionContext.loadFromFile(testContextFile, true/*requireUser*/, false/*requireTestAuth*/);

            ////////////////////////////////////////// Get target test authority ///////////////////////////////////////
            SfaAuthority wall = JFedAuthorityList.getAuthorityListModel().getByUrn(s.get(0));
            assert wall != null;

            //////////////////////////////////////////// Setup user ////////////////////////////////////////////////////
            //SimpleGeniUser user = new SimpleGeniUser(null, null, IOUtils.fileToString(pemKeyCertFilename), (pass.length == 0) ? null : pass, defaultPemKeyCertFile, defaultPemKeyCertFile);
            GeniUser user = testContext.getGeniUser();

            ///////////////////////////////////////////// Call any GetVersion /////////////////////////////////////////
            SfaConnectionPool conPool = new SfaConnectionPool();
            AggregateManagerWrapper amWrapper = new AutomaticAggregateManagerWrapper(logger, user, conPool, wall);
            UserAndSliceApiWrapper credWrapper = new AutomaticUserAndSliceApiWrapper(logger, user, conPool);

            amWrapper.getVersion();

            ///////////////////// Get a user credential. /////////////////////////
            AnyCredential cred = credWrapper.getUserCredentials(user.getUserUrn());
            System.out.println("\n\ncredential:\n" + cred.getCredentialXml().substring(0, cred.getCredentialXml().length() > 300 ? 300 : cred.getCredentialXml().length()) + "...\n");

            //////////////////////////////////////////// Call ListResources ///////////////////////////////////////
            String rspec = amWrapper.listResources(cred, true/*available*/);

            if (rspec != null) {
                System.out.println("\n\nAdvertisement RSpec: " + rspec);
            } else {
                System.out.println("\n\nListResources failed.");
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //parse Rspec
            //LISTRESOURCES_COUNT=`
            //xmlstarlet pyx ${LISTRESOURCES_OUTPUT_FILE} 
            //|grep 'ZOTAC-vm\|SERVER1P-vm\|SERVER5P-vm\|pcgen03-1p-vm\|pcgen03-2p-vm\|pcgen03-3p-vm\|pcgen03-4p-vm\|pcgen03-5p-vm\|supernode-vm\|plab-pc\|testSSHAccessableResourceHardwareType\|openEPC-WiFi\|laptop\|node+omf.nitos.node\|node+omf.netmode.node\|pc850-vm\|d710-vm\|d820-vm\|pc3000-vm\|pc600-vm\|pc3000w-vm\|pc2400w-vm\|pc2000-vm\|type1-vm\|orca-vm-cloud\|omf:nitos+node'|wc -l`;
            try {
                final Document document = parseXmlFromString(rspec);

                DOMImplementationRegistry registry;
                registry = DOMImplementationRegistry.newInstance();

                DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
                LSSerializer writer = impl.createLSSerializer();
                writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
                writer.getDomConfig().setParameter("xml-declaration", false); // true -> the xml declaration is added

                rspec = writer.writeToString(document);
            } catch (Exception e) {
                //rspec blijft rspec
            }
            //rspec = XmlUtil.formatXmlFromString_alwaysSafe(rspec);
            //rspec opslaan
            String fileName = makeTestOutputDir() + "rspec.txt";
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.write(rspec);
            writer.close();

            //temp output to console
            //System.out.println(rspec);
            Pattern pattern = Pattern.compile("^.*ZOTAC-vm|SERVER1P-vm|SERVER5P-vm|pcgen03-1p-vm|pcgen03-2p-vm|pcgen03-3p-vm|pcgen03-4p-vm|pcgen03-5p-vm|supernode-vm|plab-pc|testSSHAccessableResourceHardwareType|openEPC-WiFi|laptop|node+omf.nitos.node|node+omf.netmode.node|pc850-vm|d710-vm|d820-vm|pc3000-vm|pc600-vm|pc3000w-vm|pc2400w-vm|pc2000-vm|type1-vm|orca-vm-cloud|omf:nitos+node.*$");
            Matcher matcher = pattern.matcher(rspec);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            System.out.println("count of resources: " + count);    // prints 3

            TestResult r = handleResults("", 0);
            r.addSubResult("rspec", fileName);
            r.addSubResult("count", "" + count);

            getResultUploader().addResultToQueue(r);
        } catch (JFedException e) {
            System.err.println("JFedException: " + e);
            e.printStackTrace();
            if (e.getXmlRpcResult() != null) {
                System.err.println("HTTP request:\n" + e.getXmlRpcResult().getRequestHttpContent());
                System.err.println("\nHTTP response:\n" + e.getXmlRpcResult().getResultHttpContent());
                System.err.println("\nXMLRPC request:\n" + e.getXmlRpcResult().getRequestXmlRpcString());
                System.err.println("\nXMLRPC response:\n" + e.getXmlRpcResult().getResultXmlRpcString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected ArrayList<String> getParameters(String parsedCommand
    ) {
        ArrayList<String> commands = new ArrayList<>();

        commands.add(getParamValue("testbed.urn"));
        commands.add(getParamValue("context-file"));

        return commands;
    }

    @Override
    protected TestResult handleResults(String consoleOutput, int returnValue
    ) {
        TestResult r = super.handleResults(consoleOutput, returnValue);
        r.addSubResult("duration", "" + (System.currentTimeMillis() - start));

        return r;
    }

}
