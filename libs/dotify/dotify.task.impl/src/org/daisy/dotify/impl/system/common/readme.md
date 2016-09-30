# Task System #
A task system defines the conversion pipeline in Dotify. It controls the data flow between different file-to-file components from input to output. The concept is very similar that of the Daisy Pipeline 1 or XProc for that matter (that is used in Daisy Pipeline 2).

The task system in Dotify were intentionally created as a top level customization point which is very flexible.

## DotifyTaskSystem ##
Creating different task systems for different combinations of output formats and locales have proven to have some significant drawbacks including:
  * duplication of code
  * opaque control flow

Therefore, a generic task system called DotifyTaskSystem has been created which limits the available customization points and reduces the boilerplate code needed. It is still possible to insert additional components at certain points within in this task system, if needed. It should be useful for most, if not all, conversions needed in Dotify.

![https://docs.google.com/drawings/d/1XnEq-Yo2Ph101kFkeN5pnwC9qzxATkGkSTCCmwZog3g/pub?w=1092&h=1054&nonsense=image.png](https://docs.google.com/drawings/d/1XnEq-Yo2Ph101kFkeN5pnwC9qzxATkGkSTCCmwZog3g/pub?w=1092&h=1054&nonsense=image.png)