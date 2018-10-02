<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" version="1.0"
    xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:declare-step type="px:epubcheck" name="main">
        <!-- anyFileURI to the epub -->
        <p:option name="epub" required="true"/>

        <!-- One of: "epub" (the entire epub), "opf" (the package file), "xhtml" (the content files), "svg" (vector graphics), "mo" (media overlays), "nav" (the navigation document). -->
        <p:option name="mode" required="false"/>

        <!-- The EPUB version to validate against. Default is "3". Values allowed are: "2" and "3". -->
        <p:option name="version" required="false"/>

        <!-- The epubcheck XML report. See Java implementation for more details about the grammar: https://github.com/IDPF/epubcheck/blob/master/src/main/java/com/adobe/epubcheck/util/XmlReportImpl.java#L176 -->
        <p:output port="result" sequence="true"/>

        <p:declare-step type="pxi:epubcheck">
            <!-- step declaration for the epubcheck-adapter implemented in java -->
            <p:option name="epub" required="true"/>
            <p:option name="mode" required="false"/>
            <p:option name="version" required="false"/>
            <p:output port="result" sequence="true"/>
        </p:declare-step>

        <p:declare-step type="pxi:epubcheck-locate-mimetype-dir">
            <p:documentation>Walks up the directory tree looking for the directory that the 'mimetype' file is stored in.</p:documentation>
            <p:option name="path" required="true"/>
            <p:output port="result"/>
            <p:variable name="parent" select="replace($path,'[^/]+/?$','')"/>
            <p:directory-list>
                <p:with-option name="path" select="$parent"/>
            </p:directory-list>
            <p:choose>
                <p:when test="/*/c:file/@name='mimetype'">
                    <p:delete match="/*/*"/>
                </p:when>
                <p:when test="matches($parent,'^\w+:/+[^/]+/')">
                    <pxi:epubcheck-locate-mimetype-dir>
                        <p:with-option name="path" select="$parent"/>
                    </pxi:epubcheck-locate-mimetype-dir>
                </p:when>
                <p:otherwise>
                    <p:identity>
                        <p:input port="source">
                            <p:inline>
                                <c:directory/>
                            </p:inline>
                        </p:input>
                    </p:identity>
                </p:otherwise>
            </p:choose>
        </p:declare-step>
        
        <p:variable name="_mode"
            select="if (p:value-available('mode') and not($mode='')) then $mode else if ((not(p:value-available('mode')) or $mode='') and matches(lower-case($epub),'\.(opf|xml)$')) then 'expanded' else 'epub'"/>
        <p:variable name="_version" select="if (p:value-available('version') and not($version='')) then $version else '3'"/>

        <p:choose>
            <p:when test="p:step-available('pxi:epubcheck')">

                <p:choose>
                    <p:when test="$_mode='expanded'">

                        <pxi:epubcheck-locate-mimetype-dir>
                            <p:with-option name="path" select="$epub"/>
                        </pxi:epubcheck-locate-mimetype-dir>

                        <pxi:epubcheck>
                            <p:with-option name="epub" select="if (/*/@xml:base) then /*/@xml:base else replace($epub,'[^/]+$','')"/>
                            <p:with-option name="mode" select="$_mode"/>
                            <p:with-option name="version" select="$_version"/>
                        </pxi:epubcheck>
                    </p:when>
                    <p:otherwise>
                        <pxi:epubcheck>
                            <p:with-option name="epub" select="$epub"/>
                            <p:with-option name="mode" select="$_mode"/>
                            <p:with-option name="version" select="$_version"/>
                        </pxi:epubcheck>
                    </p:otherwise>
                </p:choose>

            </p:when>
            <p:otherwise>
                <p:in-scope-names name="vars"/>
                <p:template>
                    <p:input port="template">
                        <p:inline>
                            <jhove xmlns="http://hul.harvard.edu/ois/xml/ns/jhove" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" name="epubcheck-adapter" release="x.x"
                                date="{tokenize(string(current-date()),'\+')[1]}">
                                <date>{current-dateTime()}</date>
                                <repInfo uri="{$epub}">
                                    <messages>
                                        <message>WARN: {$epub}: EpubCheck is not available. Are you trying to run epubcheck through XProc from outside of the DAISY Pipeline 2 framework?</message>
                                    </messages>
                                </repInfo>
                            </jhove>
                        </p:inline>
                    </p:input>
                    <p:input port="source">
                        <p:inline>
                            <irrelevant/>
                        </p:inline>
                    </p:input>
                    <p:input port="parameters">
                        <p:pipe step="vars" port="result"/>
                    </p:input>
                </p:template>

            </p:otherwise>
        </p:choose>
    </p:declare-step>

</p:library>
