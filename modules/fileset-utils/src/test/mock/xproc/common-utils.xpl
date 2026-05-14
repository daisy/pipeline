<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0">
    
    <p:declare-step type="px:message" name="main" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:x="http://www.emc.com/documentum/xml/xproc"
        xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" version="1.0">
        
        <p:input port="source" primary="true" sequence="true"/>
        <p:output port="result" sequence="true"/>
        <p:option name="severity" select="'INFO'"/>
        <p:option name="message" required="true"/>
        <p:option name="param1" select="''"/>
        <p:option name="param2" select="''"/>
        <p:option name="param3" select="''"/>
        <p:option name="param4" select="''"/>
        <p:option name="param5" select="''"/>
        <p:option name="param6" select="''"/>
        <p:option name="param7" select="''"/>
        <p:option name="param8" select="''"/>
        <p:option name="param9" select="''"/>
        
        <p:identity/>
    </p:declare-step>
    
    <p:declare-step type="px:error" name="main" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:x="http://www.emc.com/documentum/xml/xproc"
        xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" version="1.0">
        
        <p:input port="source" primary="true" sequence="true"/>
        <p:output port="result" sequence="true"/>
        <p:option name="code" required="true"/>
        <p:option name="message" required="true"/>
        <p:option name="param1" select="''"/>
        <p:option name="param2" select="''"/>
        <p:option name="param3" select="''"/>
        <p:option name="param4" select="''"/>
        <p:option name="param5" select="''"/>
        <p:option name="param6" select="''"/>
        <p:option name="param7" select="''"/>
        <p:option name="param8" select="''"/>
        <p:option name="param9" select="''"/>
        
        <p:identity/>
    </p:declare-step>
    
    <p:declare-step type="px:assert" name="main" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:x="http://www.emc.com/documentum/xml/xproc"
        xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" version="1.0">
        
        <p:input port="source" primary="true" sequence="true"/>
        <p:output port="result" sequence="true"/>
        <p:option name="test" select="''"/>
        <p:option name="test-count-min" select="''"/>
        <p:option name="test-count-max" select="''"/>
        <p:option name="error-code" select="''"/>
        <p:option name="message" required="true"/>
        <p:option name="param1" select="''"/>
        <p:option name="param2" select="''"/>
        <p:option name="param3" select="''"/>
        <p:option name="param4" select="''"/>
        <p:option name="param5" select="''"/>
        <p:option name="param6" select="''"/>
        <p:option name="param7" select="''"/>
        <p:option name="param8" select="''"/>
        <p:option name="param9" select="''"/>
        
        <p:identity/>
    </p:declare-step>
    
</p:library>
