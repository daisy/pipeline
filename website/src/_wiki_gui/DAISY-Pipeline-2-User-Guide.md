# DAISY Pipeline 2 Desktop Application

## Starting the Application

### Windows

Start the application via the "DAISY Pipeline 2" icon on your desktop or in the start menu.

### MacOS

Select the "DAISY Pipeline 2" application in your "Applications" folder.

### Linux

On Linux, launch the graphical user interface by running the command `pipeline2 gui`. If you have installed the Debian package, the `pipeline2` executable is located in `/opt/daisy-pipeline2/bin/`. If you have installed the ZIP, it is located in the `bin` directory.


## Quick Start

A window should come up:

![Pipeline window](https://raw.githubusercontent.com/daisy/pipeline-gui/master/docs/img/window-init.png)

Start your job with **`File->New job`**, or by pressing **`Cmd+N`**

![File menu](https://raw.githubusercontent.com/daisy/pipeline-gui/master/docs/img/menu-file.png)

A job window creation appears. Select a script from the drop-down menu:

![Scripts menu](https://raw.githubusercontent.com/daisy/pipeline-gui/master/docs/img/menu-scripts.png)

Fill out the form (which is a bit different for each script):

![New job form](https://raw.githubusercontent.com/daisy/pipeline-gui/master/docs/img/new-job-daisy202-epub3.png)

Select **`Run`** to run or press **`Cmd+R`**.

The job appears in the sidebar, along with its status. 

![Job running](https://raw.githubusercontent.com/daisy/pipeline-gui/master/docs/img/job-running.png)

While one job is running, you may start another via **`File->New job`**.

When a job is done, its status will be updated to `Done`. you may view the results by selecting the completed job in the sidebar and selecting one or more links to files in the **Results** list.

![Job done](https://raw.githubusercontent.com/daisy/pipeline-gui/master/docs/img/job-done.png)

To view the folder containing all the results, point your file browser to the directory you specified for Pipeline output in the new job form.

If the job does not appear to have started running, check the messages log for errors. Errors may include problems with how you filled out the form, as well as system errors.

## Commands

**New job**

Shortcut: `Cmd+N`

Create a new job by selecting a script. A script may do something like convert HTML to EPUB3 or add narration to a book. After you select "New Job", choose a script and fill out the form.

The form will ask for things like input file, output directory, and specific options depending on the script you chose.

**Run job**

Shortcut: `Cmd+R`

When you've finished filling out the job form, select this to execute the job. The job status is displayed in the sidebar. 

If the job does not appear to have started running, check the messages log for errors in the form.

**Delete job**

Shortcut: `Delete`

Delete the job from the list. This does not delete the results on disk.

**Run job again**

Shortcut: `Cmd+Shift+R`

You may select this for any running or completed job to create a new job initialized with the same form parameters.

**Copy messages to clipboard**

Places the contents of the messages window onto the clipboard. Useful for reporting bugs or asking for help.

**Check Updates**

Launch the Pipeline updater.

**User guide**

This document.
