#!/usr/bin/env python

"""The local webservice set up to receive callbacks about job progress"""
import web
from lxml import etree
import settings
import string

class jobmessage:        
    def POST(self):
        xml = web.data()
        doc = etree.fromstring(xml)
        self.print_update(doc)

    def print_update(self, doc):
        xpath_expr = "//{{{0}}}job".format(settings.PX_NS)
        xpath_fn = etree.ETXPath(xpath_expr)
        results = xpath_fn(doc)
        jobid = results[0].attrib['id']
        xpath_expr = "//{{{0}}}job/{{{0}}}messages/{{{0}}}message".format(settings.PX_NS)
        xpath_fn = etree.ETXPath(xpath_expr)
        results = xpath_fn(doc)
        print "JOB UPDATE\n\tID {0}\n\n\tMessage(s):".format(jobid)
        for m in results:
            print "\t#{0}. {1} - {2}".format(m.attrib['sequence'], m.attrib['level'], m.text)
        print ""

class jobstatus:
    def POST(self):
        xml = web.data()
        doc = etree.fromstring(xml)
        self.print_update(doc)
    
    def print_update(self, doc):
        xpath_expr = "//{{{0}}}job".format(settings.PX_NS)
        xpath_fn = etree.ETXPath(xpath_expr)
        results = xpath_fn(doc)
        print "JOB UPDATE\n\tID {0}\n\tStatus:\n\t{1}\n".format(jobid, jobstatus)

class miniws:
    def __init__(self):
        urls = (
        '/ws/jobmessage', 'jobmessage',
        '/ws/jobstatus', 'jobstatus')
        self.app = web.application(urls, globals())
    
    def start(self):
        self.app.run()

def main():
    ws = miniws()
    ws.start()
    
if __name__ == "__main__":
    main()