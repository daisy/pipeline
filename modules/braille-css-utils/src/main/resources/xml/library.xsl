<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:java="implemented-in-java"
                exclude-result-prefixes="#all">
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Parse a style sheet.</p>
            <p>The style sheet must be specified as a string or a node (or empty sequence). In case
            of a node the string value is taken. Additional information may be provided by
            specifying the style sheet as an attribute node. A `css:page` attribute is parsed as a
            @page rule. A `css:volume` attribute is parsed as a @volume rule. A `css:text-transform`
            attribute is parsed as a @text-transform rule. A `css:counter-style` attribute is parsed
            as a @counter-style rule. A `css:*` attribute with the name of a property is parsed as a
            property declaration. `attr()` values in `content` and `string-set` properties are
            evaluated.</p>
            <p>An optional parent style may be specified as a `Style` item().</p>
            <p>Return value is an optional `Style` item.</p>
        </desc>
    </doc>
    <xsl:function name="css:parse-stylesheet" as="item()?">
        <xsl:param name="stylesheet" as="item()?"/> <!-- xs:string|attribute() -->
        <xsl:sequence select="s:parse($stylesheet)">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/xpath/Style.java
            -->
        </xsl:sequence>
    </xsl:function>
    <xsl:function name="css:parse-stylesheet" as="item()?">
        <xsl:param name="stylesheet" as="item()?"/> <!-- xs:string|attribute() -->
        <xsl:param name="parent" as="item()?"/>
        <xsl:sequence select="s:parse($stylesheet,$parent)">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/xpath/Style.java
            -->
        </xsl:sequence>
    </xsl:function>
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Serialize a style sheet to a string.</p>
            <p>The style sheet must be specified as a `Style` item.</p>
            <p>An optional parent style may be specified as a `Style` item().</p>
        </desc>
    </doc>
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="stylesheet" as="item()*"/>
        <xsl:sequence select="string(s:merge($stylesheet))"/>
    </xsl:function>
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="stylesheet" as="item()*"/>
        <xsl:param name="parent" as="item()?"/>
        <xsl:sequence select="s:toString(s:merge($stylesheet),$parent)"/>
    </xsl:function>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Serialize a style sheet to a pretty, indented, string.</p>
            <p>The style sheet must be specified as a `Style` item.</p>
            <p>The unit that is used for indenting lines, must be specified as a string. Each line is prefixed with
            this string a number of times that corresponds with the nesting level.</p>
        </desc>
    </doc>
    <xsl:function name="css:serialize-stylesheet-pretty" as="xs:string">
        <xsl:param name="stylesheet" as="item()*"/>
        <xsl:param name="indent" as="xs:string"/>
        <xsl:sequence select="s:toPrettyString(s:merge($stylesheet),$indent)">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/xpath/Style.java
            -->
        </xsl:sequence>
    </xsl:function>

    <xsl:function name="css:style-attribute" as="attribute(style)?">
        <xsl:param name="style" as="item()*"/>
        <xsl:variable name="style" as="item()?"
                      select="if (count($style)&lt;2) then $style else s:merge($style)"/>
        <xsl:if test="$style">
            <xsl:variable name="style" as="xs:string" select="string($style)"/>
            <xsl:if test="not($style='')">
                <xsl:attribute name="style" select="$style"/>
            </xsl:if>
        </xsl:if>
    </xsl:function>

    <xsl:function name="css:parse-counter-styles" as="map(xs:string,item())">
        <xsl:param name="stylesheet">
            <!--
                input is either:
                - a string, in which case it should be a regular style sheet consisting of @counter-style rules, or
                - a `css:counter-style' attribute, in which case it should have the form "& style1 { ... } & style2 { ... }"
                - an external object item
            -->
        </xsl:param>
        <xsl:map>
            <xsl:variable name="stylesheet" as="item()?">
                <xsl:choose>
                    <xsl:when test="not(exists($stylesheet))"/>
                    <xsl:when test="string($stylesheet)=''"/>
                    <xsl:when test="$stylesheet instance of xs:string or
                                    $stylesheet instance of attribute()">
                        <xsl:sequence select="s:get(css:parse-stylesheet($stylesheet),'@counter-style')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="$stylesheet"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:for-each select="for $s in $stylesheet return s:keys($s)">
                <xsl:map-entry key="replace(.,'^&amp; ','')">
                    <xsl:sequence select="s:get($stylesheet,.)"/>
                </xsl:map-entry>
            </xsl:for-each>
        </xsl:map>
    </xsl:function>
    
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
