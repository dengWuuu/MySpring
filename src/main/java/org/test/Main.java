package org.test;

import lombok.extern.slf4j.Slf4j;
import org.spring.context.ApplicationContext;
import org.spring.context.ClassPathXmlApplicationContext;
import org.test.service.HelloWorldService;
import org.test.service.WrapService;


/**
 * 测试类
 */
@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml");
        log.info("初始程序上下文完成");
        WrapService wrapService = (WrapService) applicationContext.getBean("wrapService");
        wrapService.say();
        HelloWorldService helloWorldService = (HelloWorldService) applicationContext.getBean("helloWorldService");
        HelloWorldService helloWorldService2 = (HelloWorldService) applicationContext.getBean("helloWorldService");
        System.out.println("prototype验证：" + (helloWorldService == helloWorldService2));
        WrapService wrapService2 = (WrapService) applicationContext.getBean("wrapService");
        System.out.println("singleton验证：" + (wrapService == wrapService2));
    }

}