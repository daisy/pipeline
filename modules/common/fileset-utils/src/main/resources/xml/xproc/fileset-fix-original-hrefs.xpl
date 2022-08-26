<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                type="pxi:fileset-fix-original-hrefs" name="main">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Make the original-href attributes reflect what is actually stored on disk.</p>
		<ul>
			<li>Remove original-href attributes of files that do not exist on disk according to @original-href.</li>
			<li>If <code>detect-existing</code> is true, set original-href attributes of files that exist on disk
			according to @href.</li>
			<li>Remove original-href attributes of files that exist in memory.</li>
		</ul>
	</p:documentation>

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:empty/>
	</p:input>
	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:pipe step="in-memory-fileset" port="result.in-memory"/>
	</p:output>

	<p:option name="detect-existing" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to set original-href attributes of files that exist on disk according to
			@href. Any existing original-href attributes will be overwritten, so by setting this
			option you prevent that files are being overwritten by other files (but not by in-memory
			documents).</p>
		</p:documentation>
	</p:option>
	<p:option name="warn-on-missing" select="true()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to raise warnings for files that exist neither on disk or in memory.</p>
		</p:documentation>
	</p:option>
	<p:option name="fail-on-missing" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to raise an error for files that exist neither on disk or in memory.</p>
		</p:documentation>
	</p:option>
	<p:option name="purge" select="false()" cx:as="xs:boolean">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Whether to remove files that exist neither on disk or in memory.</p>
		</p:documentation>
	</p:option>

	<p:import href="fileset-join.xpl">
		<p:documentation>
			px:fileset-join
		</p:documentation>
	</p:import>
	<p:import href="fileset-filter-in-memory.xpl">
		<p:documentation>
			px:fileset-filter-in-memory
		</p:documentation>
	</p:import>

	<px:fileset-filter-in-memory name="in-memory-fileset">
		<p:documentation>Also normalizes @href</p:documentation>
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-filter-in-memory>
	<p:sink/>

	<p:identity>
		<p:input port="source">
			<p:pipe step="main" port="source.fileset"/>
		</p:input>
	</p:identity>
	<p:choose>
		<p:documentation>Make @xml:base absolute</p:documentation>
		<p:when test="/*/@xml:base">
			<p:add-xml-base/>
		</p:when>
		<p:otherwise>
			<p:identity/>
		</p:otherwise>
	</p:choose>

	<px:fileset-join name="source.fileset">
		<p:documentation>Normalize @href and @original-href</p:documentation>
	</px:fileset-join>
	
	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="../xslt/fileset-fix-original-hrefs.xsl"/>
		</p:input>
		<p:with-param name="detect-existing" select="$detect-existing"/>
		<p:with-param name="fail-on-missing" select="$fail-on-missing"/>
		<p:with-param name="purge" select="$purge"/>
		<p:with-param name="warn-on-missing" select="$warn-on-missing"/>
		<p:with-param name="in-memory-fileset" select="/*">
			<p:pipe step="in-memory-fileset" port="result"/>
		</p:with-param>
	</p:xslt>

</p:declare-step>
