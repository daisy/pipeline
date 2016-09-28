package cli

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"regexp"
	"strings"
	"sync/atomic"
	"testing"

	"github.com/daisy/pipeline-clientlib-go"
)

var (
	queue = []pipeline.QueueJob{
		pipeline.QueueJob{
			Id:               "job1",
			ClientPriority:   "high",
			JobPriority:      "low",
			ComputedPriority: 1.555555,
			RelativeTime:     0.577777,
			TimeStamp:        1400237879517,
		},
	}
	queueLine = []string{
		queue[0].Id,
		fmt.Sprintf("%.2f", queue[0].ComputedPriority),
		queue[0].JobPriority,
		queue[0].ClientPriority,
		fmt.Sprintf("%.2f", queue[0].RelativeTime),
		fmt.Sprintf("%d", queue[0].TimeStamp),
	}
)

//Tests the command and checks that the output is correct
func TestQueueCommand(t *testing.T) {
	cli, link, _ := makeReturningCli(queue, t)
	r := overrideOutput(cli)
	AddQueueCommand(cli, link)
	err := cli.Run([]string{"queue"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != QUEUE_CALL {
		t.Errorf("Queue wasn't call")
	}

	if ok, line, message := checkTableLine(r, "\t", queueLine); !ok {
		t.Errorf("Queue template doesn't match (%q,%s)\n%s", queueLine, line, message)
	}
}

//Tests that the move up command links to the pipeline and checks the output format
func TestMoveUpCommand(t *testing.T) {
	cli, link, _ := makeReturningCli(queue, t)
	r := overrideOutput(cli)
	AddMoveUpCommand(cli, link)

	err := cli.Run([]string{"moveup", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != MOVEUP_CALL {
		t.Errorf("moveup wasn't called")
	}

	if ok, line, message := checkTableLine(r, "\t", queueLine); !ok {
		t.Errorf("Queue template doesn't match (%q,%s)\n%s", queueLine, line, message)
	}
}

//Tests that the move down command links to the pipeline and checks the output format
func TestMoveDownCommand(t *testing.T) {
	cli, link, _ := makeReturningCli(queue, t)
	r := overrideOutput(cli)
	AddMoveDownCommand(cli, link)

	err := cli.Run([]string{"movedown", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != MOVEDOWN_CALL {
		t.Errorf("moveup wasn't called")
	}

	if ok, line, message := checkTableLine(r, "\t", queueLine); !ok {
		t.Errorf("Queue template doesn't match (%q,%s)\n%s", queueLine, line, message)
	}
}

//Tests that the version command links to the pipeline and checks the output format
func TestVersionCommand(t *testing.T) {
	pipe := newPipelineTest(false)
	link := PipelineLink{pipeline: pipe}
	cli, err := makeCli("test", &link)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	r := overrideOutput(cli)
	AddVersionCommand(cli, &link)

	err = cli.Run([]string{"version"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	values := checkMapLikeOutput(r)
	if val, ok := values["Client version"]; ok {
		//this is set by a constant, just check there is
		//a value
		if len(val) == 0 {
			t.Errorf("Client version is empty")
		}

	} else {
		t.Errorf("Client version not present")
	}

	if val, ok := values["Pipeline version"]; !ok || val != "version-test" {
		t.Errorf("Pipeline version 'version-test'!='%s'", val)

	}

	if val, ok := values["Pipeline authentication"]; !ok || val != "false" {
		t.Errorf("Pipeline authentication'false'!=%s", val)

	}
}

func makeReturningCli(val interface{}, t *testing.T) (*Cli, PipelineLink, *PipelineTest) {
	pipe := newPipelineTest(false)
	pipe.val = val
	link := PipelineLink{pipeline: pipe}
	cli, err := makeCli("test", &link)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	return cli, link, pipe
}

//Checks the call to the pipeline link and the output
func TestLogCommand(t *testing.T) {
	expected := []byte("Oh my log!")
	cli, link, _ := makeReturningCli(expected, t)
	r := overrideOutput(cli)
	AddLogCommand(cli, link)
	err := cli.Run([]string{"log", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != LOG_CALL {
		t.Errorf("log wasn't called")
	}

	result := string(r.Bytes())
	if result != string(expected) {
		t.Errorf("Log error %s!=%s", string(expected), result)
	}
}

//Checks the call to the pipeline link and the output
func TestLogCommandWithOutputFile(t *testing.T) {
	expected := []byte("Oh my log!")
	cli, link, _ := makeReturningCli(expected, t)
	r := overrideOutput(cli)
	AddLogCommand(cli, link)
	file, err := ioutil.TempFile("", "cli_")
	defer os.Remove(file.Name())
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	err = cli.Run([]string{"log", "-o", file.Name(), "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != LOG_CALL {
		t.Errorf("log wasn't called")
	}

	contents, err := ioutil.ReadAll(file)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	result := string(contents)
	if result != string(expected) {
		t.Errorf("Log error %s!=%s", string(expected), result)
	}
	if r.Len() == 0 {
		t.Errorf("We haven't informed the user that we wrote the file somewhere")
	}
}

//Tests the log command when an error is returned by the
//link
func TestLogCommandError(t *testing.T) {
	cli, link, pipe := makeReturningCli(nil, t)
	pipe.failOnCall = LOG_CALL
	AddLogCommand(cli, link)
	err := cli.Run([]string{"log", "id"})
	if getCall(link) != LOG_CALL {
		t.Errorf("log wasn't called")
	}
	if err == nil {
		t.Errorf("Exepected error not returned")
	}
}

//Tests the log command when there is a writing error
func TestLogCommandWritingError(t *testing.T) {
	cli, link, _ := makeReturningCli(nil, t)
	cli.Output = FailingWriter{}
	AddLogCommand(cli, link)
	err := cli.Run([]string{"log", "id"})
	if getCall(link) != LOG_CALL {
		t.Errorf("log wasn't called")
	}
	if err == nil {
		t.Errorf("Exepected error not returned")
	}
}

//Checks the call to the pipeline link and the output with the status command
func TestJobStatusCommand(t *testing.T) {
	//as mocking logic is more complex for jobs
	//expected := JOB_1
	cli, link, _ := makeReturningCli(nil, t)
	r := overrideOutput(cli)
	AddJobStatusCommand(cli, link)
	err := cli.Run([]string{"status", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != JOB_CALL {
		t.Errorf("status wasn't called")
	}

	values := checkMapLikeOutput(r)
	if id, ok := values["Job Id"]; !ok || id != JOB_1.Id {
		t.Errorf("Id doesn't match %s!=%s", JOB_1.Id, id)
	}
	if status, ok := values["Status"]; !ok || status != JOB_1.Status {
		t.Errorf("Status doesn't match %s!=%s", JOB_1.Status, status)
	}
	if status, ok := values["Priority"]; !ok || status != JOB_1.Priority {
		t.Errorf("Status doesn't match %s!=%s", JOB_1.Status, status)
	}
	if _, ok := values["Messages"]; ok {
		t.Errorf("I said to shut up! but I can see some msgs")
	}
}

//Checks that we get messages when using the verbose flag
func TestVerboseJobStatusCommand(t *testing.T) {
	//as mocking logic is more complex for jobs
	//expected := JOB_1
	cli, link, _ := makeReturningCli(nil, t)
	r := overrideOutput(cli)
	AddJobStatusCommand(cli, link)
	err := cli.Run([]string{"status", "-v", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != JOB_CALL {
		t.Errorf("status wasn't called")
	}
	exp := regexp.MustCompile("\\(\\d+\\)\\[\\w+\\]\\s+\\w+")
	matches := exp.FindAll(r.Bytes(), -1)
	if len(matches) != 2 {
		t.Errorf("The messages weren't printed output:\n%s", string(string(r.Bytes())))
	}

}

//Checks that the error is propagated when the link errors when calling status
func TestJobStatusCommandError(t *testing.T) {
	//as mocking logic is more complex for jobs
	//expected := JOB_1
	cli, link, p := makeReturningCli(nil, t)
	p.failOnCall = JOB_CALL
	overrideOutput(cli)
	AddJobStatusCommand(cli, link)
	err := cli.Run([]string{"status", "id"})
	if getCall(link) != JOB_CALL {
		t.Errorf("status wasn't called")
	}
	if err == nil {
		t.Errorf("Expected error not propagated")
	}

}

//Checks the call to the pipeline link and the output when deleting a job
func TestDeleteCommand(t *testing.T) {

	cli, link, _ := makeReturningCli(true, t)
	r := overrideOutput(cli)
	AddDeleteCommand(cli, link)
	err := cli.Run([]string{"delete", "%id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != DELETE_CALL {
		t.Errorf("delete wasn't called")
	}
	expected := "Job %id removed from the server\n"
	result := string(r.Bytes())
	if expected != result {
		t.Errorf("The message is not correct '%s'!='%s'", expected, result)
	}

}

//Checks the call to the pipeline link and the output when deleting a job
func TestDeleteCommandError(t *testing.T) {

	cli, link, p := makeReturningCli(false, t)
	p.failOnCall = DELETE_CALL
	AddDeleteCommand(cli, link)
	err := cli.Run([]string{"delete", "%id"})
	if getCall(link) != DELETE_CALL {
		t.Errorf("delete wasn't called")
	}
	if err == nil {
		t.Errorf("Expected error not propagated")
	}
}

//Checks the call to the pipeline link and the output when sending the halt signal
func TestHaltCommand(t *testing.T) {
	//create fake key
	backup := keyFile
	defer func() {
		keyFile = backup
	}()
	keyFile = "fakeKey"
	file, err := createKeyFile(keyFile, "key")
	defer os.Remove(file.Name())
	//keep on with the command testing
	cli, link, _ := makeReturningCli(nil, t)
	r := overrideOutput(cli)
	AddHaltCommand(cli, link)
	err = cli.Run([]string{"halt"})
	if err != nil {
		t.Errorf("Unexpected error running cli %v", err)
	}
	if getCall(link) != HALT_CALL {
		t.Errorf("Halt wasn't called")
	}
	expected := "The webservice has been halted\n"
	result := string(r.Bytes())
	if expected != result {
		t.Errorf("The halt message is not correct '%s'!='%s'", expected, result)
	}
}

//Checks the call to the pipeline link and the output when sending the halt signal and the key file wasn't found
func TestHaltCommandNoKeyFile(t *testing.T) {
	//create fake key
	backup := keyFile
	defer func() {
		keyFile = backup
	}()
	keyFile = "keythatdoesntexist!"
	//keep on with the command testing
	cli, link, _ := makeReturningCli(nil, t)
	AddHaltCommand(cli, link)
	err := cli.Run([]string{"halt"})
	if err == nil {
		t.Errorf("Expected error about key file not found not found")
	}
}

//Checks the call to the pipeline link and the output when sending the halt signal and the link returns an err
func TestHaltCommandError(t *testing.T) {
	//create fake key
	backup := keyFile
	defer func() {
		keyFile = backup
	}()
	keyFile = "fakeKey"
	file, err := createKeyFile(keyFile, "key")
	defer os.Remove(file.Name())
	//keep on with the command testing
	cli, link, p := makeReturningCli(nil, t)
	p.failOnCall = HALT_CALL
	AddHaltCommand(cli, link)
	err = cli.Run([]string{"halt"})
	if getCall(link) != HALT_CALL {
		t.Errorf("Halt wasn't called")
	}
	if err == nil {
		t.Errorf("Expected error about the link failing not returned")
	}
}

//Checks that results link entry has been called and data has been stored
func TestResultsCommand(t *testing.T) {
	data := createZipFile(t)
	cli, link, _ := makeReturningCli(data, t)
	r := overrideOutput(cli)
	AddResultsCommand(cli, link)
	dir, err := ioutil.TempDir("", "cli_")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	defer os.Remove(dir)
	err = cli.Run([]string{"results", "-o", dir, "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != RESULTS_CALL {
		t.Errorf("results wasn't called")
	}
	files := 0
	filepath.Walk(dir, func(path string, info os.FileInfo, err error) error {
		if info.IsDir() {
			files += 1
		}
		return nil

	})
	if files != 3 {
		t.Errorf("Wrong number of files %d!=%d", files, 3)
	}

	msg := string(r.Bytes())
	expected := fmt.Sprintf("Results stored into %v\n", dir)
	if msg != expected {
		t.Errorf("Got the wrong message '%s'!='%s'", expected, msg)
	}

}

//Checks that results link entry has been called and data has been stored
func TestResultsCommandToZip(t *testing.T) {
	data := createZipFile(t)
	cli, link, _ := makeReturningCli(data, t)
	r := overrideOutput(cli)
	AddResultsCommand(cli, link)
	file, err := ioutil.TempFile("", "cli_")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	defer os.Remove(file.Name())
	err = cli.Run([]string{"results", "-z", "-o", file.Name(), "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != RESULTS_CALL {
		t.Errorf("results wasn't called")
	}

	msg := string(r.Bytes())
	expected := fmt.Sprintf("Results stored into zipfile %v\n", file.Name())
	if msg != expected {
		t.Errorf("Got the wrong message '%s'!='%s'", expected, msg)
	}

}

//Checks that results link entry has been called but the error of wrong zip data is returned
func TestResultsCommandBadZipFormat(t *testing.T) {
	data := []byte("i'm not a zip file")
	cli, link, _ := makeReturningCli(data, t)
	//r := overrideOutput(cli)
	AddResultsCommand(cli, link)
	err := cli.Run([]string{"results", "-o", "/whatever", "id"})
	if getCall(link) != RESULTS_CALL {
		t.Errorf("results wasn't called")
	}
	if err == nil {
		t.Errorf("Expected zip error not thrown")
	}

}

//Checks that results link entry has been called but returned an error
func TestResultsCommandError(t *testing.T) {
	data := createZipFile(t)
	cli, link, pipe := makeReturningCli(data, t)
	pipe.failOnCall = RESULTS_CALL

	//r := overrideOutput(cli)
	AddResultsCommand(cli, link)
	err := cli.Run([]string{"results", "-o", "/whatever", "id"})
	if getCall(link) != RESULTS_CALL {
		t.Errorf("results wasn't called")
	}
	if err == nil {
		t.Errorf("Expected ws error not returned ")
	}

}

//test the list of jobs
func TestJobs(t *testing.T) {
	jobs := pipeline.Jobs{Jobs: []pipeline.Job{JOB_1, JOB_2}}
	cli, link, _ := makeReturningCli(jobs, t)
	r := overrideOutput(cli)
	AddJobsCommand(cli, link)
	err := cli.Run([]string{"jobs"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != JOBS_CALL {
		t.Errorf("jobs wasn't called")
	}
	jobsLine := []string{
		JOB_1.Id,
		fmt.Sprintf("(%s)", JOB_1.Nicename),
		fmt.Sprintf("[%s]", JOB_1.Status),
	}
	if ok, line, message := checkTableLine(r, "\t", jobsLine); !ok {
		t.Errorf("job template doesn't match (%q,%s)\n%s", jobsLine, line, message)
	}
}

//test the list of jobs with error from the link
func TestJobsError(t *testing.T) {
	jobs := pipeline.Jobs{Jobs: []pipeline.Job{JOB_1, JOB_2}}
	cli, link, pipe := makeReturningCli(jobs, t)
	pipe.failOnCall = JOBS_CALL
	AddJobsCommand(cli, link)
	err := cli.Run([]string{"jobs"})
	if getCall(link) != JOBS_CALL {
		t.Errorf("jobs wasn't called")
	}
	if err == nil {
		t.Errorf("Error from link not propagated")
	}
}

func TestClean(t *testing.T) {
	jobs := pipeline.Jobs{Jobs: []pipeline.Job{JOB_2, JOB_3}}
	cli, link, p := makeReturningCli(jobs, t)
	jobsCalled := false
	deleteCalled := 0
	deletedId := ""
	p.jobs = func() (pipeline.Jobs, error) {
		jobsCalled = true
		return jobs, nil
	}
	p.delete = func(id string) (bool, error) {
		deleteCalled++
		deletedId = id
		return true, nil
	}

	r := overrideOutput(cli)
	AddCleanCommand(cli, link)
	err := cli.Run([]string{"clean"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if !jobsCalled {
		t.Errorf("Jobs wasn't called")
	}
	if deleteCalled != 1 {
		t.Errorf("Delete wasn't called exactly once")
	}
	if deletedId != JOB_3.Id {
		t.Errorf("Delete wasn't called on the errored job")
	}
	expected := fmt.Sprintf("Job %v removed from the server\n", JOB_3.Id)
	result := string(r.Bytes())
	if expected != result {
		t.Errorf("The message is not correct '%s'!='%s'", expected, result)
	}

}
func TestCleanAll(t *testing.T) {
	jobs := pipeline.Jobs{Jobs: []pipeline.Job{JOB_2, JOB_3}}
	cli, link, p := makeReturningCli(jobs, t)
	jobsCalled := false
	deleteCalled := int32(0)
	p.jobs = func() (pipeline.Jobs, error) {
		jobsCalled = true
		return jobs, nil
	}
	p.delete = func(id string) (bool, error) {
		atomic.AddInt32(&deleteCalled, 1)
		return true, nil
	}

	r := overrideOutput(cli)
	AddCleanCommand(cli, link)
	err := cli.Run([]string{"clean", "-d"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if !jobsCalled {
		t.Errorf("Jobs wasn't called")
	}
	if deleteCalled != 2 {
		t.Errorf("Delete wasn't called twice")
	}
	//the calls to delete are parallel
	result := string(r.Bytes())
	if !strings.Contains(result, JOB_2.Id) {
		t.Errorf("Id from JOB_2 is not in the output")
	}
	if !strings.Contains(result, JOB_3.Id) {
		t.Errorf("Id from JOB_3 is not in the output")
	}
}
func TestCleanErrors(t *testing.T) {
	jobs := pipeline.Jobs{Jobs: []pipeline.Job{JOB_2, JOB_3}}
	cli, link, p := makeReturningCli(jobs, t)
	jobsCalled := false
	deleteCalled := false
	errDel := fmt.Errorf("error")
	p.jobs = func() (pipeline.Jobs, error) {
		jobsCalled = true
		return jobs, nil
	}
	p.delete = func(id string) (bool, error) {
		deleteCalled = true
		return false, errDel
	}

	r := overrideOutput(cli)
	AddCleanCommand(cli, link)
	err := cli.Run([]string{"clean"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if !jobsCalled {
		t.Errorf("Jobs wasn't called")
	}
	if !deleteCalled {
		t.Errorf("Delete wasn't called ")
	}
	expected := fmt.Sprintf("Couldn't remove Job %v from the server (%v)\n", JOB_3.Id, errDel)
	result := string(r.Bytes())
	if expected != result {
		t.Errorf("The message is not correct '%s'!='%s'", expected, result)
	}
}
