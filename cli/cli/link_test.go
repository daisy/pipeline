package cli

import (
	"fmt"
	"net/url"
	"sort"
	"testing"

	"github.com/daisy/pipeline-clientlib-go"
)

var (
	JOB_REQUEST = JobRequest{
		Script:   "test",
		Nicename: "nice",
		Options: map[string][]func([]byte) (string, error){
			SCRIPT.Options[0].Name: []func([]byte) (string, error) {
				func([]byte) (string, error) { return "file1.xml", nil },
				func([]byte) (string, error) { return "file2.xml", nil },
			},
			SCRIPT.Options[1].Name: []func([]byte) (string, error){
				func([]byte) (string, error) { return "true", nil },
			},
		},
		Inputs: map[string][]func([]byte) (url.URL, error){
			SCRIPT.Inputs[0].Name: []func([]byte) (url.URL, error) {
				func([]byte) (url.URL, error) { return url.URL{Opaque: "tmp/file.xml"}, nil },
				func([]byte) (url.URL, error) { return url.URL{Opaque: "tmp/file1.xml"}, nil },
			},
			SCRIPT.Inputs[1].Name: []func([]byte) (url.URL, error) {
				func([]byte) (url.URL, error) { return url.URL{Opaque: "tmp/file2.xml"}, nil },
			},
		},
	}
	JOB_REQUEST_2 = JobRequest{
		Script:   "test",
		Nicename: "nice",
		Options: map[string][]func([]byte) (string, error){
			SCRIPT.Options[0].Name: []func([]byte) (string, error) {
				func([]byte) (string, error) { return "file1.xml", nil },
				func([]byte) (string, error) { return "file2.xml", nil },
			},
			SCRIPT.Options[1].Name: []func([]byte) (string, error){
				func([]byte) (string, error) { return "true", nil },
			},
		},
		Inputs: map[string][]func([]byte) (url.URL, error){
			SCRIPT.Inputs[0].Name: []func([]byte) (url.URL, error) {
				func([]byte) (url.URL, error) { return url.URL{Opaque: "tmp/file.xml"}, nil },
				func([]byte) (url.URL, error) { return url.URL{Opaque: "tmp/file1.xml"}, nil },
			},
			SCRIPT.Inputs[1].Name: []func([]byte) (url.URL, error) {
				func([]byte) (url.URL, error) { return url.URL{Opaque: "tmp/file2.xml"}, nil },
			},
		},
	}
	JOB_1 = pipeline.Job{
		Status:   "RUNNING",
		Nicename: "my_little_job",
		Id:       "job1",
		Priority: "low",
		Messages: pipeline.Messages{
			Progress: .75,
			Message: []pipeline.Message{
				pipeline.Message{
					Sequence: 1,
					Content:  "Message 1",
					Level:    "INFO",
				},
				pipeline.Message{
					Sequence: 2,
					Content:  "Message 2",
					Level:    "DEBUG",
				},
			},
		},
	}
	JOB_2 = pipeline.Job{
		Status:   "SUCCESS",
		Nicename: "the_other_job",
		Id:       "job2",
		Priority: "high",
		Messages: pipeline.Messages{
			Message: []pipeline.Message{
				pipeline.Message{
					Sequence: 3,
					Content:  "Message 3",
					Level:    "WARN",
				},
			},
		},
	}
	JOB_3 = pipeline.Job{
		Status:   "ERROR",
		Nicename: "errored job",
		Id:       "job3",
		Priority: "high",
		Messages: pipeline.Messages{
			Message: []pipeline.Message{
				pipeline.Message{
					Sequence: 3,
					Content:  "Message 3",
					Level:    "WARN",
				},
			},
		},
	}
)

type Inputs []pipeline.Input

func (in Inputs) Len() int {
	return len(in)
}
func (in Inputs) Swap(i, j int) {
	in[i], in[j] = in[j], in[i]
}
func (in Inputs) Less(i, j int) bool {
	return in[i].Name < in[j].Name
}

//Tests the correct creation of a new link
func TestNewLink(t *testing.T) {

	config[STARTING] = true
	config[HOST] = "www.daisy.org"
	config[PORT] = 8888
	config[PATH] = "ws"

	link := NewLink(config)
	{
		res := link.pipeline.(*pipeline.Pipeline).BaseUrl
		expected := "www.daisy.org:8888/ws/"
		if res != expected {
			t.Errorf("The url has not been properly set '%s'!='%s'", res, expected)
		}
	}
	{
		res := link.config[STARTING]
		expected := true
		if res != expected {
			t.Errorf("Config has not been properly set '%v'!='%v'", res, expected)
		}
	}
}
func TestBringUp(t *testing.T) {
	pipeline := newPipelineTest(false)
	pipeline.authentication = true
	config[STARTING] = false
	link := PipelineLink{pipeline: pipeline, config: config}
	err := bringUp(&link)
	if err != nil {
		t.Error("Unexpected error")
	}

	if link.Version != "version-test" {
		t.Error("Version not set")
	}
	if link.FsAllow != true {
		t.Error("Mode not set")
	}

	if !link.Authentication {
		t.Error("Authentication not set")
	}
}

func TestBringUpFail(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(true), config: config}
	err := bringUp(&link)
	if err == nil {
		t.Error("Expected error is nil")
	}
}

func TestCheckCredentials(t *testing.T) {

	pipeline := newPipelineTest(false)
	pipeline.authentication = true
	//both empty
	{
		cnf := copyConf()
		cnf[STARTING] = false
		cnf[CLIENTKEY] = ""
		cnf[CLIENTSECRET] = ""
		link := PipelineLink{pipeline: pipeline, config: cnf}
		link.Authentication = true
		err := link.Init()
		if err == nil {
			t.Errorf("Credentials should've error'd")
		}
	}
	{
		cnf := copyConf()
		cnf[STARTING] = false
		//client secret empty
		cnf[CLIENTKEY] = "key"
		cnf[CLIENTSECRET] = ""
		link := PipelineLink{pipeline: pipeline, config: cnf}
		link.Authentication = true
		err := link.Init()
		if err == nil {
			t.Errorf("Credentials should've error'd")
		}
	}
	{
		//client key  empty
		cnf := copyConf()
		cnf[STARTING] = false
		cnf[CLIENTKEY] = ""
		cnf[CLIENTSECRET] = "shhh"
		link := PipelineLink{pipeline: pipeline, config: cnf}
		link.Authentication = true
		err := link.Init()
		if err == nil {
			t.Errorf("Credentials should've error'd")
		}
	}
}
func TestSetCredentials(t *testing.T) {

	pipeline := newPipelineTest(false)
	pipeline.authentication = true
	cnf := copyConf()
	cnf[STARTING] = false
	cnf[CLIENTKEY] = "key"
	cnf[CLIENTSECRET] = "shh"
	link := PipelineLink{pipeline: pipeline, config: cnf}
	link.Authentication = true
	err := link.Init()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	{
		exp := "key"
		res := pipeline.key
		if exp != res {
			t.Errorf("Bad key %s!=%s", exp, res)
		}
	}
	{
		exp := "shh"
		res := pipeline.secret
		if exp != res {
			t.Errorf("Bad secret %s!=%s", exp, res)
		}
	}
}

func TestBadStart(t *testing.T) {

	pipeline := newPipelineTest(true)
	pipeline.authentication = true
	config[STARTING] = true
	config[APPPATH] = "nonexistingprogram"
	link := PipelineLink{pipeline: pipeline, config: config}
	err := link.Init()
	if err == nil {
		t.Errorf("Starting should've error'd")
	}
}

func TestScripts(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(false)}
	list, err := link.Scripts()
	if err != nil {
		t.Error("Unexpected error")
	}
	if len(list) != 1 {
		t.Error("Wrong list size")
	}
	res := list[0]
	exp := SCRIPT
	if exp.Href != res.Href {
		t.Errorf("Scripts decoding failed (Href)\nexpected %v \nresult %v", exp.Href, res.Href)
	}
	if exp.Description != res.Description {
		t.Errorf("Script decoding failed (Description)\nexpected %v \nresult %v", exp.Description, res.Description)
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

func TestScriptsFail(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(true)}
	_, err := link.Scripts()
	if err == nil {
		t.Error("Expected error is nil")
	}
}

func TestJobRequestToPipeline(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(false)}
	req, err := jobRequestToPipeline(JOB_REQUEST_2, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	if req.Script.Href != SCRIPT.Id {
		t.Errorf("JobRequest to pipeline failed \nexpected %v \nresult %v", SCRIPT.Id, req.Script.Href)
	}
	if "nice" != req.Nicename {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "nicename", "nice", req.Nicename)
	}
	sort.Sort(Inputs(req.Inputs))

	if len(req.Inputs) != 2 {
		t.Errorf("Bad input list len %v", len(req.Inputs))
	}
	for i := 0; i < 2; i++ {
		if req.Inputs[i].Name != SCRIPT.Inputs[i].Name {
			t.Errorf("JobRequest input %v to pipeline failed \nexpected %v \nresult %v", i, SCRIPT.Inputs[i].Name, req.Inputs[i].Name)
		}

	}
	input, _ := JOB_REQUEST_2.Inputs[req.Inputs[0].Name][0](nil)
	if (err != nil) {
		t.Error("Unexpected error")
	}
	if req.Inputs[0].Items[0].Value != input.String() {
		t.Errorf("JobRequest to pipeline failed \nexpected %v \nresult %v", input.String(), req.Inputs[0].Items[0].Value)
	}
	input, _ = JOB_REQUEST_2.Inputs[req.Inputs[0].Name][1](nil)
	if req.Inputs[0].Items[1].Value != input.String() {
		t.Errorf("JobRequest to pipeline failed \nexpected %v \nresult %v", input.String(), req.Inputs[0].Items[1].Value)
	}
	input, _ = JOB_REQUEST_2.Inputs[req.Inputs[1].Name][0](nil)
	if req.Inputs[1].Items[0].Value != input.String() {
		t.Errorf("JobRequest to pipeline failed \nexpected %v \nresult %v", input.String(), req.Inputs[1].Items[0].Value)
	}

	if len(req.Options) != 2 {
		t.Errorf("Bad option list len %v", len(req.Inputs))
	}

	optMap := map[string]pipeline.Option{}
	for _, opt := range req.Options {
		optMap[opt.Name] = opt

	}
	for i := 0; i < 2; i++ {
		if _, ok := optMap[SCRIPT.Options[i].Name]; !ok {
			t.Errorf("JobRequest to pipeline failed \nexpected %v not found in options", SCRIPT.Options[i].Name)
		}
	}
	{ //option test-opt
		name := "test-opt"
		opt := optMap[name]
		reqOpts := JOB_REQUEST_2.Options[name]
		if len(opt.Items) != len(reqOpts) {
			t.Errorf("Len of options mismatch %v %v", name, len(reqOpts))
		}
		for idx, item := range opt.Items {
			expected, _ := reqOpts[idx](nil)
			if item.Value != expected {
				t.Errorf("JobRequest to pipeline failed \nexpected %v \nresult %v", expected, item.Value)
			}

		}
	}
	{ //option another-top
		name := "another-opt"
		opt := optMap[name]
		if len(opt.Items) != 0 {
			t.Error("Simple option lenght !=0")
		}
		expected, _ := JOB_REQUEST_2.Options[name][0](nil)
		if opt.Value != expected {
			t.Errorf("JobRequest to pipeline failed \nexpected %v \nresult %v", expected, opt.Value)
		}
	}
}

func TestAsyncMessagesErr(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(true)}
	chMsg := make(chan Message)
	go getAsyncMessages(link, "jobId", chMsg)
	message := <-chMsg
	if message.Error == nil {
		t.Error("Expected error nil")
	}

}

func TestAsyncMessages(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(false)}
	chMsg := make(chan Message)
	var msgs []string
	go getAsyncMessages(link, "jobId", chMsg)
	for msg := range chMsg {
		msgs = append(msgs, msg.Message)
	}
	if len(msgs) != 4 {
		t.Errorf("Wrong message list size %v", len(msgs))
	}

	for i := 1; i != 3; i++ {
		if msgs[i-1] != fmt.Sprintf("Message %v", i) {
			t.Errorf("Wrong message %v", msgs[i-1])
		}
	}
}

func TestIsLocal(t *testing.T) {
	link := PipelineLink{FsAllow: true}
	if !link.IsLocal() {
		t.Errorf("Should be local %+v", link)
	}

	link = PipelineLink{FsAllow: false}
	if link.IsLocal() {
		t.Errorf("Should not be local %+v", link)
	}
}

func TestQueue(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(false)}
	link.Queue()
	if getCall(link) != "queue" {
		t.Errorf("The pipeline queue was not called")
	}
}

func TestMoveUp(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(false)}
	link.MoveUp("id")
	if getCall(link) != "moveup" {
		t.Errorf("moveup was not called")
	}
}

func TestMoveDown(t *testing.T) {
	link := PipelineLink{pipeline: newPipelineTest(false)}
	link.MoveDown("id")
	if getCall(link) != "movedown" {
		t.Errorf("movedown was not called")
	}
}
