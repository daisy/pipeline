<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:sch="http://purl.oclc.org/dsdl/schematron"
                type="px:relax-ng-to-schematron"
                name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Extracts embedded Schematron rules from a RELAX NG schema.</p>
		<p>The output is empty if there are no Schematron rules.</p>
		<p>Currently ISO Schematron is supported (the "http://purl.oclc.org/dsdl/schematron" namespace).</p>
		<p>Compact syntax is not supported.</p>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result" sequence="true"/>

	<p:choose>
		<p:when test="//sch:*">
			<p:xslt>
				<p:input port="stylesheet">
					<p:document href="../xslt/relaxng2isoschematron.xsl"/>
				</p:input>
				<p:input port="parameters">
					<p:empty/>
				</p:input>
			</p:xslt>
		</p:when>
		<p:otherwise>
			<p:identity>
				<p:input port="source">
					<p:empty/>
				</p:input>
			</p:identity>
		</p:otherwise>
	</p:choose>

</p:declare-step>
