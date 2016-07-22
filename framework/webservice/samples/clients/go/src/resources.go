package main
import (
	"fmt"
	"os"
	"io/ioutil"
)
func get_scripts() string{
	return get_resource(SCRIPTS_URI)
}

func get_script(id string) string {
	uri:= fmt.Sprintf("%s/%s",SCRIPTS_URI,id)
	return get_resource(uri)
}

func get_jobs() string{
	return get_resource(JOBS_URI)
}

func get_job(id string) string {
	uri:= fmt.Sprintf("%s/%s",JOBS_URI,id)
	return get_resource(uri)
}

func get_log(id string) string {
	uri:= fmt.Sprintf("%s/%s/log",JOBS_URI,id)
	return get_resource(uri)
}

func get_result(id string) string {
	uri:= fmt.Sprintf("%s/%s/result",JOBS_URI,id)
	return get_resource(uri)
}

func alive() string{
	uri:= fmt.Sprintf("%s/alive",FIRST_PART)
	return get_resource(uri)
}

func delete_job(id string) {
	uri:= fmt.Sprintf("%s/%s",JOBS_URI,id)
	delete_resource(uri)
}

func halt() string{
	keyFile:=fmt.Sprintf("%s%cdp2key.txt",os.TempDir(),os.PathSeparator)
	key,err:=ioutil.ReadFile(keyFile)
	if(err!=nil){
		println("Error reading key file",keyFile)
		return "error"
	}
	uri:=fmt.Sprintf("%s/admin/halt/%s",FIRST_PART,string(key))
	return get_resource(uri)

}

//def post_job(request, data):
    //"""Create a new job and return the job description XML document"""
    //uri = "{0}/jobs".format(settings.BASEURI)
    //if data == None:
        //doc = rest.post_resource(uri, request)
    //else:
        //doc = rest.post_resource(uri, {"job-data": data, "job-request": request})
    //return doc


//def get_job_status(job_id):
    //"""Return the status of the given job"""
    //doc = get_job(job_id)
    //if doc == None:
        //return ""
    //xpath_expr = "//{{{0}}}job".format(settings.PX_NS)
    //xpath_fn = etree.ETXPath(xpath_expr)
    //results = xpath_fn(doc)
    //return results[0].attrib['status']
