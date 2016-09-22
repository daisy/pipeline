<p:declare-step version="1.0" name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:err="http://www.w3.org/ns/xproc-error"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="px:fileset-copy" exclude-inline-prefixes="#all">

    <p:input port="source"/>
    <p:output port="result" primary="true"/>
    <p:option name="target" required="true"/>
    <p:option name="fail-on-error" select="'false'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>

    <p:try>
        <p:group>
            <p:output port="result" primary="true"/>

            <!-- Check the existence of the directory -->
            <p:group name="checkdir">
                <!--Get file system info on the directory-->
                <!--Note: we wrap the result since an empty sequence is returned when the file does not exist-->
                <p:try>
                    <p:group>
                        <px:info>
                            <p:with-option name="href" select="$target"/>
                        </px:info>
                    </p:group>
                    <p:catch>
                        <!-- err:FU01 - Occurs if the file named in href cannot be read (i.e. the directory does not exist). -->
                        <p:identity>
                            <p:input port="source">
                                <p:empty/>
                            </p:input>
                        </p:identity>
                    </p:catch>
                </p:try>
                <p:wrap-sequence wrapper="info"/>
                <p:choose>
                    <p:when test="empty(/info/*)">
                        <px:mkdir>
                            <p:with-option name="href" select="$target"/>
                        </px:mkdir>
                    </p:when>
                    <p:when test="not(/info/c:directory)">
                        <!--TODO rename the error-->
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
            </p:group>

            <p:group cx:depends-on="checkdir">
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="source" step="main"/>
                    </p:input>
                </p:identity>
                <!-- Handle relative resources outside of the base directory -->
                <p:viewport name="handle-outer-file" match="/d:fileset/d:file[not(matches(@href,'^[^/]+:')) and starts-with(@href,'..')]">
                    <!--TODO: extract XPath functions in uri-utils -->
                    <p:label-elements attribute="href" label="@href"/>
                </p:viewport>

                <!-- Handle relative resources in the base directory -->
                <p:viewport name="handle-inner-file" match="//d:file[not(matches(@href,'^[^/]+:')) and not(starts-with(@href,'..'))]">
                    <p:variable name="href" select="*/resolve-uri(@href, base-uri(.))"/>
                    <p:variable name="target-file" select="resolve-uri(*/@href, concat($target,'/'))"/>
                    <px:mkdir name="mkdir">
                        <p:with-option name="href" select="replace($target-file,'[^/]+$','')"/>
                    </px:mkdir>
                    <px:copy name="copy" cx:depends-on="mkdir">
                        <p:with-option name="href" select="$href"/>
                        <p:with-option name="target" select="$target-file"/>
                        <p:with-option name="fail-on-error" select="$fail-on-error"/>
                    </px:copy>
                    <p:identity cx:depends-on="copy">
                        <p:input port="source">
                            <p:pipe port="current" step="handle-inner-file"/>
                        </p:input>
                    </p:identity>
                </p:viewport>
            </p:group>

            <!--Set the base directory to the target directory-->
            <p:add-attribute attribute-name="xml:base" match="/*">
                <p:with-option name="attribute-value" select="$target"/>
            </p:add-attribute>

        </p:group>
        <p:catch name="catch">
            <p:output port="result" primary="true"/>
            <!--Rethrows the error if $fail-on-error is true, or issue a c:errors document-->
            <p:identity>
                <p:input port="source">
                    <p:pipe port="error" step="catch"/>
                </p:input>
            </p:identity>
            <p:choose>
                <p:when test="$fail-on-error = 'true'">
                    <p:variable name="code" select="/c:errors/c:error[1]/@code"/>
                    <p:error xmlns:err="http://www.w3.org/ns/xproc-error">
                        <p:with-option name="code" select="if ($code) then $code else 'err:XD0030'"/>
                        <p:input port="source">
                            <p:pipe port="error" step="catch"/>
                        </p:input>
                    </p:error>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:catch>
    </p:try>

</p:declare-step>
