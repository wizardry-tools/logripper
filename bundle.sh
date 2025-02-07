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
	echo -ne "Bundles the project creating a self-contained binary carrying its own JRE. "
	echo -ne "In case there is a <${ESC_BOLD}${SCRIPT_NAME}.conf${ESC_RESET}> file, then the ${ESC_BOLD}APP_NAME${ESC_RESET} property is used as ${ESC_BOLD}<appName>${ESC_RESET} and the ${ESC_BOLD}APP_VERSION${ESC_RESET} property is used as the ${ESC_BOLD}<appVersion>${ESC_RESET} (if not specified other on the command line). "
	echo -e "An empty version declaration or a version specified as ${ESC_BOLD}\"-1\"${ESC_RESET} denotes that no version is to be used in the resulting filename!"
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
# BUNDLE:
# ------------------------------------------------------------------------------

function bundle {
	local os="$1"
	local arch="$2"
	local warpArch="${os}-${arch/86_/}"
	local flavor="$3"
	local bundleBasePath="$4"
	local bundlePath="${bundleBasePath}/${os}_${arch}"
	local jreArchiveURL="$5"
	local bundleLauncherScript="$6"
	local jarFile="$7"
	local targetPath="$8"
	local bundlerCommand="$9"
	local bundleName="${10}"
	local bundleJrePath="${bundlePath}/jre"
	local jreArchiveName="$(basename ${jreArchiveURL})"
	local launcherScript="${bundleLauncherScript#bundle-}"
	mkdir -p "${bundlePath}"
	if [ ! -d "${bundleJrePath}" ]; then
		if [ ! -f "${bundleBasePath}/${jreArchiveName}" ]; then
			echo -e "> Downloading ${ESC_BOLD}${1^}${ESC_RESET} JRE <${ESC_BOLD}${jreArchiveName}${ESC_RESET}>..."
			wget -q "${jreArchiveURL}" -P "${bundleBasePath}"
			if [ $? -ne 0 ]; then
				echo -e "> ${ESC_BOLD}Unable to download JRE <${jreArchiveName}>!${ESC_RESET}>"
				cd "${CURRENT_PATH}"
				exit 1
			fi
		fi
		echo -e "> Unpacking <${jreArchiveName}>..."
		if [[ $jreArchiveName = *.zip ]] ; then
			unzip -qq "${bundleBasePath}/${jreArchiveName}" -d "${bundlePath}"
		elif [[ $jreArchiveName = *.tar.gz ]] ; then
			tar -xzf "${bundleBasePath}/${jreArchiveName}" -C "${bundlePath}"	
		else
			echo -e "> ${ESC_BOLD}Don't know how to extract JRE <${jreArchiveName}>!${ESC_RESET}>"
			cd "${CURRENT_PATH}"
			exit 1
		fi
		if [ $? -ne 0 ]; then
			echo -e "> Unable to extract JRE <${jreArchiveName}>!"
			cd "${CURRENT_PATH}"
			exit 1
		fi
		find "${bundlePath}" -maxdepth 1 -type d -name "*jre*" -exec mv {}  "${bundlePath}/jre" \;
		if [ ! -d "${bundleJrePath}" ]; then
			echo -e "> ${ESC_BOLD}Unable to setup JRE <${jreArchiveName}>!${ESC_RESET}"
			cd "${CURRENT_PATH}"
			exit 1
		fi
	fi
	if [ ! -f "${bundlePath}/${launcherScript}" ]; then
		cp "${SCRIPT_PATH}/${bundleLauncherScript}" "${bundlePath}/${launcherScript}"
	fi
	echo -e "> Copying JAR <${ESC_BOLD}${jarFile}${ESC_RESET}>..."
	cp "${jarFile}" "${bundlePath}/application.jar"
	echo -e "> Creating bundle <${ESC_BOLD}${bundleName}${ESC_RESET}> at <${ESC_BOLD}${targetPath}/${bundleName}${ESC_RESET}>..."
	if [[ "${flavor}" == "cygwin" ]]; then
		${bundlerCommand} --arch ${warpArch} --input_dir  "$(cygpath -wa "${bundlePath}")" --exec "${launcherScript}" --output "$(cygpath -wa "${targetPath}/${bundleName}")" > /dev/null
	else
		${bundlerCommand} --arch ${warpArch} --input_dir  "${bundlePath}" --exec "${launcherScript}" --output "${targetPath}/${bundleName}" > /dev/null
	fi
	if [ $? -ne 0 ]; then
		echo -e "> ${ESC_BOLD}Unable to create bundle <${bundleName}>!${ESC_RESET}"
		cd "${CURRENT_PATH}"
		exit 1
	fi
	echo -e "> Bundle <${ESC_BOLD}${bundleName}${ESC_RESET}> created at <${ESC_BOLD}${targetPath}${ESC_RESET}>!"
	printLn
}

# ------------------------------------------------------------------------------
# BUNDLE NAME:
# ------------------------------------------------------------------------------

function toBundleBaseName {
	local moduleName=$1
	local moduleVersion=$2
	local suffix=$3
	local arch=$4
	local argBinName=$5
	local confBinName=$6
	local argVersion=$7
	local confVersion=$8
	
	local binVersion="${argVersion}"
	if [ -z "${binVersion}" ]; then
		binVersion="${confVersion}"
		if [ -z "${binVersion}" ]; then
			binVersion="${moduleVersion}"
		fi
	fi
	local binName="${argBinName}"
	if [ ! -z "${binName}" ]; then
		if [ ! -z "${argVersion}" ] && [ "${argVersion}" != "-1" ] ; then
			binName="${binName}-${argVersion}"
		fi
	else
		binName=${confBinName}
		if [ ! -z "${binName}" ]; then
			if [[ "${binVersion}" == "-1" ]] ; then
				binName="${binName}"
			else
				binName="${binName}-${binVersion}"
			fi
		else
			binName="$(echo -e "${moduleName}" | cut -d- -f3- )"
			if [ -z "${binName}" ]; then
				binName="$(echo -e "${moduleName}" | cut -d- -f2- )"
				if [ -z "${binName}" ]; then
					binName="${moduleName}"
				fi
			fi
			if [[ "${binVersion}" == "-1" ]] ; then
				binName="${binName}-bundle-${arch}"
			else
				binName="${binName}-bundle-${arch}-${binVersion}"
			fi
		fi
	fi
	echo "${binName}.${suffix}"
}

# ------------------------------------------------------------------------------
# MAIN:
# ------------------------------------------------------------------------------

CONF_PATH="${SCRIPT_PATH}/${SCRIPT_NAME}.conf"
if [ -f "${CONF_PATH}" ]; then
. ${CONF_PATH}
else
	echo -e "> ${ESC_BOLD}Cannot find configuration at <${CONF_PATH}>!${ESC_RESET}"
	cd "${CURRENT_PATH}"
	exit 1
fi

cd "${SCRIPT_PATH}"

# As of https://stackoverflow.com/questions/3466166/how-to-check-if-running-in-cygwin-mac-or-linux:
unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine="Linux"; flavor="bash"; warp="linux-x64.warp-packer";;
    CYGWIN*)    machine="Windows"; flavor="cygwin"; warp="windows-x64.warp-packer.exe";;
    MINGW*)     machine="Windows"; flavor="mingw"; warp="windows-x64.warp-packer.exe";;
    *)          machine=""
esac

if [[ -z "${machine}" ]] ; then 
	echo -e "> ${ESC_BOLD}Cannot determine the machine's OS, aborting!${ESC_RESET}"
	cd "${CURRENT_PATH}"
	exit 1
fi

if [[ -z "${machine}" ]] ; then 
	echo -e "> ${ESC_BOLD}Cannot determine the machine's OS, aborting!${ESC_RESET}"
	cd "${CURRENT_PATH}"
	exit 1
fi

bundleBasePath="${SCRIPT_PATH}/.bundle"
mkdir -p "${bundleBasePath}"

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

echo -e "> Creating self-contained <${ESC_BOLD}${moduleName}${ESC_RESET}> binary on <${ESC_BOLD}${machine}${ESC_RESET}> using bundler command <${ESC_BOLD}${warp}${ESC_RESET}>..."

executable="${bundleBasePath}/${warp}"
if [ ! -f "${executable}" ]; then
	echo -e "> Downloading bundler <${ESC_BOLD}${warp}${ESC_RESET}> from <${ESC_BOLD}${WARP_PACKER_URL}${ESC_RESET}>..."
	wget -q "${WARP_PACKER_URL}/${warp}" -P "${bundleBasePath}"
	if [ $? -ne 0 ]; then
		echo -e "> ${ESC_BOLD}Unable to download bunlder <${warp}>!${ESC_RESET}"
		cd "${CURRENT_PATH}"
		exit 1
	fi
fi
chmod ug+x "${executable}"

printLn

jarFile=$(find "${targetPath}" -name "${moduleName}*.jar" ! -name "*tests.jar" ! -name "*sources.jar")
if [ ! -f "${jarFile}" ]; then
	echo -e "> ${ESC_BOLD}Unable to determine fat JAR for module <${moduleName}>!${ESC_RESET}"
	cd "${CURRENT_PATH}"
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

bundle "windows" "x86_64" "${flavor}" "${bundleBasePath}" "${WINDOWS_X86_64_JRE_URL}" "bundle-launcher.cmd" "${jarFile}" "${targetPath}" "${executable}" "$(toBundleBaseName "${moduleName}" "${moduleVersion}" "exe" "x86_64" "${argAppName}" "${APP_NAME}" "${argAppVersion}" "${APP_VERSION}")"
bundle "linux" "x86_64" "${flavor}" "${bundleBasePath}" "${LINUX_X86_64_JRE_URL}" "bundle-launcher.sh" "${jarFile}" "${targetPath}" "${executable}"  "$(toBundleBaseName "${moduleName}" "${moduleVersion}" "elf" "x86_64" "${argAppName}" "${APP_NAME}" "${argAppVersion}" "${APP_VERSION}")"

cd "${CURRENT_PATH}"

echo -e "> Done."
