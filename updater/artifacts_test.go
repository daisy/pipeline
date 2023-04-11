package main

import (
	"archive/zip"
	"bytes"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"strings"
	"testing"

	. "github.com/smartystreets/goconvey/convey"
)

func TestDownloadArtifact(t *testing.T) {
	Convey("Let's try to download some artifacts", t, func() {
		Convey("First we need to check the artifiact's sanity", func() {
			w := bytes.NewBuffer([]byte{})
			Convey("If no href is set we get a error", func() {
				a := Artifact{
					Id:         "1",
					Version:    "2.0.1",
					DeployPath: "dir",
				}
				So(a.Download(w), ShouldNotBeNil)
			})
			Convey("If no deploy path is set we get a error", func() {
				a := Artifact{
					Id:      "1",
					Version: "2.0.1",
					Href:    "www.google.com",
				}
				So(a.Download(w), ShouldNotBeNil)
			})

		})
		Convey("We simulate an error by connecting a nonexisting server", func() {

			Convey("And try to download the artifact data from it", func() {
				a := Artifact{
					DeployPath: "folder",
					Href:       "www.nonexisitingurlfortestingpurporses.com/artifact",
				}
				buf := bytes.NewBuffer([]byte{})
				err := a.Download(buf)
				Convey("We get an error", func() {
					So(err, ShouldNotBeNil)

				})

			})

		})
		Convey("Let's start a test server", func() {
			testMsg := "hola caracola!"

			ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				w.Header().Set("Content-Type", "text/plain")
				fmt.Fprint(w, testMsg)
			}))
			defer ts.Close()
			Convey("And download the artifact data from it", func() {
				a := Artifact{
					DeployPath: "folder",
					Href:       ts.URL,
				}
				buf := bytes.NewBuffer([]byte{})
				err := a.Download(buf)
				So(err, ShouldBeNil)
				Convey("Check that the data is ok", func() {
					So(buf.String(), ShouldEqual, testMsg)

				})

			})

		})
		Convey("We start a test server that returns a HTTP error", func() {

			ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				w.WriteHeader(404)
			}))
			defer ts.Close()
			Convey("And try to data from it", func() {
				a := Artifact{
					DeployPath: "folder",
					Href:       ts.URL,
				}
				buf := bytes.NewBuffer([]byte{})
				err := a.Download(buf)
				Convey("We get an error", func() {
					So(err, ShouldNotBeNil)
				})

			})

		})

	})
}
func TestDownload(t *testing.T) {
	Convey("Having a test server", t, func() {
		id1Hit := false
		id2Hit := false
		fail := false
		ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			if fail {
				w.WriteHeader(500)
				return
			}
			id := r.URL.Query().Get("id")
			if id == "1" {
				id1Hit = true
			}
			if id == "2" {
				id2Hit = true
			}
			w.Write([]byte(id))
		}))
		Convey("We create a couple of artifacts", func() {
			as := []Artifact{
				Artifact{
					Id:         "1",
					DeployPath: "arts/1.txt",
					Href:       ts.URL + "?id=1",
				},
				Artifact{
					Id:         "2",
					DeployPath: "arts/2.txt",
					Href:       ts.URL + "?id=2",
				},
			}

			Convey("And a temporal directory", func() {
				fail = false
				path := os.TempDir()
				Convey("And we download the local artifacts", func() {
					las, err := Download(path, as...)
					So(err, ShouldBeNil)
					Convey("Both artifacts have been hit", func() {
						So(id2Hit, ShouldBeTrue)
						So(id1Hit, ShouldBeTrue)
						Convey("We check the content of the local artifacts", func() {

							for _, la := range las {
								//check the path
								So(la.Path, ShouldEqual, filepath.Join(path, la.DeployPath))
								//the content of the file
								f, err := os.Open(la.Path)
								So(err, ShouldBeNil)
								data, err := ioutil.ReadAll(f)
								So(err, ShouldBeNil)
								//the content is the id
								So(string(data), ShouldEqual, la.Id)

							}
						})

					})

				})
				Convey("And we download the local artifacts but foricing an error", func() {
					fail = true
					_, err := Download(path, as...)
					So(err, ShouldNotBeNil)
					Convey("Both artifacts have been hit", func() {
						So(id2Hit, ShouldBeFalse)
						So(id1Hit, ShouldBeFalse)

					})

				})

			})
		})
		Convey("An artifact with a bad DeplymentPath", func() {
			as := []Artifact{
				Artifact{
					Id:         "1",
					DeployPath: "",
					Href:       ts.URL + "?id=1",
				},
			}

			Convey("Try to download it", func() {
				_, err := Download("", as...)
				Convey("Error ensues!", func() {
					So(err, ShouldNotBeNil)

				})
			})
		})
	})
}

func TestLocalArtifact(t *testing.T) {
	Convey("Having a local artifact pointing to a file", t, func() {
		f, err := ioutil.TempFile("", "pipeline-updater")
		So(err, ShouldBeNil)
		_, err = f.WriteString("HOLA")
		So(err, ShouldBeNil)
		err = f.Close()
		So(err, ShouldBeNil)
		la := LocalArtifact{
			Artifact: Artifact{
				Id:         "1",
				DeployPath: "larts/1.txt",
				Href:       "localhost.com/?id=1",
			},
			Path: f.Name(),
		}
		Convey("We Copy the file", func() {
			dir := os.TempDir()
			la.Copy(dir)
			Convey("Open the copy", func() {
				f, err := os.Open(filepath.Join(dir, la.DeployPath))
				So(err, ShouldBeNil)
				fmt.Printf("f.Name() %+v\n", f.Name())
				data, err := ioutil.ReadAll(f)
				Convey("The contents should be equal", func() {
					So(string(data), ShouldEqual, "HOLA")
					Convey("Then clean the artifact", func() {
						err := la.Clean()
						So(err, ShouldBeNil)
						Convey("The file is not there anymore", func() {
							_, err := os.Stat(la.Path)
							So(err, ShouldNotBeNil)

						})

					})

				})
			})
		})

	})
	Convey("Having a local artifact pointing to a zipfile", t, func() {
		f, err := ioutil.TempFile("", "pipeline-updater")
		So(err, ShouldBeNil)
		z := zip.NewWriter(f)
		//one entry
		zw, err := z.Create("one")
		So(err, ShouldBeNil)
		_, err = zw.Write([]byte("one"))
		So(err, ShouldBeNil)
		//second entry
		zw, err = z.Create("two")
		So(err, ShouldBeNil)
		_, err = zw.Write([]byte("two"))
		So(err, ShouldBeNil)
		err = z.Close()
		So(err, ShouldBeNil)
		err = f.Close()
		So(err, ShouldBeNil)

		la := LocalArtifact{
			Artifact: Artifact{
				Id:         "1",
				DeployPath: "zipped",
				Href:       "localhost.com/?id=1",
				Extract:    true,
			},
			Path: f.Name(),
		}
		Convey("We extract the file", func() {
			dir := os.TempDir()
			la.Unzip(dir)
			Convey("Walk the dir", func() {
				files := map[string]string{}
				err := filepath.Walk(
					filepath.Join(dir, la.DeployPath),
					func(path string, info os.FileInfo, err error) error {
						if info != nil && info.IsDir() {
							return nil
						}
						f, err := os.Open(path)
						if err != nil {
							return err
						}
						defer f.Close()
						data, err := ioutil.ReadAll(f)
						if err != nil {
							return nil
						}
						files[strings.Trim(filepath.Base(path), string(filepath.Separator))] = string(data)

						return nil
					},
				)
				So(err, ShouldBeNil)
				Convey("Check that the files are extracted", func() {
					f1, ok := files["one"]
					So(ok, ShouldBeTrue)
					So(f1, ShouldEqual, "one")
					f2, ok := files["two"]
					So(ok, ShouldBeTrue)
					So(f2, ShouldEqual, "two")

				})
			})
		})

	})
}

func TestApply(t *testing.T) {
	Convey("Test the apply function", t, func() {
		Convey("Having a bunch of local artifacts and a visting function", func() {
			hits := 0
			fn := func(LocalArtifact) error {
				hits++
				return nil
			}
			las := []LocalArtifact{
				LocalArtifact{},
				LocalArtifact{},
				LocalArtifact{},
			}
			Convey("Make sure that visit them all", func() {
				hits = 0
				ok, errs := apply(las, fn)
				So(ok, ShouldBeTrue)
				So(len(errs), ShouldEqual, 0)
				So(hits, ShouldEqual, 3)
				Convey("Let's error inside the function", func() {
					hits := 0
					fn := func(LocalArtifact) error {
						hits++
						return fmt.Errorf("Error!")
					}
					ok, errs := apply(las, fn)
					So(ok, ShouldBeFalse)
					So(len(errs), ShouldEqual, 3)
					So(hits, ShouldEqual, 3)

				})
			})

		})

	})
}
