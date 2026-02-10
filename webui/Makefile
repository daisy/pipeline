.PHONY : help
help :
	@echo "make help:"                              >&2 && \
	 echo "\tPrint list of commands"                >&2 && \
	 echo "make docker-image:"                      >&2 && \
	 echo "\tBuild, tag and push the Docker image"  >&2

.PHONY : docker-image
docker-image :
	docker buildx create --use --name=mybuilder \
	                     --driver docker-container \
	                     --driver-opt image=moby/buildkit:buildx-stable-1
	docker buildx build --platform linux/amd64,linux/arm64 -t daisyorg/pipeline-webui:latest-snapshot --push .
	docker buildx rm mybuilder
