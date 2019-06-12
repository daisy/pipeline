<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-check-image-file-signatures" name="main" version="1.0">

    <p:input port="source">
        <p:documentation>Input fileset (only files with media-type="image/*" will be processed)</p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation>Output report (&lt;d:document-validation-report/&gt;)</p:documentation>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:for-each name="check-image-file-signatures.iterate-images">
        <p:iteration-source select="/*/d:file[starts-with(@media-type,'image/')]"/>
        <p:variable name="href" select="resolve-uri(/*/(@original-href,@href)[1], base-uri())"/>
        <p:variable name="type" select="/*/@media-type"/>

        <px:message severity="DEBUG" message="Checking file signature for image: $1">
            <p:with-option name="param1" select="replace($href,'.*/','')"/>
        </px:message>

        <px:file-peek offset="0" length="10" name="check-image-file-signatures.iterate-images.peek">
            <p:with-option name="href" select="$href"/>
        </px:file-peek>
        <p:group name="check-image-file-signatures.iterate-images.group">
            <p:variable name="expected-file-signature"
                select="if ($type = 'image/jpeg') then '0xFF 0xD8 0xFF 0xE0 0x?? 0x?? 0x4A 0x46 0x49 0x46'
                else if ($type = 'image/png')  then '0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A'
                else ''"/>
            <p:variable name="expected-file-signature-regex" select="replace(replace($expected-file-signature,' ?0x',''),'\?','.')"/>
            <p:variable name="actual-file-signature" select="substring(/*/text(),1,string-length($expected-file-signature-regex))"/>
            <p:variable name="actual-file-signature-pretty" select="normalize-space(replace($actual-file-signature,'(..)',' 0x$1'))"/>
            <p:variable name="matches" select="if ($expected-file-signature='') then true() else matches($actual-file-signature, concat('^',$expected-file-signature-regex))"/>

            <p:in-scope-names name="check-image-file-signatures.iterate-images.group.vars"/>
            <p:template name="check-image-file-signatures.iterate-images.group.template">
                <p:input port="source">
                    <p:pipe port="result" step="check-image-file-signatures.iterate-images.peek"/>
                </p:input>
                <p:input port="parameters">
                    <p:pipe port="result" step="check-image-file-signatures.iterate-images.group.vars"/>
                </p:input>
                <p:input port="template">
                    <p:inline exclude-inline-prefixes="#all">
                        <d:message severity="{if ($matches='true') then 'info' else 'error'}">
                            <d:desc>{if ($matches='true') then 'File signature is correct: ' else 'Incorrect file signature ("magic number") for image: '}{replace($href,'.*/','')}</d:desc>
                            <d:file>{replace($href,'.*/','')}</d:file>
                            <d:was>{$actual-file-signature-pretty}</d:was>
                            <d:expected>{$expected-file-signature}</d:expected>
                        </d:message>
                    </p:inline>
                </p:input>
            </p:template>
        </p:group>
    </p:for-each>
    <p:wrap-sequence wrapper="d:report" name="check-image-file-signatures.wrap-d-report"/>
    <p:add-attribute match="/*" attribute-name="type" attribute-value="filecheck" name="check-image-file-signatures.add-type-attribute"/>
    <p:wrap-sequence wrapper="d:reports" name="check-image-file-signatures.wrap-d-reports"/>
    <p:wrap-sequence wrapper="d:document-validation-report" name="check-image-file-signatures.wrap-document-validation-report"/>
    <p:insert match="/*" position="first-child" name="check-image-file-signatures.insert-name-type-error-count">
        <p:input port="insertion">
            <p:inline exclude-inline-prefixes="#all">
                <d:document-info>
                    <d:document-name>Checking file signatures for images</d:document-name>
                    <d:document-type>Nordic Image File Signatures</d:document-type>
                    <d:error-count>ERROR-COUNT</d:error-count>
                </d:document-info>
            </p:inline>
        </p:input>
    </p:insert>
    <p:string-replace match="/*/d:document-info/d:error-count/text()" name="check-image-file-signatures.string-replace-error-count">
        <p:with-option name="replace" select="concat('''',count(/*/d:reports/*/d:message[@severity='error']),'''')"/>
    </p:string-replace>

</p:declare-step>
