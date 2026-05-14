<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:dtbook-to-latex.script"
                px:input-filesets="dtbook"
                px:output-filesets="latex">

	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">DTBook to LaTeX</h1>
		<p px:role="desc">Transforms a DTBook (DAISY 3 XML) document into a LaTeX document.</p>
		<a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-latex/">
			Online documentation
		</a>
	</p:documentation>

	<p:input port="source" primary="true" px:media-type="application/x-dtbook+xml">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">DTBook</h2>
			<p px:role="desc">The DTBook 2005 XML file.</p>
		</p:documentation>
	</p:input>

	<p:option name="font-size" required="false" select="'17pt'">
		<p:documentation>
			<h2 px:role="name">Font size</h2>
			<p px:role="desc" xml:space="preserve">Font size for the generated LaTeX.

See also the documentation of the [extsizes
package](http://www.ctan.org/tex-archive/macros/latex/contrib/extsizes).</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice>
					<value>12pt</value>
					<value>14pt</value>
					<value>17pt</value>
					<value>20pt</value>
					<value>25pt</value>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="font" required="false" select="'LMRoman10 Regular'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Font</h2>
			<p px:role="desc">Font for the generated LaTeX. This can be any installed TrueType or OpenType font (e.g. "Tiresias LPfont", "Latin Modern Roman").</p>
		</p:documentation>
	</p:option>

	<p:option name="backup-font" required="false" select="'Arial Unicode MS'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Backup font</h2>
			<p px:role="desc" xml:space="preserve">Optional secondary font for specific Unicode ranges (see backup-unicode-ranges).

Requires the [ucharclasses package](http://ctan.org/tex-archive/macros/xetex/latex/ucharclasses).</p>
		</p:documentation>
	</p:option>

	<p:option name="backup-unicode-ranges" required="false" select="''" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Backup Unicode ranges</h2>
			<p px:role="desc">Comma-separated list of Unicode ranges (camel case) for which the backup font is used, e.g. "Arabic,Hebrew,Cyrillic,GreekAndCoptic".</p>
		</p:documentation>
	</p:option>

	<p:option name="page-style" required="false" select="'plain'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Page style</h2>
			<p px:role="desc">Page style for the generated LaTeX.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice>
					<value>plain</value>
					<value>compact</value>
					<value>spacious</value>
					<value>withPageNums</value>
					<value>scientific</value>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="alignment" required="false" select="'justified'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Alignment</h2>
			<p px:role="desc">Alignment for standard text.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice>
					<value>justified</value>
					<value>left</value>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="default-language" required="false" select="'english'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Default language</h2>
			<p px:role="desc" xml:space="preserve">Language for the babel package when no xml:lang attribute is present.

See the [babel user guide](http://texdoc.net/pkg/babel) for all valid values.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>english</value>
					<a:documentation xml:lang="en">English</a:documentation>
					<value>ngerman</value>
					<a:documentation xml:lang="en">German (new/reformed orthography, post-1996)</a:documentation>
					<value>USenglish</value>
					<a:documentation xml:lang="en">English (American)</a:documentation>
					<value>UKenglish</value>
					<a:documentation xml:lang="en">English (British)</a:documentation>
					<value>swedish</value>
					<a:documentation xml:lang="en">Swedish</a:documentation>
					<value>canadian</value>
					<a:documentation xml:lang="en">English (Canadian)</a:documentation>
					<value>french</value>
					<a:documentation xml:lang="en">French</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="stock-size" required="false" select="'a4paper'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Stock size</h2>
			<p px:role="desc">Stock size for the generated LaTeX.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
					<value>a3paper</value>
					<a:documentation xml:lang="en">ISO 216 A3 paper size</a:documentation>
					<value>a4paper</value>
					<a:documentation xml:lang="en">ISO 216 A4 paper size</a:documentation>
					<value>a5paper</value>
					<a:documentation xml:lang="en">ISO 216 A5 paper size</a:documentation>
					<value>letterpaper</value>
					<a:documentation xml:lang="en">Letter paper size (11 in × 8.5 in)</a:documentation>
					<value>legalpaper</value>
					<a:documentation xml:lang="en">Legal paper size (14 in × 8.5 in)</a:documentation>
					<value>executivepaper</value>
					<a:documentation xml:lang="en">Legal paper size (10.5 in × 7.25 in)</a:documentation>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="line-spacing" required="false" select="'singlespacing'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Line spacing</h2>
			<p px:role="desc">Line spacing in the generated LaTeX.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice>
					<value>singlespacing</value>
					<value>onehalfspacing</value>
					<value>doublespacing</value>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="paper-width" required="false" select="'200mm'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Paper width</h2>
			<p px:role="desc">Width of the paper (e.g. "200mm").</p>
		</p:documentation>
	</p:option>

	<p:option name="paper-height" required="false" select="'250mm'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Paper height</h2>
			<p px:role="desc">Height of the paper (e.g. "250mm").</p>
		</p:documentation>
	</p:option>

	<p:option name="left-margin" required="false" select="'28mm'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Left margin</h2>
			<p px:role="desc">Inner (left) margin of the page (e.g. "28mm").</p>
		</p:documentation>
	</p:option>

	<p:option name="right-margin" required="false" select="'20mm'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Right margin</h2>
			<p px:role="desc">Outer (right) margin of the page (e.g. "20mm").</p>
		</p:documentation>
	</p:option>

	<p:option name="top-margin" required="false" select="'20mm'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Top margin</h2>
			<p px:role="desc">Top margin of the page (e.g. "20mm").</p>
		</p:documentation>
	</p:option>

	<p:option name="bottom-margin" required="false" select="'20mm'" px:type="string">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Bottom margin</h2>
			<p px:role="desc">Bottom margin of the page (e.g. "20mm").</p>
		</p:documentation>
	</p:option>

	<p:option name="replace-em-with-quote" required="false" px:type="boolean" select="'false'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Replace emphasis with quotes</h2>
			<p px:role="desc" xml:space="preserve">Replace em elements with quoted text.

Emphasis can be hard to render in large print.</p>
		</p:documentation>
	</p:option>

	<p:option name="endnotes" required="false" select="'none'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Endnotes</h2>
			<p px:role="desc">Put notes at the end of the chapter or document instead of as footnotes on the same page.</p>
		</p:documentation>
		<p:pipeinfo>
			<px:type>
				<choice>
					<value>none</value>
					<value>document</value>
					<value>chapter</value>
				</choice>
			</px:type>
		</p:pipeinfo>
	</p:option>

	<p:option name="include-images" required="false" px:type="boolean" select="'true'">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">Include Images</h2>
			<p px:role="desc">Whether to include or ignore images in the LaTeX output.</p>
		</p:documentation>
	</p:option>

	<p:option name="result" px:output="result" px:type="anyDirURI" px:media-type="application/x-latex" required="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2 px:role="name">LaTeX</h2>
			<p px:role="desc">The output LaTeX document.</p>
		</p:documentation>
	</p:option>

	<cx:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl" type="application/xslt+xml">
		<p:documentation>
			pf:normalize-uri
		</p:documentation>
	</cx:import>

	<p:variable name="base-name" select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
		<p:documentation>File name without extension</p:documentation>
	</p:variable>

	<p:xslt>
		<p:input port="stylesheet">
			<p:document href="dtbook-to-latex.xsl"/>
		</p:input>
		<p:with-param name="fontsize"             select="$font-size"/>
		<p:with-param name="font"                 select="$font"/>
		<p:with-param name="backupFont"           select="$backup-font"/>
		<p:with-param name="backupUnicodeRanges"  select="$backup-unicode-ranges"/>
		<p:with-param name="pageStyle"            select="$page-style"/>
		<p:with-param name="alignment"            select="$alignment"/>
		<p:with-param name="defaultLanguage"      select="$default-language"/>
		<p:with-param name="stocksize"            select="$stock-size"/>
		<p:with-param name="line_spacing"         select="$line-spacing"/>
		<p:with-param name="paperwidth"           select="$paper-width"/>
		<p:with-param name="paperheight"          select="$paper-height"/>
		<p:with-param name="left_margin"          select="$left-margin"/>
		<p:with-param name="right_margin"         select="$right-margin"/>
		<p:with-param name="top_margin"           select="$top-margin"/>
		<p:with-param name="bottom_margin"        select="$bottom-margin"/>
		<p:with-param name="replace_em_with_quote" select="$replace-em-with-quote"/>
		<p:with-param name="endnotes"             select="$endnotes"/>
		<p:with-param name="include_images"       select="$include-images"/>
		<p:with-param name="version"              select="'${project.version}'"/>
	</p:xslt>

	<p:store method="text">
		<p:with-option name="href" select="pf:normalize-uri(concat($result,'/',$base-name,'.tex'))"/>
	</p:store>

</p:declare-step>
