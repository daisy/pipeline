package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"

	"github.com/blang/semver"
	. "github.com/smartystreets/goconvey/convey"
)

func GenerateArtifacts() []Artifact {
	return []Artifact{
		Artifact{
			Id:         "1",
			Href:       "localhost/art",
			Version:    "1.0.0",
			DeployPath: "data/arts",
		},
		Artifact{
			Id:         "2",
			Href:       "localhost/art2",
			Version:    "1.0.0",
			DeployPath: "data/arts",
		},
	}
}
func TestNewReleaseDescriptor(t *testing.T) {
	Convey("So we create a new descriptor", t, func() {
		href := "localhost"
		version := "1.0.1-beta2+SNAPSHOT-a1b2c3"
		artifacts := GenerateArtifacts()
		rd, err := NewReleaseDescriptor(href, version, artifacts...)
		So(err, ShouldBeNil)
		Convey("The href is correctly set", func() {
			So(rd.Href, ShouldEqual, href)
		})
		Convey("The version is set and parsed", func() {
			So(rd.Version.Major, ShouldEqual, 1)
			So(rd.Version.Minor, ShouldEqual, 0)
			So(rd.Version.Patch, ShouldEqual, 1)
			So(rd.Version.Pre[0].String(), ShouldEqual, "beta2")
			So(rd.Version.Build[0], ShouldEqual, "SNAPSHOT-a1b2c3")
			So(rd.Version.String(), ShouldEqual, "1.0.1-beta2+SNAPSHOT-a1b2c3")
			So(rd.Version.String(), ShouldEqual, semver.MustParse(version).String())
		})
		Convey("The artifacts are set", func() {
			So(rd.Artifacts["1"], ShouldResemble, artifacts[0])
			So(rd.Artifacts["2"], ShouldResemble, artifacts[1])
		})
		Convey("An error is returned if the version can't be parsed", func() {
			_, err := NewReleaseDescriptor(href, "notavalidaversion1=2", artifacts...)
			So(err, ShouldNotBeNil)
		})

	})
}

func TestCompareReleaseDescriptors(t *testing.T) {
	Convey("Having a descriptor", t, func() {
		href := "localhost"
		version := "1.0.1"
		rd, err := NewReleaseDescriptor(href, version, GenerateArtifacts()...)
		Convey("Error should be nil", func() {
			So(err, ShouldBeNil)
		})
		Convey("If we compare it with the same descriptor", func() {
			is, diffs := rd.IsDiff(rd)
			Convey("It shouldn't be different", func() {
				So(is, ShouldBeFalse)
			})
			Convey("And the differences are empty", func() {
				So(diffs, ShouldBeEmpty)
			})

		})
		Convey("If we have a second descriptor with other version and a newer artifact", func() {
			artifacts := GenerateArtifacts()
			artifacts[0].Version = "2.0.0"
			newer, err := NewReleaseDescriptor(href, "1.0.2", artifacts...)

			Convey("Error should be nil", func() {
				So(err, ShouldBeNil)
			})
			Convey("and we calculate the difference", func() {
				is, diffs := newer.IsDiff(rd)
				Convey("they are different", func() {
					So(is, ShouldBeTrue)
				})
				Convey("We get a difference", func() {
					So(len(diffs), ShouldEqual, 1)
					Convey("The different is the artifact with id 1", func() {
						diff := diffs[0]
						So(diff.New.Id, ShouldEqual, "1")
						Convey("The diff contains the two versions", func() {

							diffNew := artifacts[0]
							diffOld := GenerateArtifacts()[0]
							So(*diff.New, ShouldResemble, diffNew)
							So(*diff.Old, ShouldResemble, diffOld)

						})

					})
				})

			})

		})
		Convey("If we have a second descriptor with other version and a new artifact", func() {
			artifacts := GenerateArtifacts()
			artifacts = append(artifacts, Artifact{Version: "0.0.1", Id: "3"})
			newer, err := NewReleaseDescriptor(href, "1.0.2", artifacts...)

			Convey("Error should be nil", func() {
				So(err, ShouldBeNil)
			})
			Convey("and we calculate the difference", func() {
				is, diffs := newer.IsDiff(rd)
				Convey("they are different", func() {
					So(is, ShouldBeTrue)
				})
				Convey("We get a difference", func() {
					So(len(diffs), ShouldEqual, 1)
					Convey("The different is that the artifact 3 is missing in the old release", func() {
						diff := diffs[0]
						So(diff.Old, ShouldBeNil)
						So(diff.New.Id, ShouldEqual, "3")

					})
				})

			})

		})
		Convey("If we have a second descriptor with other version and one of the artifacts has disappeared", func() {
			artifacts := GenerateArtifacts()[1:]
			newer, err := NewReleaseDescriptor(href, "1.0.2", artifacts...)

			Convey("Error should be nil", func() {
				So(err, ShouldBeNil)
			})
			Convey("and we calculate the difference", func() {
				is, diffs := newer.IsDiff(rd)
				Convey("they are different", func() {
					So(is, ShouldBeTrue)
				})
				Convey("We get a difference", func() {
					So(len(diffs), ShouldEqual, 1)
					Convey("The different is that the artifact 1 is missing in the new release", func() {
						diff := diffs[0]
						So(diff.New, ShouldBeNil)
						So(diff.Old.Id, ShouldEqual, "1")

					})
				})

			})

		})

	})

}
func TestIntegrationUpdate(t *testing.T) {
	//fake server for artifacts
	ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "ARTIFACT CONTENT")
	}))
	//init a couple of release descriptors
	newArtifacts := []Artifact{
		Artifact{
			Id:         "1",
			Href:       ts.URL,
			DeployPath: "dir1/art-1.0.1",
			Version:    "1.0.1",
		},
		Artifact{
			Id:         "3",
			Href:       ts.URL,
			DeployPath: "dir3/art-1.0.0",
			Version:    "1.0.1",
		},
	}
	oldArtifacts := []Artifact{
		Artifact{
			Id:         "1",
			Href:       ts.URL,
			DeployPath: "dir1/art-1.0.0",
			Version:    "1.0.0",
		},
		Artifact{
			Id:         "2",
			Href:       ts.URL,
			DeployPath: "dir2/art-1.0.0",
			Version:    "1.0.0",
		},
	}
	installDir, err := ioutil.TempDir("", "install")
	if err != nil {
		t.Errorf("Unexpected error %v", err)
		return
	}
	for _, o := range oldArtifacts {
		path := filepath.Join(installDir, o.DeployPath)
		os.MkdirAll(filepath.Dir(path), 0755)
		f, err := os.Create(path)
		if err != nil {
			t.Errorf("Unexpected error %v", err)
		}
		f.WriteString("content")
		f.Close()
	}

	remote, err := NewReleaseDescriptor("http://daisy.org", "1.0.1", newArtifacts...)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	local, err := NewReleaseDescriptor("http://daisy.org", "1.0.0", oldArtifacts...)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	Convey("This is more of an integration test", t, func() {
		Convey("We have set a fake installation environment and an artifact server", func() {
			Convey("Check existing files", func() {
				info, err := os.Stat(filepath.Join(installDir, oldArtifacts[0].DeployPath))
				So(err, ShouldBeNil)
				So(info.Size(), ShouldNotEqual, 0)
				info, err = os.Stat(filepath.Join(installDir, oldArtifacts[1].DeployPath))
				So(err, ShouldBeNil)
				So(info.Size(), ShouldNotEqual, 0)
				Convey("Call update", func() {

					err := remote.UpdateFrom(local, installDir)
					So(err, ShouldBeNil)
					Convey("Check the installation structure", func() {
						Convey("New intalled artifacts", func() {
							info, err := os.Stat(filepath.Join(installDir, newArtifacts[0].DeployPath))
							So(err, ShouldBeNil)
							So(info.Size(), ShouldNotEqual, 0)
							info, err = os.Stat(filepath.Join(installDir, newArtifacts[1].DeployPath))
							So(err, ShouldBeNil)
							So(info.Size(), ShouldNotEqual, 0)
							Convey("Old artifacts are gone", func() {
								_, err := os.Stat(filepath.Join(installDir, oldArtifacts[0].DeployPath))
								So(err, ShouldNotBeNil)
								_, err = os.Stat(filepath.Join(installDir, oldArtifacts[1].DeployPath))
								So(err, ShouldNotBeNil)

							})

						})
					})

				})

			})

		})

	})
}
