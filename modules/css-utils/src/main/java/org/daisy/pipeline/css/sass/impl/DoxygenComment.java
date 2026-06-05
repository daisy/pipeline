package org.daisy.pipeline.css.sass.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import org.apache.commons.text.StringEscapeUtils;

import org.w3c.dom.Element;

class DoxygenComment extends Comment {

	final Optional<String> varName;
	final Optional<String> type;
	final Optional<String> brief;
	final String body;
	final Optional<Element> typeDef;

	DoxygenComment(String varName, String type, String brief, String body, Element typeDef, Comment comment)
			throws IllegalArgumentException {

		super(comment.text, comment.column);
		if (type != null && typeDef != null)
			throw new IllegalArgumentException("Can not have both a type specified in a @var command, and a <type> command");
		this.varName = Optional.ofNullable(varName);
		this.type = Optional.ofNullable(type);
		this.brief = Optional.ofNullable(brief);
		this.body = processMarkdown(body);
		this.typeDef = Optional.ofNullable(typeDef);
	}

	/**
	 * @throw IllegalArgumentException if the provided comment does not have the expected
	 *                                 formatting.
	 */
	static DoxygenComment of(Comment comment) throws IllegalArgumentException {
		try {
			// pre-processing
			if (!comment.text.startsWith("*"))
				throw new IllegalArgumentException("Not a Doxygen comment: " + comment);
			StringBuilder input = new StringBuilder();
			input.append("\n  ");
			for (int i = 0; i < comment.column; i++)
				input.append(" ");
			input.append(comment.text);
			input.append("\n");
			// parse
			DoxygenParser parser = new DoxygenParser(
				new CommonTokenStream(
					new DoxygenLexer(
						new ANTLRReaderStream(
							new StringReader(input.toString()))))).init(comment);
			return parser.doxygen_comment();
		} catch (RecognitionException|IllegalArgumentException e) {
			throw new IllegalArgumentException("Not a valid Doxygen comment: " + comment, e);
		} catch (IOException e) {
			throw new RuntimeException(e); // should not happen
		}
	}

	/**
	 * Rendering markdown is currently the responsibility to the client. However for convenience we
	 * do a little bit of processing in the engine.
	 */
	private static String processMarkdown(String markdown) {
		// replace HTML character entities
		return StringEscapeUtils.unescapeHtml4(markdown);
	}
}
