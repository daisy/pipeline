<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:l="http://xproc.org/library"
                exclude-inline-prefixes="#all"
                type="px:zedai-validate" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Validate a ZedAI (ANSI/NISO Z39.98-2012 Authoring and Interchange) document.</p>
		<p>Does not throw errors. Validation issues are reported through log messages.</p>
	</p:documentation>

	<p:input port="source"/>
	<p:output port="result" primary="true"/>

	<p:option name="report-method" cx:type="port|log|error" select="'port'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Select the method used for reporting validation issues:</p>
			<dl>
				<dt>port</dt>
				<dd>Issues are reported on the xml-report and html-report output ports.</dd>
				<dt>log</dt>
				<dd>Issues are reported through warning messages.</dd>
				<dt>error</dt>
				<dd>Issues are reported through error messages and also trigger an XProc error.</dd>
			</dl>
		</p:documentation>
	</p:option>
	<p:option name="allow-aural-css-attributes" select="false()" cx:as="xs:boolean">
		<p:documentation>
			<p>Whether the input contains aural CSS attributes (attributes with namespace
			"http://www.daisy.org/ns/pipeline/tts").</p>
		</p:documentation>
	</p:option>

	<p:output port="xml-report" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h1>XML Report</h1>
			<p>Raw output from the RelaxNG validation.</p>
		</p:documentation>
		<p:pipe step="report" port="result"/>
	</p:output>
	<p:output port="html-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h1>HTML Report</h1>
			<p>A single HTML-formatted version of the validation report.</p>
		</p:documentation>
		<p:pipe step="html-report" port="result"/>
	</p:output>
	<p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h1>Validation status</h1>
			<p>Validation status (http://daisy.github.io/pipeline/StatusXML).</p>
		</p:documentation>
		<p:pipe step="report" port="status"/>
	</p:output>

	<p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
		<p:documentation>
			l:relax-ng-report
			px:report-errors
			px:combine-validation-reports
			px:validation-status
			px:validation-report-to-html
		</p:documentation>
	</p:import>
	<p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
		<p:documentation>
			px:css-speech-clean
		</p:documentation>
	</p:import>

	<p:choose px:progress="1/3">
		<p:when test="$allow-aural-css-attributes">
			<px:css-speech-clean>
				<p:documentation>Remove aural CSS attributes before validation</p:documentation>
			</px:css-speech-clean>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

	<l:relax-ng-report name="validate" px:progress="1/3">
		<p:input port="schema">
			<p:document href="../schema/z3998-book-1.0-latest/z3998-book.rng"/>
		</p:input>
	</l:relax-ng-report>
	<p:sink/>

	<p:identity>
		<p:input port="source">
			<p:pipe step="validate" port="report"/>
		</p:input>
	</p:identity>
	<p:choose px:progress="1/3" name="report">
		<p:when test="$report-method='port'">
			<p:output port="result" primary="true" sequence="true">
				<p:pipe step="xml-report" port="result"/>
			</p:output>
			<p:output port="status">
				<p:pipe step="status" port="result"/>
			</p:output>
			<p:variable name="base-uri" select="base-uri(/*)">
				<p:pipe step="main" port="source"/>
			</p:variable>
			<px:combine-validation-reports name="xml-report">
				<p:with-option name="document-type" select="'ZedAI'"/>
				<p:with-option name="document-name" select="tokenize($base-uri,'/')[last()]"/>
				<p:with-option name="document-path" select="$base-uri"/>
			</px:combine-validation-reports>
			<px:validation-status name="status"/>
			<p:sink/>
		</p:when>
		<p:otherwise>
			<p:output port="result" primary="true" sequence="true">
				<p:empty/>
			</p:output>
			<p:output port="status">
				<p:pipe step="status" port="result"/>
			</p:output>
			<px:report-errors>
				<p:input port="report">
					<p:pipe step="validate" port="report"/>
				</p:input>
				<p:with-option name="method" select="$report-method"/>
			</px:report-errors>
			<p:template name="status">
				<p:input port="template">
					<p:inline>
						<d:validation-status result="{$result}"/>
					</p:inline>
				</p:input>
				<p:with-param port="parameters" name="result" select="if (count(collection()//c:error)=0) then 'ok' else 'error'"/>
			</p:template>
			<p:sink/>
		</p:otherwise>
	</p:choose>

	<p:for-each name="html-report">
		<p:output port="result" sequence="true"/>
		<px:validation-report-to-html/>
	</p:for-each>

	<p:sink/>
	<p:identity cx:depends-on="validate">
		<p:input port="source">
			<p:pipe step="main" port="source"/>
		</p:input>
	</p:identity>

</p:declare-step>
