<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:epub3-nav-to-guide">

	<p:input port="source"/>
	<p:output port="result"/>
	<p:option name="opf-base" required="true"/>
	
	<p:xslt>
		<p:with-param name="opf-base" select="$opf-base"/>
		<p:input port="stylesheet">
			<p:document href="nav-to-guide.xsl"/>
		</p:input>
	</p:xslt>

</p:declare-step>
