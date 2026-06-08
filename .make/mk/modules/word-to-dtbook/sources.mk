modules/word-to-dtbook/.test modules/word-to-dtbook/.install modules/word-to-dtbook/.install-doc $(TARGET_DIR)/state/modules/word-to-dtbook/modified-since-release_ : \
	modules/word-to-dtbook/src/main/resources/css/dtbookbasic.css \
	modules/word-to-dtbook/src/main/resources/META-INF/catalog.xml \
	modules/word-to-dtbook/src/main/resources/xml/TOC.xsl \
	modules/word-to-dtbook/src/main/resources/xml/OOML2MML.xsl \
	modules/word-to-dtbook/src/main/resources/xml/Schematron.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/fix-dtbook.xpl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/doctyping.xpl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/repair.xpl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/narrator.xpl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-idref.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/output.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-remove-empty-elements.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/narrator-headings-r14.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-complete-structure.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-change-inline-pagenum-to-block.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/localization.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/dummy-headings-tools.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-pagenum-type.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-pagenum-type.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/narrator-title.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-metadata.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-indent.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/iterative-processor.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/narrator-headings-r100.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-levelnormalizer.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-add-author-title.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-level-cleaner.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-add-levels.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-lists.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-levelsplitter.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/narrator-metadata.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/export-doctype.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/recursive-copy2.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-flatten-redundant-nesting.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/output2.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/level-tools.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-add-lang.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-move-pagenum.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/narrator-lists.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/narrator-empty-cells.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-remove-empty-elements.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/recursive-copy.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/library.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/repair-remove-illegal-headings.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl/tidy-externalize-whitespace.xsl \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/tidy.xpl \
	modules/word-to-dtbook/src/main/resources/xml/Common.xsl \
	modules/word-to-dtbook/src/main/resources/xml/word-to-dtbook.xpl \
	modules/word-to-dtbook/src/main/resources/xml/oox2Daisy.xsl \
	modules/word-to-dtbook/src/main/resources/xml/Common3.xsl \
	modules/word-to-dtbook/src/main/resources/xml/Common2.xsl \
	modules/word-to-dtbook/src/main/resources/maven.properties \
	modules/word-to-dtbook/src/main/resources/dtd/mathml/mmlalias.ent \
	modules/word-to-dtbook/src/main/resources/dtd/mathml/mmlextra.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isoamsa.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isoamsb.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isoamsc.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isotech.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isoamsr.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isomfrk.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isogrk3.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isomopf.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isoamsn.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isoamso.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13/isomscr.ent \
	modules/word-to-dtbook/src/main/resources/dtd/mathml2-qname-1.mod \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isobox.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isonum.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isolat1.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isolat2.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isocyr2.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isocyr1.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isopub.ent \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879/isodia.ent \
	modules/word-to-dtbook/src/main/resources/dtd/mathml2.dtd \
	modules/word-to-dtbook/src/main/resources/dtd/dtbook-2005-3.dtd \
	modules/word-to-dtbook/src/main/resources/exe/ExportShapesWithWord.exe \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordToEPUB3Script.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordToHTMLScript.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordToDAISY3Script.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordToEBrailleScript.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordToMP3Script.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordBasedScript.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl/WordToDAISY202Script.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/PageStylesValidator.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/ValidationResult.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/DTBookCleanerLibrary.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/ImageProcessing.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/PageStyle.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/ImageFormat.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl/DaisyClass.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/shapes/OOShapesExporter.java \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/shapes/WordShapesExporter.java \
	modules/word-to-dtbook/src/main/csharp/docx-shapes-exporter.sln \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/ExportShapesWithWord.csproj \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/ClipboardExtensions.cs \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/DaisyTranslatorPhaseTwo.snk \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/ShapesExporter.cs \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/App.config \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/Properties/AssemblyInfo.cs \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/Program.cs \
	modules/word-to-dtbook/src/main/csharp/CheckWordArchitecture/CheckWordArchitecture.csproj \
	modules/word-to-dtbook/src/main/csharp/CheckWordArchitecture/App.config \
	modules/word-to-dtbook/src/main/csharp/CheckWordArchitecture/Properties/AssemblyInfo.cs \
	modules/word-to-dtbook/src/main/csharp/CheckWordArchitecture/Program.cs
modules/word-to-dtbook/.test modules/word-to-dtbook/.install-doc : \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Params\ 10.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Input/F10.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Output/F10-Picture\ 3.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Output/F10-Picture\ 1.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Output/F10.xml \
	modules/word-to-dtbook/src/test/resources/logback.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Input/default_sample_for_dtbook_conversion.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Output/default_sample_for_dtbook_conversion-Image\ 1.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Output/default_sample_for_dtbook_conversion-Image\ 2.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Output/default_sample_for_dtbook_conversion-Image\ 3.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Output/default_sample_for_dtbook_conversion-Image\ 4.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Output/default_sample_for_dtbook_conversion.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion-Image\ 1.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion-Image\ 2.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion-Image\ 3.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion-Image\ 4.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion-Shape478211313.png \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes/default_sample_for_dtbook_conversion-Shape8.png \
	modules/word-to-dtbook/src/test/resources/Test\ 6/Input/F6.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 6/Output/F6.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 6/Params\ 6.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 1/Input/F1.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 1/Output/F1.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 1/Output/F1-42-21112966.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 1/Params\ 1.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 8/Input/F8.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 8/Output/F8-Picture\ 1.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 8/Output/F8.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 8/Params\ 8.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 9/Input/F9.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 9/Params\ 9.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 9/Output/F9.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 7/Input/F7.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 7/Output/F7.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 7/Params\ 7.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 13/Input/mathml-core-tests.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 13/Output/mathml-core-tests.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 12/Input/Untitled.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 12/Output/Untitled.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 2/Params\ 2.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 2/Input/F\ 2.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 2/Output/F\ 2.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 5/Input/F5.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 5/Params\ 5.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 5/Output/F5.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Input/F4.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 26.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 27.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4.xml \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 8.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 19.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 25.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 24.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 18.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 9.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 20.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 21.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 23.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 22.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 2.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 12.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 3.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 1.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 10.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 11.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 4.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 15.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 14.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 5.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 7.jpg \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 16.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 17.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output/F4-Picture\ 6.png \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Params\ 4.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 3/Params\ 3.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 3/Input/F\ 3.docx \
	modules/word-to-dtbook/src/test/resources/Test\ 3/Output/F\ 3.xml \
	modules/word-to-dtbook/src/test/java/XProcSpecTest.java \
	modules/word-to-dtbook/src/test/xprocspec/test_word-to-dtbook.script.xprocspec \
	modules/word-to-dtbook/src/test/xprocspec/test_fix.xprocspec
modules/word-to-dtbook/.install-doc : \
	modules/word-to-dtbook/doc/index.md
.make/mk/modules/word-to-dtbook/sources.mk : \
	modules/word-to-dtbook/src \
	modules/word-to-dtbook/src/test \
	modules/word-to-dtbook/src/test/resources \
	modules/word-to-dtbook/src/test/resources/Test\ 10 \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 10/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 11 \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 11/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 11/OutputWithShapes \
	modules/word-to-dtbook/src/test/resources/Test\ 6 \
	modules/word-to-dtbook/src/test/resources/Test\ 6/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 6/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 1 \
	modules/word-to-dtbook/src/test/resources/Test\ 1/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 1/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 8 \
	modules/word-to-dtbook/src/test/resources/Test\ 8/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 8/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 9 \
	modules/word-to-dtbook/src/test/resources/Test\ 9/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 9/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 7 \
	modules/word-to-dtbook/src/test/resources/Test\ 7/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 7/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 13 \
	modules/word-to-dtbook/src/test/resources/Test\ 13/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 13/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 12 \
	modules/word-to-dtbook/src/test/resources/Test\ 12/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 12/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 2 \
	modules/word-to-dtbook/src/test/resources/Test\ 2/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 2/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 5 \
	modules/word-to-dtbook/src/test/resources/Test\ 5/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 5/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 4 \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 4/Output \
	modules/word-to-dtbook/src/test/resources/Test\ 3 \
	modules/word-to-dtbook/src/test/resources/Test\ 3/Input \
	modules/word-to-dtbook/src/test/resources/Test\ 3/Output \
	modules/word-to-dtbook/src/test/java \
	modules/word-to-dtbook/src/test/xprocspec \
	modules/word-to-dtbook/src/main \
	modules/word-to-dtbook/src/main/resources \
	modules/word-to-dtbook/src/main/resources/css \
	modules/word-to-dtbook/src/main/resources/META-INF \
	modules/word-to-dtbook/src/main/resources/xml \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook \
	modules/word-to-dtbook/src/main/resources/xml/fix-dtbook/xsl \
	modules/word-to-dtbook/src/main/resources/dtd \
	modules/word-to-dtbook/src/main/resources/dtd/mathml \
	modules/word-to-dtbook/src/main/resources/dtd/iso9573-13 \
	modules/word-to-dtbook/src/main/resources/dtd/iso8879 \
	modules/word-to-dtbook/src/main/resources/exe \
	modules/word-to-dtbook/src/main/java \
	modules/word-to-dtbook/src/main/java/org \
	modules/word-to-dtbook/src/main/java/org/daisy \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/script/impl \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/impl \
	modules/word-to-dtbook/src/main/java/org/daisy/pipeline/word_to_dtbook/shapes \
	modules/word-to-dtbook/src/main/csharp \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord \
	modules/word-to-dtbook/src/main/csharp/ExportShapesWithWord/Properties \
	modules/word-to-dtbook/src/main/csharp/CheckWordArchitecture \
	modules/word-to-dtbook/src/main/csharp/CheckWordArchitecture/Properties \
	modules/word-to-dtbook/doc
