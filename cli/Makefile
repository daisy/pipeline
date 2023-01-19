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
  build-dp2           build dp2 tool
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

build: test build-dp2 

build-dp2:
	@echo "Building dp2..."
	@${GO} install ${GOBUILD_FLAGS} github.com/daisy/pipeline-cli-go/dp2


dist: test
	@echo "Building for x-platform..."
	@${GO} install github.com/mitchellh/gox
	#@${GOX} -build-toolchain \
		#-osarch="linux/amd64 linux/386 darwin/amd64 windows/386 windows/amd64"
	@${GOX} -output="${GOPATH}/bin/{{.OS}}_{{.Arch}}/{{.Dir}}" \
	        -osarch="linux/amd64 linux/386 darwin/amd64 windows/386 windows/amd64" \
	        ./dp2/ 

test:
	@echo "Running tests..."
	@${GO} test -covermode=atomic -coverprofile=${BUILDDIR}/profile.cov \
		github.com/daisy/pipeline-cli-go/cli

cover-deploy: test 
	@${GO} install github.com/mattn/goveralls
	@${GOVERALLS} \
	      -coverprofile=${BUILDDIR}/profile.cov \
	      -service=travis-ci
