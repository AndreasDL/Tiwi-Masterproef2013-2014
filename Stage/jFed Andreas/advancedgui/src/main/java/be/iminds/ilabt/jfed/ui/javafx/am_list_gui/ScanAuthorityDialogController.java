package be.iminds.ilabt.jfed.ui.javafx.am_list_gui;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityList;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.ui.javafx.advanced_gui.JFedAdvancedGuiController;
import be.iminds.ilabt.jfed.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author wim
 */
public class ScanAuthorityDialogController implements Initializable {
    @FXML private BorderPane pane;
    @FXML private TextField amUrlField;
    @FXML private HBox yesNoButtons;
    @FXML private HBox postScanButtons;
    @FXML private HBox scanLogBox;
    @FXML private TextArea scanLogTextArea;
    @FXML private ProgressIndicator progress;
    @FXML private Button scanButton;
    @FXML private Button cancelButton;

    private Stage dialog;

    private AuthorityList authorityList;
    private GeniUserProvider geniUserProvider;
    private void setAuthorityList(AuthorityList authorityList) {
        this.authorityList = authorityList;
    }
    private void setGeniUserProvider(GeniUserProvider geniUserProvider) {
        this.geniUserProvider = geniUserProvider;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String validatorCss = getClass().getResource("validation.css").toExternalForm();
        pane.getStylesheets().add(validatorCss);

        amUrlField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
                validate();
            }
        });

        scanLogTextArea.textProperty().bind(scanLogText);
    }

    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

    public void cancel() {
        amUrlField.setText("");
        dialog.close();
    }

    public void scanFail() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                amUrlField.setDisable(false);
                scanButton.setDisable(false);
                cancelButton.setDisable(false);
                scanLogBox.setVisible(true);
                progress.setVisible(false);
                yesNoButtons.setVisible(false);
                postScanButtons.setVisible(false);
            }
        });
    }

    private StringProperty scanLogText = new SimpleStringProperty("");
    private void log(final String line) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (scanLogText.get().isEmpty())
                    scanLogText.set(line);
                else
                    scanLogText.set(scanLogText.get()+"\n"+line);

                //move caret to the end
                scanLogTextArea.end();
            }
        });
    }

    private boolean answer;
    private boolean yesNoQuestion(String line) {
        log("=================================================================");
        log("INPUT REQUIRED: "+line);
        log("=================================================================");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                amUrlField.setDisable(true);
                scanButton.setDisable(true);
                cancelButton.setDisable(true);
                scanLogBox.setVisible(true);
                progress.setVisible(true);
                yesNoButtons.setVisible(true);
                postScanButtons.setVisible(false);
            }
        });

        //TODO block until answered
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return answer;
    }

    public void yes() {
        answer = true;

        //TODO unblock yesNoQuestion
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void no() {
        answer = true;

        //TODO unblock yesNoQuestion
        synchronized (this) {
            this.notifyAll();
        }
    }

    Thread scanThread = null;
    public void scan() {
        final String urlString = amUrlField.getText();

        amUrlField.getStyleClass().removeAll("validation-error", "validation-warning");
        try {
            URL test = new URL(urlString);
        } catch (MalformedURLException e) {
            log("Not starting scan: invalid URL \"" + urlString + "\"");
            amUrlField.getStyleClass().add("validation-error");
            return;
        }

        if (scanThread != null) {
            log("Cannot scan: previous scan still active.");
            return;
        }

        amUrlField.setDisable(true);
        scanButton.setDisable(true);
        cancelButton.setDisable(true);
        scanLogBox.setVisible(true);
        progress.setVisible(true);
        yesNoButtons.setVisible(false);
        postScanButtons.setVisible(false);

        Runnable scanner = new Runnable() {
            @Override
            public void run() {
                performScan(urlString);
            }
        };
        Thread scanThread = new Thread(scanner);
        scanThread.start();
    }

    private SfaAuthority wizardCreatedGeniAuthority = null;
    /* Should be called in seperate Thread */
    private void performScan(String urlString) {
        log("Starting scan for \""+urlString+"\"");

        URL u = null;
        wizardCreatedGeniAuthority = null;
        boolean unknownUrn = false;
        boolean callSuccess = false;
        try {
            u = new URL(urlString);

            SSLCertificateDownloader.SSLCertificateJFedInfo info = SSLCertificateDownloader.getCertificateInfo(u);
            log("Note: got certificate info. checking...");

            String urn = info.getUrn();
            if (info.getUrn() == null) {
                log("The URN of the server could not be determined from the certificate. Using placeholder URN for now.");
                urn = "urn:publicid:IDN+"+u.getHost()+"+authority+cm";
                unknownUrn = true;
            }

            assert info != null;

            boolean cancel = info.getCert() == null;

            if (cancel) {
                log("Problem fetching certificate from server. Cancelling wizard.");
                scanFail();
                return;
            }

            Map<ServerType, URL> urlMap = new HashMap<ServerType, URL>();
            urlMap.put(new ServerType(ServerType.GeniServerRole.AM, 2), u);
            //TODO: We could also make a first guess based on url
            //Note: already, this will (possibly, depending on errors) be overwritten later
            wizardCreatedGeniAuthority = new SfaAuthority(urn, info.getSubject(), urlMap, null, null);
            log("DEBUG: created wizardCreatedGeniAuthority");

            if (!cancel && info.isSelfSigned()) {
                log("Note: server uses self signed certificate");
                wizardCreatedGeniAuthority.setPemSslTrustCert(info.getCert());

                if (yesNoQuestion("Server certificate is self signed. Trust anyway?") == false) { //TODO show info to help the user decide
                    log("ERROR: cancelling wizard: no trust");
                    cancel = true;
                } else
                    log("User trusts self signed certificate.");

                if (!cancel && !info.getSubjectMatchesHostname()) {
                    log("Note: Server certificate subject name is not for \""+u.getHost()+"\" but for \""+info.getSubject()+"");
                    wizardCreatedGeniAuthority.addAllowedCertificateHostnameAlias(info.getSubject());

                    if (yesNoQuestion("Server certificate subject name is not for \""+u.getHost()+"\" but for \""+info.getSubject()+"\". Trust anyway?") == false) {
                        log("ERROR: cancelling wizard: no trust");
                        cancel = true;
                    } else
                        log("User trusts alias in certificate.");
                }
            }
            log("DEBUG: SSL certificate info ok. cancel=" + cancel + " isUserLoggedIn=" + (geniUserProvider == null ? "null" : (geniUserProvider.isUserLoggedIn()+"")));

            if (!cancel && geniUserProvider != null && geniUserProvider.isUserLoggedIn()) {
                log("Note: Now trying to connect to call GetVersion to get all AM versions URLs...");

                try {
                    if (info.isSelfSigned() && info.getCert() != null) {
                        GeniTrustStoreHelper.addTrustedCertificate(info.getCert());

                        //TODO: these todo's are probably out of date, check this code again!

                        //TODO code below would probably be better, but  pemToX509Certificate doesn't work yet
                        //TODO or alternatively, make addTrustedCertificate ignore duplicates
//                                            String pemCert = KeyUtil.x509certificateToPem(info.getCert());
                        //                    System.out.println("Note: adding certificate to trust store:\n"+pemCert);
//                                            X509Certificate reconstructedCert = KeyUtil.pemToX509Certificate(pemCert);
                        //                    assert reconstructedCert != null;
                        //                    assert info.getCert() != null;
                        //                    assert reconstructedCert.equals(info.getCert());
                        //                    GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(pemCert);
                    }

                    //contact server using AM API(v2 but it doesn't really matter) and use GetVersion to get correct AM urls...
                    AggregateManager2 am2 = new AggregateManager2(authorityList.getEasyModel().getLogger(), true);
                    assert wizardCreatedGeniAuthority != null;
                    AggregateManager2.AggregateManagerReply<AggregateManager2.VersionInfo> versionInfoReply =
                            am2.getVersion(
                                    new GeniSslConnection(
                                            wizardCreatedGeniAuthority,
                                            urlString,
                                            geniUserProvider.getLoggedInGeniUser().getCertificate(),
                                            geniUserProvider.getLoggedInGeniUser().getPrivateKey(),
                                            // wizardCreatedGeniAuthority.getAllowedCertificateHostnameAliases(),
                                            false, null/*handleUntrustedCallback*/)
                            );
                    callSuccess = true;
                    log("  Processing GetVersion result : \n"+ XmlRpcPrintUtil.printXmlRpcResultObject(versionInfoReply.getRawResult()));
                    AggregateManager2.VersionInfo versionInfo = versionInfoReply.getValue();

                    String newHrn = versionInfo.getExtra("hrn");
                    if (newHrn != null)
                        wizardCreatedGeniAuthority.updateHrn(newHrn);

                    String newUrn = versionInfo.getExtra("urn");
                    if (newUrn != null) {
                        wizardCreatedGeniAuthority.updateUrn(newUrn);
                        unknownUrn = false;
                    }

                    urlMap = new HashMap<ServerType, URL>();
                    for (AggregateManager2.VersionInfo.VersionPair apiVersion: versionInfo.getApiVersions()) {
                        try {
                            URL versionUrl =  new URL(apiVersion.getUrl());
                            if (versionUrl.getHost().equals("localhost")) {
                                log("WARNING: Aggregate Manager returned URL containing \"localhost\" in GetVersion result: "+apiVersion.getUrl()+"   (automatically fixing url using the URL you supplied)");
                                versionUrl =  new URL(apiVersion.getUrl().replace("localhost", u.getHost()));
                            }
                            if (versionUrl.getProtocol().equals("http")) {
                                log("Note: Server gave URL with http instead of https: \"" + apiVersion.getUrl() + "\"");

                                if (yesNoQuestion("Server gave URL with http instead of https: \""+apiVersion.getUrl()+"\". This probably needs to be \"https\". Change it to \"https\"?") == true) {
                                    versionUrl =  new URL(versionUrl.toString().replace("http://", "https://"));
                                }
                            }
                            urlMap.put(new ServerType(ServerType.GeniServerRole.AM, apiVersion.getVersionNr()), versionUrl);
                        } catch (MalformedURLException e) {
                            log("Error: Aggregate Manager returned malformed URL in GetVersion result: \""+apiVersion.getUrl()+"\"");
                        }
                    }
                    if (urlMap.isEmpty()) {
                        log("ERROR: There are no valid AM URLs in GetVersion reply! Will use fallback.");

                        //TODO perhaps check if advertised URL for this version is same as known.

                        //fall back to version at this URL only (should be known when GetVersion successful)
                        int versionNr = versionInfo.getApi();
                        ServerType st = new ServerType(ServerType.GeniServerRole.AM, versionNr);
                        urlMap.put(st, u);
                        wizardCreatedGeniAuthority.replaceUrls(urlMap);

                        //no reason to cancel
                        // cancel = true;
                    } else
                        wizardCreatedGeniAuthority.replaceUrls(urlMap);
                } catch (GeniException ex) {
                    Throwable e = ex;
                    boolean httperror = false;
                    while (e.getCause() != null) {
                        e = e.getCause();
                        if (e instanceof CommonsHttpClientXmlRpcTransportFactory.HttpServerErrorException) {
                            CommonsHttpClientXmlRpcTransportFactory.HttpServerErrorException httpErrorException = (CommonsHttpClientXmlRpcTransportFactory.HttpServerErrorException) e;

                            log("The server was replied with HTTP error "+
                                    httpErrorException.getStatusNr()+" \""+httpErrorException.getReason()+
                                    "\" while trying a GetVersion call. Retry scanning later?");
                            if (ex.getXmlRpcResult() != null && ex.getXmlRpcResult() != null) {
                                log("HTTP reply details:\n" + ex.getXmlRpcResult().getResultHttpContent());
                            }
                            httperror = true;
                            //                            wizardCreatedGeniAuthority = null;
                            callSuccess = false;
                        }
                    }
                    if (!httperror) {
                        ex.printStackTrace(System.out);
                        System.out.flush();
                        System.err.println("Error while executing or processing GetVersion. Retreived info will not be used.");
                        callSuccess = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    System.out.flush();
                    System.err.println("Error while executing or processing GetVersion. Retreived info will not be used.");
                    callSuccess = false;
                }

                if (!cancel && !callSuccess) {
                    if (yesNoQuestion("There was an error when trying a \"GetVersion\" call. Continue adding anyway?") == false) {
                        log("ERROR: cancelling wizard: error in GetVersion is considered fatal by user.");
                        cancel = true;
                    }
                }
            } else {
                if (!cancel)
                    log("Warning: No user is currently logged in, so a GetVersion call cannot be done. This limits the info that can be scanned.");
            }

            if (cancel) {
                wizardCreatedGeniAuthority = null;
                log("ERROR: Cancelling wizard, perhaps try again using a different URL?");
            }
        } catch (Exception e) {
            log("ERROR: Something went wrong trying to get Aggregate Manager info: "+e.getMessage());
            e.printStackTrace();
            if (wizardCreatedGeniAuthority != null &&
                    yesNoQuestion("Something went wrong trying to get Aggregate Manager info: \""+e.getMessage()+"\". Continue adding anyway?") == false) {
                log("ERROR: cancelling wizard: error considered fatal by user.");
                wizardCreatedGeniAuthority = null;
            }
        }

        if (wizardCreatedGeniAuthority != null) {
            log("Scan completed successfully.\n");
            //success, let user press OK
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    amUrlField.setDisable(false);
                    scanButton.setDisable(false);
                    cancelButton.setDisable(false);
                    scanLogBox.setVisible(true);
                    progress.setVisible(false);
                    yesNoButtons.setVisible(false);
                    postScanButtons.setVisible(true);
                }
            });
        } else {
            log("Scan failed.\n");
            //failure, let user try again
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    amUrlField.setDisable(false);
                    scanButton.setDisable(false);
                    cancelButton.setDisable(false);
                    scanLogBox.setVisible(true);
                    progress.setVisible(false);
                    yesNoButtons.setVisible(false);
                    postScanButtons.setVisible(false);
                }
            });
        }

        scanThread = null;
    }

    public void ok() {
//        if (wizardCreatedGeniAuthority != null) {
        dialog.close();
//        }
    }

    /** Check if valid URL */
    public void validate() {
        amUrlField.getStyleClass().removeAll("validation-error", "validation-warning");
        try {
            URL test = new URL(amUrlField.getText());
        } catch (MalformedURLException e) {
            amUrlField.getStyleClass().add("validation-error");
        }
    }

    public String getAMUrl() {
        if (amUrlField.getText().equals("")) return null;
        return amUrlField.getText();
    }

    public SfaAuthority getCreatedSfaAuthority() {
        return wizardCreatedGeniAuthority;
    }

    public static SfaAuthority getDialogResult(AuthorityList authorityList, GeniUserProvider geniUserProvider) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
//        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initModality(Modality.APPLICATION_MODAL);

        URL location = ScanAuthorityDialogController.class.getResource("ScanAuthorityDialog.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);
        Parent box;
        try {
            box = (Parent) fxmlLoader.load();
            Scene scene = new Scene(box);
            ScanAuthorityDialogController controller = (ScanAuthorityDialogController) fxmlLoader.getController();
            controller.setAuthorityList(authorityList);
            controller.setGeniUserProvider(geniUserProvider);
            controller.setDialog(dialog);
            dialog.setScene(scene);
            dialog.sizeToScene();
            dialog.showAndWait();
            if (controller.getAMUrl() != null)
                return controller.getCreatedSfaAuthority();
            return null;
        } catch (IOException ex) {
            Logger.getLogger(JFedAdvancedGuiController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
