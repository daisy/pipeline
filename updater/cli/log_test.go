package main

import (
	"bytes"
	"testing"

	. "github.com/smartystreets/goconvey/convey"
)

func TestLogTest(t *testing.T) {
	back := Output
	defer func() {
		Output = back
	}()
	Convey("Test different logging", t, func() {
		Convey("Test info for an artifact", func() {
			buf := bytes.NewBuffer([]byte{})
			Output = buf
			a := Artifact{
				Id:      "test",
				Version: "1.0.0",
			}
			Info("%s log", a)
			So(buf.String(), ShouldEqual, "[INFO] Artifact test:1.0.0 : log\n")

		})
		Convey("Test error for a release descriptor", func() {
			buf := bytes.NewBuffer([]byte{})
			Output = buf
			rd := NewEmptyReleaseDescriptor()
			Error("%s log", rd)
			So(buf.String(), ShouldEqual, "[ERROR] ReleaseDescriptor 0.0.0 log\n")

		})

	})
}
