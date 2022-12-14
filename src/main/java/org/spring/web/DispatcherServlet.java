package org.spring.web;

import lombok.extern.slf4j.Slf4j;
import org.spring.annotation.Controller;
import org.spring.annotation.RequestMapping;
import org.spring.context.ClassPathXmlApplicationContext;
import org.spring.entity.BeanDefinition;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author Wu
 * @date 2022年10月26日 19:10
 */

/*
用户发送请求至前端控制器 DispatcherServlet。
DispatcherServlet 收到请求调用 HandlerMapping 处理器映射器。
处理器映射器根据请求url找到具体的处理器，生成处理器对象及处理器拦截器(如果有则生成)一并返回给 DispatcherServlet。
DispatcherServlet 通过 HandlerAdapter 处理器适配器调用处理器。
执行处理器（Controller，也叫后端控制器）。
Controller 执行完成返回 ModelAndView。
HandlerAdapter 将 controller 执行结果 ModelAndView 返回给 DispatcherServlet。
DispatcherServlet 将 ModelAndView 传给 ViewReslover 视图解析器。
ViewReslover 解析后返回具体 View。
DispatcherServlet 对 View 进行渲染视图（即将模型数据填充至视图中）。
DispatcherServlet 响应用户。
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

    private final Properties properties = new Properties();

    private final List<String> classNames = new ArrayList<>();

    /**
     * url对应的方法
     */
    private final Map<String, Method> handlerMapping = new HashMap<>();

    private final HashSet<Class<?>> classes = new HashSet<>();

    /**
     * url对应的controller
     */
    private final Map<String, Object> controllerMap = new HashMap<>();

    private ClassPathXmlApplicationContext xmlApplicationContext;

    @Override
    public void init(ServletConfig config) {
        try {
            xmlApplicationContext = new ClassPathXmlApplicationContext("application.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        doScanner(properties.getProperty("scanPackage"));
        doInstance();
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            //处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    public void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (handlerMapping.isEmpty()) return;
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(url)) {
            response.getWriter().write("404 NOT FOUND!");
            return;
        }
        //获取到那个方法
        Method method = handlerMapping.get(url);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String requestParam = parameterTypes[i].getSimpleName();
            if (requestParam.equals("HttpServletRequest")) {
                paramValues[i] = request;
                continue;
            }
            if (requestParam.equals("HttpServletResponse")) {
                paramValues[i] = response;
                continue;
            }
            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("[\\[\\]]", "").replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        try {
            method.invoke(controllerMap.get(url), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载web环境
     *
     * @param location 路径
     */
    private void doLoadConfig(String location) {
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);

        try {
            //用Properties文件加载文件里的内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            log.info("加载web.xml文件失败");
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查询所有的class文件目的看有没有controller注解
     *
     * @param packageName
     */
    private void doScanner(String packageName) {
        //把所有的.替换成/
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        assert url != null;
        File dir = new File(url.getFile());
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                //递归读取包
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 实例Controller
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                //交给Bean工厂来生成Controller单例
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    classes.add(clazz);
                    BeanDefinition definition = new BeanDefinition();
                    definition.setSingleton(true);
                    definition.setBeanClassName(clazz.getName());

                    xmlApplicationContext.addNewBeanDefinition(clazz.getName(), definition);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.debug("让我看看有什么Controller{}", classes);
        try {
            //工厂干活把新的bean弄出来
            xmlApplicationContext.refreshBeanFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 弄出url对应的方法
     * 和url对应的controller
     */
    private void initHandlerMapping() {
        if (classes.isEmpty()){
            log.debug("没有controller");
            return;
        }
        try {
            for (Class<?> clazz : classes) {
                String baseUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    baseUrl = clazz.getAnnotation(RequestMapping.class).value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) continue;
                    String url = method.getAnnotation(RequestMapping.class).value();
                    url = (baseUrl + "/" + url).replaceAll("/+", "/");
                    handlerMapping.put(url, method);
                    controllerMap.put(url, xmlApplicationContext.getBean(clazz));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}