package be.iminds.ilabt.jfed.ui.userlogin_gui;


import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import be.iminds.ilabt.jfed.lowlevel.planetlab.PlanetlabCertificateFetcher;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.UserLoginModelManager;
import be.iminds.ilabt.jfed.ui.authority_choice_gui.AuthorityChoicePanel;
import be.iminds.ilabt.jfed.ui.x509certificate_gui.X509CertificatePanel;
import be.iminds.ilabt.jfed.util.DialogUtils;
import be.iminds.ilabt.jfed.util.IOUtils;
import thinlet.FrameLauncher;
import thinlet.Thinlet;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This panel allows editing of UserLoginModel: everything related to a user login
 *
 * You can listen to it to be informed of successful logins.
 * */
public class UserLoginPanel extends Thinlet implements GeniUserProvider, AuthorityProvider {
    public interface ConfigPanelCloseHandler {
        public void closeConfig();
    }

    private AuthorityListModel authorityListModel;

    private UserLoginModelManager userLoginModel;
    private UserLoginModelManager storedUserLoginModel;

    private AuthorityChoicePanel authorityChoicePanel;

    private Object passwordField;
    private Object userurnField;
    private Object saurnField;
    private Object pemkeycertfilenameField;
    private Object pemkeycertField;
    private Object clearingHouseUrlField;
    private Object manualoverrideCheckBox;

    private File guiShownFile;

    private boolean ready;

    private List<ConfigPanelCloseHandler> configPanelCloseHandlers;

    private GeniUser loggedInGeniUser;

    public void addCloseHandler(ConfigPanelCloseHandler configPanelCloseHandler) {
        this.configPanelCloseHandlers.add(configPanelCloseHandler);
    }

    public UserLoginPanel(AuthorityListModel authorityListModel/*ConfigPanelCloseHandler configPanelCloseHandler*/, Logger logger) {
        configPanelCloseHandlers = new ArrayList<ConfigPanelCloseHandler>();
        //this.configPanelCloseHandler = configPanelCloseHandler;
        this.loggedInGeniUser = null;
        this.authorityListModel = authorityListModel;
        this.userLoginModel = new UserLoginModelManager(this.authorityListModel, logger);
        this.userLoginModel.load();
        this.storedUserLoginModel = userLoginModel;

        this.authorityChoicePanel = new AuthorityChoicePanel(this, this.authorityListModel, logger, this);
        this.guiShownFile = null;

        try {
            add(parse("UserLoginConfigPanel.xml", this));
            passwordField = find("password");
            saurnField = find("saurn");
            userurnField = find("userurn");
            pemkeycertfilenameField = find("pemkeycertfilename");
            pemkeycertField = find("pemkeycert");
            clearingHouseUrlField = find("clearingHouseUrl");
            manualoverrideCheckBox = find("manualoverride");
        } catch (Exception e) {
            throw new RuntimeException("Error creating UserLoginConfigPanel: "+e, e);
        }

        assert passwordField != null;
        assert saurnField != null;
        assert userurnField != null;
        assert pemkeycertfilenameField != null;
        assert pemkeycertField != null;
//        assert clearingHouseUrlField != null;
        assert manualoverrideCheckBox != null;

        //password not filled in, so not ready yet
        ready = false;

        updateGui();
    }

    public AuthorityListModel getAuthorityListModel() {
        return authorityListModel;
    }

    /**
     * Copy settings from userLoginModel to GUI
     */
    protected void updateGui() {
//        boolean manualOverride = userLoginModel.isOverride();
//        setBoolean(manualoverrideCheckBox, "selected", manualOverride);
//
//        String pemkeycertfilename = userLoginModel.getKeyCertFile().getPath();
//        guiShownFile = new File(pemkeycertfilename);
//        setString(pemkeycertfilenameField, "text", pemkeycertfilename);
//        setString(pemkeycertField, "text", userLoginModel.getKeyCertContent());
//
//        setString(userurnField, "text", userLoginModel.getUserUrn());
//        setString(saurnField, "text", userLoginModel.getUserAuthorityUrn());
//        if (userLoginModel.getUserAuthority() != null)
//            authorityChoicePanel.setSelectedAuthority(userLoginModel.getUserAuthority());
//        else {
//            System.err.println("Warning: could not show auth");
//            //no auth!
//            if (authorityListModel.getAuthorities().size() == 0)
//                UtahClearingHouseAuthorityList.load(authorityListModel);
//            authorityChoicePanel.setSelectedAuthority(authorityListModel.getAuthorities().get(0));
//        }
//
//        //don't show label here, so we can align it ourselves in GUI
//        if (manualOverride){
//            authorityChoicePanel.setAll("User Slice Authority: "/*labelText*/, false/*showLabel*/, true/*selectable*/, true/*editable*/, true/*addable*/);
//            setBoolean(userurnField, "editable", true);
//        } else {
//            authorityChoicePanel.setAll("User Slice Authority: "/*labelText*/, false/*showLabel*/, false/*selectable*/, false/*editable*/, false/*addable*/);
//            setBoolean(userurnField, "editable", false);
//        }
    }

    public void onManualOverride(boolean selected) {
        updateUserLoginPreferences();
        updateGui();
    }

    private SfaAuthority getSelectedGeniAuthority() {
        return authorityChoicePanel.getAuthority();
    }

    /**
     * Copy settings from GUI to userLoginModel
     * */
    protected void updateUserLoginPreferences() {
//        boolean manualOverride = getBoolean(manualoverrideCheckBox, "selected");
//
//        if (manualOverride) {
//            SfaAuthority auth = getSelectedGeniAuthority();
//            assert auth != null;
//            //currently does not allow editing of authority urn, you need to select and authority from the list instead
//            userLoginModel.set(guiShownFile, auth, getString(userurnField, "text"));
//
////            if (clearingHouseUrlField != null)
////                userLoginModel.setClearingHouseUrl(getString(clearingHouseUrlField, "text"));
//        } else {
//                userLoginModel.set(guiShownFile);
//        }
    }

    public void showCertificateDetailsWindow() {
//        updateUserLoginPreferences();
//        String keyCertContent = userLoginModel.getKeyCertContent();
//        if (keyCertContent != null) {
//            String pemCert = keyCertContent; //showManualCommandPan uses KeyUtil.pemToX509Certificate which will only look at the CERTIFICATE and ignore the rest
//            X509CertificatePanel.showManualCommandPan(pemCert, true);
//        }
    }

    public void begin() {
//        updateUserLoginPreferences();
//
//        final char[] password = getString(passwordField, "text").toCharArray();
//
//        boolean ok = true;
//        if (password.length == 0) {
////            DialogUtils.errorMessage("Cannot continue: Password is not entered");
////            ok = false;
////            return;
//            System.err.println("Warning: empty password used");
//        }
//
//        //test if key and cert given
//        String keyCertContent = userLoginModel.getKeyCertContent();
//        if (keyCertContent == null || !keyCertContent.startsWith("-----BEGIN")) {
//            DialogUtils.errorMessage("Cannot continue: Key and Certificate are not loaded: "+keyCertContent);
//            ok = false;
//            return;
//        }
//
//        try {
//            if (!userLoginModel.loadCertificateAndKey(password)){
//                DialogUtils.errorMessage("Cannot continue: error reading key and certificate");
//                ok = false;
//            }
//        } catch (Exception e) {
//            if (password.length == 0)
//                DialogUtils.errorMessage("Cannot continue: unexpected error reading key. You have not used a password, perhaps the key requires a password -> "+e.getMessage());
//            else
//                DialogUtils.errorMessage("Cannot continue: Password is incorrect (or other unexpected error reading key) -> "+e.getMessage());
//            ok = false;
//        }
////        try {
////            PrivateKey privateKey = KeyUtil.pemToRsaPrivateKey(keyCertContent, password);
////            if (privateKey == null)
////                throw new GeniException("ERROR: PEM key and certificate does not contain a key:"+keyCertContent);
////        } catch (Exception e) {
////            DialogUtils.errorMessage("Cannot continue: Password is incorrect (or other unexpected error reading key) -> "+e.getMessage());
////            ok = false;
////        }
//
//        //check if userUrn, userAuth and userName are known
//        if (userLoginModel.getUserUrn() == null || userLoginModel.getUserAuthority() == null) {
//            if (!userLoginModel.isOverride()) {
//                List<String> notfound = new ArrayList<String>();
//                if (userLoginModel.getUserUrn() == null) notfound.add("user URN");
//                if (userLoginModel.getUserAuthority() == null) notfound.add("user slice authority");
//                assert notfound.size() > 0;
//                String nf = notfound.get(0);
//                for (int i = 1; i < notfound.size(); i++)
//                    nf += ", "+notfound.get(i);
//                if (ok
//                        && userLoginModel.getUserUrn() != null &&
//                        userLoginModel.getCertificate() != null && userLoginModel.getPrivateKey() != null) {
//                    DialogUtils.errorMessage("Problem: your Slice Authority and/or Aggregate Manager is unknown. Please add it first, and try again...");
//
//                    authorityChoicePanel.showWizard(userLoginModel.getCertificate(), userLoginModel.getPrivateKey(), new AuthorityChoicePanel.WizardCallback() {
//                        @Override
//                        public void onWizardResult(SfaAuthority authority) {
//                            if (authority == null) {
//                                DialogUtils.errorMessage("Cannot continue: Cannot derive user slice authority. You'll need to use manual override to specify it.");
//                            } else {
//                                //just begin again
//                                System.out.println("Added an authority with certificate: "+authority.getPemSslTrustCert());
//                                begin();
//                            }
//                        }
//                    });
//                    return;
//                } else {
//                    if (ok)
//                        DialogUtils.errorMessage("Cannot continue: Cannot derive "+nf+". You'll need to use manual override to specify it.");
//                    ok = false;
//                }
//            } else {
//                //override yet not all given!
//                DialogUtils.errorMessage("INTERNAL ERROR: username, userUrn and auth overridden, yet not all overridden by GUI.");
//                ok = false;
//            }
//
//
//            //check if SA known for the authority
//            if (userLoginModel.getUserAuthority() != null) {
//                if (userLoginModel.getUserAuthority().getUrl(new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1)) == null) {
//                    DialogUtils.errorMessage("WARNING: Your Slice Authority's XmlRpc URL is unknown. You have limited functionality without it.\n" +
//                            "We suggest you add it. This potential problem will be ignored for now.");
//                }
//            }
//        }
//
//        /* if no errors yet, Check for any other missing info */
//        if (ok == true && !userLoginModel.isValid()){
//            DialogUtils.errorMessage("Cannot continue: not all user login info known");
//            ok = false;
//        }
//
//        //check if changes need to be saved
//        if (ok && !userLoginModel.equals(storedUserLoginModel)) {
//            boolean save = DialogUtils.getYesNoAnswer("Save preferences now? (password is never saved)");
//            if (save) {
//                userLoginModel.save();
//                this.storedUserLoginModel = this.userLoginModel;
//            }
//        }
//
//        if (ok) {
//            //store a copy
//            loggedInGeniUser = new SimpleGeniUser(userLoginModel);
//            ready = true;
//
//            configFl.setVisible(false);
//            for (ConfigPanelCloseHandler configPanelCloseHandler : configPanelCloseHandlers)
//                 configPanelCloseHandler.closeConfig();
//
//            //only let listeners know if login is ok
//            fireOnLogin();
//        } else {
//            loggedInGeniUser = null;
//            ready = false;
//        }
//
//        userLoginModel.fireChange();
    }

    @Override
    public SfaAuthority getAuthority() {
        return userLoginModel.getUserAuthority();
    }

    @Override
    public GeniUser getLoggedInGeniUser() {
        if (loggedInGeniUser == null) throw new RuntimeException("loggedInGeniUser == null");
        return loggedInGeniUser;
    }

    @Override
    public boolean isUserLoggedIn() {
        return loggedInGeniUser != null;
    }

    public void chooseAndLoadFile(Object pemkeycertfilename, Object pemkeycert) {
//        final JFileChooser fc = new JFileChooser();
//        fc.setFileHidingEnabled(false); //== show hidden files
//        int returnVal = fc.showOpenDialog(null);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File file = fc.getSelectedFile();
//            setString(pemkeycertfilename, "text", file.getPath());
//            setString(pemkeycert, "text", UserLoginModelManager.loadPemFile(file));
//            guiShownFile = file;
//
//            updateUserLoginPreferences();
//            updateGui();
//        } else {
//        }
    }

    public void saveConfig() {
        updateUserLoginPreferences();
        //Note: password is never saved
        userLoginModel.save();
        this.storedUserLoginModel = this.userLoginModel;
    }

    public void loadConfig() {
        userLoginModel.load();
        this.storedUserLoginModel = this.userLoginModel;
        updateGui();
    }

    public void resetConfig() {
        DialogUtils.infoMessage("Internal default settings restored. These have not been saved yet.");
        userLoginModel.reset();
        updateGui();
    }

    public UserLoginModelManager getUserLoginModel() {
        return userLoginModel;
    }

    public void initAuthChoicePanel(Object thinletAuthChoicePanel) {
        add(thinletAuthChoicePanel, authorityChoicePanel.getThinletPanel());
    }


    public boolean isReady() {
        return ready;
    }



    public interface UserLoginListener {
        public void onUserLoggedIn(/*UserLoginPanel userLoginConfigPanel*/GeniUser geniUser);
    }
    private List<UserLoginListener> listeners = new ArrayList<UserLoginListener>();
    public void addUserLoginConfigPanelListener(UserLoginListener listener) {
        listeners.add(listener);
    }
    public void fireOnLogin() {
        for (UserLoginListener l : listeners)
            l.onUserLoggedIn(getLoggedInGeniUser());
    }


    //TODO: this needs a nice wizard panel instead of a number of popup questions
    public void planetlabWizard() {

        //outdated, see jafaFX version
//        DialogUtils.infoMessage("Note: Currently, only PlanetLab Europe accounts are supported.");
//
//        final JFileChooser fc = new JFileChooser();
//        fc.setDialogTitle("Please select you PlanetLab Europe PRIVATE KEY file.");
//        int returnVal = fc.showOpenDialog(null);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File privKeyFile = fc.getSelectedFile();
//            if (!privKeyFile.exists()) {
//                DialogUtils.errorMessage("File does not exist. Cancelling.");
//                return;
//            }
//            try {
//                String privKeyPem = IOUtils.fileToString(privKeyFile);
//                if (privKeyPem == null || !privKeyPem.contains("PRIVATE KEY")) {
//                    DialogUtils.errorMessage("File is not a valid private key file. Cancelling.");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                DialogUtils.errorMessage("Error reading file (see console output). Cancelling.");
//                return;
//            }
//            char[] password = DialogUtils.getPassword("Enter the password for the private key (if any).");
//            String username = DialogUtils.getString("Enter your planetlab username. (typically in the form \"<firstname>_<lastname>\")");
//
//            //TODO request authority
//            AuthorityListModel authListModel = JFedAuthorityList.getAuthorityListModel();
////            AuthorityListModel authListModel = new AuthorityListModel();
////            BuiltinAuthorityList.load(authListModel);
//            SfaAuthority ple = authListModel.getByUrn("urn:publicid:IDN+ple:ibbtple+authority+cm");
//
//            SfaAuthority planetLabAuth = ple;
//            String pemkeycert = PlanetlabCertificateFetcher.createSignedClientCertificateAndPrivateKeyPem(planetLabAuth, username, privKeyFile, password);
//            if (pemkeycert == null) {
//                DialogUtils.errorMessage("Failed to retrieve certificate from planetlab. See console output for details.");
//                return;
//            }
//
//            final JFileChooser fc2 = new JFileChooser();
//            fc2.setDialogTitle("Please select a file to save the retrieved PEM private key + certificate file.");
//            int returnVal2 = fc2.showSaveDialog(null);
//            if (returnVal2 == JFileChooser.APPROVE_OPTION) {
//                File savedFile = fc2.getSelectedFile();
//                IOUtils.stringToFile(savedFile, pemkeycert);
//
//                setString(pemkeycertfilenameField, "text", savedFile.getPath());
//                setString(pemkeycertField, "text", pemkeycert);
//                guiShownFile = savedFile;
//
//                updateUserLoginPreferences();
//                updateGui();
//            } else {
//                System.out.println("You did not want to save your file. The content of the file would have been:\n" + pemkeycert);
//            }
//        } else {
//            System.out.println("Planetlab wizard cancelled.");
//        }
    }


    private FrameLauncher configFl = null;
    public void showConfigFrame() {
        if (configFl == null)
            configFl = new FrameLauncher("jFed User Log In", this, 800, 600) {
                public void	windowClosed(WindowEvent e) { }
                public void	windowClosing(WindowEvent e) {
                    //TODO ask confirmation of quit
                    //System.out.println("Exiting application.");
                    System.exit(0);
                    /*begin();*/
                }
            };
        else
            configFl.setVisible(true);
    }
}
