# Administrator Settings

**NOTE:** These instructions were originally written for version 1.6 of the Pipeline 2 Web UI, and might need some adjustments in some places.

## Users and Permissions

The "Users and Permissions" tab lets you configure who gets access to the Web UI. The "General settings" contains two items; "Authentication" and "Show optional parameters". If you find that you need to use optional parameters of a script a lot (when creating jobs) then it might be a good idea to enable the "Show optional parameters" option so that you don't have to expand them all the time. The "Authentication" option has three modes; Public, Guest and Authenticated. In authenticated mode, only users with a username and password can log in. In guest mode, users will be presented with the login page, but has the option of logging in as a guest. In public mode, users are automatically logged in as a public user.

### Authenticated Mode

The "Add user" panel lets you create a user. Users are uniquely identified by their e-mail address.

If e-mails are enabled you don't have to provide a password.

When e-mails are enabled, the user that was created will receive an e-mail with a link to where they can activate their account.

By clicking the link in the e-mail, the user will be presented with a form where they can set their password.

The name of the user that was created will appear in the list of users on the left side of the "Users and permissions" tab in "Admin settings". You can change the details of the user here, as well as re-send the activation e-mail in case the user didn't get the previous e-mail. If the user has forgot their password, the admin can send a "forgotten password" e-mail from here as well.

Normal users can only see their own jobs and cannot access other jobs than their own.

Users can change their own account details from account page, which is available by clicking their own name right next to the "Log out" menu item in the top navigation bar.

If e-mail is enabled, users can click a button on the login page to get a new password.

By clicking the button to get a new password you will get an e-mail with a link to a page where you can choose a new password.

### Guest Mode

In addition to authenticated users, guests without a username can also use the Web UI if the Web UI is running in guest mode. Guest mode is useful if you want to let users try the Web UI before aquiring an account. However, there is currently not any way to restrict which scripts guests gets access to or how big files they are allowed to upload, so the usefulness of guest mode is limited in this version of the Web UI.

By clicking the name of the guest in the list of users under the "Users and permissions" tab in the "Admin settings", you can change the name of the guest user to something else.

In guest mode, there will be a button on the login page that lets you log in as a guest.

When logged in as a guest, and if e-mails are enabled, there will be an extra script parameter at the bottom of the "Options" panel on the "Create Job" page. If you fill in your e-mail address here, you will get an e-mail with a link to the job you create so that you can access the job later.

The e-mail contains a link to the job you just created. In guest mode there is no jobs list. Guests cannot access eachothers jobs. So if you close the browser window with the job details, there is no way to find the job through the Web UI. The only way to get back to the job is to follow the link in the e-mail.

### Public Mode

In public mode, everyone is automatically logged in as a public user and redirected to the "Create Job" page. Public mode is useful if the Web UI is installed behind a firewall for use inside an organization, where having to log in can be just an annoyance that serves no purpose.

All public users have access to the jobs created by other public users.

## Pipeline 2 Web API

Under the "Pipeline 2 Web API" tab you can configure the Pipeline 2 Web API endpoint just like you could during the First Use setup wizard. Enter the address, authentication ID and secret token of the Pipeline 2 Engine you want to connect to.

## Upload Directory

The upload directory can be configured under the "Storage directories" tab in the same way that it was configured during the First Use setup wizard.

## E-mail Settings

If you enable e-mails then users can retrieve lost passwords, and guest users can get a link to their jobs.

If you have a GMail account you can simply enter your username and password in the GMail panel.

You can configure any other SMTP server manually in the "SMTP" panel.

## Maintenance

In the "Maintenance" tab you can set up automatic deletion of jobs after a certian duration.

## Appearance

Under "Appearance" you may change the title and visual theme of the Web UI. The title is what appears to the left in the top navigation bar, and as the tab or window title in the browser. Visual themes change the colors and fonts of the Web UI. Themes from the open source project Bootswatch are included as a demonstration. More information on how to create your own themes can be found in the Web UI Branding guide.
