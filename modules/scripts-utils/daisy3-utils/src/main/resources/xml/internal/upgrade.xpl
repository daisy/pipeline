<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:oebpackage="http://openebook.org/namespaces/oeb-package/1.0/"
                xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
                exclude-inline-prefixes="#all"
                type="px:daisy3-upgrade" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Upgrade a DAISY 3 publication from version 1.1.0 (Z39.86-2002) to version 2005 (Z39.86-2005).</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input DAISY 3 fileset.</p>
            <p>Must contain exactly one NCX (application/x-dtbncx+xml) file and this entry must have a
            <code>media-version</code> attribute with value "1.1.0" or "2005-1".</p>
            <p>Must contain exactly one OPF (application/oebps-package+xml) file and this entry must have a
            <code>media-version</code> attribute with value "1.0.1" or "1.2".</p>
            <p>SMIL (application/smil+xml) entries must have a <code>media-version</code> attribute
            with value "dtb-1.1.0", "dtb-2005-1" or "dtb-2005-2".</p>
            <p>DTBook (application/x-dtbook+xml) entries must have a <code>media-version</code> attribute
            with value "1.1.0", "2005-1", "2005-2" or "2005-3".</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:option name="version" select="'2005'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The version of the output DAISY 3</p>
            <p>Supported values are 2005, 2005-1, 2005-2 and 2005-3.</p>
        </p:documentation>
    </p:option>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Version 2005 DAISY 3 fileset.</p>
        </p:documentation>
        <p:pipe step="upgrade-opf" port="in-memory"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-update
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-upgrade
        </p:documentation>
    </p:import>

    <p:variable name="ncx-version" select="//d:file[@media-type='application/x-dtbncx+xml'][1]/@media-version"/>

    <px:assert error-code="XXXXX" message="There must be exactly one NCX file in the DAISY 3 fileset">
        <p:with-option name="test" select="count(//d:file[@media-type='application/x-dtbncx+xml'])=1"/>
    </px:assert>
    <px:assert error-code="XXXXX" message="Can't identify version of input DAISY 3">
        <p:with-option name="test" select="$ncx-version!=''"/>
    </px:assert>
    <px:assert error-code="XXXXX" message="Input DAISY 3 version must be '1.1.0' or '2005-1', but got '$1'">
        <p:with-option name="test" select="$ncx-version=('1.1.0','2005-1')"/>
        <p:with-option name="param1" select="$ncx-version"/>
    </px:assert>
    <px:assert error-code="XXXXX" message="The output DAISY 3 version must be 2005">
        <p:with-option name="test" select="$version=('2005','2005-1','2005-2','2005-3')"/>
    </px:assert>
    <p:identity px:message-severity="DEBUG" px:message="Input version: {$ncx-version}"/>

    <p:documentation>Upgrade NCX document</p:documentation>
    <p:group name="upgrade-ncx">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="choose" port="in-memory"/>
        </p:output>
        <p:delete match="d:file[not(@media-type='application/x-dtbncx+xml' and @media-version='1.1.0')]"/>
        <p:choose name="choose">
            <p:when test="exists(//d:file)">
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="fileset-update" port="result.in-memory"/>
                </p:output>
                <px:fileset-load name="ncx">
                    <p:input port="in-memory">
                        <p:pipe step="main" port="source.in-memory"/>
                    </p:input>
                </px:fileset-load>
                <p:for-each name="upgraded-ncx">
                    <p:output port="result"/>
                    <p:xslt>
                        <p:input port="stylesheet">
                            <p:document href="ncx/1.1.0-to-2005-1.xsl"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                </p:for-each>
                <p:sink/>
                <px:fileset-update name="fileset-update">
                    <p:input port="source.fileset">
                        <p:pipe step="main" port="source.fileset"/>
                    </p:input>
                    <p:input port="source.in-memory">
                        <p:pipe step="main" port="source.in-memory"/>
                    </p:input>
                    <p:input port="update.fileset">
                        <p:pipe step="ncx" port="result.fileset"/>
                    </p:input>
                    <p:input port="update.in-memory">
                        <p:pipe step="upgraded-ncx" port="result"/>
                    </p:input>
                </px:fileset-update>
            </p:when>
            <p:otherwise>
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="main" port="source.in-memory"/>
                </p:output>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="main" port="source.fileset"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:documentation>Update (or add) DOCTYPE</p:documentation>
        <p:delete match="d:file[@media-type='application/x-dtbncx+xml']/@doctype"/>
        <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml']"
                         attribute-name="doctype-public"
                         attribute-value="-//NISO//DTD ncx 2005-1//EN"/>
        <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml']"
                         attribute-name="doctype-system"
                         attribute-value="http://www.daisy.org/z3986/2005/ncx-2005-1.dtd"/>
        <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml']"
                         attribute-name="media-version"
                         attribute-value="2005-1"/>
    </p:group>

    <p:documentation>Upgrade SMIL documents</p:documentation>
    <p:group name="upgrade-smil">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="choose" port="in-memory"/>
        </p:output>
        <p:variable name="new-dtbsmil-version" select="if ($version='2005-1') then '2005-1' else '2005-2'"/>
        <p:delete match="d:file[not(@media-type='application/smil+xml')]"/>
        <px:assert error-code="XXXXX" message="SMIL versions must be 'dtb-1.1.0', 'dtb-2005-1' or 'dtb-2005-2'">
            <p:with-option name="test" select="not(exists(//d:file[not(@media-version=('dtb-1.1.0','dtb-2005-1','dtb-2005-2'))]))"/>
        </px:assert>
        <p:delete match="d:file[not(@media-version='dtb-1.1.0')]"/>
        <p:choose name="choose">
            <p:when test="exists(//d:file)">
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="fileset-update" port="result.in-memory"/>
                </p:output>
                <px:fileset-load name="smil">
                    <p:input port="in-memory">
                        <p:pipe step="upgrade-ncx" port="in-memory"/>
                    </p:input>
                </px:fileset-load>
                <p:for-each name="upgraded-smil">
                    <p:output port="result"/>
                    <!--
                        note that 2005-1 is valid according to 2005-2 DTD
                    -->
                    <p:xslt>
                        <p:input port="stylesheet">
                            <p:document href="smils/1.1.0-to-2005-1.xsl"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                </p:for-each>
                <p:sink/>
                <px:fileset-update name="fileset-update">
                    <p:input port="source.fileset">
                        <p:pipe step="upgrade-ncx" port="fileset"/>
                    </p:input>
                    <p:input port="source.in-memory">
                        <p:pipe step="upgrade-ncx" port="in-memory"/>
                    </p:input>
                    <p:input port="update.fileset">
                        <p:pipe step="smil" port="result.fileset"/>
                    </p:input>
                    <p:input port="update.in-memory">
                        <p:pipe step="upgraded-smil" port="result"/>
                    </p:input>
                </px:fileset-update>
            </p:when>
            <p:otherwise>
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="upgrade-ncx" port="in-memory"/>
                </p:output>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="upgrade-ncx" port="fileset"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:documentation>Update (or add) DOCTYPE</p:documentation>
        <p:delete match="d:file[@media-type='application/smil+xml']/@doctype"/>
        <p:add-attribute match="d:file[@media-type='application/smil+xml']"
                         attribute-name="doctype-public">
            <p:with-option name="attribute-value" select="concat('-//NISO//DTD dtbsmil ',$new-dtbsmil-version,'//EN')"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/smil+xml']"
                         attribute-name="doctype-system">
            <p:with-option name="attribute-value" select="concat('http://www.daisy.org/z3986/2005/dtbsmil-',$new-dtbsmil-version,'.dtd')"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/smil+xml']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="concat('dtb-',$new-dtbsmil-version)"/>
        </p:add-attribute>
    </p:group>

    <p:documentation>Upgrade content documents</p:documentation>
    <p:group name="upgrade-dtbook">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="fileset-update" port="result.in-memory"/>
        </p:output>
        <p:variable name="new-dtbook-version" select="if ($version='2005') then '2005-3' else $version"/>
        <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook">
            <p:input port="in-memory">
                <p:pipe step="upgrade-smil" port="in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:for-each name="upgraded-dtbook">
            <p:output port="result"/>
            <px:dtbook-upgrade>
                <p:with-option name="version" select="$new-dtbook-version"/>
            </px:dtbook-upgrade>
        </p:for-each>
        <p:sink/>
        <px:fileset-update name="fileset-update">
            <p:input port="source.fileset">
                <p:pipe step="upgrade-smil" port="fileset"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="upgrade-smil" port="in-memory"/>
            </p:input>
            <p:input port="update.fileset">
                <p:pipe step="dtbook" port="result.fileset"/>
            </p:input>
            <p:input port="update.in-memory">
                <p:pipe step="upgraded-dtbook" port="result"/>
            </p:input>
        </px:fileset-update>
        <p:documentation>Update (or add) DOCTYPE</p:documentation>
        <p:delete match="d:file[@media-type='application/x-dtbook+xml']/@doctype"/>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']"
                         attribute-name="doctype-public">
            <p:with-option name="attribute-value" select="concat('-//NISO//DTD dtbook ',$new-dtbook-version,'//EN')"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']"
                         attribute-name="doctype-system">
            <p:with-option name="attribute-value" select="concat('http://www.daisy.org/z3986/2005/dtbook-',$new-dtbook-version,'.dtd')"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="$new-dtbook-version"/>
        </p:add-attribute>
    </p:group>

    <p:documentation>Upgrade package document</p:documentation>
    <p:group name="upgrade-opf">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="choose" port="in-memory"/>
        </p:output>
        <p:variable name="opf-version" select="//d:file[@media-type='application/oebps-package+xml'][1]/@media-version"/>
        <p:delete match="d:file[not(@media-type='application/oebps-package+xml')]"/>
        <px:assert error-code="XXXXX" message="There must be exactly one OPF document in the DAISY 3 fileset">
            <p:with-option name="test" select="count(//d:file)=1"/>
        </px:assert>
        <px:assert error-code="XXXXX" message="Can't identify version of OPF document">
            <p:with-option name="test" select="$opf-version!=''"/>
        </px:assert>
        <px:assert error-code="XXXXX" message="Version of OPF document must be '1.0.1' or '1.2', but got '$1'">
            <p:with-option name="test" select="$opf-version=('1.0.1','1.2')"/>
            <p:with-option name="param1" select="$opf-version"/>
        </px:assert>
        <p:delete match="d:file[not(@media-version='1.0.1')]"/>
        <p:choose name="choose">
            <p:when test="exists(//d:file)">
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="fileset-update" port="result.in-memory"/>
                </p:output>
                <px:fileset-load name="opf">
                    <p:input port="in-memory">
                        <p:pipe step="upgrade-dtbook" port="in-memory"/>
                    </p:input>
                </px:fileset-load>
                <p:for-each name="upgraded-opf">
                    <p:output port="result"/>
                    <p:xslt>
                        <p:input port="stylesheet">
                            <p:document href="opf/1.0.1-to-1.2.xsl"/>
                        </p:input>
                        <p:input port="parameters">
                            <p:empty/>
                        </p:input>
                    </p:xslt>
                </p:for-each>
                <p:sink/>
                <px:fileset-update name="fileset-update">
                    <p:input port="source.fileset">
                        <p:pipe step="upgrade-dtbook" port="fileset"/>
                    </p:input>
                    <p:input port="source.in-memory">
                        <p:pipe step="upgrade-dtbook" port="in-memory"/>
                    </p:input>
                    <p:input port="update.fileset">
                        <p:pipe step="opf" port="result.fileset"/>
                    </p:input>
                    <p:input port="update.in-memory">
                        <p:pipe step="upgraded-opf" port="result"/>
                    </p:input>
                </px:fileset-update>
            </p:when>
            <p:otherwise>
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="upgrade-dtbook" port="in-memory"/>
                </p:output>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="upgrade-dtbook" port="fileset"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:documentation>Update (or add) DOCTYPE</p:documentation>
        <p:delete match="d:file[@media-type='application/oebps-package+xml']/@doctype"/>
        <p:add-attribute match="d:file[@media-type='application/oebps-package+xml']"
                         attribute-name="doctype-public"
                         attribute-value="+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN"/>
        <p:add-attribute match="d:file[@media-type='application/oebps-package+xml']"
                         attribute-name="doctype-system"
                         attribute-value="http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd"/>
        <p:add-attribute match="d:file[@media-type='application/oebps-package+xml']"
                         attribute-name="media-version"
                         attribute-value="1.2"/>
    </p:group>

</p:declare-step>
