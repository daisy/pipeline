package main

import (
	"fmt"
	"io"
	"os"
)

var Output io.Writer = os.Stdout

func Info(format string, vals ...interface{}) {
	fmt.Fprintf(Output, fmt.Sprintf("[INFO] %s\n", format), vals...)
}
func Error(format string, vals ...interface{}) {
	fmt.Fprintf(Output, fmt.Sprintf("[ERROR] %s\n", format), vals...)
}
