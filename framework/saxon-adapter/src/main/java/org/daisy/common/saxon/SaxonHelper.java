package org.daisy.common.saxon;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public final class SaxonHelper {

	public static Sequence sequenceFromObject(Object object) {
		if (object == null)
			return EmptySequence.getInstance();
		else if (object instanceof Iterator || object instanceof Iterable)
			return sequenceFromIterator(
				object instanceof Iterable
					? ((Iterable<?>)object).iterator()
					: (Iterator<?>)object);
		else if (object.getClass().isArray())
			return arrayItemFromIterator(Arrays.asList((Object[])object).iterator());
		else
			return itemFromObject(object);
	}

	private static Item itemFromObject(Object object) {
		if (object == null)
			throw new IllegalArgumentException();
		else if (object instanceof String)
			return new StringValue((String)object);
		else if (object instanceof Integer)
			return IntegerValue.makeIntegerValue(BigInteger.valueOf((Integer)object));
		else if (object instanceof Long)
			return IntegerValue.makeIntegerValue(BigInteger.valueOf((Long)object));
		else if (object instanceof Float)
			return FloatValue.makeFloatValue((Float)object);
		else if (object instanceof Double)
			return DoubleValue.makeDoubleValue((Double)object);
		else if (object instanceof BigDecimal)
			return new BigDecimalValue((BigDecimal)object);
		else if (object instanceof Boolean)
			return BooleanValue.get((Boolean)object);
		else if (object instanceof URI)
			return new AnyURIValue(((URI)object).toASCIIString());
		else if (object instanceof Locale)
			return itemFromObject(((Locale)object).toLanguageTag());
		else if (object instanceof Date)
			try {
				return DateTimeValue.fromJavaDate((Date)object);
			} catch (XPathException e) {
				throw new RuntimeException(e); // should not happen
			}
		else if (object instanceof Map)
			return mapItemFromMap((Map<?,?>)object);
		else
			return new ObjectValue<>(object);
	}

	private static Sequence sequenceFromIterator(Iterator<?> iterator) {
		List<Item> list = new ArrayList<>();
		while (iterator.hasNext())
			list.add(itemFromObject(iterator.next()));
		return new SequenceExtent(list);
	}

	private static MapItem mapItemFromMap(Map<?,?> map) {
		MapItem mapItem = new HashTrieMap();
		for (Object key : map.keySet()) {
			if (!(key instanceof String))
				throw new IllegalArgumentException();
			mapItem = mapItem.addEntry(
				new StringValue((String)key),
				sequenceFromObject(map.get(key)));
		}
		return mapItem;
	}

	private static ArrayItem arrayItemFromIterator(Iterator<?> iterator) {
		try {
			return SimpleArrayItem.makeSimpleArrayItem(sequenceFromIterator(iterator).iterate());
		} catch (XPathException e) {
			throw new RuntimeException(e); // should not happen
		}
	}

	public static XdmValue xdmValueFromObject(Object object) {
		if (object == null)
			return XdmValue.wrap(EmptySequence.getInstance());
		else if (object instanceof String)
			return new XdmAtomicValue((String)object);
		else if (object instanceof Integer)
			return new XdmAtomicValue((Integer)object);
		else if (object instanceof Boolean)
			return new XdmAtomicValue((Boolean)object);
		else if (object instanceof URI)
			return new XdmAtomicValue((URI)object);
		else
			try {
				return XdmValue.wrap(sequenceFromObject(object));
			} catch (IllegalArgumentException e) {
				return XdmValue.wrap(new ObjectValue<>(object));
			}
	}

	public static SequenceType sequenceTypeFromType(Type type) throws IllegalArgumentException {
		if (type.equals(Void.TYPE))
			return SequenceType.EMPTY_SEQUENCE;
		else if (type.equals(String.class))
			return SequenceType.SINGLE_STRING;
		else if (type.equals(Integer.class)
		         || type.equals(int.class)
		         || type.equals(Long.class)
		         || type.equals(long.class))
			return SequenceType.SINGLE_INTEGER;
		else if (type.equals(Float.class)
		         || type.equals(float.class))
			return SequenceType.SINGLE_FLOAT;
		else if (type.equals(BigDecimal.class))
			return SequenceType.SINGLE_DECIMAL;
		else if (type.equals(Boolean.class)
		         || type.equals(boolean.class))
			return SequenceType.SINGLE_BOOLEAN;
		else if (type.equals(URI.class))
			return SequenceType.OPTIONAL_ANY_URI; // SINGLE_ANY_URI
		else if (type.equals(Element.class) || type.equals(Node.class) || type.equals(Attr.class))
			return SequenceType.SINGLE_NODE;
		else if (type.equals(Object.class))
			return SequenceType.SINGLE_ITEM;
		else if (type instanceof Class && ((Class<?>)type).isArray()) {
			Type itemType = ((Class<?>)type).getComponentType();
			sequenceTypeFromType(itemType);
			return ArrayItem.SINGLE_ARRAY_TYPE;
		} else if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType)type).getRawType();
			if (rawType.equals(Optional.class)) {
				Type itemType = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (itemType.equals(Node.class) || itemType.equals(Element.class) || itemType.equals(Attr.class))
					return SequenceType.OPTIONAL_NODE;
				else if (itemType.equals(String.class))
					return SequenceType.OPTIONAL_STRING;
				else if (itemType.equals(URI.class))
					return SequenceType.OPTIONAL_ANY_URI;
				else if (itemType.equals(Object.class))
					return SequenceType.OPTIONAL_ITEM;
				else if (itemType instanceof ParameterizedType) {
					rawType = ((ParameterizedType)itemType).getRawType();
					if (rawType.equals(Iterator.class) || rawType.equals(Iterable.class))
						return sequenceTypeFromType(itemType);
					else
						return SequenceType.OPTIONAL_ITEM;
				} else
					return SequenceType.OPTIONAL_ITEM; // optional special wrapper item
			} else if (rawType.equals(Iterator.class) || rawType.equals(Iterable.class)) {
				Type itemType = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (itemType.equals(Node.class))
					return SequenceType.NODE_SEQUENCE;
				else if (itemType.equals(String.class))
					return SequenceType.STRING_SEQUENCE;
				else
					return SequenceType.ANY_SEQUENCE; // sequence of special wrapper items
			} else if (rawType.equals(Map.class)) {
				Type keyType = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (keyType.equals(String.class)) {
					Type valueType = ((ParameterizedType)type).getActualTypeArguments()[1];
					sequenceTypeFromType(valueType);
					return HashTrieMap.SINGLE_MAP_TYPE;
				}
			}
		} else
			return SequenceType.SINGLE_ITEM; // special wrapper item
		throw new IllegalArgumentException("Unsupported type: " + type);
	}

	public static Iterable<?> iterableFromSequence(Sequence sequence, Type itemType) throws XPathException {
		if (itemType instanceof Class)
			return iterableFromSequence(sequence, (Class<?>)itemType);
		else {
			List<Object> list = new ArrayList<>();
			SequenceIterator iterator = sequence.iterate();
			Item next;
			while ((next = iterator.next()) != null)
				list.add(objectFromItem(next, itemType));
			return list;
		}
	}

	@SuppressWarnings("unchecked") // safe casts
	public static <T> Iterable<T> iterableFromSequence(Sequence sequence, Class<T> itemType) throws XPathException {
		if (itemType.equals(Node.class))
			return (Iterable<T>)ImmutableList.copyOf(iteratorFromNodeSequence(sequence));
		else {
			List<T> list = new ArrayList<>();
			SequenceIterator iterator = sequence.iterate();
			Item next;
			while ((next = iterator.next()) != null)
				list.add(objectFromItem(next, itemType));
			return list;
		}
	}

	@SuppressWarnings("unchecked") // safe casts
	public static Iterator<?> iteratorFromSequence(Sequence sequence, Type itemType) throws XPathException {
		if (itemType.equals(Node.class))
			return iteratorFromSequence(sequence, (Class<Node>)itemType);
		else
			return iterableFromSequence(sequence, itemType).iterator();
	}

	@SuppressWarnings("unchecked") // safe casts
	public static <T> Iterator<T> iteratorFromSequence(Sequence sequence, Class<T> itemType) throws XPathException {
		if (itemType.equals(Node.class))
			return (Iterator<T>)iteratorFromNodeSequence(sequence);
		else
			return iterableFromSequence(sequence, itemType).iterator();
	}

	@SuppressWarnings("unchecked") // safe casts
	private static Iterator<Node> iteratorFromNodeSequence(Sequence sequence) throws XPathException {
		List<XdmNode> list = new ArrayList<>();
		SequenceIterator iterator = sequence.iterate();
		Item next;
		while ((next = iterator.next()) != null)
			list.add(objectFromItem(next, XdmNode.class));
		return list.isEmpty()
			? ((Iterable<Node>)Collections.EMPTY_LIST).iterator()
			: new SaxonInputValue(list.iterator()).asNodeIterator();
	}

	@SuppressWarnings("unchecked") // safe casts
	public static <T> T[] arrayFromArrayItem(ArrayItem array, Class<T> itemType) throws XPathException {
		T[] a = (T[])Array.newInstance(itemType, array.arrayLength());
		int i = 0;
		for (Sequence s : array)
			a[i++] = objectFromItem(getSingleItem(s), itemType);
		return a;
	}

	public static Map<String,?> mapFromMapItem(MapItem item, Type itemType) throws XPathException {
		if (itemType instanceof Class)
			return mapFromMapItem(item, (Class<?>)itemType);
		else {
			Map<String,Object> map = new HashMap<>();
			for (KeyValuePair kv : item)
				map.put(kv.key.getStringValue(), objectFromItem(getSingleItem(kv.value), itemType));
			return map;
		}
	}

	public static <T> Map<String,T> mapFromMapItem(MapItem item, Class<T> itemType) throws XPathException {
		Map<String,T> map = new HashMap<>();
		for (KeyValuePair kv : item)
			map.put(kv.key.getStringValue(), objectFromItem(getSingleItem(kv.value), itemType));
		return map;
	}

	public static Object objectFromItem(Item item, Type type) throws XPathException {
		if (type instanceof Class)
			return objectFromItem(item, (Class<?>)type);
		else if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType)type).getRawType();
			if (rawType.equals(Map.class)) {
				if (item instanceof MapItem)
					return mapFromMapItem(
						(MapItem)item,
						((ParameterizedType)type).getActualTypeArguments()[1]);
			}
		}
		throw new IllegalArgumentException();
	}

	@SuppressWarnings("unchecked") // safe casts
	public static <T> T objectFromItem(Item item, Class<T> type) throws XPathException {
		if (type.isArray())
			if (item instanceof ArrayItem) {
				return (T)arrayFromArrayItem(
					(ArrayItem)item,
					((Class<?>)type).getComponentType());
			} else
				throw new IllegalArgumentException();
		else if (type.equals(XdmNode.class))
			if (item instanceof NodeInfo)
				return (T)new XdmNode((NodeInfo)item);
			else
				throw new IllegalArgumentException();
		else if (type.equals(Element.class))
			if (item instanceof NodeInfo)
				return (T)ElementOverNodeInfo.wrap((NodeInfo)item);
			else
				throw new IllegalArgumentException();
		else if (type.equals(Attr.class))
			if (item instanceof NodeInfo)
				return (T)AttrOverNodeInfo.wrap((NodeInfo)item);
			else
				throw new IllegalArgumentException();
		else if (type.equals(Node.class))
			if (item instanceof NodeInfo)
				return (T)NodeOverNodeInfo.wrap((NodeInfo)item);
			else
				throw new IllegalArgumentException();
		else if (type.equals(String.class))
			if (item instanceof StringValue)
				return (T)(String)((StringValue)item).getStringValue();
			else
				throw new IllegalArgumentException();
		else if (type.equals(Integer.class) || type.equals(int.class))
			if (item instanceof IntegerValue)
				return (T)(Integer)((IntegerValue)item).asBigInteger().intValue();
			else
				throw new IllegalArgumentException();
		else if (type.equals(Long.class) || type.equals(long.class))
			if (item instanceof IntegerValue)
				return (T)(Long)((IntegerValue)item).asBigInteger().longValue();
			else
				throw new IllegalArgumentException();
		else if (type.equals(Float.class) || type.equals(float.class))
			if (item instanceof FloatValue)
				return (T)(Float)((FloatValue)item).getFloatValue();
			else
				throw new IllegalArgumentException();
		else if (type.equals(BigDecimal.class))
			if (item instanceof DecimalValue)
				try {
					return (T)(BigDecimal)((DecimalValue)item).getDecimalValue();
				} catch (ValidationException e) {
					throw new RuntimeException(e); // should not happen
				}
			else
				throw new IllegalArgumentException();
		else if (type.equals(Boolean.class))
			if (item instanceof BooleanValue)
				return (T)(Boolean)((BooleanValue)item).getBooleanValue();
			else
				throw new IllegalArgumentException();
		else if (type.equals(URI.class))
			if (item instanceof AnyURIValue)
				try {
					return (T)(new URI((String)((StringValue)item).getStringValue()));
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e); // should not happen
				}
			else
				throw new IllegalArgumentException();
		else if (type.equals(Locale.class))
			if (item instanceof StringValue) {
				try {
					return (T)(new Locale.Builder()).setLanguageTag((String)((StringValue)item).getStringValue().replace('_','-'))
					                                .build();
				} catch (IllformedLocaleException e) {
					throw new IllegalArgumentException(e);
				}
			} else
				throw new IllegalArgumentException();
		else if (type.equals(Date.class))
			if (item instanceof DateTimeValue)
				return (T)Date.from(((DateTimeValue)item).getCalendar().toZonedDateTime().toInstant());
			else
				throw new IllegalArgumentException();
		else if (type.equals(Object.class))
			// argument can be anything
			if (item instanceof ArrayItem)
				return (T)objectFromItem(item, Object[].class);
			else if (item instanceof NodeInfo)
				return (T)objectFromItem(item, Node.class);
			else if (item instanceof StringValue)
				return (T)objectFromItem(item, String.class);
			else if (item instanceof IntegerValue)
				return (T)objectFromItem(item, Long.class);
			else if (item instanceof FloatValue)
				return (T)objectFromItem(item, Float.class);
			else if (item instanceof DecimalValue)
				return (T)objectFromItem(item, BigDecimal.class);
			else if (item instanceof BooleanValue)
				return (T)objectFromItem(item, Boolean.class);
			else if (item instanceof AnyURIValue)
				return (T)objectFromItem(item, URI.class);
		if (item instanceof ObjectValue) {
			Object o = ((ObjectValue<?>)item).getObject();
			if (type.isInstance(o))
				return (T)o;
			else
				throw new IllegalArgumentException("expected " + type + " object, but got " + o.getClass());
		} else
			throw new IllegalArgumentException("expected " + type + " object, but got " + item.getClass());
	}

	public static Item getSingleItem(Sequence sequence) throws XPathException {
		SequenceIterator iterator = sequence.iterate();
		Item item = iterator.next();
		if (item == null)
			throw new IllegalArgumentException();
		if (iterator.next() != null)
			throw new IllegalArgumentException();
		return item;
	}

	public static Optional<Item> getOptionalItem(Sequence sequence) throws XPathException {
		SequenceIterator iterator = sequence.iterate();
		Item item = iterator.next();
		if (iterator.next() != null)
			throw new IllegalArgumentException();
		return Optional.ofNullable(item);
	}

	public static javax.xml.namespace.QName jaxpQName(QName name) {
		String prefix = name.getPrefix();
		String ns = name.getNamespaceURI();
		String localPart = name.getLocalName();
		if (prefix != null)
			return new javax.xml.namespace.QName(ns, localPart, prefix);
		else
			return new javax.xml.namespace.QName(ns, localPart);
	}

	public static Iterable<XdmItem> axisIterable(XdmNode node, Axis axis) {
		return new Iterable<XdmItem>() {
			public Iterator<XdmItem> iterator() {
				return node.axisIterator(axis);
			}
		};
	}

	public static XPathExpression compileExpression(String expression, Hashtable<String,String> namespaceBindings, Configuration configuration)
			throws XPathException {
		XPathEvaluator xpathEvaluator = new XPathEvaluator(configuration);
		xpathEvaluator.getStaticContext().setNamespaceResolver(new MatchingNamespaceResolver(namespaceBindings));
		return xpathEvaluator.createPattern(expression);
	}

	public static boolean evaluateBoolean(XPathExpression expression, XdmNode contextNode) {
		try {
			XPathDynamicContext context = expression.createDynamicContext(contextNode.getUnderlyingNode());
			return expression.effectiveBooleanValue(context);
		} catch (XPathException e) {
			return false;
		}
	}

	// copied from com.xmlcalabash.util.ProcessMatch
	public static class MatchingNamespaceResolver implements NamespaceResolver {
		
		private Hashtable<String,String> ns = new Hashtable<String,String>();
		
		public MatchingNamespaceResolver(Hashtable<String,String> bindings) {
			ns = bindings;
		}
		
		public String getURIForPrefix(String prefix, boolean useDefault) {
			if ("".equals(prefix) && !useDefault) {
				return "";
			}
			return ns.get(prefix);
		}
		
		public Iterator<String> iteratePrefixes() {
			Vector<String> p = new Vector<String> ();
			for (String pfx : ns.keySet()) {
				p.add(pfx);
			}
			return p.iterator();
		}
	}

	private SaxonHelper() {
		// no instantiation
	}
}
