package cli

import (
	"testing"

	"github.com/daisy/pipeline-clientlib-go"
)

func TestIsDone(t *testing.T) {
	jobDone := pipeline.Job{
		Status: "SUCCESS",
	}
	jobOther := pipeline.Job{
		Status: "ERROR",
	}
	if !isDone(jobDone) {
		t.Errorf("isDone doesn't return true when job done")
	}
	if isDone(jobOther) {
		t.Errorf("isDone doesn't return true when job is not done")
	}
}
func TestIsError(t *testing.T) {
	jobOther := pipeline.Job{
		Status: "SUCCESS",
	}
	jobError := pipeline.Job{
		Status: "ERROR",
	}
	if !isError(jobError) {
		t.Errorf("isError doesn't return true when job errored")
	}
	if isError(jobOther) {
		t.Errorf("isDone doesn't return true when job didn't error")
	}

}

func TestOr(t *testing.T) {
	aye := func(pipeline.Job) bool {
		return true
	}
	nay := func(pipeline.Job) bool {
		return false
	}

	if !or(aye, aye)(pipeline.Job{}) {
		t.Error("OR of true true should be true")
	}
	if !or(aye, nay)(pipeline.Job{}) {
		t.Error("OR of true false should be false")
	}
	if or(nay, nay)(pipeline.Job{}) {
		t.Error("OR of false false should be false")
	}
}

func TestMap(t *testing.T) {
	ids := map[string]bool{}
	msgs := map[string]bool{}
	jobs := []pipeline.Job{
		pipeline.Job{
			Id:     "1",
			Status: "ERROR",
		},
		pipeline.Job{
			Id:     "2",
			Status: "SUCCESS",
		},
		pipeline.Job{
			Id:     "3",
			Status: "ERROR",
		},
	}
	fn := func(j pipeline.Job, c chan string) {
		ids[j.Id] = true
		c <- j.Id
	}

	res := parallelMap(jobs, fn, isError)
	for _, s := range res {
		msgs[s] = true

	}
	for _, id := range []string{"1", "3"} {
		if _, ok := ids[id]; !ok {
			t.Errorf("Expected id %s not found", id)
		}
		if _, ok := msgs[id]; !ok {
			t.Errorf("Expected msg  %s not found", id)
		}
	}

}
