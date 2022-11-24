package org.daisy.pipeline.braille.css.xpath;

import java.util.ArrayList;
import java.util.Collections;
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
	public static String toString(Optional<Style> style, Optional<Style> relativeTo) {
		if (style.isPresent())
			return style.get().toString(relativeTo.orElse(FullStyle.EMPTY));
		else if (relativeTo.isPresent())
			return FullStyle.EMPTY.toString(relativeTo.get());
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
	// Note that Style does not implement Iterable<Style>
	public static Iterator<Style> iterator(Optional<Style> style) {
		return style.isPresent()
			? style.get().iterator()
			: Collections.emptyIterator();
	}
	
	protected Iterator<Style> iterator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Add a rule to {@code style}.
	 */
	public static Optional<Style> add(Optional<Style> style, String key, Optional<Style> s) {
		if (style.isPresent())
			return style.get().add(key, s);
		else if (s.isPresent())
			return FullStyle.EMPTY.add(key, s);
		else
			return Optional.empty();
	}

	protected Optional<Style> add(String key, Optional<Style> style) {
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
			if (s instanceof FullStyle)
				head = s;
			else if (s instanceof DeclarationStyle)
				head = new FullStyle(BrailleCssStyle.of(((DeclarationStyle)s).declaration, Context.ELEMENT));
			else { // s instanceof ValueStyle
				if (!(((ValueStyle)s).value instanceof ContentList))
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
		if (head instanceof FullStyle) {
			List<Object> objects = new ArrayList<>(); // List<BrailleCssStyle|Declaration>
			for (Style s : list) {
				if (s instanceof FullStyle)
					objects.add(((FullStyle)s).style);
				else if (s instanceof DeclarationStyle)
					objects.add(((DeclarationStyle)s).declaration);
				else
					objects.add(((ValueStyle)s).value);
			}
			Iterator<Object> it = objects.iterator();
			return Optional.of(new FullStyle(((BrailleCssStyle)it.next()).add(it)));
		} else { // head instanceof ValueStyle
			List<Term<?>> content = new ArrayList<>();
			SupportedBrailleCSS css = null;
			for (Style s : list) {
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
			return Optional.of(new ValueStyle(ContentList.of(content, css)));
		}
	}
}
