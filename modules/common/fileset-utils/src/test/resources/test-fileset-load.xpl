<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-load" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="../../main/resources/xml/xproc/fileset-load.xpl"/>
    <p:import href="compare.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="test-empty"/>
            <p:pipe port="result" step="test-href-absolute"/>
            <p:pipe port="result" step="test-href-relative"/>
            <p:pipe port="result" step="test-method-xml"/>
            <p:pipe port="result" step="test-method-html"/>
            <p:pipe port="result" step="test-method-text"/>
            <p:pipe port="result" step="test-method-binary"/>
            <p:pipe port="result" step="test-media-types-filtering"/>
            <p:pipe port="result" step="test-not-media-types-filtering"/>
            <p:pipe port="result" step="test-fail-on-not-found-false"/>
            <p:pipe port="result" step="test-fail-on-not-found-true-href-not-on-disk"/>
            <p:pipe port="result" step="test-fail-on-not-found-true-href-not-part-of-fileset"/>
            <p:pipe port="result" step="test-load-if-not-in-memory-false"/>
            <p:pipe port="result" step="test-load-from-memory"/>
        </p:input>
    </p:wrap-sequence>
    <p:add-attribute match="/*" attribute-name="script-uri">
        <p:with-option name="attribute-value" select="base-uri(/*)">
            <p:inline>
                <doc/>
            </p:inline>
        </p:with-option>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="name">
        <p:with-option name="attribute-value" select="replace(replace(/*/@script-uri,'^.*/([^/]+)$','$1'),'\.xpl$','')"/>
    </p:add-attribute>
    <p:identity name="result"/>

    <p:group name="test-fileset">
        <p:output port="result"/>
        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file media-type="application/octet-stream" href="a/MimeDetector.class"/>
    <d:file media-type="application/octet-stream" href="a/MimeDetector.java"/>
    <d:file media-type="application/x-gzip" href="a/f.tar.gz"/>
    <d:file media-type="application/octet-stream" href="a/resources/mime.cache"/>
    <d:file media-type="application/octet-stream" href="a/resources/eu/medsea/mimeutil/magic.mime"/>
    <d:file media-type="application/octet-stream" href="a/resources/eu/medsea/mimeutil/mime-types.properties"/>
    <d:file media-type="application/octet-stream" href="a/c-gif.img"/>
    <d:file media-type="application/octet-stream" href="a/e%5Bxml%5D"/>
    <d:file media-type="image/svg+xml" href="a/e.svg"/>
    <d:file media-type="text/html" href="a/a.html"/>
    <d:file media-type="image/jpeg" href="b.jpg"/>
    <d:file media-type="application/octet-stream" href="d-png.img"/>
    <d:file media-type="application/octet-stream" href="test.bin"/>
    <d:file media-type="image/png" href="d.png"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/MimeUtilTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/util/EncodingGuesserTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/MimeTypeTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/detector/WindowsRegistryMimeDetectorTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/detector/OpendesktopMimeDetectorTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/detector/TextMimeDetectorTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/detector/MagicMimeMimeDetectorTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/MimeTypeHashSetTest.java"/>
    <d:file media-type="application/octet-stream" href="eu/medsea/mimeutil/MimeUtil2Test.java"/>
    <d:file media-type="text/plain" href="plaintext.txt"/>
    <d:file media-type="application/x-gzip" href="f.tar.gz"/>
    <d:file media-type="application/x-gzip" href="porrasturvat-1.0.3.tar.gz"/>
    <d:file media-type="application/zip" href="a.zip"/>
    <d:file media-type="application/octet-stream" href="b-jpg.img"/>
    <d:file media-type="application/octet-stream" href="c-gif.img"/>
    <d:file media-type="application/octet-stream" href="e-svg.img"/>
    <d:file media-type="application/octet-stream" href="e%5Bxml%5D"/>
    <d:file media-type="application/xml" href="e.xml"/>
    <d:file media-type="application/octet-stream" href="META-INF/MANIFEST.MF"/>
    <d:file media-type="application/octet-stream" href="textfiles/western"/>
    <d:file media-type="application/octet-stream" href="textfiles/unicode"/>
    <d:file href="wrong-extensions/html.bin"/>
    <d:file href="wrong-extensions/opf.txt"/>
    <d:file href="wrong-extensions/png.xml"/>
    <d:file href="wrong-extensions/txt.xml"/>
    <d:file media-type="application/octet-stream" href="magic.mime"/>
    <d:file media-type="application/octet-stream" href="plaintext"/>
    <d:file media-type="application/octet-stream" href="afpfile.afp"/>
    <d:file media-type="image/svg+xml" href="e.svg"/>
    <d:file media-type="text/html" href="a.html"/>
    <d:file media-type="application/xml" href="epub/META-INF/container.xml"/>
    <d:file media-type="audio/mpeg" href="epub/Publication/Content/1_Jamen__Benny.mp3"/>
    <d:file media-type="application/xhtml+xml" href="epub/Publication/Content/mqia0001.xhtml"/>
    <d:file media-type="application/smil+xml" href="epub/Publication/Content/mqia0001.smil"/>
    <d:file media-type="image/jpeg" href="epub/Publication/Content/tjcs0000.jpg"/>
    <d:file media-type="image/jpeg" href="epub/Publication/Content/41077stor.jpg"/>
    <d:file media-type="application/xhtml+xml" href="epub/Publication/navigation.xhtml"/>
    <d:file media-type="application/x-dtbncx+xml" href="epub/Publication/ncx.xml"/>
    <d:file media-type="application/oebps-package+xml" href="epub/Publication/package.opf"/>
    <d:file media-type="application/octet-stream" href="epub/mimetype"/>
    <d:file media-type="image/gif" href="c.gif"/>
    <d:file media-type="application/octet-stream" href="log4j.properties"/>
    <d:file media-type="application/octet-stream" href="mime-types.properties"/>
    <d:file media-type="application/epub+zip" href="epub.epub"/>
    <d:file media-type="application/oebps-package+xml" href="xml/package.xml"/>
    <d:file media-type="application/x-dtbncx+xml" href="xml/ncx.xml"/>
    <d:file media-type="application/xml" href="xml/container.xml"/>
    <d:file media-type="application/smil+xml" href="xml/mqia0001.xml"/>
    <d:file media-type="application/xproc+xml" href="xml/XProc.xml"/>
    <d:file media-type="application/xhtml+xml" href="xml/navigation.xml"/>
    <d:file media-type="application/oebps-package+xml" href="xml/package.xml~"/>
    <d:file media-type="application/xslt+xml" href="xml/XSLT.xml"/>
    <d:file media-type="application/xml" href="xml/noFileExtension"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="resolve-uri('samples/fileset2/',base-uri())">
                <p:inline>
                    <doc/>
                </p:inline>
            </p:with-option>
        </p:add-attribute>
        <p:add-xml-base/>
    </p:group>
    <p:sink/>

    <p:group name="test-empty">
        <p:output port="result"/>

        <px:fileset-load>
            <p:input port="fileset">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:empty/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-empty"/>
    </p:group>

    <p:group name="test-href-absolute">
        <p:output port="result"/>

        <px:fileset-load>
            <p:with-option name="href" select="resolve-uri('a/a.html',/*/@xml:base)">
                <p:pipe step="test-fileset" port="result"/>
            </p:with-option>
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-href-absolute"/>
    </p:group>

    <p:group name="test-href-relative">
        <p:output port="result"/>

        <px:fileset-load href="a/a.html">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-href-relative"/>
    </p:group>

    <p:group name="test-method-xml">
        <p:output port="result"/>

        <px:fileset-load href="wrong-extensions/opf.txt" method="xml">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<package xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/" unique-identifier="pub-id" version="3.0">
    <opf:metadata xmlns:opf="http://www.idpf.org/2007/opf">
        <dc:identifier id="pub-id">613757</dc:identifier>
        <dc:title id="title">Jamen, Benny</dc:title>
        <dc:language>no</dc:language>
        <dc:date id="date">2008-11-10</dc:date>
        <dc:format>EPUB3</dc:format>
        <dc:publisher>NLB</dc:publisher>
        <dc:creator>Lindgren, Barbro</dc:creator>
        <dc:contributor id="d179e56">Arnhild Littlere</dc:contributor>
        <meta property="dcterms:modified">2012-03-16T11:10:39Z</meta>
        <opf:meta name="dcterms:modified" content="2012-03-16T11:10:39Z"/>
        <opf:meta refines="#d179e56" property="role" scheme="marc:relators">nrt</opf:meta>
        <opf:meta property="media:duration" refines="#item_7">15.416s</opf:meta>
        <opf:meta property="media:duration" refines="#item_8">10.188s</opf:meta>
        <opf:meta property="media:duration" refines="#item_9">25.614s</opf:meta>
        <opf:meta property="media:duration" refines="#item_10">03:38.948</opf:meta>
        <opf:meta property="media:duration" refines="#item_11">2.965s</opf:meta>
        <opf:meta property="media:duration">04:33.131</opf:meta>
        <opf:meta name="media:duration" content="04:33.131"/>
    </opf:metadata>
    <manifest>
        <item media-type="application/xhtml+xml" id="item_1" href="navigation.xhtml" properties="nav"/>
        <item media-type="application/xhtml+xml" id="item_2" href="Content/mqia0001.xhtml"/>
        <item media-type="application/xhtml+xml" id="item_3" href="Content/mqia0003.xhtml"/>
        <item media-type="application/xhtml+xml" id="item_4" href="Content/mqia0002.xhtml"/>
        <item media-type="application/xhtml+xml" id="item_5" href="Content/mqia0006.xhtml"/>
        <item media-type="application/xhtml+xml" id="item_6" href="Content/mqia0007.xhtml"/>
        <item media-type="application/smil+xml" id="item_7" href="Content/mqia0001.smil"/>
        <item media-type="application/smil+xml" id="item_8" href="Content/mqia0003.smil"/>
        <item media-type="application/smil+xml" id="item_9" href="Content/mqia0002.smil"/>
        <item media-type="application/smil+xml" id="item_10" href="Content/mqia0006.smil"/>
        <item media-type="application/smil+xml" id="item_11" href="Content/mqia0007.smil"/>
        <item media-type="image/jpeg" id="item_12" href="Content/41077stor.jpg"/>
        <item media-type="image/jpeg" id="item_13" href="Content/tjcs0000.jpg"/>
        <item media-type="image/gif" id="item_14" href="Content/guct0000.gif"/>
        <item media-type="image/jpeg" id="item_15" href="Content/image004.jpg"/>
        <item media-type="image/jpeg" id="item_16" href="Content/image005.jpg"/>
        <item media-type="image/jpeg" id="item_17" href="Content/image006.jpg"/>
        <item media-type="image/jpeg" id="item_18" href="Content/image007.jpg"/>
        <item media-type="image/jpeg" id="item_19" href="Content/image008.jpg"/>
        <item media-type="image/jpeg" id="item_20" href="Content/veli0000.jpg"/>
        <item media-type="image/gif" id="item_21" href="Content/btcg0000.gif"/>
        <item media-type="image/gif" id="item_22" href="Content/lbpf0000.gif"/>
        <item media-type="image/jpeg" id="item_23" href="Content/image011.jpg"/>
        <item media-type="image/jpeg" id="item_24" href="Content/image013.jpg"/>
        <item media-type="image/jpeg" id="item_25" href="Content/image014.jpg"/>
        <item media-type="image/gif" id="item_26" href="Content/vuvn0000.gif"/>
        <item media-type="image/jpeg" id="item_27" href="Content/image016.jpg"/>
        <item media-type="image/gif" id="item_28" href="Content/image017.gif"/>
        <item media-type="image/jpeg" id="item_29" href="Content/image018.jpg"/>
        <item media-type="image/jpeg" id="item_30" href="Content/image019.jpg"/>
        <item media-type="image/gif" id="item_31" href="Content/kycs0000.gif"/>
        <item media-type="image/gif" id="item_32" href="Content/bpvv0000.gif"/>
        <item media-type="image/gif" id="item_33" href="Content/image022.gif"/>
        <item media-type="image/gif" id="item_34" href="Content/eiqs0000.gif"/>
        <item media-type="image/gif" id="item_35" href="Content/bbwe0000.gif"/>
        <item media-type="image/jpeg" id="item_36" href="Content/image025.jpg"/>
        <item media-type="image/jpeg" id="item_37" href="Content/gsvi0000.jpg"/>
        <item media-type="audio/mpeg" id="item_38" href="Content/1_Jamen__Benny.mp3"/>
        <item media-type="audio/mpeg" id="item_39" href="Content/2_Publiseringsre.mp3"/>
        <item media-type="audio/mpeg" id="item_40" href="Content/3_Bokinformasjon.mp3"/>
        <item media-type="audio/mpeg" id="item_41" href="Content/4_Jamen__Benny.mp3"/>
        <item media-type="audio/mpeg" id="item_42" href="Content/5_Bok__slutt.mp3"/>
        <item media-type="application/x-dtbncx+xml" id="item_43" href="ncx.xml"/>
    </manifest>
    <spine toc="item_43">
        <itemref idref="item_2" id="itemref_1"/>
        <itemref idref="item_3" id="itemref_2"/>
        <itemref idref="item_4" id="itemref_3"/>
        <itemref idref="item_5" id="itemref_4"/>
        <itemref idref="item_6" id="itemref_5"/>
    </spine>
</package>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-method-xml"/>
    </p:group>

    <p:group name="test-method-html">
        <p:output port="result"/>

        <px:fileset-load href="wrong-extensions/html.bin" method="html">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-method-html"/>
    </p:group>

    <p:group name="test-method-text">
        <p:output port="result"/>

        <px:fileset-load href="wrong-extensions/html.bin" method="text">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:body content-type="text/plain; charset=utf-8">&lt;html&gt;&#xD;
&lt;head&gt;&#xD;
&lt;meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"&gt;&#xD;
&lt;title&gt;This is a test basic html file&lt;/title&gt;&#xD;
&lt;/head&gt;&#xD;
&lt;body&gt;&#xD;
&#xD;
&lt;/body&gt;&#xD;
&lt;/html&gt;</c:body>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-method-text"/>
    </p:group>

    <p:group name="test-method-binary">
        <p:output port="result"/>

        <px:fileset-load href="wrong-extensions/txt.xml" method="binary">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:body content-type="binary/octet-stream" encoding="base64">VGhpcyBpcyBqdXN0IGEgcGxhaW4gdGV4dCBmaWxlDQo=</c:body>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-method-binary"/>
    </p:group>

    <p:group name="test-media-types-filtering">
        <p:output port="result"/>

        <px:fileset-load media-types="application/xhtml+xml text/html">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title> Jamen, Benny </title>
    </head>
    <body>
        <span id="dol_1_1_uafu_0001">
            <span>
                <em>Jamen, Benny</em>
            </span>
        </span>
        <span id="pkrg_0000"> </span> 
        <span id="pkrg_0001">
            <img alt="Image" src="41077stor.jpg" width="342" height="432"/>
        </span>
        <p>
            <span>
                <span id="dol_1_1_pkrg_0002">Barbro Lindgren og Olof Landstrom.</span>
            </span>
        </p>
        <p>
            <span id="dol_1_1_pkrg_0003">DET NORSKE SAMMLAGET 2002</span>
        </p>
    </body>
</html>
                </p:inline>
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
    <head>
        <title>Jamen, Benny</title>
    </head>
    <body>
        <nav epub:type="toc" id="toc">
            <ol>
                <li>
                    <a href="Content/mqia0001.xhtml#dol_1_1_uafu_0001" id="mqia0001">Jamen, Benny</a>
                </li>
                <li>
                    <a href="Content/mqia0003.xhtml#dol_1_2_xtxy_0001" id="mqia_0002">Publiseringsrett</a>
                </li>
                <li>
                    <a href="Content/mqia0002.xhtml#dol_1_3_bufm_0001" id="mqia_0001">Bokinformasjon</a>
                </li>
                <li>
                    <a href="Content/mqia0006.xhtml#dol_1_4_calt_0001" id="mqia_0005">Jamen, Benny</a>
                </li>
                <li>
                    <a href="Content/mqia0007.xhtml#dol_1_5_pryd_0003" id="mqia_0006">Bok, slutt</a>
                </li>
            </ol>
        </nav>
    </body>
</html>
                </p:inline>
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
    <head>
        <title>Jamen, Benny</title>
    </head>
    <body>
        <nav epub:type="toc" id="toc">
            <ol>
                <li>
                    <a href="Content/mqia0001.xhtml#dol_1_1_uafu_0001" id="mqia0001">Jamen, Benny</a>
                </li>
                <li>
                    <a href="Content/mqia0003.xhtml#dol_1_2_xtxy_0001" id="mqia_0002">Publiseringsrett</a>
                </li>
                <li>
                    <a href="Content/mqia0002.xhtml#dol_1_3_bufm_0001" id="mqia_0001">Bokinformasjon</a>
                </li>
                <li>
                    <a href="Content/mqia0006.xhtml#dol_1_4_calt_0001" id="mqia_0005">Jamen, Benny</a>
                </li>
                <li>
                    <a href="Content/mqia0007.xhtml#dol_1_5_pryd_0003" id="mqia_0006">Bok, slutt</a>
                </li>
            </ol>
        </nav>
    </body>
</html>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="test-media-types-filtering"/>
    </p:group>

    <p:group name="test-not-media-types-filtering">
        <p:output port="result"/>

        <px:fileset-load media-types="application/xhtml+xml text/html" not-media-types="application/xhtml+xml">
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
                <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>This is a test basic html file</title>
    </head>
    <body> </body>
</html>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="test-not-media-types-filtering"/>
    </p:group>

    <p:group name="test-fail-on-not-found-false">
        <p:output port="result"/>

        <p:try>
            <p:group>
                <px:fileset-load href="file/that/does/not/exist" fail-on-not-found="false">
                    <p:input port="fileset">
                        <p:pipe step="test-fileset" port="result"/>
                    </p:input>
                    <p:input port="in-memory">
                        <p:empty/>
                    </p:input>
                </px:fileset-load>
                <p:identity>
                    <p:input port="source">
                        <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:result result="true"/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:group>
            <p:catch>
                <p:identity>
                    <p:input port="source">
                        <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:result result="false"/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:catch>
        </p:try>

        <p:add-attribute match="/*" attribute-name="name" attribute-value="fail-on-not-found-false"/>
    </p:group>

    <p:group name="test-fail-on-not-found-true-href-not-on-disk">
        <p:output port="result"/>

        <p:identity name="fail-on-not-found-true.source">
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file media-type="application/octet-stream" href="file/in/fileset/that/does/not/exist/on/disk"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>

        <p:try>
            <p:group>
                <px:fileset-load href="file/in/fileset/that/does/not/exist/on/disk" fail-on-not-found="true">
                    <p:input port="fileset">
                        <p:pipe port="result" step="fail-on-not-found-true.source"/>
                    </p:input>
                    <p:input port="in-memory">
                        <p:empty/>
                    </p:input>
                </px:fileset-load>
                <p:add-attribute match="/*" attribute-name="result">
                    <p:with-option name="attribute-value" select="'false'"/>
                    <p:input port="source">
                        <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:result result="false"/>
                        </p:inline>
                    </p:input>
                </p:add-attribute>
            </p:group>
            <p:catch>
                <p:identity>
                    <p:input port="source">
                        <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:result result="true"/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:catch>
        </p:try>

        <p:add-attribute match="/*" attribute-name="name" attribute-value="fail-on-not-found-true-href-not-on-disk"/>
    </p:group>

    <p:group name="test-fail-on-not-found-true-href-not-part-of-fileset">
        <p:output port="result"/>

        <p:try>
            <p:group>
                <px:fileset-load href="file/that/is/not/part/of/the/fileset" fail-on-not-found="true">
                    <p:input port="fileset">
                        <p:pipe step="test-fileset" port="result"/>
                    </p:input>
                    <p:input port="in-memory">
                        <p:empty/>
                    </p:input>
                </px:fileset-load>
                <p:add-attribute match="/*" attribute-name="result">
                    <p:with-option name="attribute-value" select="'false'"/>
                    <p:input port="source">
                        <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:result result="false"/>
                        </p:inline>
                    </p:input>
                </p:add-attribute>
            </p:group>
            <p:catch>
                <p:identity>
                    <p:input port="source">
                        <p:inline xml:space="preserve" exclude-inline-prefixes="#all">
<c:result result="true"/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:catch>
        </p:try>

        <p:add-attribute match="/*" attribute-name="name" attribute-value="fail-on-not-found-true-href-not-part-of-fileset"/>
    </p:group>

    <p:group name="test-load-if-not-in-memory-false">
        <p:output port="result"/>

        <px:fileset-load load-if-not-in-memory="false">
            <p:with-option name="href" select="resolve-uri('a/a.html',/*/@xml:base)">
                <p:pipe step="test-fileset" port="result"/>
            </p:with-option>
            <p:input port="fileset">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:empty/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-if-not-in-memory-false"/>
    </p:group>

    <p:group name="test-load-from-memory">
        <p:output port="result"/>

        <p:identity name="load-from-memory.source.fileset">
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file media-type="application/xml" href="file:/that/exists/in/memory/but/not/on/disk"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<in-memory-doc/>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute match="/*" attribute-name="xml:base" attribute-value="file:/that/exists/in/memory/but/not/on/disk"/>
        <p:delete match="/*/@xml:base"/>
        <p:identity name="load-from-memory.source.in-memory"/>

        <px:fileset-load href="file:/that/exists/in/memory/but/not/on/disk">
            <p:input port="fileset">
                <p:pipe port="result" step="load-from-memory.source.fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe port="result" step="load-from-memory.source.in-memory"/>
            </p:input>
        </px:fileset-load>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:pipe port="result" step="load-from-memory.source.in-memory"/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-from-memory"/>
    </p:group>

</p:declare-step>
