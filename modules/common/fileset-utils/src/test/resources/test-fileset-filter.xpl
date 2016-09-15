<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:test-fileset-filter" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="#all">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="../../main/resources/xml/xproc/fileset-filter.xpl"/>
    <p:import href="compare.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

    <p:wrap-sequence wrapper="c:results">
        <p:input port="source">
            <p:pipe port="result" step="test-empty"/>
            <p:pipe port="result" step="test-href"/>
            <p:pipe port="result" step="test-href-that-does-not-exist"/>
            <p:pipe port="result" step="test-href-that-does-not-exist-relative"/>
            <p:pipe port="result" step="test-media-types-single"/>
            <p:pipe port="result" step="test-media-types-multiple"/>
            <p:pipe port="result" step="test-not-media-types-single"/>
            <p:pipe port="result" step="test-not-media-types-multiple"/>
        </p:input>
    </p:wrap-sequence>
    <p:add-attribute match="/*" attribute-name="script-uri">
        <p:with-option name="attribute-value" select="base-uri(/*)">
            <p:inline>
                <doc/>
            </p:inline>
        </p:with-option>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="name">
        <p:with-option name="attribute-value" select="replace(replace(/*/@script-uri,'^.*/([^/]+)$','$1'),'\.xpl$','')"/>
    </p:add-attribute>
    <p:identity name="result"/>

    <p:group name="test-fileset">
        <p:output port="result"/>
        <p:identity>
            <p:input port="source">
                <p:inline>
                    <d:fileset xmlns:d="http://www.daisy.org/ns/pipeline/data" xml:base="file:/tmp/">
                        <d:file media-type="application/xhtml+xml" href="application/xhtml+xml"/>
                        <d:file media-type="application/smil+xml" href="application/smil+xml"/>
                        <d:file media-type="audio/mpeg" href="audio/mpeg"/>
                        <d:file media-type="application/epub+zip" href="application/epub+zip"/>
                        <d:file media-type="application/xproc+xml" href="application/xproc+xml"/>
                        <d:file media-type="application/xslt+xml" href="application/xslt+xml"/>
                        <d:file media-type="application/xquery+xml" href="application/xquery+xml"/>
                        <d:file media-type="application/x-font-opentype" href="application/x-font-opentype"/>
                        <d:file media-type="audio/x-wav" href="audio/x-wav"/>
                        <d:file media-type="application/oebps-package+xml" href="application/oebps-package+xml"/>
                        <d:file media-type="application/x-dtbncx+xml" href="application/x-dtbncx+xml"/>
                        <d:file media-type="audio/mpeg4-generic" href="audio/mpeg4-generic"/>
                        <d:file media-type="image/jpeg" href="image/jpeg"/>
                        <d:file media-type="image/png" href="image/png"/>
                        <d:file media-type="image/svg+xml" href="image/svg+xml"/>
                        <d:file media-type="text/css" href="text/css"/>
                        <d:file media-type="application/xml-dtd" href="application/xml-dtd"/>
                        <d:file media-type="application/x-dtbresource+xml" href="application/x-dtbresource+xml"/>
                        <d:file media-type="audio/ogg" href="audio/ogg"/>
                        <d:file media-type="audio/basic" href="audio/basic"/>
                        <d:file media-type="audio/mid" href="audio/mid"/>
                        <d:file media-type="audio/x-aiff" href="audio/x-aiff"/>
                        <d:file media-type="audio/x-mpegurl" href="audio/x-mpegurl"/>
                        <d:file media-type="audio/x-pn-realaudio" href="audio/x-pn-realaudio"/>
                        <d:file media-type="image/bmp" href="image/bmp"/>
                        <d:file media-type="image/cis-cod" href="image/cis-cod"/>
                        <d:file media-type="image/gif" href="image/gif"/>
                        <d:file media-type="image/ief" href="image/ief"/>
                        <d:file media-type="image/pipeg" href="image/pipeg"/>
                        <d:file media-type="image/tiff" href="image/tiff"/>
                        <d:file media-type="image/x-cmu-raster" href="image/x-cmu-raster"/>
                        <d:file media-type="image/x-cmx" href="image/x-cmx"/>
                        <d:file media-type="image/x-icon" href="image/x-icon"/>
                        <d:file media-type="image/x-portable-anymap" href="image/x-portable-anymap"/>
                        <d:file media-type="image/x-portable-bitmap" href="image/x-portable-bitmap"/>
                        <d:file media-type="image/x-portable-graymap" href="image/x-portable-graymap"/>
                        <d:file media-type="image/x-portable-pixmap" href="image/x-portable-pixmap"/>
                        <d:file media-type="image/x-rgb" href="image/x-rgb"/>
                        <d:file media-type="image/x-xbitmap" href="image/x-xbitmap"/>
                        <d:file media-type="image/x-xpixmap" href="image/x-xpixmap"/>
                        <d:file media-type="image/x-xwindowdump" href="image/x-xwindowdump"/>
                        <d:file media-type="video/mpeg" href="video/mpeg"/>
                        <d:file media-type="video/quicktime" href="video/quicktime"/>
                        <d:file media-type="video/x-la-asf" href="video/x-la-asf"/>
                        <d:file media-type="video/x-ms-asf" href="video/x-ms-asf"/>
                        <d:file media-type="video/x-msvideo" href="video/x-msvideo"/>
                        <d:file media-type="video/x-sgi-movie" href="video/x-sgi-movie"/>
                        <d:file media-type="text/h323" href="text/h323"/>
                        <d:file media-type="text/html" href="text/html"/>
                        <d:file media-type="text/iuls" href="text/iuls"/>
                        <d:file media-type="text/plain" href="text/plain"/>
                        <d:file media-type="text/richtext" href="text/richtext"/>
                        <d:file media-type="text/scriptlet" href="text/scriptlet"/>
                        <d:file media-type="text/tab-separated-values" href="text/tab-separated-values"/>
                        <d:file media-type="text/webviewhtml" href="text/webviewhtml"/>
                        <d:file media-type="text/x-component" href="text/x-component"/>
                        <d:file media-type="text/x-setext" href="text/x-setext"/>
                        <d:file media-type="text/x-vcard" href="text/x-vcard"/>
                        <d:file media-type="message/rfc822" href="message/rfc822"/>
                        <d:file media-type="application/envoy" href="application/envoy"/>
                        <d:file media-type="application/fractals" href="application/fractals"/>
                        <d:file media-type="application/futuresplash" href="application/futuresplash"/>
                        <d:file media-type="application/hta" href="application/hta"/>
                        <d:file media-type="application/internet-property-stream" href="application/internet-property-stream"/>
                        <d:file media-type="application/mac-binhex40" href="application/mac-binhex40"/>
                        <d:file media-type="application/msword" href="application/msword"/>
                        <d:file media-type="application/octet-stream" href="application/octet-stream"/>
                        <d:file media-type="application/oda" href="application/oda"/>
                        <d:file media-type="application/olescript" href="application/olescript"/>
                        <d:file media-type="application/pdf" href="application/pdf"/>
                        <d:file media-type="application/pics-rules" href="application/pics-rules"/>
                        <d:file media-type="application/pkcs10" href="application/pkcs10"/>
                        <d:file media-type="application/pkix-crl" href="application/pkix-crl"/>
                        <d:file media-type="application/postscript" href="application/postscript"/>
                        <d:file media-type="application/rtf" href="application/rtf"/>
                        <d:file media-type="application/set-payment-initiation" href="application/set-payment-initiation"/>
                        <d:file media-type="application/set-registration-initiation" href="application/set-registration-initiation"/>
                        <d:file media-type="application/vnd.ms-excel" href="application/vnd.ms-excel"/>
                        <d:file media-type="application/vnd.ms-outlook" href="application/vnd.ms-outlook"/>
                        <d:file media-type="application/vnd.ms-pkicertstore" href="application/vnd.ms-pkicertstore"/>
                        <d:file media-type="application/vnd.ms-pkiseccat" href="application/vnd.ms-pkiseccat"/>
                        <d:file media-type="application/vnd.ms-pkistl" href="application/vnd.ms-pkistl"/>
                        <d:file media-type="application/vnd.ms-powerpoint" href="application/vnd.ms-powerpoint"/>
                        <d:file media-type="application/vnd.ms-project" href="application/vnd.ms-project"/>
                        <d:file media-type="application/vnd.ms-works" href="application/vnd.ms-works"/>
                        <d:file media-type="application/winhlp" href="application/winhlp"/>
                        <d:file media-type="application/x-bcpio" href="application/x-bcpio"/>
                        <d:file media-type="application/x-cdf" href="application/x-cdf"/>
                        <d:file media-type="application/x-compress" href="application/x-compress"/>
                        <d:file media-type="application/x-compressed" href="application/x-compressed"/>
                        <d:file media-type="application/x-cpio" href="application/x-cpio"/>
                        <d:file media-type="application/x-csh" href="application/x-csh"/>
                        <d:file media-type="application/x-director" href="application/x-director"/>
                        <d:file media-type="application/x-dvi" href="application/x-dvi"/>
                        <d:file media-type="application/x-gtar" href="application/x-gtar"/>
                        <d:file media-type="application/x-gzip" href="application/x-gzip"/>
                        <d:file media-type="application/x-hdf" href="application/x-hdf"/>
                        <d:file media-type="application/x-internet-signup" href="application/x-internet-signup"/>
                        <d:file media-type="application/x-iphone" href="application/x-iphone"/>
                        <d:file media-type="application/x-javascript" href="application/x-javascript"/>
                        <d:file media-type="application/x-latex" href="application/x-latex"/>
                        <d:file media-type="application/x-msaccess" href="application/x-msaccess"/>
                        <d:file media-type="application/x-mscardfile" href="application/x-mscardfile"/>
                        <d:file media-type="application/x-msclip" href="application/x-msclip"/>
                        <d:file media-type="application/x-msdownload" href="application/x-msdownload"/>
                        <d:file media-type="application/x-msmediaview" href="application/x-msmediaview"/>
                        <d:file media-type="application/x-msmetafile" href="application/x-msmetafile"/>
                        <d:file media-type="application/x-msmoney" href="application/x-msmoney"/>
                        <d:file media-type="application/x-mspublisher" href="application/x-mspublisher"/>
                        <d:file media-type="application/x-msschedule" href="application/x-msschedule"/>
                        <d:file media-type="application/x-msterminal" href="application/x-msterminal"/>
                        <d:file media-type="application/x-mswrite" href="application/x-mswrite"/>
                        <d:file media-type="application/x-netcdf" href="application/x-netcdf"/>
                        <d:file media-type="application/x-perfmon" href="application/x-perfmon"/>
                        <d:file media-type="application/x-pkcs12" href="application/x-pkcs12"/>
                        <d:file media-type="application/x-pkcs7-certificates" href="application/x-pkcs7-certificates"/>
                        <d:file media-type="application/x-pkcs7-certreqresp" href="application/x-pkcs7-certreqresp"/>
                        <d:file media-type="application/x-pkcs7-mime" href="application/x-pkcs7-mime"/>
                        <d:file media-type="application/x-pkcs7-signature" href="application/x-pkcs7-signature"/>
                        <d:file media-type="application/x-sh" href="application/x-sh"/>
                        <d:file media-type="application/x-shar" href="application/x-shar"/>
                        <d:file media-type="application/x-shockwave-flash" href="application/x-shockwave-flash"/>
                        <d:file media-type="application/x-stuffit" href="application/x-stuffit"/>
                        <d:file media-type="application/x-sv4cpio" href="application/x-sv4cpio"/>
                        <d:file media-type="application/x-sv4crc" href="application/x-sv4crc"/>
                        <d:file media-type="application/x-tar" href="application/x-tar"/>
                        <d:file media-type="application/x-tcl" href="application/x-tcl"/>
                        <d:file media-type="application/x-tex" href="application/x-tex"/>
                        <d:file media-type="application/x-texinfo" href="application/x-texinfo"/>
                        <d:file media-type="application/x-troff" href="application/x-troff"/>
                        <d:file media-type="application/x-troff-man" href="application/x-troff-man"/>
                        <d:file media-type="application/x-troff-me" href="application/x-troff-me"/>
                        <d:file media-type="application/x-troff-ms" href="application/x-troff-ms"/>
                        <d:file media-type="application/x-ustar" href="application/x-ustar"/>
                        <d:file media-type="application/x-wais-source" href="application/x-wais-source"/>
                        <d:file media-type="application/x-x509-ca-cert" href="application/x-x509-ca-cert"/>
                        <d:file media-type="application/ynd.ms-pkipko" href="application/ynd.ms-pkipko"/>
                        <d:file media-type="application/zip" href="application/zip"/>
                        <d:file media-type="x-world/x-vrml" href="x-world/x-vrml"/>
                        <d:file media-type="application/xml" href="application/xml"/>
                        <d:file media-type="application/x-dtbook+xml" href="application/x-dtbook+xml"/>
                        <d:file media-type="application/z3998-auth+xml" href="application/z3998-auth+xml"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </p:identity>
    </p:group>
    <p:sink/>

    <p:group name="test-empty">
        <p:output port="result"/>

        <px:fileset-filter>
            <p:input port="source">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
        </px:fileset-filter>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-empty"/>
    </p:group>

    <p:group name="test-href">
        <p:output port="result"/>

        <px:fileset-filter href="file:/tmp/application/xhtml+xml">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/">
    <d:file media-type="application/xhtml+xml" href="application/xhtml+xml"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-href"/>
    </p:group>

    <p:group name="test-href-that-does-not-exist">
        <p:output port="result"/>
        
        <px:fileset-filter href="file:/file/that/does/not/exist">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>
        
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/"/>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-href-that-does-not-exist"/>
    </p:group>
    
    <p:group name="test-href-that-does-not-exist-relative">
        <p:output port="result"/>
        
        <px:fileset-filter href="file/that/does/not/exist">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>
        
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/"/>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-href-that-does-not-exist-relative"/>
    </p:group>

    <p:group name="test-media-types-single">
        <p:output port="result"/>

        <px:fileset-filter media-types="*+xml">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/">
    <d:file media-type="application/xhtml+xml" href="application/xhtml+xml"/>
    <d:file media-type="application/smil+xml" href="application/smil+xml"/>
    <d:file media-type="application/xproc+xml" href="application/xproc+xml"/>
    <d:file media-type="application/xslt+xml" href="application/xslt+xml"/>
    <d:file media-type="application/xquery+xml" href="application/xquery+xml"/>
    <d:file media-type="application/oebps-package+xml" href="application/oebps-package+xml"/>
    <d:file media-type="application/x-dtbncx+xml" href="application/x-dtbncx+xml"/>
    <d:file media-type="image/svg+xml" href="image/svg+xml"/>
    <d:file media-type="application/x-dtbresource+xml" href="application/x-dtbresource+xml"/>
    <d:file media-type="application/x-dtbook+xml" href="application/x-dtbook+xml"/>
    <d:file media-type="application/z3998-auth+xml" href="application/z3998-auth+xml"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-media-types-single"/>
    </p:group>

    <p:group name="test-media-types-multiple">
        <p:output port="result"/>

        <px:fileset-filter media-types="*+xml text/html">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/">
    <d:file media-type="application/xhtml+xml" href="application/xhtml+xml"/>
    <d:file media-type="application/smil+xml" href="application/smil+xml"/>
    <d:file media-type="application/xproc+xml" href="application/xproc+xml"/>
    <d:file media-type="application/xslt+xml" href="application/xslt+xml"/>
    <d:file media-type="application/xquery+xml" href="application/xquery+xml"/>
    <d:file media-type="application/oebps-package+xml" href="application/oebps-package+xml"/>
    <d:file media-type="application/x-dtbncx+xml" href="application/x-dtbncx+xml"/>
    <d:file media-type="image/svg+xml" href="image/svg+xml"/>
    <d:file media-type="application/x-dtbresource+xml" href="application/x-dtbresource+xml"/>
    <d:file media-type="text/html" href="text/html"/>
    <d:file media-type="application/x-dtbook+xml" href="application/x-dtbook+xml"/>
    <d:file media-type="application/z3998-auth+xml" href="application/z3998-auth+xml"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-media-types-multiple"/>
    </p:group>

    <p:group name="test-not-media-types-single">
        <p:output port="result"/>

        <px:fileset-filter not-media-types="application/*">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/">
    <d:file media-type="audio/mpeg" href="audio/mpeg"/>
    <d:file media-type="audio/x-wav" href="audio/x-wav"/>
    <d:file media-type="audio/mpeg4-generic" href="audio/mpeg4-generic"/>
    <d:file media-type="image/jpeg" href="image/jpeg"/>
    <d:file media-type="image/png" href="image/png"/>
    <d:file media-type="image/svg+xml" href="image/svg+xml"/>
    <d:file media-type="text/css" href="text/css"/>
    <d:file media-type="audio/ogg" href="audio/ogg"/>
    <d:file media-type="audio/basic" href="audio/basic"/>
    <d:file media-type="audio/mid" href="audio/mid"/>
    <d:file media-type="audio/x-aiff" href="audio/x-aiff"/>
    <d:file media-type="audio/x-mpegurl" href="audio/x-mpegurl"/>
    <d:file media-type="audio/x-pn-realaudio" href="audio/x-pn-realaudio"/>
    <d:file media-type="image/bmp" href="image/bmp"/>
    <d:file media-type="image/cis-cod" href="image/cis-cod"/>
    <d:file media-type="image/gif" href="image/gif"/>
    <d:file media-type="image/ief" href="image/ief"/>
    <d:file media-type="image/pipeg" href="image/pipeg"/>
    <d:file media-type="image/tiff" href="image/tiff"/>
    <d:file media-type="image/x-cmu-raster" href="image/x-cmu-raster"/>
    <d:file media-type="image/x-cmx" href="image/x-cmx"/>
    <d:file media-type="image/x-icon" href="image/x-icon"/>
    <d:file media-type="image/x-portable-anymap" href="image/x-portable-anymap"/>
    <d:file media-type="image/x-portable-bitmap" href="image/x-portable-bitmap"/>
    <d:file media-type="image/x-portable-graymap" href="image/x-portable-graymap"/>
    <d:file media-type="image/x-portable-pixmap" href="image/x-portable-pixmap"/>
    <d:file media-type="image/x-rgb" href="image/x-rgb"/>
    <d:file media-type="image/x-xbitmap" href="image/x-xbitmap"/>
    <d:file media-type="image/x-xpixmap" href="image/x-xpixmap"/>
    <d:file media-type="image/x-xwindowdump" href="image/x-xwindowdump"/>
    <d:file media-type="video/mpeg" href="video/mpeg"/>
    <d:file media-type="video/quicktime" href="video/quicktime"/>
    <d:file media-type="video/x-la-asf" href="video/x-la-asf"/>
    <d:file media-type="video/x-ms-asf" href="video/x-ms-asf"/>
    <d:file media-type="video/x-msvideo" href="video/x-msvideo"/>
    <d:file media-type="video/x-sgi-movie" href="video/x-sgi-movie"/>
    <d:file media-type="text/h323" href="text/h323"/>
    <d:file media-type="text/html" href="text/html"/>
    <d:file media-type="text/iuls" href="text/iuls"/>
    <d:file media-type="text/plain" href="text/plain"/>
    <d:file media-type="text/richtext" href="text/richtext"/>
    <d:file media-type="text/scriptlet" href="text/scriptlet"/>
    <d:file media-type="text/tab-separated-values" href="text/tab-separated-values"/>
    <d:file media-type="text/webviewhtml" href="text/webviewhtml"/>
    <d:file media-type="text/x-component" href="text/x-component"/>
    <d:file media-type="text/x-setext" href="text/x-setext"/>
    <d:file media-type="text/x-vcard" href="text/x-vcard"/>
    <d:file media-type="message/rfc822" href="message/rfc822"/>
    <d:file media-type="x-world/x-vrml" href="x-world/x-vrml"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-not-media-types-single"/>
    </p:group>

    <p:group name="test-not-media-types-multiple">
        <p:output port="result"/>

        <px:fileset-filter not-media-types="application/* text/*">
            <p:input port="source">
                <p:pipe port="result" step="test-fileset"/>
            </p:input>
        </px:fileset-filter>

        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline xml:space="preserve">
<d:fileset xml:base="file:/tmp/">
    <d:file media-type="audio/mpeg" href="audio/mpeg"/>
    <d:file media-type="audio/x-wav" href="audio/x-wav"/>
    <d:file media-type="audio/mpeg4-generic" href="audio/mpeg4-generic"/>
    <d:file media-type="image/jpeg" href="image/jpeg"/>
    <d:file media-type="image/png" href="image/png"/>
    <d:file media-type="image/svg+xml" href="image/svg+xml"/>
    <d:file media-type="audio/ogg" href="audio/ogg"/>
    <d:file media-type="audio/basic" href="audio/basic"/>
    <d:file media-type="audio/mid" href="audio/mid"/>
    <d:file media-type="audio/x-aiff" href="audio/x-aiff"/>
    <d:file media-type="audio/x-mpegurl" href="audio/x-mpegurl"/>
    <d:file media-type="audio/x-pn-realaudio" href="audio/x-pn-realaudio"/>
    <d:file media-type="image/bmp" href="image/bmp"/>
    <d:file media-type="image/cis-cod" href="image/cis-cod"/>
    <d:file media-type="image/gif" href="image/gif"/>
    <d:file media-type="image/ief" href="image/ief"/>
    <d:file media-type="image/pipeg" href="image/pipeg"/>
    <d:file media-type="image/tiff" href="image/tiff"/>
    <d:file media-type="image/x-cmu-raster" href="image/x-cmu-raster"/>
    <d:file media-type="image/x-cmx" href="image/x-cmx"/>
    <d:file media-type="image/x-icon" href="image/x-icon"/>
    <d:file media-type="image/x-portable-anymap" href="image/x-portable-anymap"/>
    <d:file media-type="image/x-portable-bitmap" href="image/x-portable-bitmap"/>
    <d:file media-type="image/x-portable-graymap" href="image/x-portable-graymap"/>
    <d:file media-type="image/x-portable-pixmap" href="image/x-portable-pixmap"/>
    <d:file media-type="image/x-rgb" href="image/x-rgb"/>
    <d:file media-type="image/x-xbitmap" href="image/x-xbitmap"/>
    <d:file media-type="image/x-xpixmap" href="image/x-xpixmap"/>
    <d:file media-type="image/x-xwindowdump" href="image/x-xwindowdump"/>
    <d:file media-type="video/mpeg" href="video/mpeg"/>
    <d:file media-type="video/quicktime" href="video/quicktime"/>
    <d:file media-type="video/x-la-asf" href="video/x-la-asf"/>
    <d:file media-type="video/x-ms-asf" href="video/x-ms-asf"/>
    <d:file media-type="video/x-msvideo" href="video/x-msvideo"/>
    <d:file media-type="video/x-sgi-movie" href="video/x-sgi-movie"/>
    <d:file media-type="message/rfc822" href="message/rfc822"/>
    <d:file media-type="x-world/x-vrml" href="x-world/x-vrml"/>
</d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <p:add-attribute match="/*" attribute-name="name" attribute-value="filter-not-media-types-multiple"/>
    </p:group>

</p:declare-step>
