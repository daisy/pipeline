package org.daisy.common.xproc.calabash.impl;

import java.util.EnumSet;

import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;

import com.google.common.base.Function;
import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.model.Serialization;

// TODO: Auto-generated Javadoc
/**
 * The Class SerializationUtils holds a collection of functions to help out with the arduous task of serialising xml files.
 */
public class SerializationUtils {

	/**
	 * The Enum SerializationOptions.
	 */
	private static enum SerializationOptions {

		/** The BYT e_ orde r_ mark. */
		BYTE_ORDER_MARK(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return Boolean.toString(serial.getByteOrderMark());
			}
		}, true),
 /** The DOCTYP e_ public. */
 DOCTYPE_PUBLIC(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getDoctypePublic();
			}
		}, false),
 /** The DOCTYP e_ system. */
 DOCTYPE_SYSTEM(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getDoctypeSystem();
			}
		}, false),
 /** The ENCODING. */
 ENCODING(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getEncoding();
			}
		}, false),
 /** The ESCAP e_ ur i_ attributes. */
 ESCAPE_URI_ATTRIBUTES(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return Boolean.toString(serial.getEscapeURIAttributes());
			}
		}, true),
 /** The INCLUD e_ conten t_ type. */
 INCLUDE_CONTENT_TYPE(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return Boolean.toString(serial.getIncludeContentType());
			}
		}, true),
 /** The INDENT. */
 INDENT(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return Boolean.toString(serial.getIndent());
			}
		}, true),
 /** The MEDI a_ type. */
 MEDIA_TYPE(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getMediaType();
			}
		}, false),
 /** The METHOD. */
 METHOD(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getMethod().getLocalName();
			}
		}, false),
 /** The NORMALIZATIO n_ form. */
 NORMALIZATION_FORM(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getNormalizationForm();
			}
		}, false),
 /** The OMI t_ xm l_ declaration. */
 OMIT_XML_DECLARATION(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return Boolean.toString(serial.getOmitXMLDeclaration());
			}
		}, true),
 /** The STANDALONE. */
 STANDALONE(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return serial.getStandalone();
			}
		}, true),
 /** The UNDECLAR e_ prefixes. */
 UNDECLARE_PREFIXES(new Function<Serialization, String>() {

			@Override
			public String apply(Serialization serial) {
				return Boolean.toString(serial.getUndeclarePrefixes());
			}
		}, true);

		/** The is boolean. */
		private final boolean isBoolean;

		/** The from serialization. */
		private final Function<Serialization, String> fromSerialization;

		/**
		 * Instantiates a new serialization options
		 *
		 * @param fromSerialization the from serialization
		 * @param isBoolean the is boolean
		 */
		private SerializationOptions(
				Function<Serialization, String> fromSerialization,
				boolean isBoolean) {
			this.isBoolean = isBoolean;
			this.fromSerialization = fromSerialization;
		};

		/**
		 * As saxon prop.
		 *
		 * @return the property
		 */
		public Property asSaxonProp() {
			return Property.valueOf(name());
		}

		/**
		 * Gets the value.
		 *
		 * @param serial the serial
		 * @param config the config
		 * @return the value
		 */
		public String getValue(Serialization serial, XProcConfiguration config) {
			String value = (serial != null) ? fromSerialization.apply(serial)
					: config.serializationOptions.get(asSaxonProp().getQName()
							.getLocalName());
			if (isBoolean) {
				return Boolean.valueOf(value) ? "yes" : "no";
			} else {
				return value;
			}
		}
	}

	/**
	 * New serializer.
	 *
	 * @param serialization the serialization
	 * @param config the config
	 * @return the serializer
	 */
	public static Serializer newSerializer(Serialization serialization,
			XProcConfiguration config) {
		Serializer serializer = config.getProcessor().newSerializer();
		for (SerializationOptions so : EnumSet
				.allOf(SerializationOptions.class)) {
			serializer.setOutputProperty(so.asSaxonProp(),
					so.getValue(serialization, config));
		}
		return serializer;
	}
}
