<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-copy" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="../../main/resources/xml/xproc/fileset-copy.xpl"/>
    <p:import href="compare.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

    <p:variable name="out-dir" select="resolve-uri('samples/out/',base-uri())">
        <p:inline>
            <irrelevant/>
        </p:inline>
    </p:variable>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="test-fileset-copy"/>
            <p:pipe port="result" step="test-filesystem"/>
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

    <p:group name="test-fileset-copy">
        <p:output port="result"/>
        <p:identity>
            <p:input port="source">
                <p:inline>
                    <d:fileset>
                        <d:file href="dir/file.txt"/>
                        <d:file href="test.txt"/>
                        <d:file href="test.xml"/>
                        <d:file href="http://www.example.org/style.css"/>
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
        <p:add-xml-base name="source"/>
        <p:add-attribute name="expected" attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="$out-dir"/>
        </p:add-attribute>
        <p:sink/>

        <px:fileset-copy>
            <p:input port="source">
                <p:pipe port="result" step="source"/>
            </p:input>
            <p:with-option name="target" select="$out-dir"/>
        </px:fileset-copy>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:pipe port="result" step="expected"/>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="fileset-copy"/>
    </p:group>

    <p:group name="test-filesystem" cx:depends-on="test-fileset-copy">
        <p:output port="result"/>
        <p:choose>
            <p:when test="not(/*/@result='true')">
                <p:xpath-context>
                    <p:pipe port="result" step="test-fileset-copy"/>
                </p:xpath-context>
                <p:identity>
                    <p:input port="source">
                        <p:inline>
                            <c:result>skipped</c:result>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:inline xml:space="preserve">
<c:directory name="out">
    <c:directory name="dir">
        <c:file name="file.txt"/>
    </c:directory>
    <c:file name="test.txt"/>
    <c:file name="test.xml"/>
</c:directory>
                        </p:inline>
                    </p:input>
                </p:identity>
                <p:add-attribute attribute-name="xml:base" match="/*/c:directory">
                    <p:with-option name="attribute-value" select="concat($out-dir,'dir/')"/>
                </p:add-attribute>
                <p:add-attribute name="expected" attribute-name="xml:base" match="/*">
                    <p:with-option name="attribute-value" select="$out-dir"/>
                </p:add-attribute>
                <p:sink/>
                
                <px:directory-list name="filesystem.alternate">
                    <p:with-option name="path" select="$out-dir"/>
                </px:directory-list>
                <p:choose>
                    <p:when
                        test="/c:directory[@name='out' and @xml:base=$out-dir and not(@* except (@name,@xml:base)) and count(*)=3]
                          and /*/c:file[@name='test.xml' and not(@* except @name) and not(node())]
                          and /*/c:file[@name='test.txt' and not(@* except @name) and not(node())]
                          and /*/c:directory[@name='dir' and @xml:base=concat($out-dir,'dir/') and not(@* except (@name,@xml:base)) and count(*)=1]
                          and /*/c:directory/c:file[@name='file.txt' and not(@* except @name) and not(node())]"
                    >
                        <p:identity>
                            <p:input port="source">
                                <p:inline>
                                    <c:result result="true"/>
                                </p:inline>
                            </p:input>
                        </p:identity>
                    </p:when>
                    <p:otherwise>
                        <p:identity>
                            <p:input port="source">
                                <p:inline xml:space="preserve">
                                    <c:result result="false">
                                        <c:expected/>
                                        <c:was/>
                                    </c:result>
                                </p:inline>
                            </p:input>
                        </p:identity>
                        <p:insert match="c:expected" position="last-child">
                            <p:input port="insertion">
                                <p:pipe port="result" step="expected"/>
                            </p:input>
                        </p:insert>
                        <p:insert match="c:was" position="last-child">
                            <p:input port="insertion">
                                <p:pipe port="result" step="filesystem.alternate"/>
                            </p:input>
                        </p:insert>
                    </p:otherwise>
                </p:choose>
                <p:identity>
                    <p:log port="result"/>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filesystem"/>
    </p:group>

</p:declare-step>
