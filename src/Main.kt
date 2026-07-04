import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    if (!System.getProperty("os.name").startsWith("Windows")) {
        println("此工具仅支持 Windows 系统（依赖 mklink 命令）")
        return
    }
    println("请输入原始文件路径（可输入多个且允许带双引号，*代表该路径下所有文件/文件名（如有后缀名），不输入代表结束）")
    val (pathList, targetPath) = getPath()
    val isAllReplace = if (pathList.size > 1) mutableListOf() else mutableListOf<Boolean?>(null)
    for (path in pathList) {
        val linkPath = File(targetPath, File(path).name).path
        if (checkLinkPath(linkPath, isAllReplace))
            link(path, linkPath)
    }
}

fun link(path: String, linkPath: String) {
    try {
        val process = arrayOf("cmd", "/c", "mklink")
            .let { if (File(path).isFile) it else it + "/J" }
            .let { it + linkPath + path }
            .let {
                ProcessBuilder(*it)
                    .redirectErrorStream(true)
                    .start()
            }

        val output = process.inputStream.bufferedReader().use { it.readText() }

        val exitCode = process.waitFor()

        if (exitCode == 0)
            println("链接成功：$linkPath <<===>> $path")
        else
            println("链接失败：$output 退出码 $exitCode")
    } catch (e: Exception) {
        println("链接失败：${e.message}")
    }
}

fun checkLinkPath(linkPath: String, isAllReplace: MutableList<Boolean?>): Boolean =
    if (File(linkPath).exists()) {
        println("发现${linkPath}已存在")
        if (isAllReplace.isEmpty()) isAllReplace += isAllReplace(true)
        if (isReplace(isAllReplace, true)) {
            try {
                val pathObj = Paths.get(linkPath)
                if (Files.deleteIfExists(pathObj)) {
                    println("已删除${linkPath}")
                    true
                } else {
                    println("删除失败：文件不存在")
                    false
                }
            } catch (e: Exception) {
                println("删除失败：${e.message}")
                println("已取消替换")
                false
            }
        } else {
            println("已取消替换")
            false
        }
    } else
        true


fun getPath(): Pair<List<String>, String> {
    val pathList = mutableListOf<String>()
    while (true) {
        print("路径${pathList.size + 1}：")
        val path = readln().trim('"')
        if (path.isEmpty())
            if (pathList.isEmpty())
                println("文件路径不能为空")
            else
                break
        else
            pathList.addPath(path)
    }

    val targetPath = getTargetPath(true)

    return Pair(pathList, targetPath)
}

fun getTargetPath(isFirst: Boolean): String {
    if (isFirst) print("请输入目标目录路径：") else print("请重新输入目标目录路径：")
    val path = readln().trim('"')
    val file = File(path)
    if (file.exists())
        return if (file.isDirectory) {
            println("已添加 $path")
            path
        } else {
            println("添加失败：${path}不是文件夹")
            getTargetPath(false)
        }
    else
        println("添加失败：${path}不存在")
    return getTargetPath(false)
}

fun MutableList<String>.addPath(path: String) {
    if (path.contains('*')) {
        expandPath(path).forEach {
            checkAndAddPath(this, it)
        }
        return
    }

    checkAndAddPath(this, path)
}

fun checkAndAddPath(pathList: MutableList<String>, path: String) {
    if (!File(path).exists())
        println("添加失败：${path}不存在")
    else
        pathList.find { File(it).name == File(path).name }?.let { duplicatePath ->
            pathList.remove(duplicatePath)
            println("添加失败：${path}与${duplicatePath}重名")
            choosePath(path, duplicatePath, true).let {
                if (it != "") {
                    pathList += it
                    println("已添加：$it")
                } else {
                    println("已取消添加")
                }
            }
        } ?: run {
            pathList += path
            println("已添加：$path")
        }

}

fun choosePath(path1: String, path2: String, isFirst: Boolean): String {
    if (isFirst)
        print("请选择要保留的路径：")
    print("\n1. $path1\n2. $path2\n3. 都不保留\n请输入序号：")
    return when (readln().toIntOrNull()) {
        1 -> path1
        2 -> path2
        3 -> ""
        else -> {
            print("输入错误，请重新输入：")
            choosePath(path1, path2, false)
        }
    }
}

fun expandPath(vararg paths: String): List<String> {
    val result = mutableListOf<String>()
    for (path in paths) {
        val subPath1 = path.substringBefore('*')
        val subPath2 = path.substringAfter('*')
        val isName = subPath2.startsWith('.')
        val pathList =
            File(subPath1)
                .listFiles()
                ?.map { it.path }
                ?.filter { !isName || it.endsWith(subPath2) }
                ?: emptyList()
        pathList.forEach {
            val resultPath = it + if (isName) "" else subPath2
            if ('*' in resultPath)
                result += expandPath(resultPath)
            else
                result += resultPath
        }
    }
    return result
}

fun isReplace(isAllReplace: MutableList<Boolean?>, isFirst: Boolean): Boolean =
    when (isAllReplace[0]) {
        true -> true
        false -> false
        null -> {
            if (isFirst) print("是否替换已存在的文件？（1：是 默认：否）：")
            when (readln().lowercase()) {
                "1" -> true
                "" -> false
                else -> {
                    print("输入错误，请重新输入：")
                    isReplace(isAllReplace, false)
                }
            }
        }
    }


fun isAllReplace(isFirst: Boolean): Boolean? {
    if (isFirst) print("是否全部替换已存在的文件？（1：全部 2：全不 默认：逐个选择）：")
    return when (readln().lowercase()) {
        "1" -> true
        "2" -> false
        "" -> null
        else -> {
            print("输入错误，请重新输入：")
            isAllReplace(false)
        }
    }
}
