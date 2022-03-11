package org.daisy.braille.css;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleList;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSInputStream;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.CSSSource.SourceType;
import cz.vutbr.web.csskit.antlr.CSSSourceReader;
import cz.vutbr.web.csskit.antlr.TreeUtil;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.fit.net.DataURLHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrailleCSSParserFactory extends CSSParserFactory {
	
	private static final RuleFactory ruleFactory = new BrailleCSSRuleFactory();
	
	@Override
	public StyleSheet parse(CSSSource source, CSSSourceReader cssReader, boolean inlinePriority)
			throws IOException, CSSException {
		StyleSheet sheet = (StyleSheet)ruleFactory.createStyleSheet().unlock();
		Preparator preparator = new Preparator(source.inlineElement, inlinePriority, null);
		StyleSheet ret = parseAndImport(source, cssReader, sheet, preparator, null);
		return ret;
	}
	
	@Override
	public StyleSheet append(CSSSource source, CSSSourceReader cssReader, boolean inlinePriority, StyleSheet sheet)
			throws IOException, CSSException {
		Preparator preparator = new Preparator(source.inlineElement, inlinePriority, null);
		StyleSheet ret = parseAndImport(source, cssReader, sheet, preparator, null);
		return ret;
	}
	
	private StyleSheet parseAndImport(CSSSource source, CSSSourceReader cssReader,
	                                  StyleSheet sheet, Preparator preparator, List<MediaQuery> media)
			throws CSSException, IOException {
		BrailleCSSTreeParser parser = createTreeParser(source, cssReader, preparator, media);
		parse(parser, source.type);
		for (int i = 0; i < parser.getImportPaths().size(); i++) {
			String path = parser.getImportPaths().get(i);
			List<MediaQuery> imedia = parser.getImportMedia().get(i);
			if (((imedia == null || imedia.isEmpty()) && CSSFactory.getAutoImportMedia().matchesEmpty()) //no media query specified
			    || CSSFactory.getAutoImportMedia().matchesOneOf(imedia)) { //or some media query matches to the autoload media spec
				URL url = DataURLHandler.createURL(source.base, path);
				try {
					if (cssReader.supportsMediaType(null, url))
						parseAndImport(new CSSSource(url, source.encoding, (String)null),
						               cssReader, sheet, preparator, imedia); }
				catch (IOException e) {
					log.warn("Couldn't read imported style sheet: {}", e.getMessage()); }}
			else
				log.trace("Skipping import {} (media not matching)", path); }
		return addRulesToStyleSheet(parser.getRules(), sheet);
	}
	
	@Override
	public List<MediaQuery> parseMediaQuery(String query) {
		try {
			CSSInputStream input = CSSInputStream.newInstance(query, new URL("file://media/query/url"));
			CommonTokenStream tokens = feedLexer(input);
			BrailleCSSParser parser = new BrailleCSSParser(tokens);
			parser.init();
			CommonTree ast = (CommonTree) parser.media().getTree();
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, null, null, null);
			return tparser.media(); }
		catch (IOException e) {
			log.error("I/O error during media query parsing: {}", e.getMessage());
			return null; }
		catch (CSSException e) {
			log.warn("Malformed media query {}", query);
			return null; }
		catch (RecognitionException e) {
			log.warn("Malformed media query {}", query);
			return null; }
	}

	@Override
	public List<CombinedSelector> parseSelector(String selector, Map<String,String> namespaces) {
		try {
			CSSInputStream input = CSSInputStream.newInstance(selector, null);
			CommonTokenStream tokens = feedLexer(input);
			BrailleCSSParser parser = new BrailleCSSParser(tokens);
			parser.init();
			CommonTree ast = (CommonTree)parser.combined_selector_list().getTree();
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, null, null, namespaces);
			return tparser.combined_selector_list(); }
		catch (IOException e) {
			log.error("I/O error during selector parsing: {}", e.getMessage());
			return null; }
		catch (CSSException e) {
			log.warn("Malformed selector {}", selector);
			return null; }
		catch (RecognitionException e) {
			log.warn("Malformed selector {}", selector);
			return null; }
	}
	
	public enum Context {
		ELEMENT,
		PAGE,
		VOLUME,
		VENDOR_RULE
	}
	
	public RuleList parseInlineStyle(String style, Context context) {
		try {
			CSSInputStream input = CSSInputStream.newInstance(style, null);
			CommonTokenStream tokens = feedLexer(input);
			CommonTree ast = feedParser(tokens, SourceType.INLINE, context);
			// OK to pass null for context element because it is only used in Analyzer.evaluateDOM()
			Preparator preparator = new Preparator(null, true, context);
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, preparator, null, null);
			return tparser.inlinestyle(); }
		catch (IOException e) {
			log.error("I/O error during inline style parsing: {}", e.getMessage());
			return null; }
		catch (CSSException e) {
			log.warn("Malformed inline style {}", style);
			return null; }
		catch (RecognitionException e) {
			log.warn("Malformed inline style {}", style);
			return null; }
	}
	
	public RuleList parseInlineStyle(String style) {
		return parseInlineStyle(style, Context.ELEMENT);
	}
	
	public RuleList parseInlinePageStyle(String style) {
		return parseInlineStyle(style, Context.PAGE);
	}
	
	public RuleList parseInlineVolumeStyle(String style) {
		return parseInlineStyle(style, Context.VOLUME);
	}
	
	public RuleList parseInlineVendorAtRuleStyle(String style) {
		return parseInlineStyle(style, Context.VENDOR_RULE);
	}
	
	public List<Declaration> parseSimpleInlineStyle(String style) {
		try {
			CSSInputStream input = CSSInputStream.newInstance(style, null);
			CommonTokenStream tokens = feedLexer(input);
			BrailleCSSParser parser = new BrailleCSSParser(tokens);
			parser.init();
			CommonTree ast = (CommonTree)parser.simple_inlinestyle().getTree();
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, null, null, null);
			return tparser.simple_inlinestyle(); }
		catch (IOException e) {
			log.error("I/O error during inline style parsing: {}", e.getMessage());
			return null; }
		catch (CSSException e) {
			log.warn("Malformed inline style {}", style);
			return null; }
		catch (RecognitionException e) {
			log.warn("Malformed inline style {}", style);
			return null; }
	}
	
	public Declaration parseDeclaration(String declaration) {
		try {
			CSSInputStream input = CSSInputStream.newInstance(declaration, null);
			CommonTokenStream tokens = feedLexer(input);
			BrailleCSSParser parser = new BrailleCSSParser(tokens);
			parser.init();
			CommonTree ast = (CommonTree)parser.declaration().getTree();
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, null, null, null);
			return tparser.declaration(); }
		catch (IOException e) {
			log.error("I/O error during declaration parsing: {}", e.getMessage());
			return null; }
		catch (CSSException e) {
			log.warn("Malformed declaration {}", declaration);
			return null; }
		catch (RecognitionException e) {
			log.warn("Malformed declaration {}", declaration);
			return null; }
	}
	
	private static BrailleCSSTreeParser createTreeParser(CSSSource source, CSSSourceReader cssReader,
	                                                     Preparator preparator, List<MediaQuery> media)
			throws IOException, CSSException {
		CSSInputStream input = getInput(source, cssReader);
		CommonTokenStream tokens = feedLexer(input);
		CommonTree ast = feedParser(tokens, source.type, Context.ELEMENT);
		return feedAST(tokens, ast, preparator, media, null);
	}
	
	private static CommonTokenStream feedLexer(CSSInputStream source) throws CSSException {
		try {
			BrailleCSSLexer lexer = new BrailleCSSLexer(source);
			lexer.init();
			return new CommonTokenStream(lexer); }
		catch (RuntimeException re) {
			if (re.getCause() instanceof CSSException)
				throw (CSSException) re.getCause();
			else {
				log.error("LEXER THROWS:", re);
				throw re; }}
	}
	
	private static CommonTree feedParser(CommonTokenStream source, SourceType type, Context context) throws CSSException {
		BrailleCSSParser parser = new BrailleCSSParser(source);
		parser.init();
		return getAST(parser, type, context);
	}
	
	private static BrailleCSSTreeParser feedAST(CommonTokenStream source, CommonTree ast,
	                                            Preparator preparator, List<MediaQuery> media,
	                                            Map<String,String> namespaces) {
		if (log.isTraceEnabled())
			log.trace("Feeding tree parser with AST:\n{}", TreeUtil.toStringTree(ast));
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(ast);
		nodes.setTokenStream(source);
		BrailleCSSTreeParser parser = new BrailleCSSTreeParser(nodes);
		parser.init(preparator, media, ruleFactory, namespaces);
		return parser;
	}
	
	private static CommonTree getAST(BrailleCSSParser parser, SourceType type, Context context) throws CSSException {
		switch (type) {
		case INLINE:
			try {
				switch (context) {
				case ELEMENT:
					return (CommonTree) parser.inlinestyle().getTree();
				case PAGE:
					return (CommonTree) parser.inline_pagestyle().getTree();
				case VOLUME:
					return (CommonTree) parser.inline_volumestyle().getTree();
				case VENDOR_RULE:
					return (CommonTree) parser.inline_vendor_atrulestyle().getTree(); }}
			catch (RecognitionException re) {
				throw encapsulateException(re,
						"Unable to parse inline CSS style"); }
			catch (RuntimeException re) {
				throw encapsulateException(re,
						"Unable to parse inline CSS style"); }
		case EMBEDDED:
			try {
				return (CommonTree) parser.stylesheet().getTree(); }
			catch (RecognitionException re) {
				throw encapsulateException(re,
						"Unable to parse embedded CSS style"); }
			catch (RuntimeException re) {
				throw encapsulateException(re,
						"Unable to parse embedded CSS style"); }
		case URL:
			try {
				return (CommonTree) parser.stylesheet().getTree(); }
			catch (RecognitionException re) {
				throw encapsulateException(re,
						"Unable to parse URL CSS style"); }
			catch (RuntimeException re) {
				throw encapsulateException(re,
						"Unable to parse URL CSS style"); }
		default:
			throw new RuntimeException("Coding error"); }
	}
	
	private static RuleList parse(BrailleCSSTreeParser parser, SourceType type) throws CSSException {
		switch (type) {
		case INLINE:
			try {
				return parser.inlinestyle(); }
			catch (RecognitionException re) {
				throw encapsulateException(re,
						"Unable to parse inline CSS style [AST]"); }
			catch (RuntimeException re) {
				throw encapsulateException(re,
						"Unable to parse inline CSS style [AST]"); }
		case EMBEDDED:
			try {
				return parser.stylesheet(); }
			catch (RecognitionException re) {
				throw encapsulateException(re,
						"Unable to parse embedded CSS style [AST]"); }
			catch (RuntimeException re) {
				throw encapsulateException(re,
						"Unable to parse embedded CSS style [AST]"); }
		case URL:
			try {
				return parser.stylesheet(); }
			catch (RecognitionException re) {
				throw encapsulateException(re,
						"Unable to parse file CSS style [AST]"); }
			catch (RuntimeException re) {
				throw encapsulateException(re,
						"Unable to parse file CSS style [AST]"); }
		default:
			throw new RuntimeException("Coding error"); }
	}
	
	private static final Logger log = LoggerFactory.getLogger(BrailleCSSParserFactory.class);
	
}
