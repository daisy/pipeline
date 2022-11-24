package org.daisy.pipeline.braille.css.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.xpath.impl.DeclarationStyle;
import org.daisy.pipeline.braille.css.xpath.impl.FullStyle;
import org.daisy.pipeline.braille.css.xpath.impl.ValueStyle;

/**
 * Interface for accessing braille CSS styles from XPath.
 */
public abstract class Style {

	/**
	 * Serialize the style to a string according to the <a
	 * href="http://braillespecs.github.io/braille-css/#h2_style-attribute">syntax of the
	 * <code>style</code> attribute</a>.
	 */
	public String toString() {
		return toString(null);
	}

	/**
	 * @param relativeTo Include only those declarations that are needed to reconstruct the
	 *                   style with <code>relativeTo</code> as the parent style.
	 */
	public abstract String toString(Style relativeTo);

	public void toXml(XMLStreamWriter writer) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get property names of declarations and selectors of rules.
	 */
	public Iterable<String> keys() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get property name if this style is a single declaration, an absent value otherwise.
	 */
	public Optional<String> property() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get selector if this style is a single rule, an absent value otherwise
	 */
	public Optional<String> selector() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Whether the style is empty (has no declarations or nested rules).
	 */
	public boolean empty() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get property value if key is a property name, or style within rule if key is a selector.
	 */
	public Optional<Style> get(String key) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the property value or the initial value if the style does not contain the property.
	 */
	public Optional<Style> getOrDefault(String propertyName) {
		return get(propertyName);
	}

	/**
	 * Remove declaration if key is a property name, or rule if key is a selector.
	 */
	public Style remove(String key) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return sequence of declarations and rules within this style, or terms within this value.
	 */
	// Note that Style does not implement Iterable<Style>
	public Iterator<Style> iterator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Add a rule to this style.
	 */
	public Style add(String key, Style style) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Merge a sequence of styles. Properties are overwritten by properties declared in following
	 * style items.
	 */
	public static Style merge(Iterator<Style> styles) {
		Style head = null;
		while (styles.hasNext()) {
			Style s = styles.next();
			if (!s.empty()) {
				if (s instanceof FullStyle)
					head = s;
				else if (s instanceof DeclarationStyle)
					head = new FullStyle(BrailleCssStyle.of(((DeclarationStyle)s).declaration, Context.ELEMENT));
				else { // s instanceof ValueStyle
					if (!(((ValueStyle)s).value instanceof ContentList))
						throw new IllegalArgumentException();
					head = s;
				}
				break;
			}
		}
		if (!styles.hasNext())
			return head != null ? head : FullStyle.EMPTY;
		List<Style> nonEmpty = new ArrayList<>();
		nonEmpty.add(head);
		while (styles.hasNext()) {
			Style s = styles.next();
			if (!s.empty())
				nonEmpty.add(s);
		}
		if (nonEmpty.size() == 1)
			return nonEmpty.get(0);
		if (head instanceof FullStyle) {
			List<Object> objects = new ArrayList<>(); // List<BrailleCssStyle|Declaration>
			for (Style s : nonEmpty) {
				if (s instanceof FullStyle)
					objects.add(((FullStyle)s).style);
				else if (s instanceof DeclarationStyle)
					objects.add(((DeclarationStyle)s).declaration);
				else
					objects.add(((ValueStyle)s).value);
			}
			Iterator<Object> it = objects.iterator();
			return new FullStyle(((BrailleCssStyle)it.next()).add(it));
		} else { // head instanceof ValueStyle
			List<Term<?>> content = new ArrayList<>();
			SupportedBrailleCSS css = null;
			for (Style s : nonEmpty) {
				if (!(s instanceof ValueStyle))
					throw new IllegalArgumentException();
				ValueStyle v = (ValueStyle)s;
				if (!(v.value instanceof ContentList))
					throw new IllegalArgumentException();
				if (css == null)
					css = ((ContentList)v.value).getSupportedBrailleCSS();
				else if (!css.equals(((ContentList)v.value).getSupportedBrailleCSS()))
					throw new IllegalArgumentException();
				content.addAll(v.value);
			}
			return new ValueStyle(ContentList.of(content, css));
		}
	}
}
