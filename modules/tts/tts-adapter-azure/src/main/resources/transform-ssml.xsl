<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/10/synthesis"
                xmlns="http://www.w3.org/2001/10/synthesis"
                xpath-default-namespace="http://www.w3.org/2001/10/synthesis"
                exclude-result-prefixes="#all">

	<xsl:output omit-xml-declaration="yes"/>

	<xsl:param name="voice" required="yes" as="xs:string"/>
	<xsl:param name="speech-rate" as="xs:double" select="1.0"/>

	<!--
	    Format the SSML according to the Cognitive Speech service's rules:
	    https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-synthesis-markup-structure
	-->

	<xsl:template mode="#default copy" match="*" priority="1">
		<!-- xml:lang will normally be present on <s> elements, but we don't assume this is always the case -->
		<xsl:param name="lang" as="xs:string" tunnel="yes" select="((ancestor::*/@xml:lang)[last()],'und')[1]"/>
		<xsl:next-match>
			<xsl:with-param name="lang" tunnel="yes" select="(@xml:lang,$lang)[1]"/>
			<xsl:with-param name="skip-lang-attr" tunnel="yes" select="@xml:lang[.=$lang]"/>
		</xsl:next-match>
	</xsl:template>

	<xsl:template mode="#default copy" match="@xml:lang">
		<xsl:param name="skip-lang-attr" as="xs:string" tunnel="yes" required="yes"/>
		<xsl:if test="not($skip-lang-attr)">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="*">
		<xsl:param name="lang" as="xs:string" tunnel="yes" required="yes"/>
		<speak version="1.0">
			<xsl:attribute name="xml:lang" select="$lang"/>
			<voice name="{$voice}">
				<!--
				    https://learn.microsoft.com/en-us/azure/ai-services/speech-service/speech-synthesis-markup-structure#add-silence
				    The silence setting is applied to all text within <voice>. To avoid the need for
				    multiple voice elements, we take the average speed rate to compute the length of
				    sentence gaps.
				-->
				<xsl:variable name="average-speech-rate" as="xs:double">
					<xsl:variable name="speech-length" as="xs:double">
						<xsl:apply-templates mode="speech-length" select="."/>
					</xsl:variable>
					<xsl:sequence select="sum(descendant::text()/string-length(normalize-space(string(.))))
					                      div $speech-length"/>
				</xsl:variable>
				<mstts:silence type="Sentenceboundary" xmlns:mstts="http://www.w3.org/2001/mstts"
				               value="{format-number(500 div $average-speech-rate,'0')}ms"/>
				<xsl:choose>
					<xsl:when test="$speech-rate!=1.0 and descendant::text()[normalize-space(.) and not(ancestor::prosody[@rate])]">
						<prosody>
							<xsl:attribute name="rate" select="format-number($speech-rate,'0.00')"/>
							<xsl:apply-templates mode="copy" select=".">
								<xsl:with-param name="rate" tunnel="yes" select="$speech-rate"/>
							</xsl:apply-templates>
						</prosody>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="copy" select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</voice>
		</speak>
	</xsl:template>

	<!-- unwrap speak -->
	<xsl:template mode="copy" match="speak">
		<xsl:apply-templates mode="#current" select="node()"/>
	</xsl:template>

	<!-- unwrap token -->
	<xsl:template mode="copy" match="token">
		<xsl:apply-templates mode="#current" select="node()"/>
	</xsl:template>

	<xsl:template mode="copy" match="prosody[@rate]">
		<xsl:param name="rate" as="xs:double" tunnel="yes" select="1.0"/>
		<xsl:variable name="parent-rate" as="xs:double" select="$rate"/>
		<xsl:variable name="rate" as="xs:double">
			<xsl:apply-templates mode="speech-rate" select="."/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="(@* except @rate) or $rate!=$parent-rate">
				<xsl:element name="{local-name(.)}" namespace="{namespace-uri(.)}">
					<xsl:if test="$rate!=$parent-rate">
						<xsl:attribute name="rate" select="format-number($rate,'0.00')"/>
					</xsl:if>
					<xsl:apply-templates mode="#current" select="@* except @rate"/>
					<xsl:apply-templates mode="#current">
						<xsl:with-param name="rate" tunnel="yes" select="$rate"/>
					</xsl:apply-templates>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="#current"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- rename mark to bookmark: not needed: regular SSML marks also supported -->
	<!--
	<xsl:template mode="copy" match="mark">
		<bookmark>
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</bookmark>
	</xsl:template>

	<xsl:template mode="copy" match="mark/@name">
		<xsl:attribute name="mark" select="string(.)"/>
	</xsl:template>
	-->

	<xsl:template mode="copy" match="s:*">
		<xsl:element name="{local-name(.)}" namespace="{namespace-uri(.)}">
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:element>
	</xsl:template>

	<xsl:template mode="copy" match="@*|node()">
		<xsl:copy copy-namespaces="no">
			<xsl:apply-templates mode="#current" select="@*|node()"/>
		</xsl:copy>
	</xsl:template>


	<xsl:template mode="speech-length" as="xs:double" match="prosody[@rate]">
		<xsl:variable name="rate" as="xs:double">
			<xsl:apply-templates mode="speech-rate" select="."/>
		</xsl:variable>
		<xsl:next-match>
			<xsl:with-param name="rate" tunnel="yes" select="$rate"/>
		</xsl:next-match>
	</xsl:template>

	<xsl:template mode="speech-length" as="xs:double" match="*">
		<xsl:variable name="length" as="xs:double*">
			<xsl:apply-templates mode="#current"/>
		</xsl:variable>
		<xsl:sequence select="sum($length)"/>
	</xsl:template>

	<xsl:template mode="speech-length" as="xs:double" match="text()">
		<xsl:variable name="text" as="xs:string" select="normalize-space(string(.))"/>
		<xsl:choose>
			<xsl:when test="$text">
				<xsl:variable name="rate" as="xs:double">
					<xsl:apply-templates mode="speech-rate" select="."/>
				</xsl:variable>
				<xsl:sequence select="string-length($text) div $rate"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="0.0"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="speech-rate" as="xs:double" match="prosody[@rate]">
		<xsl:param name="rate" as="xs:double" tunnel="yes" select="1.0"/>
		<xsl:variable name="rate" as="xs:string" select="normalize-space(string(@rate))"/>
		<xsl:choose>
			<xsl:when test="matches($rate,'^[0-9]+$')">
				<!--
				    Azure interprets an absolute number as a relative value (see
				    https://learn.microsoft.com/en-us/azure/ai-services/speech-service/speech-synthesis-markup-voice#adjust-prosody)
				    so divide by the "normal" rate of 200 words per minute (see
				    https://www.w3.org/TR/CSS2/aural.html#voice-char-props).
				-->
				<xsl:sequence select="number($rate) div 200"/>
			</xsl:when>
			<xsl:when test="matches($rate,'^[0-9]+%$')">
				<!--
				    Azure interprets a percentage as a relative change (see
				    https://learn.microsoft.com/en-us/azure/ai-services/speech-service/speech-synthesis-markup-voice#adjust-prosody),
				    so convert to number without percentage.
				-->
				<xsl:sequence select="$speech-rate * (number(substring($rate,1,string-length($rate)-1)) div 100)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="$speech-rate * (
				                             if ($rate='x-slow')  then 0.4
				                        else if ($rate='slow')    then 0.6
				                        else if ($rate='fast')    then 1.5
				                        else if ($rate='x-fast')  then 2.5
				                        else                           1.0 (: medium, default, or illegal value :)
				                      )"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="speech-rate" as="xs:double" match="*|text()">
		<xsl:param name="rate" as="xs:double" tunnel="yes" select="1.0"/>
		<xsl:sequence select="$rate"/>
	</xsl:template>

</xsl:stylesheet>
