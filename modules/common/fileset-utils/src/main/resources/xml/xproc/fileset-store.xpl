<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:err="http://www.w3.org/ns/xproc-error"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:fileset-store" name="main"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Store the fileset to disk.</p>
        <p>Files that exist in memory are stored to disk from memory. Files that do not exist in
        memory but have an <code>original-href</code> pointing to an existing file on disk are
        copied. Existing files are overwritten. Files that do not exist in memory or at the
        <code>original-href</code> location are considered missing files, even if a file already
        exists at the target location.</p>
        <p>Fails when the "fail-or-error" option is set and there are missing files.</p>
        <p>Fails if the fileset contains files outside of the base directory.</p>
    </p:documentation>

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
            <p>A number of serialization parameters can be specified with attributes on the
            <code>d:file</code> elements:</p>
            <ul>
                <li>byte-order-mark</li>
                <li>cdata-section-elements</li>
                <li>doctype</li>
                <li>doctype-public</li>
                <li>doctype-system</li>
                <li>encoding</li>
                <li>escape-uri-attributes</li>
                <li>include-content-type</li>
                <li>indent</li>
                <li>media-type</li>
                <li>method</li>
                <li>normalization-form</li>
                <li>omit-xml-declaration</li>
                <li>standalone</li>
                <li>undeclare-prefixes</li>
                <li>version</li>
                <li>xml-declaration</li>
            </ul>
            <p>These parameters only have effect when the file exists in memory.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:output port="fileset.out" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output fileset is a copy of the input fileset but with <code>original-href</code>
            attributes added (or overwritten) with the same value as <code>href</code>. Missing
            files are removed if the "fail-or-error" option is not set (default).</p>
        </p:documentation>
        <p:pipe port="result" step="store"/>
    </p:output>

    <p:option name="fail-on-error" required="false" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to ignore missing files or raise an error.</p>
        </p:documentation>
    </p:option>

    <p:import href="fileset-library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-load
            px:fileset-copy
            px:fileset-update
            px:fileset-invert
            px:fileset-apply
        </p:documentation>
    </p:import>
    <p:import href="fileset-fix-original-hrefs.xpl">
        <p:documentation>
            pxi:fileset-fix-original-hrefs
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
            px:error
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:mkdir
            px:tempdir
            px:info
            px:copy
            px:set-doctype
            px:set-xml-declaration
            px:normalize-uri
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl">
        <p:documentation>
            px:zip
        </p:documentation>
    </p:import>

    <p:variable name="fileset-base" select="base-uri(/*)"/>

    <pxi:fileset-fix-original-hrefs name="fix">
        <p:documentation>
            Make the original-href attributes reflect what is actually stored on disk. Also normalizes
            @xml:base, @href and @original-href, relativizes @href against @xml:base, makes
            @original-href absolute, and removes @xml:base from d:file.
        </p:documentation>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="in-memory.in"/>
        </p:input>
        <p:with-option name="fail-on-missing" select="$fail-on-error"/>
        <p:with-option name="purge" select="'true'"/>
    </pxi:fileset-fix-original-hrefs>

    <p:viewport match="d:file">
        <p:documentation>Fail if the file is outside of the base directory (URI is absolute or
        starts with "..")</p:documentation>
        <p:choose>
            <p:when test="/*/@href[matches(.,'^[^/]+:') or starts-with(.,'..')]">
                <px:error code="XXXX" message="File outside base directory $1: $2">
                    <p:with-option name="param1" select="base-uri(/*)">
                        <p:pipe step="fix" port="result.fileset"/>
                    </p:with-option>
                    <p:with-option name="param2" select="/*/@href"/>
                </px:error>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:viewport>

    <p:documentation>Load zipped files into memory because px:zip and px:copy does not support these
    hrefs (includes `bundle:' and `jar:' files). For performance reasons, and also to avoid that the
    documents are modified (e.g. by serialization attributes), load them into memory one by one and
    immediately store them to a temporary location on disk.</p:documentation>
    <p:choose>
        <p:when test="//d:file[@original-href[contains(resolve-uri(.,base-uri(..)),'!/') or matches(.,'^(bundle|jar):')]]">
            <p:identity name="fileset"/>
            <p:sink/>
            <px:tempdir delete-on-exit="true"/>
            <px:normalize-uri name="unzip-dir">
                <p:with-option name="href" select="string(/*)"/>
            </px:normalize-uri>
            <p:sink/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="fileset" port="result"/>
                </p:input>
            </p:identity>
            <p:viewport name="unzip"
                        match="d:file[@original-href[contains(resolve-uri(.,base-uri(..)),'!/') or matches(.,'^(bundle|jar):')]]">
                <p:variable name="href" select="/*/@href"/>
                <p:variable name="original-href" select="/*/resolve-uri(@original-href,base-uri(.))"/>
                <p:variable name="unzip-dir" select="string(/*)">
                    <p:pipe step="unzip-dir" port="normalized"/>
                </p:variable>
                <px:fileset-create/>
                <px:fileset-add-entry>
                    <p:with-option name="href" select="$original-href"/>
                    <p:with-param port="file-attributes" name="method" select="'binary'"/>
                </px:fileset-add-entry>
                <px:fileset-load/>
                <p:store cx:decode="true" encoding="base64" name="store-binary">
                    <p:with-option name="href" select="resolve-uri($href,$unzip-dir)"/>
                </p:store>
                <p:add-attribute match="/*" attribute-name="original-href">
                    <p:input port="source">
                        <p:pipe step="unzip" port="current"/>
                    </p:input>
                    <p:with-option name="attribute-value" select="string(.)">
                        <p:pipe step="store-binary" port="result"/>
                    </p:with-option>
                </p:add-attribute>
            </p:viewport>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:documentation>Add doctype to XHTML 5.0 because XProc does not do it.</p:documentation>
    <p:add-attribute match="d:file[not(@doctype|@doctype-public|@doctype-system|@method) and
                                   @media-type='application/xhtml+xml' and
                                   @media-version='5.0']"
                     attribute-name="doctype"
                     attribute-value="&lt;!DOCTYPE html&gt;"/>

    <p:documentation>Zip files.</p:documentation>
    <p:choose>
        <p:when test="//d:file[contains(resolve-uri(@href, base-uri(.)),'!/')]">
            <p:identity name="fileset"/>
            <p:delete match="d:file[not(contains(resolve-uri(@href, base-uri(.)),'!/'))]"/>
            <p:choose name="maybe-unzip">
                <p:documentation>When there are non-XML files in memory, or XML files with a doctype
                or xml-declaration attribute in memory, first store them to a temporary location on
                disk.</p:documentation>
                <!-- for the sake of convenience store all non-xml to disk, even though px:zip supports binary encoding -->
                <p:when test="//d:file[not(@original-href)
                                       and (@doctype|@xml-declaration
                                            or not(@method='xml' or @media-type[matches(.,'.*/xml$') or matches(.,'.*\+xml$')]))]">
                    <p:output port="fileset" primary="true"/>
                    <p:output port="in-memory" sequence="true">
                        <p:pipe step="update" port="result.in-memory"/>
                    </p:output>
                    <p:identity name="zip-fileset"/>
                    <p:sink/>
                    <px:tempdir delete-on-exit="true"/>
                    <px:normalize-uri name="unzip-dir">
                        <p:with-option name="href" select="string(/*)"/>
                    </px:normalize-uri>
                    <p:sink/>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe step="zip-fileset" port="result"/>
                        </p:input>
                    </p:identity>
                    <p:delete match="d:file[not(
                                              not(@original-href)
                                              and (@doctype|@xml-declaration
                                                   or not(@method='xml' or @media-type[matches(.,'.*/xml$') or matches(.,'.*\+xml$')])))]"/>
                    <!-- store to temporarily location on disk (with a recursive call) -->
                    <p:group name="copy-to-temp">
                        <p:output port="result.fileset" primary="true">
                            <p:pipe step="result.fileset" port="result"/>
                        </p:output>
                        <p:output port="result.in-memory" sequence="true">
                            <p:pipe step="result.in-memory" port="result"/>
                        </p:output>
                        <p:output port="mapping">
                            <p:pipe step="mapping" port="result"/>
                        </p:output>
                        <!-- move to temporary directory -->
                        <px:fileset-copy name="copy">
                            <p:with-option name="target" select="string(/*)">
                                <p:pipe step="unzip-dir" port="normalized"/>
                            </p:with-option>
                            <p:input port="source.in-memory">
                                <p:pipe step="fix" port="result.in-memory"/>
                            </p:input>
                        </px:fileset-copy>
                        <!-- change zip files to directories -->
                        <p:label-elements match="d:file" attribute="href" label="replace(@href,'!','')" name="result.fileset"/>
                        <p:sink/>
                        <p:label-elements match="d:file" attribute="href" label="replace(@href,'!','')" name="mapping">
                            <p:input port="source">
                                <p:pipe step="copy" port="mapping"/>
                            </p:input>
                        </p:label-elements>
                        <p:sink/>
                        <p:for-each name="result.in-memory">
                            <p:iteration-source>
                                <p:pipe step="copy" port="result.in-memory"/>
                            </p:iteration-source>
                            <p:output port="result"/>
                            <px:set-base-uri>
                                <p:with-option name="base-uri" select="replace(base-uri(/*),'!','')"></p:with-option>
                            </px:set-base-uri>
                        </p:for-each>
                        <p:sink/>
                    </p:group>
                    <px:fileset-store name="store-to-temp">
                        <p:input port="in-memory.in">
                            <p:pipe step="copy-to-temp" port="result.in-memory"/>
                        </p:input>
                        <p:with-option name="fail-on-error" select="$fail-on-error"/>
                    </px:fileset-store>
                    <!-- apply the inverse of the previous copy -->
                    <px:fileset-invert name="inverse-copy">
                        <p:input port="source">
                            <p:pipe step="copy-to-temp" port="mapping"/>
                        </p:input>
                    </px:fileset-invert>
                    <p:sink/>
                    <px:fileset-apply>
                        <p:input port="source.fileset">
                            <p:pipe step="store-to-temp" port="fileset.out"/>
                        </p:input>
                        <p:input port="mapping">
                            <p:pipe step="inverse-copy" port="result"/>
                        </p:input>
                    </px:fileset-apply>
                    <p:identity name="zip-from-temp"/>
                    <p:sink/>
                    <px:fileset-update name="update">
                        <p:input port="source.fileset">
                            <p:pipe step="zip-fileset" port="result"/>
                        </p:input>
                        <p:input port="source.in-memory">
                            <p:pipe step="fix" port="result.in-memory"/>
                        </p:input>
                        <p:input port="update.fileset">
                            <p:pipe step="zip-from-temp" port="result"/>
                        </p:input>
                        <p:input port="update.in-memory">
                            <p:empty/>
                        </p:input>
                    </px:fileset-update>
                </p:when>
                <p:otherwise>
                    <p:output port="fileset" primary="true"/>
                    <p:output port="in-memory" sequence="true">
                        <p:pipe step="fix" port="result.in-memory"/>
                    </p:output>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <p:xslt name="zip-manifests">
                <p:input port="stylesheet">
                    <p:document href="../xslt/fileset-to-zip-manifests.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <p:sink/>
            <p:for-each name="zip">
                <p:iteration-source>
                    <p:pipe step="zip-manifests" port="secondary"/>
                </p:iteration-source>
                <px:zip>
                    <p:input port="source">
                        <p:pipe step="maybe-unzip" port="in-memory"/>
                    </p:input>
                    <p:input port="manifest">
                        <p:pipe step="zip" port="current"/>
                    </p:input>
                    <p:with-option name="href" select="/*/@href"/>
                </px:zip>
            </p:for-each>
            <p:sink/>
            <p:identity cx:depends-on="zip">
                <p:input port="source">
                    <p:pipe step="fileset" port="result"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:documentation>Stores files and filters out missing files in the result
        fileset.</p:documentation>
    <p:viewport match="d:file" name="store">
        <p:output port="result"/>
        <p:variable name="on-disk" select="(/*/@original-href, '')[1]"/>
        <p:variable name="target" select="/*/resolve-uri(@href, base-uri(.))"/>
        <p:variable name="href" select="/*/@href"/>
        <p:variable name="media-type" select="/*/@media-type"/>
        <!--serialization options:-->
        <p:variable name="byte-order-mark" select="if (/*/@byte-order-mark) then /*/@byte-order-mark else 'false'"/>
        <p:variable name="cdata-section-elements" select="/*/@cdata-section-elements"/>
        <p:variable name="doctype-public" select="/*/@doctype-public"/>
        <p:variable name="doctype-system" select="/*/@doctype-system"/>
        <p:variable name="doctype" select="/*/@doctype"/>
        <p:variable name="encoding" select="if (/*/@encoding) then /*/@encoding else 'utf-8'"/>
        <p:variable name="escape-uri-attributes" select="if (/*/@escape-uri-attributes) then /*/@escape-uri-attributes else 'false'"/>
        <p:variable name="include-content-type" select="/*/@include-content-type"/>
        <p:variable name="indent" select="if (/*/@indent) then /*/@indent else 'false'"/>
        <p:variable name="method" select="/*/@method"/>
        <p:variable name="normalization-form" select="if (/*/@normalization-form) then /*/@normalization-form else 'none'"/>
        <p:variable name="omit-xml-declaration" select="if (/*/@omit-xml-declaration) then /*/@omit-xml-declaration else 'false'"/>
        <p:variable name="standalone" select="if (/*/@standalone) then /*/@standalone else 'omit'"/>
        <p:variable name="undeclare-prefixes" select="if (/*/@undeclare-prefixes) then /*/@undeclare-prefixes else 'false'"/>
        <p:variable name="version" select="if (/*/@version) then /*/@version else '1.0'"/>
        <p:variable name="xml-declaration" select="/*/@xml-declaration"/>

        <p:choose>
            <p:when test="contains($target, '!/')">
                <p:documentation>File already zipped (handled above)</p:documentation>
                <p:identity/>
            </p:when>
            <p:when test="$on-disk">
                <p:documentation>File is on disk and not in memory (handle below)</p:documentation>
                <p:identity/>
            </p:when>
            <p:otherwise>
                <p:documentation>File is in memory.</p:documentation>
                <px:message severity="DEBUG">
                    <p:with-option name="message"
                        select="concat('Writing in-memory document to ',$href)"/>
                </px:message>
                <p:split-sequence>
                    <p:with-option name="test"
                        select="concat('base-uri(/*)=&quot;',$target,'&quot;')"/>
                    <p:input port="source">
                        <p:pipe step="fix" port="result.in-memory"/>
                    </p:input>
                </p:split-sequence>
                <!-- guaranteed to return a single document (because purge option set on pxi:fileset-fix-original-hrefs above) -->
                <p:split-sequence test="position()=1" initial-only="true"/>
                <p:delete match="/*/@xml:base"/>
                <p:choose>
                    <p:when test="starts-with($media-type,'binary/') or /c:data[@encoding='base64']">
                        <p:output port="result">
                            <p:pipe port="result" step="store-binary"/>
                        </p:output>
                        <p:store cx:decode="true" encoding="base64" name="store-binary">
                            <p:with-option name="href" select="$target"/>
                        </p:store>
                    </p:when>
                    <p:when
                        test="/c:data or (starts-with($media-type,'text/') and not(starts-with($media-type,'text/xml')))">
                        <p:output port="result">
                            <p:pipe port="result" step="store-text"/>
                        </p:output>
                        <p:store method="text" name="store-text">
                            <p:with-option name="href" select="$target"/>
                            <p:with-option name="byte-order-mark" select="$byte-order-mark"/>
                            <p:with-option name="encoding" select="$encoding"/>
                            <p:with-option name="media-type" select="$media-type"/>
                            <p:with-option name="normalization-form" select="$normalization-form"/>
                        </p:store>
                    </p:when>
                    <p:otherwise>
                        <p:output port="result"/>
                        <p:store name="store-xml">
                            <p:with-option name="href" select="$target"/>
                            <p:with-option name="byte-order-mark" select="$byte-order-mark"/>
                            <p:with-option name="cdata-section-elements" select="$cdata-section-elements"/>
                            <p:with-option name="doctype-public" select="$doctype-public"/>
                            <p:with-option name="doctype-system" select="$doctype-system"/>
                            <p:with-option name="encoding" select="$encoding"/>
                            <p:with-option name="escape-uri-attributes" select="$escape-uri-attributes"/>
                            <p:with-option name="include-content-type" select="
                                if ($include-content-type) then
                                    $include-content-type
                                else string($media-type!='application/xhtml+xml')"/>
                            <p:with-option name="indent" select="$indent"/>
                            <p:with-option name="media-type" select="$media-type"/>
                            <p:with-option name="method" select="
                                if ($media-type='application/xhtml+xml' and not($method)) then
                                    'xhtml'
                                else if ($method) then
                                    $method
                                else
                                    'xml'"/>
                            <p:with-option name="normalization-form" select="$normalization-form"/>
                            <p:with-option name="omit-xml-declaration" select="$omit-xml-declaration"/>
                            <p:with-option name="standalone" select="$standalone"/>
                            <p:with-option name="undeclare-prefixes" select="$undeclare-prefixes"/>
                            <p:with-option name="version" select="$version"/>
                        </p:store>
                        <p:identity>
                            <p:input port="source">
                                <p:pipe step="store-xml" port="result"/>
                            </p:input>
                        </p:identity>
                        <p:choose>
                            <p:when test="$doctype">
                                <p:variable name="stored-file" select="/*/text()"/>
                                <p:sink/>
                                <px:set-doctype px:message="Setting doctype of {$stored-file} to {$doctype}" px:message-severity="DEBUG">
                                    <p:with-option name="href" select="$stored-file"/>
                                    <p:with-option name="doctype" select="$doctype"/>
                                </px:set-doctype>
                            </p:when>
                            <p:otherwise>
                                <p:identity/>
                            </p:otherwise>
                        </p:choose>
                        <p:choose>
                            <p:when test="$xml-declaration">
                                <p:variable name="stored-file" select="/*/text()"/>
                                <p:sink/>
                                <px:set-xml-declaration px:message="Setting XML declaration of {$stored-file} to {$xml-declaration}"
                                                        px:message-severity="DEBUG">
                                    <p:with-option name="href" select="$stored-file"/>
                                    <p:with-option name="xml-declaration" select="$xml-declaration"/>
                                    <p:with-option name="encoding" select="$encoding"/>
                                </px:set-xml-declaration>
                            </p:when>
                            <p:otherwise>
                                <p:identity/>
                            </p:otherwise>
                        </p:choose>
                    </p:otherwise>
                </p:choose>
                <p:identity name="store-complete"/>
                <p:identity cx:depends-on="store-complete">
                    <p:input port="source">
                        <p:pipe port="current" step="store"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>

        <p:choose>
            <p:when test="$on-disk and not(contains($target, '!/'))">
                <p:documentation>File is on disk and not in memory; copy it to the new location.</p:documentation>
                <p:variable name="target-dir" select="replace($target,'[^/]+$','')"/>
                
                <p:try>
                    <p:group>
                        <px:info>
                            <p:with-option name="href" select="$target-dir"/>
                        </px:info>
                    </p:group>
                    <p:catch>
                        <p:identity>
                            <p:input port="source">
                                <p:empty/>
                            </p:input>
                        </p:identity>
                    </p:catch>
                </p:try>
                <p:wrap-sequence wrapper="info"/>
                <p:choose name="mkdir">
                    <p:when test="empty(/info/*)">
                        <px:message severity="DEBUG">
                            <p:with-option name="message"
                                select="concat('Making directory: ',substring-after($target-dir, $fileset-base))"
                            />
                        </px:message>
                        <p:try>
                            <p:group>
                                <px:mkdir>
                                    <p:with-option name="href" select="$target-dir"/>
                                </px:mkdir>
                            </p:group>
                            <p:catch>
                                <px:message severity="WARN">
                                    <p:with-option name="message"
                                        select="concat('Could not create directory: ',substring-after($target-dir, $fileset-base))"
                                    />
                                </px:message>
                                <p:sink/>
                            </p:catch>
                        </p:try>
                    </p:when>
                    <p:when test="not(/info/c:directory)">
                        <!--TODO rename the error-->
                        <px:message severity="WARN">
                            <p:with-option name="message"
                                select="concat('The target is not a directory: ',$href)"/>
                        </px:message>
                        <p:error code="err:file">
                            <p:input port="source">
                                <p:inline exclude-inline-prefixes="d">
                                    <c:message>The target is not a directory.</c:message>
                                </p:inline>
                            </p:input>
                        </p:error>
                        <p:sink/>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                        <p:sink/>
                    </p:otherwise>
                </p:choose>
                <p:try cx:depends-on="mkdir" name="store.copy">
                    <p:group>
                        <p:output port="result"/>
                        <px:copy name="store.copy.do">
                            <p:with-option name="href" select="$on-disk"/>
                            <p:with-option name="target" select="$target"/>
                        </px:copy>

                        <p:identity>
                            <p:input port="source">
                                <p:pipe port="current" step="store"/>
                            </p:input>
                        </p:identity>
                        <px:message severity="DEBUG">
                            <p:with-option name="message"
                                select="concat('Copied ',replace($on-disk,'^.*/([^/]*)$','$1'),' to ',$href)"
                            />
                        </px:message>
                    </p:group>
                    <p:catch name="store.copy.catch">
                        <p:output port="result">
                            <p:empty/>
                        </p:output>
                        <p:identity>
                            <p:input port="source">
                                <p:pipe port="error" step="store.copy.catch"/>
                            </p:input>
                        </p:identity>
                        <px:message severity="WARN">
                            <p:with-option name="message" select="concat('Copy error: ',/*/*)"/>
                        </px:message>
                        <p:sink/>
                    </p:catch>
                </p:try>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        
        <p:documentation>Add original-href attribute so that the in-memory documents can be
        discarded and px:fileset-store called again without resulting in a "neither stored on disk
        nor in memory" error.</p:documentation>
        <p:add-attribute match="/d:file" attribute-name="original-href">
            <p:with-option name="attribute-value" select="$target">
                <p:empty/>
            </p:with-option>
        </p:add-attribute>
    </p:viewport>

</p:declare-step>
