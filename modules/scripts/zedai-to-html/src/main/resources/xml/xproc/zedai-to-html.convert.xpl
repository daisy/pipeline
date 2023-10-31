<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:zedai-to-html"
                name="main"
                exclude-inline-prefixes="#all">

    <p:documentation>
        Transforms a ZedAI (DAISY 4 XML) document into an EPUB 3 publication.
    </p:documentation>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="html" port="in-memory"/>
        <p:pipe step="resources" port="in-memory"/>
    </p:output>

    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that contains a mapping from input file (ZedAI) to
            output files (HTML) and contained <code>id</code> attributes.</p>
        </p:documentation>
        <p:pipe step="html" port="mapping"/>
    </p:output>

    <p:option name="output-dir" required="true"/>
    <p:option name="chunk" select="'false'" cx:as="xs:string"/>
    <p:option name="chunk-size" required="false" select="'-1'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-create
            px:fileset-add-entries
            px:fileset-join
            px:fileset-filter
            px:fileset-filter-in-memory
            px:fileset-load
            px:fileset-move
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-chunker
            px:html-outline
        </p:documentation>
    </p:import>

    <!--=========================================================================-->
    <!-- GET ZEDAI FROM FILESET                                                  -->
    <!--=========================================================================-->

    <p:documentation>Retreive the ZedAI document from the input fileset.</p:documentation>
    <p:group>
        <px:fileset-load media-types="application/z3998-auth+xml">
            <p:input port="in-memory">
                <p:pipe step="main" port="in-memory.in"/>
            </p:input>
        </px:fileset-load>
        <!-- TODO: describe the error on the wiki and insert correct error code -->
        <px:assert message="No XML documents with the ZedAI media type ('application/z3998-auth+xml') found in the fileset."
                   test-count-min="1" error-code="PEZE00"/>
        <px:assert message="More than one XML document with the ZedAI media type ('application/z3998-auth+xml') found in the fileset; there can only be one ZedAI document."
                   test-count-max="1" error-code="PEZE00"/>
    </p:group>

    <!--=========================================================================-->
    <!-- CONVERT TO XHTML                                                        -->
    <!--=========================================================================-->

    <p:documentation>Convert the ZedAI Document into several XHTML Documents</p:documentation>
    <p:group name="html">
        <p:output port="fileset" primary="true">
            <p:pipe step="zedai-to-html.fileset" port="result"/>
        </p:output>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="zedai-to-html.html-chunks" port="result"/>
        </p:output>
        <p:output port="mapping">
            <p:pipe step="compose-mapping" port="result"/>
        </p:output>
        <p:variable name="zedai-basename" select="base-uri(/*)">
        </p:variable>
        <p:variable name="result-basename" select="concat(
                                                     $output-dir,
                                                     replace(replace($zedai-basename,'^.+/([^/]+)$','$1'),'^(.+)\.[^\.]+$','$1'),
                                                     '.xhtml')">
            <p:empty/>
        </p:variable>
        <p:group name="zedai-to-html.html-single">
            <p:output port="result" primary="true">
                <p:pipe step="result" port="result"/>
            </p:output>
            <p:output port="mapping">
                <p:pipe step="mapping" port="result"/>
            </p:output>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="../xslt/zedai-to-html.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <!-- zedai-to-html.xsl only uses h1 -->
            <px:html-outline fix-heading-ranks="outline-depth"/>
            <px:set-base-uri name="result">
                <p:with-option name="base-uri" select="$result-basename"/>
            </px:set-base-uri>
            <p:template name="mapping">
                <p:input port="template">
                    <p:inline>
                        <d:fileset>
                            <d:file href="{base-uri(/*)}" original-href="{$zedai-basename}"/>
                        </d:fileset>
                    </p:inline>
                </p:input>
                <p:with-param name="zedai-basename" select="$zedai-basename"/>
            </p:template>
            <p:sink/>
        </p:group>
        <p:choose name="zedai-to-html.html-chunks">
            <p:documentation>Split XHTML document</p:documentation>
            <p:when test="$chunk='true'">
                <p:output port="result" sequence="true" primary="true"/>
                <p:output port="mapping">
                    <p:pipe step="chunker" port="mapping"/>
                </p:output>
                <px:html-chunker name="chunker">
                    <p:with-option name="max-chunk-size" select="$chunk-size"/>
                </px:html-chunker>
            </p:when>
            <p:otherwise>
                <p:output port="result" sequence="true" primary="true"/>
                <p:output port="mapping">
                    <p:inline><d:fileset/></p:inline>
                </p:output>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:sink/>

        <p:documentation>Construct HTML fileset</p:documentation>
        <px:fileset-create>
            <p:with-option name="base" select="$output-dir"/>
        </px:fileset-create>
        <px:fileset-add-entries media-type="application/xhtml+xml">
            <p:input port="entries">
                <p:pipe step="zedai-to-html.html-chunks" port="result"/>
            </p:input>
        </px:fileset-add-entries>
        <px:fileset-join name="zedai-to-html.fileset"/>
        <p:sink/>

        <px:fileset-compose name="compose-mapping">
            <p:input port="source">
                <p:pipe step="zedai-to-html.html-single" port="mapping"/>
                <p:pipe step="zedai-to-html.html-chunks" port="mapping"/>
            </p:input>
        </px:fileset-compose>
        <p:sink/>
    </p:group>
    
    <!--=========================================================================-->
    <!-- CONSOLIDATE THE OUTPUT FILESET                                          -->
    <!--=========================================================================-->

    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="fileset.in"/>
        </p:input>
    </p:identity>
    <p:group name="resources">
        <p:documentation>
            Fileset with everything except the ZedAI file, rebased from the ZedAI file to $output-dir.
        </p:documentation>
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="move" port="result.in-memory"/>
        </p:output>
        <p:variable name="zedai-uri" select="(//d:file[@media-type='application/z3998-auth+xml'])[1]/resolve-uri(@href,base-uri(.))"/>
        <px:fileset-filter not-media-types="application/z3998-auth+xml"/>
        <p:documentation>
            Remove files that are neither in memory nor on disk
        </p:documentation>
        <p:identity name="fileset.dirty"/>
        <!-- copy resources from in-memory.in port -->
        <px:fileset-filter-in-memory name="fileset.in-memory">
            <p:input port="source.in-memory">
                <p:pipe step="main" port="in-memory.in"/>
            </p:input>
        </px:fileset-filter-in-memory>
        <px:fileset-load name="in-memory">
            <p:input port="in-memory">
                <p:pipe step="main" port="in-memory.in"/>
            </p:input>
        </px:fileset-load>
        <p:sink/>
        <p:delete match="d:file[not(@original-href)]" name="fileset.on-disk">
            <p:input port="source">
                <p:pipe step="fileset.dirty" port="result"/>
            </p:input>
        </p:delete>
        <p:sink/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="fileset.in-memory" port="result"/>
                <p:pipe step="fileset.on-disk" port="result"/>
            </p:input>
        </px:fileset-join>
        <!-- move to $output-dir-->
        <px:fileset-rebase>
            <p:with-option name="new-base" select="$zedai-uri"/>
        </px:fileset-rebase>
        <px:fileset-copy name="move">
            <p:input port="source.in-memory">
                <p:pipe step="in-memory" port="result"/>
            </p:input>
            <p:with-option name="target" select="$output-dir"/>
        </px:fileset-copy>
        <!-- TODO: remove resources from fileset that are not referenced from any of the in-memory files -->
    </p:group>
    <p:sink/>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="html" port="fileset"/>
            <p:pipe step="resources" port="fileset"/>
        </p:input>
    </px:fileset-join>

</p:declare-step>
