<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:html-to-dtbook" name="main">

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>

    <p:output port="result.fileset" primary="true">
        <p:pipe step="html-to-dtbook" port="fileset"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="html-to-dtbook" port="in-memory"/>
    </p:output>

    <p:option name="dtbook-file-name" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Defaults to the name of the HTML file with file extension ".xml"</p>
        </p:documentation>
    </p:option>
    <p:option name="imply-headings" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to generate headings for untitled levels.</p>
        </p:documentation>
    </p:option>
    <p:option name="dtbook-css" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI of CSS style sheet to apply to the DTBook.</p>
            <p>If left empty, a default style sheet is used.</p>
        </p:documentation>
    </p:option>

    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p><code>d:fileset</code> document containing the mapping from input to output file
            name.</p>
        </p:documentation>
        <p:pipe step="mapping" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-filter
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-upgrade
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="extract-svg.xpl">
        <p:documentation>
            pxi:html-extract-svg
        </p:documentation>
    </p:import>
    <p:import href="epub3-html-to-dtbook.xpl">
        <p:documentation>
            pxi:epub3-html-to-dtbook
        </p:documentation>
    </p:import>
    <p:import href="daisy202-html-to-dtbook.xpl">
        <p:documentation>
            pxi:daisy202-html-to-dtbook
        </p:documentation>
    </p:import>

    <!--
        Extract SVG images into their own files and link to with img element
    -->
    <pxi:html-extract-svg name="extract-svg" px:progress="1/10">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </pxi:html-extract-svg>

    <!--
        Load HTML
    -->
    <px:fileset-filter media-types="application/xhtml+xml" name="filter-html" px:progress="1/20">
        <p:input port="source.in-memory">
            <p:pipe step="extract-svg" port="result.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="extract-svg" port="result.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one HTML file in the fileset." error-code="XXXXX"/>
    <px:assert message="The HTML file must have a file extension." error-code="XXXXX">
        <p:with-option name="test" select="$dtbook-file-name!='' or matches(base-uri(/*),'.*[^\.]\.[^\.]*$')"/>
    </px:assert>
    <p:identity name="html"/>
    <p:sink/>

    <!--
        If HTML is not coming from EPUB 3 or DAISY 2.02, upgrade to HTML 5.0 and treat it as if it
        was coming from an EPUB 3.
    -->
    <p:identity>
        <p:input port="source">
            <p:pipe step="filter-html" port="result"/>
        </p:input>
    </p:identity>
    <p:choose name="maybe-upgrade-html">
        <p:when test="not(/*/d:file/@media-version=('4.0','5.0'))">
            <p:output port="fileset" primary="true">
                <p:pipe step="html5-fileset" port="result"/>
            </p:output>
            <p:output port="in-memory">
                <p:pipe step="html5" port="result"/>
            </p:output>
            <p:add-attribute match="/d:file" attribute-name="media-version" attribute-value="5.0"
                             name="html5-fileset"/>
            <p:sink/>
            <px:html-upgrade name="html5">
                <p:input port="source">
                    <p:pipe step="html" port="result"/>
                </p:input>
            </px:html-upgrade>
            <p:sink/>
        </p:when>
        <p:otherwise>
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory">
                <p:pipe step="html" port="result"/>
            </p:output>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <!--
        Convert HTML to DTBook
    -->
    <p:choose px:progress="8/10" name="html-to-dtbook">
        <p:when test="/*/d:file/@media-version='5.0'">
            <!--
                Assume the HTML is coming from an EPUB 3
            -->
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="convert" port="result.in-memory"/>
            </p:output>
            <pxi:epub3-html-to-dtbook name="convert">
                <p:with-option name="imply-headings" select="$imply-headings"/>
                <p:with-option name="dtbook-file-name"
                               select="($dtbook-file-name[.!=''],
                                        concat(replace(base-uri(/*),'^(.*)\.[^/\.]*$','$1'),'.xml'))[1]">
                    <p:pipe step="html" port="result"/>
                </p:with-option>
                <p:input port="html.in-memory">
                    <p:pipe step="maybe-upgrade-html" port="in-memory"/>
                </p:input>
                <p:input port="resources.fileset">
                    <p:pipe step="filter-html" port="not-matched"/>
                </p:input>
                <p:input port="resources.in-memory">
                    <p:pipe step="filter-html" port="not-matched.in-memory"/>
                </p:input>
            </pxi:epub3-html-to-dtbook>
        </p:when>
        <p:otherwise> <!-- 4.0 -->
            <!--
                Assume the HTML is coming from a DAISY 2.02
            -->
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="convert" port="result.in-memory"/>
            </p:output>
            <pxi:daisy202-html-to-dtbook name="convert">
                <p:with-option name="dtbook-file-name"
                               select="($dtbook-file-name[.!=''],
                                        concat(replace(base-uri(/*),'^(.*)\.[^/\.]*$','$1'),'.xml'))[1]">
                    <p:pipe step="html" port="result"/>
                </p:with-option>
                <p:with-option name="dtbook-css" select="($dtbook-css[.!=''],
                                                          resolve-uri('../../css/dtbook.2005.basic.css',base-uri(/)))[1]">
                    <p:inline><irrelevant/></p:inline>
                </p:with-option>
                <p:input port="html.in-memory">
                    <p:pipe step="maybe-upgrade-html" port="in-memory"/>
                </p:input>
                <p:input port="resources.fileset">
                    <p:pipe step="filter-html" port="not-matched"/>
                </p:input>
                <p:input port="resources.in-memory">
                    <p:pipe step="filter-html" port="not-matched.in-memory"/>
                </p:input>
            </pxi:daisy202-html-to-dtbook>
        </p:otherwise>
    </p:choose>

    <!--
        Mapping from input to output file name
    -->
    <p:template name="mapping">
        <p:input port="template">
            <p:inline>
                <d:fileset>
                    <d:file href="{/*/d:file[@media-type='application/x-dtbook+xml']/resolve-uri(@href,base-uri(.))}"
                            original-href="{$html-base-uri}"/>
                </d:fileset>
            </p:inline>
        </p:input>
        <p:with-param name="html-base-uri" select="base-uri(/*)">
            <p:pipe step="html" port="result"/>
        </p:with-param>
    </p:template>
    <p:sink/>

</p:declare-step>
