//Package subcommand is an option parsing utility a la git/mercurial/go loosely
//inspired by Ruby's OptionParser
package subcommand

import (
	"fmt"
)

//Convinience type for funcions passed to commands
type CommandFunction func(string, ...string) error

//Command aggregates different flags under a common name. Every time a command is found during the parsing process the associated function is executed.
type Command struct {
	//Name
	Name            string
	ShortDesc       string //Command help line
	LongDesc        string
	innerFlagsLong  map[string]*Flag
	innerFlagsShort map[string]*Flag
	orderedFlags    []*Flag //so we keep the order of the flags
	fn              CommandFunction
	postFlagsFn     func() error
	parent          *Command
	arity           Arity
}

//Access to flags
type Flagged interface {
	Flags() []Flag
}

//getFlags returns a slice containing the c's flags
func (c *Command) Flags() []Flag {
	//return c.Name
	flags := make([]Flag, 0)
	for _, val := range c.orderedFlags {
		flags = append(flags, *val)
	}
	return flags
}

func (c *Command) MandatoryFlags() []Flag {
	flags := make([]Flag, 0)
	for _, val := range c.orderedFlags {
		if val.Mandatory {
			flags = append(flags, *val)
		}
	}
	return flags
}

func (c *Command) NonMandatoryFlags() []Flag {
	flags := make([]Flag, 0)
	for _, val := range c.orderedFlags {
		if !val.Mandatory {
			flags = append(flags, *val)
		}
	}
	return flags
}

//Returns the command parent
func (c Command) Parent() *Command {
	return c.parent
}

func newCommand(parent *Command, name string, shortDesc string, longDesc string, fn CommandFunction) *Command {
	if longDesc == "" {
		longDesc = shortDesc
	}
	return &Command{
		Name:            name,
		innerFlagsShort: make(map[string]*Flag),
		innerFlagsLong:  make(map[string]*Flag),
		fn:              fn,
		postFlagsFn:     func() error { return nil },
		ShortDesc :      shortDesc,
		LongDesc :       longDesc,
		parent:          parent,
		arity:           Arity{-1, "arg1 arg2 ..."},
	}
}

//Adds a new option to the command to be used as "--option OPTION" (expects a value after the flag) in the command line
//The short definition has no length restriction but it should be significantly shorter that its long counterpart, it can be an empty string.
//The function fn receives the name of the option and its value
//Example:
//command.AddOption("path","p",setPath)//option
//[...]
// func setPath(option,value string){
//      printf("According the option %v the path is set to %v",option,value);
//}
func (c *Command) AddOption(long, short, shortDesc, longDesc, values string, fn FlagFunction) *Flag {
	flag := buildFlag(long, short, shortDesc, longDesc, values, fn, Option)
	c.addFlag(flag)
	return flag
}

//Adds a new switch to the command to be used as "--switch" (expects no value after the flag) in the command line
//The short definition has no length restriction but it should be significantly shorter that its long counterpart, it can be an empty string.
//The function fn receives two strings, the first is the switch name and the second is just an empty string
//Example:
//command.AddSwitch("verbose","v",setVerbose)//option
//[...]
// func setVerbose(switch string){
//      printf("I'm get to get quite talkative! I'm set to be %v ",switch);
//}
func (c *Command) AddSwitch(long string, short string, shortDesc string, fn FlagFunction) *Flag {
	flag := buildFlag(long, short, shortDesc, "", "", fn, Switch)
	c.addFlag(flag)
	return flag
}

type Arity struct {
	Count       int
	Description string
}

//Set arity:
//-1 accepts infinite arguments.
//Other restricts the arity to the given num
func (c *Command) SetArity(arity int, description string) *Command {
	c.arity = Arity{arity, description}
	return c
}

func (c Command) Arity() Arity {
	return c.arity
}

//Adds a flag to the command
func (c *Command) addFlag(flag *Flag) {

	if _, exists := c.innerFlagsLong[flag.Long]; exists {
		panic(fmt.Errorf("Flag '%s' already exists ", flag.Long))
	}
	if _, exists := c.innerFlagsShort[flag.Short]; exists {
		panic(fmt.Errorf("Flag '%s' already exists ", flag.Short))
	}
	c.innerFlagsLong[flag.Long] = flag
	c.orderedFlags = append(c.orderedFlags, flag)
	if flag.Short != "" {
		c.innerFlagsShort[flag.Short] = flag
	}

}
