package cli

import "github.com/daisy/pipeline-clientlib-go"

//functions that process jobs
type jobFunc func(pipeline.Job, chan string)
type jobPredicate func(pipeline.Job) bool

//applies the function to the jobs that fulfil the predicate
func parallelMap(js []pipeline.Job, fn jobFunc, pred jobPredicate) []string {

	cnt := 0
	cStr := make(chan string)

	for _, j := range js {
		if pred(j) {
			cnt++
			go fn(j, cStr)

		}
	}

	msgs := []string{}
	for i := 0; i < cnt; i++ {
		msgs = append(msgs, <-cStr)
	}
	return msgs
}

func isDone(j pipeline.Job) bool {
	return j.Status == "DONE"
}
func isError(j pipeline.Job) bool {
	return j.Status == "ERROR"
}

func or(fns ...jobPredicate) jobPredicate {
	return func(j pipeline.Job) bool {
		for _, fn := range fns {
			if fn(j) {
				return true
			}
		}
		return false
	}
}
