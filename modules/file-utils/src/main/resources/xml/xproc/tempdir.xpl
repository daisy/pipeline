<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                type="px:tempdir">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Returns a <code>&lt;c:result></code> containing the absolute URI of a temporary
        directory, which is guaranteed not to already exist. The directory is created inside the
        directory specified by <code>href</code>. If <code>href</code> is not specified then the
        system-dependent default temporary-file directory will be used.</p>
    </p:documentation>
    
    <p:option name="href" required="false"/>
    <p:option name="delete-on-exit" required="false" select="'false'"/>
    
    <p:output port="result"/>
    
    <p:import href="normalize-uri.xpl">
        <p:documentation>
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="java-library.xpl">
        <p:documentation>
            px:tempfile
            px:delete
            px:mkdir
        </p:documentation>
    </p:import>
    
    <p:choose>
        <p:when test="p:value-available('href')">
            <p:output port="result">
                <p:pipe step="temp-file" port="result"/>
            </p:output>
            <px:normalize-uri name="normalize">
                <p:with-option name="href" select="if (not(ends-with($href,'/'))) then concat($href,'/') else $href"/>
            </px:normalize-uri>
            <px:mkdir name="make-parent-dir">
                <p:with-option name="href" select="string(/c:result)">
                    <p:pipe step="normalize" port="normalized"/>
                </p:with-option>
            </px:mkdir>
            <px:tempfile name="temp-file" suffix="">
                <p:with-option name="href" select="string(/c:result)">
                    <p:pipe step="make-parent-dir" port="result"/>
                </p:with-option>
                <p:with-option name="delete-on-exit" select="$delete-on-exit"/>
            </px:tempfile>
        </p:when>
        <p:otherwise>
            <p:output port="result">
                <p:pipe step="temp-file" port="result"/>
            </p:output>
            <px:tempfile name="temp-file" suffix="">
                <p:with-option name="delete-on-exit" select="$delete-on-exit"/>
            </px:tempfile>
        </p:otherwise>
    </p:choose>
    
    <px:delete name="delete-file">
        <p:with-option name="href" select="string(/c:result)"/>
    </px:delete>
    
    <p:string-replace match="/c:result/text()" name="temp-dir">
        <p:input port="source">
            <p:pipe step="delete-file" port="result"/>
        </p:input>
        <p:with-option name="replace" select="concat('&quot;', /c:result, '/&quot;')">
            <p:pipe step="delete-file" port="result"/>
        </p:with-option>
    </p:string-replace>
    
    <px:mkdir name="make-dir">
        <p:with-option name="href" select="string(/c:result)"/>
    </px:mkdir>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="make-dir" port="result"/>
        </p:input>
    </p:identity>
    
</p:declare-step>
