# LogRipper #
LogRipper is a utility designed to search for specific patterns or tokens within text files and directories, and to optionally split these files into smaller parts based on the found matches. This tool can be particularly useful for log analysis, where you need to extract relevant information from large log files.

# This is a Work in Progress #
LogRipper is currently under development and may not yet include all planned features or optimizations. As of right now, it is mainly a self-learning journey that I am taking. It may blossom into something useful one day. Contributions and critique are still welcome!

## Latest Update ##
- 2/13/2025 - managed to get the native-image built on Windows, however the graphical text looks weird. need to debug that.
- 2/14/2025 - the native executable still has incorrect characters somehow, but it at least looks presentable.
- 2/15/2025 - Fixed various aspects of file grepping, but still experimenting with read strategies for both large and small files.


## Features ##

### Text/Pattern Search ###
- **Pattern Matching**: Search for specific patterns or tokens in text files.
- **Case Insensitivity**: Option to perform case-insensitive searches.
- **Context Lines**: Include a specified number of lines before and after each match.
- **Match Limiting**: Limit the number of matches found.
- **Silent Mode**: Suppress output of matched lines, useful for counting matches only.
- **Count Only**: Output only the count of matches without showing the actual matches.
- **Line Numbering**: Optionally include line numbers in the output.

### Dir Stats ###
- **Folder Layout**: Crawl and mapp the folder/file structure of a given path.
- **Size Stat**: Include the size of each file/folder in the structure printout

### Configuration Files ###

You can create configuration files ("--init" or "--init path/to/your/config") from a template (found in "src/main/resources"), print a help text ("--help"), show system information ("--sysinfo") for support issues, regard being more verbose ("--verbose") or more quiet ("--quiet"), load a configuration from a specific location ("--config path/to/your/config"), all given that your command line syntax given uses the according predefined args syntax Term elements such as lines-before, lines-after, limit, ignore-case, silent, count, number, etc.


## Commands ##

| Command      | Property       | Alias | Usage                          | Description                                                     |
|--------------|----------------|-------|--------------------------------|-----------------------------------------------------------------|
| help         | --help         |       | `--help`                       | Shows all CLI help information.                                 |
| path         | --path         | -p    | `-p ~/some/path`               | Pass the path to a file or directory that should be processed.  |
| map          | --map          | -m    | `-p ~/some/path -m`            | Traverse the path and render a tree structure.                  |
| size         | --size         | -S    | `-p ~/some/path -m -S`         | Includes information about sizing, like file size or tree size. |
| sort         | --sort         | -o    | `-p ~/some/path -m -o`         | Sorts the mapped tree by size.                                  |
| grep         | --grep         | -g    | `-p ~/some/file -g someToken`  | Search for the provided token. Can use REGEX.                   |
| ignore-case  | --ignore-case  | -i    |                                |                                                                 |
| count        | --count        | -c    |                                |                                                                 |
| number       | --number       | -n    |                                |                                                                 |
| lines-before | --lines-before | -B    |                                |                                                                 |
| lines-after  | --lines-after  | -A    |                                |                                                                 |
| lines-around | --lines-around | -C    |                                |                                                                 |
| max-depth    | --max-depth    | -D    |                                |                                                                 |
| limit        | --limit        | -L    |                                |                                                                 |
| verbose      | --verbose      | -v    |                                |                                                                 |
| silent       | --silent       | -s    |                                |                                                                 |
| debug        | --debug        |       |                                |                                                                 |
| sysinfo      | --sysinfo      |       |                                |                                                                 |
| init         | --init         |       |                                |                                                                 |
| config       | --config       |       |                                |                                                                 |



# Build Instructions #

### Getting started ###

To get up and running, you use this archetype together with [`Maven`](https://maven.apache.org). Just change into the newly created folder and invoke `mvn` to build your new application:

```
cd logripper
mvn clean install
```

This will package your application into a [`fatjar`](https://stackoverflow.com/questions/19150811/what-is-a-fat-jar) which can be executed:

```
java -jar ./target/logripper-0.0.1.jar --help
```

When running a shell such as [`bash`](https://en.wikipedia.org/wiki/Bash_(Unix_shell)) then first (and once only) make sure the shell scripts are executable by changing the according file attributes and then invoke the `build.sh` script, so finally you can launch your application from inside the `target` folder:

```
chmod ug+x *.sh
```

Using the `build.sh` script will create a self-contained executable shell script by automatically invoking the `scriptify.sh` script after a successful build (see further down below for more information on the `scriptify.sh` script).
In addition the `build.sh` script also will create self-contained binary executables by automatically invoking the `jexefy.sh` script, also after a successful build (see further down below for more information on the `jexefy.sh` script). 

> The `build.sh` script will finish off by printing out the actual paths and the names of the created executables.

```
./build.sh
./target/logripper-launcher-x.y.z.sh --help
./target/logripper-launcher-x.y.z.exe --help
./target/logripper-launcher-x.y.z.elf --help
```

> In case your Artifact-ID `logripper` contains a slash ("-"), then just the portion after the slash is used for your executable binary shell script's name.

### Self contained executables ###

To build self-contained single binary applications for Linux and Windows, go as follows:

```
./bundle.sh
./target/logripper-bundle-x.y.z.elf --help
```

> In case your Artifact-ID `logripper` contains a slash ("-"), then just the portion after the slash is used for your executable binary's name.

On Windows, execute the file `logripper-bundle-x.y.z.exe` located in the `target` folder accordingly!

> The self-contained single binary applications (bundles) are generated by using [`Warp`](https://github.com/dgiagio/warp)!

### Windows installer ###

To build a Windows `logripper-installer-x86_64-x.y.z.msi` installer, go as follows:

```
./installer.sh
```

Then launch the `logripper-installer-x86_64-x.y.z.msi` installer located in the `target` folder to install your application alongside the bundled `JRE`.

> In case your Artifact-ID `logripper` contains a slash ("-"), then just the portion after the slash is used for your installer's name.

### Jexefy JAR ###

To create a self-contained binary executable `logripper-launcher-x86_64-x.y.z.elf` for Linux as well as `logripper-launcher-x86_64-x.y.z.exe` for Windows, consisting of a bootstrap binary executable portion and the actual Fat-JAR file, go as follows (automatically invoked when calling `build.sh`):

```
./jexefy.sh
./target/logripper-launcher-x86_64-x.y.z.exe --help
```

> On Linux, just invoke `logripper-launcher-x86_64-x.y.z.elf` instead of the `logripper-launcher-x86_64-x.y.z.exe` Windows variant. 

To run this self-contained binary executable, an installed `JRE` or `JDK` is required!

> In case your Artifact-ID `logripper` contains a slash ("-"), then just the portion after the slash is used for your executable binary shell script's name.

### Scriptify JAR ###

To create a self-contained executable `bash` script `logripper.sh` consisting of a bootstrap (`bash` script) portion and the actual Fat-JAR file, go as follows (automatically invoked when calling `build.sh`):

```
./scriptify.sh
./target/logripper-launcher-x.y.z.sh --help
```

To run this self-contained shell script, an installed `JRE` or `JDK` is required!

> In case your Artifact-ID `logripper` contains a slash ("-"), then just the portion after the slash is used for your executable binary shell script's name.

### Native image (GraalVM) ###

You may also create a native image using the GraalVM, feel free to skip steps 1) and 2) in case the provided sample files below `src/main/resources/META-INF/com.wizardry/logripper` work for you! Assuming you are in the root folder of your project:

1) Dry-run your `JAR` to get the according native-image JSON configurations:

```
java -agentlib:native-image-agent=config-output-dir=target -jar target/logripper-0.0.1.jar
```

2) Copy the generated files and tweak (sample files have already been provided):

```
cp target/jni-config.json src/main/resources/META-INF/com.wizardry/logripper/jni-config.json
cp target/proxy-config.json src/main/resources/META-INF/com.wizardry/logripper/proxy-config.json
cp target/reflect-config.json src/main/resources/META-INF/com.wizardry/logripper/reflect-config.json
cp target/resource-config.json src/main/resources/META-INF/com.wizardry/logripper/resource-config.json
cp target/serialization-config.json src/main/resources/META-INF/com.wizardry/logripper/serialization-config.json
```

3) Install GraalVM e.g. using [`SDKMAN!`](https://sdkman.io/) or install the [GraalVM](https://github.com/graalvm/graalvm-ce-builds/releases) manually

4) In case you installed the GraalVM manually, point the `GRAALVM_HOME` environment variable to your GraalVM base folder 

5) Then install the `native-image` tool for GraalVM

```
gu install native-image
```

6) On Debian install `build-essential`, `libz-dev` and `libfreetype6-dev` (in case of an "cannot find "-lfreetype" collect2: error"), on other distros do accordingly:

```
sudo apt install build-essential
sudo apt install libz-dev
sudo apt libfreetype6-dev'
```
   
7) Build with `Maven` using the `native` profile:

```
mvn clean install -P native-image
```

#### Additions for Windows ####

1) Install [Visual Studio Community](https://visualstudio.microsoft.com/de/downloads), choose (check) all `C/C++` modules

2) Install the [GraalVM](https://github.com/graalvm/graalvm-ce-builds/releases)

3) Point the `GRAALVM_HOME` environment variable to your GraalVM base folder

4) Then install the `native-image` tool for GraalVM

```
gu install native-image
```

5) Copy the `native-image.exe` file accordingly to a location on you `%PATH%` for the `native-image-maven-plugin` for find this tool:

```
copy %GRAALVM_HOME%\lib\svm\bin\native-image.exe %GRAALVM_HOME%\bin
```

6) Build in the `x64 Native Tools Command Prompt` (as of step 1) for `Maven` to see the `C/C++` Tools using the `native` profile:

```
mvn clean install -P native-image
```

### Resources ###

* *[refcodes-cli: Parse your args[]](http://www.refcodes.org/refcodes/refcodes-cli)*
* *[org.refcodes:refcodes-cli@Bitbucket](https://bitbucket.org/refcodes/refcodes-cli)*

### Terms and conditions ###

This code is written and provided by Siegfried Steiner, Munich, Germany. Feel free to use it as skeleton for your own applications. Make sure you have considered the license conditions of the included artifacts (see the provided `pom.xml` file).

The [`REFCODES.ORG`](http://www.refcodes.org/refcodes) artifacts used by this template are copyright (c) by Siegfried Steiner, Munich, Germany and licensed under some open source licenses; covered by the  [`refcodes-licensing`](https://bitbucket.org/refcodes/refcodes-licensing) ([`org.refcodes`](https://bitbucket.org/refcodes) group) artifact - evident in each artifact in question as of the `pom.xml` dependency included in such artifact.