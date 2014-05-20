package be.iminds.ilabt.jfed.util;

import javax.net.ssl.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SSLCertificateDownloader downloads an SSL certificate, given an URL.
 *
 * based on InstallCert.java
 */
public class SSLCertificateDownloader {
    /**
     * Certificate + additional derived info that might be useful
     * */
    public static class SSLCertificateJFedInfo {
        private X509Certificate cert;
        private X509Certificate[] certchain;
        private String urn;
        private String urnAuthPart;
        private String subject; //subject CN
        private Boolean selfSigned;
        private Boolean subjectMatchesHostname;
        private final URL url;
        private final String hostname;

        private boolean trusted;

        private SSLCertificateJFedInfo(X509Certificate cert, String urn, String urnAuthPart, String subject, boolean selfSigned, URL url, String hostname) {
            this.cert = cert;
            this.urn = urn;
            this.urnAuthPart = urnAuthPart;
            this.subject = subject;
            this.selfSigned = selfSigned;
            this.url = url;
            this.hostname = hostname;

            this.trusted = false;
        }
        private SSLCertificateJFedInfo(X509Certificate cert, URL url, String hostname) {
            this.cert = cert;
            this.urn = null;
            this.urnAuthPart = null;
            this.subject = null;
            this.selfSigned = null;
            this.subjectMatchesHostname = null;
            this.url = url;
            this.hostname = hostname;

            this.trusted = false;
        }
        private SSLCertificateJFedInfo(URL url, String hostname) {
            this.cert = null;
            this.urn = null;
            this.urnAuthPart = null;
            this.subject = null;
            this.selfSigned = null;
            this.subjectMatchesHostname = null;
            this.url = url;
            this.hostname = hostname;

            this.trusted = false;
        }

        public X509Certificate getCert() {
            return cert;
        }

        public String getUrn() {
            return urn;
        }

        public String getUrnAuthPart() {
            return urnAuthPart;
        }

        public String getSubject() {
            return subject;
        }

        public boolean isSelfSigned() {
            return selfSigned;
        }

        public Boolean getSubjectMatchesHostname() {
            return subjectMatchesHostname;
        }

        public X509Certificate[] getChain() {
            return certchain;
        }

        public URL getUrl() {
            return url;
        }

        public String getHostname() {
            return hostname;
        }

        @Override
        public String toString() {
            return "SSLCertificateJFedInfo{" +
                    "cert=" + (cert == null?"none":"present") +
                    ", certchain has " + (certchain == null ? null : certchain.length) + " elements" +
                    ", urn='" + urn + '\'' +
                    ", urnAuthPart='" + urnAuthPart + '\'' +
                    ", subject='" + subject + '\'' +
                    ", selfSigned=" + selfSigned +
                    ", subjectMatchesHostname=" + subjectMatchesHostname +
                    ", url=" + url +
                    ", hostname=" + hostname +
                    ", trusted=" + trusted +
                    '}';
        }

        public boolean isTrusted() {
            return trusted;
        }
    }


    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }



    static public X509Certificate getCertificate(URL url) {
        SSLCertificateJFedInfo info = getCertificateInfo(url);
        return info.getCert();
    }
    static public SSLCertificateJFedInfo getCertificateInfo(URL url) {
        try {
            boolean trusted = false;
            SSLCertificateJFedInfo res = new SSLCertificateJFedInfo(url, url.getHost());

            String host = url.getHost();
            int port = url.getPort();
            if (port == -1) port = url.getDefaultPort();
            if (port == -1) port = 443; //default https port

            KeyStore systemTrustStore = GeniTrustStoreHelper.getSystemTrustStore();
//            KeyStore systemTrustStore = GeniTrustStoreHelper.getFullTrustStore();

            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(systemTrustStore);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory factory = context.getSocketFactory();

            System.out.println("Opening connection to " + host + ":" + port + "...");
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            socket.setSoTimeout(10000);
            try {
                System.out.println("Starting SSL handshake...");
                socket.startHandshake();
                socket.close();
                System.out.println();
                System.out.println("No errors, certificate is already trusted");
                res.selfSigned = false;
                trusted = true;
            } catch (SSLException e) {
                //TODO remove output here
                System.out.println();
                e.printStackTrace(System.out);
                res.selfSigned = true;
                trusted = false;
            }

            X509Certificate[] chain = tm.chain;
            if (chain == null) {
                System.err.println("Could not obtain server certificate chain");
                return res; // res is all null
            }

            System.out.println();
            System.out.println("Server sent " + chain.length + " certificate(s):");
            System.out.println();
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            for (int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];
                System.out.println
                        (" " + (i + 1) + " Subject " + cert.getSubjectDN());
                System.out.println("   Issuer  " + cert.getIssuerDN());
                sha1.update(cert.getEncoded());
                System.out.println("   sha1    " + toHexString(sha1.digest()));
                md5.update(cert.getEncoded());
                System.out.println("   md5     " + toHexString(md5.digest()));
                System.out.println();

                try {
                    Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
                    if (altNames != null)
                        for (List<?> altName : altNames) {
                            Integer nameType = (Integer) altName.get(0);
                            if (nameType == 6) {
                                //uniformResourceIdentifier
                                String urn = (String) altName.get(1);
                                GeniUrn geniUrn = GeniUrn.parse(urn);
                                if (geniUrn == null)
                                    System.err.println("Warning: certificate alternative name URI is not a valid authority urn: \"" + urn + "\"  (will be ignored)");
                                else {
                                    res.urnAuthPart = geniUrn.getTopLevelAuthority();
                                    res.urn = urn;
    //                                userName = match.group(2);
    //                        System.out.println("DEBUG FOUND in cerificate: userUrn=\""+userUrn+"\"  userAuth=\""+userAuth+"\"  userName=\""+userName+"\"");
                                }
                            }
                        }
                } catch (CertificateParsingException e) {
                    //ignore
                }
            }

            res.cert = chain[0];
            res.certchain = chain;

            Pattern cnPattern = Pattern.compile(".*CN=([^ ,]*)[ ,]*.*");
//            Pattern emailAddressPattern = Pattern.compile("emailAddress=([^ ,]*)");
//            Pattern ouPattern = Pattern.compile("CN=([^ ,]*)");
            Matcher matchCN = cnPattern.matcher(res.cert.getSubjectX500Principal().toString());
//            Matcher matchEmailAddress = emailAddressPattern.matcher(res.cert.getSubjectX500Principal().toString());
//            Matcher matchOU = ouPattern.matcher(res.cert.getSubjectX500Principal().toString());
            res.subject = matchCN.matches() ? matchCN.group(1) : null;

//            System.out.println("SubjectX500Principle="+res.cert.getSubjectX500Principal().toString()+"   CN_present="+matchCN.matches()+"  CN="+res.subject);

            res.subjectMatchesHostname = res.subject != null && res.subject.equals(url.getHost());

            if (!res.subjectMatchesHostname)
                trusted = false;

            res.trusted = trusted;

            return res;
        } catch (Exception e) {
            System.err.println("Failed to fetch SSL certificate: "+e.getMessage());
            e.printStackTrace();
            return new SSLCertificateJFedInfo(url, url.getHost()); // res is all null
        }
    }

    public static void main(String [] args) throws MalformedURLException {
        SSLCertificateJFedInfo cert1 = SSLCertificateDownloader.getCertificateInfo(new URL("https://www.wall3.test.ibbt.be/"));
        System.out.println("returned cert: "+cert1);
//        SSLCertificateJFedInfo cert2 = SSLCertificateDownloader.getCertificateInfo(new URL("https://sfa.planet-lab.eu:12346"));
//        System.out.println("returned cert: "+cert2);
//        SSLCertificateJFedInfo cert3 = SSLCertificateDownloader.getCertificateInfo(new URL("http://sfav3.planet-lab.org:12346"));
//        System.out.println("returned cert: "+cert3);
//        SSLCertificateJFedInfo cert4 = SSLCertificateDownloader.getCertificateInfo(new URL("https://fiteagle.av.tu-berlin.de:12346"));
//        System.out.println("returned cert: "+cert4);
//        SSLCertificateJFedInfo cert5 = SSLCertificateDownloader.getCertificateInfo(new URL("https://fiteagle.av.tu-berlin.de:12347"));
//        System.out.println("returned cert: "+cert5);
    }
}
