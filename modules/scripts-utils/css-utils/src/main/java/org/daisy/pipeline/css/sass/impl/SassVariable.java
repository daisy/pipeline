package org.daisy.pipeline.css.sass.impl;

import java.util.regex.Pattern;

import com.google.common.base.CaseFormat;

import org.daisy.pipeline.css.CssAnalyzer;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.XMLBasedDatatypeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.unbescape.css.CssEscape;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

class SassVariable implements CssAnalyzer.SassVariable {

	private final static Logger logger = LoggerFactory.getLogger(SassVariable.class.getName());

	private final String name;
	private final String nicename;
	private final String description;
	private final String value;
	private boolean isDefault = true;
	private final DatatypeService type;

	/**
	 * Create a fixed variable.
	 */
	SassVariable(String name, String value) {
		this(name, null, value, null);
		isDefault = false;
	}

	/**
	 * Create a writable variable (with a {@code !default} suffix).
	 */
	SassVariable(String name, Comment precedingComment, String defaultValue, DatatypeRegistry datatypes) {
		this.name = name;
		DoxygenComment doxygenComment; {
			doxygenComment = null;
			if (precedingComment != null && precedingComment.text.startsWith("*"))
				try {
					doxygenComment = DoxygenComment.of(precedingComment);
				} catch (IllegalArgumentException e) {
					logger.warn("Expected a Doxygen comment but got: " + precedingComment, e);
				}
		}
		String nicename = null;
		String description = "";
		DatatypeService type = null; {
			if (doxygenComment != null) {
				if (doxygenComment.varName.isPresent() && !doxygenComment.varName.get().equals(name))
					logger.warn("Declaration of variable $" + name + " was preceded by a Doxygen comment " +
					            "describing a different variable: " + precedingComment);
				else {
					nicename = doxygenComment.brief.orElse(null);
					description = doxygenComment.body;
					if (doxygenComment.type.isPresent()) {
						String t = doxygenComment.type.get();
						if ("xs:string".equals(t) || "string".equals(t))
							type = DatatypeService.XS_STRING;
						else if ("xs:integer".equals(t) || "integer".equals(t))
							type = DatatypeService.XS_INTEGER;
						else if ("xs:nonNegativeInteger".equals(t) || "nonNegativeInteger".equals(t))
							type = DatatypeService.XS_NON_NEGATIVE_INTEGER;
						else if ("xs:boolean".equals(t) || "boolean".equals(t))
							type = DatatypeService.XS_BOOLEAN;
						else if ("xs:anyURI".equals(t) || "anyURI".equals(t))
							type = DatatypeService.XS_ANY_URI;
						else if ("anyFileURI".equals(t))
							type = DatatypeService.ANY_FILE_URI;
						else if ("anyDirURI".equals(t))
							type = DatatypeService.ANY_DIR_URI;
						else {
							type = datatypes != null ? datatypes.getDatatype(t).orNull() : null;
							if (type == null)
								try {
									throw new IllegalArgumentException(
										"Invalid type in variable declaration '" + type + "': does not match a known data type");
								} catch (IllegalArgumentException e) {
									logger.warn("Invalid type in Doxygen comment: " + precedingComment, e);
								}
						}
					} else if (doxygenComment.typeDef.isPresent()) {
						Element def = doxygenComment.typeDef.get();
						type = new XMLBasedDatatypeService() { // id will be filled in later by DatatypeRegistry
							@Override
							protected Node readDocument() throws Exception {
								return def;
							}
						};
						try {
							// check if type declaration is valid
							type.validate("x"); // readTypeDecl() called lazily
						} catch (RuntimeException e) {
							type = null;
							logger.warn("Invalid type declaration in Doxygen comment: " + precedingComment, e);
						}
						if (type != null && datatypes != null)
							datatypes.registerVolatile(type);
					}
				}
			} else if (precedingComment != null)
				description = Pattern.compile("^\\s*\\*", Pattern.MULTILINE)
				                     .matcher(precedingComment.text)
				                     .replaceAll("")
				                     .replaceAll("\\s+", " ")
				                     .trim();
		}
		if (nicename == null)
			nicename = name.substring(0, 1).toUpperCase()
				+ CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
				                         .replace('_', ' ')
				                         .replace('-', ' ')
				                         .toLowerCase()
				                         .substring(1);
		if (type == null) {
			// infer from default value
			if ("true".equals(defaultValue) || "false".equals(defaultValue))
				type = DatatypeService.XS_BOOLEAN;
			else {
				try {
					Integer.parseInt(defaultValue);
					type = DatatypeService.XS_INTEGER;
				} catch (NumberFormatException e) {
					type = DatatypeService.XS_STRING;
				}
			}
		}
		if (type == DatatypeService.XS_STRING) {
			if (defaultValue.startsWith("\"") || defaultValue.startsWith("'")) {
				if ((defaultValue.startsWith("\"") && !defaultValue.endsWith("\""))
				    || (defaultValue.startsWith("'") && !defaultValue.endsWith("'")))
					throw new IllegalArgumentException("Invalid value in variable declaration: " + defaultValue);
				defaultValue = CssEscape.unescapeCss(defaultValue.substring(1, defaultValue.length() - 1));
			}
		}
		this.nicename = nicename;
		this.description = description;
		this.value = defaultValue;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNiceName() {
		return nicename;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public DatatypeService getType() {
		return type;
	}

	@Override
	public String toString() {
		return "$" + name + ": " + value + (isDefault ? " !default" : "") + ";";
	}
}
