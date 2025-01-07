<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:daisy="http://www.daisy.org/ns/pipeline/"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:html="http://www.w3.org/1999/xhtml"
                exclude-inline-prefixes="#all"
                type="px:pef-store" name="store" version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Convert a PEF document to another braille file format and store to disk. Optionally also
        store a HTML preview, a PDF with ASCII braille, and the PEF itself.</p>
    </p:documentation>
    
    <p:input port="source" primary="true" px:media-type="application/x-pef+xml"/>
    
    <p:option name="output-dir" required="false" select="''"/> <!-- URI -->
    <p:option name="file-format" required="false" select="''"/> <!-- query -->
    <p:option name="name-pattern" required="false" select="''"/>
    <p:option name="single-volume-name" required="false" select="''"/>
    <p:option name="number-width" required="false" select="''"/>
    <p:option name="pef-href" required="false" select="''"/> <!-- URI -->
    <p:option name="preview-href" required="false" select="''"/> <!-- URI -->
    <p:option name="preview-table" required="false" select="''"/> <!-- query -->
    <p:option name="pdf-href" required="false" select="''"/> <!-- URI -->
    
    <p:import href="pef-to-html.convert.xpl">
        <p:documentation>
            px:pef-to-html.convert
        </p:documentation>
    </p:import>
    <p:import href="pef2text.xpl">
        <p:documentation>
            pef:pef2text
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:mkdir
            px:copy-resource
        </p:documentation>
    </p:import>
    
    <p:declare-step type="pxi:pef2pdf">
        <p:input port="source"/>
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
        <p:option name="table" required="true"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/pef/calabash/impl/PEF2PDFTextStep.java
        -->
    </p:declare-step>
    
    <!-- ============ -->
    <!-- STORE AS PEF -->
    <!-- ============ -->
    
    <p:choose px:progress=".01">
        <p:when test="not($pef-href='')">
            <p:identity px:message="Storing PEF">
                <p:input port="source">
                    <p:pipe step="store" port="source"/>
                </p:input>
            </p:identity>
            <p:store name="store.pef" px:message="Storing PEF to '{$pef-href}'" px:message-severity="DEBUG"
                     indent="true" encoding="utf-8" omit-xml-declaration="false">
                <p:with-option name="href" select="$pef-href"/>
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
    
    <!-- ===================== -->
    <!-- STORE AS BRAILLE FILE -->
    <!-- ==================== -->

    <p:choose px:progress=".15">
        <p:when test="not($output-dir='')" px:message="Storing braille file">
            <p:variable name="format" select="if (not($file-format='')) then $file-format else '(format:pef)'"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="store" port="source"/>
                </p:input>
            </p:identity>
            <!--
                delete daisy:ascii-braille-charset metadata and daisy:ascii attributes because we don't
                want it to end up in the output if the format is PEF (in which case pef:pef2text
                simply serializes the input PEF).
            -->
            <p:delete match="/pef:pef/pef:head/pef:meta/daisy:ascii-braille-charset|
                             pef:row/@daisy:ascii"/>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:inline xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                        <xsl:stylesheet version="2.0">
                            <xsl:template match="@*|*">
                                <xsl:copy copy-namespaces="no">
                                    <xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/')]">
                                        <xsl:sequence select="."/>
                                    </xsl:for-each>
                                    <xsl:apply-templates select="@*|node()"/>
                                </xsl:copy>
                            </xsl:template>
                            <xsl:template match="text()|processing-instruction()|comment()">
                                <xsl:sequence select="."/>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <pef:pef2text px:progress="1" px:message="Storing braille file to '{$output-dir}' in format '{$format}'" px:message-severity="DEBUG">
                <p:with-option name="output-dir" select="$output-dir"/>
                <p:with-option name="name-pattern" select="$name-pattern"/>
                <p:with-option name="single-volume-name" select="$single-volume-name"/>
                <p:with-option name="number-width" select="$number-width"/>
                <p:with-option name="file-format" select="$format"/>
            </pef:pef2text>
        </p:when>
        <p:otherwise>
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>
    
    <!-- ==================== -->
    <!-- STORE AS PEF PREVIEW -->
    <!-- ==================== -->
    
    <p:choose px:progress=".64">
        <p:when test="not($preview-href='')" px:message="Storing HTML preview">
            <p:variable name="table" select="if (not($preview-table=''))
                                             then $preview-table
                                             else '(id:&quot;org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US&quot;)'"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="store" port="source"/>
                </p:input>
            </p:identity>
            <px:pef-to-html.convert px:message="Storing HTML preview to '{$preview-href}' using table '{$table}'"
                                    px:message-severity="DEBUG" px:progress="62/64">
                <p:with-option name="table" select="$table"/>
            </px:pef-to-html.convert>
            <p:store px:message="Storing HTML preview as '{$preview-href}'" px:message-severity="DEBUG" px:progress="1/64"
                     indent="false"
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
            <px:copy-resource px:message="Copying braille font file (odt2braille8.ttf) to HTML preview directory"
                              px:message-severity="DEBUG" px:progress="1/64"
                              fail-on-error="true" cx:depends-on="mkdir">
                <p:with-option name="href" select="resolve-uri('../odt2braille8.ttf')"/>
                <p:with-option name="target" select="resolve-uri('odt2braille8.ttf', $preview-href)"/>
            </px:copy-resource>
            <!--<px:copy-resource fail-on-error="true" cx:depends-on="mkdir">
                <p:with-option name="href" select="resolve-uri('../NotCourierSans-Bold.otf')"/>
                <p:with-option name="target" select="resolve-uri('NotCourierSans-Bold.otf', $preview-href)"/>
            </px:copy-resource>-->
            <px:copy-resource fail-on-error="true" cx:depends-on="mkdir">
                <p:with-option name="href" select="resolve-uri('../NotCourierSans-Bold.ttf')"/>
                <p:with-option name="target" select="resolve-uri('NotCourierSans-Bold.ttf', $preview-href)"/>
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
        <p:otherwise px:message="Not including HTML preview" px:message-severity="DEBUG">
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>
    
    <!-- ============ -->
    <!-- STORE AS PDF -->
    <!-- ============ -->
    
    <p:choose px:progress=".20">
        <p:when test="not($pdf-href='')" px:message="Storing PDF">
            <p:choose>
                <p:when test="not($preview-table='')">
                    <p:choose>
                        <p:when test="p:step-available('pxi:pef2pdf')">
                            <p:identity>
                                <p:input port="source">
                                    <p:pipe step="store" port="source"/>
                                </p:input>
                            </p:identity>
                            <pxi:pef2pdf px:message="Storing PDF to '{$pdf-href}' using table '{$preview-table}'" px:message-severity="DEBUG">
                                <p:with-option name="href" select="$pdf-href"/>
                                <p:with-option name="table" select="$preview-table"/>
                            </pxi:pef2pdf>
                        </p:when>
                        <p:otherwise>
                            <p:sink px:message="Not including PDF: wkhtmltopdf was not found on the system" px:message-severity="WARN">
                                <p:input port="source">
                                    <p:empty/>
                                </p:input>
                            </p:sink>
                        </p:otherwise>
                    </p:choose>
                </p:when>
                <p:otherwise>
                    <p:sink px:message="Not including PDF: an ASCII table must be provided" px:message-severity="WARN">
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:sink>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:otherwise px:message="Not including PDF" px:message-severity="DEBUG">
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
