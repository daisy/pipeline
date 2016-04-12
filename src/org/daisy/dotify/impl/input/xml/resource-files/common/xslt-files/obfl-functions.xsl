<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
		exclude-result-prefixes="xs obfl"
		xmlns="http://www.daisy.org/ns/2011/obfl">
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	
	<!-- Wraps all nodes that are not sequences in a sequence -->
	<xsl:function name="obfl:wrapSequence">
		<xsl:param name="nodeList" as="node()"/>
		<xsl:param name="master" as="xs:string"/>
		<xsl:for-each select="$nodeList/node()[
			position()=1 or
			self::obfl:sequence or
			preceding-sibling::node()[1][self::obfl:sequence]]">
			<xsl:choose>
				<xsl:when test="self::obfl:sequence">
					<xsl:copy-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<sequence>
						<xsl:attribute name="master" select="$master"/>
						<xsl:variable name="result">
							<xsl:apply-templates select="." mode="whileNotSequence"/>
						</xsl:variable>
						<xsl:copy-of select="obfl:wrapBlocks($result)"></xsl:copy-of>
					</sequence>						
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:function>
	
	<xsl:template match="node()" mode="whileNotSequence">
		<xsl:if test="not(self::obfl:sequence)">
			<xsl:copy-of select="."></xsl:copy-of>
			<xsl:apply-templates select="following-sibling::node()[1]" mode="whileNotSequence"/>
		</xsl:if>	
	</xsl:template>
	
	<!-- Wraps all nodes that are not block elements (block, table, xml-data) in a block -->
	<xsl:function name="obfl:wrapBlocks">
		<xsl:param name="nodeList" as="node()"/>
		<xsl:for-each select="$nodeList/node()[
			position()=1 or
			obfl:allowedInSequence(.) or
			preceding-sibling::node()[1][obfl:allowedInSequence(.)]
			]">
			<xsl:choose>
				<xsl:when test="obfl:allowedInSequence(.)">
					<xsl:copy-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<block>
						<xsl:apply-templates select="." mode="whileNotBlock"/>
					</block>							
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:function>
	
	<xsl:template match="node()" mode="whileNotBlock">
		<xsl:if test="not(obfl:allowedInSequence(.))">
			<xsl:copy-of select="."></xsl:copy-of>
			<xsl:apply-templates select="following-sibling::node()[1]" mode="whileNotBlock"/>
		</xsl:if>	
	</xsl:template>

	<xsl:function name="obfl:allowedInSequence" as="xs:boolean">
		<xsl:param name="node" as="node()"/>
		<xsl:value-of select="boolean($node[self::obfl:block or self::obfl:table or self::obfl:xml-data])"/>
	</xsl:function>

</xsl:stylesheet>
