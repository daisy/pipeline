<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                px:input-filesets="epub3"
                type="px:epub3-validator">

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
        <p:pipe step="validate" port="html-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc" xml:space="preserve">An XML document describing, briefly, whether the validation was successful.

[More details on the file format](http://daisy.github.io/pipeline/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe step="validate" port="validation-status"/>
    </p:output>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
        </p:documentation>
    </p:option>

    <p:option name="accessibility-check" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Enable accessibility check</h2>
            <p px:role="desc" xml:space="preserve">Check the compliance to the EPUB accessibility specification using the [DAISY Ace](https://daisy.github.io/ace) tool.

To use this option, check [how to install
Ace](https://daisy.github.io/ace/getting-started/installation/) on your system. Note that this
option is only available for zipped EPUBs.</p>
        </p:documentation>
    </p:option>

    <p:output port="ace-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Accessibility report</h1>
            <p px:role="desc" xml:space="preserve">If the accessibility check option is enabled, an HTML report detailing the compliance to the EPUB Accessibility specification is output on this port.</p>
        </p:documentation>
        <p:pipe step="validate" port="ace-report"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub-validate
        </p:documentation>
    </p:import>

    <px:epub-validate px:progress="1" version="3" name="validate">
        <p:with-option name="epub" select="$epub"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
        <p:with-option name="accessibility-check" select="$accessibility-check"/>
    </px:epub-validate>

</p:declare-step>
