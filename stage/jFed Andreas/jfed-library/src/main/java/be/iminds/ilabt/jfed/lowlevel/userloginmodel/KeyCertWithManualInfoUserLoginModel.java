package be.iminds.ilabt.jfed.lowlevel.userloginmodel;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.KeyUtil;
import sun.security.x509.AuthorityInfoAccessExtension;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * UserLoginModel stores all preferences related to the user login:
 *   - SSL key and certificate file
 *
 *   it also stores the derived data, or allows overriding it:
 *   - authority to use
 *      - info about authority
 *      (stores urn seperately, for cases where authority is not known, but urn is)
 *   - username
 *   - user urn
 *
 *   - clearinghouse to use
 * It does not store the password, but it does store the unencrypted private key
 *
 * It is an implementation of GeniUser that is used as the underlying model for the UserLoginPanel
 *
 * Note that UserLoginModel can be in invalid states, where this is not expected users of the GeniUser interface
 */
public class KeyCertWithManualInfoUserLoginModel extends KeyCertUserLoginModel {

    public KeyCertWithManualInfoUserLoginModel(AuthorityListModel authorityListModel, UserLoginModelManager userLoginModelManager) {
        super(authorityListModel, userLoginModelManager);
    }

    @Override
    protected boolean processPemContent() {
        boolean success = processBasicPemContent();
        return success;
    }

    public void setAuthority(SfaAuthority auth) {
        this.authority = auth;
        if (authority != null)
            this.authorityUrn = auth.getUrn();
        else
            this.authorityUrn = null;
    }

    public void setUserUrn(String urn) {
        this.userUrn = urn;
    }

    public boolean isValidUserUrn() {
        if (userUrn == null) return false;
        GeniUrn u = GeniUrn.parse(userUrn);
        if (u == null) return false;
        if (!u.getResourceType().equals("user")) return false;
        return true;
    }

    public boolean deriveAuthorityFromUrn() {
        if (userUrn == null) return false;
        GeniUrn u = GeniUrn.parse(userUrn);
        if (u == null) return false;
        String userAuthPart = u.getTopLevelAuthority();

        //find authority
        for (SfaAuthority curAuth : authorityListModel.getAuthorities())
            if (curAuth.getNameForUrn().equals(userAuthPart)) {
                authority = curAuth;
                if (debug) errorInfo += "DEBUG:    found authority of user matching \""+userAuthPart+"\": "+authority.getName()+"\n";
                authorityUrn = authority.getUrn();
                return true;
            }

        return false;
    }

    public boolean authorityMatchesUrn() {
        if (authority == null) return false;
        if (userUrn == null) return false;
        GeniUrn u = GeniUrn.parse(userUrn);
        if (u == null) return false;
        String userAuthPart = u.getTopLevelAuthority();

        return authority.getNameForUrn().equals(userAuthPart);
    }

    @Override
    public void save(UserLoginModelManager.UserLoginModelPreferences prefs) {
        super.save(prefs);
        prefs.put("keyCertFileURI", keyCertFile.toURI().toString());

        if (authorityUrn != null)
            prefs.put("userauthurn", authorityUrn);
        else
            prefs.remove("userauthurn");

        if (userUrn != null)
            prefs.put("userurn", userUrn);
        else
            prefs.remove("userurn");
    }

    @Override
    public void load(UserLoginModelManager.UserLoginModelPreferences prefs) {
        super.load(prefs);

        authority = null;
        authorityUrn = prefs.get("userauthurn", null);
        userUrn = prefs.get("userurn", null);

        if (authorityUrn != null)
            for (SfaAuthority curAuth : authorityListModel.getAuthorities())
                if (curAuth.getUrn().equals(authorityUrn)) {
                    authority = curAuth;
                    if (debug) errorInfo += "DEBUG:    found authority of user matching \""+authorityUrn+"\": "+authority.getName()+"\n";
                    return;
                }
    }


//    private File keyCertFile;
//    private String keyCertContent;
//    private X509Certificate certificate;
//    private PrivateKey privateKey;
//
//    private boolean overrideAuth;
//    private SfaAuthority authority;
//    private String authorityUrn;
////    private boolean overrideUserName;
////    private String userName;
//    private boolean overrideUrn;
//    private String userUrn;
//
//    private String errorInfo;
//
//
//    private boolean setKeyCertFile(File keyCertFile) {
//        this.keyCertFile = keyCertFile;
//        this.keyCertContent = null;
//        if (!keyCertFile.exists()) {
//            errorInfo += "Key and Certificate file does not exist: \""+keyCertFile.getPath()+"\"";
//            //throw new GeniException("Key and Certificate file does not exist: \""+keyCertFile.getPath()+"\"");
//            keyCertFile = null;
//            keyCertContent = null;
//            return false;
//        }
//        try {
//            keyCertContent = IOUtils.fileToString(keyCertFile);
//        } catch (Exception e) {
//            errorInfo += "Error reading \""+keyCertFile.getPath()+"\": "+e.getMessage();
//            keyCertFile = null;
//            keyCertContent = null;
//            return false;
//        }
//        if (!keyCertContent.trim().startsWith("-----BEGIN")) {
//            String extra = ""; if (keyCertContent.length() >= 100) extra = " ...";
//            //            throw new GeniException("Key and Certificate file does not have expected content: \""+keyCertFile.getPath()+"\" -> \""+keyCertContent.substring(0,100)+"\""+extra);
//            errorInfo += "Key and Certificate file does not have expected content: \""+keyCertFile.getPath()+"\" -> \""+keyCertContent.substring(0,100)+"\""+extra;
//            keyCertFile = null;
//            keyCertContent = null;
//            return false;
//        }
//
//        certificate = KeyUtil.pemToX509Certificate(keyCertContent);
//        if (certificate == null) {
//            errorInfo += "Error parsing certificate in \""+keyCertFile.getPath()+"\"";
//            keyCertFile = null;
//            keyCertContent = null;
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Override current values with keyCertFile and automatic values derived from keyCertFile
//     * @return true if all values could be automatically filled in. False otherwise. (also false if keyCertFile could not be successfully read)
//     */
//    public boolean set(File keyCertFile) {
//        overrideAuth = false;
////        overrideUserName = false;
//        overrideUrn = false;
//        this.keyCertFile = keyCertFile;
//        this.keyCertContent = null;
//        this.authority = null;
//        this.authorityUrn = null;
////        this.userName = null;
//        this.userUrn = null;
//
//        errorInfo = "";
//
//        String userAuth = null;
//
//        boolean validKeyCertFile = setKeyCertFile(keyCertFile);
//        if (validKeyCertFile && keyCertContent != null) {
//            certificate = KeyUtil.pemToX509Certificate(keyCertContent);
//            if (certificate == null) {
//                errorInfo += "Error parsing certificate in \""+keyCertFile.getPath()+"\"";
//                keyCertFile = null;
//                keyCertContent = null;
//                return false;
//            }
//
//            //System.out.println("DEBUG cert="+cert);
//            Collection<List<?>> altNames = null;
//            try {
//                altNames = certificate.getSubjectAlternativeNames();
//            } catch (CertificateParsingException e) {
//                errorInfo += "Error processing certificate alternate names: "+e.getMessage();
//                return false;
//            }
//            if (altNames != null)
//                for (List<?> altName : altNames) {
//                    Integer nameType = (Integer) altName.get(0);
//                    if (nameType == 6) {
//                        //uniformResourceIdentifier
//                        String urn = (String) altName.get(1);
//                        GeniUrn geniUrn = GeniUrn.parse(urn);
//                        if (geniUrn == null || !geniUrn.getResourceType().equals("user"))
//                            System.err.println("Warning: certificate alternative name URI is not a user urn: \"" + urn + "\"  (will be ignored)");
//                        else {
//                            userAuth = geniUrn.getTopLevelAuthority();
//                            String userName = geniUrn.getResourceName();
//                            userUrn = urn;
//                            //                        System.out.println("DEBUG FOUND in cerificate: userUrn=\""+userUrn+"\"  userAuth=\""+userAuth+"\"  userName=\""+userName+"\"");
//                        }
//                    }
//                }
//
//
//            //check other fields in certificate
////        System.out.println("DEBUG getSubjectAlternativeNames="+cert.getSubjectAlternativeNames());
////
////        //emailaddress is part of CN
////        Pattern cnPattern = Pattern.compile(".*CN=([^ ,]*)[ ,]*.*");
////        Pattern emailAddressPattern = Pattern.compile("emailAddress=([^ ,]*)");
////        Pattern ouPattern = Pattern.compile("CN=([^ ,]*)");
////        Matcher matchCN = cnPattern.matcher(cert.getSubjectX500Principal().toString());
////        Matcher matchEmailAddress = emailAddressPattern.matcher(cert.getSubjectX500Principal().toString());
////        Matcher matchOU = ouPattern.matcher(cert.getSubjectX500Principal().toString());
////
////        String emailAddress = null;
////        if (matchEmailAddress.matches())
////            emailAddress = matchEmailAddress.group(1);
////
////        String ou = null;
////        if (matchOU.matches())
////            ou = matchOU.group(1);
////
////        System.out.println("DEBUG getSubjectX500Principal()="+cert.getSubjectX500Principal());
////        System.out.println("DEBUG getSubjectX500Principal().getName()="+cert.getSubjectX500Principal().getName());
////        System.out.println("DEBUG getSubjectX500Principal().getName(\"CANONICAL\")="+cert.getSubjectX500Principal().getName("CANONICAL"));
//
//            if (userAuth != null) {
//                //find authority
//                for (SfaAuthority curAuth : authorityListModel.getAuthorities())
//                    if (curAuth.getNameForUrn().equals(userAuth))
//                        authority = curAuth;
//
//                if (authorityUrn == null && authority != null)
//                    authorityUrn = authority.getUrn();
////                System.out.println("DEBUG FOUND using previously found userAuth=\""+userAuth+"\": authorityUrn=\""+authorityUrn+"\"  authority.getName()=\""+authority.getName()+"\"");
//                if (authority == null)
//                    System.err.println("WARNING: UserLoginModel: no authority found for \""+userAuth+"\"");
//            }
//
//            if (validKeyCertFile && (userUrn == null || userAuth == null || authority == null)) {
//                System.err.println("WARNING: UserLoginModel: Could not derive all data from certificate. userUrn="+userUrn+
//                        " userAuth="+userAuth+" authority="+authority+"  cert="+certificate);
//                return false;
//            }
//        }
//
//        return validKeyCertFile;
//    }
//
//    /**
//     *  override automatic values derived from keyCertFile
//     *
//     * @return false if keyCertFile could not be successfully read, true otherwise */
//    public boolean set(File keyCertFile, SfaAuthority authority, String userUrn) {
//        return set(keyCertFile, authority.getUrn(), authority, userUrn);
//    }
//
//    /**
//     *  override automatic values derived from keyCertFile
//     *
//     * @return false if keyCertFile could not be successfully read, true otherwise */
//    public boolean set(File keyCertFile, String authorityUrn, SfaAuthority authority, String userUrn) {
//        overrideAuth = true;
////        overrideUserName = true;
//        overrideUrn = true;
//        this.keyCertFile = keyCertFile;
//        this.keyCertContent = null;
//        this.authority = authority;
//        this.authorityUrn = authorityUrn;
////        this.userName = userName;
//        this.userUrn = userUrn;
//        errorInfo = "";
//
//        return setKeyCertFile(keyCertFile);
//    }
//
//
//
//    private Preferences prefs;
//
//    private AuthorityListModel authorityListModel;
//    private UserLoginModelManager userLoginModelManager;
//
//    public KeyCertWithManualInfoUserLoginModel(AuthorityListModel authorityListModel, UserLoginModelManager userLoginModelManager) {
//        this.authorityListModel = authorityListModel;
//        this.userLoginModelManager = userLoginModelManager;
//
//        this.overrideAuth = false;
////        this.overrideUserName = false;
//        this.overrideUrn = false;
//        this.keyCertFile = null;
//        this.keyCertContent = null;
//        this.authority = null;
//        this.authorityUrn = null;
////        this.userName = null;
//        this.userUrn = null;
//
//        Preferences prefsRoot = Preferences.userRoot();
//        prefs = prefsRoot.node("be.iminds.ilabt.jfed.ui.UserLoginPreferences");
////        reset();
//    }
//
//    public void save() {
//        prefs.put("keyCertFileURI", keyCertFile.toURI().toString());
//
//        prefs.put("overrideAuth", overrideAuth+"");
////        prefs.put("overrideUserName", overrideUserName+"");
//        prefs.put("overrideUrn", overrideUrn+"");
//
////        if (overrideUserName)
////            prefs.put("username", userName);
//        if (overrideUrn)
//            prefs.put("userurn", userUrn);
//
//        if (overrideAuth) {
//            prefs.put("authorityName",    authority.getName());
//            prefs.put("authorityUrn",     authorityUrn);
//            prefs.put("authoritySaUrl", authority.getUrl(ServerType.GeniServerRole.PROTOGENI_SA, 1).toString());
//            prefs.put("authorityAmUrl",   authority.getUrl(ServerType.GeniServerRole.AM, 1).toString());
//            prefs.put("authorityAm2Url",  authority.getUrl(ServerType.GeniServerRole.AM, 2).toString());
//            prefs.put("authorityAm3Url",  authority.getUrl(ServerType.GeniServerRole.AM, 3).toString());
//            prefs.put("authorityType",  authority.getType().toString());
//        }
//    }
//
//    private static File defaultKeyCertFile = new File(System.getProperty("user.home")+File.separator+".ssl"+File.separator+"geni_cert.pem");
//
//    public void reset() {
////        overrideAuth = false;
////        overrideUserName = false;
////        overrideUrn = false;
////
////        keyCertFile = new File(System.getProperty("user.home")+File.separator+".ssl"+File.separator+"geni_cert.pem");
////        userName = null;
////        clearingHouseUrl = "https://www.emulab.net/protogeni/xmlrpc/ch";
////
//////        if (authorityListModel.getAuthorities().size() == 0)
//////            UtahClearingHouseAuthorityList.load(authorityListModel);
////        authority = null;
////        //authority = authorityListModel.getAuthorities().get(0);
//
//        set(defaultKeyCertFile);
//    }
//
//    public void load() {
////        reset();
//        this.overrideAuth = false;
////        this.overrideUserName = false;
//        this.overrideUrn = false;
//        this.keyCertFile = null;
//        this.keyCertContent = null;
//        this.authority = null;
//        this.authorityUrn = null;
////        this.userName = null;
//        this.userUrn = null;
//
//        String keyCertFileUri = prefs.get("keyCertFileURI", defaultKeyCertFile.toURI().toString());
//        try {
//            keyCertFile = new File(new URI(keyCertFileUri));
//        } catch (URISyntaxException e) {
//            System.err.println("WARNING: Stored keyCertFileURI is not a valid URI: "+e.getMessage());
//            keyCertFile = null;
//            prefs.remove("keyCertFileURI");
//        }
//
//        overrideAuth = Boolean.parseBoolean(prefs.get("overrideAuth", "false"));
////        overrideUserName = Boolean.parseBoolean(prefs.get("overrideUserName", "false"));
//        overrideUrn = Boolean.parseBoolean(prefs.get("overrideUrn", "false"));
//        if (overrideAuth != overrideUrn) {
//            System.err.println("WARNING: Unsupported combination of overrides, removing stored overrides.");
//            prefs.remove("overrideAuth");
////            prefs.remove("overrideUserName");
//            prefs.remove("overrideUrn");
//            overrideAuth = false;
////            overrideUserName = false;
//            overrideUrn = false;
//        }
//
////        assert overrideUserName == overrideUrn;
//        assert overrideAuth == overrideUrn;
//
//        //TODO: should we stop if reading it fails?
//        setKeyCertFile(keyCertFile);
//
////        if (overrideUserName)
////            userName = prefs.get("username", null);
//        if (overrideUrn)
//            userUrn = prefs.get("userurn", null);
//
//        if (overrideAuth) {
//            String authorityName    = prefs.get("authorityName", null);
//            String loadedAuthorityUrn = prefs.get("authorityUrn", null);
//            String authoritySaUrl   = prefs.get("authoritySaUrl", null);
//            String authorityAmUrl   = prefs.get("authorityAmUrl", null);
//            String authorityAm2Url  = prefs.get("authorityAm2Url", null);
//            String authorityAm3Url  = prefs.get("authorityAm3Url", null);
////            String authoritySaUrl   = prefs.get("authoritySaUrl", authority.getUrl(ServerType.GeniServerRole.SA, 1).toString());
////            String authorityAmUrl   = prefs.get("authorityAmUrl", authority.getUrl(ServerType.GeniServerRole.AM, 1).toString());
////            String authorityAm2Url  = prefs.get("authorityAm2Url", authority.getUrl(ServerType.GeniServerRole.AM, 2).toString());
////            String authorityAm3Url  = prefs.get("authorityAm3Url", authority.getUrl(ServerType.GeniServerRole.AM, 3).toString());
//            String authorityType  = prefs.get("authorityType", null);
////            String authorityType  = prefs.get("authorityType", authority.getType());
//
//            SfaAuthority loadedAuth = null;
//            if (authorityName == null || loadedAuthorityUrn == null || authoritySaUrl == null || authorityType == null) {
//                System.err.println("WARNING: Missing stored data for authority override, removing stored overrides");
//                prefs.remove("overrideAuth");
//                prefs.remove("overrideUserName");
//                prefs.remove("overrideUrn");
//            } else {
//                try {
//                    Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
//                    urls.put(new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1), new URL(authoritySaUrl));
//                    urls.put(new ServerType(ServerType.GeniServerRole.AM, 1), new URL(authorityAmUrl));
//                    urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL(authorityAm2Url));
//                    urls.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL(authorityAm3Url));
//                    loadedAuth = new SfaAuthority(loadedAuthorityUrn, authorityName, urls, null/*gid*/, authorityType);
//                } catch (MalformedURLException e) {
//                    prefs.remove("authorityUrn");
//                    prefs.remove("authoritySaUrl");
//                    prefs.remove("authorityAmUrl");
//                    prefs.remove("authorityAm2Url");
//                    prefs.remove("authorityAm3Url");
//                    prefs.remove("authorityType");
//                    throw new RuntimeException("URL should never be malformed here", e);
//                } catch (GeniException e) {
//                    prefs.remove("authorityUrn");
//                    prefs.remove("authoritySaUrl");
//                    prefs.remove("authorityAmUrl");
//                    prefs.remove("authorityAm2Url");
//                    prefs.remove("authorityAm3Url");
//                    prefs.remove("authorityType");
//                    throw new RuntimeException("Incorrect urn stored? "+authorityUrn+"  msg="+e.getMessage(), e);
//                }
//            }
//
//            authority = loadedAuth;
//            authorityUrn = loadedAuthorityUrn;
//        }
//
//        if (overrideAuth || overrideUrn) {
//            set(keyCertFile, authorityUrn, authority, userUrn);
//        } else {
//            set(keyCertFile);
//        }
//    }
//
//
//    public String getErrorInfo() {
//        return errorInfo;
//    }
//
//    public boolean isOverrideAuth() {
//        return overrideAuth;
//    }
//    public boolean isOverrideUrn() {
//        return overrideUrn;
//    }
////    public boolean isOverrideUserName() {
////        return overrideUserName;
////    }
//    public boolean isOverride() {
////        assert overrideUserName == overrideUrn;
//        assert overrideAuth == overrideUrn;
//        return overrideAuth || overrideUrn;
//    }
//
//    public File getKeyCertFile() {
//        return keyCertFile;
//    }
//
//    public String getKeyCertContent() {
//        return keyCertContent;
//    }
//
////    public void setKeyCertFile(File keyCertFile) {
////        if (! this.keyCertFile.equals(keyCertFile))
////            changed = true;
////        this.keyCertFile = keyCertFile;
////    }
//
//
//
////    public void setClearingHouseUrl(String url_ch) {
////        if (this.clearingHouseUrl != url_ch)
////            changed = true;
////        this.clearingHouseUrl = url_ch;
////    }
////
////    public void setAuthority(GeniAuthority authority) {
////        if (!this.authority.equals(authority))
////            changed = true;
////        this.authority = authority;
////    }
//
////    @Override
////    public String getUserName() {
////        return userName;
////    }
//
//    @Override
//    public String getUserUrn() {
//        return userUrn;
//    }
//
//    @Override
//    public PrivateKey getPrivateKey() {
//        return privateKey;
//    }
//
//    @Override
//    public X509Certificate getCertificate() {
//        return certificate;
//    }
//
//    @Override
//    public SfaAuthority getUserAuthority() {
//        return authority;
//    }
//
//    public String getUserAuthorityUrn() {
//        return authorityUrn;
//    }
//
//    public boolean checkUserUrn() {
////        String a = getUserAuthority().getNameForUrn();
//////        String u = getUserName();
////        String test = "urn:publicid:IDN+"+a+"+user+"+u;
////        return test.equals(userUrn);
//
//        //TODO recreate check, excluding username check
//        return true;
//    }
//
////    public void setUserUrn(String userUrn) {
////        if (this.userUrn != userUrn)
////            changed = true;
////        this.userUrn = userUrn;
////    }
////
////    public void setUserName(String userName) {
////        if (this.userName != userName)
////            changed = true;
////        this.userName = userName;
////    }
//
////    public String readKeyCert() {
////        return loadPemFile(getKeyCertFile());
////    }
//
//    public static String loadPemFile(File pemfile) {
//        if (pemfile.exists()) {
//            try {
//                return IOUtils.fileToString(pemfile);
//            } catch(IOException e) {
//                return "error reading "+pemfile.getName();
//            }
//        } else
//            return "File \""+pemfile.getPath()+"\" does not exist";
//    }
//
//    /**
//     * reads certificate and private key and stores them
//     *
//     * Stores certificate and (decrypted) private key.
//     * Does not store password.
//     * */
//    public boolean loadCertificateAndKey(char[] keyPass) throws GeniException {
//        if (keyCertContent == null)
//            return false;
//        if (keyPass.length > 0)
//            privateKey = KeyUtil.pemToRsaPrivateKey(keyCertContent, keyPass);
//        else
//            privateKey = KeyUtil.pemToRsaPrivateKey(keyCertContent, null);
//        if (privateKey == null)
//            throw new GeniException("ERROR: PEM key and certificate does not contain a key:"+keyCertContent);
//        certificate = KeyUtil.pemToX509Certificate(keyCertContent);
//        if (certificate == null)
//            throw new GeniException("ERROR: PEM key and certificate does not contain a X509 certificate:"+keyCertContent);
//        return true;
//    }
//
//    public boolean isValid() {
//        if (authority == null) return false;
////        if (userName == null) return false;
//        if (userUrn == null) return false;
//        if (keyCertFile == null) return false;
//        if (keyCertContent == null) return false;
//        if (privateKey == null) return false;
//        if (certificate == null) return false;
//        return true;
//    }
//
//
//    public static interface UserLoginPreferencesChangeListener {
//        public void onUserLoginPreferencesChanged(KeyCertWithManualInfoUserLoginModel userLoginModel, boolean valid);
//    }
//    private List<UserLoginPreferencesChangeListener> changeListeners = new ArrayList<UserLoginPreferencesChangeListener>();
//    public void fireChange() { //TODO make this private, and instead of calling this, make slice and sliver have a change listener mechanism triggering this?
//        for (UserLoginPreferencesChangeListener l : changeListeners)
//            l.onUserLoginPreferencesChanged(this, isValid());
//    }
//    public void addUserLoginPreferencesChangeListener(UserLoginPreferencesChangeListener l) {
//        changeListeners.add(l);
//    }
//    public void removeUserLoginPreferencesChangeListener(UserLoginPreferencesChangeListener l){
//        changeListeners.remove(l);
//    }
//
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        KeyCertWithManualInfoUserLoginModel that = (KeyCertWithManualInfoUserLoginModel) o;
//
//        if (keyCertFile != null ? !keyCertFile.equals(that.keyCertFile) : that.keyCertFile != null) return false;
//
//
//        if (overrideAuth != that.overrideAuth) return false;
//        if (overrideUrn != that.overrideUrn) return false;
//
//        if (overrideAuth && (authority != null ? !authority.equals(that.authority) : that.authority != null)) return false;
//        if (overrideAuth && (authorityUrn != null ? !authorityUrn.equals(that.authorityUrn) : that.authorityUrn != null)) return false;
//        if (overrideUrn && (userUrn != null ? !userUrn.equals(that.userUrn) : that.userUrn != null)) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = keyCertFile != null ? keyCertFile.hashCode() : 0;
//        result = 31 * result + (overrideAuth ? 1 : 0);
//        result = 31 * result + (authority != null ? authority.hashCode() : 0);
//        result = 31 * result + (authorityUrn != null ? authorityUrn.hashCode() : 0);
//        result = 31 * result + (overrideUrn ? 1 : 0);
//        result = 31 * result + (userUrn != null ? userUrn.hashCode() : 0);
//        return result;
//    }
}
