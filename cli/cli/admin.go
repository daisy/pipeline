package cli

import (
	//"github.com/bertfrees/go-subcommand"
	"fmt"
	"text/template"

	"github.com/bertfrees/go-subcommand"
	"github.com/daisy/pipeline-clientlib-go"
)

const (
	TmplClients = `client_id	(role)
{{range .}}{{.Id}}	({{.Role}})
{{end}}
`
	TmplClient = `
Client id:      {{.Id}}
Role:           {{.Role}}
Contact:        {{.Contact}}
Secret:         ****
Priority:       {{.Priority}}

`
	TmplProperties = `Name          Value           Bundle
{{range .}}{{.Name}}	{{.Value}}	{{.BundleName}}
{{end}}
`
	TmplSizes = `JobId                 		Context Size    Output Size    Log Size    Total Size
{{range .}}{{.Id}}	{{format .Context}}	{{format .Output}}	{{format .Log}}	{{ total . | format}}
{{end}}

`
)

func (c *Cli) AddClientListCommand(link PipelineLink) {
	newCommandBuilder("list", "Returns the list of the available clients").
		withCall(func(args ...string) (interface{}, error) {
		return link.Clients()
	}).withTemplate(TmplClients).buildAdmin(c)
}

func (c *Cli) AddNewClientCommand(link PipelineLink) {
	client := &pipeline.Client{}
	fn := func(...string) (interface{}, error) {
		return link.NewClient(*client)
	}
	cmd := newCommandBuilder("create", "Creates a new client").
		withCall(fn).withTemplate(TmplClient).buildAdmin(c)

	cmd.AddOption("id", "i", "Client id (must be unique)", "", "", func(string, value string) error {
		client.Id = value
		return nil
	}).Must(true)
	addClientOptions(cmd, client, true)

}

func (c *Cli) AddDeleteClientCommand(link PipelineLink) {
	newCommandBuilder("remove", "Removes a client").
		withCall(func(args ...string) (v interface{}, err error) {
		id := args[0]
		_, err = link.DeleteClient(id)
		if err != nil {
			return
		}
		return fmt.Sprintf("Client %v removed\n", id), err
	}).
		buildAdmin(c).SetArity(1, "CLIENT_ID")
}

func (c *Cli) AddClientCommand(link PipelineLink) {
	fn := func(args ...string) (v interface{}, err error) {
		return link.Client(args[0])
	}

	newCommandBuilder("client", "Prints the detailed client information").
		withCall(fn).withTemplate(TmplClient).buildAdmin(c).SetArity(1, "CLIENT_ID")
}
func (c *Cli) AddModifyClientCommand(link PipelineLink) {
	client := &pipeline.Client{}
	fn := func(args ...string) (v interface{}, err error) {
		id := args[0]
		client.Id = id
		old, err := link.Client(id)
		if err != nil {
			return
		}
		if len(client.Secret) == 0 {
			client.Secret = old.Secret
		}
		if len(client.Role) == 0 {
			client.Role = old.Role
		}
		if len(client.Contact) == 0 {
			client.Contact = old.Contact
		}
		return link.ModifyClient(*client, id)
	}
	cmd := newCommandBuilder("modify", "Modifies a client").
		withCall(fn).withTemplate(TmplClient).buildAdmin(c)
	cmd.SetArity(1, "CLIENT_ID")
	addClientOptions(cmd, client, false)

}

//Adds the client options a part from the id
func addClientOptions(cmd *subcommand.Command, client *pipeline.Client, must bool) {
	cmd.AddOption("secret", "s", "Client secret", "", "", func(string, value string) error {
		client.Secret = value
		return nil
	}).Must(must)
	cmd.AddOption("role", "r", "Client role  (ADMIN,CLIENTAPP)", "", "",
		func(string, value string) error {
			if value != "ADMIN" && value != "CLIENTAPP" {
				return fmt.Errorf("%v is not a valid role", value)
			}
			client.Role = value
			return nil
		}).Must(must)
	cmd.AddOption("contact", "c", "Client e-mail address", "", "",
		func(string, value string) error {
			client.Contact = value
			return nil
		})
	cmd.AddOption("priority", "p", "Set the client priority", "", "low|medium|high",
		func(string, priority string) error {
			if checkPriority(priority) {
				client.Priority = priority
				return nil
			} else {
				return fmt.Errorf("%s is not a valid priority. Allowed values are high, medium and low",
					priority)
			}
		})
}

func (c *Cli) AddPropertyListCommand(link PipelineLink) {
	newCommandBuilder("properties", "List the pipeline ws runtime properties ").
		withCall(
		func(args ...string) (interface{}, error) {
			return link.Properties()
		}).
		withTemplate(TmplProperties).buildAdmin(c)
}

func (c *Cli) AddSizesCommand(link PipelineLink) {
	list := false
	unitFormatter := func(size int) string {
		return fmt.Sprintf("%d", size)
	}
	cmd := c.AddAdminCommand("sizes", "Prints the total size or a detailed list of job data stored in the server",
		func(command string, args ...string) error {
			sizes, err := link.Sizes()
			if err != nil {
				return err
			}
			if !list {
				c.Printf("Total %s\n", unitFormatter(sizes.Total))
			} else {
				funcMap := template.FuncMap{
					"format": unitFormatter,
					"total": func(size pipeline.JobSize) int {
						return size.Context + size.Output + size.Log
					},
				}
				tmpl := template.Must(template.New("sizes").Funcs(funcMap).Parse(TmplSizes))
				err = tmpl.Execute(c.Output, sizes.JobSizes)
			}

			return err
		})
	cmd.AddSwitch("list", "l", "Displays a detailed list rather than the total size", func(string, string) error {
		list = true
		return nil
	})
	cmd.AddSwitch("human", "h", "Use a more human readable size (megabytes)", func(string, string) error {
		unitFormatter = func(size int) string {
			return fmt.Sprintf("%.4fM", float64(size)/(1048576))
		}
		return nil
	})

}
