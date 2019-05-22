<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:html="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all">

    <!-- metadata for report -->
    <xsl:variable name="text_of_uid" select="(//html:head)[1]/html:meta[@name='dc:identifier']/@content"/>
    <xsl:variable name="text_of_title" select="(//html:head)[1]/html:title/text()"/>

    <!-- variables for category count -->
    <xsl:variable name="numberof_tables" select="count(//html:table)"/>
    <xsl:variable name="numberof_sidebars" select="count(//html:aside[tokenize(@epub:type,'\s+')='sidebar'])"/>
    <xsl:variable name="numberof_images" select="count(//html:img)"/>
    <xsl:variable name="numberof_image_series" select="count(//html:figure[tokenize(@class,'\s+')='image-series'])"/>
    <xsl:variable name="numberof_pages" select="count((//html:span | //html:div)[tokenize(@epub:type,'\s+')='pagebreak'])"/>
    <xsl:variable name="numberof_asciimath" select="count(//@class[tokenize(.,'\s+')='asciimath'])"/>

    <xsl:output method="html" encoding="UTF-8" doctype-system="about:legacy-compat"/>
    <xsl:template match="/">

        <xsl:variable name="images-percent" select="min((1,$numberof_images div max((1,$numberof_pages))))"/>
        <xsl:variable name="tables-percent" select="min((1,$numberof_tables div max((1,$numberof_pages))))"/>
        <xsl:variable name="sidebars-percent" select="min((1,$numberof_sidebars div max((1,$numberof_pages))))"/>
        <xsl:variable name="total-percent" select="min((1,($numberof_images + $numberof_tables + $numberof_sidebars) div max((1,$numberof_pages))))"/>

        <xsl:variable name="category" select="1"/>
        <xsl:variable name="category" select="if ($images-percent &gt; 0.1 or $tables-percent &gt; 0.1 or $sidebars-percent &gt; 0.1 or $total-percent &gt; 0.1) then 2 else $category"/>
        <xsl:variable name="category" select="if ($images-percent &gt;= 0.8 or $tables-percent &gt;= 0.3 or $sidebars-percent &gt;= 0.3 or $total-percent &gt;= 0.8) then 3 else $category"/>
        <xsl:variable name="category" select="if ($numberof_asciimath=0) then $category else 4"/>

        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta charset="utf-8"/>
                <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                <title><xsl:value-of select="$text_of_uid"/>: delivery control</title>
                <meta name="description" content="Report for evaluation of the book complexity"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <meta name="report.type" content="category"/>
            </head>
            <body>
                <h2>Book category for <xsl:value-of select="$text_of_uid"/> (<xsl:value-of select="$text_of_title"/>)</h2>

                <div class="alert alert-info" style="color: black; font-size: 1.6em;">
                    <p>
                        <strong>Book is category <xsl:value-of select="concat($category, if ($numberof_asciimath=0) then '' else ' (book contains ASCIIMath)')"/></strong>
                    </p>
                </div>

                <p>There are <xsl:value-of select="$numberof_pages"/> pages in this book.</p>

                <xsl:choose>
                    <xsl:when test="$numberof_tables=0">
                        <p>There are no tables in this book.</p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="table-pages"
                            select="for $table in (//html:table) return ('(no preceding pagebreak)', $table/preceding::html:*[tokenize(@epub:type,'\s+')='pagebreak'][1]/(if (@title) then string(@title) else '(unnumbered)'))[last()]"/>
                        <p>There are tables on the following pages: <xsl:value-of select="string-join($table-pages,', ')"/></p>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="$numberof_sidebars=0">
                        <p>There are no sidebars in this book.</p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="sidebar-pages"
                            select="for $sidebar in (//html:aside[tokenize(@epub:type,'\s+')='sidebar']) return ('(no preceding pagebreak)', $sidebar/preceding::html:*[tokenize(@epub:type,'\s+')='pagebreak'][1]/(if (@title) then string(@title) else '(unnumbered)'))[last()]"/>
                        <p>There are sidebars on the following pages: <xsl:value-of select="string-join($sidebar-pages,', ')"/></p>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="$numberof_images=0">
                        <p>There are no images in this book.</p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="image-pages"
                            select="for $image in (//html:img) return ('(no preceding pagebreak)', $image/preceding::html:*[tokenize(@epub:type,'\s+')='pagebreak'][1]/(if (@title) then string(@title) else '(unnumbered)'))[last()]"/>
                        <p>There are images on the following pages: <xsl:value-of select="string-join($image-pages,', ')"/></p>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="$numberof_image_series=0">
                        <p>There are no image series in this book.</p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="image-series-pages"
                            select="for $image-series in (//html:figure[tokenize(@class,'\s+')='image-series']) return ('[unknown]', $image-series/preceding::html:*[tokenize(@epub:type,'\s+')='pagebreak'][1]/string(@title))[last()]"/>
                        <p>There are image series on the following pages: <xsl:value-of select="string-join($image-series-pages,', ')"/></p>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="$numberof_asciimath=0">
                        <p>There are no ASCIIMath in this book.</p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="asciimath-pages"
                            select="for $asciimath in (//@class[tokenize(.,'\s+')='asciimath']) return ('[unknown]', $asciimath/preceding::html:*[tokenize(@epub:type,'\s+')='pagebreak'][1]/string(@title))[last()]"/>
                        <p>There are ASCIIMath on the following pages: <xsl:value-of select="string-join($asciimath-pages,', ')"/></p>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:variable name="style" select="'background-color: #d9edf7;'"/>
                <table class="table">
                    <tbody>
                        <tr>
                            <th>Tags</th>
                            <th>Images</th>
                            <th>Tables</th>
                            <th>Sidebars</th>
                            <th>Sum of tags</th>
                        </tr>
                        <tr>
                            <th>Category 1 levels</th>
                            <td style="{if ($images-percent &lt;= 0.1) then $style else ()}">less than 10%</td>
                            <td style="{if ($tables-percent &lt;= 0.1) then $style else ()}">less than 10%</td>
                            <td style="{if ($sidebars-percent &lt;= 0.1) then $style else ()}">less than 10%</td>
                            <td style="{if ($total-percent &lt;= 0.1) then $style else ()}">less than 10%</td>
                        </tr>
                        <tr>
                            <th>Category 2 levels</th>
                            <td style="{if ($images-percent &gt; 0.1 and $images-percent &lt; 0.8) then $style else ()}">between 10% and 80%</td>
                            <td style="{if ($tables-percent &gt; 0.1 and $tables-percent &lt; 0.3) then $style else ()}">between 10% and 30%</td>
                            <td style="{if ($sidebars-percent &gt; 0.1 and $sidebars-percent &lt; 0.3) then $style else ()}">between 10% and 30%</td>
                            <td style="{if ($total-percent &gt; 0.1 and $total-percent &lt; 0.8) then $style else ()}">between 10% and 80%</td>
                        </tr>
                        <tr>
                            <th>Category 3 levels</th>
                            <td style="{if ($images-percent &gt;= 0.8) then $style else ()}">more than 80%</td>
                            <td style="{if ($tables-percent &gt;= 0.3) then $style else ()}">more than 30%</td>
                            <td style="{if ($sidebars-percent &gt;= 0.3) then $style else ()}">more than 30%</td>
                            <td style="{if ($total-percent &gt;= 0.8) then $style else ()}">more than 80%</td>
                        </tr>
                        <tr>
                            <th>This book</th>
                            <td>
                                <xsl:value-of select="format-number($images-percent, '##.00%')"/>
                            </td>
                            <td>
                                <xsl:value-of select="format-number($tables-percent, '##.00%')"/>
                            </td>
                            <td>
                                <xsl:value-of select="format-number($sidebars-percent, '##.00%')"/>
                            </td>
                            <td>
                                <xsl:value-of select="format-number($total-percent, '##.00%')"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
