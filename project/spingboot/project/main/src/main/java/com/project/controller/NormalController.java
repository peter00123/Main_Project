package com.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

@Controller
public class NormalController {

    @GetMapping("/")
    public String landing() {
		return "404";
	}
}