[liblouis-java][]
=================

[![Build Status](https://travis-ci.org/liblouis/liblouis-java.png?branch=master)](https://travis-ci.org/liblouis/liblouis-java)

Java bindings to [liblouis][], an open-source braille translator and back-translator.
The bindings are based on [JNA][].

Building
--------

 * `mvn clean` will remove generated files
 * `mvn package` will build the JAR

Deploying
--------

 * `mvn deploy -Psonatype-oss-release` will deploy to Sonatype OSS
 
Authors
-------

+ [Bert Frees][bert]

License
-------

Copyright 2012 [Bert Frees][bert]

This program is free software: you can redistribute it and/or modify
it under the terms of the [GNU Lesser General Public License][lgpl]
as published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

[liblouis-java]: http://github.com/bertfrees/liblouis-java
[liblouis]: http://code.google.com/p/liblouis/
[jna]: https://github.com/twall/jna
[bert]: http://github.com/bertfrees
[lgpl]: http://www.gnu.org/licenses/lgpl.html
