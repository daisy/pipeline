/**
 * <p>
 * Provides input implementations. Dotify comes with a flexible input format system that 
 * can be extended to support any xml-format (this package). The XML input format system 
 * manages the conversion from any xml-format to OBFL.</p>
 * 
 * <p>The decision tree includes many configurable parts. When running Dotify, one of the 
 * required parameters is context locale. The context locale is the locale in which the 
 * output is to be consumed.</p>
 * 
 * <h3>Localization catalog</h3>
 * <p>The first configurable part of the input system is the localization_catalog.xml
 * which is scanned for supported locales, for example 'en-US'. It connects a locale 
 * with a base folder for the input system. This folder will be used when looking for
 * file references for that locale. Note that:</p>
 * <ul><li>The base folder in the localization catalog is relative to the 
 * <tt>XMLInputManagerFactory</tt> class.</li>
 * <li><b>An entry in the localization catalog is 
 * required for a locale to be supported.</b> However, no files are required to be localized.</li>
 * </ul>
 * 
 * <p>In addition to the base folder supplied in the localization catalog, there is a 
 * fallback base folder called "common" which is used if the input system fails
 * to locate a resource in the base folder of the locale.</p>
 * 
 * <h3>localization.xml</h3>
 * <p><del>Parameters of the input format conversion can be stored as presets, these 
 * can be placed in the "config" implementation package. See this package for more
 * information. Content localization should be stored independently of the
 * presets. A mechanism for supplying content localization exists in this package.
 * The localization data can therefore be stored separately from the input transformation
 * data. Since each locale has its own input transformation, this may seem redundant,
 * however it could be useful if there are a lot of formats to manage and the
 * localization information must be updated or if a fallback input transformation is 
 * used. Therefore, a great deal of care must be taken to ensure that the same keys 
 * are used in all input transformations in this package.</del></p>
 * 
 * <h3>Template selection</h3>
 * <p>The process of interpreting an input file format as OBFL is managed by a properties 
 * file which must be named as indicated by the input_format_catalog.xml. The input format
 * catalog connects a root element and namespace with a filename. The location of this 
 * file is determined by the template. In other words, the properties file should be located
 *  in the "templates/[template]" folder of the localization base folder. The properties 
 * file should contain two entries: transformation and validation, pointing to the xslt 
 * and schema required by the input format.</p>
 *  
 * <h3>Conversions of DTBook</h3>
 * <ul>
 * <li><a href=
 * "../../../../../../resources/org.daisy.dotify.impl.input.xml.resource-files.common.xslt-files.dtbook2obfl_base.html"
 * >dtbook2obfl_base.xsl</a></li>
 * <li><a href=
 * "../../../../../../resources/org.daisy.dotify.impl.input.xml.resource-files.common.xslt-files.xml2flow.html"
 * >xml2flow.xsl</a></li>
 * <li><a href=
 * "../../../../../../resources/org.daisy.dotify.impl.input.xml.resource-files.common.xslt-files.dtbook2obfl_layout.html"
 * >dtbook2obfl_layout.xsl</a></li>
 * <li><a href=
 * "../../../../../../resources/org.daisy.dotify.impl.input.xml.resource-files.common.xslt-files.dtbook2obfl_braille.html"
 * >dtbook2obfl_braille.xsl</a></li>
 * <li><a href=
 * "../../../../../../resources/org.daisy.dotify.impl.input.xml.resource-files.common.xslt-files.dtbook2obfl_text.html"
 * >dtbook2obfl_text.xsl</a></li>
 * </ul>
 * <h3>Adding an input format conversion</h3>
 * <p>
 * Detecting an XML input format is easy, thanks to the XMLInputManager. It is
 * specifically designed to inject the correct validation rules and XSLT
 * stylesheet for any XML-format and locale combination into the task chain.
 * </p>
 * <p>
 * Adding a new format involves the following:
 * </p>
 * <ol>
 * <li>Modify the input_format_catalog.xml</li>
 * <li>Add a selector file in the folder hierarchy</li>
 * <li>Add the desired validation rules and style sheets in folder hierarchy</li>
 * </ol>
 * 
 * <p>
 * 1. Modifying the input format catalog involves adding an entry for the new
 * format. The key must be the root element of the format followed by '@' and
 * the namespace of the root element. The value for this key should be a short
 * but descriptive filename for the format. Typically, the root element followed
 * by '.properties' will suffice as filename. However, the filename must be
 * unique throughout the file, so another name may have to be chosen.
 * </p>
 * 
 * <p>
 * 2. Add a selector file into the folder hierarchy in the appropriate location
 * of the folder hierarchy. The selector file should
 * contain two entries: a path to the validation file and a path to the
 * transformation file (relative to the root folder of the locale).
 * </p>
 * 
 * <p>
 * 3. Add the desired validation and stylesheets files to the folder hierarchy
 * as indicated by the selector file. If there already exists an XSLT for the 
 * input format for another locale, the stylesheet could be copied into the desired
 * locale's directory and then modified. It is not recommended to extend another
 * locale's stylesheets.</p>
 * 
 * <p><b>IMPORTANT: This package contains implementations that should only be 
 * accessed using the Java Services API. Additional classes in this package 
 * should only be used by these implementations. This package is not part of the 
 * public API.</b>
 * </p>
 * @author Joel Håkansson
 */
package org.daisy.dotify.impl.input.xml;