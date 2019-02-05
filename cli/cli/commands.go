package cli

import (
	"fmt"
	"os"
	"strings"

	"github.com/daisy/pipeline-clientlib-go"
)

const (
	JobStatusTemplate = `
Job Id: {{.Data.Id }}
Status: {{.Data.Status}}
{{if .Running}}Progress: {{.Data.Messages.Progress | printAsPercentage}}
{{end}}Priority: {{.Data.Priority}}
{{if .Verbose}}Messages:
{{range .Data.Messages.Message}}
[{{.Level}}]	{{.Content}}
{{end}}
{{end}}
`

	JobListTemplate = `Job Id          (Nicename)              [STATUS]
{{range .}}{{.Id}}{{if .Nicename }}	({{.Nicename}}){{end}}	[{{.Status}}]
{{end}}`

	VersionTemplate = `
Client version:                 {{.CliVersion}}         
Pipeline version:               {{.Version}}
Pipeline authentication:        {{.Authentication}}
`

	QueueTemplate = `Job Id 			Priority	Job P.	 Client P.	Rel.Time.	 Since
{{range .}}{{.Id}}	{{.ComputedPriority | printf "%.2f"}}	{{.JobPriority}}	{{.ClientPriority}}	{{.RelativeTime | printf "%.2f"}}	{{.TimeStamp}}
{{end}}`
)

//Convinience struct for printing jobs
type printableJob struct {
	Data    pipeline.Job
	Verbose bool
	Running bool
}

func AddJobStatusCommand(cli *Cli, link PipelineLink) {
	printable := &printableJob{
		Data:    pipeline.Job{},
		Verbose: false,
		Running: false,
	}
	fn := func(args ...string) (interface{}, error) {
		job, err := link.Job(args[0])
		if err != nil {
			return nil, err
		}
		printable.Data = job
		if (job.Status == "RUNNING") {
			printable.Running = true
		}
		return printable, nil
	}
	cmd := newCommandBuilder("status", "Returns the status of the job with id JOB_ID").
		withCall(fn).withTemplate(JobStatusTemplate).
		buildWithId(cli)

	cmd.AddSwitch("verbose", "v", "Prints the job's messages", func(swtich, nop string) error {
		printable.Verbose = true
		return nil
	})
}

func AddDeleteCommand(cli *Cli, link PipelineLink) {
	fn := func(args ...string) (interface{}, error) {
		id := args[0]
		ok, err := link.Delete(id)
		if err == nil && ok {
			return fmt.Sprintf("Job %v removed from the server\n", id), err
		}
		return "", err
	}
	newCommandBuilder("delete", "Removes a job from the pipeline").
		withCall(fn).buildWithId(cli)
}

func AddResultsCommand(cli *Cli, link PipelineLink) {
	outputPath := ""
	zipped := false
	cmd := newCommandBuilder("results", "Stores the results from a job").
		withCall(func(args ...string) (v interface{}, err error) {

		wc, err := zipProcessor(outputPath, zipped)
		if err != nil {
			return
		}
		ok, err := link.Results(args[0], wc)
		if err != nil {
			return
		}
		if err = wc.Close(); err != nil {
			return
		}

		var extra string
		if zipped {
			extra = "zipfile "
		}
		if ok {
			return fmt.Sprintf("Results stored into %s%v\n", extra, outputPath), err
		} else {
			return fmt.Sprintf("No results available for job %s\n", args[0]), err
		}
	}).buildWithId(cli)
	cmd.AddOption("output", "o", "Directory where to store the results", "", "DIRECTORY", func(name, folder string) error {
		outputPath = folder
		return nil
	}).Must(true)

	cmd.AddSwitch("zipped", "z", "Store the results into a zipfile rather than to folder", func(string, string) error {
		zipped = true
		return nil
	}).Must(false)
}

func AddLogCommand(cli *Cli, link PipelineLink) {
	outputPath := ""
	fn := func(vals ...string) (ret interface{}, err error) {
		data, err := link.Log(vals[0])
		if err != nil {
			return
		}
		outWriter := cli.Output
		if len(outputPath) > 0 {
			file, err := os.Create(outputPath)
			ret = fmt.Sprintf("Log written to %s\n", file.Name())
			defer func() {
				file.Close()
			}()
			if err != nil {
				return ret, err
			}
			outWriter = file
		}
		_, err = outWriter.Write(data)
		return ret, err
	}
	cmd := newCommandBuilder("log", "Stores the results from a job").
		withCall(fn).buildWithId(cli)

	cmd.AddOption("output", "o", "Write the log lines into the file provided instead of printing it", "", "", func(name, file string) error {
		outputPath = file
		return nil
	})
}

func AddHaltCommand(cli *Cli, link PipelineLink) {
	fn := func(...string) (val interface{}, err error) {
		key, err := loadKey()
		if err != nil {
			return nil, fmt.Errorf("Coudn't open key file: %s", err.Error())
		}
		err = link.Halt(key)
		if err != nil {
			return
		}
		return fmt.Sprintf("The webservice has been halted\n"), err
	}
	newCommandBuilder("halt", "Stops the webservice").withCall(fn).build(cli)
}

func AddJobsCommand(cli *Cli, link PipelineLink) {
	newCommandBuilder("jobs", "Returns the list of jobs present in the server").
		withCall(func(...string) (interface{}, error) {
		return link.Jobs()
	}).withTemplate(JobListTemplate).build(cli)
}

func AddQueueCommand(cli *Cli, link PipelineLink) {
	fn := func(...string) (queue interface{}, err error) {
		return link.Queue()
	}
	newCommandBuilder("queue", "Shows the execution queue and the job's priorities. ").
		withCall(fn).withTemplate(QueueTemplate).build(cli)
}

func AddMoveUpCommand(cli *Cli, link PipelineLink) {
	fn := func(args ...string) (queue interface{}, err error) {
		return link.MoveUp(args[0])
	}
	newCommandBuilder("moveup", "Moves the job up the execution queue").
		withCall(fn).withTemplate(QueueTemplate).
		buildWithId(cli)

}

func AddMoveDownCommand(cli *Cli, link PipelineLink) {
	fn := func(args ...string) (queue interface{}, err error) {
		return link.MoveDown(args[0])
	}
	newCommandBuilder("movedown", "Moves the job down the execution queue").
		withCall(fn).withTemplate(QueueTemplate).
		buildWithId(cli)

}

type Version struct {
	*PipelineLink
	CliVersion string
}

func AddVersionCommand(cli *Cli, link *PipelineLink) {
	newCommandBuilder("version", "Prints the version and authentication information").
		withCall(func(...string) (interface{}, error) {
		return Version{link, VERSION}, nil
	}).withTemplate(VersionTemplate).build(cli)

}

func AddCleanCommand(cli *Cli, link PipelineLink) {
	pred := isError
	fn := func(args ...string) (interface{}, error) {
		jobs, err := link.Jobs()
		if err != nil {
			return "", err
		}
		deleteFn := func(j pipeline.Job, c chan string) {
			ok, err := link.Delete(j.Id)
			if err == nil && ok {
				c <- fmt.Sprintf("Job %v removed from the server\n", j.Id)
			} else {
				c <- fmt.Sprintf("Couldn't remove Job %v from the server (%v)\n", j.Id, err)
			}
		}
		msgs := parallelMap(jobs, deleteFn, pred)
		return strings.Join(msgs, ""), nil

	}
	cmd := newCommandBuilder("clean", "Removes the jobs with an ERROR status").
		withCall(fn).build(cli)
	cmd.AddSwitch("done", "d", "Removes also the jobs with a DONE status", func(string, string) error {
		pred = or(pred, isDone)
		return nil
	})
	cmd.SetArity(0, "")
}
