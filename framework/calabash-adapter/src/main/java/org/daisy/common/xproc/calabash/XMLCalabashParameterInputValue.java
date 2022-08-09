package org.daisy.common.xproc.calabash;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.transform.InputValue;

import com.xmlcalabash.model.RuntimeValue;

public class XMLCalabashParameterInputValue extends InputValue<Map<QName,XMLCalabashOptionValue>> {

	private final Map<net.sf.saxon.s9api.QName,RuntimeValue> runtimeValueMap;

	public XMLCalabashParameterInputValue(Map<net.sf.saxon.s9api.QName,RuntimeValue> value) {
		super(value.entrySet()
		           .stream()
		           .collect(Collectors.toMap(
		                        e -> SaxonHelper.jaxpQName(e.getKey()),
		                        e -> new XMLCalabashOptionValue(e.getValue()))));
		runtimeValueMap = value;
	}

	public static XMLCalabashParameterInputValue of(InputValue<?> value) {
		if (value instanceof XMLCalabashParameterInputValue)
			return (XMLCalabashParameterInputValue)value;
		Map<net.sf.saxon.s9api.QName,RuntimeValue> runtimeValueMap = new HashMap<>();
		Map<?,?> map; {
			try {
				map = value.asObject(Map.class);
			} catch (UnsupportedOperationException e) {
				throw new IllegalArgumentException("unsupported interface for parameter input");
			}
		}
		for (Object k : map.keySet()) {
			QName name; {
				if (!(k instanceof QName))
					throw new IllegalArgumentException("unsupported interface for parameter input");
				name = (QName)k;
			}
			// note that XMLCalabash converts parameter values to strings
			RuntimeValue val; {
				Object v = map.get(k);
				if (!(v instanceof InputValue))
					throw new IllegalArgumentException("unsupported interface for parameter input");
				try {
					val = XMLCalabashOptionValue.of((InputValue<?>)v).asRuntimeValue();
				} catch (UnsupportedOperationException e) {
					throw new IllegalArgumentException("unsupported interface for parameter input");
				}
			}
			runtimeValueMap.put(new net.sf.saxon.s9api.QName(name), val);
		}
		return new XMLCalabashParameterInputValue(runtimeValueMap);
	}

	public Map<net.sf.saxon.s9api.QName,RuntimeValue> asRuntimeValueMap() {
		return runtimeValueMap;
	}
}