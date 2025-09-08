package org.daisy.common.xpath.saxon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;

import net.sf.saxon.dom.DocumentBuilderImpl;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;

/**
 * Poor man's implementation of Saxon's <a
 * href="https://www.saxonica.com/documentation/index.html#!extensibility/functions">reflexive
 * extension function mechanism</a>, which is only available in the PE and EE versions.
 */
public abstract class ReflexiveExtensionFunctionProvider implements ExtensionFunctionProvider {

	private final List<ExtensionFunctionDefinition> definitions;

	@Override
	public Collection<ExtensionFunctionDefinition> getDefinitions() {
		return definitions;
	}

	protected ReflexiveExtensionFunctionProvider(Class<?> definition) {
		this(new Class<?>[]{definition});
	}

	/**
	 * @param object When non-null, use this instance of the {@code definition} class to invoke all instance methods
	 *               on. In other words, it is used as the implicit first argument of all functions that correspond to
	 *               instance methods.
	 */
	protected <T> ReflexiveExtensionFunctionProvider(Class<T> definition, T object) {
		this.definitions = new ArrayList<>();
		addExtensionFunctionDefinitionsFromClass(definition, object);
	}

	protected ReflexiveExtensionFunctionProvider(Class<?>... definitions) {
		this.definitions = new ArrayList<>();
		for (Class<?> definition : definitions)
			addExtensionFunctionDefinitionsFromClass(definition, null);
	}

	/**
	 * This method can be used to add function definitions if for some reason it can not be done at construction time.
	 */
	protected <T> void addExtensionFunctionDefinitionsFromClass(Class<T> definition, T object) {
		definitions.addAll(extensionFunctionDefinitionsFromClass(definition, object));
	}

	private Collection<ExtensionFunctionDefinition> extensionFunctionDefinitionsFromClass(Class<?> definition, Object object) {
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
				if ("toString".equals(method.getName())
				    && method.getParameterCount() == 0
				    && !Modifier.isStatic(method.getModifiers())) {
					// skip because the method can already be called through the string() function:
					// ObjectValue.getStringValueCS() calls Object.toString()
					continue;
				}
				List<Executable> list = methods.get(method.getName());
				if (list == null) {
					list = new ArrayList<>();
					methods.put(method.getName(), list);
				}
				list.add(method);
				methods.put(method.getName(), list);
			}
		}
		Collection<ExtensionFunctionDefinition> definitions = new ArrayList<>();
		for (List<Executable> m : methods.values()) {
			Collections.sort(m, (a, b) -> new Integer(a.getParameterCount()).compareTo(b.getParameterCount()));
			definitions.add(extensionFunctionDefinitionFromMethods(m, object));
		}
		return definitions;
	}

	/**
	 * @param methods Collection of constructors of the same class, or methods of the same class and
	 *                with the same name. Assumed to be sorted by number of parameters (from least
	 *                to most number of parameters).
	 */
	private ExtensionFunctionDefinition extensionFunctionDefinitionFromMethods(Collection<Executable> methods, Object object)
			throws IllegalArgumentException {
		ExtensionFunctionDefinition ret = null;
		for (Executable m : methods) {
			ExtensionFunctionDefinition def = extensionFunctionDefinitionFromMethod(m, object);
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

	private ExtensionFunctionDefinition extensionFunctionDefinitionFromMethod(Executable method, Object object)
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
			boolean requiresXMLOutputValue = false;
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
					if (requiresXMLOutputValue || requiresXMLStreamWriter)
						throw new IllegalArgumentException(); // only one XMLStreamWriter or XMLOutputValue argument allowed
					requiresXMLStreamWriter = true;
				} else if (t.equals(XMLOutputValue.class)) {
					if (requiresXMLOutputValue || requiresXMLStreamWriter)
						throw new IllegalArgumentException(); // only one XMLStreamWriter or XMLOutputValue argument allowed
					requiresXMLOutputValue = true;
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
				+ ((isStatic || object != null) ? 0 : 1)
				- (requiresXMLOutputValue ? 1 : 0)
				- (requiresXMLStreamWriter ? 1 : 0)
				- (requiresXPath ? 1 : 0)
				- (requiresDocumentBuilder ? 1 : 0)
			];
			int i = 0;
			if (!isStatic && object == null)
				argumentTypes[i++] = SequenceType.SINGLE_ITEM; // must be special wrapper item
			for (Type t : javaArgumentTypes)
				if (!(t instanceof ParameterizedType && ((ParameterizedType)t).getRawType().equals(XMLOutputValue.class)) &&
				    !t.equals(XMLOutputValue.class) &&
				    !t.equals(XMLStreamWriter.class) &&
				    !t.equals(XPath.class) &&
				    !t.equals(DocumentBuilder.class))
					argumentTypes[i++] = SaxonHelper.sequenceTypeFromType(t);
			SequenceType resultType; {
				if (isConstructor) {
					if (requiresXMLStreamWriter || requiresXMLOutputValue)
						throw new IllegalArgumentException(); // no XMLStreamWriter or XMLOutputValue argument allowed in case of constructor
					resultType = SequenceType.SINGLE_ITEM; // special wrapper item
				} else {
					Type t = ((Method)method).getGenericReturnType();
					if (requiresXMLStreamWriter || requiresXMLOutputValue) {
						if (!t.equals(Void.TYPE))
							throw new IllegalArgumentException(); // XMLStreamWriter or XMLOutputValue argument only allowed when return type is void
						resultType = SequenceType.NODE_SEQUENCE;
					} else
						resultType = SaxonHelper.sequenceTypeFromType(t);
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
									if (object != null)
										instance = object;
									else {
										Item item = SaxonHelper.getSingleItem(args[i++]);
										try {
											instance = SaxonHelper.objectFromItem(item, declaringClass);
										} catch (IllegalArgumentException e) {
											throw new IllegalArgumentException(
												"Expected ObjectValue<" + declaringClass.getSimpleName() + ">" + ", but got: " + item, e);
										}
									}
								}
								List<NodeInfo> nodeListFromXMLOutputValue = null;
								Object[] javaArgs = new Object[ // arguments passed to Method.invoke() in addition to instance,
								                                // or to Constructor.newInstance()
									method.getParameterCount()
								];
								int j = 0;
								if (isConstructor && isInnerClass)
									// in case of constructor of inner class, first argument is enclosing instance
									javaArgs[j++] = ReflexiveExtensionFunctionProvider.this;
								for (Type type : javaArgumentTypes) {
									if (type.equals(XMLStreamReader.class) ||
									    (type instanceof ParameterizedType
									     && ((ParameterizedType)type).getRawType().equals(XMLInputValue.class))) {
										XMLInputValue xmlInputValueArg = new SaxonInputValue(
											(Iterator<XdmNode>)SaxonHelper.iteratorFromSequence(args[i++], XdmNode.class));
										if (type.equals(XMLStreamReader.class))
											javaArgs[j++] = xmlInputValueArg.asXMLStreamReader();
										else
											javaArgs[j++] = xmlInputValueArg;
									} else if (type.equals(XMLStreamWriter.class) ||
									           (type instanceof ParameterizedType
									            && ((ParameterizedType)type).getRawType().equals(XMLOutputValue.class))) {
										List<NodeInfo> list = new ArrayList<>();
										nodeListFromXMLOutputValue = list;
										XMLOutputValue xmlOutputValueArg = new SaxonOutputValue(
											item -> {
												if (item instanceof XdmNode)
													list.add(((XdmNode)item).getUnderlyingNode());
												else
													throw new RuntimeException(); // should not happen
											},
											ctxt.getConfiguration()
										);
										if (type.equals(XMLStreamWriter.class))
											javaArgs[j++] = xmlOutputValueArg.asXMLStreamWriter();
										else
											javaArgs[j++] = xmlOutputValueArg;
									} else if (type.equals(XPath.class))
										javaArgs[j++] = new XPathFactoryImpl(ctxt.getConfiguration()).newXPath();
									else if (type.equals(DocumentBuilder.class)) {
										DocumentBuilderImpl b = new DocumentBuilderImpl();
										b.setConfiguration(ctxt.getConfiguration());
										javaArgs[j++] = b;
									} else if (type instanceof ParameterizedType
									           && ((ParameterizedType)type).getRawType().equals(Iterator.class))
										javaArgs[j++] = SaxonHelper.iteratorFromSequence(
											args[i++],
											((ParameterizedType)type).getActualTypeArguments()[0]);
									else if (type instanceof ParameterizedType
									           && ((ParameterizedType)type).getRawType().equals(Iterable.class))
										javaArgs[j++] = SaxonHelper.iterableFromSequence(
											args[i++],
											((ParameterizedType)type).getActualTypeArguments()[0]);
									else if (type instanceof ParameterizedType
									           && ((ParameterizedType)type).getRawType().equals(Optional.class)) {
										Optional<Item> item = SaxonHelper.getOptionalItem(args[i++]);
										javaArgs[j++] = item.isPresent()
											? Optional.of(SaxonHelper.objectFromItem(item.get(), ((ParameterizedType)type).getActualTypeArguments()[0]))
											: Optional.empty();
									} else if (type.equals(URI.class)) {
										// could be empty because sequenceTypeFromType() returned OPTIONAL_ANY_URI
										Optional<Item> item = SaxonHelper.getOptionalItem(args[i++]);
										if (!item.isPresent())
											throw new IllegalArgumentException("Expected xs:anyURI but got an empty sequence");
										javaArgs[j++] = SaxonHelper.objectFromItem(item.get(), type);
									} else
										javaArgs[j++] = SaxonHelper.objectFromItem(SaxonHelper.getSingleItem(args[i++]), type);
								}
								Object result; {
									try {
										if (isConstructor)
											result = ((Constructor<?>)method).newInstance(javaArgs);
										else // instance is null in case of static method
											result = ((Method)method).invoke(instance, javaArgs);
									} catch (InstantiationException|InvocationTargetException e) {
										Throwable cause = e.getCause();
										if (cause instanceof XPathException)
											throw (XPathException)cause;
										else if (cause instanceof TransformerException) {
											// TransformerException is just a wrapper of the actual exception, so unwrap
											// it. Note that if the TransformerException is an instance of
											// XProcErrorException, unwrapping it will yield an XProcException.
											XPathException xpe = new XPathException(cause.getMessage(), cause.getCause());
											QName code = ((TransformerException)cause).getCode();
											if (code != null)
												xpe.setErrorCodeQName(new StructuredQName(code.getPrefix(),
												                                          code.getNamespaceURI(),
												                                          code.getLocalPart()));
											throw xpe;
										} else
											throw new XPathException(cause);
									} catch (IllegalAccessException e) {
										throw new RuntimeException(e); // should not happen
									}
								}
								if (nodeListFromXMLOutputValue != null)
									return new SequenceExtent(nodeListFromXMLOutputValue);
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
}
