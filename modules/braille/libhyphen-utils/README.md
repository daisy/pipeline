libhyphen-utils
===============

Building blocks related to the hyphenation library
[Hyphen][libhyphen], used in OpenOffice/LibreOffice, Firefox,
Chromium, etc.

[API](src/main)


## Regenerating table files

```sh
mvn generate-resources -Pgenerate-table-files
rm src/main/resources/hyphen/*
mv target/generated-resources/hyphen/* src/main/resources/hyphen/
```


[libhyphen]: http://sourceforge.net/projects/hunspell
