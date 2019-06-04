Provides a function `tr:sequence-type` that takes one `item()` as an argument and returns the sequence type of the item as a string.

Invoke it like this (assuming you have an XSLT 2 processor front-end script called `saxon` in the path):
```
saxon -xsl:xslt-util/sequence-type/xsl/test.xsl -it:test
```

The result should look like:
```
1 integer
1 integer
1 integer
1 double
true boolean
1 string
file:/…/xslt-util/sequence-type/xsl/test.xsl anyURI
<foo bar="baz"/>document-node(element(foo))
<foo bar="baz"/>document-node(element(foo))
<foo bar="baz"/>element(foo)
<?attribute name="foo" value="bar"?>attribute(foo)
<?foo bar?>processing-instruction(foo)
<!--foo=bar-->comment()
```
