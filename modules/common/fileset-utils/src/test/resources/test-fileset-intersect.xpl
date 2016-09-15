<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-intersect" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="px" xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>
    
    <p:import href="../../main/resources/xml/xproc/fileset-intersect.xpl"/>
    <p:import href="compare.xpl"/>
    
    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="same-base"/>
            <p:pipe port="result" step="different-base"/>
            <p:pipe port="result" step="different-base-with-common-resource"/>
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
        <px:fileset-intersect>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                    </d:fileset>
                </p:inline>
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc3.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-intersect>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/">
                        <d:file href="doc1.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>

    <p:group name="different-base">
        <p:output port="result"/>
        <px:fileset-intersect>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc2.html"/>
                    </d:fileset>
                </p:inline>
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/other/">
                        <d:file href="doc1.html"/>
                        <d:file href="doc3.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-intersect>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/"> </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>

    <p:group name="different-base-with-common-resource">
        <p:output port="result"/>
        <px:fileset-intersect>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/">
                        <d:file href="../doc1.html"/>
                        <d:file href="doc2.html"/>
                    </d:fileset>
                </p:inline>
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/other/">
                        <d:file href="../doc1.html"/>
                        <d:file href="doc3.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-intersect>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:///Users/me/dir/">
                        <d:file href="../doc1.html"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>

</p:declare-step>
