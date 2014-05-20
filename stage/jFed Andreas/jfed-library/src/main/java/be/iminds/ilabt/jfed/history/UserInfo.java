package be.iminds.ilabt.jfed.history;

import be.iminds.ilabt.jfed.lowlevel.Gid;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * UserInfo
 */
public class UserInfo {
    //SA API specifies these in Resolve return type for user:
//    "uid"  : "login (Emulab) ID of the user.",
//    "name" : "common name",
//    "uuid" : "rfc4122 universally unique identifier",
//    "email": "registered email address",
//    "hrn"  : "Human Readable Name (HRN)",
//    "gid"  : "ProtoGENI Identifier (an x509 certificate)",

/* Result in practice:
subauthorities -> {
                    urn:publicid:IDN+wall3.test.ibbt.be:odspracticum+authority+sa -> https://www.wall3.test.ibbt.be:12369/protogeni/xmlrpc/project/ODSPracticum/sa,
                    urn:publicid:IDN+wall3.test.ibbt.be:ocean+authority+sa -> https://www.wall3.test.ibbt.be:12369/protogeni/xmlrpc/project/OCEAN/sa
                  }   (struct)
uid -> wvdemeer
name -> Wim Van de Meerssche
slices -> [slice1, slice2]    (array)
uuid -> d84882f4-0c86-11e2-938a-005056bc479f
email -> wim.vandemeerssche@intec.ugent.be
hrn -> wall3geni.wvdemeer
urn -> urn:publicid:IDN+wall3.test.ibbt.be+user+wvdemeer
gid -> MIID0zCCAzygAwIBAgICASswDQYJKoZIhvcNAQEEBQAwgbkxCzAJBgNVBAYTAkJF
MQwwCgYDVQQIEwNPVkwxDjAMBgNVBAcTBUdoZW50MR4wHAYDVQQKExVVR2VudC1J
...
4gl0pXUjarcl01O2RN6Puq9gr1y0vtihb9ZZ9w2NNaqhB9dSjG1eem03eP6OpPe7
PylJVK+pvzUMu/g+NLYMM21Y+GbgeZU=

* */

    //subauthorities ignored for the moment: private SubAuthority subauthorities;
    private String uid;
    private String name;
    private List<String> slices;
    private String uuid;
    private String email;
    private String hrn;
    private String urn;
    private Gid gid;

    public UserInfo(Hashtable xmlrpcResultStruct) {
        this.uid = xmlrpcResultStruct.get("uid").toString();
        this.name = xmlrpcResultStruct.get("name").toString();
        this.uuid = xmlrpcResultStruct.get("uuid").toString();
        this.email = xmlrpcResultStruct.get("email").toString();
        this.hrn = xmlrpcResultStruct.get("hrn").toString();
        this.urn = xmlrpcResultStruct.get("urn").toString();
        this.gid = new Gid(xmlrpcResultStruct.get("gid").toString());
        Vector slicesVect = (Vector) xmlrpcResultStruct.get("slices");
        this.slices = new ArrayList<String>();
        for (int i = 0; i < slicesVect.size(); i++) {
            String s = slicesVect.get(i).toString();
            slices.add(s);
        }
    }

    public UserInfo(String uid, String name, List<String> slices, String uuid, String email, String hrn, String urn, Gid gid) {
        this.uid = uid;
        this.name = name;
        this.slices = slices;
        this.uuid = uuid;
        this.email = email;
        this.hrn = hrn;
        this.urn = urn;
        this.gid = gid;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public List<String> getSlices() {
        return slices;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getHrn() {
        return hrn;
    }

    public String getUrn() {
        return urn;
    }

    public Gid getGid() {
        return gid;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", slices=" + slices +
                ", uuid='" + uuid + '\'' +
                ", email='" + email + '\'' +
                ", hrn='" + hrn + '\'' +
                ", urn='" + urn + '\'' +
                ", gid='" + gid + '\'' +
                '}';
    }
}
