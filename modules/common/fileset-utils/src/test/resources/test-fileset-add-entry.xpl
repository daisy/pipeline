<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-add-entry" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="../../main/resources/xml/xproc/fileset-add-entry.xpl"/>
    <p:import href="compare.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="add-entry"/>
            <p:pipe port="result" step="add-entry-with-media-type"/>
            <p:pipe port="result" step="add-entry-first"/>
            <p:pipe port="result" step="add-entry-absolute"/>
            <p:pipe port="result" step="add-entry-absolute-same-base"/>
            <p:pipe port="result" step="add-entry-to-fileset-without-base"/>
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

    <p:group name="add-entry">
        <p:output port="result"/>
        <px:fileset-add-entry href="doc.html">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/"/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="doc.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-entry"/>
    </p:group>

    <p:group name="add-entry-with-media-type">
        <p:output port="result"/>
        <px:fileset-add-entry href="doc.html" media-type="text/html">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/"/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="doc.html" media-type="text/html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-entry-with-media-type"/>
    </p:group>

    <p:group name="add-entry-first">
        <p:output port="result"/>
        <px:fileset-add-entry href="doc.html" first="true">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="other"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="doc.html"/>
                        <d:file href="other"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-entry-first"/>
    </p:group>

    <p:group name="add-entry-absolute">
        <p:output port="result"/>
        <px:fileset-add-entry href="file:/doc.html">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/"/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="../../../doc.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-entry-absolute"/>
    </p:group>

    <p:group name="add-entry-absolute-same-base">
        <p:output port="result"/>
        <px:fileset-add-entry href="file:/users/me/dir/doc.html">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/"/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="doc.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-entry-absolute-same-base"/>
    </p:group>

    <p:group name="add-entry-to-fileset-without-base">
        <!-- fileset without a base URI: https://code.google.com/p/daisy-pipeline/issues/detail?id=278 -->
        <p:output port="result"/>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset/>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute match="/*" attribute-name="xml:base" attribute-value="file:/tmp/"/>
        <p:delete match="/*/@xml:base"/>

        <px:fileset-add-entry href="test.xpl"/>
        <px:fileset-add-entry href="/Users/marisa/Desktop/test.xpl"/>
        <px:fileset-add-entry href="file:/Users/marisa/Desktop/test.xpl"/>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data">
    <d:file href="test.xpl"/>
    <d:file href="../Users/marisa/Desktop/test.xpl"/>
    <d:file href="file:/Users/marisa/Desktop/test.xpl"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-entry-to-fileset-without-base"/>
    </p:group>

</p:declare-step>
