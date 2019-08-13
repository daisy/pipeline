<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" 
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" 
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" version="1.0"
    xmlns:c="http://www.w3.org/ns/xproc-step">


    <p:declare-step type="px:ace" name="main">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">ACE report generation</h1>
            <p px:role="desc">Generate an accessibility check report using the Accessibility Checker for EPUB tool. 
Please, be sure that ace is installed or <a href="https://daisy.github.io/ace/getting-started/installation/">install Ace</a> before using this step. 
If not, a <code>jhove</code> report will be returned by the step instead of the html report, to notify that Ace was not found.</p>

        </p:documentation>
        <!-- anyFileURI to the epub -->
        <p:option name="epub" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">EPUB URI</h1>
                <p px:role="desc">URI of the checked EPUB</p>
            </p:documentation>
        </p:option>

        <p:option name="temp-dir" required="false" select="''">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Temporary directory URI (optionnal)</h1>
                <p px:role="desc">Directory where the intermediate ace reports will be stored.
                 If empty or not defined, the system temporary folder is used</p>
            </p:documentation>
        </p:option>

        <p:option name="lang" required="false" select="'en'">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Reports localization</h1>
                <p px:role="desc">Code of the language to use to localize the reports. Default is 'en' for english.</p>
            </p:documentation>
        </p:option>

        <p:output port="html-report" sequence="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">HTML report</h1>
                <p px:role="desc">the html report created by Ace.</p>
            </p:documentation>
            <p:pipe port="html-report" step="launching-ace" />
        </p:output>
        <p:output port="json-report" sequence="true" primary="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">JSON report</h1>
                <p px:role="desc">The json report created by Ace within a <code>c:data</code> tag. If Ace could not be found, a <code>jhove</code> report is returned.</p>
            </p:documentation>
            <p:pipe port="json-report" step="launching-ace" />
        </p:output>

        <!-- Java internal step signature -->
        <p:declare-step type="pxi:ace">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <h1 px:role="name">Ace internal adapter</h1>
                <p px:role="desc">Java-implemented step to call Ace on an epub and retrieve the reports URIs.</p>
            </p:documentation>
            <!-- step declaration for the epubcheck-adapter implemented in java -->
            <p:option name="epub" required="true">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">EPUB URI</h1>
                    <p px:role="desc">URI of the checked EPUB.</p>
                </p:documentation>
            </p:option>
            <p:option name="temp-dir" required="false">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">Temporary directory URI (optionnal)</h1>
                    <p px:role="desc">Intermediate directory where the unzipped epub and html and json ACE reports will be stored.
                 If empty or not defined, the system temporary folder is used/</p>
                </p:documentation>
            </p:option>
            <p:option name="lang" required="false">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">Reports localization</h1>
                    <p px:role="desc">Code of the language to use to localize the reports. Default is 'en' for english.</p>
                </p:documentation>
            </p:option>
            <p:output port="html-report-uri">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">Ace HTML report's uri</h1>
                    <p px:role="desc">A <code>c:result</code> tag containing the accessibility check html report URI 
                    (To use with the <code>px:html-load</code> step). </p>
                </p:documentation>
            </p:output>
            <p:output port="json-report-uri" primary="true">
                <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                    <h1 px:role="name">Ace JSON report's uri</h1>
                    <p px:role="desc">A <code>c:result</code> tag containing the accessibility check json report URI.
                    (to load using a <code>c:request</code> and <code>p:http-request</code>)</p>
                </p:documentation>
            </p:output>
        </p:declare-step>

        <!-- DEPENDENCIES -->
        <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
            <p:documentation>
                px:html-load
            </p:documentation>
        </p:import>
        <!-- If the ace adapter module has been correctly loaded, 
            use it to retrieve the reports URI, load the reports
            and pipe their content through the html-report 
            and json-report output port-->
        <p:choose name="launching-ace">
            <p:when test="p:step-available('pxi:ace')">
                <p:output port="html-report" >
                    <p:pipe step="loading-html-report" port="result" />
                </p:output>
                <p:output port="json-report" primary="true">
                    <p:pipe step="loading-json-report" port="result" />
                </p:output>
                <pxi:ace name="checking-epub">
                    <p:with-option name="epub" select="$epub"/>
                    <p:with-option name="temp-dir" select="$temp-dir"/>
                    <p:with-option name="lang" select="$lang"/>
                </pxi:ace>
                <!-- Loading the json -->
                <p:group name="loading-json-report">
                    <p:output port="result" />
                    <p:variable name="json-uri" select="/c:result/text()"/>
                    <!--It seems p:data within identity does not allow to load a file from a computed URI,
                        but it is possible to use a http-request with a constructed c:request to load as plain text the content pointed by the file-uri.
                        (without content type override, the json is load in base64) -->
                    <p:try>
                        <p:group>
                            <p:identity>
                                <p:input port="source">
                                    <p:inline>
                                        <c:request method="GET" detailed="true" override-content-type="text/plain; charset=utf-8"/>
                                    </p:inline>
                                </p:input>
                            </p:identity>
                            <p:add-attribute match="c:request" attribute-name="href">
                                <p:with-option name="attribute-value" select="$json-uri"/>
                            </p:add-attribute>
                            <p:http-request/>
                        </p:group>
                        <p:catch>
                            <p:identity>
                                <p:input port="source">
                                    <p:inline>
                                        <error/>
                                    </p:inline>
                                </p:input>
                            </p:identity>
                        </p:catch>
                    </p:try>
                    <!--The resulting c:body is replaced by a c:data with spectified 
                        content type and encoding.  -->
                    <p:wrap wrapper="c:data" match="/c:body/text()"/>
                    <p:unwrap match="/*"/>
                    <p:add-attribute match="/*" 
                            attribute-name="content-type" 
                            attribute-value="application/json"/>
                    <p:add-attribute match="/*"
                            attribute-name="encoding" 
                            attribute-value="UTF-8"/>
                </p:group>
                <p:sink />
                <!-- Load the html report -->
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="checking-epub" port="html-report-uri"/>
                    </p:input>
                </p:identity>
                <px:html-load name="loading-html-report">
                    <p:with-option name="href" select="/c:result/text()" />
                </px:html-load>
                <p:sink />
            </p:when>
            <p:otherwise>
                <p:output port="json-report" primary="true">
                    <p:pipe step="ace-not-found" port="result" />
                </p:output>
                <p:output port="html-report" >
                    <p:empty />
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
