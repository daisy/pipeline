<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  exclude-result-prefixes="#all"
  
  version="2.0">
   
  <xsl:import href="docx-html-extract.xsl"/>
  
<!-- XSweet: A utility XSLT, for wrapping a call to docx-extract.xsl in logic to unpack a document.xml from its zip (docx file wrapper). Thus saving unzipping when running diagnostics. -->
  
  <!-- A 'shell' stylesheet, permitting us to pass a .docx file as an input parameter,
       using Java to retrieve the document.xml from inside it and process that file
       through imported templates (matching elements in the w: namespace), for "extraction" output. -->
  
  <!-- The full path (URI) to the input docx must be passed at runtime. -->
  <xsl:param    as="xs:string"  name="docx-file-uri" required="yes"/>
  
  <!-- Overriding imported binding  yes|no -->
  <xsl:param    as="xs:string"  name="show-css">yes</xsl:param>
  
  <xsl:output indent="no" omit-xml-declaration="yes"/>
 
 
  <xsl:variable name="document-path" select="concat('jar:',$docx-file-uri,'!/word/document.xml')"/>
  <xsl:variable name="document-xml"  select="document($document-path)"/>
  
  <xsl:template name="extract">
    <!-- Grabbing the document element of document.xml; imported templates will take over. -->
    <xsl:apply-templates select="$document-xml/*"/>
  </xsl:template>
  
  
</xsl:stylesheet>