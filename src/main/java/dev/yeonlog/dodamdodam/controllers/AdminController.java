package dev.yeonlog.dodamdodam.controllers;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String adminPage(@RequestParam(defaultValue = "dashboard") String menu, Model model) {
        model.addAttribute("currentMenu", menu);
        return "admin/admin-page";
    }
}
