## Running XSpec tests

The following configuration snippet would run as XSpec tests all the files
with an `.xspec` extension in the directory `src/test/xspec` and its
sub-directories.

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

As with vanilla XSpec, the tested stylesheet is loaded from URI returned by resolving the value of the `stylesheet` attribute of the XSpec `description`
root element against its base URI.

For instance in the following Maven project layout:

```
pom.xml
+ src
  + main
    + xslt
      - say-hello.xsl
  + test
    + xspec
      - say-hello.xspec
```

The XSpec test defined in `say-hello.xspec` would look like:

```xml
<x:description 
    xmlns:x="http://www.jenitennison.com/xslt/xspec"
    stylesheet="../../main/xslt/say-hello.xsl">
    <x:scenario>
        ...
    </x:scenario>
</x:description>

```

