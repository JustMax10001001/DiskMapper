import controls.FileEntryTreeItem
import controls.FileSizeTreeTableCell
import controls.RelativeFileSizeTreeTableCell
import controls.TypedFileNameTreeTableCell
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Priority
import models.DirectoryEntry
import models.FileEntry
import tornadofx.*
import java.io.File
import java.nio.file.Paths
import java.util.stream.Collectors

class MapperForm : View("Disk Mapper") {

    private val taskStatus by inject<TaskStatus>()
    private var fileFetcherTask: Task<Unit>? = null
    private val rootFileEntryProperty = SimpleObjectProperty<DirectoryEntry>()

    private var rootFileEntry by rootFileEntryProperty
    private var fileTreeView by singleAssign<TreeTableView<FileEntry>>()

    private var fileSizeColumn by singleAssign<TreeTableColumn<FileEntry, Number>>()
    private var relativeSizeColumn by singleAssign<TreeTableColumn<FileEntry, Number>>()

    private val useRelativeSize = config.boolean("useRelativeSize") ?: false

    override val root = vbox(8) {
        fileTreeView = treetableview<FileEntry> {
            column("Name") { x: TreeTableColumn.CellDataFeatures<FileEntry, FileEntry> ->
                ReadOnlyObjectWrapper(x.value.value)
            }.remainingWidth()
                .setCellFactory { TypedFileNameTreeTableCell() }

            if (useRelativeSize) {
                relativeSizeColumn = column("Relative Size", FileEntry::relativeFileSizeProperty)
                    .contentWidth(64, useAsMin = true, useAsMax = true)
                relativeSizeColumn.setCellFactory { RelativeFileSizeTreeTableCell() }
            }

            fileSizeColumn = column("Size", FileEntry::fileSizeProperty)
                .contentWidth(40, useAsMin = true, useAsMax = true)
            fileSizeColumn.setCellFactory { FileSizeTreeTableCell() }


            setRowFactory {
                val row = TreeTableRow<FileEntry>()

                row.contextMenu = ContextMenu(
                    MenuItem("Show in explorer").apply {
                        action {
                            if (row.treeItem.value.path != "@@smallfiles@@") {
                                with(row.treeItem.value) {
                                    val cmd = "explorer.exe " +
                                            (if (!isDirectory) "/select, " else "") +
                                            "\"$path\""
                                    println(cmd)
                                    Runtime.getRuntime().exec(
                                        cmd
                                    )
                                }
                            }
                        }
                    }
                )

                row
            }

            sortMode = TreeSortMode.ALL_DESCENDANTS
            vgrow = Priority.ALWAYS

            smartResize()
        }
        hbox(8) {
            button(taskStatus.running.stringBinding {
                if (it != null && it) TEXT_STOP
                else TEXT_START
            }) {
                action {
                    if (taskStatus.running.value) {     // cancel running scan
                        fileFetcherTask!!.cancel()
                    } else {                            // start scan
                        val startDirectory = chooseDirectory(owner = currentWindow) ?: return@action

                        var rootFileName = startDirectory.name
                        if (rootFileName.isEmpty())  // then it is drive letter
                            rootFileName = Paths.get(startDirectory.absolutePath).root.toString()

                        rootFileEntry = DirectoryEntry(
                            simpleName = rootFileName,
                            path = startDirectory.absolutePath
                        )

                        fileTreeView.root = FileEntryTreeItem(rootFileEntry as FileEntry)

                        fileFetcherTask = runAsync(true, taskStatus) {
                            doFileFetchAction(rootFileEntry)
                        } ui {
                            // ensure that root node has its children loaded
                            (fileTreeView.root as FileEntryTreeItem).ensureChildrenLoaded()

                            fileTreeView.sortOrder.add(fileSizeColumn)
                            fileSizeColumn.sortType = TreeTableColumn.SortType.DESCENDING
                        }
                    }
                }

                minWidth = 68.0
            }

            hbox(8) {
                visibleProperty().bind(taskStatus.running)

                progressindicator {
                    alignment = Pos.CENTER_LEFT

                    prefHeight = 24.0
                    prefWidth = 24.0
                    minWidth = 24.0
                }

                label(taskStatus.message) {
                    alignment = Pos.CENTER_LEFT
                }
            }
        }

        paddingAll = 12
    }

    private fun FXTask<*>.doFileFetchAction(startDirectory: DirectoryEntry) {
        if (isCancelled)
            return

        val files = File(startDirectory.path).listFiles()
            ?: return     // File.listFiles() returns null only when called on files

        updateMessage("Scanning: ${startDirectory.path}")

        startDirectory.setChildren(
            files
                .asList()
                .stream()
                .map {
                    if (it.isDirectory)
                        DirectoryEntry(
                            simpleName = it.name,
                            parent = startDirectory
                        )
                    else
                        FileEntry(
                            simpleName = it.name,
                            parent = startDirectory,
                            size = it.length()
                        )
                }
                .collect(
                    Collectors.toList()
                )
        )

        // expand root node
        if (startDirectory.isRoot)
            runLater {
                fileTreeView.root.isExpanded = true
                fileTreeView.requestResize()
            }

        var newlyLocatedFilesSize: Long = 0

        startDirectory
            .children
            .forEach {
                if (it.isDirectory)
                    doFileFetchAction(it as DirectoryEntry)
                else
                    newlyLocatedFilesSize += it.fileSize
            }

        // recursively update sizes for every directory until root
        updateSizeRecursively(startDirectory, newlyLocatedFilesSize)
    }

    private fun updateSizeRecursively(entry: DirectoryEntry?, newlyLocatedFilesSize: Long) {
        if (entry == null)
            return

        entry.fileSize += newlyLocatedFilesSize
        updateSizeRecursively(entry.parentFileEntry, newlyLocatedFilesSize)
    }

    init {
        FileEntry.UseRelativeSize = useRelativeSize
    }

    companion object {
        const val TEXT_START = "Browse..."
        const val TEXT_STOP = "Cancel"
    }
}
