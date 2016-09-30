package org.daisy.dotify.impl.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.Integer2TextConfigurationException;
import org.daisy.dotify.api.text.Integer2TextFactory;

class Integer2TextFactoryImpl implements Integer2TextFactory {
	/**
	 * Use of lower case is allowed here. The enum name is used for populating the map and list below,
	 * so the name should be equal to the language code for the implementation with '-' replaced by '_'.
	 * '_' is subsequently replaced by '-' in the map and list.
	 */
	private enum Implementation {
		//sv		(SvInt2TextLocalization.class),
		//sv_SE	(SvInt2TextLocalization.class),
		fi		(FiInt2TextLocalization.class),
		fi_FI	(FiInt2TextLocalization.class),
		no		(NoInt2TextLocalization.class),
		no_NO	(NoInt2TextLocalization.class),
		nb		(NoInt2TextLocalization.class),
		nb_NO	(NoInt2TextLocalization.class),
		nn		(NoInt2TextLocalization.class),
		nn_NO	(NoInt2TextLocalization.class);
		final Class<? extends Integer2Text> clazz;

		Implementation(Class<? extends Integer2Text> clazz) {
			this.clazz = clazz;
		}
	}
	final static Map<String, Class<? extends Integer2Text>> locales;
	final static List<String> displayNames;
	static {
		List<String> _localeNames = new ArrayList<>();
		Map<String, Class<? extends Integer2Text>> _locales = new HashMap<>();
		for (Implementation impl : Implementation.values()) {
			//Only use lower case keys
			String name = impl.name().replace('_', '-');
			_locales.put(name.toLowerCase(Locale.ENGLISH), impl.clazz);
			_localeNames.add(name);
		}
		locales = Collections.unmodifiableMap(_locales);
		displayNames = Collections.unmodifiableList(_localeNames);
	}

	@Override
	public Integer2Text newInteger2Text(String locale) throws Integer2TextConfigurationException {
		try {
			Class<? extends Integer2Text> c = locales.get(locale.toLowerCase(Locale.ENGLISH));
			if (c==null) {
				throw new Integer2TextConfigurationExceptionImpl("Locale not supported.");
			}
			return c.newInstance();
		} catch (InstantiationException e) {
			throw new Integer2TextConfigurationExceptionImpl(e);
		} catch (IllegalAccessException e) {
			throw new Integer2TextConfigurationExceptionImpl(e);
		}
	}

	@Override
	public Object getFeature(String key) {
		return null;
	}

	@Override
	public void setFeature(String key, Object value) throws Integer2TextConfigurationException {
		throw new Integer2TextConfigurationExceptionImpl();
	}
	
	private class Integer2TextConfigurationExceptionImpl extends Integer2TextConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1129385990516203885L;

		private Integer2TextConfigurationExceptionImpl() {
			super();
		}

		private Integer2TextConfigurationExceptionImpl(String message) {
			super(message);
		}

		private Integer2TextConfigurationExceptionImpl(Throwable cause) {
			super(cause);
		}

		
	}

}
