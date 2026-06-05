<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:z="http://www.daisy.org/ns/z3998/authoring/"
                xmlns:diagram="http://www.daisy.org/ns/z3998/authoring/features/description/"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:import href="zedai-to-html.xsl"/>

	<xsl:template match="/" priority="1">
		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<style type="text/css">
					body{
					    font-family:arial, sans-serif;
					    font-size:1em
					}
					h1{
					    font-size:1.2em;
					}
					h2{
					    font-size:1.1em;
					    color:rgb(0, 0, 110)
					}
					h2.about{
					    font-size:1em;
					    color:rgb(0, 0, 0)
					}
					div.container{
					    border-top:solid 1px rgb(0, 0, 255);
					    width:80%;
					    padding:5px;
					    margin-bottom:10px;
					    background-color:rgb(255, 255, 255)
					}
					div.about,
					div.access{
					    font-size:0.9em
					}
					div.annotation{
					    font-size:0.8em;
					    font-weight:bold;
					    width:60%;
					    border-top:1px solid rgb(0, 0, 0)
					}
					p.anno-hd{
					    color:rgb(0, 0, 110)
					}
					img{
					    color:rgb(0, 0, 255)
					}
					ul{
					    list-style-type:none
					}
					.center{
					    text-align:center
					}</style>
			</head>
			<body>
				<h1>DIAGRAM Description</h1>

				<xsl:for-each select="//diagram:description">
					<xsl:apply-templates select="."/>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="diagram:description">
		<xsl:apply-templates select="diagram:head"/>

		<xsl:apply-templates select="diagram:body"/>

		<xsl:if test="//z:meta[@property='dc:accessRights']">
			<div class="access center">
				<div>
					<xsl:value-of select="//z:meta[@property='dc:accessRights']"/>
				</div>
			</div>
		</xsl:if>
	</xsl:template>

	<xsl:template match="diagram:head">
		<div class="container about">
			<h2 class="about">About this description</h2>
			<ul>
				<li><strong>Author:</strong>&#160;&#160;<xsl:value-of
						select="z:meta[@property='dc:creator'][1]"/>, <xsl:value-of
						select="z:meta[@property='diagram:credentials'][1]"/></li>
				<li><strong>Target Age:</strong> &#160;&#160;<xsl:value-of
						select="z:meta[@property='diagram:targetAge']/@content"/></li>
				<li><strong>Target Grade:</strong>&#160;&#160;<xsl:value-of
						select="z:meta[@property='diagram:targetGrade']/@content"/></li>
			</ul>
		</div>
	</xsl:template>

	<xsl:template match="diagram:body/diagram:*">
		<div id="{@xml:id}" class="container">
			<h2>
				<xsl:sequence
					select="
				if (self::diagram:summary) then 'Summary'
				else if (self::diagram:longdesc) then 'Long Description'
				else if (self::diagram:simplifiedLanguageDescription) then 'Simplified Language Description'
				else if (self::diagram:tactile) then 'Tactile Image'
				else if (self::diagram:simplifiedImage) then 'Simplified Image'
				else ''
				"
				/>
			</h2>
			<xsl:apply-templates/>
			<xsl:if test="//z:annotation[@ref=current()/@xml:id]">
				<div class="annotation">
					<p class="anno-hd">Annotation added by <xsl:value-of
							select="//z:annotation[@ref=current()/@xml:id]/@by"/>:</p>
					<xsl:apply-templates select="//z:annotation[@ref=current()/@xml:id][1]/*"/>
				</div>
			</xsl:if>
		</div>
	</xsl:template>

	<xsl:template match="diagram:body/z:annotation"/>

</xsl:stylesheet>
