package org.spring;

import org.spring.annotation.Component;
import org.spring.annotation.Scope;
import org.spring.annotation.Value;

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