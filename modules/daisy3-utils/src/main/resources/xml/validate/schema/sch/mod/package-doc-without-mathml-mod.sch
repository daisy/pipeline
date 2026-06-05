<!--
        Math-specific metadata must not appear in OPF files for books with no math content.
    -->
<pattern id="no-math-tests" xmlns="http://purl.oclc.org/dsdl/schematron">
    <rule context="//pkg:package/pkg:metadata/pkg:x-metadata">
        <assert test="count(pkg:meta[@name='z39-86-extension-version']) = 0"> 
            x-metadata element with name 'z39-86-extension-version' must not be present.
        </assert>
        <assert test="count(pkg:meta[@name='DTBook-XSLTFallback']) = 0">
            x-metadata element with name 'DTBook-XSLTFallback' must not be present.
        </assert>
    </rule>
</pattern>