<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-validator" version="1.0"
    px:input-filesets="epub3"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:html="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 Validator</h1>
        <p px:role="desc">Validates a EPUB.</p>
        <address px:role="author maintainer">
            <p>Script wrapper for epubcheck maintained by <span px:role="name">Jostein Austvik Jacobsen</span>
                (organization: <span px:role="organization">NLB</span>,
                e-mail: <a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a>).</p>
        </address>
        <p><a px:role="homepage" href="http://daisy.github.io/pipeline/modules/epub3-validator">Online Documentation</a></p>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB</h2>
            <p px:role="desc">Either a *.epub file or a *.opf file.</p>
        </p:documentation>
    </p:option>

    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation report</h1>
        </p:documentation>
        <p:pipe port="result" step="html-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc" xml:space="preserve">An XML document describing, briefly, whether the validation was successful.

[More details on the file format](http://daisy.github.io/pipeline/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe port="result" step="status"/>
    </p:output>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epubcheck-adapter/library.xpl"/>

    <px:epubcheck>
        <p:with-option name="epub" select="$epub"/>
        <p:with-option name="mode" select="if (ends-with(lower-case($epub),'.epub')) then 'epub' else 'expanded'"/>
        <p:with-option name="version" select="'3'"/>
        <p:with-option name="temp-dir" select="concat($temp-dir,'/epubcheck')"/>
    </px:epubcheck>
    
    <p:xslt name="xml-report.not-wrapped">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/epubcheck-report-to-pipeline-report.xsl"/>
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
    <p:identity name="xml-report"/>

    <p:xslt name="html-report">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/epubcheck-pipeline-report-to-html-report.xsl"/>
        </p:input>
    </p:xslt>

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

</p:declare-step>
