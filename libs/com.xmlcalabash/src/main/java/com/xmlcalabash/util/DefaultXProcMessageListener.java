package com.xmlcalabash.util;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcException.XProcLocator;
import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import java.math.BigDecimal;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Dec 18, 2009
 * Time: 8:18:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultXProcMessageListener implements XProcMessageListener {
    private static Logger defaultLogger = LoggerFactory.getLogger(DefaultXProcMessageListener.class);
    private Logger log = defaultLogger;

    public void error(XProcRunnable step, XProcException error) {
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }

        log.error(message(step, error.getLocation()[0], error.getMessage(), error.getErrorCode()));
    }

    public void error(Throwable exception) {
        log.error(exceptionMessage(exception) + exception.getMessage());
        if (exception.getCause() != null) {
            log.error("    cause: " + exceptionMessage(exception.getCause()) + exception.getCause().getMessage());
        }
    }

    private String exceptionMessage(Throwable exception) {
        StructuredQName qCode = null;
        SourceLocator loc = null;
        String message = "";

        if (exception instanceof XPathException) {
            qCode = ((XPathException) exception).getErrorCodeQName();
        }

        if (exception instanceof TransformerException) {
            TransformerException tx = (TransformerException) exception;
            if (qCode == null && tx.getException() instanceof XPathException) {
                qCode = ((XPathException) tx.getException()).getErrorCodeQName();
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
                qCode = new StructuredQName(n.getPrefix(),n.getNamespaceURI(),n.getLocalName());
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

        return message;
    }

    public void warning(XProcRunnable step, XdmNode location, String message) {
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.warn(message(step, location, message));
    }

    public void warning(Throwable exception) {
        log.warn(exceptionMessage(exception) + exception.getMessage());
    }

    public void info(XProcRunnable step, XdmNode location, String message) {
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.info(message(step, location, message));
    }

    public void fine(XProcRunnable step, XdmNode location, String message) {
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.debug(message(step, location, message));
    }

    public void finer(XProcRunnable step, XdmNode location, String message) {
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.trace(message(step, location, message));
    }

    public void finest(XProcRunnable step, XdmNode location, String message) {
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.trace(message(step, location, message));
    }

    private String message(XProcRunnable step, XdmNode location, String message) {
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

    private String message(XProcRunnable step, SourceLocator location, String message, QName code) {
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

    public void openStep(XProcRunnable step, XdmNode location, String message, String level, BigDecimal portion) {}

    public void closeStep() {}

}
