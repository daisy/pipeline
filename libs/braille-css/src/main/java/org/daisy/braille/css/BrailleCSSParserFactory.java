package org.daisy.braille.css;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleList;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSInputStream;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.antlr.TreeUtil;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.fit.net.DataURLHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

public class BrailleCSSParserFactory extends CSSParserFactory {
	
	private static final RuleFactory ruleFactory = new BrailleCSSRuleFactory();
	
	@Override
	public StyleSheet parse(Object source, NetworkProcessor network, String encoding, SourceType type,
	                        Element inline, boolean inlinePriority, URL base) throws IOException, CSSException {
		StyleSheet sheet = (StyleSheet)ruleFactory.createStyleSheet().unlock();
		Preparator preparator = new Preparator(inline, inlinePriority);
		StyleSheet ret = parseAndImport(source, network, encoding, type, sheet, preparator, base, null);
		return ret;
	}
	
	@Override
	public StyleSheet append(Object source, NetworkProcessor network, String encoding, SourceType type,
	                         Element inline, boolean inlinePriority, StyleSheet sheet, URL base) throws IOException, CSSException {
		Preparator preparator = new Preparator(inline, inlinePriority);
		StyleSheet ret = parseAndImport(source, network, encoding, type, sheet, preparator, base, null);
		return ret;
	}
	
	private StyleSheet parseAndImport(Object source, NetworkProcessor network, String encoding, SourceType type,
	                                  StyleSheet sheet, Preparator preparator, URL base, List<MediaQuery> media)
			throws CSSException, IOException {
		BrailleCSSTreeParser parser = createTreeParser(source, network, encoding, type, preparator, base, media);
		parse(parser, type);
		for (int i = 0; i < parser.getImportPaths().size(); i++) {
			String path = parser.getImportPaths().get(i);
			List<MediaQuery> imedia = parser.getImportMedia().get(i);
			if (((imedia == null || imedia.isEmpty()) && CSSFactory.getAutoImportMedia().matchesEmpty()) //no media query specified
			    || CSSFactory.getAutoImportMedia().matchesOneOf(imedia)) { //or some media query matches to the autoload media spec
				URL url = DataURLHandler.createURL(base, path);
				try {
					parseAndImport(url, network, encoding, SourceType.URL, sheet, preparator, url, imedia); }
				catch (IOException e) {
					log.warn("Couldn't read imported style sheet: {}", e.getMessage()); }}
			else
				log.trace("Skipping import {} (media not matching)", path); }
		return addRulesToStyleSheet(parser.getRules(), sheet);
	}
	
	@Override
	public List<MediaQuery> parseMediaQuery(String query) {
		try {
			CSSInputStream input = CSSInputStream.stringStream(query);
			input.setBase(new URL("file://media/query/url"));
			CommonTokenStream tokens = feedLexer(input);
			BrailleCSSParser parser = new BrailleCSSParser(tokens);
			parser.init();
			CommonTree ast = (CommonTree) parser.media().getTree();
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, null, null);
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
	
	public List<Declaration> parseSimpleInlineStyle(String style) {
		try {
			CSSInputStream input = CSSInputStream.stringStream(style);
			CommonTokenStream tokens = feedLexer(input);
			BrailleCSSParser parser = new BrailleCSSParser(tokens);
			parser.init();
			CommonTree ast = (CommonTree)parser.simple_inlinestyle().getTree();
			BrailleCSSTreeParser tparser = feedAST(tokens, ast, null, null);
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
	
	private static BrailleCSSTreeParser createTreeParser(Object source, NetworkProcessor network, String encoding, SourceType type,
	                                                     Preparator preparator, URL base, List<MediaQuery> media)
			throws IOException, CSSException {
		CSSInputStream input = getInput(source, network, encoding, type);
		input.setBase(base);
		CommonTokenStream tokens = feedLexer(input);
		CommonTree ast = feedParser(tokens, type);
		return feedAST(tokens, ast, preparator, media);
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
	
	private static CommonTree feedParser(CommonTokenStream source, SourceType type) throws CSSException {
		BrailleCSSParser parser = new BrailleCSSParser(source);
		parser.init();
		return getAST(parser, type);
	}
	
	private static BrailleCSSTreeParser feedAST(CommonTokenStream source, CommonTree ast,
	                                            Preparator preparator, List<MediaQuery> media) {
		if (log.isTraceEnabled())
			log.trace("Feeding tree parser with AST:\n{}", TreeUtil.toStringTree(ast));
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(ast);
		nodes.setTokenStream(source);
		BrailleCSSTreeParser parser = new BrailleCSSTreeParser(nodes);
		parser.init(preparator, media, ruleFactory);
		return parser;
	}
	
	private static CommonTree getAST(BrailleCSSParser parser, SourceType type) throws CSSException {
		switch (type) {
		case INLINE:
			try {
				return (CommonTree) parser.inlinestyle().getTree(); }
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
