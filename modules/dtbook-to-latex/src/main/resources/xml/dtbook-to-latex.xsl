<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"	
		xmlns:xs="http://www.w3.org/2001/XMLSchema" 
		xmlns:math="http://www.w3.org/1998/Math/MathML"
		xmlns:my="http://my-functions"
		extension-element-prefixes="my"
		exclude-result-prefixes="dtb my">
  
  <xsl:include href="table-utils.xsl"/>

  <xsl:output method="text" encoding="utf-8" indent="no"/>
  <xsl:strip-space elements="*"/>
  <xsl:preserve-space elements="dtb:line dtb:address dtb:div dtb:title dtb:author dtb:note dtb:byline dtb:dateline 
				dtb:a dtb:em dtb:strong dtb:dfn dtb:kbd dtb:code dtb:samp dtb:cite dtb:abbr dtb:acronym
				dtb:sub dtb:sup dtb:span dtb:bdo dtb:sent dtb:w 
				dtb:q dtb:p 
				dtb:doctitle dtb:docauthor dtb:covertitle 
				dtb:h1 dtb:h2 dtb:h3 dtb:h4 dtb:h5 dtb:h6
				dtb:bridgehead dtb:hd dtb:dt dtb:li dtb:lic "/>
	
  <!-- Possible values are 12pt, 14pt, 17pt, 20pt and 25pt -->
  <xsl:param name="fontsize">17pt</xsl:param>
  <!-- Possible values are for example 'Tiresias LPfont', 'LMRoman10
  Regular', 'LMSans10 Regular' or 'LMTypewriter10 Regular'. Basically
  any installed TrueType or OpenType font -->
  <xsl:param name="font">LMRoman10 Regular</xsl:param>
  <!-- Optional backup font and comma-separated list of Unicode ranges for which this font needs to be applied -->
  <xsl:param name="backupFont">Arial Unicode MS</xsl:param>
  <xsl:param name="backupUnicodeRanges"></xsl:param>
  
  <xsl:param name="defaultLanguage">english</xsl:param>
  
  <xsl:param name="stocksize">a4paper</xsl:param>
  <!-- Possible values are 'left', 'justified' -->
  <xsl:param name="alignment">justified</xsl:param>
  <!-- Possible values are 'plain', 'compact', 'spacious', 'withPageNums' and 'scientific'
       - 'plain' contains no original page numbers, no section numbering
         and uses the 'plain' pagestyle. Chapters always start on a
	 new recto page.
       - 'compact' is just like 'plain' but chapters start on any new page be
         it recto or verso.
       - 'spacious' is like 'plain' but level2 start on a new page.
       - 'withPageNums' is similar to 'plain' but enables the display of the
         original page numbers.
       - 'scientific' has original page numbers, has section numbering
         and uses the normal latex page style for the document class book.
    -->
  <xsl:param name="pageStyle">plain</xsl:param>

  <!-- Possible values are singlespacing, onehalfspacing and doublespacing -->
  <xsl:param name="line_spacing">singlespacing</xsl:param>

  <!-- The following values define the paper size and the margins.
       Note that the paper size is not the same as the stock size. The
       book will be printed on stock size paper, typically letter size
       or A4. It will then usually be trimmed to the paper size. If
       you do not specify a paper size the page size will be assumed
       to be the same as the stock size, i.e. your book will be of
       size letter or A4. -->
  <xsl:param name="paperwidth">200mm</xsl:param>
  <xsl:param name="paperheight">250mm</xsl:param>
  <xsl:param name="left_margin">28mm</xsl:param>
  <xsl:param name="right_margin">20mm</xsl:param>
  <xsl:param name="top_margin">20mm</xsl:param>
  <xsl:param name="bottom_margin">20mm</xsl:param>

  <!-- FIXME: Unfortunately the current TDF Grammar doesn't allow for
       boolean params, so the following param is handled as a string.
       See
       http://data.daisy.org/projects/pipeline/doc/developer/tdf-grammar-v1.1.html -->
  <xsl:param name="replace_em_with_quote">false</xsl:param> 

  <!-- Place the notes at the end instead of on the same page.
       Possible values are none, document and chapter -->
  <xsl:param name="endnotes">none</xsl:param> 

  <!-- Include images or ignore them. -->
  <xsl:param name="include_images">true</xsl:param>

  <xsl:param name="version">unknown</xsl:param>

  <xsl:variable name="number_of_volumes" select="count(//*['volume-split-point'=tokenize(@class, '\s+')])+1"/>

  <!--
      Wrap the text in a c:data element so XProc can deal with it
  -->
  <xsl:template match="/*">
    <c:data xmlns:c="http://www.w3.org/ns/xproc-step">
      <xsl:next-match/>
    </c:data>
  </xsl:template>

  <xsl:function name="my:max-line-width" as="xs:integer">
    <xsl:sequence select="if ($fontsize='17pt') then 40 else
			  if ($fontsize='20pt') then 35 else
			  if ($fontsize='25pt') then 30 else 40"/>
  </xsl:function>

  <xsl:function name="my:includegraphics-command" as="xs:string">
    <xsl:param name="src" as="xs:string"/>
    <xsl:param name="with_caption" as="xs:boolean"/>
    <xsl:variable name="magic-number" select="3"/>
    <xsl:variable name="scale-factor">
      <xsl:sequence select="if ($fontsize='14pt') then round-half-to-even(14 div 12, 1) else 
			    if ($fontsize='17pt') then round-half-to-even(17 div 12, 1) else 
			    if ($fontsize='20pt') then round-half-to-even(20 div 12, 1) else
			    if ($fontsize='25pt') then round-half-to-even(25 div 12, 1) else 1"/>
    </xsl:variable>
    <!-- FIXME: The following code calculates the available height for an image. If there is
         a caption we assume that it will take up one line. This assumption can of course
         fail, but we basically have no way of knowing how many lines a caption will take
         from xslt (aside from crude guesses). -->
    <xsl:variable name="height" select="if ($with_caption) then '\textheightMinusCaption' else '\textheight'"/>
    <xsl:sequence select="concat('\maxsizebox{\textwidth}{',$height,'}{\includegraphics[scale=',$scale-factor*$magic-number,']{',$src,'}}&#10;')"/>
  </xsl:function>

  <!-- Captions in plain LaTeX aren't very robust, i.e. a caption
       should not contain environments such as lists, enumerations,
       etc. In DTBook you can put all sorts of stuff inside captions.
       If we really want to support this we'll have to put the float
       and the caption (as a plain para) inside a minipage. -->
  <xsl:function name="my:cleanCaptions" as="xs:string">
    <xsl:param name="context" as="node()"/>
    <xsl:value-of select="string($context)"/>
  </xsl:function>

  <!-- =========================== -->
  <!-- Queries for block vs inline -->
  <!-- =========================== -->

  <xsl:function name="my:is-block-element" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:apply-templates select="$node" mode="is-block-element"/>
  </xsl:function>

  <xsl:template match="node()" as="xs:boolean" mode="is-block-element" priority="10">
    <xsl:sequence select="false()"/>
  </xsl:template>

  <xsl:template match="dtb:*|math:*" as="xs:boolean" mode="is-block-element" priority="11">
    <xsl:sequence select="false()"/>
  </xsl:template>

  <xsl:template match="dtb:samp|dtb:cite" as="xs:boolean" mode="is-block-element" priority="12">
    <xsl:sequence select="if (parent::*[self::dtb:p|self::dtb:li|self::dtb:td|self::dtb:th]) then false() else true()"/>
  </xsl:template>

  <xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:p|dtb:list|dtb:li|dtb:author|dtb:byline|dtb:line|dtb:imggroup|dtb:blockquote" as="xs:boolean" mode="is-block-element" priority="12">
    <xsl:sequence select="true()"/>
  </xsl:template>

  <xsl:function name="my:has-preceding-non-empty-textnode-within-block" as="xs:boolean">
    <xsl:param name="context"/>
    <xsl:sequence select="some $t in ($context/preceding::text() intersect $context/ancestor-or-self::*[my:is-block-element(.)][1]//text()) satisfies normalize-space($t) != ''"/>
  </xsl:function>

  <xsl:function name="my:is-level-element" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:apply-templates select="$node" mode="is-level-element"/>
  </xsl:function>

  <xsl:template match="node()" as="xs:boolean" mode="is-level-element" priority="10">
    <xsl:sequence select="false()"/>
  </xsl:template>

  <xsl:template match="dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6" as="xs:boolean" mode="is-level-element" priority="12">
    <xsl:sequence select="true()"/>
  </xsl:template>

  <xsl:function name="my:has-preceding-para-within-parent-level" as="xs:boolean">
    <xsl:param name="context"/>
    <xsl:sequence select="exists($context/preceding::dtb:p intersect $context/ancestor::*[my:is-level-element(.)][1]//dtb:p)"/>
  </xsl:function>

  <xsl:variable name="level_to_section_map">
    <entry key="level1">\chapter</entry>
    <entry key="level2">\section</entry>
    <entry key="level3">\subsection</entry>
    <entry key="level4">\subsubsection</entry>
    <entry key="level5">\paragraph</entry>
    <entry key="level6">\subparagraph</entry>
  </xsl:variable>

  <!-- Localization -->

  <!-- This is used to set some words and phrases which are generated -->
  <!-- automatically. Many translations are provided by the babel package -->
  <!-- but a few which are defined by the memoir class have to be localized -->
  <!-- explicitely, such as Notes, Abstract, etc. See section 18.20 of the -->
  <!-- memoir manual and section 23 for example of the babel documentation -->
  <xsl:template match="*" mode="localizeWords">
    <xsl:choose>
      <xsl:when test="lang('de')">
	<xsl:text>\renewcommand*{\notesname}{Anmerkungen}&#10;</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Escape characters that have a special meaning to LaTeX (see The
       Comprehensive LaTeX Symbol List,
       http://www.ctan.org/tex-archive/info/symbols/comprehensive/symbols-a4.pdf) -->
  <xsl:function name="my:quoteSpecialChars" as="xs:string">
    <xsl:param name="text"/>
    <!-- handle backslash -->
    <xsl:variable name="tmp1" select="replace($text, '\\', '\\textbackslash ')"/>
    <!-- drop excessive white space -->
    <xsl:variable name="tmp2" select="replace($tmp1, '\s+', ' ')"/>
    <!-- quote special chars -->
    <xsl:variable name="tmp3" select="replace($tmp2, '(\$|&amp;|%|#|_|\{|\})', '\\$1')"/>
    <!-- append a '{}' to special chars so they are not missinterpreted -->
    <xsl:variable name="tmp4" select="replace($tmp3, '(~|\^)', '\\$1{}')"/>
    <!-- add non-breaking space in front of emdash or endash followed by punctuation -->
    <xsl:variable name="tmp5" select="replace($tmp4, ' ([–—]\p{P})', ' $1')"/>
    <!-- add non-breaking space in front ellipsis followed by punctuation -->
    <xsl:variable name="tmp6" select="replace($tmp5, ' ((\.{3}|…)\p{P})', ' $1')"/>
    <!-- [ and ] can sometimes be interpreted as the start or the end of an optional argument -->
    <xsl:variable name="tmp7" select="replace(replace($tmp6, '\[', '\\lbrack{}'), '\]', '\\rbrack{}')"/>
    <!-- << and >> is apparently treated as a shorthand and is not handled by our shorthand disabling code -->
    <xsl:variable name="tmp8" select="replace(replace($tmp7, '&lt;&lt;', '{&lt;}&lt;'), '&gt;&gt;', '{&gt;}&gt;')"/>
    <xsl:value-of select="$tmp8"/>
  </xsl:function>

   <xsl:template match="/">
     <!-- Pass 1: Add volume-split DIVs -->
     <xsl:variable name="pass1">
       <xsl:apply-templates mode="volume-split"/>
     </xsl:variable>
     <!-- Pass 2 -->
     <xsl:apply-templates select="$pass1/*"/>
   </xsl:template>

   <xsl:template match="dtb:dtbook">
	<xsl:text>% ***************************************&#10;</xsl:text>
   	<xsl:value-of select="concat('% DAISY Pipeline 2 dtbook-to-latex v', $version, '&#10;')"/>
	<xsl:text>% ***************************************&#10;</xsl:text>
   	<xsl:text>\documentclass[</xsl:text>
	<xsl:value-of select="concat($fontsize, ',', $stocksize, ',')"/>
	<xsl:text>extrafontsizes,twoside,showtrims]{memoir}&#10;</xsl:text>
	<xsl:text>\usepackage{calc}&#10;</xsl:text>
	<!-- Tables -->
	<xsl:if test="//dtb:table">
	  <!-- tables with variable width columns balanced -->
	  <xsl:text>\usepackage{tabulary}&#10;</xsl:text>
	  <!-- we need the xcolor package to be able to define colors such as black!60 -->
	  <xsl:text>\usepackage{xcolor}&#10;</xsl:text>
	  <xsl:text>\usepackage{colortbl}&#10;</xsl:text>
	  <xsl:text>\arrayrulecolor{black!60}&#10;</xsl:text>
	  <xsl:text>\setlength{\arrayrulewidth}{0.5mm}&#10;</xsl:text>
	</xsl:if>
	<!-- Make sure captions are left justified -->
	<xsl:text>\captionstyle{\raggedright}&#10;</xsl:text>
	<xsl:choose>
	  <xsl:when test="($paperheight ne '') and ($paperwidth ne '')">
	    <xsl:value-of select="concat('\settrimmedsize{',$paperheight,'}{',$paperwidth,'}{*}&#10;')"/>
	     <!-- Equal trims at the top and bottom and no trim in the
	          spine (apparently this is better for gluing) -->
	    <xsl:text>\setlength{\trimtop}{\stockheight - \paperheight}&#10;</xsl:text>
	    <xsl:text>\setlength{\trimedge}{\stockwidth - \paperwidth}&#10;</xsl:text>
	    <xsl:text>\settrims{0.5\trimtop}{\trimedge}&#10;</xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:text>\settrimmedsize{\stockheight}{\stockwidth}{*}&#10;</xsl:text>
	  </xsl:otherwise>
	</xsl:choose>
	<xsl:if test="($left_margin ne '') and ($right_margin ne '')">
	  <xsl:value-of select="concat('\setlrmarginsandblock{',$left_margin,'}{',$right_margin,'}{*}&#10;')"/>
	</xsl:if>
	<xsl:if test="($top_margin ne '') and ($bottom_margin ne '')">
	  <xsl:value-of select="concat('\setulmarginsandblock{',$top_margin,'}{',$bottom_margin,' + 1.5\onelineskip}{*}&#10;')"/>
	</xsl:if>
	<xsl:text>\setheadfoot{\onelineskip}{1.5\onelineskip}&#10;</xsl:text>
	<xsl:text>\setheaderspaces{*}{*}{0.4}&#10;</xsl:text>
	<xsl:text>\checkandfixthelayout&#10;&#10;</xsl:text>

	<!-- The trim marks should be outside the actual page so that you will not
	     see any lines even if you do not cut the paper absolutely precisely
	     (see section 18.3. Trim marks in the memoir manual) -->
	<!-- The default for trimLmarks in newer versions of memoir (v3.6j) is too
	     cluttered and in particular too close the the actual page. Define a
	     new 'light' version of trimLmarks which drops the trim marks in the
	     middle -->
	<xsl:text>\newcommand*{\trimLmarksLight}{%&#10;</xsl:text>
	<xsl:text>  \let\tmarktl\Ltrimpictl&#10;</xsl:text>
	<xsl:text>  \let\tmarktr\Ltrimpictr&#10;</xsl:text>
	<xsl:text>  \let\tmarkbl\Ltrimpicbl&#10;</xsl:text>
	<xsl:text>  \let\tmarkbr\Ltrimpicbr&#10;</xsl:text>
	<xsl:text>  \let\tmarktm\relax&#10;</xsl:text>
	<xsl:text>  \let\tmarkml\relax&#10;</xsl:text>
	<xsl:text>  \let\tmarkmr\relax&#10;</xsl:text>
	<xsl:text>  \let\tmarkbm\relax}&#10;&#10;</xsl:text>
	<xsl:text>\trimLmarksLight&#10;&#10;</xsl:text>

   	<xsl:text>\usepackage{graphicx}&#10;</xsl:text>
	<!-- Make sure images never get larger than the textwidth and the textheight -->
   	<xsl:text>\usepackage{adjustbox}&#10;</xsl:text>
   	<xsl:call-template name="findLanguage"/>
	<!-- The Babel package defines what they call shorthands. These are usefull
	     if you handcraft your LaTeX. But they are not wanted in the case where
	     the LaTeX is generated, as they change "o into ö for example. The
	     following disables this feature. See
	     http://tex.stackexchange.com/questions/28522/disable-babels-shorthands.
	     This works for Babel 3.8m -->
	<xsl:text>\def\languageshorthands#1{}&#10;</xsl:text>
   	<xsl:text>\setlength{\parskip}{1.5ex}&#10;</xsl:text>
   	<xsl:text>\setlength{\parindent}{0ex}&#10;</xsl:text>
	<xsl:text>\usepackage{fontspec,xunicode,xltxtra}&#10;</xsl:text>
	<xsl:text>\defaultfontfeatures{Mapping=tex-text}&#10;</xsl:text>
	<xsl:text>\setmainfont{</xsl:text><xsl:value-of select="$font"/><xsl:text>}&#10;</xsl:text>

     <xsl:if test="string-length($backupFont) > 0 and string-length($backupUnicodeRanges) > 0">
       <xsl:variable name="all-unicode-ranges" select="doc('unicode-blocks.xml')/unicodeBlocks/block"/>
       <xsl:variable name="included-unicode-ranges" as="xs:string*"
         select="distinct-values(
         for $cp in distinct-values(string-to-codepoints(string(/*)))
         return $all-unicode-ranges[$cp &gt;= number(@start) and $cp &lt;= number(@end)]/@name)" />
       <xsl:variable name="backup-unicode-ranges" as="xs:string*"
         select="tokenize($backupUnicodeRanges, ',')[.=$included-unicode-ranges]"/>
       <xsl:if test="$backup-unicode-ranges[1]">
         <xsl:text>&#10;</xsl:text>
         <xsl:text>%%Use a secondary font for some Unicode ranges&#10;</xsl:text>
         <xsl:text>%%Warning: the package ucharclasses must be installed&#10;</xsl:text>
         <xsl:text>\usepackage{ucharclasses}&#10;</xsl:text>
	 <xsl:text>\newfontfamily\lpfont{</xsl:text><xsl:value-of select="$font"/><xsl:text>}&#10;</xsl:text>
	 <xsl:text>\newfontfamily\unicodefont{</xsl:text><xsl:value-of select="$backupFont"/><xsl:text>}&#10;</xsl:text>
         <xsl:text>\setDefaultTransitions{\lpfont}{}&#10;</xsl:text>
         <xsl:for-each select="$backup-unicode-ranges">
           <xsl:text>\setTransitionTo{</xsl:text><xsl:value-of select="."/><xsl:text>}{\unicodefont}&#10;</xsl:text>
         </xsl:for-each>
         <xsl:text>&#10;</xsl:text>
       </xsl:if>
     </xsl:if>
     
     <xsl:if test="//dtb:samp[@xml:space='preserve']">
       <xsl:text>\usepackage{alltt}&#10;</xsl:text>
     </xsl:if>

     <xsl:if test="//dtb:sidebar|//dtb:prodnote">
       <xsl:text>\usepackage{tcolorbox}&#10;</xsl:text>
       <xsl:text>\tcbuselibrary{breakable}&#10;</xsl:text>
       <xsl:text>\tcbset{colframe=black!60,colback=white,arc=0mm,float,parbox=false,enlarge top by=5mm}&#10;</xsl:text>
     </xsl:if>

     <xsl:if test="//dtb:linenum|//dtb:span[@class='linenum']">
       <!-- Make sure the linenums are always on the left -->
       <xsl:text>\sideparmargin{left}&#10;</xsl:text>
     </xsl:if>

     <!-- Use the enumitem class to avoid overflowing description labels -->
     <xsl:text>\usepackage{enumitem}&#10;</xsl:text>

     <!-- to break long urls across lines -->
     <xsl:text>\usepackage[hyphens]{url}&#10;</xsl:text>
     <!-- Use the same font for urls as for the rest -->
     <xsl:text>\urlstyle{same}&#10;</xsl:text>

     <xsl:text>\usepackage{hyperref}&#10;</xsl:text>
     <xsl:text>\hypersetup{&#10;</xsl:text>
     <xsl:text>pdfinfo={&#10;</xsl:text>
     <xsl:value-of select="concat('  Title={', my:quoteSpecialChars(//dtb:meta[@name='dc:title' or @name='dc:Title']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Author={', my:quoteSpecialChars(//dtb:meta[@name='dc:creator' or @name='dc:Creator']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Subject={', my:quoteSpecialChars(//dtb:meta[@name='dc:subject' or @name='dc:Subject']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Lang={', my:quoteSpecialChars(//dtb:meta[@name='dc:language' or @name='dc:Language']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Producer={', my:quoteSpecialChars(//dtb:meta[@name='dc:publisher' or @name='dc:Publisher']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Source={', my:quoteSpecialChars(//dtb:meta[@name='dc:source' or @name='dc:Source']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Identifier={', my:quoteSpecialChars(//dtb:meta[@name='dc:identifier' or @name='dc:Identifier']/@content), '},&#10;')"/>
     <xsl:value-of select="concat('  Fontsize={', my:quoteSpecialChars($fontsize), '},&#10;')"/>
     <xsl:value-of select="concat('  Font={', my:quoteSpecialChars($font), '},&#10;')"/>
     <xsl:value-of select="concat('  Stocksize={', my:quoteSpecialChars($stocksize), '},&#10;')"/>
     <xsl:value-of select="concat('  Alignment={', my:quoteSpecialChars($alignment), '},&#10;')"/>
     <xsl:value-of select="concat('  PageStyle={', my:quoteSpecialChars($pageStyle), '},&#10;')"/>
     <xsl:value-of select="concat('  LineSpacing={', my:quoteSpecialChars($line_spacing), '},&#10;')"/>
     <xsl:value-of select="concat('  ReplaceEmWithQuote={', my:quoteSpecialChars($replace_em_with_quote), '},&#10;')"/>
     <xsl:value-of select="concat('  EndNotes={', my:quoteSpecialChars($endnotes), '}&#10;')"/>
     <xsl:text>}&#10;</xsl:text>
     <xsl:text>}&#10;</xsl:text>

	<xsl:text>\usepackage{float}&#10;</xsl:text>
	<xsl:text>\usepackage{alphalph}&#10;</xsl:text>

	<!-- avoid overfull \hbox (which is a serious problem with large fonts) -->
	<xsl:text>\sloppy&#10;</xsl:text>
	
	<!-- Use sloppybottom to avoid widow lines. According to the memoir manual (3.5
	     Sloppybottom) \topskip must have been increased beforehand for this to work -->
	<xsl:text>\setlength{\topskip}{1.6\topskip}&#10;</xsl:text>
	<xsl:text>\checkandfixthelayout&#10;</xsl:text>
	<xsl:text>\sloppybottom&#10;&#10;</xsl:text>
 
	<!-- eliminate widows and orphans -->
	<xsl:text>\clubpenalty=10000&#10;</xsl:text>
	<xsl:text>\widowpenalty=10000&#10;</xsl:text>

	<!-- avoid random stretches in the middle of a page, if need be stretch at the bottom -->
	<xsl:text>\raggedbottom&#10;</xsl:text>

	<!-- Define a chapter style suited for large print -->
	<xsl:text>\makechapterstyle{largePrint}{%&#10;</xsl:text>
	<xsl:text>  \renewcommand*{\chapterheadstart}{} %% no space before chapter&#10;</xsl:text>
	<xsl:text>  \renewcommand*{\printchaptername}{} %% do not print "Chapter"&#10;</xsl:text>
	<xsl:text>  \renewcommand*{\chaptitlefont}{\normalfont\LARGE\bfseries\raggedright} %% set the font for the title&#10;</xsl:text>
	<xsl:text>  \renewcommand*{\printchapternum}{} %% do not print chapter number&#10;</xsl:text>
	<xsl:text>  \renewcommand*{\afterchapternum}{} %% no space after the number (since it isn't printed)&#10;</xsl:text>
	<xsl:text>}&#10;</xsl:text>

	<xsl:text>\makechapterstyle{largePrintScientific}{%&#10;</xsl:text>
	<xsl:text>  \renewcommand*{\chaptitlefont}{\normalfont\LARGE\bfseries\raggedright} %% set the font for the title&#10;</xsl:text>
	<xsl:text>}&#10;</xsl:text>

	<!-- Use one of the chapter styles we just defined -->
	<xsl:choose>
	  <xsl:when test="$pageStyle='scientific'">
	    <xsl:text>\chapterstyle{largePrintScientific}&#10;</xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:text>\chapterstyle{largePrint}&#10;</xsl:text>
	  </xsl:otherwise>
	</xsl:choose>

	<!-- use slightly smaller fonts for section headings -->
	<xsl:text>\setsecheadstyle{\Large\bfseries\raggedright}&#10;</xsl:text>
	<xsl:text>\setsubsecheadstyle{\large\bfseries\raggedright}&#10;</xsl:text>
	<xsl:text>\setsubsubsecheadstyle{\bfseries\raggedright}&#10;</xsl:text>

	<!-- calculate the textheight minus the caption -->
	<xsl:text>\newlength{\textheightMinusCaption}&#10;</xsl:text>
	<xsl:text>\setlength{\textheightMinusCaption}{\textheight - \baselineskip}&#10;</xsl:text>

	<xsl:if test="$pageStyle=('plain', 'compact', 'spacious')">
	  <!-- do not number the sections -->
	  <xsl:text>\setsecnumdepth{book}&#10;&#10;</xsl:text>
	</xsl:if>

	<xsl:if test="$pageStyle!='scientific'">
	  <!-- Drop the numbering from the TOC -->
	  <xsl:text>\renewcommand{\cftchapterleader}{, }&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftchapterafterpnum}{\cftparfillskip}&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftsectionleader}{, }&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftsectionafterpnum}{\cftparfillskip}&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftsubsectionleader}{, }&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftsubsectionafterpnum}{\cftparfillskip}&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftsubsubsectionleader}{, }&#10;</xsl:text>
	  <xsl:text>\renewcommand{\cftsubsubsectionafterpnum}{\cftparfillskip}&#10;</xsl:text>

	  <!-- Display page numbers on the right on a recto page -->
	  <xsl:text>\makeevenfoot{plain}{\thepage}{}{}&#10;</xsl:text>
	  <xsl:text>\makeoddfoot{plain}{}{}{\thepage}&#10;</xsl:text>
	</xsl:if>

	<xsl:if test="$alignment='left'">
	  <!-- set the TOC entries ragged right -->
	  <xsl:text>\setrmarg{3.55em plus 1fil}&#10;</xsl:text>
	</xsl:if>

	<!-- Set the depth of the toc based on how many nested lic there are in the frontmatter -->	
	<xsl:call-template name="setmaxtocdepth"/>

	<xsl:choose>
	  <xsl:when test="$endnotes = 'none'">
	    <!-- footnote styling -->
	    <!-- Use the normal font -->
	    <xsl:text>\renewcommand{\foottextfont}{\normalsize}&#10;</xsl:text>
	    <!-- add some space after the footnote marker -->
	    <xsl:text>\footmarkstyle{\textsuperscript{#1} }&#10;</xsl:text>
	    <!-- paragraph indenting -->
	    <xsl:text>\setlength{\footmarkwidth}{0ex}&#10;</xsl:text>
	    <xsl:text>\setlength{\footmarksep}{\footmarkwidth}&#10;</xsl:text>
	    <!-- space between footnotes -->
	    <xsl:text>\setlength{\footnotesep}{\onelineskip}&#10;</xsl:text>
	    
	    <!-- rule -->
	    <xsl:text>\renewcommand{\footnoterule}{%&#10;</xsl:text>
	    <xsl:text>\kern-3pt%&#10;</xsl:text>
	    <xsl:text>\hrule height 1.5pt&#10;</xsl:text>
	    <xsl:text>\kern 2.6pt}&#10;</xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- endnotes -->
	    <xsl:text>\makepagenote&#10;</xsl:text>
	    <!-- Make the numbering of the notes continuous -->
	    <xsl:text>\continuousnotenums&#10;</xsl:text>
	    <xsl:if test="$endnotes = 'chapter'">
	      <xsl:text>\renewcommand{\notedivision}{\section{\notesname}}&#10;</xsl:text>
	      <xsl:text>\renewcommand{\pagenotesubhead}[3]{}&#10;</xsl:text>
	    </xsl:if>
	    <xsl:if test="$endnotes='document' and $pageStyle=('plain', 'compact', 'spacious', 'withPageNums')">
	      <!-- do not number the sections in the footnote chapter -->
	      <xsl:text>\renewcommand{\pagenotesubhead}[3]{\section*{#3}}&#10;</xsl:text>
	    </xsl:if>
	  </xsl:otherwise>
	</xsl:choose>

	<!-- Make sure enumerations (such as 'a)') can handle more that 26 items. This is done using the
	     enumitem and the alphalph packages. See https://tex.stackexchange.com/a/269559 -->
	<xsl:text>&#10;</xsl:text>
	<xsl:text>\makeatletter&#10;</xsl:text>
	<xsl:text>\newcommand{\AlphUpper}[1]{\@AlphUpper{#1}} % Define the \AlphAlph wrapper for enumitem&#10;</xsl:text>
	<xsl:text>\newcommand{\AlphLower}[1]{\@AlphLower{#1}} % Define the \alphalph wrapper for enumitem&#10;</xsl:text>
	<xsl:text>\newcommand{\@AlphUpper}[1]{\AlphAlph{\value{#1}}} % Internal representation&#10;</xsl:text>
	<xsl:text>\newcommand{\@AlphLower}[1]{\alphalph{\value{#1}}} % Internal representation&#10;</xsl:text>
	<xsl:text>\AddEnumerateCounter{\AlphUpper}{\@AlphUpper}{A} % Register this new format&#10;</xsl:text>
	<xsl:text>\AddEnumerateCounter{\AlphLower}{\@AlphLower}{a} % Register this new format&#10;</xsl:text>
	<xsl:text>\makeatother&#10;&#10;</xsl:text>

        <!-- Monkey patch the memoir plainbreak command as it results
             in weird page breaks -->
	<!-- FIXME: Check if this still is needed in memoir above 3.7f -->
        <xsl:text>\makeatletter&#10;</xsl:text>
        <xsl:text>\renewcommand*{\@pbreak}[1]{\par&#10;</xsl:text>
        <xsl:text>\penalty -100&#10;</xsl:text>
	<xsl:text>%% \vskip #1\onelineskip \@plus 2\onelineskip&#10;</xsl:text>
        <xsl:text>%% \penalty -20&#10;</xsl:text>
        <xsl:text>%% \vskip \z@ \@plus -2\onelineskip&#10;</xsl:text>
        <xsl:text>\vskip #1\onelineskip&#10;</xsl:text>
        <xsl:text>\@afterindentfalse&#10;</xsl:text>
	<xsl:text>\@afterheading}&#10;</xsl:text>
	<xsl:text>\makeatletter&#10;</xsl:text>

	<xsl:if test="$line_spacing = 'onehalfspacing'">
	  <xsl:text>\OnehalfSpacing&#10;</xsl:text>
	</xsl:if>
	<xsl:if test="$line_spacing = 'doublespacing'">
	  <xsl:text>\DoubleSpacing&#10;</xsl:text>
	</xsl:if>

	<!-- Increase the spacing in toc -->
	<xsl:text>\setlength{\cftparskip}{0.25\onelineskip}&#10;</xsl:text>
	
	<!-- Make sure wrapped poetry lines are not indented -->
	<xsl:text>\setlength{\vindent}{0em}&#10;</xsl:text>

	<!-- Poem titles should be left aligned (instead of centered) -->
	<xsl:if test="//dtb:poem/dtb:title|//dtb:poem/dtb:hd">
	  <xsl:text>\renewcommand*{\PoemTitlefont}{\normalfont\large}&#10;</xsl:text>
	</xsl:if>

    <xsl:if test="//dtb:dl">
      <!-- New environment for description lists (with potentially long description labels) -->
      <!-- see https://tex.stackexchange.com/a/35494 -->
      <xsl:text>\newenvironment{longdescription}&#10;</xsl:text>
      <xsl:text>  {\begin{description}[style=unboxed]}&#10;</xsl:text>
      <xsl:text>  {\end{description}}&#10;</xsl:text>
    </xsl:if>

    <xsl:apply-templates select="." mode="localizeWords"/>
    <xsl:apply-templates/>
   </xsl:template>

   <xsl:template name="iso639toBabel">
     <!-- Could probably also use lookup tables here as explained in
     http://www.ibm.com/developerworks/library/x-xsltip.html and
     http://www.ibm.com/developerworks/xml/library/x-tiplook.html -->
     <xsl:param name="iso639Code"/>
     <xsl:variable name="babelLang">
       <xsl:choose>
   	 <xsl:when test="matches($iso639Code, 'sv(-.+)?')">swedish</xsl:when>
   	 <xsl:when test="matches($iso639Code, 'en-[Uu][Ss]')">USenglish</xsl:when>
   	 <xsl:when test="matches($iso639Code, 'en-[Uu][Kk]')">UKenglish</xsl:when>
   	 <xsl:when test="matches($iso639Code, 'en(-.+)?')">english</xsl:when>
   	 <xsl:when test="matches($iso639Code, 'de-1901')">german</xsl:when>
   	 <xsl:when test="matches($iso639Code, 'de(-.+)?')">ngerman</xsl:when>
	 <xsl:otherwise>
	   <xsl:message>
	     ***** <xsl:value-of select="$iso639Code"/> not supported. Defaulting to '<xsl:value-of select="$defaultLanguage"/>' ******
	   </xsl:message>
	   <xsl:value-of select="$defaultLanguage"/></xsl:otherwise>
       </xsl:choose>
     </xsl:variable>
     <xsl:value-of select="$babelLang"/>
   </xsl:template>

   <xsl:template name="findLanguage">
     <xsl:variable name="iso639Code">
       <xsl:choose>
	 <xsl:when test="//dtb:meta[@name='dc:Language']">
	   <xsl:value-of select="//dtb:meta[@name='dc:Language']/@content"/>
	 </xsl:when>
	 <xsl:when test="//dtb:meta[@name='dc:language']">
	   <xsl:value-of select="//dtb:meta[@name='dc:language']/@content"/>
	 </xsl:when>
	 <xsl:when test="/dtb:dtbook/@xml:lang">
	   <xsl:value-of select="/dtb:dtbook/@xml:lang"/>
	 </xsl:when>   			
       </xsl:choose>
     </xsl:variable>
     <xsl:text>\usepackage[</xsl:text>
     <xsl:call-template name="iso639toBabel">
       <xsl:with-param name="iso639Code">
	 <xsl:value-of select="$iso639Code"/>
       </xsl:with-param>
     </xsl:call-template>
     <xsl:text>]{babel}&#10;</xsl:text>
   </xsl:template>

   <xsl:template name="setmaxtocdepth">
     <!-- Determine the depth of toc by calculating the depth of the lic inside list in the frontmatter -->
     <xsl:variable 
	 name="max_toc_depth" 
	 select="max(for $node in //dtb:frontmatter/dtb:level1/dtb:list//dtb:lic return count($node/ancestor::dtb:list))"/>

     <xsl:if test="$max_toc_depth &gt; 0">
       <xsl:text>\maxtocdepth{</xsl:text>
       <xsl:value-of select="substring($level_to_section_map/entry[@key=concat('level',$max_toc_depth)],2)"/>
       <xsl:text>}&#10;</xsl:text>
     </xsl:if>
   </xsl:template>

  <xsl:template name="set_frontmatter_pagestyle">
    <xsl:if test="$pageStyle=('plain', 'compact', 'spacious')">
      <xsl:text>\pagestyle{empty}&#10;</xsl:text>
      <xsl:text>\aliaspagestyle{chapter}{empty}&#10;</xsl:text>
    </xsl:if>
    <xsl:if test="$pageStyle='withPageNums'">
      <xsl:text>\pagestyle{plain}&#10;</xsl:text>
    </xsl:if>
    <xsl:text>\openright&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="restore_pagestyle">
    <xsl:value-of 
	select="if ($pageStyle=('plain', 'compact', 'spacious', 'withPageNums'))
		then '\pagestyle{plain}&#10;' else '\pagestyle{Ruled}&#10;'"/>
    <xsl:text>\aliaspagestyle{chapter}{plain}&#10;</xsl:text>
    <xsl:if test="$pageStyle='compact'">
      <xsl:text>\openany&#10;</xsl:text>
    </xsl:if>
  </xsl:template>

   <xsl:template name="current_volume_string">
     <xsl:param name="current_volume_number"/>
     <xsl:value-of select="concat('Volume ', $current_volume_number, ' of ', $number_of_volumes, '\\[0.5cm]&#10;')"/>
   </xsl:template>
   
   <xsl:template name="total_volumes_string">
     <xsl:variable name="volumes-string" select="if ($number_of_volumes = 1) then 'volume' else 'volumes'"/>
     <xsl:value-of select="concat('Large print book in \numtoname{', $number_of_volumes, '} ', $volumes-string, '\\[0.5cm]&#10;')"/>
   </xsl:template>
   
   <xsl:template name="publisher">
     <xsl:for-each select="//dtb:meta[@name='dc:publisher' or @name='dc:Publisher']">
       <xsl:text>{\large </xsl:text>
       <xsl:value-of select="my:quoteSpecialChars(string(@content))"/>
       <xsl:text>}\\[0.5cm]&#10;</xsl:text>
     </xsl:for-each>
   </xsl:template>
   
   <xsl:template name="imprint">
   </xsl:template>

   <xsl:template name="author">
     <xsl:param name="font_size" select="'\large'"/>
     <xsl:value-of select="concat('{', $font_size, ' ')"/>
     <xsl:variable name="author">
       <xsl:for-each select="//dtb:meta[@name='dc:creator' or @name='dc:Creator']">
	 <xsl:value-of select="my:quoteSpecialChars(string(@content))"/>
	 <xsl:if test="not(position() = last())"><xsl:text>, </xsl:text></xsl:if>
       </xsl:for-each>
     </xsl:variable>
     <xsl:sequence select="if (normalize-space($author) != '') then $author else '\ '"/>
     <xsl:text>}\\[1.5cm]&#10;</xsl:text>
   </xsl:template>

   <xsl:template name="title">
     <xsl:param name="font_size" select="'\huge'"/>
     <xsl:text>\begin{Spacing}{1.75}&#10;</xsl:text>
     <xsl:for-each select="//dtb:meta[@name='dc:title' or @name='dc:Title']">
       <xsl:value-of select="concat('{', $font_size, ' ')"/>
       <xsl:value-of select="my:quoteSpecialChars(string(@content))"/>
       <xsl:text>}\\[0.5cm]&#10;</xsl:text>
     </xsl:for-each>
     <xsl:text>\end{Spacing}&#10;</xsl:text>
   </xsl:template>

   <xsl:template name="cover">
     <xsl:param name="current_volume_number" select="1"/>

     <!-- Author(s) -->
     <xsl:call-template name="author"/>

     <!-- Title -->
     <xsl:call-template name="title"/>

     <xsl:if test="$number_of_volumes > 1">
       <!-- Volume information -->
       <xsl:call-template name="current_volume_string">
	 <xsl:with-param name="current_volume_number" select="$current_volume_number"/>
       </xsl:call-template>
     </xsl:if>

     <xsl:text>\vfill&#10;</xsl:text>
     <xsl:call-template name="total_volumes_string"/>
     
     <!-- Publisher -->
     <xsl:call-template name="publisher"/>

     <!-- Imprint -->
     <xsl:call-template name="imprint"/>
   </xsl:template>

   <xsl:template name="volumecover">
     <xsl:text>\cleartorecto&#10;</xsl:text>
     <xsl:text>\savepagenumber&#10;</xsl:text>
     <xsl:call-template name="set_frontmatter_pagestyle"/>
     <xsl:call-template name="cover">
       <xsl:with-param name="current_volume_number" 
		       select="count(preceding::dtb:div[@class='volume-split-point'])+2"/>
     </xsl:call-template>
     <xsl:text>\cleartorecto&#10;</xsl:text>
     <!-- insert a toc in every volume -->
     <xsl:if test="dtb:level1/dtb:list[descendant::dtb:lic]">
       <xsl:text>\tableofcontents*&#10;</xsl:text>
     </xsl:if>
     <xsl:call-template name="restore_pagestyle"/>
     <xsl:text>\restorepagenumber&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:head">
     <xsl:apply-templates/>
   </xsl:template>

   <!-- Ignore meta data and links -->
   <xsl:template match="dtb:meta|dtb:link"/>

   <xsl:template match="dtb:book">
	<xsl:text>\begin{document}&#10;</xsl:text>
	<xsl:if test="$alignment='left'">
	  <xsl:text>\raggedright&#10;</xsl:text>
	</xsl:if>
	<xsl:apply-templates/>
	<xsl:if test="//(dtb:noteref|dtb:annoref) and $endnotes = 'document'">
	  <xsl:text>\printpagenotes&#10;</xsl:text>
	</xsl:if>
	<xsl:text>\end{document}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:frontmatter">
	<xsl:call-template name="set_frontmatter_pagestyle"/>
   	<xsl:text>\frontmatter&#10;</xsl:text>
   	<xsl:apply-templates select="//dtb:meta" mode="titlePage"/>
	<xsl:call-template name="cover"/>
	<xsl:text>\cleartorecto&#10;</xsl:text>
	<xsl:if test="dtb:level1/dtb:list[descendant::dtb:lic]">
		<xsl:text>\tableofcontents*&#10;</xsl:text>
	</xsl:if>
	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:frontmatter/dtb:level1/dtb:list[descendant::dtb:lic]" priority="1">
   	<xsl:message>skipping lic in frontmatter!</xsl:message>
   </xsl:template>

   <xsl:template match="dtb:meta[@name='dc:title' or @name='dc:Title']" mode="titlePage">
     <xsl:text>\title{</xsl:text>
     <xsl:value-of select="my:quoteSpecialChars(string(@content))"/>
     <xsl:text>}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:meta[@name='dc:creator' or @name='dc:Creator']" mode="titlePage">
     <xsl:text>\author{</xsl:text>
     <xsl:value-of select="my:quoteSpecialChars(string(@content))"/>
     <xsl:text>}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:meta[@name='dc:date' or @name='dc:Date']" mode="titlePage">
     <xsl:text>\date{</xsl:text>
     <xsl:value-of select="my:quoteSpecialChars(string(@content))"/>
     <xsl:text>}&#10;</xsl:text>
   </xsl:template>

  <xsl:template match="dtb:level1">
    <!-- Insert an empty header if a level 1 has no h1 -->
    <xsl:if test="empty(dtb:h1)">
      <xsl:text>\chapter*{\ }&#10;</xsl:text>
      <xsl:text>\plainbreak{3}&#10;</xsl:text>
    </xsl:if>
    <xsl:apply-templates/>
    <xsl:if test=".//(dtb:noteref|dtb:annoref) and $endnotes = 'chapter'">
      <xsl:text>\printpagenotes*&#10;</xsl:text>
    </xsl:if>
    <xsl:if test="following::*[1][self::dtb:p]">
      <xsl:text>\plainbreak{1}&#10;</xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- Insert a pagebreak before the level if $pageStyle is set to
       'spacious' and there is some text before within the block -->

  <!-- Note: The question whether there should be a clearpage before
       which sectional divison is really orthogonal to the pageStyle.
       However in order to keep the api simple we are mixing this with
       the pageStyle concept. In theory you could want the compact
       page style and have level2 on a new page. But in reallity you
       don't :-). So we go for this simplification of mixing the two
       concepts. -->
  <xsl:template name="maybe-insert-clearpage">
    <xsl:if test="$pageStyle=('spacious') and
		  ancestor::dtb:bodymatter and
		  my:has-preceding-para-within-parent-level(.)">
      <xsl:text>\clearpage&#10;</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="dtb:level2">
    <xsl:call-template name="maybe-insert-clearpage"/>
    <!-- Insert an empty header if a level 2 has no h2 -->
    <xsl:if test="empty(dtb:h2)">
      <xsl:text>\section*{\ }&#10;</xsl:text>
      <xsl:text>\plainbreak{3}&#10;</xsl:text>
    </xsl:if>
    <xsl:apply-templates/>
    <xsl:if test="following::*[1][self::dtb:p]">
      <xsl:text>\plainbreak{1}&#10;</xsl:text>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="dtb:level3">
    <!-- Insert an empty header if a level 3 has no h3 -->
    <xsl:if test="empty(dtb:h3)">
      <xsl:text>\subsection*{\ }&#10;</xsl:text>
      <xsl:text>\plainbreak{2}&#10;</xsl:text>
    </xsl:if>
    <xsl:apply-templates/>
    <xsl:if test="following::*[1][self::dtb:p]">
      <xsl:text>\plainbreak{1}&#10;</xsl:text>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="dtb:level4">
    <!-- Insert an empty header if a level 4 has no h4 -->
    <xsl:if test="empty(dtb:h4)">
      <xsl:text>\subsubsection*{\ }&#10;</xsl:text>
      <xsl:text>\plainbreak{1}&#10;</xsl:text>
    </xsl:if>
    <xsl:apply-templates/>
    <xsl:if test="following::*[1][self::dtb:p]">
      <xsl:text>\plainbreak{1}&#10;</xsl:text>
    </xsl:if>
  </xsl:template>

   <xsl:template match="dtb:level5">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:level6">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:level">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:doctitle">
   </xsl:template>
   
   <xsl:template match="dtb:docauthor">
   </xsl:template>
   
   <xsl:template match="dtb:covertitle">
   </xsl:template>

   <xsl:template match="dtb:p">   
	<xsl:apply-templates/>
	<xsl:text>&#10;&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:pagenum">
     <xsl:if test="$pageStyle=('withPageNums', 'scientific')">
       <xsl:text>\marginpar{</xsl:text>
       <xsl:apply-templates/>
       <xsl:text>}&#10;</xsl:text>
     </xsl:if>
   </xsl:template>

   <xsl:template match="dtb:address">
     <xsl:apply-templates/>
     <xsl:text>&#10;</xsl:text>
   </xsl:template>
   
   <xsl:template match="dtb:address/dtb:line">
     <xsl:apply-templates/>
     <xsl:if test="following-sibling::*"><xsl:text>\\</xsl:text></xsl:if>
     <xsl:text>&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6">
     <xsl:variable name="level" select="local-name(ancestor::dtb:*[matches(local-name(),'^level[1-6]$')][1])"/>
     <xsl:value-of select="$level_to_section_map/entry[@key=$level]"/>
     <xsl:choose>
       <xsl:when test="ancestor::dtb:frontmatter">
	 <!-- do not add the title to the toc if we are in the frontmatter -->
	 <xsl:text>*</xsl:text>
       </xsl:when>
       <xsl:otherwise>
	 <xsl:text>[</xsl:text>
	 <xsl:value-of select="normalize-space(my:quoteSpecialChars(string()))"/>
	 <xsl:text>]</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
     <xsl:text>{</xsl:text>
     <xsl:variable name="headline-content">
       <xsl:apply-templates/>
     </xsl:variable>
     <xsl:value-of select="normalize-space($headline-content)"/>
     <xsl:text>}&#10;</xsl:text>
     <xsl:apply-templates select="my:first-pagenum-anchor-before-headline(.)" mode="inside-headline"/>
   </xsl:template>

   <xsl:template match="dtb:bridgehead">
     <!-- a bridgehead inside level{N} is displayed like a h{N+1} (without the toc entry) -->
     <xsl:variable name="level" 
		   select="concat('level',number(substring-after(local-name(ancestor::dtb:*[matches(local-name(),'^level[1-6]$')][1]),'level'))+1)"/>
     <!-- FIXME: This will fail if we are inside a level6. I guess we should define a LaTeX
          command \subsubparagraph*, give it some styling and add it to level_to_section_map
          -->
     <xsl:value-of select="$level_to_section_map/entry[@key=$level]"/>
     <xsl:text>*{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}&#10;</xsl:text>   
   </xsl:template>

   <xsl:template match="dtb:list[not(@type)]">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:lic">
   	<xsl:apply-templates/>
   	<xsl:if test="not(preceding-sibling::dtb:lic) and (following-sibling::dtb:lic or normalize-space(following-sibling::text())!='')">
	   	<xsl:text>\dotfill </xsl:text>
   	</xsl:if>
   </xsl:template>

   <!-- Older versions of memoir really do not like linebreaks in the form of '\\' inside headings. So we use '\newline' instead -->
   <xsl:template match="dtb:h1/dtb:br|dtb:h2/dtb:br|dtb:h3/dtb:br|dtb:h4/dtb:br|dtb:h5/dtb:br|dtb:h6/dtb:br|dtb:bridgehead/dtb:br">
   	<xsl:text>\newline </xsl:text>
   </xsl:template>

   <xsl:template match="dtb:br">
   	<xsl:text>\\*&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:bodymatter">
     <xsl:text>\mainmatter&#10;</xsl:text>
     <xsl:call-template name="restore_pagestyle"/>
     <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:noteref|dtb:annoref">
     <xsl:variable name="refText">
       <xsl:apply-templates select="//(dtb:note|dtb:annotation)[@id=translate(current()/@idref,'#','')]" mode="footnotes"/>
     </xsl:variable>
     <xsl:if test="self::dtb:annoref">
       <!-- for annorefs we want to keep the content -->
       <xsl:apply-templates/>
     </xsl:if>
     <xsl:choose>
       <xsl:when test="$endnotes = 'none'">
	 <xsl:text>\footnotemark</xsl:text>
	 <xsl:text>\footnotetext{</xsl:text>
	 <xsl:if test="$alignment='left'"><xsl:text>\raggedright </xsl:text></xsl:if>
	 <xsl:value-of select="string($refText)"/>
	 <xsl:text>}</xsl:text>
       </xsl:when>
       <xsl:otherwise>
	 <xsl:text>\pagenote{</xsl:text>
	 <xsl:value-of select="string($refText)"/>
	 <xsl:text>}</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="dtb:img">
     <xsl:choose>
       <xsl:when test="$include_images='false'">
	 <!-- ignore the image -->
       </xsl:when>
       <xsl:otherwise>
	 <xsl:variable name="captions" select="//dtb:caption[@id=tokenize(translate(current()/@imgref,'#',''), '\s+')]|following-sibling::*[1][self::dtb:caption]"/>
	 <xsl:text>\begin{figure}[htbp!]&#10;</xsl:text>
	 <xsl:value-of select="my:includegraphics-command(@src, exists($captions))"/>
	 <!-- a caption is associated with an image through an imgref attribute or a bit less formal
              simply by following it immediately -->
	 <xsl:apply-templates select="$captions" mode="referenced-caption" />
	 <xsl:text>\end{figure}&#10;&#10;</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="dtb:h1/dtb:img|dtb:h2/dtb:img|dtb:h3/dtb:img|dtb:h4/dtb:img|dtb:h5/dtb:img|dtb:h6/dtb:img">
     <xsl:choose>
       <xsl:when test="$include_images='false'">
	 <!-- ignore the image -->
       </xsl:when>
       <xsl:otherwise>
	 <xsl:value-of select="my:includegraphics-command(@src,false())"/>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="dtb:table//dtb:img|dtb:sidebar//dtb:img" priority="10">
     <xsl:choose>
       <xsl:when test="$include_images='false'">
	 <!-- ignore the image -->
       </xsl:when>
       <xsl:otherwise>
	 <xsl:variable name="captions" select="//dtb:caption[@id=tokenize(translate(current()/@imgref,'#',''), '\s+')]|following-sibling::*[1][self::dtb:caption]"/>
	 <!-- images inside tables and sidebars do not float -->
	 <xsl:value-of select="my:includegraphics-command(@src, exists($captions))"/>
	 <!-- a caption is associated with an image through an imgref attribute or a bit less formal
              simply by following it immediately -->
	 <xsl:apply-templates select="$captions" mode="referenced-caption">
	 </xsl:apply-templates>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="dtb:caption">
     <!-- Ignore captions that aren't inside a table or not referenced -->
   </xsl:template>

   <xsl:template match="dtb:caption" mode="referenced-caption">
     <xsl:variable name="caption" select="my:cleanCaptions(.)"/>
     <xsl:value-of select="concat('\legend{',$caption,'}&#10;')"/>
     <xsl:value-of select="concat('\addcontentsline{lof}{figure}{',$caption,'}&#10;')"/>
   </xsl:template>

   <xsl:template match="dtb:imggroup//dtb:prodnote" priority="100">
     <xsl:choose>
       <xsl:when test="exists(//dtb:img[@id=tokenize(translate(current()/@imgref,'#',''),'\s+')]|preceding-sibling::*[1][self::dtb:img])">
	 <!-- if a prodnote inside an imggroup is associated with an image it is
	      really an extended image description. -->
	 <!-- Most likely the large print user rather wants to see the image not
	      the description, so ignore the description. -->
       </xsl:when>
       <xsl:otherwise>
	 <xsl:text>\begin{tcolorbox}[colback=black!10,floatplacement=h!]</xsl:text>
	 <xsl:text>&#10;\raggedright&#10;</xsl:text>
	 <xsl:apply-templates/>
	 <xsl:text>\end{tcolorbox}&#10;</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <!-- What's the point of a div? Usually you want some visual clue
        that the content inside the div is special, hence the break
        before and after -->
   <xsl:template match="dtb:div">
     <xsl:text>\plainbreak{1.5}&#10;&#10;</xsl:text>
   	<xsl:apply-templates/>
     <xsl:text>\plainbreak{1.5}&#10;&#10;</xsl:text>
   </xsl:template>

   <!-- Volume boundaries are indicated in the xml by an empty div
        with a specific class. Insert a titlepage where the new volume
        is to start -->
   <xsl:template match="dtb:div[@class='volume-split-point']">
     <xsl:call-template name="volumecover"/>
   </xsl:template>

   <xsl:template match="@*|node()" mode="volume-split">
     <xsl:if test="contains(@class, 'volume-split-point')">
       <xsl:if test="'volume-split-point'=tokenize(@class, '\s+')">
         <xsl:element name="div" namespace="http://www.daisy.org/z3986/2005/dtbook/">
           <xsl:attribute name="class" select="'volume-split-point'"/>
           <xsl:element name="p" namespace="http://www.daisy.org/z3986/2005/dtbook/"/>
         </xsl:element>
       </xsl:if>
     </xsl:if>
     <xsl:copy>
       <xsl:apply-templates select="@*|node()" mode="volume-split"/>
     </xsl:copy>
   </xsl:template>
  
   <xsl:template match="@class[contains(., 'volume-split-point')]" mode="volume-split">
     <xsl:attribute name="class" select="string-join(tokenize(., '\s+')[not(.='volume-split-point')], ' ')"/>
   </xsl:template>
  
   <xsl:template match="dtb:imggroup">
     <!-- By default as we scale the images we probably do not have
          enough space on the page to group images, i.e. put them next
          to each other. So it is probably best to just let them float
          as if they weren't even in a imggroup. One way to indicate
          that they are grouped would be to place them inside a frame
          or put rules above and below, but this visualization will
          break down if we let them float and will have other text in
          between the images. -->
     <!-- The use case where you have one caption for multiple images
          is currently supported by using the imgref attribute as
          mentioned in the standard. The use case as mentioned in the
          nordic markup requirements that a caption is for the whole
          imggroup when it doesn't follow an image is currently not
          supported. -->
     <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:author">	
   	<xsl:apply-templates/>
   </xsl:template>

   <!-- Treat authors inside levels, divs and blockquotes as if they were paragraphs -->
  <xsl:template match="dtb:author[parent::dtb:level|parent::dtb:level1|parent::dtb:level2|parent::dtb:level3|parent::dtb:level4|parent::dtb:level5|parent::dtb:level6|parent::dtb:div|parent::dtb:blockquote]">
    <xsl:apply-templates/>
    <xsl:text>&#10;&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:blockquote">
   	<xsl:text>\begin{quote}&#10;</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>\end{quote}&#10;</xsl:text>
   </xsl:template>

  <xsl:template match="dtb:byline">
  	<xsl:apply-templates/>
   </xsl:template>

   <!-- Treat bylines inside levels, divs and blockquotes as if they were paragraphs -->
  <xsl:template match="dtb:byline[parent::dtb:level|parent::dtb:level1|parent::dtb:level2|parent::dtb:level3|parent::dtb:level4|parent::dtb:level5|parent::dtb:level6|parent::dtb:div|parent::dtb:blockquote]">
    <xsl:apply-templates/>
    <xsl:text>&#10;&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:dateline">
     <xsl:apply-templates/>
     <xsl:text>&#10;&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:epigraph">
     <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:note|dtb:annotation">
   	<!--<xsl:apply-templates/>-->
   </xsl:template>

   <xsl:template match="dtb:note|dtb:annotation" mode="footnotes">
     <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:note/dtb:p|dtb:annotation/dtb:p">
     <xsl:apply-templates/>
     <xsl:if test="position() != last()"><xsl:text>&#10;&#10;</xsl:text></xsl:if>
   </xsl:template>

   <xsl:template match="dtb:sidebar">
     <xsl:text>\begin{tcolorbox}[breakable,floatplacement=htp!]&#10;</xsl:text>
     <xsl:text>\raggedright&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{tcolorbox}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:sidebar[@class='no-float']">
     <xsl:text>\begin{tcolorbox}[breakable,nofloat]&#10;</xsl:text>
     <xsl:text>\raggedright&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{tcolorbox}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:sidebar//dtb:sidebar">
     <!-- a nested sidebar should obviously not float and cannot be
          breakable due to limitations of tcolorbox -->
     <xsl:text>\begin{tcolorbox}[nofloat]&#10;</xsl:text>
     <xsl:text>\raggedright&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{tcolorbox}&#10;</xsl:text>
   </xsl:template>

   <!-- Headings inside sidebars and linegroups aren't real headings in
        the sense that they contribute to the hierarchy and need to be
        in the toc. Simply make them stand out by making them bold -->
   <xsl:template match="dtb:sidebar/dtb:hd|dtb:linegroup/dtb:hd">
     <xsl:text>\textbf{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}&#10;&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:hd">
     <xsl:variable name="level">
       <xsl:value-of select="count(ancestor::dtb:level)"/>
     </xsl:variable>
     <xsl:value-of select="$level_to_section_map/entry[@key=concat('level',$level)]"/>
     <xsl:text>[</xsl:text>
     <xsl:value-of select="text()"/>
     <xsl:text>]{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}&#10;</xsl:text>
   </xsl:template>

   <!-- Headings inside lists aren't real headings in the sense that
        they contribute to the hierarchy and need to be in the toc.
        Simply make them stand out by making them bold -->
   <xsl:template match="dtb:list/dtb:hd" mode="insert-heading">
	<xsl:text>\paragraph{</xsl:text>
	<xsl:apply-templates/>
	<xsl:text>}&#10;</xsl:text>
   </xsl:template>

   <!-- Ignore heading inside lists as they already have been dealt with -->
   <xsl:template match="dtb:list/dtb:hd">
   </xsl:template>

   <xsl:template match="dtb:list[@type='ol']">
     <xsl:apply-templates select="dtb:hd" mode="insert-heading"/>
     <xsl:text>\begin{enumerate}</xsl:text>
     <xsl:text>[</xsl:text>
     <!-- Set the number of the first item -->
     <xsl:choose>
       <xsl:when test="@start"><xsl:text>start=</xsl:text><xsl:value-of select="@start"/></xsl:when>
       <xsl:otherwise><xsl:text>start=1</xsl:text></xsl:otherwise>
     </xsl:choose>
     <!-- Make sure the list is not indented unless it is a nested list -->
     <xsl:if test="not(parent::dtb:li)">
       <xsl:text>,leftmargin=*</xsl:text>
     </xsl:if>
     <!-- Set the label to be used -->
       <xsl:choose>
	 <xsl:when test="index-of(('1'), string(@enum))"><xsl:text>,label=\arabic*.</xsl:text></xsl:when>
	 <xsl:when test="index-of(('a'), string(@enum))"><xsl:text>,label=\AlphLower*.</xsl:text></xsl:when>
	 <xsl:when test="index-of(('A'), string(@enum))"><xsl:text>,label=\AlphUpper*.</xsl:text></xsl:when>
	 <xsl:when test="index-of(('i'), string(@enum))"><xsl:text>,label=\roman*.</xsl:text></xsl:when>
	 <xsl:when test="index-of(('I'), string(@enum))"><xsl:text>,label=\Roman*.</xsl:text></xsl:when>
       </xsl:choose>
     <xsl:text>]&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{enumerate}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:list[@type='ul']">
     <xsl:apply-templates select="dtb:hd" mode="insert-heading"/>
     <!-- Make sure the list is not indented -->
     <xsl:text>\begin{itemize}[leftmargin=*]&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{itemize}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:list//dtb:list[@type='ul']" priority="10">
     <xsl:apply-templates select="dtb:hd" mode="insert-heading"/>
     <xsl:text>\begin{itemize}&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{itemize}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:list[@type='pl']">
     <xsl:apply-templates select="dtb:hd" mode="insert-heading"/>
     <!-- Use a plain old trivlist for root lists of pl style -->
     <xsl:text>\begin{trivlist}&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{trivlist}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:list//dtb:list[@type='pl']" priority="10">
     <xsl:apply-templates select="dtb:hd" mode="insert-heading"/>
     <!-- use a normal indent for nested list -->
     <xsl:text>\begin{enumerate}[label=,leftmargin=*,labelsep=*]&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{enumerate}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:li">
     <xsl:variable name="itemContent">
	<xsl:apply-templates/>
     </xsl:variable>
     <!-- if the item contains a sublist and no text for the actual
          item itself drop the '\item' -->
     <xsl:if test="not(./dtb:list) or ./text()[1][normalize-space() != '']">
       <xsl:text>\item </xsl:text>
     </xsl:if>
     <!-- quote [] right after an \item with {} -->
     <xsl:value-of select="replace($itemContent,'^(\s*)(\[.*\])','$1{$2}')"/>
     <xsl:text>&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:dl">
   	<xsl:text>\begin{longdescription}&#10;</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>\end{longdescription}&#10;</xsl:text>
   </xsl:template>

  <xsl:template match="dtb:dt">
  	<xsl:text>\item[</xsl:text>
  	<xsl:apply-templates/>
  	<xsl:text>] </xsl:text>
   </xsl:template>

  <xsl:template match="dtb:dd">
  	<xsl:apply-templates/>
  	<xsl:text>&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:table">
     <xsl:variable name="normalized-table">
       <xsl:apply-templates mode="normalize-table" select="."/>
     </xsl:variable>
     <xsl:text>\begin{table}[H]&#10;</xsl:text>
     <xsl:text>\begin{tabulary}{\textwidth}{|</xsl:text>
     <xsl:variable name="numcols">
       <xsl:value-of select="max(for $row in $normalized-table//dtb:tr return count($row/(dtb:td|dtb:th)))"/>
     </xsl:variable>
     <!-- make all columns left justified and let tabulary deal with spacing of the table -->
     <xsl:value-of select="string-join((for $col in 1 to $numcols return 'L'),'|')"/>
     <xsl:text>|} \hline&#10;</xsl:text>
     <!-- Make sure the table is in the right order and also handle tables without tbody -->
     <xsl:apply-templates select="$normalized-table/dtb:table/dtb:thead, $normalized-table/dtb:table/dtb:tbody, $normalized-table/dtb:table/dtb:tfoot, $normalized-table/dtb:table/dtb:tr"/>
     <xsl:text>\end{tabulary}&#10;</xsl:text>
     <xsl:apply-templates select="dtb:caption"/>
     <xsl:text>\end{table}&#10;</xsl:text>
   </xsl:template>

  <xsl:template match="dtb:table" mode="normalize-table">
    <xsl:variable name="dtb:tr" as="element()*">
      <xsl:call-template name="dtb:insert-covered-table-cells">
        <xsl:with-param name="table_cells" select="dtb:tr/(dtb:td|dtb:th)"/>
        <xsl:with-param name="insert_if_colspan" select="false()" tunnel="yes"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="(dtb:thead, $dtb:tr, dtb:tbody, dtb:tfoot)"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="dtb:thead|dtb:tbody|dtb:tfoot" mode="normalize-table">
    <xsl:variable name="dtb:tr" as="element()*">
      <xsl:call-template name="dtb:insert-covered-table-cells">
        <xsl:with-param name="table_cells" select="dtb:tr/(dtb:td|dtb:th)"/>
        <xsl:with-param name="insert_if_colspan" select="false()" tunnel="yes"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="$dtb:tr"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="dtb:tr" mode="normalize-table">
    <xsl:sequence select="."/>
  </xsl:template>
  
   <xsl:template match="dtb:table/dtb:caption">
     <xsl:variable name="caption" select="my:cleanCaptions(.)"/>
     <xsl:value-of select="concat('\legend{',$caption,'}&#10;')"/>
     <xsl:value-of select="concat('\addcontentsline{lot}{table}{',$caption,'}&#10;')"/>
   </xsl:template>
   
   <xsl:template match="dtb:tbody">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:thead">
   	<xsl:apply-templates/>   
   </xsl:template>

   <xsl:template match="dtb:tfoot">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:tr">
   	<xsl:apply-templates/>
   	  <xsl:text>\\ </xsl:text>
   	<xsl:if test="not(following-sibling::dtb:tr[1]//(dtb:td|dtb:th)[@covered-table-cell='yes'])">
   	  <xsl:text>\hline</xsl:text>
   	</xsl:if>
   	  <xsl:text>&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:th">
   	<xsl:if test="preceding-sibling::dtb:th">
   		<xsl:text> &amp; </xsl:text>
   	</xsl:if>
   	<xsl:text>\textbf{</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:td">
     <xsl:if test="preceding-sibling::dtb:td">
       <xsl:text> &amp; </xsl:text>
     </xsl:if>
     <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:td[@colspan &gt; 1]">
     <xsl:if test="preceding-sibling::dtb:td">
       <xsl:text> &amp; </xsl:text>
     </xsl:if>
     <xsl:text>\multicolumn{</xsl:text><xsl:value-of select="@colspan"/><xsl:text>}{l|}{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:th[@colspan &gt; 1]">
     <xsl:if test="preceding-sibling::dtb:th">
       <xsl:text> &amp; </xsl:text>
     </xsl:if>
     <xsl:text>\multicolumn{</xsl:text><xsl:value-of select="@colspan"/><xsl:text>}{l|}{\textbf{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:colgroup|dtb:col">
     <!-- ignore -->
   </xsl:template>

   <xsl:template match="dtb:poem">
   	<xsl:text>\begin{verse}&#10;</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>\end{verse}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:poem/dtb:line|dtb:poem/dtb:author|dtb:poem/dtb:byline">
   	<xsl:apply-templates/>
     <xsl:if test="following-sibling::*"><xsl:text>\\</xsl:text></xsl:if>
	<xsl:text>&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:poem/dtb:title|dtb:poem/dtb:hd">
     <xsl:text>\PoemTitle*[]{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:cite/dtb:title">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:cite">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:q">
   	<xsl:text>\textsl{</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:samp|dtb:code|dtb:kbd">
     <xsl:text>\texttt{</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:samp[@xml:space='preserve']|dtb:code[@xml:space='preserve']">
     <xsl:text>\begin{alltt}</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{alltt}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:samp[@xml:space='preserve']//text()|dtb:code[@xml:space='preserve']//text()">
     <!-- escape backslash and curly braces -->
     <xsl:value-of select="replace(replace(., '\\', '\\textbackslash '), '(\{|\})', '\\$1')"/>
   </xsl:template>

   <xsl:template match="dtb:linegroup">
   	<xsl:apply-templates select="*"/>
	<xsl:text>&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:linegroup/dtb:line">
     <xsl:apply-templates/>
     <xsl:if test="following-sibling::*"><xsl:text>\\</xsl:text></xsl:if>
     <xsl:text>&#10;</xsl:text>
   </xsl:template>

   <!-- Ignore lines that contain only whitespace -->
   <!-- Otherwise we will end up with a line only containing whitespace and a trailing '\\', which
        will result in the error "! LaTeX Error: There's no line here to end", see
        https://texfaq.org/FAQ-noline -->
   <xsl:template match="dtb:line[normalize-space()='']" priority="1"/>

   <xsl:template match="dtb:line">
   	<xsl:apply-templates/>
	<xsl:text>\\&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:linenum">
     <xsl:variable name="num">
       <xsl:apply-templates/>
     </xsl:variable>
     <xsl:value-of select="concat('\sidepar[',$num,']{',$num,'}')"/>
   </xsl:template>

   <xsl:template match="dtb:line//text()[(preceding-sibling::*|preceding-sibling::text())[1]/self::dtb:linenum]">
     <!-- trim whitespace after the linenum element -->
     <xsl:value-of select="my:quoteSpecialChars(replace(string(current()), '^\s+(.*)$', '$1'))"/>
   </xsl:template>

   <xsl:template match="dtb:prodnote">
     <xsl:text>\begin{tcolorbox}[colback=black!10,floatplacement=h!]</xsl:text>
     <xsl:text>&#10;\raggedright&#10;</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{tcolorbox}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:p/dtb:prodnote">
     <!-- inline prodnote -->
     <xsl:text>\begin{tcolorbox}[colback=black!10,nofloat,after={}]</xsl:text>
     <xsl:apply-templates/>
     <xsl:text>\end{tcolorbox}&#10;</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:rearmatter">
   	<xsl:text>\backmatter&#10;</xsl:text>
	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:a">
     <xsl:apply-templates/>
   </xsl:template>

   <!-- Render external links as URLs -->
   <xsl:template match="dtb:a[@external='true']">
     <!-- Drop { and } as they might lead to unbalanced braces which the url packacke really doesn't
          like. Also drop '\' if it happens to be the last character. Escape the rest so LaTeX doesn't fall over -->
     <xsl:text>\url{</xsl:text><xsl:value-of select="my:quoteSpecialChars(replace(normalize-space(replace(string(), '(\{|\})', '')), '\\$', ''))"/><xsl:text>}</xsl:text>
   </xsl:template>

   <!-- cross references that contain only original page numbers -->
   <!-- We drop the original page number and replace it with a page reference -->
   <xsl:template match="dtb:a[@class='pageref' and starts-with(@href, '#')]">
     <xsl:value-of select="concat('\pageref{',substring(@href,2),'}')"/>
   </xsl:template>

   <xsl:template match="dtb:a[@id != '']">
     <!-- create a label so we can later add a reference to it -->
     <xsl:value-of select="concat('\label{',@id,'}&#10;')"/>
     <xsl:apply-templates/>
   </xsl:template>

  <xsl:function name="my:is-pagenum-anchor" as="xs:boolean">
    <xsl:param name="anchor" as="element()"/>
    <xsl:sequence
	select="exists($anchor/preceding-sibling::*[1][self::dtb:pagenum])"/>
  </xsl:function>

  <xsl:function name="my:first-pagenum-anchor-before-headline" as="element()?">
    <xsl:param name="headline" as="element()"/>
    <xsl:sequence
	select="$headline/preceding-sibling::*[1][self::dtb:a and my:is-pagenum-anchor(.)]"/>
  </xsl:function>

  <xsl:function name="my:is-first-pagenum-anchor-before-headline" as="xs:boolean">
    <xsl:param name="anchor" as="element()"/>
    <xsl:sequence
	select="exists($anchor[my:is-pagenum-anchor(.)]/following-sibling::*[1][matches(name(),'h[1-6]')])"/>
  </xsl:function>

   <xsl:template match="dtb:a[@id != '']" mode="inside-headline">
     <!-- create a label so we can later add a reference to it -->
     <xsl:value-of select="concat('\label{',@id,'}&#10;')"/>
     <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:a[@id != '' and my:is-first-pagenum-anchor-before-headline(.)]" priority="10">
     <!-- ingore anchors before a headline otherwise the label will be
	  refering to the wrong page -->
   </xsl:template>
  
   <xsl:template match="dtb:em">
     <xsl:choose>
       <xsl:when test="$replace_em_with_quote = 'true'">
	 <xsl:text>'</xsl:text>
       </xsl:when>
       <xsl:otherwise>
	 <xsl:text>\emph{</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
     <xsl:apply-templates/>
     <xsl:choose>
       <xsl:when test="$replace_em_with_quote = 'true'">
	 <xsl:text>'</xsl:text>
       </xsl:when>
       <xsl:otherwise>
	 <xsl:text>}</xsl:text>		
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="dtb:strong">
   	<xsl:text>\textbf{</xsl:text>
	<xsl:apply-templates/>
	<xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:abbr">
   	<xsl:apply-templates/>
   </xsl:template>

  <xsl:template match="dtb:acronym">
   	<xsl:apply-templates/>
   </xsl:template>

  <xsl:template match="dtb:bdo">
   	<xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="dtb:dfn">
   	<xsl:apply-templates/>
   </xsl:template>

  <xsl:template match="dtb:sent">
   	<xsl:apply-templates/>
   </xsl:template>

  <xsl:template match="dtb:w">
   	<xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="dtb:sup">
 	<xsl:text>\textsuperscript{</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:sub">
   	<xsl:text>\textsubscript{</xsl:text>
   	<xsl:apply-templates/>
   	<xsl:text>}</xsl:text>
   </xsl:template>

   <xsl:template match="dtb:span">
     <!-- FIXME: What to do with span? It basically depends on the class -->
     <!-- attribute which can be used for anything (colour, typo, error, etc) -->
     <xsl:apply-templates/>
   </xsl:template>

   <!-- remove excessive space and insert non-breaking spaces inside abbrevs -->
   <xsl:template match="dtb:abbr//text()">
    <xsl:value-of select="my:quoteSpecialChars(replace(normalize-space(string(current())), ' ', ' '))"/>
   </xsl:template>

   <!-- add hyphenation points ('\-') between each character. This is a crude method to make sure
        long words (often nonsensical such as "aaaarghhhhhhhhhhhh") do not run into the margin -->
  <xsl:function name="my:add-hyphenation-points" as="xs:string">
    <xsl:param name="word" as="xs:string"/>
    <xsl:variable name="margin" select="3"/>
    <xsl:choose>
      <xsl:when test="string-length($word) > $margin*2">
	<xsl:sequence select="string-join((substring($word,1,$margin),
			                   replace(substring($word,$margin+1,string-length($word)-(2*$margin+1)), '\w', '$0\\-'),
                                           substring($word,string-length($word)-$margin)),'')"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:sequence select="$word"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:string-replace" as="xs:string">
    <xsl:param name="input" as="xs:string"/>
    <xsl:param name="substring" as="xs:string"/>
    <xsl:param name="replacement" as="xs:string"/>
    <xsl:variable name="before" select="substring-before($input,$substring)"/>
    <xsl:variable name="after" select="substring-after($input,$substring)"/>
    <xsl:choose>
      <xsl:when test="$input">
	<xsl:sequence select="string-join(($before,$replacement,$after),'')"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:sequence select="string-join(($before,$replacement,my:string-replace($after,$substring,$replacement)),'')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:hyphenate-long-words" as="xs:string">
    <xsl:param name="wordSequence" as="xs:string*"/>
    <xsl:param name="text" as="xs:string"/>
    <xsl:variable name="word" select="$wordSequence[1]"/>
    <xsl:variable name="rest" select="$wordSequence[position() gt 1]"/>
    <xsl:choose>
      <xsl:when test="empty($wordSequence)">
	<xsl:sequence select="$text"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:sequence select="my:hyphenate-long-words($rest, my:string-replace($text, $word, my:add-hyphenation-points($word)))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:hyphenate-dashed-words" as="xs:string">
    <xsl:param name="wordSequence" as="xs:string*"/>
    <xsl:param name="text" as="xs:string"/>
    <xsl:variable name="word" select="$wordSequence[1]"/>
    <xsl:variable name="rest" select="$wordSequence[position() gt 1]"/>
    <xsl:choose>
      <xsl:when test="empty($wordSequence)">
	<xsl:sequence select="$text"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:sequence select="my:hyphenate-dashed-words($rest, my:string-replace($text, $word, replace($word,'(\w)-(\w)','$1-\\hspace{0pt}$2')))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="text()">
    <xsl:variable name="sanitized" select="my:quoteSpecialChars(current())"/>
    <xsl:variable name="long-words" select="tokenize($sanitized,'\W+')[string-length(.) > my:max-line-width()]"/>
    <xsl:variable name="long-dashed-words" select="tokenize($sanitized,'(\p{Pc}|\p{Ps}|\p{Pe}|\p{Pi}|\p{Pf}|\p{Po}|\p{Z}|\p{C})+')[string-length(.) > 20][contains(.,'-')]"/>
    <xsl:variable name="tmp" select="my:hyphenate-long-words($long-words, $sanitized)"/>
    <xsl:value-of select="my:hyphenate-dashed-words($long-dashed-words, $tmp)"/>
   </xsl:template>

   <xsl:template match="text()" mode="textOnly">
     <xsl:value-of select="my:quoteSpecialChars(string(current()))"/>
   </xsl:template>

   <xsl:template match="dtb:*">
     <xsl:message>
  *****<xsl:value-of select="name(..)"/>/{<xsl:value-of select="namespace-uri()"/>}<xsl:value-of select="name()"/>******
   </xsl:message>
   </xsl:template>

</xsl:stylesheet>
