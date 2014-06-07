package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.Gid;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import be.iminds.ilabt.jfed.util.GeniUrn;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Slice
 *
 * Example slice Urn:  "urn:publicid:IDN+"+authority.getNameForUrn()+"+slice+"+sliceName;
 */
public class Slice {
    private final StringProperty urn = new SimpleStringProperty();

    private final StringProperty auth = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty uuid = new SimpleStringProperty();
    private final StringProperty creator_uuid = new SimpleStringProperty();
    private final StringProperty creator_urn = new SimpleStringProperty();

    private final ObjectProperty<Status> status = new SimpleObjectProperty<Status>();

    /** Component managers in manifests. This data is retreived by the Resolve slice call on the Slice Authority */
    private final ListProperty<String> manifestComponentManagers = new SimpleListProperty(FXCollections.observableArrayList());

    /**
     * Component managers that can be added to the request RSpec. This list is user controller: entries may be added and removed by the user.
     * It is a good practice to keep at least the ComponentManagers in requestComponentManagers and manifestComponentManagers in this list.
     **/
    private final ListProperty<String> editorComponentManagers = new SimpleListProperty(FXCollections.observableArrayList());

    /**
     * Component managers used in the actual Rspec request.
     * The list should  be automatically updated if the user manually specifies component URN's in the RSpec details of a component.
     * The list should be updated if the user adds a node that is assigned to a component manager already
     * */
    private final ListProperty<String> requestComponentManagers = new SimpleListProperty(FXCollections.observableArrayList());

    private final ObjectProperty<Rspec> requestRspec = new SimpleObjectProperty<Rspec>();

    //manifest Rspec is altually a join af manifest Rspec from multiple authorities/slivers!
    private final ObjectProperty<Rspec> manifestRspec = new SimpleObjectProperty<Rspec>();

    private final ObjectProperty<Gid> gid = new SimpleObjectProperty<Gid>();
    private final ObjectProperty<Date> expirationDate = new SimpleObjectProperty<Date>();
    private final ObjectProperty<GeniCredential> credential = new SimpleObjectProperty<GeniCredential>();

    private ListProperty<Sliver> slivers = new SimpleListProperty(FXCollections.observableArrayList());
    private EasyModel easyModel;


    final ChangeListener<Status> statusChangeListener = new ChangeListener<Status>() {
        @Override public void changed(ObservableValue<? extends Status> observableValue, Status oldStatus, Status newStatus) {
//            System.out.println("DEBUG: Slice "+Slice.this+" -> "+getUrn()+" sliver status changed");
            updateStatus();
        }
    };

    final ChangeListener<RSpecInfo> manifestRspecChangeListener = new ChangeListener<RSpecInfo>() {
        @Override public void changed(ObservableValue<? extends RSpecInfo> observableValue, RSpecInfo oldRSpecInfo, RSpecInfo newRSpecInfo) {
//            System.out.println("DEBUG: Slice "+Slice.this+" -> "+getUrn()+" sliver manifest Rspec changed");

            joinManifestRspecs();
        }
    };

    final ListChangeListener<Sliver> sliverListChangeListener = new ListChangeListener<Sliver>() {
        @Override
        public void onChanged(Change<? extends Sliver> change) {
//            System.out.println("DEBUG: slice "+Slice.this+" -> "+getUrn()+" detected that sliver list changed. list has now "+slivers.size()+" entries");
            while (change.next()) {
                for (Sliver sliver : change.getRemoved()) {
//                    System.out.println("DEBUG:    removing slice listeners to sliver "+sliver+" -> "+sliver.getUrn()+" @ "+sliver.getAuthority().getUrn());
                    sliver.statusProperty().removeListener(statusChangeListener);
                    sliver.manifestRspecProperty().removeListener(manifestRspecChangeListener);

                    manifestComponentManagers.remove(sliver.getAuthority().getUrn());
                }
                for (Sliver sliver : change.getAddedSubList()) {
//                    System.out.println("DEBUG:    adding slice listeners to sliver "+sliver+" -> "+sliver.getUrn()+" @ "+sliver.getAuthority().getUrn());
                    sliver.statusProperty().addListener(statusChangeListener);
                    sliver.manifestRspecProperty().addListener(manifestRspecChangeListener);

                    manifestComponentManagers.add(sliver.getAuthority().getUrn());
                }
            }
            updateStatus();
            joinManifestRspecs();
        }
    };

    /** constructor is limited to package, use EasyModel to get Slices */
    Slice(String urn, EasyModel easyModel) {
        this.easyModel = easyModel;
        this.urn.setValue(urn);
        GeniUrn geniUrn = GeniUrn.parse(urn);
        if (geniUrn != null) {
            this.auth.setValue(geniUrn.getTopLevelAuthority());
            this.name.setValue(geniUrn.getResourceName());
        } else {
            System.err.println("Urn is not valid: \"" + urn + "\"");
        }

        //automatically listen to sliver all status
        slivers.addListener(sliverListChangeListener);

        this.credential.addListener(new ChangeListener<GeniCredential>() {
            @Override
            public void changed(ObservableValue<? extends GeniCredential> observableValue, GeniCredential oldGeniCredential, GeniCredential newGeniCredential1) {
                Slice.this.easyModel.getParameterHistoryModel().addSliceCredential(new CredentialInfo(newGeniCredential1));
            }
        });
    }
    /** constructor is limited to package, use EasyModel to get Slices */
    Slice(String urn, GeniCredential credential, EasyModel easyModel) {
        this(urn, easyModel);
        this.credential.set(credential);
    }



    //helpers
    private static boolean updateStatusHelper_takesPriorityOver(Status status1, Status status2) {
        return updateStatusHelper_priority(status1) > updateStatusHelper_priority(status2);
    }
    private static int updateStatusHelper_priority(Status status) {
//                    order of priority of states when aggregating them
//                  0  UNINITIALISED, /* nothing known about status*/
//                  1      READY,
//                  2      UNALLOCATED,   /* known not to exist */
//                  3      UNKNOWN,       /* known to exist, but no status known. probably it is changing. */
//                  4      CHANGING,
//                  5      FAIL;

        //returns priority of a status
        switch (status) {
            case UNINITIALISED: return 0;
            case READY: return 1;
            case UNALLOCATED: return 2;
            case UNKNOWN: return 3;
            case CHANGING: return 4;
            case FAIL: return 5;
            default: throw new RuntimeException("BUG: updateStatusHelper_priority Unhandled status: "+status);
        }
    }




    private void updateStatus() {
        //do not use list of slivers if a manifest is present: use list of auth's in manifest in that case, and fail if any is unallocated

//        System.out.println("DEBUG: slice.updateStatus() called");
        //create aggregated status
        Status newStatus = Status.UNINITIALISED;
        if (slivers.isEmpty()) {
            newStatus = Status.UNALLOCATED;
            status.set(newStatus);
            return;
        }

        //use any rspec found as base. Prefer request rspec, then joined manifest, and finally any individual manifest
        //note that some AM's return only their nodes in the manfiest
        Rspec anyRspec = null;
        if (anyRspec == null)
            for (Sliver sliver : slivers)
                if (sliver.getRequestRspec() != null)
                    anyRspec = sliver.getRequestRspec().getRSpec();
        if (anyRspec == null && manifestRspec.get() != null)
            anyRspec = manifestRspec.get();
        if (anyRspec == null)
            for (Sliver sliver : slivers)
                if (sliver.getManifestRspec() != null)
                    anyRspec = sliver.getManifestRspec().getRSpec();

        boolean statusUpToDate = false;
        if (anyRspec == null) {
            statusUpToDate = false;
        } else {
            statusUpToDate = true;


            //special case: consider UNALLOCATED as FAIL, unless all slivers are unallocated or uninitialised
            boolean allUnallocated = true;
            for (String cmUrn : anyRspec.getAllComponentManagerUrns()) {
                AuthorityInfo ai = easyModel.getAuthorityList().get(cmUrn);
                if (ai == null)
                    continue;
                SfaAuthority rspecAuth = ai.getGeniAuthority();
                assert rspecAuth != null;
                List<Sliver> sliversForAuth = findSlivers(rspecAuth);
                for (Sliver sliver : sliversForAuth) {
                    Status sliverStatus = sliver.getStatus();
                    if (sliverStatus != Status.UNALLOCATED && sliverStatus != Status.UNINITIALISED)
                        allUnallocated = false;
                }

            }

            for (String cmUrn : anyRspec.getAllComponentManagerUrns()) {
                AuthorityInfo ai = easyModel.getAuthorityList().get(cmUrn);
                if (ai == null) {
                    System.err.println("rspec manifest contained cm urn \""+cmUrn+"\". But that urn is unknown!");
                    statusUpToDate = false; //will cause other slivers to be taken into account AS WELL
                } else {
                    SfaAuthority rspecAuth = ai.getGeniAuthority();
                    assert rspecAuth != null;
                    List<Sliver> sliversForAuth = findSlivers(rspecAuth);

                    if (sliversForAuth.isEmpty())
                    {
                        //this is equal to state "UNALLOCATED"  (note: it will probably never occur)
                        Status sliverStatus;
                        if (allUnallocated)
                            sliverStatus = Status.UNALLOCATED;
                        else
                            sliverStatus = Status.FAIL;

                        if (updateStatusHelper_takesPriorityOver(sliverStatus, newStatus))
                            newStatus = sliverStatus;
                    }

                    for (Sliver sliver : sliversForAuth) {
                        Status sliverStatus = sliver.getStatus();
                        if (sliverStatus == Status.UNALLOCATED && !allUnallocated)
                            sliverStatus = Status.FAIL;

                        if (updateStatusHelper_takesPriorityOver(sliverStatus, newStatus))
                            newStatus = sliverStatus;
                    }
                }
            }
        }

        if (!statusUpToDate)
            for (Sliver sliver : slivers) {
                //UNALLOCATED is not FAIL here, as slivers that have nothing to do with manifest may be included
                Status sliverStatus = sliver.getStatus();
                if (updateStatusHelper_takesPriorityOver(sliverStatus, newStatus))
                    newStatus = sliverStatus;
            }

//        System.out.println("DEBUG: slice.updateStatus()   aggregated status = "+newStatus);
        status.set(newStatus);
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }


    public String getName() {
        return name.get();
    }
    public StringProperty nameProperty() {
        return name;
    }

    public String getAuthorityName() {
        return auth.get();
    }
    public StringProperty authorityProperty() {
        return auth;
    }

    public String getUrn() {
        return urn.get();
    }
    public StringProperty urnProperty() {
        return urn;
    }

    public boolean hasCredential() {
        return credential != null;
    }
    public GeniCredential getCredential() {
        return credential.get();
    }

    public void setCredential(GeniCredential credential) {
//        GeniCredential orig = this.credential;
        this.credential.set(credential);
//        if (!orig.equals(this.credential))
//            fireSliceChanged();
    }
    public ObjectProperty<GeniCredential> credentialProperty() {
        return credential;
    }

    public String getUuid() {
        return uuid.get();
    }
    public StringProperty uuidProperty() {
        return uuid;
    }


    public String getCreator_uuid() {
        return creator_uuid.get();
    }
    public StringProperty creator_uuidProperty() {
        return creator_uuid;
    }

    public String getCreator_urn() {
        return creator_urn.get();
    }
    public StringProperty creator_urnProperty() {
        return creator_urn;
    }

    public Gid getGid() {
        return gid.get();
    }
    public void setGid(Gid gid) {
        this.gid.set(gid);
    }
    public ObjectProperty<Gid> gidProperty() {
        return gid;
    }


    public ObservableList<String> getRequestComponentManagers() {
        return requestComponentManagers.get();
    }
    public ListProperty<String> requestComponentManagersProperty() {
        return requestComponentManagers;
    }
    public ObservableList<String> getEditorComponentManagers() {
        return editorComponentManagers.get();
    }
    public ListProperty<String> editorComponentManagersProperty() {
        return editorComponentManagers;
    }
    public ObservableList<String> getManifestComponentManagers() {
        return manifestComponentManagers.get();
    }
    public ListProperty<String> manifestComponentManagersProperty() {
        return manifestComponentManagers;
    }



    public Status getStatus() {
        return status.get();
    }

    public void setStatus(Status status) {
        this.status.set(status);
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }



    public ObjectProperty<Rspec> requestRspecProperty() {
        return requestRspec;
    }
    public ObjectProperty<Rspec> manifestRspecProperty() {
        return manifestRspec;
    }

    private void joinManifestRspecs() {
        //always need to recreate the joined RSpec, as parts may have changed, or been deleted
        Rspec joined = null;

        for (Sliver sliver : slivers) {
            //use request as base manifest for merging (since real manifests might not contain nodes from all cm's)
            if (joined == null && sliver.getRequestRspec() != null)
                joined =  Rspec.fromGeni3ManifestRspecXML(sliver.getRequestRspec().getStringContent());
        }

        for (Sliver sliver : slivers) {
            SfaAuthority auth = sliver.getAuthority();
            RSpecInfo sliverManifestRspec = sliver.getManifestRspec();
            if (sliverManifestRspec != null) {
                if (joined == null) {
                    assert sliverManifestRspec.getStringContent() != null;
                    joined = Rspec.fromGeni3ManifestRspecXML(sliverManifestRspec.getStringContent());
                } else
                    joinManifestRspecs(joined, auth, sliverManifestRspec);
            }
        }

        manifestRspec.set(joined);
    }
    private void joinManifestRspecs(Rspec mergedRspec, SfaAuthority auth, RSpecInfo manifestRspec) {
//        System.out.println("DEBUG       joinManifestRspecs() from "+auth.getName());
        Rspec rspec = manifestRspec.getRSpec();
        for (RspecNode node : rspec.getNodes())
            if (node.getComponentManagerId().equals(auth.getUrn())) {
                RspecNode existingNode = mergedRspec.getNodeById(node.getId());
                if (existingNode == null) {
                    existingNode = new RspecNode(node.getId());
                    mergedRspec.getNodes().add(existingNode);
                }
                assert node.getXmlManifestNodeContents() != null;
                existingNode.setPropertiesFromGeni3ManifestRspec(node.getXmlManifestNodeContents());
            }
        for (RspecLink link : rspec.getLinks())
            if (link.getComponentManagerUrns().contains(auth.getUrn())) { //TODO this overwrites, because 2 component managers will have the link if they share 1!
                RspecLink existingLink = mergedRspec.getLinkById(link.getId());
                if (existingLink == null) {
                    existingLink = new RspecLink(mergedRspec, link.getId());
                    mergedRspec.getLinks().add(existingLink);
                }
                assert link.getXmlManifestLinkContents() != null;
                existingLink.setPropertiesFromGeni3ManifestRspec(link.getXmlManifestLinkContents());
            }
    }

//    @Override
//    public String toString() {
//        return "Slice{" +
//                "name='" + name + '\'' +
//                ", urn='" + urn + '\'' +
//                ", uuid='" + uuid + '\'' +
//                ", creator_uuid='" + creator_uuid + '\'' +
//                ", creator_urn='" + creator_urn + '\'' +
//                ", gid='" + gid + '\'' +
//                ", requestComponentManagers='" + requestComponentManagers + '\'' +
//                ", editorComponentManagers='" + editorComponentManagers + '\'' +
//                ", manifestComponentManagers='" + manifestComponentManagers + '\'' +
//                ", credential=" + credential +
//                '}';
//    }

    public void addSliver(Sliver sliver) {
        slivers.add(sliver);
//        fireSliceChanged();
    }

    public void removeSliver(Sliver sliver) {
        //clear the status of a removed sliver
        sliver.setStatusString("sliver removed");
        sliver.setStatus(Status.UNALLOCATED);
        sliver.setManifestRspec(null);
        slivers.remove(sliver);
//        fireSliceChanged();
    }

    public ReadOnlyListProperty<Sliver> getSlivers() {
        return slivers;
    }

    public List<Sliver> findSlivers(SfaAuthority auth) {
        List<Sliver> res = new ArrayList<Sliver>();
        for (Sliver sliver : slivers)
            if (sliver.getAuthority() != null && sliver.getAuthority().equals(auth))
                res.add(sliver);
        return res;
    }

    public Sliver findSliver(String sliverUrn) {
        for (Sliver sliver : slivers)
            if (sliver.getUrn() != null) {
                if (sliver.getUrn().equals(sliverUrn))
                    return sliver;
            }
            else {
                //TODO this is a hack that works for AMv2.
                //    Problem is that the sliver URN is not known after the createSliver command in AMv2.
                //    For AMv3, this should not be needed as the sliver urn is known quickly I think
                //         it will cause bugs for AMv3 if multiple slivers for the same slice are on an AM (which is not supported in AMv2)
                String urnStart = "urn:publicid:IDN+"+sliver.getAuthority().getNameForUrn()+"+sliver+";
                if (sliverUrn.startsWith(urnStart)) {
                    sliver.setUrn(sliverUrn);
                    return sliver;
                }

            }
        return null;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate.set(expirationDate);
    }
    public Date getExpirationDate() {
        return expirationDate.get();
    }
    public ObjectProperty<Date> expirationDateProperty() {
        return expirationDate;
    }
}
