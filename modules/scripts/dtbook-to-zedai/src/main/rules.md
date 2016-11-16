<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="DTBook to ZedAI Conversion Rules"/>

<!--
labels: [Type-Doc,Compoment-UserGuide,Component-Module,Component-Script]
sidebar: UserGuideToc
-->

# DTBook To ZedAI Conversion Rules

This page gives the conversion mappings used in the DTBook-To-ZedAI module. 

##`a`

**ZedAI element**: `ref`

**Dropped attributes**: `@type`, `@hreflang`, `@accesskey`, `@tabindex`

**Adjustments**: 

* re-parent `imggroup` children
* use inline `samp` 
* `br` to `ln`

**Notes**: 

If `dtbook:a@external = true`, then we use `@xlink:href` instead of `@ref`.


## `abbr`

**ZedAI**: `abbr @type='truncation'`

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


## `acronym`

**ZedAI**: `abbr`

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

## `address`

**ZedAI**: `address`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

## `annoref`

**ZedAI**: `annoref`

**Dropped attributes**: 

* `@type` ("Type provides advisory content MIME type of the targeted id, see RFC1556." --structguide)


## `annotation`

**ZedAI**: `annotation`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`


## `author`

**ZedAI**: `citation` or `p @property='author' @about='IDREF'` 

author element should be made into citation except when found inside `poem`/`blockquote`/`cite`, then use `p @property/@about`

**Adjustments**: 

* re-parent `imggroup` children
* use inline `samp`
* `br` to `ln`


## `bdo`

**ZedAI**: `span @its:dir="rtl|ltr"`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`


## `blockquote`

**ZedAI**: `quote`


## `bodymatter`

**ZedAI**: `bodymatter`


## `book`

**ZedAI**: `body`


# `br`

**ZedAI**: use `ln` to wrap lines


## `bridgehead`

**ZedAI**: `hd`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

## `byline`

**ZedAI**: `citation` or `p @role="periodicals:byline"`

Notes: for most book (non-article) use cases, `byline` can be `citation`. The exception would be anthologies, for which we can call upon the periodicals vocab and actually use `"role = byline"`. For now, we will use just 1 vocabulary in this converter.

**Adjustments**: 

* Re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`

## `caption`

**ZedAI**: `caption`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`


## `cite`

**ZedAI**: `citation`

**Adjustments**: 

* `title` and `author`, are `span` instead of `p` 
* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`


## `code`

**ZedAI**: `code`

**Adjustments**:

* Inline content model if only children are: `em`, `strong`, `abbr`, `acronym`, `ln`, `sub`, `sup`, `span`, `bdo`, `pagenum`, `text`
* If any of the following occur, make all children block-level: `dfn`, `code`, `samp`, `kbd`, `cite`, `a`, `img`, `imggroup`, `q`, `sent`, `w`, `prodnote`, `annoref`, `noteref`
* deal with `br`


## `col`

**ZedAI**: `col`

**CSS**: `@align`, `@valign`, `@width` (all to go in CSS)


## `colgroup`

**ZedAI**: `colgroup`

**CSS**: `@align`, `@valign`, `@width`


## `covertitle`

**ZedAI**: `block @role='covertitle'`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`


## `dateline`

**ZedAI**: `p @role="time"`

Notes: Used `p` since `ln` is not suitable for all content models (e.g. `level`>`dateline` cannot be translated as `section`>`ln`)

**Adjustments**: 

* re-parent `imggroup` children
* use inline `samp` 
* `br` to `ln`

## `dd`

**ZedAI**: `definition`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` * `br` to `ln` * Re-parent list, dl, div, poem, linegroup, table, sidebar * Re-parent or re-name p, dateline, epigraph, address, author, note


## `dfn`

**ZedAI**: `term`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `div`

**ZedAI**: `block`

**Adjustments**: Normalize block/inline content model: if mixed, wrap inlines in `p` element.


## `dl`

**ZedAI**: `list`

**Adjustments**: either wrap each term, definition in its own `item` element, or combine them into one `item` element


## `docauthor`

**ZedAI**: `p @property="author"`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

## `doctitle`

**ZedAI**: `p @property="title"` or `h @property="title"` when child of the frontmatter

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `dt`

**ZedAI**: `term`

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

## `dtbook`

**ZedAI**: `document`

**Notes**: Reference default ZedAI book profile and default vocabulary.


## `epigraph`

**ZedAI**: `block @role="epigraph"`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`

## `em`

**ZedAI**: `emph`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `frontmatter`

**ZedAI**: `frontmatter`

Notes: A `section` is created automatically containing `doctitle`, `docauthor`, `covertitle`. The reason is that DTBook can have that info free-floating, whereas ZedAI must have only toc/section/biblio/glossary children and so we have to contain that info in a `section` element.


## `h1`

**ZedAI**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `h2`

**ZedAI**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `h3`

**ZedAI**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `h4`

**ZedAI**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

## `h5`

**ZedAI**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `h6`

**ZedAI**: `h`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `hd`

**ZedAI**: `hd`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`

## `head`

**ZedAI**: `head`

**Notes**: 

* ZedAI book profile is hardcoded 
* DTBook `@profile` is not preserved.


## `img`

**ZedAI**: `object`

**Notes**: 

* `dtbook:@longdesc` is a URI which resolves to a `prodnote` elsewhere the book 

## `imggroup`

**ZedAI**: `block` (with `@role='figure'` if a caption is present)

**Notes**: 

* Look at DTBook multiple `prodnote` descriptions 
* There can also be multiple captions, each targeting multiple images


## `kbd`

**ZedAI**: `code`

**Adjustments**: 

* Same as `dtb:code`


## `level`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model

## `level1`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model

## `level2`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model

## `level3`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model


## `level4`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model


## `level5`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model


## `level6`

**ZedAI**: `section`

**Adjustments**: Mixed section/block content model


## `lic`

**ZedAI**: `span`


## `line`

**ZedAI**: `ln`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp`
* deal with `br`


## `linegroup`

**ZedAI**: `block`


## `linenum`

**ZedAI**: `lnum`


## `link`

Discarded


## `li`

**ZedAI**: `item`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`


## `list`

**ZedAI**: `list`

**Adjustments**: wrap `dtbook:prodnote` and `dtbook:hd`

## `meta`

**ZedAI**: `meta`

**Notes**: See https://github.com/daisy/pipeline/issues/56

## `note`

**ZedAI**: `note`

**Adjustments**: Normalize block/inline content model: if mixed, wrap inlines in `p` element.


## `noteref`

**ZedAI**: `noteref`

Dropped: `@type` ("Type provides advisory content MIME type of the targeted id, see RFC1556.")


## `p`

**ZedAI**: `p`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln` 
* re-parent `list`, `dl` children

## `pagenum`

**ZedAI**: `pagebreak`


## `poem`

**ZedAI**: `block @role="poem"`


## `prodnote`

**ZedAI**: `annotation @by="republisher"`

**Adjustments**: 

* `br` to `ln` 
* Normalize block/inline content model: if mixed, wrap inlines in `p` element.


## `q`

**ZedAI**: `quote`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `rearmatter`

**ZedAI**: `backmatter`


## `samp`

**ZedAI**: `block @role="example"`

**Notes**: in DTBook, it seems like `samp` can be both block and inline. There are cases when it is be best translated as a `zedai:span` instead. In this case, the content model needs to be adjusted for inline-only.

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`


## `sent`

**ZedAI**: `s`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `sidebar`

**ZedAI**: `aside @role="sidebar"`

**Adjustments**: 

* Normalize block/inline content model: if mixed, wrap inlines in `p` element. 
* `br` to `ln`


## `span`

**ZedAI**: `span`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln`


## `strong`

**ZedAI**: `emph`

**Adjustments**: 

* re-parent `imggroup` children 
* use inline `samp` 
* `br` to `ln` 
* Re-parent or rename `cite`, `q`


## `sub`

**ZedAI**: `sub`

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

## `sup`

**ZedAI**: `sup`

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

## `table`

**ZedAI**: `table`

**CSS**: `@width`, `@border` (border width), `@cellspacing`, `@cellpadding`

**Adjustments**: 

* Re-parent `caption` children
* Move `col`s into a `colgroup`; zedai does not allow free-floating `col`s. 
* Move `tr` or `pagenum` into a `tbody` if `thead` or `tfoot` is present 
* Re-order `thead`, `tfoot`, `tbody` into `thead`, `tbody`, `tfoot`

**Notes**: See https://github.com/daisy/pipeline/issues/58

## `tbody`

**ZedAI**: `tbody`

**CSS**: `@align`, `@valign`

## `td`

**ZedAI**: `td`

**CSS**: `@align`, `@valign`

**Adjustments**: 

* `br` to `ln` 
* Normalize block/inline content model: if mixed, wrap inlines in `p` element.


## `tfoot`

**ZedAI**: `tfoot`

**CSS**: `@align`, `@valign`


## `th`

**ZedAI**: `th`

**CSS**: `@align`, `@valign`

**Adjustments**: 

* `br` to `ln` 
* Normalize block/inline content model: if mixed, wrap inlines in `p` element.


## `thead`

**ZedAI**: `thead`

**CSS**: `@align`, `@valign`


## `title`

**ZedAI**: `p @property='title' @about='IDREF'`

**Adjustments**: 

* re-parent `imggroup` children 
*  use inline `samp` 
* `br` to `ln`


## `tr`

**ZedAI**: `tr`

**CSS**: `@align`, `@valign`


## `w`

**ZedAI**: `w`

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

## Common Attributes

* `id` becomes `xml:id`
* `xml:space` is copied
* `class` is copied
* `xml:lang` is copied
* `dir` becomes `its:dir`
* `showin` is discarded
* `title` and `name` are discarded
