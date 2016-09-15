<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-join"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" xmlns:c="http://www.w3.org/ns/xproc-step">
    
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

    <p:import href="../../main/resources/xml/xproc/fileset-rebase.xpl"/>
    <p:import href="compare.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="rebase-fileset"/>
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

    <p:group name="rebase-fileset">
        <p:output port="result"/>

        <p:identity>
            <p:input port="source">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/home/user/">
    <d:file href="Desktop/output-dir/OEBPS/Content/content.xhtml"/>
    <d:file href="Desktop/output-dir/OEBPS/META-INF/container.xml" original-href="file:/home/user/Desktop/output-dir/OEBPS/META-INF/container.xml"/>
    <d:file href="user-file" original-href="file:/home/user/Desktop/output-dir/OEBPS/user-file"/>
</d:fileset>
                </p:inline>
            </p:input>
        </p:identity>

        <px:fileset-rebase>
            <p:with-option name="new-base" select="'file:/home/user/Desktop/output-dir/OEBPS/'"/>
        </px:fileset-rebase>
        <p:add-xml-base all="true" relative="false"/>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/home/user/Desktop/output-dir/OEBPS/">
    <d:file href="Content/content.xhtml"/>
    <d:file href="META-INF/container.xml" original-href="file:/home/user/Desktop/output-dir/OEBPS/META-INF/container.xml"/>
    <d:file href="../../../user-file" original-href="file:/home/user/Desktop/output-dir/OEBPS/user-file"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="rebase-fileset"/>
    </p:group>

</p:declare-step>
