## Regenerate JavaScript files

```sh
mvn generate-resources -Pgenerate-javascript
rm src/main/resources/javascript/*
mv target/generated-resources/javascript/* src/main/resources/javascript/
```
