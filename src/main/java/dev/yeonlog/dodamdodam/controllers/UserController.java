package dev.yeonlog.dodamdodam.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    @RequestMapping(value = "/login")
    public String getLogin() {
        return "user/login";
    }

    @RequestMapping(value = "/register")
    public String getRegister() {
        return "user/register";
    }
}
