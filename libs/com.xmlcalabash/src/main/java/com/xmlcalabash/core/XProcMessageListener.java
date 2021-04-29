package com.xmlcalabash.core;

import java.math.BigDecimal;

import com.xmlcalabash.runtime.XStep;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Dec 18, 2009
 * Time: 8:10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public interface XProcMessageListener {
    public void error(XProcRunnable step, XProcException error);
    public void error(Throwable exception);
    public void warning(XProcRunnable step, XdmNode location, String message);
    public void warning(Throwable exception);
    public void info(XProcRunnable step, XdmNode location, String message);
    public void fine(XProcRunnable step, XdmNode location, String message);
    public void finer(XProcRunnable step, XdmNode location, String message);
    public void finest(XProcRunnable step, XdmNode location, String message);

    /**
     * Begin a new step, which is either
     * - a p:group (except within a p:try),
     * - a p:choose,
     * - a p:when,
     * - a p:otherwise,
     * - a p:try,
     * - a p:for-each,
     * - a p:viewport,
     * - a single iteration of a p:for-each or p:viewport,
     * - or a pipeline or atomic step invocation.
     *
     * @param message  A message associated with the step (may be null)
     * @param level    The severity level of the message (may be null)
     * @param portion  The portion of this step within its parent step in terms of
     *                 computation time (number between 0 and 1, for p:when and
     *                 p:otherwise always equal to 1)
     */
    public void openStep(XProcRunnable step, XdmNode location, String message, String level, BigDecimal portion);
    /**
     * End the current step
     */
    public void closeStep();
}
