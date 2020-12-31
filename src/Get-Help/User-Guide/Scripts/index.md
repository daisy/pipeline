---
layout: default
---
# Scripts

"Scripts" is the term we use for the conversion tasks you can perform
with DAISY Pipeline 2. The complete list of available scripts is:

* [DAISY 2.02 Validator]({{site.baseurl}}/modules/daisy202-validator)
* [DAISY 2.02 to EPUB 3]({{site.baseurl}}/modules/daisy202-to-epub3)
* [DAISY 3 to DAISY 2.02]({{site.baseurl}}/modules/daisy3-to-daisy202)
* [DAISY 3 to EPUB 3]({{site.baseurl}}/modules/daisy3-to-epub3)
* [DTBook Validator]({{site.baseurl}}/modules/dtbook-validator)
* [DTBook to DAISY 3]({{site.baseurl}}/modules/dtbook-to-daisy3)
* [DTBook to EPUB3]({{site.baseurl}}/modules/dtbook-to-epub3)
* [DTBook to HTML]({{site.baseurl}}/modules/dtbook-to-html)
* [DTBook to ODT]({{site.baseurl}}/modules/dtbook-to-odt)
* [DTBook to PEF]({{site.baseurl}}/modules/braille/dtbook-to-pef)
* [DTBook to RTF]({{site.baseurl}}/modules/dtbook-to-rtf)
* [DTBook to ZedAI]({{site.baseurl}}/modules/dtbook-to-zedai)
* [EPUB Upgrader]({{site.baseurl}}/modules/epub2-to-epub3)
* [EPUB 3 Enhancer]({{site.baseurl}}/modules/epub3-to-epub3)
* [EPUB 3 Validator]({{site.baseurl}}/modules/epub3-validator)
* [EPUB 3 to DAISY 2.02]({{site.baseurl}}/modules/epub3-to-daisy202)
* [EPUB 3 to DAISY 3]({{site.baseurl}}/modules/epub3-to-daisy3)
* [EPUB 3 to PEF]({{site.baseurl}}/modules/braille/epub3-to-pef)
* [EPUB to DAISY]({{site.baseurl}}/modules/epub-to-daisy)
* [HTML to EPUB3]({{site.baseurl}}/modules/html-to-epub3)
* [HTML to PEF]({{site.baseurl}}/modules/braille/html-to-pef)
* [NIMAS Fileset Validator]({{site.baseurl}}/modules/nimas-fileset-validator)
* [ZedAI to EPUB 3]({{site.baseurl}}/modules/zedai-to-epub3)
* [ZedAI to HTML]({{site.baseurl}}/modules/zedai-to-html)
* [ZedAI to PEF]({{site.baseurl}}/modules/braille/zedai-to-pef)


## Matrix

<table id="scripts-matrix">
  <tr>
    <th colspan="2" rowspan="3">Inputs</th>
    <th colspan="8">Outputs</th>
  </tr>
  <tr>
    <th colspan="2">DAISY</th>
    <th rowspan="2">EPUB 3</th>
    <th rowspan="2">HTML</th>
    <th rowspan="2">ZedAI</th>
    <th rowspan="2">PEF</th>
    <th rowspan="2">RTF</th>
    <th rowspan="2">ODT</th>
  </tr>
  <tr>
    <th>2.02</th>
    <th>3</th>
  </tr>
  <tr>
    <th rowspan="2">DAISY</th>
    <th>2.02</th>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/daisy202-to-epub3">DAISY 2.02 to EPUB 3</a></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th>3</th>
    <td><a href="{{site.baseurl}}/modules/daisy3-to-daisy202">DAISY 3 to DAISY 2.02</a></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/daisy3-to-epub3">DAISY 3 to EPUB 3</a></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th colspan="2">DTBook</th>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-daisy3">DTBook to DAISY 3</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-epub3">DTBook to EPUB 3</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-html">DTBook to HTML</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-zedai">DTBook to ZedAI</a></td>
    <td><a href="{{site.baseurl}}/modules/braille/dtbook-to-pef">DTBook to PEF</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-rtf">DTBook to RTF</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-odt">DTBook to ODT</a></td>
  </tr>
  <tr>
    <th rowspan="2">EPUB</th>
    <th>2</th>
    <td colspan="2"><a href="{{site.baseurl}}/modules/epub-to-daisy">EPUB to DAISY</a></td>
    <td><a href="{{site.baseurl}}/modules/epub2-to-epub3">EPUB Upgrader</a></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th>3</th>
    <td><a href="{{site.baseurl}}/modules/epub3-to-daisy202">EPUB 3 to DAISY 2.02</a> or
        <a href="{{site.baseurl}}/modules/epub-to-daisy">EPUB to DAISY</a></td>
    <td><a href="{{site.baseurl}}/modules/epub3-to-daisy3">EPUB 3 to DAISY 3</a> or
        <a href="{{site.baseurl}}/modules/epub-to-daisy">EPUB to DAISY</a></td>
    <td><a href="{{site.baseurl}}/modules/epub3-to-epub3">EPUB 3 Enhancer</a></td>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/braille/epub3-to-pef">EPUB 3 to PEF</a></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th colspan="2">HTML</th>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/html-to-epub3">HTML to EPUB 3</a></td>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/braille/html-to-pef">HTML to PEF</a></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th colspan="2">ZedAI</th>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/zedai-to-epub3">ZedAI to EPUB 3</a></td>
    <td><a href="{{site.baseurl}}/modules/zedai-to-html">ZedAI to HTML</a></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/braille/zedai-to-pef">ZedAI to PEF</a></td>
    <td></td>
    <td></td>
  </tr>
</table>
