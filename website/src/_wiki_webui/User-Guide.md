# Web User Interface Guide

**NOTE:** The instructions in this user guide was originally written for version 1.6 of the Pipeline 2 Web UI, and might need some adjustments in some places.

## Create Job

Clicking the "Get Started" button on the welcome screen, or the "Create Job" item in the top navigation bar, will take you to the page where you create jobs. You can start either uploading the files you want to process, or choose the script you want to run.

In most modern browsers you can drag-and-drop files. ZIP-files are treated as directories. If you want to upload a directory structure, the files has to be zipped.

A progressbar will be displayed for each file that is uploading, along with the filename, filesize and a cancel button to cancel the upload. The overall progress of all uploads together are displayed above the list of uploads. When uploads finish, the Web UI will try to determine what kind of fileset has been uploaded and display it right under the "Files" headline.

In the "Scripts" panel, a table listing all the scripts along with a description of each script are listed. Select a script by clicking its name.

After selecting a script and (optionally) uploading files, you fill out the scripts parameters in the "Options" panel. Optional parameters are by default hidden; click the "Show"-link to show the optional parameters. When you're done configuring the job, click "Start job" to start the job.

## Job View

When you start a job you are automatically redirected to the job view page. You can also access this page through the "Jobs" menu item in the navigation bar at the top of the page. The job view page shows you the state of the job, job details like which script it runs and when it was started, the execution log as well as access to a detailed log.

The Pipeline 2 Engine will by default run at most two jobs at the same time. If you create more jobs, those jobs will be queued.

When a job completes without errors, a download link will appear which lets you download the results.

When a job fails, you may check the execution log for more information about why it failed.

Jobs will output log statements to indicate their execution progress. The list of messages will appear in the execution log at the bottom of the page. The most recent message will also appear right below the job state.

A link is also provided to a more detailed log. The detailed log is a text file with more technical information. If you encounter a problem while executing job and want to ask for help (for instance on the DAISY Pipeline 2 forum), you should attach the detailed log.

## Jobs List

By clicking the "Jobs" item in the top navigation menu you will be presented with a list of jobs. Normal users will see only their own jobs, while administrators can see everyones jobs. An icon in the first column indicates the state of the job (queued, running, failed, success). The second column gives the job name.

## Templates

For more information on templates, see the [Templating wiki page](Templating).

## About Page

The about page contains a description of what DAISY Pipeline 2 is as well as external links to where you can find more information and get support. At the bottom, the version of the Pipeline 2 Engine and the Web UI are stated.

## Account settings

In the account settings you can change your name, email and password.

## Admin Settings (for administrators)

Administrators have access to the "Admin settings" page, where they can configure several aspects of the Web UI; Users and Permissions, the Pipeline 2 Web API, Upload directory, E-mail settings, Maintenance, and Appearance.

For more info, see the [Administrator settings](Administrator-settings) page.
