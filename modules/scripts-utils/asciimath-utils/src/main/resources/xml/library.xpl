<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:declare-step type="px:asciimath-to-mathml">
        <p:option name="asciimath" required="true"/>
        <p:output port="result" sequence="false" primary="true"/>
        <!--
            implemented in ../../../java/org/daisy/pipeline/asciimathml/calabash/impl/ASCIIMathMLProvider.java
        -->
    </p:declare-step>

</p:library>

