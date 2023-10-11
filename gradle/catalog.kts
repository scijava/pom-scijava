#!/usr/bin/env kscript

@file:DependsOn("org.scijava:scijava-common:2.94.1")

import java.io.File
import java.util.ArrayList
import org.scijava.util.POM
import org.scijava.util.XML
import org.w3c.dom.Element

val pom: POM = POM(File(args[0]))

val deps = pom.elements("//project/dependencyManagement/dependencies/dependency")

deps.forEach { dep ->
    val g: String = XML.cdata(dep, "groupId")
    val a: String = XML.cdata(dep, "artifactId")
    val v: String = XML.cdata(dep, "version")
    val exclusionsElement: List<Element> = XML.elements(dep, "exclusions")
    val exclusions: List<Element> = if (exclusionsElement.isEmpty()) emptyList() else XML.elements(exclusionsElement[0], "exclusion")
    println("$g : $a : $v -- # of exclusions = ${exclusions.size}")
}
