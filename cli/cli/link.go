package cli

import (
	"errors"
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"regexp"
	"strings"
	"time"

	"github.com/daisy/pipeline-clientlib-go"
	"github.com/mitchellh/go-ps"
	"github.com/shirou/gopsutil/process"
)

const (
	MSG_WAIT = 1000 * time.Millisecond //waiting time for getting messages
)

//Convinience for testing, propably move to pipeline-clientlib-go
type PipelineApi interface {
	SetCredentials(string, string)
	SetUrl(string)
	Alive() (alive pipeline.Alive, err error)
	Scripts() (scripts pipeline.Scripts, err error)
	Script(id string) (script pipeline.Script, err error)
	JobRequest(newJob pipeline.JobRequest, data []byte) (job pipeline.Job, err error)
	StylesheetParametersRequest(newReq pipeline.StylesheetParametersRequest, data []byte) (params pipeline.StylesheetParameters, err error)
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
	var alive pipeline.Alive
	var err error
	if !((pLink.config[HOST] == nil || pLink.config[HOST].(string) == "") &&
		 (pLink.config[HOST] == nil || pLink.config[PORT].(int) == 0) &&
		 (pLink.config[PATH] == nil || pLink.config[PATH].(string) == "")) {
		// A webservice is configured to be used in loaded configuration either from
		// - the user provided config (config file or command line)
		// - The default webservice config, reinstated due to missing or incorrect app path
		alive, err = pLink.pipeline.Alive()
		if err != nil {
			if pLink.config[STARTING] != nil && pLink.config[STARTING].(bool) {
				execpath := pLink.config.ExecLine()
				if (execpath != "") {
					// the exec_line setting was kept for backward compatibility, intended
					// for use on Linux (where there is no Pipeline app available)
					alive, err = NewPipelineLauncher(
						pLink.pipeline,
						execpath,
						pLink.config[TIMEOUT].(int),
					).Launch(os.Stdout)
					if err != nil {
						return fmt.Errorf("failed to bring up the webservice: %v", err.Error())
					}
				}
			}
			if (err != nil) {
				return fmt.Errorf("could not connect to the webservice at %s\n\tError: %v",
					pLink.config.Url(),
					err.Error())
			}
		}
	} else {
		// No webservice configured from initial configuration checks
		// Check if Pipeline app is running
		processes, err := ps.Processes()
		if err == nil {
			execFound := ""
			for _, element := range processes {
				app := element.Executable()
				if strings.HasSuffix(app, "DAISY Pipeline") ||
					strings.HasSuffix(app, "DAISY Pipeline.exe") {
					processApp, err := process.NewProcess(int32(element.Pid()))
					if err == nil {
						app, err = processApp.Exe()
						if err == nil {
							execFound = app
						}
					}
				}

			}
			if execFound != "" {
				// Found the path of a running instance of the Pipeline app: reuse it with the command line tool
				// (Note: multiple instance of the app are not allowed in the electron app)
				args := os.Args[1:]
				if len(os.Args) == 1 {
					args = append(args, "help")
				}
				cmd := exec.Command(execFound, args...)
				cmd.Stdout = os.Stdout
				cmd.Stderr = os.Stderr
				if err := cmd.Run(); err != nil {
					return fmt.Errorf(
						"could not connect to the running instance of the DAISY Pipeline app: %v\r\n(path : %s)", err, execFound)
				} else {
					os.Exit(cmd.ProcessState.ExitCode())
				}
			}
		}
		// if no running app process was found, continue here
		// If the starting flag was set to true (set in config + valid path was provided)
		if pLink.config[STARTING] != nil && pLink.config[STARTING].(bool) {
			// app_path is validated at the config level in config.FromYaml()
			// config.AppPath() returns the resolved path
			execpath := pLink.config.AppPath()
			if (execpath != "") {
				// forward the command to the electron app
				args := os.Args[1:]
				if len(os.Args) == 1 {
					args = append(args, "help")
				}
				cmd := exec.Command(execpath, args...)
				cmd.Stdout = os.Stdout
				cmd.Stderr = os.Stderr
				if err := cmd.Run(); err != nil {
					return fmt.Errorf(
						"could not start the DAISY Pipeline app at %s: %v", execpath, err)
				} else {
					os.Exit(cmd.ProcessState.ExitCode())
				}
			} else {
				return fmt.Errorf(
					"could not locate the Pipeline app")
			}
		}
		// fallback to default configuration
		pLink.config = copyConf()
		pLink.pipeline.SetUrl(pLink.config.Url())
		alive, err = pLink.pipeline.Alive()
		if err != nil {
			return fmt.Errorf(
				"The DAISY Pipeline app is not running," +
				" and could not connect to the default webservice address (%s)." +
				" Pass \"--starting true\" to start the app automatically.\n\tError: %v",
				pLink.config.Url(),
				err.Error())
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

func (p PipelineLink) StylesheetParameters(paramReq StylesheetParametersRequest) (params pipeline.StylesheetParameters, err error) {
	req := pipeline.StylesheetParametersRequest{
		Media:               pipeline.Media{Value: paramReq.Medium},
		UserAgentStylesheet: pipeline.UserAgentStylesheet{Mediatype: paramReq.ContentType},
	}
	log.Printf("data len exec %v", len(paramReq.Data))
	params, err = p.pipeline.StylesheetParametersRequest(req, paramReq.Data)
	return
}

//Feeds the channel with the messages describing the job's execution
func getAsyncMessages(p PipelineLink, jobId string, messages chan Message) {
	msgSeq := -1
	for {
		job, err := p.pipeline.Job(jobId, msgSeq)
		if err != nil {
			messages <- Message{Error: err}
			close(messages)
			return
		}
		n := msgSeq
		if len(job.Messages.Message) > 0 {
			n = flattenMessages(job.Messages.Message, messages, job.Status, job.Messages.Progress, msgSeq + 1, 0)
		}
		if (n > msgSeq) {
			msgSeq = n
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
//Return the sequence number of the inner message with the highest sequence number
func flattenMessages(from []pipeline.Message, to chan Message, status string, progress float64, firstSeq int, depth int) (lastSeq int) {
	lastSeq = -1
	for _, msg := range from {
		seq := msg.Sequence
		if seq >= firstSeq {
			to <- Message{Message: msg.Content, Level: msg.Level, Depth: depth, Status: status, Progress: progress}
			if seq > lastSeq {
				lastSeq = seq
			}
		}
		if len(msg.Message) > 0 {
			seq := flattenMessages(msg.Message, to, status, progress, firstSeq, depth + 1)
			if seq > lastSeq {
				lastSeq = seq
			}
		}
	}
	return lastSeq
}

func jobRequestToPipeline(req JobRequest, p PipelineLink) (pipeline.JobRequest, error) {
	href := p.pipeline.ScriptUrl(req.Script)
	pReq := pipeline.JobRequest{
		Script:   pipeline.Script{Href: href},
		Nicename: req.Nicename,
		Priority: req.Priority,
	}
	for name, values := range req.Inputs {
		input := pipeline.Input{Name: name}
		for _, v := range values {
			value, err := v(req.Data)
			if (err != nil) {
				return pReq, err
			}
			input.Items = append(input.Items, pipeline.Item{Value: value.String()})
		}
		pReq.Inputs = append(pReq.Inputs, input)
	}
	var stylesheetParametersOption pipeline.Option
	for name, values := range req.Options {
		option := pipeline.Option{Name: name}
		if len(values) > 1 {
			for _, v := range values {
				value, err := v(req.Data)
				if (err != nil) {
					return pReq, err
				}
				option.Items = append(option.Items, pipeline.Item{Value: value})
			}
		} else {
			var err error
			option.Value, err = values[0](req.Data)
			if (err != nil) {
				return pReq, err
			}
		}
		if name == "stylesheet-parameters" {
			stylesheetParametersOption = option
		} else {
			pReq.Options = append(pReq.Options, option)
		}
	}
	var params []string
	for name, p := range req.StylesheetParameters {
		param, err := p(req.Data)
		if (err != nil) {
			return pReq, err
		}
		switch param.Type.(type) {
		case pipeline.XsBoolean,
		     pipeline.XsInteger,
		     pipeline.XsNonNegativeInteger:
			params = append(params, fmt.Sprintf("%s: %s", name, param.Value))
		default:
			params = append(params,
			                fmt.Sprintf("%s: '%s'", name, strings.NewReplacer(
			                                                  "\n", "\\A ",
			                                                  "'", "\\27 ",
			                                              ).Replace(param.Value)))
		}
	}
	if (len(params) > 0) {
		value := fmt.Sprintf("(%s)", strings.Join(params, ", "))
		if stylesheetParametersOption.Name == "" {
			stylesheetParametersOption = pipeline.Option{Name: "stylesheet-parameters"}
			stylesheetParametersOption.Value = value
		} else {
			if stylesheetParametersOption.Value != "" {
				stylesheetParametersOption.Items = append(
					stylesheetParametersOption.Items,
					pipeline.Item{Value: stylesheetParametersOption.Value})
				stylesheetParametersOption.Value = ""
			}
			stylesheetParametersOption.Items = append(
					stylesheetParametersOption.Items,
					pipeline.Item{Value: value})
		}
	}
	if stylesheetParametersOption.Name != "" {
		pReq.Options = append(pReq.Options, stylesheetParametersOption)
	}
	return pReq, nil
}
