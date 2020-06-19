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

**注意**，主类在运行过程中如果使用到其它类，会逐步加载这些类。jar包或war包里的类不是一次性全部加载的，是使用到时才加载。

```java
public class TestDynamicLoad {
    static {
        System.out.println("*************load TestDynamicLoad************");
    }

    public static void main(String[] args) {
        new A();
        System.out.println("*************load test************");
        B b = null;  //B不会加载，除非这里执行 new B()
    }
}

class A {
    static {
        System.out.println("*************load A************");
    }

    public A() {
        System.out.println("*************initial A************");
    }
}

class B {
    static {
        System.out.println("*************load B************");
    }

    public B() {
        System.out.println("*************initial B************");
    }
}
```
运行结果
```
*************load TestDynamicLoad************
*************load A************
*************initial A************
*************load test************
```
### 类加载器和双亲委派机制
上面的类加载过程主要是通过类加载器来实现的，Java里有如下几种类加载器

* **引导类加载器**：负责加载支撑JVM运行的位于JRE的lib目录下的核心类库，比如rt.jar、charsets.jar等
* **扩展类加载器**：负责加载支撑JVM运行的位于JRE的lib目录下的ext扩展目录中的JAR类包
* **应用程序类加载器**：负责加载ClassPath路径下的类包，主要就是加载你自己写的那些类
* **自定义加载器**：负责加载用户自定义路径下的类包

**类加载器示例：**
```java
public class TestJDKClassLoader {

    public static void main(String[] args) {
        System.out.println(String.class.getClassLoader());
        System.out.println(com.sun.crypto.provider.DESKeyFactory.class.getClassLoader().getClass().getName());
        System.out.println(TestJDKClassLoader.class.getClassLoader().getClass().getName());

        System.out.println();
        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader extClassloader = appClassLoader.getParent();
        ClassLoader bootstrapLoader = extClassloader.getParent();
        System.out.println("the bootstrapLoader : " + bootstrapLoader);
        System.out.println("the extClassloader : " + extClassloader);
        System.out.println("the appClassLoader : " + appClassLoader);

        System.out.println();
        System.out.println("bootstrapLoader加载以下文件：");
        URL[] urls = Launcher.getBootstrapClassPath().getURLs();
        for (int i = 0; i < urls.length; i++) {
            System.out.println(urls[i]);
        }

        System.out.println();
        System.out.println("extClassloader加载以下文件：");
        System.out.println(System.getProperty("java.ext.dirs"));

        System.out.println();
        System.out.println("appClassLoader加载以下文件：");
        System.out.println(System.getProperty("java.class.path"));
    }
}
```
运行结果
```
null
sun.misc.Launcher$ExtClassLoader
sun.misc.Launcher$AppClassLoader

the bootstrapLoader : null
the extClassloader : sun.misc.Launcher$ExtClassLoader@4617c264
the appClassLoader : sun.misc.Launcher$AppClassLoader@18b4aac2

bootstrapLoader加载以下文件：
file:/C:/dev/jdk/jre/lib/resources.jar
file:/C:/dev/jdk/jre/lib/rt.jar
file:/C:/dev/jdk/jre/lib/sunrsasign.jar
file:/C:/dev/jdk/jre/lib/jsse.jar
file:/C:/dev/jdk/jre/lib/jce.jar
file:/C:/dev/jdk/jre/lib/charsets.jar
file:/C:/dev/jdk/jre/lib/jfr.jar
file:/C:/dev/jdk/jre/classes

extClassloader加载以下文件：
C:\dev\jdk\jre\lib\ext;C:\WINDOWS\Sun\Java\lib\ext

appClassLoader加载以下文件：
C:\dev\jdk\jre\lib\charsets.jar;C:\dev\jdk\jre\lib\deploy.jar;C:\dev\jdk\jre\lib\ext\access-bridge-64.jar;C:\dev\jdk\jre\lib\ext\cldrdata.jar;C:\dev\jdk\jre\lib\ext\dnsns.jar;C:\dev\jdk\jre\lib\ext\jaccess.jar;C:\dev\jdk\jre\lib\ext\jfxrt.jar;C:\dev\jdk\jre\lib\ext\localedata.jar;C:\dev\jdk\jre\lib\ext\nashorn.jar;C:\dev\jdk\jre\lib\ext\sunec.jar;C:\dev\jdk\jre\lib\ext\sunjce_provider.jar;C:\dev\jdk\jre\lib\ext\sunmscapi.jar;C:\dev\jdk\jre\lib\ext\sunpkcs11.jar;C:\dev\jdk\jre\lib\ext\zipfs.jar;C:\dev\jdk\jre\lib\javaws.jar;C:\dev\jdk\jre\lib\jce.jar;C:\dev\jdk\jre\lib\jfr.jar;C:\dev\jdk\jre\lib\jfxswt.jar;C:\dev\jdk\jre\lib\jsse.jar;C:\dev\jdk\jre\lib\management-agent.jar;C:\dev\jdk\jre\lib\plugin.jar;C:\dev\jdk\jre\lib\resources.jar;C:\dev\jdk\jre\lib\rt.jar;C:\dev\code\YinBingJVM\target\classes;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\boot\spring-boot-starter\2.3.1.RELEASE\spring-boot-starter-2.3.1.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\boot\spring-boot\2.3.1.RELEASE\spring-boot-2.3.1.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\spring-context\5.2.7.RELEASE\spring-context-5.2.7.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\spring-aop\5.2.7.RELEASE\spring-aop-5.2.7.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\spring-beans\5.2.7.RELEASE\spring-beans-5.2.7.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\spring-expression\5.2.7.RELEASE\spring-expression-5.2.7.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\boot\spring-boot-autoconfigure\2.3.1.RELEASE\spring-boot-autoconfigure-2.3.1.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\boot\spring-boot-starter-logging\2.3.1.RELEASE\spring-boot-starter-logging-2.3.1.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\ch\qos\logback\logback-classic\1.2.3\logback-classic-1.2.3.jar;C:\Users\JiXiang.Wang\.m2\repository\ch\qos\logback\logback-core\1.2.3\logback-core-1.2.3.jar;C:\Users\JiXiang.Wang\.m2\repository\org\apache\logging\log4j\log4j-to-slf4j\2.13.3\log4j-to-slf4j-2.13.3.jar;C:\Users\JiXiang.Wang\.m2\repository\org\apache\logging\log4j\log4j-api\2.13.3\log4j-api-2.13.3.jar;C:\Users\JiXiang.Wang\.m2\repository\org\slf4j\jul-to-slf4j\1.7.30\jul-to-slf4j-1.7.30.jar;C:\Users\JiXiang.Wang\.m2\repository\jakarta\annotation\jakarta.annotation-api\1.3.5\jakarta.annotation-api-1.3.5.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\spring-core\5.2.7.RELEASE\spring-core-5.2.7.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\springframework\spring-jcl\5.2.7.RELEASE\spring-jcl-5.2.7.RELEASE.jar;C:\Users\JiXiang.Wang\.m2\repository\org\yaml\snakeyaml\1.26\snakeyaml-1.26.jar;C:\Users\JiXiang.Wang\.m2\repository\org\projectlombok\lombok\1.18.12\lombok-1.18.12.jar;C:\Users\JiXiang.Wang\.m2\repository\org\slf4j\slf4j-api\1.7.30\slf4j-api-1.7.30.jar;C:\dev\Program Files\IntelliJ IDEA Community Edition 2018.3.5\lib\idea_rt.jar
```

#### 类加载器初始化过程
参见类运行加载全过程图可知其中会创建JVM启动器实例sun.misc.Launcher。
sun.misc.Launcher初始化使用了单例模式设计，保证一个JVM虚拟机内只有一个sun.misc.Launcher实例。
在Launcher构造方法内部，其创建了两个类加载器，分别是sun.misc.Launcher.ExtClassLoader(扩展类加载器)和sun.misc.Launcher.AppClassLoader(应用类加载器)。
JVM默认使用Launcher的getClassLoader()方法返回的类加载器AppClassLoader的实例加载我们的应用程序。

```
//Launcher的构造方法
    public Launcher() {
        Launcher.ExtClassLoader var1;
        try {
            //构造扩展类加载器，将parent设置为null
            var1 = Launcher.ExtClassLoader.getExtClassLoader();
        } catch (IOException var10) {
            throw new InternalError("Could not create extension class loader", var10);
        }

        try {
            //构造应用程序类加载器，将parent设置为ExtClassLoader
            this.loader = Launcher.AppClassLoader.getAppClassLoader(var1);
        } catch (IOException var9) {
            throw new InternalError("Could not create application class loader", var9);
        }
        Thread.currentThread().setContextClassLoader(this.loader);
        String var2 = System.getProperty("java.security.manager");
        if (var2 != null) {
            SecurityManager var3 = null;
            if (!"".equals(var2) && !"default".equals(var2)) {
                try {
                    var3 = (SecurityManager)this.loader.loadClass(var2).newInstance();
                } catch (IllegalAccessException var5) {
                } catch (InstantiationException var6) {
                } catch (ClassNotFoundException var7) {
                } catch (ClassCastException var8) {
                }
            } else {
                var3 = new SecurityManager();
            }
            if (var3 == null) {
                throw new InternalError("Could not create SecurityManager: " + var2);
            }
            System.setSecurityManager(var3);
        }
    }
```
#### 双亲委派机制

JVM类加载器是有亲子层级结构的，如下图

![image.png](https://upload-images.jianshu.io/upload_images/23805140-608fc3df87c94774.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这里类加载其实就有一个双亲委派机制，加载某个类时会先委托父加载器寻找目标类，找不到再委托上层父加载器加载，如果所有父加载器在自己的加载类路径下都找不到目标类，则在自己的类加载路径中查找并载入目标类。
比如我们的Math类，最先会找应用程序类加载器加载，应用程序类加载器会先委托扩展类加载器加载，扩展类加载器再委托引导类加载器，顶层引导类加载器在自己的类加载路径里找了半天没找到Math类，则向下退回加载Math类的请求，扩展类加载器收到回复就自己加载，在自己的类加载路径里找了半天也没找到Math类，又向下退回Math类的加载请求给应用程序类加载器，应用程序类加载器于是在自己的类加载路径里找Math类，结果找到了就自己加载了。。
**双亲委派机制说简单点就是，先找父亲加载，不行再由儿子自己加载**

我们来看下应用程序类加载器AppClassLoader加载类的双亲委派机制源码，AppClassLoader的loadClass方法最终会调用其父类ClassLoader的loadClass方法，该方法的大体逻辑如下：

1. 首先，检查一下指定名称的类是否已经加载过，如果加载过了，就不需要再加载，直接返回。
2. 如果此类没有加载过，那么，再判断一下是否有父加载器；如果有父加载器，则由父加载器加载（即调用parent.loadClass(name, false);）.或者是调用bootstrap类加载器来加载。
3. 如果父加载器及bootstrap类加载器都没有找到指定的类，那么调用当前类加载器的findClass方法来完成类加载。

java.lang.ClassLoader.loadClass(java.lang.String, boolean)
```
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            //检查当前类加载器是否已经加载了这个类
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        //如果父加载器不为空,则委托父加载器加载该类
                        c = parent.loadClass(name, false);
                    } else {
                        //如果父加载器为空，则交由引导类加载器加载该类
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    //都会调用URLClassLoader的findClass方法在加载器的类路径里查找并加载该类
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

**为什么要设计双亲委派机制？**
* 沙箱安全机制 自己写的java.lang.String.class类不会被加载，这样便可以防止核心API库被随意篡改
* 避免类的重复加载 当父亲已经加载了该类时，就没有必要子ClassLoader再加载一次，保证被加载类的唯一性