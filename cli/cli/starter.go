package cli

import (
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/daisy/pipeline-clientlib-go"
)

const (
	//JAVA_OPTS variable name
	JAVA_OPTS = "JAVA_OPTS"
	//Tell gogo shell to not expect for input
	OH_MY_GOSH = "-Dgosh.args=--noi"
)

//Holds the needed information to launch the pipeline
type Launcher struct {
	pinger Pinger                //An object to ask about the status of the ws
	timeup int                   //The time up to wait for it
	path   string                //Path to the pipeline executable
	runner func(*exec.Cmd) error //A function to start the command (only modifiable for testing)
}

//Interface that allows to check if the ws is up
type Pinger interface {
	Alive() (alive pipeline.Alive, err error)
}

//Wraps a call to cmd.Start
func execRunner(cmd *exec.Cmd) error {
	return cmd.Start()
}

//Creates a new launcher
func NewPipelineLauncher(p Pinger, path string, timeup int) Launcher {
	return Launcher{
		pinger: p,
		timeup: timeup,
		path:   path,
		runner: execRunner,
	}
}

//Configures the command to be exected
func (l Launcher) command() *exec.Cmd {
	path := filepath.FromSlash(l.path)
	log.Printf("command path %v\n", path)
	cmd := exec.Command(path)
	cmd.Env = os.Environ()
	found := false
	for idx, env := range cmd.Env {
		if strings.HasPrefix(env, JAVA_OPTS) {
			found = true
			cmd.Env[idx] = appendOpts(env)
		}
	}
	if !found {
		cmd.Env = append(cmd.Env, appendOpts(JAVA_OPTS+"="))
	}
	cmd.Stdin, cmd.Stdout, cmd.Stderr = nil, nil, nil
	return cmd
}

//Waits for the pipeline
func (l Launcher) wait(cAlive chan pipeline.Alive, tries chan int) {
	log.Println("Calling alive")
	triesCnt := 1
	for {
		alive, err := l.pinger.Alive()
		if err != nil {
			log.Printf("retrying...")
			time.Sleep(333 * time.Millisecond)
			tries <- triesCnt
			triesCnt += 1
		} else {
			cAlive <- alive
			break
		}
	}

}

//Launches the pipeline writing the output messages to the supplied
func (l Launcher) Launch(w io.Writer) (alive pipeline.Alive, err error) {
	log.Println("Starting the fwk")
	//launch the ws
	cmd := l.command()
	err = l.runner(cmd)
	if err != nil {
		log.Println("Error running the command")
		return
	}
	//wait til it's up and running
	timeOut := time.After(time.Duration(l.timeup) * time.Second)
	////communication
	aliveChan := make(chan pipeline.Alive)
	triesChan := make(chan int)
	fmt.Fprintln(w, "Launching the pipeline webservice...")
	go l.wait(aliveChan, triesChan)
	for {
		select {
		case alive = <-aliveChan:
			fmt.Fprintln(w, "The webservice is UP!")
			log.Println("The ws seems to be up")
			return
		case tries := <-triesChan:
			log.Printf("Trying to dial to the ws (%v)\n", tries)
		//keep on going
		case <-timeOut:
			log.Println("launcher timed up")
			err = fmt.Errorf("I have been waiting %v seconds for the WS to come up but it did not", l.timeup)
			return
		}
	}
}

//Appends the gogo shell ignore input directive
//to the JAVA_OPS
func appendOpts(javaOptsVar string) string {
	//just the value
	val := strings.TrimLeft(javaOptsVar, JAVA_OPTS+"=")
	val = strings.Trim(val, `"`)
	result := val + " " + OH_MY_GOSH
	return JAVA_OPTS + `=` + result
}
