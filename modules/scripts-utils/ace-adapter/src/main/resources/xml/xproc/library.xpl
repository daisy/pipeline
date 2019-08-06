<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" 
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" 
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" version="1.0"
    xmlns:c="http://www.w3.org/ns/xproc-step">


    <p:declare-step type="px:ace" name="main">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">ACE report generation</h1>
            <p px:role="desc">Generate an accessibility check report using the Accessibility Checker for EPUB tool.</p>
        </p:documentation>
        <!-- anyFileURI to the epub -->
        <p:option name="epub" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">EPUB URI</h1>
                <p px:role="desc">URI of the EPUB to control</p>
            </p:documentation>
        </p:option>
        <!--  -->
        <p:option name="temp-dir" required="false" select="''">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Temporary directory URI</h1>
                <p px:role="desc">Intermediate directory where the unzipped epub and html and json ACE reports will be stored</p>
            </p:documentation>
        </p:option>

        <p:option name="output-dir" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Output directory URI</h1>
                <p px:role="desc">URI of the folder were the accessibility check html report will be stored</p>
            </p:documentation>
        </p:option>

        <p:output port="result" sequence="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Output result (XML)</h1>
                <p px:role="desc">A <code>c:result</code> tag containing the accessibility check html report URI</p>
            </p:documentation>
        </p:output>

        <p:declare-step type="pxi:ace">
            <!-- step declaration for the epubcheck-adapter implemented in java -->
            <p:option name="epub" required="true"/>
            <p:option name="output-dir" required="true"/>
            <p:option name="temp-dir" required="false"/>
            <p:output port="result" sequence="true"/>
        </p:declare-step>

        <!-- DEPENDENCIES -->
        <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
            <p:documentation>
                px:html-load
            </p:documentation>
        </p:import>

        <p:choose>
            <p:when test="p:step-available('pxi:ace')">
                <pxi:ace>
                    <p:with-option name="epub" select="$epub"/>
                    <p:with-option name="temp-dir" select="$temp-dir"/>
                    <p:with-option name="output-dir" select="$output-dir"/>
                </pxi:ace>
                <px:html-load>
                    <p:with-option name="href" select="/c:result/text()" />
                </px:html-load>
                <!-- test with html load -->
            </p:when>
            <p:otherwise>
                <p:in-scope-names name="vars"/>
                <p:template>
                    <p:input port="template">
                        <p:inline>
                            <jhove xmlns="http://hul.harvard.edu/ois/xml/ns/jhove" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" name="epubcheck-adapter" release="x.x"
                                date="{tokenize(string(current-date()),'\+')[1]}">
                                <date>{current-dateTime()}</date>
                                <repInfo uri="{$epub}">
                                    <messages>
                                        <message>WARN: {$epub}: ACE step is not available. Are you sure ACE is installed on your system ? Please check your environment variable PATH.</message>
                                    </messages>
                                </repInfo>
                            </jhove>
                        </p:inline>
                    </p:input>
                    <p:input port="source">
                        <p:inline>
                            <irrelevant/>
                        </p:inline>
                    </p:input>
                    <p:input port="parameters">
                        <p:pipe step="vars" port="result"/>
                    </p:input>
                </p:template>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

</p:library>
