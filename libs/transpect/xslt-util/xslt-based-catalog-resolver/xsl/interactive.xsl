<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
  xmlns:tr="http://transpect.io"
  xmlns:style="http://saxonica.com/ns/html-style-property"
  xmlns:ixsl="http://saxonica.com/ns/interactiveXSLT"
  xmlns:prop="http://saxonica.com/ns/html-property"
  exclude-result-prefixes="xs cat tr"
  extension-element-prefixes="ixsl"
  version="2.0">

  <xsl:import href="resolve-uri-by-catalog.xsl"/>

  <xsl:template match="/">
    <!-- If we don’t want to read and expand it over and over again for every new request, 
      we need to store the expanded catalog in the HTML page. Apparently it isn’t possible 
      to keep it in a variable that will be evaluated when the stylesheet is executed first.
      What happens when the user pushes one of the buttons: / is not the original source
      document (the catalog) any more, it is the HTML page instead! Damn.
      It is also important that the HTML page is an XML document (as opposed to an HTML5
      document without an XML declaration and a namespace). If it was an HTML document,
      the case of the rewriteURI elements would have been folded to rewriteuri, and the 
      imported stylesheet wouldn’t work any more. --> 
    <xsl:result-document href="#catalog" method="ixsl:replace-content">
      <xsl:sequence select="tr:expand-nextCatalog(/)"/>
    </xsl:result-document>
    <xsl:for-each select="ixsl:page()//*[@class = 'loading']">
      <ixsl:set-attribute name="style:display" select="'none'"/>
    </xsl:for-each>
    <xsl:for-each select="ixsl:page()//*:button">
      <ixsl:set-attribute name="style:visibility" select="'visible'"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*[@id = ('abstract2repo', 'repo2abstract')]" mode="ixsl:onclick">
    <xsl:variable name="source" as="xs:string" select="replace(@id, '^(.+)2.+$', '$1')"/>
    <xsl:variable name="target" as="xs:string" select="replace(@id, '^.+2(.+)$', '$1')"/>
    <xsl:variable name="uri" select="//*[@id = $source]/@prop:value" as="xs:string*"/>
    <xsl:variable name="catalog" as="document-node(element(cat:catalog))">
      <xsl:document>
        <xsl:sequence select="//*[@id = 'catalog']/*"/>
      </xsl:document>
    </xsl:variable>
    <xsl:for-each select="//*[@id = $target]">
      <!-- There’s a catch. Don’t manipulate value. Use prop:value instead. 
        Otherwise the text in the input element might not update. -->
      <xsl:choose>
        <xsl:when test="$source = 'repo'">
          <ixsl:set-attribute name="prop:value" select="tr:reverse-resolve-uri-by-catalog($uri, $catalog)"/>    
        </xsl:when>
        <xsl:otherwise>
          <ixsl:set-attribute name="prop:value" select="tr:resolve-uri-by-catalog($uri, $catalog)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- If I use xsl:sequence instead of xsl:message, clicking will yield the following exception: 
    SEVERE: Exception java.lang.ClassCastException in mode: '{http://saxonica.com/ns/interactiveXSLT}onclick' event: '[object MouseEvent]: null
    Apparently because there’s a JS object in the result tree. But I’m afraid if we somehow get rid of the returned object
    (e.g., by just assigning the ixsl:call() result to a variable and never evaluating the variable), the function will never be called.
    -->
  <xsl:template match="*[@id = ('gothere')]" mode="ixsl:onclick">
    <xsl:message select="ixsl:call(ixsl:window(), 'open', string(//*[@id = 'repo']/@prop:value), 'Repo', 'scrollbars=1,height=600,width=800')"/>
  </xsl:template>
  
</xsl:stylesheet>