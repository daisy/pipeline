package subcommand

type ParsingError struct {
	Description string
	Command     Command
}

func (e ParsingError) Error() string {
	return e.Description
}
