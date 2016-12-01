# DAISY Pipeline 2

The DAISY Pipeline 2 is a piece of software that allows you to convert, upgrade, validate, generate audio for, and produce Braille from your e-books. It has built-in support for formats like EPUB and DAISY. 

The Pipeline comes with a considerable number of scripts for the actions described above. They are indexed at the bottom of this page.

## Quick Start

Start the Pipeline 2 user interface by selecting the "DAISY Pipeline 2" icon on your desktop. 

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


## Included Scripts

* [DAISY 2.02 Validator](http://daisy.github.io/pipeline/modules/daisy202-validator)
* [DAISY 2.02 to EPUB 3](http://daisy.github.io/pipeline/modules/daisy202-to-epub3)
* [DAISY 3 to DAISY 2.02](http://daisy.github.io/pipeline/modules/daisy3-to-daisy202)
* [DAISY 3 to EPUB 3](http://daisy.github.io/pipeline/modules/daisy3-to-epub3)
* [DTBook Validator](http://daisy.github.io/pipeline/modules/dtbook-validator)
* [DTBook to DAISY 3](http://daisy.github.io/pipeline/modules/dtbook-to-daisy3)
* [DTBook to EPUB3](http://daisy.github.io/pipeline/modules/dtbook-to-epub3)
* [DTBook to HTML](http://daisy.github.io/pipeline/modules/dtbook-to-html)
* [DTBook to PEF](http://daisy.github.io/pipeline/modules/braille/dtbook-to-pef)
* [DTBook to ZedAI](http://daisy.github.io/pipeline/modules/dtbook-to-zedai)
* [EPUB 3 Validator](http://daisy.github.io/pipeline/modules/epub3-validator)
* [EPUB 3 to DAISY 2.02](http://daisy.github.io/pipeline/modules/epub3-to-daisy202)
* [EPUB 3 to PEF](http://daisy.github.io/pipeline/modules/braille/epub3-to-pef)
* [HTML to EPUB3](http://daisy.github.io/pipeline/modules/html-to-epub3)
* [HTML to PEF](http://daisy.github.io/pipeline/modules/braille/html-to-pef)
* [NIMAS Fileset Validator](http://daisy.github.io/pipeline/modules/nimas-fileset-validator)
* [ZedAI to EPUB 3](http://daisy.github.io/pipeline/modules/zedai-to-epub3)
* [ZedAI to HTML](http://daisy.github.io/pipeline/modules/zedai-to-html)
* [ZedAI to PEF](http://daisy.github.io/pipeline/modules/braille/zedai-to-pef)

## Troubleshooting

If you need assistance, please use the [forums](http://www.daisy.org/forums/pipeline-2).
