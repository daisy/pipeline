package org.daisy.pipeline.event;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobMonitorFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "job-monitor",
	service = { JobMonitorFactory.class }
)
public class JobMonitorFactoryImpl implements JobMonitorFactory {

	@Override
	public JobMonitor newJobMonitor(JobId id, boolean live) {
		return new JobMonitorImpl(id.toString(), live);
	}

	private MessageEventListener messageEventListener;

	@Reference(
		name = "event-listener",
		unbind = "-",
		service = MessageEventListener.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setMessageEventListener(MessageEventListener listener) {
		this.messageEventListener = listener;
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

	private final Map<String,MessageAccessor> liveAccessors;

	public JobMonitorFactoryImpl() {
		// use this property to configure how long messages are cached before storing them in a MessageStorage (volatile of persistent)
		// use only for testing!
		// to configure how long messages are cached in the volatile storage, use org.daisy.pipeline.messaging.cache
		int timeout = Integer.valueOf(System.getProperty("org.daisy.pipeline.messaging.cache.buffer", "60"));
		liveAccessors = CacheBuilder.newBuilder()
			.expireAfterAccess(timeout, TimeUnit.SECONDS)
			.removalListener(
				notification -> {
					LiveMessageAccessor a = (LiveMessageAccessor)notification.getValue();
					// store buffered messages to memory or database
					a.store(JobMonitorFactoryImpl.this.messageStorage);
					// free buffered messages and stop listening for new events
					a.close(); })
			.<String,MessageAccessor>build()
			.asMap();
	}

	private final class JobMonitorImpl implements JobMonitor {

		private final MessageAccessor accessor;

		private JobMonitorImpl(String id, boolean live) {
			if (liveAccessors.containsKey(id))
				this.accessor = liveAccessors.get(id);
			else {
				liveAccessors.remove(id); // this is needed to trigger removal listener (why?)
				// If the job needs to be monitorable while it runs, the messages are coming from the
				// event bus. It is assumed that the job has not started yet. The accessor is created
				// immediately so that messages are buffered from the beginning and no data is lost.
				if (live) {
					this.accessor = new LiveMessageAccessor(id, messageEventListener);
					// We need to cache the accessor because we wouldn't otherwise be able to create
					// a new job monitor while the job is already running. Another reason for the
					// caching is that the message storage is not guaranteed to be lossless. Notably
					// the database can currently not store the full message tree, so the messages
					// are flattened first.
					liveAccessors.put(id, this.accessor);
					// Keep the accessor in the cache for the time job is running. The accessor will
					// be evicted 60 seconds after the last message has arrived.
					this.accessor.listen((a,i) -> liveAccessors.get(id));
				}
				// Otherwise the messages are gathered from the message storage. In this case it is
				// assumed that the job has finished and therefore no new messages are produced.
				else
					this.accessor = new MessageAccessorFromStorage(id, messageStorage);
			}
		}

		@Override
		public MessageAccessor getMessageAccessor() {
			return accessor;
		}
	}
}
