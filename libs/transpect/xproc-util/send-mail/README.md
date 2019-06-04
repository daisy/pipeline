# send-mail

This step allows you to send mail. Basically it's a
convenient wrapper for XML Calabash's `cx:send-mail`.

## Prerequisites

* XML Calabash
* Java 1.6

## Setup

Please add the following libraries to your Java classpath

* javax.mail.jar
* xmlcalabash1-sendmail-1.1.4.jar

Put your email host configuration in a XML Calabash configuration file

```xml
<xproc-config xmlns="http://xmlcalabash.com/ns/configuration">
  <sendmail host="imap.mymailserver.com" port="25" username="johndoe" password="verysecret"/>  
</xproc-config>
```

You need to pass the path to the XML Calabash configuration file with the parameter `-c`
when XML Calabash is invoked.

## Example

Here is a brief example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  version="1.0">
  
  <p:output port="result"/>
  
  <p:import href="xproc-util/send-mail/xpl/send-mail.xpl"/>
  
  <tr:mailing name="send-mail"
    from="john.doe@foo.bar"
    from-name="Johnyboy"
    to="jane.doe@foo.bar"
    subject="Outlook"
    content="sucks!"/>
  
</p:declare-step>
```
