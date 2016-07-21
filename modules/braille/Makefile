.PHONY: release-notes
release-notes :
	test -z "$$(git status --porcelain $@)"
	xsltproc make/generate-release-notes.xsl maven/bom/pom.xml | cat - NEWS.md > NEWS.md.tmp
	mv NEWS.md.tmp NEWS.md
