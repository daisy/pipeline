package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Job;
import models.Notification;
import models.NotificationConnection;
import models.Setting;
import models.Template;
import models.User;
import models.UserSetting;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Message;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.utils.Files;

import play.Logger;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import utils.ContentType;
import utils.Pair;
import utils.XML;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;

public class Jobs extends Controller {

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static ExpressionList<Job> findWhere() {
		return Job.find.where().ne("status", "TEMPLATE");
	}
	
	public static Result newJob() {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());

		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		Job newJob = new Job(user);
		newJob.save();
		newJob.refresh();
		Logger.debug("created new job: '"+newJob.getId()+"'");
		
		return redirect(routes.Jobs.getJob(newJob.getId()));
	}
	
	public static Result newJobWithTemplate(String ownerIdOrSharedDirName, String templateName) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());

		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		Job newJob = new Job(user);
		newJob.save();
		newJob.refresh();
		
		Template template;
		if (ownerIdOrSharedDirName.matches("^\\d+$")) {
			template = Template.get(user, Long.parseLong(ownerIdOrSharedDirName), templateName);
			if (template == null) {
				Logger.warn("Could not find a template owned by "+Long.parseLong(ownerIdOrSharedDirName)+" and named "+templateName+" available for user "+user.getId());
			}
			
		} else {
			template = Template.get(user, ownerIdOrSharedDirName, templateName);
			if (template == null) {
				Logger.warn("Could not find a shared template in "+ownerIdOrSharedDirName+" and named "+templateName+" available for user "+user.getId());
			}
		}
		
		if (template != null) {
			// use the contents of the template for this job
			newJob.setJob(template.clientlibJob);
			newJob.setNicename(newJob.getNicename()+" - "+template.name);
			
			// get job and job storage
			org.daisy.pipeline.client.models.Job newClientlibJob = newJob.asJob();
			JobStorage newJobStorage = newClientlibJob.getJobStorage();
			
			// copy files from template to this job
			File templateContextDir = template.clientlibJob.getJobStorage().getContextDir();
			Map<String, File> templateFiles = null;
			try {
				templateFiles = Files.listFilesRecursively(templateContextDir, false);
				if (templateFiles.size() > 0) {
					for (String href : templateFiles.keySet()) {
						newJobStorage.addContextFile(templateFiles.get(href), href);
					}
				}
			} catch (IOException e) {
				Logger.error("Could not list files in template context", e);
			}
			
			// create job.xml and copy context files
			newJobStorage.save(false);
			newJob.save();
			
		} else Logger.warn("Could not find template...");
		Logger.debug("created new job: '"+newJob.getId()+"'");
		
		flash("templateJob", "true");
		return redirect(routes.Jobs.getJob(newJob.getId()));
	}
	
	public static Result newJobWithOldJob(Long jobId) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());

		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
				)) {
			return forbidden("You are not allowed to access this job.");
		}
		
		Job newJob = new Job(user);
		newJob.save();
		
		org.daisy.pipeline.client.models.Job oldClientlibJob = webuiJob.asJob();
		
		// use the contents of the template for this job
		newJob.setJob(oldClientlibJob);
		newJob.setNicename(newJob.getNicename());
		
		// get job and job storage
		org.daisy.pipeline.client.models.Job newClientlibJob = newJob.asJob();
		JobStorage newJobStorage = newClientlibJob.getJobStorage();
		
		// copy files from template to this job
		File oldContextDir = oldClientlibJob.getJobStorage().getContextDir();
		Map<String, File> oldFiles = null;
		try {
			oldFiles = Files.listFilesRecursively(oldContextDir, false);
			if (oldFiles.size() > 0) {
				for (String href : oldFiles.keySet()) {
					newJobStorage.addContextFile(oldFiles.get(href), href);
				}
			}
		} catch (IOException e) {
			Logger.error("Could not list files in old job context", e);
		}
		
		// create job.xml and copy context files
		newClientlibJob.setStatus(null);
		newJobStorage.save(false);
		
		// ensure that the web ui job is configured as a new job
		newJob.setStatus("NEW");
		newJob.setEngineId(null);
		newJob.setNotifiedComplete(false);
		newJob.setCreated(new Date());
		newJob.setStarted(null);
		newJob.setFinished(null);
		newJob.save();
		
		Logger.debug("newClientlibJob: "+XML.toString(newClientlibJob.toXml()));
		Logger.info("created new job: '"+newJob.getId()+"'");
		
		flash("forkedJob", "true");
		return redirect(routes.Jobs.getJob(newJob.getId()));
	}
	
	public static Result restart(Long jobId) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		Logger.debug("restart("+jobId+")");
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
				)) {
			return forbidden("You are not allowed to restart this job.");
		}
		
		webuiJob.cancelPushNotifications();
		
		if (webuiJob.getEngineId() != null) {
			Logger.debug("Deleting old job before restarting job: "+webuiJob.getEngineId());
			org.daisy.pipeline.client.models.Job engineJob = Application.ws.getJob(webuiJob.getEngineId(), 0);
			if (engineJob != null) {
				boolean deleted = Application.ws.deleteJob(webuiJob.getEngineId());
				if (!deleted) {
					Logger.info("unable to delete old job: "+webuiJob.getEngineId());
					flash("error", "An error occured while trying to delete the previous job. Please try creating a new job instead.");
					return redirect(routes.Jobs.getJob(jobId));
				}
			}
			webuiJob.setEngineId(null);
		}
		
		webuiJob.reset();
		
		Logger.debug("------------------------------ Posting job... ------------------------------");
		org.daisy.pipeline.client.models.Job clientlibJob = webuiJob.asJob();
		Logger.debug(XML.toString(clientlibJob.toJobRequestXml(true)));
		clientlibJob = Application.ws.postJob(clientlibJob);
		if (clientlibJob == null) {
			Logger.error("An error occured when trying to post job");
			return internalServerError("An error occured when trying to post job");
		}
		webuiJob.setJob(clientlibJob);
		webuiJob.save();
		
		NotificationConnection.pushJobNotification(webuiJob.getUser(), new Notification("job-status-"+webuiJob.getId(), org.daisy.pipeline.client.models.Job.Status.IDLE));
		webuiJob.pushNotifications();
		
		Logger.debug("return redirect(controllers.routes.Jobs.getJob("+webuiJob.getId()+"));");
		return redirect(controllers.routes.Jobs.getJob(webuiJob.getId()));
	}
	
	public static Result getScript(Long jobId, String scriptId) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		if ("false".equals(UserSetting.get(-2L, "scriptEnabled-"+scriptId))) {
			return forbidden();
		}
		
		Script script = Scripts.get(scriptId);
		
		if (script == null) {
			Logger.error("An error occured while trying to read the script with id '"+scriptId+"' from the engine.");
			return internalServerError("An error occured while trying to read the script with id '"+scriptId+"' from the engine.");
		}

		/* List of mime types that are supported by more than one non-optional file argument.
		 * The Web UI cannot automatically assign files of these media types to a
		 * file argument since there are multiple possible file arguments/widgets. */
		List<String> mediaTypeBlacklist = new ArrayList<String>();
		{
			Map<String,Integer> mediaTypeOccurences = new HashMap<String,Integer>();
			for (Argument arg : script.getInputs()) {
				for (String mediaType : arg.getMediaTypes()) {
					if (arg.getRequired()) {
						if (mediaTypeOccurences.containsKey(mediaType)) {
							mediaTypeOccurences.put(mediaType, mediaTypeOccurences.get(mediaType)+1);
						} else {
							mediaTypeOccurences.put(mediaType, 1);
						}
					}
				}
			}
			for (String mediaType : mediaTypeOccurences.keySet()) {
				if (mediaTypeOccurences.get(mediaType) > 1)
					mediaTypeBlacklist.add(mediaType);
			}
		}

		boolean uploadFiles = false;
		boolean hideAdvancedOptions = "true".equals(Setting.get("jobs.hideAdvancedOptions"));
		boolean hasAdvancedOptions = false;
		Map<String,List<Argument>> groupedInputsUnsorted = new HashMap<String,List<Argument>>();
		List<String> groupNames = new ArrayList<String>(); // group names in order of occurence
		List<Pair<String,List<Argument>>> groupedInputs = new ArrayList<Pair<String,List<Argument>>>();
		for (Argument arg : script.getInputs()) {
			if (arg.getRequired() != Boolean.TRUE) {
				hasAdvancedOptions = true;
			}
			if ("anyFileURI".equals(arg.getType()) || "anyURI".equals(arg.getType()) || "anyDirURI".equals(arg.getType())) {
				uploadFiles = true;
			}
			
			String groupName = "Optional parameters";
			if (arg.getRequired() == Boolean.TRUE) {
				groupName = "";
			} else if (arg.getNicename().contains(":")) {
				groupName = arg.getNicename().split(":")[0].replaceAll("\\s+$", "");
			}
			
			if (!groupNames.contains(groupName)) {
				groupNames.add(groupName);
				groupedInputsUnsorted.put(groupName, new ArrayList<Argument>());
			}
			groupedInputsUnsorted.get(groupName).add(arg);
		}
		for (String groupName : groupNames) {
			groupedInputs.add(new Pair<String, List<Argument>>(groupName, groupedInputsUnsorted.get(groupName)));
		}
		
		return ok(views.html.Jobs.getScript.render(script, script.getId().replaceAll(":", "\\x3A"), uploadFiles, hasAdvancedOptions, hideAdvancedOptions, mediaTypeBlacklist, jobId, groupedInputs));
	}
	
	public static Result getJobs() {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null || (user.getId() < 0 && !"true".equals(Setting.get("users.guest.shareJobs"))))
			return redirect(routes.Login.login());
		
		if (user.isAdmin())
			flash("showOwner", "true");
		flash("userid", user.getId()+"");
		
		List<Job> jobList;
		if (user.isAdmin()) {
			jobList = findWhere().findList();
			
		} else if (user.getId() >= 0) {
			jobList = findWhere().eq("user", user.getId()).findList();
			
		} else if (user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))) {
			jobList = findWhere().lt("user", 0).findList();
			
		} else {
			jobList = new ArrayList<Job>();
		}
		
		Collections.sort(jobList);
		Collections.reverse(jobList);
		
		return ok(views.html.Jobs.getJobs.render());
	}
	
	public static Result getJobsJson() {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");
		
		User user = User.authenticate(request(), session());
		if (user == null || (user.getId() < 0 && !"true".equals(Setting.get("users.guest.shareJobs"))))
			return unauthorized("unauthorized");
		
		List<Job> jobList;
		if (user.isAdmin()) {
			jobList = findWhere().findList();
			
		} else if (user.getId() >= 0) {
			jobList = findWhere().eq("user", user.getId()).findList();
			
		} else if (user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))) {
			jobList = findWhere().lt("user", 0).findList();
			
		} else {
			jobList = new ArrayList<Job>();
		}
		
		Collections.sort(jobList);
		Collections.reverse(jobList);
		
		JsonNode jobsJson = play.libs.Json.toJson(jobList);
		return ok(jobsJson);
	}
	
	public static Result getJob(Long id) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		Logger.debug("getJob("+id+")");
		
		Job webuiJob = Job.findById(id);
		if (webuiJob == null) {
			Logger.debug("Job #"+id+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
				)) {
			return forbidden("You are not allowed to view this job.");
		}
		
		if ("NEW".equals(webuiJob.getStatus())) {
			return ok(views.html.Jobs.newJob.render(webuiJob.getId()));
			
		} else {
			return ok(views.html.Jobs.getJob.render(webuiJob.getId()));
		}
	}
	
	public static Result getJobJson(Long id) {
		return getJobJson(id, false);
	}
	
	public static Result getEngineJobJson(Long id) {
		return getJobJson(id, true);
	}
	
	private static Result getJobJson(Long id, boolean includeEngine) {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return unauthorized("unauthorized");
		
		Job webuiJob = Job.findById(id);
		if (webuiJob == null) {
			Logger.debug("Job #"+id+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
				)) {
			return forbidden("You are not allowed to view this job.");
		}
		
		Map<String,Object> output = new HashMap<String,Object>();
		output.put("webuiJob", webuiJob);

		if (includeEngine) {
			org.daisy.pipeline.client.models.Job clientlibJob = null;
			boolean jobAvailableInEngine = false;
			if (webuiJob.getEngineId() != null) {
				clientlibJob = webuiJob.getJobFromEngine(-1);
				jobAvailableInEngine = clientlibJob != null;
			}

			if (clientlibJob == null) {
				clientlibJob = webuiJob.asJob();
			}

			if (!jobAvailableInEngine && ("RUNNING".equals(webuiJob.getStatus()) || "IDLE".equals(webuiJob.getStatus()))) {
				// When jobs are not available in the engine; don't leave them in a running or queued state.
				webuiJob.setStatus("UNAVAILABLE");
				webuiJob.save();
			}
			output.put("jobAvailableInEngine", jobAvailableInEngine);

			if (clientlibJob == null) {
				Logger.error("An error occured while retrieving the job");

			} else {
				Map<String,Object> clientlibJobMap = new HashMap<String,Object>();
				Map<String,Object> clientlibScriptMap = new HashMap<String,Object>();
				clientlibJobMap.put("id", clientlibJob.getId());
				clientlibJobMap.put("href", clientlibJob.getHref());
				clientlibJobMap.put("status", clientlibJob.getStatus());
				clientlibJobMap.put("priority", clientlibJob.getPriority());
				clientlibJobMap.put("scriptHref", clientlibJob.getScriptHref());
				clientlibJobMap.put("nicename", clientlibJob.getNicename());
				clientlibJobMap.put("description", clientlibJob.getDescription());
				clientlibJobMap.put("batchId", clientlibJob.getBatchId());
				clientlibJobMap.put("callback", clientlibJob.getCallback());
				clientlibJobMap.put("logHref", clientlibJob.getLogHref());
				clientlibJobMap.put("result", clientlibJob.getResult());
				clientlibJobMap.put("results", clientlibJob.getResults());
				clientlibJobMap.put("arguments", clientlibJob.getArguments());
				clientlibJobMap.put("progressEstimate", clientlibJob.getProgressEstimate());
				clientlibJobMap.put("progressFrom", clientlibJob.getProgressFrom());
				clientlibJobMap.put("progressFromTime", clientlibJob.getProgressFromTime());
				clientlibJobMap.put("progressTo", clientlibJob.getProgressTo());
				if (clientlibJob.getScript() != null) {
					clientlibScriptMap.put("id", clientlibJob.getScript().getId());
					clientlibScriptMap.put("href", clientlibJob.getScript().getHref());
					clientlibScriptMap.put("nicename", clientlibJob.getScript().getNicename());
					clientlibScriptMap.put("version", clientlibJob.getScript().getVersion());
					clientlibScriptMap.put("description", clientlibJob.getScript().getDescription());
					clientlibScriptMap.put("homepage", clientlibJob.getScript().getHomepage());
					clientlibScriptMap.put("inputFilesets", clientlibJob.getScript().getInputFilesets());
					clientlibScriptMap.put("outputFilesets", clientlibJob.getScript().getOutputFilesets());
				}
				clientlibJobMap.put("script", clientlibScriptMap);
				output.put("engineJob", clientlibJobMap);
				output.put("results", Job.jsonifiableResults(clientlibJob));
			}
		}
		
		// messages can take a bit of time to parse so we parse them asynchronously and push them when they are ready
		Akka.system().scheduler().scheduleOnce(
				Duration.create(0, TimeUnit.SECONDS),
				new Runnable() {
					public void run() {
						org.daisy.pipeline.client.models.Job job = includeEngine ? webuiJob.getJobFromEngine(0) : webuiJob.asJob();
						if (job == null) {
							Logger.warn("Web UI job #"+webuiJob.getId()+" was not found in "+(includeEngine ? "the engine." : "the job storage."));
							Notification notification = new Notification("job-unavailable-"+webuiJob.getId(), true);
							NotificationConnection.pushJobNotification(webuiJob.getUser(), notification);
							return;
						}
						List<Message> messages = job.getMessages();
						Job.estimateMissingTimestamps(messages, webuiJob);
						if (messages != null) {
							for (Message message : messages) {
								Notification notification = new Notification("job-message-"+webuiJob.getId(), message);
								NotificationConnection.pushJobNotification(webuiJob.getUser(), notification);
							}
							
							Map<String,String> progressMap = new HashMap<String,String>();
							progressMap.put("from", job.getProgressFrom()+"");
							progressMap.put("to", job.getProgressTo()+"");
							double estimate = job.getProgressEstimate();
							if (estimate < job.getProgressFrom() || estimate >= job.getProgressTo()) {
								estimate = job.getProgressFrom(); // for some reason there's an error in the calculation (probably due to timestamps or similar); use "from" as estimate instead
							}
							progressMap.put("estimate", estimate+"");
							Notification notification = new Notification("job-progress-"+webuiJob.getId(), progressMap);
							NotificationConnection.pushJobNotification(webuiJob.getUser(), notification);
						}
					}
				},
				Akka.system().dispatcher()
				);
		
		JsonNode jobJson = play.libs.Json.toJson(output);
		return ok(jobJson);
	}
	
	public static Result getAllResults(Long id) {
		return getResult(id, null);
	}
	
	public static Result getResult(Long id, String href) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		Job webuiJob = Job.findById(id);
		if (webuiJob == null) {
			Logger.debug("Job #"+id+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
					)) {
				return forbidden("You are not allowed to view this job.");
		}
		
//		try {
			Logger.debug("retrieving result from Pipeline 2 engine...");
			
			Logger.debug("href: "+(href==null?"[null]":href));
			
			org.daisy.pipeline.client.models.Job job = webuiJob.getJobFromEngine(-1);
			
			if (href != null && href.length() > 0) {
				href = job.getHref() + "/result/" + href;
			}
			
			org.daisy.pipeline.client.models.Result result = job.getResultFromHref(href);
			if (result == null) {
				return badRequest("Could not find result: "+href);
			}
			
			String filename;
			if (result.filename != null) {
				filename = result.filename;
				
			} else if (result.href != null) {
				filename = result.href.replaceFirst("^.*/([^/]*)$", "$1");
				
			} else {
				if ("application/zip".equals(result.mimeType) || result.from != null) {
					filename = id+".zip";
					
				} else {
					filename = id+"";
				}
			}
			response().setHeader("Content-Disposition", "inline; filename=\""+filename+"\"");
			
			File resultFile = result.asFile();
			if (resultFile == null || !resultFile.exists()) {
				Logger.debug("Result file does not exist on disk; request directly from engine...");
				
				resultFile = Application.ws.getJobResultAsFile(job.getId(), href);
			}
			
			if (resultFile == null) {
				return badRequest("Result not found: "+href);
			}
			
			try {
				String contentType = ContentType.probe(resultFile.getName(), new FileInputStream(resultFile));
				response().setContentType(contentType);
				
				Logger.debug("contentType: "+contentType);
				
			} catch (FileNotFoundException e) {
				/* ignore */
			}
			
			long size = resultFile.length();
			if (size > 0) {
				response().setHeader("Content-Length", size+"");
				Logger.debug("size: "+size);
			} else {
				Logger.debug("content size unknown (size="+size+")");
			}
			
			String parse = request().getQueryString("parse");
			if ("report".equals(parse)) {
				response().setContentType("text/html");
				String report = Files.read(resultFile);
				Pattern regex = Pattern.compile("^.*<body[^>]*>(.*)</body>.*$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				Matcher regexMatcher = regex.matcher(report);
				String body = null;
				if (regexMatcher.find()) {
					body = regexMatcher.group(1);
				} else {
					Logger.debug("no body element found in report; returning the entire report");
					body = report;
				}
				final byte[] utf8Bytes = body.getBytes(StandardCharsets.UTF_8);
				response().setHeader(CONTENT_LENGTH, ""+utf8Bytes.length);
				return ok(body);
				
			}
			
			return ok(resultFile);
			
//		} catch (Pipeline2Exception e) {
//			Logger.error(e.getMessage(), e);
//			return Application.error(500, "Sorry, something unexpected occured", "A problem occured while communicating with the Pipeline engine", e.getMessage());
//		}
	}

	public static Result getLog(final Long id) {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		response().setHeader("Content-Disposition", "attachment; filename=\"job-"+id+".log\"");
		
		Job webuiJob = Job.findById(id);
		if (webuiJob == null) {
			Logger.debug("Job #"+id+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
			))
				return forbidden("You are not allowed to view this job.");
		
		String jobLog = Application.ws.getJobLog(webuiJob.getEngineId());
		//jobLog = org.daisy.pipeline.client.Jobs.getLog(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id);
		
		if (jobLog == null) {
			Pipeline2Logger.logger().error("Was not able to read job log for "+id);
			return Application.internalServerError("Unable to retrieve job log.");
		}
		
		if (jobLog.length() == 0) {
			return ok("The log is empty.");
		}
		
		return ok(jobLog);
	}

	public static Result postJob(Long jobId) {
		if (FirstUse.isFirstUse()) {
			return redirect(routes.FirstUse.getFirstUse());
		}

		User user = User.authenticate(request(), session());
		if (user == null) {
			return redirect(routes.Login.login());
		}
		
		Job job = Job.findById(jobId);
		if (job == null) {
			return notFound("The job with ID='"+jobId+"' was not found.");
		}
		
		if (job.getUser() != user.getId()) {
			return forbidden("You can only run your own jobs.");
		}
		
		Map<String, String[]> params = request().body().asFormUrlEncoded();
		if (params == null) {
			Logger.error("Could not read form data: "+request().body().asText());
			return internalServerError("Could not read form data");
		}
		
		String scriptId = params.get("_id")[0];
		if ("false".equals(UserSetting.get(user.getId(), "scriptEnabled-"+scriptId))) {
			return forbidden();
		}
		
		Script script = Scripts.get(scriptId);
		if (script == null) {
			Logger.error("An error occured while trying to read the script with id '"+scriptId+"'.");
			return Application.internalServerError("An error occured while trying to read the script with id '"+scriptId+"'.");
		}
		try {
			script = new Script(script.toXml()); // create new instance of script
			
		} catch (Pipeline2Exception e) {
			Logger.error("An error occured while trying to read the script with id '"+scriptId+"'.");
			return Application.internalServerError("An error occured while trying to read the script with id '"+scriptId+"'.");
		}
		
		org.daisy.pipeline.client.models.Job clientlibJob = job.asJob();
		clientlibJob.setScript(script);

		// Parse the submitted form
		Scripts.ScriptForm scriptForm = new Scripts.ScriptForm(user.getId(), script, params);
		scriptForm.validate();
		
		// If we're posting a template; delegate further processing to Templates.postTemplate
		if (params.containsKey("_submit_template")) {
			Logger.debug("posted job is a template");
			return Templates.postTemplate(user, job, clientlibJob);
		}
		Logger.debug("posted job is not a template");
		
		Logger.debug("------------------------------ Posting job... ------------------------------");
		Logger.debug(XML.toString(clientlibJob.toJobRequestXml(true)));
		clientlibJob = Application.ws.postJob(clientlibJob);
		if (clientlibJob == null) {
			Logger.error("An error occured when trying to post job");
			return internalServerError("An error occured when trying to post job");
		}
		job.setJob(clientlibJob);
		job.setStatus("IDLE");
		job.setStarted(null);
		job.setFinished(null);
		job.save();
		
		NotificationConnection.push(job.getUser(), new Notification("job-created-"+job.getId(), job.getCreated().toString()));
		
		JsonNode jobJson = play.libs.Json.toJson(job);
		Notification jobNotification = new Notification("new-job", jobJson);
		Logger.debug("pushed new-job notification with status=IDLE for job #"+jobId);
		NotificationConnection.pushJobNotification(job.getUser(), jobNotification);
		job.pushNotifications();
		
		if (user.getId() < 0 && scriptForm.guestEmail != null && scriptForm.guestEmail.length() > 0) {
			scriptForm.guestEmail = scriptForm.guestEmail.toLowerCase();
			String jobUrl = Application.absoluteURL(routes.Jobs.getJob(job.getId()).absoluteURL(request())+"?guestid="+(models.User.parseUserId(session())!=null?-models.User.parseUserId(session()):""));
			String html = views.html.Account.emailJobCreated.render(jobUrl, job.getNicename()).body();
			String text = "To view your Pipeline 2 job, go to this web address: " + jobUrl;
			if (Account.sendEmail("Job created: "+job.getNicename(), html, text, scriptForm.guestEmail, scriptForm.guestEmail))
				flash("success", "An e-mail was sent to "+scriptForm.guestEmail+" with a link to this job.");
			else
				flash("error", "Was unable to send an e-mail with a link to this job.");
		}
		
		Logger.debug("return redirect(controllers.routes.Jobs.getJob("+job.getId()+"));");
		return redirect(controllers.routes.Jobs.getJob(job.getId()));
	}
	
	public static Result delete(Long jobId) {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return unauthorized("unauthorized");
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		
		if (!(	user.isAdmin()
			||	webuiJob.getUser().equals(user.getId())
			||	webuiJob.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
			)) {
			return forbidden("You are not allowed to view this job.");
		}
		
		Logger.debug("deleting "+jobId);
		boolean deletedSuccessfully = webuiJob.deleteFromEngineAndWebUi();
		if (deletedSuccessfully) {
			return ok();
		} else {
			flash("error", "An error occured while trying to delete the job. Please try creating a new job instead.");
			return internalServerError();
		}
	}
	
	public static Result deleteRedirect(Long jobId) {
		if (FirstUse.isFirstUse()) {
			return redirect(routes.FirstUse.getFirstUse());
		}

		User user = User.authenticate(request(), session());
		if (user == null) {
			return redirect(routes.Login.login());
		}
		
		Job job = Job.findById(jobId);
		if (job == null) {
			return notFound("The job with ID='"+jobId+"' was not found.");
		}
		
		if (!(	user.isAdmin()
			||	job.getUser().equals(user.getId())
			||	job.getUser() < 0 && user.getId() < 0 && "true".equals(Setting.get("users.guest.shareJobs"))
			)) {
			return forbidden("You are not allowed to view this job.");
		}
		
		Logger.debug("deleting "+jobId);
		boolean deletedSuccessfully = job.deleteFromEngineAndWebUi();
		if (deletedSuccessfully) {
			flash("success", "Job #"+jobId+" was deleted.");
			return redirect(routes.Jobs.getJobs());
		} else {
			flash("error", "An error occured while trying to delete job #"+jobId+".");
			return redirect(routes.Jobs.getJob(jobId));
		}
	}
	
	public static Result postUpload(Long jobId) {
		if (FirstUse.isFirstUse())
			return forbidden();
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return forbidden();
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		
		MultipartFormData body = request().body().asMultipartFormData();
		List<FilePart> files = body.getFiles();
		
		List<Map<String,Object>> filesResult = new ArrayList<Map<String,Object>>();
		
		for (FilePart file : files) {
			Logger.info("uploaded file: "+file.getFile());
			// rename the uploaded file so that it is not automatically deleted by Play!
			File renamedFile = new File(file.getFile().getParentFile(), file.getFile().getName()+"_");
			try {
				java.nio.file.Files.move(file.getFile().toPath(), renamedFile.toPath());
				
			} catch (IOException e) {
				Logger.error("Could not rename uploaded file. Might be a problem with permissions. Trying copying instead...", e);
				try {
					Files.copy(file.getFile(), renamedFile);
					
				} catch (IOException ex) {
					Logger.error("Could not copy uploaded file.", ex);
					return internalServerError("Could not rename or make a copy of uploaded file.");
				}
			}
			
			Logger.debug(request().method()+" | "+file.getContentType()+" | "+file.getFilename()+" | "+renamedFile.getAbsolutePath());
			
			Map<String,Object> fileObject = new HashMap<String,Object>();
			fileObject.put("name", file.getFilename());
			fileObject.put("size", file.getFile().length());
			filesResult.add(fileObject);
			
			Akka.system().scheduler().scheduleOnce(
					Duration.create(0, TimeUnit.SECONDS),
					new Runnable() {
						public void run() {
							JobStorage jobStorage = (JobStorage)webuiJob.asJob().getJobStorage();
							File f = new File(file.getFile().getParentFile(), file.getFile().getName()+"_");
							
							Map<String,Object> result = new HashMap<String,Object>();
							result.put("fileName", file.getFilename());
							result.put("contentType", file.getContentType());
							result.put("total", f.length());
							
							if (file.getFilename().toLowerCase().endsWith(".zip")) {
								Logger.debug("adding zip file: "+file.getFilename());
								try {
									File tempDir = java.nio.file.Files.createTempFile("pipeline2-webui-upload", null).toFile();
									tempDir.delete();
									tempDir.mkdirs();
									Files.unzip(f, tempDir);
									
									List<Map<String,Object>> jsonFileset = new ArrayList<Map<String,Object>>();
									Map<String, File> files = Files.listFilesRecursively(tempDir, false);
									for (String href : files.keySet()) {
										Map<String,Object> fileResult = new HashMap<String,Object>();
										String contentType = ContentType.probe(href, new FileInputStream(files.get(href)));
										fileResult.put("fileName", href);
										fileResult.put("contentType", contentType);
										fileResult.put("total", files.get(href).length());
										fileResult.put("isXML", contentType != null && (contentType.equals("application/xml") || contentType.equals("text/xml") || contentType.endsWith("+xml")));
										jsonFileset.add(fileResult);
									}
									result.put("fileset", jsonFileset);
									result.put("jobId", jobId);
									
									Logger.debug("zip file contains "+tempDir.listFiles().length+" files");
									for (File dirFile : tempDir.listFiles()) {
										Logger.debug("top-level entry in zip: "+dirFile.getName());
										jobStorage.addContextFile(dirFile, dirFile.getName());
									}
									
								} catch (IOException e) {
									Logger.error("Failed to unzip uploaded zip file", e);
								}
								
							} else {
								Logger.debug("adding normal file: "+f.getName());
								
								List<Map<String,Object>> jsonFileset = new ArrayList<Map<String,Object>>();
								Map<String,Object> fileResult = new HashMap<String,Object>();
								String contentType;
								try {
									contentType = ContentType.probe(file.getFilename(), new FileInputStream(f));
								} catch (FileNotFoundException e) {
									contentType = "application/octet-stream";
								}
								fileResult.put("fileName", file.getFilename());
								fileResult.put("contentType", contentType);
								fileResult.put("total", f.length());
								fileResult.put("isXML", contentType != null && (contentType.equals("application/xml") || contentType.equals("text/xml") || contentType.endsWith("+xml")));
								jsonFileset.add(fileResult);
								result.put("fileset", jsonFileset);
								result.put("jobId", jobId);
								
								jobStorage.addContextFile(f, file.getFilename());
							}
							
							if (System.getProperty("os.name").startsWith("Windows")) {
								// Windows throws a java.nio.file.FileSystemException sometimes, complaining that the file is
								// in use by another process and cannot be moved. So in Windows we copy the file instead of moving it.
								jobStorage.save(false);
							} else {
								jobStorage.save(true);
							}

							NotificationConnection.push(user.getId(), new Notification("uploads", result));
						}
					},
					Akka.system().dispatcher()
					);
			
		}
		
		Map<String,List<Map<String,Object>>> result = new HashMap<String,List<Map<String,Object>>>();
		result.put("files", filesResult);
		
		response().setContentType("text/html");
		return ok(play.libs.Json.toJson(result));
		
	}
	
	/**
	 * Returns all the context files in the same format as is emitted when they were initially uploaded.
	 * This includes the filesize etc. and is useful for creating new jobs based on templates.
	 * 
	 * @param jobId
	 * @return context files as json
	 */
	public static Result getUploadsJson(Long jobId) {
		if (FirstUse.isFirstUse())
			return forbidden();
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return forbidden();
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		
		JobStorage jobStorage = (JobStorage)webuiJob.asJob().getJobStorage();
		Map<String, File> files = null;
		try {
			files = Files.listFilesRecursively(jobStorage.getContextDir(), false);
		} catch (IOException e) {
			Logger.error("Unable to list context files", e);
		}
		
		List<Map<String,Object>> jsonFileset = new ArrayList<Map<String,Object>>();
		if (files != null) {
			for (String href : files.keySet()) {
				Map<String,Object> fileResult = new HashMap<String,Object>();
				String contentType = null;
				try {
					contentType = ContentType.probe(href, new FileInputStream(files.get(href)));
				} catch (FileNotFoundException e) {
					Logger.error("[Job "+jobId+"] Could not probe "+href+" for its content type", e);
					contentType = "application/octet-stream";
				}
				fileResult.put("fileName", href);
				fileResult.put("contentType", contentType);
				fileResult.put("total", files.get(href).length());
				fileResult.put("isXML", contentType != null && (contentType.equals("application/xml") || contentType.equals("text/xml") || contentType.endsWith("+xml")));
				jsonFileset.add(fileResult);
			}
		}
		
		return ok(play.libs.Json.toJson(jsonFileset));
	}
	
	public static Result saveAsTemplate(Long jobId) {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return unauthorized("unauthorized");
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		
		return Templates.postTemplate(user, webuiJob, webuiJob.asJob());
	}
	
	public static Result downloadContext(Long jobId) {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return unauthorized("unauthorized");
		
		Job webuiJob = Job.findById(jobId);
		if (webuiJob == null) {
			Logger.debug("Job #"+jobId+" was not found.");
			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
		}
		
		File zip = webuiJob.asJob().getJobStorage().makeContextZip();
		
		if (zip != null && zip.exists()) {
			response().setHeader("Content-Disposition", "attachment; filename=\"Context files for job - "+jobId+".zip\"");
			response().setContentType("application/zip");
			long size = zip.length();
			if (size > 0) {
				response().setHeader("Content-Length", size+"");
			}
			
		} else {
			return internalServerError("Was unable to create zip of job context.");
		}
		
		return ok(zip);
	}
	
}
