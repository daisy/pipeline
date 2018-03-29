# Setting up Eclipse

The bulk part of DAISY Pipeline 2 is written in Java. Any IDE may be
used for editing the Java code, but we only provide formal support for
the [Eclipse][] IDE.

The [super-build](Building#super-build) script includes a command for
generating an Eclipse project for a certain Maven module. Projects
will also be generated for all dependencies of that module. You will
be asked to create an Eclipse workspace dedicated to the Pipeline. If
you have the [eclim][] command line tool installed, projects will even
be automatically imported into the workspace.

For example, to import the `gui` module into Eclipse, run this command:

~~~sh
make eclipse-gui
~~~



[Eclipse]: http://www.eclipse.org/
[eclim]: http://eclim.org/
