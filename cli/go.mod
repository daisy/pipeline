module github.com/daisy/pipeline-cli-go

go 1.23.0

// toolchain go1.24.2

// replace github.com/daisy/pipeline-clientlib-go => /Users/bert/src/github/daisy/pipeline/clientlib/go

require (
	github.com/bertfrees/blackterm v0.0.0-20230119134958-9d34cff72a06
	github.com/bertfrees/go-subcommand v0.0.0-20230119135135-b5e2f5321a24
	github.com/capitancambio/chalk v0.0.0-20160127153406-9dc2af224a17
	github.com/capitancambio/restclient v0.0.0-20150219172137-547c7b5e0857
	// go get github.com/daisy/pipeline-clientlib-go@afd664c2c8
	github.com/daisy/pipeline-clientlib-go v0.0.0-20240813132445-afd664c2c897
	// go get github.com/gorilla/websocket
	github.com/gorilla/websocket v1.5.3
	github.com/hashicorp/go-version v1.0.0
	github.com/kardianos/osext v0.0.0-20190222173326-2bc1f35cddc0
	github.com/mattn/goveralls v0.0.11
	github.com/mitchellh/go-ps v1.0.0
	github.com/mitchellh/gox v1.0.1
	github.com/mitchellh/iochan v1.0.0
	github.com/russross/blackfriday v1.6.0
	golang.org/x/mod v0.8.0
	// go get golang.org/x/tools@a7f7db3f17fc
	golang.org/x/tools v0.5.1-0.20230111220935-a7f7db3f17fc
	launchpad.net/goyaml v0.0.0-20140305200416-000000000051
	github.com/go-ole/go-ole v1.2.6
	github.com/shirou/gopsutil v3.21.11+incompatible
	github.com/tklauser/go-sysconf v0.3.15
	github.com/tklauser/numcpus v0.10.0
	github.com/yusufpapurcu/wmi v1.2.4
	golang.org/x/sys v0.31.0
)
