package org.daisy.common.xproc.calabash;

import java.util.Hashtable;

import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.StringWithNamespaceContext;

import com.google.common.collect.ImmutableList;

import com.xmlcalabash.model.RuntimeValue;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

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
		else if (value instanceof SaxonInputValue) {
			XdmValue xdmValue = new XdmValue(ImmutableList.copyOf(((SaxonInputValue)value).asXdmItemIterator()));
			String stringValue = ""; {
				for (XdmItem item : xdmValue)
					stringValue += item.getStringValue();
			}
			return new XMLCalabashOptionValue(
				new RuntimeValue(stringValue,
				                 xdmValue,
				                 null,
				                 new Hashtable<>()));
		} else {
			try {
				Object object = value.asObject();
				if (object instanceof String)
					return new XMLCalabashOptionValue(
						new RuntimeValue(object.toString(),
						                 new XdmAtomicValue((String)object),
						                 null,
						                 new Hashtable<>()));
				else if (object instanceof Integer)
					return new XMLCalabashOptionValue(
						new RuntimeValue(object.toString(),
						                 new XdmAtomicValue((Integer)object),
						                 null,
						                 new Hashtable<>()));
				else if (object instanceof Boolean)
					return new XMLCalabashOptionValue(
						new RuntimeValue(object.toString(),
						                 new XdmAtomicValue((Boolean)object),
						                 null,
						                 new Hashtable<>()));
			} catch (UnsupportedOperationException e) {
			}
		}
		throw new IllegalArgumentException("unsupported interface for option input");
	}

	public RuntimeValue asRuntimeValue() {
		return value;
	}
}
