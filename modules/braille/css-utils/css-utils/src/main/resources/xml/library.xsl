<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    
    <xsl:import href="braille-css.xsl"/>
    
    <!--
        In addition, import this library whenever the following function is used:
        
        * css:render-table-by
        
          documentation
          =============
          - first argument is value of render-table-by
          - second argument is table element
          - input must be an html or dtbook table, assumed to be valid
          - table-header-policy property on td and th elements must be declared in
            css:table-header-policy arguments
          - other css styles must be declared in style attributes. styles that will be recognized are:
            - ::table-by(<axis>) pseudo-elements on table element
            - ::list-item pseudo-elements on table element or ::table-by(<axis>) pseudo-elements
          - function returns copy of table element with inside a multi-level list of anonymous
            elements and copies of the td and th elements contained within the leaf elements
          - render-table-by and table-header-policy properties not copied to output
          - ::table-by(<axis>) and ::list-item styles are moved to style attributes of appropriate
            anonymous elements
          - tr, tbody, thead, tfoot, col and colgroup elements not copied to output
          - other elements (caption, pagenum?) copied to output before or after the generated list
          
    -->
    
</xsl:stylesheet>
