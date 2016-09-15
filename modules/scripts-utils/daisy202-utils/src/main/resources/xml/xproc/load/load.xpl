<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" type="px:daisy202-load" version="1.0">

    <p:documentation>
        <p px:role="desc">Load a DAISY 2.02 fileset based on its NCC.</p>
    </p:documentation>

    <p:serialization port="fileset.out" indent="true"/>

    <p:option name="ncc" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">URI to input NCC.</p>
        </p:documentation>
    </p:option>

    <p:output port="fileset.out" primary="true">
        <p:documentation>A fileset containing references to all the files in the DAISY 2.02 fileset
            and any resources they reference (images etc.). The base URI of each document points to
            the original file, while the href can change during conversions to reflect changes in
            the path and filename of the resulting file. The SMIL files in the fileset are ordered
            according the the "flow" (reading order).</p:documentation>
        <p:pipe port="fileset.out" step="wrapper"/>
    </p:output>

    <p:output port="in-memory.out" sequence="true">
        <p:documentation>The NCC file serialized as XHTML.</p:documentation>
        <p:pipe port="in-memory.out" step="wrapper"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>

    <p:xslt>
        <p:with-param name="href" select="$ncc"/>
        <p:input port="source">
            <p:inline>
                <doc/>
            </p:inline>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0" exclude-result-prefixes="#all">
                    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                    <xsl:param name="href" required="yes"/>
                    <xsl:template match="/*">
                        <d:file href="{pf:normalize-uri($href)}"/>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>

    <p:group name="wrapper">
        <p:output port="fileset.out" primary="true">
            <p:pipe port="result" step="fileset"/>
        </p:output>
        <p:output port="in-memory.out" sequence="true">
            <p:pipe port="result" step="in-memory"/>
        </p:output>
        <p:variable name="href" select="/*/@href"/>
        <px:message>
            <p:with-option name="message" select="concat('loading NCC: ',$href)"/>
        </px:message>

        <px:html-load name="in-memory.ncc">
            <p:with-option name="href" select="$href"/>
        </px:html-load>

        <px:message
            message="Making an ordered list of SMIL-files referenced from the NCC according to the flow (reading order)"/>
        <p:xslt name="fileset.smil">
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ncc-to-flow-fileset.xsl"/>
            </p:input>
        </p:xslt>

        <px:message message="Loading all SMIL files"/>
        <p:for-each>
            <p:iteration-source select="//d:file"/>
            <px:message>
                <p:with-option name="message" select="concat('loading ',/*/@href,'...')"/>
            </px:message>
            <p:load>
                <p:with-option name="href" select="p:resolve-uri(/*/@href,base-uri(/*))"/>
            </p:load>
        </p:for-each>
        <p:identity name="in-memory.smil"/>

        <px:message message="Listing all resources referenced from the SMIL files"/>
        <p:for-each>
            <p:identity name="fileset.html-and-resources.in-memory.smil"/>

            <p:xslt name="fileset.html-and-resources.audio">
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document
                        href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/smil-to-audio-fileset.xsl"
                    />
                </p:input>
            </p:xslt>

            <p:xslt name="fileset.html-and-resources.text">
                <p:input port="source">
                    <p:pipe port="result" step="fileset.html-and-resources.in-memory.smil"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document
                        href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/smil-to-text-fileset.xsl"
                    />
                </p:input>
            </p:xslt>

            <px:fileset-join>
                <p:input port="source">
                    <p:pipe port="result" step="fileset.html-and-resources.audio"/>
                    <p:pipe port="result" step="fileset.html-and-resources.text"/>
                </p:input>
            </px:fileset-join>
        </p:for-each>
        <px:fileset-join name="fileset.html-and-audio"/>

        <px:message message="Loading all HTML-files"/>
        <p:for-each>
            <p:iteration-source
                select="//d:file[
            (@media-type='text/html' 
            or @media-type='application/xhtml+xml' 
            or matches(lower-case(@href),'\.x?html$'))
            and not(resolve-uri(@href,base-uri())=$href)]"/>
            <px:message>
                <p:with-option name="message" select="concat('loading ',/*/@href,'...')"/>
            </px:message>
            <px:html-load>
                <p:with-option name="href" select="p:resolve-uri(/*/@href,base-uri(/*))"/>
            </px:html-load>
        </p:for-each>
        <p:identity name="in-memory.html"/>

        <px:message message="Listing all resources referenced from the HTML files"/>
        <p:for-each name="fileset.html-resources.for-each">
            <px:html-to-fileset/>
            <px:message>
                <p:with-option name="message"
                    select="concat('extracted list of resources from ',replace(base-uri(/*),'^.*/',''))">
                    <p:pipe port="current" step="fileset.html-resources.for-each"/>
                </p:with-option>
            </px:message>
            <!--<p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="make-resource-fileset.xsl"/>
            </p:input>
        </p:xslt>-->
        </p:for-each>
        <px:fileset-join name="fileset.html-resources"/>

        <p:identity name="in-memory">
            <p:input port="source">
                <p:pipe port="result" step="in-memory.ncc"/>
                <p:pipe port="result" step="in-memory.smil"/>
                <p:pipe port="result" step="in-memory.html"/>
            </p:input>
        </p:identity>

        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="fileset.smil"/>
                <p:pipe port="result" step="fileset.html-and-audio"/>
                <p:pipe port="result" step="fileset.html-resources"/>
            </p:input>
        </px:fileset-join>
        <px:fileset-add-entry media-type="application/xhtml+xml" first="true">
            <p:with-option name="href" select="base-uri(.)">
                <p:pipe port="result" step="in-memory.ncc"/>
            </p:with-option>
        </px:fileset-add-entry>
        <px:fileset-join/>
        <px:mediatype-detect>
            <p:input port="in-memory">
                <p:pipe port="result" step="in-memory"/>
            </p:input>
        </px:mediatype-detect>
        <p:identity name="fileset"/>
    </p:group>
</p:declare-step>
