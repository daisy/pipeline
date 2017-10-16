package org.daisy.pipeline.client.models;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A job message. */
public class Message implements Comparable<Message> {
	
	public enum Level { ERROR, WARNING, INFO, DEBUG, TRACE };
	
	public Level level;
	public Integer sequence;
    public String text;
    public Integer depth = 0;
    public Integer line;
    public Integer column;
    public Long timeStamp = new Date().getTime(); // NOTE: the timeStamp is currently not exposed through the web api so we just set it here to the time the object is instantiated instead.
    public String file;
    
    public Level inferredLevel; // most severe level among itself and it's sub-messages in the message tree created based on the progress messages
    
    @Override
	public int compareTo(Message other) {
		return this.sequence - other.sequence;
	}

	public void setTimeStamp(String timeStampString) {
		// TODO timeStamp not exposed through the web api yet. Assume UNIX time for now. See: https://github.com/daisy/pipeline-framework/issues/109
		timeStamp = Long.parseLong(timeStampString);
	}

	public String formatTimeStamp() {
		// TODO timeStamp not exposed through the web api yet. Assume UNIX time for now. See: https://github.com/daisy/pipeline-framework/issues/109
		return ""+timeStamp;
	}
	
	public String getText() {
		if (this.text == null) {
			return null;
		} else { // remove progress info from message
			Matcher m = MESSAGE_PATTERN.matcher(this.text);
			m.matches();
			String text = m.group("msg");
			return text == null ? "" : text;
		}
	}
	
	public String getProgressInfo() {
		Matcher m = MESSAGE_PATTERN.matcher(this.text);
		m.matches();
		String progressInfo = m.group("progress");
		return progressInfo == null ? "" : progressInfo;
	}
	
	private static final Pattern MESSAGE_PATTERN
		= Pattern.compile("^(?<progress>\\[[Pp][Rr][Oo][Gg][Rr][Ee][Ss][Ss][^\\]]*\\])? *(?<msg>.+)?$", Pattern.DOTALL);
	
}
