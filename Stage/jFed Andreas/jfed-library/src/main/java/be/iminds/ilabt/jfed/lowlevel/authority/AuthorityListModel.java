package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.Gid;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.util.GeniUrn;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AuthorityListModel is a list of GeniAuthority
 * It can be empty.
 *
 * Authorities can be added and removed, or "merged".
 * "merging" authorities is useful when there are 2 sources of data about an authority.
 * The data can then be combined.
 *
 * You can listen for changes in the list.
 */
public class AuthorityListModel {
    public static interface AuthorityListModelListener {
        //        public void onAuthorityAdded();
        public void onAuthorityListChanged();
    }

    //was package scope, so that only JFedAuthorityList can create a new AuthorityListModel
    //however, the JavaFX gui needs to have an external list
    public AuthorityListModel() {
        authorities = new ArrayList<SfaAuthority>();
    }

    private List<SfaAuthority> authorities;
    public List<SfaAuthority> getAuthorities() {
//        assert authorities.size() > 0;
        assert !authorities.contains(null);
        return Collections.unmodifiableList(authorities);
    }

    /**
     * Retreive a GeniAuthority by URN.
     *
     * @param urn the URN of the GeniAuthority to retreive
     * @return the GeniAuthority by URN. If no authority is known for the URN, return null */
    public SfaAuthority getByUrn(String urn) {
        for (SfaAuthority auth : authorities) {
            if (auth.getUrn().equals(urn))
                return auth;
        }
        return null;
    }
    /**
     * Get the authority matching the authority part of the given urn.
     * This can be a slice, sliver, or any other type of urn.
     * */
    public SfaAuthority getFromAnyUrn(String urn) {
        GeniUrn geniUrn = GeniUrn.parse(urn);
        if (geniUrn == null) {
            System.err.println("Warning: SfaAuthority received an urn that is a not in urn format: \"" + urn + "\"  (will be ignored)");
            return null;
        } else {
            for (SfaAuthority auth : authorities) {
                if (auth.getNameForUrn().equals(geniUrn.getTopLevelAuthority()))
                    return auth;
            }
            return null;
        }
    }

    public boolean removeByUrn(String urn) {
        for (SfaAuthority auth : authorities) {
            if (auth.getUrn().equals(urn)) {
                authorities.remove(auth);
                fireChange();
                return true;
            }
        }
        return false;
    }

    void removeAll() {
        if (authorities.isEmpty()) return;
//        List<SfaAuthority> allAuth = new ArrayList<SfaAuthority>(authorities);
//        for (SfaAuthority auth : allAuth)
//            authorities.remove(auth);
        authorities.clear();
        fireChange();
    }

    /**
     * Does not call fireChange. do this manually afterwards!
     * */
    public SfaAuthority createOrUpdate(String urn, String name, Map<ServerType, URL> urls, Gid gid, String type) {
        for (SfaAuthority auth : authorities) {
            if (auth.getUrn().equals(urn)) {
                auth.updateAll(name, urls, gid, type);
                return auth;
            }
        }
        try {
            SfaAuthority newAuth = new SfaAuthority(urn, name, urls, gid, type);
            authorities.add(newAuth);
            return newAuth;
        } catch (GeniException e) {
            System.err.println("WARNING: Error adding authority (=> authority not added!): \""+urn+"\": "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Does not call fireChange. do this manually afterwards!
     * */
    public SfaAuthority mergeOrAdd(SfaAuthority auth) {
        for (SfaAuthority existingAuth : authorities) {
            if (existingAuth.getUrn().equals(auth.getUrn())) {
                existingAuth.updateAll(auth.getName(), auth.getUrls(), auth.getGid(), auth.getType());
                return existingAuth;
            }
        }
        authorities.add(auth);
        return auth;
    }

    public void addAuthority(SfaAuthority auth) {
        assert !authorities.contains(auth);
        this.authorities.add(auth);
        fireChange();
    }

    private List<AuthorityListModelListener> changeListeners = new ArrayList<AuthorityListModelListener>();
    public void fireChange() {
        for (AuthorityListModelListener l : changeListeners)
            l.onAuthorityListChanged();
    }
    public void addAuthorityListModelListener(AuthorityListModelListener l) {
        changeListeners.add(l);
    }
    public void removeAuthorityListModelListener(AuthorityListModelListener l){
        changeListeners.remove(l);
    }
}
