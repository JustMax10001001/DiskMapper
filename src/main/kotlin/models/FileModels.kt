package models

import javafx.beans.property.SimpleLongProperty
import tornadofx.getValue
import tornadofx.setValue
import java.io.File

/**
 * Lightweight class for wrapping file information
 * Observable properties are omitted where possible for memory savings
 */
open class FileEntry {

    constructor(
        simpleName: String,
        path: String = "",
        size: Long = 0,
        parent: DirectoryEntry? = null
    ) {
        this.simpleName = simpleName
        this.path = path
        this.fileSize = size
        this.parentFileEntry = parent
    }

    protected constructor(
        simpleName: String,
        path: String = "",
        size: Long = 0,
        parent: DirectoryEntry? = null,
        isDirectory: Boolean = false
    ) : this(simpleName, path, size, parent) {
        this.isDirectory = isDirectory
    }

    // absolute path of entry.
    var path: String
        get() = if (parentFileEntry == null) field else File(parentFileEntry.path, simpleName).path
        private set

    var simpleName: String = ""
        private set

    // If parentFileEntry == null then it's a root entry
    val parentFileEntry: DirectoryEntry?
    val isRoot: Boolean
        get() = parentFileEntry == null

    var isDirectory: Boolean = false
        private set

    val fileSizeProperty = SimpleLongProperty(0)
    var fileSize by fileSizeProperty

    override fun toString(): String {
        return "FileEntry [name = \"${this.simpleName}\", path = \"${this.path}\"]"
    }
}

class DirectoryEntry(
    simpleName: String,
    path: String = simpleName,
    parent: DirectoryEntry? = null
) : FileEntry(simpleName, path, parent = parent, isDirectory = true) {

    var areChildrenLoaded: Boolean = false
        private set

    private val childrenModifiable = mutableListOf<FileEntry>()
    val children: List<FileEntry> = childrenModifiable

    fun setChildren(entries: Collection<FileEntry>) {
        childrenModifiable.clear()
        childrenModifiable.addAll(entries)

        areChildrenLoaded = true
    }

    override fun toString(): String {
        return "DirectoryEntry [name = \"${this.simpleName}\", path = \"${this.path}\""
    }
}