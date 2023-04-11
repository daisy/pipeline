package org.daisy.pipeline.datatypes;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.daisy.pipeline.datatypes.ValidationResult.notValid;
import static org.daisy.pipeline.datatypes.ValidationResult.valid;

import org.w3c.dom.Document;

public interface DatatypeService {

	/**
	 * Get the datatype ID.
	 */
	public String getId();

	/**
	 * Get the XML definition of the datatype.
	 */
	public Document asDocument() throws Exception;

	/**
	 * Test whether a value matches the datatype.
	 */
	public ValidationResult validate(String content);

	public static final DatatypeService XS_STRING = new DatatypeService() {
			public String getId() {
				return "string";
			}
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"string\"/>");
			}
			public ValidationResult validate(String content) {
				return valid();
			}
		};

	public static final DatatypeService XS_INTEGER = new DatatypeService() {
			public String getId() {
				return "integer";
			}
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

	public static final DatatypeService XS_BOOLEAN = new DatatypeService() {
			public String getId() {
				return "boolean";
			}
			public Document asDocument() throws Exception {
				return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("<data type=\"boolean\"/>");
			}
			public ValidationResult validate(String content) {
				return "true".equals(content) || "false".equals(content) || "1".equals(content) || "0".equals(content)
					? valid()
					: notValid("Not a boolean: " + content);
			}
		};

	public static final DatatypeService XS_ANY_URI = new DatatypeService() {
			public String getId() {
				return "anyURI";
			}
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

	public static final DatatypeService ANY_FILE_URI = new DatatypeService() {
			public String getId() {
				return "anyFileURI";
			}
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

	public static final DatatypeService ANY_DIR_URI = new DatatypeService() {
			public String getId() {
				return "anyDirURI";
			}
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
