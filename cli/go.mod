module github.com/daisy/pipeline-cli-go

go 1.19

// replace github.com/daisy/pipeline-clientlib-go => /Users/bert/src/github/daisy/pipeline/clientlib/go

require (
	github.com/bertfrees/blackterm v0.0.0-20230119134958-9d34cff72a06
	github.com/bertfrees/go-subcommand v0.0.0-20230119135135-b5e2f5321a24
	github.com/capitancambio/chalk v0.0.0-20160127153406-9dc2af224a17
	github.com/capitancambio/restclient v0.0.0-20150219172137-547c7b5e0857
	// go get github.com/daisy/pipeline-clientlib-go@3aeed200be
	github.com/daisy/pipeline-clientlib-go v0.0.0-20240710161849-3aeed200be9b
	github.com/hashicorp/go-version v1.0.0
	github.com/kardianos/osext v0.0.0-20190222173326-2bc1f35cddc0
	github.com/mattn/goveralls v0.0.11
	github.com/mitchellh/gox v1.0.1
	github.com/mitchellh/iochan v1.0.0
	github.com/russross/blackfriday v1.6.0
	golang.org/x/mod v0.7.0
	golang.org/x/tools v0.5.0
	launchpad.net/goyaml v0.0.0-20140305200416-000000000051
)
