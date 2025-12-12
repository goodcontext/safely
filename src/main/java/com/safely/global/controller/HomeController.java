package com.safely.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "HOME!";
    }

    @GetMapping("/health")
    public String hello() { return "UP!"; }
}
