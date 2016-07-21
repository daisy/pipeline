<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pef:pef2text"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                version="1.0">
    
    <p:input port="source" sequence="false" primary="true"/>
    <p:option name="dir-href" required="true"/>
    <p:option name="file-format" required="false"/> <!-- query format -->
    <p:option name="table" required="false"/> <!-- query format -->
    <p:option name="line-breaks" required="false"/>
    <p:option name="page-breaks" required="false"/>
    <p:option name="pad" required="false"/>
    <!-- the name for a single volume -->
    <p:option name="single-volume-name" required="false"/>
    <!--
        the name for multiple volumes, {} is replaced by the volume number
        if empty, then the PEF is not split
    -->
    <p:option name="name-pattern" required="false"/>
    <!--
        the width of the volume number,
        if 0, then the volume number is not padded with zeroes
    -->
    <p:option name="number-width" required="false"/>
    
</p:declare-step>
