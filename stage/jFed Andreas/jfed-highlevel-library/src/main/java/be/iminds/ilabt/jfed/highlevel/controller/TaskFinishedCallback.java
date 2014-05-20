package be.iminds.ilabt.jfed.highlevel.controller;

/**
 * TaskFinishedCallback
 */
public interface TaskFinishedCallback {
    public void onTaskFinished(TaskThread.Task task, TaskThread.SingleTask singleTask, TaskThread.SingleTask.TaskState state);
}
