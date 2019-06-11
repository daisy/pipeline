<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:variable name="params" select="//c:param" as="element(c:param)+"/>
  
  <xsl:template match="/c:param-set">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="c:param">
    <xsl:copy>
      <xsl:copy-of select="@name"/>
      <xsl:attribute name="value" select="string-join(tr:resolve-param(@value, /c:param-set), '')"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="tr:resolve-param" as="xs:string*">
    <!--  *
          * This function expects a parameter value and a parameter-set. It resolves inline 
          parameters which follow the syntax ${name} with matching parameters in the 
          parameter-set. 
          * -->
    <xsl:param name="value" as="xs:string"/>
    <xsl:param name="param-set" as="element(c:param-set)"/>
    <xsl:variable name="param-regex" select="'\{\$(.+?)\}'" as="xs:string"/>
    <xsl:analyze-string select="$value" regex="{$param-regex}">
      <xsl:matching-substring>
        <xsl:variable name="result" select="$params[@name eq regex-group(1)]/@value" as="xs:string*"/>
        <xsl:choose>
          <xsl:when test="empty($result)">
            <xsl:message select="concat('&#xa;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~&#xa;',
              '~ UNDECLARED PARAMETER ', $value, ' FOUND IN PARAMETER SET!',
              '&#xa;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~&#xa;')" terminate="yes"/>
          </xsl:when>
          <xsl:when test="not(count($result) eq 1)">
            <xsl:message select="concat('&#xa;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~&#xa;',
              '~ PARAMETER ', $value, ' DECLARED TWICE!',
              '&#xa;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~&#xa;')" terminate="yes"/>
          </xsl:when>
          <xsl:when test="matches($result, $param-regex)">
            <xsl:value-of select="concat( '(', 
                                              string-join(tr:resolve-param($params[@name eq regex-group(1)]/@value, $param-set), ''),
                                         ')' )"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat( '(', $result, ')' )"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="." />
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>
  
</xsl:stylesheet>
