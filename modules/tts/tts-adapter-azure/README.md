# Microsoft Azure Cognitive Speech Services Adapter

## Setup

To use Microsoft Azure Cognitive Speech Services, first [create a new
Azure Cognitive Speech
resource](https://learn.microsoft.com/en-us/azure/cognitive-services/cognitive-services-apis-create-account?tabs=speech#create-a-new-azure-cognitive-services-resource). After
your resource is successfully deployed, you can [get the access key
and region from the Azure
portal](https://learn.microsoft.com/en-us/azure/cognitive-services/cognitive-services-apis-create-account#get-the-keys-for-your-resource).

## Properties

The following properties need to be set in order to use the adapter:

- `org.daisy.pipeline.tts.azure.key`
- `org.daisy.pipeline.tts.azure.region`

## Tests

For unit test to work, please modify the `org.daisy.pipeline.tts.azure.key` and
`org.daisy.pipeline.tts.azure.region` properties in pom.xml, and comment the line
`org/daisy/pipeline/tts/azure/impl/AzureCognitiveSpeechTest.java` in
src/test/java/ignore.

Java 11 is required on macOS 10.14.
