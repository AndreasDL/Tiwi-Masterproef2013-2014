package be.iminds.ilabt.jfed.highlevel.controller;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JavaFXTaskThread provides a JavaFX model for the data in the TaskThread.
 *   TaskThread contains no JavaFX code, but does contain some helpers to make the implementation of this class easier (see the javafx fields)
 *
 * periodically, this model is updated so it reflects the situation in TaskThread.
 *
 * This method of showing everything in JavaFX is some extra work, but it simplifies things.
 * JavaFX isn't very easy to use with multiple threads otherwise. (it's hard to use ObservableList and properties from other threads.)
 *
 * Performance could be improved if required. Some hints:
 *    - use a "dirty" flag on TaskThread objects so that only changed objects are updated
 *    - alternatively use lists with a builtin "dirty" flag  (boolean javafx_dirty gets set to true by each changes, while this thread sets it to false after update)
 *    - take into account that some lists only get added to, so no checks are needed as long as size doesn't change
 *    - take into account that some objects start null, get a value assigned, and never change after that (exception, start and stop date, etc)
 *    - check if ObservableList.setAll is smart enough to see a list is unchanged or not (now assumed it isn't)
 *    - trigger updates only when things are changed? (but still at limited rate)
 */
public class JavaFXTaskThread {
    private static boolean taskdebug = false;

    private final TaskThread real = TaskThread.getInstance();

    private JavaFXTaskThread() {
        assert instance == null;
        instance = this;

        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Object lock = "lock";

                while (true) {
                    //max 5 times per second
                    try { Thread.sleep(200); } catch (InterruptedException e) { }

                    synchronized (lock) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (lock) {
                                    update();
                                    lock.notifyAll();
                                }
                            }
                        });

                        //wait until update complete (important if update takes longer than wait)
                        try { lock.wait(); } catch (InterruptedException e) { e.printStackTrace(); }
                    }
                }
            }
        });
        if (taskdebug) System.out.println("Starting JavaFXTaskThread periodic updates");
        updateThread.start();
    }
    private static JavaFXTaskThread instance = null;
    public static JavaFXTaskThread getInstance() {
        if (instance == null) new JavaFXTaskThread();
        assert instance != null;
        return instance;
    }

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<SingleTask> singleTasks = FXCollections.observableArrayList();
    private final ObservableList<FutureTask> futureTasks = FXCollections.observableArrayList();

    private final IntegerProperty unfinishedCallsCount = new SimpleIntegerProperty();
    private final BooleanExpression busy = unfinishedCallsCount.greaterThan(0);

    private int taskdebug_helper_PreviousRealAllTasksSize = -1;

    public void update() {
        int unfinishedCount = 0;
        //create new FutureTask, SingleTask and Tasks
        if (taskdebug && taskdebug_helper_PreviousRealAllTasksSize != real.allTasks.size())
            System.out.println("DEBUG: JavaFXTaskThread periodic update: real.allTasks.size()=="+real.allTasks.size());
        for (TaskThread.SingleTask singleTask : real.allTasks) {
            TaskThread.Task task = singleTask.task;

            TaskThread.SingleTask.TaskState state = singleTask.getState();
            if (state == TaskThread.SingleTask.TaskState.QUEUED
                    || state == TaskThread.SingleTask.TaskState.QUEUED
                    || state == TaskThread.SingleTask.TaskState.RUNNING)
                unfinishedCount++;

            if (task.javafx == null) {
                task.javafx = new Task(task);
                tasks.add(task.javafx);
            }

            for (TaskThread.FutureTask futureTask : task.futureTasks)
                if (futureTask.javafx == null) {
                    futureTask.javafx = new FutureTask(futureTask);
                    futureTasks.add(futureTask.javafx);
                }

            if (singleTask.javafx == null) {
                singleTask.javafx = new SingleTask(singleTask, singleTask.getName(), singleTask.getId());
                singleTasks.add(singleTask.javafx);
            }
        }
        unfinishedCallsCount.set(unfinishedCount);

        if (taskdebug && taskdebug_helper_PreviousRealAllTasksSize != real.allTasks.size())
            System.out.println("DEBUG: JavaFXTaskThread periodic update:"+
                    "                                                   "+
                    "  tasks.size()=="+tasks.size()+
                    "  singleTasks.size()=="+singleTasks.size()+
                    "  futureTasks.size()=="+futureTasks.size());
        taskdebug_helper_PreviousRealAllTasksSize = real.allTasks.size();

        //update existing tasks
        for (Task task : tasks)
            task.update();
        for (SingleTask st : singleTasks)
            st.update();
        for (FutureTask ft : futureTasks)
            ft.update();
    }


    public void cancel(SingleTask task) {
        real.cancel(task.real);
    }

    public Task getJavaFXTaskByTask(TaskThread.Task task) {
        assert Platform.isFxApplicationThread();

        if (task.javafx == null)
            update();
        assert task.javafx != null;

        return task.javafx;
    }

    private static <T> void syncList(List<T> orig, ObservableList<T> target) {
        boolean changed = false;
        if (orig.size() == target.size())
            for (int i = 0; i < orig.size(); i++) {
                if (orig.get(i) != target.get(i)) {
                    changed = true;
                    break;
                }
            }
        else
            changed = true;

        if (changed)
            target.setAll(orig);
    }

    public class FutureTask {
        private final TaskThread.FutureTask real;

        private LongProperty timeLeftMs = new SimpleLongProperty();

        public void update() {
            timeLeftMs.set(real.getTimeLeftMs());
        }

        public FutureTask(TaskThread.FutureTask real) {
            this.real = real;
        }

        public ObservableLongValue getTimeLeftMs() {
            return timeLeftMs;
        }
    }

    public class Task {
        private final TaskThread.Task real;

        private ObservableList<FutureTask> futures = FXCollections.observableArrayList();
        private ObservableList<SingleTask> currentActiveSingleTasks = FXCollections.observableArrayList();

        public Task(TaskThread.Task real) {
            this.real = real;
        }

        public void update() {
            List<SingleTask> newCurrentActiveSingleTasks = new ArrayList<SingleTask>();
            for (TaskThread.SingleTask st : real.getCurrentActiveSingleTasksCopy()) {
                if (st.javafx == null) continue;
                newCurrentActiveSingleTasks.add(st.javafx);
            }
            syncList(currentActiveSingleTasks, currentActiveSingleTasks);


            List<FutureTask> newFutures = new ArrayList<FutureTask>();
            for (TaskThread.FutureTask ft : real.getFuturesCopy()) {
                if (ft.javafx == null) continue;
                newFutures.add(ft.javafx);
            }
            syncList(newFutures, futures);
        }

        public ObservableList<SingleTask> getCurrentActiveSingleTasks() {
            return currentActiveSingleTasks;
        }

        public ObservableList<FutureTask> getFutures() {
            return futures;
        }
    }

    public class SingleTask {
        private final TaskThread.SingleTask real;

        private final String name;
        private final String id;
        private final ObjectProperty<TaskThread.SingleTask.TaskState> state = new SimpleObjectProperty<TaskThread.SingleTask.TaskState>();
        private final ObservableList<SingleTask> observableDependsOn = FXCollections.observableArrayList();
        private final ObservableList<SingleTask> observableDependingOnThis = FXCollections.observableArrayList();
        private final ObservableList<ApiCallDetails> apiCallHistory = FXCollections.observableArrayList();
        private final ObjectProperty<Date> runStart = new SimpleObjectProperty<Date>();
        private final ObjectProperty<Date> runStop = new SimpleObjectProperty<Date>();
        private final BooleanProperty cancelled = new SimpleBooleanProperty(false);
        private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<Throwable>();

        public SingleTask(TaskThread.SingleTask real, String name, String id) {
            this.real = real;
            this.name = name;
            this.id = id;
            update();
        }

        public void update() {
            synchronized (real) {
                state.set(real.getState());
                syncList(real.getApiCallHistory(), apiCallHistory);
                runStart.set(real.getRunStart());
                runStop.set(real.getRunStop());
                cancelled.set(real.isCanceledByUser());
                exception.set(real.getException());


                List<SingleTask> newDeps = new ArrayList<SingleTask>();
                for (TaskThread.SingleTask dep : real.getDependsOn()) {
                    if (dep.javafx == null) continue;
                    newDeps.add(dep.javafx);
                }
                syncList(newDeps, observableDependsOn);

                List<SingleTask> newDepOn = new ArrayList<SingleTask>();
                for (TaskThread.SingleTask dep : real.getDependingOnThis()) {
                    if (dep.javafx == null) continue;
                    newDepOn.add(dep.javafx);
                }
                syncList(newDepOn, observableDependingOnThis);
            }
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public ObjectProperty<TaskThread.SingleTask.TaskState> stateProperty() {
            return state;
        }
        public TaskThread.SingleTask.TaskState getState() {
            return state.get();
        }

        public ObjectProperty<Date> runStartDateProperty() {
            return runStart;
        }

        public ObjectProperty<Date> runStopDateProperty() {
            return runStop;
        }

        public ObservableList<SingleTask> getObservableDependsOn() {
            return observableDependsOn;
        }

        public ObservableList<SingleTask> getObservableDependingOnThis() {
            return observableDependingOnThis;
        }

        public ObservableList<ApiCallDetails> getApiCallHistory() {
            return apiCallHistory;
        }

        public ObservableValue<Boolean> completedProperty() {
            return state.isEqualTo(TaskThread.SingleTask.TaskState.FAILED).or(state.isEqualTo(TaskThread.SingleTask.TaskState.SUCCESS));
        }

        public ObservableValue<Boolean> cancelledByUserProperty() {
            return cancelled;
        }

        public ObjectProperty<Throwable> getException() {
            return exception;
        }
    }

    public ObservableIntegerValue unfinishedCallsCountProperty() {
        return unfinishedCallsCount;
    }

    public BooleanExpression busyProperty() {
        return busy;
    }

    public ObservableList<JavaFXTaskThread.SingleTask> getAllTasks() {
        return singleTasks;
    }
}
