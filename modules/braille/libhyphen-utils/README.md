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
mv src/main/resources/tables/hyph/hyph-fi.dic src/main/resources/tables/hyph_fi.dic
rm -r src/main/resources/tables/hyph
```


[Hyphen]: http://hunspell.github.io/
