package cli

import (
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"testing"

	"github.com/kardianos/osext"
)

var (
	YAML = `
host: http://daisy.org
port: 9999
ws_path: ws
app_path: prog
client_key: clientid
client_secret: supersecret
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
		"app_path":      "", // value should be emptied by the loading to avoid loading non existing programs
		"exec_line":     "",
		"client_key":    "clientid",
		"client_secret": "supersecret",
		"timeout":       10,
		"starting":      false, // should be reset to false as the prog exec does not exists
		"debug":         true,
		"conf_path":     "config.yml", // default value, relative to the executable path
	}
)

func copyMap(m map[string]interface{})map[string]interface{} {
    m2 := make(map[string]interface{}, len(m))
    var id string
    for id = range m {
		m2[id] = m[id]
    }
    return m2
}

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

	test = APPPATH
	res = one[APPPATH]
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

	test = CONFPATH
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
	confpath := filepath.Join(folder, DEFAULT_FILE)
	file, err := os.Create(confpath)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	_, err = file.WriteString(YAML)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	err = file.Close()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	cnf := NewConfig()
	// When reading from a file, the confpath should point to the file path
	EXP2 := copyMap(EXP)
	EXP2[CONFPATH] = confpath
	tCompareCnfs(cnf, EXP2, t)

	// testing with a dummy executable file relative to config
	dummypath := filepath.Join(folder, "prog")
	_, err = os.Create(dummypath)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	cnf = NewConfig()
	apppath := cnf.AppPath()
	expected := filepath.Join(folder, cnf[APPPATH].(string))
	if apppath != expected {
		t.Errorf("The app path is not being resolved (result: %v | expected: %v)", apppath, expected)
	}
}


func TestBuildPath(t *testing.T) {

	//from a absolute path
	conf := Config{}
	conf[APPPATH] = "/home/cosa/pipeline2"
	base := "/tmp"
	switch runtime.GOOS {
	case "windows":
		//on windows, check by its extension if the provided runner is a script
		//or an executable, and add the .exe extension if it's not there
		//(to handle default execpath value)
		conf[APPPATH] = "C:\\Users\\cosa\\pipeline2.exe"
		base = "F:\\tmp"
	}
	
	path := buildPath(base, conf[APPPATH].(string))
	fmt.Printf("path %+v\n", path)
	if path != conf[APPPATH] {
		t.Errorf("If the path is absolute no resolving against base should be done (result: %v | tested: %v)", path, conf[APPPATH])

	}
	conf[APPPATH] = "../cosa/pipeline2"
	switch runtime.GOOS {
	case "windows":
		//on windows, check by its extension if the provided runner is a script
		//or an executable, and add the .exe extension if it's not there
		//(to handle default execpath value)
		conf[APPPATH] = "../cosa/pipeline2.exe"
	}
	path = buildPath(base, conf[APPPATH].(string))
	expected := filepath.Join(base, conf[APPPATH].(string))
	if path != expected {
		t.Errorf("The path is not being resolved (result: %v | expected: %v)", path, expected)
	}

}
