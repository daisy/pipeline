<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/internal"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    version="2.0" xpath-default-namespace="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all">

    <xsl:output indent="yes" method="xml"/>

    <xsl:key name="ids" match="*" use="@id"/>

    <xsl:template match="node()">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <!--====== Serialize mode (for escaping XML) ======-->

    <xsl:template match="*" mode="serialize">
        <xsl:text>&lt;</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:apply-templates select="@*" mode="serialize"/>
        <xsl:choose>
            <xsl:when test="node()">
                <xsl:text>&gt;</xsl:text>
                <xsl:apply-templates mode="serialize"/>
                <xsl:text>&lt;/</xsl:text>
                <xsl:value-of select="name()"/>
                <xsl:text>&gt;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> /&gt;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*" mode="serialize">
        <xsl:text> </xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>="</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="comment()" mode="serialize">
        <xsl:text>&lt;!--</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>--&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="text()" mode="serialize">
        <xsl:value-of select="string(.)"/>
    </xsl:template>

    <!--====== Global attributes ======-->

    <!--Discard all attributes by default-->
    <xsl:template match="@*"/>

    <!--Allways copy global attributes-->
    <xsl:template match="@*[f:is-global(.)]">
        <xsl:copy/>
    </xsl:template>
    <!--Function returning whether an attribute is global-->
    <xsl:function name="f:is-global" as="xs:boolean">
        <xsl:param name="at" as="attribute()"/>
        <xsl:sequence
            select="$at/(name()=(
            (: HTML 5 globals :)
            'accesskey','class','contenteditable','contextmenu','dir','draggable','dropzone','hidden','id','lang',
            'spellcheck','style','tabindex','title','translate',
            (: Event Handlers :)
            'onabort','onblur','oncancel','oncanplay','oncanplaythrough','onchange','onclick','onclose','oncontextmenu',
            'oncuechange','ondblclick','ondrag','ondragend','ondragenter','ondragexit','ondragleave','ondragover',
            'ondragstart','ondrop','ondurationchange','onemptied','onended','onerror','onfocus','oninput','oninvalid',
            'onkeydown','onkeypress','onkeyup','onload','onloadeddata','onloadedmetadata','onloadstart','onmousedown',
            'onmouseenter','onmouseleave','onmousemove','onmouseout','onmouseover','onmouseup','onmousewheel','onpause',
            'onplay','onplaying','onprogress','onratechange','onreset','onresize','onscroll','onseeked','onseeking',
            'onselect','onshow','onstalled','onsubmit','onsuspend','ontimeupdate','ontoggle','onvolumechange','onwaiting') 
            (: Data attributes :)
            or starts-with(name(),'data-')
            (: ARIA attributes :)
            or name()='role' or starts-with(name(),'aria-')
            (: Non-HTML attributes :)
            or namespace-uri())"
        />
    </xsl:function>

    <xsl:template
        match="@align|@alink|@allowtransparency|@background|@bgcolor|@border|@bordercolor|@cellpadding|@cellspacing|@char|@charoff|@clear|@color|@compact|@frame|@frameborder|@height|@hspace|@link|@marginbottom|@marginheight|@marginleft|@marginright|@margintop|@marginwidth|@noshade|@nowrap|@rules|@scrolling|@size|@text|@type|@valign|@vlink|@vspace|@width">
        <xsl:message select="concat('Discarding obsolete attribute''',name(),'''. Use CSS instead')"
        />
    </xsl:template>

    <!--====== Element 'a' ======-->
    <xsl:template
        match="a/@href|a/@target|a/@download|a/@rel|a/@hreflang|a/@type">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="a/@name|embed/@name|img/@name|option/@name">
        <xsl:choose>
            <xsl:when test="empty(../@id) and string(.) and empty(key('ids',.) except ..)">
                <xsl:message select="concat('Converting name attribute ''',.,''' to id')"/>
                <xsl:attribute name="id" select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat('Discarding ''name'' attribute ''',.,'''')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="a[ancestor::map or ancestor::template][@coords|@shape][normalize-space()='']">
        <area>
            <xsl:copy-of select="@shape|@coords"/>
            <xsl:apply-templates select="node()|@*"/>
        </area>
    </xsl:template>

    <!--====== Element 'acronym' ======-->
    <xsl:template match="acronym">
        <xsl:message select="'Converting ''acronym'' element to ''abbr'''"/>
        <abbr>
            <xsl:apply-templates select="node()|@*"/>
        </abbr>
    </xsl:template>

    <!--====== Element 'applet' ======-->
    <xsl:template match="applet">
        <xsl:message select="'Discarding applet element'"/>
    </xsl:template>

    <!--====== Element 'area' ======-->
    <xsl:template
        match="area/@alt|area/@coords|area/@shape|area/@href|area/@target|area/@download|area/@rel|area/@hreflang|area/@type">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'audio' ======-->
    <xsl:template
        match="audio/@src|audio/@crossorigin|audio/@preload|audio/@autoplay|audio/@mediagroup|audio/@loop|audio/@muted|audio/@controls">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'base' ======-->
    <xsl:template match="base/@href|base/@target">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'basefont' ======-->
    <xsl:template match="basefont">
        <xsl:message select="'Converting ''basefont'' element to ''style'''"/>
        <style>
            <xsl:text>&#xa;body {&#xa;</xsl:text>
            <xsl:sequence select="concat(
                if (@color) then concat('  color: ',@color,';&#xa;') else (),
                if (@face) then concat('  font-family: ',@face,';&#xa;')else (),
                if (@size) then concat('  font-size: ',f:convert-font-size(@size),';&#xa;') else ()
                )"/>
            <xsl:text>}&#xa;</xsl:text>
        </style>
    </xsl:template>

    <!--====== Element 'bgsound' ======-->
    <xsl:template match="bgsound">
        <audio src="{@src}">
            <xsl:if test="@loop[.='infinite']">
                <xsl:attribute name="loop" select="''"/>
            </xsl:if>
            <xsl:apply-templates select="node()|@*"/>
        </audio>
    </xsl:template>

    <!--====== Element 'big' ======-->
    <xsl:template match="big">
        <xsl:message select="'Converting ''big'' to ''strong'''"/>
        <strong>
            <xsl:apply-templates select="node()|@*"/>
        </strong>
    </xsl:template>

    <!--====== Element 'blink' ======-->
    <xsl:template match="blink">
        <xsl:message select="'Converting ''blink'' to ''span'''"/>
        <span>
            <xsl:apply-templates select="node()|@*"/>
        </span>
    </xsl:template>

    <!--====== Element 'blockquote' ======-->
    <xsl:template match="blockquote/@cite">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'body' ======-->
    <xsl:template
        match="body/@onafterprint|body/@onbeforeprint|body/@onbeforeunload|body/@onhashchange|body/@onmessage|body/@onoffline|body/@ononline|body/@onpagehide|body/@onpageshow|body/@onpopstate|body/@onstorage|body/@onunload">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'button' ======-->
    <xsl:template
        match="button/@autofocus|button/@disabled|button/@form|button/@formaction|button/@formenctype|button/@formmethod|button/@formnovalidate|button/@formtarget|button/@name|button/@type|button/@value">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'canvas' ======-->
    <xsl:template match="canvas/@width|canvas/@height">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'center' ======-->
    <xsl:template match="center">
        <xsl:message select="'Converting ''center'' to CSS-styled ''span'''"/>
        <span style="{concat('text-align: center; ', @style)}">
            <xsl:apply-templates select="node()|@* except @style"/>
        </span>
    </xsl:template>

    <!--====== Element 'col' ======-->
    <xsl:template match="col/@span">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'colgroup' ======-->
    <xsl:template match="colgroup/@span">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'data' ======-->
    <xsl:template match="data/@value">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'del' ======-->
    <xsl:template match="del/@cite|del/@datetime">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'details' ======-->
    <xsl:template match="details">
        <xsl:message select="'HTML 5.1 element ''details'''"/>
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="details/@open">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'dir' ======-->
    <xsl:template match="dir">
        <xsl:message select="'Converting ''dir'' element to ''ul'''"/>
        <ul>
            <xsl:apply-templates select="node()|@*"/>
        </ul>
    </xsl:template>

    <!--====== Element 'embed' ======-->
    <xsl:template
        match="embed/@src|embed/@type|embed/@width|embed/@height|embed/@*[namespace-uri()='' and name() ne 'name']">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="embed/@align|embed/@hspace|embed/@vspace">
        <xsl:message select="'Discarding obsolete atrribute ''',name(),'''. Use CSS instead.'"/>
    </xsl:template>
    <xsl:template match="embed[noembed]">
        <xsl:message select="'Converting ''embed'' element with alternative content to ''object'''"/>
        <object data="{@src}">
            <xsl:copy-of select="@*[f:is-global(.) or name()=('type','width','height')]"/>
            <xsl:for-each select="@*[not(f:is-global(.) or name()=('src','type','width','height'))]">
                <param name="{local-name()}" value="{.}"/>
            </xsl:for-each>
            <xsl:apply-templates select="noembed/node()"/>
        </object>
    </xsl:template>

    <!--====== Element 'fieldset' ======-->
    <xsl:template match="fieldset/@disabled|fieldset/@form|fieldset/@name">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'font' ======-->
    <xsl:template match="font">
        <xsl:message select="'Converting ''font'' element to CSS-styled ''span'''"/>
        <span
            style="{concat(
            if (@color) then concat('color: ',@color,'; ') else (),
            if (@face) then concat('font-family: ',@face,'; ')else (),
            if (@size) then concat('font-size: ',f:convert-font-size(@size),'; ') else (),
            @style
            )}">
            <xsl:apply-templates select="node()|@* except @style"/>
        </span>
    </xsl:template>
    <xsl:function name="f:convert-font-size" as="xs:string">
        <xsl:param name="size" as="xs:string"/>
        <xsl:sequence
            select="
            if (starts-with($size,'+')) then 'larger'
            else if (starts-with($size,'-')) then 'smaller'
            else if (number($size) le 1) then 'xx-small'
            else if (number($size) eq 3) then 'x-small'
            else if (number($size) eq 3) then 'small'
            else if (number($size) eq 4) then 'medium'
            else if (number($size) eq 5) then 'large'
            else if (number($size) eq 6) then 'x-large'
            else if (number($size) ge 7) then 'xx-large'
            else 'medium' 
            "
        />
    </xsl:function>

    <!--====== Element 'form' ======-->
    <xsl:template
        match="form/@accept-charset|form/@action|form/@autocomplete|form/@enctype|form/@method|form/@name|form/@novalidate|form/@target">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'frame' ======-->
    <xsl:template match="frame">
        <iframe src="{@src}">
            <xsl:apply-templates select="node()|@*"/>
        </iframe>
    </xsl:template>

    <!--====== Element 'frameset' ======-->
    <xsl:template match="frameset">
        <div>
            <xsl:apply-templates select="node()|@*"/>
        </div>
    </xsl:template>

    <!--====== Element 'html' ======-->
    <xsl:template match="hgroup">
        <header>
            <xsl:message select="'Converting ''hgroup'' element to ''header'''"/>
            <xsl:apply-templates select="node()|@*"/>
        </header>
    </xsl:template>
    <xsl:template match="hgroup/*[position()>1]">
        <p>
            <xsl:apply-templates select="node()|@*"/>
        </p>
    </xsl:template>

    <!--====== Element 'html' ======-->
    <xsl:template match="html/@manifest">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'iframe' ======-->
    <xsl:template
        match="iframe/@src|iframe/@srcdoc|iframe/@name|iframe/@sandbox|iframe/@width|iframe/@height">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'img' ======-->
    <xsl:template
        match="img/@alt|img/@src|img/@crossorigin|img/@usemap|img/@ismap|img/@width|img/@height|img/@longdesc">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="img[empty(@style)]/@border">
        <xsl:message select="'Converting ''border'' attribute to inline style'"/>
        <xsl:attribute name="style" select="concat('border-width: ',.,';')"/>
    </xsl:template>
    <xsl:template match="img[@border]/@style">
        <xsl:message select="'Converting ''border'' attribute to inline style'"/>
        <xsl:attribute name="style" select="concat('border-width: ',../@border,'; ',.)"/>
    </xsl:template>

    <!--====== Element 'input' ======-->
    <xsl:template
        match="input/@accept|input/@alt|input/@autocomplete|input/@autofocus|input/@checked|input/@dirname|input/@disabled|input/@form|input/@formaction|input/@formenctype|input/@formmethod|input/@formnovalidate|input/@formtarget|input/@height|input/@list|input/@max|input/@maxlength|input/@min|input/@minlength|input/@multiple|input/@name|input/@pattern|input/@placeholder|input/@readonly|input/@required|input/@size|input/@src|input/@step|input/@type|input/@value|input/@width">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="input[@usemap]">
        <xsl:message select="'Converting ''input'' with image map to ''img'''"/>
        <img usemap="{@usemap}">
            <xsl:apply-templates select="@*[f:is-global(.)]|@alt|@src|@usemap|@width|@height"/>
            <xsl:apply-templates select="node()"/>
        </img>
    </xsl:template>
    <xsl:template match="input[ancestor::form[1][@accept]]">
        <xsl:message select="'Moving form ''accept'' attribute to ''input'' descendants'"/>
        <input
            accept="{concat(ancestor::form[1]/@accept,if (@accept) then concat(',',@accept) else ())}">
            <xsl:apply-templates select="node()|@* except @accept"/>
        </input>
    </xsl:template>

    <!--====== Element 'ins' ======-->
    <xsl:template match="ins/@cite|ins/@datetime">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'isindex' ======-->
    <xsl:template match="isindex">
        <xsl:message select="'Converting ''isindex'' element to ''form'' with text input'"/>
        <form method="post">
            <p>
                <xsl:value-of select="@prompt"/>
                <input type="text"/>
            </p>
        </form>
    </xsl:template>


    <!--====== Element 'keygen' ======-->
    <xsl:template
        match="keygen/@autofocus|keygen/@challenge|keygen/@disabled|keygen/@form|keygen/@keytype|keygen/@name">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'label' ======-->
    <xsl:template match="label/@form|label/@for">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'li' ======-->
    <xsl:template match="li/@value">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'link' ======-->
    <xsl:template
        match="link/@href|link/@crossorigin|link/@rel|link/@media|link/@hreflang|link/@type|link/@sizes">
        <xsl:copy/>
    </xsl:template>


    <!--====== Element 'listing' ======-->
    <xsl:template match="listing">
        <xsl:message select="'Converting ''listing'' element to ''pre/code'''"/>
        <pre><xsl:apply-templates select="@*"/><code>
            <xsl:apply-templates select="node()" mode="serialize"/>
        </code></pre>
    </xsl:template>

    <!--====== Element 'map' ======-->
    <xsl:template match="map/@name">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'marquee' ======-->
    <xsl:template match="marquee">
        <xsl:message select="'Converting ''marquee'' element to ''div'''"/>
        <div>
            <xsl:apply-templates select="node()|@*"/>
        </div>
    </xsl:template>

    <!--====== Element 'meta' ======-->
    <xsl:template match="meta/@http-equiv|meta/@content|meta/@charset|meta/@name">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'meter' ======-->
    <xsl:template match="meter/@value|meter/@min|meter/@max|meter/@low|meter/@high|meter/@optimum">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'multicol' ======-->
    <xsl:template match="multicol">
        <xsl:message select="'Converting ''multicol'' to ''span'''"/>
        <span>
            <xsl:apply-templates select="@*|node()"/>
        </span>
    </xsl:template>

    <!--====== Element 'nextid' ======-->
    <xsl:template match="nextid"/>

    <!--====== Element 'nobr' ======-->
    <xsl:template match="nobr">
        <span style="{concat('white-space: nowrap; ',@style)}">
            <xsl:apply-templates select="node()|@* except @style"/>
        </span>
    </xsl:template>

    <!--====== Element 'noframes' ======-->
    <xsl:template match="noframes">
        <div hidden="">
            <xsl:apply-templates select="node()|@*"/>
        </div>
    </xsl:template>

    <!--====== Element 'object' ======-->
    <xsl:template match="object">
        <xsl:copy>
            <xsl:apply-templates select="@* except (@archive|@classid|@code|@codebase|@codetype)"/>
            <xsl:apply-templates select="@archive|@classid|@code|@codebase|@codetype"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template
        match="object/@data|object/@type|object/@typemustmatch|object/@name|object/@usemap|object/@form|object/@width|object/@height">
        <xsl:copy/>
    </xsl:template>
    <xsl:template
        match="object/@archive|object/@classid|object/@code|object/@codebase|object/@codetype">
        <xsl:message select="concat('Converting obsolte attribute ''',name(),''' to parameter')"/>
        <param name="{name()}" value="{.}"/>
    </xsl:template>

    <!--====== Element 'ol' ======-->
    <xsl:template match="ol/@reversed|ol/@start|ol/@type">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'optgroup' ======-->
    <xsl:template match="optgroup/@disabled|optgroup/@label">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'option' ======-->
    <xsl:template match="option/@disabled|option/@label|option/@selected|option/@value">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'output' ======-->
    <xsl:template match="output/@for|output/@form|output/@name">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'param' ======-->
    <xsl:template match="param/@name|param/@value">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'plaintext' ======-->
    <xsl:template match="plaintext">
        <xsl:message select="'Converting ''plaintext'' element to ''pre'''"/>
        <pre>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="node()" mode="serialize"/>
        </pre>
    </xsl:template>

    <!--====== Element 'progress' ======-->
    <xsl:template match="progress/@value|progress/@max">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'q' ======-->
    <xsl:template match="q/@cite">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'script' ======-->
    <xsl:template
        match="script/@src|script/@type|script/@charset|script/@async|script/@defer|script/@crossorigin">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="script[empty(@type)]/@language[lower-case(.)='javascript']">
        <xsl:attribute name="type" select="'text/javascript'"/>
    </xsl:template>
    <xsl:template match="script[exists(@language) and lower-case(@language) ne 'javascript']">
        <xsl:message select="'Discarding ''script'' element in unknown language.'"/>
    </xsl:template>


    <!--====== Element 'select' ======-->
    <xsl:template
        match="select/@autofocus|select/@disabled|select/@form|select/@multiple|select/@name|select/@required|select/@size">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'spacer' ======-->
    <xsl:template match="spacer">
        <xsl:message select="'Discarding ''spacer'' element.'"/>
    </xsl:template>

    <!--====== Element 'source' ======-->
    <xsl:template match="source/@src|source/@type|source/@media">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'strike' ======-->
    <xsl:template match="strike">
        <xsl:message select="'Converting ''strike'' element to ''s''.'"/>
        <s>
            <xsl:apply-templates select="node()|@*"/>
        </s>
    </xsl:template>

    <!--====== Element 'style' ======-->
    <xsl:template match="style/@media|style/@type">
        <xsl:copy/>
    </xsl:template>
    
    <!--====== Element 'summary' ======-->
    <xsl:template match="summary">
        <xsl:message select="'HTML 5.1 element ''summary'''"/>
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <!--====== Element 'table' ======-->
    <xsl:template match="table[@summary and empty(caption)]">
        <table>
            <xsl:message select="'Converting table ''summary'' attribute to caption.'"/>
            <xsl:apply-templates select="@* except @summary"/>
            <caption>
                <xsl:value-of select="@summary"/>
            </caption>
            <xsl:apply-templates select="node()"/>
        </table>
    </xsl:template>
    <xsl:template match="table/@border">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'td' ======-->
    <xsl:template match="td/@colspan|td/@rowspan|td/@headers|td/@scope">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="td[@scope]">
        <th>
            <xsl:apply-templates select="node()|@*"/>
        </th>
    </xsl:template>

    <!--====== Element 'textarea' ======-->
    <xsl:template
        match="textarea/@autofocus|textarea/@cols|textarea/@dirname|textarea/@disabled|textarea/@form|textarea/@maxlength|textarea/@minlength|textarea/@name|textarea/@placeholder|textarea/@readonly|textarea/@required|textarea/@rows|textarea/@wrap">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'th' ======-->
    <xsl:template match="th/@colspan|th/@rowspan|th/@headers|th/@scope|th/@abbr">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'time' ======-->
    <xsl:template match="time/@datetime">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'track' ======-->
    <xsl:template match="track/@default|track/@kind|track/@label|track/@src|track/@srclang">
        <xsl:copy/>
    </xsl:template>

    <!--====== Element 'tt' ======-->
    <xsl:template match="tt">
        <code>
            <xsl:apply-templates select="node()|@*"/>
        </code>
    </xsl:template>

    <!--====== Element 'video' ======-->
    <xsl:template
        match="video/@src|video/@crossorigin|video/@poster|video/@preload|video/@autoplay|video/@mediagroup|video/@loop|video/@muted|video/@controls|video/@width|video/@height">
        <xsl:copy/>
    </xsl:template>


    <!--====== Element 'xmp' ======-->
    <xsl:template match="xmp">
        <xsl:message select="'Converting ''xmp'' element to ''pre/code'''"/>
        <pre><xsl:apply-templates select="@*"/><code>
            <xsl:apply-templates select="node()" mode="serialize"/>
        </code></pre>
    </xsl:template>

</xsl:stylesheet>
