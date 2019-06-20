[jhyphen][]
===========

jhyphen provides minimal Java bindings to Hyphen, the [hyphenation library from hunspell][hunspell]. The bindings are based on [JNA][].
Installation
------------

`mvn test` will perform unit tests.
`mvn package` will create jhyphen.jar.

Release procedure
-----------------
- Version number according to semantic versioning.

  ```sh
  VERSION=1.0.0
  ```
  
- Create a release branch.

  ```sh
  git checkout -b release/${VERSION}
  ```
  
- Set the version in pom.xml to `${VERSION}-SNAPSHOT` and commit.
- Perform the release with Maven.

  ```sh
  mvn clean release:clean release:prepare
  mvn release:perform
  ```
  
- Push and make a pull request (for turning an existing issue into a PR use the `-i <issueno>` switch).

  ```sh
  git push origin release/${VERSION}:release/${VERSION}
  hub pull-request -b daisy:master -h daisy:release/${VERSION} -m "Release version ${VERSION}"
  ```
  
- Stage the artifact on https://oss.sonatype.org and comment on pull request.

  ```sh
  ghi comment -m staged ${ISSUE_NO}
  ```
  
- Test and stage all projects that depend on this release before continuing.
- Release the artifact on https://oss.sonatype.org and close pull request.

  ```sh
  ghi comment --close -m released ${ISSUE_NO}
  ```
  
- Push the tag.

  ```sh
  git push origin v${VERSION}
  ```


Authors
-------

+ [Bert Frees](http://github.com/bertfrees)
+ [Christian Egli](http://github.com/egli)

Copyright and license
---------------------

Copyright 2012, 2013 [Swiss Library for the Blind, Visually Impaired and Print Disabled][sbs]

Licensed under GNU Lesser General Public License as published by the Free Software Foundation, either [version 3][lgpl] of the License, or (at your option) any later version.

[jhyphen]: http://github.com/sbsdev/jhyphen
[hunspell]: http://hunspell.sourceforge.net/
[jna]: https://github.com/twall/jna
[sbs]: http://www.sbs.ch
[lgpl]: http://www.gnu.org/licenses/lgpl.html
