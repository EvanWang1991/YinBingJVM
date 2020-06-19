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

#### 自定义类加载器
自定义类加载器只需要继承 **java.lang.ClassLoader** 类，该类有两个核心方法，一个是
**loadClass(String, boolean)**，实现了双亲委派机制，还有一个方法是findClass，默认实现是空
方法，所以我们自定义类加载器主要是重写**findClass**方法。
```java
public class MyClassLoaderTest {
    static class MyClassLoader extends ClassLoader {
        private String classPath;

        public MyClassLoader(String classPath) {
            this.classPath = classPath;
        }

        private byte[] loadByte(String name) throws Exception {
            name = name.replaceAll("\\.", "/");
            FileInputStream fis = new FileInputStream(classPath + "/" + name
                                                              + ".class");
            int len = fis.available();
            byte[] data = new byte[len];
            fis.read(data);
            fis.close();
            return data;
        }

        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                byte[] data = loadByte(name);
                //defineClass将一个字节数组转为Class对象，这个字节数组是class文件读取后最终的字节数组。
                return defineClass(name, data, 0, data.length);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ClassNotFoundException();
            }
        }

    }

    public static void main(String args[]) throws Exception {
        //初始化自定义类加载器，会先初始化父类ClassLoader，其中会把自定义类加载器的父加载器设置为应用程序类加载器AppClassLoader
        MyClassLoader classLoader = new MyClassLoader("C:/dev");
        //将User类的复制类User.class丢入该目录
        Class clazz = classLoader.loadClass("com.yinbing.jvm.User");
        Object obj = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("print", null);
        method.invoke(obj, null);
        System.out.println(clazz.getClassLoader().getClass().getName());
    }
}
```

#### 打破双亲委派机制
再来一个沙箱安全机制示例，尝试打破双亲委派机制，用自定义类加载器加载我们自己实现的 org.springframework.util.StringUtils

```java
public class MyClassLoaderTest1 {
    static class MyClassLoader extends ClassLoader {
        private String classPath;

        public MyClassLoader(String classPath) {
            this.classPath = classPath;
        }

        private byte[] loadByte(String name) throws Exception {
            name = name.replaceAll("\\.", "/");
            FileInputStream fis = new FileInputStream(classPath + "/" + name
                                                              + ".class");
            int len = fis.available();
            byte[] data = new byte[len];
            fis.read(data);
            fis.close();
            return data;

        }

        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                byte[] data = loadByte(name);
                return defineClass(name, data, 0, data.length);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ClassNotFoundException();
            }
        }

        /**
         * 重写类加载方法，实现自己的加载逻辑，不委派给双亲加载
         * @param name
         * @param resolve
         * @return
         * @throws ClassNotFoundException
         */
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);

                if(name.equals("org.springframework.util.StringUtils")){

                }else{
                    c = super.loadClass(name, resolve);
                }


                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
    }

    public static void main(String args[]) throws Exception {
        MyClassLoader classLoader = new MyClassLoader("C:/dev");
        //尝试用自己改写类加载机制去加载自己写的java.lang.String.class
        Class clazz = classLoader.loadClass("org.springframework.util.StringUtils");
        Object obj = clazz.newInstance();
        Method method= clazz.getDeclaredMethod("print", null);
        method.invoke(obj, null);
        System.out.println(clazz.getClassLoader().getClass().getName());
    }
}
```

#### Tomcat打破双亲委派机制
以Tomcat类加载为例，Tomcat 如果使用默认的双亲委派类加载机制行不行？
我们思考一下：Tomcat是个web容器， 那么它要解决什么问题： 
1. 一个web容器可能需要部署两个应用程序，**不同的应用程序可能会依赖同一个第三方类库的不同版本**，不能要求同一个类库在同一个服务器只有一份，因此要保证每个应用程序的类库都是独立的，保证相互隔离。 
2. 部署在同一个web容器中相同的类库**相同的版本可以共享**。否则，如果服务器有10个应用程序，那么要有10份相同的类库加载进虚拟机。 
3. **web容器也有自己依赖的类库，不能与应用程序的类库混淆**。基于安全考虑，应该让容器的类库和程序的类库隔离开来。 
4. web容器要支持jsp的修改，我们知道，jsp 文件最终也是要编译成class文件才能在虚拟机中运行，但程序运行后修改jsp已经是司空见惯的事情， web容器需要支持 jsp 修改后不用重启。

再看看我们的问题：**Tomcat 如果使用默认的双亲委派类加载机制行不行？** 
答案是不行的。为什么？
第一个问题，如果使用默认的类加载器机制，那么是无法加载两个相同类库的不同版本的，默认的类加器是不管你是什么版本的，只在乎你的全限定类名，并且只有一份。
第二个问题，默认的类加载器是能够实现的，因为他的职责就是保证唯一性。
第三个问题和第一个问题一样。
我们再看第四个问题，我们想我们要怎么实现jsp文件的热加载，jsp 文件其实也就是class文件，那么如果修改了，但类名还是一样，类加载器会直接取方法区中已经存在的，修改后的jsp是不会重新加载的。那么怎么办呢？我们可以直接卸载掉这jsp文件的类加载器，所以你应该想到了，每个jsp文件对应一个唯一的类加载器，当一个jsp文件修改了，就直接卸载这个jsp类加载器。重新创建类加载器，重新加载jsp文件。

![image.png](https://upload-images.jianshu.io/upload_images/23805140-70fd2977a0df86b8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

tomcat的几个主要类加载器：
* commonLoader：Tomcat最基本的类加载器，加载路径中的class可以被Tomcat容器本身以及各个Webapp访问；
* catalinaLoader：Tomcat容器私有的类加载器，加载路径中的class对于Webapp不可见；
* sharedLoader：各个Webapp共享的类加载器，加载路径中的class对于所有Webapp可见，但是对于Tomcat容器不可见；
* WebappClassLoader：各个Webapp私有的类加载器，加载路径中的class只对当前Webapp可见，比如加载war包里相关的类，每个war包应用都有自己的WebappClassLoader，实现相互隔离，比如不同war包应用引入了不同的spring
版本，这样实现就能加载各自的spring版本；

从图中的委派关系中可以看出：
CommonClassLoader能加载的类都可以被CatalinaClassLoader和SharedClassLoader使用，从而实现了公有类库的共用，而CatalinaClassLoader和SharedClassLoader自己能加载的类则与对方相互隔离。
WebAppClassLoader可以使用SharedClassLoader加载到的类，但各个WebAppClassLoader实例之间相互隔离。
而JasperLoader的加载范围仅仅是这个JSP文件所编译出来的那一个.Class文件，它出现的目的就是为了被丢弃：当Web容器检测到JSP文件被修改时，会替换掉目前的JasperLoader的实例，并通过再建立一个新的Jsp类加载器来实现JSP文件的热加载功能。

tomcat 这种类加载机制违背了java 推荐的双亲委派模型了吗？答案是：违背了。 
很显然，tomcat 不是这样实现，**tomcat 为了实现隔离性，没有遵守这个约定，每个webappClassLoader加载自己的目录下的class文件，不会传递给父类加载器，打破了双亲委派机制。**