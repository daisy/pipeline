package org.daisy.braille.pef;


public class PEFSearchIndex extends SearchIndex<PEFBook> {
    private final static String REGEX = "[\\s\\.,:/-]";

	public void add(PEFBook p) {
		for (String key : p.getMetadataKeys()) {
			for (String val : p.getMetadata(key)) {
				if ("format".equals(key)) {
					continue;
				}
				for (String ind : val.toLowerCase().split(REGEX)) {
					if (ind!=null && ind.length()>0) {
						if ("identifier".equals(key)) {
							add(ind, p, true);
						} else {
							add(ind, p, false);
						}
					}
				}
			}
		}
	}
}
