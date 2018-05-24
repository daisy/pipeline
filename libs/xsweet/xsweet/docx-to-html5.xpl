<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:xsw="http://coko.foundation/xsweet"
                type="xsw:docx-to-html5">
	
	<p:option name="docx-file-uri" required="true"/>
	
	<p:output port="result">
		<p:pipe step="escalate" port="_Z_FINAL"/>
	</p:output>
	
	<p:import href="applications/docx-escalate.xpl"/>
	
	<xsw:docx-escalate name="escalate">
		<p:with-option name="docx-file-uri" select="$docx-file-uri"/>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</xsw:docx-escalate>
	
</p:declare-step>
