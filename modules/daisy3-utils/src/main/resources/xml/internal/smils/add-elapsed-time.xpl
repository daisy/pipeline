<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                type="px:daisy3-smil-add-elapsed-time"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Add <code>dtb:totalElapsedTime</code> metadata to SMIL documents.</p>
	</p:documentation>

	<p:input port="source" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>An ordered sequence of SMIL documents.</p>
			<p>The <code>dtb:totalElapsedTime</code> metadata fields are expected to be already
			present. Their <code>content</code> attributes will be updated with the correct
			values.</p>
		</p:documentation>
	</p:input>

	<p:output port="result" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The modified SMIL documents.</p>
		</p:documentation>
	</p:output>

	<p:xslt name="compute-durations" template-name="main">
		<p:input port="stylesheet">
			<p:document href="compute-elapsed-time.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	<p:sink/>

	<p:for-each>
		<p:iteration-source>
			<p:pipe step="main" port="source"/>
		</p:iteration-source>
		<p:variable name="doc-uri" select="base-uri(/*)"/>
		<p:viewport match="s:head/s:meta[@name='dtb:totalElapsedTime']">
			<p:add-attribute attribute-name="content" match="/*">
				<p:with-option name="attribute-value" select="//*[@doc=$doc-uri]/@duration">
					<p:pipe step="compute-durations" port="result"/>
				</p:with-option>
			</p:add-attribute>
		</p:viewport>
	</p:for-each>

</p:declare-step>
