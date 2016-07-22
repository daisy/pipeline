package main


import (
    //"os"
    "flag"
)

func main() {
    println("Pipeline 2 client")

   // var command string
    //flag.StringVar(&command, "command", "default", "The command you want to run")
 
    flag.Parse()

    var command = flag.Arg(0)
    println(command)

    if command == "scripts" {
    	rs := get_resource(SCRIPTS_URI)
    	println(rs)
    } else if command == "jobs" {
    	rs := get_resource(JOBS_URI)
    	println(rs)
    } else if command == "new-job" {
        post_resource(JOBS_URI)
    }
}