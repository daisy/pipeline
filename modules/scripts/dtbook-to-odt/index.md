<link rev="dp2:doc" href="src/main/resources/xml/dtbook-to-odt.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="DTBook to ODT"/>

# DTBook to ODT

Transforms a DTBook (DAISY 3 XML) document into an ODT (OpenDocument Text).

ODT is the native file format of OpenOffice and LibreOffice but can be
opened with Microsoft Word as well. The DTBook to ODT converter is
designed to work with both.

## Synopsis

{{>synopsis}}

## Examples

Some examples can be found [here](src/test/xprocspec/test_content.xprocspec).

## Templating

The conversion works by converting DTBook elements to ODT elements
with corresponding semantics (paragraphs, text spans, frames, images,
formulas, lists, tables, notes, comments, etc.). However because ODT
has a more limited set of elements, different DTBook elements are
mapped to the same ODT element. In order to differentiate between
DTBook elements they are given styles in ODT.

These styles can be edited afterwards in the ODT document, but more
importantly they can be saved in a template (`*.ott` file) which can
be provided to the script the next time (via the "Template" option).

The template that is used by default is <a
href="src/main/resources/templates/default.ott"
class="userdoc">`default.ott`</a>.

What follows is a mapping from DTBook elements to ODT styles. There
are different types of styles: paragraph styles, character styles,
frame styles, list styles and section styles. (Page styles also exist
but are not used by the converter.)

### Paragraph styles

| DTBOOK element                   | ODT style           |
| -------------------------------- | ------------------- |
| `dtb:annotation`                 | `dtb:annotation`    |
| `dtb:blockquote`                 | `dtb:blockquote`    |
| `dtb:bridgehead`                 | `dtb:bridgehead`    |
| `dtb:byline`                     | `dtb:byline`        |
| `dtb:caption`                    | `dtb:caption`       |
| `dtb:covertitle`                 | `dtb:covertitle`    |
| `dtb:dd`                         | `dtb:dd`            |
| `dtb:docauthor`                  | `dtb:docauthor`     |
| `dtb:doctitle`                   | `dtb:doctitle`      |
| `dtb:epigraph`                   | `dtb:epigraph`      |
| `dtb:h1`                         | `dtb:h1`            |
| `dtb:h2`                         | `dtb:h2`            |
| `dtb:h3`                         | `dtb:h3`            |
| `dtb:h4`                         | `dtb:h4`            |
| `dtb:h5`                         | `dtb:h5`            |
| `dtb:h6`                         | `dtb:h6`            |
| `dtb:hd`                         | `dtb:hd`            |
| `dtb:img`                        | `dtb:img`           |
| `dtb:li`                         | `dtb:li`            |
| `dtb:note` with class `endnote`  | `dtb:note_endnote`  |
| `dtb:note` with class `footnote` | `dtb:note_footnote` |
| `dtb:p`                          | `dtb:p`             |
| `dtb:pagenum`                    | `dtb:pagenum`       |
| `dtb:poem`                       | `dtb:poem`          |
| `dtb:prodnote`                   | `dtb:prodnote`      |
| `dtb:td`                         | `dtb:td`            |
| `dtb:th`                         | `dtb:th`            |

### Character styles

| DTBOOK element | ODT style     |
| -------------- | ------------- |
| `dtb:a`        | `dtb:a`       |
| `dtb:abbr`     | `dtb:abbr`    |
| `dtb:acronym`  | `dtb:acronym` |
| `dtb:author`   | `dtb:author`  |
| `dtb:cite`     | `dtb:cite`    |
| `dtb:code`     | `dtb:code`    |
| `dtb:dt`       | `dtb:dt`      |
| `dtb:em`       | `dtb:em`      |
| `dtb:kbd`      | `dtb:kbd`     |
| `dtb:linenum`  | `dtb:linenum` |
| `dtb:q`        | `dtb:q`       |
| `dtb:samp`     | `dtb:samp`    |
| `dtb:strong`   | `dtb:strong`  |
| `dtb:sub`      | `dtb:sub`     |
| `dtb:sup`      | `dtb:sup`     |
| `dtb:title`    | `dtb:title`   |

### Frame styles

| DTBOOK element | ODT style  |
| -------------- | ---------- |
| `dtb:img`      | `dtb:img`  |
| `math:math`    | `dtb:math` |

### List styles

| DTBOOK element            | ODT style           |
| ------------------------- | ------------------- |
| `dtb:dl`                  | `dtb:dl`            |
| `dtb:list` with type `ol` | `dtb:list_ol`       |
| `dtb:list` with type `pl` | `dtb:list_pl`       |
| `dtb:list` with type `ul` | `dtb:list_ul`       |

### Section styles

| DTBOOK element | ODT style     |
| -------------- | ------------- |
| `dtb:sidebar`  | `dtb:sidebar` |

## Comments

The resulting ODT document may contain special comments with warnings,
for instance when a certain element in the DTBook could not be
rendered in the ODT.
