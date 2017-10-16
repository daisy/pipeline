package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;

import org.daisy.pipeline.client.Pipeline2Logger;

public class JobMessages extends AbstractList<Message> {
	
	private List<Message> backingList = new ArrayList<Message>();
	
	public Message get(int index) {
		updateProgress();
		return backingList.get(index);
	}

	public int size() {
		return backingList.size();
	}
	
	@Override
	public void add(int index, Message element) {
		if (element == null) {
			throw new NullPointerException();
		}
		backingList.add(index, element);
		dirty = dirty || index < lastMessageCount;
		modCount++;
	}
	
	@Override
	public Message set(int index, Message element) {
		if (element == null) {
			throw new NullPointerException();
		}
		Message prev = backingList.set(index, element);
		dirty = dirty || (index < lastMessageCount && !element.equals(prev));
		modCount++;
		return prev;
	}
	
	@Override
	public Message remove(int index) {
		dirty = dirty || index < lastMessageCount;
		modCount++;
		return backingList.remove(index);
	}
	
	public double getProgressEstimate(Long now) {
		updateProgress();
		Long previousTime = getProgressFromTime() == null ? now : getProgressFromTime();
		Double previousPercentage = getProgressFrom();
		Double nextPercentage = getProgressTo();
		return nextPercentage - (nextPercentage - previousPercentage) * Math.exp(-(double)(now - previousTime) / progressTimeConstant);
	}
	
	private double progressLastPercentage;
	private double progressNextPercentage;
	private Long progressFirstTime;
	private Long progressLastTime;
	private double progressTimeConstant;
	
	private static double initialProgressLastPercentage = 0.0;
	private static double initialProgressNextPercentage = 100.0;
	private static Long initialProgressFirstTime = null;
	private static Long initialProgressLastTime = null;
	private static double initialProgressTimeConstant = 20000.0;
	
	double getProgressFrom() {
		updateProgress();
		return progressLastPercentage;
	}
	
	double getProgressTo() {
		updateProgress();
		return progressNextPercentage;
	}
	
	Long getProgressFromTime() {
		updateProgress();
		return progressLastTime;
	}
	
	// Progress format: [progress name FROM-TO sub-name]
	// in the main script, name must be omitted, otherwise,
	// name must be the same as either the preceding steps name or sub-name.
	// FROM is required and must be an integer in the range [0,100].
	// TO is optional if sub-name is omitted and will default to 100.
	// If not omitted, TO must also be an integer in the range [0,100],
	// and must be larger than or equal to FROM.
	// sub-name is the name of a sub-step and can be used to get a more
	// fine-grained progress info.
	private static final Pattern PROGRESS_PATTERN;
	static {
		PROGRESS_PATTERN = Pattern.compile("^\\[progress(| [^\\s\\]]+) (\\d+)([^\\s\\]]* ?[^\\s\\]]*)\\] *(.*?)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}
	// $1: " my-name"
	// $2: "from"
	// $2: "-to sub-name"
	
	public class Progress {
		private Pattern namePattern;
		private String nameString;
		private boolean nameIsGlob = false;
		
		public double from = 0.0;
		public double to = 100.0;
		public long timeStamp = new Date().getTime();
		
		public boolean active = false; // will be set to true when a message on this level arrives
		
		public Message currentMessage = null;
		public Message.Level currentLevel = Message.Level.TRACE;
		
		public Progress(String name) {
			if (name.contains("*") || name.contains("?")) {
				String regexName = "";
				for (char c : name.toCharArray()) {
					regexName += c == '?' ? "." : c == '*' ? ".*" : c == '.' ? "\\." : c;
				}
				this.namePattern = Pattern.compile(regexName, Pattern.DOTALL);
				this.nameIsGlob = true;
				
			} else {
				this.nameString = name;
			}
		}
		
		public String getName() {
			if (nameIsGlob) {
				return namePattern == null ? null : namePattern.pattern();
				
			} else {
				return nameString;
			}
		}
		
		public boolean matchesName(String name) {
			if (nameIsGlob) {
				if (active) { return false; } // only the first match should be used; otherwise a '*' would match all following messages
				return namePattern == null ? false : namePattern.matcher(name).matches();
				
			} else {
				return nameString == null ? false : nameString.equals(name);
			}
		}
		
		public void resolveGlob(String name) {
			this.nameString = name;
			this.nameIsGlob = false;
		}
		
		public void updateSeverity(Message.Level level) {
			if (level != null && currentMessage != null) {
				if (currentMessage.inferredLevel == null || level.compareTo(currentMessage.inferredLevel) < 0) {
					currentMessage.inferredLevel = level;
				}
			}
			
			if (currentLevel == null) {
				currentLevel = level;
				
			} else if (level != null) {
				if (level.compareTo(currentLevel) < 0) {
					currentLevel = level;
				}
			}
		}
	}
	
	private Stack<Progress> currentProgress = new Stack<Progress>();
	private int lastMessageCount = 0;
	private boolean dirty = true;
	private void updateProgress() {
		if (!dirty && lastMessageCount == size()) {
			return; // no new messages => no need to update
		}
		if (dirty) { // reset to initial state
			progressLastPercentage = initialProgressLastPercentage;
			progressNextPercentage = initialProgressNextPercentage;
			progressFirstTime = initialProgressFirstTime;
			progressLastTime = initialProgressLastTime;
			progressTimeConstant = initialProgressTimeConstant;
			currentProgress.clear();
			lastMessageCount = 0;
		}
		if (currentProgress.isEmpty()) {
			Progress mainProgress = new Progress("");
			mainProgress.timeStamp = new Long(backingList.get(0).timeStamp);
			currentProgress.push(mainProgress);
		}
		
		boolean progressUpdated = false;
		for (int i = lastMessageCount; i < size(); i++) {
			Message m = backingList.get(i);
			m.inferredLevel = m.level;
			if (currentProgress.peek().active) {
				m.depth = currentProgress.size();
				currentProgress.peek().updateSeverity(m.level);
				
			} else {
				m.depth = currentProgress.size() - 1;
				if (currentProgress.size() > 1) {
					currentProgress.get(currentProgress.size()-2).updateSeverity(m.level);
				}
			}
			
			// set progressFirstTime to time of first message (i.e. job start time)
			if (progressFirstTime == null) {
				progressFirstTime = m.timeStamp;
				progressLastTime = progressFirstTime;
			}
			
			if (m.text != null && m.text.contains("[")) {

				// if there's progress info in the message
				Matcher matcher = PROGRESS_PATTERN.matcher(m.text);
				if (matcher.find()) {
					String myName = matcher.group(1).trim();
					String from = matcher.group(2).trim();
					String to = matcher.group(3);
					String sub = "";
					if (to.contains(" ")) {
						String[] split = to.split(" ");
						to = split[0].trim();
						sub = split[1].trim();
					}

					// determine progress depth
					boolean validProgress = false;
					int depth = 0;
					for (Progress p : currentProgress) {
						if (p.matchesName(myName)) {
							validProgress = true;
							break;
						} else {
							if (p.active) {
								depth++;
							}
						}
					}
					m.depth = depth;
					
					if (!validProgress) {
						continue;
					}

					// remove progress elements nested under myName
					for (int j = currentProgress.size() - 1; j > depth; j--) {
						Progress poppedProgress = currentProgress.pop();
						currentProgress.peek().updateSeverity(poppedProgress.currentLevel);
					}

					// update progress element with new info
					Progress progress = currentProgress.peek();
					if (!"".equals(sub)) {
						Progress subProgress = new Progress(sub);
						subProgress.timeStamp = new Long(m.timeStamp);
						currentProgress.push(subProgress);
					}
					
					if (progress.matchesName(myName)) {
						progress.resolveGlob(myName);
						progress.active = true;
						progress.currentMessage = m;
						int parsedFrom = -1;
						int parsedTo = -1;
						int parsedTotal = -1;
						if (!"".equals(from)) {
							try { parsedFrom = Integer.parseInt(from); }
							catch (NumberFormatException e) { Pipeline2Logger.logger().warn("Unable to parse progress integer: '"+from+"'."); }
						}
						if (!"".equals(to)) {
							if (to.startsWith("/")) {
								try { parsedTotal = Integer.parseInt(to.substring(1)); }
								catch (NumberFormatException e) { Pipeline2Logger.logger().warn("Unable to parse fractioned progress 'total' integer: '"+to+"'."); }
							} else {
								try { parsedTo = Math.abs(Integer.parseInt(to)); }
								catch (NumberFormatException e) { Pipeline2Logger.logger().warn("Unable to parse ranged progress 'to' integer: '"+to+"'."); }
							}
						}
						if (parsedFrom >= 0) {
							if (parsedTo < 0) {
								// cumulative progress (optionally fractioned)
								if (!(progress.from == 0.0 && progress.to == 100.0)) {
									progress.from = progress.to;
								}
								double total = parsedTotal < 0 ? 100.0 : parsedTotal;
								progress.to = Math.min(100, progress.from + parsedFrom * 100.0 / total);
								
							} else {
								// ranged progress
								progress.from = parsedFrom;
								progress.to = parsedTo;
							}
							progress.timeStamp = m.timeStamp;
							progressUpdated = true;
						}
						
					} else {
						// progress info with wrong name => ignore it
					}
				}
			}
		}
		
		// infer levels for active progress objects
		for (int j = currentProgress.size() - 1; j > 0; j--) {
			if (currentProgress.get(j).currentMessage != null) {
				currentProgress.get(j-1).updateSeverity(currentProgress.get(j).currentLevel);
			}
		}
		
		if (progressUpdated) {
			// calculate current progress
			progressLastTime = currentProgress.peek().timeStamp;
			progressLastPercentage = 0.0;
			progressNextPercentage = 100.0;
			for (Progress p : currentProgress) {
				double lastPercentage = progressLastPercentage + (progressNextPercentage - progressLastPercentage) / 100.0 * p.from;
				double nextPercentage = progressLastPercentage + (progressNextPercentage - progressLastPercentage) / 100.0 * p.to;
				progressLastPercentage = lastPercentage;
				progressNextPercentage = nextPercentage;
			}
			if (progressLastPercentage > 0 && progressFirstTime != null && progressLastTime != null && progressLastTime - progressFirstTime > 0 && progressNextPercentage - progressLastPercentage > 0.0) {
				progressTimeConstant = - (progressLastTime-progressFirstTime) * (progressNextPercentage/progressLastPercentage - 1.0) / Math.log(0.05);
			}
		}

		lastMessageCount = size();
		dirty = false;
	}
	
	/**
	 * The progress stack contains information about the progress at each progress "depth".
	 * @return the progress stack
	 */
	public Stack<Progress> getProgressStack() {
		return currentProgress;
	}
	
}
