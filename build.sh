# /////////////////////////////////////////////////////////////////////////////
# REFCODES.ORG
# =============================================================================
# This code is copyright (c) by Siegfried Steiner, Munich, Germany and licensed
# under the following (see "http://en.wikipedia.org/wiki/Multi-licensing")
# licenses:
# =============================================================================
# GNU General Public License, v3.0 ("http://www.gnu.org/licenses/gpl-3.0.html")
# =============================================================================
# Apache License, v2.0 ("http://www.apache.org/licenses/LICENSE-2.0")
# =============================================================================
# Please contact the copyright holding author(s) of the software artifacts in
# question for licensing issues not being covered by the above listed licenses,
# also regarding commercial licensing models or regarding the compatibility
# with other open source licenses.
# /////////////////////////////////////////////////////////////////////////////
#!/bin/bash

# ------------------------------------------------------------------------------
# INIT:
# ------------------------------------------------------------------------------

CURRENT_PATH="$(pwd)"
SCRIPT_PATH="$(dirname $0)"
cd "${SCRIPT_PATH}"
SCRIPT_PATH="$(pwd)"
cd "${CURRENT_PATH}"
SCRIPT_DIR="${SCRIPT_PATH##*/}"
SCRIPT_NAME="$(basename $0 .sh)"
PARENT_PATH="$(realpath $(dirname $0)/..)"
PARENT_DIR="${PARENT_PATH##*/}"
MODULE_NAME="$(echo -e "${SCRIPT_DIR}" | cut -d- -f3- )"
if [ -z "${MODULE_NAME}" ]; then
	MODULE_NAME="$(echo -e "${SCRIPT_DIR}" | cut -d- -f2- )"
	if [ -z "${MODULE_NAME}" ]; then
		MODULE_NAME="${SCRIPT_DIR}"
	fi
fi
if [ -z ${COLUMNS} ] ; then
	export COLUMNS=$(tput cols)
fi

# ------------------------------------------------------------------------------
# ANSI ESCAPE CODES:
# ------------------------------------------------------------------------------

ESC_BOLD="\E[1m"
ESC_FAINT="\E[2m"
ESC_ITALIC="\E[3m"
ESC_UNDERLINE="\E[4m"
ESC_FG_RED="\E[31m"
ESC_FG_GREEN="\E[32m"
ESC_FG_YELLOW="\E[33m"
ESC_FG_BLUE="\E[34m"
ESC_FG_MAGENTA="\E[35m"
ESC_FG_CYAN="\E[36m"
ESC_FG_WHITE="\E[37m"
ESC_RESET="\E[0m"

# ------------------------------------------------------------------------------
# PRINTLN:
# ------------------------------------------------------------------------------

function printLn {
	char="-"
	if [[ $# == 1 ]] ; then
		char="$1"
	fi
	echo -en "${ESC_FAINT}"
	for (( i=0; i< ${COLUMNS}; i++ )) ; do
		echo -en "${char}"
	done
	echo -e "${ESC_RESET}"
}
# ------------------------------------------------------------------------------
# QUIT:
# ------------------------------------------------------------------------------

function quit {
	input=""
	while ([ "$input" != "q" ] && [ "$input" != "y" ]); do
		echo -ne "> Continue? Enter [${ESC_BOLD}q${ESC_RESET}] to quit, [${ESC_BOLD}y${ESC_RESET}] to continue: ";
		read input;
	done
	# printf '%*s\n' "${COLUMNS:-$(tput cols)}" '' | tr ' ' -
	if [ "$input" == "q" ] ; then
		printLn
		echo -e "> ${ESC_BOLD}Aborting due to user input.${ESC_RESET}"
		cd "${CURRENT_PATH}"
		exit 1
	fi
}

# ------------------------------------------------------------------------------
# BANNER:
# ------------------------------------------------------------------------------

function printBanner {
	banner=$( figlet -w 999 "/${MODULE_NAME}:>>>${SCRIPT_NAME}..." 2> /dev/null )
	if [ $? -eq 0 ]; then
		echo "${banner}" | cut -c -${COLUMNS}
	else
		banner "${SCRIPT_NAME}..." 2> /dev/null
		if [ $? -ne 0 ]; then
			echo -e "> ${SCRIPT_NAME}:" | tr a-z A-Z 
		fi
	fi
}

printBanner

# ------------------------------------------------------------------------------
# HELP:
# ------------------------------------------------------------------------------

function printHelp {
	printLn
	echo -e "Usage: ${ESC_BOLD}${SCRIPT_NAME}${ESC_RESET}.sh [-h]"
	printLn
	echo -e "Builds the project and scriptifies the Fat-JAR to a Bash script containing and launching the executable JAR."
	printLn
	echo -e "${ESC_BOLD}-h${ESC_RESET}: Print this help"
	printLn
}

if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
	printHelp
	exit 0
fi

# ------------------------------------------------------------------------------
# MAIN:
# ------------------------------------------------------------------------------

moduleName=$(xml2 < "${SCRIPT_PATH}/pom.xml" 2>/dev/null | grep "/project/artifactId=")
moduleName="${moduleName#/project/artifactId=}"
if [ -z "${moduleName}" ]; then
	moduleName=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.artifactId -q -DforceStdout)
fi
echo -e "> Building <${ESC_BOLD}${moduleName}${ESC_RESET}>..."
cd "${SCRIPT_PATH}"
mvn clean install "$@"
if (($? != 0)); then
	echo -e "> Failed to build <${moduleName}>, aborting!" 1>&2;
	exit 1
fi

"${SCRIPT_PATH}/scriptify.sh"
if (($? != 0)); then
	echo -e "> ${ESC_BOLD}Failed to scriptify <${moduleName}>, aborting!${ESC_RESET}" 1>&2;
	exit 1
fi

"${SCRIPT_PATH}/jexefy.sh"
if (($? != 0)); then
	echo -e "> ${ESC_BOLD}Failed to jexefy <${moduleName}>, aborting!${ESC_RESET}" 1>&2;
	exit 1
fi

cd "${CURRENT_PATH}"