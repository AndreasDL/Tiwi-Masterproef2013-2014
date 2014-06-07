package be.iminds.ilabt.jfed.ui.javafx.userlogin;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.AuthorityList;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.*;
import be.iminds.ilabt.jfed.ui.javafx.about_gui.AboutBoxController;
import be.iminds.ilabt.jfed.ui.javafx.am_list_gui.AMList;
import be.iminds.ilabt.jfed.ui.javafx.choosers.AuthorityChooser;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import be.iminds.ilabt.jfed.ui.userlogin_gui.UserLoginPanel;
import be.iminds.ilabt.jfed.ui.x509certificate_gui.X509CertificatePanel;
import be.iminds.ilabt.jfed.util.GeniUrn;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UserLoginController
 */
public class UserLoginController implements Initializable {
    private AuthorityList authorityList;
    private AuthorityListModel authorityListModel;
    private EasyModel easyModel;
    private UserLoginModelManager userLoginModel;
    private Logger logger;

    private boolean requireAllUserInfo = true;

    private List<UserLoginPanel.ConfigPanelCloseHandler> configPanelCloseHandlers;


    public UserLoginController() { }

    public void setRequireAllUserInfo(boolean requireAllUserInfo) {
        this.requireAllUserInfo = requireAllUserInfo;
    }

    private void setAuthorityListModel(AuthorityList authorityList) {
        this.authorityList = authorityList;
        this.authorityListModel = authorityList.getAuthorityListModel();

        boolean blockModelToGUI_eventhelper_origState = blockModelToGUI_eventhelper;
        blockModelToGUI_eventhelper = true;
        if (defaultLoginPanelSliceAuthNameChooser != null)
            defaultLoginPanelSliceAuthNameChooser.setEasyModel(easyModel);
        if (planetlabAuthChooser != null)
            planetlabAuthChooser.setEasyModel(easyModel);
        blockModelToGUI_eventhelper = blockModelToGUI_eventhelper_origState;
    }
    private void setEasyModel(EasyModel easyModel) {
        this.easyModel = easyModel;

        setAuthorityListModel(easyModel.getAuthorityList());
    }
    public AuthorityListModel getAuthorityListModel() {
        return authorityListModel;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setUserLoginModel(UserLoginModelManager userLoginModel) {
        assert easyModel != null : "initialize EasyModel first";
        this.userLoginModel = userLoginModel;
        modelToGUI();
    }

    @FXML private Parent defaultLoginPanel;
    @FXML private Parent planetLabLoginPanel;

    @FXML private RadioButton pemKeyCertInputFromFileRadioButton;
    @FXML private RadioButton pemKeyCertInputFromStringRadioButton;
    @FXML private Button pemKeyCertInputFromFileLoadButton;
    @FXML private HBox defaultLoginPanelPasswordBox;
    @FXML private Button defaultLoginPanelPasswordButton;
    @FXML private TextArea defaultLoginPanelPemField;
    @FXML private TextField defaultLoginPanelPassword;
    @FXML private TextField pemKeyCertInputFromFileField;
    @FXML private HBox pemKeyCertInputFromFileBox;

    @FXML private Label defaultLoginPanelSliceAuthName;
    @FXML private Label defaultLoginPanelSliceAuthUrn;
    @FXML private Label defaultLoginPanelUsername;
    @FXML private Label defaultLoginPanelUserUrn;

    @FXML private Button loginButton;
    @FXML private Button logoutButton;
    @FXML private Label errorLogLabel;
    @FXML private TextArea errorLog;


    @FXML private Label missingInfoWarningLabel;
    @FXML private Label missingInfoErrorLabel;


    @FXML private Parent derivedCertificateDetails;
    @FXML private Parent manualCertificateDetails;

    @FXML private AuthorityChooser defaultLoginPanelSliceAuthNameChooser;
    @FXML private TextField defaultLoginPanelUserUrnField;

    @FXML private RadioButton loginTypeKeyCertIntRadioButton;
    @FXML private RadioButton loginTypeKeyCertExtRadioButton;
    @FXML private RadioButton loginTypePlanetlabRadioButton;




    @FXML private AuthorityChooser planetlabAuthChooser;
    @FXML private Label planetlabAuthFeedbackLabel;
    @FXML private TextField planetlabSfaHrnField;
    @FXML private TextField planetlabFileField;
    @FXML private Parent planetlabDetailsBox;
    @FXML private TextArea planetlabDetailsText;

    @FXML private Parent planetlabPasswordBox;
    @FXML private PasswordField planetlabPasswordField;

    @FXML private Label planetlabCertificateFeedbackLabel;
    @FXML private Button planetlabCreateCertificateButton;


    private final boolean linked = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        defaultLoginPanelPasswordBox.managedProperty().bind(defaultLoginPanelPasswordBox.visibleProperty());
        pemKeyCertInputFromFileBox.managedProperty().bind(pemKeyCertInputFromFileBox.visibleProperty());
        errorLogLabel.managedProperty().bind(errorLogLabel.visibleProperty());
        errorLog.managedProperty().bind(errorLog.visibleProperty());
        derivedCertificateDetails.managedProperty().bind(derivedCertificateDetails.visibleProperty());
        manualCertificateDetails.managedProperty().bind(manualCertificateDetails.visibleProperty());
        missingInfoWarningLabel.managedProperty().bind(missingInfoWarningLabel.visibleProperty());
        missingInfoErrorLabel.managedProperty().bind(missingInfoErrorLabel.visibleProperty());
        planetLabLoginPanel.managedProperty().bind(planetLabLoginPanel.visibleProperty());
        defaultLoginPanel.managedProperty().bind(defaultLoginPanel.visibleProperty());

        planetlabPasswordBox.managedProperty().bind(planetlabPasswordBox.visibleProperty());
        planetlabDetailsBox.managedProperty().bind(planetlabDetailsBox.visibleProperty());
        planetlabCreateCertificateButton.managedProperty().bind(planetlabCreateCertificateButton.visibleProperty());
        planetlabCertificateFeedbackLabel.managedProperty().bind(planetlabCertificateFeedbackLabel.visibleProperty());
        assert planetlabDetailsText != null;


        errorLog.setMaxHeight(Control.USE_PREF_SIZE);

        if (easyModel != null) {
            defaultLoginPanelSliceAuthNameChooser.setEasyModel(easyModel);
            planetlabAuthChooser.setEasyModel(easyModel);
        }

        defaultLoginPanelPemField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
                if (blockModelToGUI_eventhelper) return;

                onPemEdit();
            }
        });

        defaultLoginPanelSliceAuthNameChooser.selectedAuthorityProperty().addListener(new ChangeListener<AuthorityInfo>() {
            @Override
            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthInfo, AuthorityInfo newAuthInfo) {
                if (blockModelToGUI_eventhelper) return;

                userLoginModel.getKeyCertWithManualInfoUserLoginModel().setAuthority(newAuthInfo.getGeniAuthority());

                if (linked && !userLoginModel.getKeyCertWithManualInfoUserLoginModel().isValidUserUrn()) {
                    GeniUrn authUrn = GeniUrn.parse(newAuthInfo.getGeniAuthority().getUrn());
                    if (authUrn != null) {
                        GeniUrn templateUrn = new GeniUrn(authUrn.getTopLevelAuthority(), "user", "<username>");
                        defaultLoginPanelUserUrnField.setText(templateUrn.toString());
                    }
                }
            }
        });
        defaultLoginPanelUserUrnField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldUrn, String newUrn) {
                if (blockModelToGUI_eventhelper) return;

                userLoginModel.getKeyCertWithManualInfoUserLoginModel().setUserUrn(newUrn);
                if (userLoginModel.getKeyCertWithManualInfoUserLoginModel().deriveAuthorityFromUrn()) {
                    SfaAuthority auth = userLoginModel.getKeyCertWithManualInfoUserLoginModel().getUserAuthority();
                    if (auth != null && linked) {
                        boolean blockModelToGUI_eventhelper_origState = blockModelToGUI_eventhelper;
                        blockModelToGUI_eventhelper = true;
                        defaultLoginPanelSliceAuthNameChooser.select(auth);
                        blockModelToGUI_eventhelper = blockModelToGUI_eventhelper_origState;
                    }
                }
            }
        });


        planetlabAuthChooser.selectedAuthorityProperty().addListener(new ChangeListener<AuthorityInfo>() {
            @Override
            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthInfo, AuthorityInfo newAuthInfo) {
                if (blockModelToGUI_eventhelper) return;
                PlanetlabUserLoginModel m = userLoginModel.getPlanetlabUserLoginModel();

                m.setAuthority(newAuthInfo.getGeniAuthority());
                modelToGUI_derived();
            }
        });

        planetlabSfaHrnField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldHrn, String newHrn) {
                if (blockModelToGUI_eventhelper) return;

                PlanetlabUserLoginModel m = userLoginModel.getPlanetlabUserLoginModel();
                m.setPlanetlabSfaHrn(newHrn);

                //might have updated user authority
                if (m.getUserAuthority() != null && m.getUserAuthority() != planetlabAuthChooser.getSelectedAuthority().getGeniAuthority()) {
                    boolean blockModelToGUI_eventhelper_origState = blockModelToGUI_eventhelper;
                    blockModelToGUI_eventhelper = true;
                    planetlabAuthChooser.select(m.getUserAuthority());
                    blockModelToGUI_eventhelper = blockModelToGUI_eventhelper_origState;
                }

                modelToGUI_derived();
            }
        });
    }


    @FXML private void selectLoginType() {
        if (blockModelToGUI_eventhelper) return;

        UserLoginModelManager.UserLoginModelType origType = userLoginModel.getUserLoginModelType();

        KeyCertUserLoginModel mInt = userLoginModel.getKeyCertUserLoginModel();
        KeyCertWithManualInfoUserLoginModel mExt = userLoginModel.getKeyCertWithManualInfoUserLoginModel();

        if (loginTypeKeyCertIntRadioButton.isSelected()) {
            userLoginModel.setUserLoginModelType(UserLoginModelManager.UserLoginModelType.KEY_CERT_INTERNAL_INFO);

            if (origType == UserLoginModelManager.UserLoginModelType.KEY_CERT_EXTERNAL_INFO) {
                //convert
                if (mExt.isFromFile())
                    mInt.setKeyCertPemFile(mExt.getKeyCertFile());
                else
                    mInt.setKeyCertPemString(mExt.getKeyCertContent());
            }
        }
        if (loginTypeKeyCertExtRadioButton.isSelected()) {
            userLoginModel.setUserLoginModelType(UserLoginModelManager.UserLoginModelType.KEY_CERT_EXTERNAL_INFO);

            if (origType == UserLoginModelManager.UserLoginModelType.KEY_CERT_INTERNAL_INFO) {
                //convert
                if (mInt.isFromFile())
                    mExt.setKeyCertPemFile(mInt.getKeyCertFile());
                else
                    mExt.setKeyCertPemString(mInt.getKeyCertContent());
                mExt.setAuthority(mInt.getUserAuthority());
                mExt.setUserUrn(mInt.getUserUrn());
            }
        }
        if (loginTypePlanetlabRadioButton.isSelected())
            userLoginModel.setUserLoginModelType(UserLoginModelManager.UserLoginModelType.PLANETLAB);

        modelToGUI();
    }

    private void showLoginType(UserLoginModelManager.UserLoginModelType type) {
        switch (type) {
            case KEY_CERT_INTERNAL_INFO: {
                defaultLoginPanel.setVisible(true);
                planetLabLoginPanel.setVisible(false);
                derivedCertificateDetails.setVisible(true);
                manualCertificateDetails.setVisible(false);
                break;
            }
            case KEY_CERT_EXTERNAL_INFO: {
                defaultLoginPanel.setVisible(true);
                planetLabLoginPanel.setVisible(false);
                derivedCertificateDetails.setVisible(false);
                manualCertificateDetails.setVisible(true);
                break;
            }
            case PLANETLAB: {
                defaultLoginPanel.setVisible(false);
                planetLabLoginPanel.setVisible(true);
                break;
            }
        }
    }

    /**
     * Copy settings from userLoginModel to GUI
     */
    private boolean blockModelToGUI_eventhelper = false;
    protected void modelToGUI() {
        if (blockModelToGUI_eventhelper) return;
        boolean blockModelToGUI_eventhelper_origState = blockModelToGUI_eventhelper;
        blockModelToGUI_eventhelper = true; //don't let it trigger modelToGUI() again, as we're doing it already

        showLoginType(userLoginModel.getUserLoginModelType());
        modelToGUI_input();
        modelToGUI_derived();
        blockModelToGUI_eventhelper = blockModelToGUI_eventhelper_origState;
    }
    protected void modelToGUI_input() {
        if (userLoginModel == null) return;

        switch(userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: { loginTypeKeyCertIntRadioButton.setSelected(true); break; }
            case KEY_CERT_EXTERNAL_INFO: { loginTypeKeyCertExtRadioButton.setSelected(true); break; }
            case PLANETLAB:              { loginTypePlanetlabRadioButton.setSelected(true);  break; }
        }

        switch(userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_EXTERNAL_INFO: //fall-through
            case KEY_CERT_INTERNAL_INFO: {
                KeyCertUserLoginModel m = null;
                switch( userLoginModel.getUserLoginModelType()) {
                    case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
                    case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
                    default: return;
                }

                if (m.isFromFile()) {
                    String pemkeycertfilename = m.getKeyCertFile() == null ? null : m.getKeyCertFile().getPath();
                    pemKeyCertInputFromFileField.setText(pemkeycertfilename);
                    pemKeyCertInputFromFileRadioButton.setSelected(true);
                    defaultLoginPanelPemField.setEditable(false);
                } else {
                    pemKeyCertInputFromFileField.setText("");
                    pemKeyCertInputFromStringRadioButton.setSelected(true);
                    defaultLoginPanelPemField.setEditable(true);
                }

                pemKeyCertInputFromFileBox.setVisible(m.isFromFile());

                if (m.getKeyCertContent() != null)
                    defaultLoginPanelPemField.setText(m.getKeyCertContent());
                else
                    defaultLoginPanelPemField.setText("");
                break;
            }
            case PLANETLAB: {
                PlanetlabUserLoginModel m = userLoginModel.getPlanetlabUserLoginModel();
                if (m.getUserAuthority() != null) {
                    boolean blockModelToGUI_eventhelper_origState = blockModelToGUI_eventhelper;
                    blockModelToGUI_eventhelper = true;
                    planetlabAuthChooser.select(m.getUserAuthority());
                    blockModelToGUI_eventhelper = blockModelToGUI_eventhelper_origState;
                }
//                else
//                    planetlabAuthChooser.select(null); //not allowed: must always select something
                planetlabFileField.setText(m.getSshPrivateKeyFile().getPath());
                boolean blockModelToGUI_eventhelper_origState = blockModelToGUI_eventhelper;
                blockModelToGUI_eventhelper = true;
                planetlabSfaHrnField.setText(m.getSfaHrn());
                blockModelToGUI_eventhelper = blockModelToGUI_eventhelper_origState;
                break;
            }
        }

    }

    private static enum ValidationStyle { ERROR, WARNING, OK, EXPLICIT_OK };
    private static void setValidationStyle(Parent p, ValidationStyle validationStyle) {
        String styleName = null;
        switch (validationStyle) {
            case ERROR:       { styleName = "validation_error"; break; }
            case WARNING:     { styleName = "validation_warning"; break; }
            case OK:          { styleName = null; break; }
            case EXPLICIT_OK: { styleName = "validation_explicit_ok"; break; }
            default: throw new RuntimeException("Unsupported ValidationStyle");
        }

        List<String> otherStyles = new ArrayList<String>();
        otherStyles.add("validation_error");
        otherStyles.add("validation_warning");
        otherStyles.add("validation_explicit_ok");

        if (styleName != null)
            otherStyles.remove(styleName);

        p.getStyleClass().removeAll(otherStyles);
        if (styleName != null)
            if (!p.getStyleClass().contains(styleName))
                p.getStyleClass().add(styleName);
    }

    protected void modelToGUI_derived() {
        switch (userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: {
                KeyCertUserLoginModel m = userLoginModel.getKeyCertUserLoginModel();

                defaultLoginPanelUserUrn.setText(m.getUserUrn());
                GeniUrn userUrn = GeniUrn.parse(m.getUserUrn());
                if (userUrn != null)
                    defaultLoginPanelUsername.setText(userUrn.getResourceName());
                else
                    defaultLoginPanelUsername.setText("");
                defaultLoginPanelSliceAuthUrn.setText(m.getUserAuthorityUrn());
                if (m.getUserAuthority() != null)
                    defaultLoginPanelSliceAuthName.setText(m.getUserAuthority().getName());
                else {
                    defaultLoginPanelSliceAuthName.setText("Authority not known");
                }
                defaultLoginPanelPasswordBox.setVisible(m.isPasswordRequired());
                break;
            }
            case KEY_CERT_EXTERNAL_INFO: {
                KeyCertWithManualInfoUserLoginModel m = userLoginModel.getKeyCertWithManualInfoUserLoginModel();

                defaultLoginPanelUserUrnField.setText(m.getUserUrn());

                defaultLoginPanelSliceAuthNameChooser.select(m.getUserAuthority());

                defaultLoginPanelPasswordBox.setVisible(m.isPasswordRequired());
                break;
            }
            case PLANETLAB: {
                PlanetlabUserLoginModel m = userLoginModel.getPlanetlabUserLoginModel();

                //set key correctness, password label, hrn correctness, authority correctness and details and show planetlabCreateCertificateButton
                if (m.correctPlanetlabSfaUrn())
                    setValidationStyle(planetlabSfaHrnField, ValidationStyle.EXPLICIT_OK);
                else
                    setValidationStyle(planetlabSfaHrnField, ValidationStyle.ERROR);

                boolean privatekeyReadable = m.getPrivateKeyContent() != null;
                boolean privatekeyUnlocked = m.getPrivateKey() != null;
                if (privatekeyReadable) {
                    if (privatekeyUnlocked) {
                        setValidationStyle(planetlabFileField, ValidationStyle.EXPLICIT_OK);
                        planetlabPasswordBox.setVisible(false);
                    }
                    else {
                        setValidationStyle(planetlabFileField, ValidationStyle.WARNING);
                        planetlabPasswordBox.setVisible(true);
                    }
                } else {
                    setValidationStyle(planetlabFileField, ValidationStyle.ERROR);
                    planetlabPasswordBox.setVisible(false);
                }

                if (m.getDetails() != null && !m.getDetails().isEmpty()) {
                    planetlabDetailsText.setText(m.getDetails());
                    planetlabDetailsBox.setVisible(true);
                }
                else {
                    planetlabDetailsText.setText("");
                    planetlabDetailsBox.setVisible(false);
                }

                planetlabCreateCertificateButton.setVisible(true);
                if (m.isReadyToFetchCertificate()) {
                    planetlabCreateCertificateButton.setDisable(false);
//                    planetlabCreateCertificateButton.setVisible(true);
                } else {
                    planetlabCreateCertificateButton.setDisable(true);
//                    planetlabCreateCertificateButton.setVisible(false);
                }

                if (m.getCertificate() == null) {
                    if (m.isReadyToFetchCertificate()) {
                        planetlabCertificateFeedbackLabel.setText("No certificate - All data available to fetch certificate");
                        setValidationStyle(planetlabCertificateFeedbackLabel, ValidationStyle.WARNING);
                    } else {
                        planetlabCertificateFeedbackLabel.setText("No certificate - Missing data needed to fetch certificate");
                        setValidationStyle(planetlabCertificateFeedbackLabel, ValidationStyle.ERROR);
                    }
                } else {
                    planetlabCertificateFeedbackLabel.setText("Certificate present");
                    setValidationStyle(planetlabCertificateFeedbackLabel, ValidationStyle.EXPLICIT_OK);
                }

                boolean correctAuth = m.correctAuthority();
                if (correctAuth) {
                    planetlabAuthFeedbackLabel.setText("OK");
                    setValidationStyle(planetlabAuthFeedbackLabel, ValidationStyle.EXPLICIT_OK);
                } else {
                    setValidationStyle(planetlabAuthFeedbackLabel, ValidationStyle.ERROR);
                    if (m.getUserAuthority() == null)
                        planetlabAuthFeedbackLabel.setText("no selection");
                    else
                        planetlabAuthFeedbackLabel.setText("Authority is missing URL for Planetlab Sfa Registry");
                }

                break;
            }
        }

        UserLoginModel m = userLoginModel.getCurrentUserLoginModel();
        boolean missingInfo = m.getUserAuthority() == null || m.getUserUrn() == null;
        if (requireAllUserInfo) {
            missingInfoWarningLabel.setVisible(false);
            missingInfoErrorLabel.setVisible(m.isUserLoggedIn() && missingInfo);
        } else {
            missingInfoWarningLabel.setVisible(m.isUserLoggedIn() && missingInfo);
            missingInfoErrorLabel.setVisible(false);
        }

        updateErrorLog();
        updateLoginbuttonEnabled();
    }

    public void updateFileOrString() {
        System.out.println("updateFileOrString blockModelToGUI_eventhelper="+blockModelToGUI_eventhelper+"  modelType="+userLoginModel.getUserLoginModelType());
        if (blockModelToGUI_eventhelper) return;

        KeyCertUserLoginModel m = null;
        switch( userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
            case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
            default: return;
        }

        assert pemKeyCertInputFromFileRadioButton.isSelected() != pemKeyCertInputFromStringRadioButton.isSelected();

        boolean newIsfromFile = pemKeyCertInputFromFileRadioButton.isSelected();

        if (newIsfromFile) {
            if (m.isFromString())
                m.setKeyCertPemFile(null);
        } else {
            if (m.isFromFile())
                m.setKeyCertPemString(defaultLoginPanelPemField.getText());
        }

        modelToGUI();
    }

    public void onPemEdit() {
        if (blockModelToGUI_eventhelper) return;

        KeyCertUserLoginModel m = null;
        switch( userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
            case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
            default: return;
        }

        if (m.isFromString()) {
            String text = defaultLoginPanelPemField.getText();
            assert text != null;
            m.setKeyCertPemString(text);
            modelToGUI();
        } else
            System.err.println("WARNING: assumption bug in onPemEdit()");
    }

    public void showCertificateDetailsWindow() {
        KeyCertUserLoginModel m = null;
        switch( userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
            case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
            default: return;
        }

        String keyCertContent = m.getKeyCertContent();
        if (keyCertContent != null) {
            String pemCert = keyCertContent; //showManualCommandPan uses KeyUtil.pemToX509Certificate which will only look at the CERTIFICATE and ignore the rest
            X509CertificatePanel.showManualCommandPan(pemCert, true);
        }
    }

    public void unlockAndLogin() {
        if (unlock() && loginAllowed())
            login();
    }

    public boolean unlock() {
        final char[] password = defaultLoginPanelPassword.getText().toCharArray();

        KeyCertUserLoginModel m = null;
        switch( userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
            case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
            default: return false;
        }

        boolean success = m.unlock(password);
        updateLoginbuttonEnabled();
        updateErrorLog();
        return success;
    }

    public void planetlabFetchCertificate() {
        PlanetlabUserLoginModel m = userLoginModel.getPlanetlabUserLoginModel();

        //recreate certificate if exists   if (m.getCertificate() != null) return;

        if (m.isPasswordRequired() && m.getPrivateKey() == null && m.getPrivateKeyContent() != null) {
            if (!planetLabUnlock()) return;
        }

        if (!m.isReadyToFetchCertificate()) return;

        boolean success = m.fetchCertificate();

        modelToGUI_derived();
    }

//    public void planetLabUnlockAndLogin() {
//        if (planetLabUnlock() && loginAllowed())
//            login();
//    }

    public boolean planetLabUnlock() {
        final char[] password = planetlabPasswordField.getText().toCharArray();
        boolean success = userLoginModel.getPlanetlabUserLoginModel().unlock(password);
        modelToGUI_derived();
        return success;
    }

    private boolean loginAllowed() {
        UserLoginModel m = userLoginModel.getCurrentUserLoginModel();

        if (!m.isUserLoggedIn()) return false; // is bare minimum supplied?

        boolean missingInfo = m.getUserAuthority() == null || m.getUserUrn() == null;
        if (m.isUserLoggedIn()) {
            if (!missingInfo || !requireAllUserInfo)
                return true;
            else
                return false;
        }
        else
            return false;
    }

    public void updateLoginbuttonEnabled() {
        loginButton.setDisable(!loginAllowed());
    }

    public void updateErrorLog() {
        KeyCertUserLoginModel m = null;

        switch( userLoginModel.getUserLoginModelType()) {
            case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
            case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
            default: return;
        }

        assert m != null;

        String errors = m.getErrorInfo();
        if (errors == null || errors.equals("")) {
            errorLogLabel.setVisible(false);
            errorLog.setVisible(false);
            return;
        } else {
            errorLogLabel.setVisible(true);
            errorLog.setVisible(true);

            errorLog.setText(errors);
        }
    }

    public void login() {
        if (!loginAllowed()) return;
        boolean success = userLoginModel.login();
        if (success) {
            if (userLoginModel.hasChanged()) {
                boolean save = JavaFXDialogUtil.show2ChoiceDialog("Save login details now? (password is never saved)", "Save", "Don't Save", loginButton);
                if (save)
                    userLoginModel.save();
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    userloginStage.close();
                }
            });
        }
    }
    public void nologin() {
        userLoginModel.logout();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userloginStage.close();
            }
        });
    }









    public void saveConfig() {
        //Note: password is never saved
        userLoginModel.save();
    }

    public void loadConfig() {
        userLoginModel.load();
        modelToGUI();
    }

    public void resetConfig() {
        userLoginModel.reset();
        modelToGUI();
    }

    public UserLoginModelManager getUserLoginModel() {
        return userLoginModel;
    }




    @FXML private void loadCertificateFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            KeyCertUserLoginModel m = null;
            switch( userLoginModel.getUserLoginModelType()) {
                case KEY_CERT_INTERNAL_INFO: { m = userLoginModel.getKeyCertUserLoginModel(); break; }
                case KEY_CERT_EXTERNAL_INFO: { m = userLoginModel.getKeyCertWithManualInfoUserLoginModel(); break; }
                default: return;
            }

            m.setKeyCertPemFile(file);
        }

        modelToGUI();
    }
    @FXML private void loadPlanetlabFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            userLoginModel.getPlanetlabUserLoginModel().setSshPrivateKeyFile(file);
        }

        modelToGUI();
    }


    @FXML private void showAbout() {
        AboutBoxController.showAbout();
    }

    @FXML private void editTestbeds() {
        AMList.editAuthorityList(authorityList, userLoginModel);
    }

    private static Stage userloginStage = null;
    public static void showUserLogin(Logger logger, /*AuthorityList authorityList,*/EasyModel easyModel, UserLoginModelManager userLoginModel, boolean requireAllUserInfo) {
        try {
            if (userloginStage == null) {
                URL location = UserLoginController.class.getResource("UserLogin.fxml");
                assert location != null;
                FXMLLoader fxmlLoader = new FXMLLoader(location, null);

                BorderPane root = (BorderPane)fxmlLoader.load();
                UserLoginController controller = (UserLoginController)fxmlLoader.getController();

                controller.setRequireAllUserInfo(requireAllUserInfo);
                controller.setLogger(logger);
//                controller.setAuthorityListModel(authorityList);
                controller.setEasyModel(easyModel);
                controller.setUserLoginModel(userLoginModel);

                Scene scene = new Scene(root);

                userloginStage = new Stage();
                userloginStage.setScene(scene);
            }
            assert userloginStage != null;
            userloginStage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong showing the User Login Screen: "+e.getMessage(), e);
        }
    }
}
