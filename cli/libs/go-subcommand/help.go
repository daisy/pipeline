package subcommand

import (
	"fmt"
	"io"
	"os"
	"strings"
	"text/template"
)

var output io.Writer = os.Stdout

const (
	PARSER_HELP_TEMPLATE = `
Usage {{.Name}} [GLOBAL_OPTIONS]{{if .Arity.Count}} {{.Arity.Description}}{{end}} command [COMMAND_OPTIONS] [PARAMS]
{{if .Flags}}
global options:

{{range .Flags }}       {{flagAligner .FlagStringPrefix}} {{.Description}}
{{end}}{{end}}
{{if .Commands}}
commands:

        {{range .Commands}}{{commandAligner .Name }} {{.Description}}
        {{end}}
{{end}}
`
	COMMAND_HELP_TEMPLATE = `
Usage: {{.Parent.Name}} [GLOBAL_OPTIONS] {{.Name}} [OPTIONS]  {{if .Arity.Count}} {{.Arity.Description}}{{end}}
{{.Description}}
{{if .Flags}}
Options:
{{range .Flags }}       {{flagAligner .FlagStringPrefix}} {{.Description}}
{{end}}
{{end}}
`
)

func defaultHelp(p Parser) CommandFunction {
	return func(help string, args ...string) error {
		var funcMap template.FuncMap
		var tempText string
		var element interface{}

		if len(args) > 0 {
			if cmd, ok := p.Commands[args[0]]; ok {
				funcMap = template.FuncMap{
					"flagAligner": flagAligner(cmd.Flags()),
				}
				tempText = COMMAND_HELP_TEMPLATE
				element = cmd

			} else {
				fmt.Printf("help: command not found %v\n", args[0])
			}
		} else {
			funcMap = template.FuncMap{
				"commandAligner": commandAligner(p.Commands),
				"flagAligner":    flagAligner(p.Flags()),
			}
			tempText = PARSER_HELP_TEMPLATE
			element = &p
		}
		tmpl := template.Must(template.New("").Funcs(funcMap).Parse(tempText))
		return tmpl.Execute(output, element)
	}
}

func commandAligner(commands map[string]*Command) func(string) string {
	longest := getLongestName(commands)
	return func(name string) string {
		return fmt.Sprintf("%s%s", name, strings.Repeat(" ", longest-len(name)+4))
	}
}

func flagAligner(flags []Flag) func(string) string {
	longest := getLongestFlag(flags)
	return func(name string) string {
		return fmt.Sprintf("%s%s", name, strings.Repeat(" ", longest-len(name)+4))
	}
}
func getLongestFlag(flags []Flag) int {
	max := -1
	for _, f := range flags {
		if max < len(f.FlagStringPrefix()) {
			max = len(f.FlagStringPrefix())
		}
	}
	return max
}
func getLongestName(commands map[string]*Command) int {
	max := -1
	for _, s := range commands {
		if max < len(s.Name) {
			max = len(s.Name)
		}
	}
	return max
}
