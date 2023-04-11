# pipeline-updater

Pipeline automatic updater program

## How it works

The **release descriptor** contains the list of artefacts for a release. It is an XML file that is generated in pipeline-assembly.

- `href` is the address of this file
- `version` is the version linked to this releases
- `time` is the moment that this release descriptor was built

Nested to the releaseDescriptor we find elements of the type artifacts, with the following attributes:

- `href` where to get this artefact, usually a maven central URL.
- `id` formed using `${groupId}/${artifactId}`
- `deployPath` where to store the artefact, e.g. `system/framework/org.daisy.libs.jing-1.0.3.jar`
- `extract` a boolean value indicating if this file should extract in the deploy path instead of just copying it
- `overwrite-path`
- `artifactId`
- `groupId`
- `version`
- `classifier`

pipeline-assembly has a script for publishing the descriptor to http://daisy.github.io/pipeline-assembly/releases/.

The structure is the following: http://daisy.github.io/pipeline-assembly/releases/ contains all the release descriptors, each identified by its version, e.g. "10.0.1-SNAPSHOT", and two special releases called "current" for the latest release and "snapshot" for the latest snapshot. (Apparently "latest" has some kind of special meaning for Github pages, and will not allow accessing resources that name, thus the current key word.)

The updater CLI is in https://github.com/daisy/pipeline-updater, and it's also used by the GUI. This is the help:

    Usage of ./pipeline-updater:
      -descriptor string
        	Current descriptor
      -force
        	Forces to update without comparing the versions, use if updating to nightly builds
      -install-dir string
        	Pipeline install directory
      -service string
        	Url of the update service (default "http://defaultservice.com")
      -version string
        	Version to update to (default "current")

Basically what the CLI does is to compare the local descriptor with the version passed as parameter and will compute the differences, deleting and updating the need artefacts. In the special case of updating the snapshots, that are nightly builds, force the update as the version of the release descriptor will be the same in the local release descriptor and the remote snapshot.

The GUI for the updater consists of a simple wrapper for the CLI. In Windows it has to be in a separate executable due to the permissions restrictions that break the user experience. In Linux and MacOs it is in the same GUI as the Pipeline itself.
