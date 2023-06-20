<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:nimas-fileset-validator.script"
                px:input-filesets="nimas dtbook daisy3"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">NIMAS Fileset Validator</h1>
        <p px:role="desc">Validate a NIMAS Fileset. Supports inclusion of MathML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/nimas-fileset-validator/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Marisa DeMeglio</dd>
                <dt>E-mail:</dt>
                <dd><a href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <p:output port="html-report" primary="true" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation report</h1>
            <p px:role="desc">Validation report comprising all documents' reports.</p>
        </p:documentation>
        <p:pipe step="validate-nimas-fileset" port="html-report"/>
    </p:output>

    <p:output port="package-doc-validation-report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Package Document XML Report</h1>
            <p px:role="desc">Raw validation output for the package document.</p>
        </p:documentation>
        <p:pipe step="validate-nimas-fileset" port="package-doc-validation-report"/>
    </p:output>

    <p:output port="dtbook-validation-report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">DTBook XML Report</h1>
            <p px:role="desc">Raw validation output for the DTBook file(s).</p>
        </p:documentation>
        <p:pipe step="validate-nimas-fileset" port="dtbook-validation-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <!-- whether the validation was successful -->
        <p:pipe step="validate-nimas-fileset" port="validation-status"/>
    </p:output>

    <!-- we are using a string option instead of an XML input source because
        the wellformedness of the document cannot be taken for granted -->
    <p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input NIMAS fileset</h2>
            <p px:role="desc">The package document (*.opf) of the input NIMAS fileset.</p>
        </p:documentation>
    </p:option>

    <p:option name="result" required="false" px:output="result" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validation reports</h2>
            <p px:role="desc">All validation reports.</p>
        </p:documentation>
    </p:option>

    <p:option name="mathml-version" select="'3.0'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="check-images" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Check that images exist</h2>
            <p px:role="desc">Check to see that images referenced by DTBook file(s) exist on
                disk.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="nimas-fileset-validator.validate.xpl">
        <p:documentation>
            px:nimas-fileset-validator
        </p:documentation>
    </p:import>
    <p:import href="nimas-fileset-validator.store.xpl">
        <p:documentation>
            pxi:nimas-fileset-validator.store
        </p:documentation>
    </p:import>

    <px:fileset-add-entry media-type="application/oebps-package+xml">
        <p:with-option name="href" select="$source"/>
        <p:input port="source.fileset">
            <p:inline><d:fileset/></p:inline>
        </p:input>
    </px:fileset-add-entry>

    <px:nimas-fileset-validator name="validate-nimas-fileset">
        <p:with-option name="mathml-version" select="$mathml-version"/>
        <p:with-option name="check-images" select="$check-images='true'"/>
        <p:with-option name="base-uri" select="$source"/>
    </px:nimas-fileset-validator>

    <pxi:nimas-fileset-validator.store>
        <p:input port="html-report">
            <p:pipe step="validate-nimas-fileset" port="html-report"/>
        </p:input>
        <p:input port="xml-reports">
            <p:pipe step="validate-nimas-fileset" port="package-doc-validation-report"/>
            <p:pipe step="validate-nimas-fileset" port="dtbook-validation-report"/>
        </p:input>
        <p:with-option name="output-dir" select="$result"/>
    </pxi:nimas-fileset-validator.store>

</p:declare-step>
