package org.daisy.pipeline.fileset;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import static org.daisy.pipeline.file.FileUtils.normalizeURI;
import static org.daisy.pipeline.file.FileUtils.relativizeURI;

public class Fileset {
	private Fileset() {}

	/**
	 * Java counterpart of d:file.
	 *
	 * This class is immutable.
	 */
	public static class File {

		public final URI href;
		public final Optional<String> mediaType;

		private final FileData data;

		/**
		 * @param href shoud be normalized
		 */
		private File(FileData data, URI href, Optional<String> mediaType) {
			this.data = data;
			this.href = href;
			this.mediaType = mediaType;
		}

		/**
		 * @param from For now this must be an absolute, hierarchical URI with a scheme equal to
		 *             "file", a non-empty path component, and undefined authority, query, and
		 *             fragment components.
		 * @throws IllegalArgumentException if the URI does not point to a file on disk.
		 */
		public static File load(URI from, Optional<String> mediaType) {
			return load(from, from, mediaType);
		}

		public static File load(URI from, URI href, Optional<String> mediaType) {
			return load(new java.io.File(from), href, mediaType);
		}

		public static File load(java.io.File from, Optional<String> mediaType) {
			return load(from, from.toURI(), mediaType);
		}

		public static File load(java.io.File from, URI href, Optional<String> mediaType) {
			return new File(new FileDataOnDisk(from), normalizeURI(href), mediaType);
		}

		public static File load(byte[] data, URI href, Optional<String> mediaType) {
			return new File(new FileDataInMemory(data), normalizeURI(href), mediaType);
		}

		public InputStream read() throws IOException {
			return data.read();
		}

		public File copy(URI href) {
			return new File(data, normalizeURI(href), mediaType);
		}

		/**
		 * Store file to disk
		 *
		 * @throws IOException if "href" does not point to a file on disk or if the file can not be
		 *                     written to.
		 */
		public File store() throws IOException {
			java.io.File file; {
				try {
					file = new java.io.File(href);
				} catch (IllegalArgumentException e) {
					throw new IOException("href is not a file URI: " + href);
				}
			}
			return new File(data.store(file), href, mediaType);
		}
	}

	private static abstract class FileData {

		public abstract InputStream read() throws IOException;

		public FileData store(java.io.File destination) throws IOException {
			try (OutputStream os = new FileOutputStream(destination)) {
				try (InputStream is = read()) {
					byte[] bucket = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = is.read(bucket, 0, bucket.length)) != -1) {
						os.write(bucket, 0, bytesRead);
					}
				}
			}
			return new FileDataOnDisk(destination);
		}
	}

	private static class FileDataOnDisk extends FileData {

		private final java.io.File file;

		/**
		 * @param file is read lazily, but we already check whether it exists
		 */
		private FileDataOnDisk(java.io.File file) {
			if (!(file.isFile() && file.exists()))
				throw new IllegalArgumentException(new FileNotFoundException("file does not exist: " + file));
			this.file = file;
		}

		public InputStream read() throws IOException {
			return new FileInputStream(file);
		}

		@Override
		public FileData store(java.io.File destination) throws IOException {
			if (destination.equals(file))
				return this;
			return super.store(destination);
		}
	}

	private static class FileDataInMemory extends FileData {

		private final byte[] data;

		/**
		 * @param data is read lazily
		 */
		private FileDataInMemory(byte[] data) {
			this.data = data;
		}

		public InputStream read() {
			return new ByteArrayInputStream(data);
		}
	}

	public static List<File> unmarshall(BaseURIAwareXMLStreamReader xml) throws XMLStreamException {
		List<File> fileset = new ArrayList<>();
		URI filesetBase = xml.getBaseURI();
		int depth = 0;
	  document: while (true) {
			try {
				int event = xml.next();
				switch (event) {
				case START_DOCUMENT:
					break;
				case END_DOCUMENT:
					break document;
				case START_ELEMENT:
					if (depth == 0 && XMLConstants.D_FILESET.equals(xml.getName())) {
						for (int i = 0; i < xml.getAttributeCount(); i++)
							if (XMLConstants.XML_BASE.equals(xml.getAttributeName(i))) {
								filesetBase = filesetBase.resolve(xml.getAttributeValue(i));
								break;
							}
						depth++;
						break;
					} else if (depth == 1 && XMLConstants.D_FILE.equals(xml.getName())) {
						URI href = null;
						URI originalHref = null;
						String mediaType = null;
						for (int i = 0; i < xml.getAttributeCount(); i++)
							if (XMLConstants._HREF.equals(xml.getAttributeName(i)))
								href = filesetBase.resolve(xml.getAttributeValue(i));
							else if (XMLConstants._ORIGINAL_HREF.equals(xml.getAttributeName(i)))
								originalHref = filesetBase.resolve(xml.getAttributeValue(i));
							else if (XMLConstants._MEDIA_TYPE.equals(xml.getAttributeName(i)))
								mediaType = xml.getAttributeValue(i);
						if (href != null) {
							if (originalHref != null)
								fileset.add(File.load(originalHref, href, Optional.ofNullable(mediaType)));
							else
								fileset.add(File.load(href, Optional.ofNullable(mediaType)));
						}
					}
					{ // consume whole element
						int d = depth + 1;
					  element: while (true) {
							event = xml.next();
							switch (event) {
							case START_ELEMENT:
								d++;
								break;
							case END_ELEMENT:
								d--;
								if (d == depth) break element;
							default:
							}
						}
					}
					break;
				case END_ELEMENT:
					depth--;
					break;
				default:
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		return fileset;
	}

	/**
	 * @param xmlBase if not <code>null</code>, use this to create a <code>xml:base</code> attribute
	 *                and to relativize <code>href</code> attributes against. It is up to the caller
	 *                to set the base URI on the document (see {@link BaseURIAwareXMLStreamWriter}).
	 */
	public static void marshall(XMLStreamWriter xml, URI xmlBase, List<File> fileset) throws XMLStreamException {
		xml.writeStartDocument();
		writeStartElement(xml, XMLConstants.D_FILESET);
		if (xmlBase != null)
			writeAttribute(xml, XMLConstants.XML_BASE, xmlBase.toASCIIString());
		for (File file : fileset) {
			writeStartElement(xml, XMLConstants.D_FILE);
			writeAttribute(xml,
			               XMLConstants._HREF,
			               (xmlBase != null ? relativizeURI(file.href, xmlBase) : file.href).toASCIIString());
			if (file.mediaType.isPresent())
				writeAttribute(xml, XMLConstants._MEDIA_TYPE, file.mediaType.get());
			if (file.data instanceof FileDataOnDisk)
				writeAttribute(xml,
				               XMLConstants._ORIGINAL_HREF,
				               ((FileDataOnDisk)file.data).file.toURI().toASCIIString());
			xml.writeEndElement();
		}
		xml.writeEndElement();
		xml.writeEndDocument();
	}

	public static final class XMLConstants {
		private XMLConstants() {}

		public static final QName D_FILESET = new QName("http://www.daisy.org/ns/pipeline/data", "fileset", "d");
		public static final QName XML_BASE = new QName(javax.xml.XMLConstants.XML_NS_URI, "base", "xml");
		public static final QName D_FILE = new QName("http://www.daisy.org/ns/pipeline/data", "file", "d");
		public static final QName _MEDIA_TYPE = new QName("media-type");
		public static final QName _HREF = new QName("href");
		public static final QName _ORIGINAL_HREF = new QName("original-href");
	}
}
