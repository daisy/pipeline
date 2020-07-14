package org.daisy.common.xproc.calabash;

import java.util.Hashtable;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.StringWithNamespaceContext;

import com.xmlcalabash.model.RuntimeValue;

public class XMLCalabashOptionValue extends StringWithNamespaceContext {

	private final RuntimeValue value;

	public XMLCalabashOptionValue(RuntimeValue value) {
		super(value.getString(), value.getNamespaceBindings());
		this.value = value;
	}

	public static XMLCalabashOptionValue of(InputValue<?> value) {
		if (value instanceof XMLCalabashOptionValue)
			return (XMLCalabashOptionValue)value;
		else if (value instanceof StringWithNamespaceContext)
			return new XMLCalabashOptionValue(
				new RuntimeValue(value.toString(),
				                 null,
				                 new Hashtable<>(((StringWithNamespaceContext)value).getNamespaceBindingsAsMap())));
		else {
			try {
				return new XMLCalabashOptionValue(
					new RuntimeValue(value.asObject(String.class),
					                 null,
					                 new Hashtable<>()));
			} catch (UnsupportedOperationException e) {
				throw new IllegalArgumentException("unsupported interface for option input");
			}
		}
	}

	public RuntimeValue asRuntimeValue() {
		return value;
	}
}
