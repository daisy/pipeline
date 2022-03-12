<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:delete-parameters"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Delete parameters from a c:param-set document.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            A c:param-set document.
        </p:documentation>
    </p:input>
    
    <p:option name="parameter-names" required="true">
        <p:documentation>
            The names of the parameters to remove.
        </p:documentation>
    </p:option>
    
    <p:option name="parameter-namespace" select="''">
        <p:documentation>
            The namespace of the parameters to remove, or '' for no namespace. No namespace is the
            default.
        </p:documentation>
    </p:option>
    
    <p:output port="result">
        <p:documentation>
            A c:param-set document with the specified parameters removed.
        </p:documentation>
    </p:output>
    
    <p:delete>
        <p:with-option name="match"
                       select="concat('c:param[string(@namespace)=&quot;',$parameter-namespace,'&quot;',
                                      'and @name=tokenize(&quot;',$parameter-names,'&quot;,&quot;\s+&quot;)]')"/>
    </p:delete>
    
</p:declare-step>
