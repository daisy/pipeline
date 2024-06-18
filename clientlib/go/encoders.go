package pipeline

import (
	"encoding/xml"
	"errors"
	"io"
	"io/ioutil"
	"mime/multipart"
	"net/textproto"

	"github.com/capitancambio/restclient"
)

type MultipartData struct {
	request any
	data    RawData
}

type MultipartEncoder struct {
	writer   io.Writer
	boundary string
}

var boundary = "pipelininginthefreeworld001"

func NewMultipartEncoder(w io.Writer) *MultipartEncoder {
	return &MultipartEncoder{w, boundary}
}
func (me MultipartEncoder) Encode(v interface{}) error {
	//get the fields
	var mpData *MultipartData
	switch v.(type) {
	case *MultipartData:
		mpData = v.(*MultipartData)
	default:
		return errors.New("MultipartEncoder only admits MultipartData")
	}

	w := multipart.NewWriter(me.writer)
	if me.boundary != "" {
		w.SetBoundary(me.boundary)
	}
	//Content-Disposition: form-data; name="job-data"; filename="/home/javi/daisy/pipeline-cli/samples/dtbook/dtbook.zip"
	//Content-Transfer-Encoding: binary
	//Content-Type: application/zip
	headerData := make(textproto.MIMEHeader)
	headerData.Add("Content-Disposition", `form-data; name="job-data"; filename="pipeline-client-go-data.zip"`)
	headerData.Add("Content-Transfer-Encoding", "binary")
	headerData.Add("Content-Type", "application/zip")

	dataWriter, err := w.CreatePart(headerData)
	if err != nil {
		return err
	}
	err = NewRawDataEncoder(dataWriter).Encode(mpData.data)
	if err != nil {
		return err
	}

	headerXml := make(textproto.MIMEHeader)
	headerXml.Add("Content-Disposition", `form-data; name="job-request"`)
	headerXml.Add("Content-Type", "application/xml; charset=utf-8")
	reqWriter, err := w.CreatePart(headerXml)
	if err != nil {
		return err
	}
	//reqWriter.Close()
	err = xml.NewEncoder(reqWriter).Encode(mpData.request)
	if err != nil {
		return err
	}
	if err := w.Close(); err != nil {
		return err
	}
	return nil
}

//Raw data struct defines a simple structure to
//store bytes
type RawData struct {
	Data *[]byte //Data
}

//Sets the bytes to the raw data structure
func (r *RawData) SetBytes(b []byte) {
	r.Data = &b
}

//Gets the bytes from the RawData structure
func (r RawData) Bytes() []byte {
	return *r.Data
}

type ToBytes interface {
	Bytes() []byte
}

type FromBytes interface {
	SetBytes([]byte)
}

//RawDataDecoder allows to decode raw data into a RawData structure
type RawDataDecoder struct {
	reader io.Reader
}

//RawDataDecoder allows to decode raw data into a RawData structure
type RawDataEncoder struct {
	writer io.Writer
}

//Decodes the data into a raw data struct
func (d RawDataDecoder) Decode(v interface{}) error {
	data, err := ioutil.ReadAll(d.reader)
	if err != nil {
		return err
	}
	switch v.(type) {
	case FromBytes:
		(v.(FromBytes)).SetBytes(data)
	default:
		return errors.New("RawDataDecoder only admits FromBytes interface")
	}
	return nil
}

//Decodes the data into a raw data struct
func (d RawDataEncoder) Encode(v interface{}) error {
	var data []byte
	switch v.(type) {
	case ToBytes:
		data = (v.(ToBytes)).Bytes()
	default:
		return errors.New("RawDataDecoder only admits ToBytes interface")
	}
	_, err := d.writer.Write(data)
	if err != nil {
		return err
	}
	return nil
}

//Builds a RawDataDecoder
func NewRawDataDecoder(r io.Reader) restclient.Decoder {
	return RawDataDecoder{r}
}

//Builds a RawDataDecoder
func NewRawDataEncoder(w io.Writer) restclient.Encoder {
	return RawDataEncoder{w}
}

//Just propagate reader writer
type WriterDecoder struct {
	r io.Reader
}

//Builds a WriterDecoder
func NewWriterDecoder(r io.Reader) restclient.Decoder {
	return WriterDecoder{r: r}

}

//Decodes the data into a raw data struct
func (d WriterDecoder) Decode(v interface{}) error {

	switch v.(type) {
	case io.Writer:
		_, err := io.Copy(v.(io.Writer), d.r)
		return err
	default:
		return errors.New("Writer decoder only admits io.Writer interface")
	}
	return nil
}
