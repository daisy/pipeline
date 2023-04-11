package org.daisy.pipeline.script.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Predicate;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.common.stax.EventProcessor;
import org.daisy.common.stax.StaxEventHelper;
import org.daisy.common.stax.StaxEventHelper.EventPredicates;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.script.impl.XProcScriptConstants.Attributes;
import org.daisy.pipeline.script.impl.XProcScriptConstants.Elements;
import org.daisy.pipeline.script.impl.XProcScriptConstants.Values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Parses the XProc file extracting the metadata and buiding the {@link Script} object
 */
@Component(
	name = "script-parser",
	service = { StaxXProcScriptParser.class }
)
public class StaxXProcScriptParser {

	private static final Logger logger = LoggerFactory.getLogger(StaxXProcScriptParser.class);
	private XMLInputFactory xmlInputFactory;
	private DatatypeRegistry datatypeRegistry;

	@Reference(
		name = "xml-input-factory",
		unbind = "-",
		service = XMLInputFactory.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setFactory(XMLInputFactory factory) {
		xmlInputFactory = factory;
	}

	@Reference(
		name = "datatype-registry",
		unbind = "-",
		service = DatatypeRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setDatatypeRegistry(DatatypeRegistry registry) {
		datatypeRegistry = registry;
	}

	@Activate
	protected void activate() {
		logger.trace("Activating XProc script parser");
	}

	/**
	 * Parses the XProc file.
	 */
	public XProcScript parse(final XProcScriptService descriptor) {
		return new StatefulParser().parse(descriptor);
	}

	/**
	 * StatefulParser makes the parsing process thread safe.
	 */
	private class StatefulParser {

		private final LinkedList<XMLEvent> mAncestors = new LinkedList<XMLEvent>();
		private final LinkedList<XProcPortMetadataBuilder> inputPortBuilders = new LinkedList<XProcPortMetadataBuilder>();
		private final LinkedList<XProcPortMetadataBuilder> outputPortBuilders = new LinkedList<XProcPortMetadataBuilder>();
		private final LinkedList<XProcOptionMetadataBuilder> optionBuilders = new LinkedList<XProcOptionMetadataBuilder>();
		private XProcScript.Builder scriptBuilder;

		/**
		 * Parses the XProc file extracting the metadata attached to options, ports and the step.
		 */
		public XProcScript parse(final XProcScriptService descriptor) {
			if (xmlInputFactory == null) {
				throw new IllegalStateException();
			}
			InputStream is = null;
			XMLEventReader reader = null;
			logger.debug("Parsing with descriptor:" + descriptor);
			StaxXProcPipelineInfoParser infoParser = new StaxXProcPipelineInfoParser();
			infoParser.setFactory(xmlInputFactory);
			try {
				XProcPipelineInfo info = infoParser.parse(descriptor.getURL());
				scriptBuilder = new XProcScript.Builder(descriptor, info.getURI(), datatypeRegistry);
				URL descUrl = descriptor.getURL();
				is = descUrl.openConnection().getInputStream();
				reader = xmlInputFactory.createXMLEventReader(is);

				parseStep(reader);
				for (XProcOptionMetadataBuilder b : optionBuilders) {
					scriptBuilder.withOption(info.getOption(b.name), b.build());
				}
				for (XProcPortMetadataBuilder b : inputPortBuilders) {
					if (info.getInputPort(b.name) == null)
						; // parameter port
					else
						scriptBuilder.withInputPort(info.getInputPort(b.name), b.build());
				}
				for (XProcPortMetadataBuilder b : outputPortBuilders) {
					scriptBuilder.withOutputPort(info.getOutputPort(b.name), b.build());
				}

			} catch (XMLStreamException e) {
				throw new RuntimeException("Parsing error: " + e.getMessage(),
						e);
			} catch (IOException e) {
				throw new RuntimeException(
						"Couldn't access package descriptor: " + e.getMessage(),
						e);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
					// ignore;
				}
			}
			return scriptBuilder.build();
		}

		/**
		 * Checks if is first child.
		 * 
		 * @return true, if is first child
		 */
		private boolean isFirstChild() {
			return mAncestors.size() == 2;
		}

		/**
		 * Returns the name of the current element's parent.
		 * 
		 * @return the QName of the parent
		 */
		private QName getParentName() {
			return mAncestors.get(mAncestors.size() - 2).asStartElement()
					.getName();
		}

		/**
		 * Reads the next element
		 * 
		 * @param reader
		 *            the reader
		 * @return the xML event
		 * @throws XMLStreamException
		 *             the xML stream exception
		 */
		private XMLEvent readNext(XMLEventReader reader)
				throws XMLStreamException {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				mAncestors.add(event);
			} else if (event.isEndElement()) {
				mAncestors.pollLast();
			}
			return event;
		}

		/**
		 * Parses the step.
		 * 
		 * @param reader
		 *            the reader
		 * @throws XMLStreamException
		 *             the xML stream exception
		 */
		public void parseStep(final XMLEventReader reader)
				throws XMLStreamException {
			while (reader.hasNext()) {
				XMLEvent event = readNext(reader);
				if (event.isStartElement()
						&& event.asStartElement().getName()
								.equals(Elements.P_DECLARE_STEP)) {
					parseFilesets(event.asStartElement());
				} else if (event.isStartElement()
						&& event.asStartElement().getName()
								.equals(Elements.P_DOCUMENTATION)) {
					DocumentationHolder dHolder = new DocumentationHolder();
					parseDocumentation(reader, dHolder);
					if (isFirstChild()) {
						scriptBuilder.withDescription(dHolder.description);
						scriptBuilder.withShortName(dHolder.shortName);
						scriptBuilder.withHomepage(dHolder.homepage);
					} else if (this.getParentName().equals(Elements.P_INPUT)) {
						inputPortBuilders.peekLast().description = dHolder.description;
						inputPortBuilders.peekLast().niceName = dHolder.shortName;
					} else if (this.getParentName().equals(Elements.P_OUTPUT)) {
						outputPortBuilders.peekLast().description = dHolder.description;
						outputPortBuilders.peekLast().niceName = dHolder.shortName;
					} else if (this.getParentName().equals(Elements.P_OPTION)) {
						optionBuilders.peekLast().description = dHolder.description;
						optionBuilders.peekLast().niceName = dHolder.shortName;
					}
				} else if (isFirstChild()
						&& event.isStartElement()
						&& event.asStartElement().getName().equals(Elements.P_INPUT)) {
					Attribute name = event.asStartElement().getAttributeByName(new QName("port"));
					XProcPortMetadataBuilder b = new XProcPortMetadataBuilder(name.getValue());
					inputPortBuilders.add(b);
					parsePort(event.asStartElement(), b);
				} else if (isFirstChild()
						&& event.isStartElement()
						&& event.asStartElement().getName().equals(Elements.P_OUTPUT)) {
					Attribute name = event.asStartElement().getAttributeByName(new QName("port"));
					XProcPortMetadataBuilder b = new XProcPortMetadataBuilder(name.getValue());
					outputPortBuilders.add(b);
					parsePort(event.asStartElement(), b);
				} else if (isFirstChild()
						&& event.isStartElement()
						&& event.asStartElement().getName().equals(Elements.P_OPTION)) {
					Attribute hiddenAttr = event.asStartElement().getAttributeByName(Attributes.PX_HIDDEN);
					if (hiddenAttr == null || !Values.TRUE.equals(hiddenAttr.getValue())) {
						Attribute nameAttr = event.asStartElement().getAttributeByName(
							XProcScriptConstants.Attributes.NAME);
						if (nameAttr != null) {
							String name = nameAttr.getValue();
							QName qname; {
								if (name.contains(":")) {
									String prefix = name.substring(0, name.indexOf(":"));
									String namespace = event.asStartElement().getNamespaceURI(prefix);
									String localPart = name.substring(prefix.length() + 1, name.length());
									qname = new QName(namespace, localPart, prefix);
								} else {
									qname = new QName(name);
								}
							}
							XProcOptionMetadataBuilder b = new XProcOptionMetadataBuilder(qname);
							parseOption(event.asStartElement(), b);
							optionBuilders.add(b);
						}
					}
				}
			}
		}

		protected void parseFilesets(final StartElement declareStep)
				throws XMLStreamException {
			Attribute inputs = declareStep
					.getAttributeByName(XProcScriptConstants.Attributes.PX_INPUT_FILESETS);

			Attribute outputs = declareStep
					.getAttributeByName(XProcScriptConstants.Attributes.PX_OUTPUT_FILESETS);
			if (inputs != null) {
				String splitInputs[] = inputs.getValue().split("\\s+");
				for (String fset : splitInputs) {
					this.scriptBuilder.withInputFileset(fset);
				}
			}

			if (outputs != null) {
				String splitOutputs[] = outputs.getValue().split("\\s+");
				for (String fset : splitOutputs) {
					this.scriptBuilder.withOutputFileset(fset);
				}
			}

		}

		/**
		 * Parses the option.
		 * 
		 * @param optionElement
		 *            the option element
		 * @param optionBuilder
		 *            the option builder
		 * @throws XMLStreamException
		 *             the xML stream exception
		 */
		protected void parseOption(final StartElement optionElement,
				final XProcOptionMetadataBuilder optionBuilder)
				throws XMLStreamException {

			Attribute type = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_TYPE);
			/*
			 * Attribute dir = optionElement
			 * .getAttributeByName(XProcScriptConstants.Attributes.PX_DIR);
			 */
			Attribute mediaType = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_MEDIA_TYPE);
			Attribute output = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_OUTPUT);
			Attribute sequence = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_SEQUENCE);
			Attribute ordered = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_ORDERED);
			Attribute separator = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_SEPARATOR);
			Attribute primary = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_PRIMARY);

			if (mediaType != null) {
				optionBuilder.mediaType = mediaType.getValue();
			}
			if (type != null) {
				optionBuilder.type = type.getValue();
			}
			/*
			 * if (dir != null) { optionBuilder.withDirection(dir.getValue()); }
			 */
			if (output != null) {
				try {
					optionBuilder.output = XProcOptionMetadata.Output.valueOf(output.getValue().toUpperCase());
				} catch (IllegalArgumentException e) {
				}
			}
			if (sequence != null) {
				optionBuilder.sequence = sequence.getValue().equalsIgnoreCase("true");
			}
			if (ordered != null) {
				optionBuilder.ordered = ordered.getValue().equalsIgnoreCase("true");
			}
			if (separator != null && !separator.getValue().isEmpty()) {
				optionBuilder.separator = separator.getValue();
			}
			if (primary != null) {
				optionBuilder.primary = !primary.getValue().equalsIgnoreCase("false");
			}
		}

		/**
		 * Parses the documentation.
		 * 
		 * @param reader
		 *            the reader
		 * @param dHolder
		 *            the d holder
		 * @return the documentation holder
		 * @throws XMLStreamException
		 *             the xML stream exception
		 */
		private DocumentationHolder parseDocumentation(
				final XMLEventReader reader, final DocumentationHolder dHolder)
				throws XMLStreamException {

			Predicate<XMLEvent> pred =
				EventPredicates.IS_START_ELEMENT.or(
				EventPredicates.IS_END_ELEMENT);

			// the tricky thing here is that you have to ignore blocks of markup
			// for px:role="author" and px:role="maintainer"
			StaxEventHelper.loop(reader, pred,
					EventPredicates.getChildOrSiblingPredicate(),
					new EventProcessor() {

						// keep track of when we enter and exit a block that we
						// want to ignore with these vars
						boolean processChildren = true;
						int elemsToIgnore = 0;

						@Override
						public void process(XMLEvent event)
								throws XMLStreamException {

							if (event.isStartElement()) {

								// in cases where we need to ignore the child
								// elements, just keep count of how many open
								// elements we're seeing so that we can note
								// when they have been closed
								if (processChildren == false
										&& elemsToIgnore > 0) {
									elemsToIgnore++;
								}
								// we got past the block we wanted to skip
								else if (elemsToIgnore == 0) {
									processChildren = true;
								}

								if (processChildren) {
									StartElement elm = event.asStartElement();
									Attribute attr = elm
											.getAttributeByName(Attributes.PX_ROLE);
									String role = (attr != null ? attr
											.getValue() : "");
                                    attr = elm.getAttributeByName(Attributes.XML_SPACE);
                                    String xmlSpace = (attr != null ? attr.getValue() : "default");

									// ignore blocks of author and maintainer
									// data
									if (role.contains(Values.AUTHOR)
											|| role.contains(Values.MAINTAINER)) {
										elemsToIgnore++;
										processChildren = false;
										return;
									}
                                    
                                    if (role.equals(Values.NAME)) {
										reader.next();
                                        String data = reader.peek().asCharacters().getData();
                                        if (!"preserve".equals(xmlSpace)) {
                                            data = data.replaceAll("\\s+"," ");
                                        }
                                        dHolder.shortName = data;
									}

									else if (role.equals(Values.DESC)) {
										reader.next();
                                        String data = reader.peek().asCharacters().getData();
                                        if (!"preserve".equals(xmlSpace)) {
                                            data = data.replaceAll("\\s+"," ");
                                        }
										dHolder.description = data;
									} else if (role.equals(Values.HOMEPAGE)) {
										reader.next();
                                        String data = reader.peek().asCharacters().getData();
                                        if (!"preserve".equals(xmlSpace)) {
                                            data = data.replaceAll("\\s+"," ");
                                        }
										// if @href is present, use that
										if (elm.getAttributeByName(Attributes.HREF) != null) {
											dHolder.homepage = elm
													.getAttributeByName(
															Attributes.HREF)
													.getValue().replaceAll("\\s+"," ");
										}
										// otherwise just use the text contents
										else {
											dHolder.homepage = data;
										}
									}

								}
							} else if (event.isEndElement()) {

								if (processChildren == false) {
									elemsToIgnore--;
								}
							}
						}
					});
			return dHolder;
		}

		/**
		 * Parses the port.
		 * 
		 * @param portElement
		 *            the port element
		 * @param portBuilder
		 *            the port builder
		 * @throws XMLStreamException
		 *             the xML stream exception
		 */
		private void parsePort(final StartElement portElement, final XProcPortMetadataBuilder portBuilder)
				throws XMLStreamException {

			Attribute mediaType = portElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_MEDIA_TYPE);
			if (mediaType != null) {
				portBuilder.mediaType = mediaType.getValue();
			}
		}
	}

	/**
	 * DocumentationHolde holds documentation elements
	 */
	private static class DocumentationHolder {

		String shortName = null;
		String description = null;
		String homepage = null;

	}

	/**
	 * {@link XProcOptionMetadata} builder.
	 */
	private static class XProcOptionMetadataBuilder {

		final QName name;

		XProcOptionMetadataBuilder(QName name) {
			this.name = name;
		}

		String niceName = null;
		String description = null;
		String type = null;
		String mediaType = null;
		XProcOptionMetadata.Output output = XProcOptionMetadata.Output.NA;
		boolean primary = true;
		boolean sequence = false;
		boolean ordered = true;
		String separator = XProcOptionMetadata.DEFAULT_SEPARATOR;

		XProcOptionMetadata build() {
			return new XProcOptionMetadata(niceName, description, type, mediaType,
			                               output, primary,
			                               sequence, ordered, separator);
		}
	}

	/**
	 * {@link XProcPortMetadata} builder.
	 */
	private static class XProcPortMetadataBuilder {

		final String name;

		XProcPortMetadataBuilder(String name) {
			this.name = name;
		}

		String niceName;
		String description;
		String mediaType;

		XProcPortMetadata build() {
			return new XProcPortMetadata(niceName, description, mediaType);
		}
	}
}
