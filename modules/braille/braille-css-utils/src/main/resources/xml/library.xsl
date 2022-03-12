<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:java="implemented-in-java">
    
    <xsl:import href="braille-css.xsl"/>
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Render a table as a (nested) list.</p>
        </desc>
        <!--
            - first argument is value of render-table-by
            - second argument is table element
            - input must be an html or dtbook table, assumed to be valid
            - table-header-policy property on td and th elements must be declared in
              css:table-header-policy arguments
            - other css styles must be declared in style attributes. styles that will be recognized are:
              - ::table-by(<axis>) pseudo-elements on table element
              - ::list-item pseudo-elements on table element or ::table-by(<axis>) pseudo-elements
            - function returns copy of table element with inside a multi-level list of css:table-by
              and css:list-item elements and copies of the td and th elements contained within the
              leaf elements
            - render-table-by and table-header-policy properties not copied to output
            - ::table-by(<axis>) and ::list-item styles are moved to style attributes of corresponding
              generated elements
            - tr, tbody, thead, tfoot, col and colgroup elements not copied to output
            - other elements (caption, pagenum?) copied to output before or after the generated list
        -->
    </doc>
    <java:function name="css:render-table-by" as="element()">
        <xsl:param name="render-table-by" as="xs:string"/>
        <xsl:param name="table" as="element()"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/css/saxon/impl/RenderTableByDefinition.java
        -->
    </java:function>

</xsl:stylesheet>
