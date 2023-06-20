<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:html="http://www.w3.org/1999/xhtml"
                type="pxi:daisy202-to-epub3-ncc-navigation" name="ncc-navigation">

    <p:documentation>
        <p>Transform the DAISY 2.02 NCC into a EPUB 3 Navigation Document.</p>
    </p:documentation>

    <p:input port="ncc" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The DAISY 2.02 NCC</p>
            <pre><code class="example">
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/daisy202/ncc.html">...</html>
            </code></pre>
        </p:documentation>
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

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>An EPUB3 Navigation Document with contents based purely on the DAISY 2.02 NCC.</p>
            <pre><code class="example">
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/epub3/epub/Publication/ncc.xhtml" original-href="file:/home/user/daisy202/ncc.html">...</html>
            </code></pre>
        </p:documentation>
        <p:pipe port="result" step="ncc-navigation.result"/>
    </p:output>

    <p:option name="publication-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI to the EPUB3 Publication directory.</p>
            <pre><code class="example">file:/home/user/epub3/epub/Publication/</code></pre>
        </p:documentation>
    </p:option>
    <p:option name="content-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI to the EPUB3 Content directory.</p>
            <pre><code class="example">file:/home/user/epub3/epub/Publication/Content/</code></pre>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="resolve-links.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">De-references links to SMIL-files.</p:documentation>
    </p:import>
    <p:import href="ncc-to-nav-toc.xpl"/>
    <p:import href="ncc-to-nav-page-list.xpl"/>
    <p:import href="ncc-to-nav-landmarks.xpl"/>

    <p:variable name="subdir" select="substring-after($content-dir,$publication-dir)"/>
    <pxi:daisy202-to-epub3-resolve-links>
        <p:input port="source">
            <p:pipe port="ncc" step="ncc-navigation"/>
        </p:input>
        <p:input port="resolve-links-mapping">
            <p:pipe port="resolve-links-mapping" step="ncc-navigation"/>
        </p:input>
    </pxi:daisy202-to-epub3-resolve-links>
    <px:message message="dereferenced all links in the SMIL files"/>
    <p:identity name="ncc-navigation.no-navs"/>
    <p:sink/>
    <pxi:ncc-to-epub3-nav-toc name="ncc-navigation.toc">
        <p:input port="source">
            <p:pipe port="result" step="ncc-navigation.no-navs"/>
        </p:input>
    </pxi:ncc-to-epub3-nav-toc>
    <px:message message="created TOC from NCC"/>
    <p:sink/>
    <pxi:ncc-to-epub3-nav-page-list>
        <p:input port="source">
            <p:pipe port="result" step="ncc-navigation.no-navs"/>
        </p:input>
    </pxi:ncc-to-epub3-nav-page-list>
    <px:message message="created page list from NCC"/>
    <p:identity name="ncc-navigation.page-list"/>
    <p:sink/>
    <pxi:ncc-to-epub3-nav-landmarks>
        <p:input port="source">
            <p:pipe port="result" step="ncc-navigation.no-navs"/>
        </p:input>
    </pxi:ncc-to-epub3-nav-landmarks>
    <px:message message="created landmarks from NCC"/>
    <p:identity name="ncc-navigation.landmarks"/>
    <p:sink/>
    <p:delete match="html:body/*">
        <p:input port="source">
            <p:pipe port="result" step="ncc-navigation.no-navs"/>
        </p:input>
    </p:delete>
    <p:insert match="html:body" position="last-child">
        <p:input port="insertion">
            <p:pipe port="result" step="ncc-navigation.toc"/>
            <p:pipe port="result" step="ncc-navigation.page-list"/>
            <p:pipe port="result" step="ncc-navigation.landmarks"/>
        </p:input>
    </p:insert>
    <p:identity name="ncc-navigation.original-links"/>
    <p:viewport match="html:a[@href and not(matches(@href,'^[^/]+:'))]">
        <p:xslt>
            <p:with-param name="base" select="$publication-dir"/>
            <p:with-param name="uri"
                select="concat(if (matches(/*/@href,'^(ncc\.html)?#','i'))
                                    then ''
                                    else replace(tokenize(/*/@href,'#')[1],'^(.*)\.html$','$1.xhtml'),
                               if (contains(/*/@href,'#')) then concat('#',tokenize(/*/@href,'#')[last()]) else '')"/>
            <p:input port="stylesheet">
                <p:document href="ncc-navigation.make-new-hrefs.xsl"/>
            </p:input>
        </p:xslt>
    </p:viewport>
    <p:add-attribute match="/*" attribute-name="original-href">
        <p:with-option name="attribute-value" select="base-uri(/*)"/>
    </p:add-attribute>
    <px:set-base-uri>
        <p:with-option name="base-uri" select="concat($content-dir,'ncc.xhtml')"/>
    </px:set-base-uri>
    <p:add-xml-base/>
    <px:message message="created Navigation Document from NCC"/>
    <p:identity name="ncc-navigation.result"/>
    <p:sink/>

</p:declare-step>
