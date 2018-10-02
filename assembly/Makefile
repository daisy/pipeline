GTAR := gtar
DOCKER := docker

help :
	echo "make docker:"            >&2
	echo "	Build a Docker image"  >&2
	echo "make check-docker:"      >&2
	echo "	Test the Docker image" >&2

target/daisy-pipeline :
	cd target && unzip assembly-*-linux.zip

docker : target/daisy-pipeline
	$(GTAR) -cz \
	     target/daisy-pipeline \
	     Dockerfile.without_builder \
	     --transform='s/Dockerfile.without_builder/Dockerfile/' \
	| $(DOCKER) build -t daisyorg/pipeline-assembly -

check-docker :
	bash src/test/resources/test-docker-image.sh
