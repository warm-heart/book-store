package com.book.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangqianlong
 * @create 2019-08-13 16:50
 */
@RestController
public class indexController {

    @GetMapping("hello")
    public String hello(){
        return "hello Spring Boot";
    }

}
