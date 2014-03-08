package be.iminds.ilabt.jfed.util;

import be.iminds.ilabt.jfed.log.PrintlnResultLogger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.UserLoginModelManager;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import org.testng.Reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * CommandExecutionContext: all info and helper classes needed to execute SA and AM commands
 */
public class CommandExecutionContext {
    private GeniUser geniUser;
    private SfaAuthority userAuthority;
    private SfaAuthority testedAuthority;
    private GeniConnectionProvider connectionProvider;
    private Logger logger;

    public CommandExecutionContext(GeniUser geniUser, SfaAuthority userAuthority, SfaAuthority testedAuthority, Logger logger) {
        this.geniUser = geniUser;
        this.userAuthority = userAuthority;
        this.testedAuthority = testedAuthority;
        this.logger = logger;
        connectionProvider = new GeniConnectionPool();
    }
    public CommandExecutionContext(String passwordFilename, String pemKeyAndCertFilename, String userAuthorityUrn, GeniUrn userUrn, String testedAggregateManagerUrn) {

        this.logger = new Logger();
        logger.addResultListener(new PrintlnResultLogger());

        //First try builtin info
//        AuthorityListModel authorityListModel = new AuthorityListModel();
//        BuiltinAuthorityList.load(authorityListModel);
        AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();

        if (authorityListModel.getByUrn(userAuthorityUrn) == null ||
                authorityListModel.getByUrn(testedAggregateManagerUrn) == null) {
            System.out.println("Could not find all authorities from context file, will retrieve list from Utah Emulab Clearinghouse...");

            //If that is not enough, get info from clearinghouse
            UtahClearingHouseAuthorityList.load(authorityListModel);
            UtahClearingHouseAuthorityList.retrieveCertificates();
        }

        this.userAuthority = authorityListModel.getByUrn(userAuthorityUrn);
        this.testedAuthority = authorityListModel.getByUrn(testedAggregateManagerUrn);
        if (userAuthority == null || testedAuthority == null) {
            System.err.println("FATAL ERROR in CommandExecutionContext:");
            if (userAuthority == null) System.err.println("   Could not find user authority: urn="+userAuthorityUrn);
            if (testedAuthority == null) System.err.println("   Could not find tested authority: urn="+testedAggregateManagerUrn);
            System.err.println("All known authorities:");
            for (SfaAuthority auth : authorityListModel.getAuthorities())
                System.err.println("  urn=" + auth.getUrn() + "   name=" + auth.getName());
            System.exit(-1);
        }
        assert(testedAuthority != null);
        assert(userAuthority != null);

        String pemKeyAndCert = null;
        try {
            pemKeyAndCert = IOUtils.fileToString(pemKeyAndCertFilename);
        } catch (IOException e) {
            pemKeyAndCert = null;
        }
//        String pemKeyAndCert = UserLoginModelManager.loadPemFile(new File(pemKeyAndCertFilename));
        assert pemKeyAndCert != null;
        String password = null;
        try {
            password = IOUtils.fileToString(passwordFilename).replaceAll("\n", "");
        } catch (IOException e) {
            Reporter.log("Error in TestContext: "+e.getMessage());
            throw new RuntimeException("Error reading password from file \""+passwordFilename+"\"", e);
        }
        assert password != null;
        geniUser = new SimpleGeniUser(userAuthority, userUrn, pemKeyAndCert, password.toCharArray());

        if (userAuthority.getPemSslTrustCert() != null)
            GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(userAuthority.getPemSslTrustCert());
        if (testedAuthority.getPemSslTrustCert() != null)
            GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(testedAuthority.getPemSslTrustCert());

        connectionProvider = new GeniConnectionPool();
    }

    public Logger getLogger() {
        return logger;
    }

    public GeniUser getGeniUser() {
        return geniUser;
    }

    public SfaAuthority getUserAuthority() {
        return userAuthority;
    }

    public SfaAuthority getTestedAuthority() {
        return testedAuthority;
    }

    public GeniConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }



    public static CommandExecutionContext loadFromFile(File file) throws IOException {
        Properties prop = new Properties();

        prop.load(new FileInputStream(file));

        String passwordFilename = prop.getProperty("passwordFilename");
        String userUrn = prop.getProperty("userUrn");
        String pemKeyAndCertFilename = prop.getProperty("pemKeyAndCertFilename");
        String userAuthorityUrn = prop.getProperty("userAuthorityUrn");
        String testedAggregateManagerUrn = prop.getProperty("testedAggregateManagerUrn");

        if (userAuthorityUrn == null) throw new RuntimeException("property \"userAuthorityUrn\" is missing in Test Context properties file \""+file.getName()+"\"");
        if (passwordFilename == null) throw new RuntimeException("property \"passwordFilename\" is missing in Test Context properties file \""+file.getName()+"\"");
        if (pemKeyAndCertFilename == null) throw new RuntimeException("property \"pemKeyAndCertFilename\" is missing in Test Context properties file \""+file.getName()+"\"");
        if (testedAggregateManagerUrn == null) throw new RuntimeException("property \"testedAggregateManagerUrn\" is missing in Test Context properties file \""+file.getName()+"\"");

        if (userUrn == null) {
            //TODO get from optional (and previously mandatory) "username" property
            String username = prop.getProperty("username");
            if (username == null) throw new RuntimeException("property \"userUrn\" (and alternative \"username\") is missing in Test Context properties file \""+file.getName()+"\"");
            GeniUrn authUrn = GeniUrn.parse(userAuthorityUrn);
            assert authUrn != null;
            userUrn = new GeniUrn(authUrn.getTopLevelAuthority(), "user", username).toString();
        }

        if (userUrn == null) throw new RuntimeException("property \"userUrn\" is missing in Test Context properties file \""+file.getName()+"\"");

        GeniUrn geniUserUrn;
        try {
            geniUserUrn = new GeniUrn(userUrn);
        } catch (GeniUrn.GeniUrnParseException e) {
            throw new RuntimeException("property \"userUrn\" is not an urn \""+userUrn+"\"", e);
        }

        CommandExecutionContext res = new CommandExecutionContext(passwordFilename, pemKeyAndCertFilename, userAuthorityUrn, geniUserUrn, testedAggregateManagerUrn);
        return res;
    }
}
