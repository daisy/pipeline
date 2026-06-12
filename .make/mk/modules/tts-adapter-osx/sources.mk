modules/tts-adapter-osx/.test modules/tts-adapter-osx/.install modules/tts-adapter-osx/.install-doc $(TARGET_DIR)/state/modules/tts-adapter-osx/modified-since-release_ : \
	modules/tts-adapter-osx/src/main/resources/transform-ssml.xsl \
	modules/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx/impl/OSXSpeechService.java \
	modules/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx/impl/OSXSpeechEngine.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/dispatch/GCDExecutorService.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/AbstractPropertyDictionary.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/growl/Growl.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSSpeechDictionary.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSOperation.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSSpeechSynthesizer.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSInvocationOperation.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSOperationQueue.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit/NSVoice.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/NativeEnum.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/README.md \
	modules/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSDistributedNotificationCenter.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSDockTile.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSApplication.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSMenu.java \
	modules/tts-adapter-osx/src/main/java/org/rococoa/cocoa/NSWindow.java
modules/tts-adapter-osx/.test modules/tts-adapter-osx/.install-doc : \
	modules/tts-adapter-osx/src/test/java/ignore \
	modules/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx/impl/OSXSSMLTest.java \
	modules/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx/impl/OSXSpeechTest.java
.make/mk/modules/tts-adapter-osx/sources.mk : \
	modules/tts-adapter-osx/src \
	modules/tts-adapter-osx/src/test \
	modules/tts-adapter-osx/src/test/java \
	modules/tts-adapter-osx/src/test/java/org \
	modules/tts-adapter-osx/src/test/java/org/daisy \
	modules/tts-adapter-osx/src/test/java/org/daisy/pipeline \
	modules/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts \
	modules/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx \
	modules/tts-adapter-osx/src/test/java/org/daisy/pipeline/tts/osx/impl \
	modules/tts-adapter-osx/src/main \
	modules/tts-adapter-osx/src/main/resources \
	modules/tts-adapter-osx/src/main/java \
	modules/tts-adapter-osx/src/main/java/org \
	modules/tts-adapter-osx/src/main/java/org/daisy \
	modules/tts-adapter-osx/src/main/java/org/daisy/pipeline \
	modules/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts \
	modules/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx \
	modules/tts-adapter-osx/src/main/java/org/daisy/pipeline/tts/osx/impl \
	modules/tts-adapter-osx/src/main/java/org/rococoa \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/dispatch \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/growl \
	modules/tts-adapter-osx/src/main/java/org/rococoa/contrib/appkit \
	modules/tts-adapter-osx/src/main/java/org/rococoa/cocoa
