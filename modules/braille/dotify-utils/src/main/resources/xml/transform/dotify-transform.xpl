<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dotify-transform" version="1.0" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:dotify="http://code.google.com/p/dotify/"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
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
	<p:import href="../library.xpl">
		<p:documentation>
			dotify:obfl-to-pef
		</p:documentation>
	</p:import>
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
	
	<px:transform px:message="Translating document with {
	                            replace($css-block-transform,'\((input|output):css\)','')}"
	              px:progress=".12">
		<p:with-option name="query" select="$css-block-transform"/>
		<p:with-option name="temp-dir" select="$temp-dir"/>
		<p:input port="parameters">
			<p:pipe step="main" port="parameters"/>
		</p:input>
	</px:transform>
	
	<!-- for debug info -->
	<p:for-each><p:identity/></p:for-each>
	
	<pxi:css-to-obfl px:message="Transforming from CSS to OBFL" px:progress=".83">
		<p:with-option name="text-transform" select="$text-transform"/>
		<p:with-option name="duplex" select="$duplex"/>
		<p:with-option name="skip-margin-top-of-page" select="$skip-margin-top-of-page"/>
	</pxi:css-to-obfl>
	
	<pxi:obfl-normalize-space name="obfl" px:progress=".01"/>
	
	<p:choose px:progress=".04">
		<p:when test="$output='pef'">
			
			<!-- for debug info -->
			<p:for-each><p:identity/></p:for-each>

			<!--
			    Follow the OBFL standard which says that "when volume-transition is present, the
			    last page or sheet in each volume may be modified so that the volume break occurs
			    earlier than usual: preferably between two blocks, or if that is not possible,
			    between words" (http://braillespecs.github.io/obfl/obfl-specification.html#L8701).
			    In other words, volumes should by default not be allowed to end on a hyphen.
			-->
			<px:add-parameters name="allow-ending-volume-on-hyphen">
				<p:input port="source">
					<p:empty/>
				</p:input>
				<p:with-param name="allow-ending-volume-on-hyphen"
				              select="if (/*/obfl:volume-transition) then 'false' else 'true'">
					<p:pipe step="obfl" port="result"/>
				</p:with-param>
			</px:add-parameters>
			<px:merge-parameters name="parameters">
				<p:input port="source">
					<p:pipe step="allow-ending-volume-on-hyphen" port="result"/>
					<p:pipe step="main" port="parameters"/>
				</p:input>
			</px:merge-parameters>
			<dotify:obfl-to-pef px:message="Transforming from OBFL to PEF" px:progress="1"
			                    locale="und">
				<p:input port="source">
					<p:pipe step="obfl" port="result"/>
				</p:input>
				<p:with-option name="mode" select="$text-transform"/>
				<p:input port="parameters">
					<p:pipe step="parameters" port="result"/>
				</p:input>
			</dotify:obfl-to-pef>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
