#!/bin/bash
TABLES=(
	unicode.dis
	braille-patterns.cti
	marburg.ctb
	marburg_edit.ctb
	nemeth.ctb
	nemeth_edit.ctb
	ukmaths.ctb
	ukmaths_edit.ctb
	wiskunde.ctb
	wiskunde_edit.ctb)
SEM_FILES=(
	marburg.sem
	nemeth.sem
	ukmaths.sem
	wiskunde.sem)
find_table() {
    if [ -e dependency/share/liblouis/tables/$1 ]; then
	echo "dependency/share/liblouis/tables/$1"
    elif [ -e dependency/share/liblouisutdml/lbu_files/$1 ]; then
	echo "dependency/share/liblouisutdml/lbu_files/$1"
    else
	echo "Could not locate table: $1" >&2
	exit 1
    fi
}
table_dependencies() {
	DEPENDENCIES=$(grep -E "^include.*" $(find_table $1) | sed 's/^include  *\([^ ][^ ]*\).*$/\1/')
	TRANSITIVE_DEPENDENCIES=""
	for TABLE in $DEPENDENCIES; do
		TRANSITIVE_DEPENDENCIES="$TRANSITIVE_DEPENDENCIES $(table_dependencies $TABLE)"
	done
	echo "$DEPENDENCIES $TRANSITIVE_DEPENDENCIES"
}
mkdir -p generated-resources/lbu_files/mathml
for FILE in ${SEM_FILES[@]}; do
	cp dependency/share/liblouisutdml/lbu_files/$FILE generated-resources/lbu_files/mathml/
done
ALL_TABLES=""
for TABLE in ${TABLES[@]}; do
	ALL_TABLES="$TABLE $ALL_TABLES $(table_dependencies $TABLE)"
done
ALL_TABLES="$(echo $ALL_TABLES | tr ' ' '\n' | sort | uniq)"
for TABLE in ${ALL_TABLES[@]}; do
	cp $(find_table $TABLE) generated-resources/lbu_files/mathml/
done
