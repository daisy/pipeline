<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0"
    xmlns="http://hul.harvard.edu/ois/xml/ns/jhove" xpath-default-namespace="http://hul.harvard.edu/ois/xml/ns/jhove" xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <xsl:output indent="yes"/>

    <xsl:param name="document-path" select="()"/>
    <xsl:param name="report-warning-as-error" select="'false'"/>

    <!--
        See: http://hul.harvard.edu/ois/xml/xsd/jhove/jhove.xsd for the full XSD.
        
        This stylesheet handles a subset of the full set of elements and attributes in the JHOVE XSD, as provided by the output of EpubCheck.
    -->

    <!--
        outline of epubcheck XML:
        
        jhove name="epubcheck" release="3.0.1" date="2013-05-27"
            date
            repInfo
                created
                lastModified
                format
                version
                status
                messages?
                    message*
                mimeType
                properties
                    property+
                        name
                        values arity="List" type="Property"
                            property+
                                name
                                values arity="Scalar" type="Date"
                                    value
    -->

    <xsl:template match="jhove">
        <d:document-validation-report>
            <xsl:if test="date">
                <xsl:comment select="concat(' The date/time at which epubcheck was invoked: ',date,' ')"/>
            </xsl:if>

            <d:document-info>
                <d:document-name>
                    <xsl:value-of select="repInfo/@uri"/>
                </d:document-name>
                <d:document-type>
                    <xsl:value-of select="repInfo/format"/>
                </d:document-type>
                <xsl:if test="$document-path">
                    <d:document-path>
                        <xsl:value-of select="$document-path"/>
                    </d:document-path>
                </xsl:if>
                <xsl:variable name="error-count-regex" select="if ($report-warning-as-error='true') then ', (WARN|ERROR|FATAL), ' else ', (ERROR|FATAL), '"/>
                <xsl:variable name="error-count" select="count(repInfo/messages/message[matches(.,$error-count-regex)])"/>
                <d:error-count>
                    <xsl:value-of select="$error-count"/>
                </d:error-count>
                <d:properties>
                    <xsl:if test="@name">
                        <xsl:call-template name="property">
                            <xsl:with-param name="name" select="'Tool Name'"/>
                            <xsl:with-param name="content" select="@name"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="@release">
                        <xsl:call-template name="property">
                            <xsl:with-param name="name" select="'Tool Version'"/>
                            <xsl:with-param name="content" select="@release"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="@date">
                        <xsl:call-template name="property">
                            <xsl:with-param name="name" select="'Tool Release Date'"/>
                            <xsl:with-param name="content" select="@date"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="repInfo/format">
                        <xsl:call-template name="property">
                            <xsl:with-param name="name" select="'Format'"/>
                            <xsl:with-param name="content" select="repInfo/format/text()"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="repInfo/version">
                        <xsl:call-template name="property">
                            <xsl:with-param name="name" select="'Format version'"/>
                            <xsl:with-param name="content" select="repInfo/version/text()"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="repInfo/status">
                        <!--
                            epubcheck status logic:
                            
                            if (fatalErrors.isEmpty() && errors.isEmpty()) { generateElement(ident, "status", "Well-formed"); }
                            else { generateElement(ident, "status", "Not well-formed"); }
                        -->
                        <xsl:call-template name="property">
                            <xsl:with-param name="name" select="'Status'"/>
                            <xsl:with-param name="content" select="if ($error-count = 0) then 'Well-formed' else 'Not well-formed'"/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:apply-templates select="repInfo/properties/property"/>
                </d:properties>
            </d:document-info>

            <d:reports>
                <d:report>
                    <xsl:variable name="messages">
                        <xsl:for-each select="repInfo/messages/message">
                            <xsl:variable name="id" select="substring-before(.,',')"/>
                            <xsl:variable name="severity" select="substring-after(substring-before(.,', ['),', ')">
                                <!-- FATAL, ERROR, WARN, HINT -->
                            </xsl:variable>
                            <xsl:variable name="message" select="substring-after(substring-before(.,']'),'[')"/>
                            <xsl:variable name="file" select="replace(substring-after(tokenize(.,'\]')[last()],', '),' \(.*?\)$','')"/>
                            <xsl:variable name="line" select="if (ends-with(.,')')) then replace(.,'.*\((\d+)-\d+\)$','$1') else ()"/>
                            <xsl:variable name="column" select="if (ends-with(.,')')) then replace(.,'.*\(\d+-(\d+)\)$','$1') else ()"/>

                            <xsl:element
                                name="{if ($severity='FATAL' or $severity='WARN' and $report-warning-as-error='true') then 'd:error' else if ($severity='FATAL') then 'd:exception' else if ($severity=('ERROR','WARN','HINT')) then concat('d:',lower-case($severity)) else 'd:warn'}">
                                <d:desc>
                                    <xsl:value-of select="concat($id,': ',$message)"/>
                                </d:desc>
                                <xsl:if test="$file">
                                    <d:file>
                                        <xsl:value-of select="$file"/>
                                    </d:file>
                                </xsl:if>
                                <xsl:if test="$line or $column">
                                    <d:location line="{$line}" column="{$column}"/>
                                </xsl:if>
                            </xsl:element>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:for-each select="distinct-values($messages/*/local-name())">
                        <xsl:variable name="name" select="."/>
                        <xsl:element name="d:{if ($name='exception') then 'exceptions' else if ($name='error') then 'errors' else if ($name='warn') then 'warnings' else 'hints'}">
                            <xsl:copy-of select="$messages/*[local-name()=$name]" exclude-result-prefixes="#all"/>
                        </xsl:element>
                    </xsl:for-each>
                </d:report>
            </d:reports>

        </d:document-validation-report>
    </xsl:template>

    <xsl:template name="property">
        <xsl:param name="name"/>
        <xsl:param name="content"/>
        <d:property name="{$name}" content="{$content}"/>
    </xsl:template>

    <xsl:template match="property">
        <d:property name="{name}">
            <xsl:if test="values/value">
                <xsl:attribute name="content" select="(values/value)[1]"/>
            </xsl:if>
            <xsl:apply-templates select="values/property"/>
        </d:property>
    </xsl:template>

</xsl:stylesheet>
