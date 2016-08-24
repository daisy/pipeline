<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="pxi:daisy202-to-epub3-resolve-links-create-mapping" name="resolve-links-create-mapping" version="1.0">

    <p:documentation>
        <h1 px:role="desc">Creates a reusable mapping for pxi:daisy202-to-epub3-resolve-links</h1>
    </p:documentation>

    <p:input port="daisy-smil" sequence="true" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">The DAISY 2.02 SMIL documents.</p>
            <pre><code class="example">
                <smil xml:base="file:/home/user/daisy202/a.smil">
                    <head>...</head>
                    <seq dur="10s">
                        <par endsync="last">
                            <text id="fragment" src="a.html#id"/>
                        </par>
                    </seq>
                </smil>
                <smil xml:base="file:/home/user/daisy202/b.smil">...</smil>
                <smil xml:base="file:/home/user/daisy202/c.smil">...</smil>
            </code></pre>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">A map of all the links in the SMIL files.</p>
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
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:for-each>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">For each SMIL</p:documentation>
        <p:variable name="smil-base" select="base-uri(/*)"/>
        <p:add-xml-base all="true" relative="false"/>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">For each text element where the a@fragment matches the text@id or text/parent@id</p:documentation>
        <!--<p:viewport match="//*[local-name()='par' and @system-required='sidebar-on']">
            
        </p:viewport>-->
        <!--<p:viewport match="//*[local-name()='par' and @system-required='prodnote-on']">
            
        </p:viewport>-->
        <!--<p:viewport match="//*[local-name()='seq' and (./*[local-name()='par'])[2]/@system-required='footnote-on']">
            <p:add-attribute match="TODO"/>
        </p:viewport>-->
        <!--<p:viewport match="//*[local-name()='par' and @system-required='pagenumber-on']">
            
        </p:viewport>-->
        <p:for-each>
            <p:iteration-source select="//*[local-name()='par']"/>
            <p:variable name="par-id" select="/*/@id"/>
            <p:variable name="text-id" select="/*/*[local-name()='text']/@id"/>
            <p:variable name="text-src" select="/*/*[local-name()='text']/resolve-uri(@src,base-uri(.))"/>
            <p:identity name="current-smil"/>
            <p:in-scope-names name="vars"/>
            <p:template>
                <p:input port="template">
                    <p:inline exclude-inline-prefixes="#all">
                        <di:text xmlns:di="http://www.daisy.org/ns/pipeline/tmp" par-id="{$par-id}" text-id="{$text-id}" src="{$text-src}"/>
                    </p:inline>
                </p:input>
                <p:input port="source">
                    <p:pipe port="result" step="current-smil"/>
                </p:input>
                <p:input port="parameters">
                    <p:pipe step="vars" port="result"/>
                </p:input>
            </p:template>
        </p:for-each>
        <p:wrap-sequence wrapper="di:smil" xmlns:di="http://www.daisy.org/ns/pipeline/tmp"/>
        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="$smil-base"/>
        </p:add-attribute>
        <px:message>
            <p:with-option name="message" select="concat('created a map of links from the SMIL file ',$smil-base)"/>
        </px:message>
    </p:for-each>
    <p:wrap-sequence wrapper="di:mapping" xmlns:di="http://www.daisy.org/ns/pipeline/tmp"/>
    <px:message message="created a map of links from all the SMIL files"/>

</p:declare-step>
