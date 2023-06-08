<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                name="main"
                type="px:dtbook-cleaner.script"
                px:input-filesets="dtbook"
                px:output-filesets="dtbook"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Cleaner</h1>
        <p px:role="desc">Cleaning DTBook documents</p>
        <div px:role="author maintainer">
            <p px:role="name">Nicolas Pavie</p>
            <a px:role="contact" href="mailto:pavie.nicolas@gmail.com">pavie.nicolas@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- IMPORTS -->
    <!-- ***************************************************** -->
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-fix
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl">
        <p:documentation>
            px:dtbook-break-detect
            px:dtbook-unwrap-words
        </p:documentation>
    </p:import>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <p:input port="source" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">One or more DTBook files to be cleaned</p>
        </p:documentation>
    </p:input>

    <p:output port="result" primary="true" sequence="true" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook file(s)</h2>
            <p px:role="desc">Cleaned DTBooks</p>
        </p:documentation>
        <p:pipe step="cleaned-dtbook" port="result"/>
    </p:output>


    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">The directory that will contain the resulting DTBook.</p>
        </p:documentation>
    </p:option>


    <!-- Options from dtbook-fix script -->
    <p:option name="repair" required="false" select="false()" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair the dtbook</h2>
            <p px:role="desc">Apply repair routines on the dtbook</p>
        </p:documentation>
    </p:option>
    <p:option name="fixCharset" required="false" select="false()" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Repair - Fix Charset</h2>
            <p px:role="desc">Fix the document charset (To be implemented)</p>
        </p:documentation>
    </p:option>
    <p:option name="tidy" required="false" select="false()" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy up the dtbook</h2>
            <p px:role="desc">Apply tidying routines on the dtbook</p>
        </p:documentation>
    </p:option>
    <p:option name="simplifyHeadingLayout" required="false" select="false()" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tidy - Simplify headings layout</h2>
            <p px:role="desc">TBD</p>
        </p:documentation>
    </p:option>
    <p:option name="externalizeWhitespace" required="false" select="false()" px:type="boolean">
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

    <p:option name="narrator" required="false" select="false()" px:type="boolean">
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

    <p:option name="ApplySentenceDetection" select="false()" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply sentences detection</h2>
            <p px:role="desc">Encapsulate sentences within the document</p>
        </p:documentation>
    </p:option>

    <p:option name="WithDoctype" select="true()" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include the doctype in resulting dtbook(s)</h2>
            <p px:role="desc">Resulting dtbook will have a standard dtbook 2005-3 doctype, optionnaly with mathml declaration if mathml is found in the document.</p>
        </p:documentation>
    </p:option>

    <p:for-each name="cleaned-dtbook" px:message="Cleaning dtbook(s)">
        <p:output port="result" sequence="true">
            <p:pipe step="fixed" port="result"/>
        </p:output>
        <p:variable name="output-name" select="concat(replace(replace(base-uri(.),'^.*/([^/]+)$','$1'),'\.[^\.]*$',''),'.xml')" />
        <!-- Update the dtbook -->
        <px:dtbook-upgrade/>
        <!-- Apply routines -->
        <px:dtbook-fix name="fixed">
            <p:with-option name="repair" select="$repair" />
            <p:with-option name="fixCharset" select="$fixCharset" />
            <p:with-option name="tidy" select="$tidy" />
            <p:with-option name="simplifyHeadingLayout" select="$simplifyHeadingLayout" />
            <p:with-option name="externalizeWhitespace" select="$externalizeWhitespace" />
            <p:with-option name="documentLanguage" select="$documentLanguage" />
            <p:with-option name="narrator" select="$narrator" />
            <p:with-option name="publisher" select="$publisher" />
        </px:dtbook-fix>

        <p:choose>
            <p:when test="$WithDoctype">
                <!-- dtbook with doctype (result is serialized) -->
                <px:dtbook-doctyping/>
            </p:when>
            <p:otherwise>
                <p:identity />
            </p:otherwise>
        </p:choose>
        <!-- Store on disk -->
        <p:store>
            <p:with-option name="href" select="concat(resolve-uri($output-dir),$output-name)"/>
        </p:store>
    </p:for-each>

</p:declare-step>
