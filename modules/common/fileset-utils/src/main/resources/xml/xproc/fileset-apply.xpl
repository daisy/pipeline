<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="px:fileset-apply" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Apply rename actions defined in a mapping document</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:input port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The mapping document is a <code>d:fileset</code> that maps files in the source
            fileset (<code>@original-href</code>) to files in the result fileset
            (<code>@href</code>).</p>
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true">
        <p:pipe step="fileset" port="result"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output fileset.</p>
            <p>If the mapping document has a <code>xml:base</code> attribute, it will be used as the
            new base of the output fileset.</p>
            <p>The base URI of in-memory documents are changed if needed, and
            "original-href"-attributes are added for files that exist on disk.</p>
        </p:documentation>
        <p:pipe step="in-memory" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:error
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="fileset-join.xpl"/>
    <p:import href="fileset-rebase.xpl"/>
    <p:import href="fileset-fix-original-hrefs.xpl"/>

    <p:documentation>
        Normalize input fileset, make href absolute, and make the original-href attributes reflect
        what is actually stored on disk.
    </p:documentation>
    <pxi:fileset-fix-original-hrefs detect-existing="true" warn-on-missing="false" name="fix">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </pxi:fileset-fix-original-hrefs>
    <p:label-elements match="d:file" attribute="href" replace="true" label="resolve-uri(@href,base-uri(.))"/>
    <p:identity name="source.fileset"/>
    <p:sink/>

    <p:documentation>
        Normalize mapping document and make href absolute. Also removes duplicate files.
    </p:documentation>
    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="main" port="mapping"/>
        </p:input>
    </px:fileset-join>
    <p:label-elements match="d:file" attribute="href" replace="true" label="resolve-uri(@href,base-uri(.))"/>

    <p:documentation>
        Fail if the mapping contains a file that does not exist in source fileset
    </p:documentation>
    <p:viewport match="d:file">
        <p:variable name="href" select="/*/@original-href"/>
        <p:choose>
            <p:xpath-context>
                <p:pipe step="source.fileset" port="result"/>
            </p:xpath-context>
            <p:when test="$href!='' and /*/d:file[@href=$href]">
                <p:identity/>
            </p:when>
            <p:otherwise>
                <px:error code="XXXX" message="Mapping contains file that does not exist in source fileset: $1">
                    <p:with-option name="param1" select="$href"/>
                </px:error>
            </p:otherwise>
        </p:choose>
    </p:viewport>
    <p:identity name="mapping"/>
    <p:sink/>

    <p:documentation>
        Update href in fileset
    </p:documentation>
    <p:viewport match="d:file">
        <p:viewport-source>
            <p:pipe step="source.fileset" port="result"/>
        </p:viewport-source>
        <p:variable name="original-href" select="/*/@href"/>
        <p:add-attribute match="/*" attribute-name="href">
            <p:with-option name="attribute-value" select="(/*/d:file[@original-href=$original-href]/@href,$original-href)[1]">
                <p:pipe step="mapping" port="result"/>
            </p:with-option>
        </p:add-attribute>
    </p:viewport>

    <p:documentation>
        Relativize href and inherit xml:base
    </p:documentation>
    <p:choose>
        <p:xpath-context>
            <p:pipe step="main" port="mapping"/>
        </p:xpath-context>
        <p:when test="/*/@xml:base">
            <!-- set xml:base and relativize href -->
            <px:fileset-rebase>
                <p:with-option name="new-base" select="base-uri(/*)">
                    <p:pipe step="main" port="mapping"/>
                </p:with-option>
            </px:fileset-rebase>
        </p:when>
        <p:otherwise>
            <!-- relativize href -->
            <px:fileset-join/>
        </p:otherwise>
    </p:choose>
    <p:identity name="fileset"/>
    <p:sink/>

    <p:documentation>
        Update the base URI of the in-memory documents
    </p:documentation>
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="fix" port="result.in-memory"/>
        </p:iteration-source>
        <px:normalize-uri name="normalize-uri">
            <p:with-option name="href" select="base-uri(/*)"/>
        </px:normalize-uri>
        <p:group>
            <p:variable name="base-uri" select="string(/*)">
                <p:pipe step="normalize-uri" port="normalized"/>
            </p:variable>
            <p:choose>
                <p:xpath-context>
                    <p:pipe step="mapping" port="result"/>
                </p:xpath-context>
                <p:when test="$base-uri=/*/d:file/@original-href">
                    <px:set-base-uri>
                        <p:with-option name="base-uri" select="(/*/d:file[@original-href=$base-uri])[1]/resolve-uri(@href,base-uri(.))">
                            <p:pipe step="mapping" port="result"/>
                        </p:with-option>
                    </px:set-base-uri>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:group>
    </p:for-each>
    <p:identity name="in-memory"/>
    <p:sink/>

</p:declare-step>
