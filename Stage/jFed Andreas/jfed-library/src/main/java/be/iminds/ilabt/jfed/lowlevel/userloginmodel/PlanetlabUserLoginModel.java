package be.iminds.ilabt.jfed.lowlevel.userloginmodel;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.PlanetlabSfaRegistryInterface;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.planetlab.PlanetlabCertificateFetcher;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.KeyUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.prefs.Preferences;

/**
 * This is a UserLoginModel that reads a key and certificate, and automatically extracts the authority and user URN.
 *
 * It also decodes and stores the private key using the password
 */
public class PlanetlabUserLoginModel implements UserLoginModel {
    protected String planetlabSfaHrn;

    protected SfaAuthority authority;

    protected File sshPrivateKeyFile;
    protected String privateKeyContent;

    protected X509Certificate certificate;
    protected RSAPrivateKey privateKey; //unencrypted, so never saved directly!
    protected KeyPair keypair; //unencrypted, so never saved directly!

    protected String userUrn;

    protected String details;

    protected boolean passwordRequired;


    public void setAuthority(SfaAuthority auth) {
        this.authority = auth;
    }

    /**
     */
    public boolean setSshPrivateKeyFile(File newSshPrivateKeyFile) {
        //partial reset
        this.sshPrivateKeyFile = null;
        this.passwordRequired = false;
        this.privateKeyContent = null;
        this.privateKey = null;
        this.keypair = null;

        details = "";

        if (newSshPrivateKeyFile == null) {
            this.sshPrivateKeyFile = null;
            return false;
        }

        if (!newSshPrivateKeyFile.exists()) {
            details += "SSH Private key file does not exist: \""+newSshPrivateKeyFile.getPath()+"\"";
            this.sshPrivateKeyFile = newSshPrivateKeyFile;
            privateKeyContent = null;
            return false;
        }

        try {
            this.sshPrivateKeyFile = newSshPrivateKeyFile;
            privateKeyContent = IOUtils.fileToString(newSshPrivateKeyFile);
        } catch (Exception e) {
            details += "Error reading \""+newSshPrivateKeyFile.getPath()+"\": "+e.getMessage();
            this.sshPrivateKeyFile = newSshPrivateKeyFile;
            privateKeyContent = null;
            return false;
        }

        assert this.sshPrivateKeyFile != null;
        assert this.privateKeyContent != null;

        boolean hasKey = KeyUtil.hasRsaPrivateKey(privateKeyContent);
        if (hasKey) {
            boolean encryptedKey = KeyUtil.hasEncryptedRsaPrivateKey(privateKeyContent);
            details += "DEBUG: PEM has private key. encrypted="+encryptedKey+"\n";
            this.passwordRequired = encryptedKey;
            if (!encryptedKey)
                try {
                    this.keypair = KeyUtil.pemToRsaKeyPair(privateKeyContent, null);
                    this.privateKey = (RSAPrivateKey) keypair.getPrivate();
                    details += "DEBUG: decoded unprotected private key\n";
                } catch (KeyUtil.PEMDecodingException pemDecodingError) {
                    details += "Error reading private key: "+pemDecodingError.getMessage();
                    this.privateKey = null;
                    this.keypair = null;
                }
        } else {
            details += "Error no private key found in \""+sshPrivateKeyFile.getPath()+"\"";
            privateKeyContent = null;
            return false;
        }

        return true;
    }


    public void setPlanetlabSfaHrn(String planetlabSfaHrn) {
        this.planetlabSfaHrn = planetlabSfaHrn;

        if (correctPlanetlabSfaUrn() && !correctAuthority()) {
            //see if we can find the authority automatically
            GeniUrn u = sfaHrnToUrn(planetlabSfaHrn);
            if (u.getTopLevelAuthority().startsWith("ple:") || u.getTopLevelAuthority().equals("ple")) {
                SfaAuthority authority2 = authorityListModel.getByUrn("urn:publicid:IDN+"+u.getTopLevelAuthority()+"+authority+cm");
                if (authority2 != null) {
                    authority = authority2;
                    return;
                }
                SfaAuthority authority1 = authorityListModel.getByUrn("urn:publicid:IDN+ple+authority+cm");
                if (authority1 != null) {
                    authority = authority1;
                    return;
                }
            }
        }
    }

    protected AuthorityListModel authorityListModel;
    protected UserLoginModelManager userLoginModelManager;
    private Logger logger;

    public PlanetlabUserLoginModel(AuthorityListModel authorityListModel, UserLoginModelManager userLoginModelManager, Logger logger) {
        this.authorityListModel = authorityListModel;
        this.userLoginModelManager = userLoginModelManager;
        this.logger = logger;

        reset();
    }


    protected static File defaultSshPrivateKeyFile = new File(System.getProperty("user.home")+File.separator+".ssh"+File.separator+"id_rsa");

    public void reset() {
        this.planetlabSfaHrn = null;
        this.authority = null;
        this.sshPrivateKeyFile = null;

        //derived
        this.certificate = null;

        this.passwordRequired = false;
        this.privateKeyContent = null;
        this.userUrn = null;
        this.privateKey = null;
        this.keypair = null;
    }
    public void defaults() {
        reset();
        setSshPrivateKeyFile(defaultSshPrivateKeyFile);
    }

    public void save(UserLoginModelManager.UserLoginModelPreferences prefs) {
        System.out.println("PlanetlabUserLoginModel.save(prefs)\n");

        if (sshPrivateKeyFile != null) {
            prefs.put("sshPrivateKeyFile", sshPrivateKeyFile.toURI().toString());
        } else {
            prefs.remove("sshPrivateKeyFile");
        }
        if (planetlabSfaHrn != null) {
            prefs.put("planetlabSfaHrn", planetlabSfaHrn);
        } else {
            prefs.remove("planetlabSfaHrn");
        }
        if (authority != null) {
            prefs.put("planetlabAuthority", authority.getUrn());
        } else {
            prefs.remove("planetlabAuthority");
        }
        if (certificate != null) {
            prefs.put("planetlabRetreivedCertificate", KeyUtil.x509certificateToPem(certificate));
        } else {
            prefs.remove("planetlabRetreivedCertificate");
        }
    }
    public void load(UserLoginModelManager.UserLoginModelPreferences prefs) {
        reset();

        planetlabSfaHrn = prefs.get("planetlabSfaHrn", null);
        if (planetlabSfaHrn != null) {
            if (!correctPlanetlabSfaUrn()) {
                details += "WARNING: Stored planetlabSfaHrn not valid: "+planetlabSfaHrn;
                prefs.remove("planetlabSfaHrn");
            }
            GeniUrn planetlabUserUrn = sfaHrnToUrn(planetlabSfaHrn);
            userUrn = planetlabUserUrn.toString();
        }

        String planetlabAuthorityUrn = prefs.get("planetlabAuthority", null);
        authority = authorityListModel.getByUrn(planetlabAuthorityUrn);

        String planetlabRetreivedCertificatePem = prefs.get("planetlabRetreivedCertificate", null);
        if (planetlabRetreivedCertificatePem != null) {
            certificate = KeyUtil.pemToX509Certificate(planetlabRetreivedCertificatePem);
            if (certificate == null) {
                details += "WARNING: Stored certificate PEM is not valid: "+planetlabRetreivedCertificatePem;
                prefs.remove("planetlabRetreivedCertificate");
            }
        }

        String uri = prefs.get("sshPrivateKeyFile", defaultSshPrivateKeyFile.toURI().toString());
        try {
            setSshPrivateKeyFile(new File(new URI(uri)));
        } catch (URISyntaxException e) {
            details += "WARNING: Stored file URI is not a valid URI: "+e.getMessage();
            this.sshPrivateKeyFile = null;
            this.privateKeyContent = null;
            this.privateKey = null;
            this.keypair = null;
            prefs.remove("sshPrivateKeyFile");
        }
    }

    public boolean correctPlanetlabSfaUrn() {
        if (planetlabSfaHrn == null) return false;

        //expected format: <planetlab prefix>.<subauthority>.<username>
        // example:  SFA hrn:   ple.ibbtple.wim_vandemeerssche

//        System.out.println("planetlabSfaHrn="+planetlabSfaHrn+"  splitlen="+planetlabSfaHrn.split("\\.").length);

        return planetlabSfaHrn.split("\\.").length == 3;
    }

    public boolean correctAuthority() {
        if (authority == null) return false;
        if (authority.getUrl(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1) == null) return false;
        return true;
    }

    private static GeniUrn sfaHrnToUrn(String planetlabSfaHrn) {
        if (planetlabSfaHrn == null) return null;

        int lastDot = planetlabSfaHrn.lastIndexOf('.');
        assert lastDot != -1;
        String planetlabUsername = planetlabSfaHrn.substring(lastDot+1);
        String planetlabAuthName = planetlabSfaHrn.substring(0, lastDot).replace('.', ':');

        GeniUrn planetlabUserUrn = new GeniUrn(planetlabAuthName, "user", planetlabUsername);
        return planetlabUserUrn;
    }
    private static String urnToSfaHrn(GeniUrn urn) {
        if (urn == null) return null;

        assert urn.getResourceType().equals("user");
        return urn.getTopLevelAuthority().replace(':', '.') + "." + urn.getResourceName();
    }


    public boolean isReadyToFetchCertificate() {
//        System.out.println("isReadyToCreateLogin");
//        System.out.println("   correctPlanetlabSfaUrn()="+correctPlanetlabSfaUrn());
//        System.out.println("   authority="+authority);
//        if (authority != null)
//          System.out.println("   authority PlanetLabSliceRegistry URL="+authority.getUrl(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1).toString());
//        System.out.println("   keypair="+keypair);
//        System.out.println("   privateKey="+privateKey);
//        System.out.println("   privateKeyContent="+privateKeyContent);

        if (!correctPlanetlabSfaUrn()) return false;
        if (!correctAuthority()) return false;
        if (keypair == null) return false;
        if (privateKey == null) return false;
        if (privateKeyContent == null) return false;
        return true;
    }

    /**
     * Do everything necessary to create a Sfa login from the planetlab info.
     *
     * This writes details that can be provided as feedback to the user.
     *
     * returns success. If succesfull, this UserLoginModel is also a valid login
     * */
    public boolean fetchCertificate() {
        details = "";

        if (!isReadyToFetchCertificate()) {
            details += "Some data needed to fetch the certificate is missing.";
            return false;
        }

        GeniUrn planetlabUserUrn = sfaHrnToUrn(planetlabSfaHrn);
        userUrn = planetlabUserUrn.toString();
        details += "Your planetlab Sfa hrn (\""+planetlabSfaHrn+"\") is converted to the user URN: \""+planetlabUserUrn.toString()+"\"\n";




//        if (certificate != null) {
//            details += "Success: User certificate retreived from planetlab is already stored.\n";
//            return true;
//        }

        certificate = null;






        details += "To login to planetlab using Sfa, we need to create a self signed credential, and then use that to request the real credential from planetlab.\n";

        X509Certificate selfSignedCert = PlanetlabCertificateFetcher.createSelfSignedCertificate(authority, planetlabSfaHrn, privateKeyContent, keypair);
        if (selfSignedCert == null) {
            details += "ERROR: Failed to create self signed certificate for initial planetlab connection.\n";
            userUrn = null;
            return false;
        }
        details += "   Created self signed certificate for initial planetlab connection.\n";

        GeniConnectionProvider connectionProvider = new GeniConnectionPool();

        PlanetlabSfaRegistryInterface pleReg = new PlanetlabSfaRegistryInterface(logger, false);
        PlanetlabSfaRegistryInterface.SimpleApiCallReply<GeniCredential> cred = null;
        try {
            //create user
            final GeniUser user = new SimpleGeniUser(authority, planetlabUserUrn, selfSignedCert, privateKey);
            cred = pleReg.getSelfCredential(connectionProvider.getConnectionByAuthority(
                    user, authority, PlanetlabSfaRegistryInterface.class),
                    KeyUtil.x509certificateToPem(selfSignedCert),
                    planetlabUserUrn.toString(),
                    "");
        } catch (GeniException e) {
            details += "ERROR: Failed to call \"GetSelfCredential\" on planetlab server: "+e.getMessage()+"\n";
            e.printStackTrace();
            userUrn = null;
            return false;
        }

        if (cred == null || cred.getValue() == null){
            details += "ERROR: Call \"GetSelfCredential\" on planetlab server returned empty credential\n";
            userUrn = null;
            return false;
        }

        String gid = cred.getValue().getTargetGid();
        //gid can contain multiple certs. We just take the first.
        certificate = KeyUtil.pemToX509Certificate(gid);

        details += "Success: Call \"GetSelfCredential\" on planetlab server returned our real certificate.\n";

        //all should be ok to login now!
        return true;
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
        if (privateKeyContent == null) return false;

        try {
            this.keypair = KeyUtil.pemToRsaKeyPair(privateKeyContent, password);
            this.privateKey = (RSAPrivateKey) keypair.getPrivate();

            if (keypair == null || privateKey == null)
                details += "ERROR: Failed to unlock private key using password";
            else
                details += "unlocked ssh private key using password\n";
        } catch (KeyUtil.PEMDecodingException pemDecodingError) {
            details += "ERROR: Failed to unlock private key using password: "+pemDecodingError.getMessage();
            this.privateKey = null;
        }

        return this.privateKey != null;
    }



    public boolean isPasswordRequired() {
        return passwordRequired;
    }

    public String getDetails() {
        return details;
    }


    public File getSshPrivateKeyFile() {
        return sshPrivateKeyFile;
    }

    public String getPrivateKeyContent() {
        return privateKeyContent;
    }

    public String getSfaHrn() {
        return planetlabSfaHrn;
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
        return authority.getUrn();
    }




    public boolean isValid() {
        if (authority == null) return false;
        if (userUrn == null) return false;
        if (planetlabSfaHrn == null) return false;
        if (sshPrivateKeyFile == null) return false;
        if (privateKeyContent == null) return false;
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
