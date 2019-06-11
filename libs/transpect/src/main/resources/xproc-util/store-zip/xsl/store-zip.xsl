<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io" 
  version="2.0">

  <!-- input: hub document with zero or more @fileref´s
       output: c:zip-manifest element with a sequence of zero or more c:entry elements
   -->

  <xsl:param name="additional-filerefs-to-zip-root" select="('')"/>
  <xsl:param name="default-compression-level" select="'default'"/>
  <xsl:param name="default-compression-method" select="'deflated'"/>
  <xsl:param name="default-command" select="'update'"/>

  <xsl:template name="create-manifest-from-hub">
    <xsl:variable name="source-dir-uri" as="xs:string"
    select="replace(/*/*:info/*:keywordset[@role eq 'hub']/*:keyword[@role eq 'source-dir-uri'], '^(file:)/+', '$1///')"/>
    <xsl:variable name="hub-filerefs" select="(//@fileref[. ne ''])" as="item()*"/>
    <c:zip-manifest>
      <xsl:for-each select="$hub-filerefs[. ne '']">
        <xsl:sort select="." order="ascending"/>
        <xsl:variable name="fileref-without-container-info" as="xs:string"
          select="replace(., '^container[:]', '')"/>
        <c:entry>
          <xsl:attribute name="name" select="replace($fileref-without-container-info, '^word/', '')"/>
          <xsl:attribute name="href" select="concat($source-dir-uri, $fileref-without-container-info)"/>
          <xsl:attribute name="compression-method" select="tr:zip-compression-method-from-file-ext(.)"/>
          <xsl:attribute name="compression-level" select="$default-compression-level"/>
          <xsl:attribute name="command" select="$default-command"/>
        </c:entry>
      </xsl:for-each>
      <xsl:for-each select="tokenize($additional-filerefs-to-zip-root, '\s*file:')[ . ne '']">
        <c:entry>
          <xsl:attribute name="name" select="tokenize(., '/')[last()]"/>
          <xsl:attribute name="href" select="concat('file:', .)"/>
          <xsl:attribute name="compression-method" select="tr:zip-compression-method-from-file-ext(.)"/>
          <xsl:attribute name="compression-level" select="$default-compression-level"/>
          <xsl:attribute name="command" select="$default-command"/>
        </c:entry>
      </xsl:for-each>
    </c:zip-manifest>
  </xsl:template>

  <xsl:function name="tr:zip-compression-method-from-file-ext" as="xs:string">
    <xsl:param name="fileref" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="matches($fileref, '(docx|epub|idml|zip)$')">
        <xsl:value-of select="'stored'"/>
      </xsl:when>
      <xsl:when test="$default-compression-level ne 'deflated'">
        <xsl:value-of select="$default-compression-method"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:function>
</xsl:stylesheet>