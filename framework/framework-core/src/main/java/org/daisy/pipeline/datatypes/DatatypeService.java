package org.daisy.pipeline.datatypes;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.daisy.pipeline.datatypes.ValidationResult.notValid;
import static org.daisy.pipeline.datatypes.ValidationResult.valid;

import org.w3c.dom.Document;

public abstract class DatatypeService {

	Supplier<String> id = null; // also accessed from DatatypeRegistry and UrlBasedDatatypeService

	protected DatatypeService() {
		this.id = () -> null;
	}

	protected DatatypeService(String id) {
		this.id = () -> id;
	}

	/**
	 * Get the datatype ID.
	 */
	public final String getId() {
		return id.get();
	}

	/**
	 * Get the XML definition of the datatype.
	 */
	public abstract Document asDocument() throws Exception;

	/**
	 * Test whether a value matches the datatype.
	 */
	public abstract ValidationResult validate(String content);

	public static final DatatypeService XS_STRING = new DatatypeService("string") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"string\"/>");
			}
			public ValidationResult validate(String content) {
				return valid();
			}
		};

	public static final DatatypeService XS_INTEGER = new DatatypeService("integer") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"integer\"/>");
			}
			public ValidationResult validate(String content) {
				try {
					Integer.parseInt(content);
					return valid();
				} catch (NumberFormatException e) {
					return notValid("Not an integer: " + content);
				}
			}
		};

	public static final DatatypeService XS_NON_NEGATIVE_INTEGER = new DatatypeService("nonNegativeInteger") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"nonNegativeInteger\"/>");
			}
			public ValidationResult validate(String content) {
				try {
					int i = Integer.parseInt(content);
					if (i < 0)
						return notValid("Negative integer: " + content);
					return valid();
				} catch (NumberFormatException e) {
					return notValid("Not an integer: " + content);
				}
			}
		};

	public static final DatatypeService XS_BOOLEAN = new DatatypeService("boolean") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"boolean\"/>");
			}
			public ValidationResult validate(String content) {
				if (content != null) {
					String lower = content.toLowerCase();
					if ("true".equals(lower) || "false".equals(lower) || "1".equals(lower) || "0".equals(lower))
						return valid();
				}
				return notValid("Not a boolean: " + content);
			}
		};

	public static final DatatypeService XS_ANY_URI = new DatatypeService("anyURI") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"anyURI\"/>");
			}
			public ValidationResult validate(String content) {
				try {
					new URI(content);
					return valid();
				} catch (URISyntaxException e) {
					return notValid("Not a URI: " + content);
				}
			}
		};

	public static final DatatypeService ANY_FILE_URI = new DatatypeService("anyFileURI") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					"<data type=\"anyFileURI\" datatypeLibrary=\"http://www.daisy.org/ns/pipeline/xproc\"/>");
			}
			/**
			 * Does not check that file exists.
			 */
			public ValidationResult validate(String content) {
				try {
					new File(new URI(content));
					return valid();
				} catch (URISyntaxException e) {
					return notValid("Not a valid URI: " + content);
				} catch (IllegalArgumentException e) {
					return notValid("Not a valid file URI: " + content);
				}
			}
		};

	public static final DatatypeService ANY_DIR_URI = new DatatypeService("anyDirURI") {
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					"<data type=\"anyDirURI\" datatypeLibrary=\"http://www.daisy.org/ns/pipeline/xproc\"/>");
			}
			/**
			 * Does not check that file exists and is a directory.
			 */
			public ValidationResult validate(String content) {
				try {
					new File(new URI(content));
					return valid();
				} catch (URISyntaxException e) {
					return notValid("Not a valid URI: " + content);
				} catch (IllegalArgumentException e) {
					return notValid("Not a valid file URI: " + content);
				}
			}
		};
}
