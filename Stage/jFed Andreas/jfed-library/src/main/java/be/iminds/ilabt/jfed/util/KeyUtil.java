package be.iminds.ilabt.jfed.util;

import ch.ethz.ssh2.crypto.PEMDecoder;
//import ch.ethz.ssh2.signature.RSAPrivateKey;
import org.apache.commons.codec.binary.*;
import sun.security.x509.*;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.openssl.PEMReader;
//import org.bouncycastle.openssl.PasswordFinder;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.security.cert.*;
//import java.security.interfaces.RSAPrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * KeyUtil contains various methods to convert java keys and certificates to and from PEM format
 */
public class KeyUtil {
    public static class PEMDecodingException extends Exception {
        public PEMDecodingException() { }
        public PEMDecodingException(String message) { super(message); }
        public PEMDecodingException(String message, Throwable cause) { super(message, cause); }
        public PEMDecodingException(Throwable cause) { super(cause); }
        public PEMDecodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) { super(message, cause, enableSuppression, writableStackTrace); }
    }

    private KeyUtil() {}

//    private static boolean bcLoaded = false;
//    private static void assureBCloaded() {
//        if (bcLoaded) return;
//        Security.addProvider(new BouncyCastleProvider());
//        bcLoaded = true;
//    }

    /**
     * This reads an X509Certificate from a string containing the certificate in PEM format.
     * PEM format uses markers -----BEGIN CERTIFICATE----- and -----END CERTIFICATE----- to mark the certificate data.
     * the certificate data bytes are encoded in Base64. java.security.cert.CertificateFactory can read the decoded bytes
     * into a certificate.
     *
     * @param pem is a String containing a PEM certificate.
     *        It may contain the -----BEGIN CERTIFICATE----- and -----END CERTIFICATE----- markers, or it may contain only the data.
     *        If it cointains the markers, everything outside these markers is ignored, including any other type of markers.
     *        If there are multiple CERTIFICATE markers, only the certificate between the first is read.
     * @return the certificate as a java.security.cert.X509Certificate object
     * */
    public static X509Certificate pemToX509Certificate(String pem) /*throws PEMDecodingError*/ {
        String pemCert = pem.trim();
        String start = "-----BEGIN CERTIFICATE-----";
        String end = "-----END CERTIFICATE-----";
        int startPos = pemCert.indexOf(start);
        if (startPos != -1) {
            pemCert = pemCert.substring(startPos + start.length());
            int endPos = pemCert.indexOf(end);
            if (endPos < 0) return null; //throw new PEMDecodingError("Did not find end: '"+end+"' in: "+pem);
            pemCert = pemCert.substring(0, endPos);
        }
        byte[] decodedContent = Base64.decodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(pemCert));
        InputStream inStream = new ByteArrayInputStream(decodedContent);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)cf.generateCertificate(inStream);
            try { inStream.close(); } catch (IOException e) { }
            return certificate;
        } catch (CertificateException e) {
//            System.err.println("Note: failed to parse certificate: "+e.getMessage());
            //e.printStackTrace(System.out);
            return null;
        }
    }

    public static String x509certificateToPem(X509Certificate cert) {
//        throw new RuntimeException("Error in x509certificateToPem implementation");
        try {
            String s = Base64.encodeBase64String(cert.getEncoded());
            String begin = "-----BEGIN CERTIFICATE-----\n";
            String end = "-----END CERTIFICATE-----\n";
            s = TextUtil.wrap(s+"\n", 64);
            String res = begin + s + end;
            //assert pemToX509Certificate(res) != null;
            return res;
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This code comes from sun.security.tools.KeyTool
     * choose signature algorithm that is compatible with the private key algorithm argument (keyAlgName)
     */
    private static String getCompatibleSigAlgName(String keyAlgName) {
        if ("DSA".equalsIgnoreCase(keyAlgName)) {
            return "SHA1WithDSA";
        } else if ("RSA".equalsIgnoreCase(keyAlgName)) {
            return "SHA256WithRSA";
        } else if ("EC".equalsIgnoreCase(keyAlgName)) {
            return "SHA256withECDSA";
        } else {
            throw new RuntimeException("Cannot Derive Signature Algorithm");
        }
    }
    /*
     * This code comes from sun.security.tools.KeyTool and  sun.security.x509.CertAndKeyGen
     *
     * Note: since it uses a lot of sun.* classes, it is not stable
     * */
    public static X509Certificate makeSelfSigned(KeyPair keyPair, String dn, int validityDays) {
        assert keyPair != null;
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_YEAR, validityDays);

        try {
            PrivateKey privKey = keyPair.getPrivate();
            PublicKey pubKey = keyPair.getPublic();
            assert privKey != null;
            assert pubKey != null;

            // Determine the signature algorithm
            String sigAlgName = getCompatibleSigAlgName(privKey.getAlgorithm());




            Date firstDate = new Date(System.currentTimeMillis() - (24L*60*60*1000));
            Date lastDate = new Date(System.currentTimeMillis() + (validityDays*24L*60*60*1000));
            CertificateValidity interval = new CertificateValidity(firstDate, lastDate);
            X500Name owner = new X500Name("CN="+dn);


            X509CertInfo info = new X509CertInfo();
            // Add all mandatory attributes
            info.set(X509CertInfo.VERSION,
                    new CertificateVersion(CertificateVersion.V3));
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                    new java.util.Random().nextInt() & 0x7fffffff));
            AlgorithmId algID = AlgorithmId.getAlgorithmId(sigAlgName);
            info.set(X509CertInfo.ALGORITHM_ID,
                    new CertificateAlgorithmId(algID));
            info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
            info.set(X509CertInfo.KEY, new CertificateX509Key(pubKey));
            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));

            X509CertImpl cert = new X509CertImpl(info);
            cert.sign(privKey, sigAlgName);

            return cert;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

//    /**
//     * Searches and returns the first private key found in the pem string provided. Uses bouncycastle.
//    * */
//    public static PrivateKey getPrivateKey(String pem, final char[] password) throws GeniException {
//        assureBCloaded();
//
//        PasswordFinder passwordFinder = new PasswordFinder() { public char[] getPassword() { return password; } };
//        PEMReader pemkeycertReader = new PEMReader(new StringReader(pem), passwordFinder);
//        Object o = null;
//        do {
//            try {
//                o = pemkeycertReader.readObject();
//            } catch (IOException e) {
//                throw new GeniException("Error reading Private key ("+e.getMessage()+") pem:"+pem, e);
//            }
//            if (o instanceof PrivateKey)
//                return (PrivateKey) o;
//            if (o instanceof KeyPair) {
//                KeyPair keyPair = (KeyPair) o;
//                return keyPair.getPrivate();
//            }
//        } while (o != null);
//        return null;
//    }

    //getPrivateKey doesn't work :-/


    /**
     * This uses PKCS8EncodedKeySpec (java.security.spec) to read a PKCS#8 encoded private key from a -----BEGIN PRIVATE KEY----- PEM block
     *
     * Only the first -----BEGIN PRIVATE KEY----- to -----END PRIVATE KEY----- PEM block is used, all other pem blocks are ignored.
     * */

    public static boolean isPemPrivateKeyEncrypted(String pem) {
        if (pem.contains("Proc-Type")) return true;
        if (pem.contains("DEK-Info")) return true;
        return false;
    }

    /**
     * This reads a PrivateKey from a string containing the private key in PEM format.
     * PEM format uses markers -----BEGIN PRIVATE KEY----- and -----END PRIVATE KEY----- to mark the private key.
     * the key data bytes are encoded in Base64. The bytes are in PKCS#8 format. java.security.spec.PKCS8EncodedKeySpec
     * can read the decoded bytes into a PrivateKey.
     *
     * Note: pemToRsaPrivateKey reads data between -----BEGIN/END RSA PRIVATE KEY----- markers. This data is not so different.
     *       Between these markers, PKCS#1 is used to encode the RSA key.
     *       PKCS#8 has header info, where PKCS#1 does not. This header contains info about the key, and if it specifies
     *       that the key is RSA, it also includes the RSA key in PKCS#1.
     *
     * @param pem is a String containing a PEM private key.
     *        It must contain the -----BEGIN PRIVATE KEY----- and -----END PRIVATE KEY----- markers.
     *        Everything outside these markers is ignored, including any other type of markers.
     *        If there are multiple PRIVATE KEY markers, only the private key between the first is read.
     * @return the private key as a PrivateKey object
     * */
     public static PrivateKey pemToPrivateKey(String pem, final char[] password) throws PEMDecodingException {
        //see http://www.bitpapers.com/2012/04/using-asn1-in-java-some-real-examples.html on how to support encrypted keys them
         //but you are probably looking for "pemToRsaPrivateKey" which parses -----BEGIN RSA PRIVATE KEY----- and DOES support encrypted keys.
        assert !isPemPrivateKeyEncrypted(pem) : "This method does not support encrypted keys";
        assert password == null : "This method does not support encrypted keys";

        String pemKey = pem.trim();
        String start = "-----BEGIN PRIVATE KEY-----";
        String end = "-----END PRIVATE KEY-----";
        int startPos = pemKey.indexOf(start);
        if (startPos != -1) {
            pemKey = pemKey.substring(startPos + start.length());
            int endPos = pemKey.indexOf(end);
            if (endPos < 0) throw new PEMDecodingException("Did not find end: '"+end+"' in: "+pem);
            pemKey = pemKey.substring(0, endPos).trim();
            String fullPemKey = start+"\n"+pemKey+"\n"+end;

//            System.out.println("Found PEM key: "+pemKey);
//            System.out.println("Full PEM key: "+fullPemKey);

            try {
                //this uses PKCS8EncodedKeySpec (java.security.spec) to read a PKCS#8 encoded private key from a -----BEGIN PRIVATE KEY----- pem block
                byte [] pkcs8KeyBytes = Base64.decodeBase64(pemKey.replaceAll("\n", ""));
                PKCS8EncodedKeySpec pkcs8Spec = new PKCS8EncodedKeySpec(pkcs8KeyBytes);
                KeyFactory keyFact = KeyFactory.getInstance("RSA");
                PrivateKey key = keyFact.generatePrivate(pkcs8Spec);
                return key;
            } catch (Exception e) {
                throw new PEMDecodingException("Error reading PEM private key: "+e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * RSAPrivateCrtKey contains the public key, this method extracts it
     * */
    private static RSAPublicKey rsaPrivateCrtKeyToPublicKey(PrivateKey privateKey) {
        if (privateKey instanceof RSAPrivateCrtKey)
            return rsaPrivateCrtKeyToPublicKey((RSAPrivateCrtKey) privateKey);
        else
            return null;
    }
    /**
     * RSAPrivateCrtKey contains the public key, this method extracts it
     * */
    private static RSAPublicKey rsaPrivateCrtKeyToPublicKey(RSAPrivateCrtKey rsaPrivateKey) {
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PublicKey myPublicKey = keyFactory.generatePublic(publicKeySpec);
            return (RSAPublicKey) myPublicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * This reads an RsaPrivateKey from a string containing the private key in PEM format.
     * PEM format uses markers -----BEGIN RSA PRIVATE KEY----- and -----END RSA PRIVATE KEY----- to mark the private key.
     * the key data bytes are encoded in Base64. The bytes are in PKCS#1 format. This uses Ganymed SSH2 RSAPrivateKey to
     * read a PKCS#1 encoded RSAPrivateKey. That library supports encoded keys.
     *
     * Note: see pemToPrivateKey for info about the difference between PKCS#1 and PKCS#8
     *
     * @param pem is a String containing a PEM private key.
     *        It must contain the -----BEGIN RSA PRIVATE KEY----- and -----END RSA PRIVATE KEY----- markers.
     *        Everything outside these markers is ignored, including any other type of markers.
     *        If there are multiple RSA PRIVATE KEY markers, only the private key between the first is read.
     * @return the RSA private key as a java.security.interfaces.RSAPrivateKey object
     * */
    /**
     * This uses Ganymed SSH2 RSAPrivateKey to read a PKCS#1 encoded RSAPrivateKey from a -----BEGIN RSA PRIVATE KEY----- PEM block
     *
     * Only the first -----BEGIN RSA PRIVATE KEY----- to -----END RSA PRIVATE KEY----- PEM block is used, all other pem blocks are ignored.
     * */
    public static KeyPair pemToRsaKeyPair(String pem, final char[] password) throws PEMDecodingException {
        String pemKey = pem.trim();
        String start = "-----BEGIN RSA PRIVATE KEY-----";
        String end = "-----END RSA PRIVATE KEY-----";
        int startPos = pemKey.indexOf(start);
        if (startPos != -1) {
            pemKey = pemKey.substring(startPos + start.length());
            int endPos = pemKey.indexOf(end);
            if (endPos < 0) throw new PEMDecodingException("Did not find end: '"+end+"' in: "+pem);
            pemKey = pemKey.substring(0, endPos).trim();
            String fullPemKey = start+"\n"+pemKey+"\n"+end;

//            System.out.println("Found PEM key: "+pemKey);
//            System.out.println("Full PEM key: "+fullPemKey);

            try {
                String passString = null;
                if (password != null)
                    passString = new String(password);
                //This uses Ganymed SSH2 RSAPrivateKey to read a PKCS#1 encoded RSAPrivateKey from a -----BEGIN RSA PRIVATE KEY----- pem block
                Object res = PEMDecoder.decode(fullPemKey.toCharArray(), passString);
                if (!(res instanceof ch.ethz.ssh2.signature.RSAPrivateKey))
                    throw new PEMDecodingException("PEMDecoder did not return RSAPrivateKey but "+res.getClass().getName());
                ch.ethz.ssh2.signature.RSAPrivateKey rsaPrivateKey = (ch.ethz.ssh2.signature.RSAPrivateKey) res;
                ch.ethz.ssh2.signature.RSAPublicKey rsaPublicKey = rsaPrivateKey.getPublicKey();

                //convert from Ganymed SSH2 RSAPrivateKey to java.security.interfaces.RSAPrivateKey
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                KeySpec ks = new RSAPrivateKeySpec(rsaPrivateKey.getN()/*the modulus n*/, rsaPrivateKey.getD()/*the private exponent d*/);
                java.security.interfaces.RSAPrivateKey key = (java.security.interfaces.RSAPrivateKey) keyFactory.generatePrivate(ks);

                //convert from Ganymed SSH2 RSAPrivateKey to java.security.interfaces.RSAPublicKey
                KeySpec ks2 = new RSAPublicKeySpec(rsaPublicKey.getN()/*the modulus n*/, rsaPublicKey.getE()/*the public exponent e*/);
                java.security.interfaces.RSAPublicKey pubkey = (java.security.interfaces.RSAPublicKey) keyFactory.generatePublic(ks2);


                //TODO figure out how to get all values (theoretically I have them all)
//                /*
//                 * constructor arguments:
//                        BigInteger modulus,
//                        BigInteger publicExponent,
//                        BigInteger privateExponent,
//                        BigInteger primeP,
//                        BigInteger primeQ,
//                        BigInteger primeExponentP,
//                        BigInteger primeExponentQ,
//                        BigInteger crtCoefficient
//                 */
//                RSAPrivateCrtKeySpec ks2 = new RSAPrivateCrtKeySpec(
//                        rsaPrivateKey.getN()/*the modulus n*/,
//                        rsaPrivateKey.getE(),
//                        rsaPrivateKey.getD()/*the private exponent d*/,
//                        rsaPublicKey.
//                        );



                return new KeyPair(pubkey, key);
            } catch (Exception e) {
                throw new PEMDecodingException("Error reading PEM private key: "+e.getMessage(), e);
            }
        }
        return null;
    }
    /**
     * This uses Ganymed SSH2 RSAPrivateKey to read a PKCS#1 encoded RSAPrivateKey from a -----BEGIN RSA PRIVATE KEY----- PEM block
     *
     * Only the first -----BEGIN RSA PRIVATE KEY----- to -----END RSA PRIVATE KEY----- PEM block is used, all other pem blocks are ignored.
     * */
    public static java.security.interfaces.RSAPrivateKey pemToRsaPrivateKey(String pem, final char[] password) throws PEMDecodingException {
        KeyPair res = pemToRsaKeyPair(pem, password);
        if (res == null) return null;
        return (RSAPrivateKey) res.getPrivate();
    }

    /**
     * This find either a PRIVATE KEY or a RSA PRIVATE KEY in a PEM file.
     * The RSA PRIVATE KEY will be searched first, and returned if it exists.
     * If there is no RSA PRIVATE KEY, a PRIVATE KEY will be searched and returned if it exists.
     * null will be returned if neither can be found.
     *
     * @see KeyUtil#pemToRsaPrivateKey
     * @see KeyUtil#pemToPrivateKey
     * */
    public static PrivateKey pemToAnyPrivateKey(String pem, final char[] password)  throws PEMDecodingException {
        PrivateKey pk1 = pemToRsaPrivateKey(pem, password);
        if (pk1 != null) return pk1;
        PrivateKey pk2 = pemToPrivateKey(pem, password);
        if (pk2 != null) return pk2;
        return null;
    }


    /**
     * Parses all the PEM certificates that can be found in the String and returns them.
     *
     * Certificates are be found between:
     * -----BEGIN CERTIFICATE-----
     * and
     * -----END CERTIFICATE-----
     *
     * @see KeyUtil#pemToX509Certificate
     *
     * @param pemCerts String containing PEM certificates
     * @return a list with all certificates found
     */
    public static List<Certificate> parseAllPEMCertificates(String pemCerts) {
        List<Certificate> res = new ArrayList<Certificate>();

        String begin = "-----BEGIN CERTIFICATE-----";
        String end = "-----END CERTIFICATE-----";

        int startIndex = pemCerts.indexOf(begin);
        while (startIndex != -1) {
            int endIndex = pemCerts.indexOf(end, startIndex+1);
            if (endIndex == -1) break;
            String certString = pemCerts.substring(startIndex + begin.length(), endIndex).trim();
//            System.out.println("Found cert: "+certString+"\n\n");
            Certificate cert = pemToX509Certificate(certString);
            res.add(cert);
            startIndex = pemCerts.indexOf(begin, endIndex + end.length());
        }

        return res;
    }




      /** helper for DER encoding. DER Length octets for a certain length */
      static private List<Byte> derLength(int len) {
        assert len >= 0;
        //dirty and quick implementation!
        List<Byte> res = new ArrayList<Byte>();
        if (len >= 0 && len <= 127) {
            res.add((byte)len);
//            System.out.println("DER len " + len + " in 1 octet");
            return res;
        }
        BigInteger helper = BigInteger.valueOf(len);
        byte[] helperBytes = helper.toByteArray();
        assert helper.toByteArray()[0] >= 0;

        int octets_needed = helperBytes.length;//helper.bitLength()+2 / 8;
        if (octets_needed < 2) octets_needed = 2;

//        System.out.println("DER len "+len+" fits in 1 + "+octets_needed+" octets");

        assert octets_needed < 128 && octets_needed >= 0;
        byte first = (byte)(0x80 | octets_needed);
        int firstInt = 0x80 | octets_needed; //for printing only
        res.add(first);

        assert octets_needed == helperBytes.length;

        for (byte b : helperBytes)
            res.add(b);

//        System.out.println("DER len "+len+" encoded in total "+res.size()+" octets. first="+firstInt);

        return res;
    }

    /** helper for DER encoding. Encodes a java BigInt as list of DER bytes. */
    static private List<Byte> derBigInt(BigInteger bigint) {
        List<Byte> res = new ArrayList<Byte>();

        if (bigint.signum() == 0) {
            res.add((byte)0);
            return res;
        }

        byte[] bytes = bigint.toByteArray();
        assert bytes.length > 0;
        boolean skipFirstZeroes = true;
        for (byte b : bytes) {
            if (b != 0 && skipFirstZeroes && (b > 127 || b < 0) && bigint.signum() > 0) {
                //Note: Bigint probably does this, but we make sure here
                //first is negative but number is positive, so add 00 padding to prevent negative integer
                res.add((byte) 0x00);
//                System.out.println("DER Bigint 00 padded");
            }
            if (b != 0 || !skipFirstZeroes) {
                skipFirstZeroes = false;
                res.add(b);
            }
//            else
//                System.out.println("DER Bigint skipped a zero");
        }

//        System.out.println("DER Bigint to "+res.size()+" bytes");

        return res;
    }

    /** helper for DER encoding. Encodes a list of java BigInt as list of DER bytes. */
    static public List<Byte> derSequenceBigInt(List<BigInteger> bigints) {
        List<Byte> integers = new ArrayList<Byte>();
        for (BigInteger bigint : bigints) {
            integers.add((byte) 0x02); //ANS Integer (public exponent)
            List<Byte> e = derBigInt(bigint);
            integers.addAll(derLength(e.size())); //ANS Integer Size
            integers.addAll(e); //Integer itself
        }

        List<Byte> der = new ArrayList<Byte>();
        der.add((byte) 0x30); //ANS Sequence
        der.addAll(derLength(integers.size())); //ANS Sequence size
        der.addAll(integers);

        return der;
    }

    /**
     * encodes a java RSAPrivateKey object using PKCS#1 standard
     * @param sshPrivateKey the RSAPrivateKey to encode
     * @return an array of bytes containing the PKCS#1 encoded key
     */
    public static byte[] getPrivateKeyPKCS1(RSAPrivateKey sshPrivateKey) {
       /*
                In the case of an RSA private key, PKCS #1 defines a structure called RSAPrivateKey, which looks as follows:

                RSAPrivateKey ::= SEQUENCE {
                                    version Version,
                                    modulus INTEGER,
                                    publicExponent INTEGER,
                                    privateExponent INTEGER,
                                    prime1 INTEGER,
                                    prime2 INTEGER,
                                    exponent1 INTEGER,
                                    exponent2 INTEGER,
                                    coefficient INTEGER,
                                    otherPrimeInfos OtherPrimeInfos OPTIONAL }
                Version ::= INTEGER { two-prime(0), multi(1) }
                (CONSTRAINED BY {-- version must be multi if otherPrimeInfos present --})

                OtherPrimeInfos ::= SEQUENCE SIZE(1..MAX) OF OtherPrimeInfo

                OtherPrimeInfo ::= SEQUENCE {
                                    prime INTEGER,
                                    exponent INTEGER,
                                    coefficient INTEGER }
       * */

        List<BigInteger> bigints = new ArrayList<BigInteger>();
        RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey) sshPrivateKey;
        bigints.add(BigInteger.valueOf(0));
        bigints.add(sshPrivateKey.getModulus());
        bigints.add(crtKey.getPublicExponent());
        bigints.add(sshPrivateKey.getPrivateExponent());
        bigints.add(crtKey.getPrimeP());
        bigints.add(crtKey.getPrimeQ());
        bigints.add(crtKey.getPrimeExponentP());
        bigints.add(crtKey.getPrimeExponentQ());
        bigints.add(crtKey.getCrtCoefficient());


        List<Byte> der = derSequenceBigInt(bigints);

        byte derBytes[] = new byte[der.size()];
        int i = 0;
//        String s = "";
        for (byte b : der) {
//            s += String.format("%02X ", b);
            derBytes[i++] = b;
        }
//        System.out.println(StringUtils.wrap(s, 70));

        return derBytes;
    }

    /**
        * encodes a java RSAPrivateKey object using PKCS#1 standard
        * @param sshPublicKey the RSAPrivateKey to encode
        * @return an array of bytes containing the PKCS#1 encoded key
        */
       public static byte[] getPublicKeyPKCS1(RSAPublicKey sshPublicKey) {
          /*
         An RSA public key should be represented with the ASN.1 type
            RSAPublicKey:

               RSAPublicKey ::= SEQUENCE {
                   modulus           INTEGER,  -- n
                   publicExponent    INTEGER   -- e
               }

            The fields of type RSAPublicKey have the following meanings:

             * modulus is the RSA modulus n.

             * publicExponent is the RSA public exponent e.
          * */

           List<BigInteger> bigints = new ArrayList<BigInteger>();
           bigints.add(sshPublicKey.getModulus());
           bigints.add(sshPublicKey.getPublicExponent());
           List<Byte> der = derSequenceBigInt(bigints);

           byte derBytes[] = new byte[der.size()];
           int i = 0;
           for (byte b : der) {
               derBytes[i++] = b;
           }

           return derBytes;
       }

    /**
     * encodes a java RSAPrivateKey object using PKCS#1 standard. Then encodes the bytes using Base64.
     * @param sshPrivateKey the RSAPrivateKey to encode
     * @return an array of bytes containing the PKCS#1 encoded key, encoded using Base64
     */
    public static char[] getPrivateKeyCharsPKCS1Base64(RSAPrivateKey sshPrivateKey) {
        String s = Base64.encodeBase64String(getPrivateKeyPKCS1(sshPrivateKey));
        return s.toCharArray();
    }
    /**
     * encodes a java RSAPublicKey object using PKCS#1 standard. Then encodes the bytes using Base64.
     * @param sshPublicKey the RSAPrivateKey to encode
     * @return an array of bytes containing the PKCS#1 encoded key, encoded using Base64
     */
    public static char[] getPublicKeyCharsPKCS1Base64(RSAPublicKey sshPublicKey) {
        String s = Base64.encodeBase64String(getPublicKeyPKCS1(sshPublicKey));
        return s.toCharArray();
    }

    /**
     * encodes a java PrivateKey object using PKCS#8 standard.
     * The java PrivateKey class normally actually supports this conversion. This checks that and does the conversion.
     *
     * @param privateKey the PrivateKey to encode
     * @return an array of bytes containing the PKCS#8 encoded key
     */
    public static byte[] getPrivateKeyCharsPKCS8(PrivateKey privateKey) {
        if (!privateKey.getFormat().equals("PKCS#8"))
            throw new RuntimeException(privateKey.getClass().getName()+" does not support PKCS#8");
        assert privateKey.getFormat().equals("PKCS#8");
        return privateKey.getEncoded();
    }


    /**
     * encodes a java PrivateKey object using PKCS#8 standard. Then encodes the bytes using Base64.
     *
     * @param privateKey the PrivateKey to encode
     * @return an array of bytes containing the PKCS#8 encoded key, encoded using Base64
     */
    public static char[] getPrivateKeyCharsPKCS8Base64(PrivateKey privateKey) {
        String s = Base64.encodeBase64String(getPrivateKeyCharsPKCS8(privateKey));
        return s.toCharArray();
    }
    /**
     * Convert an PrivateKey into a PEM "PRIVATE KEY". (PKCS#8)
     *
     * @param privateKey PrivateKey to encode into PEM format
     * @return an array of chars containing the private key in PEM format
     * */
    public static char[] privateKeyToPem(PrivateKey privateKey) {
        String pem = TextUtil.wrap(new String(getPrivateKeyCharsPKCS8Base64(privateKey)), 65); //PKCS#8 encoded
        String res = "-----BEGIN PRIVATE KEY-----\n" + pem + "\n-----END PRIVATE KEY-----\n"; //RSA does not need to be mentioned, since "RSA" algo is in PKCS#8 header (which also contains PKCS#1 key)
        return res.toCharArray();
    }

    /**
     * Convert an RsaPrivateKey into a PEM "RSA PRIVATE KEY". (PKCS#1)
     *
     * @param rsaPrivateKey RsaPrivateKey to encode into PEM format
     * @return an array of chars containing the RSA private key in PEM format
     * */
    public static char[] rsaPrivateKeyToPem(RSAPrivateKey rsaPrivateKey) {
        String pem = TextUtil.wrap(new String(getPrivateKeyCharsPKCS1Base64(rsaPrivateKey)), 65); //PKCS#1 encoded
        String res = "-----BEGIN RSA PRIVATE KEY-----\n" + pem + "\n-----END RSA PRIVATE KEY-----\n"; //this only works if the key is in PKCS#1 format
        return res.toCharArray();
    }



    public static boolean hasRsaPrivateKey(String keyCertContent) {
        return keyCertContent.contains("-----BEGIN RSA PRIVATE KEY-----");
    }
    public static boolean hasEncryptedRsaPrivateKey(String keyCertContent) {
//        return keyCertContent.contains("-----BEGIN RSA PRIVATE KEY-----") && keyCertContent.contains("Proc-Type: 4,ENCRYPTED");
        return keyCertContent.contains("-----BEGIN RSA PRIVATE KEY-----") && (
                keyCertContent.contains("Proc-Type:") || keyCertContent.contains("DEK-Info: ") || keyCertContent.contains("ENCRYPTED")
                );
    }





    /**
     * Converts an RsaPublicKey to the format of the OpenSSH "authorized_keys" file.
     *
     * Note: This format is not a standard (not PEM, PKCS#1, ...)
     *
     * Example public key in this format (... used to shorten example):
     *   ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAB...3DxZtLq2nuf4n name@example.com
     *
     *   @see KeyUtil#openSshAuthorizedKeysFormatRsaPublicKey(String) for the reverse
     * */
    public static String rsaPublicKeyToOpenSshAuthorizedKeysFormat(RSAPublicKey rsaPublicKey) {
        /*
         * See: http://blog.oddbit.com/post/converting-openssh-public-keys
         * For an ssh-rsa key, the PEM-encoded data is a series of (length, data) pairs. The length is encoded as four octets (in big-endian order). The values encoded are:
         *      - algorithm name (one of (ssh-rsa, ssh-dsa)). This duplicates the key type in the first field of the public key.
         *      - RSA exponent
         *      - RSA modulus
         */
        byte[] keytype = StringUtils.getBytesUtf8("ssh-rsa");
        byte[] exp = rsaPublicKey.getPublicExponent().toByteArray();
        assert new BigInteger(exp).equals(rsaPublicKey.getPublicExponent());
        byte[] mod = rsaPublicKey.getModulus().toByteArray();
        byte[] all = ByteBuffer.allocate(exp.length + mod.length + keytype.length + 12).order(ByteOrder.BIG_ENDIAN).
                putInt(keytype.length).put(keytype).
                putInt(exp.length).put(exp).
                putInt(mod.length).put(mod).
                array();
        String encoded_all =  Base64.encodeBase64String(all);

        String res = "ssh-rsa " + encoded_all;

        //self check
        assert openSshAuthorizedKeysFormatRsaPublicKey(res).getModulus().equals(rsaPublicKey.getModulus());
        assert openSshAuthorizedKeysFormatRsaPublicKey(res).getPublicExponent().equals(rsaPublicKey.getPublicExponent());

        return res;
    }

    /**
     * Converts a string containing an RSA public key in the format of the OpenSSH "authorized_keys" file to a
     * RsaPublicKey object
     *
     * Note: This format is not a standard (not PEM, PKCS#1, ...)
     *
     * Example public key in this format (... used to shorten example):
     *   ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAB...3DxZtLq2nuf4n name@example.com
     *
     *   @see KeyUtil#rsaPublicKeyToOpenSshAuthorizedKeysFormat(RSAPublicKey) for the reverse
     * */
    public static RSAPublicKey openSshAuthorizedKeysFormatRsaPublicKey(String ssh_rsa) {
        //full format (space seperated fields, check also "man sshd" under "authorized_keys"): options, keytype, base64-encoded key, comment
        //with keytype one of ssh-rsa and ssh-dsa
        String[] parts = ssh_rsa.trim().split(" ");
        String encodedKey = null;
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals("ssh-rsa")) {
                encodedKey = parts[i+1];
                break;
            }
            if (parts[i].equals("ssh-dsa"))
                throw new RuntimeException("DSA key found in OpenSSH authorized_keys format. DSA keys are not supported by this method, only RSA is supported. input=\""+ssh_rsa+"\"");
        }
        if (encodedKey == null) {
            throw new RuntimeException("No key found in OpenSSH authorized_keys format. format syntax: <options> <keytype> <base64-encoded key> <comment>. input=\""+ssh_rsa+"\"");
        }
        
        byte[] all_restored = Base64.decodeBase64(encodedKey);
        ByteBuffer bb = ByteBuffer.wrap(all_restored).order(ByteOrder.BIG_ENDIAN);

        int kts = bb.getInt();
        byte[] kt = new byte[kts];
        bb.get(kt);
        String keyType = StringUtils.newStringUtf8(kt);
        assert keyType.equals("ssh-rsa");

        int es = bb.getInt();
        byte[] exp = new byte[es];
        bb.get(exp);

        int ms = bb.getInt();
        byte[] mod = new byte[ms];
        bb.get(mod);

        BigInteger exponent = new BigInteger(exp);
        BigInteger modulus = new BigInteger(mod);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        RSAPublicKey res = null;
        try {
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            PublicKey key = keyFact.generatePublic(spec);
            res = (RSAPublicKey) key;
        } catch (Exception e) {
            throw new RuntimeException("Error creating RSAPublicKey: "+e.getMessage(), e);
        }
        return res;
    }
}
