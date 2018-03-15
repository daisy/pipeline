<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:html="http://www.w3.org/1999/xhtml"
                exclude-inline-prefixes="#all"
                type="pef:store" name="store" version="1.0">
    
    <p:input port="source" primary="true" px:media-type="application/x-pef+xml"/>
    
    <p:option name="href" required="true"/>
    <p:option name="preview-href" required="false" select="''"/>
    <p:option name="brf-dir-href" required="false" select="''"/>
    <p:option name="brf-name-pattern" required="false" select="''"/>
    <p:option name="brf-single-volume-name" required="false" select="''"/>
    <p:option name="brf-number-width" required="false" select="''"/>
    <p:option name="brf-table" required="false" select="''"/>
    <p:option name="brf-file-format" required="false" select="''"/>
    
    <p:import href="pef-to-html.convert.xpl"/>
    <p:import href="pef2text.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <!-- ============ -->
    <!-- STORE AS PEF -->
    <!-- ============ -->
    
    <px:message severity="DEBUG">
        <p:with-option name="message" select="concat('Storing PEF as ''', $href,'''')"/>
    </px:message>
    <p:store indent="true" encoding="utf-8" omit-xml-declaration="false">
        <p:input port="source">
            <p:pipe step="store" port="source"/>
        </p:input>
        <p:with-option name="href" select="$href"/>
    </p:store>
    
    <!-- ============ -->
    <!-- STORE AS BRF -->
    <!-- ============ -->

    <p:choose>
        <p:when test="not($brf-dir-href='')">
            <p:identity>
                <p:input port="source">
                    <p:pipe step="store" port="source"/>
                </p:input>
            </p:identity>
            <px:message severity="DEBUG">
                <p:with-option name="message" select="concat('Storing BRF in ''', $brf-dir-href, '''')"/>
            </px:message>
            <p:choose>
                <p:when test="not($brf-file-format='')">
                    <!--
                        TODO: try with and without brf-table?
                    -->
                    <pef:pef2text>
                        <p:with-option name="dir-href" select="$brf-dir-href"/>
                        <p:with-option name="name-pattern" select="$brf-name-pattern"/>
                        <p:with-option name="single-volume-name" select="$brf-single-volume-name"/>
                        <p:with-option name="number-width" select="$brf-number-width"/>
                        <p:with-option name="file-format" select="$brf-file-format"/>
                    </pef:pef2text>
                </p:when>
                <p:when test="not($brf-table='')">
                    <pef:pef2text line-breaks="DEFAULT" pad="BOTH">
                        <p:with-option name="dir-href" select="$brf-dir-href"/>
                        <p:with-option name="name-pattern" select="$brf-name-pattern"/>
                        <p:with-option name="single-volume-name" select="$brf-single-volume-name"/>
                        <p:with-option name="number-width" select="$brf-number-width"/>
                        <p:with-option name="table" select="$brf-table"/>
                    </pef:pef2text>
                </p:when>
                <p:otherwise>
                    <pef:pef2text>
                        <p:with-option name="dir-href" select="$brf-dir-href"/>
                        <p:with-option name="name-pattern" select="$brf-name-pattern"/>
                        <p:with-option name="single-volume-name" select="$brf-single-volume-name"/>
                        <p:with-option name="number-width" select="$brf-number-width"/>
                        <p:with-option name="file-format"
                                       select="'(table:&quot;org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US&quot;)
                                                (line-breaks:DEFAULT)
                                                (pad:BOTH)'"/>
                    </pef:pef2text>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
            <px:message severity="DEBUG" message="Not storing as BRF"/>
            <p:sink/>
        </p:otherwise>
    </p:choose>
    
    <!-- ==================== -->
    <!-- STORE AS PEF PREVIEW -->
    <!-- ==================== -->
    
    <p:choose>
        <p:when test="not($preview-href='')">
            <p:variable name="table" select="if (not($brf-table=''))
                                             then $brf-table
                                             else '(id:&quot;org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US&quot;)'"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="store" port="source"/>
                </p:input>
            </p:identity>
            <px:message severity="DEBUG">
                <p:with-option name="message" select="concat('Converting PEF to HTML preview using the BRF table ''',$table,'''')"/>
            </px:message>
            <px:pef-to-html.convert>
                <p:with-option name="table" select="$table"/>
            </px:pef-to-html.convert>
            <px:message severity="DEBUG">
                <p:with-option name="message" select="concat('Storing HTML preview as ''', $preview-href, '''')"/>
            </px:message>
            <p:store indent="false"
                     encoding="utf-8"
                     method="xhtml"
                     omit-xml-declaration="false"
                     doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
                     doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <p:with-option name="href" select="$preview-href"/>
            </p:store>
            <!--
                because copy-resource does not create parent directory
            -->
            <px:mkdir name="mkdir">
                <p:with-option name="href" select="resolve-uri('./',$preview-href)"/>
            </px:mkdir>
            <p:identity>
                <p:input port="source">
                    <p:inline>
                        <irrelevant/>
                    </p:inline>
                </p:input>
            </p:identity>
            <px:message severity="DEBUG" message="Copying braille font file (odt2braille8.ttf) to HTML preview directory"/>
            <px:copy-resource fail-on-error="true" cx:depends-on="mkdir">
                <p:with-option name="href" select="resolve-uri('../odt2braille8.ttf')"/>
                <p:with-option name="target" select="resolve-uri('odt2braille8.ttf', $preview-href)"/>
            </px:copy-resource>
            <px:copy-resource fail-on-error="true" cx:depends-on="mkdir">
                <p:with-option name="href" select="resolve-uri('pef-preview.css')"/>
                <p:with-option name="target" select="resolve-uri('pef-preview.css', $preview-href)"/>
            </px:copy-resource>
            <px:copy-resource fail-on-error="true" cx:depends-on="mkdir">
                <p:with-option name="href" select="resolve-uri('pef-preview.js')"/>
                <p:with-option name="target" select="resolve-uri('pef-preview.js', $preview-href)"/>
            </px:copy-resource>
            <p:sink/>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
            <px:message severity="DEBUG" message="Not including HTML preview"/>
            <p:sink/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
