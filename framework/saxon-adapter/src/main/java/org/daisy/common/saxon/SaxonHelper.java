package org.daisy.common.saxon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import net.sf.saxon.Configuration;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
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
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceExtent;
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
