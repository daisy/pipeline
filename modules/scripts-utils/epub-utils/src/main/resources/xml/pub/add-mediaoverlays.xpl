<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:opf="http://www.idpf.org/2007/opf"
                type="px:epub3-add-mediaoverlays" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Add media overlays to a EPUB publication</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The source fileset</p>
            <p>Must include the package document.</p>
        </p:documentation>
    </p:input>

    <p:input port="mo.fileset"/>
    <p:input port="mo.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The media overlays fileset with the SMIL and audio files</p>
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result fileset</p>
            <p>A copy of the source fileset with the media overlay files added and the package
            document updated.</p>
        </p:documentation>
        <p:pipe step="update-metadata" port="result"/>
        <p:pipe step="filter-package-doc" port="not-matched.in-memory"/>
        <p:pipe step="main" port="mo.in-memory"/>
    </p:output>

    <p:option name="compatibility-mode" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to be backward compatible with <a
            href="http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm">Open Package Format
            2.0.1</a>.</p>
        </p:documentation>
    </p:option>
    <p:option name="reserved-prefixes" required="false" select="'#default'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The <a
            href="http://www.idpf.org/epub/301/spec/epub-publications.html#sec-metadata-default-vocab">reserved
            prefix mappings</a> of the resulting package document. By default, prefixes that are
            used but not declared in the input are also not declared in the output, and if "media"
            is not used in the input, it is declared in the output.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-filter
            px:fileset-load
        </p:documentation>
    </p:import>
    <p:import href="merge-metadata.xpl">
        <p:documentation>
            pxi:merge-metadata
        </p:documentation>
    </p:import>

    <p:documentation>Load package document</p:documentation>
    <px:fileset-filter media-types="application/oebps-package+xml" name="filter-package-doc">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load name="load-package-doc">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:assert message="There must be exactly one package document in the fileset"
               test-count-min="1" test-count-max="1" error-code="XXXXX"
               name="package-doc"/>
    <p:sink/>

    <p:documentation>Load SMIL files</p:documentation>
    <px:fileset-load media-types="application/smil+xml" name="smil">
        <p:input port="fileset">
            <p:pipe step="main" port="mo.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="mo.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:sink/>

    <p:documentation>Update manifest</p:documentation>
    <p:group name="update-manifest">
        <p:output port="result"/>
        <!--
            add SMIL and audio files to manifest and add media-overlay attributes to xhtml items
            this will overwrite any existing media-overlay attributes
        -->
        <p:xslt px:message="Assigning media overlays to their corresponding content documents..."
                px:message-severity="DEBUG">
            <p:input port="source">
                <p:pipe step="package-doc" port="result"/>
                <p:pipe step="main" port="mo.fileset"/>
                <p:pipe step="smil" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="assign-media-overlays.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:group>

    <p:documentation>Update metadata with duration information</p:documentation>
    <p:group name="update-metadata">
        <p:output port="result"/>
        <p:delete match="opf:meta[@property='media:duration']"/>
        <p:label-elements match="/*[@prefix]/opf:metadata" attribute="prefix" label="../@prefix"/>
        <p:viewport match="/*/opf:metadata">
            <p:identity name="metadata"/>
            <p:sink/>
            <p:group name="duration-metadata">
                <p:documentation>Extract "duration" metadata from media overlay documents.</p:documentation>
                <p:output port="result"/>
                <p:for-each name="metadata.durations">
                    <p:iteration-source>
                        <p:pipe step="smil" port="result"/>
                    </p:iteration-source>
                    <p:output port="result" sequence="true"/>
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
                        <p:with-option name="attribute-value"
                                       select="concat('#',/*/opf:manifest/opf:item[resolve-uri(@href,base-uri(.))=$base]/@id)">
                            <p:pipe step="update-manifest" port="result"/>
                        </p:with-option>
                    </p:add-attribute>
                </p:for-each>
                <p:sink/>
                <p:group name="metadata.total-duration">
                    <p:output port="result" sequence="true"/>
                    <p:count>
                        <p:input port="source">
                            <p:pipe step="metadata.durations" port="result"/>
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
                                    <p:pipe step="metadata.durations" port="result"/>
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
                        <p:inline>
                            <opf:metadata/>
                        </p:inline>
                    </p:input>
                    <p:input port="insertion">
                        <p:pipe step="metadata.durations" port="result"/>
                        <p:pipe step="metadata.total-duration" port="result"/>
                    </p:input>
                </p:insert>
                <p:choose>
                    <p:when test="$reserved-prefixes='#default'">
                        <p:add-attribute match="/*"
                                         attribute-name="prefix"
                                         attribute-value="media: http://www.idpf.org/epub/vocab/overlays/#"/>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
            </p:group>
            <p:sink/>
            <pxi:merge-metadata>
                <p:input port="source">
                    <p:pipe step="metadata" port="result"/>
                    <p:pipe step="duration-metadata" port="result"/>
                </p:input>
                <p:input port="manifest" select="/*/opf:manifest">
                    <p:pipe step="update-manifest" port="result"/>
                </p:input>
                <p:with-option name="reserved-prefixes" select="$reserved-prefixes"/>
            </pxi:merge-metadata>
            <p:choose>
                <p:when test="$compatibility-mode='true'">
                    <p:xslt>
                        <p:input port="stylesheet">
                            <p:document href="create-package-doc.backwards-compatible-metadata.xsl"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:viewport>
        <p:choose>
            <p:when test="/*/opf:metadata/@prefix">
                <p:add-attribute attribute-name="prefix" match="/*">
                    <p:with-option name="attribute-value" select="/*/opf:metadata/@prefix"/>
                </p:add-attribute>
                <p:delete match="/*/opf:metadata/@prefix"/>
            </p:when>
            <p:otherwise>
                <p:delete match="/*/@prefix"/>
            </p:otherwise>
        </p:choose>
    </p:group>
    <p:sink/>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="filter-package-doc" port="not-matched"/>
            <p:pipe step="load-package-doc" port="result.fileset"/>
            <p:pipe step="main" port="mo.fileset"/>
        </p:input>
    </px:fileset-join>

</p:declare-step>
