package cli

import (
	"text/template"

	"github.com/capitancambio/go-subcommand"
)

//Convinience interface for building commands
type call func(...string) (interface{}, error)

//Template for printing strings
var SimpleTemplate = `{{.}}`

//commandBuilder builds commands in a reusable way
type commandBuilder struct {
	name     string //Command name
	desc     string //Command description
	linkCall call   //function to call in order to execute the command
	template string //Name of the template used to print the output
}

//Creates a new commandBuilder
func newCommandBuilder(name, desc string) *commandBuilder {
	return &commandBuilder{name: name, desc: desc, template: SimpleTemplate}
}

//Sets the call to be wrapped within the command
func (c *commandBuilder) withCall(fn call) *commandBuilder {
	c.linkCall = fn
	return c
}

//Sets the template to be used as command output
func (c *commandBuilder) withTemplate(template string) *commandBuilder {
	c.template = template
	return c
}

//builds the commands and adds it to the cli
func (c *commandBuilder) build(cli *Cli) (cmd *subcommand.Command) {
	return cli.AddCommand(c.name, c.desc, func(name string, args ...string) error {

		data, err := c.linkCall(args...)
		if err != nil {
			return err
		}
		return c.writeOutput(data, cli)
	})
}

//builds the commands and adds it to the cli
func (c *commandBuilder) buildAdmin(cli *Cli) (cmd *subcommand.Command) {
	return cli.AddAdminCommand(c.name, c.desc, func(name string, args ...string) error {

		data, err := c.linkCall(args...)
		if err != nil {
			return err
		}
		return c.writeOutput(data, cli)
	})
}

func (c commandBuilder) writeOutput(data interface{}, cli *Cli) error {
	tmpl := template.Must(template.New("template").Parse(c.template))
	if data != nil {
		err := tmpl.Execute(cli.Output, data)
		if err != nil {
			return err
		}
	}
	return nil
}

//Builds a command and configures it to expect a job id
func (c *commandBuilder) buildWithId(cli *Cli) (cmd *subcommand.Command) {
	lastId := new(bool)
	cmd = cli.AddCommand(c.name, c.desc, func(command string, args ...string) error {
		id, err := checkId(*lastId, command, args...)
		if err != nil {
			return err
		}
		data, err := c.linkCall(id)
		if err != nil {
			return err
		}
		return c.writeOutput(data, cli)
	})

	addLastId(cmd, lastId)
	return
}
