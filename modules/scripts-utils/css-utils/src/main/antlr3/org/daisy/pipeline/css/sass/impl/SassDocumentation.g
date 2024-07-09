/**
 * Parses a SCSS file and extracts information about variables. It
 * does this by parsing variable declarations and their preceding
 * comments. The comments are expected to be in a specific format that
 * is a subset of Doxygen.
 *
 * The information gathering could later be extended to other elements
 * such as functions and mixins.
 */

grammar SassDocumentation;

@lexer::header  { package org.daisy.pipeline.css.sass.impl; }
@parser::header { package org.daisy.pipeline.css.sass.impl; }

@parser::members {

    private boolean isScss;
    private java.net.URI base;
    private java.util.Collection<org.daisy.pipeline.css.Medium> media;
    private SassAnalyzer analyzer;

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    /**
     * @param base for resolving imports
     * @param analyzer for handling imports
     */
    public SassDocumentationParser init(boolean isScss,
                                        java.net.URI base,
                                        java.util.Collection<org.daisy.pipeline.css.Medium> media,
                                        SassAnalyzer analyzer) {
        this.isScss = isScss;
        this.base = base;
        this.media = media;
        this.analyzer = analyzer;
        return this;
    }

    private boolean matchingMedia(String mediaQuery) {
        return com.google.common.collect.Iterables.any(media, m -> m.matches(mediaQuery));
    }
}

//////////////////////////////////////////
///////////////// PARSER /////////////////
//////////////////////////////////////////

start : variables ; // to avoid "no start rule" warning

variables returns [java.util.Collection<SassVariable> vars]
@init {
    Comment lastComment = null;
    vars = new java.util.ArrayList<>();
    boolean isDefault = false;
}
    : ( c=comment { lastComment = c; }
      | (n=VAR_DECL { isDefault = false; } v=variable_value (DEFAULT { isDefault = true;} S?)? SEMICOLON) {
            String name = n.getText().substring(1, n.getText().length() - 1).trim();
            if (isDefault)
                vars.add(new SassVariable(name, lastComment, v, analyzer.datatypes));
            else
                vars.add(new SassVariable(name, v));
            lastComment = null;
        }
      // @import currently only supported at the top level
      | vars_from_import=import_rule {
            vars.addAll(vars_from_import);
            lastComment = null;
        }
      | vars_inside_media=media_rule {
            if (vars_inside_media != null)
                vars.addAll(vars_inside_media);
            lastComment = null;
        }
      | rule_block { lastComment = null; }
      | ~(COMMENT|VAR_DECL|IMPORT|MEDIA|LCURLY|RCURLY|S) { lastComment = null; }
      | S
      )*
    ;

variable_value returns [String value]
@init {
    value = "";
}
@after {
    value = value.trim();
}
    : ( t=~(DEFAULT|SEMICOLON) { value += t.getText(); } )+
    ;

comment returns [Comment comment]
    : c=COMMENT {
          String text = c.getText();
          $comment = new Comment(text.substring(2, text.length() - 2), c.getCharPositionInLine());
      }
    ;

import_rule returns [java.util.Collection<SassVariable> vars]
@init {
    vars = new java.util.ArrayList<>();
    java.util.List<java.net.URI> imports = new java.util.ArrayList<>();
    String mediaQuery = null;
}
    : (IMPORT
       S? u=import_uri { imports.add(u); }
       (S? COMMA S? u=import_uri { imports.add(u); })*
       S? (q=media { mediaQuery = q; })?
       SEMICOLON) {
           if (matchingMedia(mediaQuery))
               for (java.net.URI i : imports)
                   try {
                       java.net.URL url; {
                           try {
                               if (base != null)
                                   i = org.daisy.common.file.URLs.resolve(base, i);
                               url = org.daisy.common.file.URLs.asURL(i);
                           } catch (RuntimeException e) {
                               throw new java.io.IOException(e);
                           }
                       }
                       vars.addAll(
                           analyzer.getVariableDeclarations(
                               new cz.vutbr.web.csskit.antlr.CSSSource(url,
                                                                       java.nio.charset.StandardCharsets.UTF_8,
                                                                       null)));
                   } catch (java.io.IOException e) {
                       logger.warn("Could not import stylesheet; skipping: " + i, e);
                   }
       }
    ;

media_rule returns [java.util.Collection<SassVariable> vars]
    : (MEDIA S? q=media LCURLY v=variables RCURLY) {
          if (matchingMedia(q))
              $vars = v;
      }
    ;

media returns [String media]
@init {
    StringBuilder s = new StringBuilder();
}
@after {
    $media = s.toString().trim();
}
    : t=~(S|LCURLY|SEMICOLON|COMMA|STRING|URI) { s.append(t.getText()); }
      (t=~(LCURLY|SEMICOLON) { s.append(t.getText()); })*
    ;

import_uri returns [java.net.URI uri]
    : ( s=STRING {
            String t = s.getText();
            t = t.substring(1, t.length() - 1);
            t = org.unbescape.css.CssEscape.unescapeCss(t);
            $uri = java.net.URI.create(t);
        }
      | u=URI {
            String t = u.getText();
            t = t.substring(4, t.length() - 1);
            t = t.trim();
            if (t.length() > 0 && (t.charAt(0) == '\'' || t.charAt(0) == '"')) {
                t = t.substring(1, t.length() - 1);
                t = org.unbescape.css.CssEscape.unescapeCss(t);
            }
            $uri = java.net.URI.create(t);
        }
      )
    ;

rule_block
    : LCURLY
         ( rule_block
         | ~(RCURLY|LCURLY)
         )*
      RCURLY
    ;

//////////////////////////////////////////
///////////////// TOKENS /////////////////
//////////////////////////////////////////

APOS : '\'' ;

QUOT : '"' ;

COLON : ':' ;

SEMICOLON : ';' ;

COMMA : ',' ;

LCURLY : '{' ;

RCURLY : '}' ;

MINUS : '-' ;

XML_COMMENT_OPEN : '<!--' ;

XML_COMMENT_CLOSE : '-->' ;

AT
    : '@'
    ( ('import') => 'import' { $type = IMPORT; }
    | ('media') => 'media' { $type = MEDIA; }
    )?
    ;

BANG
    : '!'
    ( ('default') => 'default' { $type = DEFAULT; }
    )?
    ;

DOLLAR
    : '$' '-'? IDENT_START NAME_CHAR* SPACE_CHAR*
    ( (COLON) => COLON { $type = VAR_DECL; }
    )?
    ;

IDENT
    : IDENT_START NAME_CHAR*
    ;

STRING
    : STRING_MACR
    ;

URI
    : 'url(' SPACE_CHAR* (STRING_MACR | URI_MACR) SPACE_CHAR* ')'
    ;

COMMENT
    : COMMENT_MACR
    ;

// silent comment in Sass
LINE_COMMENT
    : '//' ~('\r' | '\n')*
    ;

S
    : SPACE_CHAR+
    ;

// any next character not matched by tokens above
ANY : . ;

//////////////////////////////////////////
/////////////// FRAGMENTS ////////////////
//////////////////////////////////////////

fragment DEFAULT : ;
fragment IMPORT : ;
fragment MEDIA : ;
fragment VAR_DECL : ;

fragment
IDENT_START
    : ('a'..'z' | 'A'..'Z' | '_' | NON_ASCII | ESCAPE_CHAR)
    ;

fragment
NAME_CHAR
    : IDENT_START | '0'..'9' | '-'
    ;

fragment
STRING_MACR
    : QUOT (STRING_CHAR|APOS)* QUOT
    | APOS (STRING_CHAR|QUOT)* APOS
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

fragment
URI_MACR
    : URI_CHAR+
    ;

fragment
URI_CHAR
    : ESCAPE_CHAR
    | '\u0021'
    // 22 = QUOT
    | '\u0023'..'\u0026'
    // 27 = APOS
    // 28 = (
    // 29 = )
    | '\u002A'..'\u007E'
    | NON_ASCII
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
NEWLINE_CHAR
    : '\u000A'
    | '\u000D' '\u000A'
    | '\u000D'
    | '\u000C'
    ;

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
COMMENT_MACR
    : '/*' ('*'~'/'|~'*')* '*/'
    ;
