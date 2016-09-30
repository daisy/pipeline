package org.daisy.dotify.common.text;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Provides a method for splitting a CharSequence using regex 
 * where both matching and non matching sub sequences are retained.
 * @author Joel Håkansson
 */
public class StringSplitter {
	private final Pattern pattern;
	
	/**
	 * Create a new StringSplitter using the supplied regex.
	 * @param regex the regular expression
	 */
	public StringSplitter(String regex) {
		pattern = Pattern.compile(regex);
	}

	/**
	 * Split the input string using the regular expression. Similar to the {@link String#split(String) split}
	 * method in the {@link String} class. However, contrary to {@link String#split(String) split},
	 * all subsequences are returned, even the ones that match. In other words,
	 * the input can be  reconstructed from the result.
	 * @param input the String to split
	 * @param pattern the compiled regular expression
	 * @return returns an array of SplitResults that, if put together, contain all the characters from the input.
	 */
	public static SplitResult[] split(CharSequence input, Pattern pattern) {
		ArrayList<SplitResult> ret = new ArrayList<>();
		Matcher m = pattern.matcher(input);

		int index = 0;
		while (m.find()) {
			if (m.start()>index) {
				ret.add(new SplitResult(input.subSequence(index, m.start()).toString(), false));
			}
			ret.add(new SplitResult(input.subSequence(m.start(), m.end()).toString(), true));
			index = m.end();
		}
		if (index==0) {
			return new SplitResult[] {new SplitResult(input.toString(), false)};
		}
		// add remaining segment
		if (index<input.length()) {
			ret.add(new SplitResult(input.subSequence(index, input.length()).toString(), false));
		}

		int resultSize = ret.size();
		SplitResult[] result = new SplitResult[resultSize];
		return ret.toArray(result);
	}
	
	/**
	 * Split the input string using the regular expression. Similar to the {@link String#split(String) split}
	 * method in the {@link String} class. However, contrary to {@link String#split(String) split},
	 * all subsequences are returned, even the ones that match. In other words,
	 * the input can be  reconstructed from the result.
	 * @param input the String to split
	 * @param regex the regular expression to use
	 * @return returns an array of SplitResults that, if put together, contain all the characters from the input.
	 */
	public static SplitResult[] split(CharSequence input, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return split(input, pattern);
	}
	
	/**
	 * Split the input string using the regular expression. Similar to the {@link String#split(String) split}
	 * method in the {@link String} class. However, contrary to {@link String#split(String) split},
	 * all subsequences are returned, even the ones that match. In other words,
	 * the input can be  reconstructed from the result.
	 * 
	 * @param input the String to split
	 * @return returns an array of SplitResults that, if put together, contain all the characters from the input.
	 */
	public SplitResult[] split(CharSequence input) {
		return split(input, pattern);
	}

}
