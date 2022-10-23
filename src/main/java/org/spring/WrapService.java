package org.spring;

public class WrapService {
    private HelloWorldService helloWorldService;

    public void say() {
        helloWorldService.saySomething();
    }
}