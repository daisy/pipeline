<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:add-ids"
                exclude-inline-prefixes="#all">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Add missing IDs and fix duplicate IDs.</p>
	</p:documentation>

	<p:input port="source" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The input documents</p>
		</p:documentation>
	</p:input>
	<p:option name="match" required="false">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Elements that need an <code>id</code> attribute.</p>
			<p>Should be a XSLTMatchPattern that matches only elements.</p>
			<p>If omitted, no IDs are added, only duplicates fixed.</p>
		</p:documentation>
	</p:option>
	<p:output port="result" sequence="true" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>The processed documents</p>
			<p>All elements matched by the <code>match</code> expression have a <code>id</code>
			attribute.</p>
			<p>All <code>id</code> attributes are unique within the whole sequence of documents.</p>
		</p:documentation>
		<p:pipe step="result" port="result"/>
	</p:output>
	<p:output port="mapping">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p><code>d:fileset</code> document that represents the renaming of <code>id</code>
			attributes.</p>
		</p:documentation>
		<p:pipe step="mapping" port="result"/>
	</p:output>

	<p:declare-step type="pxi:iterate" name="iterate">
		<p:input port="previous-docs" primary="false" sequence="true">
			<p:empty/>
		</p:input>
		<p:input port="next-docs" primary="true" sequence="true"/>
		<p:input port="previous-mappings" primary="false" sequence="true">
			<p:empty/>
		</p:input>
		<p:output port="result" primary="true" sequence="true"/>
		<p:output port="mappings" primary="false" sequence="true">
			<p:pipe step="result" port="mappings"/>
		</p:output>
		<p:option name="next-doc" cx:as="xs:string" select="1"/>
		<p:count/>
		<p:choose name="result">
			<p:when test="/*&gt;0">
				<p:output port="result" primary="true" sequence="true"/>
				<p:output port="mappings" primary="false" sequence="true">
					<p:pipe step="recursive-call" port="mappings"/>
				</p:output>
				<p:sink/>
				<p:split-sequence initial-only="true" test="position()=1" name="next-doc">
					<p:input port="source">
						<p:pipe step="iterate" port="next-docs"/>
					</p:input>
				</p:split-sequence>
				<p:xslt name="xslt" template-name="main">
					<p:input port="source">
						<p:pipe step="iterate" port="previous-docs"/>
						<p:pipe step="iterate" port="next-docs"/>
					</p:input>
					<p:input port="stylesheet">
						<p:document href="add-ids.xsl"/>
					</p:input>
					<p:with-param name="next-doc" select="$next-doc"/>
					<p:with-option name="output-base-uri" select="base-uri(/)"/>
				</p:xslt>
				<p:sink/>
				<pxi:iterate name="recursive-call">
					<p:input port="previous-docs">
						<p:pipe step="iterate" port="previous-docs"/>
						<p:pipe step="xslt" port="result"/>
					</p:input>
					<p:input port="next-docs">
						<p:pipe step="next-doc" port="not-matched"/>
					</p:input>
					<p:input port="previous-mappings">
						<p:pipe step="iterate" port="previous-mappings"/>
						<p:pipe step="xslt" port="secondary"/>
					</p:input>
					<p:with-option name="next-doc" select="$next-doc + 1"/>
				</pxi:iterate>
			</p:when>
			<p:otherwise>
				<p:output port="result" primary="true" sequence="true"/>
				<p:output port="mappings" primary="false" sequence="true">
					<p:pipe step="iterate" port="previous-mappings"/>
				</p:output>
				<p:sink/>
				<p:identity>
					<p:input port="source">
						<p:pipe step="iterate" port="previous-docs"/>
					</p:input>
				</p:identity>
			</p:otherwise>
		</p:choose>
	</p:declare-step>

	<p:choose>
		<p:xpath-context>
			<p:empty/>
		</p:xpath-context>
		<p:when test="p:value-available('match')">
			<p:for-each>
				<p:add-attribute attribute-name="pxi:need-id" attribute-value="">
					<p:with-option name="match" select="$match"/>
				</p:add-attribute>
			</p:for-each>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

	<pxi:iterate name="result"/>
	<p:sink/>

	<p:wrap-sequence wrapper="d:fileset">
		<p:input port="source">
			<p:pipe step="result" port="mappings"/>
		</p:input>
	</p:wrap-sequence>
	<p:delete match="d:file[not(d:anchor)]"/>
	<p:identity name="mapping"/>
	<p:sink/>

</p:declare-step>
