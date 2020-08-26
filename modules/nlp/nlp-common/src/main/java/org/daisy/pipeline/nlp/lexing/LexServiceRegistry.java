package org.daisy.pipeline.nlp.lexing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "lexer-registry",
	service = { LexServiceRegistry.class }
)
public class LexServiceRegistry {
	private Map<LexService, List<LexerToken>> mLexerToTokens = new ConcurrentHashMap<LexService, List<LexerToken>>();

	/**
	 * Component callback
	 */
	@Reference(
		name = "LexService",
		unbind = "removeLexService",
		service = LexService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addLexService(LexService lexer) {
		mLexerToTokens.put(lexer, new CopyOnWriteArrayList<LexService.LexerToken>());
	}

	/**
	 * Component callback
	 */
	public void removeLexService(LexService lexer) {
		List<LexerToken> tokens = mLexerToTokens.get(lexer);
		synchronized (this) {
			if (tokens.size() > 0) {
				lexer.globalRelease();
			}
		}
		mLexerToTokens.remove(lexer);
	}

	/**
	 * Choose a LexService for a language and get its local token or create a
	 * new one if there is not yet an existing one.
	 * 
	 * @param lang
	 * @param existingTokens are the current tokens of the pipeline job
	 *            previously returned by getTokenForLang().
	 * @return a token for the LexService that best works with @param lang. A
	 *         token is an instance of lexer which is unique for each pipeline
	 *         job so that it can work within multi-thread contexts. It first
	 *         attempts to find a token of LexService that is already used by
	 *         the pipeline job for other languages. If none is found, it
	 *         creates a new token. The token is initialized with the resources
	 *         of the other pipeline jobs when this is possible.
	 * @throws LexerInitException
	 */
	public LexerToken getTokenForLang(Locale lang, Collection<LexerToken> existingTokens)
	        throws LexerInitException {
		// Look in priority in the existing tokens
		int bestScore = -1;
		LexerToken bestToken = null;
		for (LexerToken token : existingTokens) {
			int score = token.getLexService().getLexQuality(lang);
			if (score > bestScore) {
				bestScore = score;
				bestToken = token;
			}
		}

		// Look in the others lexers
		Map.Entry<LexService, List<LexerToken>> best = null;
		for (Map.Entry<LexService, List<LexerToken>> entry : mLexerToTokens.entrySet()) {
			int score = entry.getKey().getLexQuality(lang);
			if (score > bestScore) {
				bestScore = score;
				best = entry;
				bestToken = null;
			}
		}

		if (bestToken != null) {
			//The best token for the lang has already been created.
			//We then bind the language to it.
			for (LexerToken otherToken : mLexerToTokens.get(bestToken.getLexService())) {
				bestToken.shareResourcesWith(otherToken, lang);
			}
			bestToken.addLang(lang);
			return bestToken;
		}

		//A new token is the best choice for the given language.
		LexerToken result = best.getKey().newToken();
		result.addLang(lang);
		best.getValue().add(result);
		synchronized (this) {
			if (best.getValue().size() == 1) {
				best.getKey().globalInit(); //first initialization of the LexService
			}
		}

		return result;
	}

	/**
	 * Choose a LexService and get its token to be used for cases when languages
	 * are unknown or not supported.
	 */
	public LexerToken getFallbackToken(Collection<LexerToken> existingTokens)
	        throws LexerInitException {
		// Look in priority in the existing tokens
		int bestScore = 0;
		LexerToken bestToken = null;
		for (LexerToken token : existingTokens) {
			int score = token.getLexService().getOverallQuality();
			if (score > bestScore) {
				bestScore = score;
				bestToken = token;
			}
		}

		// Look at the other lexers
		Map.Entry<LexService, List<LexerToken>> best = null;
		for (Map.Entry<LexService, List<LexerToken>> entry : mLexerToTokens.entrySet()) {
			int score = entry.getKey().getOverallQuality();
			if (score > bestScore) {
				bestScore = score;
				best = entry;
				bestToken = null;
			}
		}

		if (bestToken != null) {
			return bestToken;
		}

		if (best == null) {
			throw new LexerInitException("No generic lexing service available");
		}

		//The best fallback choice is to use a new token
		LexerToken result = best.getKey().newToken();
		synchronized (this) {
			if (best.getValue().size() == 1) {
				best.getKey().globalInit(); //first initialization of the LexService
			}
		}

		return result;
	}

	/**
	 * Unregister a tokens returned by getTokenForLang(). Releasing multiple
	 * times the same token has no side effect.
	 * 
	 * @param jobtokens
	 */
	public void releaseToken(LexerToken jobtoken) {
		for (List<LexerToken> tokens : mLexerToTokens.values()) {
			if (tokens.remove(jobtoken)) {
				synchronized (this) {
					if (tokens.size() == 0) {
						jobtoken.getLexService().globalRelease();
					}
				}
				return;
			}
		}

	}

}
