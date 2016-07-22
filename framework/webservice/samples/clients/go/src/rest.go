package main

// Takes care of all the rest calls
import (
  "net/http"
  "io/ioutil"
 // "net/url"
  "mime/multipart"
  "bytes"
  "io"
  "os"
  "fmt"
)


func get_resource(uri string) string {
    // TODO authentication
    println("GET", uri)
    var authuri = uri
    resp, err := http.Get(authuri)
    if resp == nil || err != nil {
        panic(err)
    }
    println("Response was ", resp.StatusCode)

    defer resp.Body.Close()
    body, _ := ioutil.ReadAll(resp.Body)
    return string(body)
}

func post_resource(uri string) {
    println("POST", uri)

    var jobrequest_filename = "/Users/marisa/Projects/pipeline2/pipeline-framework/webservice/samples/clients/testdata/job2.request.localmode.xml"
    var jobdata_filename = "/Users/marisa/Projects/pipeline2/pipeline-framework/webservice/samples/clients/testdata/job2.data.zip"

    body_buf := bytes.NewBufferString("") 
    body_writer := multipart.NewWriter(body_buf) 

    // read the XML and attach it to the multipart request body
    file_writer, err := body_writer.CreateFormFile("job-request", jobrequest_filename) 
    if err != nil { 
        panic(err)
    } 
    fh, err := os.Open(jobrequest_filename) 
    if err != nil { 
        panic(err)
    } 
    io.Copy(file_writer, fh) 

    // read the zip and attach it to the multipart request body
    zip_writer, err := body_writer.CreateFormFile("job-data", jobdata_filename) 
    if err != nil { 
        panic(err)
    } 
    zh, err := os.Open(jobdata_filename) 
    if err != nil { 
        panic(err)
    } 
    io.Copy(zip_writer, zh) 

    body_writer.Close() 
    
    var authuri = uri
    
    fmt.Println( string(body_buf.Bytes()) )

    // resp, err := http.Post(authuri, "bad/mime", body_buf) 
    // if resp == nil || err != nil {
    //     panic(err)
    // }
    
    // println("Response was", resp.StatusCode)

    defer resp.Body.Close()
}

func delete_resource(uri string) {
    var authuri = uri
    req, err := http.NewRequest("DELETE", authuri, nil)
    if err !=  nil {
        panic(err)
    }
    http.DefaultClient.Do(req)
    println ("DELETE ok",uri);
}
