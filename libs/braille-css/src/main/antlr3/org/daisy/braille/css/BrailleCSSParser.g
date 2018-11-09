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
}

// @Override
// Added volume and text_transform_def
unknown_atrule
    : volume
    | text_transform_def
    | ATKEYWORD S* LCURLY any* RCURLY -> INVALID_ATSTATEMENT
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
    : TEXT_TRANSFORM S+ IDENT S* LCURLY S* declarations RCURLY
        -> ^(TEXT_TRANSFORM IDENT declarations)
    ;

// @Override
// Added :not() and :has()
pseudo
    : pseudocolon^ (
        MINUS? IDENT
      | NOT S!* selector (COMMA! S!* selector)* RPAREN!
      | HAS S!* relative_selector (COMMA! S!* relative_selector)* RPAREN!
      | FUNCTION S!* (IDENT | MINUS? NUMBER | MINUS? INDEX) S!* RPAREN!
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

// @Override
inlineset
    : relative_or_chained_selector LCURLY S* declarations RCURLY -> ^(RULE relative_or_chained_selector declarations)
    | text_transform_def

// TODO: allowed as well but skip for now:
//  | anonymous_page // page at-rule

// TODO: need a slightly different format that allows @page inside @begin and @end:
//  | volume // volume at-rule
    ;

// FIXME: Note that in the braille CSS specification the second AMPERSAND is optional. This
// implementation requires it however. In theory, a semicolon could be used to avoid confusion
// between a term and the start of a selector, however I can't find a way to implement this in
// ANTLR, while keeping the semicolon optional.
relative_or_chained_selector
    : ( AMPERSAND selector | AMPERSAND! S!* combinator selector ) (combinator selector)*
    ;

anonymous_page
    : PAGE page_pseudo? S*
        LCURLY S*
        declarations margin_rule*
        RCURLY
        -> ^(PAGE page_pseudo? declarations ^(SET margin_rule*))
    ;
