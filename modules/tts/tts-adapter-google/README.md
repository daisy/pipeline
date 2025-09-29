# Google cloud Text-to-speech Adapter

## New properties

The following properties are used by the adapter : 
- `org.daisy.pipeline.tts.google.apikey` (mandatory) : API key to connect to Google Text-To-Speech service.
- `org.daisy.pipeline.tts.google.samplerate` (defaults to `22050`) : sample rate of the audio output
- `org.daisy.pipeline.tts.google.priority` (defaults to `15`) : priority of usage within the pipeline if other text engine are available

## Usage 

To use Google Cloud Text-to-Speech (https://cloud.google.com/text-to-speech/docs/quickstart-client-libraries) with this adapter in the pipeline, first go to the [Google cloud console](https://console.cloud.google.com)  :
1. In the project selection page, select or create a Cloud project.
2. Make sure billing is turned on for your Google Cloud project.
3. In the "API and Services" menu, open the "Library" panel, and search and activate the "Cloud Text-to-Speech API".
4. Still in In the "API and services" menu, open to the "Credentials" section and create an API key for this service.
5. Copy this API key in the pipeline configuration file, with the property "org.daisy.pipeline.tts.google.apikey".

## Tests

For the unit test to work with the actual Google service instead of a mock, please modify the `org.daisy.pipeline.tts.google.apikey` property in pom.xml and comment out the `org.daisy.pipeline.tts.google.address` property.
