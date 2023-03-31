This page is being worked on at the moment. Please check back on this page later when it is finished.

---

what can a module contain / API of a module
- "resources API": catalog.xml maps public URIs to file paths within the module
  - library.xpl
  - library.xsl
  - other
- public Java packages (should be unique: no two modules should expose same package)
- Java services (implementation of a public Java interface, should be in a *.impl.* package)
  - generated
    - Pipeline module: service that provides a catalog.xml
    - Pipeline script: service that points to a XProc file and adds metadata around it (name, description)
    - data type
    - ...
  - other

pom.xml
- modules-parent
- dependencies (compile/provided/runtime/test)
- properties
  - expose-services

catalog.xml
- based on XML Catalogs: https://www.oasis-open.org/committees/download.php/14810/xml-catalogs.pdf
- with extensions (for compile time processing)

Pipeline script definition file: see [XProcExtensions](XProcExtensions)

XProcSpec: see [Testing](Testing)