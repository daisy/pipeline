<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
  xmlns:tr="http://transpect.io" 
  exclude-result-prefixes="xs tr" 
  version="2.0">

  <xsl:import href="http://transpect.io/xslt-util/xslt-based-catalog-resolver/xsl/resolve-uri-by-catalog.xsl"/>
  <xsl:param name="cat:missing-next-catalogs-warning" as="xs:string" select="'no'"/>

  <xsl:param name="interface-language" select="'en'" as="xs:string"/>
  <xsl:param name="s9y1-path" as="xs:string?"/>
  <xsl:param name="s9y2-path" as="xs:string?"/>
  <xsl:param name="s9y3-path" as="xs:string?"/>
  <xsl:param name="s9y4-path" as="xs:string?"/>
  <xsl:param name="s9y5-path" as="xs:string?"/>
  <xsl:param name="s9y6-path" as="xs:string?"/>
  <xsl:param name="s9y7-path" as="xs:string?"/>
  <xsl:param name="s9y8-path" as="xs:string?"/>
  <xsl:param name="s9y9-path" as="xs:string?"/>
  <xsl:param name="filename" as="xs:string"/>
  <xsl:param name="required" as="xs:string"/>
  <xsl:param name="fallback" as="xs:string"/>
  <xsl:param name="set-xml-base-attribute" select="'yes'" as="xs:string"/>
  <xsl:param name="binary-resultpath-with-file-prefix" select="'no'" as="xs:string"/>
  
  <!-- variables-->

  <xsl:variable name="catalog" as="document-node(element(cat:catalog))?" select="collection()[cat:catalog]"/>

  <xsl:variable name="catalog-resolved-fallback" as="xs:string" select="tr:resolve-uri-by-catalog($fallback, $catalog)"/>

  <xsl:variable name="cascade" as="xs:string*"
    select="($s9y1-path, $s9y2-path, $s9y3-path, $s9y4-path, $s9y5-path, $s9y6-path, $s9y7-path, $s9y8-path, $s9y9-path)"/>

  <!-- initial templates -->
  
  <xsl:template name="main">
    <xsl:if test="empty($cascade) and $filename = ''">
      <xsl:message>load-cascaded: the cascade is empty. Are you supplying a paths document?</xsl:message>
    </xsl:if>
    <xsl:sequence select="if (empty($cascade))
                          then tr:load($catalog-resolved-fallback, $catalog-resolved-fallback) (: doc($fallback) :) (:didn't work well because we need tr:load to add an appropriate @xml:base :)
                          else tr:load($cascade, $filename)"/>
  </xsl:template>

  <xsl:template name="return-cascade-paths">
    <xsl:variable name="directory-from-filename" 
      select="string-join(tokenize($filename, '/')[position() != last()], '/')"/>
    <xsl:if test="matches($filename, '^file:/+')">
      <xsl:message>
        WARNING, load-cascaded, template return-cascade-paths, given file is an absolute file path: <xsl:value-of select="$filename"/>
      </xsl:message>
    </xsl:if>
    <xsl:variable name="fallback-directory"
      select="if($fallback ne '') 
              then string-join(tokenize($catalog-resolved-fallback, '/')[position() != last()], '/')
              else ()"/>
    <tr:results>
      <xsl:for-each select="for $i in $cascade return concat($i, $directory-from-filename), $fallback-directory">
        <tr:result path="{current()}"/>
      </xsl:for-each>
    </tr:results>
  </xsl:template>

  <xsl:template name="return-cascaded-binary-uri">
    <xsl:variable name="filename-only" select="tokenize($filename, '/')[last()]"/>
    <xsl:variable name="existing-binary-directory" select="(/descendant::c:directory[c:file/@name = $filename-only])[1]"/>
    <xsl:variable name="full-name" select="concat($existing-binary-directory/@xml:base, $filename-only)"/>
    <xsl:choose>
      <xsl:when test="$existing-binary-directory">
        <tr:result>
          <xsl:attribute name="uri">
            <xsl:message>load-cascaded binary: using <xsl:value-of select="$full-name"/></xsl:message>
            <xsl:choose>
              <xsl:when test="$binary-resultpath-with-file-prefix = ('no')">
                <xsl:value-of select="replace(
                                        replace(
                                          resolve-uri($full-name), 
                                          '^file:/+', 
                                          '/'), 
                                        '^/([a-z]:)', 
                                        '$1')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="resolve-uri($full-name)"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:attribute name="filename">
            <xsl:value-of select="replace(resolve-uri($full-name), '^file:(//)?(.+)\.\w+$', '$2')"/>
          </xsl:attribute>
        </tr:result>
      </xsl:when>
      <xsl:otherwise>
        <!-- add another xsl-choose to identify fallback (see function tr:load)? -->
        <xsl:message terminate="{$required}"> load-cascaded binary: no file available, <xsl:value-of select="$filename"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- functions -->
  
  <xsl:function name="tr:load" as="document-node(element(*))?">
    <xsl:param name="base-paths" as="xs:string+"/>
    <xsl:param name="file-name" as="xs:string"/>
    <xsl:variable name="full-name" select="string(resolve-uri($file-name, $base-paths[1]))" as="xs:string"/>
    <xsl:variable name="lang" select="replace($interface-language, '^([a-z]+).*$', '$1')"/>
    <xsl:variable name="l10n-name" 
      select="if ($base-paths[1] = $file-name) (: fallback case :)
              then $file-name
              else replace(
                     $full-name, 
                     '(\.[^.]+)$', 
                     concat('.', $lang, '$1')
                   )" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="doc-available($l10n-name)">
        <xsl:message>load-cascaded: using <xsl:value-of select="$l10n-name"/></xsl:message>
        <xsl:sequence select="tr:load-document-nodes($l10n-name)"/>
      </xsl:when>
      <xsl:when test="doc-available($full-name)">
        <xsl:message>load-cascaded: using <xsl:value-of select="$full-name"/></xsl:message>
        <xsl:sequence select="tr:load-document-nodes($full-name)"/>
      </xsl:when>
      <xsl:when test="doc-available(concat($full-name, '.xsl'))">
        <xsl:message>load-cascaded: using XSLT <xsl:value-of select="concat($full-name, '.xsl')"/> to obtain a document dynamically</xsl:message>
        <xsl:sequence select="tr:load-document-nodes(concat($full-name, '.xsl'))"/>
      </xsl:when>
      <xsl:when test="count($base-paths) eq 1">
        <xsl:choose>
          <xsl:when test="$fallback ne '' and doc-available($catalog-resolved-fallback)">
            <xsl:message>load-cascaded: using fallback <xsl:value-of select="$catalog-resolved-fallback"/></xsl:message>
            <xsl:sequence select="tr:load-document-nodes(xs:anyURI($catalog-resolved-fallback))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="{$required}"> load-cascaded: no file available, <xsl:value-of select="$file-name"/>
            </xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="tr:load($base-paths[position() gt 1], $file-name)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="tr:load-document-nodes" as="document-node(element(*))?">
    <xsl:param name="file-uri" as="xs:string"/>
    <xsl:apply-templates select="doc($file-uri)" mode="add-base">
      <xsl:with-param name="base" select="$file-uri"/>
    </xsl:apply-templates>
  </xsl:function>


  <!-- mode templates -->
  
  <xsl:template match="/*" mode="add-base">
    <xsl:param name="base" as="xs:string"/>
    <xsl:document>
      <xsl:copy>
        <xsl:if test="$set-xml-base-attribute eq 'yes'">
          <xsl:attribute name="xml:base" select="$base"/>
        </xsl:if>
        <!-- added due to compatibility with existdb. we experience some strange errors when running xslt in existdb -->
        <!--<xsl:copy-of select="@*, node()"/>-->
        <xsl:apply-templates select="@*, node()" mode="#current"/>
      </xsl:copy>
    </xsl:document>
  </xsl:template>

  <!-- added due to compatibility with existdb. -->
  <xsl:template match="*|@*" mode="add-base">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>