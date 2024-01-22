<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
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
        <p:pipe step="update-metadata" port="result.in-memory"/>
    </p:output>

    <p:option name="compatibility-mode" required="false" select="'true'" cx:type="xs:boolean" cx:as="xs:string">
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
            is not used in the input, the "declare-media-prefix" setting determines whether it is
            declared in the output.</p>
        </p:documentation>
    </p:option>
    <p:option name="declare-media-prefix" required="false" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Determines whether the "media" prefix is declared in the output when it is not used
            in the input.</p>
            <p>Has no effect if the "reserved-prefixes" option is set.</p>
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
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="add-metadata.xpl">
        <p:documentation>
            px:epub3-add-metadata
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
    <p:sink/>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="filter-package-doc" port="not-matched"/>
            <p:pipe step="load-package-doc" port="result.fileset"/>
            <p:pipe step="main" port="mo.fileset"/>
        </p:input>
    </px:fileset-join>

    <p:documentation>Update metadata with duration information and accessibility metadata</p:documentation>
    <p:group name="update-metadata">
        <p:output port="result.fileset" primary="true">
            <p:pipe step="add-metadata" port="result.fileset"/>
        </p:output>
        <p:output port="result.in-memory" sequence="true">
            <p:pipe step="add-metadata" port="result.in-memory"/>
        </p:output>
        <px:epub3-add-metadata name="add-metadata">
            <p:input port="source.in-memory">
                <p:pipe step="update-manifest" port="result"/>
                <p:pipe step="filter-package-doc" port="not-matched.in-memory"/>
                <p:pipe step="main" port="mo.in-memory"/>
            </p:input>
            <p:input port="metadata">
                <p:pipe step="duration-metadata" port="result"/>
                <p:pipe step="accessibility-metadata" port="result"/>
            </p:input>
            <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
            <p:with-option name="reserved-prefixes" select="$reserved-prefixes"/>
        </px:epub3-add-metadata>
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
            <p:insert match="/*/*" position="last-child">
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all" xmlns="http://www.idpf.org/2007/opf">
                        <package>
                            <metadata/>
                        </package>
                    </p:inline>
                </p:input>
                <p:input port="insertion">
                    <p:pipe step="metadata.durations" port="result"/>
                    <p:pipe step="metadata.total-duration" port="result"/>
                </p:input>
            </p:insert>
            <p:insert match="/*" position="last-child">
                <p:input port="insertion" select="//opf:manifest">
                    <p:pipe step="update-manifest" port="result"/>
                </p:input>
            </p:insert>
            <p:choose>
                <p:when test="$reserved-prefixes='#default' and $declare-media-prefix='true'">
                    <p:add-attribute match="/*"
                                     attribute-name="prefix"
                                     attribute-value="media: http://www.idpf.org/epub/vocab/overlays/#">
                        <!--
                            note that if there was already "media" metadata present in the
                            input, and the "media" prefix was not declared, px:epub3-add-metadata
                            will make sure that the prefix will not be declared in the output
                            either
                        -->
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:group>
        <p:group name="accessibility-metadata">
            <p:output port="result" sequence="true"/>
            <p:choose>
                <p:when test="empty(//opf:metadata/*)"> <!-- no duration metadata means no media-overlays added -->
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:otherwise>
                    <p:sink/>
                    <p:choose>
                        <p:xpath-context>
                            <p:pipe step="package-doc" port="result"/>
                        </p:xpath-context>
                        <p:when test="//opf:metadata/opf:meta[@property='schema:accessMode'][not(@refines)][string(.)='auditory'] and
                                      //opf:metadata/opf:meta[@property='schema:accessibilityFeature'][not(@refines)][string(.)='synchronizedAudioText']">
                            <p:identity>
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:identity>
                        </p:when>
                        <p:otherwise>
                            <!--
                                combine <meta property="schema:accessMode">auditory</meta> and <meta
                                property="schema:accessibilityFeature">synchronizedAudioText</meta> with
                                existing schema:accessMode and schema:accessibilityFeature metadata (if we
                                would simply include the new element, it would overwrite the existing
                                metadata)
                            -->
                            <p:delete match="opf:metadata/*[
                                               not(self::opf:meta[(@property='schema:accessMode' and not(string(.)='auditory')) or
                                                                  (@property='schema:accessibilityFeature' and not(string(.)='synchronizedAudioText'))]
                                                                 [not(@refines)])]">
                                <p:input port="source" select="//opf:metadata">
                                    <p:pipe step="package-doc" port="result"/>
                                </p:input>
                            </p:delete>
                            <p:insert match="/opf:metadata" position="last-child">
                                <p:input port="insertion">
                                    <p:inline exclude-inline-prefixes="#all" xmlns="http://www.idpf.org/2007/opf">
                                        <meta property="schema:accessMode">auditory</meta>
                                    </p:inline>
                                </p:input>
                            </p:insert>
                            <p:insert position="last-child">
                                <p:input port="insertion">
                                    <p:inline exclude-inline-prefixes="#all" xmlns="http://www.idpf.org/2007/opf">
                                        <meta property="schema:accessibilityFeature">synchronizedAudioText</meta>
                                    </p:inline>
                                </p:input>
                            </p:insert>
                            <p:choose>
                                <p:when test="$reserved-prefixes='#default'">
                                    <p:add-attribute match="/*"
                                                     attribute-name="prefix"
                                                     attribute-value="schema: http://schema.org/">
                                        <!--
                                            note that if there was already "schema" metadata present in the
                                            input, and the "schema" prefix was not declared,
                                            px:epub3-add-metadata will make sure that the prefix will not be
                                            declared in the output either
                                        -->
                                    </p:add-attribute>
                                </p:when>
                                <p:otherwise>
                                    <p:identity/>
                                </p:otherwise>
                            </p:choose>
                        </p:otherwise>
                    </p:choose>
                </p:otherwise>
            </p:choose>
        </p:group>
        <p:sink/>
    </p:group>

</p:declare-step>
