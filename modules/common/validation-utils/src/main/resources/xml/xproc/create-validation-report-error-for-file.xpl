<p:declare-step version="1.0" name="create-validation-report-error-for-file"
    type="pxi:create-validation-report-error-for-file" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Create d:error element(s) for the given file.</h1>
        <p px:role="desc">Based on the input d:file element (from
            http://code.google.com/p/daisy-pipeline/wiki/FileSetUtils), create one or more d:error
            elements, to be used in validation reports (http://code.google.com/p/daisy-pipeline/wiki/ValidationReportXML).</p>
    </p:documentation>

    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">d:file element. See
                http://code.google.com/p/daisy-pipeline/wiki/FileSetUtils.</p>
        </p:documentation>
    </p:input>

    <!-- produce one or more d:error elements (one per d:ref in the file) -->
    <p:output port="result" sequence="true">
        <p:pipe port="result" step="create-error"/>
    </p:output>

    <p:option name="error-type" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">error-type</h1>
            <p px:role="desc">Type of error.</p>
        </p:documentation>
    </p:option>
    <p:option name="desc" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">desc</h1>
            <p px:role="desc">A description of the error.</p>
        </p:documentation>
    </p:option>
    <p:option name="base" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">base</h1>
            <p px:role="desc">The base URI for the file.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl">
        <p:documentation>Calabash extension steps.</p:documentation>
    </p:import>

    <p:import href="create-validation-report-error.xpl"/>

    <p:variable name="filepath" select="resolve-uri(*/@href, $base)"/>

    <p:choose name="create-error">
        <p:when test="*/d:ref">
            <p:output port="result" sequence="true"/>
            <p:for-each name="iterate-refs">
                <p:iteration-source select="*/d:ref"/>
                <pxi:create-validation-report-error name="create-validation-report-error">
                    <p:with-option name="error-type" select="$error-type"/>
                    <p:with-option name="location-href" select="resolve-uri(*/@href, $filepath)"/>
                    <p:with-option name="file-href" select="$filepath"/>
                    <p:with-option name="desc" select="$desc"/>
                </pxi:create-validation-report-error>
            </p:for-each>
        </p:when>
        <p:otherwise>
            <p:output port="result" sequence="true"/>
            <pxi:create-validation-report-error>
                <p:with-option name="error-type" select="$error-type"/>
                <p:with-option name="file-href" select="$filepath"/>
                <p:with-option name="desc" select="$desc"/>
            </pxi:create-validation-report-error>
        </p:otherwise>
    </p:choose>

</p:declare-step>
