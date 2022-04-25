libhyphen-utils
===============

Building blocks related to the [Hyphen][] hyphenation library, used in
OpenOffice/LibreOffice, Firefox, Chromium, etc.

[Module content](src/main)


## Regenerating table files

```sh
mvn generate-resources -Pgenerate-table-files
mv target/generated-resources/tables/* src/main/resources/tables/
cp src/main/resources/tables/hyph_no_NO.dic src/main/resources/tables/hyph_nn_NO.dic
cp src/main/resources/tables/hyph_no_NO.dic src/main/resources/tables/hyph_nb_NO.dic
```


[Hyphen]: http://hunspell.github.io/
