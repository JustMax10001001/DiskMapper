package controls

import javafx.scene.control.TreeTableCell
import models.FileEntry
import kotlin.math.pow

class FileSizeTreeTableCell: TreeTableCell<FileEntry, Number>(){

    override fun updateItem(item: Number?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            text = null
        } else {
            text = formatFileSize(item.toLong())
        }
    }

    private fun formatFileSize(size: Long): String{
        var power = 0
        while (1024.0.pow(power + 1) < size) {
            power++
        }
        return "%.1f %sB".format(
            size / 1024.0.pow(power), when (power) {
                1 -> "Ki"
                2 -> "Mi"
                3 -> "Gi"
                4 -> "Ti"
                5 -> "Pi"
                6 -> "Ei"
                else -> ""
            }
        )
    }
}