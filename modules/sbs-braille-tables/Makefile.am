SUBDIRS = tables tests lib src utils whitelist

ACLOCAL_AMFLAGS = -I m4
EXTRA_DIST = m4/gnulib-cache.m4

BUILT_SOURCES = $(top_srcdir)/.version

$(top_srcdir)/.version:
	echo $(VERSION) > $@-t && mv $@-t $@

dist-hook:
	echo $(VERSION) > $(distdir)/.tarball-version
