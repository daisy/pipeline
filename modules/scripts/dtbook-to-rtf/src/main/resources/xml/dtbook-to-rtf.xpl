<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" px:input-filesets="dtbook" px:output-filesets="rtf" type="px:dtbook-to-rtf" version="1.0" xmlns:d="http://www.daisy.org/ns/pipeline/data" 
	xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" 
	xmlns:p="http://www.w3.org/ns/xproc" 
	xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
	xmlns:cx="http://xmlcalabash.com/ns/extensions">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DTBook to RTF</h1>
		<p px:role="desc">Transforms a DTBook (DAISY 3 XML) document into an RTF (Rich Text Format).</p>
		<a href="http://daisy.github.io/pipeline/modules/dtbook-to-rtf" px:role="homepage">Online documentation</a>
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
      <dt>Organization:</dt>
      <dd px:role="organization">DAISY</dd>
      <dt>E-mail:</dt>
      <dd><a px:role="contact" href="mailto:rdeltour@gmail.com">rdeltout@gmail.com</a></dd>
    </dl>
		<dl px:role="author">
			<dt>Name:</dt>
			<dd px:role="name">Yilin Langlois</dd>
			<dt>Organization:</dt>
			<dd href="http://www.braillenet.org/" px:role="organization">BrailleNet</dd>
			<dt>E-mail:</dt>
			<dd>
				<a href="mailto:yilin.langlois@braillenet.org" px:role="contact">yilin.langlois@braillenet.org</a>
			</dd>
		</dl>
	</p:documentation>

	<p:input port="source" primary="true" px:media-type="application/x-dtbook+xml" sequence="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DTBook file</h2>
			<p px:role="desc">One 2005-3 DTBook files to be transformed.</p>
		</p:documentation>
	</p:input>

	<p:option name="include-table-of-content" required="false" select="'false'">
		<p:documentation>
			<h2 px:role="name">Include table of content</h2>
			<p px:role="desc">A boolean indicating if a TOC should be generated.</p>
		</p:documentation>
	</p:option>

	<p:option name="include-page-number" required="false" select="'false'">
		<p:documentation>
			<h2 px:role="name">Include page number</h2>
			<p px:role="desc">A boolean indicating if a TOC should be generated.</p>
		</p:documentation>
	</p:option>
	
	<p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h2 px:role="name">Temporary directory</h2>
      <p px:role="desc">Directory used for temporary files.</p>
    </p:documentation>
  </p:option>


	<p:option name="output-dir" px:output="result" px:type="anyDirURI" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">RTF file</h2>
			<p px:role="desc">The resulting rtf file.</p>
		</p:documentation>
	</p:option>

	<p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>

	<p:split-sequence initial-only="true" name="first-dtbook" test="position()=1"/>
	<p:sink/>

	<p:xslt name="output-dir-uri">
		<p:with-param name="href" select="concat($output-dir,'/')">
			<p:empty/>
		</p:with-param>
		<p:input port="source">
			<p:inline>
				<d:file/>
			</p:inline>
		</p:input>
		<p:input port="stylesheet">
			<p:inline>
				<xsl:stylesheet version="2.0" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
					<xsl:param name="href" required="yes"/>
					<xsl:template match="/*">
						<xsl:copy>
							<xsl:attribute name="href" select="pf:normalize-uri($href)"/>
						</xsl:copy>
					</xsl:template>
				</xsl:stylesheet>
			</p:inline>
		</p:input>
	</p:xslt>
	<p:sink/>

	<p:xslt name="add-dtbook-id">
		<p:input port="source">
			<p:pipe port="matched" step="first-dtbook"/>
		</p:input>
		<p:input port="stylesheet">
			<p:document href="add_ids_to_dtbook.xsl"/>
		</p:input>
		<p:input port="parameters">
			<p:empty/>
		</p:input>
	</p:xslt>
	<p:sink/>

	<p:group>
		<p:variable name="encoded-title" select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
			<p:pipe port="matched" step="first-dtbook"/>
		</p:variable>
		<p:variable name="encoded-tmp-title" select="concat($encoded-title,'tmp.xml')"/>
		<p:variable name="output-dir-final" select="/*/@href">
			<p:pipe port="result" step="output-dir-uri"/>
		</p:variable>
		<p:variable name="tmp-file-uri" select="concat($temp-dir,$encoded-tmp-title)"/>

		<p:store name="storetmp">
			<p:with-option name="href" select="$tmp-file-uri"/>
			<p:input port="source">
				<p:pipe port="result" step="add-dtbook-id"/>
			</p:input>
		</p:store>

		<p:xslt name="convert-to-rtf" template-name="start">
			<p:input port="source">
				<p:empty/>
			</p:input>
			<p:input port="stylesheet">
				<p:document href="dtbook_to_rtf.xsl"/>
			</p:input>
			<p:with-param name="inclTOC" select="$include-table-of-content"/>
			<p:with-param name="inclPagenum" select="$include-page-number"/>
			<p:with-param name="sourceFile" select="$tmp-file-uri"/>
		</p:xslt>

		<p:store method="text">
			<p:with-option name="href" select="concat($output-dir-final,$encoded-title,'.rtf')"/>
		</p:store>
		<px:delete cx:depends-on="convert-to-rtf">
			<p:with-option name="href" select="$tmp-file-uri"/>
		</px:delete>
	</p:group>
</p:declare-step>