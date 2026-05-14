<link rev="dp2:doc" href="_definition-lists.scss"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# _definition-lists.scss

Style sheet module for the [DTBook to PEF](../../../../doc/) script
for styling definition lists.

Use this style sheet by importing it in your custom SCSS style sheet:

```scss
@import "http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/_definition-lists.scss";
```

The module provides a variable for grouping `dt` and `dd` elements and
variables to define separators between `dt` and `dd` elements.

## `$group-dt-dd` variable

Set this variable to `true` to group `dt` and `dd` elements, wrapping
them inside `li` elements, so that every group starts with a `dt` and
ends with a `dd` and does not contain a `dd` followed by a `dt`.

Note that the variable must be set *before* the `@import` rule in
order to have effect.

## `$dt-suffix` and `$dt-separator` variables

If `$group-dt-dd` is `true`, `dt` and `dd` becomes inline
elements. The `$dt-suffix` variable determines the text that is
inserted in between a `dt` and a `dd` element (default `": "`). The
`$dt-separator` variable determines the text that is inserted in
between two `dt` elements (default `" "`). In order to make `dt` and
`dd` block-level elements, set `$dt-suffix` to `null`.

Note that the variable must be set *before* the `@import` rule in
order to have effect.
