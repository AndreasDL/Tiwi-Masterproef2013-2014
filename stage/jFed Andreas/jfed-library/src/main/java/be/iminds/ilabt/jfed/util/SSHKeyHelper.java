package be.iminds.ilabt.jfed.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * SSHKeyHelper generates an SSH public and private key.
 * It has getters to access them as java object, PEM encoded string, or OpenSSH public key string
 */
public class SSHKeyHelper {
    private RSAPrivateKey sshPrivateKey;
    private KeyPair keyPair;
    private RSAPublicKey sshPublicKey;
    private String sshPublicKeyString;

    public SSHKeyHelper() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        // or: generator = KeyPairGenerator.getInstance("DSA");
        generator.initialize(2048);
        keyPair = generator.genKeyPair();
        sshPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        sshPublicKey = (RSAPublicKey) keyPair.getPublic();
        sshPublicKeyString = KeyUtil.rsaPublicKeyToOpenSshAuthorizedKeysFormat(sshPublicKey);
    }


    public RSAPrivateKey getSshPrivateKey() {
        return sshPrivateKey;
    }

    public char[] getPEMPrivateKey() {
        return KeyUtil.privateKeyToPem(sshPrivateKey);
    }
    public char[] getPEMRsaPrivateKey() {
        return KeyUtil.rsaPrivateKeyToPem(sshPrivateKey);
    }

    public RSAPublicKey getSshPublicKey() {
        return sshPublicKey;
    }

    /** The key in the OpenSSH string format. */
    public String getSshPublicKeyString() {
        return sshPublicKeyString;
    }


    public static void main(String [] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        SSHKeyHelper test = new SSHKeyHelper();
        System.out.println("Private key algorithm: \"" + test.getSshPrivateKey().getAlgorithm() + "\"");
        System.out.println("Private key encoding: \"" + test.getSshPrivateKey().getFormat() + "\"");
        System.out.println("public key: " + test.getSshPublicKeyString());
        System.out.println("private key:\n" + new String(test.getPEMPrivateKey()));


        byte[] der_pkcs1 = KeyUtil.getPrivateKeyPKCS1(test.getSshPrivateKey());
        FileOutputStream fo = new FileOutputStream("/home/wim/tmp.removeme.private_rsa_key");
        fo.write(der_pkcs1);
        fo.close();
        System.out.println("wrote der_pkcs1 to /home/wim/dummykey8");

        String der_pkcs1_pem = new String(test.getPEMRsaPrivateKey());
        PrintWriter pw1 = new PrintWriter(new FileWriter("/home/wim/tmp.removeme.private_rsa_key.pem"));
        pw1.println(der_pkcs1_pem);
        pw1.close();


        PrivateKey pk = null;
        try {
            pk = KeyUtil.pemToAnyPrivateKey(new String(test.getPEMRsaPrivateKey()), null);
        } catch (KeyUtil.PEMDecodingException e) {
            pk = null;
        }
        assert pk != null;
        System.out.println("Private key to PEM and back passed part 1/2.");

        boolean equalKeys = true; //pk.equals(test.getSshPrivateKey())
        if (! ((RSAPrivateKey)pk).getModulus().equals(test.getSshPrivateKey().getModulus())) equalKeys = false;
        if (! ((RSAPrivateKey)pk).getPrivateExponent().equals(test.getSshPrivateKey().getPrivateExponent())) equalKeys = false;
        if (equalKeys)
            System.out.println("private keys equal!");
        else
            System.out.println("ERROR private keys differ: "+pk+"\n != \n"+test.getSshPrivateKey());
        System.out.println("Private key to PEM and back passed part 2/2.");


        if (true)
            return;

        //something else: connection test

        System.out.println("Testing SSH connection to hestia01.test");

        Connection conn = new Connection("hestia01.test", 22);
        conn.connect();
        boolean isAuthenticated = conn.authenticateWithPublicKey("root", new File("/home/wim/.ssh/id_dsa"), "nopass");
        System.out.println(" SSH con isAuthenticated="+isAuthenticated);
        System.out.println(" SSH con isAuthenticationComplete="+conn.isAuthenticationComplete());
        final Session session = conn.openSession();
        System.out.println(" Session opened");

        BufferedReader sout = new BufferedReader(new InputStreamReader(session.getStdout()));
        BufferedReader serr = new BufferedReader(new InputStreamReader(session.getStderr()));
        final PrintWriter pw = new PrintWriter(session.getStdin());


//        session.execCommand("/usr/bin/who");
        session.execCommand("ls /");
        System.out.println(" command sent");

        String result = "";
        String err = "";
        Thread.sleep(1000);
        String line = sout.readLine();
        while (line != null) {
            result += line + "\n";
            line = sout.readLine();
        }
        System.out.println(" stdout read");
        line = serr.readLine();
        while (line != null) {
            err += line + "\n";
            line = serr.readLine();
        }
        System.out.println(" stderr read");
        sout.close();
        serr.close();
        pw.close();
        System.out.println("Info: \"who\" command on result: \"" + result.trim() + "\" error=\"" + err.trim() + "\"");
        session.close();
        conn.close();
        System.out.println("SSH connection Test completed");
    }
}
