package pipeline

import (
	"bytes"
	"encoding/xml"
	"io"
	"reflect"

	"github.com/capitancambio/restclient"
)

//minimal Client for testing
type MockClient struct {
	status          int
	response        string
	EncoderSupplier func(io.Writer) restclient.Encoder //Supplies the endoder objects
	DecoderSupplier func(io.Reader) restclient.Decoder //Supplies the endoder objects
	fail            bool
	request         restclient.RequestResponse
}

//Sets the decoder supplier - convey the interface
func (m *MockClient) SetDecoderSupplier(fn func(io.Reader) restclient.Decoder) {
	m.DecoderSupplier = fn
}

//Sets the encoder supplier - convey the interface
func (m *MockClient) SetEncoderSupplier(fn func(io.Writer) restclient.Encoder) {
	m.EncoderSupplier = fn
}

//set the content type - convey the interface
func (m *MockClient) SetContentType(string) {
}

//Moked Do just encodes the request and decodes response and complains about the unexpected statuses
func (m *MockClient) Do(rr *restclient.RequestResponse) (status int, err error) {
	m.request = *rr
	if m.response != "" {
		err = m.DecoderSupplier(bytes.NewBufferString(m.response)).Decode(rr.Result)
	}
	if m.fail {
		err = m.DecoderSupplier(bytes.NewBufferString(errorXml)).Decode(rr.Error)
		if err != nil {
			println("THIS errorXml SHOULD NOT HAPPEN")
			panic(err.Error())
		}
	}
	if rr.ExpectedStatus != m.status {
		err = restclient.UnexpectedStatus
	}
	return m.status, err
}

//xml based mock client, the response is the xml input and the status is the simulated status from the server
func xmlClientMock(response string, status int) func() doer {

	return func() doer {
		return &MockClient{
			status:   status,
			response: response,
			EncoderSupplier: func(w io.Writer) restclient.Encoder {
				return xml.NewEncoder(w)
			},
			DecoderSupplier: func(r io.Reader) restclient.Decoder {
				return xml.NewDecoder(r)
			},
			fail: false,
		}
	}

}

//Fake encoder/decoder that always returns the inteface provided
type MockEncoderDecoder struct {
	response interface{}
}

//Creates an new MockEncoderDecoder
func NewMockEncoderDecoder(response interface{}) MockEncoderDecoder {
	return MockEncoderDecoder{response}
}

//Does nothing
func (m MockEncoderDecoder) Encode(value interface{}) error {
	return nil
}

//Hydrates the value with the provided response
func (m MockEncoderDecoder) Decode(value interface{}) error {
	if value != nil {
		v := reflect.ValueOf(value)
		v.Elem().Set(reflect.ValueOf(m.response))
	}
	return nil
}

//client that responds with the given interface and with the give status
func structClientMock(response interface{}, status int) func() doer {

	return func() doer {
		return &MockClient{
			status:   status,
			response: "FUDGED",
			EncoderSupplier: func(w io.Writer) restclient.Encoder {
				return NewMockEncoderDecoder(response)
			},
			DecoderSupplier: func(r io.Reader) restclient.Decoder {
				return NewMockEncoderDecoder(response)
			},
			fail: false,
		}
	}

}

//Mock that returns a generic error
func failingMock() func() doer {

	return func() doer {
		return &MockClient{
			status:   200,
			response: "",
			EncoderSupplier: func(w io.Writer) restclient.Encoder {
				return xml.NewEncoder(w)
			},
			DecoderSupplier: func(r io.Reader) restclient.Decoder {
				return xml.NewDecoder(r)
			},
			fail: true,
		}
	}

}

// Empty client with no response
func emptyClientMock() doer {
	return &MockClient{status: 200, response: ""}
}

//Creates a pipeline using the given mocked doer
func createPipeline(maker func() doer) Pipeline {
	return Pipeline{BaseUrl: "base/", clientMaker: maker, authenticator: func(*restclient.RequestResponse) {}}
}
