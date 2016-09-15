<?xml version="1.0"?>
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
<!-- Stylesheet to convert dtbook3 version 3-06 to dtbook-2005-1
     James Pritchett, RFB&D
	 Based on dtb36to39.xsl (Aug 2001)
     April 2006
-->
<!-- Modified by Alex Bernier, BrailleNet, Jan 2009 -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="no" 
		    encoding="UTF-8"
		    doctype-public="-//NISO//DTD dtbook 2005-1//EN"
		    doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-1.dtd"
		    indent="yes"
		    method="xml" />

<!-- Utility: copy all attributes and convert @lang to @xml:lang -->
<xsl:template name="copyAtts">
	<xsl:for-each select="@*" >
		<xsl:choose>
			<xsl:when test="name()='lang'">
				<xsl:if test="not(../@xml:lang)">
					<xsl:attribute name="xml:lang">
						<xsl:value-of select="." />
					</xsl:attribute>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="." />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:for-each>
</xsl:template>

<xsl:template match="*">
	<xsl:copy>
		<xsl:if test="name()!='head' and name()!='meta'">
			<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		</xsl:if>
		<xsl:call-template name="copyAtts"/>
		<xsl:apply-templates />
	</xsl:copy>
</xsl:template>

<!-- Root element name is changed, plus add version & namespace -->
<xsl:template match="dtbook">
	<dtbook version="2005-1">
		<xsl:for-each select="@*" >
			<xsl:choose>
				<xsl:when test="name()='version'"/>
				<xsl:when test="name()='lang'">
					<xsl:if test="not(../@xml:lang)">
						<xsl:attribute name="xml:lang">
							<xsl:value-of select="." />
						</xsl:attribute>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="." />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		<xsl:apply-templates />
	</dtbook>
</xsl:template>

<!-- wrap non-level block items in fromtmatter in a level -->
<xsl:template match="frontmatter|bodymatter|rearmatter">
	<xsl:copy>
		<xsl:call-template name="copyAtts"/>
		<xsl:for-each-group select="*" group-adjacent="local-name()='level' or local-name()='level1' or local-name()='doctitle' or local-name()='docauthor'">
			<xsl:choose>
				<xsl:when test="current-grouping-key()">
					<xsl:for-each select="current-group()">
						<xsl:apply-templates select="."/>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<level depth="1">
						<xsl:for-each select="current-group()">
							<xsl:apply-templates select="."/>
						</xsl:for-each>
					</level>	
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each-group>
	</xsl:copy>
</xsl:template>

<!-- levels containing only a heading is appended an empty paragraph -->
<xsl:template match="level|level1|level2|level3|level4|level5|level6">
	<xsl:copy>
		<xsl:call-template name="copyAtts"/>
		<xsl:apply-templates/>
		<xsl:if test="count(*[name()!='hd' and name()!='h1' and name()!='h2' and name()!='h3' and name()!='h4' and name()!='h5' and name()!='h6'])=0">
			<p/>
		</xsl:if>
	</xsl:copy>
</xsl:template>
 
<!-- levelhd is transformed to hd, including all attributes excluding the depth attribute -->
<xsl:template match="levelhd">
	<hd>
		<xsl:for-each select="@*" >
			<xsl:choose>
				<xsl:when test="name()='lang'">
					<xsl:if test="not(../@xml:lang)">
						<xsl:attribute name="xml:lang">
							<xsl:value-of select="." />
						</xsl:attribute>
					</xsl:if>
				</xsl:when>
				<xsl:when test="name()='depth'" />
				<xsl:otherwise>
					<xsl:copy-of select="." />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		<xsl:apply-templates />
	</hd>
</xsl:template>

<!-- Kill hr's, style's, and head/title's -->
<xsl:template match="hr | style | head/title" />

<!-- prodnote.photoDesc is now an imggroup, 
     and the span.photoCaption inside it is a caption -->
<xsl:template match="prodnote[@class='photoDesc']">
	<imggroup>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:apply-templates />
	</imggroup>
</xsl:template>

<xsl:template match="prodnote/span[@class='photoCaption']">
	<caption>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:apply-templates />
	</caption>
</xsl:template>

<!-- spans of classes sidebarHead and sidebarTitle become hd's -->
<xsl:template match="span[@class='sidebarHead']">
	<hd>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:copy-of select="@*" />
		<xsl:apply-templates />
	</hd>
</xsl:template>

<xsl:template match="span[@class='sidebarTitle']">
	<hd>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:copy-of select="@*" />
		<xsl:apply-templates />
	</hd>
</xsl:template>

<!-- span.defineTerm is now em.defineTerm -->
<xsl:template match="span[@class='defineTerm']">
	<em>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:copy-of select="@*" />
		<xsl:apply-templates />
	</em>
</xsl:template>

<xsl:template match="bdo">
	<bdo>
		<xsl:copy-of select="@*" />
		<xsl:apply-templates />
	</bdo>
</xsl:template>

<!-- sidebars and prodnotes need @render (default = optional) -->
<xsl:template match="sidebar | prodnote">
	<xsl:copy>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:attribute name="render">optional</xsl:attribute>
		<xsl:call-template name="copyAtts"/>
		<xsl:apply-templates />
	</xsl:copy>
</xsl:template>

<!-- Convert the various specialized lists to the generic list element -->
<!-- Ought not be needed, since dtbook v1.1.0 does not contain ol or ul lists 
<xsl:template match="ol">
	<list type="ol">
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:for-each select="@*">
		    <xsl:choose>
			<xsl:when test="name()!='style'">
				<xsl:copy-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="enum"><xsl:value-of select="."/></xsl:attribute>
			</xsl:otherwise>
		    </xsl:choose>
		</xsl:for-each>
		<xsl:apply-templates />
	</list>
</xsl:template>

<xsl:template match="ul">
	<list type="ul">
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:copy-of select="@*" />
		<xsl:apply-templates />
	</list>
</xsl:template>
-->
 
<!-- lin becomes line -->
<xsl:template match="lin">
	<line>
		<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
		<xsl:call-template name="copyAtts"/>
		<xsl:apply-templates />
	</line>
</xsl:template>

</xsl:stylesheet>
