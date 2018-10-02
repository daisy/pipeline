<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xml-to-pef.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:input port="pef" primary="true">
        <p:documentation>PEF</p:documentation>
        <p:empty/>
    </p:input>
    <p:input port="obfl">
        <p:documentation>OBFL</p:documentation>
        <p:empty/>
    </p:input>
    
    <p:option name="pef-output-dir" select="''"/>
    <p:option name="brf-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    
    <p:option name="name" required="true"/>
    <p:option name="include-preview" select="'false'"/>
    <p:option name="include-brf" select="'false'"/>
    <p:option name="ascii-file-format" select="''"/>
    <p:option name="ascii-table" select="''"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <p:count/>
    <p:choose>
        <p:when test="number(string(/*)) &gt; 0"> <!-- must be 0 or 1 -->
            <pef:store name="pef-store">
                <p:input port="source">
                    <p:pipe step="main" port="pef"/>
                </p:input>
                <p:with-option name="href" select="concat($pef-output-dir,'/',$name,'.pef')"/>
                <p:with-option name="preview-href" select="if ($include-preview='true' and $preview-output-dir!='')
                                                           then concat($preview-output-dir,'/',$name,'.pef.html')
                                                           else ''"/>
                <p:with-option name="brf-dir-href" select="if ($include-brf='true' and $brf-output-dir!='')
                                                           then $brf-output-dir
                                                           else ''"/>
                <p:with-option name="brf-name-pattern" select="concat($name,'_vol-{}')"/>
                <p:with-option name="brf-file-format" select="concat($ascii-file-format,'(locale:',(//pef:meta/dc:language,'und')[1],')')">
                    <p:pipe step="main" port="pef"/>
                </p:with-option>
                <p:with-option name="brf-table" select="if ($ascii-table!='') then $ascii-table
                                                        else concat('(locale:',(//pef:meta/dc:language,'und')[1],')')">
                    <p:pipe step="main" port="pef"/>
                </p:with-option>
            </pef:store>
        </p:when>
        <p:otherwise>
            <p:sink/>
        </p:otherwise>
    </p:choose>
    
    <!-- ========== -->
    <!-- STORE OBFL -->
    <!-- ========== -->
    <p:count>
        <p:input port="source">
            <p:pipe step="main" port="obfl"/>
        </p:input>
    </p:count>
    <p:choose>
        <p:when test="number(string(/*)) &gt; 0"> <!-- must be 0 or 1 -->
            <p:store encoding="utf-8" omit-xml-declaration="false">
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
