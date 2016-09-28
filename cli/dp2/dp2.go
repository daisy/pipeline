package main

import (
	"fmt"
	"log"
	"os"

	"github.com/daisy/pipeline-cli-go/cli"
)

var minJavaVersion = 1.7

func main() {
	log.SetFlags(log.Lshortfile)
	cnf := cli.NewConfig()
	if cnf[cli.STARTING].(bool) {
		println("Checking java")
		if err := cli.AssertJava(minJavaVersion); err != nil {
			fmt.Printf(
				"Java version error:\n\tPlease make sure that java is accessible and the version is equal or greater than %v\n\tError: %s\n",
				minJavaVersion,
				err.Error(),
			)
			os.Exit(-1)
		}
	}
	// proper error handlign missing

	link := cli.NewLink(cnf)
	comm, err := cli.NewCli("dp2", link)

	if err != nil {
		fmt.Printf("Error creating client:\n\t%v\n", err)
		os.Exit(-1)
	}

	cli.AddJobStatusCommand(comm, *link)
	cli.AddDeleteCommand(comm, *link)
	cli.AddResultsCommand(comm, *link)
	cli.AddJobsCommand(comm, *link)
	cli.AddLogCommand(comm, *link)
	cli.AddQueueCommand(comm, *link)
	cli.AddMoveUpCommand(comm, *link)
	cli.AddMoveDownCommand(comm, *link)
	cli.AddCleanCommand(comm, *link)
	cli.AddHaltCommand(comm, *link)
	cli.AddVersionCommand(comm, link)
	//admin commands
	comm.AddClientListCommand(*link)
	comm.AddNewClientCommand(*link)
	comm.AddDeleteClientCommand(*link)
	comm.AddModifyClientCommand(*link)
	comm.AddClientCommand(*link)
	comm.AddPropertyListCommand(*link)
	comm.AddSizesCommand(*link)

	err = comm.Run(os.Args[1:])
	if err != nil {
		fmt.Printf("Error:\n\t%v\n", err)
		os.Exit(-1)
	}
}
