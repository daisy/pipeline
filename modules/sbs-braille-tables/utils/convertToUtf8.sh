#!/bin/bash
tables=$1
tables_utf8=$2
mkdir -p $tables_utf8
for file in ${tables}/*; do
    filename=`basename $file`
    if [[ $filename =~ \.(cti|dis|mod)$ ]] ; then
        encoding=`file $file --mime-encoding --brief`
        case "$encoding" in
            utf-8|us-ascii)
                echo "${filename}: file is already utf-8, copying..."
                cp "$file" "${tables_utf8}/${filename}"
                ;;
            iso-8859-1)
                echo "${filename}: file is latin1, converting to utf-8..."
                iconv -f latin1 -t utf-8 "$file" -o "${tables_utf8}/${filename}"
                ;;
            *)
                echo "${filename}: ERROR: what kind of file is this?"
        esac
    fi
done

