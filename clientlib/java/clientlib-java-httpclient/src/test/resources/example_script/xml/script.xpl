<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="foo:script" version="1.0"
                xmlns:foo="http://www.daisy.org/pipeline/modules/foo/"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                px:input-filesets="daisy202 daisy3"
                px:output-filesets="epub2 epub3"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Example script</h1>
        <p px:role="desc">Transforms a Something into a Something.</p>
        <dl px:role="author maintainer">
            <dt>Name:</dt>
            <dd px:role="name">Example Example</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:example@example.net">example@example.net</a></dd>
            <dt>Organization:</dt>
            <dd px:role="organization">Example</dd>
        </dl>
        <p><a px:role="homepage" href="http://github.com/daisy">Online Documentation</a></p>
    </p:documentation>
    
    <p:input port="source" px:name="source" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Input port</h2>
            <p px:role="desc">Input port description.</p>
        </p:documentation>
    </p:input>
    
    <p:option name="option-1" required="true" px:type="foo:choice">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Enum</h2>
            <p px:role="desc">Enum description.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="option-2" required="false" px:type="foo:regex" select="'one'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Regex</h2>
            <p px:role="desc">Regex description.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="href" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
            <p px:role="desc">Input HTML.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Output</h2>
            <p px:role="desc">Output directory for the Something.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
            <p px:role="desc">Temporary directory for the Something files.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="yes-or-no" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Yes? No?</h2>
            <p px:role="desc">Whether or not to include or not include something that you may (or may not) want to include.</p>
        </p:documentation>
    </p:option>
    
    <p:wrap match="/*" wrapper="source" name="source"/>
    
    <p:add-attribute match="/*" attribute-name="value" name="option">
        <p:input port="source">
            <p:inline><option name="href"/></p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="$href"/>
    </p:add-attribute>
    
    <p:wrap-sequence wrapper="result">
        <p:input port="source">
            <p:pipe step="source" port="result"/>
            <p:pipe step="option" port="result"/>
        </p:input>
    </p:wrap-sequence>
    
    <p:store>
        <p:with-option name="href" select="resolve-uri('result.xml', $output-dir)"/>
    </p:store>
    
</p:declare-step>
