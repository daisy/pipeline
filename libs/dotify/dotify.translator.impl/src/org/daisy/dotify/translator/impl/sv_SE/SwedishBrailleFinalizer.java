package org.daisy.dotify.translator.impl.sv_SE;

import org.daisy.dotify.translator.BrailleFinalizer;

class SwedishBrailleFinalizer implements BrailleFinalizer {
	@Override
	public String finalizeBraille(String input) {
		StringBuilder sb = new StringBuilder();
		for (char c : input.toCharArray()) {
			switch (c) {
				case ' ':
					sb.append('\u2800');
					break;
				case '\u00a0':
					sb.append('\u2800');
					break;
				case '-':
					sb.append('\u2824');
					break;
				case '\u00ad':
					sb.append('\u2824');
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
}
