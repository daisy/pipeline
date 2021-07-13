<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="px:fileset-copy" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Copy a fileset to a new location</p>
        <p>Fails if the fileset contains files outside of the base directory. No files are
        physically copied, that is done upon calling px:fileset-store.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output fileset at the new location.</p>
            <p>The xml:base is changed to "target". The hrefs are not updated, unless the "flatten"
            option is set, in which case they are reduced to the file name. The base URI of the
            in-memory documents are changed accordingly, and "original-href"-attributes are added
            for files that exist on disk.</p>
        </p:documentation>
        <p:pipe step="maybe-apply" port="in-memory"/>
    </p:output>
    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that contains the mapping from the source files
            (<code>@original-href</code>) to the copied files (<code>@href</code>).</p>
        </p:documentation>
        <p:pipe step="mapping" port="result"/>
    </p:output>

    <p:option name="target" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The target directory.</p>
        </p:documentation>
    </p:option>
    <p:option name="flatten" cx:as="xs:string" required="false" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Move all files to a single directory.</p>
            <p>Renames files when needed to avoid that files would overwrite each other.</p>
        </p:documentation>
    </p:option>
    <p:option name="prefix" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Prefix to add before file names.</p>
            <p>Only if "flatten" option is set.</p>
        </p:documentation>
    </p:option>
    <p:option name="dry-run" cx:as="xs:string" required="false" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Don't actually perform the copy operation, only return the list of intended rename
            actions on the "mapping" port.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:error
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="fileset-join.xpl"/>
    <p:import href="fileset-apply.xpl"/>

    <p:documentation>Add xml:base and normalize fileset</p:documentation>
    <p:add-xml-base/>
    <px:fileset-join/>

    <p:label-elements match="/*/d:file" attribute="href-before-move" label="resolve-uri(@href, base-uri(.))"/>

    <p:documentation>Flatten fileset</p:documentation>
    <p:choose>
        <p:when test="$flatten='true'">
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="../xslt/fileset-flatten.xsl"/>
                </p:input>
                <p:with-param name="prefix" select="$prefix"/>
            </p:xslt>
            <p:label-elements match="d:file[@href=preceding-sibling::d:file/@href]" attribute="href" replace="true"
                              label="for $href in @href
                                     return replace($href,
                                                    '^(.+?)(\.[^\.]+)?$',
                                                    concat('$1_',1+count(preceding-sibling::d:file[@href=$href]),'$2'))">
                <p:documentation>Because the renaming may have resulted in duplicate file names</p:documentation>
            </p:label-elements>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:viewport match="d:file" name="add-original-href">
        <p:documentation>Fail if the file is outside of the base directory (URI is absolute or
        starts with "..")</p:documentation>
        <p:choose>
            <p:when test="/*/@href[matches(.,'^[^/]+:') or starts-with(.,'..')]">
                <px:error code="XXXX" message="File outside base directory $1: $2">
                    <p:with-option name="param1" select="base-uri(/*)">
                        <p:pipe step="main" port="source.fileset"/>
                    </p:with-option>
                    <p:with-option name="param2" select="/*/@href"/>
                </px:error>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:viewport>

    <p:documentation>Set the base directory to the target directory</p:documentation>
    <px:set-base-uri>
        <p:with-option name="base-uri" select="$target"/>
    </px:set-base-uri>
    <p:label-elements match="d:file" attribute="original-href" label="@href-before-move" replace="true"/>
    <p:delete match="/*/*[not(self::d:file)]"/>
    <p:delete match="d:file/@*[not(name()=('href','original-href'))]" name="mapping"/>
    <p:sink/>

    <p:choose name="maybe-apply">
        <p:when test="$dry-run='true'">
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="main" port="source.in-memory"/>
            </p:output>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="main" port="source.fileset"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:otherwise>
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="apply" port="result.in-memory"/>
            </p:output>
            <px:fileset-apply name="apply">
                <p:input port="source.fileset">
                    <p:pipe step="main" port="source.fileset"/>
                </p:input>
                <p:input port="source.in-memory">
                    <p:pipe step="main" port="source.in-memory"/>
                </p:input>
                <p:input port="mapping">
                    <p:pipe step="mapping" port="result"/>
                </p:input>
            </px:fileset-apply>
        </p:otherwise>
    </p:choose>

</p:declare-step>
