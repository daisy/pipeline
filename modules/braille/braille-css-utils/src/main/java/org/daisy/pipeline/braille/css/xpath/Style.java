package org.daisy.pipeline.braille.css.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.csskit.TermStringImpl;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.impl.ContentList;
import org.daisy.pipeline.braille.css.xpath.impl.Declaration;
import org.daisy.pipeline.braille.css.xpath.impl.Stylesheet;
import org.daisy.pipeline.braille.css.xpath.impl.Value;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Interface for accessing braille CSS styles from XPath.
 */
public abstract class Style {

	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final BrailleCssParser PARSER = BrailleCssParser.getInstance();

	public static Optional<Style> parse(Optional<Object> style) {
		return parse(style.orElse(null), null, false);
	}

	public static Optional<Style> parse(Optional<Object> style, Optional<Style> parentStyle) {
		return parse(style.orElse(null), parentStyle.orElse(null), true);
	}

	private static Optional<Style> parse(Object style, Style parentStyle, boolean concretizeInherit) {
		String argStringValue;
		Attr attr = null;
		Element element = null;
		if (style == null)
			argStringValue = "";
		else if (style instanceof Attr) {
			attr = (Attr)style;
			argStringValue = attr.getNodeValue();
			element = (Element)attr.getParentNode();
		} else if (style instanceof Node) {
			argStringValue = ((Node)style).getTextContent();
		} else if (style instanceof String) {
			argStringValue = (String)style;
		} else
			throw new IllegalArgumentException("Unexpected type for first argument");
		if (parentStyle != null && !(parentStyle instanceof Stylesheet))
			throw new IllegalArgumentException("Unexpected type for second argument: " + parentStyle);
		Context styleCtxt = Context.ELEMENT;
		if (attr != null) {
			if (XMLNS_CSS.equals(attr.getNamespaceURI())) {
				String name = attr.getLocalName().replaceAll("^_", "-");
				if ("page".equals(name)) {
					styleCtxt = Context.PAGE;
				} else if ("volume".equals(name)) {
					styleCtxt = Context.VOLUME;
				} else if ("hyphenation-resource".equals(name)) {
					styleCtxt = Context.HYPHENATION_RESOURCE;
				} else if ("text-transform".equals(name)) {
					styleCtxt = Context.TEXT_TRANSFORM;
				} else if ("counter-style".equals(name)) {
					styleCtxt = Context.COUNTER_STYLE;
				} else if (PARSER.isSupportedCSSProperty(name)) {
					// assuming that context is a (pseudo-)element
					// assuming that the value is not "inherit"
					// not assuming that attr() and content() values have already been evaluated (although normally they will)
					Optional<cz.vutbr.web.css.Declaration> declaration
						= PARSER.parseDeclaration(name, argStringValue, element, false);
					if (declaration.isPresent())
						return Optional.of(new Declaration(declaration.get()));
					else
						return Optional.empty();
				}
			}
		}
		BrailleCssStyle s = concretizeInherit
			? PARSER.parseInlineStyle(argStringValue, styleCtxt, parentStyle != null ? ((Stylesheet)parentStyle).style : null)
			: PARSER.parseInlineStyle(argStringValue, styleCtxt);
		if (s.isEmpty())
			return Optional.empty();
		if (attr != null)
			if (styleCtxt == Context.ELEMENT)
				s = s.evaluate(element);
			else
				s = BrailleCssStyle.of("@" + attr.getLocalName(), s);
		return Optional.of(Stylesheet.of(s));
	}

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
			return ((Style)Stylesheet.EMPTY).toString(relativeTo.get());
		else
			return "";
	}
	
	protected abstract String toString(Style relativeTo);

	public static String toPrettyString(Optional<Style> style, String indentation) {
		if (style.isPresent())
			return style.get().toPrettyString(indentation);
		else
			return "";
	}

	protected abstract String toPrettyString(String indentation);

	public static void toXml(Optional<Style> style, XMLStreamWriter writer) throws XMLStreamException {
		if (style.isPresent())
			style.get().toXml(writer);
	}
	
	protected void toXml(XMLStreamWriter writer) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	public static void toAttributes(Optional<Style> style, XMLStreamWriter writer) throws XMLStreamException {
		if (style.isPresent())
			style.get().toAttributes(writer);
	}

	protected void toAttributes(XMLStreamWriter writer) throws XMLStreamException {
		toAttributes((Style)null, writer);
	}

	public static void toAttributes(Optional<Style> style, Optional<Style> relativeTo, XMLStreamWriter writer) throws XMLStreamException {
		if (style.isPresent())
			style.get().toAttributes(relativeTo.orElse(Stylesheet.EMPTY), writer);
		else if (relativeTo.isPresent())
			((Style)Stylesheet.EMPTY).toAttributes(relativeTo.get(), writer);
	}

	protected void toAttributes(Style relativeTo, XMLStreamWriter writer) throws XMLStreamException {
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
	 * For each provided key, remove declaration if key is a property name, or rule if key is a selector.
	 */
	public static Optional<Style> remove(Optional<Style> style, Iterator<String> keys) {
		return style.isPresent()
			? style.get().remove(keys)
			: Optional.empty();
	}

	protected Optional<Style> remove(Iterator<String> key) {
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
			return ((Style)Stylesheet.EMPTY).put(key, s);
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
	public static Optional<Style> merge(Iterator<Object> styles) {
		Style head = null;
		if (styles.hasNext()) {
			Object s = styles.next();
			if (s instanceof String)
				head = new Value(ContentList.of(PARSER,
				                                Context.ELEMENT,
				                                Collections.singletonList(stringTerm((String)s))));
			else if (!(s instanceof Style))
				throw new IllegalArgumentException();
			else if (s instanceof Stylesheet)
				head = (Stylesheet)s;
			else if (s instanceof Declaration)
				head = Stylesheet.of(BrailleCssStyle.of(((Declaration)s).declaration));
			else if (!(s instanceof Value))
				throw new IllegalStateException(); // coding error
			else if (!(((Value)s).value instanceof ContentList))
				throw new IllegalArgumentException();
			else
				head = (Value)s;
		}
		if (!styles.hasNext())
			return Optional.ofNullable(head);
		List<Style> list = new ArrayList<>();
		list.add(head);
		while (styles.hasNext()) {
			Object s = styles.next();
			if (s instanceof String)
				list.add(new Value(ContentList.of(PARSER,
				                                  Context.ELEMENT,
				                                  Collections.singletonList(stringTerm((String)s)))));
			else if (!(s instanceof Style))
				throw new IllegalArgumentException();
			else if (!(s instanceof Stylesheet ||
			           s instanceof Declaration ||
			           s instanceof Value))
				throw new IllegalStateException(); // coding error
			else
				list.add((Style)s);
		}
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
			return Optional.of(Stylesheet.of(((BrailleCssStyle)it.next()).add(it)));
		} else { // head instanceof Value
			List<Term<?>> content = new ArrayList<>();
			BrailleCssParser parser = null;
			Context context = null;
			for (Style s : list) {
				if (!(s instanceof Value))
					throw new IllegalArgumentException();
				Value v = (Value)s;
				if (!(v.value instanceof ContentList))
					throw new IllegalArgumentException();
				if (parser == null)
					parser = ((ContentList)v.value).getParser();
				else if (parser != ((ContentList)v.value).getParser())
					throw new IllegalArgumentException();
				if (context == null)
					context = ((ContentList)v.value).getContext();
				else if (context != ((ContentList)v.value).getContext())
					throw new IllegalArgumentException();
				content.addAll(v.value);
			}
			return Optional.of(new Value(ContentList.of(parser, context, content)));
		}
	}

	private static Term<?> stringTerm(String s) {
		return new TermStringImpl() {}.setValue(s);
	}
}
