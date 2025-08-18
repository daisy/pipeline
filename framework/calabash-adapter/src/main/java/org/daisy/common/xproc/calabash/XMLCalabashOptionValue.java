package org.daisy.common.xproc.calabash;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.StringWithNamespaceContext;

import com.google.common.collect.ImmutableList;

import com.xmlcalabash.model.RuntimeValue;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmExternalObject;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public interface XMLCalabashOptionValue {

	public static XMLCalabashOptionValue of(RuntimeValue value) throws IllegalArgumentException {
		if (value.hasGeneralValue()) {
			XdmValue v = value.getValue();
			if (v.size() == 1)
				v = v.itemAt(0);
			if (v instanceof XdmAtomicValue) {
				Object o = ((XdmAtomicValue)v).getValue();
				if (o instanceof String)
					return new XMLCalabashStringOptionValue((String)o, value.getNamespaceBindings());
				else
					return new XMLCalabashGeneralOptionValue(v, o);
			} else if (v instanceof XdmExternalObject) {
				return new XMLCalabashGeneralOptionValue(v, ((XdmExternalObject)v).getExternalObject());
			} else {
				try {
					return new XMLCalabashXMLOptionValue(v);
				} catch (IllegalArgumentException e) {
				}
				// simply wrap RuntimeValue in a InputValue, without possibility to access the value as a Java object
				try {
					return new XMLCalabashGeneralOptionValue(v, (Void)null);
				} catch (IllegalArgumentException e) {
				}
			}
		} else
			return new XMLCalabashStringOptionValue(value.getString(), value.getNamespaceBindings());
		throw new IllegalArgumentException("can not create XMLCalabashOptionValue from " + value);
	}

	public static XMLCalabashOptionValue of(InputValue<?> value) throws IllegalArgumentException, NoSuchElementException {
		if (value instanceof XMLCalabashOptionValue)
			return (XMLCalabashOptionValue)value;
		else if (value instanceof StringWithNamespaceContext)
			return new XMLCalabashStringOptionValue((StringWithNamespaceContext)value);
		else if (value instanceof SaxonInputValue)
			return new XMLCalabashXMLOptionValue((SaxonInputValue)value);
		else
			try {
				return new XMLCalabashGeneralOptionValue(value);
			} catch (UnsupportedOperationException e) {
			} catch (IllegalArgumentException e) {
			}
		throw new IllegalArgumentException("unsupported interface for option input: " + value);
	}

	public RuntimeValue asRuntimeValue();

	public static class XMLCalabashStringOptionValue extends StringWithNamespaceContext implements XMLCalabashOptionValue {

		private final RuntimeValue value;

		protected XMLCalabashStringOptionValue(StringWithNamespaceContext value) {
			this(value.toString(), value.getNamespaceBindingsAsMap());
		}

		protected XMLCalabashStringOptionValue(String value, Map<String,String> bindings) {
			super(value, bindings);
			this.value = new RuntimeValue(value,
			                              null,
			                              bindings != null ? new Hashtable<>(bindings) : new Hashtable<>()) {
				public URI getBaseURI() { // used e.g. in XSLT and would otherwise result in NPE
					return URI.create("");
				}
			};
		}

		public RuntimeValue asRuntimeValue() {
			return value;
		}
	}

	public static class XMLCalabashXMLOptionValue extends SaxonInputValue implements XMLCalabashOptionValue {

		private final RuntimeValue value;

		protected XMLCalabashXMLOptionValue(SaxonInputValue value) throws NoSuchElementException {
			this(new XdmValue(ImmutableList.copyOf(value.asXdmItemIterator())));
		}

		protected XMLCalabashXMLOptionValue(XdmValue value) throws IllegalArgumentException {
			super(value);
			String stringValue = ""; {
				for (XdmItem item : value)
					if (item instanceof XdmNode)
						stringValue += item.getStringValue();
					else
						throw new IllegalArgumentException("expected a node sequence"); }
			this.value = new RuntimeValue(stringValue,
			                              value,
			                              null,
			                              new Hashtable<>()) {
				public URI getBaseURI() { // used e.g. in XSLT and would otherwise result in NPE
					return URI.create("");
				}
			};
		}

		public RuntimeValue asRuntimeValue() {
			return value;
		}
	}

	public static class XMLCalabashGeneralOptionValue<V> extends InputValue<V> implements XMLCalabashOptionValue {

		private final RuntimeValue value;

		protected XMLCalabashGeneralOptionValue(InputValue<V> value) throws UnsupportedOperationException, NoSuchElementException {
			this(value.asObject());
		}

		protected XMLCalabashGeneralOptionValue(V value) {
			this(SaxonHelper.xdmValueFromObject(value), value);
		}

		protected XMLCalabashGeneralOptionValue(XdmValue value, V object) {
			super(object);
			this.value = new RuntimeValue(object == null ? "" : object.toString(),
			                              value,
			                              null,
			                              new Hashtable<>()) {
				public URI getBaseURI() { // used e.g. in XSLT and would otherwise result in NPE
					return URI.create("");
				}
			};
		}

		public RuntimeValue asRuntimeValue() {
			return value;
		}
	}
}
