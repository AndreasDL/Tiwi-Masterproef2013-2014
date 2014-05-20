package be.iminds.ilabt.jfed.highlevel.controller;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.ResultListener;
import be.iminds.ilabt.jfed.lowlevel.GeniException;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * TaskThread: manages the thread on which Tasks (typically Sfa commands) are called
 *
 * Uses ScheduledExecutorService and adds names and dependencies to Callable
 */
public class TaskThread {
    private static boolean taskdebug = false;

    private static TaskThread instance = null;
    public static TaskThread getInstance() {
        if (instance == null) new TaskThread();
        assert instance != null;

        return instance;
    }

    private List<Thread> taskThreads = new ArrayList<Thread>();
    private TaskThread() {
        assert instance == null;
        instance = this;

        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true)
                        TaskThread.this.runTask();
                }});
            taskThreads.add(t);
            t.start();
        }

    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1); //1 simultaneous assign future task possible
    public class FutureTask implements Runnable {
        JavaFXTaskThread.FutureTask javafx;

        private final SingleTask task;
        private FutureTask(SingleTask task) {
            this.task = task;
        }

        public SingleTask getTask() {
            return task;
        }

        private ScheduledFuture scheduledFuture;
        public ScheduledFuture getScheduledFuture() {
            return scheduledFuture;
        }
        void setScheduledFuture(ScheduledFuture scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        private boolean cancel = false;
        public void tryCancel() {
            cancel = true;
            if (scheduledFuture != null) {
                boolean wasCancelled = scheduledFuture.cancel(false);
                if (wasCancelled)
                    task.setState(SingleTask.TaskState.CANCELLED);
            }
        }
        public long getTimeLeftMs() {
            if (scheduledFuture != null)
                return scheduledFuture.getDelay(MILLISECONDS);
            else
                return -1;
        }

        @Override
        public void run() {
            if (cancel) {
                task.setState(SingleTask.TaskState.CANCELLED);
                return;
            }
            try {
                if (taskdebug) System.out.println("                  adding future task: \""+task+"\"");

                boolean wasRemoved = task.task.removeFuture(this);
                assert wasRemoved;

                processNewSingleTask(task);
                if (taskdebug) System.out.println("                  added future task: \"" + task + "\"");
                if (taskdebug) System.out.println("                        queuedTasks= "+ queuedTasks);
                if (taskdebug) System.out.println("                        blockedTasks=" + blockedTasks);
                System.out.flush();
            } catch (Throwable e) {
                System.err.println("Exception while running addCall on future task. This is a bug. " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    //only uses BlockingDeque when that queue is added to from some thread and wiated on in other thread.
    //example: a running queue is never waited for anywhere, the thread that runs would add AND remove from that queue

    //calls ready to be executed
    BlockingDeque<SingleTask> queuedTasks = new LinkedBlockingDeque<SingleTask>();
    //calls waiting for dependencies
    BlockingDeque<SingleTask> blockedTasks = new LinkedBlockingDeque<SingleTask>();

    //all active, future, and historic Tasks. only gets added to, never removed from
    //can be added to from multiple threads. Can be read from multiple threads.
    BlockingQueue<SingleTask> allTasks = new LinkedBlockingQueue<SingleTask>();


    private static AtomicInteger taskUniqueIdHelper = new AtomicInteger();

    /**
     *
     * */
    public static class SingleTask implements ResultListener {
        JavaFXTaskThread.SingleTask javafx;

        public static enum TaskState { UNSUBMITTED, BLOCKED, QUEUED, RUNNING, FAILED, FUTURE, SUCCESS, CANCELLED };

        protected final Task task;
        protected final String name;
        protected String id;
        protected TaskState state;

        protected Throwable exception = null;
        protected List<ApiCallDetails> apiCallHistory = new ArrayList<ApiCallDetails>();

        private boolean canceledByUser = false;

        private Date runStart;
        private Date runStop;

        private Thread thread;

        public SingleTask(Task task) {
            this.task = task;
            this.name = task.getName();
            this.id = ""+taskUniqueIdHelper.getAndIncrement();
            state = TaskState.UNSUBMITTED;
        }

        /*
        * These are resolved dependencies, with only the actually needed onces added.
        *
        * The task can be queued and run once all these dependencies are SUCCESS.
        * The task fails if any one of these FAILED.
        *
        * The difference between initDependsOn() and initAlwaysDependsOn() has been processed into this once list:
        *    - initDependsOn() deps will depend on the original SingleTask associated with this task
        *    - initAlwaysDependsOn() deps will depend on the original one only if it is not yet succesful. It it is done already, they will make a new one.
        * */
        private List<SingleTask> dependsOn = new ArrayList<SingleTask>();
        private List<SingleTask> dependingOnThis = new ArrayList<SingleTask>();
        public List<SingleTask> getDependsOn() {
            return dependsOn;
        }
        public List<SingleTask> getDependingOnThis() {
            return dependingOnThis;
        }


        @Override
        public void onResult(ApiCallDetails result) {
            apiCallHistory.add(result);
        }
        public List<ApiCallDetails> getApiCallHistory() {
            return apiCallHistory;
        }


        public boolean canRun() {
            for (SingleTask dep : dependsOn) {
                if (dep.state != TaskState.SUCCESS) return false;
            }
            return true;
        }
        public boolean mustFail() {
            for (SingleTask dep : dependsOn) {
                if (dep.state == TaskState.FAILED || dep.state == TaskState.CANCELLED) return true;
            }
            return false;
        }

        public boolean isCanceledByUser() {
            return canceledByUser;
        }

        public Date getRunStart() {
            return runStart;
        }

        public Date getRunStop() {
            return runStop;
        }

        public TaskState getState() {
            return state;
        }
        public boolean isCompleted() {
            return state.equals(TaskState.FAILED) || state.equals(TaskState.SUCCESS) || state.equals(TaskState.CANCELLED);
        }
        //These are used to set the actual start and stop time. It cannot be done directly, because this is not called on a JavaFX thread!
        private void registerRunStart() {
            assert runStart == null;
            final Date now = new Date();
            runStart = now;
        }
        private void registerRunStop() {
            assert runStop == null;
            final Date now = new Date();
            runStop = now;
        }

        public void setState(TaskState newState) {
            state = newState;
        }

        public String getName() {
            return name;
        }
        public String getId() {
            return id;
        }

        public Throwable getException() {
            return exception;
        }

        public String toString() {
            String deps = "";
            for (SingleTask dep : dependsOn) {
                if (!deps.isEmpty()) deps += ", ";
                deps += "\""+dep.name+"\" "+dep.state;
            }
            return "SingleTask("+id+" \""+getName()+"\" "+state+", deps={"+deps+"})";
        }
    }

    /** a named Runnable */
    public static abstract class Task {
        JavaFXTaskThread.Task javafx;

        protected final String name;
        private static long nextId=  0;
        private long id = nextId++;

        private final List<SingleTask> currentActiveSingleTasks = new ArrayList<SingleTask>();

        private SingleTask lastCompletedSingleTask = null;
        private final List<TaskFinishedCallback> callbacks = new ArrayList<TaskFinishedCallback>();
        public final List<FutureTask> futureTasks = new ArrayList<FutureTask>();

        public Task(String name) {
            this.name = name;
        }


        /**
         * execute the call and throw a GeniException when needed.
         * The SingleTask this is running is is provided.
         * */
        protected abstract void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException;

        /** override to specify dependencies
         * this is called only once
         * */
        public List<Task> initDependsOn() { return new ArrayList<Task>(); }

        /** override to specify dependencies that should ALWAYS run
         * this is called only once
         * */
        public List<Task> initAlwaysDependsOn() { return new ArrayList<Task>(); }

        /** Is the call still needed?
         * This checks if the data that the call retrieves is already available, or the action is already done.
         *
         * default: needed as long as this task has never completed successfully
         **/
        public boolean needed() { return lastCompletedSingleTask == null ||
                lastCompletedSingleTask.getState() == SingleTask.TaskState.FAILED ||
                lastCompletedSingleTask.getState() == SingleTask.TaskState.CANCELLED; }

        public String getName() {
            return name;
        }

        public String toString() {
            String res = "Task(\""+getName()+"";
            synchronized (currentActiveSingleTasks) {
                if (currentActiveSingleTasks.size() > 0)
                    res += " active={";
                boolean first = true;
                for (SingleTask st : currentActiveSingleTasks) {
                    if (!first)
                        res += ", ";
                    res += st.getState();
                    first = false;
                }
                if (currentActiveSingleTasks.size() > 0)
                    res += "}";
            }
            if (lastCompletedSingleTask != null)
                res += " last="+lastCompletedSingleTask.state;
            res += ")";
            return res;
        }


        //private helper that makes sure all dependencies STAY the same objects
        protected List<Task> depCache = null;
        protected List<Task> getDependsOn() {
            if (depCache == null)
                depCache = new ArrayList<Task>(initDependsOn());
            return depCache;
        }

        //private helper that makes sure all dependencies STAY the same objects
        protected List<Task> alwaysDepCache = null;
        protected List<Task> getAlwaysDependsOn() {
            if (alwaysDepCache == null)
                alwaysDepCache = new ArrayList<Task>(initAlwaysDependsOn());
            return alwaysDepCache;
        }

        public synchronized boolean isActive() {
            return !currentActiveSingleTasks.isEmpty();
        }
        public synchronized int getActiveTaskCount() {
            return currentActiveSingleTasks.size();
        }
        private synchronized void addActiveSingleTask(SingleTask singleTask) {
            currentActiveSingleTasks.add(singleTask);
        }
        private synchronized boolean removeActiveSingleTask(SingleTask singleTask) {
            return currentActiveSingleTasks.remove(singleTask);
        }
        public synchronized List<SingleTask> getCurrentActiveSingleTasksCopy() {
            return new ArrayList<SingleTask>(currentActiveSingleTasks);
        }
        private synchronized void addFuture(FutureTask futureTask) {
            futureTasks.add(futureTask);
        }
        private synchronized boolean removeFuture(FutureTask futureTask) {
            return futureTasks.remove(futureTask);
        }
        synchronized List<FutureTask> getFuturesCopy() {
            return new ArrayList<FutureTask>(futureTasks);
        }

        /*
       * WARNING: No guarantee on what thread this callback will be called!
       * In particular: it most likely is not the JavaFX thread
       *
       * Note: All Call logging callbacks are already done when this callback is called.
       * */
        public void addCallback(TaskFinishedCallback callback) {
            callbacks.add(callback);
        }
        public void removeCallback(TaskFinishedCallback callback) {
            callbacks.remove(callback);
        }
    }




    public void checkStateCorrectness(SingleTask task) {
        synchronized (task) {
            try {
                SingleTask.TaskState state = task.getState();

                if (state == SingleTask.TaskState.BLOCKED)
                    assert blockedTasks.contains(task);
                else
                    assert !blockedTasks.contains(task);

                if (state == SingleTask.TaskState.QUEUED)
                    assert queuedTasks.contains(task);
                else
                    assert !queuedTasks.contains(task);
            } catch (Throwable e) {
                System.err.println("checkStateCorrectness failed for task "+task);
                System.err.println("      queuedTasks="+ queuedTasks);
                System.err.println("     blockedTasks="+ blockedTasks);
                e.printStackTrace();

                //try to fix it
                task.setState(SingleTask.TaskState.FAILED);
                queuedTasks.remove(task);
                blockedTasks.remove(task);

                throw new RuntimeException(e);
            }
        }
    }

    public void cancel(List<Task> tasks) {
        for (Task t : tasks)
            cancel(t);
    }

    public void cancel(Task task) {
        for (SingleTask st : task.getCurrentActiveSingleTasksCopy())
            cancel(st);
        for (FutureTask ft : task.getFuturesCopy())
            ft.tryCancel();
    }

    /**
     * Try to deque or stop execution of a task. Note that this task may have started or completed during this method,
     * so success cannot be guaranteed. (Except that when this method returns, the task is not running and will
     * never start running)
     *
     * */
    public void cancel(SingleTask task) {
        synchronized (task) {
            if (task.state == SingleTask.TaskState.RUNNING) {
                task.canceledByUser = true;
                if (task.thread != null) {
                    //Note: interrupt will not force stop. It might be too late to cancel.
                    task.thread.interrupt();
                }
                return;
            }

            if (task.state == SingleTask.TaskState.BLOCKED)
                blockedTasks.remove(task);

            if (task.state == SingleTask.TaskState.QUEUED)
                queuedTasks.remove(task);

            task.canceledByUser = true;
            task.state = SingleTask.TaskState.CANCELLED;
            task.task.removeActiveSingleTask(task);
        }
        checkStateCorrectness(task);
        updateBlocking();
    }

    /** Execute a single call. (or execute no call in some cases) This blocks. */
    public void runTask() {
        SingleTask firstTask;
        try {
            firstTask = queuedTasks.takeFirst();
        } catch (InterruptedException e) {
            //someone is trying to cancel this thread?
            System.err.println("runTask() for InterruptedException: stopping");
            return;
        }

        synchronized (firstTask) {
            boolean wasCancelled = firstTask.canceledByUser; //last check of cancel flag (doesn't have to be thread safe. If it is too late, it is too late)
            if (wasCancelled) {
                firstTask.setState(SingleTask.TaskState.CANCELLED);
                return; //ignore task
            }
        }

        synchronized (firstTask) {
            firstTask.setState(SingleTask.TaskState.RUNNING);
            firstTask.thread = Thread.currentThread();
        }

        SingleTask.TaskState completedState = SingleTask.TaskState.FAILED;
        try {
            if (taskdebug) System.out.println("DEBUG TaskThread ==>       starting: " + firstTask);
            firstTask.registerRunStart();
            firstTask.task.doTask(firstTask);
            firstTask.registerRunStop();
            if (taskdebug) System.out.println("DEBUG TaskThread ==>       ran successfully: " + firstTask);
            completedState = SingleTask.TaskState.SUCCESS;
        } catch (InterruptedException e) {
            completedState = SingleTask.TaskState.CANCELLED;
            firstTask.exception = e;
            if (taskdebug) System.out.println("DEBUG TaskThread ==>       run cancelled: " + firstTask+"  -> Exception=\""+e+"\"");
            if (taskdebug) e.printStackTrace();
        } catch (Throwable t) {
            completedState = SingleTask.TaskState.FAILED;
            firstTask.exception = t;
            if (taskdebug) System.out.println("DEBUG TaskThread ==>       run failed: " + firstTask+"  -> Exception=\""+t+"\"");
            if (taskdebug) t.printStackTrace();
        } finally {
            List<TaskFinishedCallback> callbacks;
            synchronized (firstTask) {
                callbacks = new ArrayList<TaskFinishedCallback>(firstTask.task.callbacks);
                firstTask.setState(completedState);
                firstTask.thread = null;
                firstTask.task.lastCompletedSingleTask = firstTask;
                firstTask.task.removeActiveSingleTask(firstTask);
            }

            for (TaskFinishedCallback callback : callbacks) {
                callback.onTaskFinished(firstTask.task, firstTask, completedState);
            }
        }

        updateBlocking();
    }

    /**
     * check if all blocked tasks are still blocked
     * */
    public void updateBlocking() {
        List<SingleTask> tocheck = new ArrayList<SingleTask>();
        blockedTasks.drainTo(tocheck);
        //blockedTasks is now empty!

        if (!tocheck.isEmpty()) {
            if (taskdebug) System.out.println("DEBUG TaskThread ==>     assigning blocked tasks");
            for (SingleTask blockedCall : tocheck) {
                assignSingleTask(blockedCall);
            }
        }

        if (taskdebug) System.out.println("DEBUG TaskThread ==> runTask() updateAfterTaskrun done, will unlock");
        if (taskdebug) System.out.println("                        " + queuedTasks.size() + " queuedTasks= " + queuedTasks);
        if (taskdebug) System.out.println("                        "+ blockedTasks.size()+" blockedTasks="+ blockedTasks);
        System.out.flush();
    }

    /** put calls either in the queued calls (can run now) or in the blocked calls (waiting for at least 1 dependency)*/
    private void processNewSingleTask(SingleTask origSingleTask) {
        if (origSingleTask == null) return;

        //if deps are not added, add them now
        List<SingleTask> singleTasksToAssign = new ArrayList<SingleTask>();
        List<SingleTask> newSingleTasksToProcess = new ArrayList<SingleTask>();
        newSingleTasksToProcess.add(origSingleTask);
        singleTasksToAssign.add(origSingleTask);

        if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->  origSingleTask="+origSingleTask+" (id="+origSingleTask.id+")");

        while (!newSingleTasksToProcess.isEmpty()) {
//            System.out.println("********************      assigning calls: "+newSingleTasksToProcess);
            List<SingleTask> extraNewSingleTasksToProcess = new ArrayList<SingleTask>();

            for (SingleTask singleTask : newSingleTasksToProcess) {
                Task task = singleTask.task;

                if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask -> PROCESS task="+task+" (id="+task.id+")  singleTask="+singleTask+" (id="+singleTask.id+")");

                for (Task dep : task.getDependsOn()) {
                    if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->      DEP="+dep+" (id="+dep.id+")");
//                    checkStateCorrectness(dep);

                    SingleTask depSingleTask = null;
                    //Use any running task that is the same Task
                    for (SingleTask activeSingleTask : dep.getCurrentActiveSingleTasksCopy()) {
                        if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         found in currentActiveSingleTasks activeSingleTask="+activeSingleTask+"");
                        if (activeSingleTask.getState() != SingleTask.TaskState.FUTURE)
                            depSingleTask = activeSingleTask;
                    }

                    //Use any newly created unsubmitted task that is the same Task
                    for (SingleTask newlyCreatedTask : singleTasksToAssign)
                        if (newlyCreatedTask.task == dep) {
                            if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         found in singleTasksToAssign newlyCreatedTask="+newlyCreatedTask+"");
                            depSingleTask = newlyCreatedTask;
                        }

                    //Use any finished task that is the same Task
                    if (dep.lastCompletedSingleTask != null) {
                        if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         found in dep.lastCompletedSingleTask="+dep.lastCompletedSingleTask+"");
                        depSingleTask = dep.lastCompletedSingleTask;
                    }

                    //Create a new SingleTask if no singleTask to use found
                    if (depSingleTask == null) {
                        depSingleTask = new SingleTask(dep);
                        extraNewSingleTasksToProcess.add(depSingleTask);
                        singleTasksToAssign.add(depSingleTask);
                        depSingleTask.task.addActiveSingleTask(depSingleTask);
                        if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         creating new: "+depSingleTask+"");
                    }

                    assert depSingleTask != null;

                    //always register as dep
                    singleTask.dependsOn.add(depSingleTask);
                    depSingleTask.dependingOnThis.add(singleTask);
                };
                for (Task dep : task.getAlwaysDependsOn()) {
                    if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->      ALWAYSDEP="+dep+" (id="+dep.id+")");
//                    checkStateCorrectness(dep);

                    SingleTask depSingleTask = null;
                    //Use any running task that is the same Task
                    if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->            dep.currentActiveSingleTasks="+dep.currentActiveSingleTasks+"");
                    for (SingleTask activeSingleTask : dep.getCurrentActiveSingleTasksCopy()) {
                        if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         found in currentActiveSingleTasks activeSingleTask="+activeSingleTask+"");
                        if (activeSingleTask.getState() != SingleTask.TaskState.FUTURE)
                            depSingleTask = activeSingleTask;
                    }

                    //Use any newly created unsubmitted task that is the same Task
                    for (SingleTask newlyCreatedTask : singleTasksToAssign)
                        if (newlyCreatedTask.task == dep) {
                            if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         found in singleTasksToAssign newlyCreatedTask="+newlyCreatedTask+"");
                            depSingleTask = newlyCreatedTask;
                        }

                    //Create a new SingleTask if no singleTask to use found
                    if (depSingleTask == null) {
                        depSingleTask = new SingleTask(dep);
                        extraNewSingleTasksToProcess.add(depSingleTask);
                        singleTasksToAssign.add(depSingleTask);
                        depSingleTask.task.addActiveSingleTask(depSingleTask);
                        if (taskdebug) System.out.println(" ** DEBUG ** processNewSingleTask ->         creating new: "+depSingleTask+"");
                    }

                    assert depSingleTask != null;

                    //always register as dep
                    singleTask.dependsOn.add(depSingleTask);
                    depSingleTask.dependingOnThis.add(singleTask);
                }

                assert !allTasks.contains(singleTask) : "task was already added: "+singleTask.getName();
                allTasks.add(singleTask);

                newSingleTasksToProcess = extraNewSingleTasksToProcess;
            }
        }

        while (!singleTasksToAssign.isEmpty()) {
            List<SingleTask> assignedTasks = new ArrayList<SingleTask>();
            for (SingleTask newSingleTask : singleTasksToAssign) {
                boolean assigned = assignSingleTask(newSingleTask);
                if (assigned) assignedTasks.add(newSingleTask);
            }
            singleTasksToAssign.removeAll(assignedTasks);
            assert !assignedTasks.isEmpty() : "Infinite loop while assigning: "+singleTasksToAssign;
        }

        System.out.flush();
    }

    /**
     * receives a singlerun with all dependencies filled in correctly. (and recursively filled in correctly for deps of deps etc...)
     *
     * Will submit unsubmitted singleRuns.
     * Will queue blocked singleRuns if they are not blocked anymore
     * will fail (and move them to history) calls if their deps have failed.
     *
     * Also checks correctness of call status VS lists they are in
     *
     * Tasks sent here will not be in queuedTasks or blockedTasks, so checkStateCorrectness is incorrect at first!
     *
     * returns true if assign is successful (call is no longer unsubmitted)
     * in case any dependency is unsubmitted, this returns false and does not assign.
     * */
    public boolean assignSingleTask(SingleTask singleTask) {
        synchronized (singleTask) {
//            checkStateCorrectness(singleTask); //not guaranteed

            //this is only supposed to be used in the following cases:
            assert singleTask.getState() == SingleTask.TaskState.BLOCKED ||
                    singleTask.getState() == SingleTask.TaskState.QUEUED ||
                    singleTask.getState() == SingleTask.TaskState.UNSUBMITTED:
                    "did not expect task given as argument to assignSingleTask to be in state: "+singleTask.getState();

            if (taskdebug) System.out.println("DEBUG   assignSingleTask("+singleTask+")");

            //nothing to do for calls that have failed or are successful
            switch (singleTask.getState()) {
                case SUCCESS:
                case CANCELLED: //fall-through
                case FAILED: {
                    return true;
                }
                default: { }
            }

            boolean allDepsOk = true;
            for (SingleTask singleTaskDep : singleTask.getDependsOn()) {
                synchronized (singleTaskDep) {
//                    checkStateCorrectness(singleTaskDep); //not guaranteed (deps might not yet have been assigned)

                    if (singleTaskDep.getState() == SingleTask.TaskState.FAILED || singleTask.getState() == SingleTask.TaskState.CANCELLED) {
                        singleTask.setState(singleTaskDep.getState());
                        checkStateCorrectness(singleTask);
                        return true;
                    }
                    if (singleTask.isCanceledByUser()) {
                        singleTask.setState(SingleTask.TaskState.CANCELLED);
                        checkStateCorrectness(singleTask);
                        return true;
                    }

                    if (singleTaskDep.getState() == SingleTask.TaskState.UNSUBMITTED)
                        return false; //cannot continue until it is submitted

                    //new plan: if the task is successfull, OR it is not needed, the dep is OK.
                    if (singleTaskDep.getState() != SingleTask.TaskState.SUCCESS && singleTaskDep.task.needed()) {
                        if (taskdebug) System.out.println("DEBUG         singleTaskDep("+singleTaskDep+").task.needed="+singleTaskDep.task.needed()+" state="+singleTaskDep.getState()+", so allDepsOk = false");
                        allDepsOk = false;
                    }
                }
            };

            if (taskdebug) System.out.println("DEBUG         allDepsOk="+allDepsOk);

            try {
                if (allDepsOk) {
                    singleTask.setState(SingleTask.TaskState.QUEUED);
                    queuedTasks.putFirst(singleTask);
                } else {
                    singleTask.setState(SingleTask.TaskState.BLOCKED);
                    blockedTasks.putFirst(singleTask);
                }
            } catch (InterruptedException e) {
                System.err.println("Did not expect InterruptedException could occur in assignSingleTask. This is possibly a problem. It will be ignored.");
            }

            checkStateCorrectness(singleTask);

            return true;
        }
    }

    public void addTask(final Task task) {
        if (taskdebug) System.out.println("                  adding task: \""+task+"\"");

        if (task.isActive()) {
            System.err.println("ERROR: trying to add active task: " + task.getCurrentActiveSingleTasksCopy() + " -> task will not be added.");
            return;
        }

        if (taskdebug) System.out.println("                        "+ queuedTasks.size()+" queuedTasks= "+ queuedTasks);
        if (taskdebug) System.out.println("                        "+ blockedTasks.size()+" blockedTasks="+ blockedTasks);

        SingleTask singleTask = new SingleTask(task);
        task.addActiveSingleTask(singleTask);
        processNewSingleTask(singleTask);

        if (taskdebug) System.out.println("                  added task: \"" + task + "\"");
        if (taskdebug) System.out.println("                        "+ queuedTasks.size()+" queuedTasks= "+ queuedTasks);
        if (taskdebug) System.out.println("                        "+ blockedTasks.size()+" blockedTasks="+ blockedTasks);
        if (taskdebug) System.out.flush();
    }

    public void addTasks(List<Task> tasks) {
        for (Task task : tasks) {
            addTask(task);
        }
    }

    public void scheduleTask(final Task task, final long delayMs) {
        if (taskdebug) System.out.println("DEBUG TaskThread ==> DEBUG schedule task in "+delayMs+" ms: \""+task.getName()+"\".");
        SingleTask singleTask = new SingleTask(task);
        singleTask.setState(SingleTask.TaskState.FUTURE);
        task.addActiveSingleTask(singleTask);

        FutureTask futureTask = new FutureTask(singleTask);
        ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(futureTask, delayMs, MILLISECONDS);
        futureTask.setScheduledFuture(scheduledFuture);
        task.addFuture(futureTask);
    }
}
