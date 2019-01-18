<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-move" name="main" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <p:documentation>Changes the xml:base of the fileset, without updating the hrefs. "original-href"-attributes will be added for files that exist on disk. No files will be physically moved, that's
        what px:fileset-store is for.</p:documentation>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:empty/>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="fileset"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="in-memory"/>
    </p:output>

    <p:option name="new-base" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>

    <p:viewport match="/*/d:file">
        <p:add-attribute match="/*" attribute-name="href-before-move">
            <p:with-option name="attribute-value" select="resolve-uri(/*/@href,base-uri(/*))"/>
        </p:add-attribute>
    </p:viewport>
    <p:xslt name="moved-fileset">
        <p:with-param name="new-base" select="$new-base"/>
        <p:input port="stylesheet">
            <p:document href="../xslt/fileset-move.xsl"/>
        </p:input>
    </p:xslt>

    <p:viewport match="/*/d:file" name="file">
        <p:choose>
            <p:when test="not(/*/@original-href)">
                <p:variable name="on-disk" select="resolve-uri(/*/(@original-href,@href)[1],base-uri(/*))"/>
                <p:try>
                    <p:group>
                        <px:info>
                            <p:with-option name="href" select="$on-disk"/>
                        </px:info>
                        <p:count/>
                    </p:group>
                    <p:catch>
                        <p:identity>
                            <p:input port="source">
                                <p:inline>
                                    <c:result>0</c:result>
                                </p:inline>
                            </p:input>
                        </p:identity>
                    </p:catch>
                </p:try>
                <p:choose>
                    <p:when test="/*=0">
                        <p:identity>
                            <p:input port="source">
                                <p:pipe port="current" step="file"/>
                            </p:input>
                        </p:identity>
                    </p:when>
                    <p:otherwise>
                        <p:add-attribute match="/*" attribute-name="original-href">
                            <p:input port="source">
                                <p:pipe port="current" step="file"/>
                            </p:input>
                            <p:with-option name="attribute-value" select="$on-disk"/>
                        </p:add-attribute>
                    </p:otherwise>
                </p:choose>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:viewport>
    <p:identity name="fileset.with-annotations"/>
    <p:delete match="/*/*/@href-before-move"/>
    <p:identity name="fileset"/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="in-memory.in" step="main"/>
        </p:iteration-source>
        <p:variable name="base-uri" select="base-uri(/*)"/>
        <p:choose>
            <p:xpath-context>
                <p:pipe port="result" step="fileset.with-annotations"/>
            </p:xpath-context>
            <p:when test="$base-uri=/*/d:file/@href-before-move">
                <p:variable name="new-href" select="(/*/d:file[@href-before-move=$base-uri])[1]/resolve-uri(@href,base-uri(.))">
                    <p:pipe port="result" step="fileset.with-annotations"/>
                </p:variable>
                <px:set-base-uri>
                    <p:with-option name="base-uri" select="$new-href"/>
                </px:set-base-uri>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:identity name="in-memory"/>

</p:declare-step>
