#!/bin/sh

# ============================================================================
# melting-pot.sh
# ============================================================================
# Tests all components of a project affected by changes in its dependencies.
#
# First, an anecdote illustrating the problem this script solves:
#
# Suppose you have a large application, org:app:1.0.0, with many dependencies:
# org:foo:1.2.3, org:bar:3.4.5, and many others.
#
# Now suppose you make some changes to foo, and want to know whether deploying
# them (i.e., releasing a new foo and updating app to depend on that release)
# will break the app. So you manually update your local copy of app to depend
# on org:foo:1.3.0-SNAPSHOT, and run the build (including tests, of course).
#
# The build passes, but this alone is insufficient: org:bar:3.4.5 also depends
# on org:foo:1.2.3, so you manually update bar to use org:foo:1.3.0-SNAPSHOT,
# then build bar to verify that it also is not broken by the update.
#
# This process quickly becomes very tedious when there are dozens of
# components of app which all depend on foo.
#
# And more importantly, testing each component individually in this manner is
# still insufficient to determine whether all of them will truly work together
# at runtime, where only a single version of each component is deployed.
#
# For example: suppose org:bar:3.4.5 depends on org:lib:8.0.0, while
# org:foo:1.2.3 depends on org:lib:7.0.0. The relevant facts are:
#
# * Your new foo (org:foo:1.3.0-SNAPSHOT) builds against lib 7, and portions
#   of it rely on lib-7-specific API.
#
# * The bar component pinned to foo 1.3.0-SNAPSHOT builds against lib 8; it
#   compiles with passing tests because bar only invokes portions of the foo
#   API which do not require lib-7-specific API.
#
# In this scenario, it is lib 8 that is actually deployed at runtime with the
# app, so parts of foo will be broken, even though both foo and bar build with
# passing tests individually.
#
# This "melting pot" build seeks to overcome many of these issues by unifying
# all components of the app into a single multi-module build, with all
# versions uniformly pinned to the ones that will actually be deployed at
# runtime.
#
# This goal is achieved by synthesizing a multi-module build including all
# affected components (or optionally, all components period) of the specific
# project, and then executing a Maven build with uniformly overridden versions
# of all components to the ones resolved for the project itself.
#
# IMPORTANT IMPLEMENTATION DETAIL! The override works by setting a version
# property for each component of the form "artifactId.version"; it is assumed
# that all components declare their dependencies using version properties of
# this form. E.g.:
#
#   <dependency>
#     <groupId>com.google.guava</groupId>
#     <artifactId>guava</artifactId>
#     <version>${guava.version}</version>
#   </dependency>
#
# Using dependencyManagement is fine too, as long as it then uses this pattern
# to declare the versions as properties, which can be overridden.
#
# Any dependency which does not declare a version property matching this
# assumption will not be properly overridden in the melting pot!
#
# Author: Curtis Rueden
# ============================================================================

# -- Functions --

stderr() {
	>&2 echo "$@"
}

debug() {
	test "$debug" &&
		stderr "+ $@"
}

info() {
	test "$verbose" &&
		stderr "[INFO] $@"
}

warn() {
	stderr "[WARNING] $@"
}

error() {
	stderr "[ERROR] $@"
}

die() {
	error $1
	exit $2
}

unknownArg() {
	error "Unknown option: $@"
	usage=1
}

checkPrereqs() {
	while [ $# -gt 0 ]
	do
		which $1 > /dev/null 2> /dev/null
		test $? -ne 0 && die "Missing prerequisite: $1" 255
		shift
	done
}

verifyPrereqs() {
	checkPrereqs git mvn xmllint
	git --version | grep -q 'git version 2' ||
		die "Please use git v2.x; older versions (<=1.7.9.5 at least) mishandle 'git clone <tag> --depth 1'" 254
}

parseArguments() {
	while [ $# -gt 0 ]
	do
		case "$1" in
			-b|--branch)
				branch="$2"
				shift
				;;
			-c|--changes)
				test "$changes" && changes="$changes,$2" || changes="$2"
				shift
				;;
			-i|--includes)
				test "$includes" && includes="$includes,$2" || includes="$2"
				shift
				;;
			-e|--excludes)
				test "$excludes" && excludes="$excludes,$2" || excludes="$2"
				shift
				;;
			-r|--remoteRepos)
				test "$remoteRepos" && remoteRepos="$remoteRepos,$2" || remoteRepos="$2"
				shift
				;;
			-l|--localRepo)
				repoBase="$2"
				shift
				;;
			-o|--outputDir)
				outputDir="$2"
				shift
				;;
			-p|--prune)
				prune=1
				;;
			-v|--verbose)
				verbose=1
				;;
			-d|--debug)
				debug=1
				;;
			-f|--force)
				force=1
				;;
			-s|--skipBuild)
				skipBuild=1
				;;
			-h|--help)
				usage=1
				;;
			-*)
				unknownArg "$1"
				;;
			*)
				test -z "$project" && project="$1" ||
					unknownArg "$1"
				;;
		esac
		shift
	done

	test -z "$project" -a -z "$usage" &&
		error "No project specified!" && usage=1

	if [ "$usage" ]
	then
		echo "Usage: $(basename "$0") <project> [-b <branch>] [-c <GAVs>] \\
       [-i <GAs>] [-e <GAs>] [-r <URLs>] [-l <dir>] [-o <dir>] [-pvfsh]

<project>
    The project to build, including dependencies, with consistent versions.
    Can be either G:A:V form, or a local directory pointing at a project.
-b, --branch
    Override the branch/tag of the project to build. By default,
    the branch used will be the tag named \"artifactId-version\".
-c, --changes
    Comma-separated list of GAVs to inject into the project, replacing
    normal versions. E.g.: \"com.mycompany:myartifact:1.2.3-SNAPSHOT\"
-i, --includes
    Comma-separated list of GAs (no version; wildcards OK for G or A) to
    include in the build. All by default. E.g.: \"mystuff:*,myotherstuff:*\"
-e, --excludes
    Comma-separated list of GAs (no version; wildcards OK for G or A) to
    exclude from the build. E.g.: \"mystuff:extraneous,mystuff:irrelevant\"
-r, --remoteRepos
    Comma-separated list of additional remote Maven repositories to check
    for artifacts, in the format id::[layout]::url or just url.
-l, --localRepos
    Overrides the directory of the Maven local repository cache.
-o, --outputDir
    Overrides the output directory. The default is \"melting-pot\".
-p, --prune
    Build only the components which themselves depend on a changed
    artifact. This will make the build much faster, at the expense of
    not fully testing runtime compatibility across all components.
-v, --verbose
    Enable verbose/debugging output.
-f, --force
    Wipe out the output directory if it already exists.
-s, --skipBuild
    Skips the final build step. Useful for automated testing.
-h, --help
    Display this usage information.

--== Example ==--

    sh melting-pot.sh net.imagej:imagej-common:0.15.1 \\
        -r https://maven.scijava.org/content/groups/public \\
        -c org.scijava:scijava-common:2.44.2 \\
        -i 'org.scijava:*,net.imagej:*,net.imglib2:*,io.scif:*' \\
        -e net.imglib2:imglib2-roi \\
        -v -f -s

This command tests net.imagej:imagej-common:0.15.1 along with all of its
dependencies, pulled from its usual SciJava Maven repository location.

The -c flag is used to override the org.scijava:scijava-common
dependency to use version 2.44.2 instead of its declared version 2.42.0.

Note that such overrides do not need to be release versions; you can
also test SNAPSHOTs the same way.

The -i option is used to include all imagej-common dependencies with
groupIds org.scijava, net.imagej, net.imglib2 and io.scif in the pot.

The -e flag is used to exclude net.imglib2:imglib2-roi from the pot.
"
		exit 1
	fi

	# If project is a local directory path, get its absolute path.
	test -d "$project" && project=$(cd "$project" && pwd)

	# Assign default parameter values.
	test "$outputDir" || outputDir="melting-pot"
	test "$repoBase" || repoBase="$HOME/.m2/repository"
}

createDir() {
	test -z "$force" -a -e "$1" &&
		die "Directory already exists: $1" 2

	rm -rf "$1"
	mkdir -p "$1"
	cd "$1"
}

groupId() {
	echo "${1%%:*}"
}

artifactId() {
	local result="${1#*:}" # strip groupId
	echo "${result%%:*}"
}

version() {
	local result="${1#*:}" # strip groupId
	case "$result" in
		*:*)
			result="${result#*:}" # strip artifactId
			case "$result" in
				*:*:*:*)
					# G:A:P:C:V:S
					result="${result#*:}" # strip packaging
					result="${result#*:}" # strip classifier
					;;
				*:*:*)
					# G:A:P:V:S
					result="${result#*:}" # strip packaging
					;;
				*)
					# G:A:V or G:A:V:?
					;;
			esac
			echo "${result%%:*}"
			;;
	esac
}

classifier() {
	local result="${1#*:}" # strip groupId
	case "$result" in
		*:*)
			result="${result#*:}" # strip artifactId
			case "$result" in
				*:*:*:*)
					# G:A:P:C:V:S
					result="${result#*:}" # strip packaging
					;;
				*:*:*)
					# G:A:P:V:S
					result=""
					;;
				*:*)
					# G:A:V:C
					result="${result#*:}" # strip version
					;;
				*)
					# G:A:V
					result=""
					;;
			esac
			echo "${result%%:*}"
			;;
	esac
}

# Converts the given GAV into a path in the local repository cache.
repoPath() {
	local gPath="$(echo "$(groupId "$1")" | tr :. /)"
	local aPath="$(artifactId "$1")"
	local vPath="$(version "$1")"
	echo "$repoBase/$gPath/$aPath/$vPath"
}

# Gets the path to the given GAV's POM file in the local repository cache.
pomPath() {
	local pomFile="$(artifactId "$1")-$(version "$1").pom"
	echo "$(repoPath "$1")/$pomFile"
}

# Fetches the POM for the given GAV into the local repository cache.
downloadPOM() {
	local g="$(groupId "$1")"
	local a="$(artifactId "$1")"
	local v="$(version "$1")"
	debug "mvn dependency:get \\
	-DrepoUrl=\"$remoteRepos\" \\
	-DgroupId=\"$g\" \\
	-DartifactId=\"$a\" \\
	-Dversion=\"$v\" \\
	-Dpackaging=pom"
	mvn dependency:get \
		-DrepoUrl="$remoteRepos" \
		-DgroupId="$g" \
		-DartifactId="$a" \
		-Dversion="$v" \
		-Dpackaging=pom > /dev/null ||
	die "Problem fetching $g:$a:$v from $remoteRepos" 4
}

# Gets the POM path for the given GAV, ensuring it exists locally.
pom() {
	local pomPath="$(pomPath "$1")"
	test -f "$pomPath" || downloadPOM "$1"
	test -f "$pomPath" || die "Cannot access POM: $pomPath" 9
	echo "$pomPath"
}

# For the given XML file on disk ($1), gets the value of the
# specified XPath expression of the form "//$2/$3/$4/...".
xpath() {
	local xmlFile="$1"
	shift
	local xpath="/"
	while [ $# -gt 0 ]
	do
		# NB: Ignore namespace issues; see: http://stackoverflow.com/a/8266075
		xpath="$xpath/*[local-name()='$1']"
		shift
	done
	debug "xmllint --xpath \"$xpath\" \"$xmlFile\""
	xmllint --xpath "$xpath" "$xmlFile" 2> /dev/null |
		sed -E 's/^[^>]*>(.*)<[^<]*$/\1/'
}

# For the given GAV ($1), recursively gets the value of the
# specified XPath expression of the form "//$2/$3/$4/...".
pomValue() {
	local pomPath="$(pom "$1")"
	test "$pomPath" || die "Cannot discern POM path for $1" 6
	shift
	local value="$(xpath "$pomPath" $@)"
	if [ "$value" ]
	then
		echo "$value"
	else
		# Path not found in POM; look in the parent POM.
		local pg="$(xpath "$pomPath" project parent groupId)"
		if [ "$pg" ]
		then
			# There is a parent POM declaration in this POM.
			local pa="$(xpath "$pomPath" project parent artifactId)"
			local pv="$(xpath "$pomPath" project parent version)"
			pomValue "$pg:$pa:$pv" $@
		fi
	fi
}

# Gets the SCM URL for the given GAV.
scmURL() {
	pomValue "$1" project scm connection | sed -E 's/^scm:git://'
}

# Gets the SCM tag for the given GAV.
scmTag() {
	local tag=$(pomValue "$1" project scm tag)
	if [ -z "$tag" -o "$tag" = "HEAD" ]
	then
		# The <scm><tag> value was not set properly,
		# so we try to guess the tag naming scheme. :-/
		warn "$1: improper scm tag value; scanning remote tags..."
		local a=$(artifactId "$1")
		local v=$(version "$1")
		local scmURL="$(scmURL "$1")"
		local allTags=$(git ls-remote --tags "$scmURL" | sed 's/.*refs\/tags\///' ||
			error "$1: Invalid scm url: $scmURL")
		for tag in "$a-$v" "$v" "v$v"
		do
			echo "$allTags" | grep -q "^$tag$" && {
				info "$1: inferred tag: $tag"
				echo "$tag"
				return
			}
		done
		error "$1: inscrutable tag scheme"
	else
		echo "$tag"
	fi
}

# Fetches the source code for the given GAV. Returns the directory.
retrieveSource() {
	local scmURL="$(scmURL "$1")"
	test "$scmURL" || die "Cannot glean SCM URL for $1" 10
	local scmBranch
	test "$2" && scmBranch="$2" || scmBranch="$(scmTag "$1")"
	local dir="$(groupId "$1")/$(artifactId "$1")"
	debug "git clone \"$scmURL\" --branch \"$scmBranch\" --depth 1 \"$dir\""
	git clone "$scmURL" --branch "$scmBranch" --depth 1 "$dir" 2> /dev/null ||
		die "Could not fetch project source for $1" 3

	# Now verify that the cloned pom.xml contains the expected version!
	local expectedVersion=$(version "$1")
	local actualVersion=$(xpath "$dir/pom.xml" project version)
	test "$expectedVersion" = "$actualVersion" ||
		die "POM for $1 contains wrong version: $actualVersion" 14

	echo "$dir"
}

# Gets the list of dependencies for the project in the CWD.
deps() {
	cd "$1"
	debug "mvn dependency:list"
	local depList="$(mvn -B dependency:list)" ||
		die "Problem fetching dependencies!" 5
	echo "$depList" | grep '^\[INFO\]    [^ ]' |
		sed 's/\[INFO\]    //' | sed 's/  *(optional) *$//' | sort
	cd - > /dev/null
}

# Checks whether the given GA(V) matches the specified filter pattern.
gaMatch() {
	local ga="$1"
	local filter="$2"
	local g="$(groupId "$ga")"
	local a="$(artifactId "$ga")"
	local fg="$(groupId "$filter")"
	local fa="$(artifactId "$filter")"
	test "$fg" = "$g" -o "$fg" = "*" || return
	test "$fa" = "$a" -o "$fa" = "*" || return
	echo 1
}

# Determines whether the given GA(V) version is being overridden.
isChanged() {
	local IFS=","

	local change
	for change in $changes
	do
		test "$(gaMatch "$1" "$change")" && echo 1 && return
	done
}

# Determines whether the given GA(V) meets the inclusion criteria.
isIncluded() {
	# do not include the changed artifacts we are testing against
	test "$(isChanged "$1")" && return

	local IFS=","

	# ensure GA is not excluded
	local exclude
	for exclude in $excludes
	do
		test "$(gaMatch "$1" "$exclude")" && return
	done

	# ensure GA is included
	test -z "$includes" && echo 1 && return
	local include
	for include in $includes
	do
		test "$(gaMatch "$1" "$include")" && echo 1 && return
	done
}

# Deletes components which do not depend on a changed GAV.
pruneReactor() {
	local dir
	for dir in */*
	do
		info "Checking relevance of component $dir"
		local deps="$(deps "$dir")"
		test "$deps" || die "Cannot glean dependencies for '$dir'" 8

		# Determine whether the component depends on a changed GAV.
		local keep
		unset keep
		local dep
		for dep in $deps
		do
			test "$(isChanged "$dep")" && keep=1 && break
		done

		# If the component is irrelevant, prune it.
		if [ -z "$keep" ]
		then
			info "Pruning irrelevant component: $dir"
			rm -rf "$dir"
		fi
	done
}

# Tests if the given directory contains the appropriate source code.
isProject() {
	local a="$(xpath "$1/pom.xml" project artifactId)"
	test "$1" = "LOCAL/PROJECT" -o "$a" = "$(basename "$1")" && echo 1
}

# Generates an aggregator POM for all modules in the current directory.
generatePOM() {
	echo '<?xml version="1.0" encoding="UTF-8"?>' > pom.xml
	echo '<project xmlns="http://maven.apache.org/POM/4.0.0"' >> pom.xml
	echo '	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> pom.xml
	echo '	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0' >> pom.xml
	echo '		http://maven.apache.org/xsd/maven-4.0.0.xsd">' >> pom.xml
	echo '	<modelVersion>4.0.0</modelVersion>' >> pom.xml
	echo >> pom.xml
	echo '	<groupId>melting-pot</groupId>' >> pom.xml
	echo '	<artifactId>melting-pot</artifactId>' >> pom.xml
	echo '	<version>0.0.0-SNAPSHOT</version>' >> pom.xml
	echo '	<packaging>pom</packaging>' >> pom.xml
	echo >> pom.xml
	echo '	<name>Melting Pot</name>' >> pom.xml
	echo >> pom.xml
	echo '	<modules>' >> pom.xml
	local dir
	for dir in */*
	do
		if [ "$(isProject "$dir")" ]
		then
			echo "		<module>$dir</module>" >> pom.xml
		else
			# Check for a child component of a multi-module project.
			local childDir="$dir/$(basename "$dir")"
			test "$(isProject "$childDir")" &&
				echo "		<module>$childDir</module>" >> pom.xml
		fi
	done
	echo '	</modules>' >> pom.xml
	echo '</project>' >> pom.xml
}

# Generates melt.sh script for all modules in the current directory.
generateScript() {
	echo '#!/bin/sh' > melt.sh
	echo 'trap "exit" INT' >> melt.sh
	echo 'echo "Melting the pot..."' >> melt.sh
	echo 'dir=$(pwd)' >> melt.sh
	echo 'failCount=0' >> melt.sh
	echo 'for f in \' >> melt.sh
	echo "  $projectDir \\" >> melt.sh
	local dir
	for dir in */*
	do
		if [ "$(isProject "$dir")" ]
		then
			echo "	$dir \\" >> melt.sh
		else
			# Check for a child component of a multi-module project.
			local childDir="$dir/$(basename "$dir")"
			test "$(isProject "$childDir")" &&
				echo "	$childDir \\" >> melt.sh
		fi
	done
	echo >> melt.sh
	echo 'do (' >> melt.sh
	echo '	cd "$f"' >> melt.sh
	echo '	sh "$dir/build.sh" > build.log 2>&1 &&' >> melt.sh
	echo '		echo "[SUCCESS] $f" || {' >> melt.sh
	echo '			echo "[FAILURE] $f"' >> melt.sh
	echo '			failCount=$((failCount+1))' >> melt.sh
	echo '		}' >> melt.sh
	echo ') done' >> melt.sh
	echo 'test "$failCount" -gt 255 && failCount=255' >> melt.sh
	echo 'exit "$failCount"' >> melt.sh
}

# Creates and tests an appropriate multi-module reactor for the given project.
# All relevant dependencies which match the inclusion criteria are linked into
# the multi-module build, with each changed GAV overridding the originally
# specified version for the corresponding GA.
meltDown() {
	# Fetch the project source code.
	if [ -d "$1" ]
	then
		# Use local directory for the specified project.
		test -d "$1" || die "No such directory: $1" 11
		test -f "$1/pom.xml" || die "Not a Maven project: $1" 12
		info "Local Maven project: $1"
#		mkdir -p "LOCAL"
#		local dir="LOCAL/PROJECT"
#		ln -s "$1" "$dir"
		projectDir=$1
	else
		# Treat specified project as a GAV.
		info "Fetching project source"
		retrieveSource "$1" "$branch"
	fi

	# Get the project dependencies.
	info "Determining project dependencies"
	local deps="$(deps "$dir")"
	test "$deps" || die "Cannot glean project dependencies" 7

	local args="-Denforcer.skip"

	# Process the dependencies.
	info "Processing project dependencies"
	local dep
	for dep in $deps
	do
		local g="$(groupId "$dep")"
		local a="$(artifactId "$dep")"
		local v="$(version "$dep")"
		local c="$(classifier "$dep")"
		test -z "$c" || continue # skip secondary artifacts
		local gav="$g:$a:$v"

		test -z "$(isChanged "$gav")" &&
			args="$args \\\\\n  -D$a.version=$v"

		if [ "$(isIncluded "$gav")" ]
		then
			info "$a: fetching component source"
			dir="$(retrieveSource "$gav")"
		fi
	done

	# Override versions of changed GAVs.
	info "Processing changed components"
	local TLS=,
	local gav
	for gav in $changes
	do
		local a="$(artifactId "$gav")"
		local v="$(version "$gav")"
		args="$args \\\\\n  -D$a.version=$v"
	done
	unset TLS

	# Prune the build, if applicable.
	test "$prune" && pruneReactor

	# Generate build scripts.
	info "Generating build scripts"
	generatePOM
	echo "mvn $args \\\\\n  test \$@" > build.sh
	generateScript

	# Build everything.
	if [ "$skipBuild" ]
	then
		info "Skipping the build; run melt.sh to do it."
	else
		info "Building the project!"
		# NB: All code is fresh; no need to clean.
		sh melt.sh || die "Melt failed" 13
	fi

	info "Melt complete: $1"
}

# -- Main --

verifyPrereqs
parseArguments $@
createDir "$outputDir"
meltDown "$project"
