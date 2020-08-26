<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:dtbook-validator.script"
                px:input-filesets="dtbook"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Validator</h1>
        <p px:role="desc">Validates DTBook documents. Supports inclusion of MathML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/dtbook-validator">
            Online documentation
        </a>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <!-- NOTE: the "input" here is given by an option string "input-dtbook" -->


    <!--<p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">A copy of the input document; may include PSVI annotations.</p>
        </p:documentation>
        <p:pipe step="validate-dtbook" port="result"/>
     </p:output>-->

    <p:output port="report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Raw validation report</h1>
            <p px:role="desc" xml:space="preserve">Raw XML-formatted report which is a concatenation of the reports from all types of validation used (RelaxNG, Schematron and custom).

[More details on the file format](http://daisy.github.io/pipeline/ValidationReportXML).</p>
        </p:documentation>
        <p:pipe step="validate-dtbook" port="xml-report"/>
    </p:output>

    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation report</h1>
        </p:documentation>
        <p:pipe step="validate-dtbook" port="html-report" />
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc" xml:space="preserve">An XML document describing, briefly, whether the validation was successful.

[More details on the file format](http://daisy.github.io/pipeline/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe step="validate-dtbook" port="validation-status"/>
    </p:output>

    <!-- we are using a string option instead of an XML input source because
        the wellformedness of the document cannot be taken for granted -->
    <p:option name="input-dtbook" required="true" px:type="anyFileURI" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">The input DTBook document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="false" px:output="result" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validation report in XML and HTML</h2>
            <p px:role="desc" xml:space="preserve">Validation report in two formats: the raw XML-formatted report, which is a concatenation of the reports from all types of validation used (RelaxNG, Schematron and custom), and the HTML-formatted version.

[More details on the XML format](http://daisy.github.io/pipeline/ValidationReportXML).</p>
        </p:documentation>
    </p:option>

    <p:option name="mathml-version" required="false" select="'3.0'">
        <p:pipeinfo>
            <px:type>
                <choice>
                    <value>3.0</value>
                    <value>2.0</value>
                    <value>1.01</value>
                    <value>1.0</value>
                </choice>
            </px:type>
        </p:pipeinfo>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">MathML version</h2>
            <p px:role="desc">Version of MathML in the DTBook file.</p>
        </p:documentation>
    </p:option>

    <p:option name="check-images" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Check that images exist</h2>
            <p px:role="desc">Check to see that referenced images exist on disk.</p>
        </p:documentation>
    </p:option>

    <p:option name="nimas" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validate against NIMAS 1.1</h2>
            <p px:role="desc">Validate using NIMAS 1.1 rules for DTBook.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-validate
        </p:documentation>
    </p:import>
    <p:import href="dtbook-validator.store.xpl">
        <p:documentation>
            pxi:dtbook-validator.store
        </p:documentation>
    </p:import>
    
    <px:message>
        <p:with-option name="message" select="concat('DTBook validator: ', $input-dtbook)"/>
        <p:input port="source">
            <p:empty/>
        </p:input>
    </px:message>
    <p:sink/>
    
    <px:fileset-add-entry media-type="application/x-dtbook+xml">
        <p:with-option name="href" select="$input-dtbook"/>
        <p:input port="source">
            <p:inline>
                <d:fileset/>
            </p:inline>
        </p:input>
    </px:fileset-add-entry>
    
    <px:dtbook-validate name="validate-dtbook">
        <p:with-option name="mathml-version" select="$mathml-version"/>
        <p:with-option name="check-images" select="$check-images"/>
        <p:with-option name="nimas" select="$nimas"/>
    </px:dtbook-validate>
    
    <pxi:dtbook-validator.store>
        <p:input port="xml-report">
            <p:pipe step="validate-dtbook" port="xml-report"/>
        </p:input>
        <p:input port="html-report">
            <p:pipe step="validate-dtbook" port="html-report"/>
        </p:input>
        <p:with-option name="output-dir" select="$output-dir"/>
    </pxi:dtbook-validator.store>
    
</p:declare-step>
