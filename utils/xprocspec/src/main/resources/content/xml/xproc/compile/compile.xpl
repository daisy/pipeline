<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" type="pxi:test-compile" name="main" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/"
    version="1.0" xpath-version="2.0" xmlns:cxf="http://xmlcalabash.com/ns/extensions/fileutils" xmlns:x="http://www.daisy.org/ns/xprocspec">

    <p:input port="source" sequence="true"/>
    <p:output port="result" sequence="true" primary="true">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:option name="temp-dir" required="true"/>
    <p:option name="logfile" select="''"/>
    
    <p:option name="step-available-rng" select="'false'"/>
    
    <p:import href="../utils/logging-library.xpl"/>
    <p:import href="../utils/validate-with-relax-ng.xpl"/>
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" use-when="p:system-property('p:product-name') = 'XML Calabash'"/>
    
    <p:variable name="test-temp-dir" select="concat($temp-dir,'xprocspec-',replace(replace(concat(current-dateTime(),''),'\+.*',''),'[^\d]',''),'/')">
        <p:inline>
            <doc/>
        </p:inline>
    </p:variable>
    
    <cxf:mkdir fail-on-error="false" name="mkdir" p:use-when="p:system-property('p:product-name') = 'XML Calabash'">
        <p:with-option name="href" select="$test-temp-dir">
            <p:inline>
                <doc/>
            </p:inline>
        </p:with-option>
    </cxf:mkdir>
    <pxi:message message=" * created directory using Calabash's cxf:mkdir" cx:depends-on="mkdir" p:use-when="p:system-property('p:product-name') = 'XML Calabash'">
        <p:with-option name="logfile" select="$logfile">
            <p:empty/>
        </p:with-option>
    </pxi:message>
    <pxi:message message=" * using as temporary directory: $1">
        <p:with-option name="param1" select="$test-temp-dir"/>
        <p:with-option name="logfile" select="$logfile">
            <p:empty/>
        </p:with-option>
    </pxi:message>
    <p:sink/>

    <p:for-each cx:depends-on="mkdir">
        <!-- convert each x:description/scenario/x:call to an XProc script -->
        <p:iteration-source>
            <p:pipe port="source" step="main"/>
        </p:iteration-source>
        <p:choose>
            <p:when test="/*[self::c:errors]">
                <pxi:message message=" * error document; skipping">
                    <p:with-option name="logfile" select="$logfile">
                        <p:empty/>
                    </p:with-option>
                </pxi:message>
                <p:identity/>
            </p:when>
            <p:otherwise>
                <p:variable name="base" select="base-uri(/*)"/>

                <p:identity name="try.input"/>
                <p:try>
                    <p:group>
                        <p:variable name="test-name" select="concat('test',p:iteration-position())"/>
                        <pxi:message message=" * converting test '$1' to XProc">
                            <p:with-option name="param1" select="$test-name"/>
                            <p:with-option name="logfile" select="$logfile">
                                <p:empty/>
                            </p:with-option>
                        </pxi:message>
                        <p:add-attribute match="/*" attribute-name="temp-dir">
                            <p:with-option name="attribute-value" select="$test-temp-dir"/>
                        </p:add-attribute>
                        <p:xslt>
                            <p:with-param name="test-base-uri" select="$base"/>
                            <p:with-param name="name" select="$test-name"/>
                            <p:with-param name="temp-dir" select="$test-temp-dir"/>
                            <p:input port="stylesheet">
                                <p:document href="description-to-invocation.xsl"/>
                            </p:input>
                        </p:xslt>
                        <pxi:message message="   * done">
                            <p:with-option name="logfile" select="$logfile">
                                <p:empty/>
                            </p:with-option>
                        </pxi:message>
                    </p:group>
                    <p:catch name="catch">
                        <p:identity>
                            <p:input port="source">
                                <p:pipe port="error" step="catch"/>
                            </p:input>
                        </p:identity>
                        <p:add-attribute match="/*" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="$base"/>
                        </p:add-attribute>
                        <p:add-attribute match="/*" attribute-name="test-base-uri">
                            <p:with-option name="attribute-value" select="$base"/>
                        </p:add-attribute>
                        <p:add-attribute match="/*" attribute-name="error-location" attribute-value="compile.xpl - convert xprocspec to XProc"/>

                        <p:identity name="errors-without-was"/>
                        <p:wrap-sequence wrapper="x:was">
                            <p:input port="source">
                                <p:pipe port="result" step="try.input"/>
                            </p:input>
                        </p:wrap-sequence>
                        <p:add-attribute match="/*" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="base-uri(/*/*)"/>
                        </p:add-attribute>
                        <p:wrap-sequence wrapper="c:error"/>
                        <p:add-attribute match="/*" attribute-name="type" attribute-value="was"/>
                        <p:identity name="was"/>
                        <p:insert match="/*" position="last-child">
                            <p:input port="source">
                                <p:pipe port="result" step="errors-without-was"/>
                            </p:input>
                            <p:input port="insertion">
                                <p:pipe port="result" step="was"/>
                            </p:input>
                        </p:insert>
                    </p:catch>
                </p:try>

                <p:identity name="try.input.2"/>
                <p:try>
                    <p:group>
                        <!-- validate output grammar -->
                        <pxi:message message=" * validating output grammar for $1">
                            <p:with-option name="param1" select="$base"/>
                            <p:with-option name="logfile" select="$logfile">
                                <p:empty/>
                            </p:with-option>
                        </pxi:message>
                        <pxi:validate-with-relax-ng>
                            <p:input port="schema">
                                <p:document href="../../schema/xprocspec.compile.rng"/>
                            </p:input>
                            <p:with-option name="step-available" select="$step-available-rng">
                                <p:empty/>
                            </p:with-option>
                        </pxi:validate-with-relax-ng>
                        <pxi:message message="   * done!">
                            <p:with-option name="logfile" select="$logfile">
                                <p:empty/>
                            </p:with-option>
                        </pxi:message>
                    </p:group>
                    <p:catch name="catch">
                        <p:identity>
                            <p:input port="source">
                                <p:pipe port="error" step="catch"/>
                            </p:input>
                        </p:identity>
                        <p:add-attribute match="/*" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="$base"/>
                        </p:add-attribute>
                        <p:add-attribute match="/*" attribute-name="test-base-uri">
                            <p:with-option name="attribute-value" select="$base"/>
                        </p:add-attribute>
                        <p:add-attribute match="/*" attribute-name="error-location" attribute-value="compile.xpl - validate output grammar"/>

                        <p:identity name="errors-without-was"/>
                        <p:wrap-sequence wrapper="x:was">
                            <p:input port="source">
                                <p:pipe port="result" step="try.input.2"/>
                            </p:input>
                        </p:wrap-sequence>
                        <p:add-attribute match="/*" attribute-name="xml:base">
                            <p:with-option name="attribute-value" select="base-uri(/*/*)"/>
                        </p:add-attribute>
                        <p:wrap-sequence wrapper="c:error"/>
                        <p:add-attribute match="/*" attribute-name="type" attribute-value="was"/>
                        <p:identity name="was"/>
                        <p:insert match="/*" position="last-child">
                            <p:input port="source">
                                <p:pipe port="result" step="errors-without-was"/>
                            </p:input>
                            <p:input port="insertion">
                                <p:pipe port="result" step="was"/>
                            </p:input>
                        </p:insert>
                    </p:catch>
                </p:try>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:identity name="result"/>

</p:declare-step>
