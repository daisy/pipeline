package cli

import (
	"archive/zip"
	"bytes"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"os"
	"os/exec"
	"path/filepath"
	re "regexp"
	"runtime"
	"strconv"

	"github.com/bertfrees/go-subcommand"
)

var keyFile = "dp2key.txt"

//testing multienv is a pain
var pathSeparator = os.PathSeparator

//homepath service
var homePath = func() string {
	return os.Getenv("HOME")
}

//Checks that a string defines a priority value
func checkPriority(priority string) bool {

	return priority == "high" || priority == "medium" ||
		priority == "low"

}

//loads the halt key
func loadKey() (key string, err error) {
	//get temp dir
	path := filepath.Join(os.TempDir(), keyFile)
	file, err := os.Open(path)
	if err != nil {
		errors.New("Could not find the key file, is the webservice running in this machine?")
	}
	bytes, err := ioutil.ReadAll(file)
	if err != nil {
		return
	}
	key = string(bytes)
	return
}

//Checks if the job id is present when the command was called
func checkId(lastId bool, command string, args ...string) (id string, err error) {
	if len(args) != 1 && !lastId {
		return id, fmt.Errorf("Command %v needs a job id", command)
	}
	//got it from file
	if lastId {
		id, err = getLastId()
		return
	} else {
		//first arg otherwise
		id = args[0]
		return
	}
}

//Adds the last id switch to the command
func addLastId(cmd *subcommand.Command, lastId *bool) {
	cmd.AddSwitch("lastid", "l", "Get id from the last executed job instead of JOB_ID", func(string, string) error {
		*lastId = true
		return nil
	})
	cmd.SetArity(-1, "[JOB_ID]")
}

//Calculates the absolute path in base of cwd and creates the directory
func createAbsoluteFolder(folder string) (absPath string, err error) {
	absPath, err = filepath.Abs(folder)
	if err != nil {
		return
	}
	return absPath, mkdir(absPath)
}

//mkdir -p
func mkdir(path string) error {
	if err := os.MkdirAll(path, 0755); err != nil {
		return err
	}
	return nil
}

type ZipInflator struct {
	folder string
	buff   *bytes.Buffer
}

func NewZipInflator(folder string) *ZipInflator {
	return &ZipInflator{
		folder: folder,
		buff:   bytes.NewBuffer([]byte{}),
	}

}

//Writes the  data to a intermediate buffer
func (z *ZipInflator) Write(data []byte) (int, error) {
	return z.buff.Write(data)
}

//
func (z *ZipInflator) Close() error {
	l := int64(z.buff.Len())
	//if  no data do not try to compress it
	if l == 0 {
		return nil
	}
	reader, err := zip.NewReader(bytes.NewReader(z.buff.Bytes()), l)
	if err != nil {
		return err
	}
	// Iterate through the files in the archive,
	//and store the results
	for _, f := range reader.File {
		//Get the path of the new file
		path := filepath.Join(z.folder, filepath.Clean(f.Name))
		if err := mkdir(filepath.Dir(path)); err != nil {
			return err
		}

		rc, err := f.Open()
		if err != nil {
			return err
		}

		dest, err := os.Create(path)
		if err != nil {
			return err
		}

		if _, err = io.Copy(dest, rc); err != nil {
			return err
		}

		if err := dest.Close(); err != nil {
			return err
		}

		if err := rc.Close(); err != nil {
			return err
		}

	}
	return nil
}

func zipProcessor(file string, asZip bool) (io.WriteCloser, error) {
	if asZip {
		return os.Create(file)
	} else {
		return NewZipInflator(file), nil
	}
}

//gets the path for last id file
func getLastIdPath(currentOs string) string {
	var path string
	switch currentOs {
	case "linux":
		path = homePath() + "/.daisy-pipeline/dp2/lastid"
	case "windows":
		path = os.Getenv("APPDATA") + "\\DAISY Pipeline 2\\lastid"
	case "darwin":
		path = homePath() + "/Library/Application Support/DAISY Pipeline 2/dp2/lastid"
	default:
		panic(fmt.Sprintf("Platform not recognised %v", currentOs))
	}
	base := filepath.Dir(path)
	_, err := os.Stat(base)
	if err != nil {
		mkdir(base)
	}
	return path
}

func AssertJava(minJavaVersion int) error {
	//get the output
	output, err := javaVersionService()
	if err != nil {
		return err
	}
	//parse the output
	ver, err := parseVersion(output)
	if err != nil {
		return err
	}
	//check with the min version
	if ver < minJavaVersion {
		return fmt.Errorf("A java version " + strconv.Itoa(minJavaVersion) + " or greater is need in order to run the pipeline")
	}
	return nil
}

var javaVersionService = func() (string, error) {

	javaCmd := "java"
	//try with JAVA_HOME
	javaHome := os.Getenv("JAVA_HOME")
	//darwing stuff
	if javaHome == "" && runtime.GOOS == "darwin" {
		output, err := exec.Command("/usr/libexec/java_home").Output()
		if len(output) == 0 {
			javaHome = "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/"
		} else if err != nil {
			javaHome = string(output)
		}
	}
	if javaHome != "" {
		if _, err := os.Stat(javaHome); err == nil {
			javaCmd = filepath.Join(javaHome, "bin", javaCmd)
		}
	}
	cmd := exec.Command(javaCmd, "-version")
	output, err := cmd.CombinedOutput()
	if err != nil {
		return "", err
	}
	return string(output), nil
}

//parses the vesion from
func parseVersion(javaOut string) (ver int, err error) {
	strVer := ""
	// The first line of `java -version` contains the version string inside quotes.
	// Ignore "1." from the start of the version string. We regard version "1.6" as version 6, "1.7" as 7 etc.
	reg := re.MustCompile(`.*"(1\.)?(\d+).*?".*`)
	res := reg.FindStringSubmatch(javaOut)
	if len(res) > 0 {
		strVer = res[len(res)-1]
	} else {
		return ver, fmt.Errorf("Couldn't find version in %s", javaOut)
	}
	productVersion, err := strconv.ParseInt(strVer, 10, 64)
	return int(productVersion), err

}
