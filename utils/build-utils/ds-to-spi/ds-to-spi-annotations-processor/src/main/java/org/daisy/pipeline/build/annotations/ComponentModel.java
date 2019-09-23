package org.daisy.pipeline.build.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.osgi.service.component.annotations.ReferencePolicy;

public class ComponentModel {
	
	String name;
	String packageName;
	String className;
	String qualifiedClassName;
	String spiClassName;
	ActivateModel activate;
	boolean immediate;
	final List<PropertyModel> properties = new ArrayList<PropertyModel>();
	final List<String> services = new ArrayList<String>();
	final List<ReferenceModel> references = new ArrayList<ReferenceModel>();
	
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
	
	public ActivateModel getActivate() {
		return activate;
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
