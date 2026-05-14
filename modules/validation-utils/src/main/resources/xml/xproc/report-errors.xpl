<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:report-errors"
                name="main">

	<p:input port="source" primary="true"/>
	<p:input port="report" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Zero or more <a
			href="https://www.w3.org/TR/xproc/#cv.errors"><code>c:errors</code></a> documents.</p>
		</p:documentation>
	</p:input>
	<p:output port="result" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Copy of <code>source</code></p>
		</p:documentation>
	</p:output>
	<p:option name="method" cx:type="log|error" select="'log'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Select the method used for reporting validation issues:</p>
			<dl>
				<dt>log</dt>
				<dd>Issues are reported through warning messages.</dd>
				<dt>error</dt>
				<dd>Issues are reported through error messages and also trigger an XProc error.</dd>
			</dl>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:error
			px:log-error
		</p:documentation>
	</p:import>

	<p:split-sequence test="exists(//c:error)">
		<p:input port="source">
			<p:pipe step="main" port="report"/>
		</p:input>
	</p:split-sequence>
	<p:count name="count" limit="1"/>
	<p:sink/>
	<p:identity>
		<p:input port="source">
			<p:pipe step="main" port="source"/>
		</p:input>
	</p:identity>
	<p:choose>
		<p:xpath-context>
			<p:pipe port="result" step="count"/>
		</p:xpath-context>
		<p:when test="/c:result = '0'">
			<p:identity/>
		</p:when>
		<p:when test="$method='log'">
			<px:log-error severity="WARN">
				<p:input port="error">
					<p:pipe step="main" port="report"/>
				</p:input>
			</px:log-error>
		</p:when>
		<p:otherwise>
			<px:error>
				<p:input port="error">
					<p:pipe step="main" port="report"/>
				</p:input>
			</px:error>
		</p:otherwise>
	</p:choose>

</p:declare-step>
