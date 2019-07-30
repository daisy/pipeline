<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:fileset-add-tempfile" name="fileset-add-tempfile"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:cx="http://xmlcalabash.com/ns/extensions"
    exclude-inline-prefixes="#all"
    version="1.0">
    
    <p:input port="source" sequence="true" primary="true"/>
    <p:input port="directory" sequence="false"/>
    <p:option name="suffix" select="'.xml'"/>
    <p:output port="result" sequence="false" primary="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    
    <px:tempfile delete-on-exit="false" name="tempfile">
        <p:with-option name="href" select="/d:fileset/@xml:base">
            <p:pipe step="fileset-add-tempfile" port="directory"/>
        </p:with-option>
        <p:with-option name="suffix" select="$suffix">
            <p:empty/>
        </p:with-option>
    </px:tempfile>
    
    <p:store method="text">
        <p:input port="source">
            <p:pipe step="fileset-add-tempfile" port="source"/>
        </p:input>
        <p:with-option name="href" select="/c:result">
            <p:pipe step="tempfile" port="result"/>
        </p:with-option>
    </p:store>
    
    <px:fileset-add-entry>
        <p:input port="source">
            <p:pipe step="fileset-add-tempfile" port="directory"/>
        </p:input>
        <p:with-option name="href" select="replace(/c:result, '^.*/', '')">
            <p:pipe step="tempfile" port="result"/>
        </p:with-option>
    </px:fileset-add-entry>
    
</p:declare-step>
