<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                type="px:nordic-html-add-accessibility-css.step"
                name="main">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="fileset.out" primary="true"/>

    <p:output port="in-memory.out" sequence="true">
      <!--
          FIXME: don't ignore possible non-HTML documents from in-memory.in port
      -->
      <p:pipe step="insert-css-link" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-add-entry
      </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
      <p:documentation>
        px:mkdir
        px:copy-resource
      </p:documentation>
    </p:import>

    <px:fileset-load media-types="application/xhtml+xml" name="load-html">
      <p:input port="in-memory">
        <p:pipe port="in-memory.in" step="main"/>
      </p:input>
    </px:fileset-load>

    <!--
        if validation-status = error, there could be 0 documents on the input port, therefore use p:for-each
    -->
    <p:for-each name="insert-css-link">
      <p:output port="result" sequence="true"/>
      <!--
          Link to the CSS style sheet from the HTML.
      -->
      <p:insert match="/html:html/html:head" position="last-child">
        <p:input port="insertion">
          <p:inline exclude-inline-prefixes="#all">
            <link xmlns="http://www.w3.org/1999/xhtml" rel="stylesheet" type="text/css" href="css/accessibility.css"/>
          </p:inline>
        </p:input>
      </p:insert>
    </p:for-each>

    <p:count/>
    <p:choose>
      <p:when test=".='0'">
        <p:identity>
          <p:input port="source">
            <p:pipe step="main" port="fileset.in"/>
          </p:input>
        </p:identity>
      </p:when>
      <p:otherwise>

        <p:variable name="html-base" select="base-uri(/)">
          <p:pipe step="insert-css-link" port="result"/>
        </p:variable>
        <p:variable name="xproc-base" select="base-uri(/)">
          <p:inline>
            <irrelevant/>
          </p:inline>
        </p:variable>

        <px:mkdir name="mkdir">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/',$html-base)"/>
        </px:mkdir>
        <!--
            Extract resources contained in JAR
            FIXME: It should be possible to reference these files directly without first extracting
            them, which would greatly simplify the code.
        -->
        <px:copy-resource name="dtbook-to-html.step.store1" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/accessibility.css',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/accessibility.css', $html-base)"/>
        </px:copy-resource>
        <px:copy-resource name="dtbook-to-html.step.store2" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/fonts/opendyslexic/OpenDyslexic-Regular.otf',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-Regular.otf',
                                 $html-base)"/>
        </px:copy-resource>
        <px:copy-resource name="dtbook-to-html.step.store3" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/fonts/opendyslexic/OpenDyslexic-Italic.otf',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-Italic.otf',
                                 $html-base)"/>
        </px:copy-resource>
        <px:copy-resource name="dtbook-to-html.step.store4" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/fonts/opendyslexic/OpenDyslexic-Bold.otf',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-Bold.otf',
                                 $html-base)"/>
        </px:copy-resource>
        <px:copy-resource name="dtbook-to-html.step.store5" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf',
                                 $html-base)"/>
        </px:copy-resource>
        <px:copy-resource name="dtbook-to-html.step.store6" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf',
                                 $html-base)"/>
        </px:copy-resource>
        <px:copy-resource name="dtbook-to-html.step.store7" cx:depends-on="mkdir">
          <p:with-option name="href" select="resolve-uri('../../../css/fonts/opendyslexic/LICENSE.txt',$xproc-base)"/>
          <p:with-option name="target"
                         select="resolve-uri('css/fonts/opendyslexic/LICENSE.txt',
                                 $html-base)"/>
        </px:copy-resource>
        <p:identity>
          <p:input port="source">
            <p:pipe port="result" step="dtbook-to-html.step.store1"/>
            <p:pipe port="result" step="dtbook-to-html.step.store2"/>
            <p:pipe port="result" step="dtbook-to-html.step.store3"/>
            <p:pipe port="result" step="dtbook-to-html.step.store4"/>
            <p:pipe port="result" step="dtbook-to-html.step.store5"/>
            <p:pipe port="result" step="dtbook-to-html.step.store6"/>
            <p:pipe port="result" step="dtbook-to-html.step.store7"/>
          </p:input>
        </p:identity>
        <p:sink name="store-resources"/>

        <p:identity>
          <p:input port="source">
            <p:pipe step="main" port="fileset.in"/>
          </p:input>
        </p:identity>
        <px:fileset-add-entry media-type="text/css">
          <p:with-option name="href" select="resolve-uri('css/accessibility.css',$html-base)"/>
        </px:fileset-add-entry>
        <px:fileset-add-entry media-type="application/x-font-opentype">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-Regular.otf',$html-base)"/>
        </px:fileset-add-entry>
        <px:fileset-add-entry media-type="application/x-font-opentype">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-Italic.otf',$html-base)"/>
        </px:fileset-add-entry>
        <px:fileset-add-entry media-type="application/x-font-opentype">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-Bold.otf',$html-base)"/>
        </px:fileset-add-entry>
        <px:fileset-add-entry media-type="application/x-font-opentype">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf',$html-base)"/>
        </px:fileset-add-entry>
        <px:fileset-add-entry media-type="application/x-font-opentype">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf',$html-base)"/>
        </px:fileset-add-entry>
        <px:fileset-add-entry media-type="text/plain">
          <p:with-option name="href" select="resolve-uri('css/fonts/opendyslexic/LICENSE.txt',$html-base)"/>
        </px:fileset-add-entry>

        <p:identity cx:depends-on="store-resources"/>
      </p:otherwise>
    </p:choose>

</p:declare-step>

