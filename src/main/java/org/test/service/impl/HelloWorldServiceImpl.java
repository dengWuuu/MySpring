package org.test.service.impl;

import org.spring.annotation.Component;
import org.spring.annotation.Scope;
import org.spring.annotation.Value;
import org.test.service.HelloWorldService;

@Component(name = "helloWorldService")
@Scope("prototype")
public class HelloWorldServiceImpl implements HelloWorldService {

    @Value("跑起来了注解？")
    private String text;
    @Override
    public void saySomething() {
        System.out.println(text);
    }
}