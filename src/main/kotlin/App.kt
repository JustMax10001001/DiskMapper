import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch

fun main() {
    //val f = File("C:\\")
    launch<DiskMapperApp>()
}

class DiskMapperApp : App(MapperForm::class) {

    override fun start(stage: Stage) {
        super.start(stage)
        stage.apply {
            width = 600.0
            minWidth = 600.0
            minHeight = 300.0
        }
    }
}



