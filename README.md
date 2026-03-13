# JNBTExplorer: NBT Explorer - Java Edition

Vibe Coding产物，使用Swing图形库编写，重写自[NBTExplorer](https://github.com/jaquadro/NBTExplorer)

支持标准gzip压缩格式NBT文件编辑，如level.dat、UUID.dat文件


---

## 构建
项目使用Java编写，使用Swing图形库实现GUI

使用以下命令编译运行项目：

```shell
javac -encoding UTF-8 -d bin -sourcepath src src/com/MCXCC/JNBTExplorer/JNBTExplorerMainMain.java
java -cp src com.MCXCC.JNBTExplorer.JNBTExplorerMain
```

使用以下命令将项目打包为可执行jar文件：

```shell
javac -encoding UTF-8 -d bin -sourcepath src src/com/MCXCC/JNBTExplorer/JNBTExplorerMain.java
jar cvfm JNBTExplorer.jar MANIFEST.MF -C bin .
```

或者直接使用项目自带的build脚本构建：
```shell
./build
```

## 特点：

目前较C#原版新增支持：

- 首选项
- 新建窗口
- 跨窗口粘贴
- 视图排序（按名称、按标签类型）
- 更人性化的Hex Editor和UUID显示
- 拖拽复制（测试中）

支持命令行传入多个文件：

```shell
java -jar JNBTExplorer.jar File1.dat File2.dat_old ...
```

将为每个文件分别打开一个窗口。

---

## 暂不支持：

- 以存档为单位编辑
- 编辑其他格式NBT文件
- 自动识别NBT文件格式

## 实际案例：
使用多选复制+跨窗口粘贴可以快速迁移不同存档间玩家数据
