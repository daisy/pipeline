<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0" 
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-inline-prefixes="#all"
                name="dtbook-validator.store"
                type="pxi:dtbook-validator.store">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>DTBook Validator: Store</h1>
        <p>Internal step.</p>
    </p:documentation>

    <p:input port="xml-report">
        <p:documentation>
            <p>Validation report XML.</p>
        </p:documentation>
    </p:input>

    <p:input port="html-report">
        <p:documentation>
            <p>Validation report HTML.</p>
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

    <p:choose>
        <!-- save reports if we specified an output dir -->
        <p:when test="string-length($output-dir) > 0">
            <px:message>
                <p:input port="source">
                    <p:empty/>
                </p:input>
                <p:with-option name="message" select="concat('Storing reports to disk in output directory: ', $output-dir)"/>
            </px:message>
            
            <p:store name="store-xml-report">
                <p:input port="source">
                    <p:pipe port="xml-report" step="dtbook-validator.store"/>
                </p:input>
                <p:with-option name="href"
                    select="concat($output-dir,'/dtbook-validation-report.xml')"/>
            </p:store>
            
            <p:store name="store-xhtml-report">
                <p:input port="source">
                    <p:pipe port="html-report" step="dtbook-validator.store"/>
                </p:input>
                <p:with-option name="href"
                    select="concat($output-dir,'/dtbook-validation-report.xhtml')"/>
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
