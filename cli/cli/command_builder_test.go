package cli

import (
	"fmt"
	"testing"
)

var testTmpl = `Out: {{printf "%q" .}}`

//Test that the values in the builder are correct
func TestCommandBuilderCommandConfig(t *testing.T) {
	pipe := newPipelineTest(false)
	link := PipelineLink{pipeline: pipe}
	cmdBuilder := newCommandBuilder("name", "desc")
	fn := func(...string) (interface{}, error) { return link.Queue() }
	cmdBuilder.withCall(fn)
	cmdBuilder.withTemplate(testTmpl)
	if cmdBuilder.linkCall == nil {
		t.Errorf("Function is not set")
	}
	if cmdBuilder.template != testTmpl {
		t.Errorf("Template is not set")
	}

}

//Tests that the builder command executes correctly
func TestBuilderCommand(t *testing.T) {
	pipe := newPipelineTest(false)
	link := PipelineLink{pipeline: pipe}
	cli, err := makeCli("test", &link)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	w := overrideOutput(cli)

	cmdBuilder := newCommandBuilder("test_cmd", "cmd")
	fn := func(...string) (interface{}, error) {
		return "Hello", nil
	}
	cmdBuilder.withCall(fn)
	cmdBuilder.withTemplate(testTmpl)
	cmdBuilder.build(cli)

	err = cli.Run([]string{"test_cmd"})
	if err != nil {
		t.Errorf("Expected error not thrown")
	}
	expected := "Out: Hello"
	if string(w.Bytes()) == expected {
		t.Errorf("Template didn't get the expected output %s != %s", expected, string(w.Bytes()))
	}
}

//Tests that the builder command treats errors
func TestBuilderCommandError(t *testing.T) {
	pipe := newPipelineTest(false)
	link := PipelineLink{pipeline: pipe}
	cli, err := makeCli("test", &link)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	overrideOutput(cli)

	cmdBuilder := newCommandBuilder("test_cmd", "cmd")
	fn := func(...string) (val interface{}, err error) {
		return nil, fmt.Errorf("Error!")
	}
	cmdBuilder.withCall(fn)
	cmdBuilder.withTemplate(testTmpl)
	cmdBuilder.build(cli)

	err = cli.Run([]string{"test_cmd"})
	if err == nil {
		t.Errorf("Expected error not thrown")
		return
	}
	if err.Error() != "Error!" {
		t.Errorf("The error we got in not the expected %q", err.Error())
	}
}

//Tests that the builder command adds the id checks when required
func TestBuilderCommandWithId(t *testing.T) {
	pipe := newPipelineTest(false)

	link := PipelineLink{pipeline: pipe}

	cmdBuilder := newCommandBuilder("status", "gets the status")
	fn := func(args ...string) (interface{}, error) {
		//this gets the id
		return args[0], nil
	}
	cmdBuilder.withCall(fn)
	cmdBuilder.withTemplate(testTmpl)
	cli, err := makeCli("test", &link)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	w := overrideOutput(cli)
	cmdBuilder.buildWithId(cli)
	//with id
	err = cli.Run([]string{"status", "id"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	expected := "Out: id"
	result := string(w.Bytes())
	if expected == result {
		t.Errorf("WithId output doesnt match %s!=%s", expected, result)
	}
	//No id means error
	err = cli.Run([]string{"status"})
	if err == nil {
		t.Errorf("Expected error not thrown")
	}
}
