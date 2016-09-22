<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="px:file-peek" version="1.0"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" exclude-inline-prefixes="#all">

    <!--
        NOTE:
        A current limitation is that when using base64 encoding and the Java-implementation is not available, offset and length will be "rounded" down and up respectively to the nearest number with a factor of 3.
        So for instance, if you request offset=5 and length=4, then offset will be set to 3 and length will be set to 6, so that all the requested bytes are available in the result.
        The actual offset and length used are available as attributes in the result, for instance:
        
        <c:result xmlns:c="http://www.w3.org/ns/xproc-step" content-type="binary/octet-stream" encoding="base64" offset="3" length="6">bWwgdmVy</c:result>
        
        This might be improved in the future if needed. It is not a problem when using hex encoding (default).
    -->

    <p:option name="href" required="true">
        <p:documentation>URI to the file you want to read bytes from.</p:documentation>
    </p:option>
    <p:option name="offset" required="true">
        <p:documentation>Number of bytes to skip into the file.</p:documentation>
    </p:option>
    <p:option name="length" required="true">
        <p:documentation>Number of bytes to include in the result.</p:documentation>
    </p:option>
    <p:option name="use-base64" select="'false'">
        <p:documentation>By default, the output will be hex-ecoded, which is normally easier to manipulate. If you want base64-encoded output then you can set this to true.</p:documentation>
    </p:option>
    <p:option name="use-java-implementation" select="'true'">
        <p:documentation>If the Java implementation of this step is available but you don't want to use it; set this to false (default 'true').</p:documentation>
    </p:option>

    <p:output port="result"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:declare-step type="pxi:file-peek">
        <p:option name="href" required="true"/>
        <p:option name="offset" required="true"/>
        <p:option name="length" required="true"/>
        <p:output port="result"/>
    </p:declare-step>

    <p:choose>
        <p:when test="p:step-available('pxi:file-peek') and $use-java-implementation = 'true'">
            <pxi:file-peek>
                <p:with-option name="href" select="$href"/>
                <p:with-option name="offset" select="$offset"/>
                <p:with-option name="length" select="$length"/>
            </pxi:file-peek>
            <p:add-attribute match="/*" attribute-name="offset">
                <p:with-option name="attribute-value" select="$offset"/>
            </p:add-attribute>
            <p:add-attribute match="/*" attribute-name="length">
                <p:with-option name="attribute-value" select="$length"/>
            </p:add-attribute>
        </p:when>
        <p:otherwise>
            <p:variable name="offset_3" select="xs:integer($offset) - xs:integer($offset) mod 3"/>
            <p:variable name="offset_base64" select="xs:integer(round($offset_3 * 4 div 3))"/>
            <p:variable name="length_3"
                select="xs:integer($length + $offset - $offset_3) + (if (xs:integer($length + $offset - $offset_3) mod 3 = 0) then 0 else 3 - xs:integer($length + $offset - $offset_3) mod 3)"/>
            <p:variable name="length_base64" select="xs:integer(round($length_3 * 4 div 3))"/>
            <p:identity>
                <p:input port="source">
                    <p:inline>
                        <c:request method="GET" override-content-type="binary/octet-stream"/>
                    </p:inline>
                </p:input>
            </p:identity>
            <p:add-attribute match="c:request" attribute-name="href">
                <p:with-option name="attribute-value" select="$href"/>
            </p:add-attribute>
            <px:message severity="WARN" message="pxi:file-peek is not available; will read and parse entire file using XProc which might cause performance issues for large files: $1">
                <p:with-option name="param1" select="/*/@href"/>
            </px:message>
            <p:http-request/>
            <p:string-replace match="/*/text()">
                <p:with-option name="replace" select="concat('''',replace(substring(/*/replace(text(),'&#xA;',''),$offset_base64 + 1,$length_base64),'(.{76})','$1&#xA;'),'''')"/>
            </p:string-replace>
            <p:add-attribute match="/*" attribute-name="offset">
                <p:with-option name="attribute-value" select="$offset_3"/>
            </p:add-attribute>
            <p:add-attribute match="/*" attribute-name="length">
                <p:with-option name="attribute-value" select="$length_3"/>
            </p:add-attribute>
        </p:otherwise>
    </p:choose>

    <p:choose>
        <p:when test="$use-base64 = 'true'">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:xslt>
                <p:with-param name="offset" select="$offset"/>
                <p:with-param name="length" select="$length"/>
                <p:input port="stylesheet">
                    <p:document href="../xslt/base64-to-hex.xsl"/>
                </p:input>
            </p:xslt>
        </p:otherwise>
    </p:choose>

</p:declare-step>
