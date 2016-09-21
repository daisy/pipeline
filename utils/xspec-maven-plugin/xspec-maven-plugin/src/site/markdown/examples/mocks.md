## Mocking external dependencies

It is not unusual for XSLT stylesheet to import functions or templates from an
external resource. For example, the following stylesheet tries to import an
XSLT stylesheet declaring a `f:hello` utility function:

```xml
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:f="http://www.example.org/functions" version="2.0">
    
    <xsl:import href="http://www.example.org/functions.xsl"/>
    
    <xsl:template match="hello">
        <hello><xsl:value-of select="f:hello('World')"/></hello>
    </xsl:template>
    
</xsl:stylesheet>
```

Testing this XSLT would work if an internet connection is available when
running the tests, if the imported XSLT resource is effectively available at
the declared URL, and if it does not in turn depend on inaccessible remote
resources.

For pure unit tests, it is often preferrable to not depend on such external
dependencies. The XSpec Maven plugin allows to declare mock implementations of
remote resources using a local XML Catalog. For instance, to test the XSLT
defined above, a file named `catalog.xml` can be placed in the same directory
as the XSpec test description with the following content:

```
<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
    <uri name="http://www.example.org/functions.xsl" uri="mock-functions.xsl"/>
</catalog>
```

When running the XSpec tests, the plugin will then use the `mock-functions.xsl` implementation of the dependency `http://www.example.org/functions.xsl`:

```
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:f="http://www.example.org/functions"
    version="2.0">

    <xsl:function name="f:hello" as="xs:string">
        <xsl:param name="hello" as="xs:string"/>
        <xsl:sequence select="concat('Hello, ',$hello,'!')"/>
    </xsl:function>
    
</xsl:stylesheet>
```

In the end, the layout of the Maven project described in the example would be:

```
pom.xml
+ src
  + main
    + xslt
      - say-hello.xsl
  + test
    + xspec
      - catalog.xml
      - mock-functions.xsl
      - say-hello.xspec
```