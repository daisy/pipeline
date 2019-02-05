package cli

import (
	"fmt"

	"github.com/capitancambio/go-subcommand"
	//"github.com/capitancambio/go-subcommand"
	//"github.com/daisy-consortium/pipeline-clientlib-go"
	"io/ioutil"
	"log"
	"os"
	"testing"
)

func TestGetBasePath(t *testing.T) {
	//return os.Getwd()
	basePath := getBasePath(true)
	if len(basePath) == 0 {
		t.Error("Base path is 0")
	}
	if basePath[len(basePath)-1] != "/"[0] {
		t.Error("Last element of the basePath should be /")
	}
	basePath = getBasePath(false)
	if len(basePath) != 0 {
		t.Errorf("Base path len is !=0: %v", basePath)
	}
}

func TestParseInputs(t *testing.T) {
	log.SetOutput(ioutil.Discard)
	url, err := pathToUri(in1, "")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
		return
	}
	if url.String() != in1 {
		t.Errorf("Url 1 is not formatted %v", url.String())
	}
	url, err = pathToUri(in2, "")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
		return
	}
	if url.String() != in2 {
		t.Errorf("Url 2 is not formatted %v", url.String())
	}
}

func TestParseInputsBased(t *testing.T) {
	url, err := pathToUri(in1, "/mydata/")
	if !os.IsNotExist(err) {
		t.Errorf("Unexpected pass %v", err)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
			return
		}
	}
	if url.String() != "file:///mydata/"+"tmp/dir1/file.xml" {
		t.Errorf("Url 1 is not formated %v", url.String())
	}
	url, err = pathToUri(in2, "/mydata/")
	if !os.IsNotExist(err) {
		t.Errorf("Unexpected pass %v", err)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
			return
		}
	}
	if url.String() != "file:///mydata/"+"tmp/dir2/file.xml" {
		t.Errorf("Url 1 is not formated %v", url.String())
	}
}
func TestScriptPriority(t *testing.T) {
	config := copyConf()
	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	jobRequest, err := scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-o", os.TempDir(), "-d", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar", "--priority", "low"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if jobRequest.Priority != "low" {
		t.Errorf("Priority not set %s!=%s", jobRequest.Priority, "low")
	}
}

func TestScriptNiceName(t *testing.T) {
	config := copyConf()
	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	jobRequest, err := scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-o", os.TempDir(), "-d", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar", "--nicename", "my_job"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if jobRequest.Nicename != "my_job" {
		t.Errorf("Nice name not set %s!=%s", jobRequest.Nicename, "my_job")
	}
}
func TestScriptPriorityMedium(t *testing.T) {
	config := copyConf()
	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	////medium
	err = cli.Run([]string{"test", "-o", os.TempDir(), "-d", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar", "--priority", "medium"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
}
func TestScriptPriorityHigh(t *testing.T) {
	config := copyConf()
	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	////medium
	err = cli.Run([]string{"test", "-o", os.TempDir(), "-d", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar", "--priority", "high"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
}
func TestScriptPriorityWrongValue(t *testing.T) {
	config := copyConf()
	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	err = cli.Run([]string{"test", "-o", os.TempDir(), "-d", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar", "--priority", "not_so_low"})
	if err == nil {
		t.Errorf("Wrong priority value didn't error")
	}
}

func TestScriptToCommand(t *testing.T) {
	config := copyConf()
	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	jobRequest, err := scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-o", os.TempDir(), "-d", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if jobRequest.Script != "test" {
		t.Error("script not set")
	}
	fmt.Printf("jobRequest.Inputs %+v\n", jobRequest.Inputs)
	if jobRequest.Inputs["source"][0].String() != "./tmp/file" {
		t.Errorf("Input source not set %v", jobRequest.Inputs["source"][0].String())
	}
	if jobRequest.Inputs["single"][0].String() != "./tmp/file2" {
		t.Errorf("Input source not set %v", jobRequest.Inputs["source"][0].String())
	}
	if jobRequest.Options["test-opt"][0] != "./myfile.xml" {
		t.Errorf("Option test opt not set %v", jobRequest.Options["test-opt"][0])
	}

	if jobRequest.Options["another-opt"][0] != "bar" {
		t.Errorf("Option test opt not set %v", jobRequest.Options["another-opt"][0])
	}
}

func TestScriptToCommandNoLocalFail(t *testing.T) {

	config[STARTING] = false
	pipeline := newPipelineTest(false)
	pipeline.fsallow = false
	link := &PipelineLink{pipeline: pipeline, config: config}
	cli, err := makeCli("test", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	overrideOutput(cli)
	link.pipeline.(*PipelineTest).withScripts = false
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-o", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar"})
	if err == nil {
		t.Error("Expected error not thrown")
	}
}
func TestCliRequiredOptions(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{FsAllow: true, pipeline: newPipelineTest(true), config: config}
	cli, err := makeCli("test", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	overrideOutput(cli)
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "--source", "./tmp/file", "--single", "./tmp/file2", "--another-opt", "bar"})
	if err == nil {
		t.Errorf("Missing required option wasn't thrown")
	}
}

func TestStoreLastId(t *testing.T) {
	LastIdPath = os.TempDir() + string(os.PathSeparator) + "testLastId"
	//mariachi style
	id := "ayayyyyaaay"
	err := storeLastId(id)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	idGet, err := getLastId()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	if id != idGet {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "id ", id, idGet)
	}
	os.Remove(LastIdPath)
}

func TestGetLastIdErr(t *testing.T) {
	LastIdPath = os.TempDir() + string(os.PathSeparator) + "pipeline_go_testing_id_bad"
	_, err := getLastId()
	if err == nil {
		t.Error("Expected error not thrown")
	}
	LastIdPath = os.TempDir() + string(os.PathSeparator) + "testLastId"

}

func TestScriptNoOutput(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{FsAllow: true, pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("test", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	overrideOutput(cli)
	link.pipeline.(*PipelineTest).withScripts = false
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar"})
	if err == nil {
		t.Errorf("No error thrown")
	}
}

func TestScriptDefault(t *testing.T) {
	pipeline := newPipelineTest(false)
	link := &PipelineLink{FsAllow: true, pipeline: pipeline}
	cli, err := makeCli("test", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	overrideOutput(cli)
	link.pipeline.(*PipelineTest).withScripts = false
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-o", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar"})
	// FIXME: make this job pass
	if !os.IsNotExist(err) {
		t.Errorf("Unexpected pass %v", err)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
			return
		}
		if !pipeline.deleted {
			t.Error("Job not deleted ")
		}
	}
}

func TestScriptBackground(t *testing.T) {
	pipeline := newPipelineTest(false)
	link := &PipelineLink{FsAllow: true, pipeline: pipeline}
	cli, err := makeCli("test", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	overrideOutput(cli)
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-b", "-o", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar"})
	// FIXME: make this job pass
	if !os.IsNotExist(err) {
		t.Errorf("Unexpected pass %v", err)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
			return
		}
		if pipeline.deleted {
			t.Error("Job deleted in the background")
		}
		if pipeline.count != 0 {
			t.Error("gathering the job several times in the background")
		}
		if pipeline.resulted {
			t.Error("tried to get the results from a background job")
		}
	}
}

func TestScriptPersistent(t *testing.T) {
	pipeline := newPipelineTest(false)
	link := &PipelineLink{FsAllow: true, pipeline: pipeline}
	cli, err := makeCli("test", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	overrideOutput(cli)
	link.pipeline.(*PipelineTest).withScripts = false
	_, err = scriptToCommand(SCRIPT, cli, link)
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-p", "-o", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml", "--another-opt", "bar"})
	// FIXME: make this job pass
	if !os.IsNotExist(err) {
		t.Errorf("Unexpected pass %v", err)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
			return
		}
		if pipeline.deleted {
			t.Error("Job deleted and should be persistent")
		}
		if pipeline.count == 0 {
			t.Error("Persistent job did not gather several times its status from the server")
		}
		if getCall(*link) != RESULTS_CALL {
			t.Errorf("Persistent job did not gather its results")
		}
	}
}
func TestGetFlagName(t *testing.T) {
	name := getFlagName("myflag", "", []subcommand.Flag{subcommand.Flag{}})
	if name != "myflag" {
		t.Errorf("name is my myflag != %v", name)
	}

}
func TestGetFlagCommonName(t *testing.T) {
	name := getFlagName("myflag", "t-", []subcommand.Flag{subcommand.Flag{Long: "--myflag"}})
	if name != "t-myflag" {
		t.Errorf("expting t-myflag!= %v", name)
	}

}
func TestGetFlagCommonExists(t *testing.T) {
	name := getFlagName("output", "t-", []subcommand.Flag{subcommand.Flag{}})
	if name != "t-output" {
		t.Errorf("expting t-output != %v", name)
	}

}
