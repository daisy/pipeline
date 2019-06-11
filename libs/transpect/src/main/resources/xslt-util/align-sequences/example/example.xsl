<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  xmlns:sequence-align="http://transpect.io/sequence-align"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  exclude-result-prefixes="xs math tr sequence-align"
  version="3.0">

  <!-- Invocation: saxon -xsl:xslt-util/align-sequences/example/example.xsl -it:align 
       will create aligned.html in the current working directory -->

  <xsl:import href="../xsl/align-by-common-key.xsl"/>

  <xsl:output indent="true" omit-xml-declaration="false"/>
  
  <xsl:template name="align">
    <xsl:variable name="key-expr" as="xs:string" select="'normalize-space(head)'"/>
    <xsl:variable name="aligned" as="element(sequence-align:wrap)"
      select="sequence-align:align(($doc1, $doc2, $doc3), $key-expr)"/>
    <xsl:result-document href="aligned.html" html-version="5.0" method="xhtml">
      <html xmlns="http://www.w3.org/1999/xhtml">
        <head>
          <title>Alignment test</title>
          <style>
            p { margin:0 }
            td { border: 1px solid black }
            table { border-collapse: collapse; display: inline-block; margin-right: 2em }
          </style>
        </head>
        <body>
          <xsl:apply-templates 
            select="sequence-align:wrap($doc1/*, 1, $key-expr),
                    sequence-align:wrap($doc2/*, 2, $key-expr),
                    sequence-align:wrap($doc3/*, 3, $key-expr),
                    $aligned"/>
        </body>
      </html>
    </xsl:result-document>
    <xsl:sequence select="$aligned"/>
  </xsl:template>

  <xsl:variable name="doc1" as="element(doc)">
    <doc n="1">
      <item>
        <head>Wq</head>
        <content>1</content>
      </item>
      <item>
        <head>Ax</head>
        <content>1</content>
      </item>
      <item>
        <head>F5</head>
        <content>1</content>
      </item>
      <item>
        <head>Bb</head>
        <content>1</content>
      </item>
      <item>
        <head>;p</head>
        <content>1</content>
      </item>
      <item>
        <head>Yi</head>
        <content>1</content>
      </item>
    </doc>
  </xsl:variable>
  
  <xsl:variable name="doc2" as="element(doc)">
    <doc n="2">
      <item>
        <head>Ax</head>
        <content>2</content>
      </item>
      <item>
        <head>7f</head>
        <content>2</content>
      </item>
      <item>
        <head>F5</head>
        <content>2</content>
      </item>
      <item>
        <head>Bb</head>
        <content>2</content>
      </item>
      <item>
        <head>Tg</head>
        <content>2</content>
      </item>
      <item>
        <head>jj</head>
        <content>2</content>
      </item>
      <item>
        <head>;p</head>
        <content>2</content>
      </item>
      <item>
        <head>Dr</head>
        <content>2</content>
      </item>
    </doc>
  </xsl:variable>
  
  <xsl:variable name="doc3" as="element(doc)">
    <doc n="3">
      <item>
        <head>Wq</head>
        <content>3</content>
      </item>
      <item>
        <head>7f</head>
        <content>3</content>
      </item>
      <item>
        <head>Bb</head>
        <content>3</content>
      </item>
      <item>
        <head>jj</head>
        <content>3</content>
      </item>
      <item>
        <head>oP</head>
        <content>3</content>
      </item>
      <item>
        <head>Ck</head>
        <content>3</content>
      </item>
      <item>
        <head>;p</head>
        <content>3</content>
      </item>
      <item>
        <head>Dr</head>
        <content>3</content>
      </item>
    </doc>
  </xsl:variable>
  
  <xsl:variable name="docs" as="element(doc)+" select="($doc1, $doc2, $doc3)"/>
  
  <xsl:template match="sequence-align:wrap">
    <xsl:variable name="colnum" as="xs:integer" 
      select="xs:integer(max(.//sequence-align:item/@n ! tokenize(.)))"/>
    <table>
      <xsl:apply-templates select="sequence-align:item">
        <xsl:with-param name="colcount" as="xs:integer" tunnel="true" select="$colnum"/>
      </xsl:apply-templates>
    </table>
  </xsl:template>
  
  <xsl:template match="sequence-align:item[parent::sequence-align:wrap][empty(sequence-align:item)]" priority="1">
    <xsl:param name="colcount" as="xs:integer" tunnel="true"/>
    <tr xmlns="http://www.w3.org/1999/xhtml">
      <xsl:for-each select="1 to xs:integer(@n) - 1">
        <td/>
      </xsl:for-each>
      <td><xsl:apply-templates/></td>
      <xsl:for-each select="xs:integer(@n) + 1 to $colcount">
        <td/>
      </xsl:for-each>
    </tr>
  </xsl:template>
  
  <xsl:template match="sequence-align:item[exists(sequence-align:item)]">
    <!-- this has sequence-align:item children! -->
    <xsl:param name="colcount" as="xs:integer" tunnel="true"/>
    <xsl:variable name="context" select="." as="element(sequence-align:item)"/>
    <tr xmlns="http://www.w3.org/1999/xhtml">
      <xsl:for-each select="1 to $colcount">
        <xsl:choose>
          <xsl:when test="exists($context/sequence-align:item[@n = current()])">
            <td>
              <xsl:apply-templates select="$context/sequence-align:item[@n = current()]/node()"/>  
            </td>
          </xsl:when>
          <xsl:otherwise>
            <td/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
  </xsl:template>
  
  <xsl:template match="sequence-align:item[empty(sequence-align:item)]/*/*">
    <p xmlns="http://www.w3.org/1999/xhtml">
      <xsl:apply-templates/>
    </p>
  </xsl:template>
  
  <xsl:template match="sequence-align:item[empty(sequence-align:item)]/*/*[1]" priority="1">
    <p xmlns="http://www.w3.org/1999/xhtml">
      <b><xsl:apply-templates/></b>
    </p>
  </xsl:template>
  
</xsl:stylesheet>