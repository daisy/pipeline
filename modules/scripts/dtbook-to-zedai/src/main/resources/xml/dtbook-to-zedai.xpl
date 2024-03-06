<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:dtbook-to-zedai.script"
                name="main"
                px:input-filesets="dtbook"
                px:output-filesets="zedai"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to ZedAI</h1>
        <p px:role="desc">Transforms DTBook XML into ZedAI XML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-zedai/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Marisa DeMeglio</dd>
                <dt>E-mail:</dt>
                <dd><a href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.</p>
        </p:documentation>
    </p:input>

    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ZedAI</h2>
            <p px:role="desc">The resulting ZedAI XML file.</p>
        </p:documentation>
    </p:option>
    <p:option name="zedai-filename" required="false" px:type="string" select="'zedai.xml'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">ZedAI filename</h2>
            <p px:role="desc">Filename for the generated ZedAI file</p>
        </p:documentation>
    </p:option>
    <p:option name="validation" select="'abort'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>
    <p:option name="nimas" select="'false'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>
    <p:output port="validation-report" sequence="true">
        <!-- defined in ../../../../../common-options.xpl -->
        <p:pipe step="load" port="validation-report"/>
        <p:pipe step="result" port="validation-report"/>
    </p:output>
    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <!-- whether the input and/or output has validation errors -->
        <p:pipe step="result" port="status"/>
    </p:output>
    <p:option name="mods-filename" required="false" px:type="string" select="'zedai-mods.xml'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">MODS filename</h2>
            <p px:role="desc">Filename for the generated MODS file</p>
        </p:documentation>
    </p:option>
    <p:option name="css-filename" required="false" px:type="string" select="'zedai-css.css'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">CSS filename</h2>
            <p px:role="desc">Filename for the generated CSS file</p>
        </p:documentation>
    </p:option>
    <p:option name="language" select="''">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>
    <p:option name="copy-external-resources" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Copy external resources</h2>
            <p px:role="desc">Include any referenced external resources like images and CSS-files to the output.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entries
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="dtbook-to-zedai.convert.xpl">
        <p:documentation>
            px:dtbook-to-zedai
        </p:documentation>
    </p:import>

    <p:sink/>
    <px:fileset-add-entries media-type="application/x-dtbook+xml" name="dtbook">
        <p:input port="entries">
            <p:pipe step="main" port="source"/>
        </p:input>
    </px:fileset-add-entries>
    <px:dtbook-load name="load">
        <p:input port="source.in-memory">
            <p:pipe step="dtbook" port="result.in-memory"/>
        </p:input>
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
    <p:choose name="result">
        <p:when test="/d:validation-status[@result='error'] and $validation='abort'">
            <p:output port="status" primary="true"/>
            <p:output port="validation-report" sequence="true">
                <p:empty/>
            </p:output>
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:output port="status" primary="true"/>
            <p:output port="validation-report" sequence="true">
                <p:pipe step="convert" port="validation-report"/>
            </p:output>
            <p:variable name="dtbook-is-valid" cx:as="xs:boolean"
                        select="not($validation='off') and exists(/d:validation-status[@result='ok'])"/>
            <p:sink/>

            <px:dtbook-to-zedai name="convert">
                <p:input port="source.fileset">
                    <p:pipe step="load" port="result.fileset"/>
                </p:input>
                <p:input port="source.in-memory">
                    <p:pipe step="load" port="result.in-memory"/>
                </p:input>
                <p:with-option name="output-dir" select="$result"/>
                <p:with-option name="zedai-filename" select="$zedai-filename"/>
                <p:with-option name="mods-filename" select="$mods-filename"/>
                <p:with-option name="css-filename" select="$css-filename"/>
                <p:with-option name="lang" select="$language"/>
                <p:with-option name="validation" select="$validation"/>
                <p:with-option name="dtbook-is-valid" select="$dtbook-is-valid"/>
                <p:with-option name="output-validation" select="($validation[.='off'],'report')[1]"/>
                <p:with-option name="nimas" select="$nimas='true'"/>
                <p:with-option name="copy-external-resources" select="$copy-external-resources='true'"/>
            </px:dtbook-to-zedai>

            <px:fileset-store name="store">
                <p:input port="in-memory.in">
                    <p:pipe step="convert" port="result.in-memory"/>
                </p:input>
            </px:fileset-store>

            <p:identity cx:depends-on="store">
                <p:input port="source">
                    <p:pipe step="convert" port="validation-status"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

</p:declare-step>
