package org.daisy.pipeline.client.models;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.daisy.pipeline.client.utils.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** A job message. */
public class Message implements Comparable<Message>, Iterable<Message>, Cloneable {
	
	public enum Level { ERROR, WARNING, INFO, DEBUG, TRACE };
	
	public Level level;
	public Integer sequence;
    public String text;
    public Integer parentSequence;
    ProgressInfo progressInfo;
    public Integer line;
    public Integer column;
    public Long timeStamp = new Date().getTime(); // NOTE: the timeStamp is currently not exposed through the web api so we just set it here to the time the object is instantiated instead.
    public String file;
    
    List<Message> children;
    Message parent;
    
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
	
	public int getDepth() {
		int depth = 0;
		Message parent = this.parent;
		while (parent != null) {
			depth++;
			parent = parent.parent;
		}
		return depth;
	}
	
	/** Most severe level among itself and it's sub-messages */
	public Level getInferredLevel() {
		Level inferredLevel = level;
		for (Message m : this) {
			Level childLevel = m.getInferredLevel();
			if (childLevel.compareTo(inferredLevel) < 0) {
				inferredLevel = childLevel;
			}
		}
		return inferredLevel;
	}
	
	public ProgressInfo getProgressInfo() {
		if (progressInfo != null && progressInfo.progress == null) {
			
			// compute the progress based on the child messages (only used for testing
			// because if the children of a message have progress, the total progress of
			// the parent message is normally available in its `progress` attribute)
			ProgressInfo p = new ProgressInfo();
			p.portion = progressInfo.portion;
			p.progress = BigDecimal.ZERO;
			if (children != null) {
				ProgressInfo sum = children
					.stream()
					.map(Message::getProgressInfo)
					.filter(o -> o != null)
					.reduce(
						(p1, p2) -> {
							if (p1 == null) return p2;
							else if (p2 == null) return p1;
							else {
								ProgressInfo s = new ProgressInfo();
								s.portion = p1.portion.add(p2.portion);
								s.progress = s.portion.compareTo(BigDecimal.ZERO) == 0
									? BigDecimal.ONE
									: p1.progress.multiply(p1.portion)
									    .add(p2.progress.multiply(p2.portion))
									    .divide(s.portion, MathContext.DECIMAL128);
								return s; }})
					.orElse(null);
				if (sum != null) {
					p.progress = sum.progress.multiply(sum.portion).min(BigDecimal.ONE);
				}
			}
			return p;
		} else {
			return progressInfo;
		}
	}
	
	/** Iterate through sub-messages */
	public Iterator<Message> iterator() {
		if (children != null)
			return children.iterator();
		else
			return Collections.<Message>emptyList().iterator();
	}
	
	/**
	 * Merge with a previous version of this message
	 */
	void join(Message oldMessage) {
		if (oldMessage.sequence != sequence) {
			throw new IllegalArgumentException();
		}
		Message.ProgressInfo progressInfo = getProgressInfo();
		if (progressInfo != null && progressInfo.equals(oldMessage.getProgressInfo())) {
			timeStamp = oldMessage.timeStamp;
		}
		Map<Integer,Message> index = new HashMap<Integer,Message>(); {
			for (Message m : this) {
				index.put(m.sequence, m);
			}
		}
		int i = 0;
		for (Message oldChild : oldMessage) {
			Message newChild = index.get(oldChild.sequence);
			if (newChild != null) {
				newChild.join(oldChild);
			} else {
				children.add(i++, oldChild);
				oldChild.parent = this;
			}
		}
	}
	
	public Document toXml() {
		Document xml = XML.getXml("<message xmlns=\"http://www.daisy.org/ns/pipeline/data\"/>");
		toXml(xml.getDocumentElement(), true);
		return xml;
	}
	
	void toXml(Element target, boolean progress) {
		if (level != null) {
			target.setAttribute("level", level.toString());
		}
		if (sequence != null) {
			target.setAttribute("sequence", ""+sequence);
		}
		if (line != null) {
			target.setAttribute("line", ""+sequence);
		}
		if (column != null) {
			target.setAttribute("column", ""+sequence);
		}
		if (timeStamp != null) {
			target.setAttribute("timeStamp", formatTimeStamp());
		}
		if (file != null) {
			target.setAttribute("file", file);
		}
		if (text != null) {
			target.setAttribute("content", text);
		}
		if (progress) {
			Message.ProgressInfo progressInfo = getProgressInfo();
			if (progressInfo != null
			    && progressInfo.portion.compareTo(BigDecimal.ZERO) > 0) {
				target.setAttribute("portion", Float.toString(progressInfo.portion.floatValue()));
				target.setAttribute("progress", Float.toString(progressInfo.progress.floatValue()));
			} else {
				progress = false;
			}
		}
		for (Message m : this) {
			Element childElem = target.getOwnerDocument().createElementNS(target.getNamespaceURI(), target.getLocalName());
			m.toXml(childElem, progress);
			target.appendChild(childElem);
		}
	}
	
	@Override
	public String toString() {
		Element elem = XML.getXml("<message/>").getDocumentElement();
		toXml(elem, true);
		return XML.toString(elem, outputProps);
	}
	
	private final static Map<String,String> outputProps = new HashMap<String,String>(); {
		outputProps.put(OutputKeys.INDENT, "yes");
		outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
	}
	
	/* Get a deep copy of this message and its children. The original parent reference is
	 * preserved. */
	@Override
	public Object clone() {
		Message clone; {
			try {
				clone = (Message)super.clone(); }
			catch (CloneNotSupportedException e) {
				throw new InternalError("coding error"); }}
		if (progressInfo != null) {
			clone.progressInfo = new ProgressInfo();
			clone.progressInfo.portion = progressInfo.portion;
			clone.progressInfo.progress = progressInfo.progress;
		}
		if (children != null) {
			clone.children = new ArrayList<Message>();
			for (Message child : children) {
				child = (Message)child.clone();
				child.parent = clone;
				clone.children.add(child);
			}
		}
		return clone;
	}
	
	public static class ProgressInfo {
		
		/** Portion of this block within the parent block. A number between 0 and 1. */
		public BigDecimal portion;
		
		/** The total progress of this block. A number between 0 and 1. */
		public BigDecimal progress;
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ProgressInfo)) {
				return false;
			}
			ProgressInfo that = (ProgressInfo)o;
			if (portion.compareTo(that.portion) != 0) {
				return false;
			}
			if (progress == null) {
				return that.progress == null;
			} else if (that.progress == null) {
				return false;
			} else {
				return (progress.compareTo(that.progress) == 0);
			}
		}
	}
}
