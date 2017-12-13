/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.data.renter

import vandyke.siamobile.ui.renter.view.RenterFragment
import java.io.PrintStream
import java.math.BigDecimal

sealed class SiaNode {
    abstract val parent: SiaDir?
    abstract val name: String
    abstract val size: BigDecimal
}

data class SiaFile(val siapath: String = "",
                   val localpath: String = "",
                   val filesize: BigDecimal = BigDecimal.ZERO, // bytes
                   val available: Boolean = false,
                   val renewing: Boolean = false,
                   val redundancy: Int = 0,
                   val uploadedbytes: Long = 0,
                   val uploadprogress: Int = 0,
                   val expiration: Long = 0) : SiaNode() {
    override lateinit var parent: SiaDir
    override val name by lazy { siapath.substring(siapath.lastIndexOf("/") + 1) }
    override val size: BigDecimal
        get() = filesize
}

class SiaDir(override val name: String, override val parent: SiaDir?) : SiaNode() {
    val files: MutableList<SiaFile> = mutableListOf()
    val dirs: MutableList<SiaDir> = mutableListOf()

    /**
     * This is purely for iterating over - modifying it will not affect the SiaDir's files and dirs
     */
    val nodes
        get() = dirs + files

    override val size: BigDecimal // bytes
        get() {
            var result: BigDecimal = BigDecimal.ZERO
            nodes.forEach { result += it.size }
            return result
        }

    val path: List<SiaDir>
        get() = pathHelper(mutableListOf())

    val pathString: String
        get() = pathStringHelper("").replace("$name/", "$name")

    val pathStringWithoutRoot: String
        get() = pathStringHelper("").replace("${RenterFragment.ROOT_DIR_NAME}/", "").replace("$name/", "$name")

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
     * Adds the SiaNode at it's path (relative to the directory this is being called on).
     * If the path has dirs that don't exist, they will be created and added along the way.
     */
    fun addSiaNode(node: SiaNode) = addSiaNodeHelper(node, 0)

    /**
     * Adds an empty SiaDir at the given path (relative to the directory it's being called on).
     * Will not replace existing SiaDirs at the given location.
     */
    fun addEmptySiaDirAtPath(path: List<String>) = addSiaDirAtPathHelper(path, 0)

    private fun addSiaNodeHelper(node: SiaNode, currentLocation: Int) {
        val path = when (node) {
            is SiaFile -> node.siapath.split("/")
            is SiaDir ->  node.pathString.split("/")
        }
        if (path.size == 1 || path.size == currentLocation + 1) { // the node belongs in this directory
            when (node) {
                is SiaFile -> {
                    node.parent = this
                    files.add(node)
                }
                is SiaDir ->  dirs.add(node)
            }
        } else {
            val currentName = path[currentLocation]
            var nextDir = getImmediateDir(currentName)
            if (nextDir == null) { // directory that is the next step in the path doesn't already exist
                nextDir = SiaDir(currentName, this)
                dirs.add(nextDir)
            }
            nextDir.addSiaNodeHelper(node, currentLocation + 1)
        }
    }

    private fun addSiaDirAtPathHelper(path: List<String>, currentLocation: Int) {
        if (path.size == 1 || path.size == currentLocation + 1) {
            if (dirs.firstOrNull { it.name == path[path.size - 1] } == null) {
                dirs.add(SiaDir(path[path.size - 1], this))
            }
        } else {
            val currentDir = path[currentLocation]
            var nextDir = getImmediateDir(currentDir)
            if (nextDir == null) {
                nextDir = SiaDir(currentDir, this)
                dirs.add(nextDir)
            }
            nextDir.addSiaDirAtPathHelper(path, currentLocation + 1)
        }
    }

    fun getImmediateDir(name: String): SiaDir? = dirs.firstOrNull { it.name == name }

    private fun pathHelper(dirs: MutableList<SiaDir>): MutableList<SiaDir> {
        dirs.add(0, this)
        return parent?.pathHelper(dirs) ?: dirs
    }

    private fun pathStringHelper(pathSoFar: String): String {
        return parent?.pathStringHelper("$name/$pathSoFar") ?: "$name/$pathSoFar"
    }


//    fun contains(dir: SiaDir): Boolean {
//        if (dirs.contains(dir)) return true
//        dirs.forEach { if (it.contains(dir)) return true }
//        return false
//    }
//
//    override fun equals(other: Any?): Boolean { // TODO: this might not be the best way. might give false positives/negative sometimes
//        if (other is SiaDir) {
//            return this.pathString == other.pathString
//        } else {
//            return false
//        }
//    }


    /* functions for printing this directory's contents in a file explorer-like format */
    fun printAll(p: PrintStream = System.out, indent: Int = 0) {
        fun indent(p: PrintStream, indent: Int) {
            for (i in 0 until indent) {
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
        for (dir in dirs)
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
//        for (dir in dirs) {
//            result.append(dir.name).append("\n")
//        }
//        return result.toString()
//    }
}