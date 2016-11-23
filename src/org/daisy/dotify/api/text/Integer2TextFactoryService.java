package org.daisy.dotify.api.text;

import java.util.Collection;


/**
 * <p>
 * Provides an interface for a Integer2TextFactory service. The purpose of this
 * interface is to expose an implementation of a Integer2TextFactory as a
 * service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 * 
 */
public interface Integer2TextFactoryService {

	/**
	 * Returns true if this instance can create instances for the specified
	 * locale.
	 * 
	 * @param locale
	 *            a valid locale as defined by IETF RFC 3066
	 * @return returns true if the specified locale is supported, false
	 *         otherwise
	 */
	public boolean supportsLocale(String locale);
	
	/**
	 * Returns a list of supported locales as defined by IETF RFC 3066.
	 * @return returns a list of locales
	 */
	public Collection<String> listLocales();

	/**
	 * Creates a new integer to text factory.
	 * @return returns a new factory
	 */
	public Integer2TextFactory newFactory();

	/**
	 * <p>Informs the implementation that it was discovered and instantiated using
	 * information collected from a file within the <tt>META-INF/services</tt> directory.
	 * In other words, it was created using SPI (service provider interfaces).</p>
	 * 
	 * <p>This information, in turn, enables the implementation to use the same mechanism
	 * to set dependencies as needed.</p>
	 * 
	 * <p>If this information is <strong>not</strong> given, an implementation
	 * should avoid using SPIs and instead use
	 * <a href="http://wiki.osgi.org/wiki/Declarative_Services">declarative services</a>
	 * for dependency injection as specified by OSGi. Note that this also applies to
	 * several newInstance() methods in the Java API.</p>
	 * 
	 * <p>The class that created an instance with SPI must call this method before
	 * putting it to use.</p>
	 */
	public default void setCreatedWithSPI(){}

}