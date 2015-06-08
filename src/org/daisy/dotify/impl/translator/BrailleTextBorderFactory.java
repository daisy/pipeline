package org.daisy.dotify.impl.translator;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.graphics.BrailleGraphics;
import org.daisy.dotify.impl.translator.BorderSpecification.Style;

class BrailleTextBorderFactory implements TextBorderFactory {
	
	private final static String KEY_BORDER = "border";
	private final static String KEY_TOP = "top";
	private final static String KEY_LEFT = "left";
	private final static String KEY_RIGHT = "right";
	private final static String KEY_BOTTOM = "bottom";
	
	private final BorderSpecification def;
	private final BorderSpecification top;
	private final BorderSpecification left;
	private final BorderSpecification right;
	private final BorderSpecification bottom;
	
	private boolean useBorder = false;
	
	private final Map<String, Object> features;

	public BrailleTextBorderFactory() {
		this.features = new HashMap<String, Object>();
		this.def = new BorderSpecification();
		this.top = new BorderSpecification(def);
		this.left = new BorderSpecification(def);
		this.right = new BorderSpecification(def);
		this.bottom = new BorderSpecification(def);
	}
	
	public void setFeature(String key, Object value) {
		if (key!=null && key.toLowerCase().startsWith(KEY_BORDER)) {
			useBorder = true;
			HashSet<String> set = new HashSet<String>();
			for (String s : key.toLowerCase().split("-")) {
				set.add(s);
			}
			set.remove(KEY_BORDER);
			BorderSpecification b = def;
			if (set.remove(KEY_TOP)) {
				b = top;
			} else if (set.remove(KEY_LEFT)) {
				b = left;
			} else if (set.remove(KEY_RIGHT)) {
				b = right;
			} else if (set.remove(KEY_BOTTOM)) {
				b = bottom;
			}
			if (set.size()==1) {
				String s = set.iterator().next();
				b.set(s, value.toString());
			} else {
				//unknown
				Logger.getLogger(this.getClass().getCanonicalName()).warning("Unknown feature: " + key);
				features.put(key, value);
			}
		} else {
			features.put(key, value);
		}
	}

	public Object getFeature(String key) {
		return features.get(key);
	}

	public TextBorderStyle newTextBorderStyle() throws TextBorderConfigurationException {
		String mode = "";
		try {
			mode = (String) getFeature(FEATURE_MODE);
		} catch (Exception e) {
		}

		if (!mode.equals(BrailleTranslatorFactory.MODE_BYPASS)) {
			
			if (useBorder) {
				TextBorderStyle.Builder style = new TextBorderStyle.Builder();
				if (top.getStyle()==Style.NONE&&bottom.getStyle()==Style.NONE&&left.getStyle()==Style.NONE&&right.getStyle()==Style.NONE) {
					return style.build();
				}

				BufferedImage borderImage = renderBorderImage(false);
				BrailleGraphics bg = new BrailleGraphics(false);
				List<String> str = bg.renderGraphics(borderImage.getData());
				boolean t = top.getStyle()!=Style.NONE;
				boolean b = bottom.getStyle()!=Style.NONE;
				boolean l = left.getStyle()!=Style.NONE;
				boolean r = right.getStyle()!=Style.NONE;

				if (t) {
					if (l) { style.topLeftCorner(""+str.get(0).charAt(0)); }
					style.topBorder(""+str.get(0).charAt(1));
					if (r) { style.topRightCorner(""+str.get(0).charAt(2)); }
				}
				if (l) { style.leftBorder(""+str.get(1).charAt(0)); }
				if (r) { style.rightBorder(""+str.get(1).charAt(2)); }
				if (b) {
					if (l) { style.bottomLeftCorner(""+str.get(2).charAt(0)); }
					style.bottomBorder(""+str.get(2).charAt(1));
					if (r) { style.bottomRightCorner(""+str.get(2).charAt(2)); }
				}
				
				return style.build();
			}
		}
		throw new BrailleTextBorderFactoryConfigurationException();
	}
	
	/**
	 * Creates a border image compatible with both 6 and 8-dot use.
	 * @return
	 */
	private BufferedImage renderBorderImage(boolean eightDot) throws TextBorderConfigurationException {
		//cell dimensions in pixels
		final int cw = 2;
		final int ch = (eightDot?4:3);

		//border widths
		final int wt = top.getWidth();
		final int wb = bottom.getWidth();
		final int wl = left.getWidth();
		final int wr = right.getWidth();

		if (wt>3) {
			throw new BrailleTextBorderFactoryConfigurationException("Width of top border out of supported range [1,"+ch+"]: " + wt);
		}
		if (wb>3) {
			throw new BrailleTextBorderFactoryConfigurationException("Width of bottom border out of supported range [1,"+ch+"]: " + wb);
		}
		if (wl>2) {
			throw new BrailleTextBorderFactoryConfigurationException("Width of left border out of supported range [1,"+cw+"]: " + wl);
		}
		if (wr>2) {
			throw new BrailleTextBorderFactoryConfigurationException("Width of right border out of supported range [1,"+cw+"]: " + wr);
		}

		//cells required for borders (not fully implemented, because multi-cell borders not supported by TextBorderStyle)
		final int cl = (int)Math.ceil(wl/(double)cw);
		final int cr = (int)Math.ceil(wr/(double)cw);
		final int ct = (int)Math.ceil(wt/(double)ch);
		final int cb = (int)Math.ceil(wb/(double)ch);
		
		//image dimensions
		final int w = cw * (cl + cr + 1);
		final int h = ch * (ct + cb + 1);

		//alignment
		final int at = (top.getAlign()==BorderSpecification.Align.INNER?ch:0);
		final int ab = (bottom.getAlign()==BorderSpecification.Align.INNER?ch:0);
		final int al = (left.getAlign()==BorderSpecification.Align.INNER?cw:0);
		final int ar = (right.getAlign()==BorderSpecification.Align.INNER?cw:0);
		
		//border coordinates
		final int x1 = 0 + Math.max(al-wl, 0);
		final int y1 = 0 + Math.max(at-wt, 0);
		final int x2 = w - (1 + Math.max(ar-wr, 0));
		final int y2 = h - (1 + Math.max(ab-wb, 0));
		
		BufferedImage borderImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

		Graphics2D g = (Graphics2D)borderImage.getGraphics();

		//stroke all widths manually, with multiple 1 pixel lines
		Stroke s = new BasicStroke(1);
		g.setStroke(s);
		
		//top
		if (top.getStyle()!=Style.NONE) {
			for (int i=0; i<wt; i++) {
				g.drawLine(x1, y1+i, x2, y1+i);
			}
		}
		//right
		if (right.getStyle()!=Style.NONE) {
			for (int i=0; i<wr; i++) {
				g.drawLine(x2-i, y1, x2-i, y2);
			}
		}
		//bottom
		if (bottom.getStyle()!=Style.NONE) {
			for (int i=0; i<wb; i++) {
				g.drawLine(x2, y2-i, x1, y2-i);
			}
		}
		//left
		if (left.getStyle()!=Style.NONE) {
			for (int i=0; i<wl; i++) {
				g.drawLine(x1+i, y2, x1+i, y1);
			}
		}
		
		return borderImage;
	}
	
	private class BrailleTextBorderFactoryConfigurationException extends TextBorderConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2874595503401168992L;

		public BrailleTextBorderFactoryConfigurationException() {
			super();
		}

		public BrailleTextBorderFactoryConfigurationException(String message) {
			super(message);
		}
		
	}

}
