package org.daisy.pipeline.job;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.Map;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.job.impl.JobMessageAccessorFromStorage;

import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobMonitorFactory {

	/**
	 * {@link JobMonitorFactory} that is not backed by a {@link JobStorage}. Only supports the
	 * {@link #newJobMonitor(JobId, MessageAccessor, StatusNotifier}} method.
	 */
	public static final JobMonitorFactory LIVE_MONITOR_FACTORY = new JobMonitorFactory(null);

	private static Logger logger = LoggerFactory.getLogger(JobMonitorFactory.class);

	// We need to cache the monitors because we aren't able to create one while a job is already
	// running. Another reason for the caching is that the message storage is not guaranteed to be
	// lossless. Notably the database can currently not store the full message tree, so the messages
	// are flattened first.
	private static final Map<MessageStorage,JobMonitorCache> LIVE_MONITORS = new ConcurrentHashMap<>();

	private final JobMonitorCache liveMonitors;
	private final MessageStorage messageStorage;

	/**
	 * Not thread-safe.
	 */
	public JobMonitorFactory(JobStorage storage) {
		if (storage != null) {
			// It is assumed that when a JobStorage is provided, it is required that job objects can
			// be re-created from the (persistent) storage (which means that monitors need to be
			// cached). If no JobStorage is provided, it is assumed that this is not required.

			messageStorage = storage.getMessageStorage();
			if (LIVE_MONITORS.containsKey(messageStorage))
				liveMonitors = LIVE_MONITORS.get(messageStorage);
			else {
				// This is the first time a JobMonitorFactory is created (for the given MessageStorage,
				// of which there should be only one).
				liveMonitors = new JobMonitorCache();
				LIVE_MONITORS.put(messageStorage, liveMonitors);
			}
		} else {
			messageStorage = null;
			liveMonitors = null;
		}
	}

	/**
	 * Get the monitor for a job.
	 *
	 * If a "live" monitor (for monitoring the job while it runs) is present in memory, always
	 * returns it. Otherwise loads the messages from storage.
	 */
	public JobMonitor newJobMonitor(JobId id) {
		if (liveMonitors == null)
			// should not happen: JobMonitorFactory.newJobMonitor(JobId) is only called in the
			// context of a JobStorage, in which case a cache has been created
			throw new IllegalStateException();
		else {
			JobMonitor m = liveMonitors.get(id);
			// If a monitor is cached, return it.
			if (m != null)
				return m;
			// Otherwise load the messages from storage (assumes the job has finished).
			else {
				MessageAccessor messageAccessor = new JobMessageAccessorFromStorage(id, messageStorage);
				StatusNotifier statusNotifier = new StatusNotifier() {
						public void listen(Consumer<Job.Status> listener) {}
						public void unlisten(Consumer<Job.Status> listener) {}
					};
				return new JobMonitorImpl(messageAccessor, statusNotifier);
			}
		}
	}

	/**
	 * Create a "live" job monitor from a given MessageAccessor and StatusNotifier. To be used for
	 * newly created jobs. Assumes job is idle.
	 */
	public JobMonitor newJobMonitor(JobId id, MessageAccessor messageAccessor, StatusNotifier statusNotifier) {
		JobMonitor monitor = new JobMonitorImpl(messageAccessor, statusNotifier);
		if (liveMonitors != null) {
			if (liveMonitors.contains(id))
				throw new IllegalArgumentException(); // a monitor was already registered for this job
			else {
				logger.trace("Registered JobMonitor for job " + id);
				liveMonitors.put(id, monitor);
			}
		}
		return monitor;
	}

	private static class JobMonitorImpl implements JobMonitor {

		private final MessageAccessor accessor;
		private final StatusNotifier notifier;

		JobMonitorImpl(MessageAccessor accessor, StatusNotifier notifier) {
			this.accessor = accessor;
			this.notifier = notifier;
		}

		@Override
		public MessageAccessor getMessageAccessor() {
			return accessor;
		}

		@Override
		public StatusNotifier getStatusUpdates() {
			return notifier;
		}
	}

	private class JobMonitorCache {

		/**
		 * Monitors for jobs that are idle or running.
		 */
		private final Map<JobId,JobMonitor> idleOrRunning = new ConcurrentHashMap<>();

		/**
		 * Monitors for jobs that are finished less than 60 seconds ago.
		 */
		private final Map<JobId,JobMonitor> finished;

		JobMonitorCache() {
			// Use this property only for testing! Use to configure how long finished messages are cached
			// before storing them in a MessageStorage (volatile of persistent).
			int timeout = Integer.valueOf(Properties.getProperty("org.daisy.pipeline.messaging.cache.buffer", "60"));
			finished = CacheBuilder.newBuilder()
			                       .expireAfterAccess(timeout, TimeUnit.SECONDS)
			                       .<JobId,JobMonitor>build()
			                       .asMap();
		}

		/**
		 * Assumes job is idle
		 */
		synchronized void put(JobId job, JobMonitor monitor) {
			idleOrRunning.put(job, monitor);
			// remove monitor from map when job finishes
			monitor.getStatusUpdates().listen(s -> {
					switch (s) {
					case SUCCESS:
					case FAIL:
					case ERROR:
						// store buffered messages to memory or database
						logger.trace("Persisting messages to " + messageStorage);
						for (Message m : monitor.getMessageAccessor().getAll())
							messageStorage.add(m);
						// keep in finished map for 60 seconds
						finished.put(job, monitor);
						// remove from idleOrRunning only after it has been added to finished to avoid races
						idleOrRunning.remove(job);
					default:
					}});
		}

		synchronized boolean contains(JobId job) {
			return idleOrRunning.containsKey(job) || finished.containsKey(job);
		}

		synchronized JobMonitor get(JobId job) {
			JobMonitor m = idleOrRunning.get(job);
			if (m != null)
				return m;
			else
				return finished.get(job);
		}
	}
}
