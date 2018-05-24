# XSweet Header Promotion

Processing XML in the XHTML namespace, producing the same format, except with `h1-h6` where headers should be.

## Overview

XSweet supports three methods of header promotion, i.e. inferring which lines of text (nominal paragraphs) are candidated for promotion into HTML headers (`h1-h6` elements). Of the three methods, however only one (outline-level based) is a straightforward transformation: the other  higher level tranformations (invoking stylesheets generated dynamically). Accordingly, to make things easier, all this logic is wrapped in a single "header promotion" XSLT, which can choose the best available method and then orchestrate the transformations necessary to accomplish it. Alternatively, you can use a runtime switch to set the header promotion method.

For debugging, each of these processes can also be run separately or discretely, although typically they will also produce intermediate results (outputs) that may have to be managed.

The problem addressed by this transformation is worse than non-trivial: it is *sometimes* (relatively) trivial to infer headers but it is also frequently quite difficult or impossible to do so, unassisted or programmatically. It is also typically difficult or impossible to tell in advance which category a particular word processor document will fall into, sight unseen. The transformation itself may not always be difficult; but the contexts that determine what is 'good' and 'correct' for given inputs, are complex.

These complexities are reflected in the (three) ways XSweet supports producing HTML headers, in the flat series of "one paragraph after another" that results from straight-up conversion of WordML inputs, into markup.

## Configuration

The master stylesheet: is `header-promotion-CHOOSE.xsl`. Using it plus a single runtime parameter setting, you can designate the header promotion strategy you prefer. If you fail to designate one, that is also okay, the XSLT will do its best.

The flag (runtime parameter) is called `method` with any of three values recognized:

 * `method=outline-level` indicates headers will be assigned based on assigned outline level

 * `method=ranked-format` indicates headers will be assigned based on a comparative analysis and ranking of paragraphs based on their formatting

 * `method=my-styles.xml` indicates headers will be mapped (assigned to paragraphs) on the basis of assigned styles and/or contents, matching with regular expressions, as indicated in a configuration file. `my-styles.xml` will be a configuration file you borrow or create for mappings, which recognizes the styles you designate. Either named styles in Word (which result in `@class` in HTML produced by XSweet), or contents (such as a paragraph whose only contents is "Introduction"), may be matched.

Note: the last method can be combined with [../html-tweak](html-tweak) as a pre-process to map not only styles, but also patterns of formatting appearing in the HTML, into named and classed elements in an enriched and cleaned up HTML result.

When invoked with no method setting, the method inspects the documents for its use of outline levels. If they appear to have been used (more than one such line appears), that method is applied; if not, the method falls back to "ranked format"

## Details

Depending on the value of the switch at runtime, an input document will be processed according to one of the following approaches. Note that any of them may also be achieved by running its XSLT (or sequence) standalone, i.e. without the orchestration stylesheet (`header-promotion-CHOOSE.xsl`).

### `method=outline-level`

The input document should be processed with the XSLT `outline-headers.xsl`; the resulting document is a copy with headers promoted.

### `method=ranked-format`

The input document is processed with the XSLT `digest-paragraphs.xsl`. The results of this transformation are then transformed using `make-header-escalator.xsl`. The resulting XSLT is applied to the original input document -- producing a result copy, with headers promoted.

### `method=my-styles.xml`

The file `my-styles.xml` (or other XML document so designated) is processed with the XSLT `make-header-mapper.xsl`; the resulting XSLT transformation is be applied to the source document, producing a copy with promoted headers.

## More details on each of the methods

### Outline levels in WordML

Some writers actually use the outlining functionality in Word, either deliberately, or implicitly through careful use of named styles. In such cases (probably a small subset) reasonably good fidelity to relative levels of headers in the document, can be well assured; so these values are often worth using when they appear.

The translation is fairly literal: outline level 0 becomes h1, outline level 1 becomes h2 etc.

### Ranking formats

A rough and ready heuristic is possible making distinctions among paragraphs that are apparently formatted as headers (though not exclusively so). The document is submitted to an analytical process that "boils down" its paragraph formats into those more likely to represent headers (as determined by a kind of brute-force hueristic, tuned in the field), and transforming them into such. So a meta-transformation is called for: a pipeline is applied producing analytical results, which are "reinflected" as a transformation, to be applied to the original document.

XProc specifications are given to provide for this behavior.

The stylesheet `digest-paragraphs.xsl` is where the action happens in heuristic analysis, and where weights and considerations may be adjusted. Currently, relative font size and properties as well as line length and "stickiness" (whether occurring solo or in groups), are considered.

### Configure your own mapping

A standoff configuration file in XML such as the `config-mockup.xml` file shows how a mapping configuration may be specified, which is able to pick up paragraphs by virtue of either style names, or contents (such as "Introduction" and "Conclusions"). This method offers considerable power (in a relatively simple way) to those who are able, in advance, to constrain the names and usage of Styles in their Word documents (most especially as related to headers).

The rules and semantics of this format are documented with comments in the example. The XSLT make-header-mapper-xslt.xsl produces an XSLT from the configuration file, which is applied to the html file provided as main input. (This subpipeline is implemented in the master XSLT, but not in XProc at time of writing.)

## Supporting XSLT

| Transformation  | Function |
|--|--|
| `outline-headers.xsl` | Promotes paragraphs to headers based on outline level | 
| `digest-paragraphs.xsl` | Produces a ranked analysis of paragraphs by property, as input to `make-header-escalator.xsl` |
| `make-header-escalator-xslt.xsl` | Consumes the ranked inputs of `digest-paragraphs.xsl` to produce an XSLT for header promotion |
| `make-header-mapper-xslt.xsl` | Consumes a "regex-matching" XML specification document such as `config-mockup.xml` to produce an XSLT for header promotion |
| `config-mockup.xml` | Demonstration of configuration file showing mappings of style or contents into header element types. |

