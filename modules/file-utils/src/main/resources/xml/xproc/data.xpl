<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                type="px:data">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Step that behaves like a <code>p:identity</code> with a <a
		href="https://www.w3.org/TR/xproc/#p.data"><code>p:data</code></a> input, with an option to
		set the <code>href</code>.</p>
		<p>Makes use of <a
		href="https://www.w3.org/TR/xproc/#c.http-request"><code>p:http-request</code></a>.</p>
	</p:documentation>

	<p:option name="href" required="true"/>
	<p:option name="content-type" required="false"/>

	<p:output port="result">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>A <a href="https://www.w3.org/TR/xproc/#p.data"><code>c:data</code></a> document.</p>
		</p:documentation>
	</p:output>

	<p:identity>
		<p:input port="source">
			<p:inline>
				<c:request method="GET"/>
			</p:inline>
		</p:input>
	</p:identity>

	<p:add-attribute match="/c:request" attribute-name="href">
		<p:with-option name="attribute-value" select="$href"/>
	</p:add-attribute>

	<p:choose>
		<p:when test="p:value-available('content-type')">
			<p:add-attribute match="/c:request" attribute-name="override-content-type">
				<p:with-option name="attribute-value" select="$content-type"/>
			</p:add-attribute>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

	<p:http-request/>

	<p:rename match="/*" new-name="c:data"/>

</p:declare-step>
