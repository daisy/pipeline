package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.persistence.*;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.Message;
import org.daisy.pipeline.client.models.Result;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.Cancellable;

import com.avaje.ebean.Model;

import controllers.Application;

@Entity
public class Job extends Model implements Comparable<Job> {
	/** Key is the job ID; value is the sequence number of the last message read from the Pipeline 2 Web API. */ 
	public static Map<Long,Integer> lastMessageSequence = Collections.synchronizedMap(new HashMap<Long,Integer>());
	public static Map<Long,String> lastStatus = Collections.synchronizedMap(new HashMap<Long,String>());
	public static Map<Long,Date> lastAccessed = Collections.synchronizedMap(new HashMap<Long,Date>());
	public static Map<Long, org.daisy.pipeline.client.models.Job> runningJobs = Collections.synchronizedMap(new HashMap<Long,org.daisy.pipeline.client.models.Job>());
	
	@Id
	private Long id;

	// General information
	private String engineId;
	private String nicename;
	private Date created;
	private Date started;
	private Date finished;
	@Column(name="user_id") private Long user;
	private String guestEmail; // Guest users may enter an e-mail address to receive notifications
	private String scriptId;
	private String scriptName;
	private String status;
	
	// Notification flags
	public boolean notifiedCreated;
	public boolean notifiedComplete;

	// Not stored in the job table; retrieved dynamically
	@Transient
	public String href;
	@Transient
	private String userNicename;
	@Transient
	org.daisy.pipeline.client.models.Job clientlibJob;

	@Transient
	private Cancellable pushNotifier;
	
	/** Make job belonging to user */
	public Job(User user) {
		super();
		this.user = user.getId();
		this.status = "NEW";
		this.created = new Date();
		this.notifiedCreated = false;
		this.notifiedComplete = false;
		if (user.getId() < 0)
			this.userNicename = Setting.get("users.guest.name");
		else
			this.userNicename = User.findById(user.getId()).getName();
	}

	public int compareTo(Job other) {
		if (this.started != null) {
			return this.started.compareTo(other.getStarted() == null ? other.getCreated() : other.getStarted());
		} else {
			return this.created.compareTo(other.getStarted() == null ? other.getCreated() : other.getStarted());
		}
	}

	// -- Queries

    @Transient
	public static Model.Finder<String,Job> _find = null;
    
    public static Model.Finder<String,Job> find() {
        if (Job._find == null) {
            Job._find = new Model.Finder<String, Job>(Setting.ebeanServer(), Job.class);
        }
        return Job._find;
    }
	
	/** Retrieve a Job by its id. */
	public static Job findById(Long id) {
		Job job = find().where().eq("id", id).ne("status", "TEMPLATE").findUnique();
		if (job != null) {
			lastAccessed.put(id, new Date());
			User user = User.findById(job.getUser());
			if (user != null)
				job.userNicename = user.getName();
			else if (job.user < 0)
				job.userNicename = Setting.get("users.guest.name");
			else
				job.userNicename = "User";
		}
		return job;
	}

	/** Retrieve a Job by its engine id. */
	public static Job findByEngineId(String id) {
		Job job = find().where().eq("engine_id", id).ne("status", "TEMPLATE").findUnique();
		if (job != null) {
			User user = User.findById(job.getUser());
			if (user != null)
				job.userNicename = user.getName();
			else if (job.user < 0)
				job.userNicename = Setting.get("users.guest.name");
			else
				job.userNicename = "User";
		}
		return job;
	}
	
	/** The nice name of the user that owns this job */
	public String getUserNicename() {
		if (userNicename == null) {
			if (user == null || user < 0)
				userNicename = Setting.get("users.guest.name");
			else
				userNicename = User.findById(user).getName();
		}
		return userNicename;
	}
	
	public void cancelPushNotifications() {
		Logger.debug("Cancelling push notifications for job #"+id+" with engineId="+engineId);
		if (pushNotifier != null) {
			pushNotifier.cancel();
			pushNotifier = null;
		}
		if (runningJobs.containsKey(id)) {
			runningJobs.remove(id);
		}
	}
	
	public void pushNotifications() {
		if (pushNotifier != null) {
			return;
		}
		
		Logger.debug("Starting new push notifications for job #"+id+" with engineId="+engineId);
		pushNotifier = Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.SECONDS),
				Duration.create(500, TimeUnit.MILLISECONDS),
				new Runnable() {
					public void run() {
						refresh();
						try {
							int fromSequence = Job.lastMessageSequence.containsKey(id) ? Job.lastMessageSequence.get(id) : 0;
//							Logger.debug("checking job #"+id+" for updates from message #"+fromSequence);
							
							org.daisy.pipeline.client.models.Job job = getJobFromEngine(fromSequence);
							
							if (job == null) {
								Logger.debug("Could not find job in engine ("+engineId+")");
								return;
							}
							
							Job webUiJob = Job.findByEngineId(job.getId());
							
							if (webUiJob == null) {
								Logger.debug("Job has been deleted; stop updates (engine id: "+job.getId()+")");
								pushNotifier.cancel();
								return;
							}
							
							if (job.getStatus() != Status.RUNNING && job.getStatus() != Status.IDLE) {
								pushNotifier.cancel();
								if (webUiJob.getFinished() == null) {
									// pushNotifier tends to fire multiple times after canceling it, so this if{} is just to fire the "finished" event exactly once
									webUiJob.setFinished(new Date());
									Map<String,String> finishedMap = new HashMap<String,String>();
									finishedMap.put("text", webUiJob.getFinished().toString());
									finishedMap.put("number", webUiJob.getFinished().getTime()+"");
									NotificationConnection.pushJobNotification(webUiJob.getUser(), new Notification("job-finished-"+webUiJob.getId(), finishedMap));
									NotificationConnection.pushJobNotification(webUiJob.getUser(), new Notification("job-results-"+webUiJob.getId(), jsonifiableResults(job)));
								}
							}
							
							if (job.getStatus() != null && !job.getStatus().toString().equals(lastStatus.get(id))) {
								Logger.debug("    status has changed to "+job.getStatus());
								lastStatus.put(id, job.getStatus().toString());
								Logger.debug("    notifying job-status-"+webUiJob.id);
								NotificationConnection.pushJobNotification(webUiJob.getUser(), new Notification("job-status-"+webUiJob.getId(), job.getStatus()));
								
								webUiJob.setStatus(job.getStatus().toString());
								
								if (job.getStatus() == Status.RUNNING) {
									// job status changed from IDLE to RUNNING
									Logger.debug("    job status changed from IDLE to RUNNING");
									webUiJob.setStarted(new Date());
									Map<String,String> startedMap = new HashMap<String,String>();
									startedMap.put("text", webUiJob.getStarted().toString());
									startedMap.put("number", webUiJob.getStarted().getTime()+"");
									Logger.debug("    notifying job-started-"+webUiJob.id);
									NotificationConnection.pushJobNotification(webUiJob.getUser(), new Notification("job-started-"+webUiJob.getId(), startedMap));
								}
								
								Logger.debug("    saving");
								webUiJob.setJob(job);
								webUiJob.save();
							}
							
							List<org.daisy.pipeline.client.models.Message> messages = job.getMessages();
							if (messages != null) {
								for (org.daisy.pipeline.client.models.Message message : messages) {
									Notification notification = new Notification("job-message-"+webUiJob.getId(), message);
									NotificationConnection.pushJobNotification(webUiJob.getUser(), notification);
								}
								
								if (messages.size() > 0) {
									Job.lastMessageSequence.put(id, messages.get(messages.size()-1).sequence);
								}
							}
							
							if (!runningJobs.containsKey(id)) {
								runningJobs.put(id, job);
							}
							runningJobs.get(id).joinMessages(job);
							job = runningJobs.get(id);
							
							Map<String,String> progressMap = new HashMap<String,String>();
							progressMap.put("from", job.getProgressFrom()+"");
							progressMap.put("to", job.getProgressTo()+"");
							double estimate = job.getProgressEstimate();
							if (estimate < job.getProgressFrom() || estimate >= job.getProgressTo()) {
								estimate = job.getProgressFrom(); // for some reason there can be an error in the calculation (probably due to timestamps or similar); use "from" as estimate instead
							}
							progressMap.put("estimate", estimate+"");
							Notification notification = new Notification("job-progress-"+webUiJob.getId(), progressMap);
							NotificationConnection.pushJobNotification(webUiJob.getUser(), notification);
							
						} catch (javax.persistence.PersistenceException e) {
							// Ignores this exception that happens on shutdown:
							// javax.persistence.PersistenceException: java.sql.SQLException: Attempting to obtain a connection from a pool that has already been shutdown.
							// Should be safe to ignore I think...
						}
					}
				},
				Akka.system().dispatcher()
				);
	}
	
	public static Object jsonifiableResults(org.daisy.pipeline.client.models.Job job) {
		Result allResults = job.getResult();
		SortedMap<Result, List<Result>> individualResults = job.getResults();
		
		SortedMap<String,Object> jsonResults = new TreeMap<String,Object>();
		if (allResults != null) {
			jsonResults.put("filename", allResults.filename);
			jsonResults.put("from", allResults.from);
			jsonResults.put("mimeType", allResults.mimeType);
			jsonResults.put("name", allResults.name);
			jsonResults.put("nicename", allResults.nicename);
			jsonResults.put("relativeHref", allResults.relativeHref);
			jsonResults.put("prettyRelativeHref", allResults.prettyRelativeHref);
			jsonResults.put("size", allResults.size);
		}
		List<Object> jsonResultResults = new ArrayList<Object>();
		if (individualResults != null) {
			for (Result individualResult : individualResults.keySet()) {
				SortedMap<String,Object> jsonSubResults = new TreeMap<String,Object>();
				jsonSubResults.put("filename", individualResult.filename);
				jsonSubResults.put("from", individualResult.from);
				jsonSubResults.put("mimeType", individualResult.mimeType);
				jsonSubResults.put("name", individualResult.name);
				jsonSubResults.put("nicename", individualResult.nicename);
				jsonSubResults.put("relativeHref", individualResult.relativeHref);
				jsonSubResults.put("prettyRelativeHref", individualResult.prettyRelativeHref);
				jsonSubResults.put("size", individualResult.size);
				List<Object> jsonSubResultResults = new ArrayList<Object>();
				for (Result fileResult : individualResults.get(individualResult)) {
					SortedMap<String,Object> jsonSubSubResults = new TreeMap<String,Object>();
					jsonSubSubResults.put("filename", fileResult.filename);
					jsonSubSubResults.put("from", fileResult.from);
					jsonSubSubResults.put("mimeType", fileResult.mimeType);
					jsonSubSubResults.put("name", fileResult.name);
					jsonSubSubResults.put("nicename", fileResult.nicename);
					jsonSubSubResults.put("relativeHref", fileResult.relativeHref);
					jsonSubSubResults.put("prettyRelativeHref", fileResult.prettyRelativeHref);
					jsonSubSubResults.put("size", fileResult.size);
					jsonSubResultResults.add(jsonSubSubResults);
				}
				jsonSubResults.put("results", jsonSubResultResults);
				jsonResultResults.add(jsonSubResults);
			}
		}
		jsonResults.put("results", jsonResultResults);
		
		return jsonResults;
	}

	/** Same as `delete` but with boolean return value. Returns false if unable to delete the job in the engine. */
	public boolean deleteFromEngineAndWebUi() {
		try {
			if (Status.valueOf(status) != null && Application.ws.getJob(this.engineId, 0) != null) {
				Logger.debug("deleting "+this.id+" (sending DELETE request)");
				boolean success = Application.ws.deleteJob(this.engineId);
				if (!success) {
					Pipeline2Logger.logger().error("An error occured when trying to delete job "+this.id+" ("+this.engineId+") from the Pipeline 2 Engine");
					return false; // don't delete Web UI job when an error occured attempting to delete the engine job
				}
			}
			
		} catch (IllegalArgumentException e) {
			/* job status does not correspond to a engine job status; probably a new job; don't send DELETE request */
		}
		asJob().getJobStorage().delete();
		lastAccessed.remove(id);
		db(Setting.ebeanServer()).delete(this);
		return true;
	}
	
	@Override
	public void delete() {
		deleteFromEngineAndWebUi();
	}
	
	/*
	 * NOTE: this class uses getters and setters for its fields. It seems that Ebean does not
	 * generate these getters and setters automatically in the bytecode, probably because
	 * `save()` has been overridden. That is the reason for the getters and setters.
	 */
	@Override
	public void save() {
		save(true);
	}
	public void save(boolean saveToJobStorage) {
		synchronized (this) {
			db(Setting.ebeanServer()).save(this);
			
			if (id == null) {
				id = (Long) Job.find().orderBy("id desc").findIds().get(0);
			}
			if (nicename == null) {
				nicename = "Job #"+id;
			}
			db(Setting.ebeanServer()).save(this);
			refresh();
			
			// save to job storage as well
			if (saveToJobStorage && asJob() != null) {
				Logger.debug("save to job storage");
				asJob().getJobStorage().save();
				jobUpdateHelper();
			}
		}
	}
	
	@Override
    public void update() {
    	db(Setting.ebeanServer()).update(this);
    }
    
	@Override
    public void insert() {
    	db(Setting.ebeanServer()).insert(this);
    }
    
	@Override
    public void refresh() {
    	db(Setting.ebeanServer()).refresh(this);
    }

	private void jobUpdateHelper() {
		engineId = clientlibJob.getId();
		if (nicename == null) {
			nicename = clientlibJob.getNicename();
		}
		if (scriptId == null && clientlibJob.getScript() != null) {
			scriptId = clientlibJob.getScript().getId();
		}
		if (scriptName == null && clientlibJob.getScript() != null) {
			scriptName = clientlibJob.getScript().getNicename();
		}
		try {
			// if current status is one of the engines built-in types
			// (i.e. not "NEW", "UNAVAILABLE", "TEMPLATE" or anything else that might used only by the Web UI)
			// then get the status from the engine.
			if (status == null || "null".equals(status) || Status.valueOf(status) != null) {
				status = clientlibJob.getStatus()+"";
			}
		}
		catch (IllegalArgumentException e) { Logger.debug("jobUpdateHelper: IllegalArgumentException: "+status); }
		catch (NullPointerException e) { Logger.debug("jobUpdateHelper: NullPointerException: "+status); }
	}
	
	public org.daisy.pipeline.client.models.Job asJob() {
		return asJob(true);
	}
	
	public org.daisy.pipeline.client.models.Job asJob(boolean parseMessages) {
		if (clientlibJob == null || clientlibJob.getId() == null) {
			Logger.debug("getting client job (" + (clientlibJob == null ? "not cached from earlier" : "no id") + ", instance:"+this+")");
			File jobStorageDir = new File(Setting.get("jobs"));
			clientlibJob = JobStorage.loadJob(""+id, jobStorageDir);
			if (clientlibJob == null) {
				Logger.debug("not found in job storage");
				getJobFromEngine(parseMessages ? 0 : -1);
				if (clientlibJob != null) {
					Logger.debug("got job from engine; setting job storage based on id");
					new JobStorage(clientlibJob, jobStorageDir, ""+id);
					if (clientlibJob.getNicename() == null) {
						Logger.debug("setting client nicename to "+nicename);
						clientlibJob.setNicename(nicename);
					}
				}
			} else Logger.debug("found in job storage");
			if (clientlibJob == null) {
				Logger.debug("not found in engine or storage; job is a new job");
				clientlibJob = new org.daisy.pipeline.client.models.Job();
				Logger.debug("setting job storage based on id");
				clientlibJob.setNicename(nicename);
				new JobStorage(clientlibJob, jobStorageDir, ""+id);
			}
			if (clientlibJob.getId() != null) {
				setJob(clientlibJob, parseMessages);
			}
		} else Logger.debug("clientlibJob != null");
		return clientlibJob;
	}
	
	public void setJob(org.daisy.pipeline.client.models.Job job) {
		setJob(job, true);
	}
	
	public void setJob(org.daisy.pipeline.client.models.Job job, boolean parseMessages) {
		if (clientlibJob == null || clientlibJob.getId() == null) {
			clientlibJob = asJob(parseMessages);
		}
		
		if (job.getId() != null) {
			clientlibJob.setId(job.getId());
		}
		if (job.getHref() != null) {
			clientlibJob.setHref(job.getHref());
		}
		if (job.getStatus() != null) {
			clientlibJob.setStatus(job.getStatus());
		}
		if (job.getPriority() != null) {
			clientlibJob.setPriority(job.getPriority());
		}
		if (job.getLogHref() != null) {
			clientlibJob.setLogHref(job.getLogHref());
		}
		if (parseMessages && job.getMessages() != null) {
			clientlibJob.joinMessages(job);
		}
		if (job.getResult() != null && job.getResults() != null) {
			clientlibJob.setResults(job.getResult(), job.getResults());
		}

		if (clientlibJob.getBatchId() == null && job.getBatchId() != null) {
			clientlibJob.setBatchId(job.getBatchId());
		}
		if (clientlibJob.getCallback() == null && job.getCallback() != null) {
			clientlibJob.setCallback(job.getCallback());
		}
		if (clientlibJob.getNicename() == null && job.getNicename() != null) {
			clientlibJob.setNicename(job.getNicename());
		}
		if (clientlibJob.getScript() == null && job.getScript() != null) {
			clientlibJob.setScript(job.getScript());
		}
		if (clientlibJob.getScript() == null || job.getScript() != null && job.getScript().getId() != null && !clientlibJob.getScript().getId().equals(job.getScript().getId())) {
			if (job.getScript() != null) {
				clientlibJob.setScript(Application.ws.getScript(job.getScript().getId()));
				clientlibJob.setInputs(clientlibJob.getInputs());
				clientlibJob.setOutputs(clientlibJob.getOutputs());
			}
		}

		jobUpdateHelper();

	}

	/**
	 * Use this method to get the job from the engine to ensure that the XML in the webuis job storage is always up to date
	 * 
	 * @param fromSequence request messages from this message number; if negative will ignore messages (more efficient)
	 */
	public org.daisy.pipeline.client.models.Job getJobFromEngine(int fromSequence) {
		Logger.debug("Getting job from engine: "+engineId);
		if (engineId == null) {
			return null;
		}
		org.daisy.pipeline.client.models.Job clientlibJob = Application.ws.getJob(engineId, fromSequence);
		if (clientlibJob == null) {
			return null;
		}
		setJob(clientlibJob, fromSequence >= 0);
		save();
		return this.clientlibJob;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public String getNicename() {
		return nicename;
	}

	public void setNicename(String nicename) {
		this.nicename = nicename;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getStarted() {
		return started;
	}

	public void setStarted(Date started) {
		this.started = started;
	}

	public Date getFinished() {
		return finished;
	}

	public void setFinished(Date finished) {
		this.finished = finished;
	}

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	public String getGuestEmail() {
		return guestEmail;
	}

	public void setGuestEmail(String guestEmail) {
		this.guestEmail = guestEmail == null ? null : guestEmail.toLowerCase();
	}

	public String getScriptId() {
		return scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isNotifiedCreated() {
		return notifiedCreated;
	}

	public void setNotifiedCreated(boolean notifiedCreated) {
		this.notifiedCreated = notifiedCreated;
	}

	public boolean isNotifiedComplete() {
		return notifiedComplete;
	}

	public void setNotifiedComplete(boolean notifiedComplete) {
		this.notifiedComplete = notifiedComplete;
	}

	public void reset() {
		setStatus("NEW");
		setNotifiedComplete(false);
		asJob();
		JobStorage jobStorage = clientlibJob.getJobStorage();
		
		try {
			clientlibJob = new org.daisy.pipeline.client.models.Job(clientlibJob, jobStorage);
		} catch (Pipeline2Exception e) {
			Logger.error("An error occured when trying to reset the job", e);
		}
		
		setStatus("IDLE");
		setStarted(null);
		setFinished(null);
		save();
	}
	
	/** Until the fix for https://github.com/daisy/pipeline-framework/issues/109 is released */
	public static void estimateMissingTimestamps(List<Message> messages, Job job) {
		if (messages == null || messages.size() == 0 || job == null || job.started == null) {
			return;
		}
		
		// we have no idea when the messages arrived so let's just spread them out
		long timeStamp = job.started.getTime();
		long now = new Date().getTime();
		long remaining = messages.size();
		long step = (now - timeStamp) / remaining;
		for (Message m : messages) {
			remaining--;
			if (m.timeStamp != null) {
				timeStamp = m.timeStamp;
				if (now > timeStamp && remaining > 0) {
					step = (now - timeStamp) / remaining;
				}
				
			} else {
				m.timeStamp = timeStamp;
			}
			timeStamp += step;
		}
	}
	
}
