<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    exclude-result-prefixes="dtb" version="2.0">

    
    <!-- move an element out from its parent and split the parent 
        TODO: fix generate-id() problem (see below)
        This issue is tracked here http://code.google.com/p/daisy-pipeline/issues/detail?id=37
    -->
    
    <xsl:output indent="yes" method="xml"/>
    
    <xsl:template name="move-elem-out">
        <xsl:param name="elem-to-move-name"/>
        <xsl:param name="split-into-elem-name"/>
        
        
        <xsl:variable name="elem" select="."/>
        <xsl:variable name="first-child" select="child::node()[1]"/>
        
<!--        <xsl:message>Moving <xsl:value-of select="$elem-to-move-name"/> out of <xsl:value-of select="local-name($elem)"/>.</xsl:message>-->
        
        <!-- move the element out a level -->
        <xsl:for-each-group select="*|text()[normalize-space()]"
            group-adjacent="local-name() = $elem-to-move-name">
            <xsl:choose>
                <!-- the target element itself-->
                <xsl:when test="current-grouping-key()">
                    <xsl:for-each select="current-group()">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                </xsl:when>
                
                <xsl:otherwise>
                    <!-- split the parent element -->
                    <xsl:choose>
                        <!-- split into many of the same element -->
                        <xsl:when test="local-name($elem) = $split-into-elem-name">
                            <xsl:element name="{local-name($elem)}"
                                namespace="http://www.daisy.org/z3986/2005/dtbook/">
                                
                                <xsl:apply-templates select="$elem/@*"/>
                                
                                <!-- for all except the first 'copy' of the original parent:
                                    don't copy the node's ID since then it will result in many nodes with the same ID -->
                                <xsl:if
                                    test="not(position() = 1 or local-name($first-child) = $elem-to-move-name)">
                                    <xsl:if test="$elem/@id">
                                        <!-- modifying the result of generate-id() by adding a character to the end
                                            seems to correct the problem of it not being unique; however, this 
                                            is an issue that should be explored in-depth -->
                                        <xsl:variable name="tmp" select="concat(generate-id(), 'z')"/>
                                        
                                        <xsl:attribute name="id" select="$tmp"/>
                                    </xsl:if>
                                </xsl:if>
                                
                                <xsl:apply-templates select="current-group()"/>
                                
                            </xsl:element>
                        </xsl:when>
                        <!-- split into a different element type than the original -->
                        <xsl:otherwise>
                            <xsl:choose>
                                <!-- for the first group, use the original element name -->
                                <xsl:when
                                    test="position() = 1 or local-name($first-child) = $elem-to-move-name">
                                    <xsl:element name="{local-name($elem)}"
                                        namespace="http://www.daisy.org/z3986/2005/dtbook/">
                                        <xsl:apply-templates select="$elem/@*"/>
                                        <xsl:apply-templates select="current-group()"/>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element name="{$split-into-elem-name}"
                                        namespace="http://www.daisy.org/z3986/2005/dtbook/">
                                        
                                        <xsl:apply-templates select="$elem/@*"/>
                                        
                                        <!-- for all except the first 'copy' of the original parent:
                                            don't copy the node's ID since then it will result in many nodes with the same ID -->
                                        <xsl:if test="$elem/@id">
                                            <xsl:variable name="tmp" select="generate-id()"/>
                                            
                                            <!-- modifying the result of generate-id() by adding a character to the end
                                                seems to correct the problem of it not being unique; however, this 
                                                is an issue that should be explored in-depth -->
                                            <xsl:attribute name="id"
                                                select="concat(generate-id(), 'z')"/>
                                        </xsl:if>
                                        
                                        <xsl:apply-templates select="current-group()"/>
                                        
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                            
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
    </xsl:template>
    
</xsl:stylesheet>
