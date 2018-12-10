# Build the mod-nota module
FROM maven:3.5-jdk-8 as builder
ADD . /home/src
WORKDIR /home/src
RUN mvn clean package -DskipTests

# add the module to the standard Pipeline v1.11.1 image
FROM daisyorg/pipeline-assembly:v1.11.1
COPY --from=builder /home/src/target/mod-nota-1.3.4-SNAPSHOT.jar /opt/daisy-pipeline2/modules/
