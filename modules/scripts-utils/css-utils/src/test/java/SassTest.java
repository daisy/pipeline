import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.google.common.io.CharStreams;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.csskit.antlr.CSSInputStream;
import cz.vutbr.web.csskit.antlr.CSSLexer;
import cz.vutbr.web.csskit.antlr.CSSParser;
import cz.vutbr.web.csskit.antlr.CSSTreeParser;
import cz.vutbr.web.csskit.antlr.SimplePreparator;
import cz.vutbr.web.csskit.antlr.SourceMap;

import mjson.Json;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.css.CssPreProcessor.PreProcessingResult;
import org.daisy.pipeline.css.SourceMapReader;
import org.daisy.pipeline.css.sass.SassCompiler;

import org.junit.Assert;
import org.junit.Test;

public class SassTest {
	
	@Test
	public void testNestedProperties() throws Exception {
		assertCssEquals("nested_properties.scss", "nested_properties.css");
	}
	
	@Test
	public void testMisc() throws Exception {
		assertCssEquals("misc.scss", "misc.css");
	}
	
	@Test
	public void testCharset() throws Exception {
		assertCssEquals("charset.scss", "charset.css");
	}
	
	@Test
	public void testSourceMap() throws Exception {
		SassCompiler compiler = new SassCompiler(null, null);
		File scssFile = getTestResource("nested_properties.scss");
		PreProcessingResult sassCompilationResult = compiler.compile(new FileInputStream(scssFile), URLs.asURL(scssFile), null);
		Assert.assertEquals(";AAAA,AAAA,IAAI,CAAC;EAEG,UAAG,EAAE,EAAE;EACP,aAAM,EAAE,EAAE;CAEjB",
		                    Json.read(sassCompilationResult.sourceMap).at("mappings").asString());
		SourceMap sourceMap = SourceMapReader.read(sassCompilationResult.sourceMap, sassCompilationResult.base);
		assertLineAndColumnEquals(0, 0,  sourceMap.floor(1, 0));  // #foo
		assertLineAndColumnEquals(2, 8,  sourceMap.floor(2, 2));  // border-top
		assertLineAndColumnEquals(2, 13, sourceMap.floor(2, 14)); // ⠒
		assertLineAndColumnEquals(3, 8,  sourceMap.floor(3, 2));  // border-bottom
		assertLineAndColumnEquals(3, 16, sourceMap.floor(3, 17)); // ⠒
		CSSInputStream cssStream = CSSInputStream.newInstance(
			sassCompilationResult.stream,
			null,
			URLs.asURL(sassCompilationResult.base),
			sourceMap);
		CSSLexer lexer = new CSSLexer(cssStream);
		lexer.init();
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSSParser parser = new CSSParser(tokens);
		parser.init();
		CommonTree ast = (CommonTree)parser.stylesheet().getTree();
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(ast);
		nodes.setTokenStream(tokens);
		CSSTreeParser treeParser = new CSSTreeParser(nodes);
		treeParser.init(new SimplePreparator(null, true), null, null);
		List<RuleBlock<?>> rules = treeParser.stylesheet();
		Assert.assertEquals(1, rules.size());
		RuleBlock<?> rule = rules.get(0);
		Assert.assertTrue(rule instanceof RuleSet);
		List<Declaration> declarations = (RuleSet)rule;
		Assert.assertEquals(2, declarations.size());
		assertLineAndColumnEquals(2, 8, declarations.get(0).getSource());
		assertLineAndColumnEquals(3, 8, declarations.get(1).getSource());
	}
	
	private static void assertCssEquals(String scssPath, String expectedCssPath) throws Exception {
		File scssFile = getTestResource(scssPath);
		File expectedCssFile = getTestResource(expectedCssPath);
		SassCompiler compiler = new SassCompiler(null, null);
		InputStream css = compiler.compile(new FileInputStream(scssFile), URLs.asURL(scssFile), null).stream;
		Assert.assertEquals(new String(Files.readAllBytes(expectedCssFile.toPath()), StandardCharsets.UTF_8),
		                    CharStreams.toString(new InputStreamReader(css)));
	}
	
	private static File getTestResource(String path) {
		return new File(SassTest.class.getResource("/" + path).getPath());
	}
	
	private static void assertLineAndColumnEquals(int expectedLine, int expectedColumn, SourceLocator locator) throws AssertionError {
		Assert.assertEquals(expectedLine, locator.getLineNumber());
		Assert.assertEquals(expectedColumn, locator.getColumnNumber());
	}
}
