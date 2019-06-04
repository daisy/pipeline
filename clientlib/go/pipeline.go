package pipeline

import (
	"fmt"
	"io"
	"log"
	"math"
	"encoding/xml"
	"strings"
	"errors"

	"github.com/capitancambio/restclient"
)

//Available api entry names
const (
	API_ALIVE        = "alive"
	API_SCRIPT       = "script"
	API_SCRIPTS      = "scripts"
	API_DATATYPE     = "datatype"
	API_JOBREQUEST   = "jobRequest"
	API_JOB          = "job"
	API_JOBS         = "jobs"
	API_BATCH        = "batch"
	API_DEL_JOB      = "del_job"
	API_DEL_BATCH    = "del_batch"
	API_RESULT       = "results"
	API_LOG          = "log"
	API_HALT         = "halt"
	API_CLIENTS      = "clients"
	API_NEWCLIENT    = "new_client"
	API_CLIENT       = "client"
	API_DELETECLIENT = "delete_client"
	API_MODIFYCLIENT = "modify_client"
	API_PROPERTIES   = "properties"
	API_SIZE         = "size"
	API_QUEUE        = "queue"
	API_MOVE_UP      = "move_up"
	API_MOVE_DOWN    = "move_down"
)

//Defines the information for an api entry
type apiEntry struct {
	urlPath  string
	method   string
	okStatus int
}

//Available api entries
var apiEntries = map[string]apiEntry{
	API_ALIVE:        apiEntry{"alive", "GET", 200},
	API_SCRIPTS:      apiEntry{"scripts", "GET", 200},
	API_SCRIPT:       apiEntry{"scripts/%v", "GET", 200},
	API_DATATYPE:     apiEntry{"datatypes/%v", "GET", 200},
	API_JOBREQUEST:   apiEntry{"jobs", "POST", 201},
	API_JOB:          apiEntry{"jobs/%v?msgSeq=%v", "GET", 200},
	API_DEL_JOB:      apiEntry{"jobs/%v", "DELETE", 204},
	API_RESULT:       apiEntry{"jobs/%v/result", "GET", 200},
	API_JOBS:         apiEntry{"jobs", "GET", 200},
	API_QUEUE:        apiEntry{"queue", "GET", 200},
	API_MOVE_UP:      apiEntry{"queue/up/%v", "GET", 200},
	API_MOVE_DOWN:    apiEntry{"queue/down/%v", "GET", 200},
	API_LOG:          apiEntry{"jobs/%v/log", "GET", 200},
	API_HALT:         apiEntry{"admin/halt/%v", "GET", 204},
	API_CLIENTS:      apiEntry{"admin/clients", "GET", 200},
	API_NEWCLIENT:    apiEntry{"admin/clients", "POST", 201},
	API_CLIENT:       apiEntry{"admin/clients/%v", "GET", 200},
	API_DELETECLIENT: apiEntry{"admin/clients/%v", "DELETE", 204},
	API_MODIFYCLIENT: apiEntry{"admin/clients/%v", "PUT", 200},
	API_PROPERTIES:   apiEntry{"admin/properties", "GET", 200},
	API_SIZE:         apiEntry{"admin/sizes", "GET", 200},
	API_BATCH:        apiEntry{"batch/%v", "GET", 200},
	API_DEL_BATCH:    apiEntry{"batch/%v", "DELETE", 204},
}

//Pipeline struct stores different configuration paramenters
//for the communication with the pipeline framework
type Pipeline struct {
	BaseUrl       string                                //baseurl of the framework
	clientMaker   func() doer                           //client to perform the rest queries
	authenticator func(req *restclient.RequestResponse) //authentication function
}

func NewPipeline(baseUrl string) *Pipeline {
	return &Pipeline{
		BaseUrl:       baseUrl,
		authenticator: func(*restclient.RequestResponse) {},
		clientMaker:   newClient,
	}
}

func (p *Pipeline) SetCredentials(clientKey, clientSecret string) {
	p.authenticator = authenticator(clientKey, clientSecret)
}

func (p *Pipeline) SetUrl(url string) {
	p.BaseUrl = url
}

//Returns a simple string representation of the Alive struct in the format:
//Alive:[#authentication:value #mode:value #version:value]
func (a Alive) String() string {
	return fmt.Sprintf("Alive:[#authentication:%v #fsallow:%v #version:%v]", a.Authentication, a.FsAllow, a.Version)
}

//Calls the alive api entry
//TODO link to wiki
func (p Pipeline) Alive() (alive Alive, err error) {
	req := p.newResquest(API_ALIVE, &alive, nil)
	_, err = p.do(req, defaultErrorHandler())
	return
}

//Returns the list of available scripts
func (p Pipeline) Scripts() (scripts Scripts, err error) {
	req := p.newResquest(API_SCRIPTS, &scripts, nil)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	for _, script := range scripts.Scripts {
		err = processInputsAndOptions(p, &script)
		if err != nil {
			return
		}
	}
	return
}

//Returns the script for a given script id
func (p Pipeline) Script(id string) (script Script, err error) {
	req := p.newResquest(API_SCRIPT, &script, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{404: "Script " + id + " not found"}))
	if err != nil {
		return
	}
	err = processInputsAndOptions(p, &script)
	return
}

func processInputsAndOptions(p Pipeline, script *Script) (err error) {
	for i, input := range script.Inputs {
		desc := strings.Split(input.LongDesc, "\n")
		script.Inputs[i].ShortDesc = desc[0]
		// insert empty line after first line
		if len(desc) > 1 {
			if desc[1] != "" {
				desc = append(desc[:1], append([]string{""}, desc[1:]...)...)
			}
			script.Inputs[i].LongDesc = strings.Join(desc, "\n")
		}
	}
	for i, option := range script.Options {
		desc := strings.Split(option.LongDesc, "\n")
		script.Options[i].ShortDesc = desc[0]
		// insert empty line after first line
		if len(desc) > 1 {
			if desc[1] != "" {
				desc = append(desc[:1], append([]string{""}, desc[1:]...)...)
			}
			script.Options[i].LongDesc = strings.Join(desc, "\n")
		}
		// get data type definition
		var optionType DataType
		if option.TypeAttr != "" {
			if option.TypeAttr == "integer" || option.TypeAttr == "xs:integer" {
				optionType = XsInteger{
					XmlDefinition: "<data type=\"integer\"/>"}
			} else if option.TypeAttr == "boolean" || option.TypeAttr == "xs:boolean" {
				optionType = XsBoolean{
					XmlDefinition: "<data type=\"boolean\"/>"}
			} else if option.TypeAttr == "anyURI" || option.TypeAttr == "xs:anyURI" {
				optionType = XsAnyURI{
					XmlDefinition: "<data type=\"anyURI\"/>"}
			} else if option.TypeAttr == "anyFileURI" {
				optionType = AnyFileURI{
					XmlDefinition: "<data type=\"anyFileURI\" datatypeLibrary=\"http://www.daisy.org/ns/pipeline/xproc\"/>"}
			} else if option.TypeAttr == "anyDirURI" {
				optionType = AnyDirURI{
					XmlDefinition: "<data type=\"anyDirURI\" datatypeLibrary=\"http://www.daisy.org/ns/pipeline/xproc\"/>"}
			} else if option.TypeAttr == "string" || option.TypeAttr == "xs:string" {
				optionType = XsString{
					XmlDefinition: "<data type=\"string\"/>"}
			} else {
				optionType, err = p.dataType(option.TypeAttr)
			}
		} else {
			optionType = XsString{
				XmlDefinition: "<data type=\"string\"/>"}
		}
		script.Options[i].Type = optionType
	}
	return
}

//Returns the url for a given script id
func (p Pipeline) ScriptUrl(id string) string {
	//This should call the server, but it just would add more overhead
	//so it's computed here
	req := p.newResquest(API_SCRIPT, nil, nil, id)
	return req.Url
}

// the datatype (relaxng) xml
// text nodes that have sibling elements are ignored, except inside a documentation element,
// where the content is treated as text content (markdown)
type datatypeXmlElement struct {
	XMLName      xml.Name
	Attrs        []xml.Attr            `xml:",any,attr"`
	ChildNodes   []datatypeXmlElement  `xml:",any"`
	TextContent  string                `xml:",innerxml"`
}

func (e *datatypeXmlElement) UnmarshalXML(d *xml.Decoder, start xml.StartElement) (err error) {
	type elem datatypeXmlElement
	err = d.DecodeElement((*elem)(e), &start)
	if err != nil {
		return
	}
	e.Attrs = nil
	for _, a := range start.Attr {
		if (a.Name.Space != "xmlns" && !(a.Name.Space == "" && a.Name.Local == "xmlns")) {
			e.Attrs = append(e.Attrs, a)
		}
	}
	if e.XMLName.Local == "documentation" {
		e.ChildNodes = nil
	} else if len(e.ChildNodes) > 0 {
		e.TextContent = ""
	}
	return
}

func (p Pipeline) dataType(id string) (datatype DataType, err error) {
	xmlDefinition := new(datatypeXmlElement)
	req := p.newResquest(API_DATATYPE, &xmlDefinition, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{404: "Data type " + id + " not found"}))
	if err != nil {
		return
	}
	datatype, err = parseDatatypeXmlDefinition(xmlDefinition, "")
	return
}

func parseDatatypeXmlDefinition(definition *datatypeXmlElement, documentation string) (result DataType, err error) {
	var bytes []byte
	bytes, err = xml.Marshal(definition)
	if err != nil {
		return
	}
	serialized := string(bytes)
	switch definition.XMLName.Local {
	case "data":
		var documentation string
		var pattern string
		for _, child := range definition.ChildNodes {
			switch child.XMLName.Local {
			case "documentation":
				if documentation != "" {
					goto parseError
				}
				if pattern != "" {
					// documentation must come before param
					goto parseError
				}
				documentation = child.TextContent
			case "param":
				if !child.hasAttr("name", "pattern") {
					goto parseError
				}
				if pattern != "" {
					goto parseError
				}
				if !definition.hasAttr("type", "string") {
					goto parseError
				}
				pattern = child.TextContent
			default:
				goto parseError
			}
		}
		if pattern != "" {
			result = Pattern{
				XmlDefinition: serialized,
				Pattern: pattern,
				Documentation: documentation}
			return
		}
		typeAttr, ok := definition.getAttr("type")
		if !ok {
			goto parseError
		}
		switch typeAttr {
		case "string":
			result = XsString{
				XmlDefinition: serialized,
				Documentation: documentation}
			return
		case "integer":
			result = XsInteger{
				XmlDefinition: serialized,
				Documentation: documentation}
			return
		case "boolean":
			result = XsBoolean{
				XmlDefinition: serialized,
				Documentation: documentation}
			return
		case "anyURI":
			result = XsAnyURI{
				XmlDefinition: serialized,
				Documentation: documentation}
			return
		case "anyFileURI":
			result = AnyFileURI{
				XmlDefinition: serialized,
				Documentation: documentation}
			return
		case "anyDirURI":
			result = AnyDirURI{
				XmlDefinition: serialized,
				Documentation: documentation}
			return
		default:
			goto parseError
		}
	case "choice":
		var choices []DataType
		if documentation != "" {
			goto parseError
		}
		for i, child := range definition.ChildNodes {
			if child.XMLName.Local == "documentation" {
				if i == 0 || definition.ChildNodes[i-1].XMLName.Local == "documentation" {
					goto parseError
				}
			} else {
				if len(definition.ChildNodes) > i + 1 && definition.ChildNodes[i+1].XMLName.Local == "documentation" {
					documentation = definition.ChildNodes[i+1].TextContent
				} else {
					documentation = ""
				}
				var choice DataType
				switch child.XMLName.Local {
				case "value":
					choice = Value{
						XmlDefinition: serialized,
						Documentation: documentation,
						Value: child.TextContent}
				default:
					choice, err = parseDatatypeXmlDefinition(&child, documentation)
					if err != nil {
						return
					}
				}
				choices = append(choices, choice)
			}
		}
		result = Choice{
			XmlDefinition: serialized,
			Values: choices}
		return
	default:
		goto parseError
	}
parseError:
	err = errors.New("invalid datatype xml: " + serialized)
	return
}

func (elem *datatypeXmlElement) getAttr(name string) (value string, present bool) {
	for _, attr := range elem.Attrs {
		if attr.Name.Local == name {
			return attr.Value, true
		}
	}
	return "", false
}

func (elem *datatypeXmlElement) hasAttr(name string, value string) bool {
	for _, attr := range elem.Attrs {
		if attr.Name.Local == name {
			return attr.Value == value
		}
	}
	return false
}

//Overrides the xml decoder to get raw data
func multipartResultClientMaker(p Pipeline) func() doer {
	return func() doer {
		cli := p.clientMaker()
		//change the default encodersuppier by the multipart
		cli.SetEncoderSupplier(func(r io.Writer) restclient.Encoder {
			return NewMultipartEncoder(r)
		})
		cli.SetContentType("multipart/form-data; boundary=" + boundary)
		return cli
	}
}

//Specific multipart request
func buildMultipartReq(jobReq JobRequest, data []byte) *MultipartData {
	return &MultipartData{
		data:    RawData{&data},
		request: jobReq,
	}
}

//Sends a JobRequest to the server
func (p Pipeline) JobRequest(newJob JobRequest, data []byte) (job Job, err error) {
	var reqData interface{} = &newJob
	log.Println("data len request ", len(data))
	//check if we have data
	if len(data) > 0 {
		log.Println("Sending multipart job request")
		p.clientMaker = multipartResultClientMaker(p)
		reqData = buildMultipartReq(newJob, data)
	}
	log.Println("Sending job request")
	log.Println(newJob.Script.Id)
	req := p.newResquest(API_JOBREQUEST, &job, reqData)
	_, err = p.do(req, errorHandler(map[int]string{
		400: "Job request is not valid",
	}))
	return
}

//Sends a Job query to the webservice
func (p Pipeline) Job(id string, messageSequence int) (job Job, err error) {
	req := p.newResquest(API_JOB, &job, nil, id, messageSequence)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Job " + id + " not found",
	}))
	return
}

//Sends a Job query to the webservice
func (p Pipeline) Batch(id string) (jobs Jobs, err error) {
	req := p.newResquest(API_BATCH, &jobs, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Job " + id + " not found",
	}))
	return
}

//Sends a request to the server in order to get all the jobs
func (p Pipeline) Jobs() (jobs Jobs, err error) {
	req := p.newResquest(API_JOBS, &jobs, nil)
	_, err = p.do(req, defaultErrorHandler())
	return
}

//Deletes a job
func (p Pipeline) DeleteJob(id string) (ok bool, err error) {
	req := p.newResquest(API_DEL_JOB, nil, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Job " + id + " not found",
	}))
	if err == nil {
		ok = true
	}
	return
}

//Deletes a batch of jobs
func (p Pipeline) DeleteBatch(id string) (ok bool, err error) {
	req := p.newResquest(API_DEL_BATCH, nil, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Job batch " + id + " not found",
	}))
	ok = err == nil
	return
}

//Overrides the xml decoder to get the writer
func resultClientMaker(p Pipeline) func() doer {
	return func() doer {
		cli := p.clientMaker()
		cli.SetDecoderSupplier(func(r io.Reader) restclient.Decoder {
			return NewWriterDecoder(r)
		})
		return cli
	}
}

//Returns the results of the job as an array of bytes
func (p Pipeline) Results(id string, w io.Writer) (ok bool, err error) {
	//check whether results are available
	job := Job{}
	req := p.newResquest(API_JOB, &job, nil, id, math.MaxInt32)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Job " + id + " not found",
	}))
	if err == nil {
		if job.Results.Href == "" {
			return false, err
		} else {
			//override the client maker
			p.clientMaker = resultClientMaker(p)
			req = p.newResquest(API_RESULT, w, nil, id)
			_, err = p.do(req, errorHandler(map[int]string{
				404: "Job " + id + " not found",
			}))
		}
	}
	return (err == nil), err
}

//Overrides the xml decoder to get raw data
func rawClientMaker(p Pipeline) func() doer {
	return func() doer {
		cli := p.clientMaker()
		cli.SetDecoderSupplier(func(r io.Reader) restclient.Decoder {
			return NewRawDataDecoder(r)
		})
		return cli
	}
}

//Gets the log file for a job
func (p Pipeline) Log(id string) (data []byte, err error) {
	p.clientMaker = rawClientMaker(p)
	rd := &RawData{Data: new([]byte)}
	req := p.newResquest(API_LOG, rd, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Job " + id + " not found",
	}))
	if err != nil {
		return nil, err
	}
	return *(rd.Data), nil
}

//Admin api
//Halts the ws
func (p Pipeline) Halt(key string) error {
	//override the client maker
	req := p.newResquest(API_HALT, nil, nil, key)
	_, err := p.do(req, defaultErrorHandler())
	return err
}

//Returns the list of clients
func (p Pipeline) Clients() (clients []Client, err error) {
	clientsStr := Clients{}
	req := p.newResquest(API_CLIENTS, &clientsStr, nil)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	clients = clientsStr.Clients
	return
}

//Creates a new client
func (p Pipeline) NewClient(in Client) (out Client, err error) {
	req := p.newResquest(API_NEWCLIENT, &out, &in)
	_, err = p.do(req, errorHandler(map[int]string{
		400: fmt.Sprintf("Client with id %v may already exist", in.Id),
	}))
	return
}

//Retrieves a client using the its id
func (p Pipeline) Client(id string) (out Client, err error) {
	req := p.newResquest(API_CLIENT, &out, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Client with id " + id + " not found",
	}))
	return
}

//Deletes a client
func (p Pipeline) DeleteClient(id string) (ok bool, err error) {
	req := p.newResquest(API_DELETECLIENT, nil, nil, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Client with id " + id + " not found",
	}))
	if err == nil {
		ok = true
	}
	return
}

//Modifies a client with the new data TODO:include the id in the client structure
func (p Pipeline) ModifyClient(in Client, id string) (out Client, err error) {
	req := p.newResquest(API_MODIFYCLIENT, &out, &in, id)
	_, err = p.do(req, errorHandler(map[int]string{
		404: "Client with id " + id + " not found",
	}))
	return
}

//Retrieves the list of different properties which describes the framework configuration
func (p Pipeline) Properties() (out []Property, err error) {
	props := Properties{}
	req := p.newResquest(API_PROPERTIES, &props, nil)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	return props.Properties, nil
}

//Gets the physical size of the jobs
func (p Pipeline) Sizes() (sizes JobSizes, err error) {
	req := p.newResquest(API_SIZE, &sizes, nil)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	return sizes, nil
}

//Gets execution queue
func (p Pipeline) Queue() (jobs []QueueJob, err error) {
	queue := Queue{}
	req := p.newResquest(API_QUEUE, &queue, nil)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	jobs = queue.Jobs
	return jobs, nil
}

func (p Pipeline) MoveUp(jobId string) (jobs []QueueJob, err error) {
	queue := Queue{}
	req := p.newResquest(API_MOVE_UP, &queue, nil, jobId)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	jobs = queue.Jobs
	return jobs, nil
}

func (p Pipeline) MoveDown(jobId string) (jobs []QueueJob, err error) {
	queue := Queue{}
	req := p.newResquest(API_MOVE_DOWN, &queue, nil, jobId)
	_, err = p.do(req, defaultErrorHandler())
	if err != nil {
		return
	}
	jobs = queue.Jobs
	return jobs, nil
}
