package org.daisy.pipeline.braille.css.xpath;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Interface for accessing braille CSS styles from XPath.
 */
public interface Style {

	/**
	 * Serialize the style to a string according to the <a
	 * href="http://braillespecs.github.io/braille-css/#h2_style-attribute">syntax of the
	 * <code>style</code> attribute</a>.
	 */
	public String toString();

	public void toXml(XMLStreamWriter writer) throws XMLStreamException;

}
