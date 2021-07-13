package org.daisy.common.xpath.saxon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

import org.daisy.common.saxon.SaxonInputValue;

import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentBuilderImpl;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.xpath.XPathFactoryImpl;

/**
 * Poor man's implementation of Saxon's <a
 * href="https://www.saxonica.com/documentation/index.html#!extensibility/functions">reflexive
 * extension function mechanism</a>, which is only available in the PE and EE versions.
 */
public abstract class ReflexiveExtensionFunctionProvider implements ExtensionFunctionProvider {

	private final List<ExtensionFunctionDefinition> definitions;

	public Collection<ExtensionFunctionDefinition> getDefinitions() {
		return definitions;
	}

	protected ReflexiveExtensionFunctionProvider(Class<?> definition) {
		definitions = new ArrayList<>();
		Collection<String> names = new ArrayList<>();
		for (Constructor<?> constructor : definition.getConstructors()) {
			if (Modifier.isPublic(constructor.getModifiers())) {
				if (names.contains("new"))
					throw new IllegalArgumentException("function overloading not supported");
				else {
					names.add("new");
					definitions.add(extensionFunctionDefinitionFromMethod(constructor));
				}
			}
		}
		for (Method method : definition.getDeclaredMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {
				if (names.contains(method.getName()))
					throw new IllegalArgumentException("function overloading not supported");
				else {
					names.add(method.getName());
					definitions.add(extensionFunctionDefinitionFromMethod(method));
				}
			}
		}
	}

	private ExtensionFunctionDefinition extensionFunctionDefinitionFromMethod(Executable method)
			throws IllegalArgumentException {
		assert method instanceof Constructor || method instanceof Method;
		if (method.isVarArgs())
			throw new IllegalArgumentException(); // vararg functions not supported
		else {
			Class<?> declaringClass = method.getDeclaringClass();
			boolean isStatic = method instanceof Constructor || Modifier.isStatic(method.getModifiers());
			boolean requiresXPath = false;
			boolean requiresDocumentBuilder = false;
			for (Class<?> t : method.getParameterTypes()) {
				if (t.equals(XPath.class)) {
					if (requiresXPath)
						throw new IllegalArgumentException(); // only one XPath argument allowed
					requiresXPath = true;
				} else if (t.equals(DocumentBuilder.class)) {
					if (requiresDocumentBuilder)
						throw new IllegalArgumentException(); // only one DocumentBuilder argument allowed
					requiresDocumentBuilder = true;
				}
			}
			int argCount = method.getParameterCount()
				+ (isStatic ? 0 : 1)
				- (requiresXPath ? 1 : 0)
				- (requiresDocumentBuilder ? 1 : 0);
			SequenceType[] argumentTypes = new SequenceType[argCount];
			int i = 0;
			if (!isStatic)
				argumentTypes[i++] = SequenceType.SINGLE_ITEM; // must be special wrapper item
			for (Type t : method.getGenericParameterTypes())
				if (!t.equals(XPath.class) && !t.equals(DocumentBuilder.class))
					argumentTypes[i++] = sequenceTypeFromType(t);
			SequenceType resultType = (method instanceof Constructor
			                           || ((Method)method).getReturnType().equals(declaringClass))
				? SequenceType.SINGLE_ITEM // special wrapper item
				: sequenceTypeFromType(((Method)method).getGenericReturnType());
			return new ExtensionFunctionDefinition() {
				@Override
				public SequenceType[] getArgumentTypes() {
					return argumentTypes;
				}
				@Override
				public StructuredQName getFunctionQName() {
					return new StructuredQName(declaringClass.getSimpleName(),
					                           declaringClass.getName(),
					                           method instanceof Constructor ? "new" : method.getName());
				}
				@Override
				public SequenceType getResultType(SequenceType[] arg0) {
					return resultType;
				}
				@Override
				public ExtensionFunctionCall makeCallExpression() {
					return new ExtensionFunctionCall() {
						@Override
						public Sequence call(XPathContext ctxt, Sequence[] args) throws XPathException {
							try {
								if (args.length != argCount)
									throw new IllegalArgumentException(); // should not happen
								int i = 0;
								Object instance = null;
								if (!isStatic) {
									Item item = getSingleItem(args[i++]);
									if (!(item instanceof ObjectValue))
										throw new IllegalArgumentException(
											"Expected ObjectValue<" + declaringClass.getSimpleName() + ">" + ", but got: " + item);
									instance = ((ObjectValue<?>)item).getObject();
								}
								Object[] javaArgs = new Object[method.getParameterCount()];
								int j = 0;
								for (Type type : method.getGenericParameterTypes()) {
									if (type.equals(XPath.class))
										javaArgs[j++] = new XPathFactoryImpl(ctxt.getConfiguration()).newXPath();
									else if (type.equals(DocumentBuilder.class)) {
										DocumentBuilderImpl b = new DocumentBuilderImpl();
										b.setConfiguration(ctxt.getConfiguration());
										javaArgs[j++] = b;
									} else if (type instanceof ParameterizedType
									           && ((ParameterizedType)type).getRawType().equals(Iterator.class))
										javaArgs[j++] = iteratorFromSequence(
											args[i++],
											((ParameterizedType)type).getActualTypeArguments()[0]);
									else
										javaArgs[j++] = objectFromItem(getSingleItem(args[i++]), type);
								}
								Object result; {
									try {
										if (method instanceof Constructor)
											result = ((Constructor<?>)method).newInstance(javaArgs);
										else
											result = ((Method)method).invoke(instance, javaArgs);
									} catch (InstantiationException|InvocationTargetException e) {
										throw new XPathException(e.getCause());
									} catch (IllegalAccessException e) {
										throw new RuntimeException(); // should not happen
									}
								}
								if (result == null)
									return EmptySequence.getInstance();
								else if (declaringClass.isInstance(result))
									return new ObjectValue<>(result);
								else if (result instanceof Iterator)
									return sequenceFromIterator((Iterator<?>)result);
								else
									return itemFromObject(result);
							} catch (RuntimeException e) {
								throw new XPathException("Unexpected error in " + getFunctionQName().getClarkName(), e);
							}
						}
					};
				}
			};
		}
	}

	private static SequenceType sequenceTypeFromType(Type type) throws IllegalArgumentException {
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
		else if (type.equals(Boolean.class)
		         || type.equals(boolean.class))
			return SequenceType.SINGLE_BOOLEAN;
		else if (type.equals(Node.class))
			return SequenceType.SINGLE_NODE;
		else if (type.equals(Object.class))
			return SequenceType.SINGLE_ITEM;
		else if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType)type).getRawType();
			if (rawType.equals(Iterator.class)) {
				Type itemType = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (itemType.equals(Node.class))
					return SequenceType.NODE_SEQUENCE;
				else if (itemType.equals(String.class))
					return SequenceType.STRING_SEQUENCE;
			} else if (rawType.equals(List.class)) {
				Type itemType = ((ParameterizedType)type).getActualTypeArguments()[0];
				sequenceTypeFromType(itemType);
				return SequenceType.SINGLE_ITEM; // SINGLE_ARRAY
			} else if (rawType.equals(Map.class)) {
				Type keyType = ((ParameterizedType)type).getActualTypeArguments()[0];
				Type valueType = ((ParameterizedType)type).getActualTypeArguments()[1];
				if (keyType.equals(String.class)) {
					sequenceTypeFromType(valueType);
					return SequenceType.SINGLE_ITEM; // SINGLE_MAP
				}
			}
		}
		throw new IllegalArgumentException("Unsupported type: " + type);
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
		else if (object instanceof Boolean)
			return BooleanValue.get((Boolean)object);
		else
			throw new IllegalArgumentException();
	}

	private static Sequence sequenceFromIterator(Iterator<?> iterator) {
		List<Item> list = new ArrayList<>();
		while (iterator.hasNext())
			list.add(itemFromObject(iterator.next()));
		return new SequenceExtent(list);
	}

	private static Iterator<?> iteratorFromSequence(Sequence sequence, Type itemType) throws XPathException {
		if (itemType instanceof Class)
			return iteratorFromSequence(sequence, (Class<?>)itemType);
		else
			throw new IllegalArgumentException();
	}

	@SuppressWarnings("unchecked") // safe casts
	private static <T> Iterator<T> iteratorFromSequence(Sequence sequence, Class<T> itemType) throws XPathException {
		if (itemType.equals(Node.class)) {
			List<XdmNode> list = new ArrayList<>();
			Configuration config = null;
			SequenceIterator iterator = sequence.iterate();
			Item next;
			while ((next = iterator.next()) != null) {
				list.add(objectFromItem(next, XdmNode.class));
				if (config == null)
					config = ((NodeInfo)next).getConfiguration();
			}
			return list.isEmpty()
				? (Iterator<T>)Collections.EMPTY_LIST.iterator()
				: (Iterator<T>)new SaxonInputValue(list.iterator(), config).asNodeIterator();
		} else {
			List<T> list = new ArrayList<>();
			SequenceIterator iterator = sequence.iterate();
			Item next;
			while ((next = iterator.next()) != null)
				list.add(objectFromItem(next, itemType));
			return list.iterator();
		}
	}

	private static List<Object> listFromArrayItem(ArrayItem array, Type itemType) throws XPathException {
		List<Object> list = new ArrayList<>();
		for (Sequence s : array)
			list.add(objectFromItem(getSingleItem(s), itemType));
		return list;
	}

	private static Map<String,Object> mapFromMapItem(MapItem item, Type itemType) throws XPathException {
		Map<String,Object> map = new HashMap<>();
		for (KeyValuePair kv : item)
			map.put(kv.key.getStringValue(), objectFromItem(getSingleItem(kv.value), itemType));
		return map;
	}

	private static Object objectFromItem(Item item, Type type) throws XPathException {
		if (type instanceof Class)
			return objectFromItem(item, (Class<?>)type);
		else if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType)type).getRawType();
			if (rawType.equals(List.class)) {
				if (item instanceof ArrayItem)
					return listFromArrayItem(
						(ArrayItem)item,
						((ParameterizedType)type).getActualTypeArguments()[0]);
			} else if (rawType.equals(Map.class)) {
				if (item instanceof MapItem)
					return mapFromMapItem(
						(MapItem)item,
						((ParameterizedType)type).getActualTypeArguments()[1]);
			}
		}
		throw new IllegalArgumentException();
	}

	@SuppressWarnings("unchecked") // safe casts
	private static <T> T objectFromItem(Item item, Class<T> type) {
		if (type.equals(XdmNode.class))
			if (item instanceof NodeInfo)
				return (T)new XdmNode((NodeInfo)item);
			else
				throw new IllegalArgumentException();
		else if (type.equals(Node.class))
			if (item instanceof NodeInfo)
				// we can be sure node iterator will be single item
				return (T)new SaxonInputValue((NodeInfo)item).asNodeIterator().next();
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
		else if (type.equals(Object.class))
			if (item instanceof IntegerValue)
				return (T)(Object)((IntegerValue)item).asBigInteger().longValue();
			else if (item instanceof StringValue)
				return (T)(Object)((StringValue)item).getStringValue();
			else
				throw new IllegalArgumentException();
		else
			throw new IllegalArgumentException();
	}

	private static Item getSingleItem(Sequence sequence) throws XPathException {
		SequenceIterator iterator = sequence.iterate();
		Item item = iterator.next();
		if (item == null)
			throw new IllegalArgumentException();
		if (iterator.next() != null)
			throw new IllegalArgumentException();
		return item;
	}
}
