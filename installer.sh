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

TARGET_DIR="target"
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
	echo -e "Usage: ${ESC_BOLD}${SCRIPT_NAME}${ESC_RESET}.sh [ -h ] | [ <appName> [ <appVersion> ] ]"
	printLn
	echo -ne "Creates an MSI (*.msi) installer  including a JRE for Microsoft boxes. "
	echo -e "In case there is a <${ESC_BOLD}${SCRIPT_NAME}.conf${ESC_RESET}> file, then the ${ESC_BOLD}APP_NAME${ESC_RESET} property is used as ${ESC_BOLD}<appName>${ESC_RESET} and the ${ESC_BOLD}APP_VERSION${ESC_RESET} property is used as the ${ESC_BOLD}<appVersion>${ESC_RESET} (if not specified other on the command line)."
	printLn
	echo -e "   ${ESC_BOLD}<appName>${ESC_RESET}: The name of the file to be created (optional)."
	echo -e "${ESC_BOLD}<appVersion>${ESC_RESET}: The version of the file with <appName> to be created (optional)."
	echo -e "          ${ESC_BOLD}-h${ESC_RESET}: Print this help"
	printLn
}

if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
	printHelp
	exit 0
fi

# ------------------------------------------------------------------------------
# INSTALLER NAME:
# ------------------------------------------------------------------------------

function toInstallerBaseName {
	local moduleName=$1
	local arch=$2
	local argBinName=$3
	local confBinName=$4
	binName="${argBinName}"
	if [ -z "${binName}" ]; then
		binName=${confBinName}
		if [ -z "${binName}" ]; then
			binName="$(echo -e "${moduleName}" | cut -d- -f3- )"
			if [ -z "${binName}" ]; then
				binName="$(echo -e "${moduleName}" | cut -d- -f2- )"
				if [ -z "${binName}" ]; then
					binName="${moduleName}"
				fi
			fi
			binName="${binName}-installer-${arch}"
		fi
	fi
	echo "${binName}"
}

function toInstallerVersion {
	local moduleVersion=$1
	local argVersion=$2
	local confVersion=$3
	
	binVersion="${argVersion}"
	if [ -z "${binVersion}" ]; then
		binVersion="${confVersion}"
		if [ -z "${binVersion}" ]; then
			binVersion="${moduleVersion}"
			if [ -z "${binVersion}" ]; then
				binVersion="0.0.1"
			fi
		fi
	fi
	echo "${binVersion}"
}

# ------------------------------------------------------------------------------
# MAIN:
# ------------------------------------------------------------------------------

CONF_PATH="${SCRIPT_PATH}/${SCRIPT_NAME}.conf"
if [ -f "${CONF_PATH}" ]; then
. ${CONF_PATH}
fi

moduleName=$(xml2 < "${SCRIPT_PATH}/pom.xml" 2>/dev/null | grep "/project/artifactId=")
moduleName="${moduleName#/project/artifactId=}"
if [ -z "${moduleName}" ]; then
	moduleName=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.artifactId -q -DforceStdout)
fi

moduleVersion="$(xml2 <"${SCRIPT_PATH}/pom.xml" | grep "/project/version=")"
moduleVersion="${moduleVersion#/project/version=}"
moduleVersion="${moduleVersion/-SNAPSHOT}"
moduleVersion="${moduleVersion/-RELEASE}"
if [ -z "${moduleVersion}" ]; then
	moduleVersion=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout)
	if [ -z "${moduleVersion}" ]; then
		moduleVersion="X.Y.Z"
	else
		moduleVersion="${moduleVersion#/project/version=}"
		moduleVersion="${moduleVersion/-SNAPSHOT}"
		moduleVersion="${moduleVersion/-RELEASE}"
	fi
fi

targetPath="${SCRIPT_PATH}/${TARGET_DIR}"
if [ ! -e "${targetPath}" ] ; then
	echo -e "> ${ESC_BOLD}Folder <${targetPath}> does not exist, aborting!${ESC_RESET}" 1>&2;
	exit 1
fi
cd "${targetPath}"

jarFile=$(find . -name "${moduleName}*.jar" ! -name "*tests.jar" ! -name "*sources.jar")
if [ ! -e "${jarFile}" ] ; then
	echo -e "> ${ESC_BOLD}No JAR file found for module <${moduleName}> in folder <${targetPath}>, aborting!${ESC_RESET}" 1>&2;
	exit 1
fi
if command -v "unzip" &> /dev/null ; then
	isExecutable=$(unzip -q -c ${jarFile} META-INF/MANIFEST.MF | grep "Main-Class:")
	if [ -z "${isExecutable}" ] ; then
	    echo -e "> ${ESC_BOLD}JAR file at <${jarFile}> is not executable, aborting!${ESC_RESET}" 1>&2;
		exit 1
	fi
fi

argAppName="$1"
argAppVersion="$2"
if [ -v APP_VERSION ] && [ -z "${APP_VERSION}" ] ; then
	APP_VERSION=-1
fi

cpu64=$(java --version | grep -i '64-bit')
if [ -z "${cpu64}" ] ; then
	cpu="32"
else
	cpu="64"
fi
binName="$(toInstallerBaseName "${moduleName}" "x86_${cpu}" "${argAppName}" "${APP_NAME}")"
binVersion="$(toInstallerVersion "${moduleVersion}" "${argAppVersion}" "${APP_VERSION}")"

echo -e "> Creating MSI installer for <${ESC_BOLD}${binName}${ESC_RESET}> with version <${ESC_BOLD}${binVersion}${ESC_RESET}>..."
result=$(jpackage --win-dir-chooser --win-shortcut --win-console  --input "." --name "${binName}" --app-version "${binVersion}" --icon "../src/main/resources/application.ico" --main-jar "${jarFile}" --type msi 2>&1)
if (($? != 0)); then
	echo ""
	echo "${result}"
	echo ""
	echo -e "> ${ESC_BOLD}Cannot create installer, aborting!${ESC_RESET}"
else
	echo -e "> Created MSI installer <${ESC_BOLD}${binName}-${binVersion}.msi${ESC_RESET}> at <${ESC_BOLD}${targetPath}${ESC_RESET}>"
fi

cd "${CURRENT_PATH}"