<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
           xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:declare-step type="px:ace" name="main">

        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Ace check</h1>
            <p px:role="desc">Generate an accessibility report using the Accessibility Checker for EPUB (Ace) tool.

Please make sure that Ace is installed or <a
href="https://daisy.github.io/ace/getting-started/installation/">install Ace</a> before using this
step. If not, a <code>jhove</code> report will be returned by the step instead of the HTML report,
to notify that Ace was not found.</p>
        </p:documentation>

        <p:option name="epub" required="true" px:type="anyFileURI">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">EPUB URI</h1>
                <p px:role="desc">URI of the EPUB to check</p>
            </p:documentation>
        </p:option>

        <p:option name="temp-dir" required="false" select="''">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Temporary directory URI</h1>
                <p px:role="desc">Directory where the Ace reports will be stored. If empty or not
                defined, the system temporary folder is used.</p>
            </p:documentation>
        </p:option>

        <p:option name="lang" required="false" select="'en'">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Report language</h1>
                <p px:role="desc">Code of the language to use to localize the reports. Default is
                'en' (English).</p>
            </p:documentation>
        </p:option>

        <p:output port="html-report" sequence="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">HTML report</h1>
                <p px:role="desc">The HTML report created by Ace.</p>
            </p:documentation>
            <p:pipe step="launching-ace" port="html-report"/>
        </p:output>

        <p:output port="json-report" sequence="true" primary="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">JSON report</h1>
                <p px:role="desc">The JSON report created by Ace. If Ace could not be found, a
                <code>jhove</code> report is returned.</p>
            </p:documentation>
            <p:pipe step="launching-ace" port="json-report"/>
        </p:output>

        <!-- Java internal step signature -->
        <p:declare-step type="pxi:ace">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Ace internal adapter</h1>
                <p px:role="desc">Java-implemented step to call Ace on an EPUB and retrieve the report URIs.</p>
            </p:documentation>
            <p:option name="epub" required="true"/>
            <p:option name="temp-dir" required="false"/>
            <p:option name="lang" required="false"/>
            <p:output port="html-report-uri">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">HTML report URI</h1>
                    <p px:role="desc">A <code>c:result</code> document containing the URI of the
                    HTML report's location on disk.</p>
                </p:documentation>
            </p:output>
            <p:output port="json-report-uri" primary="true">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">JSON report URI</h1>
                    <p px:role="desc">A <code>c:result</code> document containing the URI of the
                    JSON report's location on disk.</p>
                </p:documentation>
            </p:output>
        </p:declare-step>

        <!-- DEPENDENCIES -->
        <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
            <p:documentation>
                px:html-load
            </p:documentation>
        </p:import>
        <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
            <p:documentation>
                px:data
            </p:documentation>
        </p:import>

        <!-- If the ace adapter module has been correctly loaded, use it to retrieve the report
             URIs, load the reports and pipe their content through the html-report and json-report
             output port -->
        <p:choose name="launching-ace">
            <p:when test="p:step-available('pxi:ace')">
                <p:output port="html-report" >
                    <p:pipe step="loading-html-report" port="result"/>
                </p:output>
                <p:output port="json-report" primary="true">
                    <p:pipe step="loading-json-report" port="result"/>
                </p:output>
                <pxi:ace name="checking-epub">
                    <p:with-option name="epub" select="$epub"/>
                    <p:with-option name="temp-dir" select="$temp-dir"/>
                    <p:with-option name="lang" select="$lang"/>
                </pxi:ace>
                <!-- Load the json report -->
                <p:group name="loading-json-report">
                    <p:output port="result"/>
                    <p:variable name="json-uri" select="/c:result/text()"/>
                    <px:data content-type="text/json">
                        <p:with-option name="href" select="$json-uri"/>
                    </px:data>
                    <p:add-attribute match="/*" attribute-name="encoding" attribute-value="UTF-8"/>
                </p:group>
                <p:sink/>
                <!-- Load the html report -->
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="checking-epub" port="html-report-uri"/>
                    </p:input>
                </p:identity>
                <px:html-load name="loading-html-report">
                    <p:with-option name="href" select="/c:result/text()"/>
                </px:html-load>
                <p:sink/>
            </p:when>
            <p:otherwise>
                <p:output port="json-report" primary="true">
                    <p:pipe step="ace-not-found" port="result"/>
                </p:output>
                <p:output port="html-report" >
                    <p:empty/>
                </p:output>
                <p:in-scope-names name="vars"/>
                <p:template name="ace-not-found">
                    <p:input port="template">
                        <p:inline>
                            <jhove xmlns="http://hul.harvard.edu/ois/xml/ns/jhove" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" name="ace-adapter" release="x.x"
                                date="{tokenize(string(current-date()),'\+')[1]}">
                                <date>{current-dateTime()}</date>
                                <repInfo uri="{$epub}">
                                    <messages>
                                        <message>WARN: {$epub}: Ace program is not available. Are you sure Ace is installed on your system ? Please check your environment variable PATH, or follow the instructions at 'https://daisy.github.io/ace/getting-started/installation/' .</message>
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
