parser grammar BrailleCSSParser;

options {
    output = AST;
    tokenVocab=BrailleCSSLexer;
    k = 2;
}

import CSSParser;

@header {package org.daisy.braille.css;}

@members {
    public void init() {
        gCSSParser.init();
    }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        gCSSParser.displayRecognitionError(tokenNames, e);
    }
}

// @Override
// Added volume, text_transform_def, hyphenation_resource_def, counter_style_def and vendor_atrule
unknown_atrule
    : volume
    | text_transform_def
    | hyphenation_resource_def
    | counter_style_def
    | vendor_atrule
    ;

volume
    : VOLUME S* (volume_pseudo S*)? LCURLY S* declarations volume_area* RCURLY
      -> ^(VOLUME volume_pseudo? declarations ^(SET volume_area*))
    ;

volume_pseudo
    : pseudocolon^ ( IDENT | FUNCTION S!* NUMBER S!* RPAREN! )
    ;

volume_area
    : VOLUME_AREA S* LCURLY S* declarations RCURLY S*
      -> ^(VOLUME_AREA declarations)
    ;

text_transform_def
    : TEXT_TRANSFORM S* (IDENT S*) ? LCURLY S* declarations RCURLY
        -> ^(TEXT_TRANSFORM IDENT? declarations)
    ;

hyphenation_resource_def
    : HYPHENATION_RESOURCE S* pseudocolon LANG S* LCURLY S* declarations RCURLY
        -> ^(HYPHENATION_RESOURCE LANG declarations)
    ;

counter_style_def
    : COUNTER_STYLE S* IDENT S* LCURLY S* declarations RCURLY
        -> ^(COUNTER_STYLE IDENT declarations)
    ;

vendor_atrule
    : VENDOR_ATRULE S* LCURLY S* declarations (vendor_atrule S*)* RCURLY
      -> ^(VENDOR_ATRULE declarations ^(SET vendor_atrule*))
    | ATKEYWORD S* LCURLY S* declarations (vendor_atrule S*)* RCURLY
      -> ^(ATKEYWORD declarations ^(SET vendor_atrule*))
    ;

// @Override
// Added :not(), :has() and :lang()
pseudo
    : pseudocolon^ (
        MINUS? IDENT
      | NOT S!* selector (COMMA! S!* selector)* RPAREN!
      | HAS S!* relative_selector (COMMA! S!* relative_selector)* RPAREN!
      | LANG
      | MINUS? FUNCTION S!* (IDENT | MINUS? NUMBER | MINUS? INDEX) S!* RPAREN!
      )
    ;
  catch [RecognitionException re] {
     retval.tree = gCSSParser.tnr.invalidFallback(INVALID_SELPART, "INVALID_SELPART", re);
  }

/*
 * Relative selector
 * (https://drafts.csswg.org/selectors-4/#relative-selector), used in
 * relational pseudo-class
 * (https://drafts.csswg.org/selectors-4/#relational)
 */
relative_selector
    : combinator_selector (combinator selector)* -> ASTERISK combinator_selector (combinator selector)*
    ;

combinator_selector
    : selector -> DESCENDANT selector
    | GREATER S* selector -> CHILD selector
    | PLUS S* selector -> ADJACENT selector
    | TILDE S* selector -> PRECEDING selector
    ;

// @Override
/*
 * The COLON recognized as the start of an invalid property (which is
 * used in some nasty CSS hacks) conflicts with the COLON in the
 * possible pseudo class selector of inlineset. jStyleParser favors
 * the hack recovery over the support for pseudo elements in inline
 * stylesheets.
 *
 * Also disable all the other characters that are a valid beginning of
 * a relative selector (see relative_or_chained_selector).
 */
noprop
    :
    (
 //   CLASSKEYWORD -> CLASSKEYWORD |
      NUMBER -> NUMBER
    | COMMA -> COMMA
 // | GREATER -> GREATER
    | LESS -> LESS
    | QUESTION -> QUESTION
    | PERCENT -> PERCENT
    | EQUALS -> EQUALS
    | SLASH -> SLASH
    | EXCLAMATION -> EXCLAMATION
 // | PLUS -> PLUS
 // | ASTERISK -> ASTERISK
    | DASHMATCH -> DASHMATCH
    | INCLUDES -> INCLUDES
 // | COLON -> COLON
 // | STRING_CHAR -> STRING_CHAR
    | CTRL -> CTRL
    | INVALID_TOKEN -> INVALID_TOKEN
    ) !S*
    ;

/*
 * Simple list of declarations.
 */
simple_inlinestyle
    : S* (declarations -> ^(INLINESTYLE declarations))
    ;

/*
 * Syntax of style attributes according to http://braillespecs.github.io/braille-css/#style-attribute.
 */
// @Override
inlinestyle
    : S* declarations (inlineset S*)* -> ^(INLINESTYLE ^(RULE declarations) inlineset*)
    ;

inline_pagestyle
    : S* declarations ( (margin_rule | relative_page_pseudo) S*)*
      -> ^(INLINESTYLE
            ^(RULE declarations)
            margin_rule*
            relative_page_pseudo*
         )
    ;

inline_volumestyle
    : S* declarations ( (inline_volume_area | relative_volume_pseudo) S*)*
      -> ^(INLINESTYLE
            ^(RULE declarations)
            inline_volume_area*
            relative_volume_pseudo*
         )
    ;

inline_hyphenation_resourcestyle
    : S* (relative_hyphenation_resource_pseudo S*)*
      -> ^(INLINESTYLE relative_hyphenation_resource_pseudo*)
    ;

inline_vendor_atrulestyle
    : S* declarations (vendor_atrule S*)*
      -> ^(INLINESTYLE
            ^(RULE declarations)
            vendor_atrule*
         )
    ;

// @Override
inlineset
    : relative_or_chained_selector LCURLY S* declarations (anonymous_page S*)* RCURLY
      -> ^(AMPERSAND ^(RULE relative_or_chained_selector declarations anonymous_page*))
    | text_transform_def
    | hyphenation_resource_def
    | counter_style_def
    | anonymous_page
    | inline_volume
    | vendor_atrule
    ;

// FIXME: Note that in the braille CSS specification the second AMPERSAND is optional. This
// implementation requires it however. In theory, a semicolon could be used to avoid confusion
// between a term and the start of a selector, however I can't find a way to implement this in
// ANTLR, while keeping the semicolon optional.
relative_or_chained_selector
    : ( AMPERSAND! selector | AMPERSAND! S!* combinator selector ) (combinator selector)*
    ;

anonymous_page
    : PAGE page_pseudo? S* LCURLY S* declarations margin_rule* RCURLY
      -> ^(PAGE page_pseudo? declarations ^(SET margin_rule*))
    ;

relative_page_pseudo
    : AMPERSAND page_pseudo S* LCURLY S* declarations margin_rule* RCURLY
      -> ^(AMPERSAND ^(PAGE page_pseudo declarations ^(SET margin_rule*)))
    ;

inline_volume
    : VOLUME S* (volume_pseudo S*)? LCURLY S* declarations inline_volume_area* RCURLY
      -> ^(VOLUME volume_pseudo? declarations ^(SET inline_volume_area*))
    ;

relative_volume_pseudo
    : AMPERSAND volume_pseudo S* LCURLY S* declarations inline_volume_area* RCURLY
      -> ^(AMPERSAND ^(VOLUME volume_pseudo declarations ^(SET inline_volume_area*)))
    ;

inline_volume_area
    : VOLUME_AREA S* LCURLY S* declarations (anonymous_page S*)* RCURLY S*
      -> ^(VOLUME_AREA declarations anonymous_page*)
    ;

relative_hyphenation_resource_pseudo
    : AMPERSAND pseudocolon LANG S* LCURLY S* declarations RCURLY
      -> ^(AMPERSAND ^(HYPHENATION_RESOURCE LANG declarations))
    ;
