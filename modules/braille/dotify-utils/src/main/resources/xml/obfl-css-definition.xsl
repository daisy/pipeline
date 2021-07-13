<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:new="css:new-definition"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils">
  
    <xsl:variable name="new:properties" as="xs:string*"
                  select="('margin-left',           'page-break-before', 'text-indent', 'text-transform', '-obfl-vertical-align',
                           'margin-right',          'page-break-after',  'text-align',  'hyphens',        '-obfl-vertical-position',
                           'margin-top',            'page-break-inside', 'line-height', 'white-space',    '-obfl-toc-range',
                           'margin-bottom',         'orphans',                          'word-spacing',   '-obfl-list-of-references-range',
                           'padding-left',          'widows',                           'letter-spacing', '-obfl-table-col-spacing',
                           'padding-right',         'volume-break-before',                                '-obfl-table-row-spacing',
                           'padding-top',           'volume-break-after',                                 '-obfl-preferred-empty-space',
                           'padding-bottom',        'volume-break-inside',                                '-obfl-use-when-collection-not-empty',
                           'border-left-pattern',   'border-left-style',                                  '-obfl-underline',
                           'border-right-pattern',  'border-right-style',                                 '-obfl-keep-with-previous-sheets',
                           'border-top-pattern',    'border-top-style',                                   '-obfl-keep-with-next-sheets',
                           'border-bottom-pattern', 'border-bottom-style',                                '-obfl-scenario-cost',
                                                                                                          '-obfl-right-text-indent'
                           )"/>
    
    <xsl:variable name="_OBFL_KEEP_FN_RE">-obfl-keep\(\s*[1-9]\s*\)</xsl:variable>
    
    <xsl:function name="new:is-valid" as="xs:boolean">
        <xsl:param name="css:property" as="element()"/>
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="valid" as="xs:boolean"
                      select="new:applies-to($css:property/@name, $context)
                              and (
                                if ($css:property/@name='-obfl-vertical-align')
                                then $css:property/@value=('before','center','after')
                                else if ($css:property/@name=('-obfl-vertical-position',
                                                              '-obfl-table-col-spacing',
                                                              '-obfl-table-row-spacing',
                                                              '-obfl-preferred-empty-space'))
                                then matches($css:property/@value,'^auto|0|[1-9][0-9]*$')
                                else if ($css:property/@name='-obfl-toc-range')
                                then ($context/@css:_obfl-toc and $css:property/@value=('document','volume'))
                                else if ($css:property/@name='-obfl-list-of-references-range')
                                then ($context/@css:_obfl-list-of-references and $css:property/@value=('document','volume'))
                                else if ($css:property/@name='-obfl-use-when-collection-not-empty')
                                then matches($css:property/@value,re:exact($css:IDENT_RE))
                                else if ($css:property/@name='-obfl-underline')
                                then matches($css:property/@value,re:exact(re:or(($css:BRAILLE_CHAR_RE,'none'))))
                                else if ($css:property/@name=('-obfl-keep-with-previous-sheets',
                                                              '-obfl-keep-with-next-sheets'))
                                then matches($css:property/@value,'^[0-9]$')
                                else if ($css:property/@name='volume-break-inside')
                                then matches($css:property/@value,re:exact(re:or(('auto',$_OBFL_KEEP_FN_RE))))
                                else if ($css:property/@name='-obfl-scenario-cost')
                                then matches($css:property/@value,re:exact(re:or(('none',$css:INTEGER_RE,$css:VENDOR_PRF_FN_RE))))
                                else if ($css:property/@name=('volume-break-after',
                                                              'volume-break-before'))
                                then $css:property/@value=('auto','always')
                                else (
                                  css:is-valid($css:property)
                                  and not($css:property/@value=('inherit','initial'))
                                )
                              )"/>
        <!--
            TODO: move to css:new-definition?
        -->
        <xsl:if test="not($valid)
                      and not($css:property/@value=('inherit','initial'))
                      and not(css:is-inherited($css:property/@name))">
            <xsl:message select="concat($css:property/@name,': ',$css:property/@value,' not supported (display: ',$context/@type,')')"/>
        </xsl:if>
        <xsl:sequence select="$valid"/>
    </xsl:function>
    
    <xsl:function name="new:initial-value" as="xs:string">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:sequence select="if ($property='-obfl-vertical-align')
                              then 'after'
                              else if ($property='-obfl-vertical-position')
                              then 'auto'
                              else if ($property=('-obfl-toc-range','-obfl-list-of-references-range'))
                              then 'document'
                              else if ($property=('-obfl-table-col-spacing','-obfl-table-row-spacing'))
                              then '0'
                              else if ($property='-obfl-preferred-empty-space')
                              then '2'
                              else if ($property='-obfl-use-when-collection-not-empty')
                              then 'normal'
                              else if ($property='-obfl-underline')
                              then 'none'
                              else if ($property=('-obfl-keep-with-previous-sheets','-obfl-keep-with-next-sheets'))
                              then '0'
                              else if ($property='-obfl-scenario-cost')
                              then 'none'
                              else css:initial-value($property)"/>
    </xsl:function>
    
    <xsl:function name="new:is-inherited" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:sequence select="$property=('text-transform','hyphens','word-spacing')"/>
    </xsl:function>
    
    <xsl:function name="new:applies-to" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:sequence select="$property=('text-transform','hyphens','word-spacing')
                              or (
                                if (matches($property,'^(border|margin|padding)-'))
                                then $context/@type=('block','table','table-cell')
                                else if ($property='line-height')
                                then $context/@type=('block','table')
                                else if ($property=('text-indent','text-align'))
                                then $context/@type=('block','table-cell')
                                else if ($property=('-obfl-table-col-spacing',
                                                    '-obfl-table-row-spacing',
                                                    '-obfl-preferred-empty-space'))
                                then $context/@type='table'
                                else if ($property='-obfl-use-when-collection-not-empty')
                                then exists($context/parent::css:_[@css:flow])
                                else $context/@type='block'
                              )"/>
    </xsl:function>
    
</xsl:stylesheet>
