<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="pxi:nimas-fileset-validator.store"
                name="nimas-fileset-validator.store"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>NIMAS Fileset Validator: Store</h1>
        <p>Internal step for NIMAS fileset validator.</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->
    <p:input port="html-report" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>An HTML report.</p>
        </p:documentation>
    </p:input>
    
    <p:input port="xml-reports" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>One or more xml reports.</p>
        </p:documentation>
    </p:input>
    
    <p:option name="output-dir" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Directory where the validation reports are stored. If left blank,
                nothing is saved to disk.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    
    <!-- ***************************************************** -->
    <!-- STORE REPORTS -->
    <!-- ***************************************************** -->
    
    <p:choose>
        <!-- save reports if we specified an output dir -->
        <p:when test="string-length($output-dir) > 0">
            <px:message>
                <p:with-option name="message"
                    select="concat('Nimas fileset validator: Storing reports to disk in output directory: ', $output-dir)"
                />
            </px:message>
            <p:sink/>
            
            <p:for-each>
                <p:iteration-source>
                    <p:pipe port="xml-reports" step="nimas-fileset-validator.store"/>
                </p:iteration-source>
                <p:variable name="report-filename"
                    select="/d:document-validation-report/d:document-info/d:report-path/text()"/>
                <p:store name="store-report">
                    <p:with-option name="href" select="concat($output-dir,'/', $report-filename)"/>
                </p:store>
            </p:for-each>
            
            <p:store name="store-html-report">
                <p:input port="source">
                    <p:pipe port="html-report" step="nimas-fileset-validator.store"/>
                </p:input>
                <p:with-option name="href" select="concat($output-dir,'/validation-report.xhtml')"/>
            </p:store>
        </p:when>
        <p:otherwise>
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>
    
    
</p:declare-step>
