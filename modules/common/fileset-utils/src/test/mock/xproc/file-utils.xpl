<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:c="http://www.w3.org/ns/xproc-step">

    <p:declare-step type="px:copy">
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
        <p:option name="target" required="true"/>
        <p:option name="fail-on-error" select="'true'"/>
    </p:declare-step>

    <p:declare-step type="px:delete">
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
    </p:declare-step>

    <p:declare-step type="px:info">
        <p:output port="result" sequence="true"/>
        <p:option name="href" required="true"/>
    </p:declare-step>

    <p:declare-step type="px:head">
        <p:output port="result"/>
        <p:option name="href" required="true"/>
        <p:option name="count" required="true"/>
        <p:option name="fail-on-error" select="'true'"/>
    </p:declare-step>

    <p:declare-step type="px:mkdir">
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
    </p:declare-step>

    <p:declare-step type="px:move">
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
        <p:option name="target" required="true"/>
    </p:declare-step>

    <p:declare-step type="px:tail">
        <p:output port="result"/>
        <p:option name="href" required="true"/>
        <p:option name="count" required="true"/>
        <p:option name="fail-on-error" select="'true'"/>
    </p:declare-step>

    <p:declare-step type="px:tempfile">
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
        <p:option name="delete-on-exit"/>
        <p:option name="suffix"/>
    </p:declare-step>

    <p:declare-step type="px:touch">
        <p:output port="result" primary="false"/>
        <p:option name="href" required="true"/>
    </p:declare-step>

    <p:declare-step type="px:cwd">
        <p:output port="result" sequence="true"/>
    </p:declare-step>

    <p:declare-step type="px:set-doctype">
        <p:output port="result"/>
        <p:option name="href" required="true"/>
        <p:option name="doctype" required="true"/>
        <p:option name="encoding" select="'utf-8'"/>
        <p:string-replace match="/*/text()">
            <p:with-option name="replace" select="concat('''',$href,'''')"/>
            <p:input port="source">
                <p:inline>
                    <c:result>REPLACEME</c:result>
                </p:inline>
            </p:input>
        </p:string-replace>
        <p:load>
            <p:with-option name="href" select="$href"/>
        </p:load>
    </p:declare-step>

    <p:declare-step version="1.0" type="px:directory-list">
        <p:output port="result"/>
        <p:option name="path" required="true"/>
        <p:option name="include-filter"/>
        <p:option name="exclude-filter"/>
        <p:option name="depth" select="-1"/>
        <p:directory-list>
            <p:with-option name="path" select="$path"/>
        </p:directory-list>
    </p:declare-step>

</p:library>
