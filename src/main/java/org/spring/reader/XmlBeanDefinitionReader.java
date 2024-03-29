package org.spring.reader;

import lombok.extern.slf4j.Slf4j;
import org.spring.annotation.*;
import org.spring.entity.BeanDefinition;
import org.spring.entity.BeanReference;
import org.spring.entity.PropertyValue;
import org.spring.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 借鉴的
 * XML配置文件形式的Bean定义读取类
 *
 * @author Wu
 */
//TODO 用委托模式解决重复代码
@Slf4j
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    public XmlBeanDefinitionReader(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    @Override
    public void loadBeanDefinitions(String location) throws Exception {
        InputStream inputStream = getResourceLoader().getResource(location).getInputStream();
        doLoadBeanDefinitions(inputStream);
    }

    protected void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        // 解析xml document并注册bean
        registerBeanDefinitions(document);
        inputStream.close();
    }

    public void registerBeanDefinitions(Document document) {
        Element root = document.getDocumentElement();
        // 从文件根递归解析
        parseBeanDefinitions(root);
    }

    protected void parseBeanDefinitions(Element root) {
        NodeList nodeList = root.getChildNodes();
        // 确定是否注解配置
        String basePackage = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                Element ele = (Element) nodeList.item(i);
                if (ele.getTagName().equals("component-scan")) {
                    basePackage = ele.getAttribute("base-package");
                    break;
                }
            }
        }
        if (basePackage != null) {
            parseAnnotation(basePackage);
            return;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                processBeanDefinition((Element) node);
            }
        }
    }

    protected void processBeanDefinition(Element ele) {
        String name = ele.getAttribute("id");
        String className = ele.getAttribute("class");
        boolean singleton = !ele.hasAttribute("scope") || !"prototype".equals(ele.getAttribute("scope"));
        BeanDefinition beanDefinition = new BeanDefinition();
        processProperty(ele, beanDefinition);
        beanDefinition.setBeanClassName(className);
        beanDefinition.setSingleton(singleton);
        if (!className.isBlank()) {
            try {
                Class<?> beanClass = Class.forName(className);
                beanDefinition.setBeanClass(beanClass);
            } catch (ClassNotFoundException e) {
                log.error("can't find the class");
                throw new RuntimeException(e);
            }
            log.debug("BeanDefinition信息从XML取出:{}", beanDefinition);
            getRegistry().put(name, beanDefinition);
        }
    }

    private void processProperty(Element ele, BeanDefinition beanDefinition) {
        NodeList propertyNode = ele.getElementsByTagName("property");
        for (int i = 0; i < propertyNode.getLength(); i++) {
            Node node = propertyNode.item(i);
            if (node instanceof Element propertyEle) {
                String name = propertyEle.getAttribute("name");
                String value = propertyEle.getAttribute("value");
                if (beanDefinition.getPropertyValues() == null) {
                    beanDefinition.setPropertyValues(new ArrayList<>());
                }
                if (value.length() > 0) {
                    // 优先进行值注入
                    beanDefinition.getPropertyValues().add(new PropertyValue(name, value));
                } else {
                    String ref = propertyEle.getAttribute("ref");
                    if (ref.length() == 0) {
                        log.error("property为空 建议自杀");
                        throw new IllegalArgumentException("Configuration problem: <property> element for property" + name + "' must specify a ref or value");
                    }
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().add(new PropertyValue(name, beanReference));
                }
            }
        }
    }

    protected void parseAnnotation(String basePackage) {
        Set<Class<?>> classes = getClasses(basePackage);
        for (Class<?> clazz : classes) {
            processAnnotationBeanDefinition(clazz);
        }
    }

    //粗略只定义了Component ,因此只需要搜索Component
    protected void processAnnotationBeanDefinition(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            String name = clazz.getAnnotation(Component.class).name();
            if (name == null || name.length() == 0) {
                name = clazz.getName();
            }
            String className = clazz.getName();
            boolean singleton = !clazz.isAnnotationPresent(Scope.class) || !"prototype".equals(clazz.getAnnotation(Scope.class).value());
            BeanDefinition beanDefinition = new BeanDefinition();
            processAnnotationProperty(clazz, beanDefinition);
            beanDefinition.setBeanClassName(className);
            beanDefinition.setSingleton(singleton);

            log.debug("BeanDefinition信息从XML取出:{}", beanDefinition);
            getRegistry().put(name, beanDefinition);
        }
    }

    public static void processAnnotationProperty(Class<?> clazz, BeanDefinition beanDefinition) {
        Field[] fields = clazz.getDeclaredFields();
        if (beanDefinition.getPropertyValues() == null) {
            beanDefinition.setPropertyValues(new ArrayList<>());
        }
        for (Field field : fields) {
            String name = field.getName();
            if (field.isAnnotationPresent(Value.class)) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                String value = valueAnnotation.value();
                if (value != null && value.length() > 0) {
                    // 优先进行值注入
                    beanDefinition.getPropertyValues().add(new PropertyValue(name, value));
                }
            } else if (field.isAnnotationPresent(Autowired.class)) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    String ref = qualifier.value();
                    if (ref == null || ref.length() == 0) {
                        throw new IllegalArgumentException("the value of Qualifier should not be null!");
                    }
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().add(new PropertyValue(name, beanReference));
                } else {
                    String ref = field.getName();
                    log.debug("ref信息{}", ref);
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().add(new PropertyValue(name, beanReference));
                }
            }
        }
    }

    /**
     * 下面的方法是为了递归查找所有文件里面哪里有注解的 cv大法的
     *
     * @param packageName
     * @return
     */
    protected Set<Class<?>> getClasses(String packageName) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        boolean recursive = true;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                // 如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    // 去掉后面的".class" 获取真正的类名
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        // 添加到classes
                                        classes.add(Class.forName(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    private void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        // 循环所有文件
        assert dirFiles != null;
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }

}