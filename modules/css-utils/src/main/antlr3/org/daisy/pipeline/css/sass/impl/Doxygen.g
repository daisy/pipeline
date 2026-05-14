/**
 * Parses a comment in a specific format that is a subset of Doxygen:
 *
 * - Javadoc variant (C-style comment block starting with two *'s,
 *   optionally with intermediate *'s at the beginning of each line)
 * - optionally beginning with @var command:
 *
 *      @var <type> $<var-name>
 *
 * - optionally followed by @brief command
 * - followed by a body of Markdown formatted text, with the first
 *   line of the body being plain text (providing the short description)
 *
 * It is assumed that the following preprocessing has been done to the
 * input before it is fed to the lexer:
 *
 * - check that first character is '*'
 * - prepend new line character plus an amount of space characters
     corresponding to the original column of the second '*' of the
     opening '/**'
 * - append new line character
 */

grammar Doxygen;

@lexer::header  { package org.daisy.pipeline.css.sass.impl; }
@parser::header { package org.daisy.pipeline.css.sass.impl; }

@parser::members {

    private Comment source;

    /**
     * @param source Source of the token stream we are parsing.
     */
    public DoxygenParser init(Comment source) {
        this.source = source;
        return this;
    }

    /* convert XMLStreamReader to Saxon Element */
    private static org.w3c.dom.Element readElement(javax.xml.stream.XMLStreamReader xml) throws net.sf.saxon.s9api.SaxonApiException,
                                                                                                net.sf.saxon.trans.XPathException,
                                                                                                javax.xml.stream.XMLStreamException {
        net.sf.saxon.s9api.XdmDestination dest = new net.sf.saxon.s9api.XdmDestination();
        net.sf.saxon.event.Receiver receiver = new net.sf.saxon.event.NamespaceReducer(
            dest.getReceiver(new net.sf.saxon.s9api.Processor(false).getUnderlyingConfiguration()));
        receiver.open();
        org.daisy.common.stax.XMLStreamWriterHelper.writeElement(new net.sf.saxon.event.StreamWriterToReceiver(receiver), xml);
        return ((org.w3c.dom.Document)net.sf.saxon.dom.DocumentOverNodeInfo.wrap(dest.getXdmNode().getUnderlyingNode())).getDocumentElement();
    }

    private static Iterable<org.w3c.dom.Node> iterateNodeList(org.w3c.dom.NodeList nodeList) {
        java.util.List<org.w3c.dom.Node> list = new java.util.ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++)
            list.add(nodeList.item(i));
        return list;
    }
}

//////////////////////////////////////////
///////////////// PARSER /////////////////
//////////////////////////////////////////

start : doxygen_comment ;

doxygen_comment returns [DoxygenComment comment] // throws IllegalArgumentException
scope {
    int bodyIndent;
}
@init {
    String varName = null;
    String varType = null;
    String brief = null;
    StringBuilder body = new StringBuilder();
    $doxygen_comment::bodyIndent = -1;
    org.w3c.dom.Element typeDef = null;
}
@after {
    java.util.Scanner bodyScanner = new java.util.Scanner(body.toString());
    body = new StringBuilder();
    if (bodyScanner.hasNextLine())
        body.append(bodyScanner.nextLine().trim());
    while (bodyScanner.hasNextLine()) {
        String line = bodyScanner.nextLine();
        if (line.length() > $doxygen_comment::bodyIndent)
            line = line.substring($doxygen_comment::bodyIndent);
        body.append('\n').append(line);
    }
    comment = new DoxygenComment(varName, varType, brief, body.toString().trim(), typeDef, source); // throws IllegalArgumentException
}
    : EOL+
      (VAR S (vt=var_type { varType = vt; } S)? DOLLAR vn=var_name { varName = vn; } S? EOL+)?
      (BRIEF S r=rest_of_line { brief = r; } EOL+)?
      (
          t=~(S|BRIEF|VAR|CODE_FENCE|AT_COMMAND|XML_COMMAND_OPEN|XML_COMMAND_CLOSE|EOL) {
              int col = t.getCharPositionInLine();
              if ($doxygen_comment::bodyIndent < 0 || col < $doxygen_comment::bodyIndent)
                  $doxygen_comment::bodyIndent = col;
              for (int k = 0; k < col; k++) body.append(" ");
              body.append(t.getText());
          }
          text=rest_of_line { body.append(text); }
          (EOL { body.append("\n"); })+
      |
          text=fenced_code_block { body.append(text); }
      )+
      (d=typedef { typeDef = d; })? // throws IllegalArgumentException
      EOF
    ;

var_type returns [String type]
@init {
    StringBuilder s = new StringBuilder();
}
@after {
    type = s.toString();
}
    : (t=~(DOLLAR|S|EOL) {
           s.append(t.getText());
      })+
    ;

var_name returns [String name]
@init {
    StringBuilder s = new StringBuilder();
}
@after {
    name = s.toString();
}
    : (t=~(S|EOL) {
           s.append(t.getText());
      })+
    ;

fenced_code_block returns [String block]
@init {
    StringBuilder s = new StringBuilder();
}
@after {
    block = s.toString();
}
    :
      t=CODE_FENCE {
          int col = t.getCharPositionInLine();
          if ($doxygen_comment::bodyIndent < 0 || col < $doxygen_comment::bodyIndent)
              $doxygen_comment::bodyIndent = col;
          for (int k = 0; k < col; k++) s.append(" ");
          s.append(t.getText());
      }
      text=rest_of_line { s.append(text); }
      (EOL { s.append("\n"); })+
      (
          t=~(CODE_FENCE|S|EOL) {
              col = t.getCharPositionInLine();
              if ($doxygen_comment::bodyIndent < 0 || col < $doxygen_comment::bodyIndent)
                  $doxygen_comment::bodyIndent = col;
              for (int k = 0; k < col; k++) s.append(" ");
              s.append(t.getText());
          }
          text=rest_of_line { s.append(text); }
          (EOL { s.append("\n"); })+
      )*
      t=CODE_FENCE {
          col = t.getCharPositionInLine();
          if ($doxygen_comment::bodyIndent < 0 || col < $doxygen_comment::bodyIndent)
              $doxygen_comment::bodyIndent = col;
          for (int k = 0; k < col; k++) s.append(" ");
          s.append(t.getText());
      }
      S?
      (EOL { s.append("\n"); })+
    ;

// for now type definition expected to come at the end, and no other XML commands allowed
typedef returns [org.w3c.dom.Element def] // throws IllegalArgumentException
@init {
    StringBuilder s = new StringBuilder();
    int indent = -1;
}
@after {
    java.util.Scanner scanner = new java.util.Scanner(s.toString());
    s = new StringBuilder();
    if (scanner.hasNextLine())
        s.append(scanner.nextLine().trim());
    while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.length() > indent)
            line = line.substring(indent);
        s.append('\n').append(line);
    }
    try {
        javax.xml.stream.XMLStreamReader xmlStream
            = javax.xml.stream.XMLInputFactory.newFactory()
                                              .createXMLStreamReader(new java.io.StringReader(s.toString()));
        switch (xmlStream.next()) {
        case javax.xml.stream.XMLStreamConstants.START_DOCUMENT:
            xmlStream.next();
            if (!xmlStream.isStartElement())
                throw new IllegalStateException(); // should not happen
        case javax.xml.stream.XMLStreamConstants.START_ELEMENT:
            if (!new javax.xml.namespace.QName("http://www.daisy.org/ns/pipeline/xproc", "type").equals(xmlStream.getName()))
                throw new IllegalArgumentException("Unexpected XML command found: " + xmlStream.getName());
            break;
        default:
            throw new IllegalStateException(); // should not happen
        }
        for (org.w3c.dom.Node n : iterateNodeList(readElement(xmlStream).getChildNodes())) {
            switch (n.getNodeType()) {
            case org.w3c.dom.Node.TEXT_NODE:
            case org.w3c.dom.Node.CDATA_SECTION_NODE:
                if (((org.w3c.dom.CharacterData)n).getData().trim().length() > 0)
                    break;
                else
                    continue;
            case org.w3c.dom.Node.COMMENT_NODE:
                continue;
            case org.w3c.dom.Node.ELEMENT_NODE:
                if (def == null) {
                    def = (org.w3c.dom.Element)n;
                    continue;
                } else
                    break;
            default:
            }
            throw new IllegalArgumentException("`type' command expects a single child element");
        }
        if (xmlStream.hasNext())
            if (xmlStream.next() != javax.xml.stream.XMLStreamConstants.END_DOCUMENT)
                throw new IllegalArgumentException("No content allowed after `type' command");
    } catch (javax.xml.stream.XMLStreamException |
             java.util.NoSuchElementException |
             net.sf.saxon.trans.XPathException |
             net.sf.saxon.s9api.SaxonApiException e) {
        throw new IllegalArgumentException("Invalid type declaration provided", e);
    }
}
    : t=XML_COMMAND_OPEN {
          int col = t.getCharPositionInLine();
          if (indent < 0 || col < indent) indent = col;
          for (int k = 0; k < col; k++) s.append(" ");
          s.append(t.getText());
      }
      text=rest_of_line { s.append(text); }
      (EOL { s.append("\n"); })+
      (
          t=~(S|EOL) {
              col = t.getCharPositionInLine();
              if (indent < 0 || col < indent) indent = col;
              for (int k = 0; k < col; k++) s.append(" ");
              s.append(t.getText());
          }
          text=rest_of_line { s.append(text); }
          (EOL { s.append("\n"); })+
      )*
    ;

rest_of_line returns [String text]
@init {
    StringBuilder s = new StringBuilder();
}
@after {
    text = s.toString();
}
    : t=(~EOL {
           s.append(t.getText());
      })*
    ;

//////////////////////////////////////////
///////////////// TOKENS /////////////////
//////////////////////////////////////////

DOLLAR : '$';

VAR : '@var' ;

BRIEF : '@brief' ;

CODE_FENCE : '~~~' ;

AT_COMMAND
    : '@' (~('\u0020'|'\u0009'|'\r'|'\n'))+
    ;

XML_COMMAND_OPEN
    : '<'
      (~('\u0020'|'\u0009'|'\r'|'\n'|'<'|'>'|'/'))+
      (
          ('\u0020'|'\u0009'|'\r'|'\n')+
          (~('\u0020'|'\u0009'|'\r'|'\n'|'<'|'='))+
          ('\u0020'|'\u0009'|'\r'|'\n')*
          '='
          ('\u0020'|'\u0009'|'\r'|'\n')*
          (
            ('"' (~'"')* '"')
          | ('\'' (~'\'')* '\'')
          )
      )*
      ('\u0020'|'\u0009'|'\r'|'\n')*
      '>'
    ;

XML_COMMAND_CLOSE
    : '</'
      (~('\u0020'|'\u0009'|'\r'|'\n'|'<'|'>'|'/'))+
      '>'
    ;

S
    : ( '\u0020'
      | '\u0009'
      )+
    ;

// includes (optional) leading space on next line
EOL
    : ( '\r\n'
      | '\n'
      | '\r' )
      ( '\u0020'
      | '\u0009'
      )*
      ( '*'
        ( '\u0020'
        | '\u0009'
        )*
      )?
    ;

// any next character not matched by tokens above
ANY : . ;

