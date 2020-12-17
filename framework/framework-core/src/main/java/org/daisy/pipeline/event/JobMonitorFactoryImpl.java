package org.daisy.pipeline.event;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.StatusNotifier;

import com.google.common.cache.CacheBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "job-monitor",
	service = { JobMonitorFactory.class }
)
public class JobMonitorFactoryImpl implements JobMonitorFactory {

	private static Logger logger = LoggerFactory.getLogger(JobMonitorFactoryImpl.class);

	@Override
	public JobMonitor newJobMonitor(JobId id) {
		return new JobMonitorImpl(id.toString());
	}

	@Override
	public JobMonitor newJobMonitor(JobId id, MessageAccessor messageAccessor, StatusNotifier statusNotifier) {
		return new JobMonitorImpl(id.toString(), messageAccessor, statusNotifier);
	}

	private MessageStorage messageStorage;
	
	@Reference(
		name = "message-storage",
		unbind = "-",
		service = MessageStorage.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setMessageStorage(MessageStorage storage) {
		this.messageStorage = storage;
	}

	// We need to cache the monitors because we aren't able to create one while a job is already running. Another reason for the caching is that the message
	// storage is not guaranteed to be lossless. Notably the database can currently not store the
	// full message tree, so the messages are flattened first.
	private final Map<String,JobMonitor> liveMonitors;

	public JobMonitorFactoryImpl() {
		// use this property to configure how long messages are cached before storing them in a MessageStorage (volatile of persistent)
		// use only for testing!
		// to configure how long messages are cached in the volatile storage, use org.daisy.pipeline.messaging.cache
		int timeout = Integer.valueOf(Properties.getProperty("org.daisy.pipeline.messaging.cache.buffer", "60"));
		liveMonitors = CacheBuilder.newBuilder()
			.expireAfterAccess(timeout, TimeUnit.SECONDS)
			.removalListener(
				notification -> {
					JobMonitor mon = (JobMonitor)notification.getValue();
					// store buffered messages to memory or database
					logger.trace("Persisting messages to " + JobMonitorFactoryImpl.this.messageStorage);
					for (Message m : mon.getMessageAccessor().getAll())
						JobMonitorFactoryImpl.this.messageStorage.add(m); })
			.<String,JobMonitor>build()
			.asMap();
	}

	private final class JobMonitorImpl implements JobMonitor {

		private final JobMonitor monitor;

		private JobMonitorImpl(String id) {
			// If a monitor is cached, return it.
			if (liveMonitors.containsKey(id))
				monitor = liveMonitors.get(id);
			// Otherwise load the messages from storage (assumes the job has finished).
			else {
				liveMonitors.remove(id); // this is needed to trigger removal listener (why?)
				MessageAccessor messageAccessor = new MessageAccessorFromStorage(id, messageStorage);
				StatusNotifier statusNotifier = new StatusNotifier() {
					public void listen(Consumer<Status> listener) {}
					public void unlisten(Consumer<Status> listener) {}
				};
				monitor = new JobMonitor() {
					public MessageAccessor getMessageAccessor() {
						return messageAccessor;
					}
					public StatusNotifier getStatusUpdates() {
						return statusNotifier;
					}
				};
			}
		}

		private JobMonitorImpl(String id, MessageAccessor messageAccessor, StatusNotifier statusNotifier) {
			if (liveMonitors.containsKey(id))
				throw new IllegalArgumentException(); // a monitor was already registered for this job
			else {
				liveMonitors.remove(id); // this is needed to trigger removal listener (why?)
				monitor = new JobMonitor() {
					public MessageAccessor getMessageAccessor() {
						return messageAccessor;
					}
					public StatusNotifier getStatusUpdates() {
						return statusNotifier;
					}
				};
				liveMonitors.put(id, monitor);
				logger.trace("Registered JobMonitor for job " + id);
				// Keep the monitor in the cache for the time job is running. The monitor will
				// be evicted 60 seconds after the last message or status update has arrived.
				messageAccessor.listen(i -> liveMonitors.get(id));
				statusNotifier.listen(s -> liveMonitors.get(id));
			}
		}

		@Override
		public MessageAccessor getMessageAccessor() {
			return monitor.getMessageAccessor();
		}

		@Override
		public StatusNotifier getStatusUpdates() {
			return monitor.getStatusUpdates();
		}
	}
}
