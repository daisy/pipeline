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
