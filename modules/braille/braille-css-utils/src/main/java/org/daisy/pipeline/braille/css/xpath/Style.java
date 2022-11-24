package org.daisy.pipeline.braille.css.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.Term;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.xpath.impl.Declaration;
import org.daisy.pipeline.braille.css.xpath.impl.Stylesheet;
import org.daisy.pipeline.braille.css.xpath.impl.Value;

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
	public static String toString(Optional<Style> style, Optional<Style> relativeTo) {
		if (style.isPresent())
			return style.get().toString(relativeTo.orElse(Stylesheet.EMPTY));
		else if (relativeTo.isPresent())
			return Stylesheet.EMPTY.toString(relativeTo.get());
		else
			return "";
	}
	
	protected abstract String toString(Style relativeTo);

	public static void toXml(Optional<Style> style, XMLStreamWriter writer) throws XMLStreamException {
		if (style.isPresent())
			style.get().toXml(writer);
	}
	
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get property names of declarations and selectors of rules.
	 */
	public static Iterable<String> keys(Optional<Style> style) {
		return style.isPresent()
			? style.get().keys()
			: Collections.emptyList();
	}

	protected Iterable<String> keys() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get property name if {@code style} is a single declaration, an absent value otherwise.
	 */
	public static Optional<String> property(Optional<Style> style) {
		return style.isPresent()
			? style.get().property()
			: Optional.empty();
	}
	
	protected Optional<String> property() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get selector if {@code style} is a single rule, an absent value otherwise
	 */
	public static Optional<String> selector(Optional<Style> style) {
		return style.isPresent()
			? style.get().selector()
			: Optional.empty();
	}

	protected Optional<String> selector() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get property value if key is a property name, or style within rule if key is a selector.
	 */
	public static Optional<Style> get(Optional<Style> style, String key) {
		return style.isPresent()
			? style.get().get(key)
			: Optional.empty();
	}
	
	protected Optional<Style> get(String key) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the property value or the initial value if the style does not contain the property.
	 */
	public static Optional<Style> getOrDefault(Optional<Style> style, String key) {
		return style.isPresent()
			? style.get().getOrDefault(key)
			: Optional.empty();
	}
	
	protected Optional<Style> getOrDefault(String propertyName) {
		return get(propertyName);
	}

	/**
	 * Remove declaration if key is a property name, or rule if key is a selector.
	 */
	public static Optional<Style> remove(Optional<Style> style, String key) {
		return style.isPresent()
			? style.get().remove(key)
			: Optional.empty();
	}

	protected Optional<Style> remove(String key) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return sequence of declarations and rules within {@code style}, or terms within this value.
	 */
	public static Iterator<Object> iterate(Optional<Style> style) {
		return style.isPresent()
			? style.get().iterate()
			: Collections.emptyIterator();
	}
	
	protected Iterator<Object> iterate() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a new rule or declaration
	 */
	public static Optional<Style> of(String key, Optional<Style> style) {
		return put(Optional.empty(), key, style);
	}
	
	/**
	 * Add a rule or declaration to {@code style}.
	 */
	public static Optional<Style> put(Optional<Style> style, String key, Optional<Style> s) {
		if (style.isPresent())
			return style.get().put(key, s);
		else if (s.isPresent())
			return Stylesheet.EMPTY.put(key, s);
		else
			return Optional.empty();
	}

	protected Optional<Style> put(String key, Optional<Style> style) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Merge a sequence of styles. Properties are overwritten by properties declared in following
	 * style items.
	 */
	public static Optional<Style> merge(Iterator<Style> styles) {
		Style head = null;
		if (styles.hasNext()) {
			Style s = styles.next();
			if (s instanceof Stylesheet)
				head = s;
			else if (s instanceof Declaration)
				head = new Stylesheet(BrailleCssStyle.of(((Declaration)s).declaration, Context.ELEMENT));
			else { // s instanceof Value
				if (!(((Value)s).value instanceof ContentList))
					throw new IllegalArgumentException();
				head = s;
			}
		}
		if (!styles.hasNext())
			return Optional.ofNullable(head);
		List<Style> list = new ArrayList<>();
		list.add(head);
		while (styles.hasNext())
			list.add(styles.next());
		if (list.size() == 1)
			return Optional.of(list.get(0));
		if (head instanceof Stylesheet) {
			List<Object> objects = new ArrayList<>(); // List<BrailleCssStyle|Declaration>
			for (Style s : list) {
				if (s instanceof Stylesheet)
					objects.add(((Stylesheet)s).style);
				else if (s instanceof Declaration)
					objects.add(((Declaration)s).declaration);
				else
					objects.add(((Value)s).value);
			}
			Iterator<Object> it = objects.iterator();
			return Optional.of(new Stylesheet(((BrailleCssStyle)it.next()).add(it)));
		} else { // head instanceof Value
			List<Term<?>> content = new ArrayList<>();
			SupportedBrailleCSS css = null;
			for (Style s : list) {
				if (!(s instanceof Value))
					throw new IllegalArgumentException();
				Value v = (Value)s;
				if (!(v.value instanceof ContentList))
					throw new IllegalArgumentException();
				if (css == null)
					css = ((ContentList)v.value).getSupportedBrailleCSS();
				else if (!css.equals(((ContentList)v.value).getSupportedBrailleCSS()))
					throw new IllegalArgumentException();
				content.addAll(v.value);
			}
			return Optional.of(new Value(ContentList.of(content, css)));
		}
	}
}
