<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="dedicon:dtbook-to-pef" version="1.0"
                xmlns:dedicon="http://www.dedicon.nl"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to PEF (Dedicon)</h1>
        <p px:role="desc">Transforms a DTBook (DAISY 3 XML) document into a PEF.</p>
    </p:documentation>
    
    <p:input port="source" primary="true" px:name="source" px:media-type="application/x-dtbook+xml"/>
    
    <p:option name="stylesheet" select="'http://www.dedicon.nl/pipeline/modules/braille/default.scss'"/>
    <p:option name="include-preview" select="'true'"/>
    <p:option name="include-brf" select="'true'"/>
    <p:option name="include-obfl" select="'false'"/>
    <p:option name="ascii-file-format" select="'(table:&quot;com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_031_01&quot;)(line-breaks:dos)(pad:both)(charset:&quot;IBM00858&quot;)(file-extension:&quot;.brl&quot;)'"/>
    <p:option name="ascii-table"/>
    <p:option name="add-boilerplate" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Add boilerplate text</h2>
            <p px:role="desc">When enabled, and when the input has a `docauthor` element, will insert boilerplate text such as a title page.</p>
        </p:documentation>
    </p:option>
    <p:option name="page-width" select="'-1'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name" px:inherit="prepend"/>
            <p px:role="desc" px:inherit="prepend" xml:space="preserve">Use `-1` to compute this from metadata.</p>
        </p:documentation>
    </p:option>
    <p:option name="page-height" select="'-1'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name" px:inherit="prepend"/>
            <p px:role="desc" px:inherit="prepend" xml:space="preserve">Use `-1` to compute this from metadata.</p>
        </p:documentation>
    </p:option>
    <p:option name="duplex" select="'true'"/>
    <p:option name="hyphenation" select="'from-meta'">
        <p:pipeinfo>
            <px:data-type>
                <choice>
                    <documentation xmlns="http://relaxng.org/ns/compatibility/annotations/1.0">
                        <value>Yes</value>
                        <value>No</value>
                        <value>Use value from metadata field</value>
                    </documentation>
                    <value>true</value>
                    <value>false</value>
                    <value>from-meta</value>
                </choice>
            </px:data-type>
        </p:pipeinfo>
    </p:option>
    <p:option name="line-spacing" select="'single'"/>
    <p:option name="capital-letters" select="'true'"/>
    <p:option name="include-captions" select="'true'"/>
    <p:option name="include-images" select="'false'"/> <!-- displays the alt text -->
    <p:option name="include-image-groups" select="'true'"/>
    <p:option name="include-line-groups" select="'true'"/>
    <p:option name="include-production-notes" select="'true'"/>
    <p:option name="show-braille-page-numbers" select="'true'"/>
    <p:option name="show-print-page-numbers" select="'true'"/>
    <p:option name="show-inline-print-page-numbers" select="'from-meta'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Show inline print page numbers</h2>
        </p:documentation>
        <p:pipeinfo>
            <px:data-type>
                <choice>
                    <documentation xmlns="http://relaxng.org/ns/compatibility/annotations/1.0">
                        <value>Yes</value>
                        <value>No</value>
                        <value>Use value from metadata field</value>
                    </documentation>
                    <value>true</value>
                    <value>false</value>
                    <value>from-meta</value>
                </choice>
            </px:data-type>
        </p:pipeinfo>
    </p:option>
    <p:option name="force-braille-page-break" select="'false'"/>
    <p:option name="toc-depth" select="'6'"/>
    <p:option name="include-document-toc-in-last-volume" required="false" px:type="boolean" select="'from-meta'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include document TOC</h2>
            <p px:role="desc" xml:space="preserve">Whether or not to include a document-level TOC in the last volume.</p>
        </p:documentation>
        <p:pipeinfo>
            <px:data-type>
                <choice>
                    <documentation xmlns="http://relaxng.org/ns/compatibility/annotations/1.0">
                        <value>Yes</value>
                        <value>No</value>
                        <value>Use value from metadata field</value>
                    </documentation>
                    <value>true</value>
                    <value>false</value>
                    <value>from-meta</value>
                </choice>
            </px:data-type>
        </p:pipeinfo>
    </p:option>
    <p:option name="include-symbols-list" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include symbols list</h2>
            <p px:role="desc" xml:space="preserve">When enabled, a list of symbols and their descriptions is included in the first volume.</p>
        </p:documentation>
    </p:option>
    <p:option name="symbols-code" select="'http://www.dedicon.nl/pipeline/modules/braille/symbols_code.xml'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Symbols list</h2>
            <p px:role="desc" xml:space="preserve">If left unchanged, the default symbols list will be used.</p>
        </p:documentation>
    </p:option>
    <p:option name="symbols-list-header" required="false" px:type="string" select="'Symbolenlijst'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Symbols list header</h2>
        </p:documentation>
    </p:option>
    <p:option name="move-print-cover-to-first-volume" required="false" px:type="boolean" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Move print cover to first volume</h2>
            <p px:role="desc">When enabled, and when the input has a `level1` element with class `flap`, will move that cover to the first volume.</p>
        </p:documentation>
    </p:option>
    <p:option name="move-print-colophon-to-last-volume" required="false" px:type="boolean" select="'from-meta'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Move print colophon to last volume</h2>
            <p px:role="desc">When enabled, and when the input has a `level1` element with class `colophon`, will move that colophon to the last volume.</p>
        </p:documentation>
        <p:pipeinfo>
            <px:data-type>
                <choice>
                    <documentation xmlns="http://relaxng.org/ns/compatibility/annotations/1.0">
                        <value>Yes</value>
                        <value>No</value>
                        <value>Use value from metadata field</value>
                    </documentation>
                    <value>true</value>
                    <value>false</value>
                    <value>from-meta</value>
                </choice>
            </px:data-type>
        </p:pipeinfo>
    </p:option>
    <p:option name="maximum-number-of-sheets" select="'-1'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name" px:inherit="prepend"/>
            <p px:role="desc" px:inherit="prepend" xml:space="preserve">Use `-1` to compute this from metadata.</p>
        </p:documentation>
    </p:option>
    <p:option name="pef-output-dir"/>
    <p:option name="brf-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    <p:option name="temp-dir" select="''"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.dedicon.nl/pipeline/modules/braille/library.xpl"/>
    
    <p:in-scope-names name="in-scope-names"/>
    <px:merge-parameters>
        <p:input port="source">
            <p:pipe port="result" step="in-scope-names"/>
        </p:input>
    </px:merge-parameters>
    <px:delete-parameters parameter-names="stylesheet
                                    symbols-code
                                    ascii-table
                                    include-brf
                                    include-preview
                                    include-obfl
                                    pef-output-dir
                                    brf-output-dir
                                    preview-output-dir
                                    temp-dir"/>
    <px:add-parameters>
      <p:with-param name="skip-margin-top-of-page" select="true()"/>
      <p:with-param name="page-width" select="if ($page-width='-1')
                               then if (//dtb:meta[@name='prod:docType']/@content='ro') then '28'
                                    else if (//dtb:meta[@name='prod:docType']/@content='sv') then '33' else ''
                               else $page-width">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-param name="page-height" select="if ($page-height='-1')
                               then if (//dtb:meta[@name='prod:docType']/@content='ro') then '26'
                                    else if (//dtb:meta[@name='prod:docType']/@content='sv') then '27' else ''
                               else $page-height">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-param name="maximum-number-of-sheets" select="if ($maximum-number-of-sheets='-1')
                               then if (//dtb:meta[@name='prod:docType']/@content='ro') then '35'
                                    else if (//dtb:meta[@name='prod:docType']/@content='sv') then '37' else ''
                               else $maximum-number-of-sheets">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-param name="hyphenation" select="if ($hyphenation='from-meta')
                               then (//dtb:meta[@name='prod:docHyphenate']/@content,'Y')[1]='Y'
                               else $hyphenation='true'">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-param name="show-inline-print-page-numbers" select="if ($show-inline-print-page-numbers='from-meta')
                               then //dtb:meta[@name='prod:docType']/@content='sv'
                               else $show-inline-print-page-numbers='true'">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-param name="include-document-toc-in-last-volume" select="if ($include-document-toc-in-last-volume='from-meta')
                               then //dtb:meta[@name='prod:docType']/@content='ro'
                               else $include-document-toc-in-last-volume='true'">
        <p:pipe step="main" port="source"/>
      </p:with-param>
      <p:with-param name="move-print-colophon-to-last-volume" select="if ($move-print-colophon-to-last-volume='from-meta')
                               then //dtb:meta[@name='prod:docType']/@content='sv'
                               else $move-print-colophon-to-last-volume='true'">
        <p:pipe step="main" port="source"/>
      </p:with-param>
    </px:add-parameters>
    <p:identity name="input-options"/>
    <p:sink/>
    
    <!-- =============== -->
    <!-- CREATE TEMP DIR -->
    <!-- =============== -->
    <px:tempdir name="temp-dir">
        <p:with-option name="href" select="if ($temp-dir!='') then $temp-dir else $pef-output-dir"/>
    </px:tempdir>
    <p:sink/>
    
    <!-- ==================== -->
    <!-- DTBOOK PREPROCESSING -->
    <!-- ==================== -->
    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
    </p:identity>
    <dedicon:src-backslash-fix/>
    <p:choose>
        <p:when test="$include-symbols-list='true'">
            <dedicon:symbols-list>
                <p:with-option name="symbols-code" select="$symbols-code"/>
                <p:with-option name="symbols-list-header" select="$symbols-list-header"/>
            </dedicon:symbols-list>
        </p:when>
        <p:otherwise>
            <px:message message="Skipping symbols list"/>
        </p:otherwise>
    </p:choose>
    <p:choose>
        <p:when test="$add-boilerplate='true'">
            <dedicon:pre-processing>
                <p:input port="parameters">
                    <p:pipe step="input-options" port="result"/>
                </p:input>
            </dedicon:pre-processing>
        </p:when>
        <p:otherwise>
            <px:message message="Skipping Dedicon-specific pre-processing steps"/>
        </p:otherwise>
    </p:choose>
    
    <!-- ============= -->
    <!-- DTBOOK TO PEF -->
    <!-- ============= -->
    <px:dtbook-to-pef.convert default-stylesheet="http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/css/default.css"
                              transform="(formatter:dotify)(translator:dedicon)"
                              name="convert">
        <p:with-option name="temp-dir" select="string(/c:result)">
            <p:pipe step="temp-dir" port="result"/>
        </p:with-option>
        <p:with-option name="stylesheet" select="$stylesheet"/>
        <p:with-option name="include-obfl" select="$include-obfl"/>
        <p:input port="parameters">
            <p:pipe port="result" step="input-options"/>
        </p:input>
    </px:dtbook-to-pef.convert>
    
    <!-- ========= -->
    <!-- STORE PEF -->
    <!-- ========= -->
    <p:group>
        <p:variable name="name" select="if (//dtb:meta[@name='dc:Identifier']/@content)
                                then //dtb:meta[@name='dc:Identifier']/@content
                                else replace(p:base-uri(/),'^.*/([^/]*)\.[^/\.]*$','$1')">
            <p:pipe step="main" port="source"/>
        </p:variable>
        <pef:store>
            <p:with-option name="href" select="concat($pef-output-dir,'/',$name,'.pef')"/>
            <p:with-option name="preview-href" select="if ($include-preview='true' and $preview-output-dir!='')
                                                       then concat($preview-output-dir,'/',$name,'.pef.html')
                                                       else ''"/>
            <!-- <p:with-option name="brf-table" select="if ($ascii-table!='') then $ascii-table
                                                    else concat('(locale:',(/*/@xml:lang,'und')[1],')')"/> -->
            <p:with-option name="brf-dir-href" select="
                if ($include-brf='true')
                then
                    if ($brf-output-dir!='')
                    then $brf-output-dir
                    else
                        if ($pef-output-dir!='')
                        then $pef-output-dir
                        else ''
                else ''
            "/>
            <p:with-option name="brf-file-format" select="$ascii-file-format"/>
            <p:with-option name="brf-name-pattern" select="concat('p',$name,'_{}')"/>
            <p:with-option name="brf-number-width" select="'3'"/>
        </pef:store>

        <!-- Store OBFL -->
        <p:for-each>
            <p:iteration-source>
                <p:pipe step="convert" port="obfl"/>
            </p:iteration-source>
            <px:message message="Storing OBFL"/>
            <p:store>
                <p:with-option name="href" select="concat($pef-output-dir,'/',$name,'.obfl')"/>
            </p:store>
        </p:for-each>
    </p:group>

</p:declare-step>
