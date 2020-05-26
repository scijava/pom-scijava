#
# generate-mega-melt.py - Make a POM depending on everything in pom-scijava.
#

import os, re, sys
from xml.dom import minidom

def child(node, tag):
    nodes = node.getElementsByTagName(tag)
    return None if len(nodes) == 0 else nodes[0]

script_dir = os.path.dirname(os.path.realpath(__file__)) if __file__ else '.'
out = minidom.parse(os.path.join(script_dir, 'mega-melt-template.xml'))
out.getElementsByTagName('project')[0].appendChild(out.createElement('dependencies'))
outDeps = out.getElementsByTagName('dependencies')[0]

psj = minidom.parse(os.path.join(script_dir, '..', 'pom.xml'))
depMgmt = psj.getElementsByTagName('dependencyManagement')[0]
deps = depMgmt.getElementsByTagName('dependencies')[0]
depList = deps.getElementsByTagName('dependency')

# Artifacts to exclude from the mega melt.
ignoredArtifacts = [
    # TEMP: Until scenerygraphics/scenery#314 is addressed.
    'scenery',
    # NB: The following artifacts have messy dependency trees.
    # Too many problems to test as part of the mega-melt.
    # See WARNING block in pom-scijava's pom.xml for details.
    'imagej-server',
    'spark-core_2.11',
    # NB: Skip scijava forks of third-party projects.
    # These are very stable, with few/no dependencies, and
    # don't need to be retested as pom-scijava evolves.
    'j3dcore',
    'j3dutils',
    'jep',
    'junit-benchmarks',
    'vecmath',
    # NB: The following artifacts require native libraries to
    # build/test successfully, which might not be installed.
    'n5-blosc',
    'n5-zarr',
    # NB: All the SWT platform JARs have the same classes.
    # The current platform will be brought in transitively.
    'org.eclipse.swt.cocoa.macosx',
    'org.eclipse.swt.cocoa.macosx.x86_64',
    'org.eclipse.swt.gtk.aix.ppc',
    'org.eclipse.swt.gtk.aix.ppc64',
    'org.eclipse.swt.gtk.hpux.ia64',
    'org.eclipse.swt.gtk.linux.ppc',
    'org.eclipse.swt.gtk.linux.ppc64',
    'org.eclipse.swt.gtk.linux.s390',
    'org.eclipse.swt.gtk.linux.s390x',
    'org.eclipse.swt.gtk.linux.x86',
    'org.eclipse.swt.gtk.linux.x86_64',
    'org.eclipse.swt.gtk.solaris.sparc',
    'org.eclipse.swt.gtk.solaris.x86',
    'org.eclipse.swt.win32.win32.x86',
    'org.eclipse.swt.win32.win32.x86_64',
    # NB: All SLF4J bindings have the same classes.
    # We'll rely on logback-classic being present here.
    'slf4j-jcl',
    'slf4j-jdk14',
    'slf4j-nop',
    'slf4j-simple',
    # NB: Cannot include both commons-logging and jcl-over-slf4j;
    # see: http://www.slf4j.org/codes.html#jclDelegationLoop
    'jcl-over-slf4j'
]

for dep in depList:
    # Harvest relevant information (ignore exclusions and version).
    groupId = child(dep, 'groupId')
    artifactId = child(dep, 'artifactId')
    classifier = child(dep, 'classifier')
    scope = child(dep, 'scope')

    if artifactId.firstChild.data in ignoredArtifacts:
        continue

    outDep = out.createElement('dependency')
    outDep.appendChild(groupId)
    outDep.appendChild(artifactId)
    if classifier:
        outDep.appendChild(classifier)
    if scope:
        outDep.appendChild(scope)
    outDeps.appendChild(outDep)

# Filter XML through a hacky substitution to avoid unwanted whitespace.
xml = re.sub('\n[\n\r\t]*\n', '\n', out.toprettyxml())

outDir = sys.argv[1] if len(sys.argv) > 1 else 'mega-melt'
try:
    os.mkdir(outDir)
except:
    pass

with open(os.path.join(outDir, 'pom.xml'), 'w') as outPOM:
    outPOM.write(xml)
