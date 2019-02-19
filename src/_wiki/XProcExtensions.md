# XProc Extensions

DAISY Pipeline 2 supports a number of extensions to XProc, in the form of [extension attributes](https://www.w3.org/TR/xproc/#extension-attributes). They have the namespace `http://www.daisy.org/ns/pipeline/xproc`. Some are applicable only to scripts and are meant to provide metadata about the script, such as data types for option. Some other, like `px:message`, are applicable everywhere.

We also list some standard XProc elements and attributes that are treated specially by DAISY Pipeline.

## `px:sequence` attribute

## `px:media-type` attribute

## `px:type` attribute

## `px:message` and `px:message-severity` attributes

## `px:progress` attribute

This attribute is used to annotate an XProc pipeline with information about how different steps in that pipeline relate to each other in terms of time it takes to complete them. This information is used to provide progress indication of jobs. For each step, the fraction relative to its current [subpipeline](https://www.w3.org/TR/xproc/#dt-subpipeline) can be expressed with a decimal number between 0 and 1 (`.666`), a percentage (`66%`), or a fraction (`2/3`).

The `px:progress` attribute is supported on the following elements:

- [`p:group`](https://www.w3.org/TR/xproc/#p.group) (except within a `p:try`)
- [`p:choose`](https://www.w3.org/TR/xproc/#p.choose)
- [`p:when`](https://www.w3.org/TR/xproc/#p.when)
- [`p:otherwise`](https://www.w3.org/TR/xproc/#p.otherwise)
- [`p:try`](https://www.w3.org/TR/xproc/#p.try)
- [`p:for-each`](https://www.w3.org/TR/xproc/#p.for-each)
- [`p:viewport`](https://www.w3.org/TR/xproc/#p.viewport)
- a standard step ([`p:foo`](https://www.w3.org/TR/xproc/#std-components))
- a user defined step

The `px:progress` attribute supports variable substitution via the curly brackets syntax from [attribute value templates](https://www.w3.org/TR/xslt20/#attribute-value-templates) in XSLT, e.g. `px:progress="{$some-var}"`. This way progress information can even be determined dynamically during step execution.

A simple example:

~~~xml
<p:label-elements match="..." attribute="..." label="..." px:progress="1/3"/>
<p:for-each px:progress="2/3">
   <p:xslt px:progress="1/2">
      ...
   </p:xslt>
   <p:choose px:progress="1/2">
      <p:when test="">
         <p:add-attribute match="..." attribute-name="..." attribute-value="..." px:progress="1/4"/>
         <p:delete match="..." px:progress="3/4"/>
      </p:when>
      <p:otherwise>
         <p:identity/>
      </p:otherwise>
   </p:choose>
</p:for-each>
~~~

## `p:documentation` element

## `p:option/@select` attribute

## `p:option/p:pipeinfo/px:type` element
