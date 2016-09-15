<p:declare-step version="1.0" name="combine-validation-reports" type="px:combine-validation-reports"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:l="http://xproc.org/library" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML" xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Combine validation reports</h1>
        <p px:role="desc">Wrap one or more validation reports and optional document data. This
            prepares it for the validation-report-to-html step.</p>
    </p:documentation>

    <p:input port="source" primary="true" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">A validation report</p>
        </p:documentation>
    </p:input>
    <p:option name="document-name" required="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">document-name</h1>
            <p px:role="desc">The name of the document that was validated. Used for display
                purposes.</p>
        </p:documentation>
    </p:option>
    <p:option name="document-type" required="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">document-type</h1>
            <p px:role="desc">The type of the document. Used for display purposes.</p>
        </p:documentation>
    </p:option>
    <p:option name="document-path" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">document-path</h1>
            <p px:role="desc">The full path to the document, if available.</p>
        </p:documentation>
    </p:option>
    <p:option name="report-path" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">report-path</h1>
            <p px:role="desc">The path to the validation report XML, if available.</p>
        </p:documentation>
    </p:option>
    <p:option name="internal-info" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">internal-info</h1>
            <p px:role="desc">A string to stash in the document-info/@internal attribute.</p>
        </p:documentation>
    </p:option>

    <p:output port="result" primary="true"/>
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

    <!-- iterate through the documents on the source port -->
    <p:for-each>
        <p:variable name="root-element-name" select="*/name()"/>
        <p:wrap match="/" wrapper="report" wrapper-prefix="d"
            wrapper-namespace="http://www.daisy.org/ns/pipeline/data"/>

        <p:choose>
            <p:when test="$root-element-name = 'c:errors'">
                <p:add-attribute match="d:report">
                    <p:with-option name="attribute-name" select="'type'"/>
                    <p:with-option name="attribute-value" select="'relaxng'"/>
                </p:add-attribute>
            </p:when>
            <p:when test="$root-element-name = 'd:errors'">
                <p:add-attribute match="d:report">
                    <p:with-option name="attribute-name" select="'type'"/>
                    <p:with-option name="attribute-value" select="'filecheck'"/>
                </p:add-attribute>
            </p:when>
            <p:when test="$root-element-name = 'svrl:schematron-output'">
                <p:add-attribute match="d:report">
                    <p:with-option name="attribute-name" select="'type'"/>
                    <p:with-option name="attribute-value" select="'schematron'"/>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:add-attribute match="d:report">
                    <p:with-option name="attribute-name" select="'type'"/>
                    <p:with-option name="attribute-value" select="'unknown'"/>
                </p:add-attribute>
            </p:otherwise>
        </p:choose>
    </p:for-each>

    <p:wrap-sequence name="combine-reports" wrapper="reports"
        wrapper-namespace="http://www.daisy.org/ns/pipeline/data" wrapper-prefix="d"/>

    <p:insert position="last-child">
        <p:input port="insertion">
            <p:pipe port="result" step="combine-reports"/>
        </p:input>
        <p:input port="source">
            <p:inline>
                <d:document-validation-report>
                    <d:document-info/>
                </d:document-validation-report>
            </p:inline>
        </p:input>
    </p:insert>

    <p:group name="add-document-metadata">
        <p:output port="result"/>
        <p:choose>
            <p:when test="string-length($document-path) > 0">
                <p:insert match="d:document-validation-report/d:document-info"
                    position="first-child">
                    <p:input port="insertion">
                        <p:inline>
                            <d:document-path>@@</d:document-path>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="//d:document-path/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;', $document-path, '&quot;')"/>
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <p:choose>
            <p:when test="string-length($document-type) > 0">
                <p:insert match="d:document-validation-report/d:document-info"
                    position="first-child">
                    <p:input port="insertion">
                        <p:inline>
                            <d:document-type>@@</d:document-type>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="//d:document-type/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;', $document-type, '&quot;')"/>
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <p:choose>
            <p:when test="string-length($document-name) > 0">
                <p:insert match="d:document-validation-report/d:document-info"
                    position="first-child">
                    <p:input port="insertion">
                        <p:inline>
                            <d:document-name>@@</d:document-name>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="//d:document-name/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;', $document-name, '&quot;')"/>
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <p:choose>
            <p:when test="string-length($report-path) > 0">
                <p:insert match="d:document-validation-report/d:document-info" position="last-child">
                    <p:input port="insertion">
                        <p:inline>
                            <d:report-path>@@</d:report-path>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="//d:report-path/text()">
                    <p:with-option name="replace" select="concat('&quot;', $report-path, '&quot;')"
                    />
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <p:choose>
            <p:when test="string-length($internal-info) > 0">
                <p:add-attribute match="d:document-validation-report/d:document-info">
                    <p:with-option name="attribute-name" select="'internal'"/>
                    <p:with-option name="attribute-value" select="$internal-info"/>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:group>

    <p:choose>
        <p:when test="//c:errors">

            <!-- replace RelaxNG's c:error elements with our own d:error elements. This reduces the number of types of error descriptions we have to deal with. -->
            <p:group name="replace-cerror-with-derror">
                <!-- convert c:errors to d:errors -->
                <p:xslt name="cerror-to-derror-xsl">
                    <p:input port="stylesheet">
                        <p:document href="../xslt/cerrors-to-derrors.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                    <p:input port="source" select="//c:errors"/>
                </p:xslt>

                <!-- replace c:errors with the results of the conversion -->
                <p:replace match="//c:errors">
                    <p:input port="replacement">
                        <p:pipe port="result" step="cerror-to-derror-xsl"/>
                    </p:input>
                    <p:input port="source">
                        <p:pipe port="result" step="add-document-metadata"/>
                    </p:input>
                </p:replace>
            </p:group>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>


    <p:group name="add-error-count">
        <p:variable name="error-count"
            select="count(//d:error) + count(//svrl:failed-assert) + count(//svrl:successful-report)"/>
        <p:insert match="d:document-validation-report/d:document-info" position="last-child">
            <p:input port="insertion">
                <p:inline>
                    <d:error-count>@@</d:error-count>
                </p:inline>
            </p:input>
        </p:insert>
        <p:string-replace match="//d:error-count/text()">
            <p:with-option name="replace" select="concat('&quot;', $error-count, '&quot;')"/>
        </p:string-replace>
    </p:group>

    <p:validate-with-relax-ng assert-valid="true">
        <p:input port="schema">
            <p:document href="../schema/document-validation-report.rng"/>
        </p:input>
    </p:validate-with-relax-ng>

</p:declare-step>
