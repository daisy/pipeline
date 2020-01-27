package cli

import (
	"errors"
	"fmt"
	"io"
	"log"
	"os"
	"time"
	"regexp"

	"github.com/daisy/pipeline-clientlib-go"
)

const (
	MSG_WAIT = 200 * time.Millisecond //waiting time for getting messages
)

//Convinience for testing, propably move to pipeline-clientlib-go
type PipelineApi interface {
	SetCredentials(string, string)
	SetUrl(string)
	Alive() (alive pipeline.Alive, err error)
	Scripts() (scripts pipeline.Scripts, err error)
	Script(id string) (script pipeline.Script, err error)
	JobRequest(newJob pipeline.JobRequest, data []byte) (job pipeline.Job, err error)
	ScriptUrl(id string) string
	Job(string, int) (pipeline.Job, error)
	DeleteJob(id string) (bool, error)
	Results(id string, w io.Writer) (bool, error)
	Log(id string) ([]byte, error)
	Jobs() (pipeline.Jobs, error)
	Halt(key string) error
	Clients() (clients []pipeline.Client, err error)
	NewClient(in pipeline.Client) (out pipeline.Client, err error)
	ModifyClient(in pipeline.Client, id string) (out pipeline.Client, err error)
	DeleteClient(id string) (ok bool, err error)
	Client(id string) (out pipeline.Client, err error)
	Properties() (props []pipeline.Property, err error)
	Sizes() (sizes pipeline.JobSizes, err error)
	Queue() ([]pipeline.QueueJob, error)
	MoveUp(id string) ([]pipeline.QueueJob, error)
	MoveDown(id string) ([]pipeline.QueueJob, error)
}

//Maintains some information about the pipeline client
type PipelineLink struct {
	pipeline       PipelineApi //Allows access to the pipeline fwk
	config         Config
	Version        string //Framework version
	Authentication bool   //Framework authentication
	FsAllow        bool   //Framework mode
}

func NewLink(conf Config) (pLink *PipelineLink) {

	pLink = &PipelineLink{
		pipeline: pipeline.NewPipeline(conf.Url()),
		config:   conf,
	}
	//assure that the pipeline is up

	return
}

func (p *PipelineLink) Init() error {
	log.Println("Initialising link")
	p.pipeline.SetUrl(p.config.Url())
	if err := bringUp(p); err != nil {
		return err
	}
	//set the credentials
	if p.Authentication {
		if !(len(p.config[CLIENTKEY].(string)) > 0 && len(p.config[CLIENTSECRET].(string)) > 0) {
			return errors.New("link: Authentication required but client_key and client_secret are not set. Please, check the configuration")
		}
		p.pipeline.SetCredentials(p.config[CLIENTKEY].(string), p.config[CLIENTSECRET].(string))
	}
	return nil
}
func (p PipelineLink) IsLocal() bool {
	return p.FsAllow
}

//checks if the pipeline is up
//otherwise it brings it up and fills the
//link object
func bringUp(pLink *PipelineLink) error {
	alive, err := pLink.pipeline.Alive()
	if err != nil {
		if pLink.config[STARTING].(bool) {
			alive, err = NewPipelineLauncher(pLink.pipeline,
				pLink.config.ExecPath(), pLink.config[TIMEOUT].(int)).Launch(os.Stdout)
			if err != nil {
				return fmt.Errorf("Error bringing the pipeline2 up %v", err.Error())
			}
		} else {
			return fmt.Errorf("Could not connect to the webservice and I'm not configured to start one\n\tError: %v", err.Error())
		}
	}
	log.Println("Setting values")
	pLink.Version = alive.Version
	pLink.FsAllow = alive.FsAllow
	pLink.Authentication = alive.Authentication
	return nil
}

//ScriptList returns the list of scripts available in the framework
func (p PipelineLink) Scripts() (scripts []pipeline.Script, err error) {
	scriptsStruct, err := p.pipeline.Scripts()
	if err != nil {
		return
	}
	scripts = make([]pipeline.Script, len(scriptsStruct.Scripts))
	//fill the script list with the complete definition
	for idx, script := range scriptsStruct.Scripts {
		scripts[idx], err = p.pipeline.Script(script.Id)
		if err != nil {
			err = fmt.Errorf("Error loading script %v: %v", script.Id, err)
			return nil, err
		}
	}
	return scripts, err
}

//Gets the job identified by the jobId
func (p PipelineLink) Job(jobId string) (job pipeline.Job, err error) {
	job, err = p.pipeline.Job(jobId, 0)
	return
}

//Deletes the given job
func (p PipelineLink) Delete(jobId string) (ok bool, err error) {
	ok, err = p.pipeline.DeleteJob(jobId)
	return
}

//Return the zipped results as a []byte
func (p PipelineLink) Results(jobId string, w io.Writer) (ok bool, err error) {
	return p.pipeline.Results(jobId, w)
}
func (p PipelineLink) Log(jobId string) (data []byte, err error) {
	data, err = p.pipeline.Log(jobId)
	return
}
func (p PipelineLink) Jobs() (jobs []pipeline.Job, err error) {
	pJobs, err := p.pipeline.Jobs()
	if err != nil {
		return
	}
	jobs = pJobs.Jobs
	return
}

//Admin
func (p PipelineLink) Halt(key string) error {
	return p.pipeline.Halt(key)
}

func (p PipelineLink) Clients() (clients []pipeline.Client, err error) {
	return p.pipeline.Clients()
}

func (p PipelineLink) NewClient(newClient pipeline.Client) (client pipeline.Client, err error) {
	return p.pipeline.NewClient(newClient)
}
func (p PipelineLink) DeleteClient(id string) (ok bool, err error) {
	return p.pipeline.DeleteClient(id)
}
func (p PipelineLink) Client(id string) (out pipeline.Client, err error) {
	return p.pipeline.Client(id)
}

func (p PipelineLink) ModifyClient(data pipeline.Client, id string) (client pipeline.Client, err error) {
	return p.pipeline.ModifyClient(data, id)
}
func (p PipelineLink) Properties() (props []pipeline.Property, err error) {
	return p.pipeline.Properties()
}
func (p PipelineLink) Sizes() (sizes pipeline.JobSizes, err error) {
	return p.pipeline.Sizes()
}

func (p PipelineLink) Queue() (queue []pipeline.QueueJob, err error) {
	return p.pipeline.Queue()
}
func (p PipelineLink) MoveUp(id string) (queue []pipeline.QueueJob, err error) {
	return p.pipeline.MoveUp(id)
}
func (p PipelineLink) MoveDown(id string) (queue []pipeline.QueueJob, err error) {
	return p.pipeline.MoveDown(id)
}

//Convience structure to handle message and errors from the communication with the pipelineApi
type Message struct {
	Message  string
	Level    string
	Depth    int
	Status   string
	Progress float64
	Error    error
}

//Returns a simple string representation of the messages strucutre:
//[LEVEL]   Message content
func (m Message) String() string {
	if m.Message != "" {
		indent := ""
		for i := 1; i <= m.Depth; i++ {
			indent += "  "
		}
		level := "[" + m.Level + "]"
		for len(level) < 10 {
			level += " "
		}
		str := ""
		for i, line := range regexp.MustCompile("\r?\n|\r").Split(m.Message, -1) {
			if (i == 0) {
				str += fmt.Sprintf("%v %v%v", level, indent, line)
			} else {
				str += fmt.Sprintf("\n           %v%v", indent, line)
			}
		}
		return str
	} else {
		return ""
	}
}

//Executes the job request and returns a channel fed with the job's messages,errors, and status.
//The last message will have no contents but the status of the in which the job finished
func (p PipelineLink) Execute(jobReq JobRequest) (job pipeline.Job, messages chan Message, err error) {
	req, err := jobRequestToPipeline(jobReq, p)
	if err != nil {
		return
	}
	log.Printf("data len exec %v", len(jobReq.Data))
	job, err = p.pipeline.JobRequest(req, jobReq.Data)
	if err != nil {
		return
	}
	messages = make(chan Message)
	if !jobReq.Background {
		go getAsyncMessages(p, job.Id, messages)
	} else {
		close(messages)
	}
	return
}

//Feeds the channel with the messages describing the job's execution
func getAsyncMessages(p PipelineLink, jobId string, messages chan Message) {
	msgNum := -1
	for {
		job, err := p.pipeline.Job(jobId, msgNum)
		if err != nil {
			messages <- Message{Error: err}
			close(messages)
			return
		}
		n := msgNum
		if len(job.Messages.Message) > 0 {
			n = flattenMessages(job.Messages.Message, messages, job.Status, job.Messages.Progress, msgNum + 1, 0)
		}
		if (n > msgNum) {
			msgNum = n
		} else {
			messages <- Message{Progress: job.Messages.Progress}
		}
		if job.Status == "SUCCESS" || job.Status == "ERROR" || job.Status == "FAIL" {
			messages <- Message{Status: job.Status}
			close(messages)
			return
		}
		time.Sleep(MSG_WAIT)
	}

}

//Flatten message coming from the Pipeline job and feed them into the channel
//Return the sequence number of the last inner message
func flattenMessages(from []pipeline.Message, to chan Message, status string, progress float64, firstNum int, depth int) (lastNum int) {
	for _, msg := range from {
		lastNum = msg.Sequence
		if lastNum >= firstNum {
			to <- Message{Message: msg.Content, Level: msg.Level, Depth: depth, Status: status, Progress: progress}
		}
		if len(msg.Message) > 0 {
			lastNum = flattenMessages(msg.Message, to, status, progress, firstNum, depth + 1)
		}
	}
	return lastNum
}

func jobRequestToPipeline(req JobRequest, p PipelineLink) (pReq pipeline.JobRequest, err error) {
	href := p.pipeline.ScriptUrl(req.Script)
	pReq = pipeline.JobRequest{
		Script:   pipeline.Script{Href: href},
		Nicename: req.Nicename,
		Priority: req.Priority,
	}
	for name, values := range req.Inputs {
		input := pipeline.Input{Name: name}
		for _, value := range values {
			input.Items = append(input.Items, pipeline.Item{Value: value.String()})
		}
		pReq.Inputs = append(pReq.Inputs, input)
	}
	for name, values := range req.Options {
		option := pipeline.Option{Name: name}
		if len(values) > 1 {
			for _, value := range values {
				option.Items = append(option.Items, pipeline.Item{Value: value})
			}
		} else {
			option.Value = values[0]
		}
		pReq.Options = append(pReq.Options, option)

	}
	return
}
