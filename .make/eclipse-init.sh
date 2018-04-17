#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e

settings_file=.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.m2e.core.prefs

if ! test -e $(dirname $settings_file); then
    echo "No Eclipse workspace in ${ROOT_DIR}. Please create one with Eclipse first." >&2
    set +e
    exit 100
fi

echo "eclipse.m2.userSettingsFile=${ROOT_DIR}/${TARGET_DIR}/effective-settings.xml" >$settings_file
echo "eclipse.preferences.version=1" >>$settings_file
echo "Now restart your Eclipse workspace '${ROOT_DIR}'" >&2
