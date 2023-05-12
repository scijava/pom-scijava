So, you want to run the mega-melt, but with some SNAPSHOTs built from branches?

Here is an example script that does so for the labkit-pixel-classification and
labkit-ui components. Copy this script into a file called `go`, change the
`adjust`... lines below to match the branches of your SNAPSHOT component(s),
  and then give it a try with `sh go`!

```shell
+#!/bin/sh
+
+# Fail on error.
+set -e
+
+# Generate the mega melt structure.
+tests/run.sh -s
+
+# Pull appropriate branches, build artifacts, and install them locally.
+adjust() {
+  dir=$1
+  remote=$2
+  branch=$3
+  cd "target/mega-melt/melting-pot/$dir"
+
+  # fetch the needed branch
+  git remote add upstream "$remote"
+  git fetch upstream
+
+  # discard pom.xml hacks
+  git checkout pom.xml
+
+  # switch to the needed branch
+  git checkout "upstream/$branch"
+
+  # reapply pom.xml hacks
+  mv pom.xml pom.xml.original &&
+  sed -E -e 's_(https?://maven.imagej.net|http://maven.scijava.org)_https://maven.scijava.org_g' \
+    -e 's_http://maven.apache.org/xsd_https://maven.apache.org/xsd_g' pom.xml.original > pom.xml ||
+    die "Failed to adjust pom.xml"
+  perl -0777 -i -pe 's/(<parent>\s*<groupId>org.scijava<\/groupId>\s*<artifactId>pom-scijava<\/artifactId>\s*<version>)[^\n]*/${1}999-mega-melt<\/version>/igs' pom.xml
+
+  # build and install the component
+  mvn -Denforcer.skip -DskipTests -Dmaven.test.skip -Dinvoker.skip clean install
+
+  cd - >/dev/null
+}
+adjust sc.fiji/labkit-pixel-classification https://github.com/ctrueden/labkit-pixel-classification bump-to-imglib2-6.1.0
+adjust sc.fiji/labkit-ui https://github.com/ctrueden/labkit-ui bump-to-imglib2-6.1.0
+
+# Run the mega melt!
+cd target/mega-melt/melting-pot
+./melt.sh 2>&1 | tee melt.log
```
