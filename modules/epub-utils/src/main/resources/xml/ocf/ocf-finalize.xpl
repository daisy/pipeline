<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc-internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:err="http://www.w3.org/ns/xproc-error"
                type="px:epub3-ocf-finalize" name="main">
    
    <p:input port="source" primary="true"/>
    <p:input port="metadata" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="rights" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="signature" sequence="true">
        <p:empty/>
    </p:input>
    <p:option name="create-odf-manifest" select="'false'"  cx:as="xs:string"/>
    <p:option name="epub-dir"/>
    <p:output port="result" primary="true">
        <p:pipe port="result" step="fileset-finalized"/>
    </p:output>
    <p:output port="container">
        <p:pipe port="result" step="create-container-descriptor"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="create-container-descriptor"/>
        <p:pipe port="result" step="create-odf-manifest"/>
        <p:pipe port="metadata" step="main"/>
        <p:pipe port="rights" step="main"/>
        <p:pipe port="signature" step="main"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/odf-utils/library.xpl">
        <p:documentation>
            px:odf-manifest-from-fileset
        </p:documentation>
    </p:import>
    
    <p:declare-step type="pxi:fileset-add-ocf-entry" name="store-in-ocf">
        <p:input port="fileset" primary="true"/>
        <p:input port="source" sequence="true"/>
        <p:option name="path" required="true"/>
        <p:option name="media-type"/>
        <p:output port="result"/>

        <p:wrap-sequence wrapper="wrapper">
            <p:input port="source">
                <p:pipe port="source" step="store-in-ocf"/>
            </p:input>
        </p:wrap-sequence>
        <p:choose>
            <p:when test="count(wrapper/*)=0">
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="fileset" step="store-in-ocf"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:when test="count(wrapper/*)>1">
                <p:error code="err:EOU001">
                    <p:input port="source">
                        <p:inline>
                            <c:message>It is a dynamic error if more than one document appears on the source port.</c:message>
                        </p:inline>
                    </p:input>
                </p:error>
            </p:when>
            <p:otherwise>
                <!-- Add to fileset -->
                <p:unwrap match="/wrapper"/>
                <p:choose>
                    <p:when test="p:value-available('media-type')">
                        <px:fileset-add-entry>
                            <p:with-option name="href" select="$path"/>
                            <p:with-option name="media-type" select="$media-type"/>
                            <p:input port="source.fileset">
                                <p:pipe port="fileset" step="store-in-ocf"/>
                            </p:input>
                        </px:fileset-add-entry>
                    </p:when>
                    <p:otherwise>
                        <px:fileset-add-entry>
                            <p:with-option name="href" select="$path"/>
                            <p:input port="source.fileset">
                                <p:pipe port="fileset" step="store-in-ocf"/>
                            </p:input>
                        </px:fileset-add-entry>
                    </p:otherwise>
                </p:choose>
            </p:otherwise>
        </p:choose>
    </p:declare-step>
    
    <p:wrap-sequence name="opf-files" wrapper="wrapper">
        <p:input port="source" select="//*[@media-type='application/oebps-package+xml' or ends-with(@href,'.opf')]">
            <p:pipe port="source" step="main"/>
        </p:input>
    </p:wrap-sequence>
    <p:sink/>

    <p:group name="create-container-descriptor">
        <p:output port="result"/>
        <p:xslt>
            <p:with-param name="result-base" select="/*/@result-base">
                <p:pipe port="result" step="result-base"/>
            </p:with-param>
            <p:input port="source">
                <p:pipe port="result" step="opf-files"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ocf-finalize.create-container-descriptor.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="resolve-uri('META-INF/container.xml',/*/@result-base)">
                <p:pipe port="result" step="result-base"/>
            </p:with-option>
        </px:set-base-uri>
    </p:group>
    <p:sink/>

    <p:group name="create-odf-manifest" xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0">
        <p:output port="result" sequence="true"/>
        <p:identity>
            <p:input port="source">
                <p:pipe port="result" step="before-odf"/>
            </p:input>
        </p:identity>
        <p:choose>
            <p:when test="$create-odf-manifest = 'true'">
                <px:fileset-add-entry media-type="application/epub+zip" href="."/>
                <px:odf-manifest-from-fileset/>
                <px:set-base-uri>
                    <p:with-option name="base-uri" select="resolve-uri('META-INF/manifest.xml',/*/@result-base)">
                        <p:pipe port="result" step="result-base"/>
                    </p:with-option>
                </px:set-base-uri>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:filter select="/manifest:manifest"/>
    </p:group>

    <p:choose name="check-fileset">
        <p:xpath-context>
            <p:pipe port="result" step="opf-files"/>
        </p:xpath-context>
        <p:when test="not(wrapper/*)">
            <p:error code="err:EOU002">
                <p:input port="source">
                    <p:inline>
                        <c:message>No OPF was found in the source file set.</c:message>
                    </p:inline>
                </p:input>
            </p:error>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <pxi:fileset-add-ocf-entry path="META-INF/container.xml" media-type="application/xml">
        <p:input port="source">
            <p:pipe port="result" step="create-container-descriptor"/>
        </p:input>
    </pxi:fileset-add-ocf-entry>
    <pxi:fileset-add-ocf-entry path="META-INF/metadata.xml" media-type="application/xml">
        <p:input port="source">
            <p:pipe port="metadata" step="main"/>
        </p:input>
    </pxi:fileset-add-ocf-entry>
    <pxi:fileset-add-ocf-entry path="META-INF/rights.xml" media-type="application/xml">
        <p:input port="source">
            <p:pipe port="rights" step="main"/>
        </p:input>
    </pxi:fileset-add-ocf-entry>
    <pxi:fileset-add-ocf-entry path="META-INF/signature.xml" media-type="application/xml">
        <p:input port="source">
            <p:pipe port="signature" step="main"/>
        </p:input>
    </pxi:fileset-add-ocf-entry>
    <p:identity name="before-odf"/>
    <!-- Finally -->
    <pxi:fileset-add-ocf-entry path="META-INF/manifest.xml" media-type="application/xml">
        <p:input port="source">
            <p:pipe port="result" step="create-odf-manifest"/>
        </p:input>
    </pxi:fileset-add-ocf-entry>
    <p:identity name="fileset-finalized"/>
    
    <p:identity>
        <p:input port="source">
            <p:inline>
                <result-base/>
            </p:inline>
        </p:input>
    </p:identity>
    <p:choose>
        <p:when test="p:value-available('epub-dir')">
            <p:add-attribute match="/*" attribute-name="result-base">
                <p:with-option name="attribute-value" select="$epub-dir"/>
            </p:add-attribute>
        </p:when>
        <p:otherwise>
            <p:add-attribute match="/*" attribute-name="result-base">
                <p:with-option name="attribute-value" select="base-uri(/*)">
                    <p:pipe port="source" step="main"/>
                </p:with-option>
            </p:add-attribute>
        </p:otherwise>
    </p:choose>
    <p:identity name="result-base"/>
    <p:sink/>

</p:declare-step>
