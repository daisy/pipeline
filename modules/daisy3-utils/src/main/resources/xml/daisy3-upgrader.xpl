<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="#all"
                type="px:daisy3-upgrader"
                name="main">

	<p:input port="source.fileset" primary="true"/>
	<p:input port="source.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Input DAISY 3</p>
		</p:documentation>
	</p:input>

	<p:output port="result.fileset" primary="true"/>
	<p:output port="result.in-memory" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>Output DAISY 3</p>
		</p:documentation>
		<p:pipe step="ensure-core-media" port="in-memory"/>
	</p:output>

	<p:option name="output-dir" required="true" cx:as="xs:anyURI">
		<p:documentation>
			<p>Directory where the output DAISY 3 should be stored.</p>
		</p:documentation>
	</p:option>

	<p:option name="temp-dir" required="true" cx:as="xs:anyURI">
		<p:documentation>
			<p>Empty directory dedicated to this step.</p>
		</p:documentation>
	</p:option>

	<p:option name="ensure-core-media" cx:as="xs:boolean" select="false()">
		<p:documentation>
			<p>Ensure that the output DAISY 3 uses allowed file formats only.</p>
		</p:documentation>
	</p:option>
	
	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
		<p:documentation>
			px:fileset-copy
		</p:documentation>
	</p:import>
	<p:import href="internal/upgrade.xpl">
		<p:documentation>
			px:daisy3-upgrade
		</p:documentation>
	</p:import>
	<p:import href="internal/audio-transcode.xpl">
		<p:documentation>
			px:daisy3-audio-transcode
		</p:documentation>
	</p:import>
	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<px:fileset-copy name="copy">
		<p:with-option name="target" select="pf:normalize-uri(concat($output-dir,'/'))"/>
		<p:input port="source.in-memory">
			<p:pipe step="main" port="source.in-memory"/>
		</p:input>
	</px:fileset-copy>
	
	<px:daisy3-upgrade name="upgrade">
		<p:input port="source.in-memory">
			<p:pipe step="copy" port="result.in-memory"/>
		</p:input>
	</px:daisy3-upgrade>

	<p:choose name="ensure-core-media">
		<p:when test="$ensure-core-media">
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="transcode" port="result.in-memory"/>
			</p:output>
			<px:daisy3-audio-transcode new-audio-file-type="audio/mpeg" name="transcode">
				<p:input port="source.in-memory">
					<p:pipe step="upgrade" port="result.in-memory"/>
				</p:input>
				<p:with-option name="temp-dir" select="$temp-dir"/>
			</px:daisy3-audio-transcode>
		</p:when>
		<p:otherwise>
			<p:output port="fileset" primary="true"/>
			<p:output port="in-memory" sequence="true">
				<p:pipe step="upgrade" port="result.in-memory"/>
			</p:output>
			<p:identity/>
		</p:otherwise>
	</p:choose>

</p:declare-step>
