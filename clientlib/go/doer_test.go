package pipeline

import (
	"fmt"
	"testing"
)

func TestDefaultErrorHandler(t *testing.T) {
	var alive Alive
	r := Pipeline{}.newResquest(API_ALIVE, &alive, nil)
	err := defaultErrorHandler()(404, *r)

	if err.Error() != fmt.Sprintf(ERR_404, apiEntries[API_ALIVE].urlPath) {
		t.Error("Default 404 not handled")
	}

	err = defaultErrorHandler()(401, *r)
	if err.Error() != ERR_401 {
		t.Error("Default 401 not handled")
	}

	err = defaultErrorHandler()(500, *r)
	if err.Error() != fmt.Sprintf(ERR_500, " from "+apiEntries[API_ALIVE].urlPath) {
		t.Error("Default 500 not handled")
	}

	r.Error.(*Error).Description = "error"
	err = defaultErrorHandler()(500, *r)
	if err.Error() != fmt.Sprintf(ERR_500, "error") {
		t.Error("Default 500 with desc not handled")
	}
	err = defaultErrorHandler()(501, *r)
	if err.Error() != fmt.Sprintf(ERR_DEFAULT, 501) {
		t.Error("Default 500 with desc not handled")
	}
}

func TestCustomErrorHandler(t *testing.T) {
	var alive Alive
	r := Pipeline{}.newResquest(API_ALIVE, &alive, nil)
	handler := errorHandler(map[int]string{404: "couldnt find it"})
	err := handler(404, *r)
	if err.Error() != "couldnt find it" {
		t.Error("custom 404 not handled")
	}
}

func TestNewRequestUnknownEntry(t *testing.T) {
	defer func() {
		if r := recover(); r == nil {
			t.Error("Not panicked with unknown api entry")
		}
	}()
	var alive Alive
	Pipeline{}.newResquest("unknown", &alive, nil)

}

func TestNewRequestBaseUrl(t *testing.T) {
	var alive Alive
	r := Pipeline{BaseUrl: "google.com/"}.newResquest(API_ALIVE, &alive, nil)
	if r.Url != "google.com/alive" {
		t.Error("basePath not set")
	}

}

func TestNewRequestPostData(t *testing.T) {
	var alive Alive
	r := Pipeline{BaseUrl: "google.com/"}.newResquest(API_ALIVE, &alive, "data")
	if r.Data != "data" {
		t.Error("post data not set")
	}

}

//Alive
func TestDoReq(t *testing.T) {
	var alive Alive
	pipeline := createPipeline(emptyClientMock)
	r := pipeline.newResquest(API_ALIVE, &alive, nil)
	if r.Url != "base/alive" {
		t.Errorf("Alive path set to %v", r.Url)
	}

}

func TestServerError(t *testing.T) {
	cli := failingMock()
	pipeline := createPipeline(cli)
	_, err := pipeline.Alive()
	if err == nil {
		t.Errorf("Exepecte error not thrown")
	}
}
