package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Setting;
import models.User;

import play.Configuration;
import play.mvc.*;

public class Log extends Controller {
	
	/**
	 * GET /log
	 * Returns an exhaustive plaintext log
	 * @return
	 */
	public static Result getLog() {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null || !user.isAdmin())
			return redirect(routes.Login.login());
		
		Result result = ok(logText("Pipeline 2 Web UI Log", null));
				
		return result;
	}
	
	/**
	 * Aggregate Web UI and Engine logs for debugging
	 */
	public static String logText(String title, List<Map<String,List<String>>> additionalLogs) {
		List<Map<String,List<String>>> logs = additionalLogs == null ? new ArrayList<Map<String,List<String>>>() : additionalLogs;
		
		// Web UI settings
		{
			List<String> settings = new ArrayList<String>();
			settings.add("Database:");
			for (Setting s : Setting.find.all()) {
				if (Setting.getObfuscatedsettings().contains(s.getName()))
					settings.add("  "+s.getName()+": [hidden]");
				else
					settings.add("  "+s.getName()+": "+s.getValue());
			}
			settings.add("application.conf and system variables:");
			for (String config : Configuration.root().keys()) {
				try {
					if (config.contains("password"))
						settings.add("  "+config+": [hidden]");
					else
						settings.add("  "+config+": "+Configuration.root().getString(config));
				} catch (RuntimeException e) {
					// could not read a configuration variable; ignore
				}
			}
			Map<String,List<String>> log = new HashMap<String,List<String>>();
			log.put("Pipeline 2 Web UI - Settings", settings);
			logs.add(log);
		}
		
		// Web UI log
		{
			List<String> webuiLog = new ArrayList<String>();
			File webuiLogFile = new File(new File(new File(controllers.Application.DP2DATA), "logs"), "webui.log");
			try {
				FileInputStream stream = new FileInputStream(webuiLogFile);
				try {
					FileChannel fc = stream.getChannel();
					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
					/* Instead of using default, pass in a decoder. */
					webuiLog.add(Charset.defaultCharset().decode(bb).toString());
				}
				finally {
					stream.close();
				}
			} catch (IOException e) {
				webuiLog.add("An error occured while trying to read "+webuiLogFile.getAbsolutePath());
				StringWriter sw = new StringWriter();
	            PrintWriter pw = new PrintWriter(sw);
	            e.printStackTrace(pw);
	            webuiLog.add(sw.toString()); // stack trace as a string
			}
			Map<String,List<String>> log = new HashMap<String,List<String>>();
			log.put("Pipeline 2 Web UI - webui.log", webuiLog);
			logs.add(log);
		}
		
		// Engine log
		{
			List<String> daisyPipelineLog = new ArrayList<String>();
			File daisyPipelineLogFile = new File(new File(new File(controllers.Application.DP2DATA_ENGINE), "log"), "daisy-pipeline.log");
			try {
				FileInputStream stream = new FileInputStream(daisyPipelineLogFile);
				try {
					FileChannel fc = stream.getChannel();
					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
					/* Instead of using default, pass in a decoder. */
					daisyPipelineLog.add(Charset.defaultCharset().decode(bb).toString());
				}
				finally {
					stream.close();
				}
			} catch (IOException e) {
				daisyPipelineLog.add("An error occured while trying to read "+daisyPipelineLogFile.getAbsolutePath());
				StringWriter sw = new StringWriter();
	            PrintWriter pw = new PrintWriter(sw);
	            e.printStackTrace(pw);
	            daisyPipelineLog.add(sw.toString()); // stack trace as a string
			}
			Map<String,List<String>> log = new HashMap<String,List<String>>();
			log.put("Pipeline 2 Engine - daisy-pipeline.log", daisyPipelineLog);
			logs.add(log);
		}
		
		// Engine DB log
		{
			List<String> derbyLog = new ArrayList<String>();
			File derbyLogFile = new File(new File(new File(controllers.Application.DP2DATA_ENGINE), "log"), "derby.log");
			if (derbyLogFile.exists()) {
				try {
					FileInputStream stream = new FileInputStream(derbyLogFile);
					try {
						FileChannel fc = stream.getChannel();
						MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
						/* Instead of using default, pass in a decoder. */
						derbyLog.add(Charset.defaultCharset().decode(bb).toString());
					}
					finally {
						stream.close();
					}
				} catch (IOException e) {
					derbyLog.add("An error occured while trying to read "+derbyLogFile.getAbsolutePath());
					StringWriter sw = new StringWriter();
		            PrintWriter pw = new PrintWriter(sw);
		            e.printStackTrace(pw);
		            derbyLog.add(sw.toString()); // stack trace as a string
				}
			} else {
				derbyLog.add("There is no Derby log file at: "+derbyLogFile.getAbsolutePath());
			}
			Map<String,List<String>> log = new HashMap<String,List<String>>();
			log.put("Pipeline 2 Engine - derby.log", derbyLog);
			logs.add(log);
		}
		
		// Web UI DB log
		{
			File derbyLogFile = new File(new File(new File(controllers.Application.DP2DATA), "logs"), "webui-database.log");
			if (derbyLogFile.exists()) {
				List<String> derbyLog = new ArrayList<String>();
				try {
					FileInputStream stream = new FileInputStream(derbyLogFile);
					try {
						FileChannel fc = stream.getChannel();
						MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
						/* Instead of using default, pass in a decoder. */
						derbyLog.add(Charset.defaultCharset().decode(bb).toString());
					}
					finally {
						stream.close();
					}
				} catch (IOException e) {
					derbyLog.add("An error occured while trying to read "+derbyLogFile.getAbsolutePath());
					StringWriter sw = new StringWriter();
		            PrintWriter pw = new PrintWriter(sw);
		            e.printStackTrace(pw);
		            derbyLog.add(sw.toString()); // stack trace as a string
				}
				Map<String,List<String>> log = new HashMap<String,List<String>>();
				log.put("Pipeline 2 Web UI - webui-database.log", derbyLog);
				logs.add(log);
			}
		}
		
		// Compile plaintext log
		String logText = h1(title);
		for (Map<String,List<String>> l : logs) {
			String logTitle = null;
			for (String t : l.keySet())
				logTitle = t;
			List<String> log = l.get(logTitle);
			if (log == null)
				continue;
			logText += "\n\n\n";
			logText += h2(logTitle);
			if (log.isEmpty()) {
				logText += "[empty log]\n";
			} else {
				for (String line : log) {
					logText += line;
					if (!line.endsWith("\n")) logText += "\n";
				}
			}
		}
		return logText;
	}
	
	/** ISO date format */
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
	
	/** Helper function to create main headline in the plaintext logs */
	private static String h1(String title) {
		String time = "Time: "+df.format(new Date());
		String engineVersion = "";//"Pipeline 2 Engine Version: "+(Application.getAlive() == null ? "unknown" : Application.getAlive().version);
		String webuiVersion = "Pipeline 2 Web UI Version: "+Application.version;
		int width = Math.max(title.length(), Math.max(time.length(), Math.max(engineVersion.length(), webuiVersion.length())));
		while (title.length() < width) title += " ";
		while (time.length() < width) time += " ";
		while (engineVersion.length() < width) engineVersion += " ";
		while (webuiVersion.length() < width) webuiVersion += " ";
		int fillerLeftWidth = (int) Math.max(0,Math.floor(34.-width/2.));
		int fillerRightWidth = (int) Math.max(0,Math.ceil(34.-width/2.));
		String fillerLeft = "####";
		for (int i = fillerLeftWidth; i > 0; i--) fillerLeft += " ";
		fillerLeft += " ";
		String fillerRight = "";
		for (int i = fillerRightWidth; i > 0; i--) fillerRight += " ";
		fillerRight += " ####";
		String h1 = "";
		for (int i = 4+fillerLeftWidth+1+width+1+fillerRightWidth+4; i > 0; i--) h1 += "#";
		h1 += "\n";
		h1 += fillerLeft+title+fillerRight+"\n";
		h1 += fillerLeft+time+fillerRight+"\n";
		h1 += fillerLeft+engineVersion+fillerRight+"\n";
		h1 += fillerLeft+webuiVersion+fillerRight+"\n";
		for (int i = 4+fillerLeftWidth+1+width+1+fillerRightWidth+4; i > 0; i--) h1 += "#";
		return h1;
	}
	
	/** Helper function to create headlines for each separate log in the plaintext logs */
	private static String h2(String title) {
		int width = title.length();
		int fillerLeftWidth = (int) Math.max(0,Math.floor(34.-width/2.));
		int fillerRightWidth = (int) Math.max(0,Math.ceil(34.-width/2.));
		String h2 = "";
		for (int i = 4+fillerLeftWidth; i > 0; i--) h2 += "#";
		h2 += " "+title+" ";
		for (int i = 4+fillerRightWidth; i > 0; i--) h2 += "#";
		h2 += "\n";
		return h2;
	}
	
}
