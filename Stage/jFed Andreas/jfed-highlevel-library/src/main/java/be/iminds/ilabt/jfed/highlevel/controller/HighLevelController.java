package be.iminds.ilabt.jfed.highlevel.controller;

import be.iminds.ilabt.jfed.highlevel.api.EasyAggregateManager2;
import be.iminds.ilabt.jfed.highlevel.api.EasySliceAuthority;
import be.iminds.ilabt.jfed.highlevel.model.*;
import be.iminds.ilabt.jfed.highlevel.stitcher.ParallelStitcher;
import be.iminds.ilabt.jfed.history.UserInfo;
import be.iminds.ilabt.jfed.lowlevel.GeniConnectionProvider;
import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.stitching.StitchingDirector;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import javafx.application.Platform;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HighLevelController
 *
 * The highlevelController provides access to various typical gui tasks with simple calls.
 * It will select API's as needed.
 * Dependant calls will be performed if needed, and post completion, timeout or failure operations will be run.
 *
 * It also stores the connectionpool.
 *
 *
 * TODO This is a VERY temporary implementation with a lot of issues. The interface will probably mostly be kept,
 * TODO   but the implementation will need to be updated
 */
public class HighLevelController {
    private EasyModel easyModel;
    private GeniConnectionProvider connectionProvider;

    public HighLevelController(EasyModel easyModel, GeniConnectionProvider connectionProvider) {
        this.easyModel = easyModel;
        this.connectionProvider = connectionProvider;
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    public GeniConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }


    //global call                 name
    //call per slice              name + slice
    //call per slice + auth pair  name + slice + auth

    //===> unique name?


    private Map<Slice, TaskThread.Task> sliceCredentialCalls = new HashMap<Slice, TaskThread.Task>();
    public TaskThread.Task getSliceCredential(final Slice slice) {
        TaskThread.Task call  = sliceCredentialCalls.get(slice);
        if (call != null) return call;

        call = new TaskThread.Task("Get Slice Credential "+slice.getUrn()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                easySliceAuthority.getCredential(slice);
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getUserCredential());
                return res;
            }

            @Override public boolean needed() { return slice.getCredential() == null; }
        };
        sliceCredentialCalls.put(slice , call);
        return call;
    }


    private Map<Slice, TaskThread.Task> sliceManifestCalls = new HashMap<Slice, TaskThread.Task>();
    public TaskThread.Task getManifest(final Slice slice) {
        TaskThread.Task call  = sliceManifestCalls.get(slice);
        if (call != null) return call;

        call = new TaskThread.Task("Get Slice Manifest") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
//                System.out.println("getManifest cm count: "+slice.manifestComponentManagersProperty().size());
                for (String cm : slice.manifestComponentManagersProperty().get()) {
                    AuthorityInfo ai = easyModel.getAuthorityList().get(cm);
                    if (ai == null) {
                        System.err.println("WARNING: Resolve Slice returned unknown component manager -> ignoring");
                        continue;
                    }
                    SfaAuthority auth = ai.getGeniAuthority();
                    if (auth == null) {
                        System.err.println("WARNING: Resolve Slice returned unknown component manager -> ignoring");
                        continue;
                    }
//                    if (auth.getUrl(ServerType.GeniServerRole.AM, 3) != null) {
//                        EasyAggregateManager3 am3 = new EasyAggregateManager3(easyModel, auth);
//                        am3.describe(slice);
//                    } else {

                    //TODO: see getStatus, here as well we need to block ourself and add these as deps instead of just adding them
                    TaskThread.Task subCall = getSliceManifest(slice, auth);
                    TaskThread.getInstance().addTask(subCall);

//                    if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
//                        EasyAggregateManager2 am2 = new EasyAggregateManager2(easyModel, auth);
//                        am2.listSliceResources(slice);
//                    }


//                    }
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
                return res;
            }
            @Override
            public List<TaskThread.Task> initAlwaysDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(resolveSlice(slice));
                return res;
            }
        };
        sliceManifestCalls.put(slice, call);
        return call;
    }

    private Map<Sliver, TaskThread.Task> sliverManifestCalls = new HashMap<Sliver, TaskThread.Task>();
    public TaskThread.Task getManifest(final Sliver sliver) {
        TaskThread.Task call  = sliverManifestCalls.get(sliver);
        if (call != null) return call;

        call = new TaskThread.Task("Get Sliver Manifest") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = sliver.getAuthority();
                assert auth != null;

                if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                    EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                    am2.listSliceResources(sliver.getSlice());
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(sliver.getSlice()));
                return res;
            }
        };
        sliverManifestCalls.put(sliver, call);
        return call;
    }

    private Map<Sliver, TaskThread.Task> sliverStatusCalls = new HashMap<Sliver, TaskThread.Task>();
    public TaskThread.Task getSliverStatus(final Sliver sliver) {
        TaskThread.Task call  = sliverStatusCalls.get(sliver);
        if (call != null) return call;

        call = new TaskThread.Task("Get Sliver Status @ "+sliver.getAuthority().getUrn()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = sliver.getAuthority();
                assert auth != null;

                if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                    EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                    am2.sliverStatus(sliver.getSlice());
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(sliver.getSlice()));
                return res;
            }
        };
        sliverStatusCalls.put(sliver, call);
        return call;
    }

    private Map<Pair<Slice, SfaAuthority>, TaskThread.Task> sliceStatusOnAuthCalls = new HashMap<Pair<Slice, SfaAuthority>, TaskThread.Task>();
    /** get a Status for a specific AM. We don't assume there is a sliver, as getSliverManifest does */
    public TaskThread.Task getSliceStatus(final Slice slice, final SfaAuthority auth) {
        assert auth != null;
        Pair<Slice, SfaAuthority> pair = new Pair<Slice, SfaAuthority>(slice, auth);
        TaskThread.Task call  = sliceStatusOnAuthCalls.get(pair);
        if (call != null) return call;

        TaskThread.Task newCall = new TaskThread.Task("Status of Slice @ "+auth.getName()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                    EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                    am2.sliverStatus(slice);
                } else
                    System.err.println("WARNING: Slice has authority ("+auth.getUrn()+") without AMv2 support. It will be ignored!");
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
                return res;
            }
        };
        sliceStatusOnAuthCalls.put(pair, newCall);
        return newCall;
    }

    private Map<Pair<Slice, SfaAuthority>, TaskThread.Task> sliceManifestOnAuthCalls = new HashMap<Pair<Slice, SfaAuthority>, TaskThread.Task>();
    /** get a manifest for a specific AM. We don't assume there is a sliver, as getSliverManifest does */
    public TaskThread.Task getSliceManifest(final Slice slice, final SfaAuthority auth) {
        assert auth != null;
        Pair<Slice, SfaAuthority> pair = new Pair<Slice, SfaAuthority>(slice, auth);
        TaskThread.Task call  = sliceManifestOnAuthCalls.get(pair);
        if (call != null) return call;


        TaskThread.Task newCall = new TaskThread.Task("get Manifest of Slice @ "+auth.getName()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                    EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                    am2.listSliceResources(slice);
                } else
                    System.err.println("WARNING: Slice has authority ("+auth.getUrn()+") without AMv2 support. It will be ignored!");
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
                return res;
            }
        };
        sliceManifestOnAuthCalls.put(pair, newCall);
        return newCall;
    }

    private Map<Sliver, TaskThread.Task> deleteSliverCalls = new HashMap<Sliver, TaskThread.Task>();
    public TaskThread.Task deleteSliver(final Sliver sliver) {
        TaskThread.Task call  = deleteSliverCalls.get(sliver);
        if (call != null) return call;

        call = new TaskThread.Task("Delete Sliver "+sliver.getUrn()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = sliver.getAuthority();
                assert auth != null;

                if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                    EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                    am2.deleteSliver(sliver.getSlice());
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(sliver.getSlice()));
                return res;
            }
        };
        deleteSliverCalls.put(sliver, call);
        return call;
    }

    private Map<Slice, TaskThread.Task> resolveSliceCalls = new HashMap<Slice, TaskThread.Task>();
    public TaskThread.Task resolveSlice(final Slice slice) {
        TaskThread.Task call  = resolveSliceCalls.get(slice);
        if (call != null) return call;

        call = new TaskThread.Task("Resolve Slice") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                easySliceAuthority.resolveSlice(slice);
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
                return res;
            }

            @Override public boolean needed() { return slice.getManifestComponentManagers().isEmpty(); }; //resolve if no component managers
        };
        resolveSliceCalls.put(slice , call);
        return call;
    }


    private TaskThread.Task userSSHKeyCall = null;
    public TaskThread.Task getUserSSHKey() {
        if (userSSHKeyCall != null) return userSSHKeyCall;

        userSSHKeyCall = new TaskThread.Task("Get User SSH Keys") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                easySliceAuthority.getSshKeys();

            }
            @Override public boolean needed() { return easyModel.getUserKeys() == null || easyModel.getUserKeys().isEmpty(); };

            //no dependencies
        };
        return userSSHKeyCall;
    }


    private TaskThread.Task userCredentialCall = null;
    public TaskThread.Task getUserCredential() {
        if (userCredentialCall != null) return userCredentialCall;

        userCredentialCall = new TaskThread.Task("Get User Credential") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                easySliceAuthority.getCredential();

            }
            @Override public boolean needed() { return easyModel.getUserCredential() == null; };

            //no dependencies
        };
        return userCredentialCall;
    }

    private TaskThread.Task resolveUserCall = null;
    public TaskThread.Task resolveUser() {
        if (resolveUserCall != null) return resolveUserCall;

        resolveUserCall = new TaskThread.Task("Resolve User") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                easySliceAuthority.resolveUser(easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserUrn());
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getUserCredential());
                return res;
            }
        };
        return resolveUserCall;
    }

    private TaskThread.Task resolveUserCallAndSlices = null;
    public TaskThread.Task resolveUserAndSlices() {
        if (resolveUserCallAndSlices != null) return resolveUserCallAndSlices;

        resolveUserCallAndSlices = new TaskThread.Task("Resolve User & Slices") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                UserInfo res = easySliceAuthority.resolveUser(easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserUrn());

                //note: resolveUser call result might not have triggered listener yet, so easymodel might not yet know of it!

                for (final String sliceUrn : res.getSlices()) {
                    //TODO very dirty, fix! (see TODOs below for info)
                    //TODO how to wait for result to be processed so we can request slice resolve? One way is to run this in JavaFX thread,
                    //TODO     it is a dirty method, as it relies on much implementation info.
                    //TODO     a better method would be to have a method that waits for all pending updates in EasyModel or the EasyModelListeners.
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Slice slice = easyModel.getSlice(sliceUrn);
                            TaskThread.Task resolveSliceCall = resolveSlice(slice);

                            //TODO reblock self and add these as dependencies instead
                            //Note: this to do requires that the other is done first
                            TaskThread.getInstance().addTask(resolveSliceCall);
                        }
                    });
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getUserCredential());
                return res;
            }
        };
        return resolveUserCallAndSlices;
    }


    private Map<SfaAuthority, TaskThread.Task> getversionCalls = new HashMap<SfaAuthority, TaskThread.Task>();
    public TaskThread.Task getVersion(final SfaAuthority auth) {
        TaskThread.Task call  = getversionCalls.get(auth);
        if (call != null) return call;

        call = new TaskThread.Task("GetVersion on "+auth.getName()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                am2.getVersion();
            }

            @Override public boolean needed() {
                //TODO: Store cached GetVersion result?
//                AuthorityInfo ai = easyModel.getAuthorityList().get(auth);
//                if (ai.hetCachehedGetVersionResult() != null) return false;
                return super.needed();
            };
        };
        getversionCalls.put(auth, call);
        return call;
    }


    private Map<SfaAuthority, TaskThread.Task> advertisementCalls = new HashMap<SfaAuthority, TaskThread.Task>();
    public TaskThread.Task getAdvertisement(final SfaAuthority auth) {
        TaskThread.Task call  = advertisementCalls.get(auth);
        if (call != null) return call;

        call = new TaskThread.Task("Fetch Advertisement RSpec on "+auth.getName()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                am2.listResources(true /*available*/);
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getUserCredential());
                return res;
            }
        };
        advertisementCalls.put(auth, call);
        return call;
    }

    public TaskThread.Task createSlice(final String sliceName) {
        TaskThread.Task call = new TaskThread.Task("Create Slice") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException, InterruptedException {
                SfaAuthority auth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();

                EasySliceAuthority easySliceAuthority = new EasySliceAuthority(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                easySliceAuthority.register(auth, sliceName);
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getUserCredential());
                return res;
            }
        };
        return call;
    }


    public List<TaskThread.Task> refreshSlices() {
        //resolve user AND get credential for each slice and resolve it
        //possibly also get all manifest/statuses

        //TODO add more?
        List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
        res.add(resolveUserAndSlices());

        //remove this hack  (was used for demo)
//        for (AuthorityInfo ai : easyModel.getAuthorityList().authorityInfosProperty()) {
//            //for ple, we need to be able to assign nodes, so we will get the advertisement manifest
//            if (ai.getGeniAuthority().getType().equals("planetlab"))
//                res.add(getAdvertisement(ai.getGeniAuthority()));
//        }
        return res;
    }

    /*
    * Checks if there are links that require stitching.
    *
    * Rules:
    *   - the link has more than one componentManager
    *   - the link is not a GRE tunnel
    * */
    public static boolean needsStitching(Rspec rspec) {
        for (RspecLink link : rspec.getLinks())
            if (link.getComponentManagerUrns().size() >= 2) {
                if (!link.getLinkTypes().contains("gre"))
                    return true;
            }

        return false;
    }

    //see SliceTabController.createSlivers() for code below
//    /**
//     * This assumes the request is stored in the slice, and it will create the slivers on all component managers
//     * that are mentioned in the Rspec */
//    public List<TaskThread.Task> createSliversWithStitching(final Slice slice) {
//        Rspec rspec = slice.requestRspecProperty().get();
//        if (needsStitching(rspec)) {
//            ParallelStitcher parallelStitcher = new ParallelStitcher(easyModel, rspec.toGeni3RequestRspec());
////            CreateSliverStatusPanel.showStitchingOverview(easyModel, parallelStitcher);
////            parallelStitcher.setSlice(stitchSlice);
////            parallelStitcher.start();
//        } else {
//            return createSliversNoStitching(slice);
//        }
//    }
    /**
     * This assumes the request is stored in the slice, and it will create the slivers on all component managers
     * that are mentioned in the Rspec */
    public List<TaskThread.Task> createSliversNoStitching(final Slice slice) {
        List<TaskThread.Task> calls = new ArrayList<TaskThread.Task>();

        List<String> componentManagerUrns = slice.requestRspecProperty().get().getAllComponentManagerUrns();
        System.out.println("  createSlivers start  componentManagerUrns="+componentManagerUrns);
        for (final String componentManagerUrn : componentManagerUrns) {
            final AuthorityInfo ai = easyModel.getAuthorityList().get(componentManagerUrn);
            if (ai == null) {
                //TODO handle better
                System.err.println("WARNING: INPUT ERROR: Component Manager Urn in Request RSpec unknown: \""+componentManagerUrn+"\". Will ignore!");
                continue;
            }
            final SfaAuthority auth = ai.getGeniAuthority();
            assert auth != null;

            //report that we are creating sliver
            Runnable startStatus = new Runnable() {
                @Override
                public void run() {
                    Sliver newSliver = easyModel.logExistSliverGeniSingle(slice.getUrn(), auth);
                    newSliver.setStatus(Status.CHANGING);
                    newSliver.setStatusString("will call CreateSliver");
                }
            };
            if (Platform.isFxApplicationThread()) startStatus.run(); else Platform.runLater(startStatus);

            TaskThread.Task call = new TaskThread.Task("Create Sliver @ "+auth.getUrn()) {
                @Override
                public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException, InterruptedException {
//                if (auth.getUrl(ServerType.GeniServerRole.AM, 3) != null) {
//                    EasyAggregateManager3 am3 = new EasyAggregateManager3(easyModel, auth);
//                    //TODO
//                    throw new RuntimeException("Error: AMv3 support not yet implemented here");
//                } else {

                    //report that we are creating sliver
                    Runnable callingStatus = new Runnable() {
                        @Override
                        public void run() {
                            Sliver newSliver = easyModel.logExistSliverGeniSingle(slice.getUrn(), auth);
                            newSliver.setStatus(Status.CHANGING);
                            newSliver.setStatusString("now calling CreateSliver");
                        }
                    };
                    if (Platform.isFxApplicationThread()) callingStatus.run(); else Platform.runLater(callingStatus);


                    boolean successfullyCreated = false;
                    if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                        EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                        List< Sliver > slivers = new ArrayList<Sliver>();
                        List<String> userUrns = new ArrayList<String>();
                        userUrns.add(easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserUrn());
                        RSpecInfo ri = new RSpecInfo(
                                slice.requestRspecProperty().get().toGeni3RequestRspec(),
                                RSpecInfo.RspecType.REQUEST,
                                slice,
                                slivers,
                                ai);
//                            System.out.println("  createSlivers AMv2 @ "+componentManagerUrn);
                        successfullyCreated = am2.createSliver(slice, ri, userUrns);
                    } else {
                        throw new RuntimeException("Error: only AMv2 support is implemented currently");
                    }
//                }

                    //Note: listener that handles createSliver should update the list of component managers, as it will have changed

                    //either something failed, or the sliver is now being prepared. Either way, we will check the status
                    if (successfullyCreated) {
                        TaskThread.Task statusCall = getStatusUntilReadyOrFail(slice, ai);
                        //try in 10 seconds
                        TaskThread.getInstance().scheduleTask(statusCall, 10000);
                    }
                }

                @Override
                public List<TaskThread.Task> initDependsOn() {
                    List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                    res.add(getSliceCredential(slice));
                    res.add(getUserSSHKey());
                    return res;
                }
            };
            calls.add(call);
        }

        return calls;
    }



    public List<TaskThread.Task> getSliceStatusAndManifest(final Slice slice) {
        List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
        res.add(getSliceStatus(slice));
        res.add(getManifest(slice));
        return res;
    }




    private Map<Slice, TaskThread.Task> sliceStatusCalls = new HashMap<Slice, TaskThread.Task>();
    /** call Status on all slivers in the slice. Needs to be sure resolve has been done first, so that all AM involved are known. */
    public TaskThread.Task getSliceStatus(final Slice slice) {
        TaskThread.Task call  = sliceStatusCalls.get(slice);
        if (call != null) return call;

        call = new TaskThread.Task("Status of Slice") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                for (String cmUrn : slice.getManifestComponentManagers()) {
                    final AuthorityInfo ai = easyModel.getAuthorityList().get(cmUrn);
                    if (ai == null) { System.err.println("WARNING: Resolve call returned component_manager for unknown authority ("+cmUrn+"). Will be ignored."); return; }
                    final SfaAuthority auth = ai.getGeniAuthority();
                    assert auth != null;

                    //TODO would it be better if we put ourself in blocked again and added these to our deps? (because whatever might depend on us will think we are done now)
                    TaskThread.Task subCall = getSliceStatus(slice, auth);
                    TaskThread.getInstance().addTask(subCall);
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
                return res;
            }
            @Override
            public List<TaskThread.Task> initAlwaysDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(resolveSlice(slice));
                return res;
            }
        };
        sliceStatusCalls.put(slice , call);
        return call;
    }

    /** call Status on al slivers in the slice. Needs to be sure resolve has been done first, so that all AM involved are known. */
    public TaskThread.Task deleteSlice(final Slice slice) {
        List<TaskThread.Task> calls = new ArrayList<TaskThread.Task>();

        TaskThread.Task call = new TaskThread.Task("Delete All Slivers on slice "+slice.getUrn()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException, InterruptedException {
                for (String cmUrn : slice.getManifestComponentManagers()) {
                    AuthorityInfo ai = easyModel.getAuthorityList().get(cmUrn);
                    if (ai == null) { System.err.println("WARNING: Resolve call returned component_manager for unknown authority ("+cmUrn+"). Will be ignored."); return; }
                    SfaAuthority auth = ai.getGeniAuthority();
                    assert auth != null;
                    if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                        EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                        am2.deleteSliver(slice);
                    } else
                        System.err.println("WARNING: Slice has authority ("+cmUrn+") without AMv2 support. It will be ignored!");
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
                return res;
            }
            @Override
            public List<TaskThread.Task> initAlwaysDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(resolveSlice(slice));
                return res;
            }
        };

        return call;
    }

    public TaskThread.Task getStatusUntilReadyOrFail(final Slice slice, final AuthorityInfo componentManager) {
        final SfaAuthority auth = componentManager.getGeniAuthority();
        assert auth != null;

        TaskThread.Task call = new TaskThread.Task("Status of Sliver @ "+auth.getUrn()) {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
//                if (auth.getUrl(ServerType.GeniServerRole.AM, 3) != null) {
//                    EasyAggregateManager3 am3 = new EasyAggregateManager3(easyModel, auth);
//                    //TODO
//                    throw new RuntimeException("Error: AMv3 support not yet implemented here");
//                } else {
                //fallback to AMv2 if no AMv3
                if (auth.getUrl(ServerType.GeniServerRole.AM, 2) != null) {
                    EasyAggregateManager2 am2 = new EasyAggregateManager2(JavaFXLogger.wrap(easyModel.getLogger(), singleTask), easyModel, auth);
                    AggregateManager2.SliverStatus am2sliverStatus = am2.sliverStatus(slice);

                    if (am2sliverStatus == null) {
                        System.err.println("WARNING: Something went wrong while waiting for slice \""+slice.getUrn()+"\" to become ready. Will stop checking status...");
                        return;
                    }

                    //if not fail or ready, schedule again!
                    List<String> scheduleAgainStatuses = new ArrayList<String>();
                    scheduleAgainStatuses.add("configuring");
                    scheduleAgainStatuses.add("unknown");
                    scheduleAgainStatuses.add("changing");
                    scheduleAgainStatuses.add("notready");
                    if (scheduleAgainStatuses.contains(am2sliverStatus.getStatus())) {
                        System.out.println("Status of Slice "+slice.getUrn()+" is \""+am2sliverStatus.getStatus()+"\": will check again in 10 seconds.");
                        TaskThread.Task callAgain = getStatusUntilReadyOrFail(slice, componentManager);
                        TaskThread.getInstance().scheduleTask(callAgain, 10000);
                    }
                } else {
                    throw new RuntimeException("Error: only AMv2 support is implemented currently");
                }
            }

            @Override
            public List<TaskThread.Task> initDependsOn() {
                List<TaskThread.Task> res = new ArrayList<TaskThread.Task>();
                res.add(getSliceCredential(slice));
//                res.add(resolveSlice(slice));
                return res;
            }
        };

        return call;
    }
}
