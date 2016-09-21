## Usage

By default the plugin attaches to the `test` phase of the build lifecycle and
runs all the XSpec tests (files with an `.xspec` extension) found in the
`${basedir}/src/test/xspec` directory and its sub-directories. An example
configuration is supplied below.

```xml
<project>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.daisy.maven</groupId>
        <artifactId>xspec-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
          <execution>
            <id>xspec-tests</id>
            <goals>
              <goal>test</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

XSpec tests will then be invoked when calling the `test` phase of the build lifecycle.

```
mvn test
```

Like with vanilla XSpec, the tested stylesheet is loaded from the URI returned
by resolving the value of the `stylesheet` attribute of the XSpec
`description` root element against its base URI.

### Customizing the XSpec tests location

The location of the XSpec scenario descriptions can be finely configured with
the `testSourceDirectory` and `excludes`/`includes` parameters:

```xml
<project>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.daisy.maven</groupId>
        <artifactId>xspec-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
          <execution>
            <id>xspec-tests</id>
            <goals>
              <goal>test</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <testSourceDirectory>src/test/xslt</testSourceDirectory>
          <includes>
            <include>**/xspec-*.xml</include>
          </includes>
        </configuration>
      </plugin>
    </plugins>
  </build>
  ...
</project>
```

### Running a single test


It is possible to run an individual XSpec test by name, overriding the `includes`/`excludes` configuration.

For instance, running the following command will run the single XSpec test located at `${basedir}/src/test/xspec/my-test.xspec`:

```
mvn test -Dxspec=my-test
```

### Using XML Catalogs

Sometimes an XSLT stylesheet imports functions or templates from an external
resource which may not be available when running unit tests. The XSpec Maven
Plugin offers a way to provide "mock" implementations of these dependencies by
via an OASIS XML Catalog. The catalog file must be named `catalog.xml` and
located in the directory containing the XSpec test description.

See the [Mocking external dependencies](examples/mocks.html) example for a detailed configuration example.



