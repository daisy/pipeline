package org.daisy.common.xproc.calabash.impl;

import java.net.URI;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import org.daisy.pipeline.event.ProgressMessageBuilder;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRunnable;
import com.xmlcalabash.util.URIUtils;

// TODO: Auto-generated Javadoc
/**
 * Collection of message formating function from the calabash dependent objects to strings
 */
public class XprocMessageHelper {

	/**
	 * Formats the message from the objects passed as argument.
	 *
	 * @param step the step
	 * @param node the node
	 * @param message the message
	 * @param code the code
	 * @return the string
	 */
	public static String logLine(XProcRunnable step, XdmNode node,
			String message, QName code) {
		String prefix = "";
		if (node != null) {
			URI cwd = URIUtils.cwdAsURI();
			String systemId = cwd.relativize(node.getBaseURI()).toASCIIString();
			int line = node.getLineNumber();
			int col = node.getColumnNumber();

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
	 *
	 * @param step the step
	 * @param node the node
	 * @param message the message
	 * @return the string
	 */
	public static String logLine(XProcRunnable step, XdmNode node,
			String message) {
		return logLine(step, node, message, null);
	}

	/**
	 * Formats an error message from the given exception.
	 *
	 * @param exception the exception
	 * @return the string
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
			loc = err.getLocator()[0];
			if (err.getErrorCode() != null) {
				QName n = err.getErrorCode();
				qCode = new StructuredQName(n.getPrefix(), n.getNamespaceURI(),
						n.getLocalName());
			}
			if (err.getStep() != null) {
				message = message + err.getStep() + ":";
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
	 *
	 * @param step the step
	 * @param node the node
	 * @param message the message
	 * @param code the code
	 * @return the string
	 */
	public static ProgressMessageBuilder message(XProcRunnable step, XdmNode node,
			String message, QName code, ProgressMessageBuilder builder) {

		if (node != null) {
			builder.withFile(node.getBaseURI().toASCIIString());
			builder.withLine(node.getLineNumber());
			builder.withColumn(node.getColumnNumber());

		}
		builder.withText(message);
		return builder;
	}

	public static ProgressMessageBuilder message(XProcRunnable step, XdmNode node,
			String message, ProgressMessageBuilder builder) {
		return message(step, node, message, null,builder);
	}

	public static ProgressMessageBuilder errorMessage(Throwable exception, ProgressMessageBuilder builder) {
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
			loc = err.getLocator()[0];
			if (err.getErrorCode() != null) {
				QName n = err.getErrorCode();
				qCode = new StructuredQName(n.getPrefix(), n.getNamespaceURI(),
						n.getLocalName());
			}
			if (err.getStep() != null) {
				message = message + err.getStep() + ":";
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
