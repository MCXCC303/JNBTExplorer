# JNBTExplorer: NBT Explorer - Java Edition

Vibe Coding产物，使用Swing图形库编写，重写自[NBTExplorer](https://github.com/jaquadro/NBTExplorer)

支持标准gzip压缩格式NBT文件编辑

---

### 特点

目前较C#版本新增支持：

- 新建窗口
- 首选项编辑
- 视图排序（按名称、按标签类型）
- 更人性化的Hex Editor和UUID显示
- 跨窗口粘贴（应用内新建的窗口之间）
- 拖拽复制（测试中）

支持命令行传入多个文件：

```shell
java -jar JNBTExplorer.jar File1.dat File2.dat_old ...
```

将为每个文件分别打开一个窗口。

---

### 暂不支持：

- 以存档为单位编辑
- 编辑其他格式NBT文件
- 自动识别NBT文件格式
