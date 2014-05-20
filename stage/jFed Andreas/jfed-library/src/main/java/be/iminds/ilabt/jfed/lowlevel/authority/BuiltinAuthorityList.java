package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
*
* Hint: to fetch server SSL certificates in PEM format, you can use openssl like this:
*    openssl s_client -showcerts -connect <hostname>:<port> -prexit
*
* */

public class BuiltinAuthorityList {
    private BuiltinAuthorityList() {    }

    private static SfaAuthority emulabAuthority(String baseUrl, String urnName, String hrn, boolean am, boolean am2, boolean am3) throws GeniException {
        try {
            URL base = new URL(baseUrl);
            String url_sa = baseUrl+"/protogeni/xmlrpc/sa";
            String url_am = baseUrl+"/protogeni/xmlrpc/am";
            String url_am2 = baseUrl+"/protogeni/xmlrpc/am/2.0";
            String url_am3 = baseUrl+"/protogeni/xmlrpc/am/3.0";

            String urn = "urn:publicid:IDN+"+urnName+"+authority+cm";

            String name = base.getHost();

            Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
            urls.put(new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1), new URL(url_sa));
            if (am)
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 1), new URL(url_am));
            if (am2)
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL(url_am2));
            if (am3)
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL(url_am3));
            SfaAuthority emulabauth = new SfaAuthority(urn, hrn, urls, null/*gid*/, "emulab");
            emulabauth.setSource(SfaAuthority.InfoSource.BUILTIN);

            return emulabauth;
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL malformed: "+baseUrl, e);
        }
    }

    private static SfaAuthority planetLabEurope() throws GeniException {
        try {
            String planetLabPemcert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICAjCCAWugAwIBAgIBAzANBgkqhkiG9w0BAQQFADAOMQwwCgYDVQQDEwNwbGUw\n" +
                    "HhcNMTEwOTA2MTM1NDQ3WhcNMTYwOTA0MTM1NDQ3WjAOMQwwCgYDVQQDEwNwbGUw\n" +
                    "gZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALkBw6LbOd11cCG16hnh/HN/r6hu\n" +
                    "pmqTHhhOI9RO5fc8ypbcMvvEzOf1+QbQlv9itVIL7jjoAKiCyVcC1PmAlQyV8U8a\n" +
                    "fsRuTOAy+gXQUGQxKeR8G4k9iv3VoXj2TuSpFUUN9EfjS/2BJl7enkdaoPTWrs9z\n" +
                    "sEdCjKGEeYlQPqRhAgMBAAGjcDBuMA8GA1UdEwEB/wQFMAMBAf8wWwYDVR0RBFQw\n" +
                    "UoYhdXJuOnB1YmxpY2lkOklETitwbGUrYXV0aG9yaXR5K3Nhhi11cm46dXVpZDo2\n" +
                    "MTMyNThjMi0wMmJlLTRkZDgtOThlMS02NjAxZmNhNTNhZjIwDQYJKoZIhvcNAQEE\n" +
                    "BQADgYEARhqfrAhxX4caWnfYVSx1fF3/adA7KkUZonSUgEd38NmAlt9xLqOJAAcZ\n" +
                    "0GLfOrxm7Url31GqwDFsND9/VLaDt7WEx5NbsbaFhMu9TNlhc/2UQOuoQK0glyaw\n" +
                    "dAyg/PfHVy1XACG/kqIKIwKvx17mpXTF+bZLzcVwhMwgb3Ipcak=\n" +
                    "-----END CERTIFICATE-----";

            String url_registry =  "https://sfa.planet-lab.eu:12345";
            String url_am2 = "https://sfa.planet-lab.eu:12346";
            String url_slicemgr = "https://sfa.planet-lab.eu:12347";

            String urn = "urn:publicid:IDN+ple:ibbtple+authority+cm";

            String name = "PlanetLab Europe";

            Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
            urls.put(new ServerType(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1), new URL(url_registry));
            urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL(url_am2));
            SfaAuthority pleAuth = new SfaAuthority(urn, name, urls, null/*gid*/, "planetlab");
            pleAuth.setSource(SfaAuthority.InfoSource.BUILTIN);
            pleAuth.setReconnectEachTime(true);

            pleAuth.setPemSslTrustCert(planetLabPemcert);

            pleAuth.addAllowedCertificateHostnameAlias("ple");

            return pleAuth;
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL malformed: "+e.getMessage(), e);
        }
    }
    private static SfaAuthority planetLabCentral() throws GeniException {
        try {
            String planetLabPemcert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIICAjCCAWugAwIBAgIBAzANBgkqhkiG9w0BAQQFADAOMQwwCgYDVQQDEwNwbGMw\n" +
                    "HhcNMTEwOTA2MTkxNzE3WhcNMTYwOTA0MTkxNzE3WjAOMQwwCgYDVQQDEwNwbGMw\n" +
                    "gZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALwfK0p5JtRfQp+kQTZfokt1mcqb\n" +
                    "IYvfnTH2SxhdDZsFmelVWISi70VWQBLWsZu/xqZrale0it9tOVNLNwQLIbSqZFqL\n" +
                    "mK7EemuPQVs5q83ezGvmJsUT4EXIAMg8JScaCTht3JMWoFQMJCOMOga50hnitfCF\n" +
                    "vm9ih44/7ehwvQf7AgMBAAGjcDBuMA8GA1UdEwEB/wQFMAMBAf8wWwYDVR0RBFQw\n" +
                    "UoYhdXJuOnB1YmxpY2lkOklETitwbGMrYXV0aG9yaXR5K3Nhhi11cm46dXVpZDpi\n" +
                    "YzVkNmFkMy0zYTA4LTRmNGItOGViOC1iZDhjNWNiMGU0NGUwDQYJKoZIhvcNAQEE\n" +
                    "BQADgYEAUqNjtwhyQWu87nklNuGa2/7DIThdSvObbs1S/7XhKUox9vwCJPQc0mmr\n" +
                    "4tzJOpsm/8Mg80UFOK9e5dSLmCiu8JANI4B/i3xozYc1GO1H/DToq2FMQjbbibUq\n" +
                    "T6KxeHlwYPrlonM1TXx9w+6YUQ4tzUX/bqT5ck1TpMDkftV9pjM="+
                    "-----END CERTIFICATE-----";

            String url_sa =  "https://";
            String url_am3 = "http://sfav3.planet-lab.org:12346";

            String urn = "urn:publicid:IDN+plc+authority+cm";

            String name = "PlanetLab Central";

            Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
//            urls.put(new ServerType(ServerType.GeniServerRole.SA, 1), new URL(url_sa));
            urls.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL(url_am3));
            SfaAuthority plcAuth = new SfaAuthority(urn, name, urls, null/*gid*/, "planetlab");
            plcAuth.setSource(SfaAuthority.InfoSource.BUILTIN);
            plcAuth.setReconnectEachTime(true);

            plcAuth.setPemSslTrustCert(planetLabPemcert);

            plcAuth.addAllowedCertificateHostnameAlias("plc");

            return plcAuth;
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL malformed: "+e.getMessage(), e);
        }
    }

    private static SfaAuthority fiTeagle() throws GeniException {
        try {
            String fiTeaglePemcert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIDRjCCAq+gAwIBAgIBCTANBgkqhkiG9w0BAQUFADBqMQswCQYDVQQGEwJERTEP\n" +
                    "MA0GA1UECAwGQmVybGluMQ8wDQYDVQQHDAZCZXJsaW4xEjAQBgNVBAoMCVRVIEJl\n" +
                    "cmxpbjELMAkGA1UECwwCQVYxGDAWBgNVBAMMD2F2LnR1LWJlcmxpbi5kZTAeFw0x\n" +
                    "MzA1MzExMzI4NDNaFw0xNDA1MzExMzI4NDNaMHYxCzAJBgNVBAYTAkRFMQ8wDQYD\n" +
                    "VQQIDAZCZXJsaW4xGTAXBgNVBAoMEEZyYXVuaG9mZXIgRk9LVVMxDTALBgNVBAsM\n" +
                    "BE5HTkkxLDAqBgNVBAMMI2ZpdGVhZ2xlLWZ1c2Vjby5mb2t1cy5mcmF1bmhvZmVy\n" +
                    "LmRlMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/52gb28jfxz/EopFy1j1r\n" +
                    "5tcGtMPqyREaHaMY4GXAmNlnfJzxILFtLUOSPa4R4hChrl8qXqCa09iLnRQfS9fC\n" +
                    "i706MdMsppwFeAaWwyYPmN9AUOR3IDearz4AHPoncd6tbcDHODxfwmikyrqW5Oen\n" +
                    "5pFz7M0xz4X1qATNteGEbwIDAQABo4HvMIHsMAkGA1UdEwQCMAAwLAYJYIZIAYb4\n" +
                    "QgENBB8WHU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTK\n" +
                    "Wq0AvCeFYdtb+bg8Ogj0SeeJbTCBhAYDVR0jBH0we6FupGwwajELMAkGA1UEBhMC\n" +
                    "REUxDzANBgNVBAgMBkJlcmxpbjEPMA0GA1UEBwwGQmVybGluMRIwEAYDVQQKDAlU\n" +
                    "VSBCZXJsaW4xCzAJBgNVBAsMAkFWMRgwFgYDVQQDDA9hdi50dS1iZXJsaW4uZGWC\n" +
                    "CQDJgKT16VcNozALBgNVHQ8EBAMCBeAwDQYJKoZIhvcNAQEFBQADgYEAYSZ7BRTl\n" +
                    "9+wVr9+pbRaMeg9BMqJCep2Xu7LEWK7tn3oH1O9uI7/Bo6axwrmcGsHw0vtgmd2N\n" +
                    "hfgeVqRGHgeFDpgtWFWwXu/rDS8pslOV9Y2pvKC3CIoCSWEIPu2VVWfMRVbN5G1l\n" +
                    "sABRUfL5sr6S0T8QSxXpg2P7qel1oa4hoYY=\n" +
                    "-----END CERTIFICATE-----\n";

//            String url_am3 = "https://fiteagle-fuseco.fokus.fraunhofer.de/api/sfa/am/v3";
            String url_am3 = "https://193.175.132.29/api/sfa/am/v3";

            String urn = "urn:publicid:IDN+fiteagle+authority+am";

            String name = "FITeagle";


            Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
            urls.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL(url_am3));
            SfaAuthority fiteagleAuth = new SfaAuthority(urn, name, urls, null/*gid*/, "teagle");
            fiteagleAuth.setSource(SfaAuthority.InfoSource.BUILTIN);
            fiteagleAuth.setReconnectEachTime(false);

            fiteagleAuth.setPemSslTrustCert(fiTeaglePemcert);

            fiteagleAuth.addAllowedCertificateHostnameAlias("fiteagle-fuseco.fokus.fraunhofer.de");

            return fiteagleAuth;
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL malformed: "+e.getMessage(), e);
        }
    }

//    private static List<GeniAuthority> builtin = null;
//    public static List<GeniAuthority> getAuthorities() {
//        if (builtin != null) return builtin;

    //this should only be called JFedCombinedAuthorityList  (TODO clean up this method name)
    public static void load(AuthorityListModel authorityListModel) {
//        List<GeniAuthority> builtin = new ArrayList<GeniAuthority>();

        try {
            String pem_wall3_CA_cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIID7jCCA1egAwIBAgIBBDANBgkqhkiG9w0BAQQFADCBuTELMAkGA1UEBhMCQkUx\n" +
                    "DDAKBgNVBAgTA09WTDEOMAwGA1UEBxMFR2hlbnQxHjAcBgNVBAoTFVVHZW50LUlu\n" +
                    "dGVjLUlCQ04vSUJCVDEeMBwGA1UECxMVQ2VydGlmaWNhdGUgQXV0aG9yaXR5MSAw\n" +
                    "HgYDVQQDExdib3NzLndhbGwzLnRlc3QuaWJidC5iZTEqMCgGCSqGSIb3DQEJARYb\n" +
                    "dndhbGwtb3BzQGF0bGFudGlzLnVnZW50LmJlMB4XDTEyMDIyODE0MjkxN1oXDTE3\n" +
                    "MDgyMDE0MjkxN1owgZwxCzAJBgNVBAYTAkJFMQwwCgYDVQQIEwNPVkwxHjAcBgNV\n" +
                    "BAoTFVVHZW50LUludGVjLUlCQ04vSUJCVDESMBAGA1UECxMJV2ViU2VydmVyMR8w\n" +
                    "HQYDVQQDExZ3d3cud2FsbDMudGVzdC5pYmJ0LmJlMSowKAYJKoZIhvcNAQkBFht2\n" +
                    "d2FsbC1vcHNAYXRsYW50aXMudWdlbnQuYmUwgZ8wDQYJKoZIhvcNAQEBBQADgY0A\n" +
                    "MIGJAoGBAOTau9JoFfrxhb2DVtP+e2KLCLkJ1v9V6Zwb6VSk+PaRTESMG5a/dIwT\n" +
                    "s4sVTLB2RSUmT5oizRHDpBQxoQVIsppGrA7KD+z7d2kkb9X9U/ssN0s5xd+T1i4i\n" +
                    "CnJf25GJgivdzk1waXEBNVpNLnO1NIVyVymORcb5uxkmmtLVesSXAgMBAAGjggEf\n" +
                    "MIIBGzAJBgNVHRMEAjAAMB0GA1UdDgQWBBTYhYInDHrLplgwmqIMkW7N9/6UlDCB\n" +
                    "7gYDVR0jBIHmMIHjgBS1oPfHOg9jA0yLFsmT3k8DIHm4O6GBv6SBvDCBuTELMAkG\n" +
                    "A1UEBhMCQkUxDDAKBgNVBAgTA09WTDEOMAwGA1UEBxMFR2hlbnQxHjAcBgNVBAoT\n" +
                    "FVVHZW50LUludGVjLUlCQ04vSUJCVDEeMBwGA1UECxMVQ2VydGlmaWNhdGUgQXV0\n" +
                    "aG9yaXR5MSAwHgYDVQQDExdib3NzLndhbGwzLnRlc3QuaWJidC5iZTEqMCgGCSqG\n" +
                    "SIb3DQEJARYbdndhbGwtb3BzQGF0bGFudGlzLnVnZW50LmJlggkA6F5CMJEpkRcw\n" +
                    "DQYJKoZIhvcNAQEEBQADgYEAps4Qg/hUOt1qjyyD0cGqid9F5d/8ByQP1TDol8wE\n" +
                    "T5LX3p/4FTQlZRZ6c8xQATMufn81efo3K1S/dQs3sZijqPq8roqUqz8C6DLHklgc\n" +
                    "nz4LcE8vRN6kLv+bF7BaBb5LSc6avH+/UFnewm44No6ADDcN/k+RqOy10yZz8MIu\n" +
                    "T+c\n" +
                    "-----END CERTIFICATE-----";
            String pem_wall3_12369_CA_cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIDCjCCAnOgAwIBAgIBCDANBgkqhkiG9w0BAQQFADCBuTELMAkGA1UEBhMCQkUx\n" +
                    "DDAKBgNVBAgTA09WTDEOMAwGA1UEBxMFR2hlbnQxHjAcBgNVBAoTFVVHZW50LUlu\n" +
                    "dGVjLUlCQ04vSUJCVDEeMBwGA1UECxMVQ2VydGlmaWNhdGUgQXV0aG9yaXR5MSAw\n" +
                    "HgYDVQQDExdib3NzLndhbGwzLnRlc3QuaWJidC5iZTEqMCgGCSqGSIb3DQEJARYb\n" +
                    "dndhbGwtb3BzQGF0bGFudGlzLnVnZW50LmJlMB4XDTEyMDMwMjE1NDQyMVoXDTE3\n" +
                    "MDgyMzE2NDQyMVowgacxCzAJBgNVBAYTAkJFMQwwCgYDVQQIEwNPVkwxHjAcBgNV\n" +
                    "BAoTFVVHZW50LUludGVjLUlCQ04vSUJCVDEdMBsGA1UECxMUUHJvdG9HRU5JIFJQ\n" +
                    "QyBTZXJ2ZXIxHzAdBgNVBAMTFnd3dy53YWxsMy50ZXN0LmliYnQuYmUxKjAoBgkq\n" +
                    "hkiG9w0BCQEWG3Z3YWxsLW9wc0BhdGxhbnRpcy51Z2VudC5iZTCBnzANBgkqhkiG\n" +
                    "9w0BAQEFAAOBjQAwgYkCgYEA1fQzdLlTCLUNgzxT0Uj6i3PTbq/GCzzzhmT4c5Yy\n" +
                    "dbirxC+ZLM698NTAFHDParrjtMDsIoc7IdGI2UuOWQ2aTrNqJt8LbInVgyyuw3s1\n" +
                    "UHYWtnLy38/WgsH4gNx1Ry4gRgak5seLUBwdyoPD0n8GYFGaYeG6v7PuUkNEAAK0\n" +
                    "NG0CAwEAAaMyMDAwHQYDVR0OBBYEFEPMSP5+M5EMBbsqdCwhgmqd6A+mMA8GA1Ud\n" +
                    "EwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEEBQADgYEABDx3ZXHJkzS5nRQBvX5b6UR/\n" +
                    "+xYePQS03Sk8cj/nXBddzCV6c/uxNq7HVt+Nr2MmQB0iQX0Ayg29i+iePZFNEg2O\n" +
                    "FFlTJRPLHi/Pf9WxTEXG/uZ05zarDU56mQwhhIt/3VxdCr/mtnS7/BuAV5J7Jstu\n" +
                    "gjGh5R1mV0mCt7jk52c=\n" +
                    "-----END CERTIFICATE-----\n";
            String pem_wall1_CA_cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIEgDCCA2igAwIBAgIRAKo2GQqQ2O8X8iMTpvmcfSUwDQYJKoZIhvcNAQEFBQAw\n" +
                    "NjELMAkGA1UEBhMCTkwxDzANBgNVBAoTBlRFUkVOQTEWMBQGA1UEAxMNVEVSRU5B\n" +
                    "IFNTTCBDQTAeFw0xMzAzMjYwMDAwMDBaFw0xNjAzMjUyMzU5NTlaMEcxITAfBgNV\n" +
                    "BAsTGERvbWFpbiBDb250cm9sIFZhbGlkYXRlZDEiMCAGA1UEAxMZd3d3LndhbGwx\n" +
                    "LmlsYWJ0LmltaW5kcy5iZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n" +
                    "APPnMT0sKUtWKvydDVdgV6XEa0EpfieeH0qWmH8bFp9SEQg20LvTZmtVMRZ3Pc8R\n" +
                    "2SsCNDjnZga+FLXHxq4xkwYO1FIB73GX22w3RyrTAngLN+6dO3fv0BoQ+5kHIR6W\n" +
                    "n/H/l9A1BEI/4HiCMm8tQ8kDuhbkxrUnreVZuIZbsKy8f+A27/Ctq434rGSjZkXp\n" +
                    "P12fxShaomCXHWp2c8vxsSM5uIcnXvbhycuUimTAq7oeYbE0nm51d/axbGpWYTlV\n" +
                    "byl6bYu9mur0gqoSosDTNkgeg8RN3vCMUVgge7cWxv2jlJByXr2WsPFZXCmZU0/e\n" +
                    "6KA9LkkTDi/49O/46tdBUNUCAwEAAaOCAXYwggFyMB8GA1UdIwQYMBaAFAy9k2gM\n" +
                    "896ro0lrKzdXR+qQ47ntMB0GA1UdDgQWBBTW8fFDUPluzPlNfLtgbvhz2a4LHDAO\n" +
                    "BgNVHQ8BAf8EBAMCBaAwDAYDVR0TAQH/BAIwADAdBgNVHSUEFjAUBggrBgEFBQcD\n" +
                    "AQYIKwYBBQUHAwIwIgYDVR0gBBswGTANBgsrBgEEAbIxAQICHTAIBgZngQwBAgEw\n" +
                    "OgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL2NybC50Y3MudGVyZW5hLm9yZy9URVJF\n" +
                    "TkFTU0xDQS5jcmwwbQYIKwYBBQUHAQEEYTBfMDUGCCsGAQUFBzAChilodHRwOi8v\n" +
                    "Y3J0LnRjcy50ZXJlbmEub3JnL1RFUkVOQVNTTENBLmNydDAmBggrBgEFBQcwAYYa\n" +
                    "aHR0cDovL29jc3AudGNzLnRlcmVuYS5vcmcwJAYDVR0RBB0wG4IZd3d3LndhbGwx\n" +
                    "LmlsYWJ0LmltaW5kcy5iZTANBgkqhkiG9w0BAQUFAAOCAQEAkpSkznVRrgxPKgCz\n" +
                    "iScydzajGy03gwNBtusw2C+kvS8ZtuJsY2Fj7gu9WQL74C7mZhSzXy7Tq+Rbgar/\n" +
                    "kmkCAK0fbWb65zP0QG07oI9xy0K1+AnVrto4tqEczY5dtavia0ZIB53KR1GKvasd\n" +
                    "NGSvvO/so4/YqTuJ8e6vjnc5oo7Zbk+wPn4GMgbeSGSGNm/A5FCK30WsZ/C1Fzok\n" +
                    "QNj+bM6Et4B6tmPFvItiV1FKZTyQE7TCvJAZurL25hnIb0oKMUjErn/XlZhusF5r\n" +
                    "nvMtYfaQ5J1WWSg4qYlbTPJKc/lozp70qutiRhEUWlaQQep9lBP7hN8ExVgUNXg6\n" +
                    "g6D70Q==\n" +
                    "-----END CERTIFICATE-----";
//            String pem_wallExperimental_CA_cert ="-----BEGIN CERTIFICATE-----\n" +
//                    "MIID4zCCA0ygAwIBAgICBAIwDQYJKoZIhvcNAQEEBQAwgbUxCzAJBgNVBAYTAkJF\n" +
//                    "MQswCQYDVQQIEwJPVjEOMAwGA1UEBxMFR2hlbnQxGDAWBgNVBAoTD2lNaW5kcyAt\n" +
//                    "IGlsYWIudDEeMBwGA1UECxMVQ2VydGlmaWNhdGUgQXV0aG9yaXR5MSMwIQYDVQQD\n" +
//                    "Expib3NzLnZ3YWxsLmlsYWJ0LmltaW5kcy5iZTEqMCgGCSqGSIb3DQEJARYbdndh\n" +
//                    "bGwtb3BzQGF0bGFudGlzLnVnZW50LmJlMB4XDTEzMDQxODEzMDU1MloXDTE4MTAw\n" +
//                    "OTEzMDU1MlowgZgxCzAJBgNVBAYTAkJFMQswCQYDVQQIEwJPVjEYMBYGA1UEChMP\n" +
//                    "aU1pbmRzIC0gaWxhYi50MRIwEAYDVQQLEwlXZWJTZXJ2ZXIxIjAgBgNVBAMTGXd3\n" +
//                    "dy52d2FsbC5pbGFidC5pbWluZHMuYmUxKjAoBgkqhkiG9w0BCQEWG3Z3YWxsLW9w\n" +
//                    "c0BhdGxhbnRpcy51Z2VudC5iZTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA\n" +
//                    "u7upYErAKn5u2zP3T2H5mVCjZ1y6Lw9CnPYuxVl0PSB2fR0opSSpxWbEeGqTKJvn\n" +
//                    "GjDuuwYerpfHxdZtfa/xpPhsIITFxpi8qEr/OUTCO8un/pg1lvFoJXNEqKE7xbRm\n" +
//                    "cY683SJblJy6JawRArTUFG1oeFllznqp/ZE5B/gGdw0CAwEAAaOCARswggEXMAkG\n" +
//                    "A1UdEwQCMAAwHQYDVR0OBBYEFLdVW1SK/W3enycU8E8iRzPfi1KgMIHqBgNVHSME\n" +
//                    "geIwgd+AFAStbdQKkLG771cOVygY+aFEbIDEoYG7pIG4MIG1MQswCQYDVQQGEwJC\n" +
//                    "RTELMAkGA1UECBMCT1YxDjAMBgNVBAcTBUdoZW50MRgwFgYDVQQKEw9pTWluZHMg\n" +
//                    "LSBpbGFiLnQxHjAcBgNVBAsTFUNlcnRpZmljYXRlIEF1dGhvcml0eTEjMCEGA1UE\n" +
//                    "AxMaYm9zcy52d2FsbC5pbGFidC5pbWluZHMuYmUxKjAoBgkqhkiG9w0BCQEWG3Z3\n" +
//                    "YWxsLW9wc0BhdGxhbnRpcy51Z2VudC5iZYIJAPH8bY0hfiL4MA0GCSqGSIb3DQEB\n" +
//                    "BAUAA4GBAAyIIftKv/9Wz7XCLys7IQ8DKa3HEf6XQvkbOcWHRGRvQjyPLGNfOVop\n" +
//                    "2Svgez0tQ9t//Iewv/KKVdMMrmiL26qyBPRx8HYCvadDrjeTg82WAdb63QovSFl1\n" +
//                    "nrPxAv/9LHrAeujrWvKnsn+6xGpl2ZeQpMV+cIW9B9xYpD8S/U62\n" +
//                    "-----END CERTIFICATE-----";
            String wilab2_cert = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIEgjCCA2qgAwIBAgIRAJtSpUYIQTrQTVeuvAt8bXQwDQYJKoZIhvcNAQEFBQAw\n" +
                    "NjELMAkGA1UEBhMCTkwxDzANBgNVBAoTBlRFUkVOQTEWMBQGA1UEAxMNVEVSRU5B\n" +
                    "IFNTTCBDQTAeFw0xMzA1MDYwMDAwMDBaFw0xNjA1MDUyMzU5NTlaMEgxITAfBgNV\n" +
                    "BAsTGERvbWFpbiBDb250cm9sIFZhbGlkYXRlZDEjMCEGA1UEAxMad3d3LndpbGFi\n" +
                    "Mi5pbGFidC5pbWluZHMuYmUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB\n" +
                    "AQDH3Tel+sJ6ikRLtZ43YaTQq8YOSaHnguy/3RDHeRoQAiNoyopQvXNwzeRgpHMR\n" +
                    "3bRwG6jjmwfIsVfMwL+f1gX/bmWrZxCoovU2Fz1NiR3p1zI1AHnW+MBWoF9/Q+5Y\n" +
                    "1eWAz61VKu/kL6X55fD8GYWs1vRrFeMyvqGhkBWDwQza0Ct5TH/x016pUM4jaF+b\n" +
                    "s5xD6cP37bCMUO5S7rlYa7v4BGLvJgMxax7x3RoMOaAddOM/4lofdZmFlKvYa0nM\n" +
                    "a/ta+dDecBz35/minYUwuq5gW25XR7KFfNeyVAkv69nNiErFGVCt2Mv5kjwm3NgV\n" +
                    "ec+mkJwU9PSAN37x+qbJHXOjAgMBAAGjggF3MIIBczAfBgNVHSMEGDAWgBQMvZNo\n" +
                    "DPPeq6NJays3V0fqkOO57TAdBgNVHQ4EFgQUqlvn1Jr9Ns1v7KUSTtweAxrieaUw\n" +
                    "DgYDVR0PAQH/BAQDAgWgMAwGA1UdEwEB/wQCMAAwHQYDVR0lBBYwFAYIKwYBBQUH\n" +
                    "AwEGCCsGAQUFBwMCMCIGA1UdIAQbMBkwDQYLKwYBBAGyMQECAh0wCAYGZ4EMAQIB\n" +
                    "MDoGA1UdHwQzMDEwL6AtoCuGKWh0dHA6Ly9jcmwudGNzLnRlcmVuYS5vcmcvVEVS\n" +
                    "RU5BU1NMQ0EuY3JsMG0GCCsGAQUFBwEBBGEwXzA1BggrBgEFBQcwAoYpaHR0cDov\n" +
                    "L2NydC50Y3MudGVyZW5hLm9yZy9URVJFTkFTU0xDQS5jcnQwJgYIKwYBBQUHMAGG\n" +
                    "Gmh0dHA6Ly9vY3NwLnRjcy50ZXJlbmEub3JnMCUGA1UdEQQeMByCGnd3dy53aWxh\n" +
                    "YjIuaWxhYnQuaW1pbmRzLmJlMA0GCSqGSIb3DQEBBQUAA4IBAQAna948n23y6gJV\n" +
                    "rR976Hxi2oW+Py7w7ycW5eHD/YF3qdhLsvWBCffh26zeWE8IXbNyLU9fP96vRHAh\n" +
                    "D99gOcD7wiUYnjmvOwRoK/1bStzxB8PYRuZVyUKp+5ksmERxSJMlFgb7HHtgF3m6\n" +
                    "e1835ONyDfxmyMH1z+G/gpw8Sl5+4tFxGnvVbyN9WnLH1ZW3mdhEIMKCRp26/wnL\n" +
                    "VU19QQHbqWnouYmeZ6ZUE3yvYobihl1Cqc207AoxmLUvQjleyC6Dr9twj775joPF\n" +
                    "rfV4VxQlZE6hhhVwgBdn/Of12KPkn4KNzxQ1G2XaGq+cBZS37F3wOKTBbp+HZjB+\n" +
                    "mqGrS2Lp\n" +
                    "-----END CERTIFICATE-----\n";

//            GeniAuthority wall3 = emulabAuthority("https://www.wall3.test.ibbt.be", "wall3.test.ibbt.be", "iMinds Virtual Wall 3 (Emulab)");
//            wall3.setPemSslTrustCert(pem_wall3_CA_cert);
            SfaAuthority wall3 = emulabAuthority("https://www.wall3.test.ibbt.be:12369", "wall3.test.ibbt.be", "iMinds Virtual Wall 3 (Emulab)", true, true, false);
            wall3.setPemSslTrustCert(pem_wall3_12369_CA_cert);

            SfaAuthority wilab2 = emulabAuthority("https://www.wilab2.ilabt.iminds.be:12369", "wilab2.ilabt.iminds.be", "iMinds WiLab 2", true, true, true);
            wilab2.setPemSslTrustCert(wilab2_cert);

            SfaAuthority wall1 = emulabAuthority("https://www.wall1.ilabt.iminds.be", "wall1.ilabt.iminds.be", "iMinds Virtual Wall 1 (Emulab)", false, false, false);
            wall1.setPemSslTrustCert(pem_wall1_CA_cert);

            //https://www.vwall.ilabt.iminds.be/protogeni/xmlrpc/am/3.0
//            GeniAuthority wallExperimental = emulabAuthority("https://www.vwall.ilabt.iminds.be", "vwall.ilabt.iminds.be", "Experimental iMinds Virtual Wall", true, true, true);
//            wallExperimental.setPemSslTrustCert(pem_wallExperimental_CA_cert);

            authorityListModel.mergeOrAdd(wall3);
            authorityListModel.mergeOrAdd(wall1);
            authorityListModel.mergeOrAdd(wilab2);
//            authorityListModel.mergeOrAdd(wallExperimental);
            authorityListModel.mergeOrAdd(emulabAuthority("https://www.emulab.net", "emulab.net", "Utah Emulab", true, true, true));
            authorityListModel.mergeOrAdd(planetLabCentral());
            authorityListModel.mergeOrAdd(planetLabEurope());
            authorityListModel.mergeOrAdd(fiTeagle());
        } catch (GeniException e) {
            throw new RuntimeException("Bug: BuiltinAuthorityList should never generate GeniException while creating GeniAuthorities: "+e.getMessage(), e);
        }

        authorityListModel.fireChange();
//        return builtin;
    }

//    public static GeniAuthority getByName(String authorityName) {
//        for (GeniAuthority auth : getAuthorities()) {
//            if (auth.getName().equals(authorityName))
//                return auth;
//        }
//        return null;
//    }
}
