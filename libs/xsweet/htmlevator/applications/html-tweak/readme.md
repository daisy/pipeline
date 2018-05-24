# HTML Tweak

XSLT pipeline for ad hoc HTML enhancement via 'class' and 'style' (property) mapping.

The simplest way to run HTML Tweak under XSLT 3.0 is to call the stylesheet `APPLY-html-tweaks.xsl` to your HTML file, with a runtime parameter `config` set to point to your HTML tweak configuration file.

For example: > XSLT my-source.html APPLY-html-tweaks.xsl config=my-set-of-html-tweaks.xml

Internally, this stylesheet performs this operations (which you can also emulate for debugging or to run under XSLT 2.0):

* Reads the tweak configuration file given, such as `html-tweak-map.xml`. (It can be named anything.)

* Run an XSLT stylesheet on this file to produce a stylesheet.

* Run this XSLT on your HTML to apply the "HTML Tweaks" your configuration has indicated

Declare your mapping in a simple XML file and XSLT will apply it to your HTML file, creating a new (modified) HTML file with the appropriate properties rearranged.

To do this, HTML Tweak uses a little mapping language. It establishes matches between categories of elements (ordinarily they will be `p` or `span` elements in your HTML, or what have you) as indicated by CSS property or CSS property-value (on a `@style` attribute) or a simple class name on a `@class` attribute.

This offers a rough-and-ready way to classify regularities in the input data, and relabel it. It may be, that a single configuration may never be useful for more than a single document. Yet if they are very easy to make, they might in certain circumstances nonetheless prove the effort of learning to make and apply them.

Accordingly, the language is very simple and versatile:

`where` a wrapper for a rule

`match` conditions on an element for it to match

`style` a (style) property name or 'property-name: value' combination

`class` a class value (name token)

All processing assumes:

* @style ('style' attributes) can reliably be parsed around ';' delimiters (actually \s\*;\s\* )
* tokens on @style will take the form 'property-name:property-value` allowing for whitespace; i.e. ':' delimiters (actually \s\*:\s\* )
* @class ('class') tokens can reliably be discriminated around white space 
* Overloaded @class is normal

### Example 1

Requirement: where a 'class' token is 'Default' ...

```
<p class="Default">Here is default class paragraph</p>
```
we wish to remove it
```
<p>Here is default class paragraph</p>
```


The HTML Tweak rule looks like:

``` xml
<where>
    <match><class>Default</class></match>
    <remove><class>Default</class></remove>
  </where>
```

### Another example: removing a style property

```
<where>
    <match><style>margin-bottom</style></match>
    <remove><style>margin-bottom</style></remove>
  </where>
```

Requirement: where a styling property appears (by name ...

```
<p style="text-indent:1em; margin-bottom: 1em">Styling includes a property</p>
```
we wish to remove it
```
<p  style="text-indent:1em">Styling includes a property</p>
```


### Removing a style property/value

Maybe we wish to remove the style only when a given property has a given value:

```
<where>
    <match><style>font-family: Helvetica</style></match>
    <remove><style>font-family</style></remove>
  </where>
```

### Mapping a particular combination of style and class, to a new style and class ...

```
<where>
    <match>
      <style>font-size: 18pt</style>
      <class>FreeForm</class>
    </match>
    <remove>
      <style>font-size</style>
      <class>FreeForm</class>
    </remove>
    <add>
      <class>FreeFormNew</class>
      <style>color: red</style>
    </add>
  </where>  
```