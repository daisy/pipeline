AC_INIT([sbs-braille-tables],
        m4_esyscmd([build-aux/git-version-gen .tarball-version]))
AM_INIT_AUTOMAKE()

AC_CONFIG_FILES([
  Makefile
  tables/Makefile
  src/Makefile
  utils/Makefile
  tables/sbs-version.utb
  tests/Makefile
  lib/Makefile
  whitelist/Makefile
])

AC_PROG_CC
gl_EARLY
gl_INIT

AC_OUTPUT
