package be.iminds.ilabt.jfed.lowlevel.stitching;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.GeniPlainConnection;
import be.iminds.ilabt.jfed.lowlevel.GeniSslConnection;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * StitchingWorkflowHop
 */
public class StitchingWorkflowHop {
    private final String hopUrn;
    private final String aggUrn;
    private final String aggUrl;
    private final SfaAuthority auth;
    private final boolean importVlans;
    private final Hashtable source;

    private final List<StitchingWorkflowHop> dependencies = new ArrayList<StitchingWorkflowHop>();

    public boolean done;

    public StitchingWorkflowHop(AuthorityListModel authList, String hopUrn, String aggUrn, String aggUrl, boolean importVlans, Hashtable source) {
        this.hopUrn = hopUrn;
        this.aggUrn = aggUrn;
        this.aggUrl = aggUrl;
        this.importVlans = importVlans;
        this.source = source;

        //Creating unknown authorities
        SfaAuthority aggAuth = authList.getByUrn(aggUrn);
        if (aggAuth == null) {
            System.out.println("Unknown Hop aggregate \""+aggUrn+"\". Registering aggregate.");

            if (aggUrl.startsWith("http://")) {
                System.err.println("WARNING: aggregate URL in hop is http instead of https! "+
                        "This is unsecure, so this connection protocol will never be used. "+
                        "Trying to fix automatically: Changing http to https (might not work).");
                aggUrl = aggUrl.replaceFirst("http", "https");
            }

            Map< ServerType, URL > urls = new HashMap< ServerType, URL >();
            try {
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL(aggUrl));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Aggregate URL in hop info is not a valid URL: "+aggUrl);
            }
            SfaAuthority newAuth = null;
            try {
                newAuth = new SfaAuthority(aggUrn, aggUrn, urls, null, "stitching-hop-aggregate");
            } catch (GeniException e) {
                throw new RuntimeException("Error creating SfaAuthority for hop: cannot continue");
            }
            authList.addAuthority(newAuth); //wil update authList automatically
            aggAuth = newAuth;
            assert aggAuth != null : "Bug: Adding newly created SfaAuthority seems to have failed";
        }
        auth = aggAuth;
        assert auth != null;

        done = false;
    }

    public String getHopUrn() {
        return hopUrn;
    }

    public String getAggUrn() {
        return aggUrn;
    }

    public String getAggUrl() {
        return aggUrl;
    }

    public SfaAuthority getAuth() {
        return auth;
    }

    public boolean isImportVlans() {
        return importVlans;
    }

    public List<StitchingWorkflowHop> getDependencies() {
        return dependencies;
    }

    public Hashtable getSource() {
        return source;
    }

    public boolean isDone() {
        return done;
    }
    public boolean areAllDepsDone() {
        if (dependencies.isEmpty()) return true;
        for (StitchingWorkflowHop dep : dependencies)
            if (!dep.isDone()) return false;
        return true;
    }

    public void addDependency(StitchingWorkflowHop dep) {
        dependencies.add(dep);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        String depString = "";
        for (StitchingWorkflowHop dep : dependencies) {
            if (!depString.equals("")) depString+=", \n                       ";
            depString += dep.getHopUrn();
        }

        return "Hop{" + "\n"+
                "    hopUrn='" + hopUrn + '\'' + "\n"+
                "    aggUrn='" + aggUrn + '\'' + "\n"+
                "    aggUrl='" + aggUrl + '\'' + "\n"+
                "    importVlans=" + importVlans + "\n"+
                "    done=" + done + "\n"+
                "        dependencies={ " + depString + " }"+ "\n"+
                '}';
    }
}