package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.util.DataConversionUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Gid a Geni GID is an X.509 certificate
 */
public class Gid {
    private String encodedContent;
    private byte[] decodedContent;
    private X509Certificate certificate;

    /** copy constructor */
    public Gid(Gid o) {
        this(o.getEncodedContent());
    }

    public Gid(String content) {
        this.encodedContent = content;
        this.decodedContent = DataConversionUtils.decodeBase64(content);
        InputStream inStream = new ByteArrayInputStream(decodedContent);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            this.certificate = (X509Certificate)cf.generateCertificate(inStream);
            inStream.close();
        } catch (Exception e) {
            System.err.println("Error reading X509Certificate: " + e.getMessage());
//            e.printStackTrace();
            this.certificate = null;
        }
    }

    public String getEncodedContent() {
        return encodedContent;
    }

    public byte[] getDecodedContent() {
        return decodedContent;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public boolean hasError() {
        return certificate == null;
    }

    public String toString() {
        if (certificate != null)
            return "X.509 certificate for "+certificate.getSubjectDN().toString()+"";
        else
            return "Certificate with read error: "+encodedContent.substring(0, 20)+"...";
    }
}
