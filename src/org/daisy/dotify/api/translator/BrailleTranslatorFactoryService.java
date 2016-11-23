package org.daisy.dotify.api.translator;

import java.util.Collection;

/**
 * <p>
 * Provides an interface for a BrailleTranslatorFactory service. The purpose of
 * this interface is to expose an implementation of a BrailleTranslatorFactory as
 * a service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 */
public interface BrailleTranslatorFactoryService {

	/**
	 * Returns true if the translator factory supports the given specification.
	 * 
	 * @param locale
	 *            the translator locale
	 * @param mode
	 *            the translator grade, or null for uncontracted braille
	 * @return returns true if the translator factory supports the specification
	 */
	public boolean supportsSpecification(String locale, String mode);
	
	/**
	 * Returns a list of supported specifications.
	 * @return returns a list of specifications
	 */
	public Collection<TranslatorSpecification> listSpecifications();

	/**
	 * Creates a new braille translator factory.
	 * @return returns a new braille translator factory
	 */
	public BrailleTranslatorFactory newFactory();

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
