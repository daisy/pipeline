package org.daisy.pipeline.webservice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.clients.WebserviceStorage;
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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

// notify clients whenever there are new messages or a change in status
// this class could evolve into a general notification utility
// e.g. it could also trigger email notifications
// TODO: be sure to only do this N times per second
@Component(
    name = "push-notifier",
    service = { CallbackHandler.class }
)
public class PushNotifier implements CallbackHandler {

        private JobManagerFactory jobManagerFactory;
        private ClientStorage clientStorage;
        private JobManager jobManager;
        private Map<Job,List<Callback>> callbacks;
        private Map<MessageAccessor,Job> jobForAccessor;
        private Map<MessageAccessor,Runnable> unlistenMessagesFunctions;
        private Map<StatusNotifier,Runnable> unlistenStatusFunctions;

        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(PushNotifier.class); 
        // for now: push notifications every second. TODO: support different frequencies.
        final int PUSH_INTERVAL = 1000;

        // track the starting point in the message sequence for every timed push
        private Map<MessageAccessor,Integer> newMessages = Collections.synchronizedMap(new HashMap<MessageAccessor,Integer>());
        private List<StatusHolder> statusList= Collections.synchronizedList(new LinkedList<StatusHolder>());

        Timer timer = null;

        public PushNotifier() {
        }

        @Activate
        public void init() {
                logger.debug("Activating push notifier");
                jobManager = jobManagerFactory.createFor(clientStorage.defaultClient());
                callbacks = new HashMap<>();
                jobForAccessor = new HashMap<>();
                unlistenMessagesFunctions = new HashMap<>();
                unlistenStatusFunctions = new HashMap<>();
                this.startTimer();
        }

        @Deactivate
        public void close() {
                cancelTimer();
        }

        public synchronized void startTimer() {
                timer = new Timer();
                timer.schedule(new NotifyTask(), 0, PUSH_INTERVAL);
        }
        public synchronized void cancelTimer() {
                if (timer != null) {
                        timer.cancel();
                        timer = null;
                }
        }

        /**
         * @param clientStorage the clientStorage to set
         */
        @Reference(
           name = "webservice-storage",
           unbind = "-",
           service = WebserviceStorage.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setWebserviceStorage(WebserviceStorage storage) {
                this.clientStorage = storage.getClientStorage();
                
        }

        @Reference(
           name = "job-manager-factory",
           unbind = "-",
           service = JobManagerFactory.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
                this.jobManagerFactory = jobManagerFactory;
        }

        @Override
        public void addCallback(Callback callback) {
                logger.debug("Adding callback: " + callback);
                Job job = callback.getJob();
                List<Callback> list = callbacks.get(job);
                if (list == null) {
                        list = new ArrayList<Callback>();
                        callbacks.put(job, list);
                }
                switch (callback.getType()) {
                case STATUS: {
                        boolean alreadyListening = false;
                        for (Callback c : list)
                                if (c.getType() == CallbackType.STATUS) {
                                        alreadyListening = true;
                                        break; }
                        if (!alreadyListening) {
                                JobMonitor monitor = job.getContext().getMonitor();
                                StatusNotifier statusNotifier = monitor.getStatusUpdates();
                                Consumer<Status> statusListener = s -> update(job.getId(), s);
                                statusNotifier.listen(statusListener);
                                unlistenStatusFunctions.put(statusNotifier, () -> statusNotifier.unlisten(statusListener));
                        }
                        break;
                }
                case MESSAGES: {
                        boolean alreadyListening = false;
                        for (Callback c : list)
                                if (c.getType() == CallbackType.MESSAGES) {
                                        alreadyListening = true;
                                        break; }
                        if (!alreadyListening) {
                                JobMonitor monitor = job.getContext().getMonitor();
                                MessageAccessor accessor = monitor.getMessageAccessor();
                                jobForAccessor.put(accessor, job);
                                Consumer<Integer> messageListener = i -> update(accessor, i);
                                accessor.listen(messageListener);
                                unlistenMessagesFunctions.put(accessor, () -> accessor.unlisten(messageListener));
                        }
                        break;
                }
                default:
                }
                list.add(callback);
        }

        // this method is currently never called
        @Override
        public void removeCallback(Callback callback) {
                Job job = callback.getJob();
                List<Callback> list = callbacks.get(job);
                if (list == null)
                        return;
                list.remove(callback);
                if (callback.getType() == CallbackType.MESSAGES) {
                        boolean keepListening = false;
                        for (Callback c : list)
                                if (c.getType() == CallbackType.MESSAGES) {
                                        keepListening = true;
                                        break; }
                        if (!keepListening) {
                                MessageAccessor accessor = job.getContext().getMonitor().getMessageAccessor();
                                jobForAccessor.remove(accessor);
                                unlistenMessagesFunctions.remove(accessor).run();
                        }
                }
                if (list.isEmpty())
                        callbacks.remove(job);
        }

        // get notified of message updates
        private void update(MessageAccessor accessor, Integer sequence) {
                Job job = jobForAccessor.get(accessor);
                logger.trace("handling message update: [job: " + job.getId() + ", event: " + sequence + "]");
                synchronized (newMessages) {
                        Integer seq = newMessages.get(accessor);
                        if (seq == null) {
                                newMessages.put(accessor, sequence);
                        }
                }
        }

        // get notified of status updates
        private void update(JobId jobId, Status status) {
                logger.debug(String.format("Status changed %s->%s", jobId, status));
                StatusHolder holder= new StatusHolder();
                holder.status = status;
                Optional<Job> job = jobManager.getJob(jobId);
                if(job.isPresent()){
                        holder.job=job.get();
                }
                statusList.add(holder);

        }


        private class NotifyTask extends TimerTask {
                public NotifyTask() {
                        super();
                }

                @Override
                public synchronized void run() {
                        postMessages();
                        postStatus();
                }
                private void postStatus() {
                        //logger.debug("Posting messages");
                        List<StatusHolder> toPost=Lists.newLinkedList();
                        synchronized(PushNotifier.this.statusList){
                                toPost.addAll(PushNotifier.this.statusList);    
                                PushNotifier.this.statusList.clear();
                        }
                        for (StatusHolder holder: toPost) {
                                logger.debug("Posting status '" + holder.status + "' for job " + holder.job.getId());
                                Job job = holder.job;
                                Iterable<Callback> callbacks = PushNotifier.this.callbacks.get(job);
                                if (callbacks != null) {
                                        for (Callback callback : callbacks) {
                                                if (callback.getType() == CallbackType.STATUS) {
                                                        callback.postStatusUpdate(holder.status);
                                                }
                                        }
                                }
                        }

                }
                private synchronized void postMessages() {
                        Map<MessageAccessor,Integer> newMessages; {
                                synchronized (PushNotifier.this.newMessages) {
                                        newMessages = new HashMap<MessageAccessor,Integer>(PushNotifier.this.newMessages);
                                        PushNotifier.this.newMessages.clear();
                                }
                        }
                        for (MessageAccessor accessor : newMessages.keySet()) {
                                Job job = jobForAccessor.get(accessor);
                                // check if the job still exists, otherwise stop listening for messages
                                if (!jobManager.getJob(job.getId()).isPresent()) {
                                        unlistenMessagesFunctions.remove(accessor).run();
                                        continue;
                                }
                                Integer seq = newMessages.get(accessor);
                                int newerThan;
                                BigDecimal progress;
                                List<Message> messages;
                                {
                                        progress = accessor.getProgress();
                                        if (seq != null) {
                                                newerThan = seq - 1;
                                                logger.debug("Posting messages starting from " + (newerThan + 1) + " for job " + job.getId());
                                                messages = accessor.createFilter().greaterThan(newerThan).getMessages();
                                        } else {
                                                newerThan = -1;
                                                messages = Collections.<Message>emptyList();
                                        }
                                }
                                Iterable<Callback> callbacks = PushNotifier.this.callbacks.get(job);
                                if (callbacks == null) {
                                        // should not happen
                                        continue;
                                }
                                for (Callback callback : callbacks) {
                                        if (callback.getType() == CallbackType.MESSAGES) {
                                                callback.postMessages(messages, newerThan, progress);
                                        }
                                }
                        }

                        // no need to keep the timer going if there are no more messages
                        // however, this doesn't really work. TODO fix it.
                        /*if (messages.isEmpty()) {
                          System.out.println("Cancelling timer");
                          cancelTimer();
                          }*/
                }
        }
        
        /*
         * In order to not lose the reference 
         * to the job if it's been deleted
         */
        private class StatusHolder{
                Status status;
                Job job;
        }

}
