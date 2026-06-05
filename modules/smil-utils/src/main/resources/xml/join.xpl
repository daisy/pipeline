<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:mo="http://www.w3.org/ns/SMIL"
                type="px:mediaoverlay-join">

    <p:input port="source" sequence="true"/>
    <p:output port="result"/>

    <p:for-each>
        <p:add-xml-base all="true" relative="false"/>
        <p:xslt>
            <p:with-param name="id-prefix" select="concat('mo',p:iteration-position(),'_')"/>
            <p:input port="stylesheet">
                <p:document href="prepare-mo-for-join.xsl"/>
            </p:input>
        </p:xslt>
    </p:for-each>
    <p:wrap-sequence wrapper="body" wrapper-namespace="http://www.w3.org/ns/SMIL"/>
    <p:wrap-sequence wrapper="smil" wrapper-namespace="http://www.w3.org/ns/SMIL"/>
    <p:choose>
        <p:when test="/mo:smil/descendant::mo:smil/@id">
            <p:add-attribute attribute-name="id" match="/*">
                <p:with-option name="attribute-value" select="(/mo:smil/descendant::mo:smil/@id)[1]"
                />
            </p:add-attribute>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:add-attribute attribute-name="version" attribute-value="3.0" match="/*"/>
    <p:unwrap match="/*/*/*"/>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="join.xsl"/>
        </p:input>
    </p:xslt>
    <p:delete match="/*//*/@xml:base"/>

</p:declare-step>
