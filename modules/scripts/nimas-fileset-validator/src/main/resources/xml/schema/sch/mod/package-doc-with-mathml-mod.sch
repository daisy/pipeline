<!--
        Math-specific metadata must appear in OPF files for books with math content.
        
        <meta name="z39-86-extension-version"
          scheme="http://www.w3.org/1998/Math/MathML"
          content="1.0" />
        <meta name="DTBook-XSLTFallback"
          scheme="http://www.w3.org/1998/Math/MathML"
          content="xslt-file-name" />
    -->
<pattern id="mathml-tests" xmlns="http://purl.oclc.org/dsdl/schematron">
    <rule context="//pkg:package/pkg:metadata/pkg:x-metadata">
        <report test="count(pkg:meta[@name='z39-86-extension-version']) = 0"> 
            x-metadata element with name 'z39-86-extension-version' must be present.
        </report>
        <report test="count(pkg:meta[@name='z39-86-extension-version']) > 1"> 
            x-metadata element with name 'z39-86-extension-version' must occur only once.
        </report>
        <report test="count(pkg:meta[@name='DTBook-XSLTFallback']) = 0"> 
            x-metadata element with name 'DTBook-XSLTFallback' must be present.
        </report>
        <report test="count(pkg:meta[@name='DTBook-XSLTFallback']) > 1"> 
            x-metadata element with name 'DTBook-XSLTFallback' must occur only once.
        </report>
    </rule>
    <rule context="//pkg:package/pkg:metadata/pkg:x-metadata/pkg:meta[@name='z39-86-extension-version']">
        <assert test="@content = '1.0'">
            x-metadata element with name 'z39-86-extension-version' must have content attribute value equal to '1.0'.
        </assert>
        <assert test="@scheme = 'http://www.w3.org/1998/Math/MathML'">
            x-metadata element with name 'z39-86-extension-version' must have scheme attribute value equal to 'http://www.w3.org/1998/Math/MathML'.
        </assert>
    </rule>
    <rule context="//pkg:package/pkg:metadata/pkg:x-metadata/pkg:meta[@name='DTBook-XSLTFallback']">
        <assert test="@scheme = 'http://www.w3.org/1998/Math/MathML'">
            x-metadata element with name 'DTBook-XSLTFallback' must have scheme attribute value equal to 'http://www.w3.org/1998/Math/MathML'.
        </assert>
        <assert test="string-length(@content) > 0">
            x-metadata element with name 'DTBook-XSLTFallback' must have non-empty content attribute value.
        </assert>
    </rule>
    <!--
            <item href="mathml-fallback-transform.xslt"
              id="XSLT_0"
              media-type="application/xslt+xml" />
      -->
    <let name="xslt-fallback" value="//pkg:package/pkg:metadata/pkg:x-metadata/pkg:meta[@name='DTBook-XSLTFallback']/@content"/> 
    <rule context="//pkg:package/pkg:manifest">
        <assert test="pkg:item[@href = $xslt-fallback]">
            Manifest must contain an item element where the href attribute is equal to the DTBook-XSLTFallback metadata content attribute.
        </assert>
    </rule>
    <rule context="//pkg:package/pkg:manifest/pkg:item[@href = $xslt-fallback]">
        <assert test="@media-type = 'application/xslt+xml'">
            XSLT fallback manifest item must have media-type equal to 'application/xslt+xml'.
        </assert>
    </rule>
</pattern>