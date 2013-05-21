package it.dk.libs.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows generic tasks to be scheduled using specific priorities.
 * Tasks scheduled with higher priorities cause running tasks to be suspended
 * and then resumed once they completes. This can happens only if the running
 * task is a ResumableTask, otherwise it will be enqueued and will start once
 * the running task completes.
 * Using setMaxThreads you can set the number of threads that can be used to
 * schedule tasks. Tasks are suspended when there are no more threads available
 * to run a specific task with higher priority.
 */
public class TaskExecutor {
    // ------------------------------------------ Private Fields
    private final int DEFAULT_MAX_THREADS_COUNT = 3;

    /** List of current running tasks */
    private final List<ThreadedTask> mTasksThreadPool;
    /** List of task enqueued waiting for execution */
    private final TaskQueue mTaskQueue = new TaskQueue();
    private int mMaxThreads = DEFAULT_MAX_THREADS_COUNT;
    private int mSystemPriority = Thread.NORM_PRIORITY;

    
    // -------------------------------------------- Constructors
    public TaskExecutor() {
        mTasksThreadPool = new ArrayList<ThreadedTask>();
    }

    public TaskExecutor(int systemPriority) {
        this();
        this.mSystemPriority = systemPriority;
    }

    
    // --------------------------------------- Public Properties
    public static final int PRIORITY_LOW = 0;
    

    /**
     * Set the max number of threads used to schedule tasks.
     * @param count
     */
    public void setMaxThreads(int count) {
        mMaxThreads = count;
    }

    
    // ------------------------------------------ Public Methods
    /**
     * Schedule the given task with low priority.
     * @param task
     */
    public void scheduleTask(Task task) {
        scheduleTaskWithPriority(task, PRIORITY_LOW);
    }


    /**
     * Schedule the given task with the given priority.
     * @param task
     * @param priority
     */
    public void scheduleTaskWithPriority(Task task, int priority) {
        synchronized(mTasksThreadPool) {
            PriorityTask ptask = new PriorityTask(task, priority);
            boolean started = startNewThreadedTask(ptask);
            if(!started) {
                enqueueTask(ptask);
                // If there is no room to run the given task, then we start looking
                // for a lower priority task to suspend.
                for(ThreadedTask threadedTask : mTasksThreadPool) {
                    if((threadedTask.getPriorityTask().getPriority() < ptask.getPriority())) {
                        if(threadedTask.isResumable() && threadedTask.suspend()) {
                            // Enqueue suspended task
                            enqueueTask(threadedTask.getPriorityTask());
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * This method scans all the tasks in the executor and returns true if it
     * finds one with the same id.
     */
    public Task findTask(Task task) {
        synchronized(mTasksThreadPool) {
            synchronized(mTaskQueue) {
                for(ThreadedTask ttask : mTasksThreadPool) {
                    if (ttask.getPriorityTask().getId().equals(task.getId())) {
                        return ttask.getPriorityTask().getTask();
                    }
                }
                return mTaskQueue.find(task);
            }
        }
    }

    public void changePriority(Task task, int priority) {
        // Depending on the task status, we need to do different things
        // First of all find the priority task associated
        synchronized(mTasksThreadPool) {
            synchronized(mTaskQueue) {
                for(ThreadedTask ttask : mTasksThreadPool) {
                    if (ttask.getPriorityTask().getId().equals(task.getId())) {
                        ttask.getPriorityTask().setPriority(priority);
                        return;
                    }
                }
                mTaskQueue.changePriority(task, priority);
            }
        }
    }


    // ----------------------------------------- Private Methods
    private void enqueueTask(PriorityTask task) {
        synchronized(mTaskQueue) {
            mTaskQueue.put(task);
        }
    }

    private boolean startNewThreadedTask(PriorityTask task) {
        synchronized(mTasksThreadPool) {
            if(mTasksThreadPool.size() < mMaxThreads) {
                ThreadedTask threadedTask = new ThreadedTask(task);
                mTasksThreadPool.add(threadedTask);
                threadedTask.start();
                return true;
            } else {
                return false;
            }
        }
    }
    
    private void taskCompleted(ThreadedTask threadedTask) {
        synchronized(mTasksThreadPool) {
            // Make room and run a new task
            mTasksThreadPool.remove(threadedTask);
            synchronized(mTaskQueue) {
                PriorityTask next = mTaskQueue.get();
                if(next != null) {
                    if(!startNewThreadedTask(next)) {
                        enqueueTask(next);
                    }
                }
            }
        }
    }

    

    // ----------------------------------------- Private Classes

    private class ThreadedTask {

        private PriorityTask ptask;
        private TaskRunnable taskRunnable;
        private Thread taskThread;

        public ThreadedTask(PriorityTask task) {
            this.ptask = task;
            this.taskRunnable = new TaskRunnable(this);
            this.taskThread = new Thread(taskRunnable);
            this.taskThread.setPriority(mSystemPriority);
        }

        public PriorityTask getPriorityTask() {
            return ptask;
        }

        /**
         * Start the task from a new thread
         */
        public void start() {
            taskThread.start();
        }

        /**
         * @return whether the current task is resumable
         */
        public boolean isResumable() {
            return (ptask.getTask() instanceof ResumableTask);
        }

        /**
         * Suspend the thread task
         * @return true if the task has been correctly suspended
         */
        public boolean suspend() {
            if(isResumable()) {
                boolean suspended = ((ResumableTask)ptask.getTask()).suspend();
                ptask.setSuspended(suspended);
                return suspended;
            } else {
                return false;
            }
        }
    }

    private class TaskRunnable implements Runnable {

        private ThreadedTask threadedTask;

        public TaskRunnable(ThreadedTask threadedTask) {
            this.threadedTask = threadedTask;
        }

        public void run() {
            PriorityTask ptask = threadedTask.getPriorityTask();
            // Resume task if it has been previously suspended
            try {
                if(ptask.isSuspended() && threadedTask.isResumable()) {
                    ptask.setSuspended(false);
                    ResumableTask resumableTask = ((ResumableTask)ptask.getTask());
                    resumableTask.resume();
                } else {
                    ptask.getTask().run();
                }
            } finally {
                taskCompleted(threadedTask);
            }
        }
    }

    private class TaskQueue {
        
        private final List<PriorityTask> mInternalQueue;
        
        public TaskQueue() {
            mInternalQueue = new ArrayList<PriorityTask>();
        }

        public PriorityTask get() {
            if(mInternalQueue.size() > 0) {
                PriorityTask pt = (PriorityTask)mInternalQueue.get(0);
                mInternalQueue.remove(0);
                return pt;
            } else {
                return null;
            }
        }

        public void put(PriorityTask task) {
            int index = 0;
            for(; index<mInternalQueue.size(); index++) {
                PriorityTask pt = mInternalQueue.get(index);
                if(pt.getPriority() < task.getPriority()) {
                    break;
                }
            }
            mInternalQueue.add(index, task);
        }

        public Task find(Task task) {
            for(PriorityTask pt : mInternalQueue) {
                if (task.getId().equals(pt.getTask().getId())) {
                    return pt.getTask();
                }
            }
            return null;
        }

        public void changePriority(Task task, int priority) {
            for(int i=0; i<mInternalQueue.size(); ++i) {
                PriorityTask pt = (PriorityTask)mInternalQueue.get(i);
                if (task.getId().equals(pt.getTask().getId())) {
                    // We need to change its position
                    mInternalQueue.remove(i);
                    pt.setPriority(priority);
                    put(pt);
                    return;
                }
            }
        }
    }

    private class PriorityTask {

        private Task mTask;
        private int mPriority;
        private boolean mSuspended;

        public PriorityTask(Task task, int priority) {
            this.mTask = task;
            this.mPriority = priority;
            this.mSuspended = false;
        }

        public Task getTask() {
            return mTask;
        }

        public int getPriority() {
            return mPriority;
        }

        public boolean isSuspended() {
            return mSuspended;
        }

        public void setSuspended(boolean suspended) {
            this.mSuspended = suspended;
        }

        public String getId() {
            return mTask.getId();
        }

        public void setPriority(int priority) {
            this.mPriority = priority;
        }
    }
}

