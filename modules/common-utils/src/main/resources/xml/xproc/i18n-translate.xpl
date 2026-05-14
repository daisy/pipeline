<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:i18n-translate" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>This step invokes the <code>pf:i18n-translate</code> function (implemented in i18n-translate.xsl) with its options as arguments and returns the result as a <code>c:result</code>
            document.</p>
    </p:documentation>

    <p:option name="string" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The string to look up in the translation map.</p>
        </p:documentation>
    </p:option>

    <p:option name="language" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The preferred language (RFC5646). For instance "en" or "en-US".</p>
        </p:documentation>
    </p:option>

    <p:input port="maps" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The i18n XML documents.</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>c:result</code> document whose content is the translation. Will be empty if no translation was found.</p>
        </p:documentation>
    </p:output>

    <cx:import href="../xslt/i18n.xsl" type="application/xslt+xml">
        <p:documentation>
            pf:i18n-translate
        </p:documentation>
    </cx:import>

    <p:sink/>
    <p:template>
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="template">
            <p:inline>
                <c:result>{$translation}</c:result>
            </p:inline>
        </p:input>
        <p:with-param name="translation" select="pf:i18n-translate($string, $language, collection()/*)">
            <p:pipe step="main" port="maps"/>
        </p:with-param>
    </p:template>

</p:declare-step>
