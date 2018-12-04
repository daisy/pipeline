MVN := mvn
DOCKER := docker

.PHONY : docker
docker :
	$(MVN) clean package -DskipTests
	$(DOCKER) build -t notalib/pipeline-assembly .

.PHONY : check
check :
	bash test-docker-image.sh

.PHONY : help
help :
	echo "make docker:"              >&2
	echo "	Builds a Docker image"   >&2
	echo "make check:"               >&2
	echo "	Tests the Docker image"  >&2
