<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  version="2.0">
  
  <xsl:param name="hub-version" as="xs:string"/>
  
  <xsl:template match="c:model/@*">
    <xsl:text xml:space="preserve"> </xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="/">
    <xsl:variable name="hub-models" as="element(c:model)*">
      <xsl:if test="$hub-version != ''">
        <c:model
          href="{concat('http://www.le-tex.de/resource/schema/hub/', $hub-version, '/hub.rng')}"
          type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"/>
        <c:model
          href="{concat('http://www.le-tex.de/resource/schema/hub/', $hub-version, '/hub.rng')}"
          type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"/>
      </xsl:if>
    </xsl:variable>
    
    <!-- just in case the hub model was specified twice, on the models port and per passed parameter: -->
    <xsl:for-each-group select="collection()[position() gt 1]/c:models/c:model, $hub-models"
      group-by="string-join(for $att in @* return concat($att/name(), '=', $att),'__')">
      <xsl:processing-instruction name="xml-model">    
                <xsl:apply-templates select="@*"/>
              </xsl:processing-instruction>
      <xsl:text>&#xa;</xsl:text>
    </xsl:for-each-group>
    <xsl:copy-of select="*"/>
  </xsl:template>
  
</xsl:stylesheet>
