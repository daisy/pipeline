<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:dtbook-to-html.script" name="main"
                px:input-filesets="dtbook"
                px:output-filesets="html"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to HTML</h1>
        <p px:role="desc">Transforms DTBook XML into HTML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-html/">
            Online documentation
        </a>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be transformed. In the case of multiple
                files, a merge will be performed.</p>
        </p:documentation>
    </p:input>

    <p:option name="language" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Language code</h2>
            <p px:role="desc">Language code of the input document.</p>
        </p:documentation>
    </p:option>

    <p:option name="nimas" select="'false'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="validation" select="'abort'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:output port="validation-report" sequence="true">
        <!-- defined in ../../../../../common-options.xpl -->
        <p:pipe step="load" port="validation-report"/>
    </p:output>

    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <!-- whether the conversion was aborted due to validation errors -->
        <p:pipe step="result" port="status"/>
    </p:output>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">The resulting HTML document.</p>
        </p:documentation>
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>

    <p:option xmlns:_="dtbook" name="_:chunk-size" select="'-1'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="convert.xpl">
        <p:documentation>
            px:dtbook-to-html
        </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
        <p:documentation>
            pf:normalize-uri
        </p:documentation>
    </cx:import>

    <px:dtbook-load name="load" px:progress=".1" px:message="Loading DTBook">
        <p:with-option name="validation" select="not($validation='off')"/>
        <p:with-option name="nimas" select="$nimas='true'"/>
        <!-- assume MathML 3.0 -->
    </px:dtbook-load>
    <p:sink/>

    <p:identity>
        <p:input port="source">
            <p:pipe step="load" port="validation-status"/>
        </p:input>
    </p:identity>
    <p:choose>
        <p:when test="/d:validation-status[@result='error']">
            <p:choose>
                <p:when test="$validation='abort'">
                    <p:identity px:message="The input contains an invalid DTBook file. See validation report for more info."
                                px:message-severity="ERROR"/>
                </p:when>
                <p:otherwise>
                    <p:identity px:message="The input contains an invalid DTBook file. See validation report for more info."
                                px:message-severity="WARN"/>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:choose name="result" px:progress=".9">
        <p:when test="/d:validation-status[@result='error'] and $validation='abort'">
            <p:output port="status"/>
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:output port="status"/>

            <p:variable name="dtbook-is-valid" cx:as="xs:boolean"
                        select="not($validation='off') and exists(/d:validation-status[@result='ok'])"/>
            <!-- get the HTML filename from the first DTBook -->
            <p:sink/>
            <p:split-sequence test="position()=1" initial-only="true">
                <p:input port="source">
                    <p:pipe step="main" port="source"/>
                </p:input>
            </p:split-sequence>
            <p:group px:progress="1">
            <!--<p:variable name="encoded-title" select="encode-for-uri(replace(//dtbook:meta[@name='dc:Title']/@content,'[/\\?%*:|&quot;&lt;&gt;]',''))"/>-->
            <!--<p:variable name="encoded-title" select="'book'"/>-->
                <p:variable name="encoded-title"
                            select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')"/>
                <p:variable name="output-dir-uri" select="pf:normalize-uri(concat($result,'/'))"/>
                <p:variable name="html-file-uri" select="concat($output-dir-uri,$encoded-title,'.epub')"/>
                <p:sink/>

                <px:dtbook-to-html name="convert" px:progress="8/9">
                    <p:input port="source.fileset">
                        <p:pipe step="load" port="result.fileset"/>
                    </p:input>
                    <p:input port="source.in-memory">
                        <p:pipe step="load" port="result.in-memory"/>
                    </p:input>
                    <p:with-option name="language" select="$language"/>
                    <p:with-option name="validation" select="$validation"/>
                    <p:with-option name="dtbook-is-valid" select="$dtbook-is-valid"/>
                    <p:with-option name="nimas" select="$nimas='true'"/>
                    <p:with-option name="chunk-size" xmlns:_="dtbook" select="$_:chunk-size"/>
                    <p:with-option name="output-dir" select="$output-dir-uri"/>
                    <p:with-option name="temp-dir" select="pf:normalize-uri(concat($temp-dir,'/'))"/>
                    <p:with-option name="filename" select="$encoded-title"/>
                </px:dtbook-to-html>

                <px:fileset-store name="store" px:progress="1/9" px:message="Storing HTML">
                    <p:input port="in-memory.in">
                        <p:pipe step="convert" port="result.in-memory"/>
                    </p:input>
                </px:fileset-store>

                <p:identity cx:depends-on="store">
                    <p:input port="source">
                        <p:inline><d:validation-status result="ok"/></p:inline>
                    </p:input>
                </p:identity>
            </p:group>
        </p:otherwise>
    </p:choose>

</p:declare-step>
