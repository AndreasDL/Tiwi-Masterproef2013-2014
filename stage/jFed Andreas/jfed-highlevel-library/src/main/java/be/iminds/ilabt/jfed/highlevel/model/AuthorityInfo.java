package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthorityInfo wraps the jFed library's SfaAuthority class with JavaFX properties
 *
 * It also stores additional info related to the current session, such as the last received advertisement RSpec
 *
 * It also offers a convenient and save delete method.
 */
public class AuthorityInfo {
    private final SfaAuthority geniAuthority;
    private final EasyModel easyModel;
    //TODO add processed list of resources

    /** true if this AuthorityInfo is the same as the SfaAuthority (geniAuthority) */
    private BooleanProperty committed = new SimpleBooleanProperty(true);
    /** true if this AuthorityInfo is the same as on disk */
    private BooleanProperty saved = new SimpleBooleanProperty(true);

    private boolean hasPreviouslyCommittedChanges = false;

    /** Note: typically, you do not want to construct yourself.
     *        instead get AuthorityInfo using EasyModel and EasyAuthorityList */
    public AuthorityInfo(EasyModel easyModel, SfaAuthority geniAuthority, int index) {
        this.easyModel = easyModel;
        this.geniAuthority = geniAuthority;
        this.index = index;

        restore();

        //listeners to keep committed and saved properties up to date automatically
        name.addListener(changeHandler);
        urn.addListener(changeHandler);
        //urnPart.addListener(changeHandler); //derived, so not needed to listen to this
        pemSslTrustCert.addListener(changeHandler);
        reconnectEachTime.addListener(changeHandler);
        allowedCertificateHostnameAliases.addListener(changeHandler);
        urls.addListener(changeHandler);
        urls.addListener(new ListChangeListener<AuthorityUrl>() {
            @Override
            public void onChanged(Change<? extends AuthorityUrl> change) {
                while (change.next()) {
                    for (AuthorityUrl added : change.getAddedSubList()) {
                        added.urlProperty().addListener(changeHandler);
                        added.roleProperty().addListener(changeHandler);
                        added.versionProperty().addListener(changeHandler);
                    }
                    for (AuthorityUrl removed : change.getRemoved()) {
                        removed.urlProperty().removeListener(changeHandler);
                        removed.roleProperty().removeListener(changeHandler);
                        removed.versionProperty().removeListener(changeHandler);
                    }
                }
            }
        });
    }

    private void updateUrnPart() {
        String part = "<ILLEGAL URN>";
        try {
            part = SfaAuthority.urnPartFromUrn(urn.get());
        } catch (GeniException e) { }
        urnPart.set(part);
    }

    public SfaAuthority getGeniAuthority() {
        return geniAuthority;
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }


    private int index;
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    public BooleanProperty committedProperty() {
        return committed;
    }
    public BooleanProperty savedProperty() {
        return saved;
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    /** safely delete this from AuthorityListModel and AuthorityList */
    public void delete() {
        if (easyModel == null) return;
        hasPreviouslyCommittedChanges = true;
        AuthorityList authorityList = easyModel.getAuthorityList();
        authorityList.getAuthorityListModel().removeByUrn(geniAuthority.getUrn());
        saved.set(false);
    }

    public void commit() {
        if (isSame()) return;

        geniAuthority.setName(name.get());
//        geniAuthority.setUrn(urn.get());
        geniAuthority.setReconnectEachTime(reconnectEachTime.get());
        geniAuthority.setPemSslTrustCert(pemSslTrustCert.get());
        geniAuthority.setAllowedCertificateHostnameAlias(allowedCertificateHostnameAliases.get());
        Map<ServerType, URL> newMap = new HashMap<ServerType, URL>();
        for (AuthorityUrl authUrl : urls)
            newMap.put(authUrl.getServerType(), authUrl.getURL());
        geniAuthority.replaceUrls(newMap);

        assert isSame();
        committed.set(true);
        hasPreviouslyCommittedChanges = true;
    }

    public void commitAsNew() {
        try {
            SfaAuthority newAuth = new SfaAuthority(urn.get());
            newAuth.setName(name.get());
            newAuth.setReconnectEachTime(reconnectEachTime.get());
            newAuth.setPemSslTrustCert(pemSslTrustCert.get());
            newAuth.setAllowedCertificateHostnameAlias(allowedCertificateHostnameAliases.get());
            Map<ServerType, URL> newMap = new HashMap<ServerType, URL>();
            for (AuthorityUrl authUrl : urls)
                newMap.put(authUrl.getServerType(), authUrl.getURL());
            newAuth.replaceUrls(newMap);
            hasPreviouslyCommittedChanges = true;
            committed.set(true);
            saved.set(false);
        } catch (GeniException e) {
            e.printStackTrace();
        }
    }

    public void restore() {
        setTo(geniAuthority);
    }
    public void setTo(SfaAuthority sfaAuth) {
        if (sfaAuth == geniAuthority && this.urn != null && this.urn.get() != null) {
            if (isSame()) return;

            //no need for this, changeHandler will automatically do this
//            //because of restore, it is still saved if no changes committed before
//            if (!hasPreviouslyCommittedChanges)
//                saved.set(true);
//
//            committed.set(true);
        }

        //TODO disable changeHandler until the end? (needs to fire only at the end, intermediate changes are not important)

        assert sfaAuth != null;

        this.name.set(sfaAuth.getName());
        this.urn.set(sfaAuth.getUrn());
        this.urls.clear();
        for (Map.Entry<ServerType, URL> entry: sfaAuth.getUrls().entrySet()) {
            AuthorityUrl authorityUrl = new AuthorityUrl(entry.getKey(), entry.getValue());
            this.urls.add(authorityUrl);
        }
        this.reconnectEachTime.set(sfaAuth.isReconnectEachTime());
        this.pemSslTrustCert.set(sfaAuth.getPemSslTrustCert());
        allowedCertificateHostnameAliases.clear();
        for (String allowedCertificateHostnameAlias : sfaAuth.getAllowedCertificateHostnameAliases())
            allowedCertificateHostnameAliases.add(allowedCertificateHostnameAlias);

        urn.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldString, String newString) {
                updateUrnPart();
            }
        });
        updateUrnPart();
    }

    //helper
    private boolean nullSupportingEquals(String a, String b) {
        if (a == null || b == null)
            return a == b;
        return a.equals(b);
    }

    /** return true if this AuthorityInfo is the same as the SfaAuthority (geniAuthority) */
    public boolean isSame() {
//        System.out.println("                            same debug:   geniAuthority != null");
        if (geniAuthority == null) return false;

        assert this.urn != null;
        assert this.urn.get() != null;

//        System.out.println("                                same debug:   name: \""+this.name.get()+"\" VS \""+geniAuthority.getName()+"\"");
        if (! nullSupportingEquals(this.name.get(), geniAuthority.getName())) return false;
//        System.out.println("                                same debug:   urn");
        if (! nullSupportingEquals(this.urn.get(), geniAuthority.getUrn())) return false;
//        System.out.println("                                same debug:   pemSslTrustCert");
        if (! nullSupportingEquals(this.pemSslTrustCert.get(), geniAuthority.getPemSslTrustCert())) return false;
//        System.out.println("                                same debug:   reconnectEachTime");
        if (this.reconnectEachTime.get() != geniAuthority.isReconnectEachTime()) return false;

//        System.out.println("                                same debug:   urls");
        if (this.urls.size() != geniAuthority.getUrls().size()) return false;
        for (AuthorityUrl authorityUrl : this.urls) {
            URL u = geniAuthority.getUrls().get(authorityUrl.getServerType());
            if (u == null) return false;
            if (!u.equals(authorityUrl.getURL())) return false;
        }

//        System.out.println("                                same debug:   allowedCertificateHostnameAliases");
        if (this.allowedCertificateHostnameAliases.size() != geniAuthority.getAllowedCertificateHostnameAliases().size()) return false;
        if (! this.allowedCertificateHostnameAliases.get().containsAll(geniAuthority.getAllowedCertificateHostnameAliases())) return false;
        if (! geniAuthority.getAllowedCertificateHostnameAliases().containsAll(this.allowedCertificateHostnameAliases.get())) return false;

//        System.out.println("                                same debug:   SAME");
        return true;
    }

    public void markSaved() {
        if (isSame()) {
            hasPreviouslyCommittedChanges = false;
            saved.set(true);
        } else {
            hasPreviouslyCommittedChanges = false;
        }
    }

    private final ChangeListener changeHandler = new ChangeListener() {
        @Override
        public void changed(ObservableValue observableValue, Object o, Object o1) {
            boolean same = isSame();

//            System.out.println("DEBUG committed: same="+same+" committed="+committed.get()+" saved="+saved.get());

            committed.set(same);

            if (saved.get() && !committed.get())
                saved.set(false);

            if (!saved.get() && !hasPreviouslyCommittedChanges && committed.get())
                saved.set(true);

//            System.out.println("                               committed="+committed.get()+" saved="+saved.get());
        }
    };


    //////////////////////////////////////////////////////////////////
    ///////////////////////// observable properties //////////////////
    //////////////////////////////////////////////////////////////////

    private StringProperty name = new SimpleStringProperty();
    public StringProperty nameProperty() {
        return name;
    }

    private StringProperty urn = new SimpleStringProperty("");
    public StringProperty urnProperty() {
        return urn;
    }

    private StringProperty urnPart = new SimpleStringProperty("");
    public ObservableValue<String> urnPartProperty() {
        return urnPart;
    }

    private StringProperty pemSslTrustCert = new SimpleStringProperty("");
    public StringProperty pemSslTrustCertProperty() {
        return pemSslTrustCert;
    }

    private BooleanProperty reconnectEachTime = new SimpleBooleanProperty(false);
    public BooleanProperty reconnectEachTimeProperty() {
        return reconnectEachTime;
    }

    private final ListProperty<String> allowedCertificateHostnameAliases =
            new SimpleListProperty<String>(FXCollections.<String>observableArrayList());
    public ObservableListValue<String> getAllowedCertificateHostnameAliases() {
        return allowedCertificateHostnameAliases;
    }

    //////////////////////////////////////////////////////////////////
    ///////////////////////// observable urls ////////////////////////
    //////////////////////////////////////////////////////////////////
    public static class AuthorityUrl {
        private ObjectProperty<ServerType.GeniServerRole> role = new SimpleObjectProperty<ServerType.GeniServerRole>();
        private StringProperty version = new SimpleStringProperty();
        private ObjectProperty<URL> url = new SimpleObjectProperty<URL>();

        public AuthorityUrl(ServerType serverType, URL url) {
            this.role.set(serverType.getRole());
            this.version.set(serverType.getVersion());
            this.url.set(url);
        }

        public ServerType getServerType() {
            return new ServerType(role.get(), version.get());
        }

        public URL getURL() {
            return url.get();
        }

        public ObjectProperty<ServerType.GeniServerRole> roleProperty() {
            return role;
        }

        public StringProperty versionProperty() {
            return version;
        }

        public ObjectProperty<URL> urlProperty() {
            return url;
        }
    }
    private final ListProperty<AuthorityUrl> urls = new SimpleListProperty<AuthorityUrl>(FXCollections.<AuthorityUrl>observableArrayList());
    public ObservableListValue<AuthorityUrl> getUrls() { return urls; }


    //////////////////////////////////////////////////////////////////
    /////////////////////////// Advertisement RSpec //////////////////
    //////////////////////////////////////////////////////////////////

    private ObjectProperty<RSpecInfo> availableAdvertisementRspec = new SimpleObjectProperty();
    private ObjectProperty<RSpecInfo> allAdvertisementRspec = new SimpleObjectProperty();

    public void setAdvertisementRspec(boolean available, String rspecString) {
        RSpecInfo ri;
        if (rspecString == null)
            ri = null;
        else
            ri = new RSpecInfo(rspecString, RSpecInfo.RspecType.ADVERTISEMENT, null, null, this);

        if (available)
            availableAdvertisementRspec.set(ri);
        else
            allAdvertisementRspec.set(ri);
    }

    public RSpecInfo getAvailableAdvertisementRspec() {
        return availableAdvertisementRspec.get();
    }

    public RSpecInfo getAllAdvertisementRspec() {
        return allAdvertisementRspec.get();
    }

    public ObjectProperty<RSpecInfo> availableAdvertisementRspecProperty() {
        return availableAdvertisementRspec;
    }

    public ObjectProperty<RSpecInfo> allAdvertisementRspecProperty() {
        return allAdvertisementRspec;
    }
}
