<?xml version="1.0" encoding="UTF-8"?>
<!-- ========================================================================= -->
<!-- There are 4 copies of this file:                                          -->
<!-- * scripts/dtbook-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl -->
<!-- * scripts/html-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl   -->
<!-- * scripts/epub3-to-pef/src/main/resources/xml/xproc/xml-to-pef.store.xpl  -->
<!-- * scripts/zedai-to-pef/src/main/resources/xml/xml-to-pef.store.xpl        -->
<!-- Whenever you update this file, also update the other copies.              -->
<!-- ========================================================================= -->
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
    
    <p:option name="output-dir" select="''"/>
    <p:option name="pef-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    <p:option name="obfl-output-dir" select="''"/>
    
    <p:option name="name" required="true"/>
    <p:option name="include-preview" select="'false'"/>
    <p:option name="include-pef" select="'false'"/>
    <p:option name="output-file-format" select="''"/>
    <p:option name="preview-table" select="''"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>

    <!-- store OBFL first so that if something goes wrong in px:pef-store we still have OBFL -->
    <p:group>
        <p:documentation>
            Store OBFL
        </p:documentation>
        <p:count>
            <p:input port="source">
                <p:pipe step="main" port="obfl"/>
            </p:input>
        </p:count>
        <p:choose px:progress=".50">
            <p:when px:message="Storing OBFL"
                    test="$obfl-output-dir!='' and number(string(/*)) &gt; 0"> <!-- must be 0 or 1 -->
                <p:store encoding="utf-8" omit-xml-declaration="false">
                    <p:input port="source">
                        <p:pipe step="main" port="obfl"/>
                    </p:input>
                    <p:with-option name="href" select="concat($obfl-output-dir,'/',$name,'.obfl')"/>
                </p:store>
            </p:when>
            <p:otherwise>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:group>
    
    <p:group>
        <p:documentation>
            Store PEF
        </p:documentation>
        <p:count>
            <p:input port="source">
                <p:pipe step="main" port="pef"/>
            </p:input>
        </p:count>
        <p:choose px:progress=".50">
            <p:when test="number(string(/*)) &gt; 0"> <!-- must be 0 or 1 -->
                <px:pef-store>
                    <p:input port="source">
                        <p:pipe step="main" port="pef"/>
                    </p:input>
                    <p:with-option name="pef-href" select="if ($include-pef='true' and $pef-output-dir!='')
                                                           then concat($pef-output-dir,'/',$name,'.pef')
                                                           else ''"/>
                    <p:with-option name="preview-href" select="if ($include-preview='true' and $preview-output-dir!='')
                                                               then concat($preview-output-dir,'/',$name,'.pef.html')
                                                               else ''"/>
                    <p:with-option name="output-dir" select="$output-dir"/>
                    <p:with-option name="name-pattern" select="concat($name,'_vol-{}')"/>
                    <p:with-option name="single-volume-name" select="$name"/>
                    <p:with-option name="file-format" select="concat(($output-file-format,'(format:pef)')[not(.='')][1],
                                                                     '(document-locale:',(//pef:meta/dc:language,'und')[1],')')">
                        <p:pipe step="main" port="pef"/>
                    </p:with-option>
                    <p:with-option name="preview-table" select="if ($preview-table!='') then $preview-table
                                                                else concat('(document-locale:',(//pef:meta/dc:language,'und')[1],')')">
                        <p:pipe step="main" port="pef"/>
                    </p:with-option>
                </px:pef-store>
            </p:when>
            <p:otherwise>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:group>

</p:declare-step>
