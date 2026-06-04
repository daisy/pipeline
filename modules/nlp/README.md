# Developer notes on Natural Language Processing in DAISY Pipeline

Warning: some information in this README might be out-of-date!

## Processing flow

Currently we can handle three input formats:

- DTBook (dtbook-utils)
- Zedai (zedai-utils)
- HTML (html-utils)

Let us consider the transformation of a DTBook document. Such a
document will be processed in the following order:

- `px:dtbook-break-detection` passes DTBook-specific lexing options
  to the generic `px:break-detection` step;
- `px:break-detection` starts by calling `px:break-detect` which adds
  format-independent word and sentence elements to the document;
- The words and sentences are moved, merged or discarded so as to
  comply with the DTBook grammar (`px:reshape`);
- The format-independent word and sentence elements are replaced with
  DTBook elements (`px:reshape`);
- The XML content is split around skippable elements (e.g. pagenums)
  so that the SMIL files will be able to reference them individually
  (`px:reshape`);
- Some IDs are generated to make sure that we'll be able to reference
  the elements in the SMIL files;
- The enriched DTBook and the list of sentence IDs are returned to the
  main script (dtbook-to-epub3 or dtbook-to-daisy3).

## Break detection (`px:break-detect`)

### Languages

At the first stage, the step retrieves all the languages that exist in
the document. Then the LexRegistry provides the best lexer for each
language according to rankings returned via the LexService
interface. The LexRegistry also provides a fallback lexer that can
handle situations where languages are unknown or not expected.

### Resource allocation

The step doesn't directly use the LexServices. Instead, the
LexRegistry returns LexerTokens that contain resources such as
dictionaries and compiled regexps. By default, there is one single
LexerToken for each pair of working LexService and Pipeline job so as
to prevent any multi-threading issues. But when the language resources
are allocated for a token, the corresponding subclass of LexerToken
can choose to share its resources with other LexerTokens if the
underlying resources are thread-safe.

The LexerTokens must be explicitly unregistered at the end of the step
to allow the GC to free the resources.

### TextCategorizers

The TextCategorizers are the main elements of the RuleBasedLexer. They
try to match text streams against lists of prioritized rules made of
regexps and dictionaries in order to both find the word boundaries and
their category. Once the words have been found and labeled, it is easy
for the RuleBasedLexer to detect the sentences (bottom-up approach).

TextCategorizers can be invoked from outside the usual NLP workflow to
categorize words whose boundaries have been detected by the NLP step.

### Current LexServices

- OmniLangLexer: based on Java's BreakIterator and a top-down
  approach. Works with any language (fallback lexer).
- LightLexer: works with any language (fallback lexer). It doesn't
  detect the words and may mistakenly detect groups of sentences
  rather than single sentences.
- RuleBasedLexer: works best for English and French. Should do OK with
  other Indo-European languages. Not suitable for languages such as
  Japanese or Chinese. Cannot be used as a fallback lexer.

## Current limitations

- Has only been thoroughly tested on DTBook documents;
- Even if there are dictionaries of abbreviations, lexers encounter
  difficulties in segmenting strings such as 'I like the U.S. John
  too.' (two sentences) and 'I like the G.R.R. Martin's books' (one
  sentence)
- The NLP is not yet designed to allow word-level audio
  synchronization;
- Some authors forget to add white spaces when there are already
  inline elements (e.g. span) to separate the words. Depending on the
  context, the break-detection steps can parse the text as if there
  were implicit spaces, although not always solicited by the authors.
- Under certain circumstances, the break-detection step reorganizes
  the inline elements to keep the sentences where they should be (this
  lexing strategy is configurable though). This can have side effects
  on the CSS-based display and the meaning of the inline elements;
- Existing word elements are never kept;
- Some of the existing sentence elements might be discarded;
- Lexicons (in TTS modules) wouldn't work well with elements that
  cannot contain word elements, such as the 'a' elements in dtbook110.
