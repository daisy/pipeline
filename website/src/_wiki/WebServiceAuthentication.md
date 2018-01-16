# Web Service Authentication

Once the DAISY Pipeline has been installed, it can be configured to accept only authenticated responses via the Java property `org.daisy.pipeline.ws.authentication`. With this property set to `true`, client applications must submit authenticated requests.

Upon being accepted as a Pipeline application, each client receives an
authentication ID and a secret string, which they must keep
secret. The process for submitting an authenticated request is as
follows:
 
 Here, we will assume the authentication ID is "myclient" and the secret is "mysecret".
 
 * The client has a URI to a resource
  Example: http://example.org/ws/scripts
 * The client appends to this string three parameters: 
   * `authid`: The client identifier is issued when the client signs
   up to be a Pipeline client app.  There is one identifier/secret
   pair issued per application.  Example: `myclient`
   * `time`: The timestamp must be UTC and must be formatted in
   [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601). Example:
   `2012-02-09T02:23:40Z`
   * `nonce`: A nonce is a number used once. The client should
   generate a unique nonce for every request. Example:
   `533473712461604713238933268313`
 * The URI string now has three additional query parameters Example:
   `http://example.org/ws/scripts?authid=myclient&time=2012-02-09T02:23:40Z&nonce=533473712461604713238933268313`

Then the client takes this URI string and generates an
[HMAC SHA1 hash](http://en.wikipedia.org/wiki/HMAC) using their
secret. The hash function looks roughly like this:

~~~
hashString = createHash("http://example.org/ws/scripts?authid=myclient&time=2012-02-09T02:23:40Z&nonce=533473712461604713238933268313", "mysecret")
print hashString
gq/lpIuWqEDjhWviAjyccNTzdZk=
~~~
 
Now escape the hash string so it can be passed as part of a URI:
`gq%2FlpIuWqEDjhWviAjyccNTzdZk%3D`
  
Finally, append the escaped hash string to the URI. It must be the
last query parameter.

`http://example.org/ws/scripts?authid=myclient&time=2012-02-09T02:23:40Z&nonce=533473712461604713238933268313&sign=gq%2FlpIuWqEDjhWviAjyccNTzdZk%3D`

This is the URI to submit to the Web Service. If `myclient` has a
permission level to do what they want, then their request will be
accepted.  Currently, only admin requests require a client to have a
special permission level (`ADMIN` instead of the default `CLIENTAPP`).
