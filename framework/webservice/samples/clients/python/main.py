#!/usr/bin/env python
"""Command line interface client for DAISY Pipeline2 web service.

Comes with a companion web service that must be started separately:

1. run ws/miniws.py
2. include <callback> element(s) in the job request
3. check for job updates in the terminal window where miniws.py is running

"""



import os
import argparse
from lxml import etree
import resources
import settings

def main():
    """Main entry point"""
    
    usage = """
  
  Usage: main.py command options
  
  Commands:
  
  new-job       Create a new job.
  alive         See if the Pipeline2 is running.
  
  Examples:
  Show all scripts:
      main.py scripts
  Show a specific script:
      main.py script --id=http://www.daisy.org/pipeline/modules/dtbook-to-zedai/dtbook-to-zedai.xpl
  Show a specific job:
      main.py job --id=873ce8d7-0b92-42f6-a2ed-b5e6a13b8cd7
  Create a job:
      main.py new-job --request=../testdata/job1.request.xml
  Create a job:
      main.py new-job --request=../testdata/job2.request.xml --job-data=../testdata/job2.data.zip
  Get notifications of job updates:
      main.py new-job --request=../testdata/job2.request.xml --job-data=../testdata/job2.data.zip
  """
    parser = argparse.ArgumentParser(description = "Sample Pipeline2 webservice client written in python")    
    subparsers = parser.add_subparsers(help='commands')

    # 'scripts'
    scripts_parser = subparsers.add_parser('scripts', help='List scripts.')
    scripts_parser.add_argument('--id', action='store', default="", help='ID of a specific script')
    scripts_parser.set_defaults(func=get_scripts)

    # 'jobs'
    jobs_parser = subparsers.add_parser('jobs', help='List jobs.')
    jobs_parser.add_argument('--id', action='store', default="", help='ID of a specific job')
    jobs_parser.set_defaults(func=get_jobs)

    # 'log'
    log_parser = subparsers.add_parser('log', help='Show the log for a finished job.')
    log_parser.add_argument('--id', action='store', required=True, default="", help='ID of a specific job')
    log_parser.set_defaults(func=get_log)

    # 'result'
    result_parser = subparsers.add_parser('result', help='Show where the result is stored for a job.')
    result_parser.add_argument('--id', action='store', required=True, default="", help='ID of a specific job')
    result_parser.set_defaults(func=get_result)    

    # 'delete-job'
    delete_job_parser = subparsers.add_parser('delete-job', help='Delete a job.')
    delete_job_parser.add_argument('--id', action='store', required=True, default="", help='ID of a specific job')
    delete_job_parser.set_defaults(func=delete_job)        

    # 'delete-all'
    delete_all_parser = subparsers.add_parser('delete-all', help='Delete all jobs.')
    delete_all_parser.set_defaults(func=delete_all)

    # 'new-job'
    new_job_parser = subparsers.add_parser('new-job', help="Create a new job.")
    new_job_parser.add_argument('--request', action='store', required=True, help="XML file representing the job request.")
    new_job_parser.add_argument('--job-data', action='store', default=None, help="Zip archive with job data.")
    new_job_parser.set_defaults(func=post_job)

    # 'alive'
    alive_parser = subparsers.add_parser('alive', help="See if the Pipeline2 is alive")
    alive_parser.set_defaults(func=alive)

    # 'halt'
    halt_parser = subparsers.add_parser('halt', help="Halt the Pipeline2")
    halt_parser.set_defaults(func=halt)

    args = parser.parse_args()
    # call the function assigned to the command
    args.func(args)

def get_scripts(args):
    """Print scripts XML"""
    doc = None
    if args.id:
        doc = resources.get_script(args.id)
    else:
        doc = resources.get_scripts()
    print_result(doc)

def get_jobs(args):
    """Print jobs XML"""
    doc = None
    if args.id:
        doc = resources.get_job(args.id)
    else:
        doc = resources.get_jobs()
    print_result(doc)

def get_log(args):
    """Print log"""
    status = resources.get_job_status(args.id)
    if status != "DONE" and status != "ERROR":
        print "Cannot get log until the job is done. Job status: %s." % status
        return
    log = resources.get_log(args.id)
    if log == "":
        print "No data returned"
        return
    print log

def get_result(args):
    """Write the job result to disk"""
    status = resources.get_job_status(args.id)
    if status != "DONE":
        print "Cannot get result until the job is done. Job status: %s." % status
        return
    
    response = resources.get_result(args.id)
    if response == None:
        print "No data returned"
        return
    
    path = "/tmp/%s.zip" % args.id
    zipfile = open(path, "wb")
    zipfile.write(response)
    zipfile.close()

def post_job(args):
    """Create a new job"""
    if os.path.exists(args.request) == False:
        print "Invalid request filepath"
        return
    
    datafile = open(args.request, "r")
    request = datafile.read()
    datafile.close()
    doc = None
    
    if args.job_data == None or args.job_data == "":
        doc = resources.post_job(request, None)
    else:
        if os.path.exists(args.job_data) == False:
            print "Invalid job data filepath"
            return
    
        doc = resources.post_job(args.request, args.job_data)
    
    print_result(doc)

def delete_job(args):
    """Delete a job"""
    status = resources.get_job_status(args.id)
    if status != "DONE" and status != "ERROR":
        print "Cannot delete until the job is done. Job status: %s." % status
        return
    
    result = resources.delete_job(args.id)
    if result != None:
        print "Job deleted"
    else:
        print "Error deleting job"

def delete_all(args):
    """Delete all jobs"""
    doc = resources.get_jobs()
    xpath_expr = "//{{{0}}}job/@id".format(settings.PX_NS)
    xpath_fn = etree.ETXPath(xpath_expr)
    results = xpath_fn(doc)
    for r in results:
        delete_job(r)

def alive(args):
    """Is the Pipeline2 alive?"""
    doc = resources.alive()
    print_result(doc)

def halt(args):
    """Halt the Pipeline2"""
    print "Halting..."
    resources.halt()

def print_result(doc):
    if doc == None:
        print "No data returned"
        return
    
    print(etree.tostring(doc, pretty_print=True))

if __name__ == "__main__":
    main()

