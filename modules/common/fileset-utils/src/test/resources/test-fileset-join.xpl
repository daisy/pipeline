<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-join" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>
    
    <p:serialization port="result" indent="true"/>

    <p:declare-step type="pxi:set-implicit-base-uri-of-documents-without-an-xml-base-attribute">
        <p:input port="source" sequence="true"/>
        <p:output port="result" sequence="true"/>
        <p:option name="base"/>
        <p:for-each>
            <p:choose>
                <p:when test="/*[@xml:base]">
                    <!--hack to workaround a re-ordering bug in Calabash-->
                    <p:add-attribute match="/*" attribute-name="foo" attribute-value="bar"/>
                    <p:delete match="/*/@foo"/>
                </p:when>
                <p:otherwise>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <p:with-option name="attribute-value" select="$base"/>
                    </p:add-attribute>
                    <p:delete match="/*/@xml:base"/>
                </p:otherwise>
            </p:choose>
        </p:for-each>
    </p:declare-step>

    <p:import href="../../main/resources/xml/xproc/fileset-join.xpl"/>
    <p:import href="compare.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="same-base"/>
            <p:pipe port="result" step="same-base-normalized"/>
            <p:pipe port="result" step="different-bases"/>
            <p:pipe port="result" step="longest-common-base"/>
            <p:pipe port="result" step="preserve-refs"/>
            <p:pipe port="result" step="dont-relativize-absolute-hrefs-for-filesets-without-base"/>
            <p:pipe port="result" step="preserve-base-uris-of-each-file-and-relativize-against-it"/>
            <p:pipe port="result" step="relativize-hrefs-against-fileset-base"/>
            <p:pipe port="result" step="normalize-all-elements-to-have-the-same-base-uri"/>
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

    <p:group name="same-base">
        <p:output port="result"/>
        <px:fileset-join>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                        <d:file href="http://www.example.org/test"/>
                    </d:fileset>
                </p:inline>
                <p:inline>
                    <d:fileset xml:base="file:/Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc3.html"/>
                        <d:file href="http://www.example.org/test"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-join>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                        <d:file href="http://www.example.org/test"/>
                        <d:file href="doc3.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="same-base"/>
    </p:group>
    
    <p:group name="same-base-normalized">
        <p:output port="result"/>
        <px:fileset-join>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/Users/me/useles/../dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                        <d:file href="http://www.example.org/test"/>
                    </d:fileset>
                </p:inline>
                <p:inline>
                    <d:fileset xml:base="file:/Users///me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc3.html"/>
                        <d:file href="http://www.example.org/test"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-join>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                        <d:file href="http://www.example.org/test"/>
                        <d:file href="doc3.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="same-base"/>
    </p:group>

    <p:group name="different-bases">
        <!-- when possible, the base URI of the resulting fileset will be made so that all files are subfiles of that folder -->
        <p:output port="result"/>
        <px:fileset-join>
            <p:input port="source">
                <p:inline exclude-inline-prefixes="#all">
                    <d:fileset xml:base="file:/Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                        <d:file href="../doc4.html"/>
                    </d:fileset>
                </p:inline>
                <p:inline exclude-inline-prefixes="#all">
                    <d:fileset xml:base="file:/Users/me/other/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc3.html"/>
                        <d:file href="../doc4.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-join>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/Users/me/">
                        <d:file href="dir/doc1.html"/>
                        <d:file href="dir/doc2.html"/>
                        <d:file href="doc4.html"/>
                        <d:file href="other/doc1.html"/>
                        <d:file href="other/doc3.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="different-bases"/>
    </p:group>

    <p:group name="longest-common-base">
        <!-- fileset can not change base unless there are multiple filesets with different bases -->
        <p:output port="result"/>
        <px:fileset-join>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/home/user/">
                        <d:file href="common/uncommon-1/doc1.html"/>
                        <d:file href="common/uncommon-2/doc2.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-join>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/home/user/">
                        <d:file href="common/uncommon-1/doc1.html"/>
                        <d:file href="common/uncommon-2/doc2.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="longest-common-base"/>
    </p:group>

    <p:group name="preserve-refs">
        <!-- preserve refs: https://code.google.com/p/daisy-pipeline/issues/detail?id=277 -->
        <p:output port="result"/>

        <px:fileset-join>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/"/>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/">
    <d:file href="href1">
        <d:ref href="ref1"/>
    </d:file>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/">
    <d:file href="href2">
        <d:ref href="ref2"/>
    </d:file>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/">
    <d:file href="href1">
        <d:ref href="ref3"/>
    </d:file>
</d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-join>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/">
    <d:file href="href1">
        <d:ref href="ref1"/>
        <d:ref href="ref3"/>
    </d:file>
    <d:file href="href2">
        <d:ref href="ref2"/>
    </d:file>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="preserve-refs"/>
    </p:group>

    <p:group name="dont-relativize-absolute-hrefs-for-filesets-without-base">
        <!-- fileset without a base URI: https://code.google.com/p/daisy-pipeline/issues/detail?id=278 -->
        <p:output port="result"/>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data"/>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="file:/href1">
        <d:ref href="ref1"/>
    </d:file>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="file:/href2">
        <d:ref href="ref2"/>
    </d:file>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="file:/href1">
        <d:ref href="ref3"/>
    </d:file>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>
        <pxi:set-implicit-base-uri-of-documents-without-an-xml-base-attribute base="file:/home/user/"/>
        <px:fileset-join/>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="file:/href1">
        <d:ref href="ref1"/>
        <d:ref href="ref3"/>
    </d:file>
    <d:file href="file:/href2">
        <d:ref href="ref2"/>
    </d:file>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="dont-relativize-absolute-hrefs-for-filesets-without-base"/>
    </p:group>
    
    <p:group name="preserve-base-uris-of-each-file-and-relativize-against-it">
        <!-- relativize hrefs against the d:file elements base uri; not the d:fileset. also merge files that resolves to the same base uri regardless of their relative hrefs. -->
        <p:output port="result"/>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="file:/href1">
        <d:ref href="ref1"/>
    </d:file>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/tmp/">
    <d:file href="file:/href2">
        <d:ref href="ref2"/>
    </d:file>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="file:/href1">
        <d:ref href="ref3"/>
    </d:file>
    <d:file href="file:/href2">
        <d:ref href="ref4"/>
    </d:file>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>
        <pxi:set-implicit-base-uri-of-documents-without-an-xml-base-attribute base="file:/home/user/"/>
        <px:fileset-join/>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/tmp/">
    <d:file href="../href1">
        <d:ref href="ref1"/>
        <d:ref href="ref3"/>
    </d:file>
    <d:file href="../href2">
        <d:ref href="ref2"/>
        <d:ref href="ref4"/>
    </d:file>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="preserve-base-uris-of-each-file-and-relativize-against-it"/>
    </p:group>

    <p:group name="relativize-hrefs-against-fileset-base">
        <p:output port="result"/>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/tmp/">
    <d:file href="../tmp/href1"/>
</d:fileset>
                    </p:inline>
            </p:input>
        </p:identity>
        <p:delete match="/*/@xml:base"/>

        <px:fileset-join/>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="href1"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="relativize-hrefs-against-fileset-base"/>
    </p:group>

    <p:group name="normalize-all-elements-to-have-the-same-base-uri">
        <p:output port="result"/>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/home/user/Desktop/OEBPS/">
    <d:file href="Content/content.xhtml"/>
</d:fileset>
                </p:inline>
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/home/user/Desktop/OEBPS/Content/">
    <d:file href="default.css"/>
    <d:file href="speechgen0001.mp3"/>
    <d:file href="speechgen0002.mp3"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>

        <px:fileset-join/>
        <p:add-xml-base all="true" relative="false"/>
        
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/home/user/Desktop/OEBPS/">
    <d:file href="Content/content.xhtml" xml:base="file:/home/user/Desktop/OEBPS/"/>
    <d:file href="Content/default.css" xml:base="file:/home/user/Desktop/OEBPS/"/>
    <d:file href="Content/speechgen0001.mp3" xml:base="file:/home/user/Desktop/OEBPS/"/>
    <d:file href="Content/speechgen0002.mp3" xml:base="file:/home/user/Desktop/OEBPS/"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="normalize-all-elements-to-have-the-same-base-uri"/>
    </p:group>

</p:declare-step>
