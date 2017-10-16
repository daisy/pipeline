package pipeline

import (
	"bytes"
	"encoding/xml"
	"fmt"
	"io/ioutil"
	"mime/multipart"
	"testing"
)

func TestRawDataDecoder(t *testing.T) {
	msg := "heyhey"
	buf := bytes.NewBufferString(msg)
	decoder := NewRawDataDecoder(buf)
	st := RawData{Data: new([]byte)}
	err := decoder.Decode(&st)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	res := string(*(st.Data))
	if msg != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "msg", msg, res)
	}

}
func TestRawDataDecoderWrongType(t *testing.T) {
	msg := "heyhey"
	buf := bytes.NewBufferString(msg)
	decoder := NewRawDataDecoder(buf)
	st := RawData{Data: new([]byte)}
	err := decoder.Decode(st)
	if err == nil {
		t.Error("Expected error not thrown")
	}

}

func TestRawDataEncoder(t *testing.T) {
	msg := []byte("heyhey")
	st := RawData{Data: &msg}
	buf := bytes.NewBufferString("")
	encoder := NewRawDataEncoder(buf)
	err := encoder.Encode(&st)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	res := string(buf.Bytes())
	if string(msg) != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "msg", string(msg), res)
	}
}

func TestRawDataEncoderErr(t *testing.T) {
	st := "heyy"
	buf := bytes.NewBufferString("")
	encoder := NewRawDataEncoder(buf)
	err := encoder.Encode(&st)
	if err == nil {
		println(err.Error())
		t.Error("Expected error not thrown")
	}
}

func TestMultipartEncodingErr(t *testing.T) {
	buf := bytes.NewBufferString("")
	err := NewMultipartEncoder(buf).Encode("hola")
	if err == nil {
		t.Error("Expected error not thrown")
	}
}

func TestMultipartEncoding(t *testing.T) {
	msg := []byte("hey yo")
	rData := RawData{Data: &msg}
	scrId := "test"
	jobReq := JobRequest{
		Script: Script{Id: scrId},
	}

	st := MultipartData{
		request: jobReq,
		data:    rData,
	}
	buf := bytes.NewBufferString("")
	enc := NewMultipartEncoder(buf)
	enc.boundary = boundary
	err := enc.Encode(&st)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	//println(buf.String())
	r := multipart.NewReader(buf, boundary)
	form, err := r.ReadForm(1024)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	fmt.Printf("%+v", form.File)
	file, err := form.File["job-data"][0].Open()
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	res, err := ioutil.ReadAll(file)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if string(msg) != string(res) {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "string(msg) ", string(msg), string(res))
	}
	buf = bytes.NewBufferString(form.Value["job-request"][0])
	resJob := JobRequest{}
	err = xml.NewDecoder(buf).Decode(&resJob)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	if scrId != resJob.Script.Id {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "script id", scrId, resJob.Script.Id)
	}
}

func TestWriterDecoder(t *testing.T) {
	msg := "heyhey"
	in := bytes.NewBufferString(msg)
	out := bytes.NewBuffer([]byte{})

	decoder := NewWriterDecoder(in)
	err := decoder.Decode(out)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	res := out.String()
	if msg != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "msg", msg, res)
	}

}
func TestWriterDecoderErr(t *testing.T) {
	msg := "heyhey"
	buf := bytes.NewBufferString(msg)
	decoder := NewWriterDecoder(buf)
	err := decoder.Decode("hola")
	if err == nil {
		t.Error("Expected error not thrown")
	}

}
