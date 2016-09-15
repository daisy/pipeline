<?xml version="1.0" encoding="UTF-8"?>
<!--
  org.daisy.util (C) 2005-2008 Daisy Consortium
  
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
--> 
<!-- Stylesheet to migrate a dtbook 2005-1 document to 2005-2 
     The only destructive action taken by this stylesheet is to remove any style attributes
     jpritchett@rfbd.org
    
     First version:  29 Oct 2007
     Revised, 1 Nov 2007
        - Fixed bug in handling of frontmatter that allowed @style to pass through
        - Tidied some things up
     Revised, 16 Nov 2007
     	- Fixed bug in handling of frontmatter that duplicated doctitle
     	- Fixed bug that stripped out comments
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" 
    xmlns="http://www.daisy.org/z3986/2005/dtbook/"
    exclude-result-prefixes="dtb">

<!-- Change #1:  Rewrite the public and system IDs -->
    <xsl:output encoding="utf-8" method="xml" version="1.0" indent="no"
        doctype-public="-//NISO//DTD dtbook 2005-2//EN"
        doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd"/>

<!-- Change #2:  Update dtbook/@version to "2005-2" -->
    <xsl:template match="dtb:dtbook">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:choose>
                    <xsl:when test="local-name()='version'">
                        <xsl:attribute name="version">2005-2</xsl:attribute>
                    </xsl:when>
                    <!-- All other attributes pass through unchanged -->
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

<!-- Change #3:  Rewrite frontmatter to conform to the new content model:
    (doctitle, covertitle?, docauthor*, (level | level1)*) 
-->
    <xsl:template match="dtb:frontmatter">
        <xsl:copy>
            <xsl:call-template name="killStyle" />
            <xsl:apply-templates select="dtb:doctitle" />
            <xsl:apply-templates select="dtb:covertitle "/>
            <xsl:apply-templates select="dtb:docauthor" />
            <xsl:apply-templates select="dtb:level | dtb:level1"/>
            </xsl:copy>
    </xsl:template>

<!-- Change #4:  Rename bdo/@lang to bdo/@xml:lang -->
    <xsl:template match="dtb:bdo">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:choose>
                    <xsl:when test="local-name()='lang'">
                        <xsl:attribute name="xml:lang"><xsl:value-of select="."/></xsl:attribute>
                    </xsl:when>
                    <!-- All other attributes pass through unchanged (except @style) -->
                    <xsl:when test="local-name()!='style'">
                        <xsl:copy-of select="." />
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>
 
<!-- Change #5:  Remove all instances of @style -->
<!-- Default template for all elements calls killStyle -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:call-template name="killStyle" />
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
<!-- This template is called to copy all attributes except @style -->
    <xsl:template name="killStyle">
        <xsl:for-each select="@*">
            <xsl:choose>
                <!-- Copy everything except @style -->
                <xsl:when test="local-name()!='style'">
                    <xsl:copy-of select="." />
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

<!-- ====  DEFAULT TEMPLATES ==== -->
    <xsl:template match="comment()|text()">
        <xsl:copy />
    </xsl:template>
</xsl:stylesheet>
