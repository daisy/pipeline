# Debugging

This page gives you some tips and tricks for debugging Pipeline modules, in particular XProc code.

## XProcSpec tests

It is important to create tests for all your steps and it is a good idea to split up big steps into smaller ones. The more tests the easier it is to spot the exact place where a problem occurs.

Note that XProcSpec tests are generally recommended over XSpec because anything you can do with XSpec you can also do with XProcSpec, and XProcSpec is more powerful and makes abstraction of how a step is implemented. A step may for example be implemented in XSLT but later re-written in XProc or Java. In this case your tests can be reused. Still you may prefer to use XSpec in some cases where it is a better fit, and it might actually give you a better debugging experience.

### XProc stack trace

Our modified version of XMLCalabash spits out XProc stack traces with file paths and line numbers of the complete call stack. It also works for errors thrown from within an XSLT. This is a big improvement over the standard version of XMLCalabash. Stack traces can be found in the log files (`target/test.log`) and/or XProcSpec reports, depending on the situation. When an error is caught by a `p:catch` it is also printed in the log. For this the log level in
`src/test/resources/logback.xml` needs to be set to "TRACE".

### p:log

With [`p:log`](https://www.w3.org/TR/xproc/#p.log) you can inspect intermediary documents in your pipelines. You can add it (temporarily) to any step. You need to specify the output port that you want to inspect and the file location where you want to store the document(s). 

As an alternative to `p:log` you can also use `p:for-each` to print intermediary documents to the log file (TRACE level). Note that this is a quirk in XMLCalabash.

### Print statements

Sometimes debugging will come down to printing messages. In XSLT you can use `xsl:message`. In XProc you can use `px:message` attributes. Both will end up in the log file. With the right configuration (`logback.xml`) you can also make them appear in the job execution log on standard out.
