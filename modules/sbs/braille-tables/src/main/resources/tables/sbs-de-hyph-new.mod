#-------------------------------------------------------------------------------
#
#  sbs-de-hyph-new.mod
#
#  Nur Trennmarken für neue Rechtschreibung erhalten
#
#-------------------------------------------------------------------------------

# Für neue Rechtschreibung reservierte in "normale" Trennmarke ändern
pass2 @cb @e

# Für alte Rechtschreibung reservierte Trennmarken entfernen
pass2 @cf ?
pass2 @cd ?
pass2 @ce ?
