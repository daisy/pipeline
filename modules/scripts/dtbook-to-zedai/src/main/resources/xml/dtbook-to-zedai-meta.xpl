<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
                type="px:dtbook-to-zedai-meta"
                exclude-inline-prefixes="tmp p px">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Generate ZedAI inline metadata from a DTBook 2005-3 document.</p>
		<div px:role="author maintainer">
			<p px:role="name">Marisa DeMeglio</p>
			<a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
			<p px:role="organization">DAISY Consortium</p>
		</div>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result"/>

	<p:xslt>
		<p:input port="stylesheet">
			<p:inline>
				<!-- This is a wrapper to XML-ify the output.  XProc will only accept it this way. -->
				<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
					<xsl:import href="dtbook-to-zedai-meta.xsl"/>
					<xsl:template match="/">
						<tmp:wrapper>
							<xsl:apply-imports/>
						</tmp:wrapper>
					</xsl:template>
				</xsl:stylesheet>
			</p:inline>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>

</p:declare-step>
