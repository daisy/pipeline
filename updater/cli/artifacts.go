package main

import (
	"archive/zip"
	"encoding/xml"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"
)

//Downloader contract
type Downloader interface {
	Download(o io.Writer) error
}

//Struct that contains the information about an artifact
type Artifact struct {
	XMLName    xml.Name `xml:"artifact"`
	Id         string   `xml:"id,attr"`         //the artifact id
	Href       string   `xml:"href,attr"`       //Artifact address
	Version    string   `xml:"version,attr"`    //version
	DeployPath string   `xml:"deployPath,attr"` //relative path where to copy the artifact file
	Extract    bool     `xml:"extract,attr"`    //tells if the artifact should be extracted
	Classifier string   `xml:"classifier,attr"` //classifier
}

//downloads the artifact from href
func (a Artifact) Download(w io.Writer) error {
	//check sanity
	if a.DeployPath == "" {
		Error("%s no deploy path", a)
		return fmt.Errorf("DeployPath not set")
	}
	if a.Href == "" {
		Error("%s no href", a)
		return fmt.Errorf("No Href not set")
	}
	Info("%s downloading from  %s", a, a.Href)
	t := time.Now()
	resp, err := http.Get(a.Href)
	if err != nil {
		Error("%s error while downloading", a)
		return err
	}
	if resp.StatusCode > 300 {
		Error("%s error while downloading", a)
		return fmt.Errorf("Server %v returned an invalid status %d", a.Href, resp.StatusCode)
	}
	defer resp.Body.Close()
	_, err = io.Copy(w, resp.Body)
	Info("%s downloaded in %v", a, time.Since(t))
	return err
}

//String for logging
func (a Artifact) String() string {
	return fmt.Sprintf("Artifact %v:%v :", a.Id, a.Version)
}

//An artifact that is present in the local fs
type LocalArtifact struct {
	Artifact
	Path string
}

//Removes this copy of the artifact
func (la LocalArtifact) Clean() error {
	Info("%s deleting", la)
	return os.Remove(la.Path)

}

func (la LocalArtifact) Unzip(path string) error {
	absolute := filepath.Join(path, la.DeployPath)
	os.MkdirAll(filepath.Dir(absolute), 0755)
	z, err := zip.OpenReader(la.Path)
	if err != nil {
		return err
	}
	for _, zEntry := range z.File {
		zC, err := zEntry.Open()
		if err != nil {
			return err
		}
		defer zC.Close()
		extrPath := filepath.Join(absolute, zEntry.Name)
		Info("extracting %s to %s", zEntry.Name, extrPath)
		os.MkdirAll(filepath.Dir(extrPath), 0755)
		f, err := os.Create(extrPath)
		if err != nil {
			return err
		}
		defer f.Close()
		err = os.Chmod(extrPath, zEntry.Mode())
		if err != nil {
			Error("%s setting file permissions", err)
		}
		_, err = io.Copy(f, zC)
		if err != nil {
			return err
		}
	}

	return nil
}

//Copies the artifact having as root directory the path
func (la LocalArtifact) Copy(path string) error {
	absolute := filepath.Join(path, la.DeployPath)
	Info("%s copying to %s", la, absolute)
	os.MkdirAll(filepath.Dir(absolute), 0755)
	out, err := os.Create(absolute)
	if err != nil {
		Error("%s could not create file %s", la, absolute)
		return err
	}
	defer out.Close()
	in, err := os.Open(la.Path)
	if err != nil {
		Error("%s could not open local file %s", la, la.Path)
		return err
	}
	defer in.Close()
	_, err = io.Copy(out, in)
	if err != nil {
		Error("%s copying file", la)
	} else {
		Info("%s copied", la)
	}

	return err
}

//convienice struct for storing download results
type downloadResult struct {
	la  LocalArtifact
	err error
}

//downloads the artifacts to the given path
func Download(path string, as ...Artifact) ([]LocalArtifact, error) {
	locals := make([]LocalArtifact, 0, len(as))
	errors := []error{}

	chanArts := make(chan downloadResult)
	chanFiles := make(chan bool, 100) // max 100 downloads at a time
	//do it async to go faster!!
	for _, artifact := range as {
		//local copy
		a := artifact
		go func(chanFiles chan bool) {
			result := downloadResult{
				la: LocalArtifact{
					Artifact: a,
				},
			}

			//create file
			path := filepath.Join(path, a.DeployPath)
			os.MkdirAll(filepath.Dir(path), 0755)
			chanFiles <- true
			f, err := os.Create(path)
			defer func(f *os.File, chanFiles chan bool) {
				f.Close()
				<-chanFiles
			}(f, chanFiles)
			if err != nil {
				result.err = err
				chanArts <- result
				return
			}
			log.Println("Downloading ", a.Id, "to", path)
			//download file
			if err := a.Download(f); err != nil {
				result.err = err
				chanArts <- result
			}
			//store the file name
			result.la.Path = f.Name()
			chanArts <- result
		}(chanFiles)

	}
	for i := 0; i < len(as); i++ {
		res := <-chanArts
		if res.err == nil {
			locals = append(locals, res.la)
		} else {
			errors = append(errors, res.err)
		}
	}
	if len(errors) != 0 {
		return []LocalArtifact{}, fmt.Errorf("Errors while downloading %v", errors)
	}
	return locals, nil
}

func Remove(las []LocalArtifact) (ok bool, errs []error) {
	fn := func(l LocalArtifact) error {
		return l.Clean()
	}
	return apply(las, fn)
}

//Deploys a local artifact, it copies the file or extracts it to the given path
//depending on whether the artifact is marked to do so
func Deploy(las []LocalArtifact, path string) (ok bool, errs []error) {
	fn := func(l LocalArtifact) error {
		if l.Extract {
			return l.Unzip(path)
		} else {
			return l.Copy(path)
		}
	}
	return apply(las, fn)
}

func apply(las []LocalArtifact, fn func(LocalArtifact) error) (ok bool, errs []error) {
	errs = []error{}
	for _, la := range las {
		if err := fn(la); err != nil {
			errs = append(errs, err)
		}
	}
	return len(errs) == 0, errs
}
