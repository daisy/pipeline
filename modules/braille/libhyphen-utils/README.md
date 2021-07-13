libhyphen-utils
===============

Building blocks related to the [Hyphen][] hyphenation library, used in
OpenOffice/LibreOffice, Firefox, Chromium, etc.

[Module content](src/main)


## Regenerating table files

```sh
mvn generate-resources -Pgenerate-table-files
rm src/main/resources/tables/*
mv target/generated-resources/tables/* src/main/resources/tables/
```


[Hyphen]: http://hunspell.github.io/
