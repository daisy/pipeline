<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-add-entry" name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
  xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="px">

  <p:input port="source"/>
  <p:output port="result"/>

  <p:option name="href" required="true"/>
  <p:option name="media-type" select="''"/>
  <p:option name="ref" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
  <p:option name="original-href" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
  <p:option name="first" select="'false'"/>

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
  <p:import href="fileset-add-ref.xpl"/>

  <!--TODO awkward, add the entry with XProc, then perform URI cleanup-->
  <p:xslt name="href-uri">
    <p:with-param name="href" select="$href"/>
    <p:with-param name="original-href" select="$original-href"/>
    <p:with-param name="base" select="base-uri(/*)"/>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0" exclude-result-prefixes="#all">
          <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
          <xsl:param name="href" required="yes"/>
          <xsl:param name="original-href" required="yes"/>
          <xsl:param name="base" required="yes"/>
          <xsl:template match="/*">
            <d:file>
              <xsl:attribute name="href" select="if (/*[@xml:base] or not(matches($href,'^\w+:'))) then pf:relativize-uri(resolve-uri($href,$base),$base) else pf:normalize-uri($href)"/>
              <xsl:if test="not($original-href='')">
                <xsl:attribute name="original-href" select="if (/*[@xml:base]) then pf:normalize-uri(resolve-uri($original-href,$base)) else pf:normalize-uri($original-href)"/>
              </xsl:if>
            </d:file>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>
  <p:sink/>

  <p:group>
    <p:variable name="href-uri-ified" select="/*/@href">
      <p:pipe port="result" step="href-uri"/>
    </p:variable>
    
    <p:identity>
      <p:input port="source">
        <p:pipe port="source" step="main"/>
      </p:input>
    </p:identity>

    <p:choose name="check-base">
      <!--TODO replace by uri-utils 'is-relative' function-->
      <p:when test="not(/*/@xml:base) and not(matches($href-uri-ified,'^[^/]+:'))">
        <px:message message="Adding a relative resource to a file set with no base URI"/>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>

    <p:group name="new-entry">
      <p:output port="result"/>
      <!--Create the new d:file entry-->
      <p:add-attribute match="/*" attribute-name="media-type">
        <p:input port="source">
          <p:inline>
            <d:file/>
          </p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="$media-type"/>
      </p:add-attribute>
      <p:add-attribute match="/*" attribute-name="href">
        <p:with-option name="attribute-value" select="$href-uri-ified"/>
      </p:add-attribute>
      <p:add-attribute match="/*" attribute-name="original-href">
        <p:with-option name="attribute-value" select="/*/@original-href">
          <p:pipe port="result" step="href-uri"/>
        </p:with-option>
      </p:add-attribute>
      <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="base-uri(/*)">
          <p:pipe port="source" step="main"/>
        </p:with-option>
      </p:add-attribute>
      <p:delete match="@xml:base"/>
      <!--Clean-up the optional attributes-->
      <p:delete match="@media-type[not(normalize-space())]"/>
      <p:delete match="@original-href[not(normalize-space())]"/>
    </p:group>
    
    <!--Insert the entry as the last or first child of the file set - unless it already exists-->
    <p:identity>
      <p:input port="source">
        <p:pipe port="source" step="main"/>
      </p:input>
    </p:identity>
    <p:choose>
      <p:when test="/*/d:file[@href=$href-uri-ified]">
        <p:identity/>
      </p:when>
      <p:otherwise>
        <p:insert match="/*">
          <p:input port="insertion">
            <p:pipe port="result" step="new-entry"/>
          </p:input>
          <p:with-option name="position" select="if ($first='true') then 'first-child' else 'last-child'"/>
        </p:insert>
      </p:otherwise>
    </p:choose>

    <p:choose>
      <p:when test="$ref=''">
        <p:identity/>
      </p:when>
      <p:otherwise>
        <px:fileset-add-ref>
          <p:with-option name="href" select="$href-uri-ified"/>
          <p:with-option name="ref" select="$ref"/>
        </px:fileset-add-ref>
      </p:otherwise>
    </p:choose>

  </p:group>

</p:declare-step>
