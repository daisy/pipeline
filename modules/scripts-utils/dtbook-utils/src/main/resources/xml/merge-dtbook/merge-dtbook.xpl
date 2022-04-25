<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-inline-prefixes="#all"
                type="px:dtbook-merge" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Merge DTBook</h1>
        <p px:role="desc">Merge 2 or more DTBook documents.</p>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a href="mailto:marisa.demeglio@gmail.com" px:role="contact">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>
    <!-- 
        TODO: 
         * copy referenced resources (such as images)
         * deal with xml:lang (either copy once and put in dtbook/@xml:lang or, if different languages are used, copy the @xml:lang attr into the respective sections.
    -->

    <p:input port="source" primary="true" sequence="true" px:name="in"
        px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">in</h2>
            <p px:role="desc">Sequence of DTBook files</p>
        </p:documentation>
    </p:input>
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">out</h2>
            <p px:role="desc">The result</p>
        </p:documentation>
    </p:output>
    <p:option name="output-base-uri" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The base URI of the result document.</p>
        </p:documentation>
    </p:option>
    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p><code>d:fileset</code> document that contains a mapping from input to output files
            and contained <code>id</code> attributes.</p>
        </p:documentation>
        <p:pipe step="file-mapping" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>

    <!-- file mapping -->
    <p:group name="file-mapping">
        <p:output port="result"/>
        <p:for-each>
            <p:iteration-source>
                <p:pipe step="main" port="source"/>
            </p:iteration-source>
            <p:variable name="input-base-uri" select="base-uri(/*)"/>
            <p:for-each>
                <p:iteration-source select="//*[@id|@xml:id]"/>
                <p:template>
                    <p:input port="template">
                        <p:inline>
                            <d:anchor id="{/*/(@xml:id,@id)[1]}"/>
                        </p:inline>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:template>
            </p:for-each>
            <p:wrap-sequence wrapper="d:file"/>
            <p:add-attribute match="/*" attribute-name="href">
                <p:with-option name="attribute-value" select="$output-base-uri">
                    <p:empty/>
                </p:with-option>
            </p:add-attribute>
            <p:add-attribute match="/*" attribute-name="original-href">
                <p:with-option name="attribute-value" select="$input-base-uri">
                    <p:empty/>
                </p:with-option>
            </p:add-attribute>
        </p:for-each>
        <p:wrap-sequence wrapper="d:fileset"/>
    </p:group>
    <p:sink/>

    <!--Store the first DTBook for later reference-->
    <p:split-sequence name="first-dtbook" initial-only="true" test="position()=1">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
    </p:split-sequence>
    <px:message severity="DEBUG" message="Merging DTBook documents"/>
    <p:sink/>

    <p:xslt template-name="merge">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="merge-dtbook.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:with-option name="output-base-uri" select="base-uri(/*)">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:with-option>
    </p:xslt>

</p:declare-step>
