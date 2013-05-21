package it.dk.libs.jobexecutor;

import it.dk.libs.common.ILogger;
import it.dk.libs.common.ResultOperation;

import static it.dk.libs.common.ContractHelper.checkNotNull;

/**
 * @param <ResultType>
 */
public abstract class Job {
    // ------------------------------------------ Private Fields
    protected final ILogger mBaseLogFacility;


    /** The listener where {@link JobExecutor} is attached, managed by {@link JobExecutor} */
    //TODO use a weak reference
    protected ExecutionListener mJobExecutorListener;
    

    // -------------------------------------------- Constructors
    public Job(ILogger logFacility) {
        mBaseLogFacility = checkNotNull(logFacility, ILogger.class.getSimpleName());
    }


    // ------------------------------------------ Public Classes
    /**
     * Interface for internal communication between job executor and its jobs
     */
    protected static interface ExecutionListener {
        void jobCompleted(Job job, JobResult jobResult);
    }

    
    // --------------------------------------- Public Properties
    /** Refresh all data job, only one in execution */
    public static final int JOBCATEGORY_REFRESHALL = 1;
    /** Generic network and database operation, may differs based on parameters */
    public static final int JOBCATEGORY_NETWORK_IO = 2;
    /** Send pot job */
    public static final int JOBCATEGORY_SENDPOT = 3;
    
    
    /** Unique ID  for this specific task instance, managed by {@link JobExecutor} */
    private long mSubmissionId;
    public long getSubmissionId()
    { return mSubmissionId; }

    
    // ------------------------------------------ Public Methods
    public void setJobExecutorParameters(
            long submissionId,
            ExecutionListener jobExecutorListener) {
        mSubmissionId = submissionId;
        mJobExecutorListener = jobExecutorListener;
    }
    
    /**
     * Returns a unique identifier associated with this kind of task,
     * not the specific instance of if
     */
    public abstract int getCategoryId();
    
    /**
     * True if only one job with the same category could be executed at time
     * otherwise false (more that one job of the same category at the same time).
     * @return
     */
    public abstract boolean onlyOneExecutionForCategory();
    
    /**
     * Job category is a quick way to filter out jobs of same type. If
     * required, a more particular match can be performed in order to check
     * if the new job to submit has a common characteristic with a job
     * already in the queue. If a match is found, new job is not submitted
     * and id of the similar job is returned. Jobs must have same category
     * in order to apply the filter by characteristic.
     * 
     * @return true if an additional in-deep check is needed, otherwise false
     */
    public boolean onlyOneExecutionForSameCharacteristics() {
        return false;
    }
    
    /**
     * Override to implement custom job comparison based on arbitrary
     * characteristic. Default implementation always return false.
     * 
     * @param jobToCompare the (submitted) job to compare against
     * @return true if this job has a common characteristics with the job
     *         specified as parameter. This means that this job will not be
     *         submitted.
     */
    public boolean hasSameCharacteristic(Job jobToCompare) {
        return false;
    }
    
    /**
     * Wrapper around {@link #executeJob()}, used to launch the execution
     * of a job. Do not touch it.
     */
    public void startJob() {
        mBaseLogFacility.v(getLogHash(), "Starting job \"" + getShortJobDescription() + "\" with submissionId " + getSubmissionId());
        JobResult result = executeJob();
        
        if (null != mJobExecutorListener) {
            mBaseLogFacility.v(getLogHash(), "Job " + getSubmissionId() + " executed, notify listener");
            mJobExecutorListener.jobCompleted(this, result);
        }
    }
    

    // ----------------------------------------- Private Methods
    /**
     * Returns a brief description of the task (for logging purposes)
     */
    protected abstract String getShortJobDescription();
    
    /**
     * Returns the log hash to use when logging
     * @return
     */
    protected abstract String getLogHash();
    
    /**
     * Executes the task. Put here all the necessary logic code
     * @return
     */
    protected abstract JobResult executeJob();
    
    /**
     * Creates a new {@link JobResult} with the data related to this job.
     * 
     * @param resultOperation
     * @return
     */
    protected JobResult createJobResult(ResultOperation<Void> resultOperation) {
        return new JobResult(getSubmissionId(), resultOperation);
    }
    
    /**
     * Creates a new {@link JobResult} with the data related to this job.
     * 
     * @param resultOperation
     * @return
     */
    protected JobResult createPositiveJobResult() {
        return new JobResult(getSubmissionId(), new ResultOperation<Void>());
    }
}
