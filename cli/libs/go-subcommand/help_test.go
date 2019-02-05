package subcommand

import (
	"io/ioutil"
	"testing"
)

func TestGetLongestFlag(t *testing.T) {
	f1 := buildFlag("1234", "", "", func(string, string) error { return nil }, Option)
	f2 := buildFlag("1235", "a", "", func(string, string) error { return nil }, Option)
	//f2 is longer for the shot desc
	res := getLongestFlag([]Flag{*f1, *f2})
	if res != len(f2.FlagStringPrefix()) {
		t.Error("longest flag wasn't f2")
	}

	f3 := buildFlag("1236", "", "", func(string, string) error { return nil }, Switch)
	res = getLongestFlag([]Flag{*f1, *f3})
	//longest f1 for the [OPTION] part
	if res != len(f1.FlagStringPrefix()) {
		t.Error("longest flag wasn't f1")
	}
}

func TestFlagAligner(t *testing.T) {

	f1 := buildFlag("1234", "", "", func(string, string) error { return nil }, Option)
	f2 := buildFlag("1235", "a", "", func(string, string) error { return nil }, Option)
	aligner := flagAligner([]Flag{*f1, *f2})

	if len(aligner(f1.FlagStringPrefix())) != len(aligner(f2.FlagStringPrefix())) {
		t.Errorf("Aligner didn't align len(f1)=%v len(f2)=%v", len(aligner(f1.FlagStringPrefix())), len(aligner(f2.FlagStringPrefix())))
	}
}

func TestGetLongestName(t *testing.T) {
	parent := &Command{}
	command1 := newCommand(parent, "c1", "", func(string, ...string) error {
		return nil
	})
	command2 := newCommand(parent, "co2", "", func(string, ...string) error {
		return nil
	})

	res := getLongestName(map[string]*Command{command1.Name: command1, command2.Name: command2})
	if res != len(command2.Name) {
		t.Error("longest command  wasn't f1")
	}
}

func TestCommandAligner(t *testing.T) {
	parent := &Command{}
	command1 := newCommand(parent, "c1", "", func(string, ...string) error {
		return nil
	})
	command2 := newCommand(parent, "co2", "", func(string, ...string) error {
		return nil
	})
	aligner := commandAligner(map[string]*Command{command1.Name: command1, command2.Name: command2})
	if len(aligner(command1.Name)) != len(aligner(command2.Name)) {
		t.Errorf("Aligner didn't align len(c1)=%v len(c2)=%v", len(aligner(command1.Name)), len(aligner(command2.Name)))
	}
}

func TestHelp(t *testing.T) {
	//just to check that everything executes
	output = ioutil.Discard
	parser := NewParser("test")
	parser.AddOption("option", "o", "This is an option", "", "", func(name, val string) error {
		return nil
	})
	parser.AddCommand("command", "desc", func(string, ...string) error {
		return nil
	}).AddOption("cop", "", "", "", "", func(string, string) error {
		return nil
	})
	_, err := parser.Parse([]string{"help"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}

	_, err = parser.Parse([]string{"help", "command"})
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
}
