package org.daisy.dotify.impl.hyphenator.latex;


public class CWHyphenationUnit {
	private final int freq;
	private final int[] hyphPos;
	
	public CWHyphenationUnit(int freq, String hyph) {
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

	public CWHyphenationUnit(int freq) {
		this(freq, null);
	}
	
	public int getFreq() {
		return freq;
	}
	/*
	public String getHyph() {
		return hyph;
	}*/
	/*
	public int[] getHyphPos() {
		return hyphPos;
	}*/
	
	public boolean hasHyphenation() {
		return hyphPos.length>0;
	}
	
	/**
	 * Hyphenates the supplied word, using the hyphenation points in this unit 
	 * @param word
	 * @return
	 */
	public String hyphenate(String word) {
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
