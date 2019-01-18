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

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>
    <p:output port="fileset.out" primary="false">
        <p:pipe port="result" step="store"/>
    </p:output>

    <p:option name="fail-on-error" required="false" select="'false'"/>

    <p:import href="fileset-library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="fileset-filter-in-memory.xpl"/>

    <p:variable name="fileset-base" select="base-uri(/*)">
        <p:pipe port="fileset.in" step="main"/>
    </p:variable>

    <pxi:fileset-filter-in-memory name="fileset.in-memory.in">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="in-memory.in"/>
        </p:input>
    </pxi:fileset-filter-in-memory>
    <p:sink/>

    <p:documentation>Load zipped files into memory.</p:documentation>
    <p:delete match="//d:file[not(contains(resolve-uri((@original-href, @href)[1], base-uri(.)), '!/'))]">
        <p:input port="source">
            <p:pipe step="main" port="fileset.in"/>
        </p:input>
    </p:delete>
    <px:fileset-diff name="fileset.unzip">
        <p:input port="secondary">
            <p:pipe step="fileset.in-memory.in" port="result"/>
        </p:input>
    </px:fileset-diff>
    <px:fileset-load name="in-memory.unzip">
        <p:input port="in-memory">
            <p:empty/>
        </p:input>
    </px:fileset-load>
    <p:sink/>
    <px:fileset-join name="fileset.in-memory">
        <p:input port="source">
            <p:pipe step="fileset.in-memory.in" port="result"/>
            <p:pipe step="fileset.unzip" port="result"/>
        </p:input>
    </px:fileset-join>
    <p:sink/>

    <p:documentation>Zip files.</p:documentation>
    <p:delete match="//d:file[not(contains(resolve-uri(@href, base-uri(.)),'!/'))]" name="fileset.zip">
        <p:input port="source">
            <p:pipe step="main" port="fileset.in"/>
        </p:input>
    </p:delete>
    <p:xslt name="zip-manifests">
        <p:input port="source">
            <p:pipe step="fileset.zip" port="result"/>
            <p:pipe step="fileset.in-memory" port="result"/>
        </p:input>
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
                <p:pipe port="in-memory.in" step="main"/>
                <p:pipe step="in-memory.unzip" port="result"/>
            </p:input>
            <p:input port="manifest">
                <p:pipe step="zip" port="current"/>
            </p:input>
            <p:with-option name="href" select="/*/@href"/>
        </px:zip>
    </p:for-each>
    <p:sink/>

    <p:documentation>Stores files and filters out missing files in the result
        fileset.</p:documentation>
    <p:viewport match="d:file" name="store" cx:depends-on="zip">
        <p:output port="result"/>
        <p:viewport-source>
            <p:pipe port="fileset.in" step="main"/>
        </p:viewport-source>
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

        <p:choose>
            <p:xpath-context>
                <p:pipe port="result" step="fileset.in-memory"/>
            </p:xpath-context>
            <p:when test="contains($target, '!/')">
                <p:documentation>File already zipped.</p:documentation>
                <p:identity/>
            </p:when>
            <p:when test="//d:file[resolve-uri(@href,base-uri(.))=$target]">
                <p:documentation>File is in memory.</p:documentation>
                <px:message severity="DEBUG">
                    <p:with-option name="message"
                        select="concat('Writing in-memory document to ',$href)"/>
                </px:message>
                <p:split-sequence>
                    <p:with-option name="test"
                        select="concat('base-uri(/*)=&quot;',$target,'&quot;')"/>
                    <p:input port="source">
                        <p:pipe port="in-memory.in" step="main"/>
                        <p:pipe step="in-memory.unzip" port="result"/>
                    </p:input>
                </p:split-sequence>
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
                        <p:choose>
                            <p:when test="$doctype">
                                <px:set-doctype>
                                    <p:with-option name="href" select="/*/text()">
                                        <p:pipe port="result" step="store-xml"/>
                                    </p:with-option>
                                    <p:with-option name="doctype" select="$doctype"/>
                                </px:set-doctype>
                            </p:when>
                            <p:otherwise>
                                <p:identity>
                                    <p:input port="source">
                                        <p:pipe port="result" step="store-xml"/>
                                    </p:input>
                                </p:identity>
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
            </p:when>
            <p:when test="not($on-disk) and $fail-on-error = 'false'">
                <px:message message="Resource not found: $1" severity="WARN">
                    <p:with-option name="param1" select="$href"/>
                </px:message>
            </p:when>
            <p:when test="not($on-disk)">
                <p:error code="PEZE00">
                    <p:input port="source">
                        <p:inline>
                            <c:message>Found document in fileset that are neither stored on disk nor
                                in memory.</c:message>
                        </p:inline>
                    </p:input>
                </p:error>
            </p:when>
            <p:otherwise>
                <p:documentation>File is already on disk; copy it to the new location.</p:documentation>
                <p:variable name="target-dir" select="replace($target,'[^/]+$','')"/>
                
                <p:try>
                    <p:group>
                        <px:info>
                            <p:with-option name="href" select="$on-disk"/>
                        </px:info>
                    </p:group>
                    <p:catch>
                        <px:error code="PEZE01" message="Found document in fileset that was declared as being on disk but were neither stored on disk nor in memory: $1">
                            <p:with-option name="param1" select="$on-disk"/>
                        </px:error>
                    </p:catch>
                </p:try>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="current" step="store"/>
                    </p:input>
                </p:identity>
                
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
            </p:otherwise>
        </p:choose>
    </p:viewport>

</p:declare-step>
