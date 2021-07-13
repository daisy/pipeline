package org.daisy.common.xproc.calabash.impl;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.messaging.MessageBuilder;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcException.XProcLocator;
import com.xmlcalabash.core.XProcRunnable;
import com.xmlcalabash.util.URIUtils;

/**
 * Collection of message formating function from the calabash dependent objects to strings
 */
public class XprocMessageHelper {

	/**
	 * Formats the message from the objects passed as argument.
	 */
	public static String logLine(XProcRunnable step, XdmNode location,
	                             String message, QName code) {
		String prefix = "";
		if (location != null) {
			String systemId = URIUtils.cwdAsURI().relativize(location.getBaseURI()).toASCIIString();
			int line = location.getLineNumber();
			int col = location.getColumnNumber();
			if (systemId != null && !"".equals(systemId)) {
				prefix = prefix + systemId + ":";
			}
			if (line != -1) {
				prefix = prefix + line + ":";
			}
			if (col != -1) {
				prefix = prefix + col + ":";
			}
		}
		return prefix + message;
	}

	public static String logLine(XProcRunnable step, SourceLocator location,
	                             String message, QName code) {
		String prefix = "";
		if (location != null) {
			String systemId = location.getSystemId();
			int line = location.getLineNumber();
			int col = location.getColumnNumber();
			if (systemId != null && !"".equals(systemId)) {
				prefix = prefix + systemId + ":";
			}
			if (line != -1) {
				prefix = prefix + line + ":";
			}
			if (col != -1) {
				prefix = prefix + col + ":";
			}
		}
		return prefix + message;
	}

	/**
	 * Formats the message from the objects passed as argument.
	 */
	public static String logLine(XProcRunnable step, XdmNode location, String message) {
		return logLine(step, location, message, null);
	}

	/**
	 * Formats an error message from the given exception. (Taken from DefaultXProcMessageListener.)
	 */
	public static String errorLogline(Throwable exception) {
		StructuredQName qCode = null;
		SourceLocator loc = null;
		String message = "";

		if (exception instanceof XPathException) {
			qCode = ((XPathException) exception).getErrorCodeQName();
		}

		if (exception instanceof TransformerException) {
			TransformerException tx = (TransformerException) exception;
			if (qCode == null && tx.getException() instanceof XPathException) {
				qCode = ((XPathException) tx.getException())
						.getErrorCodeQName();
			}

			if (tx.getLocator() != null) {
				loc = tx.getLocator();
				boolean done = false;
				while (!done && loc == null) {
					if (tx.getException() instanceof TransformerException) {
						tx = (TransformerException) tx.getException();
						loc = tx.getLocator();
					} else if (exception.getCause() instanceof TransformerException) {
						tx = (TransformerException) exception.getCause();
						loc = tx.getLocator();
					} else {
						done = true;
					}
				}
			}
		}

		if (exception instanceof XProcException) {
			XProcException err = (XProcException) exception;
			loc = err.getLocation()[0];
			if (err.getErrorCode() != null) {
				QName n = err.getErrorCode();
				qCode = new StructuredQName(n.getPrefix(), n.getNamespaceURI(),
						n.getLocalName());
			}
			if (loc instanceof XProcLocator) {
				if (((XProcLocator)loc).getStep() != null)
					message = message + ((XProcLocator)loc).getStep() + ":";
			}
		}

		if (loc != null) {
			if (loc.getSystemId() != null && !"".equals(loc.getSystemId())) {
				message = message + loc.getSystemId() + ":";
			}
			if (loc.getLineNumber() != -1) {
				message = message + loc.getLineNumber() + ":";
			}
			if (loc.getColumnNumber() != -1) {
				message = message + loc.getColumnNumber() + ":";
			}
		}

		if (qCode != null) {
			message = message + qCode.getDisplayName() + ":";
		}

		return message + exception.getMessage();
	}

	/**
	 * Formats the message from the objects passed as argument.
	 */
	public static MessageBuilder message(XProcRunnable step, XdmNode location,
	                                     String message, QName code, MessageBuilder builder) {

		if (location != null) {
			builder.withFile(location.getBaseURI().toASCIIString());
			builder.withLine(location.getLineNumber());
			builder.withColumn(location.getColumnNumber());

		}
		builder.withText(message);
		return builder;
	}

	public static MessageBuilder message(XProcRunnable step, XdmNode location,
	                                     String message, MessageBuilder builder) {
		return message(step, location, message, null, builder);
	}

	public static MessageBuilder message(XProcRunnable step, SourceLocator location,
	                                     String message, MessageBuilder builder) {
		if (location != null) {
			builder.withFile(location.getSystemId());
			builder.withLine(location.getLineNumber());
			builder.withColumn(location.getColumnNumber());
		}
		builder.withText(message);
		return builder;
	}

	public static MessageBuilder errorMessage(Throwable exception, MessageBuilder builder) {
		StructuredQName qCode = null;
		SourceLocator loc = null;
		String message = "";

		if (exception instanceof XPathException) {
			qCode = ((XPathException) exception).getErrorCodeQName();
		}

		if (exception instanceof TransformerException) {
			TransformerException tx = (TransformerException) exception;
			if (qCode == null && tx.getException() instanceof XPathException) {
				qCode = ((XPathException) tx.getException())
						.getErrorCodeQName();
			}

			if (tx.getLocator() != null) {
				loc = tx.getLocator();
				boolean done = false;
				while (!done && loc == null) {
					if (tx.getException() instanceof TransformerException) {
						tx = (TransformerException) tx.getException();
						loc = tx.getLocator();
					} else if (exception.getCause() instanceof TransformerException) {
						tx = (TransformerException) exception.getCause();
						loc = tx.getLocator();
					} else {
						done = true;
					}
				}
			}
		}

		if (exception instanceof XProcException) {
			XProcException err = (XProcException) exception;
			loc = err.getLocation()[0];
			if (err.getErrorCode() != null) {
				QName n = err.getErrorCode();
				qCode = new StructuredQName(n.getPrefix(), n.getNamespaceURI(),
						n.getLocalName());
			}
			if (loc instanceof XProcLocator) {
				if (((XProcLocator)loc).getStep() != null)
					message = message + ((XProcLocator)loc).getStep() + ":";
			}
		}

		if (loc != null) {
			if (loc.getSystemId() != null && !"".equals(loc.getSystemId())) {
				builder.withFile(loc.getSystemId());
			}


			builder.withText(message);
			if (loc.getLineNumber() != -1) {
				builder.withLine(loc.getLineNumber());

			}
			if (loc.getColumnNumber() != -1) {
				builder.withColumn(loc.getColumnNumber());
			}
		}

		if (qCode != null) {
			message = message + qCode.getDisplayName() + ":";
		}

		builder.withText(message + exception.getMessage());
		return builder;
	}
}
