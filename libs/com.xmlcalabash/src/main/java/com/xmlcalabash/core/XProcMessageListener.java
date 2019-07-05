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
    public void error(XProcRunnable step, XdmNode node, String message, QName code);
    public void error(Throwable exception);
    public void warning(XProcRunnable step, XdmNode node, String message);
    public void warning(Throwable exception);
    public void info(XProcRunnable step, XdmNode node, String message);
    public void fine(XProcRunnable step, XdmNode node, String message);
    public void finer(XProcRunnable step, XdmNode node, String message);
    public void finest(XProcRunnable step, XdmNode node, String message);

	/**
	 * Begin a new step, which is either<br>
     * - a p:group (except within a p:try),<br>
     * - a p:choose,<br>
     * - a p:when,<br>
     * - a p:otherwise,<br>
     * - a p:try,<br>
     * - a p:for-each,<br>
     * - a p:viewport,<br>
     * - a single iteration of a p:for-each or p:viewport,<br>
     * - or a pipeline or atomic step invocation.
     *
	 * @param step //FIXME doc update
	 * @param node //FIXME doc update
     * @param message  A message associated with the step (may be null)
     * @param level    The severity level of the message (may be null)
     * @param portion  The portion of this step within its parent step in terms of
     *                 computation time (number between 0 and 1, for p:when and
     *                 p:otherwise always equal to 1)
	 */
    public void openStep(XProcRunnable step, XdmNode node, String message, String level, BigDecimal portion);
    /**
     * End the current step
     */
    public void closeStep();
}
