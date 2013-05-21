package it.dk.libs.task;

/**
 * A Task that can be suspended and resumed.
 */
public interface ResumableTask extends Task {

    /**
     * Suspends the task execution
     * @return true if the task has been correctly suspended
     */
    public boolean suspend();

    /**
     * Resume the task execution
     */
    public void resume();

}

