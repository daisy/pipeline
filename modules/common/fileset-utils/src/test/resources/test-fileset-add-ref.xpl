<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-add-ref" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all" xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="../../main/resources/xml/xproc/fileset-add-ref.xpl"/>
    <p:import href="compare.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="add-ref"/>
            <p:pipe port="result" step="add-ref-first"/>
            <p:pipe port="result" step="add-ref-absolute"/>
            <p:pipe port="result" step="add-ref-relative-to-file-base"/>
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

    <p:group name="add-ref">
        <p:output port="result"/>
        <px:fileset-add-ref href="../fileA.xml" ref="./fileC.xml">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="file:/users/me/fileA.xml">
                            <d:ref href="fileB.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-add-ref>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="file:/users/me/fileA.xml">
                            <d:ref href="fileB.xml"/>
                            <d:ref href="fileC.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-ref"/>
    </p:group>

    <p:group name="add-ref-first">
        <p:output port="result"/>
        <px:fileset-add-ref href="../fileA.xml" ref="./fileC.xml" first="true">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="file:/users/me/fileA.xml">
                            <d:ref href="fileB.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-add-ref>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="file:/users/me/fileA.xml">
                            <d:ref href="fileC.xml"/>
                            <d:ref href="fileB.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-ref-first"/>
    </p:group>

    <p:group name="add-ref-absolute">
        <p:output port="result"/>
        <px:fileset-add-ref href="fileA.xml" ref="file:/users/me/dir/subdir/fileC.xml">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="fileA.xml">
                            <d:ref href="fileB.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:fileset-add-ref>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="fileA.xml">
                            <d:ref href="fileB.xml"/>
                            <d:ref href="subdir/fileC.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-ref-absolute"/>
    </p:group>

    <p:group name="add-ref-relative-to-file-base">
        <p:output port="result"/>
        <p:identity>
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="fileA.xml">
                            <d:ref href="fileB.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute match="/*/*" attribute-name="xml:base" attribute-value="file:/users/someone-else/folder/"/>
        <p:delete match="/*/*/@xml:base"/>
        <px:fileset-add-ref href="fileA.xml" ref="file:/users/someone-else/folder/subdir/fileC.xml"/>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/users/me/dir/">
                        <d:file href="fileA.xml">
                            <d:ref href="fileB.xml"/>
                            <d:ref href="subdir/fileC.xml"/>
                        </d:file>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="add-ref-relative-to-file-base"/>
    </p:group>
</p:declare-step>
