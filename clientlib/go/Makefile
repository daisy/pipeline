BUILDDIR  := ${CURDIR}/build
GOPATH    := ${BUILDDIR}
GO        := env GOPATH="${GOPATH}" go
GOVERALLS := env GOPATH="${GOPATH}" ${GOPATH}/bin/goveralls

dependencies : \
	${GOPATH}/src/github.com/capitancambio/restclient \
	${GOPATH}/src/golang.org/x/tools/cmd/cover

check : dependencies
	if ! test -d "${GOPATH}/src/github.com/daisy/pipeline-clientlib-go"; then \
		mkdir -p ${GOPATH}/src/github.com/daisy && \
		ln -s "${CURDIR}" "${GOPATH}/src/github.com/daisy/pipeline-clientlib-go"; \
	fi
	${GO} test -covermode=atomic -coverprofile=${BUILDDIR}/profile.cov \
		github.com/daisy/pipeline-clientlib-go

clean :
	rm -rf ${BUILDDIR}

# for Travis
coveralls-dependencies : \
	${GOPATH}/src/github.com/modocache/gover \
	${GOPATH}/src/github.com/mattn/goveralls

coveralls: check coveralls-dependencies
	${GOVERALLS} -coverprofile=${BUILDDIR}/profile.cov -service=travis-ci

${GOPATH}/src/github.com/capitancambio/restclient \
${GOPATH}/src/golang.org/x/tools/cmd/cover \
${GOPATH}/src/github.com/modocache/gover \
${GOPATH}/src/github.com/mattn/goveralls :
	target=$@ && \
	${GO} get $${target#${GOPATH}/src/}
