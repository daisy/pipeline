<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!-- ======= -->
    <!-- Parsing -->
    <!-- ======= -->
    
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
        <xsl:sequence select="ParseStylesheet:parse($stylesheet)"
                      xmlns:ParseStylesheet="org.daisy.pipeline.braille.css.saxon.impl.ParseStylesheetDefinition$ParseStylesheet">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/saxon/impl/ParseStylesheetDefinition.java
            -->
        </xsl:sequence>
    </xsl:function>
    <xsl:function name="css:parse-stylesheet" as="item()?">
        <xsl:param name="stylesheet" as="item()?"/> <!-- xs:string|attribute() -->
        <xsl:param name="parent" as="item()?"/>
        <xsl:sequence select="ParseStylesheet:parse($stylesheet,$parent)"
                      xmlns:ParseStylesheet="org.daisy.pipeline.braille.css.saxon.impl.ParseStylesheetDefinition$ParseStylesheet">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/saxon/impl/ParseStylesheetDefinition.java
            -->
        </xsl:sequence>
    </xsl:function>
    
    <!-- =========== -->
    <!-- Serializing -->
    <!-- =========== -->
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Serialize a style sheet to a string.</p>
            <p>The style sheet must be specified as a `Style` item.</p>
            <p>An optional parent style may be specified as a `Style` item().</p>
        </desc>
    </doc>
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="stylesheet" as="item()?"/>
        <xsl:sequence select="string($stylesheet)"/>
    </xsl:function>
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="stylesheet" as="item()?"/>
        <xsl:param name="parent" as="item()?"/>
        <xsl:sequence select="s:toString($stylesheet,$parent)"/>
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
        <xsl:sequence select="s:toPrettyString(s:merge($stylesheet),$indent)"/>
    </xsl:function>

    <xsl:function name="css:style-attribute" as="attribute(style)?">
        <xsl:param name="style" as="item()?"/>
        <xsl:if test="$style">
            <xsl:variable name="style" as="xs:string" select="string($style)"/>
            <xsl:if test="not($style='')">
                <xsl:attribute name="style" select="$style"/>
            </xsl:if>
        </xsl:if>
    </xsl:function>
    
    <!-- ======= -->
    <!-- Strings -->
    <!-- ======= -->
    
    <xsl:function name="css:string" as="element()*">
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="last-set" as="element()?"
                      select="$context/(self::*|preceding::*|ancestor::*)
                              [contains(@css:string-set,$name)]
                              [last()]"/>
        <xsl:choose>
            <xsl:when test="$context/ancestor::*/@css:flow[not(.='normal')]">
                <xsl:choose>
                    <xsl:when test="$last-set
                                    intersect $context/ancestor::*[@css:anchor][1]/descendant-or-self::*">
                        <xsl:variable name="value" as="element(css:string-set)?"
                                      select="s:toXml(css:parse-stylesheet($last-set/@css:string-set))/css:string-set
                                              [@name=$name][last()]"/>
                        <xsl:choose>
                            <xsl:when test="exists($value)">
                                <xsl:sequence select="$value/*"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="context" as="element()?"
                                              select="$last-set/(preceding::*|ancestor::*)[last()]
                                                      intersect $context/ancestor::*[@css:anchor][1]/descendant-or-self::*"/>
                                <xsl:if test="$context">
                                    <xsl:sequence select="css:string($name, $context)"/>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="anchor" as="xs:string" select="$context/ancestor::*/@css:anchor"/>
                        <xsl:variable name="context" as="element()?" select="collection()//*[@css:id=$anchor][1]"/>
                        <xsl:if test="$context">
                            <xsl:sequence select="css:string($name, $context)"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$last-set">
                <xsl:variable name="value" as="element(css:string-set)?"
                              select="s:toXml(css:parse-stylesheet($last-set/@css:string-set))/css:string-set
                                      [@name=$name][last()]"/>
                <xsl:choose>
                    <xsl:when test="exists($value)">
                        <xsl:sequence select="$value/*"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="context" as="element()?" select="$last-set/(preceding::*|ancestor::*)[last()]"/>
                        <xsl:if test="$context">
                            <xsl:sequence select="css:string($name, $context)"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>
