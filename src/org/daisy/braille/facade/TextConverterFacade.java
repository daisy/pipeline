package org.daisy.braille.facade;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.daisy.braille.pef.TextHandler;

public class TextConverterFacade {
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the title of the publication
	 */
	public final static String KEY_TITLE = "title";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the author of the publication
	 */
	public final static String KEY_AUTHOR = "author";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the identifier for the publication 
	 */
	public final static String KEY_IDENTIFIER = "identifier";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should match the table to use
	 */
	public final static String KEY_MODE = "mode";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the language of the publication
	 */
	public final static String KEY_LANGUAGE = "language";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should be "true" for duplex or "false" for simplex
	 */
	public final static String KEY_DUPLEX = "duplex";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should be a string containing a valid date on the form yyyy-MM-dd
	 */
	public final static String KEY_DATE = "date";

	/**
	 * Parses a text file and outputs a PEF-file based on the contents of the file
	 * @param input input text file
	 * @param output output PEF-file
	 * @param settings settings
	 * @throws IOException if IO fails
	 */
	public void parseTextFile(File input, File output, Map<String, String> settings) throws IOException {
		TextHandler.Builder builder = new TextHandler.Builder(input, output);
		for (String key : settings.keySet()) {
			String value = settings.get(key);
			if (KEY_TITLE.equals(key)) {
				builder.title(value);
			} else if (KEY_AUTHOR.equals(key)) {
				builder.author(value);
			} else if (KEY_IDENTIFIER.equals(key)) {
				builder.identifier(value);
			} else if (KEY_MODE.equals(key)) {
				builder.converterId(value);
			} else if (KEY_LANGUAGE.equals(key)) {
				builder.language(value);
			} else if (KEY_DUPLEX.equals(key)) {
				builder.duplex("true".equals(value.toLowerCase()));
			}else if (KEY_DATE.equals(key)) {
				try {
					builder.date(DATE_FORMAT.parse(value));
				} catch (ParseException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				throw new IllegalArgumentException("Unknown option \"" + key + "\"");
			}
		}
		TextHandler tp = builder.build();
		tp.parse();
	}	
}
