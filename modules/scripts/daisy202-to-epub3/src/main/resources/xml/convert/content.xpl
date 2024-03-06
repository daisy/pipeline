<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:daisy202-to-epub3-content"
                name="content">

    <p:input port="content-flow" primary="true" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">DAISY 2.02 content files.</p:documentation>
    </p:input>
    <p:input port="resolve-links-mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A map of all the links in the SMIL files.</p>
            <pre><code class="example">
                <di:mapping xmlns:di="http://www.daisy.org/ns/pipeline/tmp">
                    <di:smil xml:base="file:/home/user/a.smil">
                        <di:text par-id="fragment1" text-id="frg1" src="a.html#txt1"/>
                        <di:text par-id="fragment2" text-id="frg2" src="a.html#txt2"/>
                    </di:smil>
                    <di:smil xml:base="file:/home/user/b.smil">
                        <di:text par-id="fragment1" text-id="frg1" src="b.html#txt1"/>
                        <di:text par-id="fragment2" text-id="frg2" src="b.html#txt2"/>
                    </di:smil>
                </di:mapping>
            </code></pre>
        </p:documentation>
    </p:input>
    <p:input port="ncc-navigation">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">An EPUB3 Navigation Document, which if it contains a page-list will be used to annotate page-breaks in the content documents.</p:documentation>
    </p:input>

    <p:output port="content" sequence="true" primary="true">
        <p:pipe port="result" step="result.content"/>
    </p:output>
    <p:output port="fileset" primary="false">
        <p:pipe port="result" step="result.fileset"/>
    </p:output>

    <p:option name="publication-dir" required="true"/>
    <p:option name="content-dir" required="true"/>
    <p:option name="daisy-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entries
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-upgrade
        </p:documentation>
    </p:import>
    <p:import href="resolve-links.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">Resolves SMIL-linkbacks.</p:documentation>
    </p:import>

    <p:for-each>
        <p:variable name="original-uri" select="base-uri(/*)"/>
        <p:variable name="result-uri" select="resolve-uri(concat(replace(substring($original-uri,string-length($daisy-dir)+1),'\.[^/]*$',''),'.xhtml'), $content-dir)"/>
        <pxi:daisy202-to-epub3-resolve-links>
            <p:input port="resolve-links-mapping">
                <p:pipe port="resolve-links-mapping" step="content"/>
            </p:input>
        </pxi:daisy202-to-epub3-resolve-links>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$result-uri"/>
        </px:set-base-uri>
        <p:xslt>
            <p:with-param name="content-dir" select="$content-dir"/>
            <p:input port="stylesheet">
                <p:document href="daisy202-content-to-epub3-content.xsl"/>
            </p:input>
        </p:xslt>
        <px:html-upgrade/>
        <p:insert match="/*" position="first-child">
            <p:input port="insertion">
                <p:pipe port="ncc-navigation" step="content"/>
            </p:input>
        </p:insert>
        <p:xslt>
            <p:with-param name="doc-href" select="substring-after($result-uri,$publication-dir)"/>
            <p:input port="stylesheet">
                <p:document href="content.annotate-pagebreaks.xsl"/>
            </p:input>
        </p:xslt>
        <!-- TODO: add html-outline-fixer.xsl here when it's done -->
        <!-- TODO: add html-outline-cleaner.xsl here when it's done -->
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$result-uri"/>
        </px:set-base-uri>
        <p:add-attribute match="/*" attribute-name="original-href">
            <p:with-option name="attribute-value" select="$original-uri"/>
        </p:add-attribute>
        <px:message message="upgraded the DAISY 2.02 content document $1 into the EPUB3 content document $2">
            <p:with-option name="param1" select="substring($original-uri,string-length($daisy-dir)+1)"/>
            <p:with-option name="param2" select="substring($result-uri,string-length($publication-dir)+1)"/>
        </px:message>
    </p:for-each>
    <p:identity name="result.content"/>
    
    <px:fileset-create>
        <p:with-option name="base" select="$publication-dir">
            <p:empty/>
        </p:with-option>
    </px:fileset-create>
    <px:fileset-add-entries>
        <p:input port="entries">
            <p:pipe port="result" step="result.content"/>
        </p:input>
        <p:with-param port="file-attributes" name="doctype" select="'&lt;!DOCTYPE html&gt;'"/>
    </px:fileset-add-entries>
    <px:fileset-join/>
    <p:identity name="result.fileset"/>

</p:declare-step>
