<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:cx="http://xmlcalabash.com/ns/extensions" type="px:nordic-fail-on-error-status" name="main" version="1.0">
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:option name="fail-on-error" required="true"/>
    <p:option name="output-dir" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <p:choose>
        <p:when test="$fail-on-error = 'false'">
            <!-- validation status might indicate error even though conversion was successful; check whether there are files in the output directory instead -->
            <p:try>
                <p:group>
                    <px:fileset-from-dir>
                        <p:with-option name="path" select="$output-dir"/>
                    </px:fileset-from-dir>
                    <p:choose>
                        <p:when test="//@href[ends-with(.,'.xml') or ends-with(.,'.xhtml') or ends-with(.,'.epub')]">
                            <p:identity>
                                <p:input port="source">
                                    <p:inline exclude-inline-prefixes="#all">
                                        <d:validation-status result="ok"/>
                                    </p:inline>
                                </p:input>
                            </p:identity>
                        </p:when>
                        <p:otherwise>
                            <p:identity>
                                <p:input port="source">
                                    <p:inline exclude-inline-prefixes="#all">
                                        <d:validation-status result="error"/>
                                    </p:inline>
                                </p:input>
                            </p:identity>
                        </p:otherwise>
                    </p:choose>
                </p:group>
                <p:catch>
                    <p:identity>
                        <p:input port="source">
                            <p:inline exclude-inline-prefixes="#all">
                                <d:validation-status result="error"/>
                            </p:inline>
                        </p:input>
                    </p:identity>
                </p:catch>
            </p:try>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
