<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-rtf/"/>
<link rev="dp2:doc" href="../src/main/resources/xml/dtbook-to-rtf.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# DTBook To RTF

The "DTBook to RTF" script will convert a single DTBook XML document to an RTF document.

## Table of contents

{{>toc}}

## Synopsis

{{>synopsis}}


## Example running from command line

On Linux and Mac OS X:

    $ cli/dp2 dtbook-to-rtf
              --source ~/Documents/tests_dtbook/Sample1.xml
              --output-dir ~/tmp/testdtbooktortf

On Windows:

    $ cli\dp2.exe dtbook-to-rtf
                  --source samples\dtbook\Sample1.xml
                  --output-dir C:\Pipeline2-Output

Input DTBook:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE dtbook
  PUBLIC '-//NISO//DTD dtbook 2005-3//EN'
  'http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd'>
<dtbook version="2005-3" xml:lang="fr" xmlns="http://www.daisy.org/z3986/2005/dtbook/">
  <head>
    <meta content="AUTO-UID-5373731396833" name="dtb:uid"/>
    <meta content="fr-00000-packaged" name="dc:Identifier"/>
    <meta content="Test Dtbook To RTF" name="dc:Title"/>
    <meta content="Book Creator" name="dc:Creator"/>
    <meta content="Book Producer" name="dtb:producer"/>
    <meta content="Source Publisher" name="dtb:sourcePublisher"/>
    <meta content="Source Rights" name="dtb:sourceRights"/>
    <meta content="0-00000-000-0" name="dc:Identifier" scheme="ISBN"/>
    <meta content="1111111111" name="dc:Identifier" scheme="EAN"/>
    <meta content="2016-10-26" name="dc:Date"/>
    <meta content="Publisher" name="dc:Publisher"/>
    <meta content="fr-FR" name="dc:Language"/>
    <meta content="Text" name="dc:Type"/>
    <meta content="ANSI/NISO Z39.86-2005" name="dc:Format"/>
  </head>
  <book>
    <frontmatter>
      <doctitle>Test Dtbook To RTF</doctitle>
      <docauthor>Book Author</docauthor>
    </frontmatter>
    <bodymatter>
      <level1>
        <p>Content 1</p>
        <p>Content 2</p>
        <p>Content 3</p>
        <p xml:lang="en-GB">Titre original : &quot;Test Dtbook To RTF&quot;</p>
        <p>Copyright : Source Rights, 1998</p>
        <p>ISBN 0-00000-000-0</p>
        <p>Transcription en braille intégral : Bibliothèque Braille Intégral</p>
        <p>juin 2004</p>
      </level1>
      <level1>
        <h1>Content Before
          <sup>Sup</sup>
          Content After</h1>
          <p>Content</p>
      </level1>
    </bodymatter>
  </book>
</dtbook>
~~~

Output RTF:

<pre><code>{\rtf1\ansi\ansicpg1252\deff0 {\fonttbl{\f0\fswiss Arial;}{\f1\fmodern Courier New;}}{\colortbl;\red0\green0\blue0;\red255\green255\blue255;\red0\green0\blue255;}{\stylesheet{\s0\plain\fs20 \sb100\sa100\li0\ri0 \sbasedon222\snext0 Normal;}{\s1\sb200\sa100\li0\ri0\plain\fs32\b \sbasedon0\snext0 heading 1;}{\s2\sb200\sa100\li0\ri0\plain\fs28\b\i \sbasedon0\snext0 heading 2;}{\s3\sb200\sa100\li0\ri0\plain\fs24\b \sbasedon0\snext0 heading 3;}{\s4\sb200\sa100\li0\ri0\plain\fs22\b \sbasedon0\snext0 heading 4;}{\s5\sb200\sa100\li0\ri0\plain\fs22\i \sbasedon0\snext0 heading 5;}{\s6\sb200\sa100\li0\ri0\plain\fs20\i \sbasedon0\snext0 heading 6;}{\s7\plain\fs20\b \sbasedon0 strong;}{\s8\plain\fs20\b \sbasedon0 Emphazised;}{\s9\sb200\sa200\qc\plain\fs32\b \sbasedon0 title;}{\s10\sb100\sa100\li0\ri0\qc\plain\fs28\b \sbasedon0 subtitle;}{\s11\sb200\sa200\li0\ri0\plain\fs20\i \sbasedon0\snext0 citation;}{\s12\sb200\sa200\li750\ri750\box\brdrs\brdrw1\brsp250\plain\fs20 \sbasedon0\snext0 boxed;}}{\info{\title }
{\subject }
{\author }
{\company }
{\doccomm }
{\*\userprops &#123;&#123;\propname Identifier}\proptype30\staticval }
&#123;&#123;\propname Copyright}\proptype30\staticval }
}}
\deflang1024\paperw11905\paperh16838\psz9\margl1134\margr1134\margt1134\margb1134\deftab283\notabind\fet2\ftnnar\aftnnar
\sectd\sbkodd\pgnstarts1\pgnlcrm{\footer\qc\plain\chpgn\par}
\s9\sb200\sa200\qc\plain\fs32\b Test Dtbook To RTF\par
\pard \s10\sb100\sa100\li0\ri0\qc\plain\fs28\b Book Author\par
\pard \sect\sectd\sbkodd\pgnstarts1\pgnrestart\pgndec{\footer\qc\plain\chpgn\par}
\s0\plain\fs20 \sb100\sa100\li0\ri0 Content 1\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 Content 2\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 Content 3\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 Titre original : "Test Dtbook To RTF"\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 Copyright : Source Rights, 1998\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 ISBN 0-00000-000-0\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 Transcription en braille int\u233?gral : Biblioth\u232?que Braille Int\u233?gral\par
\pard \s0\plain\fs20 \sb100\sa100\li0\ri0 juin 2004\par
\pard {\*\bkmkstart d426e79}\s1\sb200\sa100\li0\ri0\plain\fs32\b \keep\keepn Content Before
          {\super Sup}
          Content After\plain\par
\pard {\*\bkmkend d426e79}\s0\plain\fs20 \sb100\sa100\li0\ri0 Content\par
\pard }
</code></pre>

This is what the RTF will look like in a word processor:

<iframe src="example-rtf.html" id="frame" style="width:90%" frameborder="0" onload="setIframeHeight(this.id)"></iframe>
<script type="application/javascript">
function getDocHeight(doc) {
    doc = doc || document;
    var body = doc.body, html = doc.documentElement;
    var height = Math.max( body.scrollHeight, body.offsetHeight, 
        html.clientHeight, html.scrollHeight, html.offsetHeight );
    return height;
}
function setIframeHeight(id) {
    var ifrm = document.getElementById(id);
    var doc = ifrm.contentDocument? ifrm.contentDocument: 
        ifrm.contentWindow.document;
    ifrm.style.visibility = 'hidden';
    ifrm.style.height = "10px";
    ifrm.style.height = getDocHeight( doc ) + 4 + "px";
    ifrm.style.visibility = 'visible';
}
</script>

## See also

* [RTF specification](https://www.microsoft.com/en-us/download/details.aspx?id=10725)

