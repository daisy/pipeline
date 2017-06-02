# Nexus Maven Repository

## Running the service

    docker build -t daisy/nexus .
    docker run -d -p 8081:8081 --name nexus daisy/nexus

If docker is not available, use the boot2docker image:

    cd ../boot2docker
    vagrant up
    vagrant ssh

    $ docker build -t daisy/nexus ~/pipeline-it/nexus
    $ docker run -d -p 8081:8081 --name nexus daisy/nexus

# Using the service

For deploying to the repository, add the following to your `distributionManagement` section:

```xml
<snapshotRepository>
  <id>daisy-nexus-snapshots</id>
  <name>DAISY Nexus Snapshots</name>
  <url>http://localhost:8081/nexus/content/repositories/snapshots/</url>
</snapshotRepository>
```

and add the following to your `servers` section in `~/.m2/settings.xml`:

```xml
<server>
  <id>daisy-nexus-snapshots</id>
  <username>deployment</username>
  <password>deployment123</password>
</server>
```

For downloading from the repository, add the following to your `repositories` section:

```xml
<repository>
  <id>daisy-nexus-snapshots</id>
  <name>DAISY Nexus Snapshots</name>
  <url>http://localhost:8081/nexus/content/repositories/snapshots/</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

Nexus can be configured through the web interface:
http://localhost:8081/nexus. The login is `admin` and the password is
`admin123`.
