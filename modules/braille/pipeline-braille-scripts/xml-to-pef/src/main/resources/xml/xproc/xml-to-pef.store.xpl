<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xml-to-pef.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:input port="source" primary="true">
        <p:documentation>PEF</p:documentation>
    </p:input>
    <p:input port="obfl" sequence="true"> <!-- sequence=false when include-obfl=true -->
        <p:documentation>OBFL</p:documentation>
    </p:input>
    
    <p:option name="pef-output-dir" select="''"/>
    <p:option name="brf-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    
    <p:option name="name" required="true"/>
    <p:option name="include-preview" select="'false'"/>
    <p:option name="include-brf" select="'false'"/>
    <p:option name="include-obfl" select="'false'"/>
    <p:option name="ascii-file-format" select="''"/>
    <p:option name="ascii-table" select="''"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <pef:store>
        <p:with-option name="href" select="concat($pef-output-dir,'/',$name,'.pef')"/>
        <p:with-option name="preview-href" select="if ($include-preview='true' and $preview-output-dir!='')
                                                   then concat($preview-output-dir,'/',$name,'.pef.html')
                                                   else ''"/>
        <p:with-option name="brf-dir-href" select="if ($include-brf='true' and $brf-output-dir!='')
                                                   then $brf-output-dir
                                                   else ''"/>
        <p:with-option name="brf-name-pattern" select="concat($name,'_vol-{}')"/>
        <p:with-option name="brf-file-format" select="concat($ascii-file-format,'(locale:',(/*/@xml:lang,'und')[1],')')"/>
        <p:with-option name="brf-table" select="if ($ascii-table!='') then $ascii-table
                                                else concat('(locale:',(/*/@xml:lang,'und')[1],')')"/>
    </pef:store>
    
    <!-- ========== -->
    <!-- STORE OBFL -->
    <!-- ========== -->
    <p:choose>
        <p:when test="$include-obfl='true'">
            <p:store>
                <p:input port="source">
                    <p:pipe step="main" port="obfl"/>
                </p:input>
                <p:with-option name="href" select="concat($pef-output-dir,'/',$name,'.obfl')"/>
            </p:store>
        </p:when>
        <p:otherwise>
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
