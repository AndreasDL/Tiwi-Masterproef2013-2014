package be.iminds.ilabt.jfed.lowlevel.planetlab;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.PlanetlabSfaRegistryInterface;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.KeyUtil;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * CertificateFetcher
 */
public class PlanetlabCertificateFetcher {
    public static X509Certificate createSelfSignedCertificate(SfaAuthority planetlabAuth, String planetlabSfaHrn, String privateKeyPem, KeyPair keypair) {
        assert privateKeyPem != null;
        assert keypair != null;

        X509Certificate cert = KeyUtil.makeSelfSigned(keypair, planetlabSfaHrn, 2);

        return cert;
    }

//    public static GeniCredential getCredential(SfaAuthority planetlabAuth, String username, String privateKeyPem, KeyPair keypair) {
//        assert privateKeyPem != null;
//        assert keypair != null;
//
////        String userurn = "urn:publicid:IDN+ple:ibbtple+user+"+username;
//        GeniUrn geniUserUrn = new GeniUrn(planetlabAuth.getNameForUrn(), "user", username); //note: the planetlabAuth must contain the correct sub-authority
////        String certusername = planetlabAuth.getNameForUrn().replaceAll(":", ".")+"."+username;
//
//        X509Certificate cert = createSelfSignedCertificate(planetlabAuth, username, privateKeyPem, keypair);
//        if (cert == null) return null;
//
//        GeniConnectionProvider connectionProvider = new GeniConnectionPool();
//        Logger logger = new Logger();
//
//        PlanetlabSfaRegistryInterface pleReg = new PlanetlabSfaRegistryInterface(logger, false);
//        PlanetlabSfaRegistryInterface.SimpleApiCallReply<GeniCredential> cred = null;
//        try {
//            //create user
//            SimpleGeniUser user = new SimpleGeniUser(planetlabAuth, geniUserUrn, cert, keypair.getPrivate());
//            cred = pleReg.getSelfCredential(connectionProvider.getConnectionByAuthority(
//                    user, planetlabAuth, PlanetlabSfaRegistryInterface.class),
//                    KeyUtil.x509certificateToPem(cert),
//                    geniUserUrn.toString(),
//                    "");
//        } catch (GeniException e) {
//            e.printStackTrace();
//            return null;
//        }
//        System.out.println("ple registry gave cred: " + cred.getValue());
//        return cred.getValue();
//    }

//    public static X509Certificate getSignedClientCertificate(SfaAuthority planetlabAuth, String username, String privateKeyPem, KeyPair keypair) {
//        assert privateKeyPem != null;
//        assert keypair != null;
//        GeniCredential cred = getCredential(planetlabAuth, username, privateKeyPem, keypair);
//
//        if (cred == null) return null;
//        String gid = cred.getTargetGid();
//
//        //gid can contain multiple certs. We just take the first.
//        X509Certificate cert = KeyUtil.pemToX509Certificate(gid);
//        return cert;
//    }

//    public static String createSignedClientCertificateAndPrivateKeyPem(SfaAuthority planetlabAuth, String username, String privateKeyPem, KeyPair keypair) {
//        assert privateKeyPem != null;
//        assert keypair != null;
//
//        X509Certificate cert = getSignedClientCertificate(planetlabAuth, username, privateKeyPem, keypair);
//        if (cert == null)
//            return null;
//
//        String certPem = KeyUtil.x509certificateToPem(cert);
//        return certPem+"\n"+privateKeyPem;
//    }
}
