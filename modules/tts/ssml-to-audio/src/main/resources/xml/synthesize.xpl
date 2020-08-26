<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" type="px:synthesize"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:input port="source" primary="true" sequence="true" px:media-type="application/ssml+xml"/>
    <p:input port="config"/>
    <p:output port="result" primary="true" />
    <p:output port="status">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>
                Status document expressing the success rate of the text-to-speech process. The
                format is an extension of the "<a
                href="http://daisy.github.io/pipeline/ValidationStatusXML">application/vnd.pipeline.status+xml</a>"
                format: a <code>d:status</code> element with a <code>result</code> attribute that
                has the value "ok" if there were no errors, or "error" when there was at least one
                error. A <code>success-rate</code> attribute contains the percentage of the SSML
                input that got successfully converted to speech.
            </p>
        </p:documentation>
    </p:output>
    <p:output port="log" sequence="true"/>
    <p:option name="temp-dir"/>

</p:declare-step>