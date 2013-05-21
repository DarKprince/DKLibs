/**
 * Copyright 2012 Alfredo "Rainbowbreeze" Morresi
 * 
 * This file is part of Eureka! project.
 * 
 * Eureka! is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Eureka! is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with Eureka!. If not, see http://www.gnu.org/licenses/.
 */
package it.dk.libs.jobexecutor;


/**
 * @author Alfredo "Rainbowbreeze" Morresi
 *
 */
public class JobResult<ResultType> {
    // ------------------------------------------ Private Fields
    private final long mSubmissionId;
    private final ResultType mResultOperation;
    
    
    // -------------------------------------------- Constructors
    public JobResult(
            long submissionId,
            ResultType resultOperation) {
        mSubmissionId = submissionId;
        mResultOperation = resultOperation;
    }

    
    // --------------------------------------- Public Properties
    
    /**
     * The submission id of the job, useful to get job result data from job
     * queue.
     */
    public long getSubmissionId()
    { return mSubmissionId; }

    /**
     * Result of the job. Used only to control if the job was executed in a
     * right way or terminated with errors. True result of the job is not
     * useful and it's up to a single job to expose it, if needed.
     */
    public ResultType getResultOperation()
    { return mResultOperation; }
    
    
    // ------------------------------------------ Public Methods

    
    // ----------------------------------------- Private Methods

}
