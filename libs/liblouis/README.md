Build
-----

To build for all platforms at once:

    docker-compose build debian
    make clean all

This requires [Docker](https://www.docker.com). Mac binaries will only
be build if the host platform is Mac OS.

Deploy
------

To deploy a snapshot to Sonatype OSS:

    make snapshot

To make a release:

    make release
