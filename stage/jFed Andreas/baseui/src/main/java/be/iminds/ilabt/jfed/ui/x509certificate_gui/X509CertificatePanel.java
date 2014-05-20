package be.iminds.ilabt.jfed.ui.x509certificate_gui;

import be.iminds.ilabt.jfed.util.KeyUtil;
import thinlet.FrameLauncher;
import thinlet.Thinlet;

import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * X509CertificatePanel
 */
public class X509CertificatePanel {
    private Thinlet thinlet;

    private String certificatePEMString;
    private X509Certificate certificate;
    private boolean showPemCertificate;

    private Object panel;
    private Object pemLabel;
    private Object pem;
    private Object errorLabel;
    private Object subject;
    private Object subjectAlt;
    private Object subjectAltLabel;
    private Object issuer;
    private Object issuerAlt;
    private Object issuerAltLabel;
    private Object validFrom;
    private Object validTo;

    public X509CertificatePanel(Thinlet thinlet, boolean showPemCertificate) {
        this.thinlet = thinlet;
        this.showPemCertificate = showPemCertificate;
        try {
            InputStream guiXml = this.getClass().getResourceAsStream("X509Certificate.xml");
            assert guiXml != null;
            panel = thinlet.parse(guiXml, this);

            pemLabel = thinlet.find(panel, "pemLabel");
            pem = thinlet.find(panel, "pem");
            errorLabel = thinlet.find(panel, "errorLabel");
            subject = thinlet.find(panel, "subject");
            subjectAlt = thinlet.find(panel, "subjectAlt");
            subjectAltLabel = thinlet.find(panel, "subjectAltLabel");
            issuer = thinlet.find(panel, "issuer");
            issuerAlt = thinlet.find(panel, "issuerAlt");
            issuerAltLabel = thinlet.find(panel, "issuerAltLabel");
            validFrom = thinlet.find(panel, "validFrom");
            validTo = thinlet.find(panel, "validTo");

            assert pemLabel != null;
            assert pem != null;
            assert errorLabel != null;
            assert subject != null;
            assert subjectAlt != null;
            assert subjectAltLabel != null;
            assert issuer != null;
            assert issuerAlt != null;
            assert issuerAltLabel != null;
            assert validFrom != null;
            assert validTo != null;
        } catch (Exception e) {
            throw new RuntimeException("Error creating X509CertificatePanel: "+e, e);
        }
    }

    public Object getThinletPanel() {
        return panel;
    }

    public void setCertificatePEMString(String certificatePEMString) {
        this.certificatePEMString = certificatePEMString;
        showCertificate();
    }

    private static String altIntToString(int altNr) {
        /*
            GeneralName ::= CHOICE {
                  otherName                       [0]     OtherName,
                  rfc822Name                      [1]     IA5String,
                  dNSName                         [2]     IA5String,
                  x400Address                     [3]     ORAddress,
                  directoryName                   [4]     Name,
                  ediPartyName                    [5]     EDIPartyName,
                  uniformResourceIdentifier       [6]     IA5String,
                  iPAddress                       [7]     OCTET STRING,
                  registeredID                    [8]     OBJECT IDENTIFIER
            }
      */
        switch (altNr) {
            case 0: return "otherName";
            case 1: return "rfc822Name";
            case 2: return "dNSName";
            case 3: return "x400Address";
            case 4: return "directoryName";
            case 5: return "ediPartyName";
            case 6: return "uniformResourceIdentifier";
            case 7: return "iPAddress";
            case 8: return "registeredID";
            default: return "invalid alt name type ("+altNr+")";
        }
    }

    public void showCertificate() {
        certificate = KeyUtil.pemToX509Certificate(certificatePEMString);

        thinlet.setBoolean(pemLabel, "visible", showPemCertificate);
        thinlet.setBoolean(pem, "visible", showPemCertificate);
        thinlet.setString(pem, "text", certificatePEMString);

        thinlet.setBoolean(subjectAlt, "visible", false);
        thinlet.setBoolean(subjectAltLabel, "visible", false);
        thinlet.setBoolean(issuerAlt, "visible", false);
        thinlet.setBoolean(issuerAltLabel, "visible", false);

        if (certificate == null) {
            thinlet.setBoolean(errorLabel, "visible", true);
            thinlet.setString(errorLabel, "text", "Error parsing certificate: Not a valid PEM encoded X509 certificate.");

            thinlet.setString(subject, "text", "");
            thinlet.setString(issuer, "text", "");
            thinlet.setString(validFrom, "text", "");
            thinlet.setString(validTo, "text", "");
        } else {
            thinlet.setBoolean(errorLabel, "visible", false);
            thinlet.setString(errorLabel, "text", "");

            thinlet.setString(subject, "text", certificate.getSubjectX500Principal().toString());
            thinlet.setString(issuer, "text", certificate.getIssuerX500Principal().toString());
            thinlet.setString(validFrom, "text", certificate.getNotBefore().toString());
            thinlet.setString(validTo, "text", certificate.getNotAfter().toString());

            Collection<List<?>> altNames = null;
            try {
                altNames = certificate.getSubjectAlternativeNames();
            } catch (CertificateParsingException e) {
                //ignore subject Alt name parsing exceptions
            }
            if (altNames != null && !altNames.isEmpty()) {
                thinlet.setBoolean(subjectAlt, "visible", true);
                thinlet.setBoolean(subjectAltLabel, "visible", true);
                String alts = "";
                for (List<?> altName : altNames) {
                    Integer nameType = (Integer) altName.get(0);
                    //                if (nameType == 6) {
                    //                    //uniformResourceIdentifier
                    //                    String urn = (String) altName.get(1);
                    //                }
//                           System.out.println("Subject alt name: type=" + nameType + " value=" + altName.get(1).toString());
                    if (alts.length() > 0)  alts += "\n";
                    alts += altIntToString(nameType) + " -> " + altName.get(1).toString();
                }
                thinlet.setString(subjectAlt, "text", alts);
            }

            altNames = null;
            try {
                altNames = certificate.getIssuerAlternativeNames();
            } catch (CertificateParsingException e) {
                //ignore subject Alt name parsing exceptions
            }
            if (altNames != null && !altNames.isEmpty()) {
                thinlet.setBoolean(issuerAlt, "visible", true);
                thinlet.setBoolean(issuerAltLabel, "visible", true);
                String alts = "";
                for (List<?> altName : altNames) {
                    Integer nameType = (Integer) altName.get(0);
                    //                if (nameType == 6) {
                    //                    //uniformResourceIdentifier
                    //                    String urn = (String) altName.get(1);
                    //                }
//                           System.out.println("Subject alt name: type=" + nameType + " value=" + altName.get(1).toString());
                    if (alts.length() > 0)  alts += "\n";
                    alts += altIntToString(nameType) + " -> " + altName.get(1).toString();
                }
                thinlet.setString(issuerAlt, "text", alts);
            }
        }

//        System.out.println("certificate=" + certificate);
    }

    private static Thinlet seperateFrameThinlet = null;
    private static FrameLauncher seperateFrameLauncher = null;
   public static X509CertificatePanel showManualCommandPan(String certificatePEMString, boolean showPemCertificate) {
       if (seperateFrameThinlet == null)
           seperateFrameThinlet = new Thinlet();

       X509CertificatePanel pan = new X509CertificatePanel(seperateFrameThinlet, showPemCertificate);
       pan.setCertificatePEMString(certificatePEMString);
       seperateFrameThinlet.add(pan.getThinletPanel());

       if (seperateFrameLauncher == null)
           seperateFrameLauncher = new FrameLauncher("X509 Certificate Details", seperateFrameThinlet, 800, 500) {
               public void	windowClosed(WindowEvent e) { /*super.windowClosed(e);*/ }
               public void	windowClosing(WindowEvent e) { /*super.windowClosing(e);*/ seperateFrameLauncher.setVisible(false);
               }
           };
       else
           seperateFrameLauncher.setVisible(true);

       return pan;
   }

}
