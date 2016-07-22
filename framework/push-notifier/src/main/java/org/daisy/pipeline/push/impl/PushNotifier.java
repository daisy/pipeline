package org.daisy.pipeline.push.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.job.JobUUIDGenerator;
import org.daisy.pipeline.job.StatusMessage;
import org.daisy.pipeline.webserviceutils.callback.Callback;
import org.daisy.pipeline.webserviceutils.callback.Callback.CallbackType;
import org.daisy.pipeline.webserviceutils.callback.CallbackRegistry;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

// notify clients whenever there are new messages or a change in status
// this class could evolve into a general notification utility
// e.g. it could also trigger email notifications
// TODO: be sure to only do this N times per second
public class PushNotifier {


        private CallbackRegistry callbackRegistry;
        private EventBusProvider eventBusProvider;
        private JobManagerFactory jobManagerFactory;
        private ClientStorage clientStorage;

        private Supplier<JobManager> jobManager= new Supplier<JobManager>() {

                @Override
                public JobManager get() {
                        return jobManagerFactory.createFor(clientStorage.defaultClient());
                }

        };
        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(PushNotifier.class); 
        // for now: push notifications every second. TODO: support different frequencies.
        final int PUSH_INTERVAL = 1000;

        // track the starting point in the message sequence for every timed push
        private MessageList messages = new MessageList();
        private List<StatusHolder> statusList= Collections.synchronizedList(new LinkedList<StatusHolder>());

        Timer timer = null;

        public PushNotifier() {
        }

        public void init(BundleContext context) {
                logger = LoggerFactory.getLogger(Poster.class.getName());
                this.startTimer();

        }

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

        public void setEventBusProvider(EventBusProvider eventBusProvider) {
                this.eventBusProvider = eventBusProvider;
                this.eventBusProvider.get().register(this);
        }

        /**
         * @param clientStorage the clientStorage to set
         */
        public void setWebserviceStorage(WebserviceStorage storage) {
                this.clientStorage = storage.getClientStorage();
                
        }

        public void setCallbackRegistry(CallbackRegistry callbackRegistry) {
                this.callbackRegistry = callbackRegistry;
        }

        public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
                this.jobManagerFactory = jobManagerFactory;
        }

        @Subscribe
        public synchronized void handleMessage(Message msg) {
                JobUUIDGenerator gen = new JobUUIDGenerator();
                JobId jobId = gen.generateIdFromString(msg.getJobId());
                synchronized(this.messages){
                        messages.addMessage(jobId, msg);
                }
        }

        @Subscribe
        public void handleStatus(StatusMessage message) {
                logger.debug(String.format("Status changed %s->%s",message.getJobId(),message.getStatus()));
                StatusHolder holder= new StatusHolder();
                holder.status=message.getStatus();
                Optional<Job> job=jobManager.get().getJob(message.getJobId());
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
                                logger.debug("Posting status for "+holder.job.getId());
                                Job job = holder.job;

                                for (Callback callback :callbackRegistry.getCallbacks(job.getContext().getId())) {
                                        if (callback.getType() == CallbackType.STATUS) {
                                                Poster.postStatusUpdate(job, holder.status, callback);
                                        }
                                }
                        }

                }
                private synchronized void postMessages() {
                        synchronized(PushNotifier.this.messages){
                                for (JobId jobId : messages.getJobs()) {
                                        Optional<Job> job = jobManager.get().getJob(jobId);
                                        if(!job.isPresent()){
                                                break;
                                        }
                                        for (Callback callback : callbackRegistry.getCallbacks(jobId)) {
                                                if (callback.getType() == CallbackType.MESSAGES) {
                                                        Poster.postMessage(job.get(), new LinkedList<Message>(messages.getMessages(jobId)), callback);
                                                }
                                        }
                                        //I don't mind noone listening for the messages they will be discarded anyway...
                                        messages.removeJob(jobId);
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

        private class MessageList {
                HashMap<JobId, List<Message>> messages;

                public MessageList() {
                        messages = new HashMap<JobId, List<Message>>();
                }
                public synchronized List<Message> getMessages(JobId jobId) {
                        return messages.get(jobId);
                }
                public synchronized MessageList copy(){
                        MessageList copy=new MessageList();     
                        for (Map.Entry<JobId,List<Message>> entry:this.messages.entrySet()){
                                copy.messages.put(entry.getKey(),new LinkedList<Message>(entry.getValue()));    
                        }
                        return copy;
                }

                public synchronized void addMessage(JobId jobId, Message msg) {
                        List<Message> list;
                        if (containsJob(jobId)) {
                                list = messages.get(jobId);
                        }
                        else {
                                list = new ArrayList<Message>();
                                messages.put(jobId, list);
                        }
                        list.add(msg);
                }
                public synchronized Set<JobId> getJobs() {
                        return Sets.newHashSet(messages.keySet());
                }

                public synchronized void removeJob(JobId jobId) {
                        messages.remove(jobId);
                }

                public synchronized boolean containsJob(JobId jobId) {
                        return messages.containsKey(jobId);
                }

                public synchronized boolean isEmpty() {
                        return messages.isEmpty();
                }

                // for debugging
                public synchronized void printList(JobId jobId) {
                        for (Message msg : messages.get(jobId)) {
                                System.out.println("#" + msg.getSequence() + ", job #" + msg.getJobId());
                        }
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
