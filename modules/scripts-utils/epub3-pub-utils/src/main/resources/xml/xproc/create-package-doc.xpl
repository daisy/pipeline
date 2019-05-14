<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:epub3-pub-create-package-doc" name="main">

    <!-- Note: all URIs in options and xml:base attributes must be absolute. -->
    <p:input port="spine-filesets" sequence="true" primary="true"/>
    <p:input port="publication-resources">
        <p:inline>
            <d:fileset/>
        </p:inline>
    </p:input>
    <p:input port="metadata" sequence="true">
        <p:inline>
            <opf:metadata/>
        </p:inline>
    </p:input>
    <p:input port="bindings" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="content-docs" sequence="true"/>
    <p:input port="mediaoverlays" sequence="true">
        <p:empty/>
    </p:input>
    <p:option name="nav-uri" select="''"/>
    <p:option name="cover-image" required="false" select="''"/>
    <p:option name="compatibility-mode" required="false" select="'true'"/>
    <p:option name="detect-properties" required="false" select="'true'"/>
    <p:option name="result-uri" required="true"/>
    <p:output port="result" primary="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
            px:add-xml-base
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-nav-utils/library.xpl">
        <p:documentation>
            px:epub3-nav-to-guide
        </p:documentation>
    </p:import>

    <px:fileset-join/>
    <px:mediatype-detect name="spine-filesets-with-mediatypes">
        <p:input port="in-memory">
            <p:pipe port="content-docs" step="main"/>
            <p:pipe port="mediaoverlays" step="main"/>
        </p:input>
    </px:mediatype-detect>

    <p:split-sequence>
        <p:with-option name="test" select="if (not($nav-uri='')) then concat('base-uri(/*)=&quot;',resolve-uri($nav-uri),'&quot;') else '//html:nav/@*[name()=&quot;epub:type&quot;]=&quot;toc&quot;'">
            <p:empty/>
        </p:with-option>
        <p:input port="source">
            <p:pipe port="content-docs" step="main"/>
        </p:input>
    </p:split-sequence>
    <px:assert message="There must be exactly one navigation document in the fileset" test-count-min="1" test-count-max="1" error-code="PEPU14"/>
    <px:message severity="DEBUG" message="Navigation document extracted from fileset"/>
    <p:identity name="nav-doc"/>
    <p:sink/>

    <p:group name="metadata">
        <p:output port="result"/>
        <p:uuid name="default-metadata" match="dc:identifier/text()">
            <p:input port="source">
                <p:inline>
                    <opf:metadata>
                        <dc:title>Unknown</dc:title>
                        <dc:identifier>generated-uuid</dc:identifier>
                    </opf:metadata>
                </p:inline>
            </p:input>
        </p:uuid>
        <p:wrap-sequence wrapper="_">
            <p:input port="source">
                <p:pipe port="metadata" step="main"/>
                <p:pipe port="result" step="default-metadata"/>
            </p:input>
        </p:wrap-sequence>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="create-metadata.merge.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:delete match="opf:meta[@property='media:duration']"/>
        <p:identity name="metadata.without-duration"/>
        <p:sink/>

        <p:for-each name="metadata.durations">
            <p:output port="result" sequence="true"/>
            <p:iteration-source>
                <p:pipe port="mediaoverlays" step="main"/>
            </p:iteration-source>
            <p:variable name="base" select="base-uri(/*)"/>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="create-package-doc.estimate-mediaoverlay-duration.xsl"/>
                </p:input>
            </p:xslt>
            <p:add-attribute match="/*" attribute-name="refines">
                <p:with-option name="attribute-value" select="concat('#',/*/d:file[replace(resolve-uri(@href,base-uri(.)),'/+','/') = replace($base,'/+','/')]/@id)">
                    <p:pipe port="fileset" step="manifest"/>
                </p:with-option>
            </p:add-attribute>
        </p:for-each>
        <p:sink/>

        <p:group name="metadata.total-duration">
            <p:output port="result" sequence="true"/>
            <p:count>
                <p:input port="source">
                    <p:pipe port="result" step="metadata.durations"/>
                </p:input>
            </p:count>
            <p:choose>
                <p:when test="/*=0">
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:otherwise>
                    <p:wrap-sequence wrapper="_">
                        <p:input port="source">
                            <p:pipe port="result" step="metadata.durations"/>
                        </p:input>
                    </p:wrap-sequence>
                    <p:xslt>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="create-package-doc.sum-mediaoverlay-durations.xsl"/>
                        </p:input>
                    </p:xslt>
                </p:otherwise>
            </p:choose>
        </p:group>
        <p:sink/>

        <p:insert match="/*" position="last-child">
            <p:input port="source">
                <p:pipe port="result" step="metadata.without-duration"/>
            </p:input>
            <p:input port="insertion">
                <p:pipe port="result" step="metadata.durations"/>
                <p:pipe port="result" step="metadata.total-duration"/>
            </p:input>
        </p:insert>

        <p:choose>
            <p:when test="$compatibility-mode='true'">
                <p:xslt>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="create-package-doc.backwards-compatible-metadata.xsl"/>
                    </p:input>
                </p:xslt>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <px:message severity="DEBUG" message="Successfully created package document metadata"/>
    </p:group>
    <p:sink/>

    <p:group name="manifest">
        <p:output port="fileset" primary="true">
            <p:pipe port="fileset" step="manifest.out"/>
        </p:output>
        <p:output port="manifest">
            <p:pipe port="manifest" step="manifest.out"/>
        </p:output>

        <p:group name="manifest.bindings">
            <p:output port="result" sequence="true"/>
            <p:count>
                <p:input port="source">
                    <p:pipe port="bindings" step="main"/>
                </p:input>
            </p:count>
            <p:choose>
                <p:when test="/*=0">
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:when test="/*=1">
                    <p:set-attributes match="d:file">
                        <p:input port="source">
                            <p:pipe port="bindings" step="main"/>
                        </p:input>
                        <p:input port="attributes">
                            <p:inline>
                                <d:file media-type="application/xhtml+xml"/>
                            </p:inline>
                        </p:input>
                    </p:set-attributes>
                </p:when>
                <p:otherwise>
                    <p:error code="PEPUTODO">
                        <p:input port="source">
                            <p:inline>
                                <c:message>There can be at most one set of bindings.</c:message>
                            </p:inline>
                        </p:input>
                    </p:error>
                </p:otherwise>
            </p:choose>
        </p:group>
        <p:sink/>

        <p:for-each name="manifest.content-docs">
            <p:output port="result" sequence="true"/>
            <p:iteration-source>
                <p:pipe port="content-docs" step="main"/>
            </p:iteration-source>
            <p:variable name="doc-base" select="p:base-uri(/*)"/>
            <p:identity name="manifest.content-docs.current"/>
            <px:fileset-create>
                <p:with-option name="base" select="$result-uri"/>
            </px:fileset-create>
            <px:fileset-add-entry>
                <p:with-option name="href" select="$doc-base"/>
                <p:with-option name="media-type" select="'application/xhtml+xml'"/>
            </px:fileset-add-entry>
            <p:choose>
                <p:when test="$detect-properties='true'">
                    <p:add-attribute attribute-name="mathml" match="/*/*">
                        <p:with-option name="attribute-value" select="distinct-values(//namespace::*)='http://www.w3.org/1998/Math/MathML'">
                            <p:pipe port="result" step="manifest.content-docs.current"/>
                        </p:with-option>
                    </p:add-attribute>
                    <p:add-attribute attribute-name="svg" match="/*/*">
                        <p:with-option name="attribute-value"
                            select="(//html:embed|//html:iframe)/@src/ends-with(.,'.svg') or (//html:embed|//html:object)/@type='image/svg+xml' or distinct-values(//namespace::*)='http://www.w3.org/2000/svg'">
                            <p:pipe port="result" step="manifest.content-docs.current"/>
                        </p:with-option>
                    </p:add-attribute>
                    <p:add-attribute attribute-name="scripted" match="/*/*">
                        <p:with-option name="attribute-value"
                            select="
                                              count(//*/@href[starts-with(.,'javascript:')]) &gt; 0 or
                                              //html:script/@type=('','text/javascript','text/ecmascript','text/javascript1.0','text/javascript1.1',
                                                                  'text/javascript1.2','text/javascript1.3','text/javascript1.4','text/javascript1.5',
                                                                  'text/jscript','text/livescript','text/x-javascript','text/x-ecmascript',
                                                                  'application/x-javascript','application/x-ecmascript','application/javascript',
                                                                  'application/ecmascript') or
                                              //*/@*/name()=('onabort','onafterprint','onbeforeprint','onbeforeunload','onblur','oncanplay','oncanplaythrough','onchange','onclick','oncontextmenu',
                                                              'oncuechange','ondblclick','ondrag','ondragend','ondragenter','ondragleave','ondragover','ondragstart','ondrop','ondurationchange',
                                                              'onemptied','onended','onerror','onfocus','onhashchange','oninput','oninvalid','onkeydown','onkeypress','onkeyup','onload','onloadeddata',
                                                              'onloadedmetadata','onloadstart','onmessage','onmousedown','onmousemove','onmouseout','onmouseover','onmouseup','onmousewheel','onoffline',
                                                              'ononline','onpagehide','onpageshow','onpause','onplay','onplaying','onpopstate','onprogress','onratechange','onreset','onresize','onscroll',
                                                              'onseeked','onseeking','onselect','onshow','onstalled','onstorage','onsubmit','onsuspend','ontimeupdate','onunload','onvolumechange','onwaiting')
                                            ">
                            <p:pipe port="result" step="manifest.content-docs.current"/>
                        </p:with-option>
                    </p:add-attribute>
                    <p:add-attribute attribute-name="switch" match="/*/*">
                        <p:with-option name="attribute-value" select="count(//epub:switch) &gt; 0">
                            <p:pipe port="result" step="manifest.content-docs.current"/>
                        </p:with-option>
                    </p:add-attribute>
                    <p:add-attribute attribute-name="remote-resources" match="/*/*">
                        <p:with-option name="attribute-value" select="count(//*/@src[contains(tokenize(.,'/')[1],':')][1]) &gt; 0">
                            <p:pipe port="result" step="manifest.content-docs.current"/>
                        </p:with-option>
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <p:add-attribute attribute-name="nav" match="/*/*">
                <p:with-option name="attribute-value" select="base-uri(/*)=$doc-base">
                    <p:pipe port="result" step="nav-doc"/>
                </p:with-option>
            </p:add-attribute>
        </p:for-each>
        <p:sink/>

        <p:for-each name="manifest.mediaoverlays">
            <p:output port="result" sequence="true" primary="true"/>
            <p:iteration-source>
                <p:pipe port="mediaoverlays" step="main"/>
            </p:iteration-source>
            <p:variable name="doc-base" select="base-uri(/*)"/>
            <px:fileset-create>
                <p:with-option name="base" select="$result-uri"/>
            </px:fileset-create>
            <px:fileset-add-entry>
                <p:with-option name="href" select="$doc-base"/>
                <p:with-option name="media-type" select="'application/smil+xml'"/>
            </px:fileset-add-entry>
        </p:for-each>
        <p:sink/>

        <px:fileset-join>
            <p:input port="source">
                <!-- TODO: test to make sure that the resulting URIs turns out as relative to $result-uri -->
                <p:pipe port="result" step="manifest.content-docs"/>
                <p:pipe port="result" step="spine-filesets-with-mediatypes"/>
                <p:pipe port="result" step="manifest.mediaoverlays"/>
                <p:pipe port="publication-resources" step="main"/>
                <p:pipe port="result" step="manifest.bindings"/>
            </p:input>
        </px:fileset-join>
        <p:group name="manifest.ids">
            <p:output port="result"/>
            <!--            <p:variable name="manifest-uri" select="base-uri(/*)"/>-->
            <p:viewport match="d:file">
                <p:add-attribute match="/*" attribute-name="href">
                    <p:with-option name="attribute-value" select="/*/resolve-uri(@href,base-uri(.))"/>
                </p:add-attribute>
                <p:add-attribute match="/*" attribute-name="id">
                    <p:with-option name="attribute-value" select="concat('item_',p:iteration-position())"/>
                </p:add-attribute>
                <p:add-attribute match="/*" attribute-name="cover-image">
                    <p:with-option name="attribute-value" select="/*/resolve-uri(@href,base-uri(.))=resolve-uri($cover-image,$result-uri)"/>
                </p:add-attribute>
            </p:viewport>
        </p:group>
        <p:sink/>

        <p:wrap-sequence wrapper="d:fallback">
            <p:input port="source">
                <p:pipe port="result" step="spine-filesets-with-mediatypes"/>
            </p:input>
        </p:wrap-sequence>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="create-package-doc.fileset-resolve.xsl"/>
            </p:input>
        </p:xslt>
        <p:insert match="/*" position="first-child">
            <p:input port="insertion">
                <p:pipe port="result" step="manifest.ids"/>
            </p:input>
        </p:insert>
        <p:xslt name="manifest.fallbacks">
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="create-package-doc.manifest-fallbacks.xsl"/>
            </p:input>
        </p:xslt>
        <p:sink/>

        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="manifest.ids"/>
            </p:input>
        </px:fileset-join>
        <p:group name="manifest.out">
            <p:output port="fileset">
                <p:pipe port="result" step="manifest.out.fileset"/>
            </p:output>
            <p:output port="manifest">
                <p:pipe port="result" step="manifest.out.manifest"/>
            </p:output>
            <p:variable name="manifest-base" select="base-uri(/*)"/>
            <px:message severity="DEBUG" message="Creating package document manifest and fileset..."/>
            <p:viewport match="//d:file" name="manifest.out.fileset">
                <p:output port="result"/>
                <p:variable name="href" select="resolve-uri(/*/@href,$manifest-base)"/>
                <p:choose>
                    <p:xpath-context>
                        <p:pipe port="result" step="manifest.fallbacks"/>
                    </p:xpath-context>
                    <p:when test="/*/d:file[resolve-uri(@href,base-uri(.))=$href]/@fallback">
                        <p:add-attribute match="/*" attribute-name="fallback">
                            <p:with-option name="attribute-value" select="/*/d:file[resolve-uri(@href,base-uri(.))=$href]/@fallback">
                                <p:pipe port="result" step="manifest.fallbacks"/>
                            </p:with-option>
                        </p:add-attribute>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
            </p:viewport>
            <px:message severity="DEBUG" message="Successfully created fileset for package document"/>
            <p:xslt name="manifest.out.manifest">
                <p:with-param name="result-uri" select="$result-uri"/>
                <p:input port="stylesheet">
                    <p:document href="create-package-doc.fileset-to-manifest.xsl"/>
                </p:input>
            </p:xslt>
            <px:message severity="DEBUG" message="Successfully created package document manifest"/>
            <p:sink/>
        </p:group>

    </p:group>
    <p:sink/>

    <p:group name="spine">
        <p:output port="result"/>
        <p:group name="content-docs-primary">
            <p:output port="result"/>
            <px:fileset-filter media-types="application/xhtml+xml">
                <p:input port="source">
                    <p:pipe port="result" step="spine-filesets-with-mediatypes"/>
                </p:input>
            </px:fileset-filter>
        </p:group>
        <p:group name="content-docs-resources">
            <p:output port="result"/>
            <px:fileset-diff>
                <p:input port="source">
                    <p:pipe port="publication-resources" step="main"/>
                </p:input>
                <p:input port="secondary">
                    <p:pipe port="result" step="spine-filesets-with-mediatypes"/>
                </p:input>
            </px:fileset-diff>
            <px:fileset-filter media-types="application/xhtml+xml"/>
            <p:add-attribute match="/d:fileset/d:file" attribute-name="linear" attribute-value="no"/>
        </p:group>    
        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="content-docs-primary"/>
                <p:pipe port="result" step="content-docs-resources"/>
            </p:input>
        </px:fileset-join>
        <p:group>
            <p:viewport match="/*/d:file">
                <p:variable name="file-uri" select="/*/resolve-uri(@href,base-uri(.))"/>
                <p:add-attribute match="/*" attribute-name="idref">
                    <p:with-option name="attribute-value" select="/*/d:file[replace(resolve-uri(@href,base-uri(.)),'/+','/') = replace($file-uri,'/+','/')]/@id">
                        <p:pipe port="fileset" step="manifest"/>
                    </p:with-option>
                </p:add-attribute>
            </p:viewport>
        </p:group>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="create-package-doc.idref-fileset-to-spine.xsl"/>
            </p:input>
        </p:xslt>
        <p:choose>
            <p:xpath-context>
                <p:pipe port="fileset" step="manifest"/>
            </p:xpath-context>
            <p:when test="$compatibility-mode='true' and //@media-type='application/x-dtbncx+xml'">
                <p:add-attribute match="/*" attribute-name="toc">
                    <p:with-option name="attribute-value" select="//*[@media-type='application/x-dtbncx+xml']/@id">
                        <p:pipe port="fileset" step="manifest"/>
                    </p:with-option>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <px:message severity="DEBUG" message="Successfully created package document spine"/>
    </p:group>
    <p:sink/>

    <p:documentation>If the navigation document contains landmarks and compatibility-mode is enabled; generate the guide element based on the landmarks.</p:documentation>
    <p:group name="guide">
        <p:output port="result" sequence="true"/>
        <p:identity>
            <p:input port="source">
                <p:pipe port="result" step="nav-doc"/>
            </p:input>
        </p:identity>
        <p:for-each>
            <p:iteration-source select="//html:nav[@*[name()='epub:type']='landmarks']"/>
            <p:identity/>
        </p:for-each>
        <p:identity name="guide.landmarks"/>
        <p:count name="guide.count"/>
        <p:identity>
            <p:input port="source">
                <p:pipe port="result" step="guide.landmarks"/>
            </p:input>
        </p:identity>
        <p:choose>
            <p:when test="/*=0 or not($compatibility-mode='true')">
                <p:xpath-context>
                    <p:pipe port="result" step="guide.count"/>
                </p:xpath-context>
                <px:message severity="DEBUG" message="No landmarks in package document"/>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <px:message severity="DEBUG" message="Creating guide element for package document"/>
                <px:epub3-nav-to-guide>
                    <p:with-option name="opf-base" select="$result-uri"/>
                </px:epub3-nav-to-guide>
                <px:message severity="DEBUG" message="guide element created successfully"/>
            </p:otherwise>
        </p:choose>
    </p:group>
    <p:sink/>

    <p:group name="bindings">
        <p:output port="result" sequence="true"/>
        <p:count>
            <p:input port="source">
                <p:pipe port="bindings" step="main"/>
            </p:input>
        </p:count>
        <p:choose>
            <p:when test="/* = 0 or not($compatibility-mode='true')">
                <px:message severity="DEBUG" message="No bindings in package document"/>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="bindings" step="main"/>
                    </p:input>
                </p:identity>
                <px:message severity="DEBUG" message="Creating bindings element for package document"/>
                <p:group>
                    <p:viewport match="/*/d:file">
                        <p:variable name="file-uri" select="/*/resolve-uri(@href,base-uri(.))"/>
                        <p:add-attribute match="/*" attribute-name="handler">
                            <p:with-option name="attribute-value" select="concat('#',/*/d:file[replace(resolve-uri(@href,base-uri(.)),'/+','/') = replace($file-uri,'/+','/')]/@id)">
                                <p:pipe port="fileset" step="manifest"/>
                            </p:with-option>
                        </p:add-attribute>
                    </p:viewport>
                </p:group>
                <p:xslt>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="create-package-doc.handler-fileset-to-bindings.xsl"/>
                    </p:input>
                </p:xslt>
                <px:message severity="DEBUG" message="bindings element created successfully"/>
            </p:otherwise>
        </p:choose>
    </p:group>
    <p:sink/>

    <p:insert match="/*" position="last-child">
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <package xmlns="http://www.idpf.org/2007/opf" version="3.0"/>
            </p:inline>
        </p:input>
        <p:input port="insertion">
            <!-- TODO declare @prefix -->
            <p:pipe port="result" step="metadata"/>
            <p:pipe port="manifest" step="manifest"/>
            <p:pipe port="result" step="spine"/>
            <p:pipe port="result" step="guide"/>
            <p:pipe port="result" step="bindings"/>
        </p:input>
    </p:insert>
    <p:add-attribute match="/*" attribute-name="unique-identifier">
        <p:with-option name="attribute-value" select="/opf:package/opf:metadata/dc:identifier/@id"/>
    </p:add-attribute>
    <p:choose>
        <p:when test="/opf:package/opf:metadata/@prefix">
            <p:add-attribute attribute-name="prefix" match="/*">
                <p:with-option name="attribute-value" select="/opf:package/opf:metadata/@prefix"/>
            </p:add-attribute>
            <p:delete match="/opf:package/opf:metadata/@prefix"/>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <px:set-base-uri>
        <p:with-option name="base-uri" select="$result-uri"/>
    </px:set-base-uri>
    <px:add-xml-base root="false"/>
    <p:identity name="package-without-mo"/>
    <p:identity>
        <p:input port="source">
            <p:pipe port="result" step="package-without-mo"/>
            <p:pipe port="mediaoverlays" step="main"/>
        </p:input>
    </p:identity>
    <px:message severity="DEBUG" message="Assigning media overlays to their corresponding content documents..."/>
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="assign-media-overlays.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <px:message severity="DEBUG" message="Finished assigning media overlays to content documents"/>

</p:declare-step>
