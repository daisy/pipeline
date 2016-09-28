package cli

import (
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"testing"

	"github.com/kardianos/osext"
)

var (
	YAML = `
#WS CONFIGURATION
host: http://daisy.org
port: 9999
ws_path: ws
ws_timeup: 10
#DP2 launch config 
exec_line: prog 
local: true
# ROBOT CONF
client_key: clientid
client_secret: supersecret
#connection settings
timeout: 10
#debug
debug: true 
starting: true
`
	T_STRING = "Wrong %v\nexpected: %v\nresult:%v\n"
	EXP      = map[string]interface{}{
		"url":           "http://localhost:8181/ws/",
		"host":          "http://daisy.org",
		"port":          9999,
		"ws_path":       "ws",
		"ws_timeup":     10,
		"exec_line":     "prog",
		"client_key":    "clientid",
		"client_secret": "supersecret",
		"timeout":       10,
		"starting":      true,
		"debug":         true,
	}
)

func tCompareCnfs(one, exp Config, t *testing.T) {
	var res interface{}
	var test string
	test = HOST
	res = one[test]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = PORT
	res = one[PORT]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = PATH
	res = one[PATH]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}
	test = WSTIMEUP
	res = one[WSTIMEUP]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = EXECLINE
	res = one[EXECLINE]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = CLIENTKEY
	res = one[CLIENTKEY]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = CLIENTSECRET
	res = one[CLIENTSECRET]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = TIMEOUT
	res = one[TIMEOUT]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}

	test = DEBUG
	res = one[DEBUG]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}
	test = STARTING
	res = one[test]
	if res != exp[test] {
		t.Errorf(T_STRING, test, exp[test], res)
	}
}
func TestConfigYaml(t *testing.T) {
	yalmStr := bytes.NewBufferString(YAML)
	cnf := copyConf()
	err := cnf.FromYaml(yalmStr)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	tCompareCnfs(cnf, EXP, t)

}

func TestConfigGetUrl(t *testing.T) {
	cnf := copyConf()
	test := "url"
	if cnf.Url() != EXP[test] {
		t.Errorf(T_STRING, test, EXP[test], cnf.Url())
	}
}
func TestNewConfig(t *testing.T) {
	//this should crash and give the default config impl
	cnf := NewConfig()
	tCompareCnfs(cnf, copyConf(), t)

}
func TestNewConfigDefaultFile(t *testing.T) {
	folder, err := osext.ExecutableFolder()
	println(folder)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	file, err := os.Create(folder + string(os.PathSeparator) + DEFAULT_FILE)
	_, err = file.WriteString(YAML)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	err = file.Close()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	cnf := NewConfig()
	tCompareCnfs(cnf, EXP, t)
}

func TestBuildPath(t *testing.T) {
	//from a absolute path
	conf := Config{}
	conf[EXECLINE] = "/home/cosa/pipeline2"
	base := "/tmp"
	path := conf.buildPath(base)
	fmt.Printf("path %+v\n", path)
	if path != conf[EXECLINE] {
		t.Errorf("If the path is absolute no resolving against base should be done %v %v", path, conf[EXECLINE])

	}
	conf[EXECLINE] = "../cosa/pipeline2"
	path = conf.buildPath(base)
	if path != filepath.FromSlash("/tmp/../cosa/pipeline2") {
		t.Errorf("The path is not being resolved %v", path)

	}

}
