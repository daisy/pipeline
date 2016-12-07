<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="DTBook to ZedAI Conversion Rules"/>

<!--
labels: [Type-Doc,Compoment-UserGuide,Component-Module,Component-Script]
sidebar: UserGuideToc
-->

# DTBook To ZedAI Conversion Rules

This page gives the conversion mappings used in the DTBook-To-ZedAI module. 

## Table of contents

{{>toc}}

---------

## DTBook element: `a`

**ZedAI element**: `ref`

**Dropped attributes**: `@type`, `@hreflang`, `@accesskey`, `@tabindex`

**Adjustments**: 

* re-parent `imggroup` children
* use inline `samp` 
* `br` to `ln`

**Notes**: 

If `dtbook:a@external = true`, then we use `@xlink:href` instead of `@ref`.

---------

## DTBook element: `abbr`

**ZedAI element**: `abbr @type='truncation'`

**Adjustments**:

The following are rather meaningless in this context, so translate them as `span`. Would be nice to carry the original intent along, such as `span` role='`cite`'`, but there aren't roles for them all.

* `dfn` becomes `span`
* `code` becomes `span`
* `kbd` becomes `span`
* `cite` becomes `span`
* `sent` becomes `span` role="`sent`ence"`
* `prodnote` becomes `extract`

These are a little trickier as they (may) reference something else, but as there is no allowed child element in ZedAI which may contain a link or reference, that information is not carried along. This is an unlikely scenario.

* `q` becomes `span`
* `a` becomes `span`
* `annoref`, `noteref` become `span`

* re-parent `imggroup` children
* use inline `samp`
* remove `br`

---------

## DTBook element: `acronym`

**ZedAI element**: `abbr`

**Notes**: 

* `@pronounce="no"` becomes `@type="initialism"`
* if missing `@pronounce`, becomes `@type="initialism"` 
* `@pronounce="yes"` becomes `@type="acronym"`

**Adjustments**:

The following are rather meaningless in this context, so translate them as `span`. Would be nice to carry the original intent along, such as `span` role='`cite`', but there aren't roles for them all.

* `dfn` becomes `span`
* `code` becomes `span`
* `kbd` becomes `span`
* `cite` becomes `span`
* `sent` becomes `span`
* `prodnote` becomes `extract`

These are a little trickier as they (may) reference something else, but as there is no allowed child element in ZedAI which may contain a link or reference, that information is not carried along. This is an unlikely scenario.

* `q` becomes `span`
* `a` becomes `span`
* `annoref`, `noteref` become `span`
* re-parent `imggroup` children
* use inline `samp`
* remove `br`

---------

## DTBook element: `address`

**ZedAI element**: `address`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `annoref`

**ZedAI element**: `annoref`

**Dropped attributes**: 

* `@type` ("Type provides advisory content MIME type of the targeted id, see RFC1556." --structguide)

---------

## DTBook element: `annotation`

**ZedAI element**: `annotation`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `author`

**ZedAI element**: `citation` or `p @property='author' @about='IDREF'` 

author element should be made into citation except when found inside `poem`/`blockquote`/`cite`, then use `p @property/@about`

**Adjustments**: 

* re-parent `imggroup` children
* use inline `samp`
* `br` to `ln`

---------

## DTBook element: `bdo`

**ZedAI element**: `span @its:dir="rtl|ltr"`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `blockquote`

**ZedAI element**: `quote`

---------

## DTBook element: `bodymatter`

**ZedAI element**: `bodymatter`

---------

## DTBook element: `book`

**ZedAI element**: `body`

---------

## DTBook element: `br`

**ZedAI element**: use `ln` to wrap lines

---------

## DTBook element: `bridgehead`

**ZedAI element**: `hd`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `byline`

**ZedAI element**: `citation` or `p @role="periodicals:byline"`

Notes: for most book (non-article) use cases, `byline` can be `citation`. The exception would be anthologies, for which we can call upon the periodicals vocab and actually use `"role = byline"`. For now, we will use just 1 vocabulary in this converter.

**Adjustments**: 

* Re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `caption`

**ZedAI element**: `caption`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `cite`

**ZedAI element**: `citation`

**Adjustments**: 

* `title` and `author`, are `span` instead of `p` 
* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `code`

**ZedAI element**: `code`

**Adjustments**:

* Inline content model if only children are: `em`, `strong`, `abbr`, `acronym`, `ln`, `sub`, `sup`, `span`, `bdo`, `pagenum`, `text`
* If any of the following occur, make all children block-level: `dfn`, `code`, `samp`, `kbd`, `cite`, `a`, `img`, `imggroup`, `q`, `sent`, `w`, `prodnote`, `annoref`, `noteref`
* deal with `br`

---------

## DTBook element: `col`

**ZedAI element**: `col`

**CSS**: `@align`, `@valign`, `@width` (all to go in CSS)

---------

## DTBook element: `colgroup`

**ZedAI element**: `colgroup`

**CSS**: `@align`, `@valign`, `@width`

---------

## DTBook element: `covertitle`

**ZedAI element**: `block @role='covertitle'`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `dateline`

**ZedAI element**: `p @role="time"`

Notes: Used `p` since `ln` is not suitable for all content models (e.g. `level`>`dateline` cannot be translated as `section`>`ln`)

**Adjustments**: 

* re-parent `imggroup` children
* use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `dd`

**ZedAI element**: `definition`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` * `br` to `ln` * Re-parent list, dl, div, poem, linegroup, table, sidebar * Re-parent or re-name p, dateline, epigraph, address, author, note

---------

## DTBook element: `dfn`

**ZedAI element**: `term`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `div`

**ZedAI element**: `block`

**Adjustments**: Normalize block/inline content model: if mixed, wrap inlines in `p` element.

---------

## DTBook element: `dl`

**ZedAI element**: `list`

**Adjustments**: either wrap each term, definition in its own `item` element, or combine them into one `item` element

---------

## DTBook element: `docauthor`

**ZedAI element**: `p @property="author"`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `doctitle`

**ZedAI element**: `p @property="title"` or `h @property="title"` when child of the frontmatter

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `dt`

**ZedAI element**: `term`

**Adjustments**:

The following are rather meaningless in this context, so translate them as `span`. Would be nice to carry the original intent along, such as `span @role='cite'`, but there aren't roles for them all.

* `dfn` becomes `span`
* `code` becomes `span`
* `kbd` becomes `span`
* `cite` becomes `span`
* `sent` becomes `span`
* `prodnote` becomes `extract`

These are a little trickier as they (may) reference something else, but as there is no allowed child element in ZedAI which may contain a link or reference, that information is not carried along. This is an unlikely scenario.

* `q` becomes `span`
* `a` becomes `span`
* `annoref`, `noteref` become `span`

* inline `samp`
* re-parent `imggroup` children
* `br` to `ln`

---------

## DTBook element: `dtbook`

**ZedAI element**: `document`

**Notes**: Reference default ZedAI book profile and default vocabulary.

---------

## DTBook element: `epigraph`

**ZedAI element**: `block @role="epigraph"`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `em`

**ZedAI element**: `emph`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `frontmatter`

**ZedAI element**: `frontmatter`

Notes: A `section` is created automatically containing `doctitle`, `docauthor`, `covertitle`. The reason is that DTBook can have that info free-floating, whereas ZedAI must have only toc/section/biblio/glossary children and so we have to contain that info in a `section` element.

---------

## DTBook element: `h1`

**ZedAI element**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `h2`

**ZedAI element**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `h3`

**ZedAI element**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `h4`

**ZedAI element**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `h5`

**ZedAI element**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `h6`

**ZedAI element**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `hd`

**ZedAI element**: `hd`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `head`

**ZedAI element**: `head`

**Notes**: 

* ZedAI book profile is hardcoded 
* DTBook `@profile` is not preserved.

---------

## DTBook element: `img`

**ZedAI element**: `object`

**Notes**: 

* `dtbook:@longdesc` is a URI which resolves to a `prodnote` elsewhere the book 

---------

## DTBook element: `imggroup`

**ZedAI element**: `block` (with `@role='figure'` if a caption is present)

**Notes**: 

* Look at DTBook multiple `prodnote` descriptions 
* There can also be multiple captions, each targeting multiple images

---------

## DTBook element: `kbd`

**ZedAI element**: `code`

**Adjustments**: 

* Same as `dtb:code`

---------

## DTBook element: `level`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `level1`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `level2`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `level3`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `level4`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `level5`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `level6`

**ZedAI element**: `section`

**Adjustments**: Mixed section/block content model

---------

## DTBook element: `lic`

**ZedAI element**: `span`

---------

## DTBook element: `line`

**ZedAI element**: `ln`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp`
* deal with `br`

---------

## DTBook element: `linegroup`

**ZedAI element**: `block`

---------

## DTBook element: `linenum`

**ZedAI element**: `lnum`

---------

## DTBook element: `link`

Discarded

---------

## DTBook element: `li`

**ZedAI element**: `item`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `list`

**ZedAI element**: `list`

**Adjustments**: wrap `dtbook:prodnote` and `dtbook:hd`

---------

## DTBook element: `meta`

**ZedAI element**: `meta`

**Notes**: See https://github.com/daisy/pipeline/issues/56

---------

## DTBook element: `note`

**ZedAI element**: `note`

**Adjustments**: Normalize block/inline content model: if mixed, wrap inlines in `p` element.

---------

## DTBook element: `noteref`

**ZedAI element**: `noteref`

Dropped: `@type` ("Type provides advisory content MIME type of the targeted id, see RFC1556.")

---------

## DTBook element: `p`

**ZedAI element**: `p`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln` 
* re-parent `list`, `dl` children

---------

## DTBook element: `pagenum`

**ZedAI element**: `pagebreak`

---------

## DTBook element: `poem`

**ZedAI element**: `block @role="poem"`

---------

## DTBook element: `prodnote`

**ZedAI element**: `annotation @by="republisher"`

**Adjustments**: 

* `br` to `ln` 
* Normalize block/inline content model: if mixed, wrap inlines in `p` element.

---------

## DTBook element: `q`

**ZedAI element**: `quote`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `rearmatter`

**ZedAI element**: `backmatter`

---------

## DTBook element: `samp`

**ZedAI element**: `block @role="example"`

**Notes**: in DTBook, it seems like `samp` can be both block and inline. There are cases when it is be best translated as a `zedai:span` instead. In this case, the content model needs to be adjusted for inline-only.

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `sent`

**ZedAI element**: `s`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `sidebar`

**ZedAI element**: `aside @role="sidebar"`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

---------

## DTBook element: `span`

**ZedAI element**: `span`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `strong`

**ZedAI element**: `emph`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln` 
* Re-parent or rename `cite`, `q`

---------

## DTBook element: `sub`

**ZedAI element**: `sub`

**Adjustments**:

The following are rather meaningless in this context, so translate them as `span`. Would be nice to carry the original intent along, such as `span @role='cite'`, but there aren't roles for them all.

* `dfn` becomes `span`
* `code` becomes `span`
* `kbd` becomes `span`
* `cite` becomes `span`
* `sent` becomes `span @role='sentence'`
* `prodnote` becomes `extract`
* `abbr` becomes `span @role='truncation'`
* `acronym` becomes `span @role='acronym' | 'initialism'`
* `w` becomes `span @role='word'`

These are a little trickier as they (may) reference something else, but as there is no allowed child element in ZedAI which may contain a link or reference, that information is not carried along. This is an unlikely scenario.

* `q` becomes `span`
* `a` becomes `span`
* `annoref`, `noteref` become `span`

* Re-parent `imggroup` children
* use inline `samp`
* remove `br`

---------

## DTBook element: `sup`

**ZedAI element**: `sup`

**Adjustments**:

The following are rather meaningless in this context, so translate them as `span`. Would be nice to carry the original intent along, such as `span` role='`cite`', but there aren't roles for them all.

* `dfn` becomes `span`
* `code` becomes `span`
* `kbd` becomes `span`
* `cite` becomes `span`
* `sent` becomes `span`
* `prodnote` becomes `extract`
* `abbr` becomes `span @role='truncation'`
* `acronym` becomes `span @role='acronym' | 'initialism'`
* `w` becomes `span @role='word'`

These are a little trickier as they (may) reference something else, but as there is no allowed child element in ZedAI which may contain a link or reference, that information is not carried along. This is an unlikely scenario.

* `q` becomes `span`
* `a` becomes `span`
* `annoref`, `noteref` become `span`

* Re-parent `imggroup` children
* use inline `samp`
* remove `br`

---------

## DTBook element: `table`

**ZedAI element**: `table`

**CSS**: `@width`, `@border` (border width), `@cellspacing`, `@cellpadding`

**Adjustments**: 

* Re-parent `caption` children
* Move `col`s into a `colgroup`; zedai does not allow free-floating `col`s. 
* Move `tr` or `pagenum` into a `tbody` if `thead` or `tfoot` is present 
* Re-order `thead`, `tfoot`, `tbody` into `thead`, `tbody`, `tfoot`

**Notes**: See https://github.com/daisy/pipeline/issues/58

---------

## DTBook element: `tbody`

**ZedAI element**: `tbody`

**CSS**: `@align`, `@valign`

---------

## DTBook element: `td`

**ZedAI element**: `td`

**CSS**: `@align`, `@valign`

**Adjustments**: 

* `br` to `ln` 
* Normalize block/inline content model: if mixed, wrap inlines in `p` element.

---------

## DTBook element: `tfoot`

**ZedAI element**: `tfoot`

**CSS**: `@align`, `@valign`

---------

## DTBook element: `th`

**ZedAI element**: `th`

**CSS**: `@align`, `@valign`

**Adjustments**: 

* `br` to `ln` 
* Normalize block/inline content model: if mixed, wrap inlines in `p` element.

---------

## DTBook element: `thead`

**ZedAI element**: `thead`

**CSS**: `@align`, `@valign`

---------

## DTBook element: `title`

**ZedAI element**: `p @property='title' @about='IDREF'`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

---------

## DTBook element: `tr`

**ZedAI element**: `tr`

**CSS**: `@align`, `@valign`

---------

## DTBook element: `w`

**ZedAI element**: `w`

**Adjustments**:

The following are rather meaningless in this context, so translate them as `span`. Would be nice to carry the original intent along, such as `span @role='cite'`, but there aren't roles for them all.

* `dfn` becomes `span`
* `code` becomes `span`
* `kbd` becomes `span`
* `cite` becomes `span`
* `abbr` becomes `span @role='truncation'`
* acronym becomes `span @role='acronym' | 'initialism'`

These are a little trickier as they (may) reference something else, but as there is no allowed child element in ZedAI which may contain a link or reference, that information is not carried along. This is an unlikely scenario.

* `q` becomes `span`
* `a` becomes `span`

* Re-parent `imggroup` children
* use inline `samp`
* remove `br`

---------

## Common Attributes

* `id` becomes `xml:id`
* `xml:space` is copied
* `class` is copied
* `xml:lang` is copied
* `dir` becomes `its:dir`
* `showin` is discarded
* `title` and `name` are discarded
