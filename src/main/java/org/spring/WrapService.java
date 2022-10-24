package org.spring;


import org.spring.annotation.Autowired;
import org.spring.annotation.Component;

@Component(name = "wrapService")
public class WrapService {
    @Autowired
    private HelloWorldService helloWorldService;

    public void say() {
        helloWorldService.saySomething();
    }
}