<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
           xmlns:dp2="http://www.daisy.org/ns/pipeline/"
           xmlns:obfl="http://www.daisy.org/ns/2011/obfl">

    <p:declare-step type="px:obfl-to-pef" name="main">
        <p:input port="source" sequence="false"/>
        <p:output port="result" sequence="false"/>
        <p:option name="locale" required="true"/>
        <p:option name="mode" required="true"/>
        <p:option name="identifier" required="false" select="''"/>
        <p:input port="parameters" kind="parameter" primary="false"/>

        <p:declare-step type="pxi:obfl-to-pef">
            <p:input port="source" sequence="false"/>
            <p:output port="result" sequence="false"/>
            <p:option name="locale" required="true"/>
            <p:option name="mode" required="true"/>
            <p:option name="identifier" required="false" select="''"/>
            <p:option name="style-type" required="false" select="''"/>
            <p:option name="css-text-transform-definitions" required="false" select="''"/>
            <p:input port="parameters" kind="parameter" primary="false"/>
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLToPEFStep.java
            -->
        </p:declare-step>

        <p:delete match="/obfl:obfl/obfl:meta/dp2:style-type">
            <!-- We don't want this to end up in the PEF. We assume that the value is "text/css" and
                 that this is also what translators understand, meaning that $mode should contain
                 "(input:text-css)". -->
        </p:delete>
        <p:delete match="/obfl:obfl/obfl:meta/dp2:css-text-transform-definitions">
            <!-- We don't want this to end up in the PEF. -->
        </p:delete>

        <pxi:obfl-to-pef>
            <p:with-option name="locale" select="$locale"/>
            <p:with-option name="mode" select="$mode"/>
            <p:with-option name="identifier" select="$identifier"/>
            <p:with-option name="style-type" select="/obfl:obfl/obfl:meta/dp2:style-type[1]">
                <p:pipe step="main" port="source"/>
            </p:with-option>
            <p:with-option name="css-text-transform-definitions" select="/obfl:obfl/obfl:meta/dp2:css-text-transform-definitions">
                <p:pipe step="main" port="source"/>
            </p:with-option>
            <p:input port="parameters">
                <p:pipe step="main" port="parameters"/>
            </p:input>
        </pxi:obfl-to-pef>
    </p:declare-step>

</p:library>
