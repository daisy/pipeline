<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:uppercase" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<p:input port="source"/>
	<p:output port="result"/>
	<p:input port="parameters" kind="parameter"/>
	<p:xslt>
		<p:input port="stylesheet">
			<p:inline>
				<xsl:stylesheet version="2.0">
					<xsl:param name="letter-spacing" as="xs:string" select="'0'"/>
					<xsl:variable name="_letter-spacing" as="xs:string"
					              select="string-join(for $i in 1 to xs:integer(number($letter-spacing)) return ' ', '')"/>
					<xsl:template match="*">
						<xsl:copy>
							<xsl:sequence select="@*"/>
							<xsl:apply-templates/>
						</xsl:copy>
					</xsl:template>
					<xsl:template match="text()">
						<xsl:value-of select="string-join(
						                        for $i in 1 to string-length(.) return upper-case(substring(.,$i,1)),
						                        $_letter-spacing)"/>
					</xsl:template>
				</xsl:stylesheet>
			</p:inline>
		</p:input>
	</p:xslt>
</p:declare-step>
