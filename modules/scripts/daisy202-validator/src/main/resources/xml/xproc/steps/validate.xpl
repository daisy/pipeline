<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy202-validator"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:l="http://xproc.org/library"
                exclude-inline-prefixes="#all"
                type="px:daisy202-validator"
                name="main">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:option name="timeToleranceMs" select="500" px:type="xs:integer"/>
    <p:option name="ncc" required="true"/>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="fileset.in" step="main"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="in-memory.in" step="main"/>
    </p:output>
    <p:output port="xml-report">
        <p:pipe step="xml-report" port="result"/>
    </p:output>
    <p:output port="html-report">
        <p:pipe step="html-report" port="result"/>
    </p:output>
    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:pipe step="validation-status" port="result"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>

    <p:variable name="start" select="current-dateTime()"/>
    <p:variable name="timeToleranceMsValid" select="if (matches($timeToleranceMs,'^\d+')) then $timeToleranceMs else 500"/>

    <px:mediatype-detect name="fileset.in">
        <p:input port="source">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:mediatype-detect>
    <px:message message="Validating DAISY 2.02 fileset"/>
    <px:message message="timeToleranceMs set to $1">
        <p:with-option name="param1" select="$timeToleranceMsValid"/>
    </px:message>
    <px:fileset-load media-types="application/smil+xml">
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="smil"/>
    <p:sink/>

    <px:fileset-load href="*/ncc.html">
        <p:input port="fileset">
            <p:pipe port="result" step="fileset.in"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="ncc"/>
    <p:sink/>

    <p:delete match="/*/d:file[ends-with(lower-case(resolve-uri(@href,base-uri(.))),'/ncc.html')]">
        <p:input port="source">
            <p:pipe port="result" step="fileset.in"/>
        </p:input>
    </p:delete>
    <px:fileset-load media-types="application/xhtml+xml text/html">
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="content"/>
    <p:sink/>

    <px:fileset-load media-types="application/*+xml application/xml text/html">
        <p:input port="fileset">
            <p:pipe port="result" step="fileset.in"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:for-each>
        <p:add-xml-base/>
    </p:for-each>
    <p:identity name="interdoc-urichecker.before-wrap"/>
    <p:wrap-sequence wrapper="wrapper">
        <p:input port="source">
            <p:pipe port="result" step="fileset.in"/>
            <p:pipe port="result" step="interdoc-urichecker.before-wrap"/>
        </p:input>
    </p:wrap-sequence>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="validate.check-references.xsl"/>
        </p:input>
    </p:xslt>
    <p:filter select="/*/*"/>
    <p:identity name="interdoc-urichecker-tests"/>
    <p:sink/>

    <p:for-each name="file-types.iterate">
        <p:iteration-source select="/*/d:file">
            <p:pipe port="result" step="fileset.in"/>
        </p:iteration-source>
        <p:variable name="base-uri-xpath" select="concat('&quot;',replace(resolve-uri(/*/@href,base-uri(/*)),'&quot;','&quot;&quot;'),'&quot;')"/>
        <p:choose>
            <p:when test="/*/@media-type=('application/xhtml+xml','text/html','application/smil+xml','audio/mpeg3','audio/mpeg','audio/wav','image/jpeg','image/gif','image/png','text/css')">
                <p:output port="result" sequence="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:output port="result" sequence="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="error">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;file type not allowed in DAISY 2.02 fileset: ',/*/@media-type,' (expected a html, smil, mp2, mp3, wav, jpg, gif, png or css file type)&quot;')">
                        <p:pipe port="current" step="file-types.iterate"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:identity name="filetype-restriction-tests"/>
    <p:sink/>

    <!-- validate SMIL files -->
    <p:for-each name="for-each.smil">
        <p:iteration-source>
            <p:pipe port="result" step="smil"/>
        </p:iteration-source>
        <p:choose>
            <p:when test="ends-with(lower-case(base-uri(/*)),'/master.smil')">
                <l:relax-ng-report name="for-each.smil.validate-smil-master">
                    <p:input port="schema">
                        <p:document href="http://www.daisy.org/pipeline/modules/daisy202-utils/d202msmil.rng"/>
                    </p:input>
                </l:relax-ng-report>
                <p:sink/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="report" step="for-each.smil.validate-smil-master"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:group>
                    <p:output port="report" sequence="true">
                        <p:pipe step="for-each.smil.validate-smil-other.rng" port="report"/>
                        <p:pipe step="for-each.smil.validate-smil-other.sch" port="report"/>
                    </p:output>
                    <l:relax-ng-report name="for-each.smil.validate-smil-other.rng">
                        <p:input port="schema">
                            <p:document href="http://www.daisy.org/pipeline/modules/daisy202-utils/d202smil.rng"/>
                        </p:input>
                    </l:relax-ng-report>
                    <p:sink/>
                    <px:relax-ng-to-schematron>
                        <p:input port="source">
                            <p:document href="http://www.daisy.org/pipeline/modules/daisy202-utils/d202smil.rng"/>
                        </p:input>
                    </px:relax-ng-to-schematron>
                    <p:for-each name="for-each.smil.validate-smil-other.sch">
                        <p:output port="report" sequence="true">
                            <p:pipe step="validate-with-schematron" port="report"/>
                        </p:output>
                        <p:validate-with-schematron assert-valid="false" name="validate-with-schematron">
                            <p:input port="source">
                                <p:pipe step="for-each.smil" port="current"/>
                            </p:input>
                            <p:input port="schema">
                                <p:pipe step="for-each.smil.validate-smil-other.sch" port="current"/>
                            </p:input>
                            <p:input port="parameters">
                                <p:empty/>
                            </p:input>
                        </p:validate-with-schematron>
                        <p:sink/>
                    </p:for-each>
                    <p:sink/>
                </p:group>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:identity name="smil-schema-tests"/>
    <p:sink/>

    <!-- validate NCC file -->
    <p:group name="ncc.validate-ncc">
        <p:output port="report" sequence="true">
            <p:pipe step="ncc.validate-ncc.rng" port="report"/>
            <p:pipe step="ncc.validate-ncc.sch" port="report"/>
        </p:output>
        <l:relax-ng-report name="ncc.validate-ncc.rng">
            <p:input port="source">
                <p:pipe port="result" step="ncc"/>
            </p:input>
            <p:input port="schema">
                <p:document href="http://www.daisy.org/pipeline/modules/daisy202-utils/d202ncc.rng"/>
            </p:input>
        </l:relax-ng-report>
        <p:sink/>
        <px:relax-ng-to-schematron>
            <p:input port="source">
                <p:document href="http://www.daisy.org/pipeline/modules/daisy202-utils/d202ncc.rng"/>
            </p:input>
        </px:relax-ng-to-schematron>
        <p:for-each name="ncc.validate-ncc.sch">
            <p:output port="report" sequence="true">
                <p:pipe step="validate-with-schematron" port="report"/>
            </p:output>
            <p:validate-with-schematron assert-valid="false" name="validate-with-schematron">
                <p:input port="source">
                    <p:pipe step="ncc" port="result"/>
                </p:input>
                <p:input port="schema">
                    <p:pipe step="ncc.validate-ncc.sch" port="current"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:validate-with-schematron>
            <p:sink/>
        </p:for-each>
        <p:sink/>
    </p:group>
    <p:identity name="ncc-schema-tests"/>
    <p:sink/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="result" step="ncc"/>
            <p:pipe port="result" step="content"/>
        </p:iteration-source>
        <px:message message="validating heading hierarchy for $1">
            <p:with-option name="param1" select="replace(base-uri(/*),'.*/','')"/>
        </px:message>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="validate.check-heading-hierarchy.xsl"/>
            </p:input>
        </p:xslt>
        <p:filter select="/*/*"/>
    </p:for-each>
    <p:identity name="html-heading-hierarchy-tests"/>
    <p:sink/>

    <!--
        validate duration:
            - compare audio duration with info from SMIL files and NCC totalTime (not implemented in DP1, but should probably have been there)
            - depends on https://github.com/daisy/pipeline-mod-audio/issues/4
        validate ID3:
            if (is mp3) {
                if (has ID3v2) - info: concat(file,' has ID3 tag')
                if (is not mono) - warning: concat(file,' is not single channel')
                if (variable bitrate) - warning: concat(file,' file uses variable bit rate (VBR)')
            }
            depends on https://github.com/daisy/pipeline-mod-audio/issues/3
    -->
    <p:identity name="audio-duration-and-id3-tests">
        <p:input port="source">
            <!-- not implemented -->
            <p:empty/>
        </p:input>
    </p:identity>
    <px:message message="MP3 file duration and ID3 tag validation is not implemented yet" severity="WARN"/>
    <p:sink/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="result" step="smil"/>
        </p:iteration-source>
        <p:add-xml-base/>
    </p:for-each>
    <p:wrap-sequence wrapper="c:result"/>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="validate.smil-times-1.xsl"/>
        </p:input>
    </p:xslt>
    <p:xslt>
        <p:with-param name="ncc-totalTime" select="/*/*[local-name()='head']/*[local-name()='meta' and @name='ncc:totalTime']/@content">
            <p:pipe port="result" step="ncc"/>
        </p:with-param>
        <p:input port="stylesheet">
            <p:document href="validate.smil-times-2.xsl"/>
        </p:input>
    </p:xslt>
    <p:identity name="smil-times"/>
    <p:for-each name="smil-times.iterate">
        <p:iteration-source select="/*/*"/>
        <p:output port="result" sequence="true">
            <p:pipe port="result" step="smil-times.iterate.totalTime"/>
            <p:pipe port="result" step="smil-times.iterate.duration"/>
        </p:output>
        <p:variable name="base-uri-xpath" select="concat('&quot;',replace(base-uri(/*),'&quot;','&quot;&quot;'),'&quot;')"/>

        <!-- test totalElapsedTime -->
        <p:choose name="smil-times.iterate.totalTime">
            <p:xpath-context>
                <p:pipe port="current" step="smil-times.iterate"/>
            </p:xpath-context>
            <p:when test="abs(/*/number(@calculated-totalTime) - /*/number(@meta-totalTime)) &gt; number($timeToleranceMsValid) div 1000">
                <p:output port="result" sequence="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="error">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                                <d:location>/smil/head/meta[@name='ncc:totalElapsedTime']</d:location>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace" select="concat('&quot;expected total elapsed time ',/*/@calculated-totalTime,' but found ',/*/@meta-totalTime,'&quot;')">
                        <p:pipe port="current" step="smil-times.iterate"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:output port="result" sequence="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="info">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                                <d:location>/smil/head/meta[@name='ncc:totalElapsedTime']</d:location>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace" select="concat('&quot;total elapsed time ',/*/@calculated-totalTime,' is close enough to the declared ',/*/@meta-totalTime,'&quot;')">
                        <p:pipe port="current" step="smil-times.iterate"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:otherwise>
        </p:choose>

        <!-- test timeInThisSmil -->
        <p:choose name="smil-times.iterate.duration">
            <p:xpath-context>
                <p:pipe port="current" step="smil-times.iterate"/>
            </p:xpath-context>
            <p:when test="abs(/*/number(@calculated-duration) - /*/number(@meta-duration)) &gt; number($timeToleranceMsValid) div 1000">
                <p:output port="result" sequence="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="error">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                                <d:location>/smil/head/meta[@name='ncc:timeInThisSmil']</d:location>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace" select="concat('&quot;expected duration ',/*/@calculated-duration,' but found ',/*/@meta-duration,'&quot;')">
                        <p:pipe port="current" step="smil-times.iterate"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:output port="result" sequence="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="info">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                                <d:location>/smil/head/meta[@name='ncc:timeInThisSmil']</d:location>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace" select="concat('&quot;duration ',/*/@calculated-duration,' is close enough to the declared ',/*/@meta-duration,'&quot;')">
                        <p:pipe port="current" step="smil-times.iterate"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:identity name="smil-time-tests"/>
    <p:sink/>

    <p:group>
        <p:variable name="base-uri-xpath" select="concat('&quot;',replace(base-uri(/*),'&quot;','&quot;&quot;'),'&quot;')">
            <p:pipe port="result" step="ncc"/>
        </p:variable>
        <p:choose>
            <p:xpath-context>
                <p:pipe port="result" step="smil-times"/>
            </p:xpath-context>
            <p:when test="abs(/*/number(@calculated-totalTime) - /*/number(@ncc-meta-totalTime)) &gt; number($timeToleranceMsValid) div 1000">
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="error">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                                <d:location>/html/head/meta[@name='ncc:totalTime']</d:location>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace" select="concat('&quot;expected total time ',/*/@calculated-totalTime,' but found ',/*/@ncc-meta-totalTime,'&quot;')">
                        <p:pipe port="result" step="smil-times"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <d:message severity="info">
                                <d:desc>DESC</d:desc>
                                <d:file>FILE</d:file>
                                <d:location>/html/head/meta[@name='ncc:totalTime']</d:location>
                            </d:message>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:string-replace match="//d:desc/text()">
                    <p:with-option name="replace" select="concat('&quot;total time ',/*/@calculated-totalTime,' is close enough to the declared ',/*/@ncc-meta-totalTime,'&quot;')">
                        <p:pipe port="result" step="smil-times"/>
                    </p:with-option>
                </p:string-replace>
                <p:string-replace match="//d:file/text()">
                    <p:with-option name="replace" select="$base-uri-xpath"/>
                </p:string-replace>
            </p:otherwise>
        </p:choose>
    </p:group>
    <p:identity name="ncc-time-test"/>
    <p:sink/>

    <p:identity>
        <p:input port="source">
            <p:pipe step="interdoc-urichecker-tests" port="result"/>
            <p:pipe step="filetype-restriction-tests" port="result"/>
            <p:pipe step="smil-schema-tests" port="result"/>
            <p:pipe step="ncc-schema-tests" port="result"/>
            <p:pipe step="html-heading-hierarchy-tests" port="result"/>
            <p:pipe step="audio-duration-and-id3-tests" port="result"/>
            <p:pipe step="smil-time-tests" port="result"/>
            <p:pipe step="ncc-time-test" port="result"/>
        </p:input>
    </p:identity>
    <p:group>
        <p:variable name="end" select="current-dateTime()">
            <p:empty/>
        </p:variable>
        <p:variable name="duration" select="xs:dateTime($end) - xs:dateTime($start)">
            <p:empty/>
        </p:variable>
        <px:message message="Validation completed in $1">
            <p:with-option name="param1" select="concat(if (contains($duration,'D')) then concat(days-from-duration(xs:duration($duration)), ' days, ') else '', if (contains($duration,'H')) then concat(hours-from-duration(xs:duration($duration)),':') else '', if (contains($duration,'H') and minutes-from-duration(xs:duration($duration)) &lt; 10) then '0' else '', minutes-from-duration(xs:duration($duration)), ':' , if (seconds-from-duration(xs:duration($duration)) &lt; 10) then '0' else '', seconds-from-duration(xs:duration($duration)) )">
                <p:empty/>
            </p:with-option>
        </px:message>
    </p:group>
    <p:identity name="report"/>

    <!-- ***************************************************** -->
    <!-- REPORT(S) TO HTML -->
    <!-- ***************************************************** -->
    
    <px:combine-validation-reports>
        <p:with-option name="document-name" select="replace($ncc,'.*/','')">
            <p:empty/>
        </p:with-option>
        <p:with-option name="document-type" select="'DAISY 2.02'">
            <p:empty/>
        </p:with-option>
        <p:with-option name="document-path" select="$ncc">
            <p:empty/>
        </p:with-option>
    </px:combine-validation-reports>
    <p:identity name="xml-report"/>
    <px:validation-report-to-html toc="false" name="html-report"/>
    <p:sink/>
    <px:validation-status name="validation-status">
        <p:input port="source">
            <p:pipe step="xml-report" port="result"/>
        </p:input>
    </px:validation-status>
    <p:sink/>
    
</p:declare-step>
