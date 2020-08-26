package org.daisy.pipeline.nlp.impl;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Prefix matching strategy based on a naive trie structure.
 * 
 * It is not memory-efficient since it uses java objects (i.e. Character) as map
 * keys, among other issues. A better way would be to perform binary searches on
 * native char[].
 */
public class PrefixMatchStringFinder implements IStringFinder {

	static class Trie {
		TreeMap<Character, Trie> children = new TreeMap<Character, Trie>();
		boolean endOfWord = false;
	}

	private Trie mTrieRoot;

	@Override
	public void compile(Collection<String> matchable) {
		mTrieRoot = new Trie();
		for (String s : matchable) {
			Trie current = mTrieRoot;
			for (int k = 0; k < s.length(); ++k) {
				Character c = s.charAt(k);
				Trie next = current.children.get(c);
				if (next == null) {
					next = new Trie();
					current.children.put(c, next);
				}
				current = next;
			}
			current.endOfWord = true;
		}
	}

	/**
	 * Match the beginning of @param input with the provided collection.
	 */
	@Override
	public String find(String input) {
		Trie current = mTrieRoot;
		int longestMatch = -1;
		for (int k = 0; k < input.length() && current != null; ++k) {
			if (current.endOfWord) {
				longestMatch = k;
			}
			current = current.children.get(input.charAt(k));
		}
		if (current != null && current.endOfWord) {
			return input;
		} else if (longestMatch > -1) {
			return input.substring(0, longestMatch);
		}

		return null;
	}

	@Override
	public boolean threadsafe() {
		return true;
	}
}
