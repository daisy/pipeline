package org.daisy.dotify.common.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides a character mapper which is activated when a specific character 
 * is encountered. The mapping continues until the sequence is interrupted
 * by a character that is not in the map.
 * @author Joel Håkansson
 */
public class ConditionalMapper {
	private final int trigger;
	private final Map<Integer, String> map;
	
	/**
	 * Creates a new mapper builder.
	 */
	public static class Builder {
		private int trigger = -1;
		private final Map<Integer, String> map;
		
		/**
		 * Creates a builder with no specified trigger character.
		 * Unless a trigger is specified using {@link #trigger(char)}
		 * or {@link #trigger(int)}, the mapper will always be active.
		 */
		public Builder() {
			this.map = new HashMap<>();
		}
		
		/**
		 * Sets a trigger that activates the character mapper. 
		 * @param trigger the trigger
		 * @return returns this builder
		 */
		public Builder trigger(char trigger) {
			return trigger((int)trigger);
		}

		/**
		 * Sets a trigger that activates the character mapper. 
		 * @param trigger the trigger
		 * @return returns this builder
		 */
		public Builder trigger(int trigger) {
			this.trigger = trigger;
			return this;
		}

		/**
		 * <p>Defines that characters found in the <code>replace</code> string are to be 
		 * replaced by the character at the corresponding position in the <code>with</code>
		 * string.</p>
		 * 
		 * <p>If there is a character in the <code>replace</code> string with no character 
		 * at a corresponding position in the <code>with</code> string (because 
		 * <code>replace</code> is longer than <code>with</code>), then that character 
		 * will be replaced by an empty string.</p> 
		 * 
		 * <p>If a character occurs more than once in the <code>replace</code> string, then the
		 * last occurrence determines the replacement character.</p>
		 * 
		 * <p>If the <code>with</code> string is longer than the second argument string, 
		 * then excess characters are ignored.</p>
		 * 
		 * <p>Calling <code>map("abc", "ABC")</code> is equal to calling:</p>
		 * <pre>
		 * put('a', "A");
		 * put('b', "B");
		 * put('c', "C");
		 * </pre>
		 * 
		 * <p>Note however that {@link #put(int, String)} and {@link #put(char, String)} also
		 * allow a replacement that contains more than one character, which {@link #map(String, String)}
		 * does not.</p>
		 * 
		 * @param replace a list of characters to replace
		 * @param with a list of replacement characters
		 * @return returns this builder
		 */
		public Builder map(String replace, String with) {
			Iterator<Integer> chars = replace.codePoints().iterator();
			int[] repl = with.codePoints().toArray();
			int i = 0;
			while (chars.hasNext()) {
				int c = chars.next();
				if (repl.length>i) {
					put(c, new String(Character.toChars(repl[i])));
				} else {
					put(c, "");
				}
				i++;
			}
			return this;
		}

		/**
		 * Defines a replacement string for the specified character. 
		 * @param key the character
		 * @param value the replacement
		 * @return returns this builder
		 */
		public Builder put(char key, String value) {
			return put((int)key, value);
		}

		/**
		 * Defines a replacement string for the specified Unicode code point. 
		 * @param key the code point
		 * @param value the replacement
		 * @return returns this builder
		 */
		public Builder put(int key, String value) {
			map.put(key, value);
			return this;
		}
		
		/**
		 * Adds a transparent mapping for the specified character. In other words,
		 * this character will not be replaced, but will not cause the mapper to
		 * deactivate either.
		 * @param value the character
		 * @return returns this builder
		 */
		public Builder putIgnorable(char value) {
			return putIgnorable((int)value);
		}

		/**
		 * Adds a transparent mapping for the specified Unicode code point.
		 * In other words, this code point will not be replaced, but will 
		 * not cause the mapper to deactivate either.
		 * @param value the character
		 * @return returns this builder
		 */		
		public Builder putIgnorable(int value) {
			map.put(value, new String(Character.toChars(value)));
			return this;
		}
		
		/**
		 * Builds the mapper with the current configuration.
		 * @return returns a new mapper instance
		 */
		public ConditionalMapper build() {
			return new ConditionalMapper(this);
		}
	}
	
	/**
	 * Creates a new builder with the specified trigger.
	 * @param trigger the trigger
	 * @return returns a new builder instance
	 */
	public static Builder withTrigger(char trigger) {
		return new Builder().trigger(trigger);
	}

	/**
	 * Creates a new builder with the specified trigger.
	 * @param trigger the trigger
	 * @return returns a new builder instance
	 */
	public static Builder withTrigger(int trigger) {
		return new Builder().trigger(trigger);
	}
	
	/**
	 * <p>Replaces in the <code>input</code> string occurrences of characters listed in 
	 * the <code>replace</code> string with the character at the corresponding 
	 * position in the <code>with</code> string.</p>
	 * 
	 * <p>This method is intentionally similar to the <code>translate</code> XPath
	 * function.</p>
	 * 
	 * <p>For example, translate( "bar", "abc", "ABC") returns the string BAr.</p>
	 * 
	 * <p>If there is a character in the <code>replace</code> string with no character 
	 * at a corresponding position in the <code>with</code> string (because 
	 * <code>replace</code> is longer than <code>with</code>), then occurrences of
	 * that character in the <code>input</code> are removed.</p> 
	 * 
	 * <p>For example, translate( "--aaa--", "abc-", "ABC") returns "AAA".</p>
	 * 
	 * <p>If a character occurs more than once in the <code>replace</code> string, then the
	 * last occurrence determines the replacement character. This is unlike the 
	 * translate XPath function, which does the opposite.</p>
	 * 
	 * <p>If the <code>with</code> string is longer than the second argument string, 
	 * then excess characters are ignored.</p>
	 * 
	 * @param input the input
	 * @param replace a list of characters to replace
	 * @param with a list of new characters
	 * @return returns the new string
	 */
	public static String translate(String input, String replace, String with) {
		return new Builder().map(replace, with).build().replace(input);
	}

	private ConditionalMapper(Builder builder) {
		this.trigger = builder.trigger;
		this.map = Collections.unmodifiableMap(new HashMap<>(builder.map));
	}
	
	/**
	 * Replaces characters in the input according to the rules of this mapper.
	 * @param input the input
	 * @return returns the modified string
	 */
	public String replace(String input) {
		StringBuilder ret = new StringBuilder();
		boolean active = false;
		Iterator<Integer> chars = input.codePoints().iterator();
		while (chars.hasNext()) {
			int current = chars.next();
			if (active || trigger<0) {
				String replacement = map.get(current);
				if (replacement==null) {
					active = false;
					ret.appendCodePoint(current);
				} else {
					ret.append(replacement);
				}
			} else {
				//leave as it is
				ret.appendCodePoint(current);
			}
			if (current==trigger) {
				active = true;
			}
		}
		return ret.toString();
	}

}
