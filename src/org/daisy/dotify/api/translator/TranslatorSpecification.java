package org.daisy.dotify.api.translator;

/**
 * Provides a translator specification.
 * 
 * @author Joel Håkansson
 */
public class TranslatorSpecification implements Comparable<TranslatorSpecification> {
	private final String locale; 
	private final String mode;
	
	/**
	 * Creates a new translator specification with the supplied values.
	 * 
	 * @param locale a locale as defined by IETF RFC 3066
	 * @param mode a translator mode
	 * @throws IllegalArgumentException if null/empty values are inserted
	 */
	public TranslatorSpecification(String locale, String mode) {
		if (locale==null || "".equals(locale)) {
			throw new IllegalArgumentException("Null/empty locale not allowed");
		}
		if (mode==null || "".equals(mode)) {
			throw new IllegalArgumentException("Null/empty mode not allowed");
		}
		this.locale = locale;
		this.mode = mode;
	}

	/**
	 * Gets the locale.
	 * @return returns the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * Gets the translator mode.
	 * @return returns the translator mode
	 */
	public String getMode() {
		return mode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TranslatorSpecification other = (TranslatorSpecification) obj;
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		if (mode == null) {
			if (other.mode != null) {
				return false;
			}
		} else if (!mode.equals(other.mode)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(TranslatorSpecification o) {
		int l = this.locale.compareTo(o.locale);
		if (l!=0) {
			return l;
		} else {
			return this.mode.compareTo(o.mode);
		}
	}

}