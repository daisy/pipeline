<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                type="px:epub-validate">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>EPUB Validator</h1>
        <p>Validates a EPUB.</p>
        <address px:role="author">
            Wrapper for epubcheck written by <span px:role="name">Jostein Austvik Jacobsen</span>
            (organisation: <span px:role="organization">NLB</span>, e-mail: <a px:role="contact"
            href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a>).
        </address>
    </p:documentation>

    <p:option name="epub" required="true" cx:type="xs:anyURI" cx:as="xs:string" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>EPUB</h2>
            <p>Either a *.epub file or a *.opf file.</p>
        </p:documentation>
    </p:option>

    <p:option name="version" required="false" select="'3'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>EPUB version: "2" or "3"</p>
        </p:documentation>
    </p:option>

    <p:option name="report-method" cx:type="port|log|error" select="'port'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Select the method used for reporting validation issues:</p>
            <dl>
                <dt>port</dt>
                <dd>Issues are reported only on the xml-report and html-report output ports.</dd>
                <dt>log</dt>
                <dd>In addition to the xml-report and html-report output ports, issues are also
                reported through warning messages.</dd>
                <dt>error</dt>
                <dd>Issues are reported through error messages and also trigger an XProc error.</dd>
            </dl>
        </p:documentation>
    </p:option>

    <p:output port="xml-report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>XML Report</h1>
            <p>Raw validation output.</p>
        </p:documentation>
        <p:pipe port="result" step="xml-report"/>
    </p:output>

    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>Validation report</h1>
        </p:documentation>
        <p:pipe port="result" step="html-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>Validation status</h1>
            <p>An XML document describing, briefly, whether the validation was successful. <a
            href="http://daisy.github.io/pipeline/StatusXML">More details on the file
            format</a>.</p>
        </p:documentation>
        <p:pipe port="result" step="status"/>
    </p:output>

    <p:option name="temp-dir" required="false" select="''" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Temporary directory</h2>
            <p>In "expanded" mode, this directory will be used to store the zipped EPUB. The
            directory must not exist yet. If this option is not provided, a default temporary
            directory will be used.</p>
        </p:documentation>
    </p:option>

    <p:option name="accessibility-check" required="false" cx:type="xs:boolean" cx:as="xs:string" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>Enable accessibility check</h2>
            <p>Check the compliance to the EPUB accessibility specification using the <a
            href="https://daisy.github.io/ace">DAISY Ace</a> tool. To use this option, check <a
            href="https://daisy.github.io/ace/getting-started/installation/">how to install Ace</a>
            on your system. Note that this option is only available for zipped EPUBs.</p>
        </p:documentation>
    </p:option>

    <p:output port="ace-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1>Accessibility report</h1>
            <p>If the accessibility check option is enabled, an HTML report detailing the compliance
            to the EPUB Accessibility specification is output on this port.</p>
        </p:documentation>
        <p:pipe port="result" step="ace-report"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            px:report-errors
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epubcheck-adapter/library.xpl">
        <p:documentation>
            px:epubcheck
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/ace-adapter/library.xpl">
        <p:documentation>
            px:ace
        </p:documentation>
    </p:import>

    <px:assert message="Version must be '2' or '3', but got '$1'" error-code="XXXXX">
        <p:with-option name="test" select="$version=('2','3')"/>
    </px:assert>

    <px:epubcheck px:message="Running EPUBCheck" px:progress=".8">
        <p:with-option name="epub" select="$epub"/>
        <p:with-option name="mode" select="if (ends-with(lower-case($epub),'.epub')) then 'epub' else 'expanded'"/>
        <p:with-option name="version" select="$version"/>
        <p:with-option name="temp-dir" select="concat($temp-dir,'/epubcheck')"/>
    </px:epubcheck>

    <p:group name="xml-report">
        <p:output port="result"/>
        <p:xslt name="xml-report.not-wrapped">
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="epubcheck-report-to-pipeline-report.xsl"/>
            </p:input>
        </p:xslt>
        <p:for-each>
            <p:iteration-source select="//d:warn">
                <p:pipe port="result" step="xml-report.not-wrapped"/>
            </p:iteration-source>
            <p:identity/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:warnings" name="warnings"/>
        <p:for-each>
            <p:iteration-source select="//d:error">
                <p:pipe port="result" step="xml-report.not-wrapped"/>
            </p:iteration-source>
            <p:identity/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:errors" name="errors"/>
        <p:for-each name="exception">
            <p:iteration-source select="//d:exception">
                <p:pipe port="result" step="xml-report.not-wrapped"/>
            </p:iteration-source>
            <p:identity/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:exceptions" name="exceptions"/>
        <p:for-each name="hint">
            <p:iteration-source select="//d:hint">
                <p:pipe port="result" step="xml-report.not-wrapped"/>
            </p:iteration-source>
            <p:identity/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:hints" name="hints"/>
        <p:delete match="//d:report/*">
            <p:input port="source">
                <p:pipe port="result" step="xml-report.not-wrapped"/>
            </p:input>
        </p:delete>
        <p:insert match="//d:report" position="first-child">
            <p:input port="insertion">
                <p:pipe port="result" step="exceptions"/>
                <p:pipe port="result" step="errors"/>
                <p:pipe port="result" step="warnings"/>
                <p:pipe port="result" step="hints"/>
            </p:input>
        </p:insert>
        <p:delete match="//d:report/*[not(*)]"/>
    </p:group>

    <!-- possibly report errors as warning messages -->
    <p:choose>
        <p:when test="$report-method=('log','error') and /d:document-validation-report/d:document-info/d:error-count[string(.)!='0']">
            <p:xslt name="errors">
                <p:input port="stylesheet">
                    <p:inline>
                        <xsl:stylesheet version="2.0" xpath-default-namespace="http://www.daisy.org/ns/pipeline/data">
                            <xsl:template match="/*">
                                <c:errors>
                                    <xsl:for-each select="/*/reports/report/errors/error">
                                        <c:error>
                                            <xsl:variable name="desc" select="normalize-space(string(desc))"/>
                                            <xsl:variable name="code" select="replace($desc,'^([a-zA-Z0-9_-]+):.*$','$1')"/>
                                            <xsl:if test="not($code='')">
                                                <xsl:attribute name="code" select="concat('EPUBCHECK:',$code)"/>
                                            </xsl:if>
                                            <xsl:if test="file">
                                                <px:location>
                                                    <px:file>
                                                        <xsl:attribute name="href" select="normalize-space(string(file))"/> <!-- relative path -->
                                                        <xsl:sequence select="location/(@line|@column)"/>
                                                    </px:file>
                                                </px:location>
                                            </xsl:if>
                                            <xsl:value-of select="replace($desc,'^([a-zA-Z0-9_-]+:\s+)','')"/>
                                        </c:error>
                                    </xsl:for-each>
                                </c:errors>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <p:sink/>
            <px:report-errors>
                <p:input port="source">
                    <p:pipe step="xml-report" port="result"/>
                </p:input>
                <p:input port="report">
                    <p:pipe step="errors" port="result"/>
                </p:input>
                <p:with-option name="method" select="$report-method"/>
            </px:report-errors>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:xslt name="html-report">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="epubcheck-pipeline-report-to-html-report.xsl"/>
        </p:input>
    </p:xslt>
    <p:sink/>

    <p:group name="status">
        <p:output port="result"/>
        <p:for-each>
            <p:iteration-source select="/d:document-validation-report/d:document-info/d:error-count">
                <p:pipe port="result" step="xml-report"/>
            </p:iteration-source>
            <p:identity/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:validation-status"/>
        <p:add-attribute attribute-name="result" match="/*">
            <p:with-option name="attribute-value" select="if (sum(/*/*/number(.))&gt;0) then 'error' else 'ok'"/>
        </p:add-attribute>
        <p:delete match="/*/node()"/>
    </p:group>
    <p:sink/>

    <p:choose name="ace-report">
        <p:when test="$accessibility-check = 'true' and not(ends-with($epub,'.opf'))">
            <p:output port="result">
                <p:pipe step="ace-check" port="html-report" />
            </p:output>
            <px:ace name="ace-check" px:message="Running Ace">
                <p:with-option name="epub" select="$epub"/>
            </px:ace>
            <p:sink/>
        </p:when>
        <p:otherwise>
            <p:output port="result" />
            <p:identity>
                <p:input port="source">
                    <p:inline>
                        <html />
                    </p:inline>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:sink/>

</p:declare-step>
