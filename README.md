# 饮冰JVM

## 剖析JVM类加载机制
### 类加载运行过程
当我们用`java`命令运行某个类的main函数启动程序时,首先需要通过**类加载器**把主类加载到JVM。
通过`java`命令执行代码的大体流程如下:

![image.png](https://upload-images.jianshu.io/upload_images/23805140-2fd98361b70c3c48.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

其中loadClass的类加载过程有如下几步:

加载 >> 验证 >> 准备 >> 解析 >> 初始化 >> 使用 >> 卸载

* **加载** 在硬盘上查找并通过IO读入字节码文件，使用到类时才会加载，例如调用类的main()方法，new对象等等，在加载阶段会在内存中生成一个代表这个类的java.lang.Class对象，作为方法区这个类的各种数据的访问入口

* **验证** 校验字节码文件的正确性

* **准备** 给类的静态变量分配内存，并赋予默认值

* **解析** 将符号引用替换为直接引用，该阶段会把一些静态方法(符号引用，比如main()方法)替换为指向数据所存内存的指针或句柄等(直接引用)，这是所谓的静态链接过程(类加载期间完成)

* **初始化** 对类的静态变量初始化为指定的值，执行静态代码块

![image.png](https://upload-images.jianshu.io/upload_images/23805140-d4d8f3f733deac28.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)