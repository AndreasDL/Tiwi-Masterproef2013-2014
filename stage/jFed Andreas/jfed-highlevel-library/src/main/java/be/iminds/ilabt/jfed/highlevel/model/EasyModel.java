package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.highlevel.controller.HighLevelController;
import be.iminds.ilabt.jfed.highlevel.history.ApiCallHistory;
import be.iminds.ilabt.jfed.history.UserInfo;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.*;

/**
 * EXPERIMENTAL
 *
 * EasyModel is passive, it just listens to replies from the SA and AM servers, and offers access to data from them.
 * It caches and informs listeners
 *
 * It also offers access to the connection pool, and the logged in user
 * It also has methods that will schedule and execute requests. Note that all updates caused by this execution is still
 *   received by the passive monitoring of easymodel.
 */
public class EasyModel /*implements SliceListener*/ {
    private JavaFXLogger logger;
    private GeniUserProvider geniUserProvider;
    private HighLevelController highLevelController;


    public EasyModel(JavaFXLogger logger,
                     AuthorityListModel authorityListModel,
                     GeniUserProvider geniUserProvider /*may be null*/) {
        this.logger = logger;
        this.authorityList = new AuthorityList(this, authorityListModel);
        this.geniUserProvider = geniUserProvider;
        this.apiCallHistory = new ApiCallHistory(logger);

        EasyModelAbstractListener sliceAuthorityListener = new EasyModelSliceAuthorityListener(this);
        logger.addResultListener(sliceAuthorityListener);
        EasyModelAggregateManager3Listener am3Listener = new EasyModelAggregateManager3Listener(this);
        logger.addResultListener(am3Listener);
        EasyModelAggregateManager2Listener am2Listener = new EasyModelAggregateManager2Listener(this);
        logger.addResultListener(am2Listener);

        this.slicesByUrn = new HashMap<String, Slice>();
        this.userCredential = null;
        this.userInfo = null;
        this.userKeys = null;
    }

    public JavaFXLogger getLogger() {
        return logger;
    }

    public GeniUserProvider getGeniUserProvider() {
        return geniUserProvider;
    }

    public void setHighLevelController(HighLevelController highLevelController) {
        this.highLevelController = highLevelController;
    }
    public HighLevelController getHighLevelController() {
        return highLevelController;
    }

    /** forget all state. (state will come back by listening to Results) */
    public void reset() {
        slices.clear();
        slicesByUrn.clear();
        userCredential = null;
        userInfo = null;
        userKeys = null;
//        fireAllChanged();
    }



    //////////////////////////////////////////////////////////////////
    ////////////////////// EasyAuthorityList /////////////////////////
    //////////////////////////////////////////////////////////////////
    private AuthorityList authorityList;

    public AuthorityList getAuthorityList() {
        return authorityList;
    }


    //////////////////////////////////////////////////////////////////
    ////////////////////// rspecList /////////////////////////
    //////////////////////////////////////////////////////////////////
    private RSpecList rspecList;

    public RSpecList getRSpecList() {
        return rspecList;
    }

    //////////////////////////////////////////////////////////////////
    /////////////////////////// Slice info ///////////////////////////
    //////////////////////////////////////////////////////////////////
//    private List<Slice> slices;
    private final ListProperty<Slice> slices = new SimpleListProperty(FXCollections.observableArrayList());
    private final ReadOnlyListProperty<Slice> slicesReadOnly = slices;
    private Map<String, Slice> slicesByUrn;

    public ReadOnlyListProperty<Slice> slicesProperty() {
        return slicesReadOnly;
    }

    void logNotExistSlice(String sliceUrn) {
        Slice existingSlice = slicesByUrn.get(sliceUrn);
        if (existingSlice != null) {
            //TODO: remove all slivers
            slices.remove(existingSlice);
            slicesByUrn.remove(existingSlice.getUrn());
//            System.out.println("EasyModel logNotExistSlice removed Slice "+existingSlice+" with urn "+existingSlice.getUrn());
//            fireSliceRemoved(existingSlice);
        }
    }
    Slice logExistSlice(String sliceUrn) {
        assert sliceUrn.startsWith("urn:publicid:IDN");
        Slice existingSlice = slicesByUrn.get(sliceUrn);
        if (existingSlice == null) {
            Slice newSlice = new Slice(sliceUrn, this);
            parameterHistoryModel.addSliceUrn(sliceUrn);
            slices.add(newSlice);
            slicesByUrn.put(sliceUrn, newSlice);
//            System.out.println("EasyModel logExistSlice created Slice "+newSlice+" for urn "+sliceUrn);
//            fireSliceAdded(newSlice);
            return newSlice;
        }
        return existingSlice;
    }
    Slice logExistSliceName(SfaAuthority auth, String sliceName) {
        assert !sliceName.startsWith("urn:publicid:IDN") : "sliceName is urn \""+sliceName+"\"";
        String sliceUrn = "urn:publicid:IDN+" + auth.getNameForUrn() + "+slice+" + sliceName;
        return logExistSlice(sliceUrn);
    }
    public Slice logExistSliceUrn(SfaAuthority auth, String sliceUrn) {
        assert auth != null;
        assert sliceUrn.startsWith("urn:publicid:IDN") : "sliceUrn is not urn \""+sliceUrn+"\"";
        assert sliceUrn.startsWith("urn:publicid:IDN+"+auth.getNameForUrn()+"+slice+") :
                "sliceUrn is not for the correct authority: urn \""+sliceUrn+"\" auth: \""+auth.getUrn()+"\"";
        return logExistSlice(sliceUrn);
    }

    /**
     * @param sliceUrn urn of the slice the sliver belongs to, may be null if unknown
     * @param sliverUrn urn of the silver
     * */
    public void logNotExistSliver(String sliceUrn, String sliverUrn) {
        Slice slice = logExistSlice(sliceUrn);
        assert slice != null;
        Sliver sliver = slice.findSliver(sliverUrn);
        if (sliver == null) return;

        sliver.setStatus(Status.UNALLOCATED);
        sliver.setStatusString("non existing");
        sliver.setManifestRspec(null);

        //Do not remove! Keep list of slivers, but mark as unallocated
//        slice.removeSliver(sliver);
    }


    /**
     * @param sliceUrn urn of the slice the sliver belongs to, may be null if unknown
     * @param sliverUrn urn of the silver
     * @param geniSingle if true, then an existing sliver for the same authority will be considered the same if its urn is null
     *
     * @return the sliver or null if not stored
     * */
    public Sliver logExistSliver(String sliceUrn, String sliverUrn, boolean geniSingle) {
        assert sliverUrn.contains("+sliver+") : "sliverUrn=\""+sliverUrn+"\" is not a sliver urn (sliceUrn="+sliceUrn+")";
        assert sliceUrn.contains("+slice+") : "sliceUrn=\""+sliceUrn+"\" is not a slice urn (sliverUrn="+sliverUrn+")";

        Slice slice = logExistSlice(sliceUrn);
        assert slice != null;
        Sliver sliver = slice.findSliver(sliverUrn);
        if (sliver == null) {
//            System.out.println("EasyModel logExistSliver did not find sliver by urn "+sliverUrn);
            AuthorityInfo auth = getAuthorityList().getFromAnyUrn(sliverUrn);
            assert auth != null : "Error, got sliver info for sliver of unknown authority: "+sliverUrn;
            System.out.println("DEBUG:     sliver with urn \""+sliceUrn+"\" is from authority "+auth.getGeniAuthority().getUrn());

            //check if sliver for that urn already exists
            if (geniSingle) {
                List<Sliver> slivers = slice.findSlivers(auth.getGeniAuthority());
                if (slivers.isEmpty()) {
//                    System.out.println("EasyModel logExistSliver with geniSingle did not find existing sliver for same authority, so it created sliver for urn "+sliverUrn);
                    sliver = new Sliver(sliverUrn, slice, null, null, auth.getGeniAuthority());
                    parameterHistoryModel.addSliverUrn(sliverUrn);
                    slice.addSliver(sliver);
                } else {
//                    System.out.println("EasyModel logExistSliver with geniSingle found existing sliver for the same authority. So it will fill in the urn");
                    assert slivers.size() == 1;
                    sliver = slivers.get(0);
                    assert sliver.getUrn() == null : "Found a sliver for the same authority, with a different urn: "+sliver.getUrn()+" != "+sliverUrn;
                    sliver.setUrn(sliverUrn);
                }
            } else {
//                System.out.println("EasyModel logExistSliver without geniSingle did not find sliver by urn "+sliverUrn+" so it will create a sliver");
                sliver = new Sliver(sliverUrn, slice, null, null, auth.getGeniAuthority());
                parameterHistoryModel.addSliverUrn(sliverUrn);
                slice.addSliver(sliver);
            }
        } else {
//            System.out.println("EasyModel logExistSliver found sliver "+sliver+" by urn "+sliverUrn);
        }
        return sliver;
    }

    /**
     * @param sliceUrn urn of the slice the sliver belongs to, may be null if unknown
     * @param auth the authority of the sliver
     *
     * There exists a sliver for the slice, on the AM which has geni_allocate value of geni_single. See specs:
     *    geni_allocate: A case insensitive string, one of fixed set of possible values. Default is geni_single.
     *             This option defines whether this AM allows adding slivers to slices at an AM (i.e. calling
     *             Allocate() multiple times, without first deleting the allocated slivers). Possible values:
     *                  geni_single: Performing multiple Allocates without a delete is an error condition because
     *                  the aggregate only supports a single sliver per slice or does not allow incrementally adding
     *                  new slivers. This is the AM API v2 behavior.
     *
     * @return the sliver or null if not stored
     * */
    public Sliver logExistSliverGeniSingle(String sliceUrn, SfaAuthority auth) {
        Slice slice = logExistSlice(sliceUrn);
        assert slice != null;
        List<Sliver> slivers = slice.findSlivers(auth);
        if (slivers.isEmpty()) {
            Sliver sliver = new Sliver(null, slice, null, null, auth);
            slice.addSliver(sliver);
//            System.out.println("EasyModel logExistSliverGeniSingle did not find sliver by authority so it created one");
            return sliver;
        }
        assert slivers.size() == 1;
//        System.out.println("EasyModel logExistSliverGeniSingle found sliver "+slivers.get(0)+" by authority");
        return slivers.get(0);
    }

    public Sliver logExistSliver(String sliceUrn, SfaAuthority auth) {
        System.err.println("WARNING: assuming AMv2 or geni_single! logExistSliver("+sliceUrn+", "+auth.getName()+")");
        return logExistSliverGeniSingle(sliceUrn, auth);
    }

    /**
     * @param sliceUrn urn of the slice the sliver belongs to, may be null if unknown
     * @param auth the authority of the sliver
     *
     *  See logExistSliverGeniSingle for detqails about "geni_single"
     *
     * @return the sliver or null if not stored
     * */
    public void logNotExistSliverGeniSingle(String sliceUrn, SfaAuthority auth) {
        Slice slice = logExistSlice(sliceUrn);
        assert slice != null;
        List<Sliver> slivers = slice.findSlivers(auth);
        if (slivers.isEmpty()) return;
        assert slivers.size() == 1;
        Sliver sliver = slivers.get(0);

        sliver.setStatus(Status.UNALLOCATED);
        sliver.setStatusString("non existing");
        sliver.setManifestRspec(null);

        //Do not remove! Keep list of slivers, but mark as unallocated
        //slice.removeSliver(sliver);
    }


    public Slice getSlice(String sliceUrn) {
        return slicesByUrn.get(sliceUrn);
    }

    public List<Slice> getSlices() {
        return Collections.unmodifiableList(slices);
    }



    public Sliver getSliver(String sliverUrn) {
        for (Slice slice : slicesByUrn.values()) {
            Sliver res = slice.findSliver(sliverUrn);
            if (res != null) return res;
        }
        return null;
    }


    //////////////////////////////////////////////////////////////////
    /////////////////////////// User info ////////////////////////////
    //////////////////////////////////////////////////////////////////
    private GeniCredential userCredential;
    private UserInfo userInfo;
    private List<String> userKeys;

    public void setUserCredential(GeniCredential userCredential) {
        parameterHistoryModel.addUserCredential(new CredentialInfo(userCredential));
        this.userCredential = userCredential;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setUserKeys(List<String> keys) {
        this.userKeys = keys;
    }

    private UserInfo getUserInfo() {
        return userInfo;
    }

    public List<String> getUserKeys() {
        return userKeys;
    }

    public GeniCredential getUserCredential() {
        return userCredential;
    }




    //////////////////////////////////////////////////////////////////
    /////////////////////// ParameterHistoryModel ////////////////////
    //////////////////////////////////////////////////////////////////

    private ParameterHistoryModel parameterHistoryModel = new ParameterHistoryModel();
    public ParameterHistoryModel getParameterHistoryModel() {
        return parameterHistoryModel;
    }

    //////////////////////////////////////////////////////////////////
    ////////////////////////// ApiCallHistory ////////////////////////
    //////////////////////////////////////////////////////////////////

    private ApiCallHistory apiCallHistory;
    public ApiCallHistory getApiCallHistory() {
        return apiCallHistory;
    }
}
