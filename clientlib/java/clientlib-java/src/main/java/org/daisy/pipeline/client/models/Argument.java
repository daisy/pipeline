package org.daisy.pipeline.client.models;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** An argument of type "string" */
public class Argument {

	/** The name of the option. This isn't necessarily unique; since inputs and options can have the same name. */
	private String name;

	/** This is the value from the px:role="name" in the script documentation. */
	private String nicename;

	/** A description of the option. */
	private String desc;

	/** whether or not this option is required */
	private Boolean required;

	/** whether or not multiple selections can be made */
	private Boolean sequence;

	/** MIME types accepted (only relevant if type=anyDirURI or anyFileURI) */
	private List<String> mediaTypes;

	/** Options with a output value of "result" or "temp" will only be included when the framework is running in local mode. */
	private Output output;
	public enum Output { result, temp };

	/** Type of underlying option. Either "input", "option" or "output". ("parameters" currently not supported) */
	private Kind kind;
	public enum Kind { input, /*parameters,*/ option, output };

	/** whether or not the ordering matters (only relevant if sequence==true) */
	private Boolean ordered;

	/** XSD type or custom data type */
	private String type;
	
	/** The default value for this argument */
	private String defaultValue;
	
	private Node argumentNode;
	private boolean lazyLoaded = false;

	private List<String> values = null;

	/** Create option instance from option node.
	 * 
	 *  @param argumentNode the XML
	 *  @throws Pipeline2Exception thrown if an error occurs
	 */
	public Argument(Node argumentNode) throws Pipeline2Exception {
		this.argumentNode = argumentNode;
	}

	private void lazyLoad() {
		if (!lazyLoaded && argumentNode != null) {
			try {
				this.name = parseTypeString(XPath.selectText("@name", argumentNode, XPath.dp2ns));

				this.nicename = parseTypeString(XPath.selectText("@nicename", argumentNode, XPath.dp2ns));
				if (this.nicename == null || "".equals(this.nicename))
					this.nicename = this.name;

				this.desc = XPath.selectText("@desc", argumentNode, XPath.dp2ns);
				if (this.desc == null)
					this.desc = "";

				this.required = parseTypeBoolean(XPath.selectText("@required", argumentNode, XPath.dp2ns));
				if (this.required == null)
					this.required = true;

				this.sequence = parseTypeBoolean(XPath.selectText("@sequence", argumentNode, XPath.dp2ns));
				if (this.sequence == null)
					this.sequence = false;

				this.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", argumentNode, XPath.dp2ns));

				try {
					this.output = Output.valueOf(parseTypeString(XPath.selectText("@outputType", argumentNode, XPath.dp2ns)));
				} catch (IllegalArgumentException e) {
					this.kind = null;
				} catch (NullPointerException e) {
					this.kind = null;
				}

				try {
					this.kind = Kind.valueOf(argumentNode.getLocalName()); // TODO "parameters": how to determine that a port is a parameter port?
				} catch (IllegalArgumentException e) {
					this.kind = null;
				} catch (NullPointerException e) {
					this.kind = null;
				}

				this.ordered = parseTypeBoolean(XPath.selectText("@ordered", argumentNode, XPath.dp2ns));
				if (this.ordered == null)
					this.ordered = true;

				this.type = parseTypeString(XPath.selectText("@type", argumentNode, XPath.dp2ns));
				if (this.type == null)
					this.type = "string";

				if (this.kind == Kind.input || this.kind == Kind.output) {
					this.type = "anyFileURI";
					
					if (this.sequence && parseTypeBoolean(XPath.selectText("@required", argumentNode, XPath.dp2ns)) == null)
						this.required = false;

					if (this.mediaTypes.size() == 0)
						this.mediaTypes.add("application/xml");
				}

				if (this.kind == Kind.output && this.output == null) {
					this.output = Output.result;
				}

				this.defaultValue = XPath.selectText("@default", argumentNode, XPath.dp2ns);

				List<Node> valueNodes = XPath.selectNodes("d:item", argumentNode, XPath.dp2ns);
				if (valueNodes.isEmpty()) {
					String value = XPath.selectText("text()", argumentNode, XPath.dp2ns);
					if (value != null && !"".equals(value)) {
						this.values = new ArrayList<String>();
						this.values.add(normalizeValue(value));
					}

				} else {
					this.values = new ArrayList<String>();
					for (Node valueNode : valueNodes) {
						String value = XPath.selectText("@value", valueNode, XPath.dp2ns);
						this.values.add(normalizeValue(value));
					}
				}

			} catch (Pipeline2Exception e) {
				Pipeline2Logger.logger().error("Failed to parse argument node", e);
			}
			lazyLoaded = true;
		}
	}
	
	private String normalizeValue(String value) {
		if (type == null || value == null) {
			return null;
		}
		
		switch (this.type) {
		case "anyFileURI":
		case "anyDirURI":
		case "anyURI":
			try {
				URI uri = new URI(value);
				uri = uri.normalize();
				value = uri.toString();
				break;
				
			} catch (URISyntaxException e) {
				Pipeline2Logger.logger().warn("Unable to parse URI", e);
			}
			break;
			
		case "boolean":
			value = value.toLowerCase();
		}
		
		return value;
	}

	/** Helper function for the Script(Document) constructor */
	private static String parseTypeString(String string) {
		if (!(string instanceof String))
			return null;
		string = string.replaceAll("\"", "'").replaceAll("\\n", " ");
		if ("".equals(string)) return null;
		else return string;
	}

	/** Helper function for the Script(Document) constructor */
	private static Boolean parseTypeBoolean(String bool) {
		if (!(bool instanceof String))
			return null;
		if ("false".equals(bool))
			return false;
		if ("true".equals(bool))
			return true;
		return null;
	}

	/** Helper function for the Script(Document) constructor */
	private static List<String> parseTypeMediaTypes(String mediaTypesString) {
		if (!(mediaTypesString instanceof String))
			return new ArrayList<String>();
		mediaTypesString = parseTypeString(mediaTypesString);
		String[] mediaTypes = (mediaTypesString==null?"":mediaTypesString).split(" ");
		List<String> mediaTypesList = new ArrayList<String>();
		for (String mediaType : mediaTypes) {
			if ("".equals(mediaType))
				continue;

			if ("text/xml".equals(mediaType))
				mediaTypesList.add("application/xml");
			else
				mediaTypesList.add(mediaType);
		}
		return mediaTypesList;
	}

	/**
	 * Returns the number of values defined for the option or input.
	 * 
	 * @return number of values
	 */
	public int size() {
		lazyLoad();
		if (values == null) {
			return 0;

		} else {
			return values.size();
		}
	}

	/**
	 * Unset the given option or input.
	 * 
	 * This is different from clearing the option in that it will no longer be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 */
	public void unset() {
		lazyLoad();
		if (values != null) {
			values.clear();
		}
		values = null;
	}

	/**
	 * Unset the given option or input.
	 * 
	 * This is different from clearing the option in that it will no longer be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 * 
	 * @return True if the argument is defined/set. False otherwise.
	 */
	public boolean isDefined() {
		lazyLoad();
		return values != null;
	}

	/**
	 * Clear the given option or input.
	 * 
	 * This is different from unsetting the option in that it will still be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 */
	public void clear() {
		lazyLoad();
		if (values == null) {
			values = new ArrayList<String>();
		} else {
			values.clear();
		}
	}

	/** Replace the value at the given position with the provided Integer value.
	 * 
	 *  @param position The position
	 *  @param value the value to use
	 */
	public void set(int position, Integer value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}

	/** Replace the value at the given position with the provided Long value.
	 * 
	 *  @param position The position
	 *  @param value the value to use
	 */
	public void set(int position, Long value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}

	/** Replace the value at the given position with the provided Double value.
	 * 
	 *  @param position The position
	 *  @param value the value to use
	 */
	public void set(int position, Double value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}

	/** Replace the value at the given position with the provided Boolean value.
	 * 
	 *  @param position The position
	 *  @param value the value to use
	 */
	public void set(int position, Boolean value) {
		if (value == null) {
			clear();
		} else {
			set(position, value+"");
		}
	}

	/** Replace the value at the given position with the provided File value.
	 * 
	 *  @param position The position
	 *  @param file the file to use
	 *  @param context the job context
	 */
	public void set(int position, File file, JobStorage context) {
		if (file == null) {
			clear();
		} else {
			context.addContextFile(file, file.getName());
			set(position, context.getContextFilePath(file));
		}
	}

	/** Replace the value at the given position with the provided String value.
	 * 
	 *  @param position The position
	 *  @param value the value to use
	 */
	public void set(int position, String value) {
		if (value == null) {
			clear();
		} else {
			lazyLoad();
			if (values != null && values.size() > position) {
				values.set(position, value);
			}
		}
	}

	/** Replace the value with the provided Integer value.
	 * 
	 *  @param value the value to use
	 */
	public void set(Integer value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}

	/** Replace the value with the provided Long value.
	 * 
	 *  @param value the value to use
	 */
	public void set(Long value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}

	/** Replace the value with the provided Double value.
	 * 
	 *  @param value the value to use
	 */
	public void set(Double value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}

	/** Replace the value with the provided Boolean value.
	 * 
	 *  @param value the value to use
	 */
	public void set(Boolean value) {
		if (value == null) {
			clear();
		} else {
			set(value+"");
		}
	}

	/** Replace the value with the provided File value.
	 * 
	 *  @param file the file to use
	 *  @param context the job context
	 */
	public void set(File file, JobStorage context) {
		if (file == null) {
			clear();
			
		} else if (getOutput() != null) {
			set(file.toURI().toString());
			
		} else {
			context.addContextFile(file, file.getName());
			set(context.getContextFilePath(file));
		}
	}

	/** Replace the value with the provided String value.
	 * 
	 *  @param value the value to use
	 */
	public void set(String value) {
		clear();
		if (value != null) {
			if (values == null) {
				values = new ArrayList<String>();
			}
			values.add(normalizeValue(value));
		}
	}

	/** Replace the values with all the provided String values.
	 * 
	 *  @param values the value to use */
	public void setAll(Collection<String> values) {
		clear();
		if (this.values == null) {
			this.values = new ArrayList<String>();
		}
		for (String value : values) {
			this.values.add(normalizeValue(value));
		}
	}

	/** Add to the list of values the provided Integer value.
	 * 
	 *  @param value the value to use
	 */
	public void add(Integer value) {
		if (value != null) {
			add(value+"");
		}
	}

	/** Add to the list of values the provided Long value.
	 * 
	 *  @param value the value to use
	 */
	public void add(Long value) {
		if (value != null) {
			add(value+"");
		}
	}

	/** Add to the list of values the provided Double value.
	 * 
	 *  @param value the value to use
	 */
	public void add(Double value) {
		if (value != null) {
			add(value+"");
		}
	}

	/** Add to the list of values the provided Boolean value.
	 * 
	 *  @param value the value to use
	 */
	public void add(Boolean value) {
		if (value != null) {
			add(value+"");
		}
	}

	/** Add to the list of values the provided File value.
	 * 
	 *  @param file the file to use
	 *  @param context the job context
	 */
	public void add(File file, JobStorage context) {
		if (file != null) {
			lazyLoad();
			context.addContextFile(file, file.getName());
			add(context.getContextFilePath(file));
		}
	}

	/** Add to the list of values the provided String value.
	 * 
	 *  @param value the value to use
	 */
	public void add(String value) {
		if (value != null) {
			lazyLoad();
			if (this.values == null) {
				this.values = new ArrayList<String>();
			}
			values.add(normalizeValue(value));
		}
	}

	/** Add to the list of values all the provided String values.
	 * 
	 *  @param values the values to use */
	public void addAll(Collection<String> values) {
		if (values != null) {
			if (this.values == null) {
				this.values = new ArrayList<String>();
			}
			for (String value : values) {
				values.add(normalizeValue(value));
			}
		}
	}

	/** Remove all occurences of the provided Integer value from the list of values.
	 * 
	 *  @param value the value to use
	 */
	public void remove(Integer value) {
		if (value != null) {
			remove(value+"");
		}
	}

	/** Remove all occurences of the provided Long value from the list of values.
	 * 
	 *  @param value the value to use
	 */
	public void remove(Long value) {
		if (value != null) {
			remove(value+"");
		}
	}

	/** Remove all occurences of the provided Double value from the list of values.
	 * 
	 *  @param value the value to use
	 */
	public void remove(Double value) {
		if (value != null) {
			remove(value+"");
		}
	}

	/** Remove all occurences of the provided Boolean value from the list of values.
	 * 
	 *  @param value the value to use
	 */
	public void remove(Boolean value) {
		if (value != null) {
			remove(value+"");
		}
	}

	/** Remove all occurences of the provided File value from the list of values.
	 * 
	 *  @param file the file to use
	 *  @param context the job context
	 */
	public void remove(File file, JobStorage context) {
		if (file != null) {
			remove(context.getContextFilePath(file));
		}
	}

	/** Remove all occurences of the provided String value from the list of values.
	 * 
	 *  @param value the value to use
	 */
	public void remove(String value) {
		if (value != null && values != null) {
			for (int i = values.size() - 1; i >= 0 ; i--) {
				if (value.equals(values.get(i))) {
					values.remove(i);
				}
			}
		}
	}

	/** Remove the first occurences of all the provided String values from the list of values.
	 * 
	 *  @param values the value to use
	 */
	public void removeAll(Collection<String> values) {
		if (values != null && this.values != null) {
			this.values.removeAll(values);
		}
	}

	/** Get the value as a Integer.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Integer, or if the value is not set.
	 *  
	 *  @return the value as a Integer
	 */
	public Integer getAsInteger() {
		lazyLoad();
		try {
			return Integer.parseInt(get());

		} catch (Exception e) {
			return null;
		}
	}

	/** Get the value as a Long.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Long, or if the value is not set.
	 *  
	 *  @return the value as a Long
	 */
	public Long getAsLong() {
		lazyLoad();
		try {
			return Long.parseLong(get());

		} catch (Exception e) {
			return null;
		}
	}

	/** Get the value as a Double.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Double, or if the value is not set.
	 *  
	 *  @return the value as a Double
	 */
	public Double getAsDouble() {
		lazyLoad();
		try {
			return Double.parseDouble(get());

		} catch (Exception e) {
			return null;
		}
	}

	/** Get the value as a Boolean.
	 *
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a Boolean, or if the value is not set.
	 *  
	 *  @return the value as a Boolean
	 */
	public Boolean getAsBoolean() {
		lazyLoad();
		String value = get();
		if (value != null && ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()))) {
			return Boolean.parseBoolean(get());

		} else {
			return null;
		}
	}

	/** Get the value as a File.
	 * 
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value cannot be parsed as a File, or if the value is not set.
	 *  
	 *  @param context the job context
	 *  @return the value as a File
	 */
	public File getAsFile(JobStorage context) {
		lazyLoad();
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return context.getContextFile(values.get(0));
		}
	}

	/** Get the value as a String.
	 * 
	 *  Returns the first value if there are more than one.
	 *  Returns null if the value is not set.
	 *  If the option or input is a sequence, you should use {@link #getAsList() getAsList} to get all values instead.
	 *  
	 * @return the value as a String
	 */
	public String get() {
		lazyLoad();
		if (values == null || values.size() == 0) {
			return null;
		} else {
			return values.get(0);
		}
	}

	/** Get all the values as a List of Strings.
	 * 
	 * @return null if the value is not set. */
	public List<String> getAsList() {
		lazyLoad();
		return values;
	}

	/** Get all the values as a List of Files.
	 * 
	 *  @param context the job context
	 *  @return null if any of the values cannot be parsed as a File, or if the value is not set. */
	public List<File> getAsFileList(JobStorage context) {
		lazyLoad();
		if (values != null) {
			List<File> contextFiles = new ArrayList<File>();
			assert(contextFiles != null);
			for (String value : values) {
				File contextFile = context.getContextFile(value);
				contextFiles.add(contextFile);
			}
			return contextFiles;

		} else {
			return null;
		}
	}

	/**
	 * Move a value from one position in the value list to another.
	 * 
	 * @param from which value to move
	 * @param to which position to move the value
	 */
	public void moveTo(int from, int to) {
		lazyLoad();
		if (values == null) {
			return;
		}
		if (from < 0 || from >= values.size()) {
			return;
		}
		if (to < 0 || to >= values.size()) {
			return;
		}
		int shiftDistance = -1;
		if (from > to) {
			int rememberMe = from;
			from = to;
			to = rememberMe;
			shiftDistance = 1;
		}
		Collections.rotate(values.subList(from, to+1), shiftDistance);
	}
	
	/** Get the default value as a Integer.
	 *
	 *  Returns null if the value cannot be parsed as a Integer, or if the value is not set.
	 *  
	 *  @return the default value as a Integer
	 */
	public Integer getDefaultValueAsInteger() {
	    lazyLoad();
	    try {
	        return Integer.parseInt(getDefaultValue());

	    } catch (Exception e) {
	        return null;
	    }
	}

	/** Get the default value as a Long.
	 *
	 *  Returns null if the value cannot be parsed as a Long, or if the value is not set.
	 *  
	 *  @return the default value as a Long
	 */
	public Long getDefaultValueAsLong() {
	    lazyLoad();
	    try {
	        return Long.parseLong(getDefaultValue());

	    } catch (Exception e) {
	        return null;
	    }
	}

	/** Get the default value as a Double.
	 *
	 *  Returns null if the value cannot be parsed as a Double, or if the value is not set.
	 *  
	 *  @return the default value as a Double
	 */
	public Double getDefaultValueAsDouble() {
	    lazyLoad();
	    try {
	        return Double.parseDouble(getDefaultValue());

	    } catch (Exception e) {
	        return null;
	    }
	}

	/** Get the default value as a Boolean.
	 *
	 *  Returns null if the value cannot be parsed as a Boolean, or if the value is not set.
	 *  
	 *  @return the default value as a Boolean
	 */
	public Boolean getDefaultValueAsBoolean() {
	    lazyLoad();
	    String value = getDefaultValue();
	    if (value != null && ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()))) {
	        return Boolean.parseBoolean(getDefaultValue());

	    } else {
	        return null;
	    }
	}

	/** Get the default value as a File.
	 * 
	 *  Returns null if the value cannot be parsed as a File, or if the value is not set.
	 *  
	 *  @param context the job context
	 *  @return the default value as a File
	 */
	public File getDefaultValueAsFile(JobStorage context) {
	    lazyLoad();
	    String value = getDefaultValue();
	    if (value == null) {
	        return null;
	    } else {
	        return context.getContextFile(value);
	    }
	}

	/** Get the default value as a String.
	 * 
	 *  Returns null if the value is not set.
	 *  
	 * @return the default value as a String
	 */
	public String getDefaultValue() {
	    lazyLoad();
	    return defaultValue;
	}

	// getters and setters to ensure lazy loading
	public String getName() { lazyLoad(); return name; }
	public String getNicename() { lazyLoad(); return nicename; }
	public String getDesc() { lazyLoad(); return desc; }
	public Boolean getRequired() { lazyLoad(); return required; }
	public Boolean getSequence() { lazyLoad(); return sequence; }
	public List<String> getMediaTypes() { lazyLoad(); return mediaTypes; }
	public Output getOutput() { lazyLoad(); return output; }
	public Kind getKind() { lazyLoad(); return this.kind; }
	public Boolean getOrdered() { lazyLoad(); return ordered; }
	public String getType() { lazyLoad(); return type; }
	public void setName(String name) { lazyLoad(); this.name = name; }
	public void setNicename(String nicename) { lazyLoad(); this.nicename = nicename; }
	public void setDesc(String desc) { lazyLoad(); this.desc = desc; }
	public void setRequired(Boolean required) { lazyLoad(); this.required = required; }
	public void setSequence(Boolean sequence) { lazyLoad(); this.sequence = sequence; }
	public void setMediaTypes(List<String> mediaTypes) { lazyLoad(); this.mediaTypes = mediaTypes; }
	public void setOutput(Output output) { lazyLoad(); this.output = output; }
	public void setKind(Kind kind) { lazyLoad(); this.kind = kind; }
	public void setOrdered(Boolean ordered) { lazyLoad(); this.ordered = ordered; }
	public void setType(String type) { lazyLoad(); this.type = type; }

	public Document toXml() {
		lazyLoad();

		Document argDoc = XML.getXml("<"+kind+" xmlns=\"http://www.daisy.org/ns/pipeline/data\"/>");
		Element argElem = argDoc.getDocumentElement();

		if (name != null) {
			argElem.setAttribute("name", name);
		}
		if (nicename != null) {
			argElem.setAttribute("nicename", nicename);
		}
		if (desc != null) {
			argElem.setAttribute("desc", desc);
		}
		if (required != null) {
			argElem.setAttribute("required", required+"");
		}
		if (sequence != null) {
			argElem.setAttribute("sequence", sequence+"");
		}
		if (mediaTypes != null) {
			String mediaTypesJoined = "";
			for (int i = 0; i < mediaTypes.size(); i++) {
				if (i > 0) {
					mediaTypesJoined += " ";
				}
				mediaTypesJoined += mediaTypes.get(i);
			}
			argElem.setAttribute("mediaType", mediaTypesJoined);
		}
		if (output != null) {
			argElem.setAttribute("outputType", output+"");
		}
		if (ordered != null) {
			argElem.setAttribute("ordered", ordered+"");
		}
		if (type != null) {
			argElem.setAttribute("type", type);
		}

		if (values == null) {
			// do nothing

		} else if (values.size() == 1 && values.get(0).length() > 0 && !sequence) {
			argElem.setTextContent(values.get(0));

		} else {
			for (String value : values) {
				Element item = argDoc.createElementNS(XPath.dp2ns.get("d"), "item");
				item.setAttribute("value", value);
				argElem.appendChild(item);
			}
		}

		return argDoc;
	}

}
