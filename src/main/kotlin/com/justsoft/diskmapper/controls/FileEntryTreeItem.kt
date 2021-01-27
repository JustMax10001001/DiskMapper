package com.justsoft.diskmapper.controls

import com.justsoft.diskmapper.models.DirectoryEntry
import com.justsoft.diskmapper.models.FileEntry
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import kotlin.math.roundToInt

/**
 * Custom tree item with lazy children loading for memory savings
 * as tree items are the com.justsoft.diskmapper.main memory hog
 */
class FileEntryTreeItem(item: FileEntry) : TreeItem<FileEntry>(item) {

    override fun isLeaf(): Boolean {
        return !value.isDirectory
    }

    private fun buildChildren(rootEntry: DirectoryEntry): List<TreeItem<FileEntry>> {
        val children = ArrayList<FileEntryTreeItem>((rootEntry.children.size * 0.2).roundToInt())

        if (!rootEntry.simpleName.contains("<")) {
            val smallChildren = ArrayList<FileEntry>((rootEntry.children.size * 0.8).roundToInt())

            val smallFilesEntry = createFileEntryWrapperForSmallFiles(rootEntry)
            var smallFilesSize: Long = 0

            rootEntry.children.forEach {
                if (!it.isDirectory && it.fileSize < SMALL_FILE_THRESHOLD) {
                    smallChildren.add(it)
                    smallFilesSize += it.fileSize
                } else
                    children.add(FileEntryTreeItem(it))
            }

            if (smallChildren.count() > 0) {
                smallFilesEntry.setChildren(smallChildren)
                smallFilesEntry.fileSize = smallFilesSize

                val smallFileEntryTreeItem = FileEntryTreeItem(smallFilesEntry)
                children.add(smallFileEntryTreeItem)
            }
        } else {
            children.ensureCapacity(rootEntry.children.size)
            rootEntry.children.forEach { children.add(FileEntryTreeItem(it)) }
        }

        return children
    }

    private var childrenLoaded = false
    private var childrenLoading = false

    override fun getChildren(): ObservableList<TreeItem<FileEntry>> {
        if (value.isDirectory && !childrenLoaded && !childrenLoading) {
            val directoryEntry = value as DirectoryEntry
            if (directoryEntry.areChildrenLoaded) {     // if children are not loaded yet just skip
                childrenLoading = true                  // to prevent recursion

                @Suppress("RecursivePropertyAccessor")
                children.setAll(buildChildren(directoryEntry))

                childrenLoaded = true
                childrenLoading = false
            }
        }
        return super.getChildren()
    }

    fun ensureChildrenLoaded() {
        children
    }

    companion object {

        const val SMALL_FILE_THRESHOLD = 1024 * 1024        // 1 MiB

        fun createFileEntryWrapperForSmallFiles(root: DirectoryEntry): DirectoryEntry {
            return DirectoryEntry(
                simpleName = "Small files (<1 MiB)",
                path = "@@smallfiles@@",
                parent = root
            )
        }
    }
}