package org.daisy.pipeline.build.annotation.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.ReferencePolicy;

public class ComponentModel {
	
	String name;
	String packageName;
	String className;
	String qualifiedClassName;
	String spiClassName;
	ActivateModel activate;
	DeactivateModel deactivate;
	boolean immediate;
	final List<PropertyModel> properties = new ArrayList<PropertyModel>();
	final List<ServiceModel> services = new ArrayList<ServiceModel>();
	boolean proxy; // whether to generate proxy class or extend the class
	final List<ServiceMethodModel> serviceMethods = new ArrayList<ServiceMethodModel>();
	final List<ReferenceModel> references = new ArrayList<ReferenceModel>();
	String classLoader;
	
	public String getName() {
		return name;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getQualifiedClassName() {
		return qualifiedClassName;
	}
	
	public String getSpiClassName() {
		return spiClassName;
	}
	
	public List<PropertyModel> getProperties() {
		return properties;
	}
	
	public List<ServiceModel> getServices() {
		return services;
	}

	public boolean getProxy() {
		return proxy;
	}
	
	public List<ServiceMethodModel> getServiceMethods() {
		return serviceMethods;
	}
	
	public ActivateModel getActivate() {
		return activate;
	}
	
	public DeactivateModel getDeactivate() {
		return deactivate;
	}
	
	public boolean getImmediate() {
		return immediate || services.isEmpty();
	}
	
	public List<ReferenceModel> getReferences() {
		Collections.sort(references, new Comparator<ReferenceModel>() {
			public int compare(ReferenceModel o1, ReferenceModel o2) {
				if (o1.policy == ReferencePolicy.STATIC && o2.policy != ReferencePolicy.STATIC)
					return -1;
				else if (o1.policy != ReferencePolicy.STATIC && o2.policy == ReferencePolicy.STATIC)
					return 1;
				else
					return 0;
			}
		});
		return references;
	}

	public String getClassLoader() {
		return classLoader;
	}
	
	public static class ServiceModel {
		
		String name; // fully qualified name, possibly parameterized
		String flatName;
		
		public String getName() {
			return name;
		}

		public String flatName() {
			return flatName;
		}
	}

	public static class ServiceMethodModel {
		
		String name;
		String returnType;
		List<String> argumentTypes = new ArrayList<String>();
		List<String> thrownTypes = new ArrayList<String>();
		
		public String getName() {
			return name;
		}
		
		public String getReturnType() {
			return returnType;
		}
		
		public List<String> getArgumentTypes() {
			return argumentTypes;
		}
		
		public List<String> getThrownTypes() {
			return thrownTypes;
		}
	}
	
	public static class ActivateModel {
		
		String methodName;
		Class<?> propertiesArgumentType;
		
		public String getMethodName() {
			return methodName;
		}
		
		public Class<?> getPropertiesArgumentType() {
			return propertiesArgumentType;
		}
	}
	
	public static class DeactivateModel {
		
		String methodName;
		
		public String getMethodName() {
			return methodName;
		}
	}

	public static class ReferenceModel {
		
		String methodName;
		String service;
		String cardinality;
		ReferencePolicy policy;
		String filter;
		Class<?> propertiesArgumentType;
		
		public String getMethodName() {
			return methodName;
		}
		
		public String getService() {
			return service;
		}
		
		public String getCardinality() {
			return cardinality;
		}
		
		public String getFilter() {
			return filter == null ? null : "\"" + filter.replaceAll("\"", "\\\\\"") + "\"";
		}
		
		public Class<?> getPropertiesArgumentType() {
			return propertiesArgumentType;
		}
	}
	
	public static class PropertyModel {
		
		Class<?> type;
		String key;
		Object value;
		
		public String getKeyLiteral() {
			return "\"" + key.replaceAll("\"", "\\\\\"") + "\"";
		}
		
		public String getValueLiteral() {
			if (type == String.class)
				return "\"" + value.toString().replaceAll("\"", "\\\\\"") + "\"";
			else
				return value.toString();
		}
	}
}
