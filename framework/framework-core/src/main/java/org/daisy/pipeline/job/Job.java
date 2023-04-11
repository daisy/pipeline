package org.daisy.pipeline.job;

import java.net.URI;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.script.Script;

/**
 * The Class Job defines the execution unit.
 */
public interface Job extends Runnable, AutoCloseable {

    public enum Status {
        IDLE,
        RUNNING,
        SUCCESS,
        ERROR,
        FAIL
    }

    /**
     * @return the job ID.
     */
    public JobId getId();

    public String getNiceName();

    public Script getScript();

    /**
     * @return the job status
     */
    public Status getStatus();

    public JobMonitor getMonitor();

    public URI getLogFile();

    public JobResultSet getResults();

    public JobBatchId getBatchId();

    public Client getClient();

    /////// AutoCloseable ///////

    /**
     * Close the job. Will clean up any resources associated with the job and will make it
     * impossible to call any methods on it.
     *
     * Call this method before discarding the job. This method should not be called when the job is
     * managed by a {@link JobManager}.
     */
    public void close();

}
