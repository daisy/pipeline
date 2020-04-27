package subcommand

import (
	"fmt"
	"strings"
)

//Parser contains other commands. It's the data structure and its name should be the program's name.
type Parser struct {
	Command
	Commands map[string]*Command
	help     Command
}

//Sets the help command. There is one default implementation automatically added when the parser is created.
func (p *Parser) SetHelp(name string, description string, fn CommandFunction) *Command {
	command := newCommand(&p.Command, name, description, "", fn)
	p.help = *command
	return command

}

//First level execution when parsing. The passed function is exectued taking the leftovers until the first command
//./prog -switch left1 left2 command
//in this case name will be prog, and left overs left1 and left2
func (p *Parser) OnCommand(fn CommandFunction) {
	p.fn = fn
}

//Execute this function once the flags have been consumed. This can be used to dinamically
//add commands depending on the flags' state
func (p *Parser) PostFlags(fn func() error) {
	p.postFlagsFn = fn
}

//NewParser constructs a parser for program name given
func NewParser(program string) *Parser {
	parser := &Parser{
		Command:  *newCommand(nil, program, "", "", func(string, ...string) error { return nil }),
		Commands: make(map[string]*Command),
	}
	parser.Command.arity = Arity{0, ""}
	parser.SetHelp("help", fmt.Sprintf("Type %v help [command] for detailed information about a command", program), defaultHelp(*parser))
	return parser
}

//AddCommand inserts a new subcommand to the parser. The callback fn receives as first argument
//the command name followed by the left overs of the parsing process
//Example:
// command "hello" prints the non flags (options and switches) arguments.
// The associated callback should be something like
// func processCommand(commandName string,args ...string){
//      fmt.Printf("The command %v says:\n",commandName)
//      for _,arg:= rage args{
//              fmt.Printf("%v \n",arg)
//      }
//}
func (p *Parser) AddCommand(name string, shortDesc string, longDesc string, fn CommandFunction) *Command {
	if _, exists := p.Commands[name]; exists {
		panic(fmt.Sprintf("Command '%s' already exists ", name))
	}
	//create the command
	command := newCommand(&p.Command, name, shortDesc, longDesc, fn)
	//add it to the parser
	p.Commands[name] = command
	return command
}

//Parse parses the arguments executing the associated functions for each command and flag.
//It returns the left overs if some non-option strings or commands  were not processed.
//Errors are returned in case an unknown flag is found or a mandatory flag was not supplied.
// The set of function calls to be performed are carried in order and once the parsing process is done
func (p *Parser) Parse(args []string) (leftOvers []string, err error) {
	err = p.parse(args, p.Command)
	if err != nil {
		return
	}
	return
}

//The actual parsing process
func (p *Parser) parse(args []string, currentCommand Command) (err error) {
	//TODO : rewrite the parsing algorithm to make it a bit more clean and clever...
	//visited flags
	var flagsToCall []flagCallable
	var leftOvers []string
	var nextCommandCall func() error
	i := 0
	//functions to call once the parsing process is over
	//go comsuming options commands and sub-options
	for ; i < len(args); i++ {
		arg := args[i]
		if strings.HasPrefix(arg, "-") { //flag
			var fCallable flagCallable
			fCallable, i, err = currentCommand.parseFlag(args, i)
			flagsToCall = append(flagsToCall, fCallable)
			if err != nil {
				return
			}

		} else { //command or leftover
			//call the flags (make sure we call it just once
			if len(leftOvers) == 0 {
				if err = currentCommand.callFlags(flagsToCall); err != nil {
					return
				}
			}

			cmd, isCommand := p.Commands[arg]
			//if its a command or help
			if isHelp := (arg == p.help.Name); (isCommand || isHelp) && currentCommand.Name != p.help.Name {
				nextCommandCall = func() error {
					i := i
					if isHelp {
						cmd = &(p.help)
					}
					//call with the rest of the args
					err := p.parse(args[i+1:], *cmd)
					if err != nil {
						return err
					}
					return nil
				}

				break
			} else {
				leftOvers = append(leftOvers, arg)
			}

		}

	}
	//call the flags
	if nextCommandCall == nil && len(leftOvers) == 0 {
		if err = currentCommand.callFlags(flagsToCall); err != nil {
			return
		}
	}
	//call current command
	if err = currentCommand.exec(leftOvers, *p); err != nil {
		return
	}
	//look for next command
	if nextCommandCall != nil {
		return nextCommandCall()
	}
	return nil
}

//Execute the command function with leftovers as parameters
func (c Command) exec(leftOvers []string, p Parser) error {
	arity := c.Arity().Count
	//check correct number of params
	if arity != -1 && arity != len(leftOvers) {
		if c.Name == p.Command.Name {
			return c.errorf("%v: subcommand not found %v",
				c.Name, leftOvers[0])
		} else {
			return c.errorf("Arity: Command %s accepts %v parameters but %v found (%v)",
				c.Name, arity, len(leftOvers), leftOvers)
		}

	}
	if err := c.fn(c.Name, leftOvers...); err != nil {
		return err
	}
	return nil
}

//Call the each flag with the associated value
func (c Command) callFlags(flagsToCall []flagCallable) error {
	//check if we got all the mandatory flags
	if err := checkVisited(flagsToCall, c); err != nil {
		return err
	}
	//call flag functions
	for _, fc := range flagsToCall {
		if err := fc.fn(); err != nil {
			return err
		}

	}
	//call post flags
	return c.postFlagsFn()
}

//convinience lambda to pass the flag function around
func flagFunction(name, value string, fn FlagFunction) func() error {
	return func() error { return fn(name, value) }
}

//contains the flag and its fucntion ready to call
type flagCallable struct {
	fn   func() error
	flag Flag
}

//parses a flag and returns a flag callable to execute and the new position of the args iterator
func (c Command) parseFlag(args []string, pos int) (callable flagCallable, newPos int, err error) {
	arg := args[pos]
	newPos = pos
	var opt *Flag
	var ok bool
	var fn func() error
	//long or shor definition
	if strings.HasPrefix(arg, "--") {
		opt, ok = c.innerFlagsLong[arg[2:]]
	} else {
		opt, ok = c.innerFlagsShort[arg[1:]]
	}
	//not present
	if !ok {
		err = c.errorf("%v is not a valid flag for %v", arg, c.Name)
		return
	}

	if opt.Type == Option { //option
		if pos+1 >= len(args) {
			err = c.errorf("No value for option %v", arg)
			return
		}
		fn = flagFunction(opt.Long, args[pos+1], opt.fn)
		newPos = pos + 1
	} else { //switch
		fn = flagFunction(opt.Long, "", opt.fn)
	}
	callable = flagCallable{fn, *opt}
	return
}

//checks if the mandatory flags were visited
func checkVisited(visited []flagCallable, command Command) error {
	for _, flag := range command.Flags() {
		if flag.Mandatory {
			ok := false
			for _, vFlag := range visited {
				if vFlag.flag.Long == flag.Long {
					ok = true
					break
				}
			}
			if !ok {
				return command.errorf("option/switch --%v is mandatory for command %v", flag.Long, command.Name)
			}
		}
	}
	return nil
}

//convinience for creating parsing errors
func (c Command) errorf(format string, args ...interface{}) ParsingError {
	return ParsingError{fmt.Sprintf(format, args...), c}
}
