<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                type="pxi:create-ncc"
                name="main">

    <p:documentation>
        Create NCC file for a given DAISY 2.02 fileset.
    </p:documentation>

    <p:input port="source.fileset" primary="true">
        <p:documentation>
            The DAISY 2.02 fileset.
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true"/>
    <p:input port="opf">
        <p:documentation>
            The package document of the original EPUB 3 from which the DAISY 2.02 was generated.
        </p:documentation>
    </p:input>
    <p:input port="page-list">
        <p:documentation>
            A fileset XML document listing all the pagebreak elements as d:anchor elements.
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true">
        <p:documentation>
            The resulting DAISY 2.02 fileset that includes the NCC and possibly additional or
            improved SMIL files.
        </p:documentation>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="add-ncc" port="result.in-memory"/>
    </p:output>
    <p:output port="ncc">
        <p:pipe step="ncc-with-linkbacks" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:add-ids
        </p:documentation>
    </p:import>
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
            px:fileset-join
            px:fileset-intersect
            px:fileset-update
            px:fileset-filter-in-memory
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:opf-spine-to-fileset
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-add-ids
            px:html-outline
        </p:documentation>
    </p:import>
    <p:import href="opf-to-ncc-metadata.xpl">
        <p:documentation>
            px:opf-to-ncc-metadata
        </p:documentation>
    </p:import>

    <p:group name="opf">
        <p:documentation>Normalize base URI of OPF</p:documentation>
        <p:output port="result"/>
        <px:fileset-create/>
        <px:fileset-add-entry>
            <p:input port="entry">
                <p:pipe step="main" port="opf"/>
            </p:input>
        </px:fileset-add-entry>
        <px:fileset-load>
            <p:input port="in-memory">
                <p:pipe step="main" port="opf"/>
            </p:input>
        </px:fileset-load>
    </p:group>
    <p:sink/>

    <p:documentation>
        Load content documents in spine order.
        <!-- assumes px:fileset-load loads documents in order defined in fileset and
             px:fileset-intersect does not alter order -->
    </p:documentation>
    <px:fileset-add-entry name="daisy202-with-opf"
                          media-type="application/oebps-package+xml">
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="entry">
            <p:pipe step="opf" port="result"/>
        </p:input>
    </px:fileset-add-entry>
    <px:opf-spine-to-fileset ignore-missing="true" name="spine.fileset">
        <p:input port="source.in-memory">
            <p:pipe step="daisy202-with-opf" port="result.in-memory"/>
        </p:input>
    </px:opf-spine-to-fileset>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>

    <p:documentation>
        Add missing IDs to heading elements. Page number elements already have an ID thanks to
        identify-pagebreaks.xsl
    </p:documentation>
    <px:html-add-ids name="xhtml-with-ids"
                     match="html:h1|
                            html:h2|
                            html:h3|
                            html:h4|
                            html:h5|
                            html:h6"
                     px:progress="1/10"/>
    <p:sink/>

    <p:documentation>
        Augment SMIL files with references to heading elements.
    </p:documentation>
    <px:fileset-load media-types="application/smil+xml" name="load-smil">
        <p:documentation>Load SMIL files</p:documentation>
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="augment-smils" px:message="Augmenting SMILs" px:message-severity="DEBUG" px:progress="3/10">
        <p:output port="smil.in-memory" sequence="true">
            <p:pipe step="drop-smil-without-associated-xhtml" port="smil"/>
        </p:output>
        <p:output port="smil-with-textref" sequence="true">
            <!-- Possibly with textref attributes. For use in create-linkbacks.xsl of NCC. -->
            <p:pipe step="drop-smil-without-associated-xhtml" port="smil-with-textref"/>
        </p:output>
        <p:output port="xhtml.in-memory" sequence="true">
            <p:pipe step="drop-smil-without-associated-xhtml" port="xhtml"/>
        </p:output>
        <p:output port="xhtml.fileset" sequence="true">
            <p:pipe step="drop-smil-without-associated-xhtml" port="xhtml.fileset"/>
        </p:output>
        <p:documentation>Get the HTML file(s) that corresponds with this SMIL.</p:documentation>
        <p:variable name="smil-base" select="base-uri(/)"/>
        <!-- get manifest items with media-overlay attribute pointing to the SMIL -->
        <p:filter>
            <p:input port="source">
                <p:pipe step="opf" port="result"/>
            </p:input>
            <p:with-option name="select"
                           select="concat('
                                     for $mo in /opf:package/opf:manifest/opf:item[resolve-uri(@href,base-uri())=&quot;',$smil-base,'&quot;]/@id
                                     return /opf:package/opf:manifest/opf:item[@media-overlay=$mo]
                                     ')"/>
        </p:filter>
        <!-- create fileset from the items -->
        <p:for-each>
            <px:fileset-add-entry>
                <p:with-option name="href" select="/*/resolve-uri(@href,base-uri())"/>
                <p:input port="source">
                    <p:inline>
                        <d:fileset/>
                    </p:inline>
                </p:input>
            </px:fileset-add-entry>
        </p:for-each>
        <px:fileset-join name="associated-xhtml.fileset"/>
        <p:sink/>
        <!-- load in spine order (and drop items not in spine) -->
        <px:fileset-intersect>
            <p:input port="source">
                <p:pipe step="spine.fileset" port="result"/>
                <p:pipe step="associated-xhtml.fileset" port="result"/>
            </p:input>
        </px:fileset-intersect>
        <p:choose name="drop-smil-without-associated-xhtml">
            <p:when test="exists(//d:file)">
                <p:output port="smil" sequence="true">
                    <p:pipe step="smil" port="result"/>
                </p:output>
                <p:output port="smil-with-textref" sequence="true">
                    <p:pipe step="smil-with-textref" port="result"/>
                </p:output>
                <p:output port="xhtml" sequence="true">
                    <p:pipe step="xhtml-with-linkbacks" port="result"/>
                </p:output>
                <p:output port="xhtml.fileset">
                    <p:pipe step="associated-xhtml" port="result.fileset"/>
                </p:output>
                <px:fileset-load name="associated-xhtml" fail-on-not-found="true">
                    <p:input port="in-memory">
                        <p:pipe step="xhtml-with-ids" port="result"/>
                    </p:input>
                </px:fileset-load>
                <p:group>
                    <p:variable name="smil-href" select="//d:file[resolve-uri(@href,base-uri(.))=$smil-base]/@href">
                        <p:pipe step="load-smil" port="result.fileset"/>
                    </p:variable>
                    <p:sink/>
                    <p:documentation>Make sure pars have an id attribute (needed for augment-smils.xsl and create-linkbacks.xsl)</p:documentation>
                    <px:add-ids match="par" name="smil-with-ids">
                        <p:input port="source">
                            <p:pipe step="augment-smils" port="current"/>
                        </p:input>
                    </px:add-ids>
                    <p:documentation>Augment the SMIL.</p:documentation>
                    <p:xslt px:message="Processing {$smil-href}" px:message-severity="DEBUG">
                        <p:input port="source">
                            <p:pipe step="smil-with-ids" port="result"/>
                            <p:pipe step="main" port="page-list"/>
                            <p:pipe step="associated-xhtml" port="result"/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/augment-smil.xsl"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                </p:group>
                <p:identity name="smil-with-textref"/>
                <p:unwrap match="*[@textref]"/>
                <p:documentation>Fix metadata</p:documentation>
                <p:xslt>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/smil-metadata.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <p:xslt name="smil">
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/pretty-print.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <p:sink/>
                <p:documentation>Add linkbacks from HTML to SMIL.</p:documentation>
                <p:for-each name="xhtml-with-linkbacks">
                    <p:iteration-source>
                        <p:pipe step="associated-xhtml" port="result"/>
                    </p:iteration-source>
                    <p:output port="result" sequence="true"/>
                    <p:variable name="base" select="base-uri()"/>
                    <p:variable name="href" select="//d:file[resolve-uri(@href,base-uri(.))=$base]/@href">
                        <p:pipe step="associated-xhtml" port="result.fileset"/>
                    </p:variable>
                    <p:identity px:message="Adding linkbacks to {$href}" px:message-severity="DEBUG"/>
                    <p:xslt>
                        <p:input port="source">
                            <p:pipe step="xhtml-with-linkbacks" port="current"/>
                            <p:pipe step="smil" port="result"/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/create-linkbacks.xsl"/>
                        </p:input>
                        <p:with-param port="parameters" name="is-ncc" select="'false'"/>
                    </p:xslt>
                </p:for-each>
            </p:when>
            <p:otherwise>
                <!-- There are no associated html documents. This could be because the smil is
                     associated with the navigation document which is not included in the
                     spine. -->
                <p:output port="smil" sequence="true">
                    <p:empty/>
                </p:output>
                <p:output port="smil-with-textref" sequence="true">
                    <p:empty/>
                </p:output>
                <p:output port="xhtml" sequence="true">
                    <p:empty/>
                </p:output>
                <p:output port="xhtml.fileset" sequence="true">
                    <p:empty/>
                </p:output>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <px:fileset-filter-in-memory name="augment-smils.smil.fileset">
        <p:input port="source.fileset">
            <p:pipe step="load-smil" port="result.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="augment-smils" port="smil.in-memory"/>
        </p:input>
    </px:fileset-filter-in-memory>
    <p:sink/>

    <p:documentation>
        Create new SMIL file with references to heading elements for every content document without
        media-overlay.
    </p:documentation>
    <p:filter select="for $itemref in /opf:package/opf:spine/opf:itemref/@idref
                      return /opf:package/opf:manifest/opf:item[@id=$itemref][not(@media-overlay)]">
        <p:input port="source">
            <p:pipe step="opf" port="result"/>
        </p:input>
    </p:filter>
    <p:for-each>
        <px:fileset-add-entry>
            <p:with-option name="href" select="/*/resolve-uri(@href,base-uri())"/>
            <p:input port="source">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join name="xhtml-without-mo.fileset"/>
    <!-- add file attributes from source fileset -->
    <px:fileset-intersect>
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
            <p:pipe step="xhtml-without-mo.fileset" port="result"/>
        </p:input>
    </px:fileset-intersect>
    <px:fileset-load fail-on-not-found="true" name="xhtml-without-mo">
        <p:input port="in-memory">
            <p:pipe step="xhtml-with-ids" port="result"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="new-smils" px:progress="3/10">
        <p:output port="smil" sequence="true" primary="true">
            <p:pipe step="smil" port="result"/>
        </p:output>
        <p:output port="xhtml" sequence="true">
            <p:pipe step="xhtml-with-linkbacks" port="result"/>
        </p:output>
        <p:variable name="base" select="base-uri()"/>
        <p:variable name="href" select="//d:file[resolve-uri(@href,base-uri(.))=$base]/@href">
            <p:pipe step="xhtml-without-mo" port="result.fileset"/>
        </p:variable>
        <p:identity px:message="Creating new SMIL for {$href}" px:message-severity="DEBUG"/>
        <px:set-base-uri name="empty-smil">
            <p:input port="source">
                <p:inline exclude-inline-prefixes="#all">
                    <smil>
                        <head>
                            <meta name="ncc:generator" content="DAISY Pipeline 2"/>
                            <layout>
                                <region id="txtView"/>
                            </layout>
                        </head>
                        <body>
                            <seq dur="0.0s"/>
                        </body>
                    </smil>
                </p:inline>
            </p:input>
            <!--
                FIXME: check that this file does not exist yet in source.fileset
            -->
            <p:with-option name="base-uri" select="concat(replace(base-uri(/),'\.x?html$',''),'.smil')">
                <p:pipe step="new-smils" port="current"/>
            </p:with-option>
        </px:set-base-uri>
        <p:xslt>
            <p:input port="source">
                <p:pipe step="empty-smil" port="result"/>
                <p:pipe step="main" port="page-list"/>
                <p:pipe step="new-smils" port="current"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/augment-smil.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:documentation>Fix metadata</p:documentation>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="../../xslt/smil-metadata.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:choose>
            <p:when test="exists(//par)">
                <px:add-ids match="par">
                    <p:documentation>Make sure pars have an id attribute (needed for create-linkbacks.xsl)</p:documentation>
                </px:add-ids>
                <p:xslt>
                    <p:input port="stylesheet">
                        <p:document href="../../xslt/pretty-print.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:identity name="smil"/>
        <p:sink/>
        <p:documentation>Add linkbacks from HTML to SMIL.</p:documentation>
        <p:for-each name="xhtml-with-linkbacks">
            <p:iteration-source>
                <p:pipe step="new-smils" port="current"/>
            </p:iteration-source>
            <p:output port="result" sequence="true"/>
            <p:xslt>
                <p:input port="source">
                    <p:pipe step="xhtml-with-linkbacks" port="current"/>
                    <p:pipe step="smil" port="result"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/create-linkbacks.xsl"/>
                </p:input>
                <p:with-param port="parameters" name="is-ncc" select="'false'"/>
            </p:xslt>
        </p:for-each>
    </p:for-each>
    <p:for-each>
        <px:fileset-add-entry media-type="application/smil+xml">
            <p:with-option name="href" select="resolve-uri(base-uri(/*))"/>
            <p:input port="source">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join name="new-smils.fileset"/>
    <p:sink/>

    <p:documentation>
        Create NCC file with references to all heading elements. Note that this NCC is invalid and
        needs to be fixed by the add-linkbacks step below.
    </p:documentation>
    <p:group name="ncc">
        <p:output port="result"/>
        <p:variable name="ncc-base-uri" select="concat(replace(base-uri(/*),'[^/]+$',''),'ncc.html')">
            <p:pipe step="opf" port="result"/>
        </p:variable>
        <px:fileset-load name="content-docs">
            <p:input port="fileset">
                <p:pipe step="spine.fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="update-html" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:documentation>
            Create outline
        </p:documentation>
        <p:for-each>
            <p:wrap match="/html:html/html:body/node()" group-adjacent="true()"
                    wrapper="section" wrapper-namespace="http://www.w3.org/1999/xhtml">
                <!-- hack to get "Untitled section" rather than "Untitled document" -->
            </p:wrap>
            <px:html-outline fix-untitled-sections-in-outline="imply-heading" heading-links-only="true">
                <p:with-option name="output-base-uri" select="$ncc-base-uri"/>
            </px:html-outline>
            <p:filter select="/html:ol/html:li/html:ol"/>
        </p:for-each>
        <p:wrap-sequence wrapper="body" wrapper-namespace="http://www.w3.org/1999/xhtml"/>
        <px:set-base-uri name="outline">
            <p:with-option name="base-uri" select="$ncc-base-uri"/>
        </px:set-base-uri>
        <p:sink/>
        <p:documentation>
            Convert outline to NCC format and add page numbers.
        </p:documentation>
        <p:xslt>
            <p:input port="source">
                <p:pipe step="outline" port="result"/>
                <p:pipe step="main" port="page-list"/>
                <p:pipe step="content-docs" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/outline-to-ncc-body.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <!--
            First entry must be a h1 with class "title".
            FIXME: check that it is actually a h1 and not e.g. a page number
            FIXME: somehow ensure that the first heading is actually the book title?
        -->
        <p:add-attribute match="html:h1[not(preceding::html:h1)]" attribute-name="class" attribute-value="title"
                         name="body"/>
        <p:sink/>
        <p:documentation>
            Create head element for NCC from package doc
        </p:documentation>
        <px:opf-to-ncc-metadata name="head">
            <p:input port="source">
                <p:pipe step="opf" port="result"/>
            </p:input>
            <p:input port="ncc-body">
                <p:pipe step="body" port="result"/>
            </p:input>
            <p:input port="smil">
                <p:pipe step="augment-smils" port="smil.in-memory"/>
                <p:pipe step="new-smils" port="smil"/>
            </p:input>
        </px:opf-to-ncc-metadata>
        <p:sink/>
        <p:wrap-sequence wrapper="html" wrapper-namespace="http://www.w3.org/1999/xhtml">
            <p:input port="source">
                <p:pipe step="head" port="result"/>
                <p:pipe step="body" port="result"/>
            </p:input>
        </p:wrap-sequence>
        <p:add-attribute match="/*" attribute-name="lang">
            <p:with-option name="attribute-value" select="/*/html:head/html:meta[@name='dc:language']/@content"/>
        </p:add-attribute>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$ncc-base-uri"/>
        </px:set-base-uri>
    </p:group>
    <p:sink/>

    <p:documentation>
        Make anchors in NCC point to SMILs.
    </p:documentation>
    <p:group px:message="Creating linkbacks for NCC" px:message-severity="DEBUG" px:progress="3/10">
        <p:xslt>
            <p:input port="source">
                <p:pipe step="ncc" port="result"/>
                <p:pipe step="augment-smils" port="smil-with-textref"/>
                <p:pipe step="new-smils" port="smil"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/create-linkbacks.xsl"/>
            </p:input>
            <p:with-param port="parameters" name="is-ncc" select="'true'">
                <p:empty/>
            </p:with-param>
        </p:xslt>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="../../xslt/pretty-print.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:group>
    <p:identity name="ncc-with-linkbacks"/>
    <p:sink/>

    <p:documentation>Fileset of all updated HTML</p:documentation>
    <px:fileset-join name="xhtml.fileset">
        <p:input port="source">
            <p:pipe step="augment-smils" port="xhtml.fileset"/>
            <p:pipe step="xhtml-without-mo" port="result.fileset"/>
        </p:input>
    </px:fileset-join>
    <p:sink/>
    
    <p:group name="add-smils">
        <p:documentation>Add new SMILs and update with augmented SMILs</p:documentation>
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="update" port="result.in-memory"/>
        </p:output>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="main" port="source.fileset"/>
                <p:pipe step="new-smils.fileset" port="result"/>
            </p:input>
        </px:fileset-join>
        <px:fileset-update name="update">
            <p:input port="source.in-memory">
                <p:pipe step="main" port="source.in-memory"/>
                <p:pipe step="new-smils" port="smil"/>
            </p:input>
            <p:input port="update.fileset">
                <p:pipe step="augment-smils.smil.fileset" port="result"/>
            </p:input>
            <p:input port="update.in-memory">
                <p:pipe step="augment-smils" port="smil.in-memory"/>
            </p:input>
        </px:fileset-update>
    </p:group>

    <px:fileset-update name="update-html">
        <p:documentation>Update HTML</p:documentation>
        <p:input port="source.in-memory">
            <p:pipe step="add-smils" port="in-memory"/>
        </p:input>
        <p:input port="update.fileset">
            <p:pipe step="xhtml.fileset" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="augment-smils" port="xhtml.in-memory"/>
            <p:pipe step="new-smils" port="xhtml"/>
        </p:input>
    </px:fileset-update>

    <px:fileset-add-entry media-type="application/xhtml+xml" name="add-ncc">
        <p:documentation>Add generated NCC</p:documentation>
        <p:input port="source.in-memory">
            <p:pipe step="update-html" port="result.in-memory"/>
        </p:input>
        <p:input port="entry">
            <p:pipe step="ncc-with-linkbacks" port="result"/>
        </p:input>
    </px:fileset-add-entry>

</p:declare-step>
