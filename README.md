# MklinkTool

一个基于 Java 的命令行工具，用于在 Windows 系统中批量创建目录符号链接（Junction Point）。支持通配符展开、重名处理、批量替换等功能。

## 环境要求

- **操作系统**：Windows（依赖 `mklink` 命令）
- **JDK**：Java 21 或更高版本

## 编译与运行

### 1. 保存源码
将代码保存为 `MklinkTool.kt`（Kotlin 源文件）或转换为 Java 文件。此处以 Kotlin 为例。

### 2. 编译
```bash
kotlinc MklinkTool.kt -include-runtime -d MklinkTool.jar
```

### 3. 运行
```bash
java -jar MklinkTool.jar
```

如果使用 IntelliJ IDEA，可直接运行 `main` 函数。

## 使用方法

工具启动后，按提示交互式输入：

1. **输入原始路径**  
   支持多个路径，每行一个。可用双引号包裹，支持通配符 `*`：
    - `C:\Data\*.txt` – 匹配所有 `.txt` 文件
    - `C:\Projects\*\bin\*` – 递归匹配所有 `bin` 目录下的所有文件
    - 直接输入文件路径（不带通配符）
    - 空行结束输入

2. **输入目标目录**  
   链接将创建在该目录下，名称与原文件/目录同名。

3. **设置替换策略**
    - 是否全部替换已存在的链接？（`y/n`）
    - 若否，每个冲突会单独询问是否替换。

4. **处理结果**  
   程序依次创建链接，并输出成功或失败信息。

### 示例会话
```
请输入原始文件路径（可输入多个且允许带双引号，...，不输入代表结束）
路径1：C:\MyFolder\*.log
路径2：D:\Backup\config.xml
路径3：（回车）
请输入目标目录路径：E:\Links
是否全部替换已存在的文件？(y/n)：n
发现E:\Links\config.xml已存在
是否替换已存在的文件？(y/n)：y
已删除E:\Links\config.xml
链接成功：...
```

## 注意事项

- 仅支持 Windows 系统（非 Windows 会自动退出）。
- 删除已有链接时仅移除链接本身，不会影响原始数据。
- 通配符 `*` 可出现在路径任意位置，支持多级展开。

好的，以下是适合加入 README 的 Junction 特点说明：

---

## 关于目录联接点（Junction）

本工具使用 `mklink /J` 创建的是 Windows NTFS 文件系统中的**目录联接点（Junction）**，它具有以下特点：

- **仅适用于目录**：不能用于链接单个文件。
- **透明访问**：对应用程序而言，访问 Junction 就像直接访问目标目录一样，读写操作均会重定向到目标位置。
- **删除不影响原始数据**：删除 Junction 本身只会移除链接入口，**不会删除目标目录中的任何文件**。这是本工具选择 Junction 而非硬链接或符号链接的重要原因之一。
- **支持跨卷**：Junction 可以链接到同一台计算机的不同驱动器（例如将 `D:\Link` 链接到 `C:\Target`）。
- **不支持网络路径**：目标必须是本地路径，不能是 UNC 路径或映射的网络驱动器。
- **不支持相对路径**：创建时必须使用绝对路径。
- **兼容性好**：大多数 Windows 原生工具（如 robocopy、xcopy）和旧版应用程序都能正确处理 Junction，无需管理员权限即可创建（但可能需要 SeCreateSymbolicLinkPrivilege 权限，通常管理员默认拥有）。

**注意事项**：
- 删除已存在的 Junction 时，本工具使用 `Files.deleteIfExists()` 安全移除链接，不会递归删除其指向的内容。
- 如果目标目录本身也是一个 Junction，删除时同样只移除链接，不会影响原始链路上的数据。

---

本readme由deepseek生成，作者制作该工具的目的是拯救红温的C盘，该工具相当于一个全自动输 mklink /j path1 path2 的脚本