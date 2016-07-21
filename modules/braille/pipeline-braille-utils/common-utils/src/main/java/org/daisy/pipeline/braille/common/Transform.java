package org.daisy.pipeline.braille.common;

import java.net.URI;
import java.util.Map;
import javax.xml.namespace.QName;

public interface Transform {
	
	public String getIdentifier();
	
	public XProc asXProc() throws UnsupportedOperationException;
	
	/* ----- */
	/* XProc */
	/* ----- */
	
	public static class XProc {
		
		private final URI uri;
		private final QName name;
		private final Map<String,String> options;
		
		public XProc(URI uri, QName name, Map<String,String> options) {
			this.uri = uri;
			this.name = name;
			this.options = options;
		}
		
		public URI getURI() {
			return uri;
		}
		
		public QName getName() {
			return name;
		}
		
		public Map<String,String> getOptions() {
			return options;
		}
	}
}
