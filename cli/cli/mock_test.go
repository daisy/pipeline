package cli

import (
	"bufio"
	"bytes"
	"errors"
	"fmt"
	"io"
	"strings"

	"github.com/daisy/pipeline-clientlib-go"
)

const (
	QUEUE_CALL         = "queue"
	MOVEUP_CALL        = "moveup"
	MOVEDOWN_CALL      = "movedown"
	LOG_CALL           = "log"
	JOB_CALL           = "job"
	DELETE_CALL        = "delete"
	HALT_CALL          = "halt"
	RESULTS_CALL       = "results"
	JOBS_CALL          = "jobs"
	DELETE_CLIENT_CALL = "delete_client"
	NEW_CLIENT_CALL    = "new_client"
	CLIENT_CALL        = "client"
	MODIFY_CLIENT_CALL = "modify_client"
	LIST_CLIENT_CALL   = "list_client"
	PROPERTIES_CALL    = "properties"
	SIZES_CALL         = "sizes"
)

//Sets the output of the cli to a bytes.Buffer
func overrideOutput(cli *Cli) *bytes.Buffer {
	buf := make([]byte, 0)
	w := bytes.NewBuffer(buf)
	cli.Output = w
	return w
}

//utility to check if the first line of the the table corresponds to the
//values passed by parameter
func checkTableLine(r io.Reader, separator string, values []string) (ok bool, line, message string) {
	reader := bufio.NewScanner(r)
	reader.Scan() //discard the header line
	reader.Scan()
	reader.Text()
	line = reader.Text()
	read := strings.Split(line, separator)
	if len(read) != len(values) {
		return false, line,
			fmt.Sprintf("Length is different got %d expected %d", len(read), len(values))
	}
	for idx, _ := range read {
		if read[idx] != values[idx] {
			return false, line,
				fmt.Sprintf("Different values  %s expected %s", read[idx], values[idx])
		}
	}
	return true, line, ""
}

//returns a map containing the key and values according to text lines separated by :, ignores lines that do not contain paired values
func checkMapLikeOutput(r io.Reader) map[string]string {
	reader := bufio.NewScanner(r)
	values := make(map[string]string)
	for reader.Scan() {
		pair := strings.Split(reader.Text(), ":")
		if len(pair) == 2 {
			values[strings.Trim(pair[0], " ")] = strings.Trim(pair[1], " ")
		} //else ignroe
	}
	return values
}

type FailingWriter struct {
}

func (f FailingWriter) Write([]byte) (int, error) {
	return 0, errors.New("writing error")
}

//Pipeline Mock
type PipelineTest struct {
	fail           bool
	count          int
	deleted        bool
	resulted       bool
	backgrounded   bool
	authentication bool
	fsallow        bool
	call           string
	val            interface{}
	failOnCall     string
	key            string
	secret         string
	withScripts    bool
	jobs           func() (pipeline.Jobs, error)
	delete         func(string) (bool, error)
}

func (p PipelineTest) mockCall() (val interface{}, err error) {

	if p.failOnCall == p.call {
		return val, errors.New("Error")
	}
	return p.val, nil
}

func (p PipelineTest) SetUrl(string) {
}
func (p *PipelineTest) SetVal(v interface{}) {
	p.val = v
}
func newPipelineTest(fail bool) *PipelineTest {
	return &PipelineTest{
		fail:           fail,
		count:          0,
		deleted:        false,
		resulted:       false,
		backgrounded:   false,
		authentication: false,
		fsallow:        true,
		call:           "",
		withScripts:    true,
	}
}

func getCall(l PipelineLink) string {
	return l.pipeline.(*PipelineTest).Call()
}
func (p PipelineTest) Call() string {
	return p.call

}

func (p *PipelineTest) SetCredentials(key, secret string) {
	p.key = key
	p.secret = secret
}

func (p *PipelineTest) Alive() (alive pipeline.Alive, err error) {
	if p.fail {
		return alive, errors.New("Error")
	}
	alive.Version = "version-test"
	alive.FsAllow = p.fsallow
	alive.Authentication = p.authentication
	return
}

func (p *PipelineTest) Scripts() (scripts pipeline.Scripts, err error) {
	if p.fail {
		return scripts, errors.New("Error")
	}
	if p.withScripts {
		return pipeline.Scripts{Href: "test", Scripts: []pipeline.Script{pipeline.Script{Id: "test"}}}, err
	} else {
		return

	}
}

func (p *PipelineTest) Script(id string) (script pipeline.Script, err error) {
	if p.fail {
		return script, errors.New("Error")
	}
	return SCRIPT, nil

}
func (p *PipelineTest) ScriptUrl(id string) string {
	return "test"
}

func (p *PipelineTest) Job(id string, msgSeq int) (job pipeline.Job, err error) {
	p.call = JOB_CALL
	_, err = p.mockCall()
	if err != nil {
		return
	}
	if p.fail {
		return job, errors.New("Error")
	}
	if p.count == 0 {
		p.count++
		return JOB_1, nil
	} else {
		p.count++
		return JOB_2, nil
	}
}

func (p *PipelineTest) JobRequest(newJob pipeline.JobRequest, data []byte) (job pipeline.Job, err error) {
	return
}

func (p *PipelineTest) DeleteJob(id string) (ok bool, err error) {
	if p.delete != nil {
		return p.delete(id)
	}
	p.deleted = true
	p.call = DELETE_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.(bool), err
	}
	return
}

func (p *PipelineTest) Results(id string, w io.Writer) error {
	p.call = RESULTS_CALL
	if p.val != nil {
		w.Write(p.val.([]byte))
	} else {
		w.Write([]byte{})
	}
	_, err := p.mockCall()
	return err
}
func (p *PipelineTest) Log(id string) (data []byte, err error) {
	p.call = LOG_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.([]byte), err
	}
	return
}
func (p *PipelineTest) Jobs() (jobs pipeline.Jobs, err error) {
	if p.jobs != nil {
		return p.jobs()
	}
	p.call = JOBS_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.(pipeline.Jobs), err
	}
	return
}

func (p *PipelineTest) Halt(key string) error {
	p.call = HALT_CALL
	_, err := p.mockCall()
	return err
}

func (p *PipelineTest) Clients() (c []pipeline.Client, err error) {
	p.call = LIST_CLIENT_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.([]pipeline.Client), err
	}
	return
}
func (p *PipelineTest) NewClient(cIn pipeline.Client) (cOut pipeline.Client, err error) {
	p.call = NEW_CLIENT_CALL
	out, err := p.mockCall()
	if err != nil {
		return
	}
	return *(out.(*pipeline.Client)), err
}
func (p *PipelineTest) DeleteClient(id string) (ok bool, err error) {
	p.call = DELETE_CLIENT_CALL
	_, err = p.mockCall()
	return true, err
}
func (p *PipelineTest) Client(id string) (client pipeline.Client, err error) {
	p.call = CLIENT_CALL
	out, err := p.mockCall()
	if err != nil {
		return
	}
	return *(out.(*pipeline.Client)), err
}
func (p *PipelineTest) ModifyClient(client pipeline.Client, id string) (c pipeline.Client, err error) {
	p.call = MODIFY_CLIENT_CALL
	_, err = p.mockCall()
	if err != nil {
		return
	}
	return client, err
}
func (p *PipelineTest) Properties() (props []pipeline.Property, err error) {
	p.call = PROPERTIES_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.([]pipeline.Property), err
	}
	return
}
func (p *PipelineTest) Sizes() (sizes pipeline.JobSizes, err error) {
	p.call = SIZES_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.(pipeline.JobSizes), err
	}
	return
}
func (p *PipelineTest) Queue() (val []pipeline.QueueJob, err error) {
	p.call = QUEUE_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.([]pipeline.QueueJob), err
	}
	return

}

func (p *PipelineTest) MoveUp(id string) (queue []pipeline.QueueJob, err error) {
	p.call = MOVEUP_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.([]pipeline.QueueJob), err
	}
	return
}
func (p *PipelineTest) MoveDown(id string) (queue []pipeline.QueueJob, err error) {
	p.call = MOVEDOWN_CALL
	ret, err := p.mockCall()
	if ret != nil {
		return ret.([]pipeline.QueueJob), err
	}
	return
}
