package controls

import javafx.scene.control.TreeTableCell
import models.FileEntry
import tornadofx.anchorpane
import tornadofx.anchorpaneConstraints
import tornadofx.progressbar

class RelativeFileSizeTreeTableCell : TreeTableCell<FileEntry, Number>() {

    override fun updateItem(item: Number?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            text = null
            graphic = null
        } else {
            graphic = anchorpane(
                progressbar(item.toDouble()) {
                    anchorpaneConstraints {
                        leftAnchor = 4
                        rightAnchor = 4
                    }
                }
            )
        }
    }
}