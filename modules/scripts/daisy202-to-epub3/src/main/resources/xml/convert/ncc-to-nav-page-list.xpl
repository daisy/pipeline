<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:ncc-to-epub3-nav-page-list" name="main">

	<p:input port="source">
		<p:documentation  xmlns="http://www.w3.org/1999/xhtml">
			<p>A NCC</p>
		</p:documentation>
	</p:input>
	<p:output port="result" sequence="true">
		<p:documentation  xmlns="http://www.w3.org/1999/xhtml">
			<p>A <code>epub:type='page-list'</code> document for inclusion in a EPUB navigation
			document.</p>
		</p:documentation>
	</p:output>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="ncc-to-nav-page-list.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	
	<p:choose>
		<p:when test="count(/*/*)=0">
			<p:identity>
				<p:input port="source">
					<p:empty/>
				</p:input>
			</p:identity>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

</p:declare-step>
