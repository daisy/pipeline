<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-daisy202.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                name="main">
    
    <!--
        This step should be moved to pipeline-scripts-utils (as px:epub3-load)
    -->
    
    <p:output port="fileset.out" primary="true">
        <p:pipe step="validate" port="fileset.out"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="validate" port="in-memory.out"/>
    </p:output>
    <p:output port="validation-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
        <p:pipe step="validate" port="report"/>
    </p:output>
    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <p:pipe step="validate" port="status"/>
    </p:output>
    
    <p:option name="epub" required="true" px:media-type="application/epub+zip application/oebps-package+xml"/>
    <p:option name="temp-dir" required="true"/>
    <p:option name="validation" required="true"/> <!-- off | report | abort -->
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-validator/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <p:choose name="load">
        <p:when test="ends-with(lower-case($epub),'.epub')">
            <p:output port="fileset.out" primary="true">
                <p:pipe step="mediatype" port="result"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe step="load.fileset" port="result"/>
            </p:output>
            <px:fileset-unzip store-to-disk="true" name="unzip">
                <p:with-option name="href" select="$epub"/>
                <p:with-option name="unzipped-basedir" select="concat($temp-dir,'epub/')"/>
            </px:fileset-unzip>
            <p:sink/>
            <px:mediatype-detect name="mediatype">
                <p:input port="source">
                    <p:pipe step="unzip" port="fileset"/>
                </p:input>
            </px:mediatype-detect>
            <px:fileset-load name="load.fileset">
                <p:input port="in-memory">
                    <p:empty/>
                </p:input>
            </px:fileset-load>
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true">
                <p:pipe port="result" step="load.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="opf"/>
            </p:output>
            <px:fileset-create>
                <p:with-option name="base" select="replace($epub,'(.*/)([^/]*)','$1')"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/oebps-package+xml">
                <p:with-option name="href" select="replace($epub,'(.*/)([^/]*)','$2')"/>
                <p:with-option name="original-href" select="$epub"/>
            </px:fileset-add-entry>
            <px:mediatype-detect/>
            <px:fileset-load>
                <p:input port="in-memory">
                    <p:empty/>
                </p:input>
            </px:fileset-load>
            <p:identity name="opf"/>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../../xslt/opf-manifest-to-fileset.xsl"/>
                </p:input>
            </p:xslt>
            <!--
                FIXME: px:fileset-move should take care of this, but doesn't seem to work
            -->
            <p:label-elements match="d:file" attribute="original-href" label="@href"/>
            <p:identity name="load.fileset"/>
        </p:otherwise>
    </p:choose>
    
    <p:choose name="validate">
        <p:when test="$validation='off'">
            <p:output port="fileset.out">
                <p:pipe step="load" port="fileset.out"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe step="load" port="in-memory.out"/>
            </p:output>
            <p:output port="report" sequence="true">
                <p:empty/>
            </p:output>
            <p:output port="status">
                <p:inline>
                    <d:validation-status result="ok"/>
                </p:inline>
            </p:output>
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out">
                <p:pipe step="load" port="fileset.out"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe step="load" port="in-memory.out"/>
            </p:output>
            <p:output port="report" sequence="true">
                <p:pipe step="status-and-report" port="report"/>
            </p:output>
            <p:output port="status">
                <p:pipe step="status-and-report" port="status"/>
            </p:output>
            <px:epub3-validator name="epub3-validator">
                <p:with-option name="epub" select="/d:fileset/d:file[@media-type='application/oebps-package+xml']
                                                   /resolve-uri((@original-href,@href)[1], base-uri(.))">
                    <p:pipe step="load" port="fileset.out"/>
                </p:with-option>
            </px:epub3-validator>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="epub3-validator" port="validation-status"/>
                </p:input>
            </p:identity>
            <p:choose name="status-and-report">
                <p:when test="/d:validation-status[@result='ok']">
                    <p:output port="status" primary="true"/>
                    <p:output port="report" sequence="true">
                        <p:empty/>
                    </p:output>
                    <p:identity/>
                </p:when>
                <p:when test="$validation='report'">
                    <p:output port="status" primary="true">
                        <!--
                            Return OK here even though validation failed. This is because
                            "VALIDATION_FAIL" is going to be generalized to "FAIL"
                            (https://github.com/daisy/pipeline-framework/issues/121).
                        -->
                        <p:inline>
                            <d:validation-status result="ok"/>
                        </p:inline>
                    </p:output>
                    <p:output port="report">
                        <p:pipe step="epub3-validator" port="html-report"/>
                    </p:output>
                    <p:sink>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:sink>
                </p:when>
                <p:otherwise>
                    <p:output port="status" primary="true"/>
                    <p:output port="report" sequence="true">
                        <p:pipe step="epub3-validator" port="html-report"/>
                    </p:output>
                    <px:message message="The EPUB 3 input is invalid. Aborting."/>
                </p:otherwise>
            </p:choose>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
