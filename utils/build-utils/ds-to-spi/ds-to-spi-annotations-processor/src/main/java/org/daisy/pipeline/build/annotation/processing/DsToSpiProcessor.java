package org.daisy.pipeline.build.annotation.processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

import aQute.bnd.header.OSGiHeader;
import aQute.bnd.osgi.Instruction;
import aQute.bnd.osgi.Instructions;

import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Type.ClassType;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.daisy.common.spi.annotations.LoadWith;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

// TODO: warnings for felix and bnd annotations?

@SupportedAnnotationTypes({
	"org.daisy.common.spi.annotations.LoadWith",
	"org.osgi.service.component.annotations.Component",
	"org.osgi.service.component.annotations.Activate",
	"org.osgi.service.component.annotations.Deactivate",
	"org.osgi.service.component.annotations.Modified",
	"org.osgi.service.component.annotations.Reference"
})
@SupportedOptions({
	"dsToSpi.generatedResourcesDirectory",
	"dsToSpi.generatedSourcesDirectory"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DsToSpiProcessor extends AbstractProcessor {
	
	final static Pattern PROPERTY_PATTERN = Pattern.compile(
		"\\s*([^=\\s:]+)\\s*(?::\\s*(Boolean|Byte|Character|Short|Integer|Long|Float|Double|String)\\s*)?=(.*)"
	);
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.isEmpty())
			return true;
		try {
			Map<String,String> options = processingEnv.getOptions();
			File generatedResourcesDirectory; {
				String path = options.get("dsToSpi.generatedResourcesDirectory");
				if (path == null)
					throw new RuntimeException("Option 'dsToSpi.generatedResourcesDirectory' must be set");
				generatedResourcesDirectory = new File(path);
			}
			File generatedSourcesDirectory; {
				String path = options.get("dsToSpi.generatedSourcesDirectory");
				if (path == null)
					throw new RuntimeException("Option 'dsToSpi.generatedSourcesDirectory' must be set");
				generatedSourcesDirectory = new File(path);
			}
			Instructions includes; {
				String s = options.get("dsToSpi.includes");
				if (s == null)
					throw new RuntimeException("Option 'dsToSpi.includes' must be set");
				includes = new Instructions(OSGiHeader.parseHeader(s));
			}
			Map<String,ComponentModel> components = new HashMap<String,ComponentModel>();
			Set<String> includeClasses = new HashSet<String>();
			Set<String> excludeClasses = new HashSet<String>();
			Set<String> services = new HashSet<String>();
			for (Element e : roundEnv.getElementsAnnotatedWith(Component.class)) {
				if (e.getKind() == ElementKind.CLASS) {
					TypeElement classElement = (TypeElement)e;
					PackageElement packageElement = getPackageElement(classElement);
					Component componentAnnotation = classElement.getAnnotation(Component.class);
					LoadWith loadWithAnnotation = classElement.getAnnotation(LoadWith.class);
					ComponentModel component = new ComponentModel(); {
						component.name = componentAnnotation.name();
						component.qualifiedClassName = classElement.getQualifiedName().toString();
						component.packageName = packageName(packageElement);
						component.className = component.packageName != null ?
							component.qualifiedClassName.substring(component.packageName.length() + 1) :
							component.qualifiedClassName;
						if (includeClass(classElement, includes, includeClasses, excludeClasses)) {
							component.spiClassName = component.className.replaceAll("\\.", "_")  + "_SPI";
							if (!"".equals(componentAnnotation.factory())) {
								printError("@Component with factory = " + componentAnnotation.factory() + " not supported", e);
								throw new RuntimeException();
							}
							if (componentAnnotation.servicefactory()) {
								printError("@Component with servicefactory = true not supported", e);
								throw new RuntimeException();
							}
							if (!componentAnnotation.enabled()) {
								printError("@Component with enabled = false not supported", e);
								throw new RuntimeException();
							}
							component.immediate = componentAnnotation.immediate();
							for (String p : componentAnnotation.property()) {
								Matcher m = PROPERTY_PATTERN.matcher(p);
								if (m.matches()) {
									ComponentModel.PropertyModel property = new ComponentModel.PropertyModel(); {
										property.key = m.group(1);
										String type = m.group(2);
										String value = m.group(3);
										if (type == null || type.equals("String")) {
											property.type = String.class;
											property.value = value;
										} else if (type.equals("Boolean")) {
											property.type = Boolean.class;
											property.value = Boolean.getBoolean(value);
										} else {
											printError("Properties with type '" + type + "' not supported", e);
											throw new RuntimeException();
										}
									}
									component.properties.add(property);
								} else {
									printError("Malformed property '" + p + "' on component: " + classElement.getQualifiedName(), e);
									throw new RuntimeException();
								}
							}
							if (componentAnnotation.properties().length > 0) {
								printError("@Component with properties = " + componentAnnotation.properties() + " not supported", e);
								throw new RuntimeException();
							}
							AnnotationValue serviceList = getAnnotationValue(classElement, Component.class.getName(), "service");
							boolean onlyInterfaces = true;
							if (serviceList != null) {
								Map<String,TypeElement> interfaces = new HashMap<String,TypeElement>();
								for (AnnotationValue v : (List<? extends AnnotationValue>)serviceList.getValue()) {
									ClassType classType = (ClassType)v.getValue();
									if (classType.isInterface()) {
										TypeElement typeElement = (TypeElement)processingEnv.getTypeUtils().asElement(classType);
										interfaces.put(typeElement.getQualifiedName().toString(), typeElement);
									} else {
										onlyInterfaces = false;
										if (loadWithAnnotation != null) {
											printError(
												"@LoadWith only supported when all services are interfaces, but got " + classType.toString(),
												e);
											throw new RuntimeException();
										}
									}
								}
								if (onlyInterfaces) {
									List<TypeElement> superInterfaces = new ArrayList<>();
									BoundTypeParameters boundTypeParameters = new BoundTypeParameters();
									getInterfacesRecursively(classElement, x -> {}, boundTypeParameters);
									for (TypeElement i : interfaces.values()) {
										for (TypeParameterElement p : i.getTypeParameters()) {
											TypeMirror upper = null;
											for (TypeMirror b : p.getBounds())
												if (upper != null)
													throw new IllegalArgumentException("multiple-bounded type parameters");
												else
													upper = b;
											boundTypeParameters.put(p, upper);
										}
										getInterfacesRecursively(i, superInterfaces::add, boundTypeParameters);
									}
									for (AnnotationValue v : (List<? extends AnnotationValue>)serviceList.getValue()) {
										ClassType classType = (ClassType)v.getValue();
										ComponentModel.ServiceModel service = new ComponentModel.ServiceModel();
										service.name = boundTypeParameters.resolveVariables(classType);
										service.flatName = classType.tsym.flatName().toString();
										component.services.add(service);
										services.add(service.flatName);
									}
									for (TypeElement i : superInterfaces)
										interfaces.put(i.getQualifiedName().toString(), i);
									for (String k : interfaces.keySet()) {
										TypeElement i = interfaces.get(k);
										for (Element enclosedElement : i.getEnclosedElements()) {
											if (enclosedElement instanceof ExecutableElement) {
												ExecutableElement exeElement = (ExecutableElement)enclosedElement;
												if (!exeElement.getModifiers().contains(Modifier.STATIC)) {
													ComponentModel.ServiceMethodModel method = new ComponentModel.ServiceMethodModel();
													method.name = exeElement.getSimpleName().toString();
													method.returnType = boundTypeParameters.resolveVariables(exeElement.getReturnType());
													for (VariableElement variableElement : exeElement.getParameters()) {
														method.argumentTypes.add(boundTypeParameters.resolveVariables(variableElement.asType()));
													}
													for (TypeMirror thrown : exeElement.getThrownTypes()) {
														method.thrownTypes.add(thrown.toString());
													}
													component.serviceMethods.add(method);
												}
											}
										}
									}
								} else {
									for (AnnotationValue v : (List<? extends AnnotationValue>)serviceList.getValue()) {
										ClassType classType = (ClassType)v.getValue();
										ComponentModel.ServiceModel service = new ComponentModel.ServiceModel();
										service.flatName = classType.tsym.flatName().toString();
										component.services.add(service);
										services.add(service.flatName);
									}
								}
							}
							component.proxy = onlyInterfaces;
							if (componentAnnotation.configurationPolicy() != ConfigurationPolicy.OPTIONAL) {
								printError("@Component with configurationPolicy = " + componentAnnotation.configurationPolicy() + " not supported", e);
								throw new RuntimeException();
							}
							component.classLoader = loadWithAnnotation != null
								? getAnnotationValue(classElement, LoadWith.class.getName(), "value").getValue().toString()
								: null;
							components.put(classElement.getQualifiedName().toString(), component);
						}
					}
				} else {
					printError("@Component only applies to classes", e);
					throw new RuntimeException();
				}
			}
			for (Element e : roundEnv.getElementsAnnotatedWith(Reference.class)) {
				if (e.getKind() == ElementKind.METHOD) {
					ExecutableElement exeElement = (ExecutableElement)e;
					TypeElement classElement = (TypeElement)exeElement.getEnclosingElement();
					ComponentModel component = components.get(classElement.getQualifiedName().toString());
					if (!includeClass(classElement, includes, includeClasses, excludeClasses)) {
					} else if (component != null) {
						Reference referenceAnnotation = exeElement.getAnnotation(Reference.class);
						ComponentModel.ReferenceModel reference = new ComponentModel.ReferenceModel(); {
							String bindMethod = exeElement.getSimpleName().toString();
							reference.methodName = bindMethod;
							reference.service = getAnnotationValue(exeElement, Reference.class.getName(), "service").getValue().toString();
							reference.cardinality = referenceAnnotation.cardinality().toString();
							reference.policy = referenceAnnotation.policy();
							if (reference.policy != ReferencePolicy.STATIC) {
								printWarning("No dynamic binding with SPI: method '" + bindMethod + "' will never be called after component has been activated", e);
							}
							if (!"".equals(referenceAnnotation.target())) {
								reference.filter = referenceAnnotation.target();
							}
							if (!"-".equals(referenceAnnotation.unbind())) {
								String unbindMethod = referenceAnnotation.unbind();
								if ("".equals(unbindMethod)) {
									if (bindMethod.startsWith("add")) {
										unbindMethod = bindMethod.replaceAll("^add", "remove");
									} else {
										unbindMethod = "un" + bindMethod;
									}
								}
								// TODO: verify if method exists
								printWarning("No unbinding with SPI: method '" + unbindMethod + "' (if it exists) will never be called", e);
							}
							if (referenceAnnotation.policyOption() != ReferencePolicyOption.RELUCTANT) {
								printError("@Component with policyOption = " + referenceAnnotation.policyOption() + " not supported", e);
								throw new RuntimeException();
							}
							if (!"-".equals(referenceAnnotation.updated())) {
								String updatedMethod = referenceAnnotation.updated();
								if ("".equals(updatedMethod)) {
									updatedMethod = "updated" + bindMethod.replaceAll("^(bind|set|add)", "");
									// TODO: verify if method exists
									printWarning("No component updating with SPI: method '" + updatedMethod + "' (if it exists) will never be called", e);
								}
							}
							if (exeElement.getParameters().size() == 1) {
								reference.propertiesArgumentType = null;
							} else if (exeElement.getParameters().size() == 2) {
								String argTypeName = exeElement.getParameters().get(1).asType().toString();
								Class<?> argType; {
									try {
										argType = Class.forName(argTypeName.replaceAll("<.+>$", ""));
									} catch (ClassNotFoundException ex) {
										printError("@Reference method with argument type '" + argTypeName + "' not supported", e);
										throw new RuntimeException();
									}
								}
								if (argType == Dictionary.class) {
									printWarning("No component configuration with SPI except within component declaration itself", e);
									reference.propertiesArgumentType = Dictionary.class;
								} else if (argType == Map.class) {
									printWarning("No component configuration with SPI except within component declaration itself", e);
									reference.propertiesArgumentType = HashMap.class;
								} else {
									printError("@Reference method with argument type '" + argTypeName + "' not supported", e);
									throw new RuntimeException();
								}
							} else {
								printError("@Reference method with more than 2 arguments not supported", e);
								throw new RuntimeException();
							}
						}
						component.references.add(reference);
					} else {
						printError("@Reference without @Component", e);
						throw new RuntimeException();
					}
				} else {
					printError("@Reference only applies to methods", e);
					throw new RuntimeException();
				}
			}
			for (Element e : roundEnv.getElementsAnnotatedWith(Activate.class)) {
				if (e.getKind() == ElementKind.METHOD) {
					ExecutableElement exeElement = (ExecutableElement)e;
					TypeElement classElement = (TypeElement)exeElement.getEnclosingElement();
					ComponentModel component = components.get(classElement.getQualifiedName().toString());
					if (!includeClass(classElement, includes, includeClasses, excludeClasses)) {
					} else if (component != null) {
						ComponentModel.ActivateModel activate = new ComponentModel.ActivateModel(); {
							activate.methodName = exeElement.getSimpleName().toString();
							if (exeElement.getParameters().size() == 0) {
								activate.propertiesArgumentType = null;
							} else if (exeElement.getParameters().size() == 1) {
								String argTypeName = exeElement.getParameters().get(0).asType().toString();
								Class<?> argType; {
									try {
										argType = Class.forName(argTypeName.replaceAll("<.+>$", ""));
									} catch (ClassNotFoundException ex) {
										printError("@Activate method with argument type '" + argTypeName + "' not supported", e);
										throw new RuntimeException();
									}
								}
								if (argType == Dictionary.class) {
									printWarning("No component configuration with SPI except within component declaration itself", e);
									activate.propertiesArgumentType = Dictionary.class;
								} else if (argType == Map.class) {
									printWarning("No component configuration with SPI except within component declaration itself", e);
									activate.propertiesArgumentType = HashMap.class;
								} else {
									printError("@Activate method with argument type '" + argTypeName + "' not supported", e);
									throw new RuntimeException();
								}
							} else {
								printError("@Activate method with more than 1 argument not supported", e);
								throw new RuntimeException();
							}
						}
						component.activate = activate;
					} else {
						printError("@Activate without @Component", e);
						throw new RuntimeException();
					}
				} else {
					printError("@Activate only applies to methods", e);
					throw new RuntimeException();
				}
			}
			for (Element e : roundEnv.getElementsAnnotatedWith(Deactivate.class)) {
				if (e.getKind() == ElementKind.METHOD) {
					ExecutableElement exeElement = (ExecutableElement)e;
					TypeElement classElement = (TypeElement)exeElement.getEnclosingElement();
					ComponentModel component = components.get(classElement.getQualifiedName().toString());
					if (!includeClass(classElement, includes, includeClasses, excludeClasses)) {
					} else if (component != null) {
						ComponentModel.DeactivateModel deactivate = new ComponentModel.DeactivateModel(); {
							deactivate.methodName = exeElement.getSimpleName().toString();
							if (exeElement.getParameters().size() != 0) {
								printError("@Deactivate method with arguments not supported", e);
								throw new RuntimeException();
							}
						}
						component.deactivate = deactivate;
					} else {
						printError("@Deactivate without @Component", e);
						throw new RuntimeException();
					}
				} else {
					printError("@Deactivate only applies to methods", e);
					throw new RuntimeException();
				}
			}
			for (Element e : roundEnv.getElementsAnnotatedWith(Modified.class)) {
				printWarning("No component modification with SPI: method '" + ((ExecutableElement)e).getSimpleName().toString() + "' will never be called", e);
			}
			for (String service : services) {
				File dest = new File(new File(generatedResourcesDirectory, "META-INF/services"), service);
				dest.getParentFile().mkdirs();
				printWarning("creating META-INF/services file: " + dest);
				BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
				for (ComponentModel component : components.values()) {
					for (ComponentModel.ServiceModel s : component.services)
						if (service.equals(s.flatName)) {
							writer.write((component.packageName != null ? component.packageName + "." : "") + component.spiClassName);
							writer.newLine();
							break;
						}
				}
				writer.close();
			}
			Set<ComponentModel> immediateComponents = new HashSet<ComponentModel>();
			for (ComponentModel component : components.values()) {
				if (component.getImmediate()) {
					immediateComponents.add(component);
				}
			}
			if (!immediateComponents.isEmpty()) {
				String service = "org.daisy.common.spi.CreateOnStart";
				File dest = new File(new File(generatedResourcesDirectory, "META-INF/services"), service);
				dest.getParentFile().mkdirs();
				printWarning("creating META-INF/services file: " + dest);
				BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
				for (ComponentModel component : immediateComponents) {
					writer.write((component.packageName != null ? component.packageName + "." : "") + component.spiClassName);
					writer.newLine();
				}
				writer.close();
			}
			if (!components.isEmpty()) {
				Properties props = new Properties();
				URL url = this.getClass().getClassLoader().getResource("velocity.properties");
				props.load(url.openStream());
				VelocityEngine ve = new VelocityEngine(props);
				ve.init();
				Template vt = ve.getTemplate("ds-to-spi.vm");
				for (ComponentModel component : components.values()) {
					VelocityContext vc = new VelocityContext();
					vc.put("component", component);
					File dest = new File(generatedSourcesDirectory,
					                     (component.packageName != null ? component.packageName.replaceAll("\\.", "/") + "/" : "")
					                      + component.spiClassName + ".java");
					dest.getParentFile().mkdirs();
					printWarning("creating source file: " + dest);
					Writer writer = new FileWriter(dest);
					vt.merge(vc, writer);
					writer.close();
				}
			}
		} catch (ResourceNotFoundException rnfe) {
			printError(rnfe);
		} catch (ParseErrorException pee) {
			printError(pee);
		} catch (IOException ioe) {
			printError(ioe);
		} catch (Exception e) {
			printError(e);
		}
		return true;
	}
	
	private boolean includeClass(TypeElement classElement, Instructions includes,
	                             Set<String> includeClasses, Set<String> excludeClasses) {
		String qualifiedClassName = classElement.getQualifiedName().toString();
		if (includeClasses.contains(qualifiedClassName))
			return true;
		else if (excludeClasses.contains(qualifiedClassName))
			return false;
		else {
			boolean include = false;
			String packageName = packageName(getPackageElement(classElement));
			String className = packageName != null ? qualifiedClassName.substring(packageName.length() + 1) : qualifiedClassName;
			String fqn = (packageName != null ? packageName + "." : "") + className.replaceAll("\\.", "\\$");
			for (Instruction i : includes.keySet())
				if (i.matches(fqn)) {
					include = !i.isNegated();
					break;
				}
			if (include)
				includeClasses.add(qualifiedClassName);
			else {
				printNote("Skipping annotations processing of class " + qualifiedClassName);
				excludeClasses.add(qualifiedClassName);
			}
			return include;
		}
	}
	
	private static String packageName(PackageElement packageElement) {
		String name = packageElement.getQualifiedName().toString();
		if ("".equals(name))
			return null;
		return name;
	}
	
	private static PackageElement getPackageElement(TypeElement typeElement) {
		Element enclosingElement = typeElement.getEnclosingElement();
		while(!(enclosingElement instanceof PackageElement)) {
			enclosingElement = enclosingElement.getEnclosingElement();
		}
		return (PackageElement)enclosingElement;
	}
	
	private void getInterfacesRecursively(TypeElement element, Consumer<TypeElement> collect, BoundTypeParameters bindTypeParameters) {
		TypeMirror s = element.getSuperclass();
		if (s instanceof ClassType) {
			TypeElement e = (TypeElement)processingEnv.getTypeUtils().asElement(s);
			Iterator<? extends TypeParameterElement> typeParameters = e.getTypeParameters().iterator();
			for (TypeMirror arg : ((ClassType)s).getTypeArguments()) {
				if (!typeParameters.hasNext())
					throw new IllegalStateException();
				bindTypeParameters.put(typeParameters.next(), arg);
			}
			while (typeParameters.hasNext()) {
				TypeMirror upper = null;
				for (TypeMirror b : typeParameters.next().getBounds())
					if (upper != null)
						throw new IllegalArgumentException("multiple-bounded type parameters");
					else
						upper = b;
				bindTypeParameters.put(typeParameters.next(), upper);
			}
			getInterfacesRecursively(e, collect, bindTypeParameters);
		}
		for (TypeMirror i : element.getInterfaces()) {
			TypeElement e = (TypeElement)processingEnv.getTypeUtils().asElement(i);
			collect.accept(e);
			Iterator<? extends TypeParameterElement> typeParameters = e.getTypeParameters().iterator();
			for (TypeMirror arg : ((ClassType)i).getTypeArguments()) {
				if (!typeParameters.hasNext())
					throw new IllegalStateException();
				bindTypeParameters.put(typeParameters.next(), arg);
			}
			while (typeParameters.hasNext()) {
				TypeMirror upper = null;
				for (TypeMirror b : typeParameters.next().getBounds())
					if (upper != null)
						throw new IllegalArgumentException("multiple-bounded type parameters");
					else
						upper = b;
				bindTypeParameters.put(typeParameters.next(), upper);
			}
			getInterfacesRecursively(e, collect, bindTypeParameters);
		}
	}
	
	private static AnnotationValue getAnnotationValue(Element element, String annotationName, String valueName) {
		for (AnnotationMirror am : element.getAnnotationMirrors())
			if (annotationName.equals(am.getAnnotationType().toString()))
				for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : am.getElementValues().entrySet())
					if (valueName.equals(entry.getKey().getSimpleName().toString()))
						return entry.getValue();
		return null;
	}
	
	private void printError(Exception e) {
		e.printStackTrace();
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "");
	}
	
	private void printError(String message, Element e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e);
	}
	
	private void printWarning(String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
	}
	
	private void printWarning(String message, Element e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, e);
	}
	
	private void printNote(String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
	}
	
	/**
	 * To keep track of which type parameters are bound to which types
	 */
	private class BoundTypeParameters {
		
		private final Map<String,BoundTypeParameter> map = new HashMap<>();
		
		public BoundTypeParameter put(TypeParameterElement parameter, TypeMirror typeArgument) {
			BoundTypeParameter b = new BoundTypeParameter(parameter, typeArgument);
			String k = parameterKey(parameter);
			BoundTypeParameter prev = map.put(k, b);
			if (prev != null) {
				b = prev.combine(b);
				map.put(k, b);
			}
			return b;
		}
		
		public BoundTypeParameter get(TypeParameterElement parameter) {
			return map.get(parameterKey(parameter));
		}
		
		public BoundTypeParameter get(TypeVariable parameter) {
			return map.get(parameterKey(parameter));
		}
		
		/**
		 * Key for looking up parameter in a map
		 */
		private String parameterKey(TypeParameterElement parameter) {
			return parameter.getGenericElement() + ":" + parameter;
		}
		
		private String parameterKey(TypeVariable parameter) {
			return ((TypeVariableSymbol)parameter.asElement()).getGenericElement() + ":" + parameter;
		}
		
		/**
		 * Resolve variables in a type expression
		 */
		public String resolveVariables(TypeMirror type) {
			if (type instanceof NoType // void
			    || type instanceof PrimitiveType)
				return type.toString();
			else
				return new BoundTypeParameter(null, type).toString();
		}
		
		private class BoundTypeParameter implements Cloneable {
			
			/**
			 * The parameter
			 */
			private final TypeParameterElement parameter;
			/**
			 * The type that {@code parameter} is bound to
			 */
			private TypeElement type;
			/**
			 * Bound parameters of {@code type}
			 */
			private List<BoundTypeParameter> boundTypeParameters;
			
			/**
			 * @param typeArgument a specified type argument
			 * @param boundTypeParameters to lookup type variable bindings of the enclosing type
			 */
			private BoundTypeParameter(TypeParameterElement parameter, TypeMirror typeArgument) {
				this.parameter = parameter;
				if (typeArgument == null) {
					this.type = null; // represents Object
					this.boundTypeParameters = null;
				} else if (typeArgument instanceof ClassType) {
					this.type = (TypeElement)processingEnv.getTypeUtils().asElement((ClassType)typeArgument);
					Iterator<? extends TypeParameterElement> typeParameters = this.type.getTypeParameters().iterator();
					this.boundTypeParameters = typeParameters.hasNext() ? new ArrayList<>() : null;
					for (TypeMirror arg : ((ClassType)typeArgument).getTypeArguments()) {
						if (!typeParameters.hasNext())
							throw new IllegalStateException();
						this.boundTypeParameters.add(BoundTypeParameters.this.put(typeParameters.next(), arg));
					}
					while (typeParameters.hasNext()) {
						TypeParameterElement p = typeParameters.next();
						TypeMirror upper = null;
						for (TypeMirror b : p.getBounds())
							if (upper != null)
								throw new IllegalArgumentException("multiple-bounded type parameters");
							else
								upper = b;
						this.boundTypeParameters.add(BoundTypeParameters.this.put(p, upper));
					}
				} else if (typeArgument instanceof TypeVariable) {
					BoundTypeParameter type = BoundTypeParameters.this.get((TypeVariable)typeArgument);
					if (type == null)
						throw new IllegalStateException();
					this.type = type.type;
					this.boundTypeParameters = type.boundTypeParameters;
				} else
					throw new IllegalArgumentException();
			}
		
			/**
			 * Combine two types (which are assumed to be compatible) into
			 * a new type that both types are assignable from.
			 */
			public BoundTypeParameter combine(BoundTypeParameter other) {
				if (!this.parameter.equals(other.parameter))
					throw new IllegalArgumentException();
				BoundTypeParameter common = clone();
				if (this.isAssignableFrom(other.type))
					common.type = other.type;
				else if (other.isAssignableFrom(this.type))
					;
				else
					throw new IllegalStateException("incompatible types");
				if (common.boundTypeParameters != null)
					for (int i = 0; i < common.boundTypeParameters.size(); i++)
						common.boundTypeParameters.set(
							i,
							common.boundTypeParameters.get(i).combine(other.boundTypeParameters.get(i)));
				return common;
			}

			/**
			 * Naive implementation of isAssignableFrom
			 */
			private boolean isAssignableFrom(TypeElement other) {
				if (type.equals(other))
					return true;
				for (TypeMirror i : other.getInterfaces())
					if (isAssignableFrom((TypeElement)processingEnv.getTypeUtils().asElement(i)))
						return true;
				TypeMirror s = other.getSuperclass();
				if (!(s instanceof NoType))
					if (isAssignableFrom((TypeElement)processingEnv.getTypeUtils().asElement(s)))
						return true;
				return false;
			}
			
			@Override
			public String toString() {
				if (type == null)
					return "java.lang.Object";
				else {
					String s = "";
					s += type.getQualifiedName();
					if (boundTypeParameters != null && boundTypeParameters.size() > 0) {
						s += "<";
						boolean first = true;
						for (BoundTypeParameter p : boundTypeParameters) {
							if (!first) s += ",";
							first = false;
							s += p;
						}
						s += ">";
					}
					return s;
				}
			}
			
			@Override
			public BoundTypeParameter clone() {
				BoundTypeParameter clone; {
					try {
						clone = (BoundTypeParameter)super.clone();
					} catch (CloneNotSupportedException e) {
						throw new InternalError("coding error");
					}
				}
				if (clone.boundTypeParameters != null)
					clone.boundTypeParameters = new ArrayList<>(clone.boundTypeParameters);
				return clone;
			}
		}
	}
}
