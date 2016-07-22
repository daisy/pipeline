"""Convenience layer on top of REST calls"""
import rest
import settings
from lxml import etree
import string
import tempfile

def get_scripts():
    """Return an XML document for all scripts"""
    uri = "{0}/scripts".format(settings.BASEURI)
    doc = rest.get_resource_as_xml(uri)
    return doc

def get_script(script_id):
    """Return an XML document for a given script"""
    uri = "{0}/scripts/{1}".format(settings.BASEURI, script_id)
    doc = rest.get_resource_as_xml(uri)
    return doc

def get_jobs():
    """Return an XML document for all jobs"""
    uri = "{0}/jobs".format(settings.BASEURI)
    doc = rest.get_resource_as_xml(uri)
    return doc

def get_job(job_id):
    """Return an XML document for a given job"""
    uri = "{0}/jobs/{1}".format(settings.BASEURI, job_id)
    doc = rest.get_resource_as_xml(uri)
    return doc

def get_log(job_id):
    """Return the log data for a given job as a string"""    
    uri = "{0}/jobs/{1}/log".format(settings.BASEURI, job_id)
    doc = rest.get_resource(uri)
    return doc

def get_result(job_id):
    """Return an XML document for the result of a given job"""
    uri = "{0}/jobs/{1}/result".format(settings.BASEURI, job_id)
    doc = rest.get_resource(uri)
    return doc

def post_job(request, data):
    """Create a new job and return the job description XML document"""
    uri = "{0}/test".format(settings.BASEURI)
    if data == None:
        doc = rest.post_resource(uri, request)
    else:
        doc = rest.post_resource(uri, {"job-data": data, "job-request": request})
    return doc

def delete_job(job_id):
    """Delete a job and return whether the deletion was successful"""
    uri = "{0}/jobs/{1}".format(settings.BASEURI, job_id)
    success = rest.delete_resource(uri)
    return success

def alive():
    """Is the Pipeline2 alive?"""
    uri = "{0}/alive".format(settings.BASEURI)
    doc = rest.get_resource_as_xml(uri)
    return doc

def halt():
    """Halt the Pipeline2"""
    keyfile = "{0}/dp2key.txt".format(tempfile.gettempdir())
    f = open(keyfile)
    key = f.read()
    f.close()
    uri = "{0}/admin/halt/{1}".format(settings.BASEURI, key)
    doc = rest.get_resource(uri)
    return doc

def get_job_status(job_id):
    """Return the status of the given job"""
    doc = get_job(job_id)
    if doc == None:
        return ""
    xpath_expr = "//{{{0}}}job".format(settings.PX_NS)
    xpath_fn = etree.ETXPath(xpath_expr)
    results = xpath_fn(doc)
    return results[0].attrib['status']
