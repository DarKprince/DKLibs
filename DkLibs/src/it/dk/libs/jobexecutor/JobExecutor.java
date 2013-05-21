package it.dk.libs.jobexecutor;

import it.dk.libs.common.Logger;
import it.dk.libs.common.RainbowPriorityQueue;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import static it.dk.libs.common.ContractHelper.checkNotNull;

public class JobExecutor {
    // ------------------------------------------ Private Fields
    private static final String LOG_HASH = JobExecutor.class.getSimpleName();
    
    private final Logger mBaseLogFacility;

    /** List of jobs submitted, in execution, but not terminated */
    private final List<Job> mJobsPool;
    
    /** List of jobs submitted, but still not executed */
    private final PriorityQueue mJobsQueue;
    
    /** keep progressive job counter */
    private static long mSubmissionIdCounter = 0;
    
    /** Number of max concurrent jobs */
    private int mMaxConcurrentJobs = 2;
    
    /** Executes the jobs */
    private final Executor mExecutor;

    /** Queue of listeners to job competition */
    private HashMap<String, JobExecutorListener> mJobExecutorListeners;
    
    
    // -------------------------------------------- Constructors
    public JobExecutor(Logger logFacility) {
        mBaseLogFacility = checkNotNull(logFacility, "LogFacility");
        mJobExecutorListeners = new HashMap<String, JobExecutorListener>();
        mJobsPool = new ArrayList<Job>();
        mExecutor = new ThreadPerTaskExecutor();
        mJobsQueue = new PriorityQueue();
    }

    
    // --------------------------------------- Public Properties
    /** Job not found */
    public static final long SUBMITTED_JOB_NOT_FOUND = -1;
    
    /** Lowest priority, maintenance jobs */
    public static final int PRIORITY_LOWEST = PriorityQueue.PRIORITY_LOW;
    /** Add your priorities here */
    public static final int PRIORITY_USER_FIRST = PriorityQueue.PRIORITY_LOW + 1;
    
    
    /**
     * Sets the number of max jobs in concurrent execution
     */
    public void setMaxConcurrentJobs(int newValue) {
        if (newValue > 0) mMaxConcurrentJobs = newValue;
    }

    
    // --------------------------------------- Public Interfaces
    /**
     * Listener called when a job ends its execution
     */
    public static interface JobExecutorListener {
        /**
         * Called when a job finish its execution
         * @param jobResult
         */
        public void jobCompleted(JobResult jobResult);
    }

    
    // ------------------------------------------ Public Methods
    
    /**
     * 
     * @param listenerKey a key used to identify the listener during its removal
     *        from the queue
     * @param listener
     */
    public void registerListener(String listenerKey, JobExecutorListener listener) {
        mJobExecutorListeners.put(listenerKey, listener);
    }
    
    /**
     * Remove a listener from the listeners queue
     * 
     * @param listenerKey same key used during {@link #registerListener(String, JobExecutorListener)}
     *        call
     */
    public void deregisterListener(String listenerKey) {
        if (mJobExecutorListeners.containsKey(listenerKey)) {
            mJobExecutorListeners.remove(listenerKey);
        }
    }
    
    /**
     * Submit a new job with lowest priority. Depends on current execution
     * queue and max allowed concurrent jobs, the new job can be run
     * immediately or scheduled for further execution.
     * 
     * @param jobToExecute
     * @return id of the submitted job
     */
    public long submitJob(Job jobToExecute) {
        return submitJob(jobToExecute, RainbowPriorityQueue.PRIORITY_LOW);
    }

    /**
     * Submit a new job with a given priority. Depends on current execution
     * queue and max allowed concurrent jobs, the new job can be run
     * immediately or scheduled for further execution.
     * 
     * @param job
     * @return id of the submitted job
     */
    public long submitJob(Job job, int priority) {
        if (null == job) throw new InvalidParameterException("Cannot pass a null job");
        
        //first check, same job was already submitted
        
        //check if the new job could be submitted or a job with the same category has been
        //already submitted and is running
        if (job.onlyOneExecutionForCategory()) {
            long jobId = findJobWithSameCategory(job.getCategoryId());
            if (SUBMITTED_JOB_NOT_FOUND != jobId) return jobId;
        }
        
        if (job.onlyOneExecutionForSameCharacteristics()) {
            long jobId = findJobWithSameCharacteristics(job);
            if (SUBMITTED_JOB_NOT_FOUND != jobId) return jobId;
        }
        
        //add new submission id to the task and sets its listener
        long submissionId = getNewSubmissionId();
        job.setJobExecutorParameters(submissionId, mJobListener);

        //enqueue the job
        mBaseLogFacility.v(LOG_HASH, "Enqueued new job of type " + job.getCategoryId() + " with submission id " + job.getSubmissionId() + " and priority " + priority);
        synchronized (mJobsQueue) {
            mJobsQueue.put(job, priority);
        }
        
        //and, in case, executes it
        startEnqueuedJob();
        
        //returns the external listener
        return submissionId;
    }
    
    
    /**
     * Checks if a specific job was already submitted, and not terminated.
     * 
     * @param submissionId the submission id of the job
     * 
     * @return true if the job was executed, otherwise false
     */
    public boolean isJobAlreadySubmittedById(long submissionId) {
        synchronized (mJobsPool) { synchronized (mJobsQueue) {
            //checks if there is already a job with same category in the processing queue
            for (Job job : mJobsPool) {
                if (jobHasSameId(submissionId, job)) return true;
            }
            return mJobsQueue.isJobAlreadySubmittedById(submissionId);
        } }
    }
    
    /**
     * Checks if a job of a given category is already in the submission queue
     * 
     * @param categoryId category id to check
     * @return the id of the submitted job, otherwise {@link JobExecutor#SUBMITTED_JOB_NOT_FOUND}
     */
    public long findJobWithSameCategory(int categoryId) {
        long submissionId;
        //checks for a corresponding job in the list of running jobs
        synchronized (mJobsPool) { synchronized (mJobsQueue) {
            for (Job job : mJobsPool) {
                submissionId = compareForJobWithSameCategory(categoryId, job);
                if (submissionId > SUBMITTED_JOB_NOT_FOUND) return submissionId;
            }
            return mJobsQueue.findJobWithSameCategory(categoryId);
        } }
    }
    
    /**
     * Checks if a submitted job has the same characteristics than the given job
     * 
     * @param jobToCompare
     * @return the id of the submitted job, otherwise {@link JobExecutor#SUBMITTED_JOB_NOT_FOUND}
     */
    public long findJobWithSameCharacteristics(Job jobToCompare) {
        long submissionId;
        //checks for a corresponding job in the list of running jobs
        synchronized (mJobsPool) { synchronized (mJobsQueue) {
            for (Job job : mJobsPool) {
                submissionId = compareForJobWithSameCharacteristics(jobToCompare, job);
                if (submissionId > SUBMITTED_JOB_NOT_FOUND) return submissionId;
            }
            return mJobsQueue.findJobWithSameCharacteristics(jobToCompare);
        } }
    }
    
    /**
     * Removes all jobs in queue, only current job remains
     */
    public void stopAllEnqueuedJobs() {
        synchronized (mJobsQueue) {
            mBaseLogFacility.v(LOG_HASH, "Removing all " + mJobsQueue.size()  + " jobs from queue");
            mJobsQueue.clear();
        }
    }
    
    
    // ----------------------------------------- Private Methods
    private Job.ExecutionListener mJobListener = new Job.ExecutionListener() {
        public void jobCompleted(Job job, JobResult jobResult) {
            mBaseLogFacility.v(LOG_HASH, "Received end of execution notification from job " + job.getSubmissionId());
            synchronized (mJobsPool) {
                //remove job from pool of running jobs
                mJobsPool.remove(job);
                notifyListeners(jobResult);
            }
            //checks if a new job must be started
            startEnqueuedJob();
        }
    };
    
    /**
     * Generate a new submission id value
     * @return
     */
    private synchronized long getNewSubmissionId() {
        return ++mSubmissionIdCounter;
    }

    /**
     * Notifies the attached listeners about the job completition
     * @param jobResult
     */
    private void notifyListeners(JobResult jobResult) {
        for (JobExecutorListener listener : mJobExecutorListeners.values()) {
            if (null != listener) {
                listener.jobCompleted(jobResult);
            }
        }
    }

    /**
     * Picks a job from the queue and starts it, if there are available
     * resources in the jobs' pool
     */
    private void startEnqueuedJob() {
        synchronized (mJobsPool) {
            if (mJobsPool.size() >= mMaxConcurrentJobs) {
                //nothing to do, jobs pool is full
                return;
            }
        }
        
        //gets a new job to execute, or exists if no job can be executed
        Job job = null;
        while (job == null) {
            synchronized (mJobsQueue) {
                //no additional jobs to execute
                if (mJobsQueue.size() == 0) return;
                //get new job from the queue
                job = mJobsQueue.get();
            }
        }
        
        final Job jobToExecute = job;
        //and executes it
        mBaseLogFacility.v(LOG_HASH, "Starting job of type " + jobToExecute.getCategoryId() + " with submission id " + jobToExecute.getSubmissionId());
        //add the task to the running queue
        synchronized (mJobsPool) {
            mJobsPool.add(jobToExecute);
        }
        
        mExecutor.execute(new Runnable() {
            public void run() {
                // At the end of job execution, a call to job registered listener is performed
                jobToExecute.startJob();
            }
        });
    }

    /**
     * Checks if a specific job was already submitted, and not terminated.
     * 
     * @param submissionId the submission id of the job
     * @param jobInList the job to compare against
     * 
     * @return true if the job was executed, otherwise false
     */
    private boolean jobHasSameId(long submissionId, Job jobInList) {
        if (null == jobInList) return false;
        if (submissionId == jobInList.getSubmissionId()) {
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a job has a given category
     * 
     * @param categoryId category id to check
     * @param jobInList the job to compare against
     * 
     * @return the id of the submitted job, otherwise {@link JobExecutor#SUBMITTED_JOB_NOT_FOUND}
     */
    private long compareForJobWithSameCategory(int categoryId, Job jobInList) {
        if (null == jobInList) return SUBMITTED_JOB_NOT_FOUND;
        return (categoryId == jobInList.getCategoryId())
                ? jobInList.getSubmissionId()
                : SUBMITTED_JOB_NOT_FOUND;
    }

    /**
     * Checks if a submitted job has the same characteristics than the given job
     * 
     * @param jobToCompare the job to compare
     * @param jobInList the job to compare against
     * 
     * @return the id of the submitted job, otherwise {@link JobExecutor#SUBMITTED_JOB_NOT_FOUND}
     */
    private long compareForJobWithSameCharacteristics(
            Job jobToCompare,
            Job jobInList) {
        if (null == jobInList) return SUBMITTED_JOB_NOT_FOUND;
        if (jobToCompare.getCategoryId() == jobInList.getCategoryId()) {
            if (jobToCompare.hasSameCharacteristic(jobInList)) return jobInList.getSubmissionId();
        }
        return SUBMITTED_JOB_NOT_FOUND;
    }

    
    // ----------------------------------------- Private Classes

    /**
     * Simplest {@link Executor} implementation. Used because I wanna like to
     * user that paradigm into my JobExecutor.
     * Ok, real motivation is that
     * 
     */
    class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }
    
    
    /**
     * Adds search capabilities thru jobs
     * @author Alfredo "Rainbowbreeze" Morresi
     *
     */
    class PriorityQueue extends RainbowPriorityQueue<Job> {

        public long findJobWithSameCharacteristics(Job jobToCompare) {
            synchronized (mInternalQueue) {
                for(PriorityItem item:mInternalQueue) {
                    long submissionId = compareForJobWithSameCharacteristics(jobToCompare, item.getItem());
                    if (submissionId > SUBMITTED_JOB_NOT_FOUND) return submissionId;
                }
            }
            return SUBMITTED_JOB_NOT_FOUND;
        }

        public boolean isJobAlreadySubmittedById(long submissionId) {
            synchronized (mInternalQueue) {
                //checks if there is already a job with same category in the processing queue
                for (PriorityItem item : mInternalQueue) {
                    if (jobHasSameId(submissionId, item.getItem())) return true;
                }
            }
            return false;
        }

        public long findJobWithSameCategory(int categoryId) {
            synchronized (mInternalQueue) {
                for(PriorityItem item:mInternalQueue) {
                    long submissionId = compareForJobWithSameCategory(categoryId, item.getItem());
                    if (submissionId > SUBMITTED_JOB_NOT_FOUND) return submissionId;
                }
            }
            return SUBMITTED_JOB_NOT_FOUND;
        }
        
    }}
