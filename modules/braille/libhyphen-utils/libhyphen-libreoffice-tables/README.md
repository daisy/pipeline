## Regenerating table files

```sh
mvn generate-resources -Pgenerate-table-files
rm src/main/resources/hyphen/*
mv target/generated-resources/hyphen/* src/main/resources/hyphen/
```
