package com.xmlcalabash.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.SourceLocator;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XStep;
import com.xmlcalabash.util.URIUtils;

import net.sf.saxon.s9api.*;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.AttributeValueTemplate;
import net.sf.saxon.style.ExpressionContext;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.type.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XProcMessageListenerHelper {

	private static final String NS_DAISY_PIPELINE_XPROC = "http://www.daisy.org/ns/pipeline/xproc";

	private static final QName px_message = new QName("px", NS_DAISY_PIPELINE_XPROC, "message");
	private static final QName cx_message = new QName("cx", XProcConstants.NS_CALABASH_EX, "message");
	private static final QName px_message_severity = new QName("px", NS_DAISY_PIPELINE_XPROC, "message-severity");
	private static final QName px_progress = new QName("px", NS_DAISY_PIPELINE_XPROC, "progress");

	private static final Logger logger = LoggerFactory.getLogger(XProcMessageListenerHelper.class);

	public static void openStep(XProcRuntime runtime, XStep step) {
		openStep(runtime, step, null, step.getInScopeOptions());
	}
	
	public static void openStep(XProcRuntime runtime, XStep step, BigDecimal fixedPortion, Hashtable<QName,RuntimeValue> globals) {
		String msg = evaluateExtensionAttribute(px_message, runtime, step, globals);
		if (msg == null) {
			// FIXME: also handled in DefaultStep
			msg = evaluateExtensionAttribute(cx_message, runtime, step, globals);
		}
		BigDecimal portion; {
			String s = evaluateExtensionAttribute(px_progress, runtime, step, globals);
			if (fixedPortion != null) {
				portion = fixedPortion;
				if (s != null) {
					logger.debug("px:progress attribute ignored: " + s);
				}
			} else if (s == null)
				portion = BigDecimal.ZERO;
			else
				try {
					portion = parseNumber(s);
				} catch (ArithmeticException e) {
					// probably division by zero, ignore
					logger.debug("Could not compute px:progress, ignoring: " + s, e);
					portion = BigDecimal.ZERO;
				} catch (NumberFormatException e) {
					throw new XProcException("Not a valid number: " + s);
				}
		}
		String severity;
		if (msg == null) {
			severity = null;
		} else {
			severity = step.getExtensionAttribute(px_message_severity);
		}
		XProcMessageListener listener = runtime.getMessageListener();
		listener.openStep(step, step.getNode(), msg, severity, portion);
	}
	
	public static String evaluateExtensionAttribute(QName attName, XProcRuntime runtime, XStep step, Hashtable<QName,RuntimeValue> globals) {
		String val = step.getExtensionAttribute(attName);
		if (val != null) {
			if (val.contains("{")) {
				// may be an attribute value template
				try {
					val = evaluateAVT(val, runtime, globals);
				} catch (Exception e) {
					throw new XProcException(
						new SourceLocator[]{XProcException.prettyLocator(null, "@"+attName.toString())},
						new RuntimeException("Could not evaluate " + attName + " attribute: " + val, e)
					).rebase(step);
				}
			}
		}
		return val;
	}

	public static String evaluateAVT(String avt, final XProcRuntime runtime, final Hashtable<QName,RuntimeValue> globals) throws XPathException {
		final XPathEvaluator evaluator = new XPathEvaluator(); {
			((IndependentContext)evaluator.getStaticContext()).setAllowUndeclaredVariables(true);
		}

		// AVT is a XSLT-only concept, we need to hack around that
		ExpressionContext dummyContext; {
			final Configuration config = runtime.getProcessor().getUnderlyingConfiguration();
			final StylesheetPackage pack = config.makeStylesheetPackage();
			pack.createFunctionLibrary();
			final StyleElement dummyXslt = new StyleElement() {
				{
					NodeInfo dummyNode; {
						LinkedTreeBuilder builder = new LinkedTreeBuilder(config.makePipelineConfiguration());
						builder.open();
						builder.startDocument(0);
						builder.endDocument();
						builder.close();
						dummyNode = builder.getCurrentRoot();
					}
					initialise(NameOfNode.makeName(dummyNode), dummyNode.getSchemaType(), null, dummyNode, 0);
				}
				protected void prepareAttributes() throws XPathException {}
			};
			dummyContext = new ExpressionContext(dummyXslt, null) {
				@Override
				public FunctionLibraryList getFunctionLibrary() {
					return pack.getFunctionLibrary();
				}
				@Override
				public StylesheetPackage getPackageData() {
					return pack;
				}
				@Override
				public DecimalFormatManager getDecimalFormatManager() {
					return null;
				}
				@Override
				public Expression bindVariable(StructuredQName name) throws XPathException {
					if (!globals.containsKey(new QName(name.toJaxpQName())))
						throw new XPathException("Variable is not defined: $" + name);
					return evaluator.getStaticContext().bindVariable(name);
				}
			};
		}

		// parse the AVT expression
		Expression expr = AttributeValueTemplate.make(avt, dummyContext);

		// now set the variables and evaluate
		XPathDynamicContext dynamicContext; {
			XPathExpression dummyXPath = evaluator.createExpression("()");
			dynamicContext = dummyXPath.createDynamicContext();
			Iterator<XPathVariable> variables = ((IndependentContext)evaluator.getStaticContext()).iterateExternalVariables();
			while (variables.hasNext()) {
				XPathVariable var = variables.next();
				QName name = new QName(var.getVariableQName().toJaxpQName());
				dynamicContext.setVariable(var, globals.get(name).getStringValue());
			}
		}
		expr.typeCheck(ExpressionVisitor.make(dummyContext),
		               new ContextItemStaticInfo(Type.ITEM_TYPE, true));
		return expr.evaluateAsString(dynamicContext.getXPathContextObject()).toString();
	}

	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
	private static final DecimalFormat decimalFormat; static {
		decimalFormat = new DecimalFormat("#.###", symbols);
		decimalFormat.setParseBigDecimal(true);
	}
	private static final DecimalFormat percentFormat; static {
		percentFormat = new DecimalFormat("#%", symbols);
		percentFormat.setParseBigDecimal(true);
	}
	private static final Pattern ratioPattern = Pattern.compile("^(?<numerator>[-+]?[0-9]+)/(?<denominator>[0-9]+)$");

	public static BigDecimal parseNumber(String input) throws NumberFormatException {
		ParsePosition pos = new ParsePosition(0);
		BigDecimal dec = (BigDecimal)decimalFormat.parse(input, pos);
		if (pos.getIndex() == input.length() && pos.getErrorIndex() == -1) {
			return dec;
		}
		pos = new ParsePosition(0);
		dec = (BigDecimal)percentFormat.parse(input, pos);
		if (pos.getIndex() == input.length() && pos.getErrorIndex() == -1) {
			return dec;
		}
		Matcher m = ratioPattern.matcher(input);
		if (m.matches()) {
			try {
				int numerator = Integer.parseInt(m.group("numerator"));
				int denominator = Integer.parseInt(m.group("denominator"));
				return new BigDecimal(numerator).divide(new BigDecimal(denominator), MathContext.DECIMAL128);
			} catch (NumberFormatException ee) {
			}
		}
		throw new NumberFormatException();
	}
}
