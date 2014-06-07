package be.iminds.ilabt.jfed.ui.authority_choice_gui;


import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import be.iminds.ilabt.jfed.ui.exception_gui.ExceptionHandlerDialog;
import be.iminds.ilabt.jfed.ui.x509certificate_gui.X509CertificatePanel;
import be.iminds.ilabt.jfed.util.*;
import thinlet.Thinlet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * This panel allows editing, retrieving and selecting authorities
 *
 * To make things simple:
 *   - At least one authority is always in this list
 *   - At least one authority is always selected
 *
 * This uses AuthorityListModel, which is just a list, without a selection. It can be empty.
 * */
public class AuthorityChoicePanel implements AuthorityProvider, AuthorityListModel.AuthorityListModelListener {
    private Object authChoice;
    private Object authChoiceText;
    private Object label;
    private Object editbutton;
    private Object addbutton;
    private Object updatebutton;
    private Thinlet choiceThinlet;

    private AuthorityListModel model;
    private Map<Object, SfaAuthority> authoritiesBythinletItem;
    private Map<SfaAuthority, Object> thinletItemByAuthority;

    private boolean showLabel;
    private String labelText;
    private boolean selectable;
    private boolean editable;
    private boolean addable;


    private Logger logger;
    private GeniUserProvider geniUserProvider;

    private Object thinletPanel;
    public Object getThinletPanel() {
        return thinletPanel;
    }

    public AuthorityChoicePanel(Thinlet thinlet, AuthorityListModel model, Logger logger, GeniUserProvider geniUserProvider) {
        this.logger = logger;
        this.geniUserProvider = geniUserProvider;

        this.choiceThinlet = thinlet;
        this.model = model;
        model.addAuthorityListModelListener(this);
        this.authoritiesBythinletItem = new HashMap<Object, SfaAuthority>();
        this.thinletItemByAuthority = new HashMap<SfaAuthority, Object>();

        this.showLabel = true;
        this.labelText = "Authority:";
        this.selectable = true;
        this.editable = true;
        this.addable = true;

        try {
            InputStream guiXml = this.getClass().getResourceAsStream("AuthorityChoicePanel.xml");
            thinletPanel = thinlet.parse(guiXml, this);
            authChoice = thinlet.find(thinletPanel, "authChooser");
            authChoiceText = thinlet.find(thinletPanel, "authTextField");
            label = thinlet.find(thinletPanel, "label");
            editbutton = thinlet.find(thinletPanel, "editbutton");
            addbutton = thinlet.find(thinletPanel, "addbutton");
            updatebutton = thinlet.find(thinletPanel, "updatebutton");
            assert authChoice != null;
            assert authChoiceText != null;
            assert label != null;
            assert editbutton != null;
            assert addbutton != null;
            assert updatebutton != null;
        } catch (Exception e) {
            throw new RuntimeException("Error creating UserLoginConfigPanel: "+e.getMessage(), e);
        }

        updateAuthListGui();
    }

    public void loadFromClearingHouse() {
        UtahClearingHouseAuthorityList.load(model);
        UtahClearingHouseAuthorityList.retrieveCertificates();
    }

    @Override
    public void onAuthorityListChanged() {
        updateAuthListGui();
    }
    protected void updateAuthListGui() {
        List<SfaAuthority> authorities = model.getAuthorities();
        assert !authorities.contains(null);

        assert authoritiesBythinletItem.size() == thinletItemByAuthority.size() : authoritiesBythinletItem.size() + " != " + thinletItemByAuthority.size();

        //Update authority list (supports remove and add, auto selects new if needed)
        SfaAuthority selection = getUISelectedGeniAuthority();
        List<SfaAuthority> authToAdd = new ArrayList<SfaAuthority>(authorities);
        authToAdd.removeAll(authoritiesBythinletItem.values());
        List<SfaAuthority> authToRemove = new ArrayList<SfaAuthority>(authoritiesBythinletItem.values());
        authToRemove.removeAll(authorities);
        if (authToRemove.size() > 0) {
            Set<Map.Entry<Object, SfaAuthority>> entries = new HashSet<Map.Entry<Object, SfaAuthority>>();
            entries.addAll(authoritiesBythinletItem.entrySet());
            for (Map.Entry<Object, SfaAuthority> e : entries) {
                Object thinletItem = e.getKey();
                SfaAuthority ga = e.getValue();
                if (authToRemove.contains(ga)) {
                    choiceThinlet.remove(thinletItem);
                    authoritiesBythinletItem.remove(thinletItem);
                    thinletItemByAuthority.remove(ga);
                }
            }
        }
        for (SfaAuthority toAdd : authToAdd) {
            Object thinletItem = choiceThinlet.create("choice");
            assert thinletItem != null;
            assert toAdd != null;
            choiceThinlet.setString(thinletItem, "text", toAdd.getName());
            choiceThinlet.add(authChoice, thinletItem);
            authoritiesBythinletItem.put(thinletItem, toAdd);
            thinletItemByAuthority.put(toAdd, thinletItem);
        }

        assert authoritiesBythinletItem.size() == thinletItemByAuthority.size() : authoritiesBythinletItem.size() + " != " + thinletItemByAuthority.size();
        assert thinletItemByAuthority.size() == authorities.size() : thinletItemByAuthority.size() + " != " + authorities.size() +
                "  authToAdd="+authToAdd+" authToRemove="+authToRemove+" authoritiesBythinletItem.values()="+authoritiesBythinletItem.values();

        if (selection == null || authToRemove.contains(selection))
            selection = authorities.get(0);
        //select
        choiceThinlet.setString(authChoice, "text", selection.getName());
        choiceThinlet.setString(authChoiceText, "text", selection.getName());
        choiceThinlet.setInteger(authChoice, "selected", authorities.indexOf(selection)); //TODO this index is probably incorrect! get it in a better way


        //update label and buttons
        choiceThinlet.setBoolean(label, "visible", showLabel);
        choiceThinlet.setBoolean(authChoice, "visible", selectable);
        choiceThinlet.setBoolean(authChoiceText, "visible", !selectable);
        choiceThinlet.setBoolean(editbutton, "visible", editable);
        choiceThinlet.setBoolean(addbutton, "visible", addable);
        choiceThinlet.setBoolean(updatebutton, "visible", addable);
        choiceThinlet.setString(label, "text", labelText);
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isAddable() {
        return addable;
    }

    public String getLabelText() {
        return labelText;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
        updateAuthListGui();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        updateAuthListGui();
    }

    public void setAddable(boolean addable) {
        this.addable = addable;
        updateAuthListGui();
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        updateAuthListGui();
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        updateAuthListGui();
    }

    public void setAll(String labelText, boolean showLabel, boolean selectable, boolean editable, boolean addable) {
        this.labelText = labelText;
        this.editable = editable;
        this.addable = addable;
        this.showLabel = showLabel;
        this.selectable = selectable;
        updateAuthListGui();
    }

    /**
     * @return the authority currently selected in the GUI. Can be null.
     */
    private SfaAuthority getUISelectedGeniAuthority() {
        Object selection = choiceThinlet.getSelectedItem(authChoice);
        if (selection == null) return null;
        SfaAuthority res = authoritiesBythinletItem.get(selection);
        return res;
    }

    public void setSelectedAuthority(SfaAuthority authority) {
        Object thinletItem = thinletItemByAuthority.get(authority);
        if (thinletItem == null)
            throw new RuntimeException("Error, authority selected is unknown: "+authority == null ? "authority == null" : authority.getUrn());
        choiceThinlet.setString(authChoice, "text", authority.getName());
        choiceThinlet.setString(authChoiceText, "text", authority.getName());
        choiceThinlet.setInteger(authChoice, "selected", model.getAuthorities().indexOf(authority)); //TODO this index is probably incorrect! get it in a better way
        fireChange(authority);
    }

    @Override
    public SfaAuthority getAuthority() {
        SfaAuthority auth = getUISelectedGeniAuthority();
        if (auth != null) return auth;
        auth = model.getAuthorities().get(0);
        choiceThinlet.setString(authChoice, "text", auth.getName());
        choiceThinlet.setString(authChoiceText, "text", auth.getName());
        choiceThinlet.setInteger(authChoice, "selected", 0);
        return auth;
    }

    private boolean edit = false;
    private String originalEditedUrn = null;

    private Object dialog;
    private Object dialogCertificate;
    private Object dialogCertificateDetails;
    private X509CertificatePanel x509CertificatePanel;
    public void editAuth() {
        edit = true;
        originalEditedUrn = null;
        showAuth();
    }
    public void addAuth() {
        edit = false;
        originalEditedUrn = null;
//        showAuth();
        showWizard();
    }
    public void showAuth() {
        assert dialog == null;
        try {
            InputStream guiXml = this.getClass().getResourceAsStream("AuthorityDialog.xml");
            assert guiXml != null;
            dialog = choiceThinlet.parse(guiXml, this);
            dialogCertificate = choiceThinlet.find(dialog, "certificate");
            assert(dialogCertificate != null);

            //To embed certificate details below certificate view uncomment the following:
//            dialogCertificateDetails = choiceThinlet.find(dialog, "certificateDetails");
//            assert(dialogCertificateDetails != null);
//            x509CertificatePanel = new X509CertificatePanel(choiceThinlet, false);
//            choiceThinlet.add(dialogCertificateDetails, x509CertificatePanel.getThinletPanel());
//            updateCertificateDetails();

            if (edit)
                choiceThinlet.setString(dialog, "text", "Edit Authority");
            else
                choiceThinlet.setString(dialog, "text", "Add Authority");
            choiceThinlet.add(dialog);
            choiceThinlet.setBoolean(dialog, "visible", true);
        } catch (IOException e) {
            ExceptionHandlerDialog.handleException(e);
        }
    }

    public void updateCertificateDetails() {
        if (dialogCertificate != null && x509CertificatePanel != null)
            x509CertificatePanel.setCertificatePEMString(choiceThinlet.getString(dialogCertificate, "text"));
    }

    public void showCertificateDetailsWindow() {
        if (dialogCertificate == null) return;

//        if (x509CertificatePanel == null)
        x509CertificatePanel = X509CertificatePanel.showManualCommandPan(choiceThinlet.getString(dialogCertificate, "text"), true);
    }

    public interface WizardCallback {
        public void onWizardResult(SfaAuthority authority);
    }
    private X509Certificate overrideCertificateForWizard;
    private PrivateKey overridePrivateKeyForWizard;
    private WizardCallback wizardCallback;
    public void doWizardCallback(SfaAuthority authority) {
        if (wizardCallback != null) {
            wizardCallback.onWizardResult(authority);
            wizardCallback = null;
            this.overrideCertificateForWizard = null;
            this.overridePrivateKeyForWizard = null;
        }
    }
    public void showWizard(X509Certificate overrideCertificateForWizard, PrivateKey overridePrivateKeyForWizard, WizardCallback wizardCallback) {
        this.overrideCertificateForWizard = overrideCertificateForWizard;
        this.overridePrivateKeyForWizard = overridePrivateKeyForWizard;
        this.wizardCallback = wizardCallback;
        showWizard();
    }
    public void showWizard() {
        assert dialog == null;
        try {
            InputStream guiXml = this.getClass().getResourceAsStream("WizardDialog.xml");
            assert guiXml != null;
            dialog = choiceThinlet.parse(guiXml, this);
            choiceThinlet.setString(dialog, "text", "Add Authority");
            choiceThinlet.add(dialog);
            choiceThinlet.setBoolean(dialog, "visible", true);
        } catch (IOException e) {
            ExceptionHandlerDialog.handleException(e);
        }
    }

    public void skipWizard() {
        try {
            Map<ServerType, URL> urlMap = new HashMap<ServerType, URL>();
            urlMap.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL("https://example.com:12369/"));
            wizardCreatedGeniAuthority = new SfaAuthority("urn:publicid:IDN+example.com+authority+auth", "example", urlMap, null, null);
            edit = false;
            originalEditedUrn = null;
            removeWizard();
            showAuth();
        } catch (Exception e) {
            System.err.println("bug in skipWizard:");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    SfaAuthority wizardCreatedGeniAuthority = null;
    public void runWizard(String url) {
        URL u = null;
        wizardCreatedGeniAuthority = null;
        boolean unknownUrn = false;
        boolean callSuccess = false;
        try {
            u = new URL(url);

            SSLCertificateDownloader.SSLCertificateJFedInfo info = SSLCertificateDownloader.getCertificateInfo(u);
            System.out.println("Note: got certificate info. checking...");

            String urn = info.getUrn();
            if (info.getUrn() == null) {
//                String urn = DialogUtils.getString("The URN of the server could not be determined");
                //make a placeholder URN. Will be replaced later!
                urn = "urn:publicid:IDN+"+u.getHost()+"+authority+cm";
                unknownUrn = true;
            }

            assert info != null;

            boolean cancel = info.getCert() == null;

            if (cancel) {
                DialogUtils.infoMessage("Problem fetching certificate from server. Cancelling wizard.");
                System.err.println("ERROR: cancelling wizard: no certificate found");
            }

            Map<ServerType, URL> urlMap = new HashMap<ServerType, URL>();
            urlMap.put(new ServerType(ServerType.GeniServerRole.AM, 2), u); //TODO let user select which AM api
            //TODO: or even better: detect it
            //TODO: We could also make a first guess based on url
            //Note: already, this will (possibly, depending on errors) be overwritten later
            wizardCreatedGeniAuthority = new SfaAuthority(urn, info.getSubject(), urlMap, null, null);
            System.out.println("Note: created wizardCreatedGeniAuthority");

            if (!cancel && info.isSelfSigned()) {
                System.out.println("Note: server uses self signed certificate");
                wizardCreatedGeniAuthority.setPemSslTrustCert(info.getCert());

                if (DialogUtils.getYesNoAnswer("Server certificate is self signed. Trust anyway?") == false) { //TODO show info to help the user decide
                    System.err.println("ERROR: cancelling wizard: no trust");
                    cancel = true;
                }

                if (!cancel && !info.getSubjectMatchesHostname()) {
                    System.out.println("Note: Server certificate subject name is not for \""+u.getHost()+"\" but for \""+info.getSubject()+"");
                    wizardCreatedGeniAuthority.addAllowedCertificateHostnameAlias(info.getSubject());

                    if (DialogUtils.getYesNoAnswer("Server certificate subject name is not for \""+u.getHost()+"\" but for \""+info.getSubject()+"\". Trust anyway?") == false) {
                        System.err.println("ERROR: cancelling wizard: no trust");
                        cancel = true;
                    }
                }
            }
            System.out.println("Note: SSL certificate info ok. cancel="+cancel+
                    " isUserLoggedIn="+geniUserProvider.isUserLoggedIn()+
                    " override="+(overrideCertificateForWizard != null && overridePrivateKeyForWizard != null));

            if (!cancel && (geniUserProvider.isUserLoggedIn() || (overrideCertificateForWizard != null && overridePrivateKeyForWizard != null))) {
                System.out.println("Note: Now trying to connect to call GetVersion to get all AM versions URLs...");

                try {
                    if (info.isSelfSigned() && info.getCert() != null) {
                        GeniTrustStoreHelper.addTrustedCertificate(info.getCert());

                        //TODO code below would probably be better, but  pemToX509Certificate doesn't work yet
                        //TODO or alternatively, make addTrustedCertificate ignore duplicates
//                    String pemCert = KeyUtil.x509certificateToPem(info.getCert());
//                    System.out.println("Note: adding certificate to trust store:\n"+pemCert);
//                    X509Certificate reconstructedCert = KeyUtil.pemToX509Certificate(pemCert);
//                    assert reconstructedCert != null;
//                    assert info.getCert() != null;
//                    assert reconstructedCert.equals(info.getCert());
//                    GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(pemCert);
                    }

                    //contact server using AM API(v2 but it doesn't really matter) and use GetVersion to get correct AM urls...
                    AggregateManager2 am2 = new AggregateManager2(logger, true);
                    assert wizardCreatedGeniAuthority != null;
                    AggregateManager2.AggregateManagerReply<AggregateManager2.VersionInfo> versionInfoReply =
                            am2.getVersion(
                                    new GeniSslConnection(
                                            wizardCreatedGeniAuthority,
                                            url,
                                            overrideCertificateForWizard != null ? overrideCertificateForWizard : geniUserProvider.getLoggedInGeniUser().getCertificate(),
                                            overridePrivateKeyForWizard != null? overridePrivateKeyForWizard : geniUserProvider.getLoggedInGeniUser().getPrivateKey(),
                                            // wizardCreatedGeniAuthority.getAllowedCertificateHostnameAliases(),
                                            false, null/*handleUntrustedCallback*/)
                            );
                    callSuccess = true;
                    System.out.println("  Processing GetVersion result : \n"+XmlRpcPrintUtil.printXmlRpcResultObject(versionInfoReply.getRawResult()));
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
                                System.err.println("WARNING: Aggregate Manager returned URL containing \"localhost\" in GetVersion result: "+apiVersion.getUrl()+"   (automatically fixing url using the URL you supplied)");
                                versionUrl =  new URL(apiVersion.getUrl().replace("localhost", u.getHost()));
                            }
                            if (versionUrl.getProtocol().equals("http")) {
                                System.out.println("Note: Server gave URL with http instead of https: \"" + apiVersion.getUrl() + "\"");

                                if (DialogUtils.getYesNoAnswer("Server gave URL with http instead of https: \""+apiVersion.getUrl()+"\". This probably needs to be https. Change it to https?") == true) {
                                    versionUrl =  new URL(versionUrl.toString().replace("http://", "https://"));
                                }
                            }
                            urlMap.put(new ServerType(ServerType.GeniServerRole.AM, apiVersion.getVersionNr()), versionUrl);
                        } catch (MalformedURLException e) {
                            System.err.println("Error: Aggregate Manager returned malformed URL in GetVersion result: "+apiVersion.getUrl());
                        }
                    }
                    if (urlMap.isEmpty()) {
                        System.err.println("ERROR: No valid AM URLs in GetVersion reply! Will use fallback.");

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
//                            ex.printStackTrace(System.out);
//                            System.out.flush();
                            System.err.println("The server was replied with HTTP error "+
                                    httpErrorException.getStatusNr()+" \""+httpErrorException.getReason()+
                                    "\" while trying a GetVersion call. Retry scanning later?");
                            DialogUtils.errorMessage("The server was replied with HTTP error "+
                                    httpErrorException.getStatusNr()+" \""+httpErrorException.getReason()+
                                    "\" while trying a GetVersion call. Retry scanning later?");
                            if (ex.getXmlRpcResult() != null && ex.getXmlRpcResult() != null) {
                                System.out.println("HTTP reply details:\n" + ex.getXmlRpcResult().getResultHttpContent());
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
            }

            if (!cancel && !callSuccess) {
                if (DialogUtils.getYesNoAnswer("There was an error when trying a \"GetVersion\" call. Continue adding anyway?") == false) {
                    System.err.println("ERROR: cancelling wizard: error in GetVersion is considered fatal by user.");
                    cancel = true;
                }
            }

            if (cancel) {
                wizardCreatedGeniAuthority = null;
                System.err.println("ERROR: Cancelling wizard, perhaps try again using a different URL?");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Something went wrong trying to get Aggregate Manager info: "+e.getMessage());
            e.printStackTrace();
            if (wizardCreatedGeniAuthority != null &&
                    DialogUtils.getYesNoAnswer("Something went wrong trying to get Aggregate Manager info: \""+e.getMessage()+"\". Continue adding anyway?") == false) {
                System.err.println("ERROR: cancelling wizard: error considered fatal by user.");
                wizardCreatedGeniAuthority = null;
            }
        }

        if (wizardCreatedGeniAuthority != null) {
            edit = false;
            originalEditedUrn = null;
            if (unknownUrn)
                DialogUtils.infoMessage("The URN of the authority could not be detected, so an URN was guessed automatically. This might be incorrect: please verify!");
            removeWizard();
            showAuth();
        } else {
            //keep wizard open: let user try again or cancel wizard
        }
    }

    public void updateUrn(Object urn, Object urnPart) {
        String urnPartText = choiceThinlet.getString(urnPart, "text");
        choiceThinlet.setString(urn, "text", "urn:publicid:IDN+" + urnPartText + "+authority+cm");
    }

    public void initAuthDialog(Object saveOrAddButton, Object cancelButton, Object type, Object urn, Object urnPart,
                               Object authName, Object urlPanel, Object certificate, Object certificateAliases,
                               Object reconnectEachTime, Object gid) {
        if (edit)
            editAuth(saveOrAddButton, cancelButton, type, urn, urnPart, authName, urlPanel, certificate,
                    certificateAliases, reconnectEachTime, gid);
        else
            addAuth(saveOrAddButton, cancelButton, type, urn, urnPart, authName, urlPanel, certificate,
                    certificateAliases, reconnectEachTime, gid);
    }

    public class UrlPanelLine {
        //thinlet objects
        public Object combobox;
        public Object versionField;
        public Object urlField;
        public Object deleteButton;

        private Thinlet thinlet;

        public List<ServerType.GeniServerRole> comboboxTypes = new ArrayList<ServerType.GeniServerRole>();

        public UrlPanelLine(Thinlet thinlet, Object urlPanel, ServerType type, URL url) {
            this.thinlet = thinlet;
            combobox = thinlet.create("combobox");
            versionField = thinlet.create("textfield");
            urlField = thinlet.create("textfield");
            deleteButton = thinlet.create("button");

            int selectedIndex = 0;
            int i = 0;
            for (ServerType.GeniServerRole role : ServerType.GeniServerRole.values()) {
                Object choice = thinlet.create("choice");
                thinlet.setString(choice, "text", role.toString());
                comboboxTypes.add(role);
                thinlet.add(combobox, choice);
                if (role.equals(type.getRole())) {
                    thinlet.setString(combobox, "text", role.toString());
                    selectedIndex = i;
                }
                i++;
            }
            thinlet.setInteger(combobox, "selected", selectedIndex);
            thinlet.setString(versionField, "text", type.getVersion());
            thinlet.setString(urlField, "text", url.toString());
            thinlet.setMethod(deleteButton, "action", "delete", urlPanel, this);

            thinlet.setInteger(combobox, "weightx", 1);
            thinlet.setInteger(versionField, "weightx", 1);
            thinlet.setInteger(urlField, "weightx", 1);
            thinlet.setInteger(deleteButton, "weightx", 0);

            thinlet.setInteger(urlField, "columns", 50);
            thinlet.setString(deleteButton, "text", "delete");

            thinlet.add(urlPanel, combobox);
            thinlet.add(urlPanel, versionField);
            thinlet.add(urlPanel, urlField);
            thinlet.add(urlPanel, deleteButton);
        }

        public void delete() {
            thinlet.remove(combobox);
            thinlet.remove(versionField);
            thinlet.remove(urlField);
            thinlet.remove(deleteButton);

            urlPanelLines.remove(this);

            combobox = null;
            versionField = null;
            urlField = null;
            deleteButton = null;
        }

        public ServerType getServerType() {
            int apiIndex = thinlet.getInteger(combobox, "selected");
            if (apiIndex == -1)
                apiIndex = 0;
            assert !comboboxTypes.isEmpty();
            ServerType.GeniServerRole role = comboboxTypes.get(apiIndex);

            String versionString = thinlet.getString(versionField, "text");
            int versionNr = Integer.parseInt(versionString);

            return new ServerType(role, versionNr);
        }
        public URL getUrl() {
            String urlString = thinlet.getString(urlField, "text");
            try {
                return new URL(urlString);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid URL: "+urlString, e);
            }
        }
    }

    public void addUrlLine(Object urlPanel) {
        UrlPanelLine line = null;
        try {
            line = new UrlPanelLine(choiceThinlet, urlPanel, new ServerType(ServerType.GeniServerRole.AM, 3), new URL("http://example.com/"));
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        urlPanelLines.add(line);
    }

    private List<UrlPanelLine> urlPanelLines;

    public void editAuth(Object saveOrAddButton, Object cancelButton, Object type, Object urn, Object urnPart, Object authName, Object urlPanel, Object certificate, Object certificateAliases,
                         Object reconnectEachTime, Object gid) {
        SfaAuthority selected = getUISelectedGeniAuthority();
        choiceThinlet.setString(type, "text", selected.getType());
        choiceThinlet.setString(urn, "text", selected.getUrn());
        choiceThinlet.setBoolean(urn, "editable", false);
        choiceThinlet.setString(urnPart, "text", selected.getNameForUrn());
        choiceThinlet.setString(authName, "text", selected.getName());

        Map<ServerType, URL> urls = selected.getUrls();
        urlPanelLines = new ArrayList<UrlPanelLine>();
        for (Map.Entry<ServerType, URL> e : urls.entrySet()) {
            UrlPanelLine line = new UrlPanelLine(choiceThinlet, urlPanel, e.getKey(), e.getValue());
            urlPanelLines.add(line);
        }

        choiceThinlet.setString(certificate, "text", selected.getPemSslTrustCert());
        choiceThinlet.setString(certificateAliases, "text", TextUtil.mkString(selected.getAllowedCertificateHostnameAliases(), ","));
        updateCertificateDetails();
        choiceThinlet.setBoolean(reconnectEachTime, "selected", selected.isReconnectEachTime());
        choiceThinlet.setString(gid, "text", selected.getGid() + "");

        choiceThinlet.setString(saveOrAddButton, "text", "Save Changes");
        choiceThinlet.setString(cancelButton, "text", "Discard Changes");
        edit = true;
        originalEditedUrn = selected.getUrn();
    }

    public void addAuth(Object saveOrAddButton, Object cancelButton, Object type, Object urn, Object urnPart, Object authName, Object urlPanel, Object certificate, Object certificateAliases,
                        Object reconnectEachTime, Object gid) {
        assert wizardCreatedGeniAuthority != null;
        SfaAuthority selected = wizardCreatedGeniAuthority;
        choiceThinlet.setString(type, "text", selected.getType());
        choiceThinlet.setString(urn, "text", selected.getUrn());
        choiceThinlet.setBoolean(urn, "editable", false);
        choiceThinlet.setString(urnPart, "text", selected.getNameForUrn());
        choiceThinlet.setString(authName, "text", selected.getName());

        Map<ServerType, URL> urls = selected.getUrls();
        urlPanelLines = new ArrayList<UrlPanelLine>();
        for (Map.Entry<ServerType, URL> e : urls.entrySet()) {
            UrlPanelLine line = new UrlPanelLine(choiceThinlet, urlPanel, e.getKey(), e.getValue());
            urlPanelLines.add(line);
        }

        choiceThinlet.setString(certificate, "text", selected.getPemSslTrustCert());
        choiceThinlet.setString(certificateAliases, "text", TextUtil.mkString(selected.getAllowedCertificateHostnameAliases(), ","));
        updateCertificateDetails();
        choiceThinlet.setBoolean(reconnectEachTime, "selected", selected.isReconnectEachTime());
        choiceThinlet.setString(gid, "text", selected.getGid() + "");

//        choiceThinlet.setString(type, "text", "emulab");
//        choiceThinlet.setString(urn, "text", "urn:publicid:IDN+<AUTH>+authority+cm");
//        choiceThinlet.setBoolean(urn, "editable", true);
//        choiceThinlet.setString(urnPart, "text", "<AUTH>");
//        choiceThinlet.setString(authName, "text", "");

//        choiceThinlet.setString(certificate, "text", "");
//        choiceThinlet.setString(certificateAliases, "text", "");
//        choiceThinlet.setBoolean(reconnectEachTime, "selected", false);
//        choiceThinlet.setString(gid, "text", "");
//
        choiceThinlet.setString(saveOrAddButton, "text", "Add");
        choiceThinlet.setString(cancelButton, "text", "Cancel");
        edit = false;
        originalEditedUrn = null;
    }
    public void saveAuth(String type, String urn, String authName,
                         boolean reconnectEachTime, String certificateText, String certificateAliasesText, Object urlPanel) {
        SfaAuthority oldAuth = model.getByUrn(urn);
//            List<String> aliases = new ArrayList<String>(Arrays.asList(certificateAliasesText.split(",")));
        List<String> aliases = new ArrayList<String>();
        for (String part : certificateAliasesText.split(","))
            aliases.add(part.trim());

        if (oldAuth == null) {
            if (edit && DialogUtils.getYesNoAnswer("The URN of the authority was changed to \""+urn+"\". Add as new authority?", "Add", "Continue Editing") == false) {
                System.err.println("NOTE: cancelling save: not adding");
                return;
            }

            System.out.println("Adding new auth with URN: " + urn);
            try {
                Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
                for (UrlPanelLine line : urlPanelLines) {
                    urls.put(line.getServerType(), line.getUrl());
                }
                SfaAuthority newAuth = new SfaAuthority(urn, authName, urls, null/*gid*/, type);

                newAuth.setReconnectEachTime(reconnectEachTime);
                if (!certificateText.trim().equals(""))
                    newAuth.setPemSslTrustCert(certificateText);
                if (!certificateAliasesText.trim().equals(""))
                    newAuth.setAllowedCertificateHostnameAlias(aliases);

                model.addAuthority(newAuth);
                removeAuth();

                doWizardCallback(newAuth);

                //automatically save the list of authorities to file
                JFedAuthorityList.getInstance().save();
            } catch (Exception e) {
//                    DialogUtils.errorMessage(choiceThinlet, "Error in edited GeniAuthority", "urn=\""+urn+"\"\nauthName=\""+authName
//                            +"\"\nauthSAurl=\""+authSAurl+"\"\nauthAMurl=\""+authAMurl+
//                            "\"\nauthAM2url=\""+authAM2url+"\"\nError message: "+e.getMessage(), e);
                ExceptionHandlerDialog.handleException("Error in edited GeniAuthority", "urn=\""+urn+"\"\nauthName=\""+authName
                        +"\nError message: "+e.getMessage(), e);
            }
        } else {
            if (!edit && DialogUtils.getYesNoAnswer("An authority with the same urn (\""+urn+"\") already existed in the list. Replace it?", "Replace", "Continue Editing") == false) {
                System.err.println("NOTE: cancelling save: not replacing");
                return;
            }
            if (edit && (!urn.equals(originalEditedUrn)) && DialogUtils.getYesNoAnswer("WARNING: The URN of the authority was changed from \""+originalEditedUrn+"\" to \""+urn
                    +"\", which already existed. Replace that existing authority? (You probably don't want this)", "Replace! (Are you sure?)", "Continue Editing") == false) {
                System.err.println("NOTE: cancelling save: not replacing \""+originalEditedUrn+"\" by \""+urn+"\"");
                return;
            }

            System.out.println("Updating existing auth with URN: "+urn);
            oldAuth.updateName(authName);
            Map<ServerType, URL> urlMap = new HashMap<ServerType, URL>();
            for (UrlPanelLine line : urlPanelLines) {
//                oldAuth.updateUrl(line.getServerType(), line.getUrl()); //this did not cause remove
                urlMap.put(line.getServerType(), line.getUrl());
            }
            oldAuth.replaceUrls(urlMap);

            oldAuth.setReconnectEachTime(reconnectEachTime);
            if (certificateText.trim().equals(""))
                oldAuth.setPemSslTrustCert((String)null);
            else
                oldAuth.setPemSslTrustCert(certificateText.trim());
            if (certificateAliasesText.trim().equals(""))
                oldAuth.setAllowedCertificateHostnameAlias(new ArrayList<String>());
            else
                oldAuth.setAllowedCertificateHostnameAlias(aliases);

            //let everybody know that the authority list has changed
            model.fireChange();

            doWizardCallback(oldAuth);

            removeAuth();

            //save the updated list to file immediately
            JFedAuthorityList.getInstance().save();
        }
    }

    public void showXml(String type, String urn, String authName, boolean reconnectEachTime, String certificateText, String certificateAliasesText, Object urlPanel) {
        List<String> aliases = new ArrayList<String>();
        for (String part : certificateAliasesText.split(","))
            aliases.add(part.trim());

        try {
            Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
            for (UrlPanelLine line : urlPanelLines) {
                urls.put(line.getServerType(), line.getUrl());
            }
            SfaAuthority newAuth = new SfaAuthority(urn, authName, urls, null, type);

            newAuth.setReconnectEachTime(reconnectEachTime);
            if (!certificateText.trim().equals(""))
                newAuth.setPemSslTrustCert(certificateText);
            if (!certificateAliasesText.trim().equals(""))
                newAuth.setAllowedCertificateHostnameAlias(aliases);

            //write to file
            StringWriter writer = new StringWriter();
//            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities xmlAuthorities = new be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities();
            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority xmlAuth = newAuth.toXml();
//            xmlAuthorities.getAuthority().add(xmlAuth);

//            JAXBContext context = JAXBContext.newInstance(be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.class);
            JAXBContext context = JAXBContext.newInstance(be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            m.marshal(xmlAuthorities, writer);
            m.marshal(xmlAuth, writer);

            writer.close();

            String xml = writer.toString();

            System.out.println("Authority in XML:\n\n"+xml+"\n\n");
            DialogUtils.infoNonEditabaleTextField("Authority XML", "Authority as XML (also outputted to console):", xml);
        } catch (Exception e) {
            System.err.println("WARNING: Error writing authorities to XML: "+e.getMessage());
            e.printStackTrace();
        }
    }


    public void removeAuth() {
//        setBoolean(dialog, "visible", false);
        choiceThinlet.remove(dialog);
        dialog = null;
    }
    public void cancelAuth() {
        removeAuth();
        doWizardCallback(null);
    }
    public void cancelWizard() {
        removeWizard();
        doWizardCallback(null);
    }

    public void removeWizard() {
        choiceThinlet.remove(dialog);
        dialog = null;
    }

    public void deleteEditedAuth(String urn) {
        //check if urn by that auth exists
        SfaAuthority curAuth = model.getByUrn(urn);
        if (curAuth == null) {
            DialogUtils.infoMessage("No authority found with URN \""+urn+"\": cannot delete what does not exists.");
            System.err.println("NOTE: cancelling delete: no such auth found");
            return;
        }

        //TODO check if urn still matches original edited urn
        if (edit && (!urn.equals(originalEditedUrn)) && DialogUtils.getYesNoAnswer("WARNING: The URN of the authority was changed from \""+originalEditedUrn+"\" to \""+urn
                +"\", which also exists. Delete that existing authority (\""+urn+"\")?", "Delete blindly! (Are you sure?)", "Continue Editing") == false) {
            System.err.println("NOTE: cancelling delete: user is not sure");
            return;
        }
        if ((!edit || originalEditedUrn == null) && DialogUtils.getYesNoAnswer("WARNING: The URN of the authority was changed from \""+urn
                +"\", already existed for an already saved authority. Delete that existing authority (\""+urn+"\")?", "Delete blindly! (Are you sure?)", "Continue Editing") == false) {
            System.err.println("NOTE: cancelling delete: user is not sure");
            return;
        }

        //remove auth
        model.removeByUrn(urn);

        removeAuth();
        doWizardCallback(null);

        //save the updated list to file immediately
        JFedAuthorityList.getInstance().save();
    }


    public void onUserSelectedAuth(Object auth) {
        fireChange(getAuthority());
    }

    /* Note: There will always be an authority selected. */
    public static interface AuthoritySelectionListener {
        public void onSelectAuthority(SfaAuthority authority);
    }
    private List<AuthoritySelectionListener> changeListeners = new ArrayList<AuthoritySelectionListener>();
    public void fireChange(SfaAuthority authority) {
        assert authority != null;
        for (AuthoritySelectionListener l : changeListeners)
            l.onSelectAuthority(authority);
    }
    public void addAuthoritySelectionListener(AuthoritySelectionListener l) {
        changeListeners.add(l);
    }
    public void removeAuthoritySelectionListener(AuthoritySelectionListener l){
        changeListeners.remove(l);
    }
}
