<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:dtbook-cleaner.script"
                px:input-filesets="dtbook"
                px:output-filesets="dtbook"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Cleaner</h1>
        <p px:role="desc">Apply cleanup routines and optionally tag sentences on a given DTBbook</p>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Nicolas Pavie</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:pavie.nicolas@gmail.com">pavie.nicolas@gmail.com</a></dd>
                <dt>Organisation:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be cleaned</p>
        </p:documentation>
    </p:input>
    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">Cleaned DTBooks</p>
        </p:documentation>
    </p:option>

    <p:option name="repair" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair the DTBook</h2>
            <p px:role="desc">Apply repair routines on the DTBook</p>
        </p:documentation>
    </p:option>
    <p:option name="fixCharset" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair - Fix charset</h2>
            <p px:role="desc">Fix the document charset (To be implemented)</p>
        </p:documentation>
    </p:option>
    <p:option name="tidy" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy up the DTBook</h2>
            <p px:role="desc">Apply tidying routines on the DTBook</p>
        </p:documentation>
    </p:option>
    <p:option name="simplifyHeadingLayout" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Simplify headings layout</h2>
            <p px:role="desc">TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="externalizeWhitespace" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Externalize whitespaces</h2>
            <p px:role="desc">TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="documentLanguage" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Document language</h2>
            <p px:role="desc">Set a document language</p>
        </p:documentation>
    </p:option>
    <p:option name="narrator" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Prepare DTBook for pipeline 1 narrator</h2>
            <p px:role="desc">Apply Pipeline 1 "narrator" cleaning routines on the document</p>
        </p:documentation>
    </p:option>
    <p:option name="publisher" select="''" >
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Narrator - Publisher</h2>
            <p px:role="desc">Publisher metadata (dc:Publisher) to be added if none is defined in the DTBook</p>
        </p:documentation>
    </p:option>
    <p:option name="ApplySentenceDetection" select="'false'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply sentences detection</h2>
            <p px:role="desc">Encapsulate sentences within the document</p>
        </p:documentation>
    </p:option>
    <p:option name="WithDoctype" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include the doctype in resulting DTBook(s)</h2>
            <p px:role="desc">The resulting DTBook will have a standard DTBook 2005-3 doctype, optionally with MathML declaration if MathML is present in the document.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl">
        <p:documentation>
            px:dtbook-break-detect
            px:dtbook-unwrap-words
        </p:documentation>
    </p:import>
    <p:import href="../upgrade-dtbook/upgrade-dtbook.xpl">
        <p:documentation>
            px:dtbook-upgrade
        </p:documentation>
    </p:import>
    <p:import href="fix-dtbook.xpl">
        <p:documentation>
            pxi:dtbook-fix
        </p:documentation>
    </p:import>
    <p:import href="doctyping.xpl">
        <p:documentation>
            pxi:dtbook-doctyping
        </p:documentation>
    </p:import>

    <p:for-each px:message="Cleaning DTBook(s)">
        <p:variable name="output-name" select="concat(replace(replace(base-uri(.),'^.*/([^/]+)$','$1'),'\.[^\.]*$',''),'.xml')"/>
        <!-- Update the DTBook -->
        <px:dtbook-upgrade/>
        <!-- Apply routines -->
        <pxi:dtbook-fix>
            <p:with-option name="repair" select="$repair='true'"/>
            <p:with-option name="fixCharset" select="$fixCharset='true'"/>
            <p:with-option name="tidy" select="$tidy='true'"/>
            <p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout='true'"/>
            <p:with-option name="externalizeWhitespace" select="$externalizeWhitespace='true'"/>
            <p:with-option name="documentLanguage" select="$documentLanguage='true'"/>
            <p:with-option name="narrator" select="$narrator='true'"/>
            <p:with-option name="publisher" select="$publisher='true'"/>
        </pxi:dtbook-fix>

        <p:choose>
            <p:when test="$ApplySentenceDetection='true'">
                <px:dtbook-break-detect/>
                <px:dtbook-unwrap-words/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <!--
            FIXME: this should be handled with px:fileset-store
        -->
        <p:choose>
            <p:when test="$WithDoctype='true'">
                <!-- DTBook with doctype (result is serialized) -->
                <pxi:dtbook-doctyping/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <!-- Store on disk -->
        <p:store>
            <p:with-option name="href" select="concat(resolve-uri($result),$output-name)"/>
        </p:store>
    </p:for-each>

</p:declare-step>
