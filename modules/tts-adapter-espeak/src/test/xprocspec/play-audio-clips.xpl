<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="x:play-audio-clips" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Play the audio files in a d:audio-clips document. The step is made available as a (fake
        because passes always) XProcSpec assertion.</p>
    </p:documentation>
    
    <p:input port="context" primary="false"/>
    <p:input port="expect" primary="false"/>
    <p:input port="parameters" kind="parameter" primary="true"/>
    
    <p:output port="result" primary="true"/>
    
    <p:for-each name="play">
        <p:iteration-source select="/d:audio-clips/d:clip">
            <p:pipe step="main" port="context"/>
        </p:iteration-source>
        <p:choose>
            <p:when test="ends-with(/*/@src,'.mp3')">
                <p:exec command="mpg123" result-is-xml="false">
                    <p:with-option name="args" select="substring-after(/*/@src,'file:')"/>
                </p:exec>
            </p:when>
            <p:when test="ends-with(/*/@src,'.wav')">
                <p:exec command="afplay" result-is-xml="false">
                    <p:with-option name="args" select="substring-after(/*/@src,'file:')"/>
                </p:exec>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:sink/>
    
    <p:identity cx:depends-on="play">
        <p:input port="source">
            <p:inline>
                <x:test-result result="passed"/>
            </p:inline>
        </p:input>
    </p:identity>
    
</p:declare-step>
