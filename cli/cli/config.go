package cli

import (
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"slices"
	"strings"

	"github.com/kardianos/osext"
	"launchpad.net/goyaml"
)

//Yaml file keys
const (
	HOST         = "host"
	PORT         = "port"
	PATH         = "ws_path"
	APPPATH      = "app_path"
	EXECLINE     = "exec_line" // hidden feature
	CLIENTKEY    = "client_key"
	CLIENTSECRET = "client_secret"
	TIMEOUT      = "timeout"
	DEBUG        = "debug"
	STARTING     = "starting"
	CONFPATH     = "conf_path"
)

//Other convinience constants
const (
	ERR_STR      = "Error parsing configuration: %v"
	DEFAULT_FILE = "config.yml"
)

var WIN_EXT = []string{".exe", ".bat", ".cmd", ".ps1"}

//Config is just a map
type Config map[string]interface{}

//Default minimal configuration
var config = Config{

	HOST:         "http://localhost",
	PORT:         8181,
	PATH:         "ws",
	APPPATH:      "",
	EXECLINE:     "",
	CLIENTKEY:    "",
	CLIENTSECRET: "",
	TIMEOUT:      10,
	DEBUG:        false,
	STARTING:     false,
	CONFPATH:     DEFAULT_FILE, // path to the config file, for path resolution (not exposed through config_descriptions)
}

//Config items descriptions
var config_descriptions = map[string]string{

	HOST:         "Host part of webservice address. Leave empty if you wish to use the app_path setting",
	PORT:         "Port part of webservice address. Leave empty if you wish to use the app_path setting",
	PATH:         "Path part of webservice address, as in HOST:PORT/PATH (e.g. http://localhost:8181/ws). Leave empty if you wish to use the app_path setting",
	APPPATH:      "Path to the DAISY Pipeline app. If left empty, the PATH is searched for a \"DAISY Pipeline\" executable",
	CLIENTKEY:    "Client key for authenticated requests",
	CLIENTSECRET: "Client secret for authenticated requests",
	TIMEOUT:      "Timeout for requests to the webservice in seconds",
	DEBUG:        "Print debug messages",
	STARTING:     "Start the DAISY Pipeline app if it is not running",
}


//Makes a copy of the default config
func copyConf() Config {
	ret := make(Config)
	for k, v := range config {
		ret[k] = v
	}
	return ret
}

//Tries to load the default configuration file ( folder where the executable is located / config.yml) if not possible
//returns a minimal configuration setup
func NewConfig() Config {
	cnf := copyConf()
	if err := loadDefault(cnf); err != nil {
		cnf = copyConf()
		// Only warn about missing configurationg file when debugging is enabled.
		// Because debugging is disabled by default, we assume that if a --debug
		// argument is found in the command line, it is there for enabling it.
		if cnf[DEBUG].(bool) || slices.Contains(os.Args[1:], "--debug") {
			fmt.Println("Warning : no default configuration file found")
			fmt.Println(err.Error())
		}
	}
	cnf.UpdateDebug()
	return cnf
}

//Loads the default configuration file
func loadDefault(cnf Config) error {
	// first check if a config file is present in the current working directory
	cwd, err := os.Getwd()
	if err == nil {
		file, err := os.Open(filepath.Join(cwd, DEFAULT_FILE))
		if err == nil {
			cnf[CONFPATH] = filepath.Join(cwd, DEFAULT_FILE)
			err = cnf.FromYaml(file)
			defer file.Close()
			if err == nil {
				return nil
			}
		}
	}
	// no config file found in folder, check next to the dp2 executable
	// check if the current executable is a symlink, and if so, get the real path
	execPath, err := osext.Executable()
	if err != nil {
		return err
	}
	if info, err := os.Lstat(execPath); err == nil && (info.Mode()&os.ModeSymlink != 0) {
		if resolvedPath, err := os.Readlink(execPath); err == nil {
			execPath = resolvedPath
		} else {
			return err
		}
	}

	folder := filepath.Dir(execPath)
	file, err := os.Open(filepath.Join(folder, DEFAULT_FILE))
	if err != nil {
		return err
	}
	defer file.Close()
	cnf[CONFPATH] = filepath.Join(folder, DEFAULT_FILE)
	err = cnf.FromYaml(file)
	if err != nil {
		return err
	}
	return nil
}

//Loads the contents of the yaml file into the configuration
func (c Config) FromYaml(r io.Reader) error {
	// only use the default webservice values if app_path is not set or can
	// not be found (and the webservice is not configured by the user)
	c[HOST] = ""
	c[PORT] = 0
	c[PATH] = ""
	bytes, err := ioutil.ReadAll(r)
	if err != nil {
		return err
	}
	err = goyaml.Unmarshal(bytes, c)
	if err != nil {
		return err
	}
	c.UpdateDebug()
	// Check app path validity
	if (c[APPPATH].(string) != "" ) {
		apppath := c.AppPath()
		switch runtime.GOOS {
		case "windows":
			for _, ext := range WIN_EXT {
				if _, err := os.Stat(apppath + ext); !errors.Is(err, os.ErrNotExist) {
					// found an executable with the given extension
					c[APPPATH] = apppath + ext
					break
				}
			}
			// Check if the app path exists as is
			if _, err := os.Stat(apppath); errors.Is(err, os.ErrNotExist) {
				// app does not exists, disable the app path and warn the user
				fmt.Printf("warning - provided app path was not found : %s (resolved as %s)\n", c[APPPATH].(string), apppath)
				c[APPPATH] = ""
			}
		default:
			if _, err := os.Stat(apppath); errors.Is(err, os.ErrNotExist) {
				// app does not exists, disable the app path and warn the user
				fmt.Printf("warning - provided app path was not found : %s (resolved as %s)\n", c[APPPATH].(string), apppath)
				c[APPPATH] = ""
			}
		}
		
	}
	// App path is empty or points to a non-existing path or a webservice config is defined
	// - Reset webservice default values if not defined in config file
	// - Disable starting the app
	if(c[APPPATH].(string) == "" || c[HOST].(string) != "" || c[PORT].(int) != 0 || c[PATH].(string) != "") {
		c[STARTING] = false
		if(c[HOST].(string) == "") {
			c[HOST] = config[HOST]
		}
		if(c[PORT].(int) == 0) {
			c[PORT] = config[PORT]
		}
		if(c[PATH].(string) == "") {
			c[PATH] = config[PATH]
		}
	}
	return err
}

//This method should be called if the DEBUG configuration is changed. The internal Config methods
//do this automatically
func (c Config) UpdateDebug() {
	if !c[DEBUG].(bool) {
		log.SetOutput(ioutil.Discard)
	} else {
		log.SetOutput(os.Stdout)
	}
}

// Returns the Url composed by `HOST:PORT/PATH/`
//
// For each HOST, PORT and PATH values, if not defined in the config file,
// the default values are used to build the url (http://localhost:8181/ws/).
//
// Note that the PATH is trimmed from slashes at begining or end of the string
func (c Config) Url() string {
	var path = config[PATH].(string);
	if c[PATH] != nil {
		path = strings.Trim(c[PATH].(string), "/")
	}
	if (path != "") {
		// re-add trailing slash at the end of the path
		path += "/"
	}
	var host = config[HOST].(string)
	if (c[HOST] != nil) {
		host = c[HOST].(string)
	}
	var port = config[PORT].(int)
	if (c[PORT] != nil) {
		port = c[PORT].(int)
	}
	testUrl := fmt.Sprintf("%v:%v/%v", host, port, path)
	return testUrl
}

func (c Config) AppPath() string {
	var base = ""
	err := error(nil)
	if c[CONFPATH] != nil {
		base = filepath.Dir(c[CONFPATH].(string))
	} else {
		// this will possibly not resolve symlinked executables
		base, err = osext.ExecutableFolder()
		if err != nil {
			panic("Error getting executable path")
		}
	}
	
	execpath := c[APPPATH].(string)
	// An empty app path defaults to looking for DAISY Pipeline app in
	// the PATH. Note that we choose not to use "DAISY Pipeline" as
	// the default config value, because this would result in a
	// "provided app path was not found" warning if no app is found.
	if execpath == "" {
		execpath = "DAISY Pipeline"
	}
	return buildPath(base, execpath)
}

func (c Config) ExecLine() string {
	var base = ""
	err := error(nil)
	if c[CONFPATH] != nil {
		base = filepath.Dir(c[CONFPATH].(string))
	} else {
		// this will possibly not resolve symlinked executables
		base, err = osext.ExecutableFolder()
		if err != nil {
			panic("Error getting executable path")
		}
	}
	return buildPath(base, c[EXECLINE].(string))
}

func buildPath(base string, execpath string) string {
	if (execpath == "") {
		return ""
	}
	// Check if the execpath is available the PATH
	switch runtime.GOOS {
	case "windows":
		//on windows, check for all extensions if a matching executable can be found in PATH
		for _, ext := range WIN_EXT {
			testingapp := execpath
			if len(testingapp) < len(ext) || testingapp[len(testingapp)-len(ext):] != ext {
				testingapp = testingapp + ext
			}
			if path, _err := exec.LookPath(testingapp); _err == nil {
				//app or executable found in path, return the absolute path of it
				return filepath.FromSlash(path)
			}
		}
		fallthrough
	default:
		// Check if the execpath is available the PATH
		if path, _err := exec.LookPath(execpath); _err == nil {
			//app or executable found in path, return the absolute path of it
			return filepath.FromSlash(path)
		}
	}
	p := filepath.FromSlash(execpath)
	if filepath.IsAbs(p) {
		return p
	} else {
		return filepath.Join(base, p)
	}
}
