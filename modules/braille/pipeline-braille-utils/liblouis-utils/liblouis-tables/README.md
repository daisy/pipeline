## Regenerating table files

In order to update to a new version of Liblouis, update the properties
`liblouis.version`, `liblouis.tarball.url` and `liblouis.tarball.sha1`
in the POM, then run:

```sh
find src/main/resources/tables -depth 1 -type file -exec rm {} \;
mvn generate-resources -Pgenerate-liblouis-files
```
