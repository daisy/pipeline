<p:declare-step version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline"
            xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:c="http://www.w3.org/ns/xproc-step"
            xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
            type="pxd:htmlize-xproc"
            name="main"
            exclude-inline-prefixes="#all">
    
    <p:input port="sources" sequence="true"/>
    <p:input port="parameters" kind="parameter"/>
    <p:option name="input-base-uri" required="true"/>
    <p:option name="output-base-uri" required="true"/>
    
    <p:load name="catalog-xml">
        <p:with-option name="href" select="//c:param[@name='catalog-xml-uri']/@value">
            <p:pipe step="main" port="parameters"/>
        </p:with-option>
    </p:load>
    <p:sink/>
    
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="main" port="sources"/>
        </p:iteration-source>
        <p:variable name="input-uri" select="base-uri(/*)"/>
        <p:choose>
            <p:xpath-context>
                <p:pipe step="catalog-xml" port="result"/>
            </p:xpath-context>
            <p:when test="exists(//cat:uri[@px:content-type='script' and resolve-uri(@uri,base-uri(.))=$input-uri])">
                <p:output port="result" primary="true">
                    <p:pipe step="xslt" port="secondary"/>
                </p:output>
                <p:xslt name="xslt">
                    <p:input port="stylesheet">
                        <p:document href="htmlize-xproc-script.xsl"/>
                    </p:input>
                    <p:with-param name="input-base-uri" select="$input-base-uri"/>
                    <p:with-param name="output-base-uri" select="$output-base-uri"/>
                    <p:input port="parameters">
                        <p:pipe step="main" port="parameters"/>
                    </p:input>
                </p:xslt>
                <p:sink/>
            </p:when>
            <p:otherwise>
                <p:output port="result" primary="true">
                    <p:pipe step="xslt" port="secondary"/>
                </p:output>
                <p:xslt name="xslt">
                    <p:input port="stylesheet">
                        <p:document href="htmlize-xproc.xsl"/>
                    </p:input>
                    <p:with-param name="input-base-uri" select="$input-base-uri"/>
                    <p:with-param name="output-base-uri" select="$output-base-uri"/>
                    <p:input port="parameters">
                        <p:pipe step="main" port="parameters"/>
                    </p:input>
                </p:xslt>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    
    <p:for-each>
        <p:store>
            <p:with-option name="href" select="p:base-uri()"/>
        </p:store>
    </p:for-each>
    
</p:declare-step>
