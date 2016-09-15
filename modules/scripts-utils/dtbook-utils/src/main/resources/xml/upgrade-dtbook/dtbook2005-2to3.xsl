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
<!-- Stylesheet to migrate a dtbook 2005-2 document to 2005-3 
     This stylesheet performs no destructive actions
     jpritchett@rfbd.org
    
     First version:  13 Feb 2008
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" 
    xmlns="http://www.daisy.org/z3986/2005/dtbook/"
    exclude-result-prefixes="dtb">

<!-- Change #1:  Rewrite the public and system IDs -->
    <xsl:output encoding="utf-8" method="xml" version="1.0" indent="no"
        doctype-public="-//NISO//DTD dtbook 2005-3//EN"
        doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"/>

<!-- Change #2:  Update dtbook/@version to "2005-3" -->
    <xsl:template match="dtb:dtbook">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:choose>
                    <xsl:when test="local-name()='version'">
                        <xsl:attribute name="version">2005-3</xsl:attribute>
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

<!-- ====  DEFAULT TEMPLATES ==== -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>
    <xsl:template match="@*|comment()|text()">
        <xsl:copy />
    </xsl:template>
</xsl:stylesheet>
