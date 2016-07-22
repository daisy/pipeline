"""Takes care of all the rest calls"""
from lxml import etree
from multipart import MultipartPost
#import authentication
from urlparse import urlparse
import httplib
import authentication

def get_resource(uri):
    """Return a string representation of a resource"""
    authuri = urlparse(authentication.prepare_authenticated_uri(uri))
    connection = httplib.HTTPConnection(authuri.netloc)
    try:
        connection.request("GET", authuri.path + "?" + authuri.query)
        response = connection.getresponse()
        print "Response was %s" % response.reason
        
        if response.status == httplib.OK:
            return response.read()
        elif response.status == httplib.NO_CONTENT:
            return "success"
        elif response.status == httplib.INTERNAL_SERVER_ERROR:
            return None
        else:
            return None
    except httplib.HTTPException:
        print "GET failed for %s" % uri
    return None

def get_resource_as_xml(uri):
    """Return an XML document representing a resource"""
    resource = get_resource(uri)
    if resource != None:
        return xml_from_string(resource)
    else:
        return None

def xml_from_string(xmlstr):
    """Creates an XML document from a string"""
    if xmlstr == None or xmlstr == "":
        return None
    return etree.fromstring(xmlstr)

def post_resource(uri, data):
    """Post a new resource"""
    authuri = urlparse(authentication.prepare_authenticated_uri(uri))
    connection = httplib.HTTPConnection(authuri.netloc)
    try:
        # if this is a dict, treat it like a multipart request
        if isinstance(data, dict) == True:
            multipart = MultipartPost()
            query, headers = multipart.prepare_query(data)
            connection.request("POST", authuri.path + "?" + authuri.query, query, headers)
        else:
            connection.request("POST", authuri.path + "?" + authuri.query, data)
    
        response = connection.getresponse()
        print "Response was %s" % response.reason
    
        if response.status == httplib.CREATED:
            print "success"
            return xml_from_string(response.read())
        elif response.status == httplib.INTERNAL_SERVER_ERROR:
            print "failed"
            return None
        else:
            return None
    except httplib.HTTPException:
        print "POST failed for %s" % uri
    return None
	
def delete_resource(uri):
    """Delete a resource from the server"""
    authuri = urlparse(authentication.prepare_authenticated_uri(uri))
    connection = httplib.HTTPConnection(authuri.netloc)
    try:
        connection.request("DELETE", authuri.path + "?" + authuri.query)
        response = connection.getresponse()
    
        if response.status == httplib.NO_CONTENT:
            return True
        elif response.status == httplib.INTERNAL_SERVER_ERROR:
            return False
        else:
            return False
    except httplib.HTTPException:
        print "DELETE failed for %s" % uri
    return False
