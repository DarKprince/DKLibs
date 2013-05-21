package it.dk.libs.task;

/**
 * A generic task that can be scheduled by the TaskExecutor with a specific 
 * priority.
 */
public interface Task {

    /**
     * Run the task
     */
    public void run();

    /**
     * Returns an id for this task. This id is used to compare tasks and check
     * if a given task has already been submitted to the TaskExecutor.
     */
    public String getId();
}

