package pipeline

import (
	"crypto/hmac"
	"crypto/sha1"
	"encoding/base64"
	"encoding/xml"
	"errors"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net/url"
	"strings"
	"time"

	"github.com/capitancambio/restclient"
)

//Error messages
const (
	ERR_404     = "Resource not found %v"
	ERR_401     = "You don't have enough permissions, check your configuration"
	ERR_500     = "Server error: %v"
	ERR_DEFAULT = "Framework server error (code: %v)"
)

//Default error handler has generic treatment for errors derived from the http status
func defaultErrorHandler() func(status int, respose restclient.RequestResponse) error {
	return errorHandler(make(map[int]string))
}

//Returns an error handler adding specific treatments to different status apart from the ones defined in the default
func errorHandler(handlers map[int]string) func(status int, respose restclient.RequestResponse) error {
	return func(status int, req restclient.RequestResponse) error {
		if err, ok := handlers[status]; ok {
			return errors.New(err)
		}
		switch status {
		case 404:
			return fmt.Errorf(ERR_404, req.Url)
		case 401:
			return errors.New(ERR_401)
		case 500: //check response from the server
			if req.Error.(*Error).Description != "" {
				return fmt.Errorf(ERR_500, req.Error.(*Error).Description)
			} else {
				return fmt.Errorf(ERR_500, " from "+req.Url)
			}
		}
		return fmt.Errorf(ERR_DEFAULT, status)
	}
}

//Adds the extra info + hash needed by the server
func authenticator(cKey, cSecret string) func(*restclient.RequestResponse) {
	return func(r *restclient.RequestResponse) {
		uri := r.Url
		//timestamp = Time.now.utc.strftime('%Y-%m-%dT%H:%M:%SZ')
		timestamp := time.Now().Format("2006-01-02T15:04:05Z")
		//nonce = generate_nonce
		nonce := fmt.Sprintf("%v", rand.Int63())
		//rpadding with zeros
		nonce = strings.Repeat("0", 30-len(nonce)) + nonce

		authPart := fmt.Sprintf("authid=%v&time=%v&nonce=%v", cKey, timestamp, nonce)
		charater := "?"
		if strings.Contains(uri, "?") {
			charater = "&"
		}
		uri = uri + charater + authPart
		hasher := hmac.New(sha1.New, []byte(cSecret))
		hasher.Write([]byte(uri))
		hash := base64.StdEncoding.EncodeToString(hasher.Sum(nil))
		hashSt := url.QueryEscape(hash)
		r.Url = uri + "&sign=" + hashSt
	}
}

//Convinience interface for testing
type doer interface {
	Do(*restclient.RequestResponse) (status int, err error)
	SetDecoderSupplier(func(io.Reader) restclient.Decoder)
	SetEncoderSupplier(func(io.Writer) restclient.Encoder)
	SetContentType(string)
}

//Creates a new client setting the correct encoders
func newClient() doer {
	client := restclient.New()
	client.EncoderSupplier = func(w io.Writer) restclient.Encoder {
		return xml.NewEncoder(w)
	}
	client.DecoderSupplier = func(r io.Reader) restclient.Decoder {
		return xml.NewDecoder(r)
	}
	return client
}

//Creates a new request object for the api entry and the target struct where the response for the sever will be decoded
func (p Pipeline) newResquest(apiEntry string, targetPtr interface{}, postData interface{}, args ...interface{}) *restclient.RequestResponse {

	if entry, ok := apiEntries[apiEntry]; ok {
		url := p.BaseUrl + entry.urlPath
		if len(args) > 0 {
			url = fmt.Sprintf(url, args...)
		}
		r := &restclient.RequestResponse{
			Url:            url,
			Method:         entry.method,
			Result:         targetPtr,
			Error:          &Error{},
			ExpectedStatus: entry.okStatus,
			Data:           postData,
		}

		return r
	} else {
		panic(fmt.Sprintf("No api entry found for %v ", apiEntry))
	}
}

//Executes the request against the client
func (p Pipeline) do(req *restclient.RequestResponse, handler func(int, restclient.RequestResponse) error) (status int, err error) {
	p.authenticator(req)
	log.Printf("Request %v", req)
	status, err = p.clientMaker().Do(req)
	if err != nil {
		if err == restclient.UnexpectedStatus {
			err = handler(status, *req)
		}
		return
	}
	errStr := req.Error.(*Error).Description
	if errStr != "" {
		return status, fmt.Errorf("WS ERROR: %v", errStr)
	}
	return
}
