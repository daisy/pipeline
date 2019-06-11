<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="#all"
  xmlns:tr="http://transpect.io"
  xmlns="http://www.w3.org/1999/xhtml"
  version="2.0">
  
  <!-- this example demonstrates tr:flat-list-to-tree() 
  
       invoke with saxon -it:main -xsl:test.xsl
  -->
  
  <xsl:include href="flat-list-to-tree.xsl"/>
  
  <xsl:variable name="flat-list" as="element()+">
    <li class="toc_1">1</li>
    <li class="toc_1">2</li>
    <li class="toc_2">2.1</li>
    <li class="toc_2">2.2</li>
    <li class="toc_3">2.2.1</li>
    <li class="toc_3">2.2.2</li>
    <li class="toc_3">2.2.3</li>
    <li class="toc_1">3</li>
    <li class="toc_2">3.1</li>
    <li class="toc_2">3.2</li>
  </xsl:variable>
  
  <xsl:template name="main">
    <xsl:sequence select="tr:flat-list-to-tree($flat-list, 
                                               0, 
                                               3, 
                                               QName('http://www.w3.org/1999/xhtml', 'ol'), 
                                               'class', 
                                               '^[a-z]+_')"/>
  </xsl:template>
  
</xsl:stylesheet>