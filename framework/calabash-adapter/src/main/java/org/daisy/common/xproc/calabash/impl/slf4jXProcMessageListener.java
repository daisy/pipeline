package org.daisy.common.xproc.calabash.impl;

import java.math.BigDecimal;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;


/**
 * The listener interface for receiving slf4jXProcMessage events.
 * The class that is interested in processing a slf4jXProcMessage
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addslf4jXProcMessageListener<code> method. When
 * the slf4jXProcMessage event occurs, that object's appropriate
 * method is invoked.
 *
 * @see slf4jXProcMessageEvent
 */
public class slf4jXProcMessageListener implements XProcMessageListener {

    /** The default logger. */
    private static Logger defaultLogger = LoggerFactory.getLogger("com.xmlcalabash");

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#error(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String, net.sf.saxon.s9api.QName)
     */
    @Override
	public void error(XProcRunnable step, XProcException error) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.error(XprocMessageHelper.logLine(step, error.getLocation()[0], error.getMessage(), error.getErrorCode()));
    }

    private void error(XProcRunnable step, XdmNode location, String message, QName code) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.error(XprocMessageHelper.logLine(step, location, message, code));
    }

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#error(java.lang.Throwable)
     */
    @Override
	public void error(Throwable exception) {
        Logger log = defaultLogger;
        log.error(XprocMessageHelper.errorLogline(exception));
    }

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#warning(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
     */
    @Override
	public void warning(XProcRunnable step, XdmNode location, String message) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.warn(XprocMessageHelper.logLine(step, location, message));
    }

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#info(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
     */
    @Override
	public void info(XProcRunnable step, XdmNode location, String message) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.info(XprocMessageHelper.logLine(step, location, message));
    }

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#fine(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
     */
    @Override
	public void fine(XProcRunnable step, XdmNode location, String message) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.debug(XprocMessageHelper.logLine(step, location, message));
    }

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#finer(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
     */
    @Override
	public void finer(XProcRunnable step, XdmNode location, String message) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.debug(XprocMessageHelper.logLine(step, location, message));
    }

    /* (non-Javadoc)
     * @see com.xmlcalabash.core.XProcMessageListener#finest(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
     */
    @Override
	public void finest(XProcRunnable step, XdmNode location, String message) {
        Logger log;
        if (step != null) {
            log = LoggerFactory.getLogger(step.getClass());
        } else {
            log = defaultLogger;
        }
        log.debug(XprocMessageHelper.logLine(step, location, message));
    }

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#warning(java.lang.Throwable)
	 */
	@Override
	public void warning(Throwable exception) {
		 Logger log = defaultLogger;
		 log.warn(XprocMessageHelper.errorLogline(exception));

	}

	@Override
	public void openStep(XProcRunnable step, XdmNode location, String message, String level, BigDecimal portion) {
		if (message == null)
			return;
		if (level == null || level.equals("INFO")) {
			info(step, location, message);
		} else if (level.equals("ERROR")) {
			error(step, location, message, null);
		} else if (level.equals("WARN")) {
			warning(step, location, message);
		} else if (level.equals("DEBUG")) {
			fine(step, location, message);
		} else if (level.equals("TRACE")) {
			finest(step, location, message);
		} else {
			fine(step, location, "Message with invalid level '" + level + "': " + message);
		}
	}

	@Override
	public void closeStep() {}

}
