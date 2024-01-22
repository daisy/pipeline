lexer grammar BrailleCSSLexer;

import CSSLexer;

@header {package org.daisy.braille.css;}

@members {
    public void init() {
        gCSSLexer.init();
    }
    
    @Override
    public Token emit() {
        Token t = gCSSLexer.tf.make();
        emit(t);
        return t;
    }
}

VOLUME : '@volume' ;

VOLUME_AREA
    : '@begin'
    | '@end'
    ;

MARGIN_AREA
  : '@top-left'
  | '@top-center'
  | '@top-right'
  | '@bottom-left'
  | '@bottom-center'
  | '@bottom-right'
  | '@left'
  | '@right'
  | '@footnotes'
  ;

NOT : 'not(' ;

HAS : 'has(' ;

// https://drafts.csswg.org/selectors-4/#lang-pseudo
LANG
  : 'lang(' S* LANGUAGE_RANGE S* (COMMA S* LANGUAGE_RANGE S*)* RPAREN
  ;

TEXT_TRANSFORM : '@text-transform' ;

HYPHENATION_RESOURCE : '@hyphenation-resource' ;

COUNTER_STYLE : '@counter-style' ;

VENDOR_ATRULE : '@' MINUS IDENT_MACR ;

fragment
LANGUAGE_RANGE : LANGUAGE_RANGE_IDENT | LANGUAGE_RANGE_STRING ;

// https://www.rfc-editor.org/rfc/rfc4647#section-2.2
fragment
LANGUAGE_RANGE_IDENT
   : ( (ALPHA (ALPHA (ALPHA (ALPHA (ALPHA (ALPHA (ALPHA ALPHA?)?)?)?)?)?)?)
     | '\*' )
     ( MINUS ( (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM ALPHANUM?)?)?)?)?)?)?)
             | '\*' ))*
   ;

fragment
LANGUAGE_RANGE_STRING
   : QUOT LANGUAGE_RANGE_STRING_UNQUOTED QUOT
   | APOS LANGUAGE_RANGE_STRING_UNQUOTED APOS
   ;

fragment
LANGUAGE_RANGE_STRING_UNQUOTED
   : ( (ALPHA (ALPHA (ALPHA (ALPHA (ALPHA (ALPHA (ALPHA ALPHA?)?)?)?)?)?)?)
     | '*' )
     ( MINUS ( (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM (ALPHANUM ALPHANUM?)?)?)?)?)?)?)
             | '*' ))*
   ;

// https://www.rfc-editor.org/rfc/rfc4234#appendix-B.1
fragment
ALPHA : 'a'..'z' | 'A'..'Z' ;

fragment
ALPHANUM : ALPHA | '0'..'9' ;
