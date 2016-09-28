package cli

import (
	//"github.com/capitancambio/go-subcommand"
	"io/ioutil"
	"os"
	"strconv"
	"testing"

	"github.com/daisy/pipeline-clientlib-go"
)

var SCRIPT pipeline.Script = pipeline.Script{
	Nicename:    "test-to-test",
	Description: "Mocked script",
	Homepage:    "daisy.org",
	Href:        "daisy.org/test",
	Id:          "test",
	Options: []pipeline.Option{
		pipeline.Option{
			Required:  true,
			Sequence:  false,
			Name:      "test-opt",
			Ordered:   false,
			Mediatype: "xml",
			Desc:      "I'm a test option",
			Type:      "anyFileURI",
		},
		pipeline.Option{
			Required:  false,
			Sequence:  false,
			Name:      "another-opt",
			Ordered:   false,
			Mediatype: "xml",
			Desc:      "I'm a test option",
			Type:      "boolean",
		},
	},
	Inputs: []pipeline.Input{
		pipeline.Input{
			Desc:      "input port not seq",
			Mediatype: "application/x-dtbook+xml",
			Name:      "single",
			Sequence:  false,
		},
		pipeline.Input{
			Desc:      "input port",
			Mediatype: "application/x-dtbook+xml",
			Name:      "source",
			Sequence:  true,
		},
	},
}

var in1, in2 = "tmp/dir1/file.xml", "tmp/dir2/file.xml"

func makeCli(name string, link *PipelineLink) (*Cli, error) {

	cli, err := NewCli(name, link)
	if err != nil {
		return nil, err
	}
	cli.Output = ioutil.Discard
	link.pipeline.(*PipelineTest).withScripts = false
	return cli, err
}

func TestCliAddScriptCommand(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	cli.AddScriptCommand("test", "", func(string, ...string) error { return nil }, nil)
	if cli.Scripts[0].Name != "test" {
		t.Error("Add script is not adding scripts to the list")
	}
}

func TestCliAddAdminCommand(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	cli.AddAdminCommand("stest", "", func(string, ...string) error { return nil })
	if cli.AdminCommands[0].Name != "stest" {
		t.Error("Add Command is not adding commands to the list")
	}

	if len(cli.Scripts) != 0 {
		t.Error("Scripts is not empty")
	}
}

func TestCliNonRequiredOptions(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{FsAllow: true, pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("testprog", link)
	link.pipeline.(*PipelineTest).withScripts = true
	if err != nil {
		t.Error("Unexpected error")
	}
	//parser.Parse([]string{"test","--source","value"})
	err = cli.Run([]string{"test", "-o", os.TempDir(), "--source", "./tmp/file", "--single", "./tmp/file2", "--test-opt", "./myfile.xml"})
	if err != nil {
		t.Errorf("Non required option threw an error %v", err.Error())
	}
}

func TestPrintHelpErrors(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	cli.AddScripts([]pipeline.Script{SCRIPT}, link)
	//more than one parameter fail
	err = printHelp(*cli, false, false, "one", "two")
	if err == nil {
		t.Error("Expected error (more than one param) is nil")
	}
	err = printHelp(*cli, false, false, "one")
	if err == nil {
		t.Error("Expected error (unknown command) is nil")
	}

}

func TestClientNew(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("testprog", link)
	link.pipeline.(*PipelineTest).withScripts = false
	if err != nil {
		t.Error("Unexpected error")
	}
	cli.AddNewClientCommand(*link)
	//Bad role
	err = cli.Run([]string{"create", "-i", "paco", "-r", "PLUMBER", "-s", "sshh"})
	if err == nil {
		t.Error("Bad role didn't err")
	}
	cli, err = makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	cli.AddNewClientCommand(*link)
	err = cli.Run([]string{"create", "-r", "ADMIN", "-s", "sshh"})
	if err == nil {
		t.Error("No id didn't err")
	}
	cli, err = makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	cli.AddNewClientCommand(*link)
	err = cli.Run([]string{"create", "-r", "ADMIN", "-i", "paco"})
	if err == nil {
		t.Error("No no secret didn't err")
	}
}

func TestClientDelete(t *testing.T) {
	config[STARTING] = false
	link := &PipelineLink{pipeline: newPipelineTest(false), config: config}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	cli.AddDeleteClientCommand(*link)
	//Bad role
	err = cli.Run([]string{"delete"})
	if err == nil {
		t.Error("Bad number of args didn't err")
	}

	err = cli.Run([]string{"delete", "uno", "due"})
	if err == nil {
		t.Error("Bad number of args didn't err")
	}
}

func TestConfigIntOptions(t *testing.T) {
	res := copyConf()
	link := &PipelineLink{pipeline: newPipelineTest(false), config: res}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false

	err = cli.Run([]string{"--" + PORT, "harwich", "test"})
	if err == nil {
		t.Errorf("Port: non numeric type controll failed")
	}
	err = cli.Run([]string{"--" + WSTIMEUP, "abit", "test"})
	if err == nil {
		t.Errorf("ws_time_out: non numeric type controll failed")
	}
	err = cli.Run([]string{"--timeout" + TIMEOUT, "now!", "test"})
	if err == nil {
		t.Errorf("Port: non numeric type controll failed")
	}

}
func TestConfigBooleanOptions(t *testing.T) {
	res := copyConf()
	link := &PipelineLink{pipeline: newPipelineTest(false), config: res}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false

	err = cli.Run([]string{"--" + DEBUG, "please", "test"})
	if err == nil {
		t.Errorf("debug: non boolean type controll failed")
	}
	err = cli.Run([]string{"--" + STARTING, "abit", "test"})
	if err == nil {
		t.Errorf("starting: non boolean type controll failed")
	}
	err = cli.Run([]string{"--timeout" + TIMEOUT, "now!", "test"})
	if err == nil {
		t.Errorf("Port: no numeric type controll failed")
	}

}
func TestConfigOptions(t *testing.T) {
	res := copyConf()
	link := &PipelineLink{pipeline: newPipelineTest(false), config: res}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	exp := Config{
		HOST:         "http://google.com",
		PORT:         80,
		PATH:         "pipeline",
		WSTIMEUP:     1,
		EXECLINE:     "the_noose",
		CLIENTKEY:    "rounded",
		CLIENTSECRET: "he_likes_justin_beiber",
		TIMEOUT:      3,
		DEBUG:        true,
		STARTING:     true,
	}

	err = cli.Run([]string{"--" + HOST, exp[HOST].(string),
		"--" + PORT, strconv.Itoa(exp[PORT].(int)),
		"--" + PATH, exp[PATH].(string),
		"--" + WSTIMEUP, strconv.Itoa(exp[WSTIMEUP].(int)),
		"--" + EXECLINE, exp[EXECLINE].(string),
		"--" + CLIENTKEY, exp[CLIENTKEY].(string),
		"--" + CLIENTSECRET, exp[CLIENTSECRET].(string),
		"--" + TIMEOUT, strconv.Itoa(exp[TIMEOUT].(int)),
		"--" + DEBUG, strconv.FormatBool(true),
		"--" + STARTING, strconv.FormatBool(true),
		"help",
	})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	for k := range res {
		if res[k] != exp[k] {
			t.Errorf("Config item not set %v\n Expected: %v\nResult: %v", k, res[k], exp[k])
		}

	}
}
func TestConfigFileDoesNotExists(t *testing.T) {

	res := copyConf()
	link := &PipelineLink{pipeline: newPipelineTest(false), config: res}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	err = cli.Run([]string{"-f", "/tmp/theprobabilitythatthisfileactuallyexistsshouldbereallycloseto0",
		"help",
	})
	if err == nil {
		t.Error("Well, you took your chances and you lost")
	}

}

func TestConfigFile(t *testing.T) {
	tmpFile, err := ioutil.TempFile("", "dp2_test_")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	//YAML defined in config_test
	_, err = tmpFile.WriteString(YAML)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	err = tmpFile.Close()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	res := copyConf()
	link := &PipelineLink{pipeline: newPipelineTest(false), config: res}
	cli, err := makeCli("testprog", link)
	if err != nil {
		t.Error("Unexpected error")
	}
	link.pipeline.(*PipelineTest).withScripts = false
	err = cli.Run([]string{"-f", tmpFile.Name(),
		"help",
	})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	tCompareCnfs(res, EXP, t)

}
