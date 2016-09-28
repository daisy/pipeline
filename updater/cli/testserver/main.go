package main

import "net/http"

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		println("Serving")
		http.ServeFile(w, r, "releaseDescriptor.xml")

	})
	println("Listening")
	panic(http.ListenAndServe(":9090", nil))
}
