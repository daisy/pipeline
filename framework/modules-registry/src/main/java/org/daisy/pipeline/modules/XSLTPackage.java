package org.daisy.pipeline.modules;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NoSuchFileException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XSLTPackage extends XSLTResource {

	private String name;
	private String version;

	public XSLTPackage(Module module, String name, String version, String path) throws NoSuchFileException {
		super(module, path);
		this.name = name;
		this.version = version != null
			? version
			: module.getVersion().replaceAll("-SNAPSHOT$", "");
	}

	public XSLTPackage(Module module, String path, XMLInputFactory parser) throws NoSuchFileException, IllegalArgumentException {
		super(module, path);
		getPackageNameAndVersion(getResource(), parser);
		if (version == null)
			version = module.getVersion().replaceAll("-SNAPSHOT$", "");
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	private void getPackageNameAndVersion(URL xsltFile, XMLInputFactory parser) throws IllegalArgumentException {
		try (InputStream is = xsltFile.openStream()) {
			XMLEventReader reader = parser.createXMLEventReader(is);
			try {
				while (reader.hasNext()) {
					XMLEvent event = reader.peek();
					if (event.isStartElement()) {
						StartElement elem = event.asStartElement();
						QName elemName = elem.getName();
						if (!XSL_PACKAGE.equals(elemName))
							throw new IllegalArgumentException(
								"File is not a XSLT package: " + xsltFile + ": found root element " + elemName);
						Attribute name = elem.getAttributeByName(_NAME);
						if (name == null)
							throw new IllegalArgumentException(
								"" + xsltFile + ": Invalid XSLT: missing name attribute on " + elemName);
						this.name = name.getValue();
						Attribute version = elem.getAttributeByName(_PACKAGE_VERSION);
						if (version != null)
							this.version = version.getValue();
						return;
					}
					reader.next();
				}
				throw new IllegalStateException("coding error"); // document without a root element: can not happen
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Couldn't access XSLT file " + xsltFile, e);
		} catch (XMLStreamException e) {
			throw new RuntimeException("Couldn't parse XSLT file " + xsltFile, e);
		}
	}
}
