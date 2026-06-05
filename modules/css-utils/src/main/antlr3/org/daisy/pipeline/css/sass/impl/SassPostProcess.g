/**
 * In order to fully support stacked pseudo-elements and
 * pseudo-classes on pseudo-elements (also in combination
 * with @extend), we have previously pre-processed the SASS before it
 * was compiled to CSS, and now a CSS post-processing step is
 * required.
 *
 * Before the SASS was compiled, pseudo-elements were replaced with a
 * child selector followed by a pseudo-class. E.g. `::after' became
 * `>:after`. This needs to be reverted now.
 *
 * This simple parser does not fully parse the CSS. It only parses the
 * selectors in order to be able to modify them. Everything else
 * (declarations, at-rules, etc.) is left intact and unparsed.
 */

grammar SassPostProcess;

@lexer::header  { package org.daisy.pipeline.css.sass.impl; }
@parser::header { package org.daisy.pipeline.css.sass.impl; }

//////////////////////////////////////////
///////////////// MODEL //////////////////
//////////////////////////////////////////

@parser::members {
    private static class StringBuilder {
        protected java.lang.StringBuilder sb = new java.lang.StringBuilder();
        protected java.util.List<Object> list = new java.util.ArrayList<>();
        public void append(Token token) {
            append(token.getText());
            list.add(token);
        }
        public void append(Object o) {
            sb.append(o);
            list.add(o);
        }
        @Override
        public String toString() {
            return sb.toString();
        }
    }
    private static class SelectorRule extends StringBuilder {
        private boolean hasSelector = false;
        public void append(SelectorGroup selector) {
            if (!selector.invalid()) {
                hasSelector = true;
                super.append(selector);
            }
        }
        public void append(Object o) {
            if (hasSelector)
                super.append(o);
        }
        public boolean invalid() {
            return !hasSelector;
        }
    }
    private static class SelectorGroup extends StringBuilder {
        private String pendingSeparator = null;
        private boolean skipSeparator = true;
        public void append(Token separator) {
            if (!skipSeparator) {
                if (pendingSeparator == null)
                    pendingSeparator = separator.getText();
                else
                    pendingSeparator += separator.getText();
            }
        }
        public void append(Selector selector) {
            if (!selector.invalid()) {
                if (pendingSeparator != null)
                    append(pendingSeparator);
                super.append(selector);
                skipSeparator = false;
            }
            pendingSeparator = null;
        }
        public boolean invalid() {
            return sb.length() == 0;
        }
    }
    private static class Selector extends StringBuilder {
        private boolean invalid = false;
        protected Combinator lastCombinator = null;
        public void append(Combinator combinator) {
            lastCombinator = combinator;
        }
        public void append(SimpleSelector selector) {
            invalid |= selector.invalid();
            // drop the child combinator that should always preceed a pseudo-element
            if (selector.isPseudoElement()) {
                if (lastCombinator == null || lastCombinator.type != Combinator.CHILD)
                    invalid = true;
                else
                    // remove trailing space from previous simple selector
                    sb = new java.lang.StringBuilder(sb.toString().replaceAll("\\s+$", ""));
            } else if (lastCombinator != null)
                super.append(lastCombinator);
            lastCombinator = null;
            super.append(selector);
        }
        public boolean invalid() {
            return invalid || sb.length() == 0;
        }
    }
    private static class RelativeSelector extends Selector {
        public void append(Selector selector) {
            for (Object o : selector.list)
                if (o instanceof Combinator)
                    append((Combinator)o);
                else if (o instanceof SimpleSelector)
                    append((SimpleSelector)o);
        }
    }
    private static class Combinator extends StringBuilder {
        public static final int CHILD = GREATER;
        public static final int DESCENDANT = SPACE;
        public static final int ADJACENT = PLUS;
        public static final int SIBLING = TILDE;
        public final int type;
        public Combinator(int type) {
            this.type = type;
        }
        public Combinator(Token token) {
            this(token.getType());
            append(token);
        }
    }
    private static class SimpleSelector extends StringBuilder {
        private int size = 0;
        private PseudoElement pseudoElement = null;
        private boolean hasEmptyNegationSelector = false;
        private boolean hasOnlyPseudoClasses = true;
        private boolean invalid = false;
        public void append(SelectorPart selector) {
            // an empty negation pseudo class will always match
            if (selector instanceof NegationSelector
                && ((NegationSelector)selector).invalid())
                hasEmptyNegationSelector = true;
            else if (selector instanceof RelationalSelector
                     && ((RelationalSelector)selector).invalid())
                invalid = true;
            else {
                size++;
                // if a selector contains a pseudo-element there may
                // be only one and if other parts are present they
                // must be pseudo classes
                if (selector instanceof PseudoElement) {
                    invalid |= !hasOnlyPseudoClasses;
                    pseudoElement = (PseudoElement)selector;
                } else if (!(selector instanceof PseudoClass)) {
                    hasOnlyPseudoClasses = false;
                    invalid |= (pseudoElement != null);
                }
                super.append(selector);
            }
        }
        public boolean invalid() {
            return invalid || (size == 0 && !hasEmptyNegationSelector);
        }
        public boolean isPseudoElement() {
            return !invalid && pseudoElement != null;
        }
        @Override
        public String toString() {
            if (size == 0 && !invalid())
                return "*";
            else if (pseudoElement == null || size == 1)
                return super.toString();
            else {
                // SASS may switch order of pseudo element and pseudo
                // classes; move pseudo element to the beginning
                String s = pseudoElement.toString();
                for (Object o : list)
                    if (o instanceof PseudoClass)
                        s += o.toString();
                    else if (o instanceof Token)
                        // SPACE
                        s += ((Token)o).getText();
                return s;
            }
        }
    }
    private static class NegationSelector extends PseudoClass {
        private SelectorGroup selector = null;
        public void append(SimpleSelector ss) {
            if (selector == null)
                selector = new SelectorGroup();
            Selector s = new Selector();
            s.append(ss);
            selector.append(s);
        }
        public void append(Token t) {
            if (selector == null)
                // NOT SPACE?
                super.append(t);
            else if (t.getType() == RPAREN)
                ; // RPAREN
            else
                // COMMA SPACE?
                selector.append(t);
        }
        public boolean invalid() {
            return selector != null ? selector.invalid() : true;
        }
        @Override
        public String toString() {
            String s = super.toString();
            if (selector != null) {
                s += selector.toString();
                s += ")";
            }
            return s;
        }
    }
    private static class RelationalSelector extends PseudoClass {
        private SelectorGroup selector = null;
        public void append(RelativeSelector s) {
            if (selector == null)
                selector = new SelectorGroup();
            selector.append(s);
        }
        public void append(Token t) {
            if (selector == null)
                // HAS SPACE?
                super.append(t);
            else if (t.getType() == RPAREN)
                ; // RPAREN
            else
                // COMMA SPACE?
                selector.append(t);
        }
        public boolean invalid() {
            return selector != null ? selector.invalid() : true;
        }
        @Override
        public String toString() {
            String s = super.toString();
            if (selector != null) {
                s += selector.toString();
                s += ")";
            }
            return s;
        }
    }
    private static class SelectorPart extends StringBuilder {}
    private static class PseudoElement extends SelectorPart {}
    private static class PseudoClass extends SelectorPart {}
}

//////////////////////////////////////////
///////////////// PARSER /////////////////
//////////////////////////////////////////

start : stylesheet ; // to avoid "no start rule" warning

stylesheet returns [String str]
scope {
    StringBuilder sb;
}
@init {
    $stylesheet::sb = new StringBuilder();
}
@after {
    str = $stylesheet::sb.toString();
}
    : ( t=(SPACE
          |XML_COMMENT_OPEN
          |XML_COMMENT_CLOSE) { $stylesheet::sb.append(t); }
      | t=COMMENT             { $stylesheet::sb.append(t); }
      | media_rule
      | at_rule
      | ( r=selector_rule     { if (!r.invalid())
                                  $stylesheet::sb.append(r); } )
      )*
    ;

at_rule
    : ( t=AT                   { $stylesheet::sb.append(t); } )
      ( t=(~(LCURLY
            |SEMICOLON))       { $stylesheet::sb.append(t); } )+
      ( ( t=SEMICOLON          { $stylesheet::sb.append(t); } )
      | ( t=LCURLY             { $stylesheet::sb.append(t); } )
        ( at_rule
        | ( t=(~(AT
                |RCURLY))      { $stylesheet::sb.append(t); } )
        )*
        ( t=RCURLY             { $stylesheet::sb.append(t); } )
      )
    ;

media_rule
     : ( t=MEDIA               { $stylesheet::sb.append(t); } )
       ( t=(~LCURLY)           { $stylesheet::sb.append(t); } )+
       ( t=LCURLY              { $stylesheet::sb.append(t); } )
       ( s=stylesheet          { $stylesheet::sb.append(s); } )
       ( t=RCURLY              { $stylesheet::sb.append(t); } )
    ;

selector_rule returns [SelectorRule rule]
@init {
    rule = new SelectorRule();
}
    : ( s=selector_group     { rule.append(s); } )
      ( t=LCURLY             { rule.append(t); } )
      ( t=(~RCURLY)          { rule.append(t); } )*
      ( t=RCURLY             { rule.append(t); } )
    ;

selector_group returns [SelectorGroup sel]
@init {
    sel = new SelectorGroup();
}
    : ( s=selector           { sel.append(s); } )
      ( ( t=COMMA            { sel.append(t); } )
        ( t=SPACE            { sel.append(t); } )?
        ( s=selector         { sel.append(s); } )
        )*
    ;

selector returns [Selector sel]
@init {
    sel = new Selector();
}
    : ( s=simple_selector    { sel.append(s); } )
      ( ( c=combinator       { sel.append(c); } )
        ( s=simple_selector  { sel.append(s); } )
        )*
    ;

combinator returns [Combinator c]
    : ( t=(GREATER
          |PLUS
          |TILDE)            { c = new Combinator(t); } )
      ( t=SPACE              { c.append(t);           } )?
    | ( t=SPACE              { c = new Combinator(t); } )
    ;

relative_selector returns [RelativeSelector sel]
@init {
    sel = new RelativeSelector();
    Combinator c = null;
}
    : ( ( t=(GREATER
            |PLUS
            |TILDE)          { c = new Combinator(t); } )
        ( t=SPACE            { c.append(t);           } )?
        )?
      ( s=selector           { if (c == null)
                                   c = new Combinator(SPACE);
                               sel.append(c);
                               sel.append(s);         } )
    ;

simple_selector returns [SimpleSelector sel]
@init {
    sel = new SimpleSelector();
}
    : ( ( s=type_selector    { sel.append(s); } )
        ( s=selector_part    { sel.append(s); } )*
      | ( s=selector_part    { sel.append(s); } )+
      )
      ( t=SPACE              { sel.append(t); } )?
    ;

selector_part returns [SelectorPart sel]
    : ( s=id_selector        { sel = s; } )
    | ( s=class_selector     { sel = s; } )
    | ( s=attribute_selector { sel = s; } )
    | ( s=pseudo_element     { sel = s; } )
    | ( s=pseudo_class       { sel = s; } )
    ;

type_selector returns [SelectorPart sel]
@init {
    sel = new SelectorPart();
}
    : prefix? ( t=(IDENT
                  |ASTERISK) { sel.append(t); } )
    ;

prefix returns [String str]
@init {
    StringBuilder sb = new StringBuilder();
}
@after {
    str = sb.toString();
}
    : ( t=(IDENT
          |ASTERISK)        { sb.append(t); } )?
      ( t=BAR               { sb.append(t); } )
    ;

id_selector returns [SelectorPart sel]
@init {
    sel = new SelectorPart();
}
    : ( t=HASH              { sel.append(t); } )
      ( t=(IDENT|NAME)      { sel.append(t); } )
    ;

class_selector returns [SelectorPart sel]
@init {
    sel = new SelectorPart();
}
    : ( t=PERIOD            { sel.append(t); } )
      ( t=IDENT             { sel.append(t); } )
    ;

attribute_selector returns [SelectorPart sel]
@init {
    sel = new SelectorPart();
}
    : ( t=LBRACE            { sel.append(t); } )
      ( t=(~RBRACE)         { sel.append(t); } )+
      ( t=RBRACE            { sel.append(t); } )
    ;

pseudo_element returns [SelectorPart sel]
@init {
    sel = new PseudoElement();
}
    : ( t=COLON             { sel.append(t); } )
      ( t=COLON             { sel.append(t); } )
      ( t=(IDENT|NAME)      { sel.append(t); } )
      ( ( t=LPAREN          { sel.append(t); } )
        ( t=(~RPAREN)       { sel.append(t); } )*
        ( t=RPAREN          { sel.append(t); } )
        )?
    ;

pseudo_class returns [SelectorPart sel]
@init {
    sel = new PseudoClass();
}
    : ( ns=negation_selector    { sel = ns;      } )
    | ( rs=relational_selector  { sel = rs;      } )
    | ( t=COLON                 { sel.append(t); } )
      ( t=(IDENT|NAME)          { sel.append(t); } )
      ( ( t=LPAREN              { sel.append(t); } )
        ( t=(~RPAREN)           { sel.append(t); } )*
        ( t=RPAREN              { sel.append(t); } )
        )?
    ;

negation_selector returns [NegationSelector sel]
@init {
    sel = new NegationSelector();
}
    : ( t=NOT                 { sel.append(t); } )
      ( t=SPACE               { sel.append(t); } )?
      ( s=simple_selector     { sel.append(s); } )
      ( ( t=COMMA             { sel.append(t); } )
        ( t=SPACE             { sel.append(t); } )?
        ( s=simple_selector   { sel.append(s); } )
        )*
      ( t=RPAREN              { sel.append(t); } )
    ;

relational_selector returns [RelationalSelector sel]
@init {
    sel = new RelationalSelector();
}
    : ( t=HAS                 { sel.append(t); } )
      ( t=SPACE               { sel.append(t); } )?
      ( s=relative_selector   { sel.append(s); } )
      ( ( t=COMMA             { sel.append(t); } )
        ( t=SPACE             { sel.append(t); } )?
        ( s=relative_selector { sel.append(s); } )
        )*
      ( t=RPAREN              { sel.append(t); } )
    ;

//////////////////////////////////////////
///////////////// TOKENS /////////////////
//////////////////////////////////////////

XML_COMMENT_OPEN : '<!--' ;

XML_COMMENT_CLOSE : '-->' ;

COMMENT
    : COMMENT_MACR
    ;

SPACE
    : SPACE_CHAR+
    ;

STRING
    : STRING_MACR
    ;

IDENT
    : IDENT_START NAME_CHAR*
    ;

NAME
    : NAME_CHAR+
    ;

APOS : '\'' ;

QUOT : '"' ;

LCURLY : '{' ;

RCURLY : '}' ;

LBRACE : '[' ;

RBRACE : ']' ;

LPAREN : '(' ;

RPAREN : ')' ;

MEDIA : '@media' ;

AT : '@' ;

PERIOD : '.' ;

HASH : '#' ;

BAR : '|' ;

ASTERISK : '*' ;

COLON
    : ':'
    ( (('not(') => 'not(' { $type = NOT; } )
    | (('has(') => 'has(' { $type = HAS; } )
    )?
    ;

fragment NOT : ;
fragment HAS : ;

SEMICOLON : ';' ;

GREATER : '>' ;

PLUS : '+' ;

TILDE : '~' ;

COMMA : ',' ;

// any next character not matched by tokens above
ANY : . ;

//////////////////////////////////////////
/////////////// FRAGMENTS ////////////////
//////////////////////////////////////////

fragment
SPACE_CHAR
    : '\u0009'
    | '\u000A'
    | '\u000B'
    | '\u000C'
    | '\u000D'
    | '\u0020'
    ;

fragment
NEWLINE_CHAR
    : '\u000A'
    | '\u000D' '\u000A'
    | '\u000D'
    | '\u000C'
    ;

fragment
COMMENT_MACR
    : '/*' ('*'~'/'|~'*')* '*/'
    ;

fragment
STRING_MACR
    : QUOT (STRING_CHAR|APOS)* QUOT
    | APOS (STRING_CHAR|QUOT)* APOS
    ;

fragment
NAME_CHAR
    : ('a'..'z' | 'A'..'Z' | '0'..'9' | '-' | '_' | NON_ASCII | ESCAPE_CHAR)
    ;

fragment
IDENT_START
    : ('a'..'z' | 'A'..'Z' | '_' | NON_ASCII | ESCAPE_CHAR)
    ;

fragment
NON_ASCII
    : '\u0080'..'\uD7FF' | '\uE000'..'\uFFFD'
    ;

fragment
ESCAPE_CHAR
    : '\\' (
        ('0'..'9' | 'a'..'f' | 'A'..'F')+
        | '\u0020'..'\u002F'
        | '\u003A'..'\u0040'
        | '\u0047'..'\u0060'
        | '\u0067'..'\u007E'
        | '\u0080'..'\uD7FF'
        | '\uE000'..'\uFFFD'
      )
    ;

fragment
STRING_CHAR
    : ESCAPE_CHAR
    | '\\' NEWLINE_CHAR
    | '\u0009'
    | '\u0020'..'\u0021'
    // 22 = QUOT
    | '\u0023'..'\u0026'
    // 27 = APOS
    | '\u0028'..'\u007E'
    | NON_ASCII
    ;
