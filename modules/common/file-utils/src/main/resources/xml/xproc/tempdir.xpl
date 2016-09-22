<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:tempdir"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:cx="http://xmlcalabash.com/ns/extensions"
    exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Returns a <code>&lt;c:result></code> containing the absolute URI of a temporary
            directory, which is guaranteed not to already exist. The directory is created inside
            the directory specified by <code>href</code>.</p>
    </p:documentation>
    
    <p:option name="href" required="true"/>
    
    <p:output port="result" primary="true"/>
    
    <p:import href="normalize-uri.xpl"/>
    <p:import href="java-library.xpl"/>
    
    <pxi:normalize-uri name="parent-dir">
        <p:with-option name="href" select="$href"/>
    </pxi:normalize-uri>
    
    <px:mkdir>
        <p:with-option name="href" select="string(/c:result)"/>
    </px:mkdir>
    
    <px:tempfile name="temp-file" suffix="">
        <p:with-option name="href" select="string(/c:result)">
            <p:pipe step="parent-dir" port="result"/>
        </p:with-option>
    </px:tempfile>
    
    <px:delete>
        <p:with-option name="href" select="string(/c:result)">
            <p:pipe step="temp-file" port="result"/>
        </p:with-option>
    </px:delete>
    
    <p:string-replace match="/c:result/text()" name="temp-dir">
        <p:input port="source">
            <p:pipe step="temp-file" port="result"/>
        </p:input>
        <p:with-option name="replace" select="concat('&quot;', /c:result, '/&quot;')">
            <p:pipe step="temp-file" port="result"/>
        </p:with-option>
    </p:string-replace>
    
    <px:mkdir name="mkdir">
        <p:with-option name="href" select="string(/c:result)"/>
    </px:mkdir>
    
    <p:identity cx:depends-on="mkdir">
        <p:input port="source">
            <p:pipe step="temp-dir" port="result"/>
        </p:input>
    </p:identity>
    
</p:declare-step>
