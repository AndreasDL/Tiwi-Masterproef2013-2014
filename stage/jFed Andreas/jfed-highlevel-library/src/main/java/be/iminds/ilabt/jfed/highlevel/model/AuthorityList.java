package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.BuiltinAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyAuthorityList
 *
 * List of EasyAuthorityInfo objects
 *
 * listeners can be registered to listen for updates
 */
public class AuthorityList implements AuthorityListModel.AuthorityListModelListener {
    private ListProperty<AuthorityInfo> infos;
    private Map<String, AuthorityInfo> infosByUrn;

    private AuthorityListModel authorityListModel;
    private final EasyModel easyModel;

    /** constructor is limited to package, use EasyModel to get the EasyAuthorityList */
    AuthorityList(EasyModel easyModel, AuthorityListModel authorityListModel) {
        this.easyModel = easyModel;
        this.authorityListModel = authorityListModel;
        authorityListModel.addAuthorityListModelListener(this);

        infos = new SimpleListProperty(FXCollections.observableArrayList());
        infosByUrn = new HashMap<String, AuthorityInfo>();
        onAuthorityListChanged();
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    public void add(AuthorityInfo easyAuthorityInfo) {
        infos.add(easyAuthorityInfo);
        infosByUrn.put(easyAuthorityInfo.getGeniAuthority().getUrn(), easyAuthorityInfo);
    }


    public AuthorityInfo add(String urn) {
        SfaAuthority sfaAuthority = null;
        try {
            sfaAuthority = new SfaAuthority(urn);
        } catch (GeniException e) {
            System.err.println("Error creating SfaAuthority: "+e.getMessage());
            return null;
        }
        authorityListModel.addAuthority(sfaAuthority);

        AuthorityInfo easyAuthorityInfo = get(sfaAuthority);
        return easyAuthorityInfo;
    }

    public AuthorityInfo add(SfaAuthority newAuth) {
        assert authorityListModel.getByUrn(newAuth.getUrn()) == null;
        authorityListModel.addAuthority(newAuth);

        AuthorityInfo easyAuthorityInfo = get(newAuth);
        return easyAuthorityInfo;
    }

    /** get (and add if non existing) */
    public AuthorityInfo get(SfaAuthority geniAuthority) {
        AuthorityInfo existing = get(geniAuthority.getUrn());

        if (existing == null) {
            existing = new AuthorityInfo(easyModel, geniAuthority, infos.size());
            add(existing);
        }

        return existing;
    }


    public AuthorityListModel getAuthorityListModel() {
        return authorityListModel;
    }

    /** get by urn (return null if non existing) */
    public AuthorityInfo get(String urn) {
        return infosByUrn.get(urn);
    }

    public AuthorityInfo getFromAnyUrn(String urn) {
        SfaAuthority authority = authorityListModel.getFromAnyUrn(urn);
        if (authority == null) return null;
        return get(authority);
    }


    public ReadOnlyListProperty<AuthorityInfo> authorityInfosProperty() {
        return infos;
    }


    @Override
    public void onAuthorityListChanged() {
        //check if internal mapping is ok (could have been removes, updates and adds)
        //TODO this is not a fast operation. But normally, it should not happen too often or on large sets
        Map<String, AuthorityInfo> copy = new HashMap<String, AuthorityInfo>(infosByUrn);

        for (SfaAuthority sfaAuthority : authorityListModel.getAuthorities()) {
            copy.remove(sfaAuthority.getUrn());

            AuthorityInfo ai = get(sfaAuthority);
            assert ai != null;
        }

        for (AuthorityInfo aiToBeRemoved : copy.values()) {
            infosByUrn.remove(aiToBeRemoved.getGeniAuthority().getUrn());
            infos.remove(aiToBeRemoved);
        }

        assert authorityListModel.getAuthorities().size() == infos.size() :
                "authorityListModel.getAuthorities().size()"+authorityListModel.getAuthorities().size()+
                        " == infos.size()="+infos.size();
        assert infosByUrn.size() == infos.size() :
                "infosByUrn.size()"+infosByUrn.size()+
                        " == infos.size()"+infos.size();
    }

    public void save() {
        for (AuthorityInfo auth : infos) {
            //assume uncommitted changes need to be saved
            boolean committed = auth.committedProperty().get();
            if (!committed) {
                auth.commit();
            }
            assert auth.committedProperty().get();
            assert auth.isSame();
        }

        JFedAuthorityList.getInstance().save();

        //mark all as saved
        for (AuthorityInfo auth : infos) {
           auth.markSaved();
        }
    }

    public void resetToInternalDefaults() {
        AuthorityListModel tmpAuthorityListModel = new AuthorityListModel();
        BuiltinAuthorityList.load(tmpAuthorityListModel);

        List<String> urnsToRemove = new ArrayList<String>();
        urnsToRemove.addAll(infosByUrn.keySet());

        for (SfaAuthority auth : tmpAuthorityListModel.getAuthorities()) {
            AuthorityInfo ai = get(auth.getUrn());
            if (ai != null) {
                ai.setTo(auth);
                urnsToRemove.remove(auth.getUrn());
            }
        }

        for (String urn : urnsToRemove) {
            AuthorityInfo ai = get(urn);
            assert ai != null;
            ai.delete();
        }
    }

    public void load() {
        JFedAuthorityList.getInstance().load();

        //mark as saved, since they are stored in current state
        for (AuthorityInfo auth : infos) {
           auth.markSaved();
        }
    }
}
