<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-mediatype-detect" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="../../main/resources/xml/xproc/mediatype.xpl"/>
    <p:import href="compare.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="test-empty"/>
            <p:pipe port="result" step="test-load-if-not-in-memory-true-xml"/>
            <p:pipe port="result" step="test-use-in-memory-representation-dont-load-from-disk"/>
            <p:pipe port="result" step="test-in-memory-xml-overrides-file-extension"/>
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
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="a/MimeDetector.class"/>
    <d:file href="a/MimeDetector.java"/>
    <d:file href="a/f.tar.gz"/>
    <d:file href="a/resources/mime.cache"/>
    <d:file href="a/resources/eu/medsea/mimeutil/magic.mime"/>
    <d:file href="a/resources/eu/medsea/mimeutil/mime-types.properties"/>
    <d:file href="a/c-gif.img"/>
    <d:file href="a/e%5Bxml%5D"/>
    <d:file href="a/e.svg"/>
    <d:file href="a/a.html"/>
    <d:file href="b.jpg"/>
    <d:file href="d-png.img"/>
    <d:file href="test.bin"/>
    <d:file href="d.png"/>
    <d:file href="eu/medsea/mimeutil/MimeUtilTest.java"/>
    <d:file href="eu/medsea/mimeutil/util/EncodingGuesserTest.java"/>
    <d:file href="eu/medsea/mimeutil/MimeTypeTest.java"/>
    <d:file href="eu/medsea/mimeutil/detector/WindowsRegistryMimeDetectorTest.java"/>
    <d:file href="eu/medsea/mimeutil/detector/OpendesktopMimeDetectorTest.java"/>
    <d:file href="eu/medsea/mimeutil/detector/TextMimeDetectorTest.java"/>
    <d:file href="eu/medsea/mimeutil/detector/MagicMimeMimeDetectorTest.java"/>
    <d:file href="eu/medsea/mimeutil/MimeTypeHashSetTest.java"/>
    <d:file href="eu/medsea/mimeutil/MimeUtil2Test.java"/>
    <d:file href="plaintext.txt"/>
    <d:file href="f.tar.gz"/>
    <d:file href="porrasturvat-1.0.3.tar.gz"/>
    <d:file href="a.zip"/>
    <d:file href="b-jpg.img"/>
    <d:file href="c-gif.img"/>
    <d:file href="e-svg.img"/>
    <d:file href="e%5Bxml%5D"/>
    <d:file href="e.xml"/>
    <d:file href="META-INF/MANIFEST.MF"/>
    <d:file href="textfiles/western"/>
    <d:file href="textfiles/unicode"/>
    <d:file href="wrong-extensions/html.bin"/>
    <d:file href="wrong-extensions/opf.txt"/>
    <d:file href="wrong-extensions/png.xml"/>
    <d:file href="wrong-extensions/txt.xml"/>
    <d:file href="magic.mime"/>
    <d:file href="plaintext"/>
    <d:file href="afpfile.afp"/>
    <d:file href="e.svg"/>
    <d:file href="a.html"/>
    <d:file href="epub/META-INF/container.xml"/>
    <d:file href="epub/Publication/Content/1_Jamen__Benny.mp3"/>
    <d:file href="epub/Publication/Content/mqia0001.xhtml"/>
    <d:file href="epub/Publication/Content/mqia0001.smil"/>
    <d:file href="epub/Publication/Content/tjcs0000.jpg"/>
    <d:file href="epub/Publication/Content/41077stor.jpg"/>
    <d:file href="epub/Publication/navigation.xhtml"/>
    <d:file href="epub/Publication/ncx.xml"/>
    <d:file href="epub/Publication/package.opf"/>
    <d:file href="epub/mimetype"/>
    <d:file href="c.gif"/>
    <d:file href="log4j.properties"/>
    <d:file href="mime-types.properties"/>
    <d:file href="epub.epub"/>
    <d:file href="xml/package.xml"/>
    <d:file href="xml/ncx.xml"/>
    <d:file href="xml/container.xml"/>
    <d:file href="xml/mqia0001.xml"/>
    <d:file href="xml/XProc.xml"/>
    <d:file href="xml/navigation.xml"/>
    <d:file href="xml/package.xml~"/>
    <d:file href="xml/XSLT.xml"/>
    <d:file href="xml/noFileExtension"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="resolve-uri('samples/fileset/',base-uri())">
                <p:inline>
                    <doc/>
                </p:inline>
            </p:with-option>
        </p:add-attribute>
        <p:add-xml-base/>
    </p:group>
    <p:sink/>

    <p:group name="test-in-memory">
        <p:output port="result" sequence="true">
            <p:pipe port="result" step="in-memory.xml"/>
            <p:pipe port="result" step="in-memory.html"/>
        </p:output>
        <p:delete match="//d:file[not(@href=('xml/package.xml~','xml/noFileExtension','wrong-extensions/opf.txt'))]">
            <p:input port="source">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
        </p:delete>
        <px:fileset-load method="xml" name="in-memory.xml">
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>
        
        <p:delete match="//d:file[not(@href=('wrong-extensions/html.bin'))]">
            <p:input port="source">
                <p:pipe step="test-fileset" port="result"/>
            </p:input>
        </p:delete>
        <px:fileset-load method="html" name="in-memory.html">
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>
    </p:group>
    <p:sink/>

    <p:group name="test-empty">
        <p:output port="result"/>

        <px:mediatype-detect>
            <p:input port="source">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:mediatype-detect>

        <px:compare>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="empty"/>
    </p:group>

    <p:group name="test-load-if-not-in-memory-true-xml">
        <p:output port="result"/>
        <p:add-attribute match="/*" attribute-name="xml:base" name="load-if-not-in-memory-true-xml.alternate">
            <p:with-option name="attribute-value" select="/*/@xml:base">
                <p:pipe port="result" step="test-fileset"/>
            </p:with-option>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset>
    <d:file media-type="application/oebps-package+xml" href="xml/package.xml"/>
    <d:file media-type="application/x-dtbncx+xml" href="xml/ncx.xml"/>
    <d:file media-type="application/xml" href="xml/container.xml"/>
    <d:file media-type="application/smil+xml" href="xml/mqia0001.xml"/>
    <d:file media-type="application/xproc+xml" href="xml/XProc.xml"/>
    <d:file media-type="application/xhtml+xml" href="xml/navigation.xml"/>
    <d:file media-type="application/octet-stream" href="xml/package.xml~"/>
    <d:file media-type="application/xslt+xml" href="xml/XSLT.xml"/>
    <d:file media-type="application/octet-stream" href="xml/noFileExtension"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:add-attribute>

        <p:delete match="//d:file[not(starts-with(@href,'xml/'))]">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </p:delete>
        <px:mediatype-detect load-if-not-in-memory="true">
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:mediatype-detect>

        <px:compare>
            <p:input port="alternate">
                <p:pipe port="result" step="load-if-not-in-memory-true-xml.alternate"/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="load-if-not-in-memory-true-xml"/>
    </p:group>

    <p:group name="test-use-in-memory-representation-dont-load-from-disk">
        <p:output port="result"/>
        <p:add-attribute match="/*" attribute-name="xml:base" name="use-in-memory-representation-dont-load-from-disk.alternate">
            <p:with-option name="attribute-value" select="/*/@xml:base">
                <p:pipe port="result" step="test-fileset"/>
            </p:with-option>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset>
    <d:file media-type="application/oebps-package+xml" href="xml/package.xml~"/>
    <d:file media-type="application/xml" href="xml/XSLT.xml"/>
    <d:file media-type="application/xml" href="xml/noFileExtension"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:add-attribute>

        <p:delete match="//d:file[not(@href=('xml/package.xml~','xml/XSLT.xml','xml/noFileExtension'))]">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </p:delete>
        <px:mediatype-detect>
            <p:input port="in-memory">
                <p:pipe port="result" step="test-in-memory"/>
            </p:input>
        </px:mediatype-detect>

        <px:compare>
            <p:input port="alternate">
                <p:pipe port="result" step="use-in-memory-representation-dont-load-from-disk.alternate"/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="use-in-memory-representation-dont-load-from-disk"/>
    </p:group>

    <p:group name="test-in-memory-xml-overrides-file-extension">
        <p:output port="result"/>
        <p:add-attribute match="/*" attribute-name="xml:base" name="use-in-memory-representation-dont-load-from-disk.alternate">
            <p:with-option name="attribute-value" select="/*/@xml:base">
                <p:pipe port="result" step="test-fileset"/>
            </p:with-option>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset>
    <d:file media-type="application/xhtml+xml" href="wrong-extensions/html.bin"/>
    <d:file media-type="application/oebps-package+xml" href="wrong-extensions/opf.txt"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:add-attribute>

        <p:delete match="//d:file[not(@href=('wrong-extensions/html.bin','wrong-extensions/opf.txt'))]">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </p:delete>
        <px:mediatype-detect>
            <p:input port="in-memory">
                <p:pipe port="result" step="test-in-memory"/>
            </p:input>
        </px:mediatype-detect>

        <px:compare>
            <p:input port="alternate">
                <p:pipe port="result" step="use-in-memory-representation-dont-load-from-disk.alternate"/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="in-memory-xml-overrides-file-extension"/>
    </p:group>

</p:declare-step>
