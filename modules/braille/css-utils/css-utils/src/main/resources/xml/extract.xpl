<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0"
                type="css:extract"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-inline-prefixes="#all"
                name="main">
	
	<p:documentation>
		Extract inlined CSS (defined in attributes) into an external stylesheet. This is the inverse
		of css:inline.
	</p:documentation>
	
	<p:input port="source">
		<p:documentation>
			The source document must have inlined CSS defined in attributes according to
			http://braillespecs.github.io/braille-css/#h2_style-attribute. The target medium is
			assumed to be 'embossed'.
		</p:documentation>
	</p:input>
	
	<p:output port="result" primary="true">
		<p:documentation>
			'id' attributes are added to the document as needed to attach styles to it from an
			external stylesheet.
		</p:documentation>
	</p:output>
	
	<p:output port="stylesheet">
		<p:documentation>
			The extracted stylesheet, wrapped in a 'c:result' element.
		</p:documentation>
		<p:pipe step="xslt" port="secondary"/>
	</p:output>
	
	<p:option name="attribute-name" required="false" select="'style'">
		<p:documentation>
			Name of attribute used for inlined styles. Default name is 'style'.
		</p:documentation>
	</p:option>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:error
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
		<p:documentation>
			px:set-base-uri
		</p:documentation>
	</p:import>
	
	<p:choose>
		<p:when test="contains($attribute-name,':')">
			<px:error code="BRL01" message="Invalid `attribute-name` option specified: '$1'. No namespace prefix allowed.">
				<p:with-option name="param1" select="$attribute-name"/>
			</px:error>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
	<p:xslt name="xslt">
		<p:input port="stylesheet">
			<p:document href="extract.xsl"/>
		</p:input>
		<p:with-param name="attribute-name" select="$attribute-name"/>
	</p:xslt>
	
	<!--
	    FIXME: for some reason output-base-uri option does not work when there are documents on
	    secondary port
	-->
	<px:set-base-uri>
		<p:with-option name="base-uri" select="base-uri(/)">
			<p:pipe step="main" port="source"/>
		</p:with-option>
	</px:set-base-uri>
	
	<p:choose>
		<p:when test="count(distinct-values((//@id|//@xml:id)/string())) != count(//*[@id or @xml:id])">
			<px:error code="XXXXX" message="Runtime error: document contains duplicate IDs"/>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
