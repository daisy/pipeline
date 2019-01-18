package pipeline

import (
	// "bytes"
	"fmt"
	"strings"
	"testing"
	"reflect"
	"encoding/xml"
)

const (
	aliveXml   = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><alive authentication='false' localfs='true' version='1.6' xmlns='http://www.daisy.org/ns/pipeline/data'/>"
	scriptsXml = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><scripts href='http://localhost:8181/ws/scripts' xmlns='http://www.daisy.org/ns/pipeline/data' ><script href='http://localhost:8181/ws/scripts/zedai-to-epub3' id='zedai-to-epub3'><nicename>ZedAI to EPUB3</nicename><description>Transforms a ZedAI (DAISY 4 XML) document into an EPUB 3 publication.</description></script><script href='http://localhost:8181/ws/scripts/dtbook-to-html' id='dtbook-to-html'><nicename>DTBook to HTML</nicename><description>Transforms DTBook XML into HTML.</description></script><script href='http://localhost:8181/ws/scripts/dtbook-to-zedai' id='dtbook-to-zedai'><nicename>DTBook to ZedAI</nicename><description>Transforms DTBook XML into ZedAI XML.</description><version>1.0.0</version></script></scripts>"

	scriptXml     = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><script href='http://localhost:8181/ws/scripts/dtbook-to-zedai' id='dtbook-to-zedai' xmlns='http://www.daisy.org/ns/pipeline/data'><nicename>DTBook to ZedAI</nicename><description>Transforms DTBook XML into ZedAI XML.</description><version>1.0.0</version><homepage>http://code.google.com/p/daisy-pipeline/wiki/DTBookToZedAI</homepage><input desc='One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.' mediaType='application/x-dtbook+xml' name='source' sequence='true'/><option desc='The directory to store the generated files in.' name='output-dir' ordered='true' outputType='result' required='true' sequence='false' type='anyDirURI'/></script>"
	jobCreationOk = "<job xmlns='http://www.daisy.org/ns/pipeline/data' id='job-id-01' href='http://example.org/ws/jobs/job-id-01' status='DONE'/>"
	jobStatus     = `<?xml version="1.0" encoding="UTF-8" standalone="no"?>
                        <job xmlns="http://www.daisy.org/ns/pipeline/data" id="job-id-01" href="http://example.org/ws/jobs/job-id-01" status="DONE">
                                <!-- nicename is optional -->
                                <nicename>simple-dtbook-1</nicename>
                                <batchId>my-batch</batchId>
                                <script id="dtbook-to-zedai" href="http://example.org/ws/scripts/dtbook-to-zedai">
                                        <nicename>DTBook to ZedAI</nicename>
                                        <description>Transforms DTBook XML into ZedAI XML.</description>
                                </script>
                                <messages progress=".5">
                                        <message level="WARNING" sequence="22" content="Warning about this job"/>
                                </messages>
                                <log href="log"/>
                                <results href="http://example.org/ws/jobs/job-id-01/result" mime-type="zip">
                                        <result from="option" href="http://example.org/ws/jobs/job-id-01/result/option/output-dir" mime-type="zip" name="output-dir">
                                                <result href="http://example.org/ws/jobs/job-id-01/result/option/output-dir/file-1.xhtml" mime-type="application/xml"/>
                                        </result>
                                        <result from="port" href="http://example.org/ws/jobs/job-id-01/result/port/result" mime-type="zip" name="output-dir">
                                                <result href="http://example.org/ws/jobs/job-id-01/result/port/result/result-1.xml" mime-type="application/xml"/>
                                                <result href="http://example.org/ws/jobs/job-id-01/result/port/result/result-2.xml" mime-type="application/xml"/>
                                        </result>
                                </results>
                        </job>

                `
	errorXml = `
<?xml version="1.0" encoding="UTF-8"?>
<error query="http://localhost:8181/ws/jobs" xmlns="http://www.daisy.org/ns/pipeline/data">
    <description>Error while acquiring jobs</description>
    <trace>
    </trace>
</error>
        `
	jobsXml = `
<jobs xmlns="http://www.daisy.org/ns/pipeline/data" href="http://example.org/ws/jobs">
    <job id="job-id-01" href="http://example.org/ws/jobs/job-id-01" status="DONE">
        <nicename>job1</nicename>
    </job>
    <job id="job-id-02" href="http://example.org/ws/jobs/job-id-02" status="ERROR"/>
    <job id="job-id-03" href="http://example.org/ws/jobs/job-id-03" status="IDLE"/>
    <job id="job-id-04" href="http://example.org/ws/jobs/job-id-04" status="RUNNING">
        <nicename>job4</nicename>
    </job>
</jobs>
`
	T_STRING = "Wrong %v\nexpected: %v\nresult:%v\n"
)

var expected = map[string]interface{}{
	API_ALIVE:   Alive{FsAllow: true, Version: "1.6", Authentication: false},
	API_SCRIPTS: Scripts{Href: "http://localhost:8181/ws/scripts", Scripts: []Script{Script{}, Script{}, Script{}}},
	API_SCRIPT: Script{
		Href:        "http://localhost:8181/ws/scripts/dtbook-to-zedai",
		Nicename:    "DTBook to ZedAI",
		Description: "Transforms DTBook XML into ZedAI XML.",
		Version:     "1.0.0",
		Homepage:    "http://code.google.com/p/daisy-pipeline/wiki/DTBookToZedAI",
		Inputs: []Input{
			Input{
				Desc:      "One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.",
				Mediatype: "application/x-dtbook+xml",
				Name:      "source",
				Sequence:  true,
			},
		},
		Options: []Option{
			Option{
				Desc:       "The directory to store the generated files in.",
				Mediatype:  "application/x-dtbook+xml",
				Name:       "output-dir",
				Required:   true,
				Sequence:   false,
				Ordered:    true,
				OutputType: "result",
				Type:       "AnyFileURI",
			},
		},
	},
	API_JOBREQUEST: JobRequest{},

	API_JOB: Job{
		Id:       "job-id-01",
		BatchId:  "my-batch",
		Status:   "DONE",
		Nicename: "simple-dtbook-1",
		Log:      Log{Href: "log"},
		Messages: Messages{
			XMLName: xml.Name{Space: "http://www.daisy.org/ns/pipeline/data", Local: "messages",},
			Progress: .5,
			Message: []Message{
				Message{
					XMLName: xml.Name{Space: "http://www.daisy.org/ns/pipeline/data", Local: "message",},
					Level:    "WARNING",
					Sequence: 22,
					Content:  "Warning about this job",
				},
			},
		},
	},
}

//Actual tests
func TestAlive(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(aliveXml, 200))
	alive, err := pipeline.Alive()
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	exp := expected[API_ALIVE].(Alive).String()
	res := alive.String()
	if exp != res {
		t.Errorf("Alive decoding failed \nexpected %v \nresult %v", exp, res)
	}
}
func TestReqScripts(t *testing.T) {
	var scripts Scripts
	pipeline := createPipeline(emptyClientMock)
	r := pipeline.newResquest(API_SCRIPTS, &scripts, nil)
	if r.Url != "base/scripts" {
		t.Errorf("Scripts path set to %v", r.Url)
	}

}

func TestScripts(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(scriptsXml, 200))
	res, err := pipeline.Scripts()
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	exp := expected[API_SCRIPTS].(Scripts)
	if exp.Href != res.Href {
		t.Errorf("Scripts decoding failed (Href)\nexpected %v \nresult %v", exp.Href, res.Href)
	}
	if len(exp.Scripts) != len(res.Scripts) {
		t.Errorf("Scripts decoding failed (Scripts len)\nexpected %v \nresult %v", len(exp.Scripts), len(res.Scripts))
	}
}

func TestReqScript(t *testing.T) {
	var script Script
	pipeline := createPipeline(emptyClientMock)
	r := pipeline.newResquest(API_SCRIPT, &script, nil, "test")
	if r.Url != "base/scripts/test" {
		t.Errorf("Scripts path set to %v", r.Url)
	}

}

func TestScript(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(scriptXml, 200))
	res, err := pipeline.Script("test")
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	exp := expected[API_SCRIPT].(Script)
	if exp.Href != res.Href {
		t.Errorf("Scripts decoding failed (Href)\nexpected %v \nresult %v", exp.Href, res.Href)
	}
	if exp.Description != res.Description {
		t.Errorf("Script decoding failed (Description)\nexpected %v \nresult %v", exp.Description, res.Description)
	}
	if exp.Version != res.Version {
		t.Errorf("Script decoding failed (Version)\nexpected %v \nresult %v", exp.Version, res.Version)
	}
	if exp.Homepage != res.Homepage {
		t.Errorf("Scripts decoding failed (Homepage)\nexpected %v \nresult %v", exp.Homepage, res.Homepage)
	}
	if len(exp.Inputs) != len(res.Inputs) {
		t.Errorf("Scripts decoding failed (inputs)\nexpected %v \nresult %v", len(exp.Inputs), len(res.Inputs))
	}
	if len(exp.Options) != len(res.Options) {
		t.Errorf("Scripts decoding failed (inputs)\nexpected %v \nresult %v", len(exp.Options), len(res.Options))
	}
}
func TestJobReq(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(jobCreationOk, 201))
	res, err := pipeline.JobRequest(expected[API_JOBREQUEST].(JobRequest), nil)
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	if res.Id == "" {
		t.Error("job id not ok", err)
	}
}
func TestJobReqMultipart(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(jobCreationOk, 201))
	data := []byte("data")
	res, err := pipeline.JobRequest(expected[API_JOBREQUEST].(JobRequest), data)
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	if res.Id == "" {
		t.Error("job id not ok", err)
	}
}

func TestScriptUrl(t *testing.T) {
	pipeline := createPipeline(xmlClientMock("", 0))
	url := pipeline.ScriptUrl("unpalo")
	if url != "base/scripts/unpalo" {
		t.Errorf("Script url \nexpected %v \nresult %v", "base/scripts/unpalo", url)
	}
}

func TestJob(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(jobStatus, 200))
	res, err := pipeline.Job("jobId", 0)
	expJob := expected[API_JOB].(Job)
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}

	if expJob.Id != res.Id {
		t.Errorf(T_STRING, "id", expJob.Id, res.Id)
	}

	if expJob.Nicename != res.Nicename {
		t.Errorf(T_STRING, "nicename", expJob.Id, res.Id)
	}
	if expJob.BatchId != res.BatchId {
		t.Errorf(T_STRING, "batchId", expJob.BatchId, res.BatchId)
	}
	if expJob.Log.Href != res.Log.Href {
		t.Errorf(T_STRING, "log", expJob.Id, res.Id)
	}
	if len(res.Results.Result) != 2 {
		t.Errorf(T_STRING, "results len", 2, len(res.Results.Result))
	}
	if len(res.Results.Result[0].Result) != 1 {
		t.Errorf(T_STRING, "results len", 1, len(res.Results.Result[0].Result))
	}
	if len(res.Results.Result[1].Result) != 2 {
		t.Errorf(T_STRING, "results len", 2, len(res.Results.Result[1].Result))
	}
	if ! reflect.DeepEqual(expJob.Messages, res.Messages) {
		t.Errorf("Wrong messages\nexpected: %+v\nresult:%+v\n", expJob.Messages, res.Messages)
	}
}

// FIXME: mock currently can not handle two different requests (API_JOB and API_RESULT)
/*func TestResults(t *testing.T) {
	msg := "learn to swim"
	pipeline := createPipeline(xmlClientMock(msg, 200))
	pipeline := createPipeline(xmlClientMock(jobStatus, 200))
	buf := bytes.NewBuffer([]byte{})
	_, err := pipeline.Results("id", buf)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	//fmt.Printf("buf %+v\n", buf)
	res := buf.String()
	if msg != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "msg ", msg, res)
	}
}*/

func TestJobs(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(jobsXml, 200))
	res, err := pipeline.Jobs()
	idTemp := "job-id-0%v"
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	if len(res.Jobs) != 4 {
		t.Errorf("Wrong jobs size", res.Jobs)
	}
	for idx, job := range res.Jobs {
		jobId := fmt.Sprintf(idTemp, idx+1)
		if jobId != job.Id {
			t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "jobId ", jobId, job.Id)
		}
	}

}
func TestBatch(t *testing.T) {
	pipeline := createPipeline(xmlClientMock(jobsXml, 200))
	res, err := pipeline.Batch("my-batch")
	idTemp := "job-id-0%v"
	if err != nil {
		t.Errorf("Error not nil %v", err)
	}
	if len(res.Jobs) != 4 {
		t.Errorf("Wrong jobs size", res.Jobs)
	}
	for idx, job := range res.Jobs {
		jobId := fmt.Sprintf(idTemp, idx+1)
		if jobId != job.Id {
			t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "jobId ", jobId, job.Id)
		}
	}

}

/*func TestResultsNotFound(t *testing.T) {
	pipeline := createPipeline(structClientMock(true, 404))
	_, err := pipeline.Results("non existing", bytes.NewBuffer([]byte{}))
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
}*/

func TestDeleteBatch(t *testing.T) {
	pipeline := createPipeline(structClientMock(true, 204))
	ok, err := pipeline.DeleteBatch("my-batch")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if !ok {
		t.Errorf("Job was not deleted")
	}

}
func TestDeleteJob(t *testing.T) {
	pipeline := createPipeline(structClientMock(true, 204))
	ok, err := pipeline.DeleteJob("job1")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if !ok {
		t.Errorf("Job was not deleted")
	}

}
func TestDeleteJobError(t *testing.T) {
	pipeline := createPipeline(structClientMock(true, 404))
	ok, err := pipeline.DeleteJob("job1")
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
	if ok {
		t.Errorf("ok should be false")
	}

}

func TestAutheticator(t *testing.T) {
	var alive Alive
	r := Pipeline{}.newResquest(API_ALIVE, &alive, nil)
	authenticator("cli", "shhh")(r)
	url := r.Url
	if !strings.Contains(url, "sign") {
		t.Errorf("No sign in url %v", url)
	}
	if !strings.Contains(url, "time") {
		t.Errorf("No time in url %v", url)
	}

	if !strings.Contains(url, "nonce") {
		t.Errorf("No nonce in url %v", url)
	}

	if !strings.Contains(url, "authid") {
		t.Errorf("No nonce in url %v", url)
	}
}

func TestLog(t *testing.T) {
	log := "This is my log"
	pipeline := createPipeline(xmlClientMock(log, 200))
	cout, err := pipeline.Log("id1")
	if err != nil {
		t.Errorf("NewClient returned error but should be ok")
	}
	if string(cout) != log {
		t.Errorf("The returned log is not '%s' %s", log, string(cout))
	}
}
func TestLogNotFound(t *testing.T) {
	log := "This is my log"
	pipeline := createPipeline(xmlClientMock(log, 404))
	_, err := pipeline.Log("id1")
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
}

func TestHalt(t *testing.T) {
	pipeline := createPipeline(structClientMock(true, 204))
	err := pipeline.Halt("mykey")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

}

func TestHaltError(t *testing.T) {
	pipeline := createPipeline(structClientMock(true, 500))
	err := pipeline.Halt("mykey")
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
}
func TestNewClient(t *testing.T) {
	client := Client{Id: "id"}
	pipeline := createPipeline(structClientMock(client, 201))
	cout, err := pipeline.NewClient(client)
	if err != nil {
		t.Errorf("NewClient returned error but should be ok")
	}
	if cout.Id != "id" {
		t.Errorf("The returned id is not 'id' %v", cout.Id)
	}
}
func TestClientExists(t *testing.T) {
	client := Client{Id: "id"}
	pipeline := createPipeline(structClientMock(client, 400))
	_, err := pipeline.NewClient(client)
	if err == nil {
		t.Errorf("The client is already there but I got no error")
	}

}

func TestClient(t *testing.T) {
	client := Client{Id: "id"}
	pipeline := createPipeline(structClientMock(client, 200))
	cout, err := pipeline.Client("myclient!")
	if err != nil {
		t.Errorf("Client returned error but should be ok")
	}
	if cout.Id != "id" {
		t.Errorf("The returned id is not 'id' %v", cout.Id)
	}

}
func TestClientNotFound(t *testing.T) {
	client := Client{Id: "id"}
	pipeline := createPipeline(structClientMock(client, 404))
	_, err := pipeline.Client("that one, you know...")
	if err == nil {
		t.Errorf("Not found should have been thrown")
	}

}

func TestClients(t *testing.T) {
	clients := Clients{Clients: []Client{Client{Id: "1"}, Client{Id: "2"}}}
	pipeline := createPipeline(structClientMock(clients, 200))
	result, err := pipeline.Clients()
	if err != nil {
		t.Errorf("Clients returned error but should be ok")
	}
	if len(result) != 2 {
		t.Errorf("The returned client size is wrong 2!=%d", len(result))
	}

}
func TestClientsError(t *testing.T) {
	clients := Clients{Clients: []Client{Client{Id: "1"}, Client{Id: "2"}}}
	pipeline := createPipeline(structClientMock(clients, 500))
	_, err := pipeline.Clients()
	if err == nil {
		t.Errorf("Expected error not thrown")
	}

}
func TestDeleteClient(t *testing.T) {
	pipeline := createPipeline(structClientMock("", 204))
	ok, err := pipeline.DeleteClient("myclient!")
	if err != nil {
		t.Errorf("DeleteClient returned error but should be ok %#v", err)
	}
	if !ok {
		t.Errorf("Client deletion went wrong")
	}

}

func TestDeleteClientNotFound(t *testing.T) {

	pipeline := createPipeline(structClientMock("", 404))
	ok, err := pipeline.DeleteClient("that one, you know...")
	if err == nil {
		t.Errorf("Not found should have been thrown")
	}
	if ok {
		t.Errorf("Client deletion went wrong ... as expected")
	}

}

func TestModifyClient(t *testing.T) {
	client := Client{Id: "id", Secret: "other"}
	pipeline := createPipeline(structClientMock(client, 200))
	cout, err := pipeline.ModifyClient(client, "myclient!")
	if err != nil {
		t.Errorf("Client returned error but should be ok")
	}
	if cout.Id != "id" {
		t.Errorf("The returned id is not 'id' %v", cout.Id)
	}

}
func TestModifyClientNotFound(t *testing.T) {
	client := Client{Id: "id"}
	pipeline := createPipeline(structClientMock(client, 404))
	_, err := pipeline.ModifyClient(client, "that one, you know...")
	if err == nil {
		t.Errorf("Not found should have been thrown")
	}
}

func TestProperties(t *testing.T) {
	props := Properties{
		Properties: []Property{
			Property{Name: "prop1", Value: "ok"},
			Property{Name: "prop2", Value: "other"},
		},
	}
	pipeline := createPipeline(structClientMock(props, 200))
	res, err := pipeline.Properties()
	if err != nil {
		t.Errorf("Unexpected error %#v", err)
	}
	if len(res) != 2 {
		t.Errorf("I didn't get my two properties 2!= %d", len(res))
	}

}

func TestPropertiesError(t *testing.T) {
	pipeline := createPipeline(failingMock())
	_, err := pipeline.Properties()
	if err == nil {
		t.Errorf("Expected error didn't happen")
	}

}

func TestSizes(t *testing.T) {
	sizes := JobSizes{
		JobSizes: []JobSize{
			JobSize{Id: "job1", Context: 100},
			JobSize{Id: "job2", Context: 100},
		},
		Total: 200,
	}
	pipeline := createPipeline(structClientMock(sizes, 200))
	res, err := pipeline.Sizes()
	if err != nil {
		t.Errorf("Unexpected error %#v", err)
	}
	if len(res.JobSizes) != 2 {
		t.Errorf("I didn't get my two sizes 2!= %d", len(res.JobSizes))
	}
	if res.Total != 200 {
		t.Errorf("Total is wrong 200!=%#v", res.Total)
	}

}

func TestSizesError(t *testing.T) {
	pipeline := createPipeline(failingMock())
	_, err := pipeline.Sizes()
	if err == nil {
		t.Errorf("Expected error didn't happen")
	}

}

func TestQueue(t *testing.T) {
	queue := Queue{
		Jobs: []QueueJob{
			QueueJob{Id: "job1", JobPriority: "high"},
			QueueJob{Id: "job2", JobPriority: "high"},
		},
	}
	pipeline := createPipeline(structClientMock(queue, 200))
	res, err := pipeline.Queue()
	if err != nil {
		t.Errorf("Unexpected error %#v", err)
	}
	if len(res) != 2 {
		t.Errorf("I didn't get my two jobs 2!= %d", len(res))
	}

}

func TestQueueError(t *testing.T) {
	pipeline := createPipeline(failingMock())
	_, err := pipeline.Queue()
	if err == nil {
		t.Errorf("Expected error didn't happen")
	}
}
func TestMoveUp(t *testing.T) {
	queue := Queue{
		Jobs: []QueueJob{
			QueueJob{Id: "job1", JobPriority: "high"},
			QueueJob{Id: "job2", JobPriority: "high"},
		},
	}
	pipeline := createPipeline(structClientMock(queue, 200))
	res, err := pipeline.MoveUp("id")
	if err != nil {
		t.Errorf("Unexpected error %#v", err)
	}
	if len(res) != 2 {
		t.Errorf("I didn't get my two jobs 2!= %d", len(res))
	}

}
func TestMoveUpNotFound(t *testing.T) {
	queue := Queue{
		Jobs: []QueueJob{
			QueueJob{Id: "job1", JobPriority: "high"},
			QueueJob{Id: "job2", JobPriority: "high"},
		},
	}
	pipeline := createPipeline(structClientMock(queue, 404))
	_, err := pipeline.MoveUp("bad id")
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
}

func TestMoveDown(t *testing.T) {
	queue := Queue{
		Jobs: []QueueJob{
			QueueJob{Id: "job1", JobPriority: "high"},
			QueueJob{Id: "job2", JobPriority: "high"},
		},
	}
	pipeline := createPipeline(structClientMock(queue, 200))
	res, err := pipeline.MoveDown("id")
	if err != nil {
		t.Errorf("Unexpected error %#v", err)
	}
	if len(res) != 2 {
		t.Errorf("I didn't get my two jobs 2!= %d", len(res))
	}

}
func TestMoveDownNotFound(t *testing.T) {
	queue := Queue{
		Jobs: []QueueJob{
			QueueJob{Id: "job1", JobPriority: "high"},
			QueueJob{Id: "job2", JobPriority: "high"},
		},
	}
	pipeline := createPipeline(structClientMock(queue, 404))
	_, err := pipeline.MoveDown("bad id")
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
}
