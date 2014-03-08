package be.iminds.ilabt.jfed.ui.commandline;

import be.iminds.ilabt.jfed.lowlevel.api.ProtoGeniClearingHouse1;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.util.*;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * CommandLineClient
 */
public class CommandLineClient {

    public static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, "jfed-probe-cli [options ... ] <command> [command arguments ...]", "Possible options:", options, "Required options:\n" +
                "         --api\n" +
                "         either --context-file or both -k and -u    (-u can be replaced by -a)\n");
    }

    public static void main(String[] args) throws GeniException, IOException {
        CommandLineParser parser = new BasicParser();

        // create the Options
        Options options = new Options();
        options.addOption( OptionBuilder.withLongOpt( "api" )
                .withDescription( "The API to call (MANDATORY)" )
                .hasArg()
                .withArgName("API")
                .isRequired()
                .create() );
        options.addOption( "k", "key-file", true, "file containing user certificate and key in PEM format" );
        options.addOption( "u", "server-url", true, "URL of server" );
        options.addOption( "a", "authority", true, "fill in server url based on hrn. Only works for known authorities" );
        options.addOption( OptionBuilder.withLongOpt( "context-file" )
                .withDescription( "replaces all previous parameters by those in the provided context properties file (same format as used by compliance tester)" )
                .hasArg()
                .withArgName("CONTEXT_FILE")
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "output-file" )
                .withDescription( "file to write output to" )
                .hasArg()
                .withArgName("CONTEXT_FILE")
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "use-tested-SA" )
                .withDescription( "do not use the SA of the user, use the SA of the tested authority" )
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "accept-self-signed" )
                .withDescription( "accept unknown self signed certificates (DANGER: this is a big security risk)" )
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "clearinghouse" )
                .withDescription( "fetch certificates etc from clearinghouse first" )
                .create() );
        options.addOption( "q", "quiet", false, "less output" );
        options.addOption( "d", "debug", false, "extra debugging output" );
        options.addOption( "i", "interactive", false, "interactively request some missing options and command arguments. The --api option and the <command> are still required" );

        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            System.err.println( "Command line argument Syntax error: " + exp.getMessage() );
            help(options);
            System.exit(1);
        }

        boolean useTestedSA = line.hasOption("use-tested-SA");
        boolean debug = line.hasOption("debug");
        String outputfile = line.getOptionValue("output-file");
        boolean silent = line.hasOption("quiet");
        boolean interactive = line.hasOption("interactive");
        boolean acceptSelfSigned = line.hasOption("accept-self-signed");
        boolean clearinghouse = line.hasOption("clearinghouse");
        String serverUrl = line.getOptionValue("server-url");
        String auth = line.getOptionValue("authority");
        String pemKeyCertFilename = line.getOptionValue("key-file");
        String contextProperties = line.getOptionValue("context-file");

        List<String> arguments = line.getArgList();
        if (arguments.size() < 1) {
            System.err.println("Error: No command specified after options.");
            help(options);
            System.exit(1);
        }

        //first check if mandatory options are given (we don;t do this using the library, because it's either context-file or it is a number of other options  )
        boolean contextExists = line.hasOption("context-file");

        if (line.hasOption("u") && line.hasOption("a")) {
            System.err.println("You can only use either -u or -a, not both");
            help(options);
            System.exit(1);
        }
        boolean connectionDetailsExist = line.hasOption("k") && (line.hasOption("u") || line.hasOption("a"));
        if (contextExists &&connectionDetailsExist) {
            System.err.println("--context-file cannot be mixed with -u, -k- and -a");
            help(options);
            System.exit(1);
        }
        if (!interactive && !contextExists && !connectionDetailsExist) {
            System.err.println("illegal combination of --context-file and -u, -k-, -a");
            help(options);
            System.exit(1);
        }



        SfaAuthority.debug = false; //ugly hack to suppress output

        Logger logger = new Logger();

        AggregateManager2 am2 = new AggregateManager2(logger);
        CommandLineCore<AggregateManager2> coreAm2 = new CommandLineCore<AggregateManager2>(AggregateManager2.class, am2);
        AggregateManager3 am3 = new AggregateManager3(logger);
        CommandLineCore<AggregateManager3> coreAm3 = new CommandLineCore<AggregateManager3>(AggregateManager3.class, am3);
        SliceAuthority sa = new SliceAuthority(logger);
        CommandLineCore<SliceAuthority> coreSa = new CommandLineCore<SliceAuthority>(SliceAuthority.class, sa);
        ProtoGeniClearingHouse1 ch = new ProtoGeniClearingHouse1(logger);
        CommandLineCore<ProtoGeniClearingHouse1> coreCh = new CommandLineCore<ProtoGeniClearingHouse1>(ProtoGeniClearingHouse1.class, ch);

//        String wall3Am2ServerUrl = "https://www.wall3.test.ibbt.be/protogeni/xmlrpc/am/2.0";
//        String wall3SaServerUrl = "https://www.wall3.test.ibbt.be/protogeni/xmlrpc/sa";
//        String chServerUrl = "https://www.emulab.net/protogeni/xmlrpc/ch";


        CommandLineCore core = null;
        ServerType serverType = null;
        String apiArg = line.getOptionValue("api");
        if (apiArg.equalsIgnoreCase("AM2") || apiArg.equalsIgnoreCase("AMv2")) {
            serverType = new ServerType(ServerType.GeniServerRole.AM, 2);
            core = coreAm2;
        }
        if (apiArg.equalsIgnoreCase("AM3") || apiArg.equalsIgnoreCase("AMv3")) {
            serverType = new ServerType(ServerType.GeniServerRole.AM, 3);
            core = coreAm3;
        }
        if (apiArg.equalsIgnoreCase("PROTOGENI_SA")) {
            serverType = new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1);
            core = coreSa;
        }
        if (apiArg.equalsIgnoreCase("PROTOGENI_CH")) {
            serverType = new ServerType(ServerType.GeniServerRole.PROTOGENI_CH, 1);
            core = coreCh;
        }
        if (core == null || serverType == null) {
            System.err.println("Error: unknown API: \""+apiArg+"\"  (registered here: PROTOGENI_SA, PROTOGENI_CH, AM2, AM3)");
            help(options);
            System.exit(1);
        }

        String command = arguments.remove(0);
        if (core != null && !core.getAvailableMethodNamesLowerCase().contains(command.toLowerCase())) {
            System.err.println("Error: Unknown command: '"+command+"'");
            help(options);
            System.exit(1);
        }

        AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();
        if (clearinghouse) {
            if (!silent)
                System.out.println("Fetching info from Emulab Utah Clearinghouse...");
            UtahClearingHouseAuthorityList.load(authorityListModel);
            for (SfaAuthority curAuth : authorityListModel.getAuthorities())
                if (curAuth.getPemSslTrustCert() != null)
                    GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(curAuth.getPemSslTrustCert());

            UtahClearingHouseAuthorityList.retrieveCertificates();
            System.out.flush();
            System.err.flush();
        }


        SfaAuthority usedGeniAuthority = null;
        String pemKeyCert = null;
        char[] pass = null;
        GeniUser user = null;
        GeniConnection con = null;

        if (contextProperties == null) {
            boolean asked = false;

            if (serverUrl == null) {
                if (auth != null) {
                    for (SfaAuthority knownAuth : authorityListModel.getAuthorities()) {
                        if (knownAuth.getHrn().equals(auth)) {
                            serverUrl = knownAuth.getUrl(serverType).toString();
                            usedGeniAuthority = knownAuth;
                        }
                    }
                    if (serverUrl == null) {
                        System.err.println("ERROR: Server URL for authority '"+auth+"' and server type "+apiArg+" is unknown.");
                        System.exit(-1);
                    }
                }
                if (serverUrl == null) {
                    assert interactive;
                    serverUrl = IOUtils.askCommandLineInput("Server URL");
                    asked = true;
                }
            }
            if (usedGeniAuthority == null) {
                //find authority for server
                URL server = new URL(serverUrl);
                String serverHostname = server.getHost();
                for (SfaAuthority knownAuth : authorityListModel.getAuthorities()) {
                    for (Map.Entry<ServerType, URL> e : knownAuth.getUrls().entrySet()) {
                        if (usedGeniAuthority == null && e.getValue() != null && e.getValue().getHost().equals(serverHostname)) {
                            usedGeniAuthority = knownAuth;
                        }
                    }
                }
            }
            if (usedGeniAuthority == null) {
                System.out.println("WARNING: No known authority matches server URL. Will check if certificate is trusted.");

                SSLCertificateDownloader.SSLCertificateJFedInfo info = SSLCertificateDownloader.getCertificateInfo(new URL(serverUrl));

                SfaAuthority fakeAuthority = new SfaAuthority("urn:publicid:IDN+fake+authority+fake");
                fakeAuthority.setSource(null);
                fakeAuthority.setHrn("Temporarily created fake authority for URL " + serverUrl);
                fakeAuthority.setType("temp-fake-unknown");
                fakeAuthority.setUrl(serverType, new URL(serverUrl));
                System.out.println("Note: Unknown authority, so created temporary fake authority info, in order to connect to server.");

                if (info.isSelfSigned()) {
                    //TODO currently this assumes the certificate is not trusted, but we don't know that for sure. We should check...

                    boolean trust = acceptSelfSigned;
                    if (!trust) {
                        if (!interactive) {
                            System.err.println("FATAL: The server's self-signed certificate is not trusted! Either add the authority to the list, or add a command line option to trust any self signed certificate (dangerous).");
                            System.exit(1);
                        }
                        System.out.println("Note: The server's self-signed certificate is not trusted! Either add the authority to the list, or trust it temporarily. Certificate:");
                        System.out.println(KeyUtil.x509certificateToPem(info.getCert()));
                        assert interactive;
                        String trustSelfSignedCert = IOUtils.askCommandLineInput("Trust self-signed certificate (Unsafe!)? (y/N)");
                        trust = trustSelfSignedCert.equalsIgnoreCase("y");
                    }

                    if (trust) {
                        fakeAuthority.setPemSslTrustCert(info.getCert());
                        if (!info.getSubjectMatchesHostname())
                            fakeAuthority.addAllowedCertificateHostnameAlias(info.getSubject());
                    } else {
                        System.err.println("FATAL: the server's certificate is not trusted. Connecting would fail.");
                        System.exit(1);
                    }
                }
                usedGeniAuthority = fakeAuthority;
            }
            assert usedGeniAuthority != null;

            if (pemKeyCertFilename == null) {
                File defaultPemKeyCertFile = new File(System.getProperty("user.home")+ File.separator+".ssl"+File.separator+"geni_cert.pem");
                assert interactive;
                pemKeyCertFilename = IOUtils.askCommandLineInput("PEM key and certificate filename (default: \""+defaultPemKeyCertFile.getPath()+"\")");
                asked = true;
                if (pemKeyCertFilename == null || pemKeyCertFilename.equals(""))
                    pemKeyCertFilename = defaultPemKeyCertFile.getPath();
            }

            if (asked) {
                if (!silent)
                    System.out.println("Full commandline parameter set would be:  -k "+pemKeyCertFilename+" -u "+serverUrl+" -api "+apiArg+" "+command);
            }

            if (!interactive) {
                System.err.println("FATAL: Not in interactive mode, so password cannot be requested.");
                System.exit(-1);
            }
            assert interactive;
            pass = IOUtils.askCommandLinePassword("Key password");
            pemKeyCert = IOUtils.fileToString(pemKeyCertFilename);

            user = new SimpleGeniUser(null, null, pemKeyCert, pass); //hack: userAuth not known, but not needed either

            assert usedGeniAuthority != null;
            con = new GeniSslConnection(usedGeniAuthority, serverUrl, user.getCertificate(), user.getPrivateKey(), false, null/*handleUntrustedCallback*/);
        } else {
            File testContextFile = new File(contextProperties);
            if (!testContextFile.exists()) throw new FileNotFoundException("Cannot find Context properties file: "+contextProperties);

            CommandExecutionContext context = CommandExecutionContext.loadFromFile(testContextFile);
            usedGeniAuthority = context.getTestedAuthority();
            user = context.getGeniUser();

            if (!useTestedSA &&
                    (serverType.getRole() == ServerType.GeniServerRole.PlanetLabSliceRegistry ||
                            serverType.getRole() == ServerType.GeniServerRole.PROTOGENI_SA)) {
                if (!silent)
                    System.out.println("useTestedSA="+useTestedSA+" => using user authority instead of tested authority!");
                usedGeniAuthority = user.getUserAuthority();
            }

            assert usedGeniAuthority != null;
            assert serverType != null;
            assert usedGeniAuthority.getUrl(serverType) != null : "Authority "+usedGeniAuthority.getUrn()+" has no URL for "+serverType;
            serverUrl = usedGeniAuthority.getUrl(serverType).toString();

            if (!silent) {
                System.out.println("Read context properties from file \"" + contextProperties + "\":");
                if (usedGeniAuthority != context.getTestedAuthority())
                    System.out.println("   To test Authority: "+context.getTestedAuthority().getName());
                else
                    System.out.println("   Tested Authority: "+context.getTestedAuthority().getName());
                System.out.println("      URN: "+context.getTestedAuthority().getUrn());
                System.out.println("      Hrn: "+context.getTestedAuthority().getHrn());
                System.out.println("      Server certificate: "+context.getTestedAuthority().getPemSslTrustCert());
                System.out.println("      allowed server certificate hostname aliases: "+context.getTestedAuthority().getAllowedCertificateHostnameAliases());
                if (usedGeniAuthority != context.getTestedAuthority()) {
                    System.out.println("   Actual contacted authority: "+usedGeniAuthority.getName());
                    System.out.println("      URN: "+usedGeniAuthority.getUrn());
                    System.out.println("      Server certificate: "+usedGeniAuthority.getPemSslTrustCert());
                    System.out.println("      Allowed server certificate hostname aliases: "+usedGeniAuthority.getAllowedCertificateHostnameAliases());
                }
                System.out.println("      URL for "+serverType+": "+serverUrl);
                System.out.println("   User: "+user.getUserUrn());
                System.out.println("      Authority URN: "+user.getUserAuthority().getUrn());
            }

            con = context.getConnectionProvider().getConnectionByAuthority(context.getGeniUser(), usedGeniAuthority, serverType);
        }

        assert (con != null);

        if (!silent)
            System.out.println("Connecting and executing "+command+"...");

        int exitstatus = 0;
        CommandLineCore.ExecuteMethodResult res = core.executeMethod(con, command, arguments, interactive);

        if (!silent && outputfile == null)
            System.out.println(apiArg+" "+command+" result: ");
        if (!silent && outputfile != null)
            System.out.println("Writing "+apiArg+" "+command+" result to file \""+outputfile+"\"");

        if (outputfile == null)
            System.out.println(res.output);
        else {
            IOUtils.stringToFile(new File(outputfile), res.output);
        }

        if (!silent && debug) {
            System.out.println("DEBUG:");
            System.out.println("  Server URL: "+con.getServerUrl());
            System.out.println("  HTTP sent: "+con.getXmlRpcTransportFactory().getHttpSentHistory());
            System.out.println("  HTTP reply: "+con.getXmlRpcTransportFactory().getHttpReceivedHistory());
        }

        System.exit(res.exitvalue);
    }
}
