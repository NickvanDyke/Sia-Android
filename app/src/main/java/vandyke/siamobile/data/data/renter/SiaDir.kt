/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.data.renter

import java.io.PrintStream
import java.math.BigDecimal

class SiaDir(override val name: String, override val parent: SiaDir?) : SiaNode() {
    private val files: MutableList<SiaFile> = mutableListOf()
    private val directories: MutableList<SiaDir> = mutableListOf()

    val nodes: List<SiaNode>
        get() = directories + files

    override val size: BigDecimal // bytes
        get() {
            var result: BigDecimal = BigDecimal.ZERO
            files.forEach { result += it.size }
            directories.forEach { result += it.size }
            return result
        }

    val fullPath: List<SiaDir>
        get() = fullPathHelper(mutableListOf())

    val fullPathString: String
        get() = fullPathStringHelper("")


    /**
     * Returns the directory that's the given number of levels above the current one.
     * If it finds a null parent directory before reaching the desired height, it will
     * return the most recent non-null directory in the path it followed up the tree
     */
    fun getParentDirAt(level: Int): SiaDir {
        var current: SiaDir = this
        var height = level
        while (height > 0 && current.parent != null) {
            current = current.parent!!
            height--
        }
        return current
    }

    /**
     * Adds the SiaFile at it's siapath (relative to the directory this is being called on).
     * If the siapath has directories that don't exist, they will be created along the way.
     */
    fun addSiaFile(file: SiaFile) = addSiaFileHelper(file, 0)

    fun addSiaDir(dir: SiaDir) = addSiaDirHelper(dir, 0)

    /**
     * Adds an empty SiaDir at the given path (relative to the directory it's being called on).
     * Will not replace existing SiaDirs at the given location.
     */
    fun addEmptySiaDirAtPath(path: List<String>) = addSiaDirAtPathHelper(path, 0)

    fun addImmediateSiaDir(dir: SiaDir) = directories.add(dir)

    private fun addSiaFileHelper(file: SiaFile, currentLocation: Int) {
        val path = file.siapath.split("/")
        if (path.size == 1 || path.size == currentLocation + 1) { // the file belongs in this directory
            files.add(file)
        } else {
            val currentName = path[currentLocation]
            var nextDir = getImmediateDir(currentName)
            if (nextDir == null) { // directory that is the next step in the path doesn't already exist
                nextDir = SiaDir(currentName, this)
                directories += nextDir
            }
            nextDir.addSiaFileHelper(file, currentLocation + 1)
        }
    }

    private fun addSiaDirHelper(dir: SiaDir, currentLocation: Int) {
        val path = dir.fullPath
        if (path.size == 1 || path.size == currentLocation + 1) {
            directories.add(dir)
        } else {
            val currentDir = path[currentLocation]
            var nextDir = getImmediateDir(currentDir.name)
            if (nextDir == null) {
                nextDir = SiaDir(currentDir.name, this)
                directories.add(nextDir)
            }
            nextDir.addSiaDirHelper(dir, currentLocation + 1)
        }
    }

    private fun addSiaDirAtPathHelper(path: List<String>, currentLocation: Int) {
        println("at dir $name")
        if (path.size == 1 || path.size == currentLocation + 1) {
            println("reached the directory that the new dir belongs in")
            if (directories.firstOrNull { it.name == path[path.size - 1] } == null) {
                println("didn't find existing dir with same name - adding new one")
                directories.add(SiaDir(path[path.size - 1], this))
            }
        } else {
            println("searching for next dir in path")
            val currentDir = path[currentLocation]
            var nextDir = getImmediateDir(currentDir)
            if (nextDir == null) {
                println("next dir in path doesn't exist - creating it")
                nextDir = SiaDir(currentDir, this)
                directories.add(nextDir)
            }
            nextDir.addSiaDirAtPathHelper(path, currentLocation + 1)
        }
    }

    fun getImmediateDir(name: String): SiaDir? = directories.firstOrNull { it.name == name }

    private fun fullPathHelper(dirs: MutableList<SiaDir>): MutableList<SiaDir> {
        dirs.add(0, this)
        return parent?.fullPathHelper(dirs) ?: dirs
    }

    private fun fullPathStringHelper(pathSoFar: String): String {
        return parent?.fullPathStringHelper("$name/$pathSoFar") ?: "$name/$pathSoFar"
    }


//    fun contains(dir: SiaDir): Boolean {
//        if (directories.contains(dir)) return true
//        directories.forEach { if (it.contains(dir)) return true }
//        return false
//    }
//
//    override fun equals(other: Any?): Boolean { // TODO: this might not be the best way. might give false positives/negative sometimes
//        if (other is SiaDir) {
//            return this.fullPathString == other.fullPathString
//        } else {
//            return false
//        }
//    }


    /* functions for printing this directory's contents in a file explorer-like format */
    fun printAll(p: PrintStream = System.out, indent: Int = 0) {
        fun indent(p: PrintStream, indent: Int) {
            for (i in 0..indent - 1) {
                if (i < indent - 1)
                    p.print("   ")
                else
                    p.print(" |-")
            }
        }

        indent(p, indent)
        p.println(name)
        for (file in files) {
            indent(p, indent + 1)
            p.println(file.name)
        }
        for (dir in directories)
            dir.printAll(p, indent + 1)
    }

//    override fun toString(): String {
//        val result = StringBuilder()
//        result.append("SiaDir: ").append(name)
//        result.append("\nFiles:\n")
//        for (file in files) {
//            result.append(file.name).append("\n")
//        }
//        result.append("Directories:\n")
//        for (dir in directories) {
//            result.append(dir.name).append("\n")
//        }
//        return result.toString()
//    }
}