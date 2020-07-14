<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:catch-xslt-terminate-error" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data">
    
    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <p:inline>
            <d:status result="error"/>
        </p:inline>
    </p:output>
    
    <p:output port="result" primary="true"/>
    
    <p:try>
        <p:group>
            <p:xslt template-name="start">
                <p:input port="source">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:inline>
                        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                            <xsl:template name="start">
                                <xsl:message terminate="yes">Runtime Error</xsl:message>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:group>
        <p:catch name="catch">
            <p:identity>
                <p:input port="source">
                    <p:pipe step="catch" port="error"/>
                </p:input>
            </p:identity>
        </p:catch>
    </p:try>
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">catch-xslt-terminate-error</h1>
        <p px:role="desc">catch-xslt-terminate-error</p>
    </p:documentation>
    
</p:declare-step>

