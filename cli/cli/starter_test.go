package cli

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"strings"
	"testing"

	"github.com/daisy/pipeline-clientlib-go"
)

type MockPinger struct {
	maxCalls int
	err      bool
	count    int
}

//Mocks the pipeline alive funcionality
func (p *MockPinger) Alive() (alive pipeline.Alive, err error) {
	if p.err {
		return alive, fmt.Errorf("error!")
	}
	if p.count < p.maxCalls {
		p.count += 1
		return alive, fmt.Errorf("error!")
	}
	return pipeline.Alive{Version: "test"}, err
}

//Tests the laucher creation
func TestNewPipelineLauncher(t *testing.T) {
	pinger := &MockPinger{}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	if launcher.pinger != pinger {
		t.Errorf("Pinger wasn't set")
	}
	if launcher.path != "pipeline2" {
		t.Errorf("Path wasn't set")
	}
	if launcher.timeup != 10 {
		t.Errorf("Time out wasn't set %v!=%v", 10, launcher.timeup)
	}
	if launcher.runner == nil {
		t.Errorf("Runner is not set")
	}
}

//Tests the command configuration (path)
func TestCommandPath(t *testing.T) {

	pinger := &MockPinger{}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	cmd := launcher.command()
	if cmd.Args[0] != "pipeline2" {
		t.Errorf("Path wasn't set in the command")
	}
}

//Tests the command has set the gogo shell no input configuration
//the JAVA_OPTS is missing from the environment
func TestCommandJavaOptsNew(t *testing.T) {

	pinger := &MockPinger{}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	cmd := launcher.command()
	//if java opts is actually present in the
	//system we have to skip this test
	for _, env := range os.Environ() {
		if strings.HasPrefix(env, JAVA_OPTS) {
			t.Skipf("JAVA_OPTS present in the system, can't fake that")
			return
		}
	}

	res := cmd.Env[len(cmd.Env)-1]
	expected := appendOpts(JAVA_OPTS + "=")
	if res != expected {
		t.Errorf("JAVA_OPTS wasn't appended")
	}

}

//Tests the command has set the gogo shell no input configuration
//the JAVA_OPTS is missing from the environment
func TestCommandJavaOptsAppend(t *testing.T) {

	pinger := &MockPinger{}
	val := "-Dcosa"
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	os.Setenv("JAVA_OPTS", val)
	cmd := launcher.command()

	var res string
	for idx, env := range cmd.Env {
		if strings.HasPrefix(env, JAVA_OPTS) {
			res = cmd.Env[idx]
		}
	}
	expected := appendOpts("JAVA_OPTS=" + val)
	if res != expected {
		t.Errorf("JAVA_OPTS wasn't appended")
	}

}

func TestAppendOps(t *testing.T) {
	//from empty variable
	res := appendOpts("JAVA_OPTS=")
	javaOptsEmpty := "JAVA_OPTS= " + OH_MY_GOSH
	if javaOptsEmpty != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "javaOptsEmpty ", javaOptsEmpty, res)
	}
	//non-empty no quotes
	res = appendOpts("JAVA_OPTS=-Dsomething")
	javaOptsNoQuotes := "JAVA_OPTS=-Dsomething " + OH_MY_GOSH
	if javaOptsNoQuotes != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "javaOptsNoQuotes ", javaOptsNoQuotes, res)
	}

	res = appendOpts("JAVA_OPTS=\"-Dsomething -Dandsthelse\"")
	javaOptsQuotes := "JAVA_OPTS=-Dsomething -Dandsthelse " + OH_MY_GOSH
	if javaOptsQuotes != res {
		t.Errorf("Wrong %v\n\tExpected: %v\n\tResult: %v", "javaOptsQuotes ", javaOptsQuotes, res)
	}
}

//Tests the wait method
func TestWait(t *testing.T) {
	pinger := &MockPinger{err: false, maxCalls: 2}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	chAlive := make(chan pipeline.Alive)
	chTries := make(chan int)
	go launcher.wait(chAlive, chTries)

	<-chTries
	nTries := <-chTries
	if nTries != 2 {
		t.Errorf("Two Retries")
	}
	alive := <-chAlive
	//if this doesn't work this woud panic...
	if alive.Version != "test" {
		t.Errorf("Alive doesn't seem to be correct %+v", alive)
	}

}

//Tests the launch method when calling the pipeline2 fails
func TestLauncherCmdFail(t *testing.T) {
	pinger := &MockPinger{err: false, maxCalls: 2}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	launcher.runner = func(*exec.Cmd) error {
		return fmt.Errorf("cmd error!")
	}
	bf := make([]byte, 0)
	w := bytes.NewBuffer(bf)
	_, err := launcher.Launch(w)
	if err == nil {
		t.Errorf("Expected error not returned")

	}

}

//Tests the launcher method and everything goes fine
func TestLauncherCmd(t *testing.T) {
	pinger := &MockPinger{err: false, maxCalls: 1}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 10)
	launcher.runner = func(*exec.Cmd) error {
		return nil
	}
	bf := make([]byte, 0)
	w := bytes.NewBuffer(bf)
	alive, err := launcher.Launch(w)
	if err != nil {
		t.Errorf("Unexpected error %v", err)
	}
	if alive.Version != "test" {
		t.Errorf("Alive doesn't seem to be correct %+v", alive)
	}

}

//Tests the launcher method timing out
func TestLauncherTimeUp(t *testing.T) {
	pinger := &MockPinger{err: true, maxCalls: 1}
	launcher := NewPipelineLauncher(pinger, "pipeline2", 1)
	launcher.runner = func(*exec.Cmd) error {
		return nil
	}
	bf := make([]byte, 0)
	w := bytes.NewBuffer(bf)
	_, err := launcher.Launch(w)
	if err == nil {
		t.Errorf("Launcher should've timed out")
	}

}
