<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <!-- XSweet: top level 'macro XSLT' stylesheet for dynamic dispatch of header promotion logic [1] -->
  <!-- Input:  an HTML Typescript document (wf) -->
  <!-- Output: a copy, with headers promoted according to the logic selected -->
  <!-- Note: runtime parameter `method` may be "ranked-format", "outline-level", or the name of an (XML) configuration file; if the method is not designated the XSLT falls back to "outline-level" (when such headers are detected) or "ranked-format" (when they are not) --> 
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

  <!-- Use this XQuery to get a list of stylesheets called by an XProc pipeline:

declare namespace p='http://www.w3.org/ns/xproc';
declare namespace xsw ="http://coko.foundation/xsweet";

<xsw:variable name="transformation-sequence">
{ //p:input[@port='stylesheet']/*/@href/
  <xsw:transform>{string(.)}</xsw:transform> 

}</xsw:variable>

  -->


  <!-- XSLT promotes headers in an HTML Typescript file produced by XSweet -->
  <!-- The logic it uses to do this is determined dynamically, or provided by the user. -->

  <!-- $method can be provided at runtime
     recognized values:
  method='outline-level' infers based on outline level numbering in the Word data
  method='ranked-format'   tries to assign header levels based on paragraph-level properties (format)
  or: anyURI will point to anyURI as the source for a mapping specification
    e.g. method="styles-mapping.xml" will use 'styles-mapping.xml' as the mapping file.
  
  
  
  -->

  <!-- $override $method at runtime. Values:
     *.xml is treated as regex mapping document to follow
     or 'outline-level' if by outline level
     or 'ranked-format' if by ranking property sets on paragraphs
     if two outline levels are found, outline level is used otherwise property sets are used
     Specify method='ranked-format' to be sure it is used -->
  <xsl:param name="method" as="xs:string">default</xsl:param>

  <!-- $mapping-spec provides the name of an XML document found at location on $method e.g. method='my-mapping.xml' -->
  <!-- if there is no such document it is empty -->
  <xsl:variable name="mapping-spec" as="xs:string?"
    select="$method[matches(., '\.xml$')] ! (if (doc-available(.)) then (.) else ())"/>

  <xsl:variable name="outlined" select="count(//p[matches(@style, 'xsweet-outline-level')]) gt 1"/>

<!--XSweet mini-pipelining-language semantics:

  xsw:produce returns a document, either
    An XML file at the given location, or
    A result of a sequence of operations (productions and transformations)
  xsw:transform returns a document, the result of a transformation
    using the previous step as main input
    and the contents of the xsw:transform as an XSLT transformation
      (this needs to be either a URI pointing to a stylesheet, or a single xsw:produce or xsw:transform producing an XSLT; no error trapping here)
  xsw:annotate returns a document, a copy of the input document stamped with an annotation
-->

  <xsl:variable name="transformation-sequence" expand-text="true">
    <xsw:produce>
      <xsl:choose>
        <xsl:when test="exists($mapping-spec)">
          <!-- if a mapping spec exists, we produce a tranformation from it ...      -->
          <xsw:transform>
            <!-- We produce a stylesheet, which is applied -->
            <xsw:produce>
              <!-- xsw:produce with string value returns the parsed XML file at that location.
                   Here the mapping spec replaces the main source as input for this subpipeline -->
              <xsw:produce>{ $mapping-spec }</xsw:produce>
              <xsw:transform>make-header-mapper-xslt.xsl</xsw:transform>
            </xsw:produce>
          </xsw:transform>
          <xsw:annotate> element mapping applied: { $mapping-spec } </xsw:annotate>
        </xsl:when>
        <xsl:when test="$method = 'outline-level'">
          <xsw:transform>outline-headers.xsl</xsw:transform>
          <xsw:annotate> header promotion by outline levels (as requested) </xsw:annotate>
        </xsl:when>
        <xsl:when test="$method = 'ranked-format'">
          <xsw:transform>
            <xsw:produce>
              <xsw:transform>digest-paragraphs.xsl</xsw:transform>
              <xsw:transform>make-header-escalator-xslt.xsl</xsw:transform>
            </xsw:produce>
          </xsw:transform>
          <xsw:annotate> header promotion by ranking paragraph formatting (as requested) </xsw:annotate>
        </xsl:when>
        <xsl:when test="$outlined">
          <xsw:transform>outline-headers.xsl</xsw:transform>
          <xsw:annotate> header promotion by outline levels (by default, from detected outline levels) </xsw:annotate>
        </xsl:when>
        <xsl:otherwise>
          <!-- value is either 'default' or something else not recognized or a file name -->
          <xsw:transform>
            <xsw:produce>
              <xsw:transform>digest-paragraphs.xsl</xsw:transform>
              <xsw:transform>make-header-escalator-xslt.xsl</xsw:transform>
            </xsw:produce>
          </xsw:transform>
          <xsw:annotate> header promotion by dynamic ranking of paragraph formatting (fingers crossed) </xsw:annotate>
        </xsl:otherwise>
      </xsl:choose>
      <xsw:annotate> touched by header promotion logic: { current-date() }</xsw:annotate>
    </xsw:produce>
    <!--  <xsw:transform>collapse-paragraphs.xsl</xsw:transform>-->
  </xsl:variable>

  <!-- Dummy template quiets anxious XSLT engines.  -->
  <xsl:template match="/html:html" xmlns:html="http://www.w3.org/1999/xhtml">
    <xsl:next-match/>
  </xsl:template>

  <!-- traps the root node of the source and passes it down the chain of transformation references -->
  <xsl:template match="/" name="entry">
    <xsl:variable name="source" select="."/>
    <xsl:apply-templates select="$transformation-sequence/xsw:produce">
      <xsl:with-param tunnel="yes" name="sourcedoc" select="."/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- When it has elements, xsw:produce-xslt is a pipeline or subpipeline producing the results of a sequence
       of operations ... -->
  <xsl:template match="xsw:produce">
    <xsl:param tunnel="yes" name="sourcedoc" as="document-node()"/>
    <xsl:iterate select="text() | *">
      <xsl:param name="doc" select="$sourcedoc" as="document-node()"/>
      <xsl:on-completion select="$doc"/>
      <xsl:next-iteration>
        <xsl:with-param name="doc">
          <xsl:apply-templates select=".">
            <xsl:with-param name="sourcedoc" tunnel="yes" select="$doc"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:next-iteration>
    </xsl:iterate>
  </xsl:template>

  <!-- When xsl:transform has xsw:produce, the produced document is taken to be a stylesheet
       to be applied to the input -->
  <xsl:template match="xsw:transform">
    <xsl:param tunnel="yes" name="sourcedoc" as="document-node()"/>
    <xsl:variable name="xslt" as="document-node()">
      <!-- either a text node, or more production, or a transformation ... -->
      <xsl:apply-templates/>
    </xsl:variable>

    <!--<xsl:copy-of select="$xslt"/>-->
    <!-- don't just copy it, apply it -->
    <xsl:variable name="runtime"
      select="map {
      'xslt-version'        : if (empty(@version)) then 2.0 else xs:decimal(@version),
      'stylesheet-node'     : $xslt,
      'source-node'         : $sourcedoc }"/>
    <!-- The function returns a map; primary results are under 'output'
         unless a base output URI is given
         https://www.w3.org/TR/xpath-functions-31/#func-transform -->
    <xsl:sequence select="transform($runtime)?output"/>

  </xsl:template>

  <xsl:template match="xsw:annotate">
    <xsl:param tunnel="yes" name="sourcedoc" as="document-node()"/>
    <xsl:document>
      <xsl:processing-instruction expand-text="true" name="xsweet">{ . }</xsl:processing-instruction>
      <xsl:text>&#xA;</xsl:text>
      <xsl:sequence select="$sourcedoc"/>
    </xsl:document>
  </xsl:template>
  
  <!-- Text only value is taken to be a (relative) URL; XML at that location is produced. -->
  <xsl:template match="text()[. castable as xs:anyURI]">
    <xsl:sequence select="document(.)"/>
  </xsl:template>
  
  <!-- Not knowing any better, we simply pass along. -->
  <xsl:template match="node()">
    <xsl:param tunnel="yes" name="sourcedoc" as="document-node()"/>
    <xsl:sequence select="$sourcedoc"/>
  </xsl:template>

  <!-- Next up: an xsw:annotate element! -->

</xsl:stylesheet>
