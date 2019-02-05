package pipeline

import (
	"encoding/xml"
)

//Error with information from the server
type Error struct {
	XMLName     xml.Name `xml:"http://www.daisy.org/ns/pipeline/data error"`
	Description string   `xml:"http://www.daisy.org/ns/pipeline/data description"`
	Trace       string   `xml:"http://www.daisy.org/ns/pipeline/data trace"`
	Query       string   `xml:"query,attr"`
}

//More info TODO link to wiki
//Alive struct defined from the xmls
//TODO link to wiki
type Alive struct {
	XMLName        xml.Name `xml:"http://www.daisy.org/ns/pipeline/data alive"`
	Authentication bool     `xml:"authentication,attr"` //Indicates if the framework is expecting authentication
	FsAllow        bool     `xml:"localfs,attr"`        //Fwk allows access to the local fs
	Version        string   `xml:"version,attr"`        //Version of the pipeline framework
}

//TODO link to wiki
type Scripts struct {
	XMLName xml.Name `xml:"http://www.daisy.org/ns/pipeline/data scripts"`
	Scripts []Script `xml:"http://www.daisy.org/ns/pipeline/data script"` //List of scripts available

	Href string `xml:"href,attr"` //Url used to perform this call
}

//Script struct
type Script struct {
	XMLName  xml.Name `xml:"http://www.daisy.org/ns/pipeline/data script"`
	Nicename string   `xml:"http://www.daisy.org/ns/pipeline/data nicename,omitempty"`

	Description string `xml:"http://www.daisy.org/ns/pipeline/data description,omitempty"`
	Version     string `xml:"http://www.daisy.org/ns/pipeline/data version,omitempty"`

	Homepage string   `xml:"http://www.daisy.org/ns/pipeline/data homepage,omitempty"`
	Inputs   []Input  `xml:"http://www.daisy.org/ns/pipeline/data input,omitempty"`
	Options  []Option `xml:"http://www.daisy.org/ns/pipeline/data option,omitempty"`
	Href     string   `xml:"href,attr"`
	Id       string   `xml:"id,attr,omitempty"`
}

type Option struct {
	XMLName    xml.Name `xml:"http://www.daisy.org/ns/pipeline/data option"`
	Required   bool     `xml:"required,attr,omitempty"`
	Sequence   bool     `xml:"sequence,attr,omitempty"`
	Name       string   `xml:"name,attr,omitempty"`
	NiceName   string   `xml:"nicename,attr,omitempty"`
	Ordered    bool     `xml:"ordered,attr,omitempty"`
	Mediatype  string   `xml:"mediaType,attr,omitempty"`
	ShortDesc  string   `xml:"-"`
	LongDesc   string   `xml:"desc,attr,omitempty"`
	TypeAttr   string   `xml:"type,attr,omitempty"`
	DataTypeAttr string `xml:"data-type,attr,omitempty"`
	Type       DataType `xml:"-"`
	Default    string   `xml:"default,attr,omitempty"`
	OutputType string   `xml:"optionType,attr,omitempty"`
	Separator  string   `xml:"separator,attr,omitempty"`
	Value      string   `xml:",chardata"`
	Items      []Item
}

type DataType interface{}

type Choice struct {
	XmlDefinition string
	Values        []DataType
}

type Value struct {
	XmlDefinition string
	Documentation string
	Value         string
}

type Pattern struct {
	XmlDefinition string
	Pattern       string
	Documentation string
}

type AnyFileURI struct {
	XmlDefinition string
	Documentation string
}

type AnyDirURI struct {
	XmlDefinition string
	Documentation string
}

type XsAnyURI struct {
	XmlDefinition string
	Documentation string
}

type XsBoolean struct {
	XmlDefinition string
	Documentation string
}

type XsInteger struct {
	XmlDefinition string
	Documentation string
}

type XsString struct {
	XmlDefinition string
	Documentation string
}

type Input struct {
	XMLName   xml.Name `xml:"http://www.daisy.org/ns/pipeline/data input"`
	ShortDesc string   `xml:"-"`
	LongDesc  string   `xml:"desc,attr,omitempty"`
	Mediatype string   `xml:"mediaType,attr,omitempty"`
	Name      string   `xml:"name,attr,omitempty"`
	NiceName  string   `xml:"nicename,attr,omitempty"`
	Sequence  bool     `xml:"sequence,attr,omitempty"`
	Items     []Item
}

type Item struct {
	XMLName xml.Name `xml:"http://www.daisy.org/ns/pipeline/data item"`
	Value   string   `xml:"value,attr"`
}

type Callback struct {
	XMLName   xml.Name `xml:"http://www.daisy.org/ns/pipeline/data callback"`
	Href      string   `xml:"href,attr"`
	Frequency string   `xml:"frequency,attr"`
	Type      string   `xml:"type,attr"`
}

type JobRequest struct {
	XMLName  xml.Name   `xml:"http://www.daisy.org/ns/pipeline/data jobRequest"`
	Nicename string     `xml:"http://www.daisy.org/ns/pipeline/data nicename,omitempty"`
	BatchId  string     `xml:"http://www.daisy.org/ns/pipeline/data batchId,omitempty"`
	Priority string     `xml:"http://www.daisy.org/ns/pipeline/data priority,omitempty"`
	Script   Script     `xml:"http://www.daisy.org/ns/pipeline/data script"`
	Inputs   []Input    `xml:"http://www.daisy.org/ns/pipeline/data input,omitempty"`
	Options  []Option   `xml:"http://www.daisy.org/ns/pipeline/data option,omitempty"`
	Callback []Callback `xml:"http://www.daisy.org/ns/pipeline/data callback,omitempty"`
}

type Job struct {
	XMLName  xml.Name `xml:"http://www.daisy.org/ns/pipeline/data job"`
	Nicename string   `xml:"http://www.daisy.org/ns/pipeline/data nicename"`
	BatchId  string   `xml:"http://www.daisy.org/ns/pipeline/data batchId"`
	Script            `xml:"http://www.daisy.org/ns/pipeline/data script"`
	Messages Messages `xml:"http://www.daisy.org/ns/pipeline/data messages"`
	Log      Log      `xml:"http://www.daisy.org/ns/pipeline/data log"`
	Results  Results  `xml:"http://www.daisy.org/ns/pipeline/data results"`
	Priority string   `xml:"priority,attr"`
	Status   string   `xml:"status,attr"`
	Href     string   `xml:"href,attr"`
	Id       string   `xml:"id,attr"`
}
type Result struct {
	XMLName  xml.Name `xml:"http://www.daisy.org/ns/pipeline/data result"`
	MimeType string   `xml:"mime-type,attr"`
	Href     string   `xml:"href,attr"`
	Result   []Result `xml:"http://www.daisy.org/ns/pipeline/data result"`
}
type Message struct {
	XMLName  xml.Name  `xml:"http://www.daisy.org/ns/pipeline/data message"`
	Level    string    `xml:"level,attr"`
	Sequence int       `xml:"sequence,attr"`
	Content  string    `xml:"content,attr"`
	Message  []Message `xml:"http://www.daisy.org/ns/pipeline/data message"`
}
type Log struct {
	XMLName xml.Name `xml:"http://www.daisy.org/ns/pipeline/data log"`
	Href    string   `xml:"href,attr"`
}
type Messages struct {
	XMLName  xml.Name  `xml:"http://www.daisy.org/ns/pipeline/data messages"`
	Progress float64   `xml:"progress,attr"`
	Message  []Message `xml:"http://www.daisy.org/ns/pipeline/data message"`
}
type Results struct {
	XMLName  xml.Name `xml:"http://www.daisy.org/ns/pipeline/data results"`
	Result   []Result `xml:"http://www.daisy.org/ns/pipeline/data result"`
	Href     string   `xml:"href,attr"`
	MimeType string   `xml:"mime-type,attr"`
}

type Jobs struct {
	XMLName xml.Name `xml:"http://www.daisy.org/ns/pipeline/data jobs"`
	Jobs    []Job    `xml:"http://www.daisy.org/ns/pipeline/data job"`
	Href    string   `xml:"href,attr"`
}

//Admin stuff
type Clients struct {
	XMLName xml.Name `xml:"http://www.daisy.org/ns/pipeline/data clients"`
	Clients []Client `xml:"http://www.daisy.org/ns/pipeline/data client"`
	Href    string   `xml:"href,attr"`
}
type Client struct {
	XMLName  xml.Name `xml:"http://www.daisy.org/ns/pipeline/data client"`
	Secret   string   `xml:"secret,attr"`
	Href     string   `xml:"href,attr"`
	Role     string   `xml:"role,attr"`
	Id       string   `xml:"id,attr"`
	Contact  string   `xml:"contact,attr"`
	Priority string   `xml:"priority,attr"`
}
type Property struct {
	XMLName    xml.Name `xml:"http://www.daisy.org/ns/pipeline/data property"`
	BundleName string   `xml:"bundleName,attr"`
	BundleId   string   `xml:"bundleId,attr"`
	Value      string   `xml:"value,attr"`
	Name       string   `xml:"name,attr"`
}

type Properties struct {
	XMLName    xml.Name   `xml:"http://www.daisy.org/ns/pipeline/data properties"`
	Properties []Property `xml:"http://www.daisy.org/ns/pipeline/data property"`
	Href       string     `xml:"href,attr"`
}

type JobSizes struct {
	XMLName  xml.Name  `xml:"http://www.daisy.org/ns/pipeline/data jobSizes"`
	JobSizes []JobSize `xml:"http://www.daisy.org/ns/pipeline/data jobSize"`
	Href     string    `xml:"href,attr"`
	Total    int       `xml:"total,attr"`
}
type JobSize struct {
	XMLName xml.Name `xml:"http://www.daisy.org/ns/pipeline/data jobSize"`
	Output  int      `xml:"output,attr"`
	Id      string   `xml:"id,attr"`
	Context int      `xml:"context,attr"`
	Log     int      `xml:"log,attr"`
}
type Queue struct {
	XMLName xml.Name   `xml:"http://www.daisy.org/ns/pipeline/data queue"`
	Jobs    []QueueJob `xml:"http://www.daisy.org/ns/pipeline/data job"`
	Href    string     `xml:"href,attr"`
}
type QueueJob struct {
	XMLName          xml.Name `xml:"http://www.daisy.org/ns/pipeline/data job"`
	Moveup           string   `xml:"moveUp,attr"`
	Id               string   `xml:"id,attr"`
	ClientPriority   string   `xml:"clientPriority,attr"`
	RelativeTime     float64  `xml:"relativeTime,attr"`
	JobPriority      string   `xml:"jobPriority,attr"`
	Href             string   `xml:"href,attr"`
	TimeStamp        int64    `xml:"timestamp,attr"`
	MoveDown         string   `xml:"moveDown,attr"`
	ComputedPriority float64  `xml:"computedPriority,attr"`
}
