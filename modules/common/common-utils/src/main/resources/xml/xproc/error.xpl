<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:error" name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Example usage:</p>
        <pre xml:space="preserve">
            &lt;px:error code="pxe:PEOU0001" message="URI must be absolute and refer to the local file system: $1"&gt;
                &lt;p:with-param name="param1" select="$href"/&gt;
            &lt;/px:error&gt;
        </pre>
    </p:documentation>
    
    <p:input port="error" primary="false" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>If a <code>c:errors</code> document is supplied on this port, the errors will be
            reported and the last error will be raised.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:output port="result" sequence="true">
        <p:pipe port="result" step="error"/>
    </p:output>
    
    <p:option name="code" select="''" cx:as="xs:string">     <!-- QName -->
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            Must be set if there is a document on the "error" port. Must not be set if there is no
            document on the "error" port.
        </p:documentation>
    </p:option>
    <p:option name="code-prefix" select="''"/>                <!-- NCName -->
    <p:option name="code-namespace" select="''"/>             <!-- anyURI -->
    <p:option name="message" select="''" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            Description of the error that occured. $1, $2 etc will be replaced with the contents of
            optios "param1", "param2", etc. Must be set if there is a document on the "error"
            port. Must not be set if there is no document on the "error" port.
        </p:documentation>
    </p:option>
    <p:option name="param1" select="''" cx:as="xs:string"/>
    <p:option name="param2" select="''" cx:as="xs:string"/>
    <p:option name="param3" select="''" cx:as="xs:string"/>
    <p:option name="param4" select="''" cx:as="xs:string"/>
    <p:option name="param5" select="''" cx:as="xs:string"/>
    <p:option name="param6" select="''" cx:as="xs:string"/>
    <p:option name="param7" select="''" cx:as="xs:string"/>
    <p:option name="param8" select="''" cx:as="xs:string"/>
    <p:option name="param9" select="''" cx:as="xs:string"/>
    <!-- in the unlikely event that you need more parameters you'll have to format the message string yourself -->
    
    <p:declare-step type="pxi:error">
        <p:input port="error" sequence="false">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>A <code>c:errors</code> document.</p>
            </p:documentation>
        </p:input>
        <p:output port="result" sequence="true"/>
    </p:declare-step>

    <p:count>
        <p:input port="source">
            <p:pipe step="main" port="error"/>
        </p:input>
    </p:count>
    <p:choose>
        <p:when test="number(/*)&gt;1">
            <p:error code="XXX">
                <p:input port="source">
                    <p:inline><message>Not more than one document allowed on the "error" port.</message></p:inline>
                </p:input>
            </p:error>
        </p:when>
        <p:when test="number(/*)=1">
            <p:choose>
                <p:when test="$message!=''">
                    <p:error code="XXX">
                        <p:input port="source">
                            <p:inline><message>"message" option must not be set if there is a document on the "error" port.</message></p:inline>
                        </p:input>
                    </p:error>
                </p:when>
                <p:when test="$code!=''">
                    <p:error code="XXX">
                        <p:input port="source">
                            <p:inline><message>"code" option must not be set if there is a document on the "error" port.</message></p:inline>
                        </p:input>
                    </p:error>
                </p:when>
                <p:otherwise>
                    <pxi:error>
                        <p:input port="error">
                            <p:pipe step="main" port="error"/>
                        </p:input>
                    </pxi:error>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:when test="$message=''">
            <p:error code="XXX">
                <p:input port="source">
                    <p:inline><message>"message" option must be set</message></p:inline>
                </p:input>
            </p:error>
        </p:when>
        <p:when test="$code=''">
            <p:error code="XXX">
                <p:input port="source">
                    <p:inline><message>"code" option must be set</message></p:inline>
                </p:input>
            </p:error>
        </p:when>
        <p:otherwise>
            <p:variable name="code-localName" select="if (contains($code,':')) then substring-after($code,':') else $code"/>
            <p:variable name="prefix" select="concat(substring-before($code,':'),$code-prefix)"/>
            <p:string-replace match="/*/text()" name="message">
                <p:input port="source">
                    <p:inline><message>MESSAGE</message></p:inline>
                </p:input>
                <p:with-option name="replace"
                               select="concat('&quot;',
                                              replace(replace(replace(replace(replace(replace(replace(replace(replace(
                                                $message,
                                                '\$1',replace($param1,'\$','\\\$')),
                                                '\$2',replace($param2,'\$','\\\$')),
                                                '\$3',replace($param3,'\$','\\\$')),
                                                '\$4',replace($param4,'\$','\\\$')),
                                                '\$5',replace($param5,'\$','\\\$')),
                                                '\$6',replace($param6,'\$','\\\$')),
                                                '\$7',replace($param7,'\$','\\\$')),
                                                '\$8',replace($param8,'\$','\\\$')),
                                                '\$9',replace($param9,'\$','\\\$')),
                                                '&quot;')"/>
            </p:string-replace>
            <p:choose>
                <p:when test="not($code-namespace='') and not($prefix='')">
                    <p:error>
                        <p:input port="source">
                            <p:pipe port="result" step="message"/>
                        </p:input>
                        <p:with-option name="code" select="$code-localName"/>
                        <p:with-option name="code-namespace" select="$code-namespace"/>
                        <p:with-option name="code-prefix" select="$prefix"/>
                    </p:error>
                </p:when>
                <p:when test="not($code-namespace='')">
                    <p:error>
                        <p:input port="source">
                            <p:pipe port="result" step="message"/>
                        </p:input>
                        <p:with-option name="code" select="$code-localName"/>
                        <p:with-option name="code-namespace" select="$code-namespace"/>
                    </p:error>
                </p:when>
                <p:when test="not($prefix='')">
                    <p:error>
                        <p:input port="source">
                            <p:pipe port="result" step="message"/>
                        </p:input>
                        <p:with-option name="code" select="$code-localName"/>
                        <p:with-option name="code-prefix" select="$prefix"/>
                    </p:error>
                </p:when>
                <p:otherwise>
                    <p:error>
                        <p:input port="source">
                            <p:pipe port="result" step="message"/>
                        </p:input>
                        <p:with-option name="code" select="$code-localName"/>
                    </p:error>
                </p:otherwise>
            </p:choose>
        </p:otherwise>
    </p:choose>
    <p:identity name="error"/>
    
</p:declare-step>
