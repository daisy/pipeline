pipeline-gui
============

Desktop GUI interface for the pipeline. Written in JavaFX.

# Building this code

Requires Java 8. Get the latest Java 8 for the best accessibility support.

Get the following repositories:
 * pipeline-gui 'javafx' branch
 * pipeline-assembly 'javafx' branch
 * pipeline-framework 'master' branch

Plus any scripts or modules that you want to use.

Build all and run './pipeline2' from the assembly target directory.

# Features
 * View jobs list
 * View job details
 * Access job results in separate application(s)
 * Create new job
 * Run job again
 * Delete job

## Keyboard shortcuts
 * New job: Control + N
 * Delete job: Delete
 * Run job again: Control + Shift + R
 
# Out of scope 
 * authentication/connecting to a remote pipeline installation
 * batch jobs
 * job template
 * install new scripts from repo
 * autoupdate installed components
 * user management (e.g. client user account creation)
 * editing content 
 


