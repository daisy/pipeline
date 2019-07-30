liblouis-utils
===============

Building blocks related to the Braille translation library
[liblouis][].

[API](src/main)


## Regenerating files for math conversion

When moving to a newer version of Liblouisutdml, you may want to
update the liblouis files:

- Possibly edit the `filter_liblouis_files.sh` script.
- Generate the files:
  ```sh
  mvn process-sources -Pgenerate-liblouis-mathml-files
  ```
- Move the generated files to the `src` directory and check them in:
  ```sh
  mvn process-sources -Pgenerate-liblouis-mathml-files
  mv target/generated-resources/lbu_files/* src/main/resources/lbu_files/mathml/
  ```


[liblouis]: https://code.google.com/p/liblouis
