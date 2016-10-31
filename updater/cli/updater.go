package main

import (
	"encoding/xml"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"github.com/kardianos/osext"
)

const (
	Latest = "current"
)

var (
	env = os.Getenv("DP2_HOME")
)

var service = flag.String("service", "https://daisy.github.io/pipeline/updates", "URL of the update service")
var version = flag.String("version", Latest, "Version to update to")
var installDir = flag.String("install-dir", env, "Pipeline install directory")
var localDescriptor = flag.String("descriptor", "", "Current descriptor")
var force = flag.Bool("force", false, "Forces to update without comparing the versions, use if updating to nightly builds")

func main() {
	flag.Parse()
	exePath, err := osext.Executable()
	if err != nil {
		Error(err.Error())
		os.Exit(-1)
	}
	logfile, err := os.Create(filepath.Join(filepath.Dir(exePath), "log.txt"))
	if err != nil {
		Error(err.Error())
		os.Exit(-1)
	}
	log.SetOutput(logfile)

	remote, err := LoadRemote(*service, *version)
	if err != nil {
		Error(err.Error())
		log.Println(err)
		os.Exit(-1)
	}
	local, err := LoadLocal(*localDescriptor, *force)
	if err != nil {
		Error(err.Error())
		log.Println(err)
		os.Exit(-1)
	}
	err = remote.UpdateFrom(local, *installDir)
	if err != nil {
		Error(err.Error())
		log.Println(err)
		os.Exit(-1)
	}
	if err := Backup(*localDescriptor); err != nil {
		Error(err.Error())
		log.Println(err)
		os.Exit(-1)
	}

	if err := remote.Save(*localDescriptor); err != nil {
		Error(err.Error())
		log.Println(err)
		os.Exit(-1)
	}
}
func LoadRemote(service, version string) (rd ReleaseDescriptor, err error) {
	rd = NewEmptyReleaseDescriptor()
	resp, err := http.Get(fmt.Sprintf("%s/%s", service, version))
	if err != nil {
		return
	}
	if resp.StatusCode > 300 {
		return rd, fmt.Errorf("Invalid status %v", resp.Status)
	}
	err = xml.NewDecoder(resp.Body).Decode(&rd)
	return

}
func LoadLocal(path string, force bool) (rd ReleaseDescriptor, err error) {
	rd = NewEmptyReleaseDescriptor()
	if force || path == "" {
		return
	}
	f, err := os.Open(path)
	if err != nil {
		return
	}
	defer f.Close()
	err = xml.NewDecoder(f).Decode(&rd)
	return

}

func Backup(path string) error {
	return os.Rename(path, fmt.Sprintf("%s_%s", path, time.Now().Format("200601021504050.000")))
}
