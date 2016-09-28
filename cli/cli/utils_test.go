package cli

import (
	"archive/zip"
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"testing"
)

var files = []struct {
	Name, Body string
}{
	{"readme.txt", "This archive contains some text files."},
	{"fold1/gopher.txt", "Gopher names:\nGeorge\nGeoffrey\nGonzo"},
	{"fold1/fold2/todo.txt", "Get animal handling licence.\nWrite more examples."},
}

//Creates a new zip file to test the dump function
func createZipFile(t *testing.T) []byte {
	// Create a buffer to write our archive to.
	buf := new(bytes.Buffer)

	// Create a new zip archive.
	w := zip.NewWriter(buf)

	// Add some files to the archive.
	for _, file := range files {
		f, err := w.Create(file.Name)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
		}
		_, err = f.Write([]byte(file.Body))
		if err != nil {
			t.Errorf("Unexpected error %v", err)
		}
	}

	// Make sure to check the error on Close.
	err := w.Close()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	return buf.Bytes()
}

//Test the zip dumping functionality
func TestDumpFiles(t *testing.T) {
	data := createZipFile(t)
	folder := filepath.Join(os.TempDir(), "pipeline_commands_test")
	err := os.MkdirAll(folder, 0755)
	visited := make(map[string]bool)
	for _, f := range files {
		visited[filepath.Clean(f.Name)] = false
	}

	defer func() {
		os.RemoveAll(folder)
	}()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	zi := NewZipInflator(folder)
	_, err = zi.Write(data)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	err = zi.Close()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	filepath.Walk(folder, func(path string, inf os.FileInfo, err error) error {
		entry, err := filepath.Rel(folder, path)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
		}
		visited[entry] = true
		return nil
	})
	for _, f := range files {
		if !visited[filepath.Clean(f.Name)] {
			t.Errorf("%v was not visited", filepath.Clean(f.Name))
		}
	}

}

//Creates a fake key file
func createKeyFile(keyFile, key string) (file *os.File, err error) {
	path := filepath.Join(os.TempDir(), keyFile)
	file, err = os.Create(path)
	if err != nil {
		return
	}
	_, err = file.Write([]byte(key))
	if err != nil {
		return
	}
	if file.Close() != nil {
		return
	}
	return
}

//Test that the key is correctly loaded
func TestLoadKey(t *testing.T) {
	backup := keyFile
	defer func() {
		keyFile = backup
	}()
	expected := "dondeestanlasllavesmatarile"
	keyFile = "fakeKey"
	file, err := createKeyFile(keyFile, expected)
	defer os.Remove(file.Name())
	key, err := loadKey()
	if err != nil {
		t.Errorf("Unexpected error loading key %v", err)
	}
	if expected != key {
		t.Errorf("The stored key doesn't correspond with the loaded key '%s'!='%s'", expected, key)
	}
}

//Test that the error is propagated if the file doesn't exist
func TestLoadKeyOpenError(t *testing.T) {
	backup := keyFile
	defer func() {
		keyFile = backup
	}()
	keyFile = "thiskeyfiledoensntexist"
	_, err := loadKey()
	if err == nil {
		t.Errorf("Expected error loading key didn't occur", err)
	}
}

//Test that check priority recognises the allowed priorities
func TestCheckPriorityOk(t *testing.T) {
	if !checkPriority("high") {
		t.Errorf("high wasn't recognised as priority")
	}
	if !checkPriority("medium") {
		t.Errorf("medium wasn't recognised as priority")
	}
	if !checkPriority("low") {
		t.Errorf("low wasn't recognised as priority")
	}
}

//Test that check priority discards non-allowed values
func TestCheckPriorityNotOk(t *testing.T) {
	if checkPriority("asdfasdf") {
		t.Errorf("non-recognised value passed checkPriority")
	}
}

func TestGetLastId(t *testing.T) {
	oldSep := pathSeparator
	oldHome := homePath
	homePath = func() string {
		return "home"
	}
	defer func() {
		pathSeparator = oldSep
		homePath = oldHome
	}()
	//for linux
	pathSeparator = '/'
	path := getLastIdPath("linux")
	if "home/.daisy-pipeline/dp2/lastid" != path {
		t.Errorf("Lastid path for linux is wrong %v", path)
	}

	//for windows
	os.Setenv("APPDATA", "windows")
	path = getLastIdPath("windows")
	pathSeparator = '\\'
	if path != "windows\\DAISY Pipeline 2\\lastid" {
		t.Errorf("Lastid path for windows is wrong %v", path)
	}
	//for darwin
	pathSeparator = '/'
	path = getLastIdPath("darwin")
	if "home/Library/Application Support/DAISY Pipeline 2/dp2/lastid" != path {
		t.Errorf("Lastid path for darwin is wrong %v", path)
	}
}

func TestUnknownOs(t *testing.T) {
	defer func() {
		if recover() == nil {
			t.Errorf("Expecting panic didn't happend")
		}
	}()
	getLastIdPath("myos")

}

func TestHomePath(t *testing.T) {
	if runtime.GOOS == "windows" {
		t.Skip("this would fail in windows")
	}
	res := homePath()
	if res != os.Getenv("HOME") || res == "" {
		t.Errorf("Error getting home")
	}
}

const (
	OracleJdkVersion = `java version "%s"
Java(TM) SE Runtime Environment (build 1.7.0_67-b01)
Java HotSpot(TM) Client VM (build 24.65-b04, mixed mode, sharing)`
	OpenJdkVersion = `java version "%s"
OpenJDK Runtime Environment (IcedTea 2.5.2) (7u65-2.5.2-3~14.04)
OpenJDK 64-Bit Server VM (build 24.65-b04, mixed mode)`
	OpenJdkVersionUbuntu = `openjdk version "%s"
OpenJDK Runtime Environment (build 1.8.0_45-internal-b14)
OpenJDK 64-Bit Server VM (build 25.45-b02, mixed mode)`
)

func TestParseVersion(t *testing.T) {
	v, err := parseVersion(fmt.Sprintf(OracleJdkVersion, "1.7_u12"))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if v != 1.7 {
		t.Errorf("version was expected to be 1.7")
	}
	v, err = parseVersion(fmt.Sprintf(OpenJdkVersion, "1.7_12"))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if v != 1.7 {
		t.Errorf("version was expected to be 1.7")
	}

	v, err = parseVersion(fmt.Sprintf(OracleJdkVersion, "1.6.12"))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if v != 1.6 {
		t.Errorf("version was expected to be 1.6")
	}
	v, err = parseVersion(fmt.Sprintf(OpenJdkVersion, "1.8"))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if v != 1.8 {
		t.Errorf("version was expected to be 1.8")
	}
	v, err = parseVersion(fmt.Sprintf(OpenJdkVersionUbuntu, "1.8.0_45-internal"))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if v != 1.8 {
		t.Errorf("version was expected to be 1.8")
	}


}
func TestParseVersionErrors(t *testing.T) {
	//no java out
	_, err := parseVersion("")
	if err == nil {
		t.Errorf("Expected error not returned for empty string")
	}
	_, err = parseVersion("this is not a version line")
	if err == nil {
		t.Errorf("Expected error not returned for nonsense")
	}
	_, err = parseVersion(fmt.Sprintf(OpenJdkVersion, "arg!"))
	if err == nil {
		t.Errorf("Expected error not returned for an unparseable version")
	}

}

func TestAssertJava(t *testing.T) {
	back := javaVersionService
	defer func() {
		javaVersionService = back
	}()
	javaVersionService = func() (string, error) {
		return fmt.Sprintf(OracleJdkVersion, "1.7_u89"), nil
	}
	if err := AssertJava(1.7); err != nil {
		t.Errorf("Unexpected error %v", err.Error())
	}
	javaVersionService = func() (string, error) {
		return fmt.Sprintf(OracleJdkVersion, "1.6_u89"), nil
	}
	if err := AssertJava(1.7); err == nil {
		t.Errorf("Expected error not returned")
	}
	javaVersionService = func() (string, error) {
		return "", fmt.Errorf("error!")
	}
	if err := AssertJava(1.7); err == nil {
		t.Errorf("Expected error not returned")
	}
	javaVersionService = func() (string, error) {
		return fmt.Sprintf(OpenJdkVersionUbuntu, "1.8.0_45-internal"), nil
	}
	if err := AssertJava(1.8); err != nil {
		t.Errorf("Unexpected error %v", err.Error())
	}

}
