<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                type="px:daisy3-prepare-dtbook"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Add missing elements to a DTBook so as to make the NCX/OPF/SMIL generation easier.</p>
      <p>Also add UID metadata and set DOCTYPE.</p>
    </p:documentation>

    <p:input port="source" primary="true"/>
    <p:output port="result.fileset" primary="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Result fileset with the DTBook, and with the MathML altimg fallback if it was
        required.</p>
        <p>Exactly one document is loaded in memory: the DTBook.</p>
      </p:documentation>
    </p:output>
    <p:output port="result.in-memory">
      <p:pipe step="dtbook" port="result"/>
    </p:output>

    <p:option name="output-base-uri" select="''">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>The base URI of the result DTBook</p>
        <p>Defaults to the base URI of the input DTBook.</p>
      </p:documentation>
    </p:option>
    <p:input port="mathml-altimg-fallback" sequence="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Fileset manifest with as single file the image to use as MathML altimg fallback.</p>
      </p:documentation>
      <p:empty/>
    </p:input>
    <p:option name="uid" required="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>UID of the DTBook (in the meta elements)</p>
      </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-create
        px:fileset-add-entry
      </p:documentation>
    </p:import>

    <p:variable name="dtd-version" select="(//dtb:dtbook)[1]/@version"/>

    <!-- fix structure -->
    <p:xslt>
      <p:input port="source">
        <p:pipe step="main" port="source"/>
        <p:pipe step="main" port="mathml-altimg-fallback"/>
      </p:input>
      <p:input port="stylesheet">
        <p:document href="fix-dtbook-structure.xsl"/>
      </p:input>
      <p:with-param name="output-base-uri" select="($output-base-uri[not(.='')],base-uri(/*))[1]">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-option name="output-base-uri" select="($output-base-uri[not(.='')],base-uri(/*))[1]">
        <p:pipe step="main" port="source"/>
      </p:with-option>
    </p:xslt>

    <!-- add metadata -->
    <p:add-attribute match="//dtb:meta[@name='dtb:uid']" attribute-name="content">
      <p:with-option name="attribute-value" select="$uid"/>
    </p:add-attribute>
    <p:add-attribute match="//dtb:meta[@name='dc:Identifier' and count(@*)=2]"
                     attribute-name="content">
      <p:with-option name="attribute-value" select="$uid"/>
    </p:add-attribute>

    <p:identity name="dtbook"/>
    <p:sink/>

    <px:fileset-create>
      <p:with-option name="base" select="resolve-uri('./',base-uri(/*))">
        <p:pipe step="dtbook" port="result"/>
      </p:with-option>
    </px:fileset-create>
    <px:fileset-add-entry media-type="application/x-dtbook+xml">
      <p:input port="entry">
        <p:pipe step="dtbook" port="result"/>
      </p:input>
      <p:with-param port="file-attributes" name="doctype-public"
                    select="concat('-//NISO//DTD dtbook ', $dtd-version, '//EN')"/>
      <p:with-param port="file-attributes" name="doctype-system"
                    select="concat('http://www.daisy.org/z3986/2005/dtbook-', $dtd-version, '.dtd')"/>
    </px:fileset-add-entry>
    <p:choose>
      <p:when test="//math:math[not(@altimg)]">
        <p:xpath-context>
          <p:pipe step="main" port="source"/>
        </p:xpath-context>
        <p:identity name="dtbook-fileset"/>
        <px:fileset-join>
          <p:input port="source">
            <p:pipe step="dtbook-fileset" port="result"/>
            <p:pipe step="main" port="mathml-altimg-fallback"/>
          </p:input>
        </px:fileset-join>
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>

</p:declare-step>
