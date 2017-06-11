package box.white.reins.util

/**
 * @author seri
 */
class FileUtil {

    /**
     * @param path
     * @return
     */
    static List<String> readLinesExcludeBlank(String path) {
        List<String> lines = []
        new File(path).eachLine('UTF-8') {
            if (!StringUtil.isBlank(it)) {
                lines << it
            }
        }
        lines
    }

    /**
     * @param lines
     * @return
     */
    static List<String> skipCommentLine(List<String> lines) {
        List<String> skipedList = lines.collect()
        skipedList.removeAll { it.startsWith("#") || it.startsWith("//") }
        skipedList
    }
}
