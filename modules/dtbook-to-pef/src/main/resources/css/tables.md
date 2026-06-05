<link rev="dp2:doc" href="_tables.scss"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# _tables.scss

Style sheet module for the [DTBook to PEF script](../../../../doc/) for styling tables.

Use this style sheet by importing it in your custom SCSS style sheet:

```scss
@import "http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/_tables.scss";
```

The module provides two mixins that correspond with the two main table
styles, and one mixin for automatically finding the optimal table
layout. It also provides variables for duplicating tables.

## `table-matrix` mixin

Include this mixin to render a table in "matrix" or "grid" format,
i.e. with `display: table`.

```scss
table.matrix {
   @include table-matrix;
}
```

The mixin takes one optional argument, `$transpose`, to specify the
orientation of the table. Possible values are `false` (default),
`true` or `auto`. With `true` the `tr` elements become the columns of
the grid instead of the rows. `auto` automatically determines the
optimal orientation of the table (with least number of rows).

```scss
table.matrix {
   @include table-matrix($transpose: auto);
}
```

## `table-nested-list` mixin

Include this mixin to render a table as a list, the table cells being
the list items, grouped into sub-lists by row or column. Table cells
are preceded by their associated table headers, avoiding repetitions
when possible. The main and sub lists are styled through four
placeholder selectors: `%outer-list`, `%outer-list-item`,
`%inner-list` and `%inner-list-item`.

Example:

```scss
table.list {
   @include table-nested-list(
      $header-suffix: ": "
   ) {
      %outer-list-item {
         display: block;
         margin-left: 2;
      }
      %inner-list-item {
         display: block;
         margin-left: 2;
         text-indent: -2;
         &:first-child {
            text-indent: -4;
         }
      }
   }
}
```

The mixin takes three optional arguments.

`$transpose` is used to specify whether table cells are grouped by row
(`false`, the default) or by column (`true`), or whether an optimal
layout is automatically determined (`auto`).

With `$header-suffix` you can specify the text or braille to be
inserted after table headers.

`$blank-cell-text` is the text or braille that is inserted as
relacement for empty table cells.

## `table-optimal` mixin

Include this mixin to automatically select the best out of a number of
different layouts (two or more). The "best" layout is the one with the
lowest cost. The mixin takes as arguments the expressions to compute
the costs of the different layouts. The layouts are specified as
placeholder selectors `%layout1`, `%layout2`, etc.

The following example tries to lay out a table in a grid and falls
back to a list if the grid format is not suitable.

```scss
table.auto {
   @include table-optimal(
      "(+ (* 10 $forced-break-count) (* .75 $total-height))",  // cost of layout 1
      "$total-height"                                          // cost of layout 2
   )
   &%layout1 {
       @include table-matrix;
   }
   &%layout2 {
       @include table-nested-list {
          ...
       }
   }
}
```

Note that the placeholder selectors are attached to the outer selector
with a `&` (parent selector).

Parameters that can be used in cost expressions are:

- `$forced-break-count`: the number of forced line breaks, meaning
  when a line of text is broken at a character that is not white space
  and not a valid hyphenation point.
- `$total-height`: the total height (in number of braille cells) of the table.
- `$min-block-width`: the width (in number of braille cells) of the
  narrowest block within the table, typically a table cell.

## `$duplicate-tables-with-class` and `$classes-for-table-duplicates` variables

Set `$duplicate-tables-with-class` to duplicate all tables with that
class. If the value is the empty string, all tables without a class
attribute are duplicated. The `$classes-for-table-duplicates` variable
determines how many copies are made and which classes are added. The
value must be a space separated list of class names. As many copies
are made as there are items in the list. The first class is added to
the first copy, etc. At least two items are required. A `.` may be
used to separate classes if multiple classes need to be added to a
single copy.

This feature is useful for trying out different table layouts before
deciding which layout to use for which table.

Note that the variables must be set *before* the `@import` rule in
order to have effect.
