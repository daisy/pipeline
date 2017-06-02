Pipeline Integration Tests
==========================

Integration tests for the [pipeline2 framework] (https://github.com/daisy/pipeline-framework) and webservice.

How to run the tests
====================

 The idea is to be able to run the integration tests in the three major targeted platforms. Currently it is only possible to run tests against a linux box (guest environment, you should be able to run the regardless your host OS).  

 1. Get [vagrant](https://www.vagrantup.com/) (The software, not a random bearded guy).
 2. ``cd platform/linux``
 3. The first time you'll need run ``vagrant up --provision`` to fetch the ubuntu image and install all the dependencies. In subsequent testing sessions ``vagrant up`` should do the trick to bring the box up.
 4. You could ssh into the vm using ``vagrant ssh`` and run the tests by hand, although there a convenience script called ``test.sh``.
 5. If for some reason you need to change the tests (i.e. specify a branch or tag of the pipeline-assembly to test in the daisy/framework-integration/pom.xml) you can do it locally as the folders are in sync with the vm.
 6. Once you're done, bring down the machine by typing ``vagrant halt``




