package main

import (
	"bytes"
	"encoding/xml"
	"testing"

	. "github.com/smartystreets/goconvey/convey"
)

const XmlTestData = `
<releaseDescriptor href="http://daisy.org/pipeline-release/latest" version="1.0.0">
	<artifact href="http://daisy.org/pipeline-release/artifacts/artifact_1/1.0.0" version="1.0.0" id="artifact_1" deployPath="libs/pipeline/artifact_1"/>
	<artifact href="http://daisy.org/pipeline-release/artifacts/artifact_2/2.0.0" version="2.0.0" id="artifact_2" deployPath="libs/pipeline/artifact_2"/>
</releaseDescriptor>
`

func TestUnmarshal(t *testing.T) {
	Convey("Try to unmarshal a release descriptor", t, func() {
		buf := bytes.NewBufferString(XmlTestData)
		r := NewEmptyReleaseDescriptor()
		err := xml.NewDecoder(buf).Decode(&r)
		So(err, ShouldBeNil)
		Convey("Check that the struct has been populated", func() {
			So(r.Href, ShouldEqual, "http://daisy.org/pipeline-release/latest")
			So(r.Version.String(), ShouldEqual, "1.0.0")
			Convey("Check that the artifacts have been inserted in the map", func() {
				Convey("artifact_1", func() {
					art, ok := r.Artifacts["artifact_1"]
					So(ok, ShouldBeTrue)
					So(art.Href, ShouldEqual, "http://daisy.org/pipeline-release/artifacts/artifact_1/1.0.0")
					So(art.Version, ShouldEqual, "1.0.0")
					So(art.Id, ShouldEqual, "artifact_1")
					So(art.DeployPath, ShouldEqual, "libs/pipeline/artifact_1")

				})
				Convey("artifact_2", func() {
					art, ok := r.Artifacts["artifact_2"]
					So(ok, ShouldBeTrue)
					So(art.Href, ShouldEqual, "http://daisy.org/pipeline-release/artifacts/artifact_2/2.0.0")
					So(art.Version, ShouldEqual, "2.0.0")
					So(art.Id, ShouldEqual, "artifact_2")
					So(art.DeployPath, ShouldEqual, "libs/pipeline/artifact_2")

				})

			})

		})

	})
}
