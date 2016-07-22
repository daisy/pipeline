DAISY Pipeline 2 Framework
==========================

[![Build Status](https://travis-ci.org/daisy/pipeline-framework.png?branch=master)](https://travis-ci.org/daisy/pipeline-framework)

Core projects for the DAISY Pipeline 2 runtime framework.

Each project is an OSGi bundle.

The engine relies on [XML Calabash](http://xmlcalabash.com/) for running XProc scritps and [Saxon HE](http://saxonica.com/) for XSLT.


Release procedure
-----------------
- At this point there should be no *external* snapshot dependencies. For every module that is enabled (not commented out) in the aggregator (`pom.xml`), its version as declared in its own POM and its version as declared in the BoM (`bom/pom.xml`) should match. Disabled modules must not have snapshot versions declared in the BoM. Modules must be versioned according to SemVer (should have been taken care of when merging pull requests).
- Perform the release with Maven:

  ```sh
  mvn clean release:clean release:prepare -DpushChanges=false
  mvn release:perform -DlocalCheckout=true
  ```

- Revert the automatic snapshot increments of modules done by Maven in the BoM (bom/pom.xml), and disable all modules in the aggregator POM (pom.xml). Amend to the last commit ("[maven-release-plugin] prepare for next development iteration").
- Stage artifacts at https://oss.sonatype.org.
- Do all the required testing.
- Release artifacts at https://oss.sonatype.org.
