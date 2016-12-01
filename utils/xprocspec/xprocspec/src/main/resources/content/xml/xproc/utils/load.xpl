<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" name="main" type="pxi:load" xmlns:cx="http://xmlcalabash.com/ns/extensions" version="1.0" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/">

    <p:output port="result"/>

    <p:option name="href" required="true"/>
    <p:option name="method" select="'xml'"/>
    <p:option name="logfile" select="''"/>

    <p:import href="logging-library.xpl"/>
    <p:import href="html-load.xpl"/>

    <p:declare-step type="pxi:load-text">
        <p:output port="result"/>
        <p:option name="href"/>
        <p:identity>
            <p:input port="source">
                <p:inline>
                    <c:request method="GET" override-content-type="text/plain; charset=utf-8"/>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute match="c:request" attribute-name="href">
            <p:with-option name="attribute-value" select="$href"/>
        </p:add-attribute>
        <p:http-request/>
        <p:add-attribute match="/*" attribute-name="xml:space" attribute-value="preserve"/>
    </p:declare-step>

    <p:declare-step type="pxi:load-binary">
        <p:output port="result"/>
        <p:option name="href"/>
        <p:identity>
            <p:input port="source">
                <p:inline>
                    <c:request method="GET" override-content-type="binary/octet-stream"/>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute match="c:request" attribute-name="href">
            <p:with-option name="attribute-value" select="$href"/>
        </p:add-attribute>
        <p:http-request/>
    </p:declare-step>

    <pxi:message>
        <p:input port="source">
            <p:inline>
                <doc/>
            </p:inline>
        </p:input>
        <p:with-option name="message" select="concat('loading &quot;',$href,'&quot;')">
            <p:empty/>
        </p:with-option>
        <p:with-option name="logfile" select="$logfile">
            <p:empty/>
        </p:with-option>
    </pxi:message>
    <p:sink/>

    <p:choose>
        <!-- Force HTML -->
        <p:when test="$method='html'">
            <pxi:html-load>
                <p:with-option name="href" select="$href"/>
            </pxi:html-load>
        </p:when>

        <!-- XML -->
        <p:when test="$method='xml'">
            <p:try>
                <p:group>
                    <p:load>
                        <p:with-option name="href" select="$href"/>
                    </p:load>
                </p:group>
                <p:catch>
                    <pxi:message>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                        <p:with-option name="message" select="concat('unable to load ',$href,' as XML')"/>
                        <p:with-option name="logfile" select="$logfile">
                            <p:empty/>
                        </p:with-option>
                    </pxi:message>
                </p:catch>
            </p:try>
        </p:when>

        <!-- text -->
        <p:when test="$method='text'">
            <pxi:load-text>
                <p:with-option name="href" select="$href"/>
            </pxi:load-text>
        </p:when>

        <!-- HTML -->
        <p:when test="matches(lower-case($href),'\.x?html?$')">
            <pxi:html-load>
                <p:with-option name="href" select="$href"/>
            </pxi:html-load>
        </p:when>

        <!-- default to 'binary' -->
        <p:otherwise>
            <pxi:load-binary>
                <p:with-option name="href" select="$href"/>
            </pxi:load-binary>
        </p:otherwise>
    </p:choose>

</p:declare-step>
