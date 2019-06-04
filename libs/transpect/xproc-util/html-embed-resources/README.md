# html-embed-resources

This step takes an HTML file and embeds the referenced files
via data URI or style element into the HTML. This facilitates the generation of offline readable HTML.

The step also tries to load external HTTP file references. 

If a file reference cannot be loaded or embedded, the original file reference remains.

## Ports

* `input` expects an XHTML document
* `result` provides the XHTML document with the patched file references

## Options

* `fail-on-error` if this option is set to `true`, the step fails
if a resource could not be embedded.


## Example

### Input

This HTML file includes external an external CSS and JavaScript file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>example</title>
    <script type="text/javascript" src="example.js"></script>
    <link type="text/css" rel="stylesheet" href="stylesheet.css"/>
  </head>
  <body>
    <p class="red" id="myPara">This paragraph should be 
      red-colored and you should see a popup with the message "it works!"</p>
  </body>
</html>
```

stylesheet.css

```css
.red{color:red}
```

example.js

```JavaScript
alert("it works!");
```

### Output

The XProc step loads CSS and JavaScript and embeds them into the HTML file. 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>example</title>
    <script type="text/javascript" src="data:application/javascript;base64,YWxlcnQoIml0IHdvcmtzISIpOw==&#xA;"></script>
    <style>
      .red{color:red}
    </style>
  </head>
  <body>
    <p class="red" id="myPara">This paragraph should be 
      red-colored and you should see a popup with the message "it works!"</p>
  </body>
</html>
```
