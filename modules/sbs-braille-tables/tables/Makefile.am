copied_tables =					\
	sbs-code.cti				\
	sbs-de-accents-ch.mod			\
	sbs-de-accents-reduced.mod		\
	sbs-de-accents.cti			\
	sbs-de-accents.mod			\
	sbs-de-begendcaps.mod			\
	sbs-de-capsign.mod			\
	sbs-de-core6.cti			\
	sbs-de-hyph-new.mod			\
	sbs-de-hyph-none.mod			\
	sbs-de-hyph-old.mod			\
	sbs-de-hyph-word.mod			\
	sbs-de-letsign.mod			\
	sbs-numsign.mod				\
	sbs-special.cti				\
	sbs-special.mod				\
	sbs-whitespace.mod			\
	sbs-wordsplit.dis			\
	sbs.dis

generated_tables =				\
	sbs-de-core.mod				\
	sbs-de-g0-core.mod			\
	sbs-de-g1-core.mod			\
	sbs-de-g2-core.mod			\
	sbs-de-g2-name.mod			\
	sbs-de-g2-place.mod			\
	sbs-litdigit-lower.mod			\
	sbs-litdigit-upper.mod


version_table = sbs-version.utb

table_files = $(copied_tables) $(generated_tables) $(version_table)

tablesdir = $(datadir)/liblouis/tables
tables_DATA = $(table_files)
CLEANFILES = $(table_files)

utils_dir = $(top_srcdir)/utils

$(copied_tables): %: $(top_srcdir)/src/%
	cp $< $@

$(generated_tables): %.mod: $(top_srcdir)/src/%.mod_src
	PERL5LIB=$(utils_dir)  $(utils_dir)/tab2lou.pl $< $@
