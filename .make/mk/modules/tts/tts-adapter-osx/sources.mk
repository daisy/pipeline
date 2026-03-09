modules/tts/tts-adapter-osx/.test modules/tts/tts-adapter-osx/.install modules/tts/tts-adapter-osx/.install-doc $(TARGET_DIR)/state/modules/tts/tts-adapter-osx/modified-since-release_ : \
	modules/tts/tts-adapter-osx/src/main/resources/transform-ssml.xsl \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx/impl/OSXSpeechService.java \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx/impl/OSXSpeechEngine.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/dispatch/GCDExecutorService.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/AbstractPropertyDictionary.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/growl/Growl.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSSpeechDictionary.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSOperation.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSSpeechSynthesizer.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSInvocationOperation.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSOperationQueue.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSVoice.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/NativeEnum.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/README.md \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSDistributedNotificationCenter.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSDockTile.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSApplication.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSMenu.java \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSWindow.java
modules/tts/tts-adapter-osx/.test modules/tts/tts-adapter-osx/.install-doc : \
	modules/tts/tts-adapter-osx/src/test/java/ignore \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx/impl/OSXSSMLTest.java \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx/impl/OSXSpeechTest.java
.make/mk/modules/tts/tts-adapter-osx/sources.mk : \
	modules/tts/tts-adapter-osx/src \
	modules/tts/tts-adapter-osx/src/test \
	modules/tts/tts-adapter-osx/src/test/java \
	modules/tts/tts-adapter-osx/src/test/java/org \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy/pipeline \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx \
	modules/tts/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx/impl \
	modules/tts/tts-adapter-osx/src/main \
	modules/tts/tts-adapter-osx/src/main/resources \
	modules/tts/tts-adapter-osx/src/main/java \
	modules/tts/tts-adapter-osx/src/main/java/org \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy/pipeline \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx \
	modules/tts/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx/impl \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/dispatch \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/growl \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit \
	modules/tts/tts-adapter-osx/src/main/java/org/rococoa/cocoa
