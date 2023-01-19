package org.daisy.common.xpath.saxon;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;

import com.google.common.collect.ImmutableList;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.saxon.SaxonOutputValue;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.dom.DocumentBuilderImpl;
import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DecimalValue;
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
		Map<String,List<Executable>> methods = new HashMap<>();
		for (Constructor<?> constructor : definition.getConstructors()) {
			if (Modifier.isPublic(constructor.getModifiers())) {
				List<Executable> list = methods.get("new");
				if (list == null) {
					list = new ArrayList<>();
					methods.put("new", list);
				}
				list.add(constructor);
				methods.put("new", list);
			}
		}
		for (Method method : definition.getDeclaredMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {
				List<Executable> list = methods.get(method.getName());
				if (list == null) {
					list = new ArrayList<>();
					methods.put(method.getName(), list);
				}
				list.add(method);
				methods.put(method.getName(), list);
			}
		}
		definitions = new ArrayList<>();
		for (List<Executable> m : methods.values()) {
			Collections.sort(m, (a, b) -> new Integer(a.getParameterCount()).compareTo(b.getParameterCount()));
			definitions.add(extensionFunctionDefinitionFromMethods(m));
		}
	}

	/**
	 * @param methods Collection of constructors of the same class, or methods of the same class and
	 *                with the same name. Assumed to be sorted by number of parameters (from least
	 *                to most number of parameters).
	 */
	private ExtensionFunctionDefinition extensionFunctionDefinitionFromMethods(Collection<Executable> methods)
			throws IllegalArgumentException {
		ExtensionFunctionDefinition ret = null;
		for (Executable m : methods) {
			ExtensionFunctionDefinition def = extensionFunctionDefinitionFromMethod(m);
			if (ret == null)
				ret = def;
			else {
				ExtensionFunctionDefinition defA = ret;
				ExtensionFunctionDefinition defB = def;
				StructuredQName funcName = defA.getFunctionQName();
				if (!defB.getFunctionQName().equals(funcName))
					throw new IllegalArgumentException(); // should not happen
				SequenceType[] argTypes = defB.getArgumentTypes();
				if (defB.getMinimumNumberOfArguments() <= defA.getMaximumNumberOfArguments()
				    || !Arrays.equals(Arrays.copyOfRange(argTypes, 0, defA.getArgumentTypes().length), defA.getArgumentTypes())) {
					if (m instanceof Constructor)
						throw new IllegalArgumentException("Incompatible constructors");
					else // m instanceof Method
						throw new IllegalArgumentException("Incompatible '" + m.getName() + "' methods");
				}
				int minArgs = defA.getMinimumNumberOfArguments();
				int maxArgs = defB.getMaximumNumberOfArguments();
				ret = new ExtensionFunctionDefinition() {
						@Override
						public StructuredQName getFunctionQName() {
							return funcName;
						}
						@Override
						public int getMinimumNumberOfArguments() {
							return minArgs;
						}
						@Override
						public int getMaximumNumberOfArguments() {
							return maxArgs;
						}
						@Override
						public SequenceType[] getArgumentTypes() {
							return argTypes;
						}
						@Override
						public SequenceType getResultType(SequenceType[] suppliedArgTypes) {
							if (suppliedArgTypes.length >= defB.getMinimumNumberOfArguments())
								return defB.getResultType(suppliedArgTypes);
							else if (suppliedArgTypes.length <= defA.getMaximumNumberOfArguments())
								return defA.getResultType(suppliedArgTypes);
							else
								throw new IllegalArgumentException(
									"Function " + funcName + " can not be called with " + suppliedArgTypes.length + " arguments");
						}
						@Override
						public ExtensionFunctionCall makeCallExpression() {
							return new ExtensionFunctionCall() {
								ExtensionFunctionCall callA = null;
								ExtensionFunctionCall callB = null;
								@Override
								public Sequence call(XPathContext ctxt, Sequence[] args) throws XPathException {
									if (args.length <= defA.getMaximumNumberOfArguments()) {
										if (callA == null)
											callA = defA.makeCallExpression();
										return callA.call(ctxt, args);
									} else if (args.length >= defB.getMinimumNumberOfArguments()) {
										if (callB == null)
											callB = defB.makeCallExpression();
										return callB.call(ctxt, args);
									} else
										throw new IllegalArgumentException(
											"Function " + funcName + " can not be called with " + args.length + " arguments");
								}
							};
						}
					};
			}
		}
		if (ret == null)
			throw new IllegalArgumentException();
		return ret;
	}

	private ExtensionFunctionDefinition extensionFunctionDefinitionFromMethod(Executable method)
			throws IllegalArgumentException {
		assert method instanceof Constructor || method instanceof Method;
		if (method.isVarArgs())
			throw new IllegalArgumentException(); // vararg functions not supported
		else {
			Class<?> declaringClass = method.getDeclaringClass();
			boolean isInnerClass = Arrays.stream(getClass().getClasses()).anyMatch(declaringClass::equals)
				// not static nested
				&& !Modifier.isStatic(declaringClass.getModifiers());
			boolean isConstructor = method instanceof Constructor;
			boolean isStatic = isConstructor || Modifier.isStatic(method.getModifiers());
			boolean requiresXMLStreamWriter = false;
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
				} else if (t.equals(XMLStreamWriter.class)) {
					if (requiresXMLStreamWriter)
						throw new IllegalArgumentException(); // only one XMLStreamWriter argument allowed
					requiresXMLStreamWriter = true;
				}
			}
			Type[] javaArgumentTypes; { // arguments of Java method/constructor
				Type[] types = method.getGenericParameterTypes();
				javaArgumentTypes = isConstructor && isInnerClass
					// in case of constructor of inner class, first element is type of enclosing instance
					? Arrays.copyOfRange(types, 1, types.length)
					: types;
			}
			SequenceType[] argumentTypes = new SequenceType[ // arguments of XPath function
				javaArgumentTypes.length
				+ (isStatic ? 0 : 1)
				- (requiresXMLStreamWriter ? 1 : 0)
				- (requiresXPath ? 1 : 0)
				- (requiresDocumentBuilder ? 1 : 0)
			];
			int i = 0;
			if (!isStatic)
				argumentTypes[i++] = SequenceType.SINGLE_ITEM; // must be special wrapper item
			for (Type t : javaArgumentTypes)
				if (!t.equals(XMLStreamWriter.class) &&
				    !t.equals(XPath.class) &&
				    !t.equals(DocumentBuilder.class))
					argumentTypes[i++] = sequenceTypeFromType(t);
			SequenceType resultType; {
				if (isConstructor) {
					if (requiresXMLStreamWriter)
						throw new IllegalArgumentException(); // no XMLStreamWriter argument allowed in case of constructor
					resultType = SequenceType.SINGLE_ITEM; // special wrapper item
				} else {
					Type t = ((Method)method).getGenericReturnType();
					if (requiresXMLStreamWriter) {
						if (!t.equals(Void.TYPE))
							throw new IllegalArgumentException(); // XMLStreamWriter argument only allowed when return type is void
						resultType = SequenceType.NODE_SEQUENCE;
					} else
						resultType = sequenceTypeFromType(t);
				}
			}
			return new ExtensionFunctionDefinition() {
				@Override
				public SequenceType[] getArgumentTypes() {
					return argumentTypes;
				}
				@Override
				public StructuredQName getFunctionQName() {
					return new StructuredQName(declaringClass.getSimpleName(),
					                           declaringClass.getName(),
					                           isConstructor ? "new" : method.getName());
				}
				@Override
				public SequenceType getResultType(SequenceType[] suppliedArgTypes) {
					return resultType;
				}
				@Override
				public ExtensionFunctionCall makeCallExpression() {
					return new ExtensionFunctionCall() {
						@Override
						public Sequence call(XPathContext ctxt, Sequence[] args) throws XPathException {
							try {
								if (args.length != argumentTypes.length)
									throw new IllegalArgumentException(); // should not happen
								int i = 0;
								Object instance = null;
								if (!isStatic) {
									Item item = getSingleItem(args[i++]);
									try {
										instance = objectFromItem(item, declaringClass);
									} catch (IllegalArgumentException e) {
										throw new IllegalArgumentException(
											"Expected ObjectValue<" + declaringClass.getSimpleName() + ">" + ", but got: " + item, e);
									}
								}
								List<NodeInfo> nodeListFromXMLStreamWriter = null;
								Object[] javaArgs = new Object[ // arguments passed to Method.invoke() in addition to instance,
								                                // or to Constructor.newInstance()
									method.getParameterCount()
								];
								int j = 0;
								if (isConstructor && isInnerClass)
									// in case of constructor of inner class, first argument is enclosing instance
									javaArgs[j++] = ReflexiveExtensionFunctionProvider.this;
								for (Type type : javaArgumentTypes) {
									if (type.equals(XMLStreamWriter.class)) {
										List<NodeInfo> list = new ArrayList<>();
										nodeListFromXMLStreamWriter = list;
										XMLStreamWriter xmlStreamWriterArg = new SaxonOutputValue(
											item -> {
												if (item instanceof XdmNode)
													list.add(((XdmNode)item).getUnderlyingNode());
												else
													throw new RuntimeException(); // should not happen
											},
											ctxt.getConfiguration()
										).asXMLStreamWriter();
										javaArgs[j++] = xmlStreamWriterArg;
									} else if (type.equals(XPath.class))
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
									else if (type instanceof ParameterizedType
									           && ((ParameterizedType)type).getRawType().equals(Iterable.class))
										javaArgs[j++] = iterableFromSequence(
											args[i++],
											((ParameterizedType)type).getActualTypeArguments()[0]);
									else if (type instanceof ParameterizedType
									           && ((ParameterizedType)type).getRawType().equals(Optional.class)) {
										Optional<Item> item = getOptionalItem(args[i++]);
										javaArgs[j++] = item.isPresent()
											? Optional.of(objectFromItem(item.get(), ((ParameterizedType)type).getActualTypeArguments()[0]))
											: Optional.empty();
									} else if (type.equals(URI.class)) {
										// could be empty because sequenceTypeFromType() returned OPTIONAL_ANY_URI
										Optional<Item> item = getOptionalItem(args[i++]);
										if (!item.isPresent())
											throw new IllegalArgumentException("Expected xs:anyURI but got an empty sequence");
										javaArgs[j++] = objectFromItem(item.get(), type);
									} else
										javaArgs[j++] = objectFromItem(getSingleItem(args[i++]), type);
								}
								Object result; {
									try {
										if (isConstructor)
											result = ((Constructor<?>)method).newInstance(javaArgs);
										else // instance is null in case of static method
											result = ((Method)method).invoke(instance, javaArgs);
									} catch (InstantiationException|InvocationTargetException e) {
										throw new XPathException(e.getCause());
									} catch (IllegalAccessException e) {
										throw new RuntimeException(); // should not happen
									}
								}
								if (nodeListFromXMLStreamWriter != null)
									return new SequenceExtent(nodeListFromXMLStreamWriter);
								if (result instanceof Optional)
									return SaxonHelper.sequenceFromObject(((Optional<?>)result).orElse(null));
								else
									return SaxonHelper.sequenceFromObject(result);
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

	private static Iterable<?> iterableFromSequence(Sequence sequence, Type itemType) throws XPathException {
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
	private static <T> Iterable<T> iterableFromSequence(Sequence sequence, Class<T> itemType) throws XPathException {
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
	private static Iterator<?> iteratorFromSequence(Sequence sequence, Type itemType) throws XPathException {
		if (itemType.equals(Node.class))
			return iteratorFromSequence(sequence, (Class<Node>)itemType);
		else
			return iterableFromSequence(sequence, itemType).iterator();
	}

	@SuppressWarnings("unchecked") // safe casts
	private static <T> Iterator<T> iteratorFromSequence(Sequence sequence, Class<T> itemType) throws XPathException {
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
	private static <T> T[] arrayFromArrayItem(ArrayItem array, Class<T> itemType) throws XPathException {
		T[] a = (T[])Array.newInstance(itemType, array.arrayLength());
		int i = 0;
		for (Sequence s : array)
			a[i++] = objectFromItem(getSingleItem(s), itemType);
		return a;
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
	private static <T> T objectFromItem(Item item, Class<T> type) throws XPathException {
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
			else
				throw new IllegalArgumentException();
		else if (item instanceof ObjectValue) {
			Object o = ((ObjectValue<?>)item).getObject();
			if (type.isInstance(o))
				return (T)o;
			else
				throw new IllegalArgumentException("expected " + type + " object, but got " + o);
		} else
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

	private static Optional<Item> getOptionalItem(Sequence sequence) throws XPathException {
		SequenceIterator iterator = sequence.iterate();
		Item item = iterator.next();
		if (iterator.next() != null)
			throw new IllegalArgumentException();
		return Optional.ofNullable(item);
	}
}
