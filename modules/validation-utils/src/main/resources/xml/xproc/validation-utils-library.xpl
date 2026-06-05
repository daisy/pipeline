<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation>
        <h1>Validation Utilities Library</h1>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Romain Deltour</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:rdeltour@gmail.com">rdeltour@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>
    
    <p:import href="combine-validation-reports.xpl">
        <p:documentation>Utility step that combines many validation reports into one XML document.</p:documentation>
    </p:import>
    
    <p:import href="validation-report-to-html.xpl">
        <p:documentation>Step that converts one or more validation reports (created by the above step) into one HTML report.</p:documentation>
    </p:import>
    
    <p:import href="check-files-exist.xpl">
        <p:documentation>Step that, given a list of files, reports whether each exists on disk or not.</p:documentation>
    </p:import>

    <p:import href="validation-status.xpl">
        <p:documentation>Step that, given one or more validation reports, outputs validation status XML (http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML).</p:documentation>
    </p:import>
    
    <p:import href="check-files-wellformed.xpl">
        <p:documentation>Step that, given a list of files, reports whether each is well-formed XML.</p:documentation>
    </p:import>
    
    <p:import href="create-validation-report-error.xpl">
        <p:documentation>Create an error for use in Validation Report XML.</p:documentation>
    </p:import>
    
    <p:import href="relax-ng-report.xpl">
        <p:documentation>Performs RELAX NG validation, returning two results: the source document (validated, if validation succeeds) and a report of the validation errors (if any).</p:documentation>
    </p:import>
    
    <p:import href="relax-ng-to-schematron.xpl">
        <p:documentation>Extracts embedded Schematron rules from a RELAX NG schema (not in compact syntax).</p:documentation>
    </p:import>
    
    <p:import href="report-errors.xpl"/>
    
</p:library>
