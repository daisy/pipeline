help :
	echo "make docker:"            >&2
	echo "	Build a Docker image"  >&2
	echo "make check-docker:"      >&2
	echo "	Test the Docker image" >&2

docker :
	gtar -cz \
	     target/pipeline2-*_linux \
	     Dockerfile.without_builder \
	     --transform='s/Dockerfile.without_builder/Dockerfile/' \
	| docker build -t daisyorg/pipeline2 -

check-docker :
	bash src/test/resources/test-docker-image.sh
