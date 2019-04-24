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

    <p:output port="result.fileset" primary="true">
        <p:documentation>
            The resulting DAISY 2.02 fileset that includes the NCC and possibly additional or
            improved SMIL files.
        </p:documentation>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="result.xhtml" port="result"/>
        <p:pipe step="result.smil" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-add-entry,
            px:fileset-join,
            px:fileset-intersect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl">
        <p:documentation>
            px:opf-spine-to-fileset
        </p:documentation>
    </p:import>

    <p:documentation>
        Load content documents in spine order.
        <!-- assumes px:fileset-load loads documents in order defined in fileset and
             px:fileset-intersect does not alter order -->
    </p:documentation>
    <px:opf-spine-to-fileset name="spine.fileset">
        <p:input port="source">
            <p:pipe step="main" port="opf"/>
        </p:input>
    </px:opf-spine-to-fileset>
    <px:fileset-intersect>
        <p:input port="source">
            <p:pipe step="spine.fileset" port="result"/>
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
    </px:fileset-intersect>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>

    <p:documentation>
        Add missing IDs to heading and page number elements.
    </p:documentation>
    <p:for-each>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="../../xslt/add-missing-ids.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:for-each>
    <p:identity name="xhtml-with-ids"/>
    <p:sink/>

    <p:documentation>
        Create NCC file with references to all heading elements. Note that this NCC is invalid and
        needs to be fixed by the add-linkbacks step below.
    </p:documentation>
    <p:group>
        <p:variable name="ncc-base-uri" select="concat(replace(base-uri(/*),'[^/]+$',''),'ncc.html')">
            <p:pipe step="main" port="opf"/>
        </p:variable>
        <p:variable name="ncc-base-dir-string-length" select="string-length(replace($ncc-base-uri,'[^/]+$',''))"/>
        <p:xslt>
            <p:input port="source">
                <p:pipe step="main" port="opf"/>
                <p:pipe step="ncc.body" port="result"/>
                <p:pipe step="result.smil" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/opf-to-ncc-metadata.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:identity name="ncc.head"/>
        <p:for-each name="ncc-items">
            <p:iteration-source
                select="//*[self::html:h1 or
                            self::html:h2 or
                            self::html:h3 or
                            self::html:h4 or
                            self::html:h5 or
                            self::html:h6 or
                            self::html:span[matches(@class,'(^|\s)page-(front|normal|special)(\s|$)')]
                            ]">
                <p:pipe step="xhtml-with-ids" port="result"/>
            </p:iteration-source>
            <p:string-replace match="/*/text()">
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all">
                        <a xmlns="http://www.w3.org/1999/xhtml">REPLACEME</a>
                    </p:inline>
                </p:input>
                <p:with-option name="replace"
                               select="concat('&quot;',
                                              replace(normalize-space(string-join(//text(),' ')),'&quot;','&quot;&quot;'),
                                              '&quot;')">
                    <p:pipe step="ncc-items" port="current"/>
                </p:with-option>
            </p:string-replace>
            <p:wrap-sequence wrapper-namespace="http://www.w3.org/1999/xhtml">
                <p:with-option name="wrapper" select="/*/local-name()">
                    <p:pipe step="ncc-items" port="current"/>
                </p:with-option>
            </p:wrap-sequence>
            <p:add-attribute match="/*" attribute-name="id">
                <p:with-option name="attribute-value" select="concat('_',p:iteration-position())"/>
            </p:add-attribute>
            <p:choose>
                <p:when test="p:iteration-position()=1">
                    <p:add-attribute match="/*" attribute-name="class" attribute-value="title">
                        <p:documentation>
                            First entry must be a h1 with class "title".
                            FIXME: check that it is actually a h1 and not e.g. a page number
                        </p:documentation>
                    </p:add-attribute>
                </p:when>
                <p:when test="/html:span">
                    <p:add-attribute match="/*" attribute-name="class">
                        <p:with-option name="attribute-value" select="/*/@class">
                            <p:pipe step="ncc-items" port="current"/>
                        </p:with-option>
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <p:add-attribute match="/*/html:a" attribute-name="href">
                <p:with-option name="attribute-value"
                               select="concat(substring(/*/base-uri(),$ncc-base-dir-string-length + 1),'#',/*/@id)">
                    <p:pipe step="ncc-items" port="current"/>
                </p:with-option>
            </p:add-attribute>
        </p:for-each>
        <p:wrap-sequence wrapper="body" wrapper-namespace="http://www.w3.org/1999/xhtml"/>
        <p:identity name="ncc.body"/>
        <p:wrap-sequence wrapper="html" wrapper-namespace="http://www.w3.org/1999/xhtml">
            <p:input port="source">
                <p:pipe step="ncc.head" port="result"/>
                <p:pipe step="ncc.body" port="result"/>
            </p:input>
        </p:wrap-sequence>
        <p:add-attribute match="/*" attribute-name="lang">
            <p:with-option name="attribute-value" select="/*/html:head/html:meta[@name='dc:language']/@content"/>
        </p:add-attribute>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$ncc-base-uri"/>
        </px:set-base-uri>
    </p:group>
    <p:identity name="ncc"/>
    <p:sink/>

    <p:documentation>
        Augment SMIL files with references to heading elements.
    </p:documentation>
    <px:fileset-load media-types="application/smil+xml">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="augment-smils" px:message="Augmenting SMILs" px:message-severity="DEBUG">
        <p:output port="smil" sequence="true">
            <p:pipe step="smil" port="result"/>
        </p:output>
        <p:output port="xhtml" sequence="true">
            <p:pipe step="xhtml-with-linkbacks" port="result"/>
        </p:output>
        <p:documentation>Get the HTML file(s) that corresponds with this SMIL.</p:documentation>
        <p:variable name="smil-base" select="base-uri(/)"/>
        <p:filter>
            <p:input port="source">
                <p:pipe step="main" port="opf"/>
            </p:input>
            <p:with-option name="select"
                           select="concat('
                                     for $mo in /opf:package/opf:manifest/opf:item[resolve-uri(@href,base-uri())=&quot;',$smil-base,'&quot;]/@id
                                     return /opf:package/opf:manifest/opf:item[@media-overlay=$mo]
                                     ')"/>
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
        <px:fileset-join name="associated-xhtml.fileset"/>
        <px:fileset-intersect>
            <p:input port="source">
                <p:pipe step="spine.fileset" port="result"/>
                <p:pipe step="associated-xhtml.fileset" port="result"/>
            </p:input>
        </px:fileset-intersect>
        <!-- load HTML in spine order -->
        <px:fileset-load name="associated-xhtml" fail-on-not-found="true">
            <p:input port="in-memory">
                <p:pipe step="xhtml-with-ids" port="result"/>
            </p:input>
        </px:fileset-load>
        <p:documentation>Augment the SMIL.</p:documentation>
        <p:xslt px:message="- {$smil-base}" px:message-severity="DEBUG">
            <p:input port="source">
                <p:pipe step="augment-smils" port="current"/>
                <p:pipe step="associated-xhtml" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/augment-smil.xsl"/>
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
            <p:variable name="base-uri" select="base-uri()"/>
            <p:identity px:message="Adding linkbacks to {$base-uri}" px:message-severity="DEBUG"/>
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

    <p:documentation>
        Create new SMIL file with references to heading elements for every content document without
        media-overlay.
    </p:documentation>
    <p:filter select="for $itemref in /opf:package/opf:spine/opf:itemref/@idref
                      return /opf:package/opf:manifest/opf:item[@id=$itemref][not(@media-overlay)]">
        <p:input port="source">
            <p:pipe step="main" port="opf"/>
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
    <px:fileset-intersect>
        <p:input port="source">
            <p:pipe step="xhtml-without-mo.fileset" port="result"/>
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
    </px:fileset-intersect>
    <px:fileset-load fail-on-not-found="true">
        <p:input port="in-memory">
            <p:pipe step="xhtml-with-ids" port="result"/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="new-smils">
        <p:output port="smil" sequence="true" primary="true">
            <p:pipe step="smil" port="result"/>
        </p:output>
        <p:output port="xhtml" sequence="true">
            <p:pipe step="xhtml-with-linkbacks" port="result"/>
        </p:output>
        <p:variable name="base-uri" select="base-uri()"/>
        <p:identity px:message="Creating new SMIL for {$base-uri}" px:message-severity="DEBUG"/>
        <px:set-base-uri name="empty-smil">
            <p:input port="source">
                <p:inline exclude-inline-prefixes="#all">
                    <smil>
                        <head>
                            <meta name="dc:format" content="Daisy 2.02"/>
                            <meta name="ncc:generator" content="DAISY Pipeline 2"/>
                            <meta name="ncc:timeInThisSmil" content="00:00:00"/>
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
                <p:pipe step="new-smils" port="current"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/augment-smil.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:choose>
            <p:when test="exists(//par)">
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

    <p:identity name="result.smil">
        <p:input port="source">
            <p:pipe step="augment-smils" port="smil"/>
            <p:pipe step="new-smils" port="smil"/>
        </p:input>
    </p:identity>
    <p:sink/>

    <p:documentation>
        Make anchors in NCC point to SMILs.
    </p:documentation>
    <p:group px:message="Creating linkbacks for NCC" px:message-severity="DEBUG">
        <p:xslt>
            <p:input port="source">
                <p:pipe step="ncc" port="result"/>
                <p:pipe step="result.smil" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/create-linkbacks.xsl"/>
            </p:input>
            <p:with-param port="parameters" name="is-ncc" select="'true'"/>
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
    
    <p:identity name="result.xhtml">
        <p:input port="source">
            <p:pipe step="ncc-with-linkbacks" port="result"/>
            <p:pipe step="augment-smils" port="xhtml"/>
            <p:pipe step="new-smils" port="xhtml"/>
        </p:input>
    </p:identity>
    <p:sink/>
    
    <px:fileset-join>
        <p:documentation>Add SMILs to fileset</p:documentation>
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
            <p:pipe step="new-smils.fileset" port="result"/>
        </p:input>
    </px:fileset-join>
    <px:fileset-add-entry name="result.fileset"
                          media-type="application/xhtml+xml">
        <p:documentation>Add generated NCC</p:documentation>
        <p:with-option name="href" select="base-uri(/*)">
            <p:pipe step="ncc" port="result"/>
        </p:with-option>
    </px:fileset-add-entry>
    
    <px:fileset-update name="result.in-memory">
        <p:input port="update">
            <p:pipe step="result.xhtml" port="result"/>
            <p:pipe step="result.smil" port="result"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-update>
    
</p:declare-step>
