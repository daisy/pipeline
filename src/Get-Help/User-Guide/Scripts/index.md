---
layout: default
---
# Scripts

"Scripts" is the term we use for the conversion tasks you can perform
with DAISY Pipeline 2. The complete list of available scripts is:

{% sparql doc in "SELECT ?href ?title WHERE { [] a dp2:script ; dp2:doc ?href . ?href dc:title ?title ; a dp2:userdoc } ORDER BY ?title" %}
* [{{doc.title}}]({{doc.href}})
{% endsparql %}

## In matrix format

<table id="scripts-matrix">
  <tr>
    <th colspan="1" rowspan="2">Inputs</th>
    <th colspan="6">Outputs</th>
  </tr>
  <tr>
    <th>DAISY 2.02</th>
    <th>DAISY 3</th>
    <th>EPUB 3</th>
    <th>HTML</th>
    <th>ZedAI</th>
    <th>PEF</th>
  </tr>
  <tr>
    <th>DAISY 2.02</th>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/daisy202-to-epub3">x</a></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th>DAISY 3</th>
    <td><a href="{{site.baseurl}}/modules/daisy3-to-daisy202">x</a></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/daisy3-to-epub3">x</a></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <th>DTBook</th>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-daisy3">x</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-epub3">x</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-html">x</a></td>
    <td><a href="{{site.baseurl}}/modules/dtbook-to-zedai">x</a></td>
    <td><a href="{{site.baseurl}}/modules/braille/dtbook-to-pef">x</a></td>
  </tr>
  <tr>
    <th>EPUB 3</th>
    <td><a href="{{site.baseurl}}/modules/epub3-to-daisy202">x</a></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/braille/epub3-to-pef">x</a></td>
  </tr>
  <tr>
    <th>HTML</th>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/html-to-epub3">x</a></td>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/braille/html-to-pef">x</a></td>
  </tr>
  <tr>
    <th>ZedAI</th>
    <td></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/zedai-to-epub3">x</a></td>
    <td><a href="{{site.baseurl}}/modules/zedai-to-html">x</a></td>
    <td></td>
    <td><a href="{{site.baseurl}}/modules/braille/zedai-to-pef">x</a></td>
  </tr>
</table>
