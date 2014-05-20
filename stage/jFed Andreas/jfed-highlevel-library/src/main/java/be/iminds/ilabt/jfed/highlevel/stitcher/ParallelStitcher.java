package be.iminds.ilabt.jfed.highlevel.stitcher;

import be.iminds.ilabt.jfed.highlevel.controller.TaskFinishedCallback;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.AuthorityList;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractGeniAggregateManager;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.StitchingComputationService;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.stitching.StitchingDirector;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ParallelStitcher
 */
public class ParallelStitcher {
    private final static int MAX_CREATE_RETRIES = 3;
    private final static int MAX_STATUS_RETRIES = 3;
    private final static long STATUS_TIMEOUT_MS = 5 * 60 * 1000; /*5 minutes to get to ready */



    public static enum Status { INIT, WAITING_FOR_DEP, WAITING, CREATING, CHANGING, FAIL, READY }
    public static class SliverReadyTracker {
        private boolean extraSliver; //is this only needed for a link? (true if it is an extra aggregate manager)
        private ObjectProperty<Status> sharedStatus = new SimpleObjectProperty<Status>();
        private Status privateStatus;
        private SfaAuthority auth;

        private int createRetries = 0;
        private long createFinishMillis = 0;
        private int statusFailureRetries = 0;

        private final ListProperty<TaskThread.Task> createTasks =
                new SimpleListProperty<TaskThread.Task>(FXCollections.<TaskThread.Task>observableArrayList());
        private final ListProperty<TaskThread.Task> statusTasks =
                new SimpleListProperty<TaskThread.Task>(FXCollections.<TaskThread.Task>observableArrayList());
        private final ListProperty<TaskThread.Task> deleteTasks =
                new SimpleListProperty<TaskThread.Task>(FXCollections.<TaskThread.Task>observableArrayList());

        private StitchingDirector.ReadyHopDetails readyHopDetails;

        private String manifestRspec;
        private boolean ready = false; //set when a StatusCall returns "ready"
        private boolean fail = false; //set when a Status call returns a failure
        private boolean deleting = false; //when this flag is set, every non-delete call should cancel
        private Throwable failureThrowable;

        private SliverReadyTracker(SfaAuthority auth) {
            this.auth = auth;
            this.sharedStatus.set(Status.INIT);
            privateStatus = Status.INIT;
        }

        private void setStatus(final Status s) {
            privateStatus = s;
            if (Platform.isFxApplicationThread())
                sharedStatus.set(s);
            else
                Platform.runLater(new Runnable() { @Override public void run() { sharedStatus.set(s); } });
        }

        public ObjectProperty<Status> getStatus() {
            return sharedStatus;
        }

        public boolean isExtraSliver() {
            return extraSliver;
        }

        public SfaAuthority getAuth() {
            return auth;
        }

        public ReadOnlyListProperty<TaskThread.Task> getCreateTasks() {
            return createTasks;
        }
        public  ReadOnlyListProperty<TaskThread.Task> getStatusTasks() {
            return statusTasks;
        }
        public ReadOnlyListProperty<TaskThread.Task> getDeleteTasks() {
            return deleteTasks;
        }

        public boolean isReady() {
            return ready;
        }

        public Throwable getFailureThrowable() {
            return failureThrowable;
        }
    }

    private BooleanProperty scsCallOk = new SimpleBooleanProperty(false);
    private BooleanProperty stitchingSuccess = new SimpleBooleanProperty(false);
    private BooleanProperty stitchingFailed = new SimpleBooleanProperty(false);

    private BooleanProperty deleteAllCalled = new SimpleBooleanProperty(false);
    private BooleanProperty deleteAllSuccess = new SimpleBooleanProperty(false);
    private BooleanProperty deleteAllFailure = new SimpleBooleanProperty(false);

    private final ListProperty<SliverReadyTracker> sliverTrackers = new SimpleListProperty<SliverReadyTracker>(FXCollections.<SliverReadyTracker>observableArrayList());
    private final ListProperty<TaskThread.Task> scsRequestTasks = new SimpleListProperty<TaskThread.Task>(FXCollections.<TaskThread.Task>observableArrayList());

    private Slice slice;
    private String originalRspecRequest;
    private final StitchingDirector director;
    private final EasyModel easyModel;
    private final GeniConnectionProvider connectionProvider;
    public ParallelStitcher(EasyModel easyModel, String originalRspecRequest) {
        assert originalRspecRequest != null;

        this.easyModel = easyModel;
        this.originalRspecRequest = originalRspecRequest;
        this.director = new StitchingDirector(easyModel.getAuthorityList().getAuthorityListModel());
        this.connectionProvider = new GeniConnectionPool();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// interactive part of the interface ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setSlice(Slice slice) {
        assert slice != null;
        assert slice.getCredential() != null : "Must use a slice with a known credential: "+slice.getUrn();
        this.slice = slice;
    }

    /**
     * Contact SCS and Create all slivers
     * */
    public void start() {
        assert slice != null : "must set slice first";
        assert  easyModel.getGeniUserProvider().isUserLoggedIn() : "User needs to be logged in";
        doScsRequest();
    }

    /**
     * Abort all calls and call delete on all involved aggregates
     **/
    public void delete() {
        assert slice != null : "must set slice first";

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                deleteAllCalled.set(true);
            }
        });

        //stop all current tasks and calls
        for (final SliverReadyTracker tracker : sliverTrackers) {
            tracker.deleting = true;
            TaskThread.getInstance().cancel(tracker.getCreateTasks());
            TaskThread.getInstance().cancel(tracker.getStatusTasks());
        }

        final IntegerProperty deleteAllCounter = new SimpleIntegerProperty(sliverTrackers.size());
        //call delete on all involved aggregates
        for (final SliverReadyTracker tracker : sliverTrackers) {
            if (tracker.privateStatus.equals(Status.INIT) || tracker.privateStatus.equals(Status.WAITING_FOR_DEP)) {
                //no need to delete
                System.out.println("ParallelStitcher.delete() -> no need to Delete (state="+tracker.privateStatus+") on "+tracker.getAuth().getUrn());
                continue;
            }
            System.out.println("ParallelStitcher.delete() -> Deleting (state="+tracker.privateStatus+") on "+tracker.getAuth().getUrn());

            TaskThread.Task deleteTask = deleteSliver(tracker);
            deleteTask.addCallback(new TaskFinishedCallback() {
                @Override
                public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state) {
                    if (state == TaskThread.SingleTask.TaskState.SUCCESS) {
                        //todo handle delete success: set global delete status
                        tracker.setStatus(Status.INIT);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                int o = deleteAllCounter.get() - 1;
                                deleteAllCounter.set(o);
                                if (o == 0)
                                    deleteAllSuccess.set(true);
                            }
                        });
                    }
                    else {
                        //todo handle delete failure: set global delete status
                        tracker.setStatus(Status.FAIL);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                deleteAllSuccess.set(false);
                                deleteAllFailure.set(true);
                            }
                        });
                    }
                }
            });
            TaskThread.getInstance().addTask(deleteTask);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// read only feedback ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getOriginalRspecRequest() {
        return originalRspecRequest;
    }

    public ReadOnlyListProperty<SliverReadyTracker> getSliverTrackers() {
        return sliverTrackers;
    }

    public Slice getSlice() {
        return slice;
    }

    public StitchingDirector getDirector() {
        return director;
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    public BooleanProperty getStitchingSuccess() {
        return stitchingSuccess;
    }
    public BooleanProperty getStitchingFailed() {
        return stitchingFailed;
    }
    public BooleanBinding getStitchingFinished() {
        return stitchingFailed.or(stitchingSuccess);
    }

    public ReadOnlyListProperty<TaskThread.Task> getScsRequestTasks() {
        return scsRequestTasks;
    }

    public BooleanProperty getScsCallOk() {
        return scsCallOk;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// private internals /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private SfaAuthority getStitchingAuthority() {
        //create auth if doesn't exist
        String sticherUrl = "http://oingo.dragon.maxgigapop.net:8081/geni/xmlrpc";
        String maxScsUrn = "urn:publicid:IDN+oingo.dragon.maxgigapop.net+auth+am";
        SfaAuthority stitcherAuth = null;
        if (easyModel.getAuthorityList().get(maxScsUrn) == null) {
            Map< ServerType, URL> urlMap = new HashMap< ServerType, URL>();
            try {
                urlMap.put(new ServerType(ServerType.GeniServerRole.SCS, 1), new URL(sticherUrl));
                stitcherAuth = new SfaAuthority(maxScsUrn, "Max SCS", urlMap, null, "scs");
            } catch (GeniException e) {
                System.err.println("Failed to create Authority for stitcher");
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                System.err.println("Failed to create Authority for stitcher");
                e.printStackTrace();
                return null;
            }
            AuthorityList authorityList = easyModel.getAuthorityList();
            AuthorityListModel authorityListModel = authorityList.getAuthorityListModel();
            authorityListModel.addAuthority(stitcherAuth);
            authorityListModel.fireChange();

            assert easyModel.getAuthorityList().get(maxScsUrn) != null;
        } else
            stitcherAuth = easyModel.getAuthorityList().get(maxScsUrn).getGeniAuthority();
        return stitcherAuth;
    }

    private final ObjectProperty<StitchingComputationService.ComputePathResult> scsResult = new SimpleObjectProperty<StitchingComputationService.ComputePathResult>();
    private void doScsRequest() {
        if (!scsRequestTasks.isEmpty()) {
            //TODO correctly handle this restart

            //forget all previous state
            scsCallOk.set(false);
            stitchingSuccess.set(false);
            stitchingFailed.set(false);
            sliverTrackers.clear();
        }

        final SfaAuthority stitcherAuth = getStitchingAuthority();
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();
        final StitchingComputationService scs = new StitchingComputationService(easyModel.getLogger());

        scsResult.set(null);
        final TaskThread.Task scsRequestTask = new TaskThread.Task("ParallelStitcher StitchingComputationService call") {
            @Override
            protected void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                GeniConnection con = null;
                try {
                    con = connectionProvider.getConnectionByAuthority(user, stitcherAuth, StitchingComputationService.class);
                    StitchingComputationService.SCSReply<StitchingComputationService.ComputePathResult> res =
                            scs.computePath(con, slice.getUrn(), originalRspecRequest, null);
                    scsResult.set(res.getValue());
                } catch (GeniException e) {
                    System.err.println("Failed to create Authority for stitcher");
                    e.printStackTrace();
                }
            }
        };

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scsRequestTasks.add(scsRequestTask);
            }
        });

        scsRequestTask.addCallback(new TaskFinishedCallback() {
            @Override
            public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state) {
                if (state == TaskThread.SingleTask.TaskState.SUCCESS)
                    onScsTaskSuccess();
                else {
                    //TODO handle SCS failure
                    return;
                }
                return;
            }
        });
        TaskThread.getInstance().addTask(scsRequestTask);
    }

    private final Map<SfaAuthority, SliverReadyTracker> trackers = new HashMap<SfaAuthority, SliverReadyTracker>();
    private void onScsTaskSuccess() {
        StitchingComputationService.ComputePathResult res = scsResult.get();
        assert res != null;

        director.setComputePathResult(res);

        Platform.runLater(new Runnable() { @Override public void run() { scsCallOk.set(true); } });

        for (SfaAuthority auth : director.getInvolvedAuthorities()) {
            assert auth != null;
            final SliverReadyTracker tracker = new SliverReadyTracker(auth);
            trackers.put(auth, tracker);

            tracker.setStatus(Status.WAITING_FOR_DEP);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    sliverTrackers.add(tracker);
                }
            });
        }
        activateReadyHops();
    }

    private void activateReadyHops() {
        for (StitchingDirector.ReadyHopDetails rhd : director.getReadyHops()) {
            final SliverReadyTracker tracker = trackers.get(rhd.getAuthority());
            assert tracker != null;
//            assert tracker.status.get() == Status.WAITING : "tracker for "+tracker.auth.getUrn()+" is in state "+tracker.status;

            if (tracker.privateStatus == Status.INIT || tracker.privateStatus == Status.WAITING_FOR_DEP) {
                tracker.readyHopDetails = rhd;
                tracker.setStatus(Status.WAITING);
                createSliver(tracker, 0/*now*/);
            }
            //else it is already being created
        }
    }

    private void createSliver(final SliverReadyTracker tracker, long delayMs) {
//        if (!tracker.getAuth().getNameForUrn().equals("emulab.net")) {
        if (!tracker.getAuth().getNameForUrn().equals("all")) {
            System.out.println("DEBUG preventing CreateSliver task for "+tracker.getAuth().getUrn());
//            System.out.println("      auth= "+tracker.getAuth().toXmlString());
            return;
        }

        System.out.println("adding CreateSliver task for "+tracker.getAuth().getUrn());

        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        final TaskThread.Task createTask = new TaskThread.Task("ParallelStitcher CreateSliver call") {
            @Override
            protected void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                GeniConnection con = null;
                try {
                    con = connectionProvider.getConnectionByAuthority(user, tracker.auth, AggregateManager2.class);
                    //do createsliver call

                    AggregateManager2 am2 = new AggregateManager2(easyModel.getLogger(), true);
                    String manifestRspec = null;

                    tracker.setStatus(Status.CREATING);

                    //TODO handle errors (adding retry etc)
                    AbstractGeniAggregateManager.AggregateManagerReply<String> reply = am2.createSliver(
                            con, getSliceCredentialList(), slice.getUrn(), tracker.readyHopDetails.getRequestRspec(), getUsers(),
                            null);

                    if (!reply.getGeniResponseCode().isSuccess())
                        throw new RuntimeException("Call no succesfull: " + reply.getGeniResponseCode());
                    manifestRspec = reply.getValue();

                    if (manifestRspec == null)
                        throw new RuntimeException("Call returned no manifest rspec: " + reply);
                    assert manifestRspec != null;

                    tracker.manifestRspec = manifestRspec;
                } catch (GeniException e) {
                    System.err.println("Failed to create sliver at " + tracker.auth.getName());
                    throw e;
                }
            }
        };
        Platform.runLater(new Runnable() { @Override public void run() { tracker.createTasks.add(createTask); } });

        createTask.addCallback(new TaskFinishedCallback() {
            @Override
            public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state) {
                if (tracker.deleting) return;

                if (state == TaskThread.SingleTask.TaskState.SUCCESS)
                    onCreateSliverTaskSuccess(tracker);
                else
                    onCreateSliverTaskFailed(tracker, singleTask.getException());
            }
        });
        if (delayMs > 0)
            TaskThread.getInstance().scheduleTask(createTask, delayMs);
        else
            TaskThread.getInstance().addTask(createTask);
    }

    private void onCreateSliverTaskFailed(final SliverReadyTracker tracker, Throwable throwable) {
        //TODO handle failure
        tracker.failureThrowable = throwable;

        if (tracker.createRetries++ < MAX_CREATE_RETRIES)
            createSliver(tracker, 5000);
        else {
            tracker.setStatus(Status.FAIL);
            delete();
        }
    }
    private void onCreateSliverTaskSuccess(final SliverReadyTracker tracker) {
        assert tracker.manifestRspec != null;
        System.out.println("Processing manifest for "+tracker.getAuth().getUrn());
        director.processManifest(tracker.readyHopDetails, tracker.manifestRspec);

        tracker.setStatus(Status.CHANGING);

        tracker.createFinishMillis = System.currentTimeMillis();

        activateReadyHops();

        checkStatusUntilReady(tracker);
    }

    private void checkStatusUntilReady(final SliverReadyTracker tracker) {
        System.out.println("adding CheckStatus task for "+tracker.getAuth().getUrn());

        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        final TaskThread.Task statusTask = new TaskThread.Task("ParallelStitcher CheckStatus call") {
            @Override
            protected void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
//                assert tracker.status.get() == Status.CHANGING;

                GeniConnection con = null;
                con = connectionProvider.getConnectionByAuthority(user, tracker.auth, AggregateManager2.class);
                //do createsliver call

                AggregateManager2 am2 = new AggregateManager2(easyModel.getLogger(), true);
                //TODO handle errors (adding retry etc)
                AbstractGeniAggregateManager.AggregateManagerReply<AggregateManager2.SliverStatus> reply = am2.sliverStatus(
                        con, getSliceCredentialList(), slice.getUrn(), null);

                if (!reply.getGeniResponseCode().isSuccess())
                    throw new RuntimeException("Call not succesfull: " + reply.getGeniResponseCode());

                if (reply.getValue().getStatus().equals("ready")) {
                    tracker.ready = true;
                } else {
                    long now = System.currentTimeMillis();
                    long diff = now - tracker.createFinishMillis;
                    if (diff > STATUS_TIMEOUT_MS)
                        delete();
                }

                //TODO: detect if sliver is in failed status and set tracker.fail
                //if (reply.getValue().getStatus().equals("fail"))
            }
        };
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tracker.statusTasks.add(statusTask);
            }
        });

        statusTask.addCallback(new TaskFinishedCallback() {
            @Override
            public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state) {
                if (tracker.deleting) return;

                if (state == TaskThread.SingleTask.TaskState.SUCCESS) {
                    if (tracker.fail) {
                        onSliverFailedStatus(tracker);
                    } else {
                        if (!tracker.ready) {
                            //try again in 5 seconds
//                          TaskThread.getInstance().scheduleTask(statusTask, 5000); //don't reuse task. GUI doesn't like it.
                            checkStatusUntilReady(tracker);
                        } else {
                            tracker.setStatus(Status.READY);
                        }
                    }
                }
                else {
                    //handle status failure
                    onSliverStatusCallFailed(tracker, singleTask.getException());
                }

            }
        });
        TaskThread.getInstance().scheduleTask(statusTask, 5000);
    }

    /** status call itself failed (not sliver) */
    private void onSliverStatusCallFailed(final SliverReadyTracker tracker, Throwable exception) {
        if (tracker.statusFailureRetries++ < MAX_STATUS_RETRIES)
            checkStatusUntilReady(tracker);
        else
            delete();
    }

    /** status call successful, but sliver status is failure */
    private void onSliverFailedStatus(final SliverReadyTracker tracker) {

        if (tracker.createRetries++ < MAX_CREATE_RETRIES) {
            //delete and try again.

            tracker.setStatus(Status.WAITING);

            TaskThread.Task deleteTask = deleteSliver(tracker);
            deleteTask.addCallback(new TaskFinishedCallback() {
                @Override
                public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state) {
                    if (state == TaskThread.SingleTask.TaskState.SUCCESS)
                        createSliver(tracker, 0);
                    else {
                        tracker.setStatus(Status.FAIL);
                        delete();
                    }
                }
            });
            TaskThread.getInstance().addTask(deleteTask);
        }
        else {
            tracker.setStatus(Status.FAIL);
            delete();
        }
    }


    private TaskThread.Task deleteSliver(final SliverReadyTracker tracker) {
        System.out.println("making DeleteSliver task for "+tracker.getAuth().getUrn());

        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        final TaskThread.Task deleteTask = new TaskThread.Task("ParallelStitcher DeleteSliver call") {
            @Override
            protected void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                GeniConnection con = null;
                try {
                    con = connectionProvider.getConnectionByAuthority(user, tracker.auth, AggregateManager2.class);
                    //do createsliver call

                    AggregateManager2 am2 = new AggregateManager2(easyModel.getLogger(), true);

                    AbstractGeniAggregateManager.AggregateManagerReply<Boolean> reply = am2.deleteSliver(
                            con, getSliceCredentialList(), slice.getUrn(), null);

                    if (reply.getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED))
                        return; //this is acceptable, that means the sliver was already deleted

                    if (!reply.getGeniResponseCode().isSuccess())
                        throw new RuntimeException("Delete Call not succesfull: " + reply.getGeniResponseCode());

                    if (!reply.getValue())
                        throw new RuntimeException("Delete failed, returned: " + reply);
                } catch (GeniException e) {
                    System.err.println("Failed to delete sliver at " + tracker.auth.getName());
                    throw e;
                }
            }
        };
        Platform.runLater(new Runnable() { @Override public void run() { tracker.deleteTasks.add(deleteTask); } });

//        deleteTask.addCallback(new TaskFinishedCallback() {
//            @Override
//            public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state) {
//                if (state == TaskThread.SingleTask.TaskState.SUCCESS)
//                    onCreateSliverTaskSuccess(tracker);
//                else
//                    onCreateSliverTaskFailed(tracker, singleTask.getException());
//            }
//        });
//        if (delayMs > 0)
//            TaskThread.getInstance().scheduleTask(deleteTask, delayMs);
//        else
//            TaskThread.getInstance().addTask(deleteTask);
        return deleteTask;
    }



    private List<GeniCredential> getSliceCredentialList() {
        List<GeniCredential> res = new ArrayList<GeniCredential>();
        res.add(slice.getCredential());
        return res;
    }
    private List<UserSpec> getUsers() {
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        List<UserSpec> users = new ArrayList<UserSpec>();
        UserSpec userSpec = new UserSpec(user.getUserUrn(), easyModel.getUserKeys());
        users.add(userSpec);
        return users;
    }
}
