docker :
	gtar -cz \
	     target/pipeline2-*_linux \
	     Dockerfile.without_builder \
	     --transform='s/Dockerfile.without_builder/Dockerfile/' \
	| docker build -t daisyorg/pipeline2 -
