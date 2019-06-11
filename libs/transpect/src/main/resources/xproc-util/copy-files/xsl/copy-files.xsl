<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io">

  <!-- input: hub document with zero or more @fileref´s
       output: a sequence of c:entry elements
  -->

  <xsl:param name="retain-subpaths" select="'no'"/>
  <xsl:param name="change-uri-new-subpath" select="''"/>
  <xsl:param name="target-dir-uri"/>
  <xsl:param name="fileref-attribute-name-regex" select="'^fileref$'"/>
  <xsl:param name="fileref-hosting-element-name-regex" select="'^(audiodata|imagedata|textdata|videodata)$'"/>

  <xsl:variable name="source-dir-uri" as="xs:string"
    select="replace(
              /*/*:info/*:keywordset[@role eq 'hub']/*:keyword[@role eq 'source-dir-uri'], 
              '^(file:)/+', 
              '$1///'
            )"/>

  <xsl:template name="create-entries-from-hub">
    <xsl:variable name="hub-filerefs" 
      select="(//*[matches(name(), $fileref-hosting-element-name-regex)]
                  /@*[matches(name(), $fileref-attribute-name-regex)][. ne ''])" as="item()*"/>
    <c:copy-files xml:base="{base-uri(/*)}">
      <xsl:for-each select="$hub-filerefs[. ne '']">
        <xsl:sort select="." order="ascending"/>
        <c:entry>
          <xsl:attribute name="href" select="tr:expand-container-fileref(.)"/>
          <xsl:attribute name="target" select="tr:change-fileref(., $target-dir-uri)"/>
        </c:entry>
      </xsl:for-each>
    </c:copy-files>
  </xsl:template>

  <xsl:function name="tr:expand-container-fileref" as="xs:string">
    <xsl:param name="fileref" as="xs:string"/>
    <xsl:sequence select="replace($fileref, '^container[:]', $source-dir-uri)"/>
  </xsl:function>

  <xsl:function name="tr:change-fileref" as="xs:string">
    <xsl:param name="fileref" as="xs:string"/>
    <xsl:param name="prefix-path" as="xs:string?"/>
    <xsl:variable name="container-subdir" as="xs:string"
      select="replace($fileref, '^container[:]', '')"/>
    <xsl:variable name="normalized-uri" as="xs:string"
      select="tr:expand-container-fileref($fileref)"/>
    <xsl:choose>
      <xsl:when test="$retain-subpaths eq 'yes'">
        <xsl:value-of select="string-join(
                                (
                                  $change-uri-new-subpath, 
                                  substring-after($normalized-uri, $source-dir-uri)
                                )[ . ne ''], 
                                '/'
                              )"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string-join(
                                (
                                  $prefix-path, 
                                  $change-uri-new-subpath, 
                                  tokenize($normalized-uri, '/')[last()]
                                )[. ne ''], 
                                '/'
                              )"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="*[matches(name(), $fileref-hosting-element-name-regex)]
                        /@*[matches(name(), $fileref-attribute-name-regex)]" mode="change-uri">
    <xsl:attribute name="{name()}" select="tr:change-fileref(., '')"/>
  </xsl:template>

  <xsl:template match="node()|@*" mode="change-uri" priority="-1">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*"     mode="#current"/>
      <xsl:apply-templates select="node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>