<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pxi:dtbook-fix" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Fix DTBook XML</h1>
        <p>Apply fixing routines on a given dtbook xml.</p>
    </p:documentation>

    <!-- I/O -->
    <p:input port="source" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A single DTBook document</p>
        </p:documentation>
    </p:input>
    <p:output port="result" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result DTBook document</p>
        </p:documentation>
    </p:output>

    <!-- Options -->
    <p:option name="repair" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Repair the dtbook</h2>
            <p>Apply repair routines on the dtbook</p>
        </p:documentation>
    </p:option>
    <p:option name="fixCharset" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Repair - Fix Charset</h2>
            <p>Fix the document charset (To be implemented)</p>
        </p:documentation>
    </p:option>
    <p:option name="tidy" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Tidy up the dtbook</h2>
            <p>Apply tidying routines on the dtbook</p>
        </p:documentation>
    </p:option>
    <p:option name="simplifyHeadingLayout" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Tidy - Simplify headings layout</h2>
            <p>TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="externalizeWhitespace" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Tidy - Externalize whitespaces</h2>
            <p>TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="documentLanguage" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Tidy - Document language</h2>
            <p>Set a document language</p>
        </p:documentation>
    </p:option>
    <p:option name="narrator" select="false()" cx:as="xs:boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Prepare dtbook for pipeline 1 narrator</h2>
            <p>Apply pipeline 1 "narrator" cleaning routines on the document</p>
        </p:documentation>
    </p:option>
    <p:option name="publisher" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Narrator - Publisher</h2>
            <p>Publisher to be added as dc:Publisher if none is defined in the dtbook</p>
        </p:documentation>
    </p:option>

    <!-- Import -->
    <p:import href="repair.xpl"/>
    <p:import href="tidy.xpl"/>
    <p:import href="narrator.xpl"/>

    <!-- Execution -->
    <p:choose>
        <p:when test="$repair">
            <pxi:dtbook-repair>
                <p:input port="source">
                    <p:pipe step="main" port="source"/>
                </p:input>
                <p:with-option name="fixCharset" select="$fixCharset"/>
            </pxi:dtbook-repair>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:choose>
        <p:when test="$tidy">
            <pxi:dtbook-tidy>
                <p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout"/>
                <p:with-option name="externalizeWhitespace" select="$externalizeWhitespace"/>
                <p:with-option name="documentLanguage" select="$documentLanguage"/>
            </pxi:dtbook-tidy>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:choose>
        <p:when test="$narrator">
            <pxi:dtbook-narrator>
                <p:with-option name="publisher" select="$publisher"/>
            </pxi:dtbook-narrator>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
