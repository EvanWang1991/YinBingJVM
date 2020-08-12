## JVM内存模型剖析与优化

### JDK体系结构

![image.png](https://upload-images.jianshu.io/upload_images/23805140-40f55965b300bd92.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### Java语言的跨平台特性
![image.png](https://upload-images.jianshu.io/upload_images/23805140-e657d4472ac8f842.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### JVM整体结构及内存模型

![image.png](https://upload-images.jianshu.io/upload_images/23805140-490b9f809f8b9b07.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### JVM内存参数设置

![image.png](https://upload-images.jianshu.io/upload_images/23805140-224ff649ea13a702.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Spring Boot程序的JVM参数设置格式(Tomcat启动直接加在bin目录下catalina.sh文件里)：

```
java ‐Xms2048M ‐Xmx2048M ‐Xmn1024M ‐Xss512K ‐XX:MetaspaceSize=256M ‐XX:MaxMetaspaceSize=256M ‐jar microservice‐eureka‐server.jar
```
关于元空间的JVM参数有两个：-XX:MetaspaceSize=N和 -XX:MaxMetaspaceSize=N，对于64位JVM来说，元空间的默认初始大小是
21MB，**默认的元空间的最大值是无限**。
-XX：MaxMetaspaceSize： 设置元空间最大值， 默认是-1， 即不限制， 或者说只受限于本地内存大小。
-XX：MetaspaceSize： 指定元空间的初始空间大小， 以字节为单位，默认是21M，达到该值就会触发full gc进行类型卸载， 同时收集
器会对该值进行调整： 如果释放了大量的空间， 就适当降低该值； 如果释放了很少的空间， 那么在不超过-XX：
MaxMetaspaceSize（如果设置了的话） 的情况下， 适当提高该值。
**由于调整元空间的大小需要Full GC，这是非常昂贵的操作，如果应用在启动的时候发生大量Full GC，通常都是由于永久代或元空间发生
了大小调整，基于这种情况，一般建议在JVM参数中将MetaspaceSize和MaxMetaspaceSize设置成一样的值，并设置得比初始值要大，
对于8G物理内存的机器来说，一般我会将这两个值都设置为256M。**

-Xss设置越小count值越小，说明一个线程栈里能分配的栈帧就越少，但是对JVM整体来说能开启的线程数会更多。

JVM内存参数大小该如何设置？
JVM参数大小设置并没有固定标准，需要根据实际项目情况分析，下面是我找的一个例子

![image.png](https://upload-images.jianshu.io/upload_images/23805140-0d4be1950aa9d28b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](https://upload-images.jianshu.io/upload_images/23805140-1e22c4464a5c3914.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**总结 就是尽可能让对象都在新生代里分配和回收，尽量别让太多对象频繁进入老年代，避免频繁对老年代进行垃圾回收，同时给系统充足的内存大小，避免新生代频繁的进行垃圾回收。**



