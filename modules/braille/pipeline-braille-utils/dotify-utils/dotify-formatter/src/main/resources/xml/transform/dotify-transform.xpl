<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dotify-transform" version="1.0" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:dotify="http://code.google.com/p/dotify/"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-inline-prefixes="#all">
	
	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:input port="parameters" kind="parameter" primary="false"/>
	
	<p:option name="output" select="pef"/> <!-- pef | obfl -->
	<p:option name="css-block-transform" required="true"/>
	<p:option name="text-transform" required="true"/>
	<p:option name="temp-dir" required="true"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl"/>
	<p:import href="../css-to-obfl.xpl"/>
	<p:import href="../obfl-normalize-space.xpl"/>
	
	<p:variable name="duplex"
	            select="(//c:param[@name='duplex' and not(@namespace[not(.='')])]/@value,'true')[.=('true','false')][1]">
		<p:pipe step="main" port="parameters"/>
	</p:variable>
	<p:variable name="skip-margin-top-of-page"
	            select="(//c:param[@name='skip-margin-top-of-page' and not(@namespace[not(.='')])]/@value,'false')[.=('true','false')][1]">
		<p:pipe step="main" port="parameters"/>
	</p:variable>
	
	<!-- for debug info -->
	<p:for-each><p:identity/></p:for-each>
	
	<px:transform>
		<p:with-option name="query" select="$css-block-transform"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</px:transform>
	
	<!-- for debug info -->
	<p:for-each><p:identity/></p:for-each>
	
	<pxi:css-to-obfl>
		<p:with-option name="text-transform" select="$text-transform"/>
		<p:with-option name="duplex" select="$duplex"/>
		<p:with-option name="skip-margin-top-of-page" select="$skip-margin-top-of-page"/>
	</pxi:css-to-obfl>
	
	<pxi:obfl-normalize-space/>
	
	<p:choose>
		<p:when test="$output='pef'">
			
			<!-- for debug info -->
			<p:for-each><p:identity/></p:for-each>
				
			<dotify:obfl-to-pef locale="und">
				<p:with-option name="mode" select="$text-transform"/>
				<p:input port="parameters">
					<p:pipe step="main" port="parameters"/>
				</p:input>
			</dotify:obfl-to-pef>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
