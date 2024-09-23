package org.daisy.common.xproc.calabash.impl;

import java.util.EnumSet;

import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;

import com.google.common.base.Function;
import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.model.Serialization;

/**
 * The Class SerializationUtils holds a collection of functions to help out with the arduous task of serialising xml files.
 */
public class SerializationUtils {

	private static enum SerializationOptions {
		BYTE_ORDER_MARK(
			serial -> Boolean.toString(serial.getByteOrderMark()),
			true),
		DOCTYPE_PUBLIC(
			Serialization::getDoctypePublic,
			false),
		DOCTYPE_SYSTEM(
			Serialization::getDoctypeSystem,
			false),
		ENCODING(
			Serialization::getEncoding,
			false),
		ESCAPE_URI_ATTRIBUTES(
			serial -> Boolean.toString(serial.getEscapeURIAttributes()),
			true),
		INCLUDE_CONTENT_TYPE(
			serial -> Boolean.toString(serial.getIncludeContentType()),
			true),
		INDENT(
			serial -> Boolean.toString(serial.getIndent()),
			true),
		MEDIA_TYPE(
			Serialization::getMediaType,
			false),
		METHOD(
			serial -> serial.getMethod().getLocalName(),
			false),
		NORMALIZATION_FORM(
			Serialization::getNormalizationForm,
			false),
		OMIT_XML_DECLARATION(
			serial -> Boolean.toString(serial.getOmitXMLDeclaration()),
			true),
		STANDALONE(
			Serialization::getStandalone,
			true),
		UNDECLARE_PREFIXES(
			serial -> Boolean.toString(serial.getUndeclarePrefixes()),
			true);

		private final boolean isBoolean;

		private final Function<Serialization,String> fromSerialization;

		private SerializationOptions(Function<Serialization,String> fromSerialization,
		                             boolean isBoolean) {
			this.isBoolean = isBoolean;
			this.fromSerialization = fromSerialization;
		};

		public Property asSaxonProp() {
			return Property.valueOf(name());
		}

		public String getValue(Serialization serial, XProcConfiguration config) {
			String value = serial != null
				? fromSerialization.apply(serial)
				: config.serializationOptions.get(asSaxonProp().getQName().getLocalName());
			if (isBoolean) {
				return Boolean.valueOf(value) ? "yes" : "no";
			} else {
				return value;
			}
		}
	}

	public static Serializer newSerializer(Serialization serialization, XProcConfiguration config) {
		Serializer serializer = config.getProcessor().newSerializer();
		for (SerializationOptions so : EnumSet.allOf(SerializationOptions.class)) {
			serializer.setOutputProperty(so.asSaxonProp(),
			                             so.getValue(serialization, config));
		}
		return serializer;
	}
}
