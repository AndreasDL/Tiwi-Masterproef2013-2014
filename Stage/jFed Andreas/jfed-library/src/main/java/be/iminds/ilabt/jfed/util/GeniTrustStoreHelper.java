package be.iminds.ilabt.jfed.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * GeniTrustStoreHelper a class which stores trusted SSL root certificates, and trusted SSL self-singed certificates
 *
 * {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} uses this when initialising connections.
 *
 */
public class GeniTrustStoreHelper {
    private GeniTrustStoreHelper() {}

    /**
     * Note: there is no good way to find the loaded system Trust Store, so we have to search for the Trust Store file.
     *       There are default locations for that, and these can be overwritten by system properties.
     *
     *       TODO: If we cannot file a file, we fall back to an internal trust store.
     *
     * @returns the a KeyStore containing all certificates in the java system Trust Store.
     * */
    public static KeyStore getSystemTrustStore() {
        final String jssecacertsFilename = System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "jssecacerts";
        final String cacertsFilename = System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";

        //default can be overriden by setting property javax.net.ssl.trustStore
        String systemTrustStoreFilename = System.getProperty("javax.net.ssl.trustStore");
        if (systemTrustStoreFilename == null) {
            //if property not set, system default is used. If jssecacerts does no exist, cacerts should be used.
            systemTrustStoreFilename = jssecacertsFilename;
            if (! new File(systemTrustStoreFilename).exists())
                systemTrustStoreFilename = cacertsFilename;
            if (! new File(systemTrustStoreFilename).exists()) {
                //TODO fall back to an internal trust store, and warn about that
                throw new RuntimeException("Could not find any system trust store!");
            }
        }

        KeyStore systemTrustStore = null;
        //default truststore password can be overriden by setting property javax.net.ssl.trustStorePassword
        String systemTrustStorePass = System.getProperty("javax.net.ssl.trustStorePassword");
        if (systemTrustStorePass == null)
            systemTrustStorePass = "changeit"; //default password for system truststore
        try {
            systemTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream system_trustStore_in = new FileInputStream ( systemTrustStoreFilename );
            systemTrustStore.load(system_trustStore_in, systemTrustStorePass.toCharArray());
            system_trustStore_in.close();
//            System.out.println("System trust store loaded. Contains "+systemTrustStore.size()+" certificates.");
        } catch (Exception e) {
            System.err.println("ERROR loading system trust store: "+e.getMessage());
            System.err.println("  Normally, the trust store is at one of these locations:\n"+
                    "   - <JAVA_HOME>/lib/security/jssecacerts => \""+jssecacertsFilename+"\"\n"+
                    "   - <JAVA_HOME>/lib/security/cacerts => \""+cacertsFilename+"\"\n");
            System.err.println("  You can use another by setting the system property \"javax.net.ssl.trustStore\"");
            System.err.println("  You can specify a non default password with \"javax.net.ssl.trustStorePassword\" (default pass is \"changeit\")");
            systemTrustStore = null;
            throw new RuntimeException("Cannot locate and load system trust store: "+e.getMessage(), e);
        }

        return systemTrustStore;
    }


    private static List<Certificate> extraTrustedCertificates = new ArrayList<Certificate>();
    public static void addTrustedCertificate(Certificate c) {
        //TODO: do nat add if already added
        extraTrustedCertificates.add(c);
        //force remake of getFullTrustStore() result
        fullKeyStore = null;
    }
    public static void addTrustedPemCertificate(String pem) {
//            PEMReader pem_wall3_CA_certReader = new PEMReader(new StringReader(pem_wall3_CA_cert));
//            Certificate wall3_CA_cert = (Certificate) pem_wall3_CA_certReader.readObject();
        addTrustedCertificate(KeyUtil.pemToX509Certificate(pem));
    }
    public static List<String> addedPems = new ArrayList<String>();
    public static void addTrustedPemCertificateIfNotAdded(String pem) {
        if (addedPems.contains(pem)) return;
        addedPems.add(pem);
        addTrustedPemCertificate(pem);
    }

    private static KeyStore fullKeyStore = null;
    public static KeyStore getFullTrustStore() {
        if (fullKeyStore != null) return fullKeyStore;

        String trust_store_pass = "somepass"; //we never save the trust store, so this doesn't matter

        try {
            fullKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            fullKeyStore.load( null , trust_store_pass.toCharArray());

            int count = 0;
            for (Certificate extraCert : extraTrustedCertificates)
                fullKeyStore.setCertificateEntry("extraCert"+(count++), extraCert);

            KeyStore systemTrustStore = getSystemTrustStore();
            //add systemTrustStore to our custom trust store
            if (systemTrustStore != null) {
                for (Enumeration<String> e = systemTrustStore.aliases(); e.hasMoreElements();) {
                    final String alias = e.nextElement();
                    final KeyStore.Entry entry = systemTrustStore.getEntry(alias, null);
                    KeyStore.TrustedCertificateEntry tcEntry = (KeyStore.TrustedCertificateEntry) entry;
                    fullKeyStore.setCertificateEntry(alias, tcEntry.getTrustedCertificate());
                }
//                System.out.println("Added certificates from system truststore to our custom truststore.");
            }
        } catch (Exception e) {
            fullKeyStore = null;
            throw new RuntimeException("Could not create full truststore: "+e, e);
        }

        return fullKeyStore;
    }
}
