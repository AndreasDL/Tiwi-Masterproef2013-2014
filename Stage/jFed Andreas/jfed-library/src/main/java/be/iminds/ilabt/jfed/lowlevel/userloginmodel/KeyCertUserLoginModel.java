package be.iminds.ilabt.jfed.lowlevel.userloginmodel;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.KeyUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * This is a UserLoginModel that reads a key and certificate, and automatically extracts the authority and user URN.
 *
 * It also decodes and stores the private key using the password
 */
public class KeyCertUserLoginModel implements UserLoginModel {
    public static boolean debug = false;

    private static enum PemSource { FILE, STRING };
    private PemSource pemSource;
    protected File keyCertFile;
    protected String keyCertContent;
    protected X509Certificate certificate;
    protected PrivateKey privateKey; //unencrypted, so never saved directly!

    protected SfaAuthority authority;
    protected String authorityUrn;
    protected String userUrn;

    protected String errorInfo;

    protected boolean passwordRequired;


    /**
     * Override current values with keyCertFile and automatic values derived from keyCertFile
     *
     * It is not required that authority and user urn are found.
     *
     * @return true if all requirements are met. False otherwise. (also false if keyCertFile could not be successfully read)
     */
    public boolean setKeyCertPemFile(File newKeyCertFile) {
        this.keyCertFile = null;
        this.pemSource = PemSource.FILE; //even if set to null, it still is source from file
        this.keyCertContent = null;
        this.authority = null;
        this.authorityUrn = null;
        this.userUrn = null;
        this.privateKey = null;
        this.certificate = null;

        errorInfo = "";
        if (debug) errorInfo += "DEBUG: setKeyCertPemFile\n";

        String userAuth = null;

        if (newKeyCertFile == null) {
//            errorInfo += "Key and Certificate file is null";
            this.keyCertFile = null;
            keyCertContent = null;
            return false;
        }

        if (!newKeyCertFile.exists()) {
            errorInfo += "Key and Certificate file does not exist: \""+newKeyCertFile.getPath()+"\"";
            this.keyCertFile = newKeyCertFile;
            keyCertContent = null;
            return false;
        }

        try {
            this.keyCertFile = newKeyCertFile;
            keyCertContent = IOUtils.fileToString(newKeyCertFile);
        } catch (Exception e) {
            errorInfo += "Error reading \""+keyCertFile.getPath()+"\": "+e.getMessage();
            this.keyCertFile = newKeyCertFile;
            keyCertContent = null;
            return false;
        }

        assert this.keyCertFile != null;
        boolean validContent = processPemContent();
        return validContent;
    }
    /**
     * Override current values with String contents and automatic values derived from keyCertFile
     *
     * It is not required that authority and user urn are found.
     *
     * @return true if all requirements are met. False otherwise. (also false if keyCertFile could not be successfully read)
     */
    public boolean setKeyCertPemString(String newKeyCertContent) {
        assert newKeyCertContent != null;

        this.keyCertFile = null;
        this.pemSource = PemSource.STRING;
        this.keyCertContent = null;
        this.authority = null;
        this.authorityUrn = null;
        this.userUrn = null;
        this.privateKey = null;
        this.certificate = null;

        errorInfo = "";
        if (debug) errorInfo += "DEBUG: setKeyCertPemString\n";

        this.keyCertContent = newKeyCertContent;
        boolean validContent = processPemContent();
        return validContent;
    }

    /**
     * @returns whether PEM content is valid. If not all info is found, it is still valid!
     * */
    protected boolean processBasicPemContent() {
        assert keyCertContent != null;
        this.authority = null;
        this.authorityUrn = null;
        this.userUrn = null;

        if (debug) errorInfo += "DEBUG: processing PEM content\n";

        if (keyCertContent == null) {
            errorInfo += "Key and Certificate PEM is empty\n";
            return false;
        }

        if (!keyCertContent.trim().startsWith("-----BEGIN")) {
            errorInfo += "Key and Certificate PEM does not have expected content\n";
            return false;
        }

        certificate = KeyUtil.pemToX509Certificate(keyCertContent);
        if (certificate == null) {
            errorInfo += "Error parsing certificate PEM\n";
            return false;
        }


        boolean hasKey = KeyUtil.hasRsaPrivateKey(keyCertContent);
        if (hasKey) {
            boolean encryptedKey = KeyUtil.hasEncryptedRsaPrivateKey(keyCertContent);
            if (debug) errorInfo += "DEBUG: PEM has private key. encrypted="+encryptedKey+"\n";
            this.passwordRequired = encryptedKey;
            if (!encryptedKey)
                try {
                    this.privateKey = KeyUtil.pemToRsaPrivateKey(keyCertContent, null);
                    if (debug) errorInfo += "DEBUG: decoded unprotected private key\n";
                } catch (KeyUtil.PEMDecodingException pemDecodingError) {
                    errorInfo += "Error reading private key: "+pemDecodingError.getMessage()+"\n";
                    this.privateKey = null;
                }
        } else {
            errorInfo += "Error no private key found in PEM\n";
            return false;
        }
        return true;
    }

    protected boolean processPemContent() {
        boolean success = processBasicPemContent();
        if (success)
            deriveFromPemContent();
        return success;
    }

    protected void deriveFromPemContent() {
        assert certificate != null;
        Collection<List<?>> altNames = null;
        try {
            altNames = certificate.getSubjectAlternativeNames();
        } catch (CertificateParsingException e) {
            errorInfo += "Error processing certificate alternate names: "+e.getMessage();
            return;
        }

        String userAuth = null;

        if (debug) errorInfo += "DEBUG: certificate has alt names: "+(altNames != null && altNames.isEmpty())+"\n";

        if (altNames != null)
            for (List<?> altName : altNames) {
                Integer nameType = (Integer) altName.get(0);
                if (debug) errorInfo += "DEBUG: certificate has altname of type "+nameType+"\n";
                if (nameType == 6) {
                    //uniformResourceIdentifier
                    String urn = (String) altName.get(1);
                    GeniUrn geniUrn = GeniUrn.parse(urn);
                    if (geniUrn == null || !geniUrn.getResourceType().equals("user"))
                        errorInfo += "Warning: certificate alternative name URI is not a valid user urn: \"" + urn + "\"  (will be ignored)";
                    else {
                        userAuth = geniUrn.getTopLevelAuthority();
                        String userName = geniUrn.getResourceName();
                        userUrn = urn;
                        if (debug) errorInfo += "DEBUG:    processed altName of URN type. userUrn="+userUrn+"\n";
                        //                        System.out.println("DEBUG FOUND in cerificate: userUrn=\""+userUrn+"\"  userAuth=\""+userAuth+"\"  userName=\""+userName+"\"");
                    }
                }
            }
        else
            errorInfo += "WARNING: no alternative names in certificate\n";


        //check other fields in certificate
        //        System.out.println("DEBUG getSubjectAlternativeNames="+cert.getSubjectAlternativeNames());
        //
        //        //emailaddress is part of CN
        //        Pattern cnPattern = Pattern.compile(".*CN=([^ ,]*)[ ,]*.*");
        //        Pattern emailAddressPattern = Pattern.compile("emailAddress=([^ ,]*)");
        //        Pattern ouPattern = Pattern.compile("CN=([^ ,]*)");
        //        Matcher matchCN = cnPattern.matcher(cert.getSubjectX500Principal().toString());
        //        Matcher matchEmailAddress = emailAddressPattern.matcher(cert.getSubjectX500Principal().toString());
        //        Matcher matchOU = ouPattern.matcher(cert.getSubjectX500Principal().toString());
        //
        //        String emailAddress = null;
        //        if (matchEmailAddress.matches())
        //            emailAddress = matchEmailAddress.group(1);
        //
        //        String ou = null;
        //        if (matchOU.matches())
        //            ou = matchOU.group(1);
        //
        //        System.out.println("DEBUG getSubjectX500Principal()="+cert.getSubjectX500Principal());
        //        System.out.println("DEBUG getSubjectX500Principal().getName()="+cert.getSubjectX500Principal().getName());
        //        System.out.println("DEBUG getSubjectX500Principal().getName(\"CANONICAL\")="+cert.getSubjectX500Principal().getName("CANONICAL"));


        if (userAuth != null) {
            //find authority
            for (SfaAuthority curAuth : authorityListModel.getAuthorities())
                if (curAuth.getNameForUrn().equals(userAuth)) {
                    authority = curAuth;
                    if (debug) errorInfo += "DEBUG:    found authority of user matching \""+userAuth+"\": "+authority.getName()+"\n";
                }

            if (authorityUrn == null && authority != null)
                authorityUrn = authority.getUrn();

            //                System.out.println("DEBUG FOUND using previously found userAuth=\""+userAuth+"\": authorityUrn=\""+authorityUrn+"\"  authority.getName()=\""+authority.getName()+"\"");

            if (authority == null) {
                errorInfo += ("WARNING: User authority found \""+userAuth+"\", but no authority info about it is known. Try adding authority info to the internal list and try again.");
            }
        } else {
            errorInfo += "ERROR: Did not find info about user (urn and authority) in certificate.\n";
        }
    }


    protected AuthorityListModel authorityListModel;
    protected UserLoginModelManager userLoginModelManager;

    public KeyCertUserLoginModel(AuthorityListModel authorityListModel, UserLoginModelManager userLoginModelManager) {
        this.authorityListModel = authorityListModel;
        this.userLoginModelManager = userLoginModelManager;

        this.keyCertFile = null;
        this.pemSource = PemSource.FILE;
        this.keyCertContent = null;
        this.authority = null;
        this.authorityUrn = null;
        this.userUrn = null;
    }

    public void save(UserLoginModelManager.UserLoginModelPreferences prefs) {
        System.out.println("KeyCertUserLoginModel.save(prefs)\n");

        if (debug) errorInfo += "DEBUG: saving preferences\n";
        if (pemSource == PemSource.FILE) {
            prefs.putBoolean("file", true);
            if (keyCertFile != null)
                prefs.put("keyCertFileURI", keyCertFile.toURI().toString());
        } else {
            prefs.putBoolean("file", false);
            if (keyCertContent != null)
                prefs.put("keyCertFileContent", keyCertContent);
        }
    }

    protected static File defaultKeyCertFile = new File(System.getProperty("user.home")+File.separator+".ssl"+File.separator+"geni_cert.pem");

    public void reset() {
        this.keyCertFile = null;
        this.pemSource = PemSource.FILE;
        this.keyCertContent = null;
        this.authority = null;
        this.authorityUrn = null;
        this.userUrn = null;
    }
    public void defaults() {
        reset();
        setKeyCertPemFile(defaultKeyCertFile);
    }

    public void load(UserLoginModelManager.UserLoginModelPreferences prefs) {
        if (debug) errorInfo += "DEBUG: loading from stored preferences\n";
        reset();

        boolean fromFile = prefs.getBoolean("file", true);

        if (fromFile) {
            String keyCertFileUri = prefs.get("keyCertFileURI", defaultKeyCertFile.toURI().toString());
            try {
                setKeyCertPemFile(new File(new URI(keyCertFileUri)));
            } catch (URISyntaxException e) {
                errorInfo += "WARNING: Stored file URI is not a valid URI: "+e.getMessage();
                this.keyCertFile = null;
                this.keyCertContent = null;
                prefs.remove("keyCertFileURI");
            }
        } else {
            this.keyCertFile = null;
            String loadedKeyCertContent = prefs.get("keyCertFileContent", null);
            setKeyCertPemString(loadedKeyCertContent);
        }
    }


    /**
     * password is not stored internally
     *
     * @returns success
     * */
    public boolean unlock(String password) {
        return unlock(password.toCharArray());
    }

    /**
     * password is not stored internally
     *
     * @returns success
     * */
    public boolean unlock(char[] password) {
        if (debug) errorInfo += "DEBUG: trying to unlock\n";
        if (keyCertContent == null) return false;

        try {
            this.privateKey = KeyUtil.pemToRsaPrivateKey(keyCertContent, password);

            if (privateKey == null)
                errorInfo += "ERROR: Failed to decode private key using password";
        } catch (KeyUtil.PEMDecodingException pemDecodingError) {
            errorInfo += "ERROR: Failed to decode private key using password: "+pemDecodingError.getMessage();
            this.privateKey = null;
        }

        return this.privateKey != null;
    }

    /**
     * Did the user specify a PEM file or String yet?
     * */
    public boolean isPemGiven() {
        return keyCertContent != null || keyCertFile != null;
    }
    /**
     * Only valid if isPemGiven() is true!
     * Did the user provide a filename to read the private key and certificate from?
     * */
    public boolean isFromFile() {
        return pemSource == PemSource.FILE;
    }
    /**
     * Only valid if isPemGiven() is true!
     * Did the user provide a String to read the private key and certificate from?
     * */
    public boolean isFromString() {
        return pemSource == PemSource.STRING;
    }


    public boolean isPasswordRequired() {
        return passwordRequired;
    }

    public String getErrorInfo() {
        return errorInfo;
    }


    public File getKeyCertFile() {
        return keyCertFile;
    }

    public String getKeyCertContent() {
        return keyCertContent;
    }

    @Override
    public String getUserUrn() {
        return userUrn;
    }




    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public SfaAuthority getUserAuthority() {
        return authority;
    }

    public String getUserAuthorityUrn() {
        return authorityUrn;
    }




    public boolean isValid() {
        if (authority == null) return false;
        if (userUrn == null) return false;
        if (keyCertContent == null) return false;
        if (privateKey == null) return false;
        if (certificate == null) return false;
        return true;
    }



    @Override
    public GeniUser getLoggedInGeniUser() {
        if (!isValid()) return null;
        return this;
    }

    @Override
    public boolean isUserLoggedIn() {
        return isValid();
    }



    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("unimplemented");
    }
    @Override
    public int hashCode() {
        throw new RuntimeException("unimplemented");
    }
}
