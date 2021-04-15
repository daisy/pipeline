package org.daisy.pipeline.tts.cereproc.impl.util;

import org.daisy.pipeline.tts.cereproc.impl.CereProcEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A class providing simple regex search and replace.
 * A rule-file is parsed and regexes and their replacement
 * string are stored in an ordered collection.
 * This Class has been migrated from Pipeline 1.
 * @author Martin Blomberg
 * @author carl walinder
 *
 */
public class RegexReplace {
    private final static Logger logger = LoggerFactory.getLogger(CereProcEngine.class);

    private XPath xpathObj = XPathFactory.newInstance().newXPath();
    private Vector<PatternReplace> patterns = new Vector<PatternReplace>();
    private boolean DEBUG = false;

    /**
     * @param rulesXML
     */
    public RegexReplace(URL rulesXML) {
        initResources(rulesXML);
    }


    /**
     * Reads the config file containing regexes and replace strings.
     * @param rulesXML the config file.
     */
    private void initResources(URL rulesXML) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            String xpath = "//rule";

            Document config = db.parse(rulesXML.openStream());
            NodeList rules = this.selectNodes(config.getDocumentElement(), xpath);

            for (int i = 0; i < rules.getLength(); i++) {
                Element rule = (Element) rules.item(i);
                try {
                    Pattern p = Pattern.compile(rule.getAttribute("match"));
                    PatternReplace pr = new PatternReplace(p, rule.getAttribute("replace"));
                    patterns.add(pr);
                    DEBUG(pr);
                } catch(PatternSyntaxException pse) {
                    logger.error("There is a problem with the regular expression!");
                    logger.error("The pattern in question is: "+pse.getPattern());
                    logger.error("The description is: "+pse.getDescription());
                    logger.error("The message is: "+pse.getMessage());
                    logger.error("The index is: "+pse.getIndex());

                    throw new IllegalArgumentException(pse.getMessage());
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluates an XPath expression and returns the result as a {@link NodeList}.
     * @param node the {@link Node} to evaluate the expression on
     * @param xpath the XPath expression to evaluate
     * @return a {@link NodeList}, or <code>null</code> if there is an
     * error in the XPath expression.
     */
    private NodeList selectNodes(Node node, String xpath) {
        try {
            return (NodeList) this.xpathObj.evaluate(xpath, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return null;
        }
    }

    /**
     * Filters a string using the regexes.
     * @param text the text to filter.
     * @return the filtered text.
     */
    public String filter(String text) {
        for (Iterator<PatternReplace> it = patterns.iterator(); it.hasNext(); ) {
            PatternReplace pr = it.next();
            text = processTest(text, pr.getPattern(), pr.getReplace());
        }
        return text;
    }

    /**
     * Performes a regex-replace.
     * @param input the input text.
     * @param pattern the pattern to match.
     * @param replace the replacement string.
     * @return the filtered text.
     */
    private String processTest(String input, Pattern pattern, String replace) {
        try {
            Matcher matcher = pattern.matcher(input);
            return matcher.replaceAll(replace);
        } catch (Exception e) {
            logger.error("RegexReplace:");
            logger.error("     pattern: " + pattern.toString());
            logger.error("       input: " + input);
            logger.error("     replace: " + replace);

            throw new IllegalArgumentException(e.getMessage());
        }
    }


    private void DEBUG(PatternReplace pr) {
        DEBUG(pr.getPattern().toString() + "\t" + pr.getReplace());
    }


    private void DEBUG(String msg) {
        if (DEBUG) {
            logger.error("RegexReplace: " + msg);
        }
    }
}

/**
 * A class combining a pattern and a replacement string.
 * @author Martin Blomberg
 *
 */
class PatternReplace {
    public Pattern mPattern; 	// the regex pattern to match
    public String mReplace;		// the replacement string

    public PatternReplace(Pattern regex, String replace) {
        this.mPattern = regex;
        this.mReplace = replace;
    }

    /**
     * Returns the pattern.
     * @return the pattern.
     */
    public Pattern getPattern() {
        return mPattern;
    }

    /**
     * Returns the prefix.
     * @return the prefix.
     */
    public String getReplace() {
        return mReplace;
    }
}