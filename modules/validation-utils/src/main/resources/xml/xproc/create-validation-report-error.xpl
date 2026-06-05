<p:declare-step version="1.0" name="create-validation-report-error" type="pxi:create-validation-report-error"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Create a d:error element</h1>
        <p>Create a d:error element, used in validation reports. See
        http://code.google.com/p/daisy-pipeline/wiki/ValidationReportXML</p>
    </p:documentation>

    <p:option name="error-type" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Type of error.</p>
        </p:documentation>
    </p:option>
    <p:option name="location-href" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The file containing a reference to this error.</p>
        </p:documentation>
    </p:option>
    <p:option name="file-href" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The file where the error itself is.</p>
        </p:documentation>
    </p:option>
    <p:option name="desc" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A description of the error.</p>
        </p:documentation>
    </p:option>
    <p:output port="result"/>

    <!--
        <d:error type="file-not-wellformed">
            <d:desc>File is not well-formed XML</d:desc>
            <d:file>@@</d:file>
            <d:location href="@@"/>
        </d:error>
    -->
    
    <p:identity>
        <p:input port="source">
            <p:inline>
                <d:error type="@@">
                    <d:desc>@@</d:desc>
                    <d:file>@@</d:file>
                    <d:location href="@@"/>
                </d:error>
            </p:inline>
        </p:input>
    </p:identity>
    
    <p:string-replace match="//d:error/@type">
        <p:with-option name="replace" 
            select="concat('&quot;', $error-type, '&quot;')"/>
    </p:string-replace>
    
    <p:string-replace match="//d:desc/text()">
        <p:with-option name="replace"
            select="concat('&quot;', $desc, '&quot;')"/>
    </p:string-replace>
    
    <p:string-replace match="//d:file/text()">
        <p:with-option name="replace"
            select="concat('&quot;', $file-href, '&quot;')"/>
    </p:string-replace>
    
    <p:string-replace match="//d:location/@href">
        <p:with-option name="replace"
            select="concat('&quot;', $location-href, '&quot;')"/>
    </p:string-replace>
    

</p:declare-step>
