## Branding

Branding is the process of applying the name of your organizations, logo, colors and overall appearance to the Web UI so that it matches your organizations profile.

### Title

The default title of the Web UI is "DAISY Pipeline 2". It appears on the left side of the top navigation bar, in the web browsers window or tab title, as well as other places. It can be customized through the "Admin settings" under "Appearance".

### Visual themes

A theme consists of images, two CSS-files (bootstrap.min.css and main.css), and a JavaScript file (theme.js). All of them are optional, and if not present, the defaults will be used (the defaults for main.css and theme.js are empty files).

Themes must be stored in the `themes` directory. A theme stored in `themes/My Theme` will appear as "My Theme" in the list of themes under "Admin settings" &gt; "Appearance" &gt; "Visual themes". Resources stored inside `/themes/My Theme` will override the default resources if present.

- `themes/My Theme/stylesheets/bootstrap.min.css` and `themes/My Theme/stylesheets/bootstrap-responsive.css` - this is a minified version of a [Bootstrap](http://getbootstrap.com/) theme. As of Web UI version 2.5.x, the Bootstrap version used are v2.3.0.
- `themes/My Theme/stylesheets/main.css` - this CSS file can be used for anything not related to Bootstrap
- `themes/My Theme/javascripts/theme.js` - if any JavaScript is neccessary, it can be put into this file
- If you need to include images; then they should be stored under `themes/My Theme/images`. For instance, the favicon can be configured by adding a t`hemes/My Theme/images/favicon.png`.

There are many Bootstrap themes available online, and they are typically distributed as a file called bootstrap.min.css. If you find a theme you like or create one using online tools, simply place the bootstrap.min.css file in the "stylesheets" directory of your theme to use it.

There also exist tools online to create custom Bootstrap themes. One tool you can try are **[Bootswatchr](http://bootswatchr.com/)**. Remember to choose the correct version of Bootstrap, make your changes in the menu on the left and see the changes in the page to the right. Then download it as CSS and store it as bootstrap.min.css in your theme folder.

#### Example: use CSS to add a logo

Store the logo (about 20px in height) as `themes/My Theme/images/logo.png`.

**stylesheets/main.css**
```
a.brand::before {
    content: url('../images/logo.png');
    padding-right: 1em;
}
```

#### Example: use JavaScript to add a navigation item

jQuery is available for use when writing scripts.

The following inserts a link to another website, after the "About" navigation item.

**javascripts/theme.css**
```
$(document).ready(function() {
    $(".navbar-fixed-top nav ul:first-child").append(
        '<li class="active"><a href="https://example.org" target="_blank">'+
        '<i class="icon-share icon-white"></i> '+
        'My Organization'+
        '</a></li>');
});
```
