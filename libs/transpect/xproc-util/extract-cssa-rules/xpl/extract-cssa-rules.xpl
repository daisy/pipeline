<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:css="http://www.w3.org/1996/css" 
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:dbk="http://docbook.org/ns/docbook"
  xmlns:tr="http://transpect.io"  
  version="1.0"
  name="extract-cssa-rules"
  type="tr:extract-cssa-rules"
  >
  
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" />
  
  <p:input port="source" primary="true">
    <p:documentation>A Hub 1.1 document</p:documentation>
  </p:input>
  <p:input port="previous" sequence="true">
    <p:documentation>Optional: an old version of cssa.xml</p:documentation>
  </p:input>
  <p:output port="result" primary="true">
    <p:documentation>A document with a css:rules top-level element</p:documentation>
  </p:output>
  <p:serialization port="result" omit-xml-declaration="false" indent="true"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  
  <p:choose name="check-all-styles">
    <p:when test="not(/*/dbk:info/dbk:keywordset[@role eq 'hub']/dbk:keyword[@role eq 'used-rules-only'] = 'false')">
      <cx:message message="Please include all styles when generating Hub XML (all-styles=yes)"/>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>

  <p:filter name="filter" select="//css:rule"/>

  <p:wrap-sequence name="wrap" wrapper="css:rules"/>
  
  <p:xslt name="sort">
    <p:with-param name="filename" 
      select="(
                /*/dbk:info/dbk:keywordset[@role eq 'hub']/dbk:keyword[@role eq 'source-basename'], 
                replace(
                  tokenize(base-uri(/*), '/')[last()], 
                  '^([^.]+).*$', 
                  '$1'
                )
              )[1]">
      <p:pipe port="source" step="extract-cssa-rules"/>      
    </p:with-param>
    <p:input port="parameters"><p:empty/></p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:param name="filename"/>
          <xsl:template match="/css:rules">
            <xsl:copy>
              <xsl:copy-of select="@*"/>
              <xsl:attribute name="comment" select="string-join(($filename, string(current-dateTime())), '  ')"/>
              <xsl:apply-templates select="css:rule">
                <xsl:sort select="css:layout-type-sortkey(@layout-type)"/>
                <xsl:sort select="(@native-name, @name)[1]"/>
              </xsl:apply-templates>
            </xsl:copy>
          </xsl:template>
          <xsl:template match="css:rule">
            <xsl:copy copy-namespaces="no">
              <xsl:copy-of select="@name, @native-name, @layout-type"/>
              <xsl:perform-sort select="@*[not(name() = ('name', 'native-name', 'layout-type'))]">
                <xsl:sort select="name()"/>
              </xsl:perform-sort>
              <xsl:copy-of select="*"/>
            </xsl:copy>
          </xsl:template>
          <xsl:function name="css:layout-type-sortkey" as="xs:integer">
            <xsl:param name="type" as="xs:string?"/>
            <xsl:choose>
              <xsl:when test="$type = 'para'"><xsl:sequence select="0"/></xsl:when>
              <xsl:when test="$type = 'inline'"><xsl:sequence select="5"/></xsl:when>
              <xsl:when test="$type = 'table'"><xsl:sequence select="10"/></xsl:when>
              <xsl:when test="$type = 'cell'"><xsl:sequence select="15"/></xsl:when>
              <xsl:when test="$type = 'object'"><xsl:sequence select="20"/></xsl:when>
              <xsl:otherwise><xsl:sequence select="50"/></xsl:otherwise>
            </xsl:choose>
          </xsl:function>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>

  <p:xslt name="compare">
    <p:input port="source">
      <p:pipe port="result" step="sort"/>
      <p:pipe port="previous" step="extract-cssa-rules"/>
    </p:input>
    <p:input port="parameters"><p:empty/></p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:template match="/*">
            <xsl:copy>
              <xsl:copy-of select="@*"/>
              <xsl:apply-templates select="css:rule"/>
              <xsl:apply-templates mode="removed" 
                select="collection()[2]/css:rules/css:rule[not(css:name-equivalent(., collection()[1]/css:rules/css:rule))]"/>
            </xsl:copy>
          </xsl:template>
          <xsl:template match="css:rule">
            <xsl:variable name="old-rule" as="element(css:rule)*" 
              select="css:name-equivalent(., collection()[2]/css:rules/css:rule)"/>
            <xsl:if test="count($old-rule) gt 1">
              <xsl:message>More than one old rule found: <xsl:value-of select="$old-rule/@native-name"/></xsl:message>
            </xsl:if>
            <xsl:copy>
              <xsl:copy-of select="@name, @native-name, @layout-type"/>
              <xsl:copy-of select="$old-rule[1]/(@comment, @status)"/>
              <xsl:copy-of select="@* except (@name, @native-name, @layout-type), *"/>
            </xsl:copy>
          </xsl:template>
          <xsl:template match="css:rule" mode="removed">
            <xsl:text>&#xa;</xsl:text>
            <xsl:comment>REMOVED:<xsl:apply-templates select="@*" mode="#current"/>
            </xsl:comment>
          </xsl:template>
          <xsl:template match="@*" mode="removed">
            <xsl:text>&#xa;  </xsl:text>
            <xsl:value-of select="name()"/>=<xsl:value-of select="."/>
          </xsl:template>
          <xsl:function name="css:name-equivalent" as="element(css:rule)*">
            <xsl:param name="current-rule" as="element(css:rule)"/>
            <xsl:param name="other-rules" as="element(css:rule)*"/>
            <xsl:sequence select="$other-rules[@native-name = $current-rule/@native-name]"/>
          </xsl:function>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>

  <p:xslt name="prepend-xml-model">
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:template match="/">
            <xsl:text>&#xa;</xsl:text>
            <xsl:processing-instruction name="xml-model" 
                select="'href=&#x22;http://www.le-tex.de/resource/schema/hub/1.1/css/cssa-rules.rng&#x22; type=&#x22;application/xml&#x22; schematypens=&#x22;http://relaxng.org/ns/structure/1.0&#x22;'"/>
            <xsl:text>&#xa;</xsl:text>
            <xsl:copy-of select="*"/>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
    <p:input port="parameters"><p:empty/></p:input>
  </p:xslt>

</p:declare-step>
