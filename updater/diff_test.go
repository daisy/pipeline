package main

import (
	"path/filepath"
	"testing"

	. "github.com/smartystreets/goconvey/convey"
)

func TestDiff(t *testing.T) {
	Convey("Having different diffs", t, func() {
		Convey("If we have both new and old artifacts", func() {
			d := Diff{
				New: &Artifact{Id: "new"},
				Old: &Artifact{Id: "old"},
			}
			Convey("We need to download the new artifact", func() {
				a, ok := d.ToDownload()
				So(ok, ShouldBeTrue)
				So(a, ShouldResemble, *d.New)
				Convey("And remove the old artifact", func() {
					a, ok := d.ToRemove()
					So(ok, ShouldBeTrue)
					So(a, ShouldResemble, *d.Old)
				})

			})

		})

		Convey("If have only a new artifact", func() {
			d := Diff{
				New: &Artifact{Id: "new"},
			}
			Convey("We need to download the new artifact", func() {
				a, ok := d.ToDownload()
				So(ok, ShouldBeTrue)
				So(a, ShouldResemble, *d.New)
				Convey("And we do NOT have to delete the old one", func() {
					_, ok := d.ToRemove()
					So(ok, ShouldBeFalse)
				})

			})

		})
		Convey("If have only an old artifact", func() {
			d := Diff{
				Old: &Artifact{Id: "old"},
			}
			Convey("We need to download the new artifact", func() {
				_, ok := d.ToDownload()
				So(ok, ShouldBeFalse)
				Convey("And we do NOT have to delete the old one", func() {
					a, ok := d.ToRemove()
					So(ok, ShouldBeTrue)
					So(a, ShouldResemble, *d.Old)
				})

			})

		})
	})
}

func TestDiffTest(t *testing.T) {
	Convey("Having a diff set", t, func() {
		ds := DiffSet{
			Diff{
				New: &Artifact{Id: "new1"},
				Old: &Artifact{Id: "old1", DeployPath: "arts1"},
			},
			Diff{
				New: &Artifact{Id: "new2"},
			},
			Diff{
				Old: &Artifact{Id: "old3", DeployPath: "arts3"},
			},
		}
		Convey("Get the artifacts we need to download", func() {
			toDownload := ds.ToDownload()
			So(len(toDownload), ShouldEqual, 2)
			Convey("Check that we have all the elements that we need", func() {
				So(toDownload[0], ShouldResemble, Artifact{Id: "new1"})
				So(toDownload[1], ShouldResemble, Artifact{Id: "new2"})
			})

		})
		Convey("Get the artifacts we need to delete", func() {
			p := "rootpath"
			toDelete := ds.ToRemove(p)
			So(len(toDelete), ShouldEqual, 2)
			Convey("Check that we have all the elements that we need", func() {
				So(toDelete[0].Artifact, ShouldResemble, Artifact{Id: "old1", DeployPath: "arts1"})
				So(toDelete[1].Artifact, ShouldResemble, Artifact{Id: "old3", DeployPath: "arts3"})
				Convey("The paths should be be ready to clear", func() {
					So(toDelete[0].Path, ShouldEqual, filepath.Join(p, "arts1"))
					So(toDelete[1].Path, ShouldEqual, filepath.Join(p, "arts3"))

				})
			})

		})

	})
}
