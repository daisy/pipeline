<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>


  <!-- XSweet: A generalized HTML modifier with a configurable driver. Use to clean up and improve HTML. [1] -->
  <!-- Input: an HTML typescript file. -->
  <!-- Output: a copy, except with tweaks to the HTML as specified by the configuration. -->
  <!-- Note: runtime parameter `config` enables naming a config file. Its name must be suffixed `xml`. See the file `html-tweak-map.xml` for an example. The configuration provides for matching elements in the HTML based on regularities in 'style' or 'class' assignment. -->
  
  <xsl:param name="config" as="xs:string" required="yes"/>

  <!-- $mapping-spec provides the name of an XML document found at location on $method e.g. method='my-mapping.xml' -->
  <!-- if there is no such document it is empty -->
  <xsl:variable name="config-spec" as="xs:string?"
    select="$config[matches(., '\.xml$')] ! (if (doc-available(.)) then (.) else ())"/>


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
      <xsw:transform>
        <!-- We produce a stylesheet, which is applied -->
        <xsw:produce>
          <!-- xsw:produce with string value returns the parsed XML file at that location.
                   Here the mapping spec replaces the main source as input for this subpipeline -->
          <xsw:produce>{ $config-spec }</xsw:produce>
          <xsw:transform>make-html-tweak-xslt.xsl</xsw:transform>
        </xsw:produce>
      </xsw:transform>
      <xsw:annotate> touched by html tweak using { $config-spec} : { current-date() }</xsw:annotate>
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
      'xslt-version'        : if (empty(@version)) then 3.0 else xs:decimal(@version),
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
