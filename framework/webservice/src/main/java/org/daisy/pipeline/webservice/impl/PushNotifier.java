package org.daisy.pipeline.webservice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.base.Optional;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.StatusNotifier;
import org.daisy.pipeline.webservice.Callback;
import org.daisy.pipeline.webservice.Callback.CallbackType;
import org.daisy.pipeline.webservice.CallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// notify clients whenever there are new messages or a change in status
// this class could evolve into a general notification utility
// e.g. it could also trigger email notifications
// TODO: be sure to only do this N times per second
public class PushNotifier implements CallbackHandler {

        private JobManager jobManager;
        private Map<JobId,List<Callback>> callbacks;
        private Map<MessageAccessor,JobId> jobForAccessor;
        private Map<MessageAccessor,Runnable> unlistenMessagesFunctions;
        private Map<StatusNotifier,Runnable> unlistenStatusFunctions;

        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(PushNotifier.class); 
        // for now: push notifications every second. TODO: support different frequencies.
        final int PUSH_INTERVAL = 1000;

        // track the sequence numbers of the first and last message for the next timed push
        private Map<Callback,Integer> lastPushedMessage = new HashMap<Callback,Integer>();
        private Map<MessageAccessor,Integer> lastUnpushedMessage = Collections.synchronizedMap(new HashMap<>());

        // track the last status of each job
        private Map<JobId,Status> lastUnpushedStatus = Collections.synchronizedMap(new HashMap<>());

        private Timer timer = null;

        PushNotifier(JobManagerFactory jobManagerFactory) {
                logger.debug("Activating push notifier");
                jobManager = jobManagerFactory.create();
                callbacks = new HashMap<>();
                jobForAccessor = Collections.synchronizedMap(new HashMap<>());
                unlistenMessagesFunctions = new HashMap<>();
                unlistenStatusFunctions = new HashMap<>();
                startTimer();
        }

        void close() {
                cancelTimer();
        }

        private synchronized void startTimer() {
                timer = new Timer();
                timer.schedule(new NotifyTask(), 0, PUSH_INTERVAL);
        }
        private synchronized void cancelTimer() {
                if (timer != null) {
                        timer.cancel();
                        timer = null;
                }
        }

        @Override
        public void addCallback(Callback callback) {
                logger.debug("Adding callback: " + callback);
                synchronized (callbacks) {
                        Job job = callback.getJob();
                        List<Callback> list = callbacks.get(job.getId());
                        if (list == null) {
                                list = new ArrayList<Callback>();
                                callbacks.put(job.getId(), Collections.synchronizedList(list));
                        }
                        switch (callback.getType()) {
                        case STATUS: {
                                // push current status when callback is registered
                                Status status = job.getStatus();
                                lastUnpushedStatus.put(job.getId(), status);
                                if (status == Status.SUCCESS
                                    || status == Status.ERROR
                                    || status == Status.FAIL)
                                        ; // don't listen for updates if the job has already finished
                                else {
                                        boolean alreadyListening = false;
                                        for (Callback c : list)
                                                if (c.getType() == CallbackType.STATUS) {
                                                        alreadyListening = true;
                                                        break; }
                                        if (!alreadyListening) {
                                                JobMonitor monitor = job.getMonitor();
                                                StatusNotifier statusNotifier = monitor.getStatusUpdates();
                                                Consumer<Status> statusListener = s -> update(job.getId(), s);
                                                statusNotifier.listen(statusListener);
                                                unlistenStatusFunctions.put(statusNotifier, () -> statusNotifier.unlisten(statusListener));
                                        }
                                }
                                break;
                        }
                        case MESSAGES: {
                                Status status = job.getStatus();
                                if (status == Status.SUCCESS
                                    || status == Status.ERROR
                                    || status == Status.FAIL) {
                                        // if the job has already finished when the callback is registered, push all messages
                                        lastUnpushedMessage.put(job.getMonitor().getMessageAccessor(),
                                                                Integer.MAX_VALUE);
                                } else {
                                        // otherwise push the initial messages on the first new message event
                                        boolean alreadyListening = false;
                                        for (Callback c : list)
                                                if (c.getType() == CallbackType.MESSAGES) {
                                                        alreadyListening = true;
                                                        break; }
                                        if (!alreadyListening) {
                                                JobMonitor monitor = job.getMonitor();
                                                MessageAccessor accessor = monitor.getMessageAccessor();
                                                jobForAccessor.put(accessor, job.getId());
                                                Consumer<Integer> messageListener = i -> update(accessor, i);
                                                accessor.listen(messageListener);
                                                unlistenMessagesFunctions.put(accessor, () -> accessor.unlisten(messageListener));
                                        }
                                }
                                break;
                        }
                        default:
                        }
                        list.add(callback);
                }
        }

        @Override
        public void removeCallback(Callback callback) {
                synchronized (callbacks) {
                        Job job = callback.getJob();
                        List<Callback> list = callbacks.get(job.getId());
                        if (list == null || !list.remove(callback))
                                // already removed
                                return;
                        boolean keepListeningForMessages = false;
                        boolean keepListeningForStatusUpdates = false;
                        for (Callback c : list) {
                                if (c.getType() == CallbackType.MESSAGES)
                                        keepListeningForMessages = true;
                                else
                                        keepListeningForStatusUpdates = true;
                                if (keepListeningForMessages && keepListeningForStatusUpdates)
                                        break;
                        }
                        if (!keepListeningForMessages || !keepListeningForStatusUpdates) {
                                JobMonitor monitor = job.getMonitor();
                                if (!keepListeningForMessages) {
                                        MessageAccessor accessor = monitor.getMessageAccessor();
                                        jobForAccessor.remove(accessor);
                                        Runnable unlistenMessages = unlistenMessagesFunctions.remove(accessor);
                                        if (unlistenMessages != null) unlistenMessages.run();
                                }
                                if (!keepListeningForStatusUpdates) {
                                        Runnable unlistenStatus = unlistenStatusFunctions.remove(monitor.getStatusUpdates());
                                        if (unlistenStatus != null) unlistenStatus.run();
                                }
                        }
                        if (list.isEmpty())
                                callbacks.remove(job.getId());
                        synchronized (lastPushedMessage) {
                                lastPushedMessage.remove(callback);
                        }
                }
        }

        /**
         * Stop listening to a job and remove all references to it.
         */
        private void unlistenJob(JobId jobId) {
                synchronized (callbacks) {
                        List<Callback> callbacks = PushNotifier.this.callbacks.remove(jobId);
                        if (callbacks == null || callbacks.isEmpty())
                                // already removed
                                return;
                        Job job = null;
                        for (Callback c : callbacks) {
                                job = c.getJob();
                                break;
                        }
                        JobMonitor monitor = job.getMonitor();
                        MessageAccessor accessor = monitor.getMessageAccessor();
                        jobForAccessor.remove(accessor);
                        Runnable unlistenMessages = unlistenMessagesFunctions.remove(accessor);
                        if (unlistenMessages != null) unlistenMessages.run();
                        Runnable unlistenStatus = unlistenStatusFunctions.remove(monitor.getStatusUpdates());
                        if (unlistenStatus != null) unlistenStatus.run();
                        synchronized (lastPushedMessage) {
                                for (Callback c : callbacks)
                                        lastPushedMessage.remove(c);
                        }
                }
        }

        // get notified of message updates
        private void update(MessageAccessor accessor, Integer sequence) {
                logger.trace("handling message update: [job: " + jobForAccessor.get(accessor) + ", event: " + sequence + "]");
                lastUnpushedMessage.put(accessor, sequence);
        }

        // get notified of status updates
        private void update(JobId job, Status status) {
                logger.debug(String.format("Status changed %s->%s", job, status));
                lastUnpushedStatus.put(job, status);
        }


        private class NotifyTask extends TimerTask {

                private Set<JobId> finishedJobs = new HashSet<>();

                public NotifyTask() {
                        super();
                }

                @Override
                public synchronized void run() {
                        for (JobId j : finishedJobs) unlistenJob(j);
                        finishedJobs.clear();
                        postMessages();
                        postStatus();
                }

                private void postStatus() {
                        Map<JobId,Status> lastUnpushedStatus; {
                                synchronized (PushNotifier.this.lastUnpushedStatus) {
                                        lastUnpushedStatus = new HashMap<JobId,Status>(PushNotifier.this.lastUnpushedStatus);
                                        PushNotifier.this.lastUnpushedStatus.clear();
                                }
                        }
                        for (JobId job : lastUnpushedStatus.keySet()) {
                                Status status = lastUnpushedStatus.get(job);
                                logger.debug("Posting status '" + status + "' for job " + job);
                                List<Callback> callbacks; {
                                        synchronized (PushNotifier.this.callbacks) {
                                                callbacks = PushNotifier.this.callbacks.get(job);
                                                if (callbacks != null)
                                                        callbacks = new ArrayList<>(callbacks);
                                        }
                                }
                                if (callbacks != null) {
                                        for (Callback callback : callbacks) {
                                                if (callback.getType() == CallbackType.STATUS) {
                                                        callback.postStatusUpdate(status);
                                                }
                                        }
                                }
                                // check if the job still exists and has not finished, otherwise stop listening
                                if (status == Status.SUCCESS
                                    || status == Status.ERROR
                                    || status == Status.FAIL
                                    || !jobManager.getJob(job).isPresent())
                                        finishedJobs.add(job);
                        }
                }

                private synchronized void postMessages() {
                        Map<MessageAccessor,Integer> lastUnpushedMessage; {
                                synchronized (PushNotifier.this.lastUnpushedMessage) {
                                        lastUnpushedMessage = new HashMap<MessageAccessor,Integer>(PushNotifier.this.lastUnpushedMessage);
                                        PushNotifier.this.lastUnpushedMessage.clear();
                                }
                        }
                        for (MessageAccessor accessor : lastUnpushedMessage.keySet()) {
                                // Note that a new callback will only receive the initial messages
                                // after the first new message event (first event after the callback has
                                // been registered) has arrived.
                                JobId job = jobForAccessor.get(accessor);
                                List<Callback> callbacks; {
                                        synchronized (PushNotifier.this.callbacks) {
                                                callbacks = PushNotifier.this.callbacks.get(job);
                                                if (callbacks != null)
                                                        callbacks = new ArrayList<>(callbacks);
                                        }
                                }
                                if (callbacks != null) {
                                        BigDecimal progress = accessor.getProgress();
                                        int to = lastUnpushedMessage.get(accessor);
                                        // The same sequence number may appear twice (once to close a message and once to open a message).
                                        // By subtracting 1 from the sequence number we make sure that we never try to access a message
                                        // that does not exist yet when a message is closed.
                                        if (to > 0) to--;
                                        Map<Integer,List<Message>> messagesFrom = new HashMap<>();
                                        for (Callback callback : callbacks) {
                                                if (callback.getType() == CallbackType.MESSAGES) {
                                                        int from = lastPushedMessage.containsKey(callback)
                                                                ? lastPushedMessage.get(callback) + 1
                                                                : callback.getFirstMessage();
                                                        List<Message> messages = messagesFrom.get(from);
                                                        if (messages == null) {
                                                                if (to >= from) {
                                                                        logger.debug("Posting messages starting from " + from + " for job " + job);
                                                                        messages = accessor.createFilter().inRange(from, to).getMessages();
                                                                } else {
                                                                        messages = Collections.<Message>emptyList();
                                                                }
                                                                messagesFrom.put(from, messages);
                                                        }
                                                        callback.postMessages(messages, from - 1, progress);
                                                        lastPushedMessage.put(callback, to);
                                                }
                                        }
                                }
                                // check if the job still exists and has not finished, otherwise stop listening
                                Optional<Job> j = jobManager.getJob(job);
                                if (!j.isPresent())
                                        finishedJobs.add(job);
                                else {
                                        Status status = j.get().getStatus();
                                        if (status == Status.SUCCESS
                                            || status == Status.ERROR
                                            || status == Status.FAIL)
                                                finishedJobs.add(job);
                                }
                        }
                }
        }
}
