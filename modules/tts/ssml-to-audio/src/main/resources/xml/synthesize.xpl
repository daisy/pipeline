<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" type="px:synthesize"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:input port="source" primary="true" sequence="true" />
    <p:input port="config"/>
    <p:output port="result" primary="true" />
    <p:option name="output-dir"/>

</p:declare-step>