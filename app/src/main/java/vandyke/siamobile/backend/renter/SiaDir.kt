/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.renter

import java.io.PrintStream

class SiaDir(override val name: String, override val parent: SiaDir?) : SiaNode() {
    private val files: ArrayList<SiaFile> = ArrayList()
    private val directories: ArrayList<SiaDir> = ArrayList()
    override val size: Long
        get() {
            var result: Long = 0
            files.forEach { result += it.size }
            directories.forEach { result += it.size }
            return result
        }
    val nodes: ArrayList<SiaNode>
        get() {
            val result = ArrayList<SiaNode>()
            result.addAll(directories)
            result.addAll(files)
            return result
        }

    fun addSiaFile(file: SiaFile) {
        addSiaFileHelper(file, file.siapath.split("/"), 0)
    }

    /**
     * @param file            the file being added
     * @param path            should be relevant to the directory this method is being called on
     * @param currentLocation the index in path that we're currently at
     */
    fun addSiaFileHelper(file: SiaFile, path: List<String>, currentLocation: Int) {
        if (path.size == 1 || path.size == currentLocation + 1) { // the file belongs in this directory
            files.add(file)
        } else {
            val currentName = path[currentLocation]
            var nextDir = getImmediateDir(currentName)
            if (nextDir == null) { // directory that is the next step in the path doesn't already exist
                nextDir = SiaDir(currentName, this)
                directories += nextDir
            }
            nextDir.addSiaFileHelper(file, path, currentLocation + 1)
        }
    }

    fun getImmediateDir(name: String): SiaDir? {
        for (node in directories)
            if (node.name == name)
                return node
        return null
    }

    fun getFullPath(pathSoFar: String): String {
        return parent?.getFullPath(name + "/" + pathSoFar) ?: pathSoFar
    }

    fun printAll(p: PrintStream, indent: Int) {
        indent(p, indent)
        p.println(name)
        for (file in files) {
            indent(p, indent + 1)
            p.println(file.name)
        }
        for (dir in directories)
            dir.printAll(p, indent + 1)
    }

    private fun indent(p: PrintStream, indent: Int) {
        for (i in 0..indent - 1) {
            if (i < indent - 1)
                p.print("   ")
            else
                p.print(" |-")
        }
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("SiaDir: ").append(name)
        result.append("\nFiles:\n")
        for (file in files) {
            result.append(file.name).append("\n")
        }
        result.append("Directories:\n")
        for (dir in directories) {
            result.append(dir.name).append("\n")
        }
        return result.toString()
    }
}