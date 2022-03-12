<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-inline-prefixes="#all"
                type="px:add-xml-base" name="main">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Does the same as <a
		href="https://www.w3.org/TR/xproc/#c.add-xml-base">p:add-xml-base</a>, but has an additional
		option called <code>root</code> to omit the xml:base attribute on the document element if
		the input does not have an xml:base attribute on the document element either.</p>
	</p:documentation>
	
	<p:input port="source"/>
	<p:output port="result"/>
	<p:option name="root" select="'true'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>If false (default is true), the result will not have an xml:base attribute on the
			document element if the input does not have an absolute xml:base attribute on the
			document element.</p>
		</p:documentation>
	</p:option>
	<p:option name="all" select="'false'"/>
	<p:option name="relative" select="'true'"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
		<p:documentation>
			px:assert
		</p:documentation>
	</p:import>
	<cx:import href="../xslt/uri-functions.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:is-relative
		</p:documentation>
	</cx:import>

	<px:assert message="The 'root' option can not be false if the 'all' option is true" error-code="XC0058">
		<p:with-option name="test" select="not($root='false' and $all='true')"/>
	</px:assert>
	
	<p:add-xml-base name="with-xml-base">
		<p:with-option name="all" select="$all"/>
		<p:with-option name="relative" select="$relative"/>
	</p:add-xml-base>
	
	<p:choose>
		<p:when test="$root='false'">
			<p:variable name="absolute-xml-base" cx:as="xs:string?" select="/*/@xml:base[not(pf:is-relative(.))]">
				<p:pipe step="main" port="source"/>
			</p:variable>
			<p:choose>
				<p:when test="not(exists($absolute-xml-base))">
					<p:delete match="/*/@xml:base">
						<p:input port="source">
							<p:pipe step="with-xml-base" port="result"/>
						</p:input>
					</p:delete>
				</p:when>
				<p:otherwise>
					<p:identity>
						<p:input port="source">
							<p:pipe step="with-xml-base" port="result"/>
						</p:input>
					</p:identity>
				</p:otherwise>
			</p:choose>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>
	
</p:declare-step>
