package be.iminds.ilabt.jfed.lowlevel.userloginmodel;

import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * UserLoaderImpl
 */
public interface UserLoginModel extends GeniUser, GeniUserProvider {
    //GeniUser:
//    public PrivateKey getPrivateKey();
//    public X509Certificate getCertificate();
//    public SfaAuthority getUserAuthority();
//    public String getUserUrn();
//
    //GeniUserProvider:
//    public GeniUser getLoggedInGeniUser();
//    public boolean isUserLoggedIn();

    public void save(UserLoginModelManager.UserLoginModelPreferences preferences);
    public void load(UserLoginModelManager.UserLoginModelPreferences preferences);
    public void reset();
    public void defaults();
}
