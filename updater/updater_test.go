package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	. "github.com/smartystreets/goconvey/convey"
)

const XmlTest = `
<releaseDescriptor href="http://daisy.org/pipeline-release/latest" version="1.0.0">
	<artifact href="http://daisy.org/pipeline-release/artifacts/artifact_1/1.0.0" version="1.0.0" id="artifact_1" deployPath="libs/pipeline/artifact_1"/>
	<artifact href="http://daisy.org/pipeline-release/artifacts/artifact_2/2.0.0" version="2.0.0" id="artifact_2" deployPath="libs/pipeline/artifact_2"/>
</releaseDescriptor>
`

func TestLoadRemote(t *testing.T) {
	Convey("Test getting remote descriptors", t, func() {
		Convey("Start a working remote server", func() {
			ver := ""
			ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				ver = strings.Trim(r.URL.Path, "/")
				fmt.Fprintf(w, "%v", XmlTest)
			}))
			Convey("And try to get the descriptor", func() {
				ver = ""
				rd, err := LoadRemote(ts.URL, Latest)
				So(err, ShouldBeNil)
				Convey("Check the remote rd", func() {
					So(rd.Version.String(), ShouldEqual, "1.0.0")
					Convey("And the ver sent to the server", func() {
						So(ver, ShouldEqual, Latest)
					})

				})

			})

		})
		Convey("Errors are propagated", func() {
			_, err := LoadRemote("http://nonexistinghost.com/", Latest)
			So(err, ShouldNotBeNil)

		})

	})
}
func TestLoadLocal(t *testing.T) {
	Convey("Test loading the local desciptor", t, func() {
		Convey("If no local descriptor is indicated return an empty descriptor", func() {
			rd, err := LoadLocal("", false)
			So(err, ShouldBeNil)
			So(rd.Version.String(), ShouldEqual, "0.0.0")
		})
		Convey("If forced update return empty descriptor", func() {
			rd, err := LoadLocal("", false)
			So(err, ShouldBeNil)
			So(rd.Version.String(), ShouldEqual, "0.0.0")
		})
		Convey("Errors are propagated", func() {
			_, err := LoadLocal("lkjaslkjaslkjasdl", false)
			So(err, ShouldNotBeNil)
		})
		Convey("it loads the release descriptor from a file", func() {
			ft, err := ioutil.TempFile("", "rd-test")
			So(err, ShouldBeNil)
			ft.WriteString(XmlTest)
			err = ft.Close()
			So(err, ShouldBeNil)
			rd, err := LoadLocal(ft.Name(), false)
			So(err, ShouldBeNil)
			Convey("Check the remote rd", func() {
				So(rd.Version.String(), ShouldEqual, "1.0.0")
			})

		})

	})
}
