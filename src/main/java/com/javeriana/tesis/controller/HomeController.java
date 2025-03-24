package com.javeriana.tesis.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/")
public class HomeController{
    @GetMapping("")
    public String getMethodName() {
        return new String("Hello World");
    }
    
}
