<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io"
  xmlns="http://transpect.io"
  exclude-result-prefixes="xs c"
  version="2.0">
  
  <xsl:strip-space elements="*"/>
  
  <xsl:variable name="exclude-filter" select="/c:directory/c:param-set/c:param[@name eq 'exclude-filter']/@value" as="xs:string?"/>
  <xsl:variable name="exclude-filter-names" select="if($exclude-filter) 
    then tokenize(replace($exclude-filter, '[\(\)'']', ''), '\|')
    else ''" as="xs:string*"/>
  
  <xsl:template match="/">
    <xsl:processing-instruction name="xml-model">href="http://transpect.io/cascade/schema/cascade.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"</xsl:processing-instruction>
    <xsl:processing-instruction name="xml-model">href="http://transpect.io/cascade/schema/cascade.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"</xsl:processing-instruction>
    <conf>
      <xsl:variable name="content-base-uri-attribute" select="c:directory/c:param-set/c:param[@name eq 'content-base-uri']" as="element(c:param)?"/>
      <xsl:variable name="paths-xsl-uri-attribute" select="c:directory/c:param-set/c:param[@name eq 'paths-xsl-uri']" as="element(c:param)?"/>
      <xsl:if test="$content-base-uri-attribute">
        <xsl:attribute name="content-base-uri" select="$content-base-uri-attribute/@value"/>
      </xsl:if>
      <xsl:if test="$paths-xsl-uri-attribute">
        <xsl:attribute name="paths-xsl-uri" select="$paths-xsl-uri-attribute/@value"/>
      </xsl:if>
      <cascade>
        <!-- the following directory names are excluded from the cascade iteration -->
        <xsl:for-each select="$exclude-filter-names">
          <reserved name="{.}"/>
        </xsl:for-each>
        <xsl:apply-templates/>
      </cascade>
    </conf>
  </xsl:template>
  
  <xsl:template match="c:directory[not(@name = $exclude-filter-names)]">
    <clade name="{@name}">
      <xsl:variable name="inherited-clade-role-attribute" as="element(c:param)?"
                    select="(c:param-set/c:param[@name eq 'clade-role'], 
                             ancestor::c:directory[c:param-set/c:param[@name eq 'role']]/c:param-set/c:param[@name eq 'clade-role'][1])[1]"/>
      <xsl:attribute name="role" select="if($inherited-clade-role-attribute) then $inherited-clade-role-attribute/@value else 'default'"/>
      <!-- unfortunately, content element is necessary for paths.xsl -->
      <xsl:apply-templates select="c:param-set/c:param"/>
      <xsl:sequence select="c:create-content(c:param-set/c:param[@name eq 'content-roles']/@value, 0)"/>
      <xsl:apply-templates select="node() except c:param-set"/>
    </clade>
  </xsl:template>
  
  <xsl:function name="c:create-content" as="element(tr:content)">
    <!-- creates nested content elements based on a whitespace separated list of names, e.g.
       
       <c:param name="content-roles" value="volume issue work"/>
       
       is converted to:
       
       <content role="volume">
          <content role="issue">
            <content role="work"/>
          </content>
        </content>
  -->
    <xsl:param name="content-roles" as="xs:string?"/><!-- whitespace-separated list of content roles, e.g. 'publisher series work' -->
    <xsl:param name="index" as="xs:integer"/><!-- start index should be zero -->
    <xsl:choose>
      <xsl:when test="tokenize($content-roles, '\s')[$index + 1]">
        <content role="{tokenize($content-roles, '\s')[$index + 1]}">
          <xsl:if test="tokenize($content-roles, '\s')[$index + 2]">
            <xsl:sequence select="c:create-content($content-roles, $index + 1)"/>
          </xsl:if>
        </content>
      </xsl:when>
      <xsl:otherwise>
        <content role="work"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:template match="c:param">
    <param name="{@name}" value="{@value}"/>
  </xsl:template>
  
</xsl:stylesheet>