package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * GeniUser
 */
public interface GeniUser {
    PrivateKey getPrivateKey();
    X509Certificate getCertificate();

    SfaAuthority getUserAuthority();

//    String getUserName();
    String getUserUrn();
}
