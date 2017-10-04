# pipeline-mod-dedicon

Dedicon specific modules for the DAISY Pipeline 2

## Release procedure

- View changes since previous release and update version number according to semantic versioning.  
  `git diff v1.1.0...HEAD`  
  `VERSION=1.2.0`
- Create a release branch.  
  `git checkout -b release/${VERSION}`
- Resolve snapshot dependencies and commit.
- Set the version in pom.xml to `${VERSION}-SNAPSHOT` and commit.
- Make release notes and commit.  
  (View changes since previous release with `git diff v1.1.0...HEAD`
  and look for relevant issues in [Jira](https://dedicon.atlassian.net/projects/PI3))
- Perform the release with Maven.  
  `mvn clean release:clean release:prepare`  
  `mvn release:perform`
- Push and make a pull request (for turning an existing issue into a PR use the `-i <issueno>` switch).  
  `git push origin release/${VERSION}:release/${VERSION}`  
  This command outputs an URL for creating a pull request in BitBucket.
- Create the pull request.  
  Open the URL in a browser. The pull request is from the release branch to the master branch.
- Locate the artifact on https://oss.sonatype.org.  
  Log in on https://oss.sonatype.org, in the left pane select `Staging repositories` and in the top-right corner search for `nldedicon`.
- Test and stage all projects that depend on this release before continuing.
- Release the artifact on https://oss.sonatype.org  
  Close and release the staging repository.
  (For a detailed description, see [http://central.sonatype.org/pages/releasing-the-deployment.html](http://central.sonatype.org/pages/releasing-the-deployment.html))
- Merge the pull request.
- Push the tag.  
  `git push origin v${VERSION}`

## Preliminaries

### Software installations
- Git
- GnuPG

### System variables
- Path: Should contain the location of Git and GnuPG, for instance:  
  C:\Program Files\Git\cmd  
  C:\Program Files (x86)\gnupg\bin

### Permissions
- Valid usercode/password on https://bitbucket.org/dedicon
- Valid usercode/password on https://oss.sonatype.org, with write access to nl.dedicon (please contact Joost Aalbers for this)  
  These must be registered in settings.xml in your local Maven folder.  
  For instance, file C:\Users\prambags\\.m2\settings.xml looks like:  

```
    <settings>
      <servers>
        <server>
          <id>sonatype-nexus-staging</id>
          <username>paulrambags</username>
          <password>xxxxxxxx</password>
        </server>
      </servers>
    </settings>
```

- A private and public key (generated with GnuPG).  
  The public key should be published with the correct ID.  
  This ID is shown by https://oss.sonatype.org upon closing a staging repository.  
  E.g.: `gpg --send-keys 82632f50954b2195`  
  This sends the public key to key server hkps://hkps.pool.sks-keyservers.net.
