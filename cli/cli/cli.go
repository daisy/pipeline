package cli

import (
	"fmt"
	"io"
	"log"
	"os"
	"strconv"
	"strings"
	"text/template"
	"regexp"

	"github.com/bertfrees/go-subcommand"
)

const (
	VERSION = "2.1.7"
)

const (
	MAIN_HELP_TEMPLATE = `
Usage {{.Name}} [GLOBAL_OPTIONS] command [COMMAND_OPTIONS] [PARAMS]

{{if .Scripts}}
Script commands:

        {{range .Scripts}}{{commandAligner .Name }} {{.ShortDesc}}
        {{end}}
{{end}}
General commands:

        {{range .StaticCommands}}{{commandAligner .Name}} {{.ShortDesc}}
        {{end}}


List of global options:                 {{.Name}} help -g
List of admin commands:                 {{.Name}} help -a
Detailed help for a single command:     {{.Name}} help COMMAND
`
	ADMIN_HELP_TEMPLATE = `
Usage {{.Name}} [GLOBAL_OPTIONS] command [COMMAND_OPTIONS] [PARAMS]

Admin commands:
        {{range .AdminCommands}}{{commandAligner .Name}} {{.ShortDesc}}
        {{end}}

List of global options:                 {{.Name}} help -g 
Detailed help for a single command:     {{.Name}} help COMMAND
`
	COMMAND_HELP_TEMPLATE = `
Usage: {{.Parent.Name}} [GLOBAL_OPTIONS] {{.Name}}{{if .MandatoryFlags}} REQUIRED_OPTIONS{{end}}{{if .MandatoryFlags}} [OPTIONS]{{end}} {{ .Arity.Description}}

{{.LongDesc}}

{{if .MandatoryFlags}}Required options:
{{range .MandatoryFlags }}       {{flagAligner .FlagStringPrefix}} {{.ShortDesc}}
{{end}}{{end}}
{{if .NonMandatoryFlags}}{{if .MandatoryFlags}}Other options{{end}}{{if not .MandatoryFlags}}Options{{end}}:
{{range .NonMandatoryFlags }}       {{flagAligner .FlagStringPrefix}} {{.ShortDesc}}
{{end}}{{end}}

List of global options:                 {{.Parent.Name}} help -g
Detailed help:                          {{.Parent.Name}} help --verbose {{.Name}}
{{if .Flags}}Detailed help for a single option:      {{.Parent.Name}} help {{.Name}} OPTION
{{end}}
`

	COMMAND_DETAILED_HELP_TEMPLATE = `
Usage: {{.Parent.Name}} [GLOBAL_OPTIONS] {{.Name}}{{if .MandatoryFlags}} REQUIRED_OPTIONS{{end}}{{if .MandatoryFlags}} [OPTIONS]{{end}} {{ .Arity.Description}}

{{.LongDesc}}

{{if .MandatoryFlags}}Required options:
{{range .MandatoryFlags }}       {{.FlagStringPrefix}}
{{indent .LongDesc}}

{{end}}{{end}}{{if .NonMandatoryFlags}}{{if .MandatoryFlags}}Other options{{end}}{{if not .MandatoryFlags}}Options{{end}}:
{{range .NonMandatoryFlags }}       {{.FlagStringPrefix}}
{{indent .LongDesc}}

{{end}}{{end}}
List of global options:       {{.Parent.Name}} help -g

`

	GLOBAL_OPTIONS_TEMPLATE = `

Global Options:
{{range .Flags }}       {{flagAligner .FlagStringPrefix}} {{.ShortDesc}}
{{end}}

`

//{{range .Flags}}{{if len .Short}}-{{.Short}},{{end}}--{{.Long}}{{if isOption .}}   {{upper .Long}}{{end}}       {{.Description}}{{end}}
)

//Cli is a subcommand that differenciates between script commands and regular commands just to treat them correctly during
//the help display
type Cli struct {
	*subcommand.Parser
	Scripts        []*ScriptCommand      //pipeline scripts
	StaticCommands []*subcommand.Command //commands which are always present
	AdminCommands  []*subcommand.Command //admin commands
	Output         io.Writer             //writer where to dump the output
}

//Script commands have a job request associated
type ScriptCommand struct {
	*subcommand.Command
	req *JobRequest
}

//Creates a new CLI with a name and pipeline link to perform queries
func NewCli(name string, link *PipelineLink) (cli *Cli, err error) {
	cli = &Cli{
		Parser: subcommand.NewParser(name),
		Output: os.Stdout,
	}
	//set the help command
	cli.setHelp()
	//when the first command is processed
	//initialise the link so we take into account the
	//global configuration flags
	cli.PostFlags(func() error {
		if err = link.Init(); err != nil {
			return err
		}
		scripts, err := link.Scripts()
		if err != nil {
			fmt.Printf("Error loading scripts:\n\t%v\n", err)
			os.Exit(-1)
		}
		cli.AddScripts(scripts, link)
		if !link.IsLocal() {
			//it we are not in local mode we need to send the data
			for _, cmd := range cli.Scripts {

				cmd.addDataOption()
			}
		}

		return nil
	})
	//add config flags
	cli.addConfigOptions(link.config)
	return
}

//Sets the help function
func (c *Cli) setHelp() {
	globals := false
	admin := false
	details := false
	cmd := c.Parser.SetHelp("help", "Help description", func(help string, args ...string) error {
		return printHelp(*c, globals, admin, details, args...)
	})
	cmd.AddSwitch("globals", "g", "Show global options", func(string, string) error {
		globals = true
		return nil
	})
	cmd.AddSwitch("admin", "a", "Show admin options", func(string, string) error {
		admin = true
		return nil
	})
	cmd.AddSwitch("verbose", "", "Show detailed help", func(string, string) error {
		details = true
		return nil
	})
}

//Adds the configuration global options to the parser
func (c *Cli) addConfigOptions(conf Config) {
	for option, desc := range config_descriptions {
		c.AddOption(option, "", fmt.Sprintf("%v (default %v)", desc, conf[option]), "", "", func(optName string, value string) error {
			log.Println("option:", optName, "value:", value)
			switch conf[optName].(type) {
			case int:
				val, err := strconv.Atoi(value)
				if err != nil {
					return fmt.Errorf("option %v must be a numeric value (found %v)", optName, value)
				}
				conf[optName] = val
			case bool:
				switch {
				case value == "true":
					conf[optName] = true
				case value == "false":
					conf[optName] = false
				default:
					return fmt.Errorf("option %v must be true or false (found %v)", optName, value)
				}

			case string:
				conf[optName] = value

			}
			conf.UpdateDebug()
			return nil
		})
	}
	//alternative configuration file
	c.AddOption("file", "f", "Alternative configuration file", "", "", func(string, filePath string) error {
		file, err := os.Open(filePath)
		if err != nil {
			log.Printf(err.Error())
			return fmt.Errorf("File not found %v", filePath)
		}
		return conf.FromYaml(file)
	})
}

//Adds the command to the cli and stores the it into the scripts list
func (c *Cli) AddScriptCommand(name, shortDesc string, longDesc string, fn func(string, ...string) error, request *JobRequest) *subcommand.Command {
	cmd := c.Parser.AddCommand(name, shortDesc, longDesc, fn)
	c.Scripts = append(c.Scripts, &ScriptCommand{cmd, request})
	return cmd
}

//Adds a static command to the cli and keeps track of it for the displaying the help
func (c *Cli) AddCommand(name, desc string, fn func(string, ...string) error) *subcommand.Command {
	cmd := c.Parser.AddCommand(name, desc, "", fn)
	c.StaticCommands = append(c.StaticCommands, cmd)
	return cmd
}

//Adds admin related commands to the cli and keeps track of it for displaying help
func (c *Cli) AddAdminCommand(name, desc string, fn func(string, ...string) error) *subcommand.Command {
	cmd := c.Parser.AddCommand(name, desc, "", fn)
	c.AdminCommands = append(c.AdminCommands, cmd)
	return cmd
}

//convinience function to gather all the command names
func (c Cli) mergeCommands() []string {
	names := make([]string, 0, len(c.StaticCommands)+len(c.Scripts))
	for _, cmd := range c.StaticCommands {
		names = append(names, cmd.Name)
	}
	for _, script := range c.Scripts {
		names = append(names, script.Name)
	}

	return names
}

//convinience function to gather all the command names
func flagsToStrings(flags []subcommand.Flag) []string {
	names := make([]string, 0, len(flags))
	for _, flag := range flags {
		names = append(names, flag.FlagStringPrefix())
	}
	return names
}

//Runs the client
func (c *Cli) Run(args []string) error {
	_, err := c.Parser.Parse(args)
	return err
}

//Prints using the client output
func (c *Cli) Printf(format string, vals ...interface{}) {
	fmt.Fprintf(c.Output, format, vals...)
}

//prints the help
func printHelp(cli Cli, globals, admin, details bool, args ...string) error {
	if globals {
		funcMap := template.FuncMap{
			"flagAligner": aligner(flagsToStrings(cli.Flags())),
		}
		template.Must(template.New("globals").Funcs(funcMap).Parse(GLOBAL_OPTIONS_TEMPLATE)).Execute(os.Stdout, cli)

	} else if len(args) == 0 {
		funcMap := template.FuncMap{
			"commandAligner": aligner(cli.mergeCommands()),
		}
		tmplName := MAIN_HELP_TEMPLATE
		if admin {
			tmplName = ADMIN_HELP_TEMPLATE
		}
		template.Must(template.New("mainHelp").Funcs(funcMap).Parse(tmplName)).Execute(os.Stdout, cli)

	} else {
		if len(args) > 2 {
			return fmt.Errorf("help: only one or two parameters accepted. %v found (%v)", len(args), strings.Join(args, ","))
		}
		cmd, ok := cli.Parser.Commands[args[0]]
		if !ok {
			return fmt.Errorf("help: command %v not found ", args[0])
		}
		if len(args) == 1 {
			funcMap := template.FuncMap{
				"flagAligner": aligner(flagsToStrings(cmd.Flags())),
				"indent": func(s string) string {
					margin := "            "
					return margin + indent(s, margin)
				},
			}
			tmpl := COMMAND_HELP_TEMPLATE
			if details {
				tmpl = COMMAND_DETAILED_HELP_TEMPLATE
			}
			template.Must(template.New("commandHelp").Funcs(funcMap).Parse(tmpl)).Execute(os.Stdout, cmd)
		} else {
			for _, flag := range cmd.Flags() {
				if flag.Long == args[1] {
					help := flag.FlagStringPrefix()
					if !flag.Mandatory {
						help = "[" + help + "]"
					}
					help = "\nUsage: " + cli.Parser.Name + " " + cmd.Name + " " + help + " ...\n\n" + flag.LongDesc + "\n\n"
					fmt.Fprintf(os.Stdout, help)
					return nil
				}
			}
			return fmt.Errorf("help: %v option %v not found ", cmd.Name, args[1])
		}
	}
	return nil
}

func aligner(names []string) func(string) string {
	longest := getLongestName(names)
	return func(name string) string {
		return fmt.Sprintf("%s%s", name, strings.Repeat(" ", longest-realLen(name)+4))
	}
}

func getLongestName(name []string) int {
	max := -1
	for _, s := range name {
		len := realLen(s)
		if max < len {
			max = len
		}
	}
	return max
}

func realLen(s string) int {
	return len(uncolor(s))
}

// remove ANSI color codes
func uncolor(s string) string {
	return regexp.MustCompile(`\x1b\[[0-9;]*m`).ReplaceAllString(s, "")
}

// hanging indent
func indent(s string, indent string) string {
	parts := strings.Split(s, "\n")
	result := parts[0]
	for _, part := range parts[1:] {
		result += "\n"
		result += indent
		result += part
	}
	return result
}
