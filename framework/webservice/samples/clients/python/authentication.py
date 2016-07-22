import hashlib
import hmac
import base64
import random
import time
import urllib
import string

AUTH_ID = "clientid"
SECRET = "supersecret"

def prepare_authenticated_uri(uri):
    """Prepare an authenticated URI. 
    The uri param includes all parameters except id, timestamp, and hash"""
    
    uristring = ""
    timestamp = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    nonce = generate_nonce()
    params = "authid={0}&time={1}&nonce={2}".format(AUTH_ID, timestamp, nonce)
    if uri.find("?") == -1:
        uristring = "{0}?{1}".format(uri, params)
    else:
        uristring = "{0}&{1}".format(uri, params)
    
    hashed_string = generate_hash(uristring)
    auth_uri = "{0}&sign={1}".format(uristring, hashed_string)
    print auth_uri
    return auth_uri


def generate_hash(data):
    """Return a SHA1 hash of the data using the secret"""
    digest = hmac.new(SECRET, data, hashlib.sha1).digest()
    hash64 = base64.b64encode(digest)
    return urllib.quote_plus(hash64) # escape chars for URI

def generate_nonce():
    """Generate a random number 30 digits long"""
    randomnum = random.randrange(10**30)
    rjust_str = '{:0<30}'.format(str(randomnum))
    return rjust_str


