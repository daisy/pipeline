<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:translate-files" name="main"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:louis="http://liblouis.org/liblouis"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-inline-prefixes="#all"
    version="1.0">
    
    <p:input port="source" sequence="true" primary="true"/>
    <p:input port="temp-result.valid" sequence="true"/>
    <p:input port="temp-result.invalid" sequence="true"/>
    <p:input port="spine" sequence="false"/>
    <p:output port="result" sequence="true" primary="true"/>
    
    <p:option name="temp-dir" required="true"/>
    
    <p:import href="utils/select-by-base.xpl"/>
    <p:import href="utils/select-by-position.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xpl"/>
    
    <!-- ================ -->
    <!-- HELPER FUNCTIONS -->
    <!-- ================ -->
    
    <p:declare-step type="pxi:set-braille-page-begin" name="set-braille-page-begin">
        <p:input port="source" sequence="false" primary="true"/>
        <p:input port="source.all" sequence="false"/>
        <p:input port="temp-result.all" sequence="false"/>
        <p:input port="spine" sequence="false"/>
        <p:output port="result" sequence="false" primary="true"/>
        <p:choose>
            <p:when test="/*/louis:page-layout//c:param[@name='louis:braille-page-position' and @value='none']">
                <p:identity/>
            </p:when>
            <p:otherwise>
                <p:insert match="/*/louis:page-layout/c:param-set" position="last-child">
                    <p:input port="insertion">
                        <p:inline>
                            <c:param name="louis:braille-page-begin"/>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:choose>
                    <p:when test="/*/@louis:braille-page-reset">
                        <p:add-attribute match="/*/louis:page-layout//c:param[@name='louis:braille-page-begin']"
                                         attribute-name="value">
                            <p:with-option name="attribute-value" select="/*/@louis:braille-page-reset"/>
                        </p:add-attribute>
                    </p:when>
                    <p:otherwise>
                        <p:variable name="section-base" select="base-uri(/*)"/>
                        <p:variable name="braille-page-reset-base"
                                    select="string-join(/*/*[@louis:braille-page-reset]/base-uri(.), ' ')">
                            <p:pipe step="set-braille-page-begin" port="source.all"/>
                        </p:variable>
                        <p:variable name="preceding-pages-base"
                                    select="string-join(
                                              //louis:section[@href=$section-base]/preceding::louis:section
                                              [not(following::louis:section[following::louis:section[@href=$section-base]]
                                                                           [@href=tokenize($braille-page-reset-base, ' ')])]
                                              /@href, ' ')">
                            <p:pipe step="set-braille-page-begin" port="spine"/>
                        </p:variable>
                        <p:variable name="last-braille-page-reset"
                                    select="(/*/*[base-uri(.)=tokenize($preceding-pages-base, ' ')[1]]/@louis:braille-page-reset, 1)[1]">
                            <p:pipe step="set-braille-page-begin" port="source.all"/>
                        </p:variable>
                        <p:add-attribute match="/*/louis:page-layout//c:param[@name='louis:braille-page-begin']" attribute-name="value">
                            <p:with-option name="attribute-value"
                                           select="number($last-braille-page-reset)
                                                   + sum(/*/*[base-uri(.)=tokenize($preceding-pages-base, ' ')]/number(@pages))">
                                <p:pipe step="set-braille-page-begin" port="temp-result.all"/>
                            </p:with-option>
                        </p:add-attribute>
                    </p:otherwise>
                </p:choose>
            </p:otherwise>
        </p:choose>
    </p:declare-step>
    
    <p:declare-step type="pxi:include-liblouis-results" name="include-liblouis-results">
        <p:input port="source" sequence="false" primary="true"/>
        <p:input port="liblouis-results" sequence="true"/>
        <p:output port="result" sequence="false" primary="true"/>
        <p:viewport match="louis:include" name="viewport">
            <pxi:select-by-base name="select-result">
                <p:input port="source">
                    <p:pipe step="include-liblouis-results" port="liblouis-results"/>
                </p:input>
                <p:with-option name="base" select="resolve-uri(/*/@href)"/>
            </pxi:select-by-base>
            <p:sink/>
            <p:delete match="/*/*">
                <p:input port="source">
                    <p:pipe step="viewport" port="current"/>
                </p:input>
            </p:delete>
            <p:insert match="/*" position="first-child">
                <p:input port="insertion">
                    <p:pipe step="select-result" port="matched"/>
                </p:input>
            </p:insert>
        </p:viewport>
    </p:declare-step>
    
    <p:declare-step type="pxi:update-liblouis-results" name="update-liblouis-results">
        <p:input port="source.valid" sequence="true"/>
        <p:input port="source.invalid" sequence="true"/>
        <p:input port="update" sequence="true"/>
        <p:output port="result.valid" sequence="true">
            <p:pipe step="select-from-valid" port="not-matched"/>
            <p:pipe step="update-liblouis-results" port="update"/>
        </p:output>
        <p:output port="result.invalid" sequence="true">
            <p:pipe step="select-from-invalid" port="not-matched"/>
        </p:output>
        <p:wrap-sequence wrapper="_" name="wrap-update">
            <p:input port="source">
                <p:pipe step="update-liblouis-results" port="update"/>
            </p:input>
        </p:wrap-sequence>
        <pxi:select-by-base name="select-from-valid">
            <p:input port="source">
                <p:pipe step="update-liblouis-results" port="source.valid"/>
            </p:input>
            <p:with-option name="base" select="string-join(/*/louis:result/base-uri(.), ' ')">
                <p:pipe step="wrap-update" port="result"/>
            </p:with-option>
        </pxi:select-by-base>
        <p:sink/>
        <pxi:select-by-base name="select-from-invalid">
            <p:input port="source">
                <p:pipe step="update-liblouis-results" port="source.invalid"/>
            </p:input>
            <p:with-option name="base" select="string-join(/*/louis:result/base-uri(.), ' ')">
                <p:pipe step="wrap-update" port="result"/>
            </p:with-option>
        </pxi:select-by-base>
        <p:sink/>
    </p:declare-step>
    
    <p:declare-step type="pxi:invalidate-liblouis-results" name="invalidate-liblouis-results">
        <p:input port="source.valid" sequence="true"/>
        <p:input port="source.invalid" sequence="true"/>
        <p:option name="base" required="true"/>
        <p:output port="result.valid" sequence="true">
            <p:pipe step="select-from-valid" port="not-matched"/>
        </p:output>
        <p:output port="result.invalid" sequence="true">
            <p:pipe step="invalidate-liblouis-results" port="source.invalid"/>
            <p:pipe step="select-from-valid" port="matched"/>
        </p:output>
        <pxi:select-by-base name="select-from-valid">
            <p:input port="source">
                <p:pipe step="invalidate-liblouis-results" port="source.valid"/>
            </p:input>
            <p:with-option name="base" select="$base">
                <p:empty/>
            </p:with-option>
        </pxi:select-by-base>
        <p:sink/>
    </p:declare-step>
    
    <!-- ================================================================== -->
    
    <p:wrap-sequence wrapper="_" name="source.all">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
    </p:wrap-sequence>
    <p:sink/>
    
    <p:wrap-sequence wrapper="_" name="temp-result.all">
        <p:input port="source">
            <p:pipe step="main" port="temp-result.valid"/>
            <p:pipe step="main" port="temp-result.invalid"/>
            <p:inline><_/></p:inline>
        </p:input>
    </p:wrap-sequence>
    <p:sink/>
    
    <p:wrap-sequence wrapper="_" name="temp-result.valid.all">
        <p:input port="source">
            <p:pipe step="main" port="temp-result.valid"/>
            <p:inline><_/></p:inline>
        </p:input>
    </p:wrap-sequence>
    <p:sink/>
    
    <!-- ================================================================== -->
    
    <pxi:select-by-base name="source.valid">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:with-option name="base" select="string-join(/*/louis:result/base-uri(.), ' ')">
            <p:pipe step="temp-result.valid.all" port="result"/>
        </p:with-option>
    </pxi:select-by-base>
    <p:sink/>
    
    <pxi:select-by-position name="source.invalid.first" position="1">
        <p:input port="source">
            <p:pipe step="source.valid" port="not-matched"/>
        </p:input>
    </pxi:select-by-position>
    
    <p:count/>
    <p:choose>
        <p:when test="number(/c:result) > 0">
            
            <p:identity>
                <p:input port="source">
                    <p:pipe step="source.invalid.first" port="matched"/>
                </p:input>
            </p:identity>
            
            <p:choose>
                <p:when test="/louis:toc">
                    
                    <!-- ============= -->
                    <!--      TOC      -->
                    <!-- ============= -->
                    
                    <p:variable name="toc-base" select="base-uri(/louis:toc)"/>
                    <p:variable name="section-base" select="resolve-uri(/louis:toc/@href)"/>
                    
                    <p:identity name="source.toc"/>
                    
                    <pxi:select-by-base name="source.section">
                        <p:input port="source">
                            <p:pipe step="source.invalid.first" port="not-matched"/>
                            <p:pipe step="source.valid" port="matched"/>
                        </p:input>
                        <p:with-option name="base" select="$section-base"/>
                    </pxi:select-by-base>
                    
                    <p:identity name="rotate-sources">
                        <p:input port="source">
                            <p:pipe step="source.section" port="not-matched"/>
                            <p:pipe step="source.toc" port="result"/>
                            <p:pipe step="source.section" port="matched"/>
                        </p:input>
                    </p:identity>
                    
                    <pxi:include-liblouis-results name="include-results">
                        <p:input port="source">
                            <p:pipe step="source.section" port="matched"/>
                        </p:input>
                        <p:input port="liblouis-results">
                            <p:pipe step="main" port="temp-result.valid"/>
                            <p:pipe step="main" port="temp-result.invalid"/>
                        </p:input>
                    </pxi:include-liblouis-results>
                    
                    <p:xslt>
                        <p:input port="source">
                            <p:pipe step="include-results" port="result"/>
                            <p:pipe step="source.toc" port="result"/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="mark-toc-items.xsl"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                    
                    <p:insert match="/*" position="first-child">
                        <p:input port="insertion">
                            <p:inline>
                                <louis:toc>&#xA0;</louis:toc>
                            </p:inline>
                        </p:input>
                    </p:insert>
                    
                    <louis:translate-file name="translate-file">
                        <p:input port="styles" select="/*/louis:styles/d:fileset">
                            <p:pipe step="source.section" port="matched"/>
                            <p:pipe step="source.toc" port="result"/>
                        </p:input>
                        <p:input port="semantics" select="/*/louis:semantics/d:fileset">
                            <p:pipe step="source.section" port="matched"/>
                            <p:pipe step="source.toc" port="result"/>
                        </p:input>
                        <p:input port="page-layout" select="/*/louis:page-layout/c:param-set">
                            <p:pipe step="source.section" port="matched"/>
                        </p:input>
                        <p:with-param name="louis:braille-pages-in-toc" select="/louis:toc/@braille-pages='true'">
                            <p:pipe step="source.toc" port="result"/>
                        </p:with-param>
                        <p:with-param name="louis:print-pages-in-toc" select="/louis:toc/@print-pages='true'">
                            <p:pipe step="source.toc" port="result"/>
                        </p:with-param>
                        <p:with-param name="louis:toc-leader-pattern"
                                      select="translate(
                                                string(/louis:toc/@leader),
                                                '⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿⠀',
                                                'a1b''k2l`cif/msp&quot;e3h9o6r~djg>ntq,*5&lt;-u8v.%{$+x!&amp;;:4|0z7(_?w}#y)=')">
                            <p:pipe step="source.toc" port="result"/>
                        </p:with-param>
                        <p:with-option name="temp-dir" select="$temp-dir"/>
                    </louis:translate-file>
                    
                    <p:group name="result.toc">
                        <p:output port="result"/>
                        <p:variable name="width" select="number(/louis:toc/@width)">
                            <p:pipe step="source.toc" port="result"/>
                        </p:variable>
                        <p:variable name="page-width" select="number(/*/louis:page-layout//c:param[@name='louis:page-width']/@value)">
                            <p:pipe step="source.section" port="matched"/>
                        </p:variable>
                        <pxi:select-by-position position="1"/>
                        <p:xslt>
                            <p:input port="stylesheet">
                                <p:document href="read-liblouis-result.xsl"/>
                            </p:input>
                            <p:input port="parameters">
                                <p:empty/>
                            </p:input>
                            <p:with-param name="width" select="$width"/>
                            <p:with-param name="crop-top" select="1"/>
                            <p:with-param name="crop-left" select="max((0,$page-width - $width))"/>
                        </p:xslt>
                        <p:add-attribute match="/*" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="$toc-base"/>
                        </p:add-attribute>
                    </p:group>
                    
                    <p:group name="result.section">
                        <p:output port="result"/>
                        <pxi:select-by-position position="2" >
                            <p:input port="source">
                                <p:pipe step="translate-file" port="result"/>
                            </p:input>
                        </pxi:select-by-position>
                        <p:add-attribute match="/louis:result" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="$section-base"/>
                        </p:add-attribute>
                        <p:add-attribute match="/louis:result" attribute-name="cols">
                            <p:with-option name="attribute-value" select="replace(/*/louis:page-layout//c:param[@name='louis:page-width']/@value,'\.0*$','')">
                                <p:pipe step="source.section" port="matched"/>
                            </p:with-option>
                        </p:add-attribute>
                        <p:add-attribute match="/louis:result" attribute-name="rows">
                            <p:with-option name="attribute-value" select="replace(/*/louis:page-layout//c:param[@name='louis:page-height']/@value,'\.0*$','')">
                                <p:pipe step="source.section" port="matched"/>
                            </p:with-option>
                        </p:add-attribute>
                    </p:group>
                    
                    <pxi:update-liblouis-results name="update-results">
                        <p:input port="source.valid">
                            <p:pipe step="main" port="temp-result.valid"/>
                        </p:input>
                        <p:input port="source.invalid">
                            <p:pipe step="main" port="temp-result.invalid"/>
                        </p:input>
                        <p:input port="update">
                            <p:pipe step="result.toc" port="result"/>
                            <p:pipe step="result.section" port="result"/>
                        </p:input>
                    </pxi:update-liblouis-results>
                    
                    <p:group>
                        <p:variable name="include-base" select="base-uri(/*/*[descendant::louis:include[resolve-uri(@href)=$toc-base]])">
                            <p:pipe step="source.all" port="result"/>
                        </p:variable>
                        <p:variable name="old-toc-length" select="count(/*/louis:result[base-uri(.)=$toc-base]//louis:line)">
                            <p:pipe step="temp-result.all" port="result"/>
                        </p:variable>
                        <p:variable name="new-toc-length" select="count(//louis:line)">
                            <p:pipe step="result.toc" port="result"/>
                        </p:variable>
                        <p:variable name="old-section-length" select="(/*/louis:result[base-uri(.)=$section-base]/number(@pages), 0)[1]">
                            <p:pipe step="temp-result.all" port="result"/>
                        </p:variable>
                        <p:variable name="new-section-length" select="number(/*/@pages)">
                            <p:pipe step="result.section" port="result"/>
                        </p:variable>
                        <p:variable name="braille-page-reset-base"
                                    select="string-join(/*/*[@louis:braille-page-reset]/base-uri(.), ' ')">
                            <p:pipe step="source.all" port="result"/>
                        </p:variable>
                        <p:variable name="following-pages-base"
                                    select="string-join(
                                              //louis:section[@href=$section-base]/following::louis:section
                                              [not(@href=tokenize($braille-page-reset-base, ' '))]
                                              [not(preceding::louis:section[preceding::louis:section[@href=$section-base]]
                                                                           [@href=tokenize($braille-page-reset-base, ' ')])]
                                              /@href, ' ')">
                            <p:pipe step="main" port="spine"/>
                        </p:variable>
                        <p:variable name="following-numbered-pages-base"
                                    select="string-join(
                                              /*/*[base-uri()=tokenize($following-pages-base, ' ')]
                                                  [louis:page-layout//c:param[@name='louis:braille-page-position' and not(@value='none')]]
                                            /base-uri(), ' ')">
                            <p:pipe step="source.all" port="result"/>
                        </p:variable>
                        <pxi:invalidate-liblouis-results name="invalidate-results">
                            <p:input port="source.valid">
                                <p:pipe step="update-results" port="result.valid"/>
                            </p:input>
                            <p:input port="source.invalid">
                                <p:pipe step="update-results" port="result.invalid"/>
                            </p:input>
                            <p:with-option name="base" select="string-join(distinct-values((
                                                                 $include-base,
                                                                 if ($new-toc-length &gt; $old-toc-length)
                                                                   then /*/louis:toc[resolve-uri(@href)=$include-base]/base-uri(.)
                                                                   else (),
                                                                 if ($new-section-length &gt; $old-section-length)
                                                                   then (/*/louis:section[base-uri()=tokenize($following-numbered-pages-base, ' ')]/base-uri(.),
                                                                         /*/louis:toc[resolve-uri(@href)=tokenize($following-numbered-pages-base, ' ')]/base-uri(.))
                                                                   else ()
                                                               )), ' ')">
                                <p:pipe step="source.all" port="result"/>
                            </p:with-option>
                        </pxi:invalidate-liblouis-results>
                        <pxi:translate-files>
                            <p:input port="source">
                                <p:pipe step="rotate-sources" port="result"/>
                            </p:input>
                            <p:input port="temp-result.valid">
                                <p:pipe step="invalidate-results" port="result.valid"/>
                            </p:input>
                            <p:input port="temp-result.invalid">
                                <p:pipe step="invalidate-results" port="result.invalid"/>
                            </p:input>
                            <p:input port="spine">
                                <p:pipe step="main" port="spine"/>
                            </p:input>
                            <p:with-option name="temp-dir" select="$temp-dir">
                                <p:empty/>
                            </p:with-option>
                        </pxi:translate-files>
                    </p:group>
                </p:when>
                <p:when test="/louis:box">
                    
                    <!-- ============= -->
                    <!--      BOX      -->
                    <!-- ============= -->
                    
                    <p:variable name="box-base" select="base-uri(/louis:box)"/>
                    
                    <p:identity name="source.box"/>
                    
                    <p:identity name="rotate-sources">
                        <p:input port="source">
                            <p:pipe step="source.invalid.first" port="not-matched"/>
                            <p:pipe step="source.valid" port="matched"/>
                            <p:pipe step="source.box" port="result"/>
                        </p:input>
                    </p:identity>
                    
                    <pxi:include-liblouis-results>
                        <p:input port="source">
                            <p:pipe step="source.box" port="result"/>
                        </p:input>
                        <p:input port="liblouis-results">
                            <p:pipe step="main" port="temp-result.valid"/>
                            <p:pipe step="main" port="temp-result.invalid"/>
                        </p:input>
                    </pxi:include-liblouis-results>
                    
                    <p:insert match="/*" position="first-child">
                        <p:input port="insertion">
                            <p:inline>
                                <louis:line>&#xA0;</louis:line>
                            </p:inline>
                        </p:input>
                    </p:insert>
                    <p:insert match="/*" position="last-child">
                        <p:input port="insertion">
                            <p:inline>
                                <louis:line>&#xA0;</louis:line>
                            </p:inline>
                        </p:input>
                    </p:insert>
                    
                    <p:group name="result.box">
                        <p:output port="result"/>
                        <louis:translate-file paged="false">
                            <p:input port="styles" select="/*/louis:styles/d:fileset">
                                <p:pipe step="source.box" port="result"/>
                            </p:input>
                            <p:input port="semantics" select="/*/louis:semantics/d:fileset">
                                <p:pipe step="source.box" port="result"/>
                            </p:input>
                            <p:with-param name="louis:page-width" select="/*/@width">
                                <p:pipe step="source.box" port="result"/>
                            </p:with-param>
                            <p:with-option name="temp-dir" select="$temp-dir"/>
                        </louis:translate-file>
                        <p:xslt>
                            <p:input port="stylesheet">
                                <p:document href="read-liblouis-result.xsl"/>
                            </p:input>
                            <p:with-param name="width" select="/*/@width">
                                <p:pipe step="source.box" port="result"/>
                            </p:with-param>
                            <p:with-param name="border-left" select="/*/@border-left">
                                <p:pipe step="source.box" port="result"/>
                            </p:with-param>
                            <p:with-param name="border-right" select="/*/@border-right">
                                <p:pipe step="source.box" port="result"/>
                            </p:with-param>
                            <p:with-param name="border-top" select="/*/@border-top">
                                <p:pipe step="source.box" port="result"/>
                            </p:with-param>
                            <p:with-param name="border-bottom" select="/*/@border-bottom">
                                <p:pipe step="source.box" port="result"/>
                            </p:with-param>
                            <p:with-param name="keep-empty-trailing-lines" select="'true'"/>
                            <p:with-param name="crop-top" select="1"/>
                            <p:with-param name="crop-bottom" select="1"/>
                        </p:xslt>
                        <p:add-attribute match="/louis:result" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="$box-base"/>
                        </p:add-attribute>
                    </p:group>
                    
                    <pxi:update-liblouis-results name="update-results">
                        <p:input port="source.valid">
                            <p:pipe step="main" port="temp-result.valid"/>
                        </p:input>
                        <p:input port="source.invalid">
                            <p:pipe step="main" port="temp-result.invalid"/>
                        </p:input>
                        <p:input port="update">
                            <p:pipe step="result.box" port="result"/>
                        </p:input>
                    </pxi:update-liblouis-results>
                    
                    <p:group>
                        <p:variable name="include-base" select="base-uri(/*/*[descendant::louis:include[resolve-uri(@href)=$box-base]])">
                            <p:pipe step="source.all" port="result"/>
                        </p:variable>
                        <p:variable name="old-box-length" select="count(/*/louis:result[base-uri(.)=$box-base]//louis:line)">
                            <p:pipe step="temp-result.all" port="result"/>
                        </p:variable>
                        <p:variable name="new-box-length" select="count(//louis:line)">
                            <p:pipe step="result.box" port="result"/>
                        </p:variable>
                        <pxi:invalidate-liblouis-results name="invalidate-results">
                            <p:input port="source.valid">
                                <p:pipe step="update-results" port="result.valid"/>
                            </p:input>
                            <p:input port="source.invalid">
                                <p:pipe step="update-results" port="result.invalid"/>
                            </p:input>
                            <p:with-option name="base" select="string-join(distinct-values((
                                                                 $include-base,
                                                                 if ($new-box-length &gt; $old-box-length)
                                                                   then /*/louis:toc[resolve-uri(@href)=$include-base]/base-uri(.)
                                                                   else ()
                                                               )), ' ')">
                                <p:pipe step="source.all" port="result"/>
                            </p:with-option>
                        </pxi:invalidate-liblouis-results>
                        <pxi:translate-files>
                            <p:input port="source">
                                <p:pipe step="rotate-sources" port="result"/>
                            </p:input>
                            <p:input port="temp-result.valid">
                                <p:pipe step="invalidate-results" port="result.valid"/>
                            </p:input>
                            <p:input port="temp-result.invalid">
                                <p:pipe step="invalidate-results" port="result.invalid"/>
                            </p:input>
                            <p:input port="spine">
                                <p:pipe step="main" port="spine"/>
                            </p:input>
                            <p:with-option name="temp-dir" select="$temp-dir">
                                <p:empty/>
                            </p:with-option>
                        </pxi:translate-files>
                    </p:group>
                </p:when>
                <p:otherwise>
                    
                    <!-- ============= -->
                    <!--    SECTION    -->
                    <!-- ============= -->
                    
                    <p:variable name="section-base" select="base-uri(/*)"/>
                    
                    <p:identity name="source.section"/>
                    
                     <p:identity name="rotate-sources">
                        <p:input port="source">
                            <p:pipe step="source.invalid.first" port="not-matched"/>
                            <p:pipe step="source.valid" port="matched"/>
                            <p:pipe step="source.section" port="result"/>
                        </p:input>
                    </p:identity>
                    
                    <pxi:set-braille-page-begin name="set-braille-page-begin">
                        <p:input port="source">
                            <p:pipe step="source.section" port="result"/>
                        </p:input>
                        <p:input port="source.all">
                            <p:pipe step="source.all" port="result"/>
                        </p:input>
                        <p:input port="temp-result.all">
                            <p:pipe step="temp-result.all" port="result"/>
                        </p:input>
                        <p:input port="spine">
                            <p:pipe step="main" port="spine"/>
                        </p:input>
                    </pxi:set-braille-page-begin>
                    
                    <pxi:include-liblouis-results>
                        <p:input port="liblouis-results">
                            <p:pipe step="main" port="temp-result.valid"/>
                            <p:pipe step="main" port="temp-result.invalid"/>
                        </p:input>
                    </pxi:include-liblouis-results>
                    
                    <p:group name="result.section">
                        <p:output port="result" sequence="true"/>
                        <louis:translate-file>
                            <p:input port="styles" select="/*/louis:styles/d:fileset">
                                <p:pipe step="source.section" port="result"/>
                            </p:input>
                            <p:input port="semantics" select="/*/louis:semantics/d:fileset">
                                <p:pipe step="source.section" port="result"/>
                            </p:input>
                            <p:input port="page-layout" select="/*/louis:page-layout/c:param-set">
                                <p:pipe step="set-braille-page-begin" port="result"/>
                            </p:input>
                            <p:with-option name="temp-dir" select="$temp-dir"/>
                        </louis:translate-file>
                        <p:add-attribute match="/louis:result" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="$section-base"/>
                        </p:add-attribute>
                        <p:add-attribute match="/louis:result" attribute-name="cols">
                            <p:with-option name="attribute-value" select="replace(/*/louis:page-layout//c:param[@name='louis:page-width']/@value,'\.0*$','')">
                                <p:pipe step="source.section" port="result"/>
                            </p:with-option>
                        </p:add-attribute>
                        <p:add-attribute match="/louis:result" attribute-name="rows">
                            <p:with-option name="attribute-value" select="replace(/*/louis:page-layout//c:param[@name='louis:page-height']/@value,'\.0*$','')">
                                <p:pipe step="source.section" port="result"/>
                            </p:with-option>
                        </p:add-attribute>
                    </p:group>
                    
                    <pxi:update-liblouis-results name="update-results">
                        <p:input port="source.valid">
                            <p:pipe step="main" port="temp-result.valid"/>
                        </p:input>
                        <p:input port="source.invalid">
                            <p:pipe step="main" port="temp-result.invalid"/>
                        </p:input>
                        <p:input port="update">
                            <p:pipe step="result.section" port="result"/>
                        </p:input>
                    </pxi:update-liblouis-results>
                    
                    <p:group>
                        <p:variable name="old-section-length" select="(/*/louis:result[base-uri(.)=$section-base]/number(@pages), 0)[1]">
                            <p:pipe step="temp-result.all" port="result"/>
                        </p:variable>
                        <p:variable name="new-section-length" select="number(/*/@pages)">
                            <p:pipe step="result.section" port="result"/>
                        </p:variable>
                        <p:variable name="braille-page-reset-base"
                                    select="string-join(/*/*[@louis:braille-page-reset]/base-uri(.), ' ')">
                            <p:pipe step="source.all" port="result"/>
                        </p:variable>
                        <p:variable name="following-pages-base"
                                    select="string-join(
                                              //louis:section[@href=$section-base]/following::louis:section
                                              [not(@href=tokenize($braille-page-reset-base, ' '))]
                                              [not(preceding::louis:section[preceding::louis:section[@href=$section-base]]
                                                                           [@href=tokenize($braille-page-reset-base, ' ')])]
                                              /@href, ' ')">
                            <p:pipe step="main" port="spine"/>
                        </p:variable>
                        <p:variable name="following-numbered-pages-base"
                                    select="string-join(
                                              /*/*[base-uri()=tokenize($following-pages-base, ' ')]
                                                  [louis:page-layout//c:param[@name='louis:braille-page-position' and not(@value='none')]]
                                            /base-uri(), ' ')">
                            <p:pipe step="source.all" port="result"/>
                        </p:variable>
                        <pxi:invalidate-liblouis-results name="invalidate-results">
                            <p:input port="source.valid">
                                <p:pipe step="update-results" port="result.valid"/>
                            </p:input>
                            <p:input port="source.invalid">
                                <p:pipe step="update-results" port="result.invalid"/>
                            </p:input>
                            <p:with-option name="base" select="string-join(distinct-values((
                                                                if ($new-section-length &gt; $old-section-length)
                                                                   then (/*/louis:section[base-uri()=tokenize($following-numbered-pages-base, ' ')]/base-uri(.),
                                                                         /*/louis:toc[resolve-uri(@href)=tokenize($following-numbered-pages-base, ' ')]/base-uri(.))
                                                                   else ()
                                                               )), ' ')">
                                <p:pipe step="source.all" port="result"/>
                            </p:with-option>
                        </pxi:invalidate-liblouis-results>
                        <pxi:translate-files>
                            <p:input port="source">
                                <p:pipe step="rotate-sources" port="result"/>
                            </p:input>
                            <p:input port="temp-result.valid">
                                <p:pipe step="invalidate-results" port="result.valid"/>
                            </p:input>
                            <p:input port="temp-result.invalid">
                                <p:pipe step="invalidate-results" port="result.invalid"/>
                            </p:input>
                            <p:input port="spine">
                                <p:pipe step="main" port="spine"/>
                            </p:input>
                            <p:with-option name="temp-dir" select="$temp-dir">
                                <p:empty/>
                            </p:with-option>
                        </pxi:translate-files>
                    </p:group>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="main" port="temp-result.valid"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
