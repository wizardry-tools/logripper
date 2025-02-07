#!/bin/bash

if [ ! -z "${TERM}" ] ; then
	if command -v "tput" &> /dev/null ; then
		if [ -z "$COLUMNS" ]; then
			export COLUMNS="$(tput cols)"
		fi
		if [ -z "$LINES" ]; then
			export LINES="$(tput lines)"
		fi
	fi
fi

# See "https://coderwall.com/p/ssuaxa/how-to-make-a-jar-file-linux-executable"
SCRIPT_PATH=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && SCRIPT_PATH="./$0"
LAUNCHER_DIR="$(dirname $SCRIPT_PATH 2>/dev/null)"
if [ $? -ne 0 ]; then
    LAUNCHER_DIR="."
fi
if type "uname" &> /dev/null; then
	if [ `uname -o` = "Cygwin" ]; then
		SCRIPT_PATH=$(cygpath -w ${SCRIPT_PATH})
		LAUNCHER_DIR=$(cygpath -w ${LAUNCHER_DIR})
	fi
fi
java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi
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
exec "${java}" -XX:TieredStopAtLevel=1 "${javaArgs[@]}" -jar "$SCRIPT_PATH" "${appArgs[@]}"
exit $?
