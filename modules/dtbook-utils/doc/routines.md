<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="DTBook cleaning routine details"/>

# DTBook cleaning routine details

This page details the cleaning process applied by the DTBook Cleaner
script for each set of routines provided.

## Table of contents

{{>toc}}

-----------

### Repair routines

The "repair" set of routines repairs some structural errors when encountered :
- Removes `levelx` if it has descendant headings of x-1 
  (this simplifies later steps). <br/>
  Note: Level normalizer cannot fix `level1/level2/level1`
- Splits a level into several levels on every additional heading on the same
  level
- Add levels where needed.
- Changes a `hx` into a `p` with `@class="hx"` if parent isn't `levelx`<br/>
  Note: "Remove illegal headings" cannot handle `hx` in inline context.
  Support for this could be added.
- Removes nested `p`
- Adds an empty `p`-tag if `hx` is the last element
- Apply fixes for lists:
    - wraps a list in `li` when the parent of the list is another list
    - adds `@type` if missing (default value is `"pl"`)
    - corrects `@depth` attribute
    - removes `@enum` attribute if the list is not ordered
    - removes `@start` attribute if the list is not ordered
- `idref` must be present on `noteref` and `annoref`. Add `idref` if missing or 
  change if empty.<br/>
  The value of the `idref` must include a fragment identifier. Add a hash mark 
  in the beginning of all idref attributes that don't contain a hash mark.
- Removes
    - empty/whitespace `p` except when
        - receded by `hx` or no preceding element and parent is a level
        - and followed only by other empty `p`
    - empty/whitespace `em`, `strong`, `sub`, `sup`
    - empty/whitespace elements that must have children.
- Update the @page attribute to make it match the contents of the pagenum
  element.
    - If `@page="normal"` but the contents of the element doesn't match "normal"
      content, the @page attribute is changed to:
        - `@page="front"` if the contents is roman numerals and the pagenum
          element is located in the frontmatter of the book
        - `@page="special"` otherwise
    - If `@page="front"` but the contents of the element doesn't match "front"
      content (neither roman nor arabic numerals), the @page attribute is
      changed to "special"
- Fix metadata case errors
    - remove unknown dc-metadata
    - add `dtb:uid` (if missing) from dc:Identifier
    - add `dc:Title` (if missing) from doctitle
    - add auto-generated `dtb:uid` if missing (or if it has empty contents)

### Tidy routines

The "tidy" set of routines removes empty elements and reorganize misplaced
elements:
- Removes
    - empty/whitespace `p` except when
        - preceded by `hx` or no preceding element and parent is a level
        - and followed only by other empty `p`
    - empty/whitespace `em`, `strong`, `sub`, `sup`
- Moves
    - `pagenum` inside `h[x]` before `h[x]`
    - `pagenum` inside a word after the word
- Update the `@page` attribute to make it match the contents of the `pagenum`
  element.
    - If `@page="front"` but the contents of the element is an arabic number,
      the `@page` attribute is changed to `"normal"`<br/>
      (Note: arabic numbers are theoretically allowed from `@page="front"`, but
      are not considered standard practice by many)
    - If `@page="special"` but the element has no content, adds a dummy content
      ("page break").
- Removes otherwise empty `p` or `li` around `pagenum` (except `p` in `td`)
- Inserts `docauthor` and `doctitle` if a `frontmatter` exists without those
  elements
- Removes existing whitespace nodes and indents output to aid debugging.
    - Does not remove whitespace or apply indentation in inline context
    - Does not apply indentation when number of children is 1

### Narrator routines

the "narrator" set of routines tries to optimize the dtbook for text-to-speech
processes:
- Adds `dc:Language`, `dc:Date` and `dc:Publisher` to dtbook, if not present in
  input, or given but with null/whitespace only content values
- Removes `dc:description` and `dc:subject` if not valued
- Removes `dc:Format` (will be added by the fileset generator)
- Prepare the DTBook for audio synthesis:
    - Don't allow `h[x+1]` in `level[x+1]` unless `h[x]` in `li[x]` is present
        - This fix assumes headings are not empty (e.g. empty headings were
          removed by a previous fix)
    - Every document needs at least one heading on `level1`
        - This fix assumes headings are not empty (e.g. empty headings were 
          removed by a previous fix)
    - No `list` or `dl` inside `p` :
        - Breaks the parent paragraph into a sequence of paragraphs, list and dl
        - Each newly created paragraph has the same attributes as the original
          one
        - New paragraph IDs are created if necessary
        - The original paragraph ID is conserved for the first paragraph created
- Adds the `dc:Title` meta element and the `doctitle` frontmatter element, if
  not present in input, or given but with null/whitespace only content values.
  <br/>Title value is taken:
    - from the `dc:Title` metadata if it is present
    - or else from the first `doctitle` element in the `frontmatter`
    - or else from the first heading 1.
