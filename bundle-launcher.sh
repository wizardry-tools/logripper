#!/usr/bin/env bash
#!/bin/bash

if type "tput" &> /dev/null; then 
	if [ -z "$COLUMNS" ]; then
		export COLUMNS="$(tput cols)"
	fi
	if [ -z "$LINES" ]; then
		export LINES="$(tput lines)"
	fi
fi

HERE=${BASH_SOURCE%/*}
java="$HERE/jre/bin/java"
javaArgs[0]="-Dlauncher.dir=$LAUNCHER_DIR"
javaIndex="1"
appIndex="0"
for var in "$@"; do
    if [[ ${var} == -Dlauncher.dir=* ]]; then
		javaArgs[0]="${var}"
	elif [[ ${var} == -D* ]] && [ "${var}" != "-D" ]; then
		javaArgs[${javaIndex}]="${var}"
		javaIndex=$((javaIndex + 1))
	else
		appArgs[${appIndex}]="${var}"
		appIndex=$((appIndex + 1))
	fi
done
stty -echoctl &> /dev/null
exec "${java}" "${javaArgs[@]}" -jar "$HERE/application.jar" "${appArgs[@]}"
exit $?
