# XSLT-based catalog resolver


The XSLT-based catalog resolver was originally developed to overcome a
limitation of Saxon’s default behavior: We tried to exploit the
recursive wildcard search of
[`collection()`](http://www.saxonica.com/documentation/index.html#!sourcedocs/collections)
URLs, but discovered that this would only work for `file:` URLs, while
the URLs in our code were abstract `http:` URLs. This would be ok if
Saxon sent the URLs to the catalog resolver *before* deciding whether
recursive wildcard search was feasible. (It should be, post-resolution,
because they are `file:` URLs then.)

We might have written our own URI resolver in Java, but this would make
deployment more difficult, and it would probably require that we run
commercial versions of Saxon everywhere which would be to high a hurdle
for the adoption of transpect as an open-source, (almost) ready-to-run
framework.

So we wrote this XSLT-based resolver. It doesn’t implement the [whole
catalog
standard](https://www.oasis-open.org/committees/download.php/14810/xml-catalogs.pdf),
but the elements that are most important to our pipelines, namely `uri`,
`rewriteURI`, and `nextCatalog`.

We usually rely on the standard catalog resolvers (Apache Common or
Norman Walsh’s). We use the XSLT-based resolver only for collections
with wildcard `http:` URLs, for reverse resolution, and when we just
need the local name for a resource without actually retrieving the
resource, for example when we prepare the packing list for an EPUB.
Fonts in our font library are referenced by canonical URL but must be
included from a working copy of their [repository
location](https://github.com/transpect/fontlib/).


