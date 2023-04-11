BUILDDIR  := ${CURDIR}/build
GOPATH    := ${BUILDDIR}
GO        := env GOPATH="${GOPATH}" go
GOX       := env GOPATH="${GOPATH}" ${GOPATH}/bin/gox
GOVERALLS := env GOPATH="${GOPATH}" ${GOPATH}/bin/goveralls


define HELP_TEXT
Available targets:
  help                this help
  clean               clean up
  all                 build binaries
  build               build all
  build-updater       build dp2 tool
  dist                build x-platform binaries
  test                run tests with coverage
  cover-deploy        deploy test coverage results
endef
export HELP_TEXT

.PHONY: help clean build-setup build dist test cover-deploy all

all: build

help:
	@echo "$$HELP_TEXT"

clean:
	-rm -rf "${BUILDDIR}"

build: test build-updater

build-setup:
	@export GOPATH="${GOPATH}"
	@echo "Getting dependencies..."
	@mkdir -p "${GOPATH}/src/github.com/capitancambio"
	@test -d "${GOPATH}/src/github.com/capitancambio/pipeline-updater" || ln -s "${CURDIR}" "${GOPATH}/src/github.com/capitancambio/pipeline-updater"
	@${GO} get github.com/kardianos/osext
	@${GO} get github.com/blang/semver 
	@${GO} get github.com/smartystreets/goconvey
	@${GO} get golang.org/x/tools/cmd/cover 

build-updater: build-setup
	@echo "Building updater..."
	@${GO} install ${GOBUILD_FLAGS} github.com/capitancambio/pipeline-updater


dist: build-setup test
	@echo "Building for x-platform..."
	@${GO} get github.com/mitchellh/gox
	#@${GOX} -build-toolchain \
		#-osarch="linux/amd64 linux/386 darwin/386 darwin/amd64 windows/386 windows/amd64"
	@${GOX} -output="${GOPATH}/bin/{{.OS}}_{{.Arch}}/pipeline-updater" \
	        -osarch="linux/amd64 linux/386 darwin/386 darwin/amd64 windows/386 windows/amd64" \
	        

test: build-setup
	@echo "Running tests..."
	@${GO} test -covermode=atomic -coverprofile=${BUILDDIR}/profile.cov \
		github.com/capitancambio/pipeline-updater

cover-deploy: test 
	@${GO} get github.com/modocache/gover
	@${GO} get github.com/mattn/goveralls
	@${GOVERALLS} \
	      -coverprofile=${BUILDDIR}/profile.cov \
	      -service=travis-ci
