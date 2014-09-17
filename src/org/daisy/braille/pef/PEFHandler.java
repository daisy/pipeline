/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.pef;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import org.daisy.braille.embosser.EmbosserWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides a handler for reading a PEF-file and sending the contents to an Embosser.
 * Constructor is private on purpose, use a Builder to create a new PEFHandler.
 * @author  Joel Håkansson
 * @author Bert Frees
 * @version 3 sep 2008
 */
/*
 * NOTE: Always use upper case in enum values
 * TODO: check height/width
 */
public class PEFHandler extends DefaultHandler {
	private static final String PEF_NS="http://www.daisy.org/ns/2008/pef";
	private static enum AlignmentFallback {LEFT, CENTER_LEFT, CENTER_RIGHT, RIGHT, ABORT};
	/**
	 * Defines alignment values
	 */
	public static enum Alignment {
		/**
		 * Align left
		 */
		LEFT,
		/**
		 * Align right
		 */
		RIGHT,
		/**
		 * Align to the inner edge of the page
		 */
		INNER,
		/**
		 * Align to the outer edge of the page
		 */
		OUTER,
		/**
		 * Align center, round off towards the inner edge of the page
		 */
		CENTER_INNER,
		/**
		 * Align center, round off towards the outer edge of the page
		 */
		CENTER_OUTER,
		/**
		 * Abort processing if paper is wider than the contents of the file
		 */
		ABORT
		};
	
	private final EmbosserWriter embosser;
	private final Range range;
	private final AlignmentFallback alignFallback;
	private final boolean mirrorAlign;
	private final int offset;
//**** Added by Bert Frees *****************************************
	private final int topOffset;
//****************************************************************** 

	private Stack<Element> elements;
	private Element currentPage;
	private Element currentSection;
	private Element currentVolume;
	//private int inputPages;
	private int pageCount;
	private int alignmentPadding;
	private int versoAlignmentPadding;
	private boolean verso;
	private boolean isDuplex;
	private boolean widthError;
	private boolean newVolume;

	/**
	 * Provides a Builder for PEFHandler
	 * @author Joel Håkansson
	 */
	public static class Builder {
		//required params
		private EmbosserWriter embosser;
		//optional params
		private Range range = new Range(1);
		private AlignmentFallback alignFallback = AlignmentFallback.LEFT;
		private boolean mirrorAlign = false;
		private int offset = 0;
//**** Added by Bert Frees *****************************************
		private int topOffset = 0;
//****************************************************************** 

		/**
		 * Create a new PEFHandler builder
		 * @param embosser the embosser writer to use
		 */
		public Builder(EmbosserWriter embosser) {
			this.embosser = embosser;
		}
		
		//init optional params here
		/**
		 * Sets the range of pages to output
		 */
		public Builder range(Range value) {
			if (value!=null && !"".equals(value)) {
				range = value;
			}
			return this; 
		}
		/**
		 * Sets page alignment to use if the physical paper is bigger than the pages 
		 * @param value the value to use
		 * @return returns this object
		 */
		public Builder align(Alignment value) {
			switch (value) {
				case LEFT:
			        mirrorAlign = false;
			        alignFallback = AlignmentFallback.LEFT;
			        break;
				case RIGHT:
			        mirrorAlign = false;
			        alignFallback = AlignmentFallback.RIGHT;
			        break;
				case INNER:
			        mirrorAlign = true;
			        alignFallback = AlignmentFallback.LEFT;
					break;
				case OUTER:
			        mirrorAlign = true;
			        alignFallback = AlignmentFallback.RIGHT;
					break;
				case CENTER_INNER:
					mirrorAlign = true;
					alignFallback = AlignmentFallback.CENTER_LEFT;
					break;
				case CENTER_OUTER:
					mirrorAlign = true;
					alignFallback = AlignmentFallback.CENTER_RIGHT;
					break;
				case ABORT:
					alignFallback = AlignmentFallback.ABORT;
					break;
				default:
					throw new RuntimeException("Unexpected value: " + value);
				
			}
			return this;
		}
		/**
		 * Sets the page margin offset where positive numbers adjust towards
		 * the right side of the paper, and negative numbers adjust towards the
		 * left side.
		 * @param value the offset
		 * @return returns this object
		 */
		public Builder offset(int value) {
			offset = value;
			return this;
		}
//**** Added by Bert Frees *****************************************
		/**
		 * Sets the top offset.
		 */
		public Builder topOffset(int value) {
			topOffset = value;
			return this;
		}
//****************************************************************** 
		/**
		 * Builds a PEFHandler from this builder's current configuration.
		 * @return returns a new PEFHandler 
		 */
		public PEFHandler build() throws IOException {
			return new PEFHandler(this);
		}
	}
	
	private PEFHandler(Builder builder) throws IOException {
		this.range = builder.range;
		this.embosser = builder.embosser;
		this.alignFallback = builder.alignFallback;
		this.mirrorAlign = builder.mirrorAlign;
		this.offset = builder.offset;
//**** Added by Bert Frees *****************************************
		this.topOffset = builder.topOffset;
//****************************************************************** 
        this.elements = new Stack<Element>();
        this.currentPage = null;
        this.currentSection = null;
        this.currentVolume = null;
        //this.inputPages = 0;
        this.pageCount = 0;
        this.alignmentPadding = 0;
        this.versoAlignmentPadding = 0;
        this.widthError = false;
        this.newVolume = false;
	}

	
	// Pages, XPath 2:
	// sum(//section/(if (ancestor-or-self::*[@duplex][1]/@duplex=false()) then (count(descendant::page) * 2) else (count(descendant::page) + count(descendant::page) mod 2)))
	// Pages, XPath 1:
	// count(//section[ancestor-or-self::*[@duplex][1][@duplex='false']]/descendant::page)*2 + count(//section[ancestor-or-self::*[@duplex][1][@duplex='true']]/descendant::page) + count(//section[count(descendant::page) mod 2 = 1][ancestor-or-self::*[@duplex][1][@duplex='true']])
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		HashMap<String, String> atts = new HashMap<String, String>();
		if (PEF_NS.equals(uri)) {
			if (!elements.isEmpty()) {
				for (int i=0; i<attributes.getLength(); i++) {
					String attNS = attributes.getURI(i);
					addKey(atts, attNS, attributes.getLocalName(i), attributes.getValue(i));
				}
				inheritKey(atts, "", "rowgap");
				inheritKey(atts, "", "duplex");
				inheritKey(atts, "", "cols");
			}
			if (elements.isEmpty() && !"pef".equals(localName)) {
				throw new RuntimeException("Wrong root element.");
			} else if ("row".equals(localName)) {
				if (range.inRange(pageCount)) {
					try {
						if (currentPage==elements.peek()) { // same page 
							embosser.newLine();
						}
//**** Added by Bert Frees *****************************************
						else if (embosser.supportsAligning()) { // first row of new page
							addVerticalAlignPadding(topOffset);
						}
//******************************************************************
						if (mirrorAlign && verso && isDuplex) {
							addAlignPadding(versoAlignmentPadding);
						} else {
							addAlignPadding(alignmentPadding);
						}
					} catch (IOException e) {
						throw new SAXException(e);
					}
				}
				embosser.setRowGap(Integer.parseInt(getKey(atts, "", "rowgap")));
			} else if ("page".equals(localName)) {
				if (isDuplex) {
					verso = !verso;
				} else {
					verso = false;
					if (pageCount % 2 == 1) {
						pageCount++;
					}
				}
				pageCount++;
				//inputPages++;
				try {
					if (currentSection==elements.peek()) { // same section
						if (range.inRange(pageCount)) {
							embosser.newPage();
						}
					} else if (currentSection!=null) { // not the same section as the previous page
						if (range.inRange(pageCount)) {
							if (newVolume) {
								newVolume = false;
							} else {
								embosser.newSectionAndPage(isDuplex);
							}
						}
					} else { // nothing has been written yet
						if (range.inRange(pageCount)) {
							embosser.open(isDuplex);
							if (verso) {
								embosser.newPage(); 
							}
						}
					}
					//System.out.println("page:" + pageCount);
				} catch (IOException e) { throw new SAXException(e); }
			} else if ("section".equals(localName)) {
				int currentWidth = Integer.parseInt(getKey(atts, "", "cols"));
				isDuplex = "true".equals(getKey(atts, "", "duplex"));
				
				if (currentVolume==elements.peek()) { // same volume
					
				} else if (currentVolume!=null) { // not the same volume as the previous section
					if (range.inRange(pageCount)) {
						try {
							embosser.newVolumeSectionAndPage(isDuplex);
							newVolume = true;
						} catch (IOException e) {
							throw new SAXException("Could not create new volume", e);
						}
					}
				} else {
					
				}
				verso = true;
				if (pageCount % 2 == 1) {
					pageCount++;
				}
				if (!embosser.supportsAligning() || currentWidth==embosser.getMaxWidth()) {
					alignmentPadding = 0;
					versoAlignmentPadding = 0;
				} else {
					switch (alignFallback) {
						case LEFT:
							alignmentPadding = 0 + offset;
							break;
						case CENTER_LEFT:
							alignmentPadding = (embosser.getMaxWidth()-currentWidth) / 2  + offset;
							break;
						case CENTER_RIGHT:
							alignmentPadding = (int)Math.ceil((embosser.getMaxWidth()-currentWidth) / 2d) - offset;
							break;
						case RIGHT:
							alignmentPadding = embosser.getMaxWidth()-currentWidth - offset;
							break;
						case ABORT:
							throw new SAXException("Section width does not match paper width");
						default:
							throw new SAXException("Unexpected value: " + alignFallback);
					}
					versoAlignmentPadding = embosser.getMaxWidth() - currentWidth - alignmentPadding;
					if (alignmentPadding<0 || versoAlignmentPadding<0) {
						widthError = true;
						if (offset==0) {
							throw new SAXException("Cannot fit page on paper");
						} else {
							throw new SAXException("Cannot fit page on paper with offset " + offset);
						}
					}
					
				}
			}
		} 
		elements.push(new Element(uri, localName, atts));
	}
	public boolean hasWidthError() {
		return widthError;
	}
	private void addAlignPadding(int align) throws IOException {
		if (align < 1) return;
		char[] c = new char[align];
		for (int i=0; i<align; i++) {
			c[i] = '\u2800';
		}
		embosser.write(new String(c));
	}
//**** Added by Bert Frees *****************************************
	private void addVerticalAlignPadding(int align) throws IOException {
		for (int i=0;i<align;i++) {
			embosser.newLine();
		}
	}
//******************************************************************
	private String toKey(String uri, String localName) {
		return uri+">"+localName;
	}
	private void inheritKey(HashMap<String, String> to, String uri, String localName) {
		String key = toKey(uri, localName);
		if (!to.containsKey(key)) {
			Element e = elements.peek();
			if (e.getAttributes().containsKey(key)) {
				addKey(to, uri, localName, e.getAttributes().get(key));
			}
		}
	}
	
	private void addKey(HashMap<String, String> map, String uri, String localName, String value) {
		map.put(toKey(uri, localName), value);
	}
	
	private String getKey(HashMap<String, String> map, String uri, String localName) {
		return map.get(toKey(uri, localName));
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		elements.pop();
		if (PEF_NS.equals(uri)) {
			if ("section".equals(localName) && range.inRange(pageCount)) {
				currentVolume = elements.peek();
			} else if ("page".equals(localName) && range.inRange(pageCount)) {
				currentSection = elements.peek();
			} else if ("row".equals(localName) && range.inRange(pageCount)) {
				currentPage = elements.peek();
			}
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		Element context = elements.peek();
		if (PEF_NS.equals(context.getUri()) 
				&& "row".equals(context.getLocalName())
				&& range.inRange(pageCount)) {
			String text = new String(ch, start, length);
			try {
				embosser.write(text);
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}
	}

	public void endDocument() throws SAXException {
		try {
			embosser.newPage();
			verso = !verso;
			embosser.close();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}
}
