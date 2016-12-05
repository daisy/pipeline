<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0" exclude-inline-prefixes="#all" type="pxi:log" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/"
    xmlns:cx="http://xmlcalabash.com/ns/extensions">

    <p:input port="source" sequence="true"/>
    <p:output port="result" sequence="true"/>

    <p:option name="logfile" select="''"/>
    <p:option name="message" required="true"/>
    <p:option name="severity" select="'INFO'"/>

    <!-- the log is loaded and stored for each log message -->
    <!-- this will certainly be slow if there's a lot of log statements; use with care! -->
    <!-- I would love to find a way to append to a file without loading it (something like "echo 'message' >> 'logfile'") -->

    <p:choose>
        <p:xpath-context>
            <p:empty/>
        </p:xpath-context>
        <p:when test="$logfile">
            <p:try>
                <p:group>
                    <p:load>
                        <p:with-option name="href" select="$logfile">
                            <p:empty/>
                        </p:with-option>
                    </p:load>
                </p:group>
                <p:catch name="catch">
                    <p:identity>
                        <p:input port="source">
                            <p:inline>
                                <c:log/>
                            </p:inline>
                        </p:input>
                    </p:identity>
                </p:catch>
            </p:try>

            <p:insert match="/*" position="last-child">
                <p:input port="insertion">
                    <p:inline>
                        <c:line> </c:line>
                    </p:inline>
                </p:input>
            </p:insert>
            <p:add-attribute match="/*/*[last()]" attribute-name="time">
                <p:with-option name="attribute-value" select="adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H'))"/>
            </p:add-attribute>
            <p:add-attribute match="/*/*[last()]" attribute-name="message">
                <p:with-option name="attribute-value" select="$message"/>
            </p:add-attribute>
            <p:string-replace match="/*/*[last()]/text()" replace="/*/*[last()]/@message"/>
            <p:delete match="/*/*[last()]/@message"/>
            <p:add-attribute match="/*/*[last()]" attribute-name="severity">
                <p:with-option name="attribute-value" select="if ($severity) then $severity else 'INFO'"/>
            </p:add-attribute>

            <p:store name="store">
                <p:with-option name="href" select="$logfile"/>
            </p:store>

            <!-- wait until store operation is complete before continuing -->
            <p:split-sequence initial-only="true" test="position()=1" name="split-sequence">
                <p:input port="source">
                    <p:pipe port="result" step="store"/>
                    <p:pipe port="source" step="main"/>
                </p:input>
            </p:split-sequence>
            <p:sink/>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="not-matched" step="split-sequence"/>
                </p:input>
            </p:identity>

        </p:when>
        <p:otherwise>
            <!-- no log file specified -->
            <p:identity/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
