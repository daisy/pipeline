clientlib/java/clientlib-java-httpclient/.test clientlib/java/clientlib-java-httpclient/.install $(TARGET_DIR)/state/clientlib/java/clientlib-java-httpclient/modified-since-release_ : \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client/http/Pipeline2HttpClient.java \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client/http/WSCallbackHandler.java \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client/http/WS.java \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client/http/WSInterface.java \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client/http/WSResponse.java
clientlib/java/clientlib-java-httpclient/.test : \
	clientlib/java/clientlib-java-httpclient/src/test/resources/logback.xml \
	clientlib/java/clientlib-java-httpclient/src/test/resources/input1.xml \
	clientlib/java/clientlib-java-httpclient/src/test/resources/input2.html \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/META-INF/MANIFEST.MF \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/OSGI-INF/scripts.xml \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/OSGI-INF/datatypes.xml \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/xml/regex.xml \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/xml/script.xpl \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/xml/choice.xml \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client/test/MockHttpClient.java \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client/http/WSRemoteTest.java \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client/http/WSTest.java \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client/http/PaxExamConfig.java \
	clientlib/java/clientlib-java-httpclient/src/test/java/Datatype_foo_choice.java \
	clientlib/java/clientlib-java-httpclient/src/test/java/XProcScript_foo_script.java \
	clientlib/java/clientlib-java-httpclient/src/test/java/Datatype_foo_regex.java
.make/mk/clientlib/java/clientlib-java-httpclient/sources.mk : \
	clientlib/java/clientlib-java-httpclient/src \
	clientlib/java/clientlib-java-httpclient/src/test \
	clientlib/java/clientlib-java-httpclient/src/test/resources \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/META-INF \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/OSGI-INF \
	clientlib/java/clientlib-java-httpclient/src/test/resources/example_script/xml \
	clientlib/java/clientlib-java-httpclient/src/test/java \
	clientlib/java/clientlib-java-httpclient/src/test/java/org \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client/test \
	clientlib/java/clientlib-java-httpclient/src/test/java/org/daisy/pipeline/client/http \
	clientlib/java/clientlib-java-httpclient/src/main \
	clientlib/java/clientlib-java-httpclient/src/main/java \
	clientlib/java/clientlib-java-httpclient/src/main/java/org \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client \
	clientlib/java/clientlib-java-httpclient/src/main/java/org/daisy/pipeline/client/http
