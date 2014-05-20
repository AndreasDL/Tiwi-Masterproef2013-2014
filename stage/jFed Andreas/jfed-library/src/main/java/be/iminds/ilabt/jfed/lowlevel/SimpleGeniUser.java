package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.KeyUtil;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * SimpleGeniUser
 */
public class SimpleGeniUser implements GeniUser {
    private X509Certificate certificate;
    private PrivateKey privateKey;

    private SfaAuthority authority;
    private GeniUrn userUrn;

//    public SimpleGeniUser(SfaAuthority userAuthority, String username, String keyCertContent, char[] keyPass) {
//        this(userAuthority, "urn:publicid:IDN+"+userAuthority.getNameForUrn()+"+user+"+username, keyCertContent, keyPass);
//    }
    public SimpleGeniUser(SfaAuthority userAuthority, GeniUrn userUrn, String keyCertContent, char[] keyPass) {
        this.authority = userAuthority;
        this.userUrn = userUrn;

        try {
            privateKey = KeyUtil.pemToRsaPrivateKey(keyCertContent, keyPass);
        } catch (KeyUtil.PEMDecodingException e) {
            privateKey = null;
            throw new RuntimeException("ERROR reading PEM key:"+keyCertContent+" -> "+e.getMessage(), e);
        }
        if (privateKey == null)
            throw new RuntimeException("ERROR: PEM key and certificate does not contain a key:"+keyCertContent);

        certificate = KeyUtil.pemToX509Certificate(keyCertContent);
        if (certificate == null)
            throw new RuntimeException("ERROR: PEM key and certificate does not contain a X509 certificate:"+keyCertContent);
    }
    public SimpleGeniUser(SfaAuthority userAuthority, GeniUrn userUrn, X509Certificate certificate, PrivateKey privateKey) {
        this.authority = userAuthority;
        this.userUrn = userUrn;
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    /**
     * make a copy of a GeniUser
     * */
    public SimpleGeniUser(GeniUser geniUser) {
        this.certificate = geniUser.getCertificate();
        this.privateKey = geniUser.getPrivateKey();
        this.authority = geniUser.getUserAuthority();
//        this.userName = geniUser.getUserName();
        this.userUrn = GeniUrn.parse(geniUser.getUserUrn());
    }


    @Override
    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public SfaAuthority getUserAuthority() {
        return authority;
    }

//    @Override
//    public String getUserName() {
//        return userName;
//    }

    @Override
    public String getUserUrn() {
        return userUrn.toString();
    }
}
