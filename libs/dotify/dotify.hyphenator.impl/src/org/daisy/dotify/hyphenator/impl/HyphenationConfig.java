package org.daisy.dotify.hyphenator.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Logger;

import net.davidashen.text.Utf8TexParser.TexParserException;

class HyphenationConfig {
	public enum Mode {BYTE, CHARACTER};
	private static final String LEFT_HYPHEN_MIN_KEY = "beginLimit";
	private static final String RIGHT_HYPHEN_MIN_KEY = "endLimit";
	private static final String ENCODING_KEY = "encoding";
	private static final String PATTERN_PATH_KEY = "patternPath";
	private static final String MODE_KEY = "mode";
	private static final String BYTE_MODE = "byte";
	private static final String CHARACTER_MODE = "character";
	private final String encoding;
	private final int beginLimit;
	private final int endLimit;
	private final Mode mode;
	private final String patternPath;
	private final net.davidashen.text.Hyphenator hyphenator;
	
	HyphenationConfig(Properties props) {
		Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
		patternPath = props.getProperty(PATTERN_PATH_KEY);
		if (patternPath==null) {
			throw new RuntimeException("Required property named '" + PATTERN_PATH_KEY + "' missing.");
		}
		String modeStr = props.getProperty(MODE_KEY);
		if (modeStr==null) {
			throw new RuntimeException("Required property named '" + MODE_KEY + "' missing.");
		} else if (modeStr.equals(BYTE_MODE)) {
			mode = Mode.BYTE;
		} else if (modeStr.equals(CHARACTER_MODE)) {
			mode = Mode.CHARACTER;
		} else {
			throw new RuntimeException("Unrecognized mode. Allowed values are " + BYTE_MODE + " and " + CHARACTER_MODE);
		}
		encoding = props.getProperty(ENCODING_KEY);
		String leftHyphenMinStr = props.getProperty(LEFT_HYPHEN_MIN_KEY);
		String rightHyphenMinStr = props.getProperty(RIGHT_HYPHEN_MIN_KEY);
		if (leftHyphenMinStr!=null) {
			beginLimit = Integer.parseInt(leftHyphenMinStr);
		} else {
			beginLimit = 1;
		}
		if (rightHyphenMinStr!=null) {
			endLimit = Integer.parseInt(rightHyphenMinStr);
		} else {
			endLimit = 1;
		}

		hyphenator = new net.davidashen.text.Hyphenator();
		try {
			switch (mode) {
				case BYTE: {
					if (encoding!=null) {
						logger.warning("Configuration problem: Encoding has no effect in byte mode.");
					}
			        InputStream language = new LatexRulesLocator().getResource(patternPath).openStream();
					hyphenator.loadTable(language);
					break;
				}
				case CHARACTER: {
					if (encoding==null) {
						logger.warning("Configuration problem: Encoding should be set in character mode.");
					}
			        InputStream language = new LatexRulesLocator().getResource(patternPath).openStream();
			        InputStreamReader sr = new InputStreamReader(language, Charset.forName(encoding));
					hyphenator.loadTable(sr);
					break;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load resource: " + patternPath, e);
		} catch (TexParserException e) {
			throw new RuntimeException("Failed to load resource: " + patternPath, e);
		}
	}

	public Mode getMode() {
		return mode;
	}

	public String getEncoding() {
		return encoding;
	}

	public int getDefaultBeginLimit() {
		return beginLimit;
	}

	public int getDefaultEndLimit() {
		return endLimit;
	}

	public String getPatternPath() {
		return patternPath;
	}

	public net.davidashen.text.Hyphenator getHyphenator() {
		return hyphenator;
	}

	@Override
	public String toString() {
		return "HyphenationConfig [encoding=" + encoding + ", beginLimit=" + beginLimit + ", endLimit=" + endLimit + ", mode=" + mode + ", patternPath=" + patternPath + "]";
	}
	
}