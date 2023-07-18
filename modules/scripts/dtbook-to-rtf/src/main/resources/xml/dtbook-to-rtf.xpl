<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:dtbook-to-rtf.script" name="main"
                px:input-filesets="dtbook"
                px:output-filesets="rtf">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DTBook to RTF</h1>
		<p px:role="desc">Transforms a DTBook (DAISY 3 XML) document into an RTF (Rich Text Format).</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-rtf/">
			Online documentation
		</a>
		<address>
			Authors:
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Markus Gylling</dd>
			</dl>
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Ole Holst Andersen</dd>
				<dt>Organization:</dt>
				<dd href="https://nota.dk" px:role="organization">Nota</dd>
			</dl>
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Romain Deltour</dd>
				<dt>E-mail:</dt>
				<dd><a px:role="contact" href="mailto:rdeltour@gmail.com">rdeltour@gmail.com</a></dd>
				<dt>Organization:</dt>
				<dd px:role="organization">DAISY Consortium</dd>
			</dl>
			<dl px:role="author">
				<dt>Name:</dt>
				<dd px:role="name">Yilin Langlois</dd>
				<dt>E-mail:</dt>
				<dd><a href="mailto:yilin.langlois@braillenet.org" px:role="contact">yilin.langlois@braillenet.org</a></dd>
				<dt>Organization:</dt>
				<dd href="http://www.braillenet.org/" px:role="organization">BrailleNet</dd>
			</dl>
		</address>
	</p:documentation>

	<p:input port="source" primary="true" px:media-type="application/x-dtbook+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DTBook file</h2>
			<p px:role="desc">The 2005-3 DTBook file to be transformed.</p>
		</p:documentation>
	</p:input>

	<p:option name="include-table-of-content" px:type="boolean" required="false" select="'false'">
		<p:documentation>
			<h2 px:role="name">Include table of content</h2>
			<p px:role="desc">A boolean indicating if a TOC should be generated.</p>
		</p:documentation>
	</p:option>

	<p:option name="include-page-number" px:type="boolean" required="false" select="'false'">
		<p:documentation>
			<h2 px:role="name">Include page number</h2>
			<p px:role="desc">A boolean indicating if a TOC should be generated.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
		<!-- directory used for temporary files -->
	</p:option>

	<p:option name="output-dir" px:output="result" px:type="anyDirURI" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">RTF file</h2>
			<p px:role="desc">The resulting rtf file.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
	<p:import href="convert.xpl"/>

	<px:normalize-uri name="output-dir-uri">
		<p:with-option name="href" select="concat($output-dir,'/')"/>
	</px:normalize-uri>

	<p:group>
		<p:variable name="encoded-title" select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
			<p:pipe step="main" port="source"/>
		</p:variable>
		<p:variable name="output-dir-final" select="/c:result/string()">
			<p:pipe step="output-dir-uri" port="normalized"/>
		</p:variable>

		<px:dtbook-load name="load"/>
		
		<px:dtbook-to-rtf>
			<p:input port="source.in-memory">
				<p:pipe step="load" port="result.in-memory"/>
			</p:input>
			<p:with-option name="include-table-of-content" select="$include-table-of-content"/>
			<p:with-option name="include-page-number" select="$include-page-number"/>
			<p:with-option name="temp-dir" select="$temp-dir"/>
		</px:dtbook-to-rtf>

		<p:store method="text">
			<p:with-option name="href" select="concat($output-dir-final,$encoded-title,'.rtf')"/>
		</p:store>

	</p:group>

</p:declare-step>
