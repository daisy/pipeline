<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:zedai-to-epub3" name="main"
                exclude-inline-prefixes="#all">

    <p:documentation> Transforms a ZedAI (DAISY 4 XML) document into an EPUB 3 publication. </p:documentation>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:input port="tts-config">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
	<h2 px:role="name">Text-To-Speech configuration file</h2>
	<p px:role="desc">Configuration file that contains Text-To-Speech
	properties, links to aural CSS stylesheets and links to PLS
	lexicons.</p>
      </p:documentation>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="ocf"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="in-memory.result"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:pipe step="validation-status" port="result"/>
    </p:output>
  
    <p:option name="output-dir" required="true">
        <p:documentation>Empty directory dedicated to this conversion.</p:documentation>
    </p:option>
    <p:option name="temp-dir" select="''">
        <p:documentation>Empty directory dedicated to this conversion. May be left empty in which
        case a temporary directory will be automaticall created.</p:documentation>
    </p:option>
    <p:option name="chunk-size" required="false" select="'-1'"/>
    <p:option name="audio" required="false" select="'false'"/>
    <p:option name="process-css" required="false" select="'true'">
        <p:documentation>Set to false to bypass aural CSS processing.</p:documentation>
    </p:option>

    <p:import href="zedai-to-opf-metadata.xpl">
        <p:documentation>
            px:zedai-to-opf-metadata
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-nav-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-pub-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/tts-helpers/library.xpl" />
    <p:import href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/library.xpl" />
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-tts/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/css-speech/library.xpl"/>

    <p:variable name="epub-dir" select="concat($output-dir,'epub/')"/>
    <p:variable name="content-dir" select="concat($epub-dir,'EPUB/')"/>

    <!--=========================================================================-->
    <!-- GET ZEDAI FROM FILESET                                                  -->
    <!--=========================================================================-->

    <p:documentation>Retreive the ZedAI document from the input fileset.</p:documentation>
    <p:group>
        <px:fileset-load media-types="application/z3998-auth+xml">
            <p:input port="in-memory">
                <p:pipe step="main" port="in-memory.in"/>
            </p:input>
        </px:fileset-load>
        <!-- TODO: describe the error on the wiki and insert correct error code -->
        <px:assert message="No XML documents with the ZedAI media type ('application/z3998-auth+xml') found in the fileset."
                   test-count-min="1" error-code="PEZE00"/>
        <px:assert message="More than one XML document with the ZedAI media type ('application/z3998-auth+xml') found in the fileset; there can only be one ZedAI document."
                   test-count-max="1" error-code="PEZE00"/>
    </p:group>
    <p:identity name="first-zedai"/>

    <!--=========================================================================-->
    <!-- CSS INLINING                                                            -->
    <!--=========================================================================-->
    <p:choose>
        <p:xpath-context>
            <p:empty/>
        </p:xpath-context>
        <p:when test="$audio='true' and $process-css='true'">
            <px:inline-css-speech content-type="application/z3998-auth+xml">
                <p:input port="source">
                    <p:pipe step="first-zedai" port="result"/>
                </p:input>
                <p:input port="fileset.in">
                    <p:pipe step="main" port="fileset.in"/>
                </p:input>
                <p:input port="config">
                    <p:pipe step="main" port="tts-config"/>
                </p:input>
            </px:inline-css-speech>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="first-zedai" port="result"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:identity name="zedai-with-css"/>
    
    <!--=========================================================================-->
    <!-- METADATA                                                                -->
    <!--=========================================================================-->

    <p:documentation>Extract metadata from ZedAI</p:documentation>
    <px:zedai-to-opf-metadata name="metadata"/>

    <!--=========================================================================-->
    <!-- CONVERT TO XHTML                                                        -->
    <!--=========================================================================-->

    <p:documentation>Convert the ZedAI Document into several XHTML Documents</p:documentation>
    <p:group name="zedai-to-html">
        <p:output port="result" primary="true" sequence="true"/>
        <p:output port="html-files" sequence="true">
            <p:pipe port="html-files" step="zedai-to-html.iterate"/>
        </p:output>
        <p:variable name="zedai-basename" select="replace(replace(//*[@media-type='application/z3998-auth+xml']/@href,'^.+/([^/]+)$','$1'),'^(.+)\.[^\.]+$','$1')">
            <p:pipe port="fileset.in" step="main"/>
        </p:variable>
        <p:variable name="result-basename" select="concat($content-dir,$zedai-basename,'.xhtml')"/>
        <p:xslt name="zedai-to-html.html-single">
            <p:input port="source">
                <p:pipe step="zedai-with-css" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="http://www.daisy.org/pipeline/modules/zedai-to-html/xslt/zedai-to-html.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$result-basename"/>
        </px:set-base-uri>
        <p:xslt name="zedai-to-html.html-with-ids">
            <p:input port="stylesheet">
                <p:document href="http://www.daisy.org/pipeline/modules/html-utils/html-id-fixer.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <px:html-chunker name="zedai-to-html.html-chunks">
            <p:with-option name="max-chunk-size" select="$chunk-size"/>
        </px:html-chunker>
        <p:for-each name="zedai-to-html.iterate">
            <p:output port="fileset" primary="true"/>
            <p:output port="html-files" sequence="true">
                <p:pipe port="result" step="zedai-to-html.iterate.html"/>
            </p:output>
            <p:variable name="result-uri" select="base-uri(/*)"/>
            <p:identity name="zedai-to-html.iterate.html"/>
            <px:fileset-create>
                <p:with-option name="base" select="$content-dir"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/xhtml+xml">
                <p:with-option name="href" select="$result-uri"/>
            </px:fileset-add-entry>
        </p:for-each>
        <px:message message="Converted to XHTML."/>
    </p:group>

    <!--=========================================================================-->
    <!-- GENERATE THE NAVIGATION DOCUMENT                                        -->
    <!--=========================================================================-->

    <p:documentation>Generate the EPUB 3 navigation document</p:documentation>
    <p:group name="navigation-doc">
        <p:output port="result" primary="true">
            <p:pipe port="result" step="navigation-doc.result.fileset"/>
        </p:output>
        <p:output port="html-file">
            <p:pipe port="result" step="navigation-doc.result.html-file"/>
        </p:output>
        <p:variable name="nav-base" select="concat($content-dir,'toc.xhtml')">
            <p:empty/>
        </p:variable>
        <px:epub3-nav-create-toc>
            <p:input port="source">
                <p:pipe port="html-files" step="zedai-to-html"/>
            </p:input>
            <p:with-option name="output-base-uri" select="$nav-base">
                <p:empty/>
            </p:with-option>
        </px:epub3-nav-create-toc>
        <px:set-base-uri name="navigation-doc.toc">
            <p:with-option name="base-uri" select="$nav-base"/>
        </px:set-base-uri>
        <px:epub3-nav-create-page-list>
            <p:input port="source">
                <p:pipe port="html-files" step="zedai-to-html"/>
            </p:input>
        </px:epub3-nav-create-page-list>
        <px:set-base-uri name="navigation-doc.page-list">
            <p:with-option name="base-uri" select="$nav-base"/>
        </px:set-base-uri>
        <px:epub3-nav-aggregate name="navigation-doc.html-file">
            <p:input port="source">
                <p:pipe port="result" step="navigation-doc.toc"/>
                <p:pipe port="result" step="navigation-doc.page-list"/>
            </p:input>
        </px:epub3-nav-aggregate>
        <!--TODO create other nav types (configurable ?)-->
        <px:fileset-create>
            <p:with-option name="base" select="$content-dir"/>
        </px:fileset-create>
        <px:fileset-add-entry media-type="application/xhtml+xml" name="navigation-doc.result.fileset">
            <p:with-option name="href" select="$nav-base"/>
        </px:fileset-add-entry>
        <px:set-base-uri>
            <p:input port="source">
                <p:pipe port="result" step="navigation-doc.html-file"/>
            </p:input>
            <p:with-option name="base-uri" select="$nav-base"/>
        </px:set-base-uri>
        <px:message message="Navigation Document Created." name="navigation-doc.result.html-file"/>
    </p:group>

    <!--=========================================================================-->
    <!-- Call the TTS								 -->
    <!--=========================================================================-->

    <px:fileset-join name="fileset-for-tts">
      <!-- TODO: include resources such as lexicons -->
      <p:input port="source">
	<p:pipe port="result" step="zedai-to-html"/>
	<p:pipe port="result" step="navigation-doc"/>
      </p:input>
    </px:fileset-join>
    <px:tts-for-epub3 name="tts">
      <p:input port="in-memory.in">
	<p:pipe port="html-file" step="navigation-doc"/>
	<p:pipe port="html-files" step="zedai-to-html"/>
      </p:input>
      <p:input port="fileset.in">
	<p:pipe port="result" step="fileset-for-tts"/>
      </p:input>
      <p:input port="config">
	<p:pipe port="tts-config" step="main"/>
      </p:input>
      <p:with-option name="audio" select="$audio"/>
      <p:with-option name="output-dir" select="$output-dir"/>
      <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:tts-for-epub3>

    <!--=========================================================================-->
    <!-- GENERATE THE MEDIA-OVERLAYS                                             -->
    <!--=========================================================================-->

    <p:choose name="mo-work">
      <p:xpath-context>
	<p:pipe port="audio-map" step="tts"/>
      </p:xpath-context>
      <p:when test="count(/d:audio-clips/*) = 0">
	<p:output port="audio-filesets" sequence="true">
	  <p:empty/>
	</p:output>
	<p:output port="smil-fileset">
	  <p:empty/>
	</p:output>
	<p:output port="smils" sequence="true">
	  <p:empty/>
	</p:output>
	<p:sink/>
      </p:when>
      <p:otherwise>
	<p:output port="smil-fileset">
	  <p:pipe port="fileset.out" step="create-mo"/>
	</p:output>
	<p:output port="audio-filesets" sequence="true">
	  <p:pipe port="fileset.out" step="audio-fileset"/>
	</p:output>
	<p:output port="smils" sequence="true">
	  <p:pipe port="in-memory.out" step="create-mo"/>
	</p:output>

	<!--=========================================================================-->
	<!-- CREATE THE FILESET THAT LINKS TO THE AUDIO FILES                        -->
	<!--=========================================================================-->
	<px:create-audio-fileset name="audio-fileset">
	  <p:input port="source">
	    <p:pipe step="tts" port="audio-map"/>
	  </p:input>
	  <p:with-option name="output-dir" select="$content-dir">
	    <p:empty/>
	  </p:with-option>
	  <p:with-option name="audio-relative-dir" select="'audio/'">
	    <p:empty/> <!-- TODO: make it an px:create-mediaoverlays' option as well so as to avoid inconsistencies  -->
	  </p:with-option>
	</px:create-audio-fileset>

	<!--=========================================================================-->
	<!-- CREATE THE SMILS FROM THE AUDIO MAP		                     -->
	<!--=========================================================================-->

	<px:create-mediaoverlays name="create-mo">
	  <p:input port="content-docs">
	    <p:pipe port="content.out" step="tts"/>
	  </p:input>
	  <p:input port="audio-map">
	    <p:pipe port="audio-map" step="tts"/>
	  </p:input>
	  <p:with-option name="content-dir" select="$content-dir">
	    <p:empty/>
	  </p:with-option>
	</px:create-mediaoverlays>
      </p:otherwise>
    </p:choose>


    <!--=========================================================================-->
    <!-- GENERATE THE PACKAGE DOCUMENT                                           -->
    <!--=========================================================================-->
    <p:documentation>Generate the EPUB 3 package document</p:documentation>
    <p:group name="package-doc">
        <p:output port="result" primary="true"/>
        <p:output port="opf">
            <p:pipe port="result" step="package-doc.create"/>
        </p:output>

        <p:variable name="opf-base" select="concat($content-dir,'package.opf')"/>

        <p:identity>
            <p:input port="source">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
        </p:identity>
        <p:group name="resources">
            <p:output port="result"/>
            <p:variable name="zedai-uri" select="(//d:file[@media-type='application/z3998-auth+xml'])[1]/resolve-uri(@href,base-uri(.))"/>
            <p:delete match="d:file[@media-type='application/z3998-auth+xml']"/>
            <p:viewport match="/*/*">
                <p:documentation>Make sure that the files in the fileset is relative to the ZedAI file.</p:documentation>
                <p:xslt>
                    <p:with-param name="uri" select="/*/resolve-uri(@href,base-uri(.))"/>
                    <p:with-param name="base" select="$zedai-uri"/>
                    <p:input port="stylesheet">
                        <p:inline>
                            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0">
                                <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                                <xsl:param name="uri" required="yes"/>
                                <xsl:param name="base" required="yes"/>
                                <xsl:template match="/*">
                                    <xsl:copy>
                                        <xsl:copy-of select="@*"/>
                                        <xsl:attribute name="href" select="pf:relativize-uri($uri,$base)"/>
                                    </xsl:copy>
                                </xsl:template>
                            </xsl:stylesheet>
                        </p:inline>
                    </p:input>
                </p:xslt>
                <p:identity/>
            </p:viewport>
            <px:set-base-uri>
                <p:with-option name="base-uri" select="$content-dir"/>
            </px:set-base-uri>
            <!-- TODO: remove resources from fileset that are not referenced from any of the in-memory files -->
        </p:group>

        <px:fileset-join name="package-doc.join-filesets">
            <p:input port="source">
                <p:pipe port="result" step="zedai-to-html"/>
                <p:pipe port="result" step="navigation-doc"/>
                <p:pipe port="result" step="resources"/>
		<p:pipe port="smil-fileset" step="mo-work"/>
		<p:pipe port="audio-filesets" step="mo-work"/>
            </p:input>
        </px:fileset-join>
        <p:sink/>

	<px:fileset-join name="publication-resources">
	    <p:input port="source">
	      <p:pipe port="result" step="resources"/>
	      <p:pipe port="audio-filesets" step="mo-work"/>
	    </p:input>
	</px:fileset-join>

        <px:epub3-pub-create-package-doc name="package-doc.create">
            <p:input port="spine-filesets">
		<p:pipe port="result" step="navigation-doc"/>
                <p:pipe port="result" step="zedai-to-html"/>
            </p:input>
            <p:input port="publication-resources">
                <p:pipe port="result" step="publication-resources"/>
            </p:input>
	    <p:input port="mediaoverlays">
	      <p:pipe port="smils" step="mo-work"/>
	    </p:input>
            <p:input port="metadata">
                <p:pipe port="result" step="metadata"/>
            </p:input>
            <p:input port="content-docs">
                <p:pipe port="content.out" step="tts"/>
            </p:input>
            <p:with-option name="result-uri" select="$opf-base"/>
            <p:with-option name="compatibility-mode" select="'false'"/>
	    <p:with-option name="nav-uri" select="base-uri(/*)">
                <p:pipe port="html-file" step="navigation-doc"/>
            </p:with-option>
            <!--TODO configurability for other META-INF files ?-->
        </px:epub3-pub-create-package-doc>

        <px:fileset-add-entry media-type="application/oebps-package+xml">
            <p:input port="source">
                <p:pipe port="result" step="package-doc.join-filesets"/>
            </p:input>
            <p:with-option name="href" select="$opf-base"/>
        </px:fileset-add-entry>

        <px:message message="Package Document Created."/>
    </p:group>

    <p:group name="fileset.without-ocf">
        <p:output port="result"/>

        <p:identity name="fileset.dirty"/>
        <p:wrap-sequence wrapper="wrapper">
            <p:input port="source">
                <p:pipe step="package-doc" port="opf"/>
                <p:pipe step="tts" port="content.out"/>
		<p:pipe port="smils" step="mo-work"/>
            </p:input>
        </p:wrap-sequence>
        <p:delete match="/*/*/*" name="wrapped-in-memory"/>
        <p:identity>
            <p:input port="source">
                <p:pipe port="result" step="fileset.dirty"/>
            </p:input>
        </p:identity>
        <p:viewport match="//d:file" name="fileset.clean">
            <p:variable name="file-href" select="/*/resolve-uri(@href,base-uri(.))"/>
            <p:variable name="file-original" select="if (/*/@original-href) then resolve-uri(/*/@original-href) else ''"/>
            <p:choose>
                <p:xpath-context>
                    <p:pipe port="result" step="wrapped-in-memory"/>
                </p:xpath-context>
                <p:when test="not($file-original) and not(/*/*[base-uri(.) = $file-href])">
                    <!-- Fileset contains file reference to a file that is neither stored on disk nor in memory; discard it -->
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:otherwise>
                    <!-- File refers to a document on disk or in memory; keep it -->
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:viewport>
        <px:fileset-create name="fileset.with-epub-base">
            <p:with-option name="base" select="$epub-dir"/>
        </px:fileset-create>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="fileset.with-epub-base"/>
                <p:pipe port="result" step="fileset.clean"/>
            </p:input>
        </px:fileset-join>
    </p:group>
    <p:sink/>

    <px:epub3-ocf-finalize name="ocf">
        <p:input port="source">
            <p:pipe port="result" step="fileset.without-ocf"/>
        </p:input>
    </px:epub3-ocf-finalize>

    <p:for-each name="in-memory.result">
        <p:output port="result" sequence="true"/>
        <p:iteration-source>
            <p:pipe step="ocf" port="in-memory.out"/>
            <p:pipe step="package-doc" port="opf"/>
            <p:pipe step="tts" port="content.out"/>
	    <p:pipe port="smils" step="mo-work"/>
        </p:iteration-source>
        <p:variable name="doc-base" select="base-uri(/*)"/>
        <p:choose>
            <p:xpath-context>
                <p:pipe port="result" step="ocf"/>
            </p:xpath-context>
            <p:when test="//d:file[resolve-uri(@href,base-uri(.)) = $doc-base]">
                <!-- document is in fileset; keep it -->
                <p:identity/>
            </p:when>
            <p:otherwise>
                <!-- document is not in fileset; discard it -->
                <p:sink/>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
    </p:for-each>

    <!--=========================================================================-->
    <!-- Status								 -->
    <!--=========================================================================-->

    <p:rename match="/*" new-name="d:validation-status" name="validation-status">
        <p:input port="source">
            <p:pipe step="tts" port="status"/>
        </p:input>
    </p:rename>

</p:declare-step>
