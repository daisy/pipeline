<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <!-- XSweet: This XSLT has settings for using XHTML serialization rules (including a DOCTYPE declaration). -->
  
  <!-- Requests a tool use HTML5 serialization rules. -->
  <xsl:output method="xml" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
     doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  
  <!-- Otherwise, just copies. -->
  <xsl:mode on-no-match="deep-copy"/>
  
</xsl:stylesheet>