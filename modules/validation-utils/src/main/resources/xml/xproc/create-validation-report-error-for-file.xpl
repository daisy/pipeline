<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="pxi:create-validation-report-error-for-file"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Create d:error element(s) for the given file.</h1>
        <p>Based on the input <code>d:file</code> element, create one or more <code>d:error</code>
        elements.</p>
    </p:documentation>

    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p><code>d:file</code> element</p>
        </p:documentation>
    </p:input>

    <!-- produce one or more d:error elements (one per d:ref in the file) -->
    <p:output port="result" sequence="true">
        <p:pipe port="result" step="create-error"/>
    </p:output>

    <p:option name="error-type" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Type of error.</p>
        </p:documentation>
    </p:option>
    <p:option name="desc" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A description of the error.</p>
        </p:documentation>
    </p:option>
    <p:option name="base" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The base URI for the file.</p>
        </p:documentation>
    </p:option>

    <p:import href="create-validation-report-error.xpl"/>

    <p:variable name="filepath" select="/*/resolve-uri(@href, $base)"/>

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
