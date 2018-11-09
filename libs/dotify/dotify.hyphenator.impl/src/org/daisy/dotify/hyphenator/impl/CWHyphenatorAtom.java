package org.daisy.dotify.hyphenator.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.common.text.SplitResult;
import org.daisy.dotify.common.text.StringSplitter;

class CWHyphenatorAtom {
	static final String DICTIONARY_KEY = "dictionary";
	static final String EXCEPTIONS_FILE_KEY = "exceptions-file";
	static final String DECOMPOUND_LIMIT_KEY = "decompound-limit";
	static final String MINIMUM_WORD_LENGTH_KEY = "minimum-word-length";
	static final String PATTERN_PROPERTIES_KEY = "pattern-properties";
	static final String EVALUATE_THRESHOLD_KEY = "evaluate-threshold";
	private static final int DEFAULT_DECOMPOUND_LIMIT = 6;
	private static final int DEFAULT_MINIMUM_WORD_LENGTH = 2;
	private static final double DEFAULT_EVALUATE_THRESHOLD = 0.5;
	
	private final Logger logger;
	private final HyphenationConfig base;
	private final Properties exceptions;
	private final String dictionaryPath;
	
	private CWDecompounder decompounder;
	private double threshold;
	
	private int decompLimit;
	private int minWord;
	
	
	
	CWHyphenatorAtom(String subPath, String locale) throws HyphenatorConfigurationException {
		logger = Logger.getLogger(this.getClass().getCanonicalName());

        Properties imp;
        if(subPath==null) {
			throw new LatexHyphenatorConfigurationException("Locale not supported: " + locale);
        } else {
    		logger.fine("Loading implementation: " + subPath);
    		imp = loadProperties(subPath);
        }
        
        Properties patternProps;
        if (imp.containsKey(PATTERN_PROPERTIES_KEY)) {
        	patternProps = loadProperties(imp.get(PATTERN_PROPERTIES_KEY).toString());
        } else {
        	throw new RuntimeException("Missing property: " + PATTERN_PROPERTIES_KEY);
        }
        
        base = new HyphenationConfig(patternProps);

        String exceptionsPath = imp.getProperty(EXCEPTIONS_FILE_KEY);
		if (exceptionsPath!=null && !exceptionsPath.equals("")) {
			exceptions = loadProperties(exceptionsPath);
		} else {
			exceptions = new Properties();
		}

		decompLimit = DEFAULT_DECOMPOUND_LIMIT;
		try {
			decompLimit = Integer.parseInt(imp.getProperty(DECOMPOUND_LIMIT_KEY));
		} catch (Exception e) {
			logger.log(Level.FINE, "Could not parse decompound limit", e);
		}

		minWord = DEFAULT_MINIMUM_WORD_LENGTH;
		try {
			minWord = Integer.parseInt(imp.getProperty(MINIMUM_WORD_LENGTH_KEY));
		} catch (Exception e) {
			logger.log(Level.FINE, "Could not parse minimum word length", e);
		}
		
		threshold = DEFAULT_EVALUATE_THRESHOLD;
		try {
			threshold = Double.parseDouble(imp.getProperty(EVALUATE_THRESHOLD_KEY));
		} catch (Exception e) {
			logger.log(Level.FINE, "Could not parse evaluate threshold, using default: " + threshold, e);
		}
		
		dictionaryPath = imp.getProperty(DICTIONARY_KEY);

	}
	
	private Properties loadProperties(String path) {
		Properties ret = new Properties();
		try {
	        URL propertiesURL = this.getClass().getResource(path);
	        if (propertiesURL!=null){
	        	ret.loadFromXML(propertiesURL.openStream());
	        } else {
	        	logger.warning("Cannot locate properties file: " + path);
	        }
		} catch (IOException e) {
			logger.warning("Failed to load properties file: " + path);
		}
		return ret;
	}
	
	int getDefaultBeginLimit() {
		return base.getDefaultBeginLimit();
	}

	int getDefaultEndLimit() {
		return base.getDefaultEndLimit();
	}

	String hyphenate(String input, int beginLimit, int endLimit, int accuracy) {
		if (accuracy<5) {
			return base.getHyphenator().hyphenate(input, beginLimit, endLimit);
		} else {
			if (decompounder==null) {
				//lazy load
				decompounder = new CWDecompounder(decompLimit);
				try {
					decompounder.loadDictionary(dictionaryPath, minWord);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to read dictionary: " + dictionaryPath, e);
				}
			}
			StringBuffer output = new StringBuffer();
			for (SplitResult sr : StringSplitter.split(input, "\\p{L}+")) {
				if (sr.isMatch()) {
					String word = sr.getText();
					if (exceptions.containsKey(word)) {
						output.append((String)exceptions.get(word));
					} else {
						output.append(decompounder.findCompounds(word, beginLimit, endLimit, threshold));
					}
				} else {
					output.append(sr.getText());
				}
			}
			return base.getHyphenator().hyphenate(output.toString(), beginLimit, endLimit);
		}
	}
}
