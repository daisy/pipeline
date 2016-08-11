<?xml version="1.0" encoding="UTF-8"?>
<p:library version="1.0"
           xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:css="http://www.daisy.org/ns/pipeline/braille-css">
    
    <p:declare-step type="css:inline">
        <p:input port="source" sequence="false"/>
        <p:input kind="parameter" port="sass-variables" primary="false"/>
        <p:output port="result" sequence="false"/>
        <p:option name="default-stylesheet" required="false"/>
        <p:option name="media" required="false" select="'embossed'"/>
        <p:option name="attribute-name" required="false" select="'style'"/>
    </p:declare-step>
    
</p:library>
