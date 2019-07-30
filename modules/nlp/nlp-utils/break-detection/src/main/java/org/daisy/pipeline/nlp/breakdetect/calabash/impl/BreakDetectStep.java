package org.daisy.pipeline.nlp.breakdetect.calabash.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.nlp.DummyLangDetector;
import org.daisy.pipeline.nlp.LangDetector;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.daisy.pipeline.nlp.lexing.LexServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

/**
 * XprocStep built on the top of a Lexer meant to be provided by an OSGI service
 * through BreakDetectProvider.
 */
public class BreakDetectStep extends DefaultStep implements TreeWriterFactory,
        InlineSectionProcessor {

	private Logger mLogger = LoggerFactory.getLogger(BreakDetectStep.class);
	private ReadablePipe mSource = null;
	private WritablePipe mResult = null;
	private XProcRuntime mRuntime = null;
	private LexServiceRegistry mLexerRegistry;
	private Set<Locale> mLangs;
	private Collection<String> inlineTagsOption;
	private Collection<String> wordBeforeOption;
	private Collection<String> wordAfterOption;
	private Collection<String> sentenceBeforeOption;
	private Collection<String> sentenceAfterOption;
	private String tmpNs;
	private String wordTagOption;
	private String sentenceTagOption;
	private LangDetector mLangDetector = null;

	public BreakDetectStep(XProcRuntime runtime, XAtomicStep step, LexServiceRegistry registry) {
		super(runtime, step);
		mRuntime = runtime;
		mLexerRegistry = registry;
	}

	public void setInput(String port, ReadablePipe pipe) {
		if ("source".equals(port)) {
			mSource = pipe;
		}
	}

	@Override
	public void setOption(QName name, RuntimeValue value) {
		super.setOption(name, value);
		if ("inline-tags".equalsIgnoreCase(name.getLocalName())) {
			inlineTagsOption = processListOption(value.getString());
		} else if ("ensure-word-before".equalsIgnoreCase(name.getLocalName())) {
			wordBeforeOption = processListOption(value.getString());
		} else if ("ensure-word-after".equalsIgnoreCase(name.getLocalName())) {
			wordAfterOption = processListOption(value.getString());
		} else if ("ensure-sentence-before".equalsIgnoreCase(name.getLocalName())) {
			sentenceBeforeOption = processListOption(value.getString());
		} else if ("ensure-sentence-after".equalsIgnoreCase(name.getLocalName())) {
			sentenceAfterOption = processListOption(value.getString());
		} else if ("output-word-tag".equalsIgnoreCase(name.getLocalName())) {
			wordTagOption = value.getString();
		} else if ("output-sentence-tag".equalsIgnoreCase(name.getLocalName())) {
			sentenceTagOption = value.getString();
		} else if ("tmp-ns".equalsIgnoreCase(name.getLocalName())) {
			tmpNs = value.getString();
		} else {
			runtime.error(new RuntimeException("unrecognized option " + name));
		}
	}

	public void setOutput(String port, WritablePipe pipe) {
		mResult = pipe;
	}

	public void reset() {
		mSource.resetReader();
		mResult.resetWriter();
	}

	static private Collection<String> processListOption(String opt) {
		return Arrays.asList(opt.split(","));
	}

	public void run() throws SaxonApiException {
		super.run();

		HashMap<Locale, LexerToken> langToToken = new HashMap<Locale, LexerToken>();

		//Retrieve a generic lexer that can handle unexpected languages.
		LexerToken generic;
		try {
			generic = mLexerRegistry.getFallbackToken(Collections.EMPTY_LIST);
		} catch (LexerInitException e1) {
			mRuntime.error(e1);
			return;
		}
		langToToken.put(null, generic);

		FormatSpecifications formatSpecs = new FormatSpecifications(tmpNs, sentenceTagOption,
		        wordTagOption, "http://www.w3.org/XML/1998/namespace", "lang",
		        inlineTagsOption, wordBeforeOption, wordAfterOption, sentenceBeforeOption,
		        sentenceAfterOption);

		XmlBreakRebuilder xmlRebuilder = new XmlBreakRebuilder();

		long before = System.currentTimeMillis();

		if (mLangDetector == null) {
			mLangDetector = new DummyLangDetector();
			mLangDetector.train();
		}

		while (mSource.moreDocuments()) {
			XdmNode doc = mSource.read();

			//init the lexers with the languages
			mLangs = new HashSet<Locale>();
			try {
				new InlineSectionFinder().find(doc, 0, formatSpecs, this,
				        Collections.EMPTY_SET);
				for (Locale lang : mLangs) {
					if (!langToToken.containsKey(lang)) {
						LexerToken token = mLexerRegistry.getTokenForLang(lang, langToToken
						        .values());
						if (token == null) {
							throw new LexerInitException(
							        "cannot find a lexer for the language: " + lang);
						}
						langToToken.put(lang, token);
					}
				}
			} catch (LexerInitException e) {
				mRuntime.error(e);
				continue;
			}

			mRuntime.info(null, null, "Total number of language(s): "
			        + (langToToken.size() - 1));
			for (Map.Entry<Locale, LexerToken> entry : langToToken.entrySet()) {
				mRuntime.info(null, null, "LexService for language '"
				        + (entry.getKey() == null ? "<ANY>" : entry.getKey()) + "': "
				        + entry.getValue().getLexService().getName());
			}

			//rebuild the XML tree and lex the content on-the-fly
			List<String> parsingErrors = new ArrayList<String>();
			XdmNode tree;
			try {
				tree = xmlRebuilder.rebuild(this, langToToken, doc, formatSpecs,
				        mLangDetector, false, parsingErrors);
				mResult.write(tree);
			} catch (LexerInitException e) {
				mRuntime.error(e);
			}
			for (String error : parsingErrors) {
				mRuntime.info(null, null, doc.getBaseURI() + ": " + error);
			}
		}

		for (LexerToken token : langToToken.values()) {
			mLexerRegistry.releaseToken(token);
		}

		long after = System.currentTimeMillis();
		mLogger.debug("lexing time = " + (after - before) / 1000.0 + " s.");

		mLangs = null;
	}

	@Override
	public TreeWriter newInstance() {
		return new TreeWriter(mRuntime);
	}

	@Override
	public void onInlineSectionFound(List<Leaf> leaves, List<String> text, Locale lang)
	        throws LexerInitException {

		//TODO: find a way to not doing this multiple times (it is done also in the rebuilder)
		//If this is really too CPU-intensive, one can skip this detection.
		//As a result, no lexer would be loaded for the true language if the language is not
		//also used somewhere else, and the XMLRebuilder will eventually use a generic lexer for
		//the detected language.

		lang = mLangDetector.findLang(lang, text);

		if (lang != null) {
			mLangs.add(lang);
		}
	}

	@Override
	public void onEmptySectionFound(List<Leaf> leaves) {

	}
}
