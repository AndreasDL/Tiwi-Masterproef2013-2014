package be.iminds.ilabt.jfed.ui.javafx.choosers;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.*;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.ui.javafx.util.ExpandableHelp;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import be.iminds.ilabt.jfed.util.SSLCertificateDownloader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;

/**
 * ServerUrlChooser
 */
public class ServerUrlChooser {
    @FXML private RadioButton useUserRadioButton;
    @FXML private RadioButton userAuthorityRadioButton;
    @FXML private RadioButton userCustomServerRadioButton;

    @FXML private HBox userAuthChoiceBox;
    @FXML private AuthorityChooser authChooser;

    @FXML private Label loggedInUserLabel;

    @FXML private HBox fixedServerURLBox;
    @FXML private VBox editableServerURLBox;

    @FXML private TextField serverUrlField;

    @FXML private TextField customServerUrlField;
    @FXML private CheckBox ignoreSelfSignedCheckBox;
    @FXML private Label ignoreSelfSignedCheckLabel;


    private EasyModel easyModel;


    public ServerUrlChooser() {
        URL location = getClass().getResource("ServerUrlChooser.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assert useUserRadioButton != null;

        //if any of these is set invisible, remove them from layout as well
        useUserRadioButton.managedProperty().bind(useUserRadioButton.visibleProperty());
        userAuthChoiceBox.managedProperty().bind(userAuthChoiceBox.visibleProperty());
        fixedServerURLBox.managedProperty().bind(fixedServerURLBox.visibleProperty());
        editableServerURLBox.managedProperty().bind(editableServerURLBox.visibleProperty());
        ignoreSelfSignedCheckLabel.managedProperty().bind(ignoreSelfSignedCheckLabel.visibleProperty());
        loggedInUserLabel.managedProperty().bind(loggedInUserLabel.visibleProperty());

        //bind radiobuttons to visible regions
        userAuthChoiceBox.visibleProperty().bind(userAuthorityRadioButton.selectedProperty().or(useUserRadioButton.selectedProperty()));
        fixedServerURLBox.visibleProperty().bind(userAuthorityRadioButton.selectedProperty().or(useUserRadioButton.selectedProperty()));
        editableServerURLBox.visibleProperty().bind(userCustomServerRadioButton.selectedProperty());

        //bind other logical connections
        loggedInUserLabel.visibleProperty().bind(useUserRadioButton.selectedProperty());
        authChooser.editableProperty().bind(userAuthorityRadioButton.selectedProperty());
        ignoreSelfSignedCheckLabel.visibleProperty().bind(ignoreSelfSignedCheckBox.selectedProperty());
        useUserRadioButton.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldIsVisible, Boolean newIsVisible) {
                if (!newIsVisible && useUserRadioButton.isSelected())
                    userAuthorityRadioButton.selectedProperty().set(true);
            }
        });
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    //TODO: implement this if it is needed outside Probe

//    public void setEasyModel(EasyModel easyModel) {
//        assert this.easyModel == null;
//        this.easyModel = easyModel;
//
//        assert authChooser != null;
//
//        assert easyModel != null;
//
//        authChooser.setEasyModel(easyModel);
//
//        authChooser.selectedAuthorityProperty().addListener(new ChangeListener<AuthorityInfo>() {
//            @Override
//            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthorityInfo, AuthorityInfo newAuthorityInfo) {
//                updateFixedServerUrlField(currentlySelectedTreeApi());
//            }
//        });
//        useUserRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldBool, Boolean newIsSelected) {
//                if (newIsSelected) updateFixedServerUrlField(currentlySelectedTreeApi());
//                if (newIsSelected && easyModel.getGeniUserProvider().isUserLoggedIn())
//                    authChooser.select(easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority());
//            }
//        });
//        userAuthorityRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldBool, Boolean newIsSelected) {
//                if (newIsSelected) updateFixedServerUrlField(currentlySelectedTreeApi());
//            }
//        });
//    }
//
//    public void updateServerUrlChoiceOnApiChange(AbstractApi newSelectedAbstractApi) {
//        boolean isSA = newSelectedAbstractApi == null ? false : newSelectedAbstractApi.getServerType().getRole().equals(ServerType.GeniServerRole.PROTOGENI_SA);
//
//        if (true) {
//            if (easyModel.getGeniUserProvider().isUserLoggedIn()) {
//                loggedInUserLabel.setText("User "+easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserUrn()+"");
//                boolean wasInvisible = !useUserRadioButton.isVisible();
//                useUserRadioButton.setVisible(true);
//                if (wasInvisible && isSA)
//                    useUserRadioButton.setSelected(true);
//            }
//            else {
//                useUserRadioButton.setVisible(false);
//                //loggedInUserLabel will not be visible now, we update text anyway
//                loggedInUserLabel.setText("No User logged in.");
//            }
//        }
//        else {
//            useUserRadioButton.setVisible(false);
//            //loggedInUserLabel will not be visible now, we update text anyway
//            loggedInUserLabel.setText("Not using user's auth server URL");
//        }
//
//        updateFixedServerUrlField(newSelectedAbstractApi);
//    }
//
//    public AuthorityInfo getSelectedAuthority() {
//
//    }
//    public URL getServerURL() {
//
//    }
//
//    private GeniConnectionProvider connectionProvider = new GeniConnectionPool();
//    public GeniConnection getConnectionToServer() {
//        if (useUserRadioButton.isSelected()) {
//            return connectionProvider.getConnectionByUserAuthority(geniUser, calledApi.getServerType());
//        }
//        if (userAuthorityRadioButton.isSelected()) {
//            return connectionProvider.getConnectionByAuthority(geniUser, authChooser.getSelectedAuthority().getGeniAuthority(), calledApi.getClass());
//        }
//        if (userCustomServerRadioButton.isSelected()) {
//            System.out.println("SECURITY WARNING: making connection which accepts all certificates");
//            ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback;
//            if (ignoreSelfSignedCheckBox.isSelected())
//                handleUntrustedCallback = new ClientSslAuthenticationXmlRpcTransportFactory.INSECURE_TRUSTALL_HandleUntrustedCallback();
//            else {
//                handleUntrustedCallback = new ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback() {
//                    @Override
//                    public boolean trust(SSLCertificateDownloader.SSLCertificateJFedInfo sslCertificateJFedInfo) {
//                        if (sslCertificateJFedInfo.isTrusted()) return true;
//
//                        String problemDescription = "";
//                        if (sslCertificateJFedInfo.isSelfSigned()) {
//                            problemDescription += "The server's certificate is self signed. Certificate info:\n";
//                            for (X509Certificate cert : sslCertificateJFedInfo.getChain()) {
//                                problemDescription += ""+cert.toString();
//                                problemDescription += "\n\n";
//                            }
//                        }
//                        if (!sslCertificateJFedInfo.getSubjectMatchesHostname()) {
//                            if (!problemDescription.equals(""))
//                                problemDescription += "\n\nADDITIONAL SECURITY PROBLEM:\n";
//                            problemDescription += "The certificate's subject hostname does not match the server URL:\n";
//                            problemDescription += "    Certificate Subject: "+sslCertificateJFedInfo.getSubject()+"\n";
//                            problemDescription += "    Server Hostname: "+sslCertificateJFedInfo.getHostname()+"\n";
//                        }
//                        return JavaFXDialogUtil.show2ChoiceDialog(problemDescription,
//                                "I know what I am doing, I checked the certificate manually, and I trust the server",
//                                "I do not trust this",
//                                userCustomServerRadioButton);
//                    }
//                };
//            }
//            return connectionProvider.getConnectionByUrl(geniUser, new URL(customServerUrlField.getText()), handleUntrustedCallback);
//        }
//    }
}
