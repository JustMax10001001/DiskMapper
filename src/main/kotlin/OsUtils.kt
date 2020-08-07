enum class OS {
    WINDOWS, LINUX, MAC, SOLARIS
}

class OsUtils {
    companion object {
        fun getOS(): OS? {
            val os = System.getProperty("os.name").toLowerCase()
            return when {
                os.contains("win") -> {
                    OS.WINDOWS
                }
                os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                    OS.LINUX
                }
                os.contains("mac") -> {
                    OS.MAC
                }
                os.contains("sunos") -> {
                    OS.SOLARIS
                }
                else -> null
            }
        }
    }
}