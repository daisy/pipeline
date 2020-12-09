libhyphen-utils
===============

Building blocks related to the [Hyphen][] hyphenation library, used in
OpenOffice/LibreOffice, Firefox, Chromium, etc.

[Module content](src/main)


## Regenerating table files

```sh
mvn generate-resources -Pgenerate-table-files
rm src/main/resources/hyphen/*
mv target/generated-resources/hyphen/* src/main/resources/hyphen/
```


[Hyphen]: http://hunspell.github.io/
