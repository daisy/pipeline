package cli

import (
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/url"
	"os"
	"runtime"
	"strings"
	"regexp"
	"strconv"

	"github.com/bertfrees/blackterm"
	"github.com/capitancambio/chalk"
	"github.com/bertfrees/go-subcommand"
	"github.com/daisy/pipeline-clientlib-go"
)

//set the last id path (in utils)
var LastIdPath = getLastIdPath(runtime.GOOS)

//Represents the job request
type JobRequest struct {
	Script               string                                     //Script id to call
	Nicename             string                                     //Job's nicename
	Priority             string                                     //Job's priority
	Options              map[string][]func([]byte) (string, error)  //Options for the script
	Inputs               map[string][]func([]byte) (url.URL, error) //Input documents for the script
	Data                 []byte                                     //Data to send with the job request
	Background           bool                                       //Send the request and return
	StylesheetParameters map[string]func([]byte) (pipeline.StylesheetParameter, error)
}

//Creates a new JobRequest
func newJobRequest() *JobRequest {
	return &JobRequest{
		Options:              make(map[string][]func([]byte) (string, error)),
		Inputs:               make(map[string][]func([]byte) (url.URL, error)),
		StylesheetParameters: make(map[string]func([]byte) (pipeline.StylesheetParameter, error)),
	}
}

//Represents the stylesheet-parameters request
type StylesheetParametersRequest struct {
	Medium      string
	ContentType string
	Data        []byte
}

//Convinience method to add several scripts to a client
func (c *Cli) AddScripts(scripts []pipeline.Script, link *PipelineLink) error {
	for _, s := range scripts {
		if _, err := scriptToCommand(s, c, link); err != nil {
			return err
		}
	}
	return nil
}

//Executes a job request
type jobExecution struct {
	link       *PipelineLink
	req        *JobRequest
	output     string
	verbose    bool
	persistent bool
	zipped     bool
}

func (j jobExecution) run(stdOut io.Writer) error {
	log.Printf("run data len %v\n", len(j.req.Data))
	//manual check of output
	if !j.req.Background && j.output == "" {
		return errors.New("--output option is mandatory if the job is not running in the background")
	}
	if j.req.Background && j.output != "" {
		fmt.Printf("Warning: --output option ignored as the job will run in the background\n")
	}
	storeId := j.req.Background || j.persistent
	//send the job
	job, messages, err := j.link.Execute(*(j.req))
	if err != nil {
		return err
	}
	fmt.Fprintf(stdOut, "Job %v sent to the server\n", job.Id)
	//store id if it suits
	if storeId {
		err = storeLastId(job.Id)
		if err != nil {
			return err
		}
	}
	//get realtime messages, status and progress from the webservice
	status := job.Status
	progress := 0.0
	printProgressBar(stdOut, progress)
	for msg := range messages {
		if msg.Error != nil {
			err = msg.Error
			return err
		}
		if j.verbose && msg.Message != "" || msg.Progress > progress {
			//erase the progress bar (last two lines)
			//FIXME: don't do this when debug logging enabled
			fmt.Fprint(stdOut, "\n\033[1A\033[K\033[1A\033[K")
			if j.verbose && msg.Message != "" {
				fmt.Fprintf(stdOut, "%v\n", msg.String())
			}
			if (msg.Progress > progress) {
				progress = msg.Progress
			}
			printProgressBar(stdOut, progress)
		}
		status = msg.Status
	}

	if status != "ERROR" {
		//get the data
		if !j.req.Background {
			wc, err := zipProcessor(j.output, j.zipped)
			if err != nil {
				return err
			}
			ok, err := j.link.Results(job.Id, wc)
			if err != nil {
				return err
			}
			if err := wc.Close(); err != nil {
				return err
			}
			fmt.Fprintln(stdOut)
			if !j.persistent {
				_, err = j.link.Delete(job.Id)
				if err != nil {
					return err
				}
				fmt.Fprintf(stdOut, "The job has been deleted from the server\n")
			}
			fmt.Fprintf(stdOut, "Job finished with status: %v\n", status)
			if (!ok && (status == "SUCCESS" || status == "FAIL")) {
				fmt.Fprintf(stdOut, "No results available\n")
			}
		}

	}
	return nil
}

func printProgressBar(stdOut io.Writer, value float64) {
	line := ""
	for len(line) < 78 {
		line += "_"
	}
	bar := ""
	for i := 1; i <= int(value * 72); i++ {
		bar += "█"
	}
	for len(bar) < 216 { // 72 * 3 because 3 bytes per character
		bar += "░"
	}
	fmt.Fprintf(stdOut, "%v\n%v %.1f%% ", line, bar, value * 100)
}

var commonFlags = []string{"--output", "--zip", "--nicename", "--priority", "--quiet", "--persistent", "--background"}

func getFlagName(name, prefix string, flags []subcommand.Flag) string {
	flaggedName := "--" + name

	allFlags := []string{}
	allFlags = append(allFlags, commonFlags...)
	allFlags = append(allFlags, flagsToString(flags)...)
	for _, f := range allFlags {
		if f == flaggedName {
			return prefix + name
		}

	}

	return name
}
func flagsToString(flags []subcommand.Flag) []string {
	res := make([]string, len(flags), len(flags))
	for idx, flag := range flags {
		res[idx] = flag.Long
	}
	return res
}

// FIXME: don't hard code
var mediumForScript = map[string]string {
	"dtbook-to-daisy3": "speech",
	"dtbook-to-epub3":  "speech",
	"dtbook-to-pef":    "embossed",
	"epub-to-daisy":    "speech",
	"epub3-to-epub3":   "speech, embossed",
	"epub3-to-pef":     "embossed",
	"html-to-pef":      "embossed",
	"zedai-to-epub3":   "speech",
}
var contentTypeForScript = map[string]string {
	"dtbook-to-daisy3": "application/x-dtbook+xml",
	"dtbook-to-epub3":  "application/x-dtbook+xml",
	"dtbook-to-pef":    "application/x-dtbook+xml",
	"epub-to-daisy":    "application/xhtml+xml",
	"epub3-to-epub3":   "application/xhtml+xml",
	"epub3-to-pef":     "application/xhtml+xml",
	"html-to-pef":      "application/xhtml+xml",
	"zedai-to-epub3":   "application/z3998-auth+xml",
}

//Adds the command and flags to be able to call the script to the cli
func scriptToCommand(script pipeline.Script, cli *Cli, link *PipelineLink) (req *JobRequest, err error) {
	jobRequest := newJobRequest()
	jobRequest.Script = script.Id
	jobRequest.Background = false
	jExec := jobExecution{
		link:    link,
		req:     jobRequest,
		output:  "",
		verbose: true,
		zipped:  false,
	}
	desc := blackterm.MarkdownString(script.Description)
	command := cli.AddScriptCommand(
		script.Id,
		desc,
		fmt.Sprintf("%s [v%s]", desc, script.Version),
		func(string, ...string) error {
			if err := jExec.run(cli.Output); err != nil {
				return err
			}
			return nil
		},
		jobRequest,
	)
	command.SetArity(0, "")

	for _, input := range script.Inputs {
		name := getFlagName(input.Name, "i-", command.Flags())
		shortDesc := input.ShortDesc
		longDesc := input.LongDesc
		// FIXME: assumes markdown without html
		if (longDesc != "" && shortDesc != "" && strings.HasPrefix(longDesc, shortDesc + "\n\n")) {
			// don't interpret first line as markdown
			longDesc = shortDesc + "\n\n" + blackterm.MarkdownString(longDesc[len(shortDesc)+2:])
		} else {
			longDesc = blackterm.MarkdownString(longDesc)
		}
		if (shortDesc == "") {
			shortDesc = input.NiceName
		}
		command.AddOption(name, "", shortDesc, longDesc, italic("FILE"), inputFunc(jobRequest)).Must(input.Required)
	}

	var hasStylesheetParametersOption bool
	for _, option := range script.Options {
		//desc:=option.Desc+
		name := getFlagName(option.Name, "x-", command.Flags())
		shortDesc := option.ShortDesc
		longDesc := option.LongDesc
		possibleValues := optionTypeToDetailedHelp(option.Type)
		if (possibleValues != "") {
			longDesc += ("\n\nPossible values: " + possibleValues)
		}
		if ! option.Required {
			longDesc += "\n\nDefault value: "
			if option.Default == "" {
				longDesc += "(empty)"
			} else {
				longDesc += "`" + option.Default + "`"
			}
		}
		// FIXME: assumes markdown without html
		if (longDesc != "" && shortDesc != "" && strings.HasPrefix(longDesc, shortDesc + "\n\n")) {
			// don't interpret first line as markdown
			longDesc = shortDesc + "\n\n" + blackterm.MarkdownString(longDesc[len(shortDesc)+2:])
		} else {
			longDesc = blackterm.MarkdownString(longDesc)
		}
		if (shortDesc == "") {
			shortDesc = option.NiceName
		}
		command.AddOption(
			name, "", shortDesc, longDesc, optionTypeToString(option.Type, name, option.Default),
			optionFunc(jobRequest, option.Type, option.Sequence)).Must(option.Required)
		if option.Name == "stylesheet-parameters" {
			hasStylesheetParametersOption = true
		}
	}

	if hasStylesheetParametersOption {
		medium := mediumForScript[script.Id]
		contentType := contentTypeForScript[script.Id]
		if medium != "" && contentType != "" {
			params, err := link.StylesheetParameters(
				StylesheetParametersRequest{
					Medium:  medium,
					ContentType: contentType,
				})
			if err != nil {
				return jobRequest, err
			}
			for _, param := range params.Parameters {
				name := getFlagName(param.Name, "x-", command.Flags())
				shortDesc := param.ShortDesc
				longDesc := param.LongDesc
				possibleValues := optionTypeToDetailedHelp(param.Type)
				if (possibleValues != "") {
					longDesc += ("\n\nPossible values: " + possibleValues)
				}
				longDesc += "\n\nDefault value: "
				if param.Default == "" {
					longDesc += "(empty)"
				} else {
					longDesc += "`" + param.Default + "`"
				}
				if (shortDesc != "" && strings.HasPrefix(longDesc, shortDesc + "\n\n")) {
					// don't interpret first line as markdown
					longDesc = shortDesc + "\n\n" + blackterm.MarkdownString(longDesc[len(shortDesc)+2:])
				} else {
					longDesc = blackterm.MarkdownString(longDesc)
				}
				if (shortDesc == "") {
					shortDesc = param.NiceName
				}
				command.AddOption(
					name, "", shortDesc, longDesc, optionTypeToString(param.Type, name, param.Default),
					paramFunc(jobRequest, param)).Must(false)
			}
		}
	}

	command.AddOption("output", "o", "Path where to store the results. This option is mandatory when the job is not executed in the background", "", italic("DIRECTORY"), func(name, folder string) error {
		jExec.output = folder
		return nil
	})
	command.AddSwitch("zip", "z", "Write the output to a zip file rather than to a folder", func(string, string) error {
		jExec.zipped = true
		return nil
	})

	command.AddOption("nicename", "n", "Set job's nice name", "", italic("NICENAME"), func(name, nice string) error {
		jExec.req.Nicename = nice

		return nil
	})
	command.AddOption("priority", "r", "Set job's priority", "", "(high|" + underline("medium") + "|low)", func(name, priority string) error {
		if checkPriority(priority) {
			jExec.req.Priority = priority
			return nil
		} else {
			return fmt.Errorf("%s is not a valid priority. Allowed values are high, medium and low",
				priority)
		}
	})
	command.AddSwitch("quiet", "q", "Do not print the job's messages", func(string, string) error {
		jExec.verbose = false
		return nil
	})
	command.AddSwitch("persistent", "p", "Do not delete the job after it is executed", func(string, string) error {
		jExec.persistent = true
		return nil
	})

	command.AddSwitch("background", "b", "Sends the job and exits", func(string, string) error {
		jExec.req.Background = true
		return nil
	})

	return jobRequest, nil
}

func optionTypeToString(optionType pipeline.DataType, optionName string, defaultValue string) string {
	switch t := optionType.(type) {
	case pipeline.AnyFileURI:
		return italic("FILE")
	case pipeline.AnyDirURI:
		return italic("DIRECTORY")
	case pipeline.XsBoolean:
		if defaultValue == "true" {
			return "(" + underline("true") + "|false)"
		} else if defaultValue == "false" {
			return "(true|" + underline("false") + ")"
		} else {
			return "(true|false)"
		}
	case pipeline.XsInteger:
		return italic("INTEGER")
	case pipeline.XsNonNegativeInteger:
		return italic("NON-NEGATIVE-INTEGER")
	case pipeline.Choice:
		var choices []string
		for _, value := range t.Values {
			choices = append(choices, optionTypeToString(value, "", defaultValue))
		}
		var choicesString = "(" + strings.Join(choices, "|") + ")"
		if len(choicesString) > 60 {
			break;
		}
		return choicesString
	case pipeline.Value:
		if t.Value == defaultValue {
			return underline(t.Value)
		} else {
			return t.Value
		}
	case pipeline.Pattern:
		if optionName == "" {
			return italic("PATTERN")
		}
	case pipeline.XsAnyURI:
		return italic("URI")
	case pipeline.XsString:
		return italic("STRING")
	default:
		if optionName == "" {
			return italic("STRING")
		}
	}
	return italic(strings.ToUpper(optionName))
}

func italic(s string) string {
	return chalk.Italic.TextStyle(s)
}

func underline(s string) string {
	return chalk.Underline.TextStyle(s)
}

func optionTypeToDetailedHelp(optionType pipeline.DataType) string {
	help := ""
	switch t := optionType.(type) {
	case pipeline.AnyFileURI:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "A _FILE_"
		}
	case pipeline.AnyDirURI:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "A _DIRECTORY_"
		}
	case pipeline.XsBoolean:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "`true` or `false`"
		}
	case pipeline.XsInteger:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "An _INTEGER_"
		}
	case pipeline.XsNonNegativeInteger:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "An non-negative _INTEGER_"
		}
	case pipeline.Choice:
		help += "One of the following:\n"
		for _, value := range t.Values {
			help += "\n- "
			help += indent(optionTypeToDetailedHelp(value), "  ")
		}
	case pipeline.Value:
		if t.Value == "" {
			help += "(empty)";
		} else {
			help += ("`" + t.Value + "`")
		}
		if t.Documentation != "" {
			help += (": " + t.Documentation)
		}
	case pipeline.Pattern:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += ("A string that matches the pattern:\n" + t.Pattern)
		}
	case pipeline.XsAnyURI:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "A _URI_"
		}
	case pipeline.XsString:
		if t.Documentation != "" {
			help += t.Documentation
		} else {
			help += "A _STRING_"
		}
	default:
	}
	return help
}

func (c *ScriptCommand) addDataOption(required bool) {
	c.AddOption("data", "d", "Zip file containing the files to convert", "", "", func(name, path string) error {
		file, err := os.Open(path)
		defer func() {
			err := file.Close()
			if err != nil {
				log.Printf("Error closing file %v :%v", path, err.Error())
			}
		}()
		if err != nil {
			return err
		}
		c.req.Data, err = ioutil.ReadAll(file)
		//FIXME: this breaks the tests, but focused in a different thing right now
		//if err != nil {
		//return err
		//}
		log.Printf("data len %v\n", len(c.req.Data))
		return nil
	}).Must(required)
}

//Returns a function that fills the request info with the subcommand option name
//and value
func inputFunc(req *JobRequest) func(string, string) error {
	return func(name, value string) error {
		//control prefix
		if strings.HasPrefix("i-", name) {
			name = name[2:]
		}
		// FIXME: check if input is a sequence
		for _, path := range strings.Split(value, ",") {
			req.Inputs[name] = append(req.Inputs[name], func(data []byte) (result url.URL, err error) {
				basePath := getBasePath(data)
				var u *url.URL
				u, err = pathToUri(path, basePath)
				if err != nil {
					return
				}
				return *u, nil
			})
		}
		return nil
	}
}

//Returns a function that fills the request option with the subcommand option name
//and value
func optionFunc(req *JobRequest, optionType pipeline.DataType, sequence bool) func(string, string) error {
	return func(name, value string) error {
		if strings.HasPrefix("x-", name) {
			name = name[2:]
		}
		var err error
		if sequence {
			for _, v := range strings.Split(value, ",") {
				req.Options[name] = append(req.Options[name], func(data []byte) (string, error) {
					v, err = validateOption(v, optionType, data)
					if err != nil {
						return v, validationError(name, v, err)
					}
					return v, nil
				})
			}
		} else {
			req.Options[name] = append(req.Options[name], func(data []byte) (string, error) {
				value, err = validateOption(value, optionType, data)
				if err != nil {
					return value, validationError(name, value, err)
				}
				return value, nil
			})
		}
		return nil
	}
}

//Returns a function that fills the stylesheet-parameters option with the subcommand option name
//and value
func paramFunc(req *JobRequest, param pipeline.StylesheetParameter) func(string, string) error {
	return func(name, value string) error {
		if strings.HasPrefix("x-", name) {
			name = name[2:]
		}
		req.StylesheetParameters[name] = func(data []byte) (pipeline.StylesheetParameter, error) {
			value, err := validateOption(value, param.Type, data)
			if err != nil {
				return param, validationError(name, value, err)
			}
			param.Value = value;
			return param, nil
		}
		return nil
	}
}

func validationError(optionName, value string, cause error) error {
	msg := "'" + value + "' is not allowed as the value for option --" + optionName
	if cause != nil {
		msg += (": " + cause.Error())
	}
	return errors.New(msg)
}

func validateOption(value string, optionType pipeline.DataType, data []byte) (result string, err error) {
	result = value
	switch t := optionType.(type) {
	case pipeline.XsBoolean:
		var b bool
		b, err = strconv.ParseBool(value)
		if err == nil {
			result = strconv.FormatBool(b)
		}
	case pipeline.XsInteger:
		_, err = strconv.ParseInt(value, 0, 0)
	case pipeline.XsNonNegativeInteger:
		var i int64
		i, err = strconv.ParseInt(value, 0, 0)
		if (i < 0) {
			err = errors.New("does not match " + uncolor(optionTypeToString(t, "", "") + ": value is negative"))
			return
		}
	case pipeline.XsAnyURI:
		// _, err = url.Parse(value)
	case pipeline.AnyFileURI:
		var u *url.URL
		u, err = pathToUri(value, getBasePath(data))
		if err == nil {
			result = u.String()
		}
	case pipeline.AnyDirURI:
		var u *url.URL
		u, err = pathToUri(value, getBasePath(data))
		if err == nil {
			result = u.String()
		}
	case pipeline.Pattern:
		var match bool
		match, err = regexp.MatchString("^(?:" + t.Pattern + ")$", value)
		if err == nil && ! match {
			err = errors.New("does not match /" + t.Pattern + "/")
			return
		}
	case pipeline.Choice:
		for _, v := range t.Values {
			result, err = validateOption(value, v, data)
			if err == nil {
				break
			}
		}
		if err != nil {
			err = errors.New("does not match " + uncolor(optionTypeToString(t, "", "")))
			return
		}
	case pipeline.Value:
		if t.Value != value {
			err = errors.New("does not match '" + t.Value + "'")
			return
		}
	default:
	}
	if err != nil {
		err = errors.New("does not match " + uncolor(optionTypeToString(optionType, "", ""))+ ": " + err.Error())
	}
	return
}

//Gets the basepath. If the fwk accepts local uri's (file:///)
//getBasePath os.Getwd() otherwise it returns an empty string
func getBasePath(data []byte) string {
	if data == nil {
		base, err := os.Getwd()
		if err != nil {
			panic("Error while getting current directory:" + err.Error())
		}
		return base + "/"
	} else {
		return ""
	}
}

// mockable version of filepath.ToSlash
func toSlash(path string) string {
	if pathSeparator == '/' {
		return path
	}
	return strings.Replace(path, string(pathSeparator), "/", -1)
}

//Accepts several paths separated by separator and constructs the URLs
//relative to base path
func pathToUri(path string, basePath string) (u *url.URL, err error) {
	if basePath != "" {
		// localfs
		var baseUrl *url.URL
		basePath = toSlash(basePath)
		if string(basePath[0]) != "/" {
			//for windows path to build a proper url
			basePath = "/" + basePath
		}
		baseUrl, err = url.Parse("file:" + basePath)
		if err != nil {
			return
		}
		u, err = url.Parse(toSlash(path))
		if err == nil && u.IsAbs() && u.Scheme != "file" {
			// this means the path started with a drive name (like C:) which resulted in an absolute but wrong URL (e.g. c:///)
			u, err = url.Parse("file:/" + toSlash(path))
		}
		if err != nil {
			return
		}
		u = baseUrl.ResolveReference(u)
		// check that file exists (do it at the end so that the url resolving part can be tested)
		// FIXME: use basePath instead of implicit pwd
		// FIXME: does this also work for directories?
		_, err = os.Stat(path)
		if os.IsNotExist(err) {
			return
		} else {
			err = nil
		}
		if u.Scheme != "file" {
			err = errors.New("not a file uri: " + u.String())
		} else if ! u.IsAbs() {
			err = errors.New("uri not absolute: " + u.String())
		}
	} else {
		// FIXME: check if file present in zip
		//TODO is opaque really apropriate?
		u = &url.URL{
			Opaque: toSlash(path),
		}
	}
	return
}

func storeLastId(id string) error {
	file, err := os.Create(LastIdPath)
	if err != nil {
		return err
	}
	defer func() {
		file.Close()
	}()
	if _, err := file.Write([]byte(id)); err != nil {
		return err
	}
	return nil
}

func getLastId() (id string, err error) {
	idBuf, err := ioutil.ReadFile(LastIdPath)
	if err != nil {
		return "", err
	}
	return string(idBuf), nil
}
