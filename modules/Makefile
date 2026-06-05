.PHONY: release-notes
release-notes :
	test -z "$$(git status --porcelain $@)"
	xsltproc ./generate-release-notes.xsl ../bom/pom.xml | cat - NEWS.md > NEWS.md.tmp
	mv NEWS.md.tmp NEWS.md
