package org.test.controller;

import org.spring.annotation.Autowired;
import org.spring.annotation.Controller;
import org.spring.annotation.RequestMapping;
import org.spring.annotation.RequestParam;
import org.test.service.HelloWorldService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private HelloWorldService helloWorldService;

    @RequestMapping("/test1")
    public void test1(HttpServletRequest request, HttpServletResponse response, @RequestParam("param") String param) {
        try {
            String text = helloWorldService.getString();
            response.getWriter().write(text + " and the param is " + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}