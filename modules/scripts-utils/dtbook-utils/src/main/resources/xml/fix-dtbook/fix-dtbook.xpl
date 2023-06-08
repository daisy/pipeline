<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:dtbook-fix" name="main">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Fix DTBook XML</h1>
        <p px:role="desc">Apply fixing routines on a given dtbook xml.</p>
    </p:documentation>

    <!-- Import -->
    <p:import href="repair.xpl"/>
    <p:import href="tidy.xpl"/>
    <p:import href="narrator.xpl"/>

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
    <p:option name="repair" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair the dtbook</h2>
            <p px:role="desc">Apply repair routines on the dtbook</p>
        </p:documentation>
    </p:option>
    <p:option name="fixCharset" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair - Fix Charset</h2>
            <p px:role="desc">Fix the document charset (To be implemented)</p>
        </p:documentation>
    </p:option>
    <p:option name="tidy" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy up the dtbook</h2>
            <p px:role="desc">Apply tidying routines on the dtbook</p>
        </p:documentation>
    </p:option>
    <p:option name="simplifyHeadingLayout" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Simplify headings layout</h2>
            <p px:role="desc">TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="externalizeWhitespace" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Externalize whitespaces</h2>
            <p px:role="desc">TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="documentLanguage" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Document language</h2>
            <p px:role="desc">Set a document language</p>
        </p:documentation>
    </p:option>

    <p:option name="narrator" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Prepare dtbook for pipeline 1 narrator</h2>
            <p px:role="desc">Apply pipeline 1 "narrator" cleaning routines on the document</p>
        </p:documentation>
    </p:option>
    <p:option name="publisher" required="false" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Narrator - Publisher</h2>
            <p px:role="desc">Publisher to be added as dc:Publisher if none is defined in the dtbook</p>
        </p:documentation>
    </p:option>

    <!-- Execution -->
    <p:choose>
        <p:when test="$repair">
            <px:dtbook-repair>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
                <p:with-option name="fixCharset" select="$fixCharset" />
            </px:dtbook-repair>
        </p:when>
        <p:otherwise><p:identity /></p:otherwise>
    </p:choose>
    <p:choose>
        <p:when test="$tidy">
            <px:dtbook-tidy>
                <p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout" />
                <p:with-option name="externalizeWhitespace" select="$externalizeWhitespace" />
                <p:with-option name="documentLanguage" select="$documentLanguage" />
            </px:dtbook-tidy>
        </p:when>
        <p:otherwise><p:identity /></p:otherwise>
    </p:choose>
    <p:choose>
        <p:when test="$narrator">
            <px:dtbook-narrator>
                <p:with-option name="publisher" select="$publisher" />
            </px:dtbook-narrator>
        </p:when>
        <p:otherwise><p:identity /></p:otherwise>
    </p:choose>

</p:declare-step>