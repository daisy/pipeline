package org.daisy.pipeline.script;

import java.net.URL;
import java.util.Map;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.script.impl.StaxXProcScriptParser;

/**
 * Implementation of {@link ScriptService} based on an XProc step with some extra information such
 * as port and option metadata.
 */
public class XProcScriptService implements ScriptService<XProcScript> {

	/** The Constant SCRIPT_URL. */
	public static final String SCRIPT_URL = "script.url";

	/** The Constant SCRIPT_ID. */
	public static final String SCRIPT_ID = "script.id";

	/** The Constant SCRIPT_ID. */
	public static final String SCRIPT_VERSION = "script.version";

	/** The url. */
	private URL url;

	/** The id. */
	private String id;

	/** The version. */
	private String version;

	/** The script. */
	private XProcScript script;

	private StaxXProcScriptParser parser;

	/**
	 * Instantiates a new {@link XProcScriptService}
	 */
	public XProcScriptService() {
	}

	/**
	 * Activate method called by ds.
	 *
	 * @param properties the properties
	 */
	public void activate(Map<?, ?> properties, Class<?> context) {
		if (properties.get(SCRIPT_ID) == null
				|| properties.get(SCRIPT_ID).toString().isEmpty()) {
			throw new IllegalArgumentException(SCRIPT_ID
					+ " property must not be empty");
		}
		if (properties.get(SCRIPT_URL) == null
				|| properties.get(SCRIPT_URL).toString().isEmpty()) {
			throw new IllegalArgumentException(SCRIPT_URL
					+ " property must not be empty");
		}
		if (properties.get(SCRIPT_VERSION) == null
				|| properties.get(SCRIPT_VERSION).toString().isEmpty()) {
			throw new IllegalArgumentException(SCRIPT_VERSION
					+ " property must not be empty");
		}
		String path = properties.get(SCRIPT_URL).toString();
		url = URLs.getResourceFromJAR(path, context);
		if (url == null)
			throw new IllegalArgumentException("Resource at location " + path + " could not be found");
		id = properties.get(SCRIPT_ID).toString();
                version= properties.get(SCRIPT_VERSION).toString();
	}

	/**
	 * Get the script URL.
	 */
	public URL getURL() {
		return url;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public XProcScript load() {
		if (parser == null)
			throw new IllegalStateException("Object was not properly initialized");
		if (script == null) {
			script = parser.parse(this);
		}
		return script;
	}

	/**
	 * {@link StaxXProcScriptParser} set by {@link ScriptRegistry}
	 */
	void setParser(StaxXProcScriptParser parser) {
		this.parser = parser;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Id: " + id);
		buf.append(", url: " + url.toString());
		buf.append(", version: " + version.toString());
		return buf.toString();
	}
}
