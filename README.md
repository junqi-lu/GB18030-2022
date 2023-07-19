# README

## 一些术语

该小节属于基本翻译自[Supplementary Characters in the Java Platform](https://www.oracle.com/technical-resources/articles/javase/supplementary.html) 。
这篇文章清晰且经典，能帮助读者建立起对人与计算机读取文字方式的系统理解。

### 字符（Character）

字符是构成人类文本（text） 的抽象出来的最小单位。它可以是可见的，也可以是不可见的（比如一些控制字符）；
它没有规定的字节长度；它代表一种人类能够理解的字形或意义。

### 字符集（Character Set）

字符集是字符的集合。

### 编码字符集（Coded Character Set）

编码字符集给字符集中的每一个字符赋予了单独的数字。字符集和编码字符集常常并不区分得很明显，这也是合理的，
因为对计算机而言，一个没有编码的字符集是没有意义的，计算机读不懂它。

### 码点（Code Point）

编码字符集中，字符对应的数字就是码点。Unicode码点的范围是`U+0000`到`U+10FFFF`。编码字符集中并不一定给每个码点都分配一个字符。

### 补充字符（Supplementary Characters）

Unicode码点在`U+10000`到`U+10FFFF`的字符称为补充字符。因为他们不能用两个字节（16位比特）来表示。而在
`U+0000`到`U+FFFF`的字符我们称之为其在基本多语言平面（Basic Multilingual Plane）中。

### 字符编码方案（Character Encoding Scheme）

字符编码方案是将一个或多个编码字符集的编号映射到一个或多个固定宽度的编码单元序列。最常用的编码单元是字节，但也可以使用16位或32位整型数进行内部处理。
UTF-32、UTF-16和UTF-8是Unicode标准编码字符集的字符编码方案。

### UTF-32

UTF-32简单地将Unicode码点映射成相同值的32位整型。这种字符编码方案足够简单，但是所需要的空间成本比较高。

### UTF-16

UTF-16的编码单元长度为16位比特。
UTF-16用一个或两个编码单元序列来表示Unicode码点。
对于范围在`U+0000`到`U+FFFF`的码点，UTF-16编码方案将其编码为一个与码点值相同的双字节编码单元。
Unicode补充字符会被编码成两个编码单元，第一个编码单元取自高位代理（high-surrogates）范围，范围是`U+D800`到`U+DBFF` 。
第二个编码单元取自低位代理（low-surrogates）范围，范围是`U+DC00`到`U+DFFF`。

之所以能这么做是因为`U+D800`到`U+DFFF`虽然被定义在Unicode码点范围中，但是并没有被赋予字符，所以程序能够
辨识出某个16位是指BMP码点，还是补充字符的高位或是低位。此外`U+D800`到`U+DBFF`共1024个数，`U+DC00`到`U+DFFF`共1024个数，
他们的组合可以表示1048576(0X100000)个数，刚好可以表示补充字符`U+10000`到`U+10FFFF`所有码点。

### UTF-8

UTF-8用1到4个字节来对Unicode码点进行编码。`U+0000`到`U+007F`被编码成一个字节，`U+0080`到`U+07FF`被编码成两个字节，
`U+0800`到`U+FFFF`被编码成三个字节。`U+10000`到`U+10FFFF`被编码成四个字节。UTF-8字符编码方案对 Unicode 的 Basic Latin Block 是友好的，
即`U+0000`到`U+007F`这128个字符只需要1个字节编码。

## 测试文件

测试文件来源于[全国信息技术标准化网 GB18030-2022与UCS代码映射表](http://www.nits.org.cn/index/article/4034)

两个文件中左边列都代表 Unicode 的码点，右边列代表 GB18030-2022 编码。

对于 Unicode 每个平面的描述见 [Unicode Planes](https://www.compart.com/en/unicode/plane).

Unicode中的 BMP 为 Plane 0（码点范围为`U+0000`-`U+FFFF`），
SMP 为 Plane 1（码点范围为`U+10000`-`U+1FFFF`）。

测试文件 [GB18030_2022_MappingTableSMP](resources/GB18030_2022_MappingTableSMP.txt) 的码点显然不止对应 Unicode 的 Plane 1。
文件共有 1048576 行，`0x10000` - `0x10FFFF`共计1048576个码点。二者能对上，可以看出，
除了 Plane 0 之外的其他所有平面的码点都在该文件中。Java源码 `Character` 类的文档中写道：

## 程序逻辑

`Integer` 能保存32位（4字节）带符号整数。所以可以用来存储文件中的编码。

### 编码（Unicode -> GB）

Unicode 码点用整型数字表示，所以要验证 jdk 是否能将 Unicode 码点正确转成 GB18030-2022 编码需要：

1. 从`0`循环到`0X10FFFF`, 排除掉 UTF-16 用到的代理码点点位（`0XD800`-`0XDFFF`）。
2. 对于每个数字码点，将其转换为 `String` 类型，java中以UTF-16编码单元序列存储，再将 `String` 以 GB18030 编码成 `byte[]` 数组。
3. 将 `byte[]` 数组转成 `Integer` 类型，并与 `Map` 中 `value` 进行比较。

### 解码（GB -> Unicode）

1. 将 GB18030-2022 编码对应的无符号整数转成 `byte[]` 数组
2. 将 `byte[]` 数组按 GB18030 编码转成 `String` 字符串
3. 将 `String` 字符串的第一个码点与测试文件中的码点作比较

## 参考

* [Roadmaps to Unicode](https://www.unicode.org/roadmaps/index.html)
* [Unicode Planes](https://www.compart.com/en/unicode/plane)
* [Supplementary Characters in the Java Platform](https://www.oracle.com/technical-resources/articles/javase/supplementary.html)
