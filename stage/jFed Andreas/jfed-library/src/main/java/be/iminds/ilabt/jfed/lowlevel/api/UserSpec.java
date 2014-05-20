package be.iminds.ilabt.jfed.lowlevel.api;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
* UserSpec used in Aggregate Manager V2 and V3
*/
public class UserSpec {
    protected final String urn;
    protected final Vector<String> sshKey;

    /**
     * @param sshKeys a list of ssh keys for the user. Note: an internal copy is taken by this constructor. */
    public UserSpec(String urn, Collection<String> sshKeys) {
        assert urn != null;
        this.urn = urn;
        if (sshKeys != null)
            this.sshKey = new Vector<String>(sshKeys);
        else
            this.sshKey = new Vector<String>();
    }
    public UserSpec(String urn, String sshKey) {
        assert urn != null;
        this.urn = urn;
        this.sshKey = new Vector<String>();
        if (sshKey != null)
            this.sshKey.add(sshKey);
    }
    public UserSpec(String urn) {
        this(urn, new Vector<String>());
    }
    public String getUrn() {
        return urn;
    }
    public Vector<String> getSshKey() {
        return sshKey;
    }

    @Override
    public String toString() {
        return "UserSpec{" +
                "urn='" + urn + '\'' +
                ", sshKey=" + sshKey +
                '}';
    }

    public Hashtable getAsHashtable() {
        Hashtable res = new Hashtable();
        res.put("urn", urn);
        res.put("keys", sshKey);
        return res;
    }
}
