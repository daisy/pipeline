package org.daisy.dotify.hyphenator.impl;


class CWHyphenationUnit {
	private final int freq;
	private final int[] hyphPos;
	
	CWHyphenationUnit(int freq, String hyph) {
		this.freq = freq;
		int i = 0;
		if (hyph==null) {
			hyphPos = new int[0];
		} else {
			String[] sa = hyph.split("\\u00ad");
			hyphPos = new int[sa.length];
			for (int j=0; j<sa.length; j++) {
				i += sa[j].length();
				hyphPos[j] = i;
			}
		}
	}

	CWHyphenationUnit(int freq) {
		this(freq, null);
	}
	
	int getFreq() {
		return freq;
	}
	/*
	String getHyph() {
		return hyph;
	}*/
	/*
	int[] getHyphPos() {
		return hyphPos;
	}*/
	
	boolean hasHyphenation() {
		return hyphPos.length>0;
	}
	
	/**
	 * Hyphenates the supplied word, using the hyphenation points in this unit 
	 * @param word the word to hyphenate
	 * @return returns the hyphenated string
	 */
	String hyphenate(String word) {
		if (hasHyphenation()) {
			StringBuilder hyph = new StringBuilder();
			int i = 0;
			boolean first = true;
			for (int x : hyphPos) {
				if (!first) {
					hyph.append("\u00ad");
				} else {
					first = false;
				}
				if (x<=i) {
					throw new IllegalArgumentException("Next position must be higher");
				}
				hyph.append(word.substring(i, x));
				i = x;
			}
			return hyph.toString();
		} else {
			return word;
		}
	}

}
