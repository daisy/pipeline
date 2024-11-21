[braille-css-java][]
====================

Implementation of [Braille CSS][braille-css] in Java/ANTLR.

Release procedure
-----------------
- Version number according to semantic versioning.

  ```sh
  VERSION=1.3.0
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
  hub pull-request -b snaekobbi:master -h snaekobbi:release/${VERSION} -m "Release version ${VERSION}"
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
  

[braille-css]: http://braillespecs.github.io/braille-css
[braille-css-java]: http://github.com/daisy/braille-css-java
