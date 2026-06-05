<?xml version="1.0" encoding="UTF-8"?>

<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" xmlns:m="http://www.w3.org/1998/Math/MathML"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <ns uri="http://www.daisy.org/z3986/2005/dtbook/" prefix="dtb"/>
    <ns uri="http://www.w3.org/1998/Math/MathML" prefix="m"/>

    <xsl:key name="notes" match="dtb:note[@id]" use="@id"/>
    <xsl:key name="annotations" match="dtb:annotation[@id]" use="@id"/>

    <!-- 
        because we override the ID datatype with NMTOKEN in the dtbook-mathml-integration schema,
        we need to double check that all @id values are unique
    -->
    <pattern id="id-unique">
        <let name="id-set" value="//*[@id]"/>
        <rule context="*[@id]">
            <assert test="count($id-set[@id = current()/@id]) = 1">Duplicate ID '<value-of
                    select="current()/@id"/>'</assert>
        </rule>
    </pattern>

    <!-- ****************************************************** -->
    <!-- Patterns in this section were imported from Pipeline 1 -->
    <!-- ****************************************************** -->


    <!-- removing these rules for NIMAS compliance (requested by APH) -->
    <!--
    <pattern id="dtbook_MetaUid">
        <rule context="dtb:head">
            <assert test="count(dtb:meta[@name='dtb:uid'])=1"> There must be exactly one
                dtb:uid metadata item. </assert>
        </rule>
        <rule context="dtb:meta[@name='dtb:uid']">
            <assert test="string-length(normalize-space(@content))!=0"> Content of dtb:uid metadata
                may not be empty. </assert>
        </rule>
    </pattern>

    <pattern id="dtbook_MetaTitle">
        <rule context="dtb:head">
            <assert test="count(dtb:meta[@name='dc:Title'])>0"> There must be at least one dc:Title
                metadata item. </assert>
        </rule>
        <rule context="dtb:meta[@name='dc:Title']">
            <assert test="string-length(normalize-space(@content))!=0"> Content of dc:Title metadata
                may not be empty. </assert>
        </rule>
    </pattern>
-->
    
    <!-- added for NIMAS -->
    <pattern id="dtbook_NimasHeadMeta">
        <rule context="dtb:meta">
            <assert test="count(*) &gt; 0">The meta element must be empty.</assert>
        </rule>
    </pattern>
    
    <pattern id="dtbook_idrefNote">
        <rule context="dtb:noteref">
            <assert test="contains(@idref, '#')"> noteref URI value does not contain a fragment
                identifier. </assert>
            <report
                test="contains(@idref, '#') and string-length(substring-before(@idref, '#'))=0 and count(key('notes',substring(current()/@idref,2)))!=1"
                > noteref URI value does not resolve to a note element. </report>
            <!-- Do not perform any checks on external note references since you cannot set a URIResolver in Jing
	       <sch:report test="string-length(substring-before(@idref, '#'))>0 and not(document(substring-before(@idref, '#')))">External document does not exist</sch:report>
	       <sch:report test="string-length(substring-before(@idref, '#'))>0 and count(document(substring-before(@idref, '#'))//dtb:note[@id=substring-after(current()/@idref, '#')])!=1">Incorrect external fragment identifier</sch:report>
	        -->
        </rule>
    </pattern>

    <pattern id="dtbook_idrefAnnotation">
        <rule context="dtb:annoref">
            <assert test="contains(@idref, '#')"> annoref URI value does not contain a fragment
                identifier. </assert>
            <report
                test="contains(@idref, '#') and string-length(substring-before(@idref, '#'))=0 and count(key('annotations',substring(current()/@idref,2)))!=1"
                > annoref URI value does not resolve to a annotation element. </report>
            <!-- Do not perform any checks on external note references since you cannot set a URIResolver in Jing
	        <sch:report test="string-length(substring-before(@idref, '#'))>0 and not(document(substring-before(@idref, '#')))">External document does not exist</sch:report>
	        <sch:report test="string-length(substring-before(@idref, '#'))>0 and count(document(substring-before(@idref, '#'))//dtb:annotation[@id=substring-after(current()/@idref, '#')])!=1">Incorrect external fragment identifier</sch:report>
	        -->
        </rule>
    </pattern>

    <pattern id="dtbook_internalLinks">
        <rule context="dtb:a[starts-with(@href, '#')]">
            <assert test="count(//dtb:*[@id=substring(current()/@href, 2)])=1"> Internal link does
                not resolve. </assert>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_enumAttrInList">
        <rule context="dtb:list">
            <report test="@enum and @type!='ol'"> The enum attribute is only allowed in numbered
                lists. </report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_depthList">
        <rule context="dtb:list">
            <report test="@depth and @depth!=count(ancestor-or-self::dtb:list)"> The depth attribute
                on list element does not contain the list wrapping level. </report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_headersThTd">
        <rule context="dtb:*[@headers and (self::dtb:th or self::dtb:td)]">
            <assert
                test="
                count(
                ancestor::dtb:table//dtb:th/@id[contains( concat(' ',current()/@headers,' '), concat(' ',normalize-space(),' ') )]
                ) = 
                string-length(normalize-space(@headers)) - string-length(translate(normalize-space(@headers), ' ','')) + 1
                "
                > Not all the tokens in the headers attribute match the id attributes of 'th'
                elements in this or a parent table. </assert>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_imgrefProdnote">
        <rule context="dtb:prodnote[@imgref]">
            <assert
                test="
                count(
                //dtb:img/@id[contains( concat(' ',current()/@imgref,' '), concat(' ',normalize-space(),' ') )]
                ) = 
                string-length(normalize-space(@imgref)) - string-length(translate(normalize-space(@imgref), ' ','')) + 1
                "
                > Not all the tokens in the imgref attribute match the id attributes of 'img'
                elements. </assert>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_imgrefCaption">
        <rule context="dtb:caption[@imgref]">
            <assert
                test="
                count(
                //dtb:img/@id[contains( concat(' ',current()/@imgref,' '), concat(' ',normalize-space(),' ') )]
                ) = 
                string-length(normalize-space(@imgref)) - string-length(translate(normalize-space(@imgref), ' ','')) + 1
                "
                > Not all the tokens in the imgref attribute match the id attributes of 'img'
                elements. </assert>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_accesskeyTabindex">
        <rule context="dtb:a">
            <report test="@accesskey and string-length(@accesskey)!=1">The accesskey attribute value
                is not 1 character long.</report>
            <report test="@tabindex and string-length(translate(@width,'0123456789',''))!=0">The
                tabindex attribute value is not expressed in numbers.</report>
            <report test="@accesskey and count(//dtb:a/@accesskey=@accesskey)!=1">The accesskey
                attribute value is not unique within the document.</report>
            <report test="@tabindex and count(//dtb:a/@tabindex=@tabindex)!=1">The tabindex
                attribute value is not unique within the document.</report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_charAttribute">
        <rule
            context="dtb:*[self::dtb:col   or self::dtb:colgroup or self::dtb:tbody or self::dtb:td or 
            self::dtb:tfoot or self::dtb:th       or self::dtb:thead or self::dtb:tr]">
            <report test="@char and string-length(@char)!=1">The char attribute value is not 1
                character long.</report>
            <report test="@char and @align!='char'">char attribute may only occur when align
                attribute value is 'char'.</report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_imgWidthHeight">
        <rule context="dtb:img">
            <assert
                test="not(@width) or 
                string-length(translate(@width,'0123456789',''))=0 or
                (contains(@width,'%') and substring-after(@width,'%')='' and translate(@width,'%0123456789','')='' and string-length(@width)>=2)"
                >The image width is not expressed in pixels or percentage.</assert>
            <assert
                test="not(@height) or 
                string-length(translate(@height,'0123456789',''))=0 or
                (contains(@height,'%') and substring-after(@height,'%')='' and translate(@height,'%0123456789','')='' and string-length(@height)>=2)"
                >The image height is not expressed in pixels or percentage.</assert>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_tableAttributes">
        <rule context="dtb:table">
            <assert
                test="not(@width) or 
                string-length(translate(@width,'0123456789',''))=0 or
                (contains(@width,'%') and substring-after(@width,'%')='' and translate(@width,'%0123456789','')='' and string-length(@width)>=2)"
                >Table width is not expressed in pixels or percentage.</assert>
            <assert
                test="not(@cellspacing) or 
                string-length(translate(@cellspacing,'0123456789',''))=0 or
                (contains(@cellspacing,'%') and substring-after(@cellspacing,'%')='' and translate(@cellspacing,'%0123456789','')='' and string-length(@cellspacing)>=2)"
                >Table cellspacing is not expressed in pixels or percentage.</assert>
            <assert
                test="not(@cellpadding) or 
                string-length(translate(@cellpadding,'0123456789',''))=0 or
                (contains(@cellpadding,'%') and substring-after(@cellpadding,'%')='' and translate(@cellpadding,'%0123456789','')='' and string-length(@cellpadding)>=2)"
                >Table cellpadding is not expressed in pixels or percentage.</assert>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_startAttrInList">
        <rule context="dtb:list">
            <report test="@start and @type!='ol'">The start attribute occurs in a non-numbered
                list.</report>
            <report test="@start='' or string-length(translate(@start,'0123456789',''))!=0">The
                start attribute is not a non negative number.</report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_dcMetadata">
        <rule context="dtb:meta">
            <report
                test="starts-with(@name, 'dc:') and not(@name='dc:Title' or @name='dc:Subject' or @name='dc:Description' or
                @name='dc:Type' or @name='dc:Source' or @name='dc:Relation' or 
                @name='dc:Coverage' or @name='dc:Creator' or @name='dc:Publisher' or 
                @name='dc:Contributor' or @name='dc:Rights' or @name='dc:Date' or 
                @name='dc:Format' or @name='dc:Identifier' or @name='dc:Language')"
                >Unrecognized Dublin Core metadata name.</report>
            <report
                test="starts-with(@name, 'DC:') or starts-with(@name, 'Dc:') or starts-with(@name, 'dC:')"
                >Unrecognized Dublin Core metadata prefix.</report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern id="dtbook_spanColColgroup">
        <rule context="dtb:*[self::dtb:col or self::dtb:colgroup]">
            <report
                test="@span and (translate(@span,'0123456789','')!='' or starts-with(@span,'0'))"
                >span attribute is not a positive integer.</report>
        </rule>
    </pattern>

    <!-- MG20061101: added as a consequence of zedval feature request #1565049 -->
    <pattern>
        <rule context="dtb:*[self::dtb:td or self::dtb:th]">
            <report
                test="@rowspan and (translate(@rowspan,'0123456789','')!='' or starts-with(@rowspan,'0'))"
                > The rowspan attribute value is not a positive integer.</report>
            <report
                test="@colspan and (translate(@colspan,'0123456789','')!='' or starts-with(@colspan,'0'))"
                > The colspan attribute value is not a positive integer.</report>
            <report
                test="@rowspan and number(@rowspan) > count(parent::dtb:tr/following-sibling::dtb:tr)+1"
                > The rowspan attribute value is larger than the number of rows left in the
                table.</report>
        </rule>
    </pattern>

    <!-- MG20070522: added as a consequence of zedval feature request #1593192 -->
    <pattern id="dtbook_levelDepth">
        <rule context="dtb:level[@depth]">
            <assert test="@depth=count(ancestor-or-self::dtb:level)">The value of the depth
                attribute on the level element does not correspond to actual nesting level.</assert>
        </rule>
    </pattern>

    <!-- ****************************************************** -->
    <!-- end Pipeline 1 pattern imports -->
    <!-- ****************************************************** -->

    <!-- 
        The math element has optional attributes alttext and altimg. To be valid with the MathML in DAISY spec, 
        the alttext and altimg attributes must be part of the math element.
    -->
    <pattern>
        <rule context="//m:math">
            <assert test="//node()[@alttext]">alttext attribute must be present</assert>
            <assert test="not(empty(//node()[@alttext]))">alttext attribute must be
                non-empty</assert>

            <assert test="//node()[@altimg]">altimg attribute must be present</assert>
            <assert test="not(empty(//node()[@altimg]))">altimg attribute must be non-empty</assert>

            <!-- Note that there is not a test for the rule 
                "@smilref may be present and if so must be non-empty"
                because this is designed to be used with standalone DTBook files
            -->
        </rule>
    </pattern>

    <!-- because we override the IDREF datatype with NMTOKEN in the dtbook-mathml-integration schema,
        we need to double check MathML @xref values (which were originally of type IDREF)
        
        Note that we don't need to perform these checks on DTBook elements because of the 
        Pipeline 1 patterns here that look at annoref and noteref already, which are the only 2 elements
        to use an attribute originally of the type IDREF.
    -->
    <pattern id="xref">
        <rule context="m:*[@xref]">
            <assert test="some $elem in //* satisfies ($elem/@id eq @xref)"> xref attribute does not
                resolve.</assert>
        </rule>
    </pattern>

    <pattern id="annotation">
        <rule context="m:annotation-xml">
            <assert test="node()/ancestor::m:semantics"> </assert>
        </rule>
    </pattern>

    <include href="mod/mathml-content-elements.sch"/>
</schema>
