package org.spring;

public class HelloWorldServiceImpl implements HelloWorldService {
    private String text;
    @Override
    public void saySomething() {
        System.out.println(text);
    }
}