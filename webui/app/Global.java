import play.*;
import models.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.Job.Status;

import controllers.Application;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import utils.Pipeline2PlayLogger;

public class Global extends GlobalSettings {
	
	@Override
	public synchronized void beforeStart(play.Application app) {
		
	}
	
	@Override
	public synchronized void onStart(play.Application app) {
		// Application has started...
		
		Pipeline2Logger.setLogger(new Pipeline2PlayLogger());
		if ("DEBUG".equals(Configuration.root().getString("logger.application"))) {
			Logger.debug("Enabling clientlib debug mode");
			Pipeline2Logger.logger().setLevel(Pipeline2Logger.LEVEL.DEBUG);
		}
		
		NotificationConnection.notificationConnections = new ConcurrentHashMap<Long,List<NotificationConnection>>();
		
		Akka.system().scheduler().scheduleOnce(
				Duration.create(0, TimeUnit.SECONDS),
				new Runnable() {
					public void run() {
						
						if (Setting.get("appearance.title") == null)
							Setting.set("appearance.title", "DAISY Pipeline 2");
						
						if (Setting.get("appearance.titleLink") == null)
							Setting.set("appearance.titleLink", "scripts");
						
						if (Setting.get("appearance.titleLink.newWindow") == null)
							Setting.set("appearance.titleLink.newWindow", "false");
						
						if (Setting.get("appearance.landingPage") == null)
							Setting.set("appearance.landingPage", "welcome");
						
						if (Setting.get("appearance.theme") == null)
							Setting.set("appearance.theme", "");
						
						if (Setting.get("jobs.hideAdvancedOptions") == null)
							Setting.set("jobs.hideAdvancedOptions", "true");
						
						if (Setting.get("jobs.deleteAfterDuration") == null)
							Setting.set("jobs.deleteAfterDuration", "0");
						
						String userTracking = Play.application().configuration().getString("userTracking");
						if ("true".equals(userTracking) || Play.application().isDev()) {
							Setting.set("userTracking", "true");
						} else {
							Setting.set("userTracking", "false");
						}
						
						String endpoint = Setting.get("dp2ws.endpoint");
						if (endpoint == null) {
							controllers.Application.ws.setEndpoint(controllers.Application.DEFAULT_DP2_ENDPOINT);
						} else {
							controllers.Application.ws.setEndpoint(Setting.get("dp2ws.endpoint"));
							controllers.Application.ws.setCredentials(Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"));
						}
						
					}
				},
				Akka.system().dispatcher());
		
		Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.SECONDS),
				Duration.create(10, TimeUnit.SECONDS),
				new Runnable() {
					public void run() {
						try {
							String endpoint = Setting.get("dp2ws.endpoint");
							if (endpoint == null) {
								Application.setAlive(null);
								return;
							}
							if (!endpoint.equals(controllers.Application.ws.getEndpoint())) {
								controllers.Application.ws.setEndpoint(endpoint);
								controllers.Application.ws.setCredentials(Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"));
							}

							Application.setAlive(controllers.Application.ws.alive());
							
						} catch (javax.persistence.PersistenceException e) {
							// Ignores this exception that happens on shutdown:
							// javax.persistence.PersistenceException: java.sql.SQLException: Attempting to obtain a connection from a pool that has already been shutdown.
							// Should be safe to ignore I think...
						}
					}
				},
				Akka.system().dispatcher()
				);
		
		// Push "heartbeat" notifications (keeping the push notification connections alive). Hopefully this scales...
		Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.SECONDS),
				Duration.create(1, TimeUnit.SECONDS),
				new Runnable() {
					public void run() {
						try {
							synchronized (NotificationConnection.notificationConnections) {
								for (Long userId : NotificationConnection.notificationConnections.keySet()) {
									List<NotificationConnection> browsers = NotificationConnection.notificationConnections.get(userId);
									
									for (int c = browsers.size()-1; c >= 0; c--) {
										if (!browsers.get(c).isAlive()) {
	//										Logger.debug("Browser: user #"+userId+" timed out browser window #"+browsers.get(c).browserId+" (last read: "+browsers.get(c).lastRead+")");
											browsers.remove(c);
										}
									}
									
									for (NotificationConnection c : browsers) {
										if (c.getNotifications().size() == 0) {
	//										Logger.debug("*heartbeat* for user #"+userId+" and browser window #"+c.browserId);
											c.push(new Notification("heartbeat", controllers.Application.pipeline2EngineAvailable()));
										}
									}
								}
							}
						} catch (javax.persistence.PersistenceException e) {
							// Ignores this exception that happens on shutdown:
							// javax.persistence.PersistenceException: java.sql.SQLException: Attempting to obtain a connection from a pool that has already been shutdown.
							// Should be safe to ignore I think...
						}
					}
				},
				Akka.system().dispatcher()
			);
		
		// Delete jobs and uploads after a certain time. Configurable by administrators.
		Akka.system().scheduler().schedule(
				Duration.create(1, TimeUnit.MINUTES),
				Duration.create(1, TimeUnit.MINUTES),
				new Runnable() {
					public void run() {
						try {
							// jobs are only deleted if that option is set in admin settings
							if ("0".equals(Setting.get("jobs.deleteAfterDuration")))
								return;
							
							Date timeoutDate = new Date(new Date().getTime() - Long.parseLong(Setting.get("jobs.deleteAfterDuration")));
							
							List<Job> jobs = Job.find().all();
							for (Job job : jobs) {
								if (job.getCreated() == null) {
									// if for some reason 'created' is null, set it to the current time
									job.setCreated(new Date());
									job.save();
								}
								if (job.getCreated().before(timeoutDate)) {
									Logger.info("Deleting old job: "+job.getId()+" ("+job.getNicename()+")");
									job.deleteFromEngineAndWebUi();
								}
							}
						} catch (javax.persistence.PersistenceException e) {
							// Ignores this exception that happens on shutdown:
							// javax.persistence.PersistenceException: java.sql.SQLException: Attempting to obtain a connection from a pool that has already been shutdown.
							// Should be safe to ignore I think...
						}
					}
				},
				Akka.system().dispatcher()
				);
		
		// If jobs.deleteAfterDuration is not set; clean up jobs that no longer exists in the Pipeline engine. This typically happens if the Pipeline engine is restarted.
		Akka.system().scheduler().schedule(
				Duration.create(1, TimeUnit.MINUTES),
				Duration.create(10, TimeUnit.MINUTES),
				new Runnable() {
					public void run() {
						try {
							String endpoint = Setting.get("dp2ws.endpoint");
							if (endpoint == null)
								return;
							
							List<org.daisy.pipeline.client.models.Job> engineJobs = controllers.Application.ws.getJobs();
							if (engineJobs == null) {
								return;
							}
							
							Logger.debug("checking for jobs in webui that is not in engine...");
							List<Job> webUiJobs = Job.find().all();
							for (Job webUiJob : webUiJobs) {
								// new jobs should be deleted after a while if they're not run
								if ("NEW".equals(webUiJob.getStatus())) {
									Date lastAccessed = Job.lastAccessed.get(webUiJob.getId());
									final int deleteNewJobsAfterSeconds = 600;
									if (lastAccessed == null || (new Date().getTime() - lastAccessed.getTime())/1000 > deleteNewJobsAfterSeconds) {
										webUiJob.deleteFromEngineAndWebUi();
									}
									
									continue;
								}
								
								/*
								if (webUiJob.getEngineId() != null) {
									boolean exists = false;
									for (org.daisy.pipeline.client.models.Job engineJob : engineJobs) {
										if (webUiJob.getEngineId().equals(engineJob.getId())) {
											exists = true;
											break;
										}
									}
									if (!exists) {
										// TODO: instead of deleting them:
										// - keep them in the webui (job xml is stored, results are not)
										// - allow for re-running these jobs
										// - have an automatic cleanup setting in the maintenance tab of the admin settings similar to the current job persistence option
										//
										//if ( should delete job ) {
											//Logger.info("Deleting job that no longer exists in the Pipeline engine: "+webUiJob.getId()+" ("+webUiJob.getEngineId()+" - "+webUiJob.getNicename()+")");
											//webUiJob.deleteFromEngineAndWebUi();
										//}
									}
								}
								*/
							}
							
							Logger.debug("checking for jobs in engine that is not in webui...");
							if (controllers.Application.getAlive() != null) {
								for (org.daisy.pipeline.client.models.Job engineJob : engineJobs) {
									if (engineJob.getId() == null) {
										Logger.info("engine job is null: "+engineJob.getId()+". skipping...");
										continue;
									}
									boolean exists = false;
									for (Job webUiJob : webUiJobs) {
										// check if status is a engine status; if not, that meanse this is not a engine job and we should skip it
										if ("NEW".equals(webUiJob.getStatus())) { continue; }
										try { Status.valueOf(webUiJob.getStatus()); }
										catch (IllegalArgumentException e) { Logger.info("not a engine job: #"+webUiJob.getId()+" (status:"+webUiJob.getStatus()+")"); continue; }
										
										if (engineJob.getId().equals(webUiJob.getEngineId())) {
											exists = true;
											break;
										}
									}
									if (!exists) {
										Logger.info("Adding job from the Pipeline engine (with status: "+engineJob.getStatus()+") that does not exist in the Web UI: "+engineJob.getId());
										
										org.daisy.pipeline.client.models.Job job = controllers.Application.ws.getJob(engineJob.getId(), 0);
										
										User notLoggedIn = User.findById(-1L);
										if (notLoggedIn == null) {
											notLoggedIn = new User("not-logged-in@example.net", "Not logged in", "not logged in", false);
											notLoggedIn.setId(-1L);
										}
										Job webUiJob = new Job(notLoggedIn);
										webUiJob.save();
										webUiJob.setStatus(job.getStatus()+"");
										webUiJob.setJob(job);
										webUiJob.save();
										Logger.info("saved job with engine id "+webUiJob.getEngineId()+" as #"+webUiJob.getId());
									}
								}
							}
							
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

}
