<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0">

    <!--
        Added here and not in common-utils because that would introduce a cyclic dependency between
        common-utils and css-utils
    -->
    <p:import href="apply-stylesheets.xpl"/>
    
    <p:import href="xml-to-pef.store.xpl"/>
    
</p:library>
