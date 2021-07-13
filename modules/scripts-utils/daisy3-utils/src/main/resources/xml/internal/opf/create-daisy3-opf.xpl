<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                type="px:daisy3-create-opf" name="main">

    <p:input port="source" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>The DAISY 3 fileset.</p>
        <p>If the fileset contains a file marked with a <code>role</code> attribute with value
        <code>mathml-xslt-fallback</code>, it will be used as the "DTBook-XSLTFallback" for
        MathML.</p>
      </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true"/>

    <p:output port="result" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>The <a
        href="http://web.archive.org/web/20101221093536/http://www.idpf.org/oebps/oebps1.2/download/oeb12-xhtml.htm">OEBPS</a>
        package document</p>
      </p:documentation>
      <p:pipe step="opf" port="result"/>
    </p:output>

    <p:output port="result.fileset">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Result fileset with a single file, the package document.</p>
      </p:documentation>
      <p:pipe step="fileset" port="result"/>
    </p:output>

    <p:option name="uid">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Globally unique identifier for the DTB.</p>
        <p>Will be used as the dc:Identifier metadata element referenced by the package document's
        unique-identifier attribute.</p>
      </p:documentation>
    </p:option>

    <p:input port="metadata">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Metadata to be included in the package document.</p>
        <p>Must be a <code>metadata</code> document in the
        "http://openebook.org/namespaces/oeb-package/1.0/" namespace, with <code>dc-metadata</code>
        and <code>x-metadata</code> child elements.</p>
      </p:documentation>
      <p:inline><metadata xmlns="http://openebook.org/namespaces/oeb-package/1.0/"/></p:inline>
    </p:input>

    <p:input kind="parameter" port="dc-metadata">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Metadata to be included in the dc-metadata element of the package document.</p>
        <p>Names must be in the Dublin Core namespace.</p>
      </p:documentation>
    </p:input>

    <p:option name="output-base-uri">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Result base URI.</p>
      </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-create
            px:fileset-add-entry
        </p:documentation>
    </p:import>

    <px:fileset-load media-types="application/smil">
      <p:documentation>Assumes that SMILs are listed in order in fileset and that they are also
      loaded in that order.</p:documentation>
      <p:input port="in-memory">
        <p:pipe step="main" port="source.in-memory"/>
      </p:input>
    </px:fileset-load>
    <p:xslt name="total-time" template-name="main">
      <p:input port="stylesheet">
        <p:document href="compute-total-time.xsl"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>
    <p:sink/>

    <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook">
      <p:input port="fileset">
        <p:pipe step="main" port="source"/>
      </p:input>
      <p:input port="in-memory">
        <p:pipe step="main" port="source.in-memory"/>
      </p:input>
    </px:fileset-load>
    <p:sink/>

    <p:parameters name="dc-metadata">
      <p:input port="parameters">
        <p:pipe step="main" port="dc-metadata"/>
      </p:input>
    </p:parameters>

    <p:xslt>
      <p:input port="source">
        <p:pipe step="main" port="source"/>
        <p:pipe step="main" port="metadata"/>
        <p:pipe step="dc-metadata" port="result"/>
        <!--
            FIXME: also allow specifying DTBook when audio only
        -->
        <p:pipe step="dtbook" port="result"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="create-opf.xsl"/>
      </p:input>
      <p:with-param name="uid" select="$uid"/>
      <p:with-param name="total-time" select="string(/*)">
        <p:pipe step="total-time" port="result"/>
      </p:with-param>
      <p:with-param name="output-base-uri" select="$output-base-uri"/>
    </p:xslt>

    <px:set-base-uri>
      <p:with-option name="base-uri" select="$output-base-uri"/>
    </px:set-base-uri>
    <p:identity name="opf"/>
    <p:sink/>

    <px:fileset-create>
      <p:with-option name="base" select="resolve-uri('./',$output-base-uri)"/>
    </px:fileset-create>
    <px:fileset-add-entry media-type="text/xml" name="fileset">
      <p:input port="entry">
        <p:pipe step="opf" port="result"/>
      </p:input>
      <p:with-param port="file-attributes" name="indent" select="'true'"/>
      <p:with-param port="file-attributes" name="doctype-public" select="'+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN'"/>
      <p:with-param port="file-attributes" name="doctype-system" select="'http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd'"/>
    </px:fileset-add-entry>
    <p:sink/>

</p:declare-step>
