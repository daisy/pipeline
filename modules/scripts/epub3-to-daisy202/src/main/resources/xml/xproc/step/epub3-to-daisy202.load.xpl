<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-daisy202.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:output port="result.fileset" primary="true">
        <p:pipe step="fileset" port="result"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="load" port="result.in-memory"/>
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
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl">
        <p:documentation>
            px:epub3-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-validator/library.xpl">
        <p:documentation>
            px:epub3-validator
        </p:documentation>
    </p:import>
    
    <!--
        setting store-to-disk="true" is currently more memory efficient
    -->
    <px:epub3-load name="load" store-to-disk="true">
        <p:with-option name="href" select="$epub"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:epub3-load>
    
    <p:identity name="fileset"/>
    
    <p:choose name="validate">
        <p:when test="$validation='off'">
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
            <p:output port="report" sequence="true">
                <p:pipe step="status-and-report" port="report"/>
            </p:output>
            <p:output port="status">
                <p:pipe step="status-and-report" port="status"/>
            </p:output>
            <px:epub3-validator name="epub3-validator">
                <!--
                    epub option must point to a file that exists on disk (and may not be a file inside a ZIP)
                -->
                <p:with-option name="epub" select="$epub">
                    <p:pipe step="load" port="result.fileset"/>
                </p:with-option>
                <p:with-option name="temp-dir" select="concat($temp-dir,'validate/')"/>
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
