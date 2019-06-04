<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  version="2.0">
  
  <xsl:import href="http://transpect.io/xslt-util/paths/xsl/paths.xsl"/>
  
  <!--  *
        * This function expects a file reference, cuts of the file extension and 
        * returns the mime-type. It's obvious that this method works only when 
        * the file extension corresponds to the real file type. 
        * -->
  
  <xsl:variable name="mime-types" as="element(mime)+">
    <mime type="application/epub+zip" ext="epub"/>
    <mime type="application/javascript" ext="js"/>
    <mime type="application/json" ext="json"/>
    <mime type="application/msword" ext="doc"/>
    <mime type="application/oebps-package+xml" ext="opf"/>
    <mime type="application/pdf" ext="pdf"/>
    <mime type="application/pls+xml" ext="pls"/>
    <mime type="application/smil+xml" ext="smil"/>
    <mime type="application/vnd.adobe-page-template+xml" ext="xpgt"/>
    <mime type="application/vnd.cinderella" ext="cdy"/>
    <mime type="application/vnd.ms-fontobject" ext="eot"/>
    <mime type="application/vnd.ms-opentype" ext="otf"/>
    <mime type="application/vnd.openxmlformats-officedocument.wordprocessingml.document" ext="docx"/>
    <mime type="application/vnd.openxmlformats-officedocument.wordprocessingml.template" ext="dotx"/>
    <mime type="application/vnd.ms-word.document.macroEnabled.12" ext="docm"/>
    <mime type="application/vnd.ms-word.template.macroEnabled.12" ext="dotm"/>
    <mime type="application/vnd.ms-excel" ext="xls"/>
    <mime type="application/vnd.ms-excel" ext="xlt"/>
    <mime type="application/vnd.ms-excel" ext="xla"/>
    <mime type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ext="xlsx"/>
    <mime type="application/vnd.openxmlformats-officedocument.spreadsheetml.template" ext="xltx"/>
    <mime type="application/vnd.ms-excel.sheet.macroEnabled.12" ext="xlsm"/>
    <mime type="application/vnd.ms-excel.template.macroEnabled.12" ext="xltm"/>
    <mime type="application/vnd.ms-excel.addin.macroEnabled.12" ext="xlam"/>
    <mime type="application/vnd.ms-excel.sheet.binary.macroEnabled.12" ext="xlsb"/>
    <mime type="application/vnd.ms-powerpoint" ext="ppt"/>
    <mime type="application/vnd.ms-powerpoint" ext="pot"/>
    <mime type="application/vnd.ms-powerpoint" ext="pps"/>
    <mime type="application/vnd.ms-powerpoint" ext="ppa"/>
    <mime type="application/vnd.openxmlformats-officedocument.presentationml.presentation" ext="pptx"/>
    <mime type="application/vnd.openxmlformats-officedocument.presentationml.template" ext="potx"/>
    <mime type="application/vnd.openxmlformats-officedocument.presentationml.slideshow" ext="ppsx"/>
    <mime type="application/vnd.ms-powerpoint.addin.macroEnabled.12" ext="ppam"/>
    <mime type="application/vnd.ms-powerpoint.presentation.macroEnabled.12" ext="pptm"/>
    <mime type="application/vnd.ms-powerpoint.template.macroEnabled.12" ext="potm"/>
    <mime type="application/vnd.ms-powerpoint.slideshow.macroEnabled.12" ext="ppsm"/>
    <mime type="application/vnd.ms-access" ext="mdb"/>
    <mime type="application/x-dtbncx+xml" ext="ncx"/>
    <mime type="application/xhtml+xml" ext="html xhtml"/>
    <mime type="application/rtf" ext="rtf"/>
    <mime type="application/zip" ext="zip"/>
    <mime type="audio/aac" ext="aac"/>
    <mime type="audio/mp4" ext="m4a"/>
    <mime type="audio/mpeg" ext="mp3"/>
    <mime type="audio/webm" ext="weba"/>
    <mime type="audio/x-ms-wma" ext="wma"/>
    <mime type="audio/wav" ext="wav"/>
    <mime type="font/ttf" ext="ttf"/>
    <mime type="font/woff" ext="woff"/>
    <mime type="font/woff" ext="woff2"/>
    <mime type="image/bmp" ext="bmp"/>
    <mime type="image/gif" ext="gif"/>
    <mime type="image/jpeg" ext="jpg jpeg"/>
    <mime type="image/png" ext="png"/>
    <mime type="image/svg+xml" ext="svg"/>
    <mime type="image/tiff" ext="tif tiff"/>
    <mime type="image/x-eps" ext="eps"/>
    <mime type="image/x-emf" ext="emf"/>
    <mime type="text/css" ext="css"/>
    <mime type="text/csv" ext="csv"/>
    <mime type="text/xml" ext="xml"/>
    <mime type="text/plain" ext="txt"/>
    <mime type="video/mp4" ext="mp4 mpg"/>
    <mime type="video/mpeg" ext="mpeg"/>
    <mime type="video/quicktime" ext="mov"/>
    <mime type="video/x-m4v" ext="m4v"/>
    <mime type="video/x-ms-wmv" ext="wmv"/>
    <mime type="video/x-msvideo" ext="avi"/>
    <mime type="video/webm" ext="webm"/>
  </xsl:variable>
  
  <xsl:function name="tr:fileref-to-mime-type" as="xs:string?">
    <xsl:param name="path" as="xs:string?"/>
    <xsl:if test="$path != ''">
      <xsl:sequence select="tr:ext-to-mime-type(lower-case(tr:ext($path)))"/>
    </xsl:if>
  </xsl:function>
  
  <xsl:function name="tr:fileext-to-mime-type" as="xs:string?">
    <!-- keep this function for compatibility -->
    <xsl:param name="path" as="xs:string"/>
    <xsl:sequence select="tr:fileref-to-mime-type(lower-case(tr:ext($path)))"/>
  </xsl:function>
  
  <xsl:function name="tr:ext-to-mime-type" as="xs:string?">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:variable name="temp" as="xs:string?"
      select="$mime-types[tokenize(@ext, '\s') = lower-case($ext)]/@type"/>
    <xsl:if test="empty($temp)">
      <xsl:message select="'xslt-util/mime-type/xsl/mime-type.xsl, tr:ext-to-mime-type(): Empty type for ', $ext"/>
    </xsl:if>
    <xsl:sequence select="$temp"/>
  </xsl:function>
  
  <xsl:function name="tr:mime-type-to-fileext" as="xs:string?">
    <xsl:param name="media-type" as="xs:string?"/>
    <xsl:sequence select="tokenize($mime-types[@type eq $media-type]/@ext, '\s')[1]"/>
  </xsl:function>
  
</xsl:stylesheet>
