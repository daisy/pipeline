<!-- [![Build Status](https://travis-ci.org/daisy/pipeline-webui.png?branch=master)](https://travis-ci.org/daisy/pipeline-webui) -->

# Pipeline 2 Web UI

This project provides a Web User Interface for the DAISY Pipeline 2, developed with the Play! framework.

## Publishing builds

*NOTE*: To build RPM packages in Ubuntu, you first need to install the `rpm` package using `sudo apt-get install rpm`.

### 1. Prepare the release

In `build.sbt`, update the `version := "..."`.
The versioning should follow semantic versioning rules.
Snapshots should have a `-SNAPSHOT` version suffix.

### 2. Perform the release

#### Snapshot version

If you just want to publish a snapshot version of a debian package, simply run:

```
./activator clean universal:publish debian:publish
```

That will upload snapshot versions to sonatype.

#### Release version

If you want to publish a release version, you need to sign the files.
The following are instructions on how to do this manually.

<small>(We could possibly find out how to configure sbt so that it
automatically signs the files for us, but for now this is fine.)</small>

To build the Windows MSI installer, you need to have [WiX Toolset](http://wixtoolset.org/)
installed, and run the following command from Windows:

```
.\activator clean windows:packageBin
```

If you do this on a separate computer / OS; copy the resulting MSI-file
from `target\windows` back to the `target` folder (i.e. in Linux) with
the rest of the packaged files before continuing. Make sure you're on the
same git commit *and tag* in both git folders.

To build the DEB and RPM packages, and then sign everything (including the
MSI assuming it's default location of `target/windows`) run the following:

```
./activator clean debian:debianSign rpm:packageBin
gpg -ab target/*.pom
gpg -ab target/*.deb
gpg -ab target/rpm/RPMS/noarch/*.rpm
gpg -ab target/windows/*.msi
```

Then:

- Log into the [Sonatype Nexus Repository Manager web interface](https://oss.sonatype.org/#stagingRepositories).
- Click "Staging Upload"
- Select the upload mode "Artifact(s) with a POM"
- Using the "Select POM to Upload..." button, upload the `target/*pom` file
- One by one, using the "Select Artifact(s) to Upload..." button
  - Upload the `target/*.pom.asc` file, then click "Add Artifact"
  - Upload the `target/*.deb` file, then click "Add Artifact"
  - Upload the `target/*.deb.asc` file, then click "Add Artifact"
  - Upload the `target/rpm/RPMS/noarch/*.rpm` file, then click "Add Artifact"
  - Upload the `target/rpm/RPMS/noarch/*.rpm.asc` file, then click "Add Artifact"
  - Upload the `target/windows/*.msi` file, then click "Add Artifact"
  - Upload the `target/windows/*.msi.asc` file, then click "Add Artifact"
- In the "Description" field, enter "A web-based user interface for the DAISY Pipeline 2."
- Click "Upload Artifact(s)"
- Click "Staging Repositories"
- Scroll to the bottom and click the "orgdaisy" repository that was created
- Wait for the repository to be ready; click "Refresh" until the repository can be released (Release button not disabled)
- Click the "Release" button, and "Confirm"

The Web UI is now published.

#### ...if you need permissions

You will need to have publish rights to the DAISY group on Sonatype.
Ask the [developers mailinglist](https://groups.google.com/forum/#!forum/daisy-pipeline-dev) if you don't have permissions.
Once you have permissions you need to create the file `~/.sbt/0.13/sonatype.sbt` with the following contents,
replacing with your username and password:

```
credentials += Credentials("Sonatype Nexus Repository Manager",
                           "oss.sonatype.org",
                           "<username>",
                           "<password>")
```

### 3. Prepare for the next development iteration

Merge with the `master` branch if necessary.

Add a `-SNAPSHOT` suffix to the version in `build.sbt`.
