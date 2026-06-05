<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0">

    <p:import href="upgrade-smil.xpl"/>
    <p:import href="downgrade-smil.xpl"/>
    <p:import href="smil-to-text-fileset.xpl"/>
    <p:import href="smil-to-audio-fileset.xpl"/>
    <p:import href="smil-to-audio-clips.xpl"/>
    <p:import href="smil-update-links.xpl"/>

    <!--
        Steps specific to EPUB 3
    -->
    <p:import href="join.xpl"/>
    <p:import href="rearrange.xpl"/>

    <!--
        Steps related to the internal d:audio-clips format that is a simplified version of SMIL.
    -->
    <p:import href="audio-clips-to-fileset.xpl"/>
    <p:import href="audio-clips-update-files.xpl"/>

</p:library>
