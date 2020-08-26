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
step. If this is not the case, the reports will contain a message that Ace was not found.</p>
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

        <p:output port="html-report">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">HTML report</h1>
                <p px:role="desc">The HTML report created by Ace, or if Ace could not be found, a
                short HTML document stating this.</p>
            </p:documentation>
            <p:pipe step="ace-or-not-found" port="html-report"/>
        </p:output>

        <p:output port="json-report" primary="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">JSON report</h1>
                <p px:role="desc">The JSON report created by Ace, or if Ace could not be found, a
                short JSON document stating this.</p>
            </p:documentation>
            <p:pipe step="ace-or-not-found" port="json-report"/>
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
        <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
            <p:documentation>
                px:fileset-create
                px:fileset-add-entry
                px:fileset-load
            </p:documentation>
        </p:import>

        <!-- If the ace adapter module has been correctly loaded, use it to retrieve the report
             URIs, load the reports and pipe their content through the html-report and json-report
             output port -->
        <p:choose name="ace-or-not-found">
            <p:when test="p:step-available('pxi:ace')">
                <p:output port="html-report">
                    <p:pipe step="html" port="result"/>
                </p:output>
                <p:output port="json-report" primary="true">
                    <p:pipe step="json" port="result"/>
                </p:output>
                <pxi:ace name="ace">
                    <p:with-option name="epub" select="$epub"/>
                    <p:with-option name="temp-dir" select="$temp-dir"/>
                    <p:with-option name="lang" select="$lang"/>
                </pxi:ace>
                <p:sink/>
                <!-- Load the json report -->
                <p:group name="json">
                    <p:output port="result"/>
                    <px:fileset-create/>
                    <px:fileset-add-entry media-type="text/json">
                        <p:with-option name="href" select="/c:result/text()">
                            <p:pipe step="ace" port="json-report-uri"/>
                        </p:with-option>
                    </px:fileset-add-entry>
                    <px:fileset-load/>
                    <p:add-attribute match="/*" attribute-name="content-type" attribute-value="text/json"/>
                    <p:add-attribute match="/*" attribute-name="encoding" attribute-value="UTF-8"/>
                </p:group>
                <p:sink/>
                <!-- Load the html report -->
                <p:group name="html">
                    <p:output port="result"/>
                    <px:fileset-create/>
                    <px:fileset-add-entry media-type="text/html">
                        <p:with-option name="href" select="/c:result/text()">
                            <p:pipe step="ace" port="html-report-uri"/>
                        </p:with-option>
                    </px:fileset-add-entry>
                    <px:fileset-load/>
                </p:group>
                <p:sink/>
            </p:when>
            <p:otherwise>
                <p:output port="json-report" primary="true">
                    <p:pipe step="json" port="result"/>
                </p:output>
                <p:output port="html-report">
                    <p:pipe step="html" port="result"/>
                </p:output>
                <p:identity name="json">
                    <p:input port="source">
                        <p:inline>
                            <c:data content-type="text/json" encoding="UTF-8">
                                {
                                  "error": "ace-not-available",
                                  "message": "Ace was not found. Please check your \"PATH\" environment variable, or follow the installation instructions at https://daisy.github.io/ace/getting-started/installation/." }
                            </c:data>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:sink/>
                <p:identity name="html">
                    <p:input port="source">
                        <p:inline>
                            <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
                                <head>
                                    <meta charset="utf-8"/>
                                    <title>EPUB Accessibility Report</title>
                                </head>
                                <body>
                                    <h1>EPUB Accessibility Report</h1>
                                    <p>Ace was not found. Please check your "PATH" environment
                                    variable, or follow the <a
                                    href="https://daisy.github.io/ace/getting-started/installation/">installation
                                    instructions</a>.</p>
                                </body>
                            </html>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

</p:library>
