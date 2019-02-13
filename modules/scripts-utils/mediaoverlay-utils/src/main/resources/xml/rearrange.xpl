<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="rearrange" type="px:mediaoverlay-rearrange" version="1.0" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:err="http://www.w3.org/ns/xproc-error" xmlns:mo="http://www.w3.org/ns/SMIL" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">

    <p:input port="mediaoverlay" primary="true" sequence="true"/>
    <p:input port="content" sequence="true"/>
    <p:output port="result" sequence="true" primary="true"/>

    <p:group name="rearrange.mediaoverlay-map">
        <p:output port="result"/>
        <!--TODO shouldn't need to rely on @xml:base -->
        <p:for-each>
            <p:add-xml-base all="true" relative="false"/>
        </p:for-each>
        <p:wrap-sequence wrapper="smil-map" wrapper-namespace="http://www.daisy.org/ns/pipeline/tmp"/>
        <p:xslt name="rearrange.mediaoverlay-annotated">
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:inline>
                    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:mo="http://www.w3.org/ns/SMIL" xmlns:di="http://www.daisy.org/ns/pipeline/tmp">
                        <xsl:template match="@*|node()">
                            <xsl:copy>
                                <xsl:apply-templates select="@*|node()"/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="/di:smil-map">
                            <!--
                                avoids the SXXP0005 warning
                                see: http://sourceforge.net/p/saxon/mailman/saxon-help/thread/E68C5E59-6E71-4E91-9B1D-29B4B9B0F290@saxonica.com/
                            -->
                            <xsl:copy>
                                <xsl:apply-templates select="@*|node()"/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="mo:text">
                            <xsl:copy>
                                <xsl:copy-of select="@*"/>
                                <xsl:attribute name="fragment" select="if (contains(@src,'#')) then substring-after(@src,'#') else ''"/>
                                <xsl:attribute name="src" select="resolve-uri(substring-before(@src,'#'),base-uri())"/>
                                <xsl:apply-templates select="node()"/>
                            </xsl:copy>
                        </xsl:template>
                    </xsl:stylesheet>
                </p:inline>
            </p:input>
        </p:xslt>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="rearrange.prepare.xsl"/>
            </p:input>
        </p:xslt>
        <p:identity px:message="created annotated mediaoverlay" px:message-severity="DEBUG"/>
    </p:group>
    <p:sink/>

    <p:for-each name="rearrange.for-each">
        <p:output port="mediaoverlay" sequence="true" primary="true"/>
        <p:iteration-source>
            <p:pipe port="content" step="rearrange"/>
        </p:iteration-source>
        <p:variable name="content-base" select="base-uri(/*)"/>

        <p:add-attribute match="//*" attribute-name="xml:base" name="rearrange.for-each.content">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
        <p:wrap-sequence wrapper="content-and-mediaoverlay" wrapper-namespace="http://www.daisy.org/ns/pipeline/tmp">
            <p:input port="source">
                <p:pipe port="result" step="rearrange.for-each.content"/>
                <p:pipe port="result" step="rearrange.mediaoverlay-map"/>
            </p:input>
        </p:wrap-sequence>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="rearrange.xsl"/>
            </p:input>
        </p:xslt>
        <p:delete match="//mo:seq[not(descendant::mo:par)]"/>

        <p:documentation>generate ids</p:documentation>
        <p:xslt>
            <p:with-param name="iteration-position" select="p:iteration-position()"/>
            <p:input port="stylesheet">
                <p:document href="generate-ids.xsl"/>
            </p:input>
        </p:xslt>

        <p:documentation>resolve relative uris</p:documentation>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="resolve-relative-uris.xsl"/>
            </p:input>
        </p:xslt>

        <p:documentation>if there is only one top-level seq; turn it into a body element</p:documentation>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="conditionally-join-toplevel-seq-with-body.xsl"/>
            </p:input>
        </p:xslt>

        <p:identity px:message="created media overlay for {$content-base}" px:message-severity="DEBUG"/>
    </p:for-each>

</p:declare-step>
