# pipeline-mod-nota
Nota specific modules for the DAISY Pipeline 2

## Release procedure
- View changes since previous release and update version number according to semantic versioning.

  ```sh
  git diff v1.0.0...HEAD
  VERSION=1.0.1
  ```

- Create a release branch.

  ```sh
  git checkout -b release/${VERSION}
  ```
  
- Resolve snapshot dependencies and commit.
- Set the version in pom.xml to `${VERSION}-SNAPSHOT` and commit.
- Make release notes and commit. (View changes since previous release with `git diff v1.1.0...HEAD`
  and look for relevant Github issues on [https://github.com/search](https://github.com/search))
- Perform the release with Maven.

```sh
  mvn clean release:clean release:prepare -DpushChanges=false
  mvn release:perform -DlocalCheckout=true
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
- Release the artifact on https://oss.sonatype.org  and close pull request.

  ```sh
  ghi comment --close -m released ${ISSUE_NO}
  ```
  
- Push the tag.

  ```sh
  git push origin v${VERSION}
  ```
  
- Add the release notes to http://github.com/snaekobbi/pipeline-mod-nota/releases/v${VERSION}.
