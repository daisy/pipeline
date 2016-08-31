# Documentation contract

- Documentation must be written in HTML (with file extension `.html`)
  or [Markdown][] (file extension `.md`). Other files are allowed but
  are considered static and get no special treatment.
- Layout/styling of HTML files is the responsibility of the
  author/generator. The final rendering of the HTML file is determined
  solely by the HTML itself. The processor only determines the
  viewport. No additional CSS is applied.
- Layout/styling of Markdown files is the responsibility of the
  processor. This means that the Markdown should ideally not include
  any CSS. The rendering may be influenced by using standardised
  classes (defined below). Because
  [Markdown may contain HTML fragments](http://daringfireball.net/projects/markdown/syntax#html),
  HTML that needs the same treatment should be given the extension
  `.md`.
- Documentation may contain metadata defined using [RDFa][]. A list of
  recognised types and properties is defined below.
- Some metadata is automatically inferred if not present as RDFa.
  - The `dc:title` property of a file is set to the string content of
    the `title` element, or if there is no such element, the string
    content of the first `h1`, `h2`, `h3`, `h4`, `h5` or `h6` element,
    or if there is no such element, the file name without extension.
- Processors must handle hyperlinks (anchor elements) according to the
  following rules:
  - Relative links are adapted when the location of documentation
    files relative to each other changes.
  - Links are automatically changed when the element has one of the
    special classes `source`, `apidoc` or `userdoc`. These classes
    match the RDF types `dp2:source`, `dp2:apidoc`, `dp2:userdoc`. If
    the resource the original link points to is documented by (via the
    property `dp2:doc`) a resource of the indicated type, that
    resource becomes the new target of the link. The same happens if
    the original HREF _is an alias of_ (via the `dp2:alias` property)
    a resource that is documented by a resource of the indicated type.
  - Whenever a link is changed, any "rel", "rev" and "property"
    attributes on the anchor element are removed in order to not
    produce incorrect RDFa metadata.
- Documentation files may contain [Mustache][] tags. A list of
  available variables, partials and lambdas is defined below.


## RDF grammar

### Pre-defined prefixes

- `rdf`: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
- `dc`: <http://purl.org/dc/elements/1.1/>
- `xsd`: <http://www.w3.org/2001/XMLSchema#>
- `dp2`: <http://www.daisy.org/ns/pipeline/>

### Types

- `dp2:userdoc`: Documentation file of type "user documentation".
- `dp2:apidoc`: Documentation file of type "API documentation",
  targeted at developers (e.g. Javadoc).
- `dp2:source`: Documentation file that includes source code.
- `dp2:script`: XProc file that implements a Pipeline script.

### Properties

- `dp2:doc`: The object (documentation file) documents the subject
  (source file).
- `dp2:alias`: The object (URI) is an alias of the subject (source
  file). This can be used for example to link source files with URIs
  defined in the catalog XML.
- `dp2:option`: The object (anonymous node) is an option of the
  subject (script).
- `dp2:input`: The object (anonymous node) is an input port of the
  subject (script).
- `dp2:id`: The object (string) is the name/ID of the subject (option
  or input port).
- `dp2:name`: The object (string) is the nice name of the subject
  (option or input port).
- `dp2:desc`: The object (string) is the description of the subject
  (option or input port).
- `dp2:required`: The object (boolean) determines whether the subject
  (option) is required or not. Defaults to false.
- `dp2:default`: The object (string) is the default value of the
  subject (option).
- `dp2:sequence`: The object (boolean) determines whether the subject
  (input port) accepts a sequence of documents or not. Defaults to
  false.

## Special classes

- `source`, `apidoc`, `userdoc`: These classes influence the special
  processing of anchor elements (see above).

## Mustache tags

### Variables

- `options`: The options of a script. This variable is only available
  in the context of script documentation. The data is built from
  existing RDF metadata and is structured as follows:

  ~~~yml
  - option1:
      name: 'Option 1'
      desc: '...'
      required: true
  - option2:
      name: 'Option 2'
      required: false
      default: 'foo'
  - all:
    - id: option1
      name: 'Option 1'
      desc: '...'
      required: true
    - id: option2
      name: 'Option 2'
      required: false
      default: 'foo'
  ~~~

  `desc` may contain HTML (so you probably want to use the triple
  mustache to render it: `{{{desc}}}`).

- `inputs`: The input ports of a script. This variable is only
  available in the context of script documentation. The data is built
  from existing RDF metadata and is structured as follows:

  ~~~yml
  - source:
      name: 'Source'
      desc: '...'
      sequence: true
  - all:
    - id: source
      name: 'Source'
      desc: '...'
      sequence: true
  ~~~

  Here also `desc` may contain HTML.
  
### Partials

- `toc`: Inserts a table of contents from the existing headings in the
  document. What exactly the TOC looks like is up to the processor.
- `synopsis`: Inserts an overview of the input ports and options of a
  script. This tag is only available in the context of script
  documentation. The synopsis is built from existing RDF
  metadata. What exactly the synopsis looks like is up to the
  processor.

### Lambdas

- `sparql`: Evaluates a [SPARQL][] expression against existing RDF
  metadata and for each solution of the query, evaluates a Mustache
  expression with the SPARQL variables bound to identically named
  Mustache variables. A call might look like this:
  
  ~~~mustache
  {{#sparql}}
  SELECT ?href ?title WHERE {
    []    a        dp2:script ;
          dp2:doc  ?href .
    ?href dc:title ?title ;
          a        dp2:userdoc .
  }
  ORDER BY ?title
  - [{{title}}]({{href}})
  {{/sparql}}
  ~~~


[Markdown]: http://daringfireball.net/projects/markdown/
[RDFa]: https://www.w3.org/TR/2015/REC-rdfa-core-20150317/
[Mustache]: http://mustache.github.io/mustache.5.html
[SPARQL]: https://www.w3.org/TR/2013/REC-sparql11-query-20130321/
