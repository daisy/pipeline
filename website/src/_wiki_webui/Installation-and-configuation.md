**TODO:** Add instructions on how to install debian package here. And also Windows instructions when a working Windows version is available.

## First Use

**NOTE:** These instructions were originally written for version 1.6 of the Pipeline 2 Web UI, and might need some adjustments in some places.

The first time that you use the Web UI, you will be asked to create an administrative account. You are able to create additional administrators later. This administrator will be named "Administrator", but you can change the name later as well.

For the Web UI to be able to communicate with the Pipeline 2 Engine, you must tell it where to find the Web API endpoint of the Pipeline 2 Engine. A common configuration is to run the Web API endpoint on the same computer as the Web UI, on port 8181 or 8182. If this is the case, a message will appear saying that a Pipeline 2 Engine is running at one of those addresses, and a button to choose that address.

If the Pipeline 2 Engine at the given Web API endpoint requires authentication, you will be required to provide the authentication ID and the secret text.

When the administrator account and the Web API is configured, a welcome message will appear, and you are ready to start using the Pipeline 2 Web UI.
