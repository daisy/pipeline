<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml" version="2.0">
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="html:a">
        <xsl:choose>
            <xsl:when test="starts-with(@href,'file:') or starts-with(@href,'#') and substring(@href,2) = (@id | ancestor::*/@id | descendant::*/@id)">
                <xsl:element name="span" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:copy-of
                        select="@accesskey|@contenteditable|@contextmenu|@dir|@draggable|@dropzone|@hidden|@id|@lang|@spellcheck|@style|@tabindex|@title|@translate|@onabort|@onblur|@oncanplay|@oncanplaythrough|@onchange|@onclick|@oncontextmenu|@oncuechange|@ondblclick|@ondrag|@ondragend|@ondragenter|@ondragleave|@ondragover|@ondragstart|@ondrop|@ondurationchange|@onemptied|@onended|@onerror|@onfocus|@oninput|@oninvalid|@onkeydown|@onkeypress|@onkeyup|@onload|@onloadeddata|@onloadedmetadata|@onloadstart|@onmousedown|@onmousemove|@onmouseout|@onmouseover|@onmouseup|@onmousewheel|@onpause|@onplay|@onplaying|@onprogress|@onratechange|@onreset|@onscroll|@onseeked|@onseeking|@onselect|@onshow|@onstalled|@onsubmit|@onsuspend|@ontimeupdate|@onvolumechange|@onwaiting|@*[not(namespace-uri()=('','http://www.w3.org/1999/xhtml')) or starts-with(local-name(),'data-')]"/>
                    <xsl:apply-templates select="node()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
