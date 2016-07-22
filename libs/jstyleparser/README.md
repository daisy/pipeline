[![Build Status](https://travis-ci.org/snaekobbi/jStyleParser.png)](https://travis-ci.org/snaekobbi/jStyleParser)

jStyleParser
============

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/radkovo/jStyleParser?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

jStyleParser parses CSS2 and CSS3 into structures which can be assigned to DOM elements.
While handling errors, it is a user agent conforming to specification.

All the source code of jStyleParser itself is licensed under the GNU Lesser General
Public License (LGPL), version 3. A copy of the LGPL can be found 
in the LICENSE file.

See the project page for more information and downloads:
[http://cssbox.sourceforge.net/jstyleparser](http://cssbox.sourceforge.net/jstyleparser)

Release procedure
=================

- Remove "SNAPSHOT" from the version number in `pom.xml`.
- Build and deploy the artifact to Sonatype with `mvn clean deploy -Psonatype-oss-release`
- Stage the artifact at https://oss.sonatype.org.
- Do all the necessary testing.
- Release artifact at https://oss.sonatype.org.
- `git tag` the last commit. The tag format should be "jStyleParser-1.20-p4".
- Bump the version number in `pom.xml` to the next snapshot.
