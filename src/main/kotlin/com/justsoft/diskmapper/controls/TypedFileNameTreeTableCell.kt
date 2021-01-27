package com.justsoft.diskmapper.controls

import com.justsoft.diskmapper.models.FileEntry
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.scene.control.TreeTableCell

class TypedFileNameTreeTableCell : TreeTableCell<FileEntry, FileEntry>() {

    override fun updateItem(item: FileEntry?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            text = null
        } else {
            text = item.simpleName
            graphic = FontAwesomeIconView(
                if (item.isDirectory)
                    FontAwesomeIcon.FOLDER
                else
                    FontAwesomeIcon.FILE
            )
        }
    }
}