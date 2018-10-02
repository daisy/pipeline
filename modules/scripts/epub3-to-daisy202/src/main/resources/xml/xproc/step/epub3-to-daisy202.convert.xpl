<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:epub3-to-daisy202-convert" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:dc="http://purl.org/dc/elements/1.1/">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="result.fileset"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="result.in-memory.smil"/>
        <p:pipe port="result" step="result.in-memory.xhtml"/>
    </p:output>

    <p:option name="bundle-dtds" select="'false'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <px:assert test-count-min="1" test-count-max="1" error-code="PED01" message="The EPUB must contain exactly one OPF document"/>
    <px:assert error-code="PED02" message="There must be at least one dc:identifier meta element in the OPF document">
        <p:with-option name="test" select="exists(/opf:package/opf:metadata/dc:identifier)"/>
    </px:assert>
    <p:identity name="opf.in"/>
    <p:sink/>

    <px:fileset-load media-types="application/smil+xml">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="smil.in"/>
    <p:sink/>

    <!--<px:fileset-load media-types="application/xhtml+xml">
        <p:with-option name="href" select="(/*/opf:manifest/opf:item[tokenize(@properties,'\s+')='nav']/resolve-uri(@href,base-uri()))[1]">
            <p:pipe port="result" step="opf.in"/>
        </p:with-option>
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="xhtml.in.nav"/>
    <p:sink/>-->

    <!-- get content documents in spine order -->
    <p:for-each>
        <p:iteration-source select="/*/opf:spine/opf:itemref">
            <p:pipe port="result" step="opf.in"/>
        </p:iteration-source>
        <p:variable name="idref" select="/*/@idref"/>
        <p:identity>
            <p:input port="source">
                <p:pipe port="result" step="opf.in"/>
            </p:input>
        </p:identity>
        <p:filter>
            <p:with-option name="select" select="concat('/*/opf:manifest/opf:item[@id=&quot;',$idref,'&quot;]')"/>
        </p:filter>
        <px:fileset-load>
            <p:with-option name="href" select="/*/resolve-uri(@href,base-uri())"/>
            <p:input port="fileset">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe port="in-memory.in" step="main"/>
            </p:input>
        </px:fileset-load>
    </p:for-each>
    <p:identity name="xhtml.in"/>
    <p:sink/>



    <p:for-each>
        <p:iteration-source>
            <p:pipe port="result" step="smil.in"/>
        </p:iteration-source>
        <p:variable name="smil-original-base" select="base-uri(/*)"/>

        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>

        <px:message message="converting SMIL-file from 3.0 (EPUB3 MO profile) to 1.0 (DAISY 2.02 profile): $1">
            <p:with-option name="param1" select="$smil-original-base"/>
        </px:message>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/smil3-to-smil1.xsl"/>
            </p:input>
        </p:xslt>
        <p:viewport match="//text[@src]" xmlns="">
            <p:add-attribute match="/*" attribute-name="src">
                <p:with-option name="attribute-value" select="replace(/*/@src,'\.xhtml(#|$)','.html$1','i')"/>
            </p:add-attribute>
        </p:viewport>
    </p:for-each>
    <p:identity name="result.in-memory.smil"/>
    <p:sink/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="result" step="xhtml.in"/>
        </p:iteration-source>

        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>

        <!-- normalize HTML5 -->
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <!-- stylesheet hopefully preserves all IDs (!) -->
                <p:document href="http://www.daisy.org/pipeline/modules/html-utils/html5-upgrade.xsl"/>
            </p:input>
        </p:xslt>

        <!-- downgrade to HTML4 -->
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <!-- stylesheet preserves all IDs -->
                <p:document href="../../xslt/html5-to-html4.xsl"/>
            </p:input>
        </p:xslt>

        <!-- update links to HTML files -->
        <p:group>
            <p:viewport match="//html:*[matches(@href,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="href">
                    <p:with-option name="attribute-value" select="replace(/*/@href,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:viewport match="//html:*[matches(@src,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="src">
                    <p:with-option name="attribute-value" select="replace(/*/@src,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:viewport match="//html:*[matches(@cite,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="cite">
                    <p:with-option name="attribute-value" select="replace(/*/@cite,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:viewport match="//html:*[matches(@longdesc,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="longdesc">
                    <p:with-option name="attribute-value" select="replace(/*/@longdesc,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:viewport match="//html:object[matches(@data,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="data">
                    <p:with-option name="attribute-value" select="replace(/*/@data,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:viewport match="//html:form[matches(@action,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="action">
                    <p:with-option name="attribute-value" select="replace(/*/@action,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:viewport match="//html:head[matches(@profile,'\.xhtml(#|$)')]">
                <p:add-attribute match="/*" attribute-name="profile">
                    <p:with-option name="attribute-value" select="replace(/*/@profile,'.xhtml(#|$)','.html$1')"/>
                </p:add-attribute>
            </p:viewport>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/add-missing-ids.xsl"/>
                </p:input>
            </p:xslt>
        </p:group>
    </p:for-each>
    <p:identity name="converted-content"/>
    <p:sink/>

    <p:group>
        <p:variable name="ncc-base-uri" select="concat(replace(base-uri(/*),'[^/]+$',''),'ncc.html')">
            <p:pipe port="result" step="opf.in"/>
        </p:variable>
        <p:variable name="ncc-base-dir-string-length" select="string-length(replace($ncc-base-uri,'[^/]+$',''))"/>

        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="source">
                <p:pipe port="result" step="opf.in"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/opf-to-html-metadata.xsl"/>
            </p:input>
        </p:xslt>
        <p:identity name="generated-ncc.head"/>

        <p:for-each name="ncc-items">
            <p:iteration-source
                select="//*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6 or self::html:span[matches(@class,'(^|\s)page-(front|normal|special)(\s|$)')]]">
                <p:pipe step="converted-content" port="result"/>
            </p:iteration-source>
            <p:string-replace match="/*/text()">
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all">
                        <a xmlns="http://www.w3.org/1999/xhtml">REPLACEME</a>
                    </p:inline>
                </p:input>
                <p:with-option name="replace" select="concat('&quot;',replace(normalize-space(string-join(//text(),' ')),'&quot;','&quot;&quot;'),'&quot;')">
                    <p:pipe port="current" step="ncc-items"/>
                </p:with-option>
            </p:string-replace>
            <p:wrap-sequence wrapper-namespace="http://www.w3.org/1999/xhtml">
                <p:with-option name="wrapper" select="/*/local-name()">
                    <p:pipe port="current" step="ncc-items"/>
                </p:with-option>
            </p:wrap-sequence>
            <p:add-attribute match="/*/html:a" attribute-name="href">
                <p:with-option name="attribute-value" select="concat(substring(/*/base-uri(),$ncc-base-dir-string-length),'#',/*/@id)">
                    <p:pipe port="current" step="ncc-items"/>
                </p:with-option>
            </p:add-attribute>
        </p:for-each>
        <p:identity name="generated-ncc.body"/>

        <p:wrap-sequence wrapper="body" wrapper-namespace="http://www.w3.org/1999/xhtml"/>
        <p:wrap-sequence wrapper="html" wrapper-namespace="http://www.w3.org/1999/xhtml">
            <p:input port="source">
                <p:pipe port="result" step="generated-ncc.head"/>
                <p:pipe port="result" step="generated-ncc.body"/>
            </p:input>
        </p:wrap-sequence>
        <p:add-attribute match="/*" attribute-name="lang">
            <p:with-option name="attribute-value" select="/*/html:head/html:meta[@name='dc:language']/@content"/>
        </p:add-attribute>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="$ncc-base-uri"/>
        </p:add-attribute>
    </p:group>
    <p:identity name="generated-ncc"/>
    <p:sink/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="result" step="generated-ncc"/>
            <p:pipe port="result" step="converted-content"/>
        </p:iteration-source>

        <p:variable name="xhtml-original-base" select="base-uri(/*)"/>
        <p:variable name="xhtml-new-base" select="replace(base-uri(/*),'^(.*)\.([^/\.]*)$','$1.html')"/>

        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="$xhtml-new-base"/>
        </p:add-attribute>

        <!-- add linkbacks to html files where a smil file is associated -->
        <p:identity name="xhtml.before-linkbacks"/>
        <p:group>
            <p:variable name="associated-smil-original-base" select="(//opf:item[//opf:item[resolve-uri(@href,base-uri())=$xhtml-original-base]/@media-overlay=@id])[1]/resolve-uri(@href,base-uri())">
                <p:pipe port="result" step="opf.in"/>
            </p:variable>
            <p:for-each>
                <p:iteration-source>
                    <p:pipe port="result" step="result.in-memory.smil"/>
                </p:iteration-source>
                <p:choose>
                    <p:when test="base-uri(/*) = $associated-smil-original-base">
                        <p:identity/>
                    </p:when>
                    <p:otherwise>
                        <p:identity>
                            <p:input port="source">
                                <p:empty/>
                            </p:input>
                        </p:identity>
                    </p:otherwise>
                </p:choose>
            </p:for-each>
            <p:identity name="associated-smil"/>
            <p:count name="associated-smil.count"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="result" step="xhtml.before-linkbacks"/>
                </p:input>
            </p:identity>
            <p:choose>
                <p:xpath-context>
                    <p:pipe port="result" step="associated-smil.count"/>
                </p:xpath-context>
                <p:when test=".=1">
                    <p:insert match="/*" position="first-child">
                        <p:input port="insertion">
                            <p:pipe port="result" step="associated-smil"/>
                        </p:input>
                    </p:insert>
                    <p:xslt>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="../../xslt/create-linkbacks.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:group>
        <p:identity name="xhtml.after-linkbacks"/>

        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="../../xslt/pretty-print.xsl"/>
            </p:input>
        </p:xslt>
    </p:for-each>
    <p:identity name="result.in-memory.xhtml"/>
    <p:sink/>

    <p:identity>
        <p:input port="source">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
    </p:identity>
    <px:fileset-rebase>
        <p:with-option name="new-base" select="replace((/*/d:file[@media-type='application/oebps-package+xml'])[1]/resolve-uri(@href,base-uri()),'[^/]+$','')"/>
    </px:fileset-rebase>
    <p:delete match="//d:file[@media-type=('application/oebps-package+xml','application/x-dtbncx+xml') or starts-with(@href,'..') or starts-with(@href,'META-INF/') or @href='mimetype']"/>
    <p:viewport match="//d:file[@media-type='application/xhtml+xml']">
        <p:variable name="base-uri" select="resolve-uri(/*/@href,base-uri(/*))"/>
        <p:choose>
            <p:when test="(//opf:item[tokenize(@properties,'\s+')='nav']/resolve-uri(@href,base-uri()))[1] = $base-uri">
                <p:xpath-context>
                    <p:pipe port="result" step="opf.in"/>
                </p:xpath-context>
                <p:output port="result" sequence="true">
                    <p:empty/>
                </p:output>
                <p:sink>
                    <p:documentation>Delete original navigation document. It will be replaced with the generated NCC.</p:documentation>
                </p:sink>
            </p:when>
            <p:otherwise>
                <p:output port="result"/>
                <p:add-attribute attribute-name="href" match="/*">
                    <p:with-option name="attribute-value" select="replace(/*/@href,'^(.*)\.([^/\.]*)$','$1.html')"/>
                </p:add-attribute>
            </p:otherwise>
        </p:choose>
    </p:viewport>
    <px:fileset-add-entry href="ncc.html" media-type="application/xhtml+xml">
        <p:documentation>Add generated NCC</p:documentation>
    </px:fileset-add-entry>
    <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']"
                     attribute-name="doctype-public"
                     attribute-value="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
    <p:add-attribute match="//d:file[@media-type='application/xhtml+xml']"
                     attribute-name="doctype-system"
                     attribute-value="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
    <p:viewport match="//d:file[@media-type='application/smil+xml']">
        <p:add-attribute match="/*" attribute-name="doctype-public" attribute-value="-//W3C//DTD SMIL 1.0//EN"/>
        <p:add-attribute match="/*" attribute-name="doctype-system" attribute-value="http://www.w3.org/TR/REC-SMIL/SMIL10.dtd"/>
    </p:viewport>
    <p:xslt>
        <p:with-param name="preserve-empty-whitespace" select="'false'"/>
        <p:input port="stylesheet">
            <p:document href="../../xslt/pretty-print.xsl"/>
        </p:input>
    </p:xslt>
    <p:identity name="result.fileset"/>

</p:declare-step>
