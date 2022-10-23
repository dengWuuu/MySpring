package org.spring;

import org.spring.context.ApplicationContext;
import org.spring.context.ClassPathXmlApplicationContext;


/**
 * 测试类
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml");
        WrapService wrapService = (WrapService) applicationContext.getBean("wrapService");
        wrapService.say();
        HelloWorldService helloWorldService = (HelloWorldService) applicationContext.getBean("helloWorldService");
        HelloWorldService helloWorldService2 = (HelloWorldService) applicationContext.getBean("helloWorldService");
        System.out.println("prototype验证：" + (helloWorldService == helloWorldService2));
        WrapService wrapService2 = (WrapService) applicationContext.getBean("wrapService");
        System.out.println("singleton验证：" + (wrapService == wrapService2));
    }

}