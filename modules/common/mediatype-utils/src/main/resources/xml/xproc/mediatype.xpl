<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    exclude-inline-prefixes="#all" version="1.0" type="px:mediatype-detect">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Media type detect</h1>
        <p px:role="desc">Determine the media type of a file.</p>
        <div px:role="author maintainer">
            <p px:role="name">Jostein Austvik Jacobsen</p>
            <a href="mailto:josteinaj@gmail.com" px:role="contact">josteinaj@gmail.com</a>
            <p px:role="organization">NLB</p>
        </div>
    </p:documentation>

    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Fileset</h2>
            <p px:role="desc">A DAISY Pipeline 2 fileset document as described on <a href="http://code.google.com/p/daisy-pipeline/wiki/FileSetDocument">http://code.google.com/p/daisy-pipeline/wiki/FileSetDocument</a>.</p>
        </p:documentation>
    </p:input>
    <p:input port="in-memory" sequence="true" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">In-memory documents</h2>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Result fileset</h2>
            <p px:role="desc">The same d:fileset that arrived on the input port, but with "media-type"-attributes added to all d:file elements.</p>
        </p:documentation>
    </p:output>

    <p:option name="load-if-not-in-memory" select="'false'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

    <p:declare-step type="pxi:mediatype-detect-from-extension">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:variable name="ext" select="lower-case(replace(/*/@href,'^.+?([^/\.]+)$','$1'))"/>
        <p:add-attribute match="/*" attribute-name="media-type">
            <p:with-option name="attribute-value" select="(//entry[@key=$ext]/@value, 'application/octet-stream')[1]">
                <p:document href="../maps/ext-to-mediatype.xml"/>
            </p:with-option>
        </p:add-attribute>
    </p:declare-step>
    
    <p:declare-step type="pxi:mediatype-detect-from-namespace" name="detect-from-namespace">
        <p:input port="source" primary="true"/>
        <p:input port="in-memory"/>
        <p:output port="result"/>
        <p:variable name="ns" select="namespace-uri(/*)">
            <p:pipe port="in-memory" step="detect-from-namespace"/>
        </p:variable>
         <p:add-attribute match="/*" attribute-name="media-type">
            <p:with-option name="attribute-value" select="(//entry[@key=$ns]/@value, 'application/xml')[1]">
                <p:document href="../maps/ns-to-mediatype.xml"/>
            </p:with-option>
         </p:add-attribute>
    </p:declare-step>

    <p:viewport match="//d:file" name="file">
        <p:choose>
            <p:when test="/*/@media-type">
                <!-- only try to find missing media types -->
                <p:identity/>
            </p:when>
            <p:otherwise>
                <!-- check if the file is in memory -->
                <p:group name="file-in-memory">
                    <p:output port="result"/>
                    <p:variable name="target" select="resolve-uri(/*/@href,base-uri(/*))"/>
                    <p:split-sequence>
                        <p:with-option name="test" select="concat('base-uri(/*)=&quot;',$target,'&quot;')"/>
                        <p:input port="source">
                            <p:pipe port="in-memory" step="main"/>
                        </p:input>
                    </p:split-sequence>
                    <p:split-sequence test="position()=1" initial-only="true"/>
                </p:group>
                <p:count name="filecount-in-memory"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="current" step="file"/>
                    </p:input>
                </p:identity>
                <p:choose>
                    <p:when test="number(.)&gt;0">
                        <!-- file is in memory -->
                        <p:xpath-context>
                            <p:pipe port="result" step="filecount-in-memory"/>
                        </p:xpath-context>
                        <pxi:mediatype-detect-from-namespace>
                            <p:input port="in-memory">
                                <p:pipe port="result" step="file-in-memory"/>
                            </p:input>
                        </pxi:mediatype-detect-from-namespace>
                    </p:when>
                    <p:otherwise>
                        <!-- file is not in memory -->
                        <pxi:mediatype-detect-from-extension name="from-extension"/>
                        <p:xslt>
                            <p:input port="parameters">
                                <p:empty/>
                            </p:input>
                            <p:input port="stylesheet">
                                <p:document href="../xslt/mediatype-functions.xsl"/>
                            </p:input>
                        </p:xslt>

                        <p:choose>
                            <p:when test="/*/@is-xml='true' and $load-if-not-in-memory='true'">
                                <!-- try to load from disk -->
                                <px:fileset-load method="xml" name="file-from-disk">
                                    <p:with-option name="load-if-not-in-memory" select="$load-if-not-in-memory"/>
                                    <p:with-option name="href" select="resolve-uri(/*/@href,base-uri(/*))"/>
                                    <p:input port="fileset">
                                        <p:pipe port="source" step="main"/>
                                    </p:input>
                                    <p:input port="in-memory">
                                        <p:pipe port="in-memory" step="main"/>
                                    </p:input>
                                </px:fileset-load>
                                <p:count name="file.load-count"/>
                                <p:identity>
                                    <p:input port="source">
                                        <p:pipe port="current" step="file"/>
                                    </p:input>
                                </p:identity>
                                <p:choose>
                                    <p:xpath-context>
                                        <p:pipe port="result" step="file.load-count"/>
                                    </p:xpath-context>
                                    <p:when test="number(/*)&gt;0">
                                        <pxi:mediatype-detect-from-namespace>
                                            <p:input port="in-memory">
                                                <p:pipe port="result" step="file-from-disk"/>
                                            </p:input>
                                        </pxi:mediatype-detect-from-namespace>
                                    </p:when>
                                    <p:otherwise>
                                        <!-- could not load xml from disk; use the file extension -->
                                        <p:identity>
                                            <p:input port="source">
                                                <p:pipe port="result" step="from-extension"/>
                                            </p:input>
                                        </p:identity>
                                    </p:otherwise>
                                </p:choose>
                            </p:when>
                            <p:otherwise>
                                <!-- not xml or not allowed to load from memory; use the file extension -->
                                <p:identity>
                                    <p:input port="source">
                                        <p:pipe port="result" step="from-extension"/>
                                    </p:input>
                                </p:identity>
                            </p:otherwise>
                        </p:choose>
                    </p:otherwise>
                </p:choose>
            </p:otherwise>

        </p:choose>

    </p:viewport>

</p:declare-step>
