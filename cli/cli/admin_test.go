package cli

import (
	"fmt"
	"regexp"
	"strings"
	"testing"

	"github.com/daisy/pipeline-clientlib-go"
)

//Test delete client command output
func TestDeleteClient(t *testing.T) {
	cli, link, _ := makeReturningCli(nil, t)
	r := overrideOutput(cli)
	cli.AddDeleteClientCommand(link)
	err := cli.Run([]string{"remove", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != DELETE_CLIENT_CALL {
		t.Errorf("delete client wasn't called")
	}

	result := string(r.Bytes())
	expected := "Client id removed\n"
	if result != string(expected) {
		t.Errorf("client delete error %s!=%s", string(expected), result)
	}
}

//Test delete client command id check
func TestDeleteClientNoId(t *testing.T) {
	cli, link, _ := makeReturningCli(nil, t)
	//r := overrideOutput(cli)
	cli.AddDeleteClientCommand(link)
	err := cli.Run([]string{"remove"})
	if err == nil {
		t.Errorf("Delete client needs an id")
	}
}

//Test delete client command id check
func TestDeleteClientError(t *testing.T) {
	cli, link, pipe := makeReturningCli(nil, t)
	pipe.failOnCall = DELETE_CLIENT_CALL
	//r := overrideOutput(cli)
	cli.AddDeleteClientCommand(link)
	err := cli.Run([]string{"remove", "nonexistent id"})
	if getCall(link) != DELETE_CLIENT_CALL {
		t.Errorf("delete client wasn't called")
	}
	if err == nil {
		t.Errorf("Link error")
	}
}

//Test a successful client creation
func TestNewClientCommand(t *testing.T) {
	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, pipe := makeReturningCli(*client, t)
	w := overrideOutput(cli)
	pipe.val = client
	cli.AddNewClientCommand(link)
	strArgs := fmt.Sprintf("create -i %s -s %s -c %s -r %s -p high", client.Id, client.Secret, client.Contact, client.Role)
	err := cli.Run(strings.Split(strArgs, " "))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != NEW_CLIENT_CALL {
		t.Errorf("NewClient wasn't called")
	}
	outs := checkMapLikeOutput(w)
	result := outs["Client id"]
	expected := client.Id
	if expected != result {
		t.Errorf("Id '%s'!='%s'", expected, result)
	}
	result = outs["Secret"]
	expected = "****"
	if expected != result {
		t.Errorf("Secret '%s'!='%s'", expected, result)
	}
	result = outs["Contact"]
	expected = client.Contact
	if expected != result {
		t.Errorf("Contact '%s'!='%s'", expected, result)
	}
	result = outs["Role"]
	expected = client.Role
	if expected != result {
		t.Errorf("Role '%s'!='%s'", expected, result)
	}
	result = outs["Priority"]
	expected = client.Priority
	if expected != result {
		t.Errorf("Priority'%s'!='%s'", expected, result)
	}
}

//Test new client with link error
func TestNewClientCommandError(t *testing.T) {
	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, pipe := makeReturningCli(*client, t)
	pipe.failOnCall = NEW_CLIENT_CALL
	cli.AddNewClientCommand(link)
	strArgs := fmt.Sprintf("create -i %s -s %s -c %s -r %s", client.Id, client.Secret, client.Contact, client.Role)
	err := cli.Run(strings.Split(strArgs, " "))
	if getCall(link) != NEW_CLIENT_CALL {
		t.Errorf("NewClient wasn't called")
	}
	if err == nil {
		t.Errorf("Link error not propagated")
	}
}

//Test new client different flags errors
func TestNewClientCommandFlagErrors(t *testing.T) {
	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, _ := makeReturningCli(client, t)
	cli.AddNewClientCommand(link)
	//no id
	err := cli.Run(strings.Split("create -s sh -c h@c.com -r ADMIN", " "))
	if err == nil {
		t.Errorf("No id error missing")
	}
	//no secret
	err = cli.Run(strings.Split("create -i id -c h@c.com -r ADMIN", " "))
	if err == nil {
		t.Errorf("No secret error missing")
	}
	//no role
	err = cli.Run(strings.Split("create -i id -s shh -c h@c.com", " "))
	if err == nil {
		t.Errorf("No role error missing")
	}
}

//Test new client different flags errors
func TestNewClientCommandRoles(t *testing.T) {
	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, _ := makeReturningCli(client, t)
	cli.AddNewClientCommand(link)
	//admin ok
	err := cli.Run(strings.Split("create -i id -s sh -c h@c.com -r ADMIN", " "))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	//clientapp ok
	err = cli.Run(strings.Split("create -i id -s sh -c h@c.com -r CLIENTAPP", " "))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	//other values
	err = cli.Run(strings.Split("create -i id -s sh -c h@c.com -r HAMLET", " "))
	if err == nil {
		t.Errorf("Hamlet is not a valid role error expected")
	}

}

//Tests the client command checking its output
func TestClientCommand(t *testing.T) {

	client := &pipeline.Client{
		Id:       "id",
		Secret:   "secret",
		Role:     "ADMIN",
		Contact:  "admin@localhost",
		Priority: "low",
	}
	cli, link, pipe := makeReturningCli(*client, t)
	w := overrideOutput(cli)
	pipe.val = client
	cli.AddClientCommand(link)
	err := cli.Run(strings.Split("client id", " "))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != CLIENT_CALL {
		t.Errorf("Client wasn't called")
	}
	outs := checkMapLikeOutput(w)
	result := outs["Client id"]
	expected := client.Id
	if expected != result {
		t.Errorf("Id '%s'!='%s'", expected, result)
	}
	result = outs["Secret"]
	expected = "****"
	if expected != result {
		t.Errorf("Secret '%s'!='%s'", expected, result)
	}
	result = outs["Contact"]
	expected = client.Contact
	if expected != result {
		t.Errorf("Contact '%s'!='%s'", expected, result)
	}
	result = outs["Role"]
	expected = client.Role
	if expected != result {
		t.Errorf("Role '%s'!='%s'", expected, result)
	}
	result = outs["Priority"]
	expected = client.Priority
	if expected != result {
		t.Errorf("Priority '%s'!='%s'", expected, result)
	}

}

//Test if an error is returned if the client id is missing
func TestClientCommandMissingId(t *testing.T) {

	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, _ := makeReturningCli(*client, t)
	cli.AddClientCommand(link)
	err := cli.Run(strings.Split("client", " "))
	if err == nil {
		t.Errorf("Id error missing")
	}

}

//Test if an error is returned from the link it's propagated
func TestClientCommandError(t *testing.T) {

	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, pipe := makeReturningCli(*client, t)
	pipe.failOnCall = CLIENT_CALL
	cli.AddClientCommand(link)
	err := cli.Run(strings.Split("client id", " "))
	if getCall(link) != CLIENT_CALL {
		t.Errorf("Client wasn't called")
	}
	if err == nil {
		t.Errorf("Link error missing")
	}

}

//Test if an error is returned if the client id is missing
func TestModifyClientCommandMissingId(t *testing.T) {

	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, _ := makeReturningCli(client, t)
	cli.AddModifyClientCommand(link)
	err := cli.Run(strings.Split("modify", " "))
	if err == nil {
		t.Errorf("Id error missing")
	}

}

//Test if an error is returned if the client id is missing
func TestModifyClientCommandClientNotFound(t *testing.T) {

	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, pipe := makeReturningCli(client, t)
	//fail when looking for the client
	pipe.failOnCall = CLIENT_CALL
	cli.AddModifyClientCommand(link)
	args := "modify id -s sh"
	err := cli.Run(strings.Split(args, " "))
	if getCall(link) != CLIENT_CALL {
		t.Errorf("Client wasn't called")
	}
	if err == nil {
		t.Errorf("No client found error missing")
	}

}

//Test modify client
func TestModifyClientCommand(t *testing.T) {

	client := &pipeline.Client{
		Id:       "id",
		Secret:   "secret",
		Role:     "ADMIN",
		Contact:  "admin@localhost",
		Priority: "low",
	}
	cli, link, _ := makeReturningCli(client, t)
	w := overrideOutput(cli)
	cli.AddModifyClientCommand(link)
	args := "modify -s noso -r CLIENTAPP -c other@localhost -p high id"
	err := cli.Run(strings.Split(args, " "))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != MODIFY_CLIENT_CALL {
		t.Errorf("Modify Client wasn't called")
	}

	outs := checkMapLikeOutput(w)
	result := outs["Secret"]
	expected := "****"
	if expected != result {
		t.Errorf("Secret '%s'!='%s'", expected, result)
	}
	result = outs["Contact"]
	expected = "other@localhost"
	if expected != result {
		t.Errorf("Contact '%s'!='%s'", expected, result)
	}
	result = outs["Role"]
	expected = "CLIENTAPP"
	if expected != result {
		t.Errorf("Role '%s'!='%s'", expected, result)
	}
	result = outs["Priority"]
	expected = "high"
	if expected != result {
		t.Errorf("Priority '%s'!='%s'", expected, result)
	}

}

//Test modify client without arguments leaves the same client
func TestModifyClientCommandNoModify(t *testing.T) {

	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, _ := makeReturningCli(client, t)
	w := overrideOutput(cli)
	cli.AddModifyClientCommand(link)
	args := "modify id"
	err := cli.Run(strings.Split(args, " "))
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != MODIFY_CLIENT_CALL {
		t.Errorf("Modify Client wasn't called")
	}

	outs := checkMapLikeOutput(w)
	result := outs["Secret"]
	expected := "****"
	if expected != result {
		t.Errorf("Secret '%s'!='%s'", expected, result)
	}
	result = outs["Contact"]
	expected = client.Contact
	if expected != result {
		t.Errorf("Contact '%s'!='%s'", expected, result)
	}
	result = outs["Role"]
	expected = client.Role
	if expected != result {
		t.Errorf("Role '%s'!='%s'", expected, result)
	}
}

//Test modify client when the modify client call to link breaks
func TestModifyClientCommandError(t *testing.T) {

	client := &pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}
	cli, link, pipe := makeReturningCli(client, t)
	pipe.failOnCall = MODIFY_CLIENT_CALL
	cli.AddModifyClientCommand(link)
	args := "modify id"
	err := cli.Run(strings.Split(args, " "))
	if getCall(link) != MODIFY_CLIENT_CALL {
		t.Errorf("Modify Client wasn't called")
	}
	if err == nil {
		t.Errorf("Expected error not returned")
	}

}

//Test client list
func TestClients(t *testing.T) {

	clients := []pipeline.Client{pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}}
	cli, link, _ := makeReturningCli(clients, t)
	w := overrideOutput(cli)
	cli.AddClientListCommand(link)
	err := cli.Run([]string{"list"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != LIST_CLIENT_CALL {
		t.Errorf("List client wasn't called")
	}
	exp := regexp.MustCompile("\\w+\\s+\\(\\w+\\)")
	matches := exp.FindAll(w.Bytes(), -1)
	if len(matches) != 2 {
		t.Errorf("The messages weren't printed output:\n%s", string(string(w.Bytes())))
	}
}

//Test client list when the link returns an error
func TestClientsError(t *testing.T) {

	clients := []pipeline.Client{pipeline.Client{
		Id:      "id",
		Secret:  "secret",
		Role:    "ADMIN",
		Contact: "admin@localhost",
	}}
	cli, link, pipe := makeReturningCli(clients, t)
	pipe.failOnCall = LIST_CLIENT_CALL
	cli.AddClientListCommand(link)
	err := cli.Run([]string{"list"})
	if getCall(link) != LIST_CLIENT_CALL {
		t.Errorf("List client wasn't called")
	}
	if err == nil {
		t.Errorf("Link error missing")
	}
}

//Test the properties list
func TestPropeties(t *testing.T) {

	props := []pipeline.Property{pipeline.Property{
		Name:       "org.daisy.pipeline.property",
		Value:      "secret",
		BundleName: "framework",
	}}
	cli, link, _ := makeReturningCli(props, t)
	r := overrideOutput(cli)
	cli.AddPropertyListCommand(link)
	err := cli.Run([]string{"properties"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != PROPERTIES_CALL {
		t.Errorf("List client wasn't called")
	}
	outputLine := []string{
		props[0].Name,
		props[0].Value,
		props[0].BundleName,
	}
	if ok, line, message := checkTableLine(r, "\t", outputLine); !ok {
		t.Errorf("Properties template doesn't match (%q,%s)\n%s", queueLine, line, message)
	}
}

func TestWithLinkError(t *testing.T) {

	props := []pipeline.Property{pipeline.Property{
		Name:       "org.daisy.pipeline.property",
		Value:      "secret",
		BundleName: "framework",
	}}
	cli, link, pipe := makeReturningCli(props, t)
	pipe.failOnCall = PROPERTIES_CALL
	cli.AddPropertyListCommand(link)
	err := cli.Run([]string{"properties"})
	if getCall(link) != PROPERTIES_CALL {
		t.Errorf("List client wasn't called")
	}
	if err == nil {
		t.Errorf("Expected error not returned")
	}
}

//Tests the sizes command
func TestSizesTotal(t *testing.T) {

	sizes := pipeline.JobSizes{
		Total: 10,
	}
	cli, link, _ := makeReturningCli(sizes, t)
	r := overrideOutput(cli)
	cli.AddSizesCommand(link)
	err := cli.Run([]string{"sizes"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != SIZES_CALL {
		t.Errorf("sizes wasn't called")
	}
	expected := "Total 10\n"
	res := r.String()
	if res != expected {
		t.Errorf("Wrong total '%s'!='%s'", expected, res)
	}
}

//Tests the sizes command when an error is returned from the link
func TestSizesTotalError(t *testing.T) {

	sizes := pipeline.JobSizes{
		Total: 10,
	}
	cli, link, pipe := makeReturningCli(sizes, t)
	pipe.failOnCall = SIZES_CALL
	cli.AddSizesCommand(link)
	err := cli.Run([]string{"sizes"})
	if getCall(link) != SIZES_CALL {
		t.Errorf("sizes wasn't called")
	}
	if err == nil {
		t.Errorf("Link error not propagated")
	}
}

//Tests the sizes command printing the total as M
func TestSizesTotalFormat(t *testing.T) {

	sizes := pipeline.JobSizes{
		Total: 1048576, //1M
	}
	cli, link, _ := makeReturningCli(sizes, t)
	r := overrideOutput(cli)
	cli.AddSizesCommand(link)
	err := cli.Run([]string{"sizes", "-h"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != SIZES_CALL {
		t.Errorf("sizes wasn't called")
	}
	expected := "Total 1.0000M\n"
	res := r.String()
	if res != expected {
		t.Errorf("Wrong total '%s'!='%s'", expected, res)
	}
}

//Sizes list
func TestSizesList(t *testing.T) {
	sizes := pipeline.JobSizes{
		JobSizes: []pipeline.JobSize{
			pipeline.JobSize{
				Id:      "id",
				Context: 2,
				Output:  3,
				Log:     3,
			},
		},
		Total: 10,
	}
	cli, link, _ := makeReturningCli(sizes, t)
	r := overrideOutput(cli)
	cli.AddSizesCommand(link)
	err := cli.Run([]string{"sizes", "-l"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if getCall(link) != SIZES_CALL {
		t.Errorf("sizes wasn't called")
	}
	outputLine := []string{
		sizes.JobSizes[0].Id,
		fmt.Sprintf("%d", sizes.JobSizes[0].Context),
		fmt.Sprintf("%d", sizes.JobSizes[0].Output),
		fmt.Sprintf("%d", sizes.JobSizes[0].Log),
		"8",
	}
	if ok, line, message := checkTableLine(r, "\t", outputLine); !ok {
		t.Errorf("Sizes list doesn't match (%q,%s)\n%s", outputLine, line, message)
	}
}
