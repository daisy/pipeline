<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-from-dir-list" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="px">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>
    
    <p:import href="../../main/resources/xml/xproc/fileset-from-dir-list.xpl"/>
    <p:import href="compare.xpl"/>
    
    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="test"/>
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
    
    <p:group name="test">
        <p:output port="result"/>
        <px:fileset-from-dir-list>
            <p:input port="source">
                <p:inline>
                    <c:directory xmlns:c="http://www.w3.org/ns/xproc-step" name="tmp" xml:base="file:/tmp/">
                        <c:other name="socket"/>
                        <c:file name=".hidden"/>
                        <c:file name="file.txt"/>
                        <c:directory name="sub1" xml:base="file:/tmp/sub1/">
                            <c:file name="other.txt"/>
                            <c:directory name="sub2" xml:base="file:/tmp/sub1/sub2/">
                                <c:file name="other.txt"/>
                            </c:directory>
                        </c:directory>
                    </c:directory>
                </p:inline>
            </p:input>
        </px:fileset-from-dir-list>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/tmp/">
                        <d:file href=".hidden"/>
                        <d:file href="file.txt"/>
                        <d:file href="sub1/other.txt"/>
                        <d:file href="sub1/sub2/other.txt"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>

</p:declare-step>
