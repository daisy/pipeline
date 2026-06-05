<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dotify-transform" version="1.0" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                exclude-inline-prefixes="#all">
	
	<p:input port="source"/>
	<p:output port="result"/>
	
	<p:input port="parameters" kind="parameter" primary="false"/>
	
	<p:option name="output" select="pef"/> <!-- pef | obfl -->
	<p:option name="css-block-transform" required="true"/> <!-- empty means disable pre-translation -->
	<p:option name="document-locale" required="true"/>
	<p:option name="text-transform" required="true"/>
	<p:option name="braille-charset" select="''"/>
	<p:option name="medium" select="'embossed'"/>
	<p:option name="skip-margin-top-of-page" select="false()"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
		<p:documentation>
			px:transform
		</p:documentation>
	</p:import>
	<p:import href="../library.xpl">
		<p:documentation>
			px:obfl-to-pef
		</p:documentation>
	</p:import>
	<p:import href="../css-to-obfl.xpl">
		<p:documentation>
			pxi:css-to-obfl
		</p:documentation>
	</p:import>
	<p:import href="../obfl-normalize-space.xpl">
		<p:documentation>
			pxi:obfl-normalize-space
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:css-parse-medium
		</p:documentation>
	</cx:import>
	
	<p:choose px:progress=".12">
		<p:when test="$css-block-transform!=''">
			<px:transform px:message="Pre-translating document with {
			                            replace($css-block-transform,'\((input|output):css\)','')}">
				<p:with-option name="query" select="$css-block-transform"/>
				<p:input port="parameters">
					<p:pipe step="main" port="parameters"/>
				</p:input>
			</px:transform>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
	<pxi:css-to-obfl px:message="Transforming from CSS to OBFL" px:progress=".83">
		<p:with-option name="document-locale" select="$document-locale"/>
		<p:with-option name="text-transform" select="$text-transform"/>
		<p:with-option name="braille-charset" select="$braille-charset"/>
		<p:with-option name="medium" select="pf:css-parse-medium($medium)"/>
		<p:with-option name="skip-margin-top-of-page" select="$skip-margin-top-of-page"/>
	</pxi:css-to-obfl>
	
	<p:choose px:progress=".05">
		<p:when test="$output='pef'">
			<px:obfl-to-pef px:message="Transforming from OBFL to PEF" px:progress="1">
				<p:input port="parameters">
					<p:pipe step="main" port="parameters"/>
				</p:input>
			</px:obfl-to-pef>
		</p:when>
		<p:otherwise>
			<pxi:obfl-normalize-space px:progress="1"/>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
