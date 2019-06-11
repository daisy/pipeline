<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  xmlns:functx="http://www.functx.com"
  version="2.0">
  
  <xsl:import href="http://transpect.io/xslt-util/functx/xsl/functx.xsl"/>
  
  <xsl:param name="schematron-like-paths" select="'no'"/>
  <xsl:param name="exclude-elements"/>
  <xsl:param name="exclude-descendants"/>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*[not(name() = tokenize($exclude-elements, '\s') 
                         or ($exclude-descendants eq 'yes' 
                         and ancestor::*/name() = tokenize($exclude-elements, '\s')))]">
    <xsl:copy>
      <xsl:attribute name="srcpath" select="if($schematron-like-paths eq 'yes')
                                            then tr:path-to-node-with-pos-verbose(.)
                                            else functx:path-to-node-with-pos(.)"/>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="tr:path-to-node-with-pos-verbose" as="xs:string">
    <xsl:param name="node" as="node()?"/> 
    <xsl:variable name="names" as="xs:string*">
      <xsl:for-each select="$node/ancestor-or-self::*">
        <xsl:variable name="ancestor" select="."/>
        <xsl:variable name="sibsOfSameName" select="$ancestor/../*[name() = name($ancestor)]"/>
        <xsl:sequence select="concat('*:', local-name($ancestor),
          '[namespace-uri()=''', namespace-uri(), ''']',
          '[',functx:index-of-node($sibsOfSameName,$ancestor),']')"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:sequence select="concat('/', string-join($names,'/'))"/>
  </xsl:function>
  
</xsl:stylesheet>