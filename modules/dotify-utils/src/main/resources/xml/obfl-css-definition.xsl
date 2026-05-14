<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:o="org.daisy.pipeline.braille.dotify.saxon.impl.ObflStyle"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all">

    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>

    <xsl:param name="initial-braille-charset" select="'unicode'"/>

    <xsl:variable name="obfl-properties" as="xs:string*"
                  select="('margin-left',           'page-break-before', 'text-indent', 'text-transform',      '-obfl-vertical-align',
                           'margin-right',          'page-break-after',  'text-align',  'braille-charset',     '-obfl-vertical-position',
                           'margin-top',            'page-break-inside', 'line-height', 'hyphens',             '-obfl-toc-range',
                           'margin-bottom',         'orphans',                          'hyphenate-character',
                           'padding-left',          'widows',                           'white-space',         '-obfl-table-col-spacing',
                           'padding-right',         'volume-break-before',              'word-spacing',        '-obfl-table-row-spacing',
                           'padding-top',           'volume-break-after',               'letter-spacing',      '-obfl-preferred-empty-space',
                           'padding-bottom',        'volume-break-inside',                                     '-obfl-use-when-collection-not-empty',
                           'border-left-pattern',   'border-left-style',                                       '-obfl-underline',
                           'border-right-pattern',  'border-right-style',                                      '-obfl-keep-with-previous-sheets',
                           'border-top-pattern',    'border-top-style',                                        '-obfl-keep-with-next-sheets',
                           'border-bottom-pattern', 'border-bottom-style',                                     '-obfl-scenario-cost',
                                                                                                               '-obfl-right-text-indent'
                           )"/>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:sequence select="."/>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:next-match>
            <xsl:with-param name="result-style" tunnel="yes"
                            select="o:of(css:parse-stylesheet(concat('braille-charset: ',$initial-braille-charset)))"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="css:box">
        <xsl:param name="source-style" as="item()?" select="()" tunnel="yes"/>
        <xsl:param name="result-style" as="item()?" select="()" tunnel="yes"/>
        <xsl:variable name="source-style" as="item()" select="css:parse-stylesheet(@style,$source-style)"/>
        <xsl:variable name="source-style" as="item()" select="s:merge(($source-style,
                                                                       @css:*[replace(local-name(),'^_','-')=$obfl-properties]
                                                                             /css:parse-stylesheet(.)))"/>
        <xsl:variable name="obfl-style" as="item()" select="o:of($source-style)"/>
        <xsl:copy>
            <xsl:sequence select="@* except (@style|@css:*[replace(local-name(),'^_','-')=$obfl-properties])"/>
            <xsl:variable name="obfl-style" as="item()*">
                <xsl:variable name="context" as="element()" select="."/>
                <xsl:for-each select="s:iterate($obfl-style)">
                    <xsl:variable name="property-name" as="xs:string" select="s:property(.)"/>
                    <xsl:choose>
                        <!--
                            skip OBFL properties that do not apply in the current context
                            
                            properties apply if their corresponding CSS property applies, except:
                            
                            - the (inherited) properties line-height, text-align, text-indent and
                              -obfl-right-text-indent only apply on block boxes that have no child block boxes
                            - text-align, text-indent, page-break-after, page-break-before, page-break-inside,
                              volume-break-after, volume-break-before and volume-break-inside do not apply on
                              table boxes
                            - -obfl-table-col-spacing, -obfl-table-row-spacing and -obfl-preferred-empty-space
                              apply on table boxes only
                            - -obfl-use-when-collection-not-empty applies on boxes with a flow property
                            - other -obfl- prefixed properties apply on any box
                        -->
                        <xsl:when test="if ($property-name=('text-transform','braille-charset','hyphens','hyphenate-character',
                                                            'word-spacing','white-space','letter-spacing'))
                                        then true()
                                        else if (matches($property-name,'^(border|margin|padding)-'))
                                        then $context/@type=('block','table','table-cell')
                                        else if ($property-name='line-height')
                                        then $context[@type=('block','table') and not(descendant::css:box[@type='block'])]
                                        else if ($property-name=('text-indent','-obfl-right-text-indent','text-align'))
                                        then $context[@type=('block','table-cell') and not(descendant::css:box[@type='block'])]
                                        else if ($property-name=('-obfl-table-col-spacing',
                                                                 '-obfl-table-row-spacing',
                                                                 '-obfl-preferred-empty-space'))
                                        then $context/@type='table'
                                        else if ($property-name='-obfl-use-when-collection-not-empty')
                                        then exists($context/parent::css:_[@css:flow])
                                        else $context/@type='block'">
                            <xsl:sequence select="."/>
                        </xsl:when>
                        <xsl:when test="$property-name=('text-align','text-indent','-obfl-right-text-indent')">
                            <!-- don't show a warning if the property is inherited on child boxes -->
                        </xsl:when>
                        <xsl:when test="not(matches($property-name,'^(page|volume)-break-')
                                            and $context/@type='table')">
                            <!-- don't show a warning if the corresponding CSS property does not apply either -->
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:message select="concat(string(.),' not supported (display: ',$context/@type,')')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="obfl-style" as="item()?" select="s:merge($obfl-style)"/>
            <xsl:sequence select="s:toAttributes($obfl-style,$result-style)"/>
            <xsl:apply-templates>
                <xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
                <xsl:with-param name="result-style" tunnel="yes" select="$obfl-style"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
